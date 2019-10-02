package com.hstudio.jcalendarview

import android.animation.Animator
import android.animation.ValueAnimator
import android.graphics.Color
import android.util.SparseArray
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import android.widget.CalendarView
import android.widget.FrameLayout
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import androidx.core.animation.addListener
import androidx.core.animation.doOnEnd
import androidx.core.animation.doOnStart
import androidx.core.util.valueIterator
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.viewpager.widget.PagerAdapter
import androidx.viewpager.widget.ViewPager
import java.util.*
import kotlin.collections.HashMap
import kotlin.reflect.KClass

internal class OldJCalenderPagerAdapter<adapter : JCalendarAdapter<*, *>>(private val adapterClass: Class<adapter>) : PagerAdapter() {
    internal val maxViewCount = 3
    internal val maxInt = 10 //Int.MAX_VALUE - 1
    internal val centerValue = maxInt / 2
    internal var lastPosition: Int = centerValue
    internal var viewIdMap = SparseArray<Pair<JCalendarView, View>>()
    internal var viewMaxHeight: Int? = null
    var collapseRatio: Float
        get() = _collapseRatio
        set(value) {
            _collapseRatio = value
            viewIdMap[lastPosition]?.first?.collapseRatio = value
        }
    var _collapseRatio = 0.6f
    internal var animateDuration: Int = 300
    internal var targetDate: Date = Date()

    var lastViewHeight: Int = 0
    var lastViewHeightRatio = 1f
    var heightAnimator: ValueAnimator? = null

    init {
        JLog.i("HJ", "JCalendaerPagerAdapter Init")
    }

    internal var fragmentClass: Class<*>? = null
    internal var fragmentManager: FragmentManager? = null

    fun <T : Any> setFragmentClass(fragmentManager: FragmentManager, fragmentClass: Class<T>) {
        this.fragmentClass = fragmentClass
        this.fragmentManager = fragmentManager
    }

    override fun instantiateItem(container: ViewGroup, position: Int): Any {
        val realPos = position % maxViewCount

        val animateView = ConstraintLayout(container.context)
        animateView.id = Util.makeViewId()
        animateView.layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)

        val calendarView = JCalendarView(container.context)
        calendarView.id = Util.makeViewId()
        //val adapter = adapters.value[realPos]
        calendarView.adapter = adapterClass.newInstance()
        calendarView.animateDuration = this.animateDuration
        calendarView.layoutParams = ConstraintLayout.LayoutParams(
            ConstraintSet.MATCH_CONSTRAINT,
            ConstraintSet.MATCH_CONSTRAINT
        ) //ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
        animateView.addView(calendarView)

        val fragmentLayout = FrameLayout(container.context)
        fragmentLayout.id = Util.makeViewId()
        fragmentLayout.layoutParams = ConstraintLayout.LayoutParams(
            ConstraintSet.MATCH_CONSTRAINT,
            ConstraintSet.MATCH_CONSTRAINT
        ) //ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
        animateView.addView(fragmentLayout)
        fragmentLayout.setBackgroundColor(Color.parseColor("#44FF0000"))

        val set = ConstraintSet()
        set.clone(animateView)
        set.connect(calendarView.id, ConstraintSet.LEFT, animateView.id, ConstraintSet.LEFT)
        set.connect(calendarView.id, ConstraintSet.TOP, animateView.id, ConstraintSet.TOP)
        set.connect(calendarView.id, ConstraintSet.RIGHT, animateView.id, ConstraintSet.RIGHT)
        set.connect(calendarView.id, ConstraintSet.BOTTOM, fragmentLayout.id, ConstraintSet.TOP)
        //set.connect(fragmentLayout.id, ConstraintSet.TOP, calendarView.id, ConstraintSet.BOTTOM)
        set.connect(fragmentLayout.id, ConstraintSet.LEFT, animateView.id, ConstraintSet.LEFT)
        set.connect(fragmentLayout.id, ConstraintSet.BOTTOM, animateView.id, ConstraintSet.BOTTOM)
        set.connect(fragmentLayout.id, ConstraintSet.RIGHT, animateView.id, ConstraintSet.RIGHT)
        set.applyTo(animateView)
        // View 속성 설정
        // 위의 realPos 기반으로 현재 뷰에서 보여주어야 하는 값을 세팅한다던가 하는 작업 필요
        container.addView(animateView)
//        animateView.viewTreeObserver.addOnGlobalLayoutListener(object: ViewTreeObserver.OnGlobalLayoutListener {
//            override fun onGlobalLayout() {
//                JLog.i("HJ", "lastViewHeight : $lastViewHeight")
//                Util.setViewHeight(animateView, lastViewHeight)
//                animateView.viewTreeObserver.removeOnGlobalLayoutListener(this)
//            }
//        })
        viewIdMap.put(position, Pair(calendarView, fragmentLayout))
        return animateView
    }

    //    fun currentAdapter(): adapter {
    fun currentAdapter(): adapter? {
        return viewIdMap[lastPosition]?.first?.adapter as? adapter
//        val pos = lastPosition % maxViewCount
        //return adapters.value[lastPosition % maxViewCount]
    }

    fun prevAdapter(): adapter? {
        return viewIdMap[lastPosition - 1]?.first?.adapter as? adapter
//        val pos = (lastPosition - 1) % maxViewCount
//        return if (pos in 0 until lastPosition) adapters.value[pos]
//        else null
    }

    fun nextAdapter(): adapter? {
        return viewIdMap[lastPosition + 1]?.first?.adapter as? adapter
    }

    fun changePosition(position: Int): Date {
        val move = position - lastPosition
        lastPosition = position
        val gap = lastPosition - centerValue
        val calendar = Calendar.getInstance()
        JLog.i("HJ", "[$position]calendar Time($gap) : ${calendar.time.toLocaleString()}")
        calendar.add(Calendar.MONTH, gap)
        targetDate = calendar.time
        JLog.i("HJ", "\tresult : ${targetDate.toLocaleString()}")
        currentAdapter()?.setTargetDate(targetDate)
        calendar.add(Calendar.MONTH, -1)
        prevAdapter()?.setTargetDate(calendar.time)
        calendar.add(Calendar.MONTH, 2)
        nextAdapter()?.setTargetDate(calendar.time)
        prevAdapter()?.notifyMonthChanged()
        nextAdapter()?.notifyMonthChanged()
//        if (move == 1) {
//            prevAdapter()?.notifyMonthChanged()
//        } else if (move == -1) {
//            nextAdapter()?.notifyMonthChanged()
//        }
        return targetDate
    }

    fun animateView(toFloat: Float) {
        val lastView = viewIdMap[lastPosition]?.second ?: return
        val maxViewHeight = viewMaxHeight ?: return
        val calendar = viewIdMap[lastPosition]?.first ?: return
        val adapter = calendar.adapter ?: return
        val cells = adapter.maxGridHeight ?: return
        val headerHeight = calendar.getHeaderViewHeight() ?: return
        val cellHeight = calendar.getFirstRowHeight() ?: return
        val beforeLast = lastViewHeightRatio
        JLog.i("HJ", "Animate $maxViewHeight $lastViewHeightRatio -> $toFloat")
        heightAnimator?.cancel()
        val animator = ValueAnimator.ofFloat(lastViewHeightRatio, toFloat).apply {
            this.duration = animateDuration.toLong()
            this.addUpdateListener {
                (it.animatedValue as? Float)?.let { animateValue ->
                    val anotherCell = ((cellHeight * (cells - 2)).toFloat() * animateValue).toInt()
                    val targetCell = if (animateValue <= collapseRatio) (cellHeight.toFloat() * collapseRatio).toInt() else (cellHeight.toFloat() * animateValue).toInt()
                    val height = maxViewHeight - headerHeight - targetCell - anotherCell
                    calendar.animateViewHeight(animateValue)
                    Util.setViewHeight(lastView, height)
                }
                lastViewHeight = (maxViewHeight.toFloat() * toFloat).toInt()
                lastViewHeightRatio = toFloat
            }
        }
        animator.addListener(object : Animator.AnimatorListener {
            override fun onAnimationRepeat(animation: Animator?) {
            }

            override fun onAnimationEnd(animation: Animator?) {
                calendar.onFinishAnimation(beforeLast, toFloat)
                lastViewHeight = (maxViewHeight.toFloat() * toFloat).toInt()
                lastViewHeightRatio = toFloat
                JLog.i("HJ", "maxViewHeight : $maxViewHeight, lastViewHeight : $lastViewHeight, lastRatio : $lastViewHeightRatio")
                val anotherCell = ((cellHeight * (cells - 2)).toFloat() * toFloat).toInt()
                val targetCell = if (toFloat <= collapseRatio) (cellHeight.toFloat() * collapseRatio).toInt() else (cellHeight.toFloat() * toFloat).toInt()
                val height = maxViewHeight - headerHeight - targetCell - anotherCell
                viewIdMap.valueIterator().forEach {
                    if (it.second != lastView) {
                        Util.setViewHeight(it.second, height)
                        it.first.onFinishAnimation(beforeLast, toFloat)
                    }
                }
            }

            override fun onAnimationCancel(animation: Animator?) {
            }

            override fun onAnimationStart(animation: Animator?) {
                calendar.onStartAnimation(beforeLast, toFloat)
                lastViewHeight = (maxViewHeight.toFloat() * toFloat).toInt()
                lastViewHeightRatio = toFloat
            }

        })
        animator.start()
        heightAnimator = animator
    }

    fun minimize() {
        animateView(0f)
        setVisibleType(VisibleType.MINIMIZE)
    }

    fun collapse() {
        animateView(collapseRatio)
        setVisibleType(VisibleType.COLLAPSE)
    }

    fun full() {
        animateView(1f)
        setVisibleType(VisibleType.FULL)
    }

    private fun setVisibleType(visibleType: VisibleType) {
        viewIdMap.valueIterator().forEach { it.first.setVisibleType(visibleType) }
    }

    override fun destroyItem(container: ViewGroup, position: Int, objects: Any) {
        viewIdMap.remove(position)
        container.removeView(objects as View)
    }

    override fun isViewFromObject(view: View, objects: Any): Boolean {
        return objects == view
    }

    override fun getCount(): Int {
        return maxInt
    }
}
