package com.tpu.mobile.timetracker.Database.Controller;

import android.content.Intent;
import android.util.Log;
import android.view.View;

import com.apollographql.apollo.ApolloCall;
import com.apollographql.apollo.ApolloClient;
import com.apollographql.apollo.api.Response;
import com.apollographql.apollo.rx2.Rx2Apollo;
import com.tpu.mobile.timetracker.Database.Model.Project;
import com.tpu.mobile.timetracker.Database.Model.StatTask;
import com.tpu.mobile.timetracker.Database.Model.Task;
import com.tpu.mobile.timetracker.Main.MainActivity;
import com.tpu.mobile.timetracker.TaskEdit.Pager.PageStatistics;

import java.util.ArrayList;
import java.util.List;

import api.CreateTask;
import api.GetTasks;
import api.RemoveTask;
import api.StartTask;
import api.StopTimeEntry;
import api.UpdateTask;
import api.UpdateTimeEntry;
import api.type.TaskInput;
import api.type.TimeEntryInput;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.observers.DisposableObserver;
import io.realm.Realm;
import io.realm.RealmList;
import io.realm.RealmResults;
import io.realm.Sort;

/**
 * Created by Igorek on 25.12.2017.
 */

public class TaskController {
    Realm realm;
    ApolloClient client;

    public TaskController(Realm realm, ApolloClient client) {
        this.realm = realm;
        this.client = client;
    }

    public void createTask(String id, String name, String description, String idProject) {
        Task task = new Task();
        task.setId(id);
        task.setName(name);
        task.setDescription(description);
        task.setTimeCreated(System.currentTimeMillis());
        task.setState(Task.TASK_CREATED);
        task.setDuration(0);
        task.setProject(realm.where(Project.class).equalTo("id", idProject).findFirst());
        realm.beginTransaction();
        realm.copyToRealm(task);
        realm.commitTransaction();
    }

    public RealmResults<Task> createTasks(List<GetTasks.Task> tasks, List<String> ids) {
        if (tasks == null) return null;
        for (int i = 0; i < tasks.size(); i++) {
            realm.beginTransaction();
            Task task = realm.createObject(Task.class);
            task.setId(tasks.get(i).id());
            task.setName(tasks.get(i).name());
            task.setDescription(tasks.get(i).description());
            task.setProject(realm.where(Project.class).equalTo("id", ids.get(i)).findFirst());
            for (int j = 0; j < tasks.get(i).timeEntries().size(); j++) {
                StatTask stat = realm.createObject(StatTask.class);
                stat.setId(tasks.get(i).timeEntries().get(j).id());
                stat.setCreate(tasks.get(i).timeEntries().get(j).crdate());
                stat.setStart(tasks.get(i).timeEntries().get(j).startDate());
                if (!tasks.get(i).timeEntries().get(j).endDate().equals(""))
                    stat.setEnd(tasks.get(i).timeEntries().get(j).endDate());
                else
                    stat.setEnd(tasks.get(i).timeEntries().get(j).startDate());
                stat.setDuration(tasks.get(i).timeEntries().get(j).duration());
                stat.setTask(realm.where(Task.class).equalTo("id", task.getId()).findFirst());
                task.getStatistics().add(stat);
            }

            if (task.getStatistics().size() != 0) {
                StatTask stat = realm.where(StatTask.class).equalTo("task.id", task.getId()).findAllSorted("create", Sort.DESCENDING).first();
                task.setTimeCreated(stat.getCreate());
                task.setTimeStart(stat.getStart());
                task.setTimeFinish(stat.getEnd());
                task.setDuration(stat.getDuration());
                task.setIdActiveStat(stat.getId());
                if (stat.getStart() == 0)
                    task.setState(Task.TASK_CREATED);
                else if (stat.getEnd() == stat.getStart())
                    task.setState(Task.TASK_RUNNING);
                else
                    task.setState(Task.TASK_STOPPED);
            }
            else
            {
                task.setState(Task.TASK_CREATED);
                task.setTimeCreated(tasks.get(i).crdate());
            }
            realm.commitTransaction();
        }
        return realm.where(Task.class).findAllSorted("create", Sort.DESCENDING);
    }

    public Task getTask(String idTask) {
        return realm.where(Task.class).equalTo("id", idTask).findFirst();
    }

    public RealmResults<Task> getTasks() {
        return realm.where(Task.class).findAllSorted("create", Sort.DESCENDING);
    }

    public RealmResults<Task> getTasksOfProject(String idProject) {
        return realm.where(Task.class).equalTo("project.id", idProject).findAllSorted("create", Sort.DESCENDING);
    }

