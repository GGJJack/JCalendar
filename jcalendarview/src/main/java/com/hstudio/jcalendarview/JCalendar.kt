package com.hstudio.jcalendarview

import android.animation.Animator
import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View
import android.view.ViewTreeObserver
import android.widget.FrameLayout
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import androidx.core.util.valueIterator
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.viewpager.widget.ViewPager
import java.util.*

class JCalendar : ConstraintLayout {
    constructor(context: Context?) : super(context)
    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

    private val viewPager: SwipeViewPager by lazy { SwipeViewPager(this.context) }
    private val fragmentLayout: FrameLayout by lazy { FrameLayout(this.context) }
    private var pagerAdapter: JCalenderPagerAdapter<JCalendarAdapter<*, *>>? = null

    private var _monthChangeListener: MonthChangeListener? = null
    var monthChangeListener: MonthChangeListener?
        get() = _monthChangeListener
        set(value) {
            _monthChangeListener = value
        }

    init {
        if (this.id == View.NO_ID) this.id = Util.makeViewId()
        viewPager.id = Util.makeViewId()
        this.addView(viewPager, LayoutParams(ConstraintSet.MATCH_CONSTRAINT, ConstraintSet.MATCH_CONSTRAINT))
        fragmentLayout.id = Util.makeViewId()
        this.addView(fragmentLayout, LayoutParams(ConstraintSet.MATCH_CONSTRAINT, ConstraintSet.MATCH_CONSTRAINT))

        val set = ConstraintSet()
        set.clone(this)
        set.connect(viewPager.id, ConstraintSet.LEFT, this.id, ConstraintSet.LEFT)
        set.connect(viewPager.id, ConstraintSet.TOP, this.id, ConstraintSet.TOP)
        set.connect(viewPager.id, ConstraintSet.RIGHT, this.id, ConstraintSet.RIGHT)
        set.connect(viewPager.id, ConstraintSet.BOTTOM, fragmentLayout.id, ConstraintSet.TOP)
        set.connect(fragmentLayout.id, ConstraintSet.TOP, viewPager.id, ConstraintSet.BOTTOM)
        set.connect(fragmentLayout.id, ConstraintSet.LEFT, this.id, ConstraintSet.LEFT)
        set.connect(fragmentLayout.id, ConstraintSet.BOTTOM, this.id, ConstraintSet.BOTTOM)
        set.connect(fragmentLayout.id, ConstraintSet.RIGHT, this.id, ConstraintSet.RIGHT)
        set.applyTo(this)
        this.viewTreeObserver.addOnGlobalLayoutListener(object : ViewTreeObserver.OnGlobalLayoutListener {
            override fun onGlobalLayout() {
                this@JCalendar.viewTreeObserver.removeOnGlobalLayoutListener(this)
                viewMaxHeight = this@JCalendar.height
            }
        })
        fragmentLayout.visibility = View.GONE
    }

    fun setFragment(supportFragmentManager: FragmentManager, fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(fragmentLayout.id, fragment)
            .commit()
    }

    @Suppress("UNCHECKED_CAST")
    fun <T : JCalendarAdapter<*, *>> calendarAdapter(adapterClass: Class<T>) {
        JLog.i("HJ", "setAdapter")
        val adapter = JCalenderPagerAdapter(adapterClass) as JCalenderPagerAdapter<JCalendarAdapter<*, *>>
        adapter.animateDuration = this.animateDuration
        attachListener?.let { adapter.setAdapterAttachedListener(it) }
        pagerAdapter = adapter
        viewPager.adapter = adapter
        viewPager.setCurrentItem(adapter.centerValue, false)
        viewPager.offscreenPageLimit = adapter.maxViewCount
        adapter.changePosition(adapter.centerValue).let { _monthChangeListener?.monthChanged(it) }
//        this.fragmentClass?.let {
//            if (this.fragmentManager != null) pagerAdapter?.setFragmentClass(this.fragmentManager!!, it::class.java)
//        }
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        viewPager.addOnPageChangeListener(viewPagerListener)
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        viewPager.removeOnPageChangeListener(viewPagerListener)
    }

    val viewPagerListener: ViewPager.OnPageChangeListener = object : ViewPager.OnPageChangeListener {
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

    fun getDate(): Date? {
        return pagerAdapter?.targetDate
    }

    var animateDuration: Int = 300
    private var lastViewHeight: Int = 0
    private var lastViewHeightRatio = 1f
    private var lastCalendarHeaderViewHeight: Int? = null
    private var lastCalendarCellViewHeight: Int? = null
    private var heightAnimator: Animator? = null
    private var _collapseRatio = 0.6f
    private var viewMaxHeight: Int? = null
    var collapseRatio: Float
        get() = _collapseRatio
        set(value) {
            _collapseRatio = value
            pagerAdapter?.allViews { it.collapseRatio = value }
        }

    private fun animateView(toFloat: Float) {
        val maxViewHeight = viewMaxHeight ?: return
        val calendar = pagerAdapter?.currentCalendar() ?: return
        val adapter = calendar.adapter ?: return
        val cells = adapter.maxGridHeight
        val headerHeight = lastCalendarHeaderViewHeight ?: calendar.getHeaderViewHeight() ?: return
        val cellHeight = lastCalendarCellViewHeight ?: calendar.getFirstRowHeight() ?: return
        val beforeLast = lastViewHeightRatio
        lastCalendarHeaderViewHeight = headerHeight
        lastCalendarCellViewHeight = cellHeight

        heightAnimator?.cancel()
        val animator = ValueAnimator.ofFloat(lastViewHeightRatio, toFloat)
        animator.duration = this.animateDuration.toLong()
        animator.addUpdateListener {
            (it.animatedValue as? Float)?.let { animateValue ->
                val anotherCell = ((cellHeight * (cells - 2)).toFloat() * animateValue).toInt()
                val targetCell = if (animateValue <= collapseRatio) (cellHeight.toFloat() * collapseRatio).toInt() else (cellHeight.toFloat() * animateValue).toInt()
                val height = maxViewHeight - headerHeight - targetCell - anotherCell
                calendar.animateViewHeight(animateValue)
                Util.setViewHeight(fragmentLayout, if (height <= 0) 1 else height)
            }
        }
        animator.addListener(object : Animator.AnimatorListener {
            override fun onAnimationRepeat(animation: Animator?) {
            }

            override fun onAnimationEnd(animation: Animator?) {
                calendar.onFinishAnimation(beforeLast, toFloat, lastCalendarCellViewHeight)
                lastViewHeight = (maxViewHeight.toFloat() * toFloat).toInt()
                lastViewHeightRatio = toFloat
                val anotherCell = ((cellHeight * (cells - 2)).toFloat() * toFloat).toInt()
                val targetCell = if (toFloat <= collapseRatio) (cellHeight.toFloat() * collapseRatio).toInt() else (cellHeight.toFloat() * toFloat).toInt()
                val height = maxViewHeight - headerHeight - targetCell - anotherCell
//                JLog.i("HJ", "maxViewHeight : $maxViewHeight, lastViewHeight : $lastViewHeight, lastRatio : $lastViewHeightRatio")
//                JLog.i("HJ", "Header : $headerHeight, AnotherCell : $anotherCell, TargetCell : $targetCell, Height : $height")
                Util.setViewHeight(fragmentLayout, height)
                pagerAdapter?.allViews {
                    if (it != calendar) {
                        it.onFinishAnimation(beforeLast, toFloat, lastCalendarCellViewHeight)
                    }
                }
                if (toFloat == 1f) {
                    fragmentLayout.visibility = View.GONE
                }
            }

            override fun onAnimationCancel(animation: Animator?) {
            }

            override fun onAnimationStart(animation: Animator?) {
                if (fragmentLayout.visibility == View.GONE) {
                    Util.setViewHeight(fragmentLayout, 1)
                    fragmentLayout.visibility = View.VISIBLE
                }
                calendar.onStartAnimation(beforeLast, toFloat)
                lastViewHeight = (maxViewHeight.toFloat() * toFloat).toInt()
                lastViewHeightRatio = toFloat
            }
        })
        animator.start()
        heightAnimator = animator
    }

    final fun getFocusDate(): Date? = pagerAdapter?.currentAdapter()?.let { it.getFocusDate() }

    final fun getFocusXY(): Pair<Int, Int>? = pagerAdapter?.currentAdapter()?.let { it.getFocusXY() }

    final fun getDateFromXY(x: Int, y: Int): Date? = pagerAdapter?.currentAdapter()?.getDateFromXY(x, y)

    final fun getCalendarStartDate(): Date? = pagerAdapter?.currentAdapter()?.getCalendarStartDate()

    final fun getCalendarEndDate(): Date? = pagerAdapter?.currentAdapter()?.getCalendarEndDate()

    final fun focusNextDay() = pagerAdapter?.currentAdapter()?.focusNextDay()

    final fun notifyDateItemChanged(date: Date) = pagerAdapter?.currentAdapter()?.notifyDateItemChanged(date)

    fun currentAdapter(): JCalendarAdapter<*, *>? = pagerAdapter?.currentAdapter()
    fun prevAdapter(): JCalendarAdapter<*, *>? = pagerAdapter?.prevAdapter()
    fun nextAdapter(): JCalendarAdapter<*, *>? = pagerAdapter?.nextAdapter()

    fun minimize() {
        pagerAdapter?.setVisibleType(VisibleType.MINIMIZE)
        animateView(0f)
    }

    fun collapse() {
        pagerAdapter?.setVisibleType(VisibleType.COLLAPSE)
        animateView(collapseRatio)
    }

    fun full() {
        pagerAdapter?.setVisibleType(VisibleType.FULL)
        animateView(1f)
    }

    fun swipeLock() = viewPager.setSwipe(false)

    fun swipeUnLock() = viewPager.setSwipe(true)

    fun prev() {}

    fun next() {}

    fun moveToday() {
        pagerAdapter?.changeCenterPosition(viewPager.currentItem)?.let { _monthChangeListener?.monthChanged(it) }
    }

    fun setLinePaint(field: JCalendarLine, paint: Paint) {
        pagerAdapter?.setLinePaint(field, paint)
    }

    private var attachListener: AdapterAttachedListener? = null

    fun setAdapterAttachedListener(listener: AdapterAttachedListener) {
        attachListener = listener
        pagerAdapter?.let {
            it.setAdapterAttachedListener(listener)
        }
    }
}