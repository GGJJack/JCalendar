package com.hstudio.jcalendarview

import android.view.LayoutInflater
import android.view.ViewGroup
import java.lang.IllegalArgumentException
import java.util.*

abstract class JCalendarAdapter<ViewHolder : JCalendarViewHolder, HeaderViewHolder : JCalendarViewHolder> {

    private val startOfWeek: Int = Calendar.SUNDAY
    internal var focusDay: Date = Date()
    internal var calendar: Calendar = Calendar.getInstance()
    internal val maxGirdHeight = 6 + 1
    internal val maxGridWidth = 7
    internal val gridData = Array(maxGirdHeight) { Array<JCalendarViewHolder?>(maxGridWidth) { null } }

    var monthChangeListener: MonthChangeListener? = null

    abstract fun onCreateView(layoutInflater: LayoutInflater, parent: ViewGroup, viewType: Int): ViewHolder

    abstract fun onBindViewHolder(holder: ViewHolder, x: Int, y: Int, date: Date)

    @Suppress("UNCHECKED_CAST")
    fun _onBindViewHolder(holder: JCalendarViewHolder, x: Int, y: Int, date: Date) {
        val passHolder = holder as? ViewHolder ?: throw IllegalArgumentException("Invalid type ${holder::class.java.name}")
        this.onBindViewHolder(passHolder, x, y, date)
    }

    abstract fun onCreateHeaderView(layoutInflater: LayoutInflater, parent: ViewGroup): HeaderViewHolder

    abstract fun onBindHeaderViewHolder(holder: HeaderViewHolder, dayOfWeek: Date)

    @Suppress("UNCHECKED_CAST")
    internal fun _onBindHeaderViewHolder(holder: JCalendarViewHolder, dayOfWeek: Date) {
        val passHolder = holder as? HeaderViewHolder ?: throw IllegalArgumentException("Invalid type ${holder::class.java.name}")
        this.onBindHeaderViewHolder(passHolder, dayOfWeek)
    }

    fun getViewType(x: Int, y: Int): Int = 0

    final fun getDate() = focusDay

    final fun setMonth(month: Int) {
        calendar.time = focusDay
        calendar.set(Calendar.MONTH, month)
        focusDay = calendar.time
        refresh()
        monthChangeListener?.monthChanged(this.focusDay)
    }

    final fun nextMonth() {
        calendar.time = focusDay
        calendar.add(Calendar.MONTH, 1)
        focusDay = calendar.time
        refresh()
        monthChangeListener?.monthChanged(this.focusDay)
    }

    final fun beforeMonth() {
        calendar.time = focusDay
        calendar.add(Calendar.MONTH, -1)
        focusDay = calendar.time
        refresh()
        monthChangeListener?.monthChanged(this.focusDay)
    }

    fun notifyMonthChanged() {
        refresh()
    }

    private fun refresh() {
        calendar.set(Calendar.DAY_OF_MONTH, 1)
        val startDay = calendar.get(Calendar.DAY_OF_WEEK) - 1
        calendar.time = focusDay
        for (week in 0 until maxGirdHeight) {
            for (day in 0 until maxGridWidth) {
                val viewHolder = gridData[week][day]!!
                if (week == 0) {
                    calendar.set(Calendar.DAY_OF_WEEK, startOfWeek)
                    calendar.add(Calendar.DAY_OF_MONTH, day)
                    _onBindHeaderViewHolder(viewHolder, calendar.time)
                } else {
                    calendar.time = focusDay
                    calendar.set(Calendar.DAY_OF_MONTH, 1)
                    calendar.add(Calendar.DAY_OF_MONTH, day + (7 * (week - 1)) - startDay)
                    _onBindViewHolder(viewHolder, day, week, calendar.time)
                }
            }
        }
        refreshCallback?.let { it() }
    }

    internal var refreshCallback: (() -> Unit)? = null

    final fun setFocusDate(date: Date) {
        this.focusDay = date
        monthChangeListener?.monthChanged(this.focusDay)
    }

    final fun getFocusDate() = this.focusDay

    @Suppress("UNCHECKED_CAST")
    internal fun _changeFocus(x: Int, y: Int, beforeViewHolder: JCalendarViewHolder?, currentViewHolder: JCalendarViewHolder) {
        val before = beforeViewHolder as? ViewHolder
        val current = currentViewHolder as? ViewHolder ?: throw IllegalArgumentException("Invalid type ${currentViewHolder::class.java.name}")
        before?.lostFocusView()
        current.hasFocusView()
        changeFocus(x, y, before, current)
    }

    fun changeFocus(x: Int, y: Int, beforeViewHolder: ViewHolder?, currentViewHolder: ViewHolder) {
    }
}