package com.tpu.mobile.timetracker.Task;

import android.content.Context;
import android.content.Intent;
import android.os.SystemClock;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.apollographql.apollo.ApolloClient;
import com.apollographql.apollo.api.Response;
import com.apollographql.apollo.rx2.Rx2Apollo;
import com.tpu.mobile.timetracker.Database.Controller.ApolloResponse;
import com.tpu.mobile.timetracker.Database.Controller.ProjectController;
import com.tpu.mobile.timetracker.Database.Controller.TaskController;
import com.tpu.mobile.timetracker.Database.Model.Project;
import com.tpu.mobile.timetracker.Database.Model.Task;
import com.tpu.mobile.timetracker.R;
import com.tpu.mobile.timetracker.Task.ItemTaskTouchHelper.ItemTaskTouchHelperAdapter;
import com.tpu.mobile.timetracker.TaskEdit.TaskEditActivity;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import api.RemoveTask;
import api.StartTask;
import api.StopTimeEntry;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.observers.DisposableObserver;
import io.realm.Realm;

/**
 * Created by Igorek on 12.10.2017.
 */

public class TaskRecyclerViewAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>
        implements ItemTaskTouchHelperAdapter{
    Context context;
    TaskController taskController;
    List<ModelTask> models;
    List<Task> tasks;
    Project project;
    int types[];

    public TaskRecyclerViewAdapter(Context context, TaskController taskController, List<ModelTask> models, List<Task> tasks, Project project) {
        this.context = context;
        this.tasks = tasks;
        this.project = project;
        this.taskController = taskController;
        setModels(models);
    }

    @Override
    public int getItemViewType(int position) {
        return (types[position] == 1) ? 1:0;
    }

    public void setTasks(List<Task> tasks) {
        this.tasks = tasks;
    }

    public void setProject(Project project)
    {
        this.project = project;
    }

    public void setModels(List<ModelTask> models)
    {
        this.models = models;
        if (models != null) {
            types = new int[models.size()];
            for (int i = 0; i < models.size(); i++) {
                if (models.get(i).getDate() == -1)
                    types[i] = 1;
                else
                    types[i] = 0;
            }
        }
    }

    @Override
    public int getItemCount() {
        if (models != null)
            return models.size();
        else
            return 0;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (viewType == 0)
            return new DateViewHolder(LayoutInflater.from(context).inflate(
                    R.layout.task_item_date, parent, false), context);
        else
            return new TaskViewHolder(LayoutInflater.from(context).inflate(
                    R.layout.task_item_main, parent, false));
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, final int position) {
        if (holder.getItemViewType() == 0) {
            final DateViewHolder vh = (DateViewHolder) holder;
            long date = models.get(position).getDate() * 3600000; //ЧАС
            String time = new SimpleDateFormat("dd MMMM yyyy HH:mm").format(new Date(date));
            vh.tvName.setText(time);
        }
        else {
            final TaskViewHolder vh = (TaskViewHolder) holder;
            final Task task = models.get(position).getTask();
            vh.textName.setText(task.getName());
            if (project == null) {
                Project project = task.getProject();
                vh.imageIndicator.setBackgroundColor(project.getColor());
                vh.textDescription.setText(project.getName());
            } else {
                vh.imageIndicator.setBackgroundColor(project.getColor());
                if (task.getDescription() == null || task.getDescription().equals(""))
                    vh.textDescription.setText("No description");
                else
                    vh.textDescription.setText(task.getDescription());
            }

            if (task.getState() == Task.TASK_CREATED)
                vh.create();

            if (task.getState() == Task.TASK_RUNNING) {
                vh.setStep(task.getTimeStart());
                vh.start();
            }

            if (task.getState() == Task.TASK_STOPPED) {
                vh.setStep(task.getDuration());
                vh.chronometer.setBase(SystemClock.elapsedRealtime() - vh.getStep());
                vh.stop();
            }

            View.OnClickListener onClickActive = new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (task.getState() == Task.TASK_STOPPED ||
                            task.getState() == Task.TASK_CREATED) {
                        vh.ibActive.setClickable(false);
                        vh.layoutClickable.setClickable(false);
                        startTask(vh, task);
                        return;
                    }

                    if (task.getState() == Task.TASK_RUNNING) {
                        vh.ibActive.setClickable(false);
                        vh.layoutClickable.setClickable(false);
                        finishTask(vh, task);
                        return;
                    }
                }
            };
            vh.ibActive.setOnClickListener(onClickActive);

            vh.layoutClickable.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(context, TaskEditActivity.class);
                    intent.putExtra("taskID", task.getId());
                    if (project == null) {
                        Project prj = task.getProject();
                        intent.putExtra("projectID", prj.getId());
                    } else
                        intent.putExtra("projectID", project.getId());
                    context.startActivity(intent);
                }
            });
        }
    }

    private void startTask(final TaskViewHolder vh, final Task task)
    {
        vh.progressBar.setVisibility(View.VISIBLE);
        taskController.startTaskApollo(task.getId(), new ApolloResponse() {
            @Override
            public void onNext(boolean success) {
            }

            @Override
            public void onError() {
                vh.progressBar.setVisibility(View.GONE);
                vh.layoutClickable.setClickable(true);
                vh.ibActive.setClickable(true);
            }

            @Override
            public void onComplete() {
                vh.progressBar.setVisibility(View.GONE);
                vh.layoutClickable.setClickable(true);
                vh.ibActive.setClickable(true);
                vh.start();
                update();
            }
        });
    }

    private void finishTask(final TaskViewHolder vh, final Task task)
    {
        vh.progressBar.setVisibility(View.VISIBLE);

        taskController.finishTaskApollo(task, new ApolloResponse() {
            @Override
            public void onNext(boolean success) {
            }

            @Override
            public void onError() {
                vh.progressBar.setVisibility(View.GONE);
                vh.layoutClickable.setClickable(true);
                vh.ibActive.setClickable(true);
            }

            @Override
            public void onComplete() {
                vh.progressBar.setVisibility(View.GONE);
                vh.ibActive.setClickable(true);
                vh.layoutClickable.setClickable(true);
                vh.stop();
                update();
            }
        });
    }


    public void update()
    {
        setModels(TaskActivity.setData(tasks));
        notifyDataSetChanged();
    }

    @Override
    public boolean onItemMove(int fromPosition, int toPosition) {
        return false;

    }

    @Override
    public void onItemDismiss(RecyclerView.ViewHolder viewHolder) {
        final int position = viewHolder.getAdapterPosition();
        final Task task = models.get(position).getTask();
        final String idTask = task.getId();

        taskController.removeTaskApollo(idTask, new ApolloResponse() {
            @Override
            public void onNext(boolean success) {
            }

            @Override
            public void onError() {
            }

            @Override
            public void onComplete() {
                update();
            }
        });
    }
}