package com.hstudio.jcalendarview

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import androidx.viewpager.widget.ViewPager

class SwipeViewPager : ViewPager {

    var enabledSwipe = true

    constructor(context: Context) : super(context) {}

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {}


    override fun onInterceptTouchEvent(ev: MotionEvent): Boolean {
        return if (enabledSwipe) super.onInterceptTouchEvent(ev) else false
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(ev: MotionEvent): Boolean {
        return if (enabledSwipe) super.onTouchEvent(ev) else false
    }

    fun setSwipe(enabled: Boolean) {
        this.enabledSwipe = enabled
    }


}