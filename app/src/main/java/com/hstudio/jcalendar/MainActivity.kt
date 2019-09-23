package com.hstudio.jcalendar

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val adapter = SampleAdapter()
        calendar.adapter = adapter
        tv_date.text = adapter.getDate().toLocaleString()
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