    public void startTask(String idTask, StartTask.StartTask1 timeEntry) {
        Task task = realm.where(Task.class).equalTo("id", idTask).findFirst();
        realm.beginTransaction();
        task.setTimeCreated(timeEntry.crdate());
        task.setTimeStart(System.currentTimeMillis());
        task.setState(Task.TASK_RUNNING);
        task.setDuration(0);
        StatTask stat = realm.createObject(StatTask.class);
        stat.setId(timeEntry.id());
        stat.setCreate(timeEntry.crdate());
        stat.setDuration(0);
        stat.setStart(timeEntry.startDate());
        stat.setEnd(timeEntry.crdate());
        stat.setTask(realm.where(Task.class).equalTo("id", idTask).findFirst());
        task.getStatistics().add(stat);
        task.setIdActiveStat(stat.getId());
        realm.commitTransaction();
    }

    public void finishTask(String idTask, StopTimeEntry.StopTimeEntry1 timeEntry) {
        Task task = realm.where(Task.class).equalTo("id", idTask).findFirst();
        StatTask stat = realm.where(StatTask.class).equalTo("id", task.getIdActiveStat()).findFirst();
        realm.beginTransaction();
        task.setState(Task.TASK_STOPPED);
        task.setTimeFinish(timeEntry.endDate());
        task.setDuration(timeEntry.duration());
        stat.setCreate(timeEntry.crdate());
        stat.setStart(timeEntry.startDate());
        stat.setEnd(timeEntry.endDate());
        stat.setDuration(timeEntry.duration());
        realm.commitTransaction();
    }

    public void removeTask(String idTask) {
        Task task = realm.where(Task.class).equalTo("id", idTask).findFirst();
        RealmList<StatTask> stats = task.getStatistics();
        realm.beginTransaction();
        stats.deleteAllFromRealm();
        task.deleteFromRealm();
        realm.commitTransaction();
    }

    public void updateTask(String idTask, String name, String description, String idProject)
    {
        Task task = realm.where(Task.class).equalTo("id", idTask).findFirst();
        realm.beginTransaction();
        task.setName(name);
        task.setDescription(description);
        task.setProject(realm.where(Project.class).equalTo("id", idProject).findFirst());
        realm.commitTransaction();
    }

    public void updateStat(String idStat, long start, long end, long duration)
    {
        StatTask stat = realm.where(StatTask.class).equalTo("id", idStat).findFirst();
        realm.beginTransaction();
        stat.setDuration(duration);
        stat.setStart(start);
        stat.setEnd(end);
        realm.commitTransaction();
    }

    public void createTaskApollo(final TaskInput task, final String idProject, final ApolloResponse apolloResponse)
    {
        Rx2Apollo.from(client.mutate(new CreateTask(idProject, task)))
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(new DisposableObserver<Response<CreateTask.Data>>() {
                    @Override
                    public void onNext(Response<CreateTask.Data> dataResponse) {
                        if (dataResponse.errors().isEmpty())
                            createTask(dataResponse.data().createTask(), task.name(), task.description(), idProject);
                        Log.d("myLog", "createTask-data:" + dataResponse.data());
                        Log.d("myLog", "createTask-error:" + dataResponse.errors());
                        apolloResponse.onNext(dataResponse.errors().isEmpty());
                    }

                    @Override
                    public void onError(Throwable e) {
                        e.printStackTrace();
                        apolloResponse.onError();
                    }

                    @Override
                    public void onComplete() {
                        apolloResponse.onComplete();
                    }
                });
    }

    public void updateTaskApollo(final String id, final TaskInput task, final String idProject, final ApolloResponse apolloResponse)
    {
        Rx2Apollo.from(client.mutate(new UpdateTask(id, task)))
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(new DisposableObserver<Response<UpdateTask.Data>>() {
                    @Override
                    public void onNext(Response<UpdateTask.Data> dataResponse) {
                        if (dataResponse.errors().isEmpty()) {
                            if (dataResponse.data().updateTask())
                                updateTask(id, task.name(), task.description(), idProject);
                        }
                        Log.d("myLog", "updateTask-data:" + dataResponse.data());
                        Log.d("myLog", "updateTask-error:" + dataResponse.errors());
                        apolloResponse.onNext(dataResponse.errors().isEmpty());
                    }

                    @Override
                    public void onError(Throwable e) {
                        e.printStackTrace();
                        apolloResponse.onError();
                    }

                    @Override
                    public void onComplete() {
                        apolloResponse.onComplete();
                    }
                });
    }

    public void updateStat(final StatTask stat, TimeEntryInput timeEntry, final ApolloResponse apolloResponse)
    {
        Rx2Apollo.from(client.mutate(new UpdateTimeEntry(stat.getId(), timeEntry)))
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(new DisposableObserver<Response<UpdateTimeEntry.Data>>() {
                    @Override
                    public void onNext(Response<UpdateTimeEntry.Data> dataResponse) {
                        if (dataResponse.errors().isEmpty()) {
                            if (dataResponse.data().updateTimeEntry())
                                updateStat(stat.getId(), stat.getStart(), stat.getEnd(), stat.getDuration());
                        }
                        Log.d("myLog", "updateStat-data:" + dataResponse.data());
                        Log.d("myLog", "updateStat-error:" + dataResponse.errors());
                        apolloResponse.onNext(dataResponse.errors().isEmpty());
                    }

                    @Override
                    public void onError(Throwable e) {
                        e.printStackTrace();
                        apolloResponse.onError();
                    }

                    @Override
                    public void onComplete() {
                        apolloResponse.onComplete();
                    }
                });
    }

