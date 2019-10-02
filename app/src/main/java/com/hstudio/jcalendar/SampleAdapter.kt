package com.hstudio.jcalendar

import android.annotation.SuppressLint
import android.graphics.Color
import android.icu.util.LocaleData
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.hstudio.jcalendarview.JCalendarAdapter
import com.hstudio.jcalendarview.JCalendarViewHolder
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.time.DayOfWeek
import java.time.LocalDate
import java.util.*

class SampleAdapter : JCalendarAdapter<SampleAdapter.SampleViewHolder, SampleAdapter.SampleHeaderViewHolder>() {
    private val calendar = Calendar.getInstance()
    private val dateFormat = SimpleDateFormat("EE", Locale.getDefault())

    override fun onCreateView(layoutInflater: LayoutInflater, parent: ViewGroup, viewType: Int): SampleAdapter.SampleViewHolder {
        val view = layoutInflater.inflate(R.layout.item_day, parent, false)
        return SampleViewHolder(view)
    }

    override fun onBindViewHolder(holder: SampleViewHolder, x: Int, y: Int, date: Date) {
        calendar.time = getTargetDate()
        val displayMonth = calendar.get(Calendar.MONTH)
        calendar.time = date
        val nowMonth = calendar.get(Calendar.MONTH)
        val dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK)
        holder.bind(calendar.get(Calendar.DAY_OF_MONTH).toString(), displayMonth == nowMonth, dayOfWeek, date)
    }

    override fun onCreateHeaderView(layoutInflater: LayoutInflater, parent: ViewGroup): SampleHeaderViewHolder {
        val view = layoutInflater.inflate(R.layout.item_header, parent, false)
        return SampleHeaderViewHolder(view)
    }

    override fun onBindHeaderViewHolder(holder: SampleHeaderViewHolder, dayOfWeek: Date) {
        calendar.time = dayOfWeek
        holder.bind(dateFormat.format(dayOfWeek).toUpperCase(), calendar.get(Calendar.DAY_OF_WEEK))
    }

    class SampleViewHolder(private val rootView: View) : JCalendarViewHolder(rootView) {
        private var refresh = 0
        private var myDay: Date? = null
        private var dayString: String? = null
        @SuppressLint("SetTextI18n")
        fun bind(day: String, isThisMonth: Boolean, dayOfWeek: Int, myDay: Date) {
            this.myDay = myDay
            this.dayString = day
            rootView.findViewById<TextView>(R.id.tv_day)?.let {
                val refMessage = if(refresh > 0) " (${refresh})" else ""
                it.text = "$day$refMessage"
                it.alpha = if (isThisMonth) 1f else 0.4f
                it.setTextColor(
                    when (dayOfWeek) {
                        Calendar.SUNDAY -> Color.RED
                        Calendar.SATURDAY -> Color.BLUE
                        else -> Color.BLACK
                    }
                )
                refresh++
            }
        }

        override fun hasFocusView() {
//            JLog.i("HJ", "Click[${this.dayString}] : ${myDay?.toLocaleString()} / height : ${rootView.height} / measured : ${rootView.measuredHeight}")
            rootView.setBackgroundColor(Color.parseColor("#90CAF9"))
        }

        override fun lostFocusView() {
            rootView.setBackgroundColor(Color.TRANSPARENT)
        }
    }

    class SampleHeaderViewHolder(private val rootView: View) : JCalendarViewHolder(rootView) {
        private var refresh = 0
        @SuppressLint("SetTextI18n")
        fun bind(weekString: String, dayOfWeek: Int) {
            rootView.findViewById<TextView>(R.id.tv_day)?.let {
                val refMessage = if(refresh > 0) " (${refresh})" else ""
                it.text = "$weekString$refMessage"
                it.setTextColor(
                    when (dayOfWeek) {
                        Calendar.SUNDAY -> Color.RED
                        Calendar.SATURDAY -> Color.BLUE
                        else -> Color.BLACK
                    }
                )
//                refresh++
            }
        }
    }
}