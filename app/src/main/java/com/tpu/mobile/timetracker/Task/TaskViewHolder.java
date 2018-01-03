package com.tpu.mobile.timetracker.Task;

import android.graphics.Color;
import android.os.SystemClock;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Chronometer;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.tpu.mobile.timetracker.R;

/**
 * Created by Igorek on 21.10.2017.
 */

public class TaskViewHolder extends RecyclerView.ViewHolder{
    public ProgressBar progressBar;
    public RelativeLayout baseLayout;
    public RelativeLayout layoutClickable;
    public RelativeLayout frontLayout;
    public ImageView imageIndicator;
    public TextView textName;
    public TextView textDescription;
    public Chronometer chronometer;
    public ImageButton ibActive;
    long step = 0;

    public TaskViewHolder(View view) {
        super(view);
        baseLayout = (RelativeLayout) view.findViewById(R.id.baseLayout);
        progressBar = (ProgressBar) view.findViewById(R.id.progressBar);
        layoutClickable = (RelativeLayout) view.findViewById(R.id.layoutClickable);
        frontLayout = (RelativeLayout) view.findViewById(R.id.frontLayout);
        imageIndicator = (ImageView)view.findViewById(R.id.imageIndicator);
        textName = (TextView) view.findViewById(R.id.tvNameTask);
        textDescription = (TextView) view.findViewById(R.id.tvDescriptionTask);
        chronometer = (Chronometer) view.findViewById(R.id.tvTimeTask);
        ibActive = (ImageButton) view.findViewById(R.id.ibActive);
    }

    public void create() {
        ibActive.setImageResource(R.drawable.ic_play);
        frontLayout.setBackgroundColor(Color.WHITE);
        step = 0;
        chronometer.setBase(SystemClock.elapsedRealtime());
        chronometer.stop();
        chronometer.setTextColor(Color.DKGRAY);
    }

    public void start() {
        ibActive.setImageResource(R.drawable.ic_stop);
        chronometer.setBase(SystemClock.elapsedRealtime() - step);
        chronometer.start();
        chronometer.setTextColor(Color.parseColor("#1ABC9C"));
    }

    public void stop() {
        ibActive.setImageResource(R.drawable.ic_play);
        chronometer.stop();
        step = 0;
        chronometer.setTextColor(Color.RED);
    }

    public long getCurrentStep() {
        long r = SystemClock.elapsedRealtime() - chronometer.getBase();
        r = r / 1000 * 1000;
        return r;
    }

    public long getStep() {
        return step;
    }

    public void setStep(long step) {
        this.step = step;
    }
}