    public void removeTaskApollo(final String id, final ApolloResponse apolloResponse)
    {
        Rx2Apollo.from(client.mutate(new RemoveTask(id)))
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(new DisposableObserver<Response<RemoveTask.Data>>() {
                    @Override
                    public void onNext(Response<RemoveTask.Data> dataResponse) {
                        if (dataResponse.errors().isEmpty()) {
                            if (dataResponse.data().removeTask())
                                removeTask(id);
                        }
                        Log.d("myLog", "removeTask-data:" + dataResponse.data());
                        Log.d("myLog", "removeTask-error:" + dataResponse.errors());
                        apolloResponse.onNext(dataResponse.errors().isEmpty());
                    }

                    @Override
                    public void onError(Throwable e) {
                        e.printStackTrace();
                        apolloResponse.onError();
                    }

                    @Override
                    public void onComplete() {
                        apolloResponse.onComplete();
                    }
                });
    }

    public void startTaskApollo(final String id, final ApolloResponse apolloResponse)
    {
        Rx2Apollo.from(client.mutate(new StartTask(id)))
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(new DisposableObserver<Response<StartTask.Data>>() {
                    @Override
                    public void onNext(Response<StartTask.Data> dataResponse) {
                        if (dataResponse.errors().isEmpty()) {
                            startTask(id, dataResponse.data().startTask());
                        }
                        Log.d("myLog", "startTask-data:" + dataResponse.data());
                        Log.d("myLog", "startTask-error:" + dataResponse.errors());
                        apolloResponse.onNext(dataResponse.errors().isEmpty());
                    }

                    @Override
                    public void onError(Throwable e) {
                        e.printStackTrace();
                        apolloResponse.onError();
                    }

                    @Override
                    public void onComplete() {
                        apolloResponse.onComplete();
                    }
                });
    }

    public void finishTaskApollo(final Task task, final ApolloResponse apolloResponse)
    {
        Rx2Apollo.from(client.mutate(new StopTimeEntry(task.getIdActiveStat())))
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(new DisposableObserver<Response<StopTimeEntry.Data>>() {
                    @Override
                    public void onNext(Response<StopTimeEntry.Data> dataResponse) {
                        if (dataResponse.errors().isEmpty()) {
                            finishTask(task.getId(), dataResponse.data().stopTimeEntry());
                        }
                        Log.d("myLog", "stopTask-data:" + dataResponse.data());
                        Log.d("myLog", "stopTask-error:" + dataResponse.errors());
                        apolloResponse.onNext(dataResponse.errors().isEmpty());
                    }

                    @Override
                    public void onError(Throwable e) {
                        e.printStackTrace();
                        apolloResponse.onError();
                    }

                    @Override
                    public void onComplete() {
                        apolloResponse.onComplete();
                    }
                });
    }

    public void loadTasks(String ownerID, final ApolloResponse apolloResponse)
    {
        ApolloCall<GetTasks.Data> response = client.query(new GetTasks(ownerID));
        Rx2Apollo.from(response)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(new DisposableObserver<Response<GetTasks.Data>>() {
                    @Override
                    public void onNext(Response<GetTasks.Data> dataResponse) {
                        if (dataResponse.errors().isEmpty()) {
                            List<GetTasks.Task> tsks = new ArrayList<GetTasks.Task>();
                            List<String> ids = new ArrayList<String>();
                            for (int i = 0; i < dataResponse.data().workspaces().size(); i++) {
                                for (int j = 0; j < dataResponse.data().workspaces().get(i).projects().size(); j++) {
                                    for (int k = 0; k < dataResponse.data().workspaces().get(i).projects().get(j).tasks().size(); k++)
                                    {
                                        tsks.add(dataResponse.data().workspaces().get(i).projects().get(j).tasks().get(k));
                                        ids.add(dataResponse.data().workspaces().get(i).projects().get(j).id());
                                    }
                                }
                            }
                            createTasks(tsks, ids);
                            Log.d("myLog", "loadTasks-data:" + dataResponse.data());
                            Log.d("myLog", "loadTasks-error:" + dataResponse.errors());
                            apolloResponse.onNext(dataResponse.errors().isEmpty());
                        }
                    }

                    @Override
                    public void onError(Throwable e) {
                        e.printStackTrace();
                        apolloResponse.onError();
                    }

                    @Override
                    public void onComplete() {
                        apolloResponse.onComplete();
                    }
                });
    }
}
