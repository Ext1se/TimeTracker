package com.tpu.mobile.timetracker.TaskEdit.Pager;

import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Chronometer;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;


import com.apollographql.apollo.ApolloClient;
import com.apollographql.apollo.api.Response;
import com.apollographql.apollo.rx2.Rx2Apollo;
import com.tpu.mobile.timetracker.Database.Controller.ApolloResponse;
import com.tpu.mobile.timetracker.Database.Controller.ProjectController;
import com.tpu.mobile.timetracker.Database.Controller.TaskController;
import com.tpu.mobile.timetracker.Database.Model.Project;
import com.tpu.mobile.timetracker.Database.Model.Task;
import com.tpu.mobile.timetracker.MainApplication;
import com.tpu.mobile.timetracker.R;


import java.util.List;

import api.UpdateTask;
import api.type.TaskInput;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.observers.DisposableObserver;
import io.realm.Realm;

/**
 * Created by Igorek on 01.11.2017.
 */

public class PageMain extends Fragment {
    ApolloClient client;
    Realm realm;
    ProjectController projectController;
    TaskController taskController;
    EditText etName, etDescription, etProject;
    Chronometer chronometer;
    ImageButton ibActive;
    ImageView imageIndicator;
    List<Project> projects;
    long step = 0;
    String idTask;
    Task task;
    Project project;

    public PageMain() {
    }

    public static PageMain newInstance() {
        PageMain fragment = new PageMain();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.task_page_info, container, false);
        client = ((MainApplication)getActivity().getApplication()).getApolloClient();
        realm = ((MainApplication)getActivity().getApplication()).getRealm();
        projectController = new ProjectController(realm, client);
        taskController = new TaskController(realm, client);

        idTask = getActivity().getIntent().getStringExtra("taskID");
        task = taskController.getTask(idTask);
        project = task.getProject();

        etName = (EditText) view.findViewById(R.id.etName);
        etProject = (EditText) view.findViewById(R.id.etProject);
        etDescription = (EditText) view.findViewById(R.id.etDescription);
        chronometer = (Chronometer) view.findViewById(R.id.tvTimeTask);
        ibActive = (ImageButton) view.findViewById(R.id.ibActive);
        imageIndicator = (ImageView) view.findViewById(R.id.imageIndicator);

        etProject.setKeyListener(null);
        etProject.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showProjectsDialog();
            }
        });

        projects = projectController.getProjects();

        etName.setText(task.getName());
        etDescription.setText(task.getDescription());
        etProject.setText(project.getName());
        return view;
    }

    private void showProjectsDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this.getContext());
        builder.setTitle("Select a project").setIcon(R.drawable.project);
        AdapterDialog adapter = new AdapterDialog(this.getContext(), R.layout.dialog_project_row, projects);
        builder.setAdapter(adapter, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int item) {
                if (project != projects.get(item)) {
                    etProject.setText(projects.get(item).getName());
                    project = projects.get(item);
                }
            }
        });
        builder.setCancelable(true);
        builder.create();
        builder.show();
    }

    public void Save() {

        final String name = etName.getText().toString();
        final String description = etDescription.getText().toString();
        final String idProject = project.getId();

        final TaskInput taskInput = TaskInput.builder()
                .name(name)
                .description(description)
                .projectId(idProject)
                .build();

        taskController.updateTaskApollo(task.getId(), taskInput, idProject, new ApolloResponse() {
            @Override
            public void onNext(boolean success) {
            }

            @Override
            public void onError() {
                PageStatistics pageStatistics = (PageStatistics) getFragmentManager().getFragments().get(1);
                pageStatistics.Save();
            }

            @Override
            public void onComplete() {
                PageStatistics pageStatistics = (PageStatistics) getFragmentManager().getFragments().get(1);
                pageStatistics.Save();
            }
        });
    }
}