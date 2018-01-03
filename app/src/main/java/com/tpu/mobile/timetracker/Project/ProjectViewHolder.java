package com.tpu.mobile.timetracker.Project;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.chauthai.swipereveallayout.SwipeRevealLayout;
import com.tpu.mobile.timetracker.R;

/**
 * Created by Igorek on 21.10.2017.
 */

public class ProjectViewHolder extends RecyclerView.ViewHolder{
    public SwipeRevealLayout baseLayout;
    public RelativeLayout layoutClickable;
    public RelativeLayout frontLayout;
    public ImageView imageIndicator;
    public TextView textName;
    public TextView textDescription;
    public TextView textNumber;
    public Button bOpen, bEdit, bDelete, bInfo;
    public boolean isOpen = false;

    public ProjectViewHolder(View view) {
        super(view);
        baseLayout = (SwipeRevealLayout) view.findViewById(R.id.baseLayout);
        layoutClickable = (RelativeLayout) view.findViewById(R.id.layoutClickable);
        frontLayout = (RelativeLayout) view.findViewById(R.id.frontLayout);
        imageIndicator = (ImageView)view.findViewById(R.id.imageIndicator);
        textName = (TextView) view.findViewById(R.id.tvName);
        textDescription = (TextView) view.findViewById(R.id.tvDescription);
        textNumber = (TextView) view.findViewById(R.id.tvNumber);
        bInfo = (Button)view.findViewById(R.id.bInfo);
        bOpen = (Button)view.findViewById(R.id.bGo);
        bEdit = (Button)view.findViewById(R.id.bEdit);
        bDelete = (Button)view.findViewById(R.id.bDelete);
        baseLayout.close(false);
    }
}
