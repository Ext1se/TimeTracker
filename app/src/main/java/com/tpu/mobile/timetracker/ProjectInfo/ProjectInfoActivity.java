package com.tpu.mobile.timetracker.ProjectInfo;

import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.TextView;

import com.apollographql.apollo.ApolloClient;
import com.tpu.mobile.timetracker.Database.Controller.ProjectController;
import com.tpu.mobile.timetracker.Database.Controller.TaskController;
import com.tpu.mobile.timetracker.Database.Model.Project;
import com.tpu.mobile.timetracker.MainApplication;
import com.tpu.mobile.timetracker.ProjectInfo.Pager.PageAdapter;
import com.tpu.mobile.timetracker.R;

import io.realm.Realm;

/**
 * Created by Igorek on 06.11.2017.
 */

public class ProjectInfoActivity extends AppCompatActivity {
    ViewPager viewPager;
    PageAdapter pagerAdapter;
    FragmentManager fragmentManager;
    ApolloClient client;
    Realm realm;
    ProjectController projectController;
    TaskController taskController;
    String idProject;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.project_activity_info);
        client = ((MainApplication)getApplication()).getApolloClient();
        realm = ((MainApplication)getApplication()).getRealm();
        projectController = new ProjectController(realm, client);
        taskController = new TaskController(realm, client);
        idProject = getIntent().getStringExtra("projectID");
        Project project = projectController.getProject(idProject);
        TextView tvName = (TextView)findViewById(R.id.tvNameProject);
        tvName.setText(project.getName());
        TabLayout tabLayout = (TabLayout) findViewById(R.id.tabs);
        fragmentManager = getSupportFragmentManager();
        viewPager = (ViewPager)findViewById(R.id.container);
        tabLayout.setupWithViewPager(viewPager);
        pagerAdapter = new PageAdapter(this.getSupportFragmentManager());
        viewPager.setAdapter(pagerAdapter);

        TextView tvBack = (TextView)findViewById(R.id.tvBack);
        tvBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });
    }
}
