package com.hstudio.jcalendarview

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import android.os.Build
import android.view.ViewTreeObserver
import java.util.concurrent.atomic.AtomicInteger


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

    private val sNextGeneratedId = lazy { AtomicInteger(1) }

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
                    if (viewHolder.view.id == View.NO_ID) viewHolder.view.id = makeViewId()
                    this.addView(viewHolder.view, LayoutParams(ConstraintSet.MATCH_CONSTRAINT, if (week == 0) viewHolder.view.layoutParams.height else ConstraintSet.MATCH_CONSTRAINT))
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
            if (y != 0) set.constrainDefaultHeight(view.id, ConstraintSet.MATCH_CONSTRAINT_SPREAD)
            set.constrainDefaultWidth(view.id, ConstraintSet.MATCH_CONSTRAINT)
        }
    }

    private fun makeViewId(): Int {
        if (Build.VERSION.SDK_INT < 17) {
            while (true) {
                val result = sNextGeneratedId.value.get()
                // aapt-generated IDs have the high byte nonzero; clamp to the range under that.
                var newValue = result + 1
                if (newValue > 0x00FFFFFF)
                    newValue = 1 // Roll over to 1, not 0.
                if (sNextGeneratedId.value.compareAndSet(result, newValue)) {
                    return result
                }
            }
        } else {
            return View.generateViewId()
        }
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        JLog.i("HJ", "width : $w, height : $h")
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        this.viewTreeObserver.addOnGlobalLayoutListener(object : ViewTreeObserver.OnGlobalLayoutListener {
            override fun onGlobalLayout() {
                JLog.i("HJ", "width : $width, height : $height")
                calculateHeights(height)
                this@JCalendarView.viewTreeObserver.removeOnGlobalLayoutListener(this)
            }
        })
    }

    private fun calculateHeights(fullHeight: Int) {
        _fullHeight = fullHeight
        _collapseHeight = fullHeight / 3
    }

    fun setVisibleType(visibleType: VisibleType) {
        this.visibleType = visibleType
    }

    fun startVisibleTypeAnimate(visibleType: VisibleType) {

    }

    fun minimize() {
    }

    fun collapse() {

    }

    fun expand() {

    }

}