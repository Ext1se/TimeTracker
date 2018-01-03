package com.tpu.mobile.timetracker.TaskEdit.Pager;

/**
 * Created by Igorek on 26.11.2017.
 */

public class ChangeStat {
    String id;
    int state;
    String description;
    long durationManual;
    long startManual;
    long endManual;
    boolean isChanged;


    public ChangeStat()
    {
        isChanged = false;
        id = "1";
        state = -1;
        description = null;
        durationManual = -1;
        startManual = -1;
        endManual = -1;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public int getState() {
        return state;
    }

    public void setState(int state) {
        this.state = state;
        isChanged = true;
    }

    public long getDurationManual() {
        return durationManual;
    }

    public void setDurationManual(long durationManual) {
        this.durationManual = durationManual;
        isChanged = true;
    }

    public long getStartManual() {
        return startManual;
    }

    public void setStartManual(long startManual) {
        this.startManual = startManual;
        isChanged = true;
    }

    public long getEndManual() {
        return endManual;
    }

    public void setEndManual(long endManual) {
        this.endManual = endManual;
        isChanged = true;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
        isChanged = true;
    }
}
