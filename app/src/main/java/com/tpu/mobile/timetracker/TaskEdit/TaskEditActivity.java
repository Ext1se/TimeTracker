package com.tpu.mobile.timetracker.TaskEdit;

import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.apollographql.apollo.ApolloClient;
import com.tpu.mobile.timetracker.Database.Controller.ProjectController;
import com.tpu.mobile.timetracker.Database.Controller.TaskController;
import com.tpu.mobile.timetracker.MainApplication;
import com.tpu.mobile.timetracker.R;
import com.tpu.mobile.timetracker.TaskEdit.Pager.PageAdapter;
import com.tpu.mobile.timetracker.TaskEdit.Pager.PageMain;
import com.tpu.mobile.timetracker.TaskEdit.Pager.PageStatistics;

import io.realm.Realm;

/**
 * Created by Igorek on 06.11.2017.
 */

public class TaskEditActivity extends AppCompatActivity {
    ApolloClient client;
    Realm realm;
    ProjectController projectController;
    TaskController taskController;
    ViewPager viewPager;
    ProgressBar progressBar;
    PageAdapter pagerAdapter;
    FragmentManager fragmentManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.task_activity_edit);
        client = ((MainApplication)getApplication()).getApolloClient();
        realm = ((MainApplication)getApplication()).getRealm();
        projectController = new ProjectController(realm, client);
        taskController = new TaskController(realm, client);
        TabLayout tabLayout = (TabLayout) findViewById(R.id.tabs);
        fragmentManager = getSupportFragmentManager();
        progressBar = (ProgressBar) findViewById(R.id.progressBar);
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

        final Button bSave = (Button) findViewById(R.id.tvSave);
        bSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                bSave.setEnabled(false);
                bSave.setClickable(false);
                PageMain pageMain = (PageMain) fragmentManager.getFragments().get(0);
                pageMain.Save();
                //PageStatistics pageStat = (PageStatistics) fragmentManager.getFragments().get(1);
                //pageStat.Save();
                //onBackPressed();
            }
        });
    }
}
