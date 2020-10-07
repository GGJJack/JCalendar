package com.hstudio.jcalendarview

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import android.view.ViewTreeObserver
import android.graphics.Canvas
import android.graphics.Paint
import java.util.*
import kotlin.collections.HashMap


class JCalendarView : ConstraintLayout {
    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

    private var adapter: JCalendarAdapter<out JCalendarViewHolder, out JCalendarViewHolder>? = null
    private var _fullHeight = 0
    private var _collapseHeight = 0
    private var headerHeight: Int? = null
    private var cellHeight: Int? = null
    private var lineMap = HashMap<JCalendarLine, Paint?>()

    init {
        inflateViews()
    }

    private fun inflateViews() {
        this.removeAllViews()
        this.adapter?.let { adapter ->
            val layoutInflater = LayoutInflater.from(context)
            // (1(weekTitle) + 7(Week width)) * 6(height Month)
            for (week in 0 until adapter.maxGridHeight) {
                for (day in 0 until adapter.maxGridWidth) {
                    val viewHolder = (if (week == 0) adapter.onCreateHeaderView(layoutInflater, this)
                    else adapter.onCreateView(layoutInflater, this, adapter.getViewType(week, day))) as JCalendarViewHolder
                    if (viewHolder.view.id == View.NO_ID) viewHolder.view.id = Util.makeViewId()
                    this.addView(
                        viewHolder.view,
                        LayoutParams(ConstraintSet.MATCH_CONSTRAINT, if (week == 0) viewHolder.view.layoutParams.height else ConstraintSet.MATCH_CONSTRAINT)
                    )
                    adapter.gridData[week][day] = viewHolder
                    viewHolder.view.setOnClickListener {
                        clickViewHolder(day, week, viewHolder)
                        if (lineMap.containsKey(JCalendarLine.FOCUS_BODY)) invalidate()
                    }
                    viewHolder.view.visibility = View.VISIBLE
                }
            }
            val set = ConstraintSet()
            set.clone(this)
            for (week in 0 until adapter.maxGridHeight) {
                for (day in 0 until adapter.maxGridWidth) {
                    magnetViews(day, week, adapter.gridData[week][day]!!, set)
                }
            }
            set.applyTo(this)
            refresh()
        }
    }

    override fun dispatchDraw(canvas: Canvas?) {
        super.dispatchDraw(canvas)
        val viewCanvas = canvas ?: return
        val headerViewHeight = getHeaderViewHeight()?.toFloat() ?: return
        lineMap.iterator().forEach { data ->
            val key = data.key
            data.value?.let { value ->
                val strokeWidth = value.strokeWidth
                when (key) {
                    JCalendarLine.HEADER_LEFT -> viewCanvas.drawLine(0f, 0f, 0f, headerViewHeight, value)
                    JCalendarLine.HEADER_TOP -> viewCanvas.drawLine(0f, 0f, width.toFloat(), 0f, value)
                    JCalendarLine.HEADER_RIGHT -> viewCanvas.drawLine(width.toFloat() - strokeWidth, 0f, width.toFloat() - strokeWidth, headerViewHeight, value)
                    JCalendarLine.HEADER_BOTTOM -> viewCanvas.drawLine(0f, headerViewHeight - strokeWidth, width.toFloat(), headerViewHeight - strokeWidth, value)
                    JCalendarLine.HEADER_SPLIT -> {
                        this.adapter?.let { adapter ->
                            for (day in 1 until adapter.maxGridWidth) {
                                val newX = width.toFloat() / adapter.maxGridWidth.toFloat() * day.toFloat()
                                viewCanvas.drawLine(newX, 0f, newX, headerViewHeight, value)
                            }
                        }
                    }
                    JCalendarLine.BODY_LEFT -> viewCanvas.drawLine(0f, headerViewHeight, 0f, height.toFloat(), value)
                    JCalendarLine.BODY_TOP -> viewCanvas.drawLine(0f, headerViewHeight, width.toFloat(), headerViewHeight, value)
                    JCalendarLine.BODY_RIGHT -> viewCanvas.drawLine(width.toFloat() - strokeWidth, headerViewHeight, width.toFloat() - strokeWidth, height.toFloat(), value)
                    JCalendarLine.BODY_BOTTOM -> viewCanvas.drawLine(0f, height.toFloat() - strokeWidth, width.toFloat(), height.toFloat() - strokeWidth, value)
                    JCalendarLine.BODY_SPLIT_HORIZONTAL -> {
                        this.adapter?.let { adapter ->
                            for (week in 1 until adapter.maxGridHeight) {
                                val heightSize = (height.toFloat() - headerViewHeight) / (adapter.maxGridHeight - 1).toFloat()
                                val newYStart = heightSize * (week - 1) + headerViewHeight
                                val newYEnd = heightSize * (week) + headerViewHeight
                                for (day in 1 until adapter.maxGridWidth) {
                                    val newX = width.toFloat() / adapter.maxGridWidth.toFloat() * day.toFloat()
                                    viewCanvas.drawLine(newX, newYStart, newX, newYEnd, value)
                                }
                            }
                        }
                    }
                    JCalendarLine.BODY_SPLIT_VERTICAL -> {
                        this.adapter?.let { adapter ->
                            for (week in 1 until adapter.maxGridHeight - 1) {
                                val heightSize = (height.toFloat() - headerViewHeight) / (adapter.maxGridHeight - 1).toFloat()
                                val newY = heightSize * (week) + headerViewHeight
                                for (day in 0 until adapter.maxGridWidth) {
                                    val newXStart = width.toFloat() / adapter.maxGridWidth.toFloat() * (day).toFloat()
                                    val newXEnd = width.toFloat() / adapter.maxGridWidth.toFloat() * (day + 1).toFloat()
                                    viewCanvas.drawLine(newXStart, newY, newXEnd, newY, value)
                                }
                            }
                        }
                    }
                    JCalendarLine.FOCUS_BODY -> {
                    }
                }
            }
        }
        if (lineMap.containsKey(JCalendarLine.FOCUS_BODY)) {
            lineMap[JCalendarLine.FOCUS_BODY]?.let { value ->
                this.adapter?.let { adapter ->
                    adapter.lastFocusPosition?.let { lastPosition ->
                        val x = lastPosition.first
                        val y = lastPosition.second
                        val heightSize = (height.toFloat() - headerViewHeight) / (adapter.maxGridHeight - 1).toFloat()
                        val newYStart = heightSize * (y - 1) + headerViewHeight
                        val newYEnd = heightSize * (y) + headerViewHeight
                        val newXStart = width.toFloat() / adapter.maxGridWidth.toFloat() * (x).toFloat()
                        val newXEnd = width.toFloat() / adapter.maxGridWidth.toFloat() * (x + 1).toFloat()
                        viewCanvas.drawLine(newXStart, newYStart, newXStart, newYEnd, value)
                        viewCanvas.drawLine(newXStart, newYStart, newXEnd, newYStart, value)
                        viewCanvas.drawLine(newXEnd, newYStart, newXEnd, newYEnd, value)
                        viewCanvas.drawLine(newXStart, newYEnd, newXEnd, newYEnd, value)
                    }
                }
            }
        }
    }

    fun clickViewHolder(x: Int, y: Int, viewHolder: JCalendarViewHolder) {
        adapter?.let {
            it._changeFocus(x, y, viewHolder)
        }
    }

    fun refresh() {
        adapter?.notifyMonthChanged()
    }

    private fun adapterRefresh() {
        adapter?.clearFocus()
    }

    private fun invalidViews() {
        this.invalidate()
    }

    fun setLinePaint(field: JCalendarLine, paint: Paint) {
        lineMap[field] = paint
        invalidate()
    }

    fun setLineMap(map: HashMap<JCalendarLine, Paint?>) {
        map.iterator().forEach {
            this.lineMap[it.key] = it.value
        }
        invalidate()
    }

    private fun adapterChanged(newAdapter: JCalendarAdapter<out JCalendarViewHolder, out JCalendarViewHolder>?) {
        this.adapter = newAdapter
        newAdapter?.refreshCallback = this::adapterRefresh
        newAdapter?.invalidCallback = this::invalidViews
        inflateViews()
    }

    private fun magnetViews(x: Int, y: Int, viewHolder: JCalendarViewHolder, set: ConstraintSet) {
        val view = viewHolder.view
        adapter?.let { adapter ->
            when (x) {
                0 -> {
                    set.connect(view.id, ConstraintSet.LEFT, this.id, ConstraintSet.LEFT)
                }
                adapter.maxGridWidth - 1 -> {
                    val leftView = adapter.gridData[y][x - 1]!!.view
                    set.connect(view.id, ConstraintSet.LEFT, leftView.id, ConstraintSet.RIGHT)
                    set.connect(leftView.id, ConstraintSet.RIGHT, view.id, ConstraintSet.LEFT)
                    set.connect(view.id, ConstraintSet.RIGHT, this.id, ConstraintSet.RIGHT)
                }
                else -> {
                    val leftView = adapter.gridData[y][x - 1]!!.view
                    set.connect(leftView.id, ConstraintSet.RIGHT, view.id, ConstraintSet.LEFT)
                    set.connect(view.id, ConstraintSet.LEFT, leftView.id, ConstraintSet.RIGHT)
                }
            }
            when (y) {
                0 -> {
                    set.connect(view.id, ConstraintSet.TOP, ConstraintSet.PARENT_ID, ConstraintSet.TOP)
                }
                adapter.maxGridHeight - 1 -> {
                    val topView = adapter.gridData[y - 1][x]!!.view
                    set.connect(view.id, ConstraintSet.TOP, topView.id, ConstraintSet.BOTTOM)
                    set.connect(topView.id, ConstraintSet.BOTTOM, view.id, ConstraintSet.TOP)
                    set.connect(view.id, ConstraintSet.BOTTOM, ConstraintSet.PARENT_ID, ConstraintSet.BOTTOM)
                }
                else -> {
                    val topView = adapter.gridData[y - 1][x]!!.view
                    set.connect(topView.id, ConstraintSet.BOTTOM, view.id, ConstraintSet.TOP)
                    set.connect(view.id, ConstraintSet.TOP, topView.id, ConstraintSet.BOTTOM)
                }
            }
            //if (y != 0) set.constrainDefaultHeight(view.id, ConstraintSet.MATCH_CONSTRAINT_SPREAD)
            set.constrainDefaultWidth(view.id, ConstraintSet.MATCH_CONSTRAINT)
        }
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        //JLog.i("HJ", "width : $w, height : $h")
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        this.viewTreeObserver.addOnGlobalLayoutListener(object : ViewTreeObserver.OnGlobalLayoutListener {
            override fun onGlobalLayout() {
                //JLog.i("HJ", "width : $width, height : $height")
                calculateHeights(height)
                this@JCalendarView.viewTreeObserver.removeOnGlobalLayoutListener(this)
            }
        })
    }

    private fun calculateHeights(fullHeight: Int) {
        _fullHeight = fullHeight
        _collapseHeight = fullHeight - fullHeight / 3
    }

    fun getHeaderViewHeight(): Int? {
        if (headerHeight == null) headerHeight = adapter?.gridData?.get(0)?.get(0)?.view?.height
        return headerHeight
    }

    fun getFirstRowHeight(): Int? {
        if (cellHeight == null) cellHeight = adapter?.gridData?.get(1)?.get(0)?.view?.height
        return cellHeight
    }
    
    fun setAdapter(adapter: JCalendarAdapter<out JCalendarViewHolder, out JCalendarViewHolder>) {
        adapterChanged(adapter)
    }
    
    fun getAdapter(): JCalendarAdapter<out JCalendarViewHolder, out JCalendarViewHolder>? = this.adapter
}