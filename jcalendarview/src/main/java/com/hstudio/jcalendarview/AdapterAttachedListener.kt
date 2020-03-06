package com.hstudio.jcalendarview

import java.util.*


public interface AdapterAttachedListener {
    fun adapterAttached(date: Date, targetDate: Date, adapter: JCalendarAdapter<*, *>)
}