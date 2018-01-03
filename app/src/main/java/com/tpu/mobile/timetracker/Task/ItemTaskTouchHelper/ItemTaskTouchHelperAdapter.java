package com.tpu.mobile.timetracker.Task.ItemTaskTouchHelper;

import android.support.v7.widget.RecyclerView;

public interface ItemTaskTouchHelperAdapter {
    boolean onItemMove(int fromPosition, int toPosition);
    void onItemDismiss(RecyclerView.ViewHolder viewHolder);
}
