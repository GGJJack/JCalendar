package com.hstudio.jcalendar

import android.graphics.Color
import android.graphics.Paint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.hstudio.jcalendarview.JCalendar
import com.hstudio.jcalendarview.JCalendarLine
import com.hstudio.jcalendarview.OldJCalendar
import com.hstudio.jcalendarview.MonthChangeListener
import java.util.*

class MainFragment : Fragment() {
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_main, container, false)
        val calendar = view.findViewById<JCalendar>(R.id.calendar)
        val tv_date = view.findViewById<TextView>(R.id.tv_date)
        val btn_left = view.findViewById<Button>(R.id.btn_left)
        val btn_right = view.findViewById<Button>(R.id.btn_right)
        calendar.calendarAdapter(SampleAdapter::class.java)
        calendar.monthChangeListener = object : MonthChangeListener {
            override fun monthChanged(focusDate: Date) {
                tv_date.text = focusDate.toLocaleString()
            }
        }
        calendar.getDate()?.let {
            tv_date.text = it.toLocaleString()
        }
//        val adapter = SampleAdapter()
//        calendar.adapter = adapter
//        tv_date.text = adapter.getDate().toLocaleString()
//        adapter.monthChangeListener = object: MonthChangeListener {
//            override fun monthChanged(focusDate: Date) {
//                Toast.makeText(this@MainActivity, focusDate.toString(), Toast.LENGTH_SHORT).show()
//            }
//        }
        btn_left.setOnClickListener {
            calendar.prev()
//            adapter.beforeMonth()
//            tv_date.text = adapter.getDate().toLocaleString()
        }
        btn_right.setOnClickListener {
            calendar.next()
//            adapter.nextMonth()
//            tv_date.text = adapter.getDate().toLocaleString()
        }

        view.findViewById<Button>(R.id.btn_minimize)?.let {
            it.setOnClickListener {
                calendar.minimize()
            }
        }
        view.findViewById<Button>(R.id.btn_collapse)?.let {
            it.setOnClickListener {
                calendar.collapse()
            }
        }
        view.findViewById<Button>(R.id.btn_full)?.let {
            it.setOnClickListener {
                calendar.full()
            }
        }
        view.findViewById<Button>(R.id.btn_test1)?.let {
            it.setOnClickListener {
                calendar.getFocusDate()?.let {
                    calendar.notifyDateItemChanged(it)
                }
                calendar.focusNextDay()
//                JLog.i("HJ", "Calendar Height : ${calendar.height}")
            }
        }
        view.findViewById<Button>(R.id.btn_test2)?.let {
            it.setOnClickListener {
                JLog.i(
                    "HJ",
                    "Start : ${calendar.getCalendarStartDate()?.toLocaleString()} / End : ${calendar.getCalendarEndDate()?.toLocaleString()} / Focus : ${calendar.getFocusDate()?.toLocaleString()} / XY : ${calendar.getFocusXY()}"
                )
            }
        }
        val redPaint = Paint().apply {
            this.color = Color.RED
            this.style = Paint.Style.STROKE
            this.strokeWidth = 2f
        }
        val greenPaint = Paint().apply {
            this.color = Color.GREEN
            this.style = Paint.Style.STROKE
            this.strokeWidth = 4f
        }
        val bluePaint = Paint().apply {
            this.color = Color.BLUE
            this.style = Paint.Style.STROKE
            this.strokeWidth = 2f
        }
        calendar.setLinePaint(JCalendarLine.HEADER_LEFT, redPaint)
        calendar.setLinePaint(JCalendarLine.HEADER_TOP, redPaint)
        calendar.setLinePaint(JCalendarLine.HEADER_RIGHT, redPaint)
        calendar.setLinePaint(JCalendarLine.HEADER_BOTTOM, redPaint)
        calendar.setLinePaint(JCalendarLine.HEADER_SPLIT, greenPaint)
        calendar.setLinePaint(JCalendarLine.BODY_LEFT, greenPaint)
        calendar.setLinePaint(JCalendarLine.BODY_TOP, greenPaint)
        calendar.setLinePaint(JCalendarLine.BODY_RIGHT, greenPaint)
        calendar.setLinePaint(JCalendarLine.BODY_BOTTOM, greenPaint)
        calendar.setLinePaint(JCalendarLine.BODY_SPLIT_HORIZONTAL, redPaint)
        calendar.setLinePaint(JCalendarLine.BODY_SPLIT_VERTICAL, bluePaint)
        calendar.setLinePaint(JCalendarLine.FOCUS_BODY, greenPaint)
        return view
    }
}