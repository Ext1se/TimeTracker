package com.tpu.mobile.timetracker.Database.Model;

import java.util.UUID;

import io.realm.RealmList;
import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

/**
 * Created by Igorek on 01.11.2017.
 */

public class Workspace extends RealmObject {
    @PrimaryKey
    public String id = UUID.randomUUID().toString();
    public String name;
    public String description;
    public Long create;

    public Workspace() {
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Long getTimeCreated() {
        return create;
    }

    public void setTimeCreated(String createDate) {
        this.create = Long.parseLong(createDate);
    }

    public void setTimeCreated(Long createDate) {
        this.create = createDate;
    }
}
