package com.hstudio.jcalendarview

import android.content.Context
import android.util.AttributeSet
import android.view.ViewTreeObserver
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.viewpager.widget.PagerAdapter
import androidx.viewpager.widget.ViewPager
import java.util.*
import kotlin.reflect.KClass

class JCalendar : ViewPager {
    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)

    internal var pagerAdapter: JCalenderPagerAdapter<JCalendarAdapter<*, *>>? = null
    internal var fragmentClass: Class<*>? = null
    internal var fragmentManager: FragmentManager? = null

    init {
        JLog.i("HJ", "Init")
        this.viewTreeObserver.addOnGlobalLayoutListener(object: ViewTreeObserver.OnGlobalLayoutListener {
            override fun onGlobalLayout() {
                this@JCalendar.viewTreeObserver.removeOnGlobalLayoutListener(this)
                pagerAdapter?.let {
                    it.viewMaxHeight = this@JCalendar.height
                }
            }
        })
    }

    val viewPagerListener: OnPageChangeListener = object : OnPageChangeListener {
        override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {
            //JLog.i("HJ", "position : $position / positionOffset : $positionOffset") //애니메이션 되는동안 호출되는듯
        }

        override fun onPageSelected(position: Int) { //활성화된 페이지
            JLog.i("HJ", "position : $position")
            pagerAdapter?.changePosition(position)?.let {
                _monthChangeListener?.monthChanged(it)
            }
        }

        override fun onPageScrollStateChanged(state: Int) {
            // 1 시작 2 진행중 0 종료
            //JLog.i("HJ", "state : $state")
        }
    }
    private var _monthChangeListener: MonthChangeListener? = null
    var monthChangeListener: MonthChangeListener?
        get() = _monthChangeListener
        set(value) {
            _monthChangeListener = value
        }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        this.addOnPageChangeListener(viewPagerListener)
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        this.removeOnPageChangeListener(viewPagerListener)
    }

    @Suppress("UNCHECKED_CAST")
    fun <T : JCalendarAdapter<*, *>> calendarAdapter(adapterClass: Class<T>) {
        JLog.i("HJ", "setAdapter")
        val adapter = JCalenderPagerAdapter(adapterClass) as JCalenderPagerAdapter<JCalendarAdapter<*, *>>
        this.adapter = adapter
        this.pagerAdapter = adapter
        this.setCurrentItem(adapter.centerValue, false)
        this.offscreenPageLimit = adapter.maxViewCount
        adapter.changePosition(this.currentItem).let {
            _monthChangeListener?.monthChanged(it)
        }
        this.fragmentClass?.let {
            if (this.fragmentManager != null) pagerAdapter?.setFragmentClass(this.fragmentManager!!, it::class.java)
        }
    }

    fun <T: Fragment> setChildFragment(fragmentManager: FragmentManager, fragmentClass: Class<T>){
        this.fragmentClass = fragmentClass
        this.fragmentManager = fragmentManager
        pagerAdapter?.let {
            it.setFragmentClass(fragmentManager, fragmentClass)
        }
    }

    fun next(): Boolean {
        if (adapter != null && this.currentItem < adapter!!.count.plus(-1)) {
            setCurrentItem(this.currentItem + 1, true)
            return true
        }
        return false
    }

    fun prev(): Boolean {
        if (this.currentItem > 0) {
            setCurrentItem(this.currentItem - 1, true)
            return true
        }
        return false
    }

    fun getDate(): Date? {
        return pagerAdapter?.targetDate
    }

    fun minimize() {
        pagerAdapter?.minimize()
    }

    fun collapse() {
        pagerAdapter?.collapse()
    }

    fun full() {
        pagerAdapter?.full()
    }
}