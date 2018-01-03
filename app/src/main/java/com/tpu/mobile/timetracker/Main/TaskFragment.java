package com.tpu.mobile.timetracker.Main;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.apollographql.apollo.ApolloCall;
import com.apollographql.apollo.ApolloClient;
import com.apollographql.apollo.api.Response;
import com.apollographql.apollo.rx2.Rx2Apollo;
import com.tpu.mobile.timetracker.Database.Controller.ApolloResponse;
import com.tpu.mobile.timetracker.Database.Controller.ProjectController;
import com.tpu.mobile.timetracker.Database.Controller.TaskController;
import com.tpu.mobile.timetracker.Database.Controller.WorkspaceController;
import com.tpu.mobile.timetracker.Database.Model.Project;
import com.tpu.mobile.timetracker.Database.Model.Task;
import com.tpu.mobile.timetracker.Database.Model.Workspace;
import com.tpu.mobile.timetracker.MainApplication;
import com.tpu.mobile.timetracker.Project.ProjectCreateActivity;
import com.tpu.mobile.timetracker.Project.ProjectEditActivity;
import com.tpu.mobile.timetracker.R;
import com.tpu.mobile.timetracker.Task.AdapterDialog;
import com.tpu.mobile.timetracker.Task.ItemTaskTouchHelper.ItemTaskTouchHelper;
import com.tpu.mobile.timetracker.Task.ModelTask;
import com.tpu.mobile.timetracker.Task.TaskRecyclerViewAdapter;

import java.util.ArrayList;
import java.util.List;

import api.CreateTask;
import api.CreateWorkspace;
import api.GetProjects;
import api.GetTasks;
import api.GetWorkspaces;
import api.type.TaskInput;
import api.type.WorkspaceInput;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.observers.DisposableObserver;
import io.realm.Realm;
import io.realm.RealmResults;
import io.realm.Sort;

public class TaskFragment extends Fragment {
    ApolloClient client;
    Realm realm;
    WorkspaceController workspaceController;
    ProjectController projectController;
    TaskController taskController;
    RealmResults<Task> tasks;
    RealmResults<Project> projects;
    String ownerID;
    RecyclerView recyclerView;
    ProgressBar progressBar;
    TaskRecyclerViewAdapter taskAdapter;
    ItemTouchHelper touchHelper;
    TextView tvProject;
    Project project;
    List<ModelTask> models;
    String[] projectsName;
    int[] projectsColor;
    int pos = 0;
    String idProject;

    public TaskFragment(String ownerID)
    {
        this.ownerID = ownerID;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        View view = inflater.inflate(R.layout.xxx_task_app_bar_main, container, false);
        Toolbar toolbar = (Toolbar) view.findViewById(R.id.toolbar);
        ((MainActivity)getActivity()).setSupportActionBar(toolbar);
        ((MainActivity)getActivity()).getSupportActionBar().setDisplayShowTitleEnabled(false);
        DrawerLayout drawer = (DrawerLayout) getActivity().findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                getActivity(), drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();
        FloatingActionButton fab = (FloatingActionButton) view.findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                createTask();
            }
        });
        recyclerView = (RecyclerView) view.findViewById(R.id.recyclerView);
        progressBar = (ProgressBar) view.findViewById(R.id.progressBar);

        client = ((MainApplication)getActivity().getApplication()).getApolloClient();
        realm = ((MainApplication)getActivity().getApplication()).getRealm();
        workspaceController = new WorkspaceController(realm, client);
        projectController = new ProjectController(realm, client);
        taskController = new TaskController(realm, client);
        idProject = getActivity().getIntent().getStringExtra("projectID");
        projects = projectController.getProjects();
        if (idProject == null || idProject.isEmpty()){
            idProject = "all";
            tasks = taskController.getTasks();
        }
        else {
            if (projectController.getProject(idProject) == null)
                idProject = projects.get(0).getId();
            project = projectController.getProject(idProject);
            tasks = taskController.getTasksOfProject(idProject);
        }
        if (tasks.size() != 0)
            models = setData(tasks);
        taskAdapter = new TaskRecyclerViewAdapter(getContext(), taskController, models, tasks, project);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false));
        recyclerView.setNestedScrollingEnabled(false); //Если используется совместно с ScrollView
        recyclerView.setAdapter(taskAdapter);
        ItemTouchHelper.Callback callback = new ItemTaskTouchHelper(taskAdapter);
        touchHelper = new ItemTouchHelper(callback);
        touchHelper.attachToRecyclerView(recyclerView);
        if (models != null)
            recyclerView.setItemViewCacheSize(models.size());
        createListProjects(projects);
        tvProject = (TextView) view.findViewById(R.id.tvProject);
        if (idProject.equals("all"))
            tvProject.setText(projectsName[0]);
        else
            tvProject.setText(project.getName());
        tvProject.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showProjectDialog();
            }
        });
        return view;
    }


    private void createListProjects(RealmResults<Project> projects)
    {
        if (projects == null)
        {
            projectsName = new String[]{"All tasks"};
            return;
        }
        projectsName = new String[projects.size() + 1];
        projectsName[0] = "All tasks";
        projectsColor = new int[projects.size() + 1];
        projectsColor[0] = Color.WHITE;
        for (int i = 0; i < projects.size(); i++)
        {
            projectsName[i + 1] = projects.get(i).getName();
            projectsColor[i + 1] = projects.get(i).getColor();
        }
    }



    private void showProjectDialog()
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("Select a project").setIcon(R.drawable.project);
        AdapterDialog adapter = new AdapterDialog(getContext(), R.layout.dialog_project_row, projectsName, projectsColor);
        builder.setAdapter(adapter, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int item) {
                loadTasks(item);
            }
        });
        builder.setCancelable(true);
        builder.create();
        builder.show();
    }

    private void loadTasks(int pos)
    {
        this.pos = pos;
        tvProject.setText(projectsName[pos]);
        if (pos == 0) {
            tasks = taskController.getTasks();
            project = null;
            idProject = "all";
        }
        else {
            project = projects.get(pos - 1);
            idProject = project.getId();
            tasks = taskController.getTasksOfProject(idProject);
        }

        if (models != null)
            models.clear();
        models = setData(tasks);
        taskAdapter.setTasks(tasks);
        taskAdapter.setProject(project);
        taskAdapter.setModels(models);
        recyclerView.setItemViewCacheSize(tasks.size());
        taskAdapter.notifyDataSetChanged();
    }

    private void createTask()
    {
        final View v = (LinearLayout) getActivity().getLayoutInflater().inflate(R.layout.dialog_task_create, null);
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("  Create new task!")
                .setIcon(R.drawable.task)
                .setCancelable(true)
                .setNegativeButton("Add task",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                TextView tvName = (TextView) v.findViewById(R.id.tvNameTask);
                                TextView tvDesc = (TextView) v.findViewById(R.id.tvDescriptionTask);
                                String name = tvName.getText().toString();
                                final String description = tvDesc.getText().toString();
                                final String idPrj;
                                if (name.isEmpty() && project == null)
                                    name = "Task: " + projectController.getProject(projects.get(0).getId()).getName();
                                if (name.isEmpty())
                                    name = "Task: " + project.getName();
                                final String nameTask = name;
                                if (project == null)
                                    idPrj = projects.get(0).getId();
                                else
                                    idPrj = project.getId();
                                progressBar.setVisibility(View.VISIBLE);
                                final TaskInput task = TaskInput.builder()
                                        .name(name)
                                        .description(description)
                                        .projectId(idPrj)
                                        .build();
                                taskController.createTaskApollo(task, idPrj, new ApolloResponse() {
                                    @Override
                                    public void onNext(boolean success) {
                                    }

                                    @Override
                                    public void onError() {
                                        progressBar.setVisibility(View.GONE);
                                    }

                                    @Override
                                    public void onComplete() {
                                        progressBar.setVisibility(View.GONE);
                                        if (models != null)
                                            models.clear();
                                        models = setData(tasks);
                                        recyclerView.setItemViewCacheSize(models.size());
                                        taskAdapter.setModels(models);
                                        taskAdapter.notifyDataSetChanged();
                                    }
                                });
                            }
                        });
        builder.setView(v);
        AlertDialog alert = builder.create();
        alert.show();
    }


    public static List<ModelTask> setData(List<Task> tasks)
    {
        long day = 3600000; //Пока час
        List<ModelTask> models = new ArrayList<ModelTask>();
        Task task;
        if (tasks.size() != 0)
            task = tasks.get(0);
        else
            return null;

        long time = task.getTimeCreated() / day;
        models.add(new ModelTask(time));
        for (Task t : tasks)
        {
            long d = t.getTimeCreated() / day;
            if (d != time)
            {
                time = d;
                models.add(new ModelTask(time));
                models.add(new ModelTask(t));
            }
            else
                models.add(new ModelTask(t));
        }
        return models;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.task, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.actionCreate) {
            Intent intent = new Intent(getActivity(), ProjectCreateActivity.class);
            startActivity(intent);
            return true;
        }

        if (idProject.equals("all") || idProject.equals(projects.get(0).getId()))
        {
            Toast.makeText(getContext(), "Select a project", Toast.LENGTH_SHORT).show();
            return false;
        }


        if (id == R.id.actionEdit) {
            Intent intent = new Intent(getActivity(), ProjectEditActivity.class);
            intent.putExtra("projectID", idProject);
            startActivity(intent);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onResume() {
        createListProjects(projectController.getProjects());
        if (pos != 0)
            tvProject.setText(projectsName[pos]);
        taskAdapter.notifyDataSetChanged();
        super.onResume();
    }
}
