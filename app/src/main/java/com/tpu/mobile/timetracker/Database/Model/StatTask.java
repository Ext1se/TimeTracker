package com.tpu.mobile.timetracker.Database.Model;

import java.util.UUID;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

/**
 * Created by Igorek on 02.11.2017.
 */

public class StatTask extends RealmObject {
    @PrimaryKey
    public String id = UUID.randomUUID().toString();
    public long duration;
    public long create;
    public long start;
    public long end;
    public String note;
    public Task task;

    public StatTask() {
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Task getTask() {
        return task;
    }

    public void setTask(Task task) {
        this.task = task;
    }

    public long getDuration() {
        return duration;
    }

    public void setDuration(long duration) {
        this.duration = duration;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }

    public long getCreate() {
        return create;
    }

    public void setCreate(long create) {
        this.create = create;
    }

    public void setCreate(String create) {
        this.create = Long.parseLong(create);
    }

    public long getStart() {
        return start;
    }

    public void setStart(long start) {
        this.start = start;
    }

    public void setStart(String start) {
        this.start = Long.parseLong(start);
    }

    public long getEnd() {
        return end;
    }

    public void setEnd(long end) {
        this.end = end;
    }

    public void setEnd(String end) {
        if (!end.equals(""))
            this.end = Long.parseLong(end);
        else
            this.end = getStart();
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }
}
