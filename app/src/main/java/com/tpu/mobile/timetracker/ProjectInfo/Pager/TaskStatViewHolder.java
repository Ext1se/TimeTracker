package com.tpu.mobile.timetracker.ProjectInfo.Pager;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
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
    LinearLayout layoutNote;
    TextView tvTime, tvStartDate, tvEndDate, tvNote;
    Context context;

    public TaskStatViewHolder(View view, Context context) {
        super(view);
        this.context = context;
        layoutTracker = (LinearLayout) view.findViewById(R.id.layoutTracker);
        layoutNote = (LinearLayout) view.findViewById(R.id.layoutNote);
        tvNote = (TextView)view.findViewById(R.id.tvNote);
        tvTime = (TextView)view.findViewById(R.id.tvTime);
        tvStartDate = (TextView)view.findViewById(R.id.tvStartDate);
        tvEndDate = (TextView)view.findViewById(R.id.tvEndDate);
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
        return new SimpleDateFormat("dd MMMM yyyy HH:mm").format(new Date(date));
    }

    public String calculateTime(long time)
    {
        long result = time / 1000;
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(time);
        int hour = (int) result / 3600;
        int min = (int) (result % 3600) / 60;
        int sec = (int) (result % 60);;
        Log.d("myLog", "time = " + String.format("%02d:%02d:%02d", hour, min, sec)); //ВЕРНО
        return String.format("%02d:%02d:%02d", hour, min, sec);
    }
}
