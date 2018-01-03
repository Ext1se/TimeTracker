package com.tpu.mobile.timetracker.Task;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.tpu.mobile.timetracker.Database.Model.Project;
import com.tpu.mobile.timetracker.Database.Model.Task;
import com.tpu.mobile.timetracker.Database.Model.User;
import com.tpu.mobile.timetracker.Database.Model.Workspace;
import com.tpu.mobile.timetracker.Project.ProjectCreateActivity;
import com.tpu.mobile.timetracker.Project.ProjectEditActivity;
import com.tpu.mobile.timetracker.R;
import com.tpu.mobile.timetracker.Task.ItemTaskTouchHelper.ItemTaskTouchHelper;
import com.tpu.mobile.timetracker.User.UserActivity;

import java.util.ArrayList;
import java.util.List;

import io.realm.Realm;
import io.realm.RealmResults;
import io.realm.Sort;

public class TaskActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    Realm realm;
    RealmResults<Task> tasks;
    RealmResults<Project> projects;
    RecyclerView recyclerView;
    TaskRecyclerViewAdapter taskAdapter;
    ItemTouchHelper touchHelper;
    TextView tvProject;
    Project project;
    List<ModelTask> models;

    String[] projectsName;
    int[] projectsColor;
    int pos = 0;
    String idProject;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.task_activity_main);
        Realm.init(this);
        realm = Realm.getDefaultInstance();
        checkUser();
        /////////////////////////////////////////////////////
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                createTask();
            }
        });

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        navigationView.setCheckedItem(R.id.nav_task);
        /////////////////////////////////////////////////////

        projects = realm.where(Project.class).findAll().sort("id");
        if (projects.size() == 0) initProject();

        idProject = getIntent().getStringExtra("projectID");
        if (idProject == "-1")
            tasks = realm.where(Task.class).findAllSorted("timeCreated", Sort.DESCENDING);
        else {
            project = realm.where(Project.class).equalTo("id", idProject).findFirst();
            tasks = realm.where(Task.class).equalTo("project.id", project.getId())
                    .findAllSorted("timeCreated", Sort.DESCENDING);
        }

        if (tasks.size() != 0)
            models = setData(tasks);
        recyclerView = (RecyclerView) findViewById(R.id.recyclerView);
        //taskAdapter = new TaskRecyclerViewAdapter(this, models, tasks, project, realm);
        recyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        recyclerView.setAdapter(taskAdapter);
        ItemTouchHelper.Callback callback = new ItemTaskTouchHelper(taskAdapter);
        touchHelper = new ItemTouchHelper(callback);
        touchHelper.attachToRecyclerView(recyclerView);
        if (models != null)
            recyclerView.setItemViewCacheSize(models.size());
        createProjects(projects);

        tvProject = (TextView) findViewById(R.id.tvProject);
        if (idProject == "-1")
            tvProject.setText(projectsName[0]);
        else
            tvProject.setText(project.getName());
        tvProject.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showProjectDialog();
            }
        });

    }

    private void createProjects(RealmResults<Project> projects)
    {
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

    private void initProject()
    {
        Workspace workspace = new Workspace();
        workspace.setName("No workspace");
        workspace.setDescription("No description");
        //workspace.setId(1);
        realm.beginTransaction();
        realm.copyToRealm(workspace);
        realm.commitTransaction();

        Project project = new Project();
        project.setName("No project");
        project.setDescription("No description");
        project.setId("1");
        project.setColor(Color.LTGRAY);
        project.setWorkspace(realm.where(Workspace.class).findFirst());
        project.setStart(System.currentTimeMillis());
        realm.beginTransaction();
        realm.copyToRealm(project);
        realm.commitTransaction();
    }

    private void showProjectDialog()
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Select a project").setIcon(R.drawable.project);
        AdapterDialog adapter = new AdapterDialog(this, R.layout.dialog_project_row, projectsName, projectsColor);
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
            tasks = realm.where(Task.class).findAllSorted("timeCreated", Sort.DESCENDING);
            project = null;
            idProject = "-1";
        }
        else {
            project = projects.get(pos - 1);
            idProject = project.getId();
            tasks = realm.where(Task.class).equalTo("project.id", project.getId())
                    .findAllSorted("timeCreated", Sort.DESCENDING);
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
        final View v = (LinearLayout) getLayoutInflater().inflate(R.layout.dialog_task_create, null);
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("  Create new task!")
                .setIcon(R.drawable.task)
                .setCancelable(true)
                .setNegativeButton("Add task",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                TextView tvName = (TextView) v.findViewById(R.id.tvNameTask);
                                TextView tvDesc = (TextView) v.findViewById(R.id.tvDescriptionTask);
                                String name = tvName.getText().toString();
                                String description = tvDesc.getText().toString();

                                if (name.isEmpty() && project == null)
                                    name = "Task: " + realm.where(Project.class).equalTo("id", 1).findFirst().getName();
                                if (name.isEmpty())
                                    name = "Task: " + project.getName();

                                realm.beginTransaction();
                                Task taskModel = realm.createObject(Task.class);
                                taskModel.setName(name);
                                taskModel.setDescription(description);
                                taskModel.setTimeCreated(System.currentTimeMillis());
                                taskModel.setState(Task.TASK_CREATED);
                                taskModel.setDuration(0);
                                //taskModel.setId(realm.where(Task.class).max("id").intValue() + 1);
                                if (project == null)
                                    taskModel.setProject(realm.where(Project.class).equalTo("id", 1).findFirst());
                                else
                                    taskModel.setProject(realm.where(Project.class).equalTo("id", project.getId()).findFirst());
                                realm.commitTransaction();

                                if (models != null)
                                    models.clear();
                                models = setData(tasks);
                                recyclerView.setItemViewCacheSize(models.size());
                                taskAdapter.setModels(models);
                                taskAdapter.notifyDataSetChanged();
                            }
                        });
        builder.setView(v);
        AlertDialog alert = builder.create();
        alert.show();
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.task, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.actionCreate) {
            Intent intent = new Intent(this, ProjectCreateActivity.class);
            startActivity(intent);
            return true;
        }

        if (idProject == "-1" || idProject == "1")
        {
            Toast.makeText(this, "Select a project", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (id == R.id.actionEdit) {
            Intent intent = new Intent(this, ProjectEditActivity.class);
            intent.putExtra("projectID", idProject);
            startActivity(intent);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume() {
        createProjects(realm.where(Project.class).findAll().sort("id"));
        if (pos != 0)
            tvProject.setText(projectsName[pos]);
        taskAdapter.notifyDataSetChanged();
        super.onResume();
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        final int id = item.getItemId();
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);

        drawer.addDrawerListener(new DrawerLayout.DrawerListener() {
            @Override
            public void onDrawerSlide(View drawerView, float slideOffset) {
            }

            @Override
            public void onDrawerOpened(View drawerView) {
            }

            @Override
            public void onDrawerClosed(View drawerView) {
                    loadActivity(id);
            }

            @Override
            public void onDrawerStateChanged(int newState) {
            }
        });

        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    private void loadActivity(int id)
    {
        Intent intent = null;
        if (id == R.id.nav_task)
            return;

        intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
        startActivity(intent);
        //overridePendingTransition(0,0);
    }

    public static List<ModelTask> setData(List<Task> tasks)
    {
        long day = 3600000; //Пока час для тестирования
        List<ModelTask> models = new ArrayList<ModelTask>();
        Task task;
        if (tasks.size() != 0)
            task = tasks.get(0);
        else
            return null;

        long time = task.getTimeCreated() / day;
        models.add(new ModelTask(time));
        //models.add(new ModelTask(task));

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

    private void checkUser()
    {
        if (realm.where(User.class).equalTo("id", 0).findFirst() == null)
        {
            Intent intent = new Intent(this, UserActivity.class);
            startActivity(intent);
        }
    }
}
