package com.hstudio.jcalendarview

import android.graphics.Paint
import android.util.SparseArray
import android.view.View
import android.view.ViewGroup
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import androidx.core.util.valueIterator
import androidx.viewpager.widget.PagerAdapter
import java.util.*

internal class JCalenderPagerAdapter<adapter : JCalendarAdapter<*, *>>(private val adapterClass: Class<adapter>) : PagerAdapter() {
    internal val maxInt = Int.MAX_VALUE - 1
    internal var centerValue = maxInt / 2
    internal var targetDate: Date = Date()
    internal val maxViewCount = 3
    internal var animateDuration: Int = 300

    internal var lastPosition: Int = centerValue
    internal var viewIdMap = SparseArray<JCalendarView>()
    internal var visibleType: VisibleType = VisibleType.FULL

    internal var calendarUnit = Calendar.MONTH
    private val lineMap = HashMap<JCalendarLine, Paint?>()

    override fun instantiateItem(container: ViewGroup, position: Int): Any {
        val calendarView = JCalendarView(container.context)
        calendarView.id = Util.makeViewId()
        calendarView.setLineMap(lineMap)
        calendarView.setVisibleType(visibleType)
        val calendar = Calendar.getInstance()
        val targetDate = Calendar.getInstance()
        val adapter = adapterClass.newInstance().apply {
            val gap = lastPosition - centerValue
            calendar.add(calendarUnit, gap)
            targetDate.add(calendarUnit, gap)
            if (position == lastPosition) {
                this.setTargetDate(calendar.time)
            } else if (position == lastPosition - 1) {
                calendar.add(calendarUnit, -1)
                this.setTargetDate(calendar.time)
            } else if (position == lastPosition + 1) {
                calendar.add(calendarUnit, 1)
                this.setTargetDate(calendar.time)
            }
        }
        calendarView.adapter = adapter
        JLog.e("HJ", "position : $position / Date : ${calendarView.adapter!!.targetDate.toLocaleString()}")
        calendarView.animateDuration = this.animateDuration
        calendarView.layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
        container.addView(calendarView)
        viewIdMap.put(position, calendarView)
        attachListener?.let {
            if (lastPosition - 1 <= position && position <= lastPosition + 1) it.adapterAttached(calendar.time, targetDate.time, adapter)
        }
        return calendarView
    }

    fun currentAdapter(): adapter? {
        val adapter = viewIdMap[lastPosition]?.adapter as? adapter
        adapter?.setOnClearFocus {
            if (lineMap.isNotEmpty()) {
                viewIdMap[lastPosition]?.invalidate()
                viewIdMap[lastPosition - 1]?.invalidate()
                viewIdMap[lastPosition + 1]?.invalidate()
            }
        }
        return adapter
    }

    fun prevAdapter(): adapter? {
        return viewIdMap[lastPosition - 1]?.adapter as? adapter
    }

    fun nextAdapter(): adapter? {
        return viewIdMap[lastPosition + 1]?.adapter as? adapter
    }

    private val isMinimize get() = visibleType == VisibleType.MINIMIZE

    fun changePosition(position: Int): Date {
        lastPosition = position
        val gap = lastPosition - centerValue
        val calendar = Calendar.getInstance()
        JLog.i("HJ", "[$position]calendar Time($gap) : ${calendar.time.toLocaleString()}")
        calendar.add(calendarUnit, gap)
        targetDate = calendar.time
        JLog.i("HJ", "\tresult : ${targetDate.toLocaleString()}")
        currentAdapter()?.setTargetDate(targetDate)
        JLog.i("HJ", "toDay(${lastPosition}) : ${targetDate.toLocaleString()}")
        calendar.add(calendarUnit, -1)
        prevAdapter()?.setTargetDate(calendar.time)
        JLog.i("HJ", "YesterDay(${lastPosition - 1}) : ${calendar.time.toLocaleString()}")
        calendar.add(calendarUnit, 2)
        JLog.i("HJ", "Tomorrow(${lastPosition + 1}) : ${calendar.time.toLocaleString()}")
        nextAdapter()?.setTargetDate(calendar.time)
        prevAdapter()?.notifyMonthChanged()
        nextAdapter()?.notifyMonthChanged()
        if (lineMap.isNotEmpty()) {
            viewIdMap[lastPosition]?.invalidate()
            viewIdMap[lastPosition - 1]?.invalidate()
            viewIdMap[lastPosition + 1]?.invalidate()
        }
//        val move = position - lastPosition
//        if (move == 1) {
//            prevAdapter()?.notifyMonthChanged()
//        } else if (move == -1) {
//            nextAdapter()?.notifyMonthChanged()
//        }
        return targetDate
    }

    fun changeCenterPosition(position: Int): Date {
        centerValue = position
        val date = changePosition(position)
        currentAdapter()?.notifyMonthChanged()
        return date
    }

    fun setVisibleType(visibleType: VisibleType) {
        when (visibleType) {
            VisibleType.FULL, VisibleType.COLLAPSE -> {
                calendarUnit = Calendar.MONTH
                if (this.visibleType == VisibleType.MINIMIZE) {
                    val gap = lastPosition - centerValue
                    val calendar = Calendar.getInstance()
                    calendar.add(Calendar.MONTH, gap)
                    val curDate = calendar.time
                    prevAdapter()?.let {
                        calendar.time = curDate
                        calendar.add(calendarUnit, -1)
                        it.setTargetDate(calendar.time)
                        it.notifyMonthChanged()
                        JLog.i("HJ", "YesterDay(${lastPosition - 1}) : ${calendar.time.toLocaleString()}")
                    }
                    nextAdapter()?.let {
                        calendar.time = curDate
                        calendar.add(calendarUnit, 1)
                        it.setTargetDate(calendar.time)
                        it.notifyMonthChanged()
                        JLog.i("HJ", "Tomorrow(${lastPosition + 1}) : ${calendar.time.toLocaleString()}")
                    }
                }
            }
            VisibleType.MINIMIZE -> {
                calendarUnit = Calendar.WEEK_OF_MONTH
                if (this.visibleType != VisibleType.MINIMIZE) {
                    val gap = lastPosition - centerValue
                    val calendar = Calendar.getInstance()
                    calendar.add(Calendar.MONTH, gap)
                    val curDate = calendar.time
                    prevAdapter()?.let {
                        calendar.time = curDate
                        calendar.add(calendarUnit, -1)
                        it.setTargetDate(calendar.time)
                        it.notifyMonthChanged()
                        JLog.i("HJ", "YesterDay(${lastPosition - 1}) : ${calendar.time.toLocaleString()}")
                    }
                    nextAdapter()?.let {
                        calendar.time = curDate
                        calendar.add(calendarUnit, 2)
                        it.setTargetDate(calendar.time)
                        it.notifyMonthChanged()
                        JLog.i("HJ", "Tomorrow(${lastPosition + 1}) : ${calendar.time.toLocaleString()}")
                    }
                }
            }
        }
        this.visibleType = visibleType
    }

    fun currentCalendar(): JCalendarView? = viewIdMap[lastPosition]

    fun allViews(callback: (JCalendarView) -> Unit) {
        viewIdMap.valueIterator().forEach { callback(it) }
    }

    override fun destroyItem(container: ViewGroup, position: Int, objects: Any) {
        viewIdMap.remove(position)
        container.removeView(objects as View)
    }

    override fun isViewFromObject(view: View, objects: Any): Boolean {
        return objects == view
    }

    override fun getCount(): Int {
        return maxInt
    }

    internal fun setLinePaint(field: JCalendarLine, paint: Paint) {
        lineMap[field] = paint
        allViews { it.setLinePaint(field, paint) }
    }

    var attachListener: AdapterAttachedListener? = null

    fun setAdapterAttachedListener(listener: AdapterAttachedListener) {
        attachListener = listener
    }
}