package com.hstudio.jcalendarview

import android.util.SparseArray
import android.view.View
import android.view.ViewGroup
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import androidx.core.util.valueIterator
import androidx.viewpager.widget.PagerAdapter
import java.util.*

internal class JCalenderPagerAdapter<adapter : JCalendarAdapter<*, *>>(private val adapterClass: Class<adapter>) : PagerAdapter() {
    internal val maxInt = 10 //Int.MAX_VALUE - 1
    internal val centerValue = maxInt / 2
    internal var targetDate: Date = Date()
    internal val maxViewCount = 3
    internal var animateDuration: Int = 300

    internal var lastPosition: Int = centerValue
    internal var viewIdMap = SparseArray<JCalendarView>()
    internal var visibleType: VisibleType = VisibleType.FULL

    override fun instantiateItem(container: ViewGroup, position: Int): Any {
        val calendarView = JCalendarView(container.context)
        calendarView.id = Util.makeViewId()
        calendarView.adapter = adapterClass.newInstance().apply {
            val gap = lastPosition - centerValue
            val calendar = Calendar.getInstance()
            calendar.add(Calendar.MONTH, gap)
            if (position == lastPosition) {
                this.setTargetDate(calendar.time)
            } else if (position == lastPosition - 1) {
                calendar.add(Calendar.MONTH, -1)
                this.setTargetDate(calendar.time)
            } else if (position == lastPosition + 1) {
                calendar.add(Calendar.MONTH, 1)
                this.setTargetDate(calendar.time)
            }
        }
        calendarView.animateDuration = this.animateDuration
        calendarView.layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
        container.addView(calendarView)
        viewIdMap.put(position, calendarView)
        return calendarView
    }

    fun currentAdapter(): adapter? {
        return viewIdMap[lastPosition]?.adapter as? adapter
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
        calendar.add(if (isMinimize) Calendar.WEEK_OF_MONTH else Calendar.MONTH, gap)
        targetDate = calendar.time
        JLog.i("HJ", "\tresult : ${targetDate.toLocaleString()}")
        currentAdapter()?.setTargetDate(targetDate)
        JLog.i("HJ", "toDay(${lastPosition}) : ${targetDate.toLocaleString()}")
        calendar.add(if (isMinimize) Calendar.WEEK_OF_MONTH else Calendar.MONTH, -1)
        prevAdapter()?.setTargetDate(calendar.time)
        JLog.i("HJ", "YesterDay(${lastPosition - 1}) : ${calendar.time.toLocaleString()}")
        calendar.add(if (isMinimize) Calendar.WEEK_OF_MONTH else Calendar.MONTH, 2)
        JLog.i("HJ", "Tomorrow(${lastPosition + 1}) : ${calendar.time.toLocaleString()}")
        nextAdapter()?.setTargetDate(calendar.time)
        prevAdapter()?.notifyMonthChanged()
        nextAdapter()?.notifyMonthChanged()
//        val move = position - lastPosition
//        if (move == 1) {
//            prevAdapter()?.notifyMonthChanged()
//        } else if (move == -1) {
//            nextAdapter()?.notifyMonthChanged()
//        }
        return targetDate
    }

    fun setVisibleType(visibleType: VisibleType) {
        when (visibleType) {
            VisibleType.FULL, VisibleType.COLLAPSE -> {
                if (this.visibleType == VisibleType.MINIMIZE) {
                    val gap = lastPosition - centerValue
                    val calendar = Calendar.getInstance()
                    calendar.add(Calendar.MONTH, gap)
                    val curDate = calendar.time
                    prevAdapter()?.let {
                        calendar.time = curDate
                        calendar.add(Calendar.MONTH, -1)
                        it.setTargetDate(calendar.time)
                        it.notifyMonthChanged()
                        JLog.i("HJ", "YesterDay(${lastPosition - 1}) : ${calendar.time.toLocaleString()}")
                    }
                    nextAdapter()?.let {
                        calendar.time = curDate
                        calendar.add(Calendar.MONTH, 1)
                        it.setTargetDate(calendar.time)
                        it.notifyMonthChanged()
                        JLog.i("HJ", "Tomorrow(${lastPosition + 1}) : ${calendar.time.toLocaleString()}")
                    }
                }
            }
            VisibleType.MINIMIZE -> {
                if (this.visibleType != VisibleType.MINIMIZE) {
                    val gap = lastPosition - centerValue
                    val calendar = Calendar.getInstance()
                    calendar.add(Calendar.MONTH, gap)
                    val curDate = calendar.time
                    prevAdapter()?.let {
                        calendar.time = curDate
                        calendar.add(Calendar.WEEK_OF_MONTH, -1)
                        it.setTargetDate(calendar.time)
                        it.notifyMonthChanged()
                        JLog.i("HJ", "YesterDay(${lastPosition - 1}) : ${calendar.time.toLocaleString()}")
                    }
                    nextAdapter()?.let {
                        calendar.time = curDate
                        calendar.add(Calendar.WEEK_OF_MONTH, 1)
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
}