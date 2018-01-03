package com.tpu.mobile.timetracker.ProjectInfo.Pager;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.apollographql.apollo.ApolloClient;
import com.tpu.mobile.timetracker.Database.Controller.ProjectController;
import com.tpu.mobile.timetracker.Database.Controller.TaskController;
import com.tpu.mobile.timetracker.Database.Model.Project;
import com.tpu.mobile.timetracker.Database.Model.Task;
import com.tpu.mobile.timetracker.MainApplication;
import com.tpu.mobile.timetracker.R;

import java.text.SimpleDateFormat;
import java.util.Date;

import io.realm.Realm;
import io.realm.RealmResults;
import io.realm.Sort;

/**
 * Created by Igorek on 01.11.2017.
 */

public class PageMain extends Fragment {
    Realm realm;
    ApolloClient client;
    LinearLayout layout;
    TextView etName, etDescription, etWorkspace;
    TextView tvNumber;
    TextView tvStart, tvEnd;
    ProjectController projectController;
    TaskController taskController;
    Project project;
    RealmResults<Task> tasks;

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
        View view = inflater.inflate(R.layout.project_page_info, container, false);
        realm = ((MainApplication)getActivity().getApplication()).getRealm();
        client = ((MainApplication)getActivity().getApplication()).getApolloClient();
        projectController = new ProjectController(realm, client);
        taskController = new TaskController(realm, client);
        String idProject = getActivity().getIntent().getStringExtra("projectID");
        project = projectController.getProject(idProject);
        tasks = taskController.getTasksOfProject(idProject);
        layout = (LinearLayout) view.findViewById(R.id.layoutDescription);
        etName = (TextView) view.findViewById(R.id.etName);
        etWorkspace = (TextView) view.findViewById(R.id.etWorkspace);
        etDescription = (TextView) view.findViewById(R.id.etDescription);
        tvNumber = (TextView) view.findViewById(R.id.tvNumber);
        tvStart = (TextView) view.findViewById(R.id.tvStart);
        tvEnd = (TextView) view.findViewById(R.id.tvEnd);

        etName.setText(project.getName());
        
        String description = project.getDescription();
        if (description != null && !description.isEmpty()) {
            layout.setVisibility(View.VISIBLE);
            etDescription.setText(project.getDescription());

        } else
            layout.setVisibility(View.GONE);


        etWorkspace.setText(project.getWorkspace().getName());
        tvNumber.setText(Integer.toString(realm.where(Task.class).equalTo("project.id", idProject).findAll().size()));

        String startDate = new SimpleDateFormat("dd MMMM yyyy").format(new Date(project.getStart()));
        tvStart.setText(startDate);

        if (tasks.size() != 0) {
            long date = Math.max(
                    Math.max(tasks.where().max("timeCreated").longValue(), tasks.where().max("timeStart").longValue()),
                    tasks.where().max("timeFinish").longValue()
            );
            String endDate = new SimpleDateFormat("dd MMMM yyyy").format(new Date(date));
            tvEnd.setText(endDate);
        } else
            tvEnd.setText(startDate);
        return view;
    }
}