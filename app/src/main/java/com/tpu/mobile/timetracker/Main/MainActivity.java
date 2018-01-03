package com.tpu.mobile.timetracker.Main;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.apollographql.apollo.ApolloCall;
import com.apollographql.apollo.ApolloClient;
import com.apollographql.apollo.api.Response;
import com.apollographql.apollo.rx2.Rx2Apollo;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.tpu.mobile.timetracker.Database.Controller.ApolloResponse;
import com.tpu.mobile.timetracker.Database.Controller.ProjectController;
import com.tpu.mobile.timetracker.Database.Controller.TaskController;
import com.tpu.mobile.timetracker.Database.Controller.WorkspaceController;
import com.tpu.mobile.timetracker.Database.Model.User;
import com.tpu.mobile.timetracker.MainApplication;
import com.tpu.mobile.timetracker.R;
import com.tpu.mobile.timetracker.User.UserActivity;

import java.util.ArrayList;
import java.util.List;

import api.GetProjects;
import api.GetTasks;
import api.GetWorkspaces;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.observers.DisposableObserver;
import io.realm.Realm;
import jp.wasabeef.glide.transformations.CropCircleTransformation;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {
    ApolloClient client;
    Realm realm;
    WorkspaceController workspaceController;
    ProjectController projectController;
    TaskController taskController;
    GoogleSignInAccount account;
    GoogleSignInClient mGoogleSignInClient;
    GoogleSignInOptions gso;
    String ownerID;
    ProgressBar progressBar;
    int idFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.xxx_activity_main);
        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        View header = navigationView.getHeaderView(0);
        TextView tvName = (TextView)header.findViewById(R.id.tvName);
        TextView tvEmail = (TextView)header.findViewById(R.id.tvMail);
        ImageView photo = (ImageView)header.findViewById(R.id.imagePhoto);
        client = ((MainApplication)getApplication()).getApolloClient();
        realm = ((MainApplication)getApplication()).getRealm();
        workspaceController = new WorkspaceController(realm, client);
        projectController = new ProjectController(realm, client);
        taskController = new TaskController(realm, client);
        account = GoogleSignIn.getLastSignedInAccount(this);
        ownerID = ((MainApplication)getApplication()).getPreferences().getString("idOwner", "0");
        tvName.setText(account.getDisplayName());
        tvEmail.setText(account.getEmail());
        Glide.with(this)
                .load(account.getPhotoUrl())
                .asBitmap()
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .skipMemoryCache(false)
                .transform(new CropCircleTransformation(this))
                .into(photo);
        gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.server_client_id))
                .requestEmail()
                .build();
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        progressBar = (ProgressBar) findViewById(R.id.progressBar);
        idFragment = getIntent().getIntExtra("fragmentID", R.id.nav_task);
        checkRealm();
        navigationView.setCheckedItem(idFragment);
    }


    private void checkRealm()
    {
        if (workspaceController.getWorkspaces().size() == 0)
            loadWorkspaces();
        else
            loadFragment(idFragment);
    }

    private void loadWorkspaces()
    {
        progressBar.setVisibility(View.VISIBLE);
        workspaceController.loadWorkspaces(ownerID, new ApolloResponse() {
            @Override
            public void onNext(boolean success) {
            }

            @Override
            public void onError() {
                progressBar.setVisibility(View.GONE);
            }

            @Override
            public void onComplete() {
                loadProjects();
            }
        });
    }

    private void loadProjects()
    {
        progressBar.setVisibility(View.VISIBLE);
        projectController.loadProjects(ownerID, new ApolloResponse() {
            @Override
            public void onNext(boolean success) {
            }

            @Override
            public void onError() {
                progressBar.setVisibility(View.GONE);
            }

            @Override
            public void onComplete() {
                loadTasks();
            }
        });
    }

    private void loadTasks()
    {
        progressBar.setVisibility(View.VISIBLE);
        taskController.loadTasks(ownerID, new ApolloResponse() {
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
                loadFragment(idFragment);
            }
        });
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

    private void displaySelectedScreen(final int itemId) {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.setDrawerListener(new DrawerLayout.DrawerListener() {
            @Override
            public void onDrawerSlide(View drawerView, float slideOffset) {
            }

            @Override
            public void onDrawerOpened(View drawerView) {
            }

            @Override
            public void onDrawerClosed(View drawerView) {
                loadFragment(itemId);
            }

            @Override
            public void onDrawerStateChanged(int newState) {
            }
        });

        drawer.closeDrawer(GravityCompat.START);
    }

    private void loadFragment(int itemId)
    {
        Fragment fragment = null;
        switch (itemId) {
            case R.id.nav_workspace:
                fragment = new WorkspaceFragment(ownerID);
                break;
            case R.id.nav_project:
                fragment = new ProjectFragment(ownerID);
                break;
            case R.id.nav_task:
                fragment = new TaskFragment(ownerID);
                break;
            case R.id.nav_logout:
                mGoogleSignInClient.signOut()
                        .addOnCompleteListener(this, new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                Intent intent = new Intent(MainActivity.this, UserActivity.class);
                                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                                startActivity(intent);
                                finish();
                                return;
                            }
                        });
                return;
        }

        if (fragment != null) {
            FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
            ft.replace(R.id.content_frame, fragment);
            ft.commit();
        }
    }


    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        displaySelectedScreen(item.getItemId());
        return true;
    }

    @Override
    protected void onStart() {
        super.onStart();
        account = GoogleSignIn.getLastSignedInAccount(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
    }
}