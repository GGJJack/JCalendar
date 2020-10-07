package com.hstudio.jcalendarview

import android.view.LayoutInflater
import android.view.ViewGroup
import java.lang.IllegalArgumentException
import java.util.*

abstract class JCalendarAdapter<ViewHolder : JCalendarViewHolder, HeaderViewHolder : JCalendarViewHolder> {

    private val startOfWeek: Int = Calendar.SUNDAY
    private var targetDate: Date = Date()
    internal val maxGridHeight = 6 + 1
    internal val maxGridWidth = 7
    internal val gridData = Array(maxGridHeight) { Array<JCalendarViewHolder?>(maxGridWidth) { null } }

    private var monthChangeListener: MonthChangeListener? = null
    private var lastActiveViewHolder: JCalendarViewHolder? = null
    internal var lastFocusPosition: Pair<Int, Int>? = null
    private var holderMaxHeight: Int? = null

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

    final fun setYear(year: Int) {
        val calendar = getCalendar(targetDate)
        calendar.set(Calendar.YEAR, year)
        targetDate = calendar.time
        refresh()
        monthChangeListener?.monthChanged(this.targetDate)
    }

    final fun setMonth(month: Int) {
        val calendar = getCalendar(targetDate)
        calendar.set(Calendar.MONTH, month)
        targetDate = calendar.time
        refresh()
        monthChangeListener?.monthChanged(this.targetDate)
    }

    final fun setYearAndMonth(year: Int, month: Int) {
        val calendar = getCalendar(targetDate)
        calendar.set(Calendar.YEAR, year)
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

    final fun getDateFromXY(x: Int, y: Int): Date {
        val calendar = getCalendar(targetDate)
        calendar.set(Calendar.DAY_OF_MONTH, 1)
        val startDay = calendar.get(Calendar.DAY_OF_WEEK) - 1
        calendar.add(Calendar.DAY_OF_MONTH, x + (7 * (y - 1)) - startDay)
        return calendar.time
    }

    final fun getXYFromDate(date: Date): Pair<Int, Int>? {
        val startDate = getCalendar(targetDate).let {
            it.set(Calendar.DAY_OF_MONTH, 1)
            val startDay = it.get(Calendar.DAY_OF_WEEK) - 1
            it.add(Calendar.DAY_OF_MONTH, -startDay)
            it.time
        }
        val diff = daysBetween(startDate, date)
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
        if (lastFocusPosition == null) { _changeFocus(0, 1); return }
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

    final fun focusMonthStartDay() {
        val cal = Calendar.getInstance()
        for (x in 0 until 7) {
            cal.time = getDateFromXY(x, 1)
            if (cal.get(Calendar.DAY_OF_MONTH) == 1) {
                _changeFocus(x, 1)
                return
            }
        }
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
        onClearFocus?.let { it() }
    }

    private var onClearFocus: (() -> Unit)? = null

    fun setOnClearFocus(func: (() -> Unit)) {
        onClearFocus = func
    }

    final fun getViewHolder(x: Int, y: Int): JCalendarViewHolder? {
        return if (gridData.size <= y || y < 0 || gridData[y].size <= x || x < 0) null
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

    internal var invalidCallback: (() -> Unit)? = null

    final fun setTargetDate(date: Date) {
        this.targetDate = date.clearTime()
        monthChangeListener?.monthChanged(this.targetDate)
    }

    final fun getTargetDate() = this.targetDate

    internal fun _changeFocus(x: Int, y: Int) {
        getViewHolder(x, y)?.let { focus -> _changeFocus(x, y, focus) }
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
        invalidCallback?.let { it() }
    }

    fun changeFocus(x: Int, y: Int, beforeViewHolder: ViewHolder?, currentViewHolder: ViewHolder) {
    }

    private fun getCalendar() = Calendar.getInstance()
    private fun getCalendar(date: Date) = Calendar.getInstance().apply { this.time = date }

    private fun daysBetween(startDate: Date, endDate: Date): Long {
        val sDate = startDate.clearTimeToCalendar()
        val eDate = endDate.clearTimeToCalendar()

        var daysBetween: Long = 0
        if (sDate.after(eDate)) {
            while (sDate.after(eDate)) {
                sDate.add(Calendar.DAY_OF_MONTH, -1)
                daysBetween--
            }
        } else {
            while (sDate.before(eDate)) {
                sDate.add(Calendar.DAY_OF_MONTH, 1)
                daysBetween++
            }
        }

        return daysBetween
    }

    fun setMonthChangeListener(listener: MonthChangeListener) {
        monthChangeListener = listener
    }
}