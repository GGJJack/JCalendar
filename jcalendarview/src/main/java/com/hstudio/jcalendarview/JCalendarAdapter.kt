package com.hstudio.jcalendarview

import android.view.LayoutInflater
import android.view.ViewGroup
import java.lang.IllegalArgumentException
import java.util.*

abstract class JCalendarAdapter<ViewHolder : JCalendarViewHolder, HeaderViewHolder : JCalendarViewHolder> {

    private val startOfWeek: Int = Calendar.SUNDAY
    internal var targetDate: Date = Date()
    internal val maxGridHeight = 6 + 1
    internal val maxGridWidth = 7
    internal val gridData = Array(maxGridHeight) { Array<JCalendarViewHolder?>(maxGridWidth) { null } }

    var monthChangeListener: MonthChangeListener? = null
    internal var lastActiveViewHolder: JCalendarViewHolder? = null
    internal var lastFocusPosition: Pair<Int, Int>? = null
    internal var holderMaxHeight: Int? = null

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

    open fun getViewType(x: Int, y: Int): Int = 0

    final fun getDate() = targetDate

    final fun setMonth(month: Int) {
        val calendar = getCalendar(targetDate)
        calendar.set(Calendar.MONTH, month)
        targetDate = calendar.time
        refresh()
        monthChangeListener?.monthChanged(this.targetDate)
    }

    final fun nextMonth() {
        val calendar = getCalendar(targetDate)
        calendar.add(Calendar.MONTH, 1)
        targetDate = calendar.time
        refresh()
        monthChangeListener?.monthChanged(this.targetDate)
    }

    final fun beforeMonth() {
        val calendar = getCalendar(targetDate)
        calendar.add(Calendar.MONTH, -1)
        targetDate = calendar.time
        refresh()
        monthChangeListener?.monthChanged(this.targetDate)
    }

    final fun notifyHeaderViewHolders() {
        val calendar = getCalendar(targetDate)
        calendar.set(Calendar.DAY_OF_MONTH, 1)
        for (day in 0 until maxGridWidth) {
            val viewHolder = gridData[0][day]!!
            calendar.set(Calendar.DAY_OF_WEEK, startOfWeek)
            calendar.add(Calendar.DAY_OF_MONTH, day)
            _onBindHeaderViewHolder(viewHolder, calendar.time)
        }
    }

    final fun notifyMonthChanged() {
        refresh()
    }

    final fun notifyXYItemChanged(x: Int, y: Int): Boolean {
        val viewHolder = getViewHolder(x, y)
        viewHolder?.let { holder ->
            if (y == 0) {
                val calendar = getCalendar(targetDate)
                calendar.set(Calendar.DAY_OF_WEEK, startOfWeek)
                calendar.add(Calendar.DAY_OF_MONTH, x)
                _onBindHeaderViewHolder(holder, calendar.time)
            } else {
                val date = getDateFromXY(x, y) ?: return false
                _onBindViewHolder(holder, x, y, date)
            }
        }
        return viewHolder != null
    }

    final fun notifyDateItemChanged(date: Date): Boolean {
        val point = getXYFromDate(date) ?: return false
        val viewHolder = getViewHolder(point.first, point.second)
        viewHolder?.let { _onBindViewHolder(it, point.first, point.second, date) }
        return viewHolder != null
    }

    final fun getDateFromXY(x: Int, y: Int): Date? {
        val calendar = getCalendar(targetDate)
        calendar.set(Calendar.DAY_OF_MONTH, 1)
        val startDay = calendar.get(Calendar.DAY_OF_WEEK) - 1
        calendar.add(Calendar.DAY_OF_MONTH, x + (7 * (y - 1)) - startDay)
        return calendar.time
    }

    final fun getXYFromDate(date: Date): Pair<Int, Int>? {
        val currentCalendar = getCalendar(targetDate)
        val targetCalendar = getCalendar(date)
        currentCalendar.set(Calendar.DAY_OF_MONTH, 1)
        val startDay = currentCalendar.get(Calendar.DAY_OF_WEEK) - 1
        currentCalendar.add(Calendar.DAY_OF_MONTH, 0 + (7 * (0)) - startDay)
        currentCalendar.set(Calendar.HOUR, 0)
        currentCalendar.set(Calendar.MINUTE, 0)
        currentCalendar.set(Calendar.SECOND, 0)
        currentCalendar.set(Calendar.MILLISECOND, 0)
        val startTime = currentCalendar.time.time
        currentCalendar.set(Calendar.DAY_OF_MONTH, 1)
        currentCalendar.add(Calendar.DAY_OF_MONTH, (maxGridWidth - 1) + (7 * ((maxGridHeight))) - startDay)
        currentCalendar.set(Calendar.HOUR, 23)
        currentCalendar.set(Calendar.MINUTE, 59)
        currentCalendar.set(Calendar.SECOND, 59)
        currentCalendar.set(Calendar.MILLISECOND, 999)
        val endTime = currentCalendar.time.time
        val targetTime = targetCalendar.time.time
        if (targetTime < startTime || endTime < targetTime) return null
        currentCalendar.time = targetDate
        currentCalendar.set(Calendar.DAY_OF_MONTH, 1)
        val diff = daysBetween(currentCalendar.time, targetCalendar.time)
        val week = diff / maxGridWidth + 1
        val day = diff % maxGridHeight
        return Pair(day.toInt(), week.toInt())
    }

    final fun setFocusDay(x: Int, y: Int) {
        _changeFocus(x, y)
    }

    final fun setFocusDay(date: Date) {
        getXYFromDate(date)?.let { position ->
            _changeFocus(position.first, position.second)
        }
    }

    final fun focusNextDay() {
        if (lastFocusPosition == null) {
            _changeFocus(0, 1)
            return
        }
        val last = lastFocusPosition!!
        if (last.first >= maxGridWidth && last.second >= maxGridHeight) return
        val overflow = last.first + 1 >= maxGridWidth
        val nextX = if (overflow) 0 else last.first + 1
        val nextY = if (overflow) last.second + 1 else last.second
        _changeFocus(nextX, nextY)
    }

    final fun focusStartDay() {
        val calendar = getCalendar()
        calendar.getActualMaximum(Calendar.DAY_OF_MONTH)
        setFocusDay(calendar.time)
    }

    final fun focusEndDay() {
        val calendar = getCalendar()
        calendar.getActualMinimum(Calendar.DAY_OF_MONTH)
        setFocusDay(calendar.time)
    }

    final fun focusPreviewDay() {
        if (lastFocusPosition == null) {
            _changeFocus(maxGridWidth - 1, maxGridHeight - 1)
            return
        }
        val last = lastFocusPosition!!
        if (last.first <= 0 && last.second <= 1) return
        val overflow = last.first - 1 < 0
        val nextX = if (overflow) maxGridWidth - 1 else last.first - 1
        val nextY = if (overflow) last.second - 1 else last.second
        _changeFocus(nextX, nextY)
    }

    final fun getFocusDate(): Date? = lastFocusPosition?.let { getDateFromXY(it.first, it.second) }

    final fun getFocusXY(): Pair<Int, Int>? = lastFocusPosition

    final fun getCalendarStartDate(): Date? = getDateFromXY(0, 1)

    final fun getCalendarEndDate(): Date? = getDateFromXY(maxGridWidth - 1, maxGridHeight - 1)

    final fun clearFocus() {
        lastActiveViewHolder?.lostFocusView()
        lastActiveViewHolder = null
        lastFocusPosition = null
    }

    final fun getViewHolder(x: Int, y: Int): JCalendarViewHolder? {
        return if (gridData.size <= y || gridData[y].size <= x) null
        else gridData[y][x]
    }

    final fun getViewHolder(date: Date): JCalendarViewHolder? {
        val point = getXYFromDate(date)
        return if (point == null) null else getViewHolder(point.first, point.second)
    }

    @Suppress("SENSELESS_COMPARISON")
    private fun refresh() {
        if (gridData == null) return
        val calendar = getCalendar(targetDate)
        calendar.set(Calendar.DAY_OF_MONTH, 1)
        val startDay = calendar.get(Calendar.DAY_OF_WEEK) - 1
        for (week in 0 until maxGridHeight) {
            if (gridData[week] == null) return
            for (day in 0 until maxGridWidth) {
                if (gridData[week][day] == null) return
                val viewHolder = gridData[week][day]!!
                if (week == 0) {
                    calendar.set(Calendar.DAY_OF_WEEK, startOfWeek)
                    calendar.add(Calendar.DAY_OF_MONTH, day)
                    _onBindHeaderViewHolder(viewHolder, calendar.time)
                } else {
                    calendar.time = targetDate
                    calendar.set(Calendar.DAY_OF_MONTH, 1)
                    calendar.add(Calendar.DAY_OF_MONTH, day + (7 * (week - 1)) - startDay)
                    _onBindViewHolder(viewHolder, day, week, calendar.time)
                }
            }
        }
        refreshCallback?.let { it() }
    }

    internal var refreshCallback: (() -> Unit)? = null

    final fun setTargetDate(date: Date) {
        this.targetDate = date
        monthChangeListener?.monthChanged(this.targetDate)
    }

    final fun getTargetDate() = this.targetDate

    internal fun _changeFocus(x: Int, y: Int) {
        getViewHolder(x, y)?.let { focus ->
            _changeFocus(x, y, focus)
        }
    }

    @Suppress("UNCHECKED_CAST")
    internal fun _changeFocus(x: Int, y: Int, currentViewHolder: JCalendarViewHolder) {
        val before = lastActiveViewHolder as? ViewHolder
        val current = currentViewHolder as? ViewHolder ?: throw IllegalArgumentException("Invalid type ${currentViewHolder::class.java.name}")
        before?.lostFocusView()
        current.hasFocusView()
        changeFocus(x, y, before, current)
        lastActiveViewHolder = current
        lastFocusPosition = Pair(x, y)
    }

    fun changeFocus(x: Int, y: Int, beforeViewHolder: ViewHolder?, currentViewHolder: ViewHolder) {
    }

    private fun getCalendar() = Calendar.getInstance()
    private fun getCalendar(date: Date) = Calendar.getInstance().apply { this.time = date }

    private fun daysBetween(startDate: Date, endDate: Date): Long {
        val sDate = getCalendar(startDate)
        sDate.set(Calendar.HOUR_OF_DAY, 0)            // set hour to midnight
        sDate.set(Calendar.MINUTE, 0)                 // set minute in hour
        sDate.set(Calendar.SECOND, 0)                 // set second in minute
        sDate.set(Calendar.MILLISECOND, 0)            // set millisecond in second
        val eDate = getCalendar(endDate)
        eDate.set(Calendar.HOUR_OF_DAY, 0)            // set hour to midnight
        eDate.set(Calendar.MINUTE, 0)                 // set minute in hour
        eDate.set(Calendar.SECOND, 0)                 // set second in minute
        eDate.set(Calendar.MILLISECOND, 0)            // set millisecond in second

        var daysBetween: Long = 0
        while (sDate.before(eDate)) {
            sDate.add(Calendar.DAY_OF_MONTH, 1)
            daysBetween++
        }
        return daysBetween
    }
}