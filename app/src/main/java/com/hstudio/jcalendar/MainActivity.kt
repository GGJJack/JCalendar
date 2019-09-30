package com.hstudio.jcalendar

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.hstudio.jcalendarview.JCalendar
import com.hstudio.jcalendarview.MonthChangeListener
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.fragment_main.*
import java.util.*

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        supportFragmentManager.beginTransaction()
            .replace(R.id.ly_fragment, MainFragment())
            .commitAllowingStateLoss()
//        val adapter = SampleAdapter()
//        calendar.adapter = adapter
//        tv_date.text = adapter.getDate().toLocaleString()
//        adapter.monthChangeListener = object: MonthChangeListener {
//            override fun monthChanged(focusDate: Date) {
//                Toast.makeText(this@MainActivity, focusDate.toString(), Toast.LENGTH_SHORT).show()
//            }
//        }
//        btn_left.setOnClickListener {
//            adapter.beforeMonth()
//            tv_date.text = adapter.getDate().toLocaleString()
//        }
//        btn_right.setOnClickListener {
//            adapter.nextMonth()
//            tv_date.text = adapter.getDate().toLocaleString()
//        }
//        btn_preview.setOnClickListener { adapter.focusPreviewDay() }
//        btn_current.setOnClickListener { adapter.setFocusDay(Date()) }
//        btn_xy.setOnClickListener { adapter.setFocusDay(1, 2) }
//        btn_clear.setOnClickListener { adapter.clearFocus() }
//        btn_next.setOnClickListener { adapter.focusNextDay() }
//        adapter.notifyXYItemChanged(1, 2)
//        adapter.notifyXYItemChanged(1, 2)
//        val calendar = Calendar.getInstance()
//        adapter.notifyDateItemChanged(calendar.time)
//        adapter.notifyDateItemChanged(calendar.time)
//        val date = adapter.getDateFromXY(1, 2)
//        JLog.i("HJ", "date : ${date?.toLocaleString() ?: "null"}")
//        val location = adapter.getXYFromDate(calendar.time)
//        JLog.i("HJ", "location : ${location?.toString() ?: "Null"}")
//        adapter.notifyHeaderViewHolders()
//        adapter.notifyHeaderViewHolders()
//        adapter.notifyXYItemChanged(1, 0)
//        adapter.notifyXYItemChanged(1, 0)
    }
}
