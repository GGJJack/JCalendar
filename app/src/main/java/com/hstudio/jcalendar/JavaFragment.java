package com.hstudio.jcalendar;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.hstudio.jcalendarview.JCalendarView;
import com.hstudio.jcalendarview.MonthChangeListener;

import org.jetbrains.annotations.NotNull;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class JavaFragment extends Fragment {

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        JCalendarView calendarView = view.findViewById(R.id.calendar);
        final TextView tvDate = view.findViewById(R.id.tv_date);
        final SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd hh:mm:ss", Locale.KOREA);
        final SampleAdapter adapter = new SampleAdapter();
        calendarView.setAdapter(adapter);
        adapter.setMonthChangeListener(new MonthChangeListener() {
            @Override
            public void monthChanged(@NotNull Date focusDate) {
                tvDate.setText(sdf.format(focusDate));
            }
        });
        view.findViewById(R.id.btn_left).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                adapter.beforeMonth();
            }
        });
        view.findViewById(R.id.btn_right).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                adapter.nextMonth();
            }
        });
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_main, container, false);
    }
}
