package com.tpu.mobile.timetracker.ProjectInfo.Pager;

import com.tpu.mobile.timetracker.Database.Model.StatTask;
import com.tpu.mobile.timetracker.Database.Model.Task;

/**
 * Created by Igorek on 25.11.2017.
 */

public class ModelTaskStat {
    Task task;
    StatTask stat;

    public ModelTaskStat()
    {
        task = null;
        stat = null;
    }

    public ModelTaskStat(Task task)
    {
        this.task =task;
        stat = null;
    }

    public ModelTaskStat(StatTask stat)
    {
        task = null;
        this.stat = stat;
    }

    public Task getTask() {
        return task;
    }

    public void setTask(Task task) {
        this.task = task;
    }

    public StatTask getStat() {
        return stat;
    }

    public void setStat(StatTask stat) {
        this.stat = stat;
    }
}
