package com.hstudio.jcalendarview

import java.util.*

fun Date.clearTime(): Date {
    return this.clearTimeToCalendar().time
}

fun Date.clearTimeToCalendar(): Calendar {
    val calendar = this.toCalendar()
    return calendar.clearTime()
}

fun Date.toCalendar(): Calendar = Calendar.getInstance().apply { this.time = this@toCalendar }

fun Calendar.clearTime(): Calendar {
    this.set(Calendar.HOUR_OF_DAY, 0)            // set hour to midnight
    this.set(Calendar.MINUTE, 0)                 // set minute in hour
    this.set(Calendar.SECOND, 0)                 // set second in minute
    this.set(Calendar.MILLISECOND, 0)            // set millisecond in second
    return this
}

fun Calendar.same(compare: Calendar, vararg fields: Int): Boolean {
    fields.forEach { if (this.get(it) != compare.get(it)) return false }
    return true
}

fun Date.equalY(date: Date): Boolean {
    return this.toCalendar().same(date.toCalendar(), Calendar.YEAR)
}

fun Date.equalYM(date: Date): Boolean {
    return this.toCalendar().same(date.toCalendar(), Calendar.YEAR, Calendar.MONTH)
}

fun Date.equalYMD(date: Date): Boolean {
    return this.toCalendar().same(date.toCalendar(), Calendar.YEAR, Calendar.MONTH, Calendar.DAY_OF_MONTH)
}

fun Date.equalY(date: Calendar): Boolean {
    return this.toCalendar().same(date, Calendar.YEAR)
}

fun Date.equalYM(date: Calendar): Boolean {
    return this.toCalendar().same(date, Calendar.YEAR, Calendar.MONTH)
}

fun Date.equalYMD(date: Calendar): Boolean {
    return this.toCalendar().same(date, Calendar.YEAR, Calendar.MONTH, Calendar.DAY_OF_MONTH)
}

fun Calendar.equalY(date: Date): Boolean {
    return this.same(date.toCalendar(), Calendar.YEAR)
}

fun Calendar.equalYM(date: Date): Boolean {
    return this.same(date.toCalendar(), Calendar.YEAR, Calendar.MONTH)
}

fun Calendar.equalYMD(date: Date): Boolean {
    return this.same(date.toCalendar(), Calendar.YEAR, Calendar.MONTH, Calendar.DAY_OF_MONTH)
}

fun Calendar.equalY(date: Calendar): Boolean {
    return this.same(date, Calendar.YEAR)
}

fun Calendar.equalYM(date: Calendar): Boolean {
    return this.same(date, Calendar.YEAR, Calendar.MONTH)
}

fun Calendar.equalYMD(date: Calendar): Boolean {
    return this.same(date, Calendar.YEAR, Calendar.MONTH, Calendar.DAY_OF_MONTH)
}

fun Date.thisWeek(date: Date): Boolean {
    val start = this.toCalendar().firstDayOfWeek
    val end = this.toCalendar().apply {
        this.set(Calendar.DAY_OF_MONTH, start)
        this.add(Calendar.DAY_OF_MONTH, 7)
    }.get(Calendar.DAY_OF_MONTH)
    val target = date.toCalendar().get(Calendar.DAY_OF_MONTH)
    return this.equalYM(date) && start <= target && target <= end
}