package com.hstudio.jcalendarview

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import android.view.ViewTreeObserver
import android.animation.ValueAnimator
import android.graphics.Color
import android.icu.lang.UCharacter.GraphemeClusterBreak.T
import com.hstudio.jcalendarview.JCalendarViewHolder
import java.util.*


class JCalendarView : ConstraintLayout {
    constructor(context: Context?) : super(context)
    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

    private var visibleType: VisibleType = VisibleType.FULL
    private var _adapter: JCalendarAdapter<out JCalendarViewHolder, out JCalendarViewHolder>? = null
    var adapter: JCalendarAdapter<out JCalendarViewHolder, out JCalendarViewHolder>?
        get() = _adapter
        set(value) {
            adapterChanged(value)
        }
    private var _fullHeight = 0
    private var _collapseHeight = 0
    internal var animateDuration: Int = 300
    private var isTurnOnAnimateMode = false
    private var headerHeight: Int? = null
    private var cellHeight: Int? = null
    internal var collapseRatio: Float = 0.6f

    init {
        inflateViews()
    }

    private fun inflateViews() {
        this.removeAllViews()
        this._adapter?.let { adapter ->
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
                    viewHolder.view.setOnClickListener { clickViewHolder(day, week, viewHolder) }
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

    fun clickViewHolder(x: Int, y: Int, viewHolder: JCalendarViewHolder) {
        adapter?.let {
            it._changeFocus(x, y, viewHolder)
        }
    }

    fun refresh() {
        _adapter?.notifyMonthChanged()
    }

    private fun adapterRefresh() {
        adapter?.clearFocus()
    }

    private fun adapterChanged(newAdapter: JCalendarAdapter<out JCalendarViewHolder, out JCalendarViewHolder>?) {
        this._adapter = newAdapter
        newAdapter?.refreshCallback = this::adapterRefresh
        inflateViews()
    }

    private fun magnetViews(x: Int, y: Int, viewHolder: JCalendarViewHolder, set: ConstraintSet) {
        val view = viewHolder.view
        _adapter?.let { adapter ->
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

    fun setVisibleType(visibleType: VisibleType) {
        this.visibleType = visibleType
    }

    var lastRatio = 1f

    fun onStartAnimation(fromRatio: Float, toRatio: Float) {
        JLog.i("HJ", "from : $fromRatio, toRatio : $toRatio")
        if (lastRatio <= 0f) {
            adapter?.let { adapter ->
                val targetWeek = adapter.lastFocusPosition?.second ?: adapter.getXYFromDate(adapter.targetDate)?.second ?: adapter.getViewHolder(0, 1) ?: return
                for (week in 1 until adapter.maxGridHeight) {
                    if (week != targetWeek) {
                        for (day in 0 until adapter.maxGridWidth) {
                            adapter.gridData[week][day]?.let { viewHolder ->
                                viewHolder.view.visibility = View.VISIBLE
                            }
                        }
                    }
                }
            }
        }
    }

    fun onFinishAnimation(fromRatio: Float, toRatio: Float, anotherCellHeight: Int? = null) {
        adapter?.let { adapter ->
            JLog.i("HJ", "[${adapter.targetDate.toLocaleString()}] from : $fromRatio, toRatio : $toRatio / cellHeight : ${this.cellHeight}")
            val cellHeight = this.cellHeight ?: anotherCellHeight ?: return
            val targetWeek = adapter.lastFocusPosition?.second ?: adapter.getXYFromDate(adapter.targetDate)?.second ?: adapter.getViewHolder(0, 1) ?: return
            JLog.i("HJ", "Header Height : ${adapter.gridData[0][0]?.view?.height}")
            for (week in 1 until adapter.maxGridHeight) {
                for (day in 0 until adapter.maxGridWidth) {
                    adapter.gridData[week][day]?.let { viewHolder ->
                        if (week != targetWeek) {
                            if (toRatio <= 0f) {
                                viewHolder.view.visibility = View.GONE
                            } else {
                                if (day == 0) JLog.i("HJ", "$week Cell : ${(cellHeight.toFloat() * toRatio).toInt()}")
                                Util.setViewHeight(viewHolder.view, (cellHeight.toFloat() * toRatio).toInt())
                            }
                        } else {
                            val targetHeight = if (collapseRatio > toRatio) collapseRatio else toRatio
                            if (day == 0) JLog.i("HJ", "$week Cell : ${(cellHeight.toFloat() * targetHeight).toInt()}")
                            Util.setViewHeight(viewHolder.view, (cellHeight.toFloat() * targetHeight).toInt())
                        }
                    }
                }
            }
        }
        lastRatio = toRatio
    }

    fun animateViewHeight(animateRatio: Float) {
        //JLog.i("HJ", "Animating : $animateRatio")
        val cellHeight = this.cellHeight ?: return
        adapter?.let { adapter ->
            val targetWeek = adapter.lastFocusPosition?.second ?: adapter.getXYFromDate(Date())?.second ?: adapter.getViewHolder(0, 1) ?: return
            for (week in 1 until adapter.maxGridHeight) {
                if (animateRatio >= collapseRatio || (animateRatio < collapseRatio && week != targetWeek)) {
                    for (day in 0 until adapter.maxGridWidth) {
                        adapter.gridData[week][day]?.let { viewHolder ->
                            Util.setViewHeight(viewHolder.view, (cellHeight.toFloat() * animateRatio).toInt())
                        }
                    }
                }
            }
        }
        lastRatio = animateRatio
    }

    fun getHeaderViewHeight(): Int? {
        if (headerHeight == null) headerHeight = adapter?.gridData?.get(0)?.get(0)?.view?.height
        return headerHeight
    }

    fun getFirstRowHeight(): Int? {
        if (cellHeight == null) cellHeight = adapter?.gridData?.get(1)?.get(0)?.view?.height
        return cellHeight
    }
}