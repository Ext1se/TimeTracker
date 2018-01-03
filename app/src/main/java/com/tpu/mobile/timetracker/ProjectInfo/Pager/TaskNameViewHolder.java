package com.tpu.mobile.timetracker.ProjectInfo.Pager;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import com.tpu.mobile.timetracker.R;

/**
 * Created by Igorek on 03.11.2017.
 */

public class TaskNameViewHolder extends RecyclerView.ViewHolder {
    TextView tvName;
    TextView tvResult;
    Context context;

    public TaskNameViewHolder(View view, Context context) {
        super(view);
        this.context = context;
        tvName = (TextView)view.findViewById(R.id.tvTaskName);
        tvResult = (TextView)view.findViewById(R.id.tvResult);
    }
}
