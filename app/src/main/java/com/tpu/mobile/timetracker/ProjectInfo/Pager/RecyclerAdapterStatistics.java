package com.tpu.mobile.timetracker.ProjectInfo.Pager;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.tpu.mobile.timetracker.Database.Model.StatTask;
import com.tpu.mobile.timetracker.Database.Model.Task;
import com.tpu.mobile.timetracker.R;

import java.util.List;

import io.realm.Realm;
import io.realm.RealmList;


/**
 * Created by Igorek on 01.11.2017.
 */

public class RecyclerAdapterStatistics extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    Context context;
    List<ModelTaskStat> models;
    List<Task> tasks;
    RealmList<StatTask> stats;
    Realm realm;
    int types[];

    public RecyclerAdapterStatistics(Context context, List<Task> tasks, RealmList<StatTask> stats, List<ModelTaskStat> models,
                                     Realm realm) {
        this.context = context;
        this.stats = stats;
        this.tasks = tasks;
        this.models = models;
        this.realm = realm;
        types = new int[models.size()];
        for (int i = 0; i < models.size(); i++) {
            if (models.get(i).getTask() == null)
                types[i] = 1;
            else
                types[i] = 0;
        }
    }

    @Override
    public int getItemViewType(int position) {
        return (types[position] == 1) ? 1 : 0;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (viewType == 0)
            return new TaskNameViewHolder(LayoutInflater.from(context).inflate(
                    R.layout.project_item_stat_name, parent, false), context);
        else
            return new TaskStatViewHolder(LayoutInflater.from(context).inflate(
                    R.layout.project_item_stat, parent, false), context);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, final int position) {
        if (holder.getItemViewType() == 0) {
            final TaskNameViewHolder vh = (TaskNameViewHolder) holder;
            Task task = models.get(position).getTask();
            vh.tvName.setText(task.getName());
            if (task.getStatistics().size() != 0)
            {
                vh.tvResult.setVisibility(View.GONE);
            }
        } else {
            final TaskStatViewHolder vh = (TaskStatViewHolder) holder;
            final StatTask stat = models.get(position).getStat();


            vh.tvStartDate.setText(vh.calculateDate(stat.getStart()));
            vh.tvEndDate.setText(vh.calculateDate(stat.getEnd()));
            vh.tvTime.setText(vh.calculateTime(stat.getDuration()));

            String note = stat.getNote();
            if (note != null) {
                if (!note.isEmpty()) {
                    vh.layoutNote.setVisibility(View.VISIBLE);
                    vh.tvNote.setText(note);
                } else
                    vh.layoutNote.setVisibility(View.GONE);
            } else
                vh.layoutNote.setVisibility(View.GONE);
        }
    }


    @Override
    public int getItemCount() {
        return models.size();
    }

}
