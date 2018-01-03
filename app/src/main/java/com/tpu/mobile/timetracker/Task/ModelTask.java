package com.tpu.mobile.timetracker.Task;

import com.tpu.mobile.timetracker.Database.Model.Task;

/**
 * Created by Igorek on 25.11.2017.
 */

public class ModelTask {
    long date;
    Task task;

    public ModelTask()
    {
        task = null;
        date = -1;
    }

    public ModelTask(Task task)
    {
        this.task = task;
        date = -1;
    }

    public ModelTask(long date)
    {
        task = null;
        this.date = date;
    }

    public Task getTask() {
        return task;
    }

    public void setTask(Task task) {
        this.task = task;
    }

    public long getDate() {
        return date;
    }

    public void setDate(long date) {
        this.date = date;
    }
}
