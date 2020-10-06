package com.hstudio.jcalendarview

import android.os.Build
import android.view.View
import java.util.*

abstract class JCalendarViewHolder(private val _rootView: View) {
    var viewType: Int = 0

    val view: View get() = _rootView

    init {
        _rootView.isFocusable = true
        _rootView.isClickable = true
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            _rootView.focusable = View.FOCUSABLE
        }
    }

    open fun hasFocusView(){}

    open fun lostFocusView(){}
}