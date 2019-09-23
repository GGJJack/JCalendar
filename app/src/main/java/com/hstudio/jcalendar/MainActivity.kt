package com.hstudio.jcalendar

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.hstudio.jcalendarview.MonthChangeListener
import kotlinx.android.synthetic.main.activity_main.*
import java.util.*

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val adapter = SampleAdapter()
        calendar.adapter = adapter
        tv_date.text = adapter.getDate().toLocaleString()
        adapter.monthChangeListener = object: MonthChangeListener {
            override fun monthChanged(focusDate: Date) {
                Toast.makeText(this@MainActivity, focusDate.toString(), Toast.LENGTH_SHORT).show()
            }
        }
        btn_left.setOnClickListener {
            adapter.beforeMonth()
            tv_date.text = adapter.getDate().toLocaleString()
        }
        btn_right.setOnClickListener {
            adapter.nextMonth()
            tv_date.text = adapter.getDate().toLocaleString()
        }
    }
}
