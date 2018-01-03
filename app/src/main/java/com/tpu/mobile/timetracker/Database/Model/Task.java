package com.tpu.mobile.timetracker.Database.Model;

import java.util.UUID;

import io.realm.RealmList;
import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

/**
 * Created by Igorek on 13.10.2017.
 */

public class Task extends RealmObject {
    @PrimaryKey
    public String id = UUID.randomUUID().toString();
    public String name;
    public String description;
    public long create;
    public long start;
    public long end;
    public static final int TASK_CREATED = 0;
    public static final int TASK_RUNNING = 1;
    public static final int TASK_STOPPED = 2;
    public int state;
    public long duration;
    public RealmList<StatTask> stats;
    public String idActiveStat;
    public Project project;

    public Task()
    {
        state = TASK_CREATED;
        duration = 0;
        stats = new RealmList<StatTask>();
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public int getState() {
        return state;
    }

    public void setState(int state) {
        this.state = state;
    }

    public long getTimeCreated() {
        return create;
    }

    public void setTimeCreated(long timeCreated) {
        this.create = timeCreated;
    }

    public void setTimeCreated(String timeCreated) {
        this.create = Long.parseLong(timeCreated);
    }

    public long getTimeStart() {
        long currentTime = System.currentTimeMillis();
        long step = currentTime - start;
        long d = getDuration() + step;
        return d;
    }

    public void setTimeStart(long timeStart) {
        this.start = timeStart;
    }

    public void setTimeStart(String timeStart) {
        this.start = Long.parseLong(timeStart);
    }

    public long getTimeFinish() {
        return end;
    }

    public void setTimeFinish(long timeFinish) {
        this.end = timeFinish;
    }

    public void setTimeFinish(String timeFinish) {
        this.end = Long.parseLong(timeFinish);
    }

    public long getDuration() {
        return duration;
    }

    public void setDuration(long duration) {
        this.duration = duration;
    }

    public RealmList<StatTask> getStatistics() {
        return stats;
    }

    public void setStatistics(RealmList<StatTask> statistics) {
        this.stats = statistics;
    }

    public Project getProject() {
        return project;
    }

    public void setProject(Project project) {
        this.project = project;
    }

    public String getIdActiveStat() {
        return idActiveStat;
    }

    public void setIdActiveStat(String idActiveStat) {
        this.idActiveStat = idActiveStat;
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof Task)) return false;
        Task task = (Task) obj;
        return this.getId().equals(task.getId());
    }
}
