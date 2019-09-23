package com.hstudio.jcalendarview

import android.os.Build
import android.view.View
import java.util.*

abstract class JCalendarViewHolder(private val _rootView: View): AnimateAbleViewInterface {
    var viewType: Int = 0

    val view: View get() = _rootView

    init {
        _rootView.isFocusable = true
        _rootView.isClickable = true
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            _rootView.focusable = View.FOCUSABLE
        }
    }

    override fun startAnimate(){}

    override fun animating(){}

    override fun finishAnimate(){}

    open fun hasFocusView(){}

    open fun lostFocusView(){}
}