package com.tpu.mobile.timetracker.TaskEdit.Pager;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Paint;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.tpu.mobile.timetracker.R;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * Created by Igorek on 03.11.2017.
 */

public class TaskStatViewHolder extends RecyclerView.ViewHolder {
    LinearLayout layoutTracker;
    //Button bAuto, bManual;
    TextView tvTime, tvStartDate, tvEndDate, tvNote;
    //Ограничения для установки времени в режиме Manual
    int hourTask, minTask, secTask;
    long start, end, result;
    EditText etDescription;
    Context context;

    public TaskStatViewHolder(View view, Context context) {
        super(view);
        this.context = context;
        layoutTracker = (LinearLayout) view.findViewById(R.id.layoutTracker);
/*        bAuto = (Button) view.findViewById(R.id.bAuto);
        bManual = (Button) view.findViewById(R.id.bManual);*/
        tvNote = (TextView)view.findViewById(R.id.tvNote);
        tvTime = (TextView)view.findViewById(R.id.tvTime);
        tvStartDate = (TextView)view.findViewById(R.id.tvStartDate);
        tvEndDate = (TextView)view.findViewById(R.id.tvEndDate);
        etDescription = (EditText)view.findViewById(R.id.etNote);
        etDescription.setHorizontallyScrolling(false);
        etDescription.setMaxLines(8);
    }

    public void init(long start, long end)
    {
        tvTime.setEnabled(true);
        tvStartDate.setEnabled(true);
        tvEndDate.setEnabled(true);
        tvTime.setTextColor(Color.DKGRAY);
        tvStartDate.setTextColor(Color.DKGRAY);
        tvEndDate.setTextColor(Color.DKGRAY);
        //tvTime.setPaintFlags(0);
        //tvStartDate.setPaintFlags(0);
        //tvEndDate.setPaintFlags(0);
        tvTime.setPaintFlags(tvTime.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
        tvStartDate.setPaintFlags(tvStartDate.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
        tvEndDate.setPaintFlags(tvEndDate.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);

        this.start = start;
        this.end = end;
        result = (end - start) / 1000;
        if (result < 0)
        {
            hourTask = 0;
            minTask = 0;
        }
        else
        {
            hourTask = (int) result / 3600;
            minTask = (int) (result % 3600) / 60;
        }
    }


    public void initAuto()
    {
        //bAuto.getBackground().setColorFilter(context.getResources().getColor(R.color.colorAccent), PorterDuff.Mode.SRC_ATOP);
        //bManual.getBackground().setColorFilter(Color.LTGRAY, PorterDuff.Mode.SRC_ATOP);
        tvTime.setEnabled(false);
        tvStartDate.setEnabled(false);
        tvEndDate.setEnabled(false);
        tvTime.setTextColor(Color.GRAY);
        tvStartDate.setTextColor(Color.GRAY);
        tvEndDate.setTextColor(Color.GRAY);
        tvTime.setPaintFlags(0);
        tvStartDate.setPaintFlags(0);
        tvEndDate.setPaintFlags(0);
    }

    public void initManual(long start, long end)
    {
        //bManual.getBackground().setColorFilter(context.getResources().getColor(R.color.colorAccent), PorterDuff.Mode.SRC_ATOP);
        //bAuto.getBackground().setColorFilter(Color.LTGRAY, PorterDuff.Mode.SRC_ATOP);
        tvTime.setEnabled(true);
        tvStartDate.setEnabled(true);
        tvEndDate.setEnabled(true);
        tvTime.setTextColor(Color.BLACK);
        tvStartDate.setTextColor(Color.BLACK);
        tvEndDate.setTextColor(Color.BLACK);
        tvTime.setPaintFlags(tvTime.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
        tvStartDate.setPaintFlags(tvStartDate.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
        tvEndDate.setPaintFlags(tvEndDate.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);

        this.start = start;
        this.end = end;
        result = (end - start) / 1000;
        if (result < 0)
        {
            hourTask = 0;
            minTask = 0;
        }
        else
        {
            hourTask = (int) result / 3600;
            minTask = (int) (result % 3600) / 60;
        }
    }

    public void calculateTime()
    {
        String time;
        result = (end - start) / 1000;
        if (result < 0)
        {
            hourTask = 0;
            minTask = 0;
            secTask = 0;
            time = "00:00:00";
        }
        else
        {
            hourTask = (int) result / 3600;
            minTask = (int) (result % 3600) / 60;
            time = String.format("%02d:%02d:00", hourTask, minTask);
        }

        tvTime.setText(time);
    }

    public String calculateDate(long date)
    {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(date);
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        int min = calendar.get(Calendar.MINUTE);
        Log.d("myLog", "date = " + String.format("%02d.%02d.%02d %02d:%02d", day, month, year, hour, min));
        return new SimpleDateFormat("dd.MM.yyyy HH:mm").format(new Date(date));
    }

    public String calculateTime(long time)
    {
        long result = time / 1000;

        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(time);
        int hour = (int) result / 3600;
        int min = (int) (result % 3600) / 60;
        int sec = (int) (result % 60);;
        return String.format("%02d:%02d:%02d", hour, min, sec);
    }
}
