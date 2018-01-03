package com.tpu.mobile.timetracker.TaskEdit.Pager;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.apollographql.apollo.ApolloClient;
import com.tpu.mobile.timetracker.Database.Controller.ProjectController;
import com.tpu.mobile.timetracker.Database.Controller.TaskController;
import com.tpu.mobile.timetracker.Database.Model.StatTask;
import com.tpu.mobile.timetracker.Database.Model.Task;
import com.tpu.mobile.timetracker.MainApplication;
import com.tpu.mobile.timetracker.R;

import java.util.List;

import io.realm.Realm;
import io.realm.RealmResults;

/**
 * Created by Igorek on 01.11.2017.
 */

public class PageStatistics extends Fragment  {
    ApolloClient client;
    Realm realm;
    ProjectController projectController;
    TaskController taskController;
    RecyclerView recyclerView;
    RecyclerAdapterStatistics adapter;
    String idTask;

    public PageStatistics() {
    }


    public static PageStatistics newInstance() {
        PageStatistics fragment = new PageStatistics();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.task_page_stat, container, false);
        client = ((MainApplication)getActivity().getApplication()).getApolloClient();
        realm = ((MainApplication)getActivity().getApplication()).getRealm();
        projectController = new ProjectController(realm, client);
        taskController = new TaskController(realm, client);
        idTask = getActivity().getIntent().getStringExtra("taskID");
        Task task = taskController.getTask(idTask);
        if (task.getState() != Task.TASK_CREATED) {
            RealmResults<StatTask> stats = realm.where(StatTask.class).equalTo("task.id", task.getId()).findAllSorted("create");
            List<StatTask> results = realm.copyFromRealm(stats);
            recyclerView = (RecyclerView) view.findViewById(R.id.recyclerView);
            recyclerView.setLayoutManager(new LinearLayoutManager(this.getContext(),
                    LinearLayoutManager.VERTICAL, false));
            if (results != null)
                recyclerView.setItemViewCacheSize(results.size());
            adapter = new RecyclerAdapterStatistics(this.getContext(), taskController, results);
            recyclerView.setAdapter(adapter);
        }
        return view;
    }

    public void Save()
    {
        if (taskController.getTask(idTask).getState() != Task.TASK_CREATED)
            adapter.Save();
        else
            getActivity().onBackPressed();
    }
}