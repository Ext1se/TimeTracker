package com.tpu.mobile.timetracker.Main;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
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
import com.tpu.mobile.timetracker.Database.Controller.WorkspaceController;
import com.tpu.mobile.timetracker.Database.Model.Project;
import com.tpu.mobile.timetracker.Database.Model.Workspace;
import com.tpu.mobile.timetracker.MainApplication;
import com.tpu.mobile.timetracker.Project.ProjectCreateActivity;
import com.tpu.mobile.timetracker.Project.ProjectRecyclerViewAdapter;
import com.tpu.mobile.timetracker.R;
import com.tpu.mobile.timetracker.Workspace.WorkspaceEditActivity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import api.CreateWorkspace;
import api.GetProjects;
import api.GetWorkspaces;
import api.type.WorkspaceInput;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.observers.DisposableObserver;
import io.realm.Realm;
import io.realm.RealmResults;

public class ProjectFragment extends Fragment {
    ApolloClient client;
    Realm realm;
    WorkspaceController workspaceController;
    ProjectController projectController;
    RealmResults<Project> projects;
    RealmResults<Workspace> workspaces;
    Workspace workspace;
    String ownerID;
    RecyclerView recyclerView;
    ProgressBar progressBar;
    ProjectRecyclerViewAdapter projectAdapter;
    TextView tvWorkspace;
    String[] workspacesName;
    int pos = 0;
    String idWorkspace;

    public ProjectFragment(String ownerID) {
        this.ownerID = ownerID;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        View view = inflater.inflate(R.layout.xxx_project_app_bar_main, container, false);
        Toolbar toolbar = (Toolbar) view.findViewById(R.id.toolbar);
        ((MainActivity)getActivity()).setSupportActionBar(toolbar);
        DrawerLayout drawer = (DrawerLayout) getActivity().findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                getActivity(), drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();
        FloatingActionButton fab = (FloatingActionButton) view.findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                createProject();
            }
        });
        recyclerView = (RecyclerView) view.findViewById(R.id.recyclerView);
        progressBar = (ProgressBar) view.findViewById(R.id.progressBar);

        client = ((MainApplication)getActivity().getApplication()).getApolloClient();
        realm = ((MainApplication)getActivity().getApplication()).getRealm();
        workspaceController = new WorkspaceController(realm, client);
        projectController = new ProjectController(realm, client);
        idWorkspace = getActivity().getIntent().getStringExtra("workspaceID");
        workspaces = workspaceController.getWorkspaces();
        if (idWorkspace == null || idWorkspace.isEmpty()) {
            idWorkspace = "all";
            projects = projectController.getProjects();
        }
        else {
            if (workspaceController.getWorkspace(idWorkspace).getId() == null)
                idWorkspace = workspaces.get(0).getId();
            workspace = workspaceController.getWorkspace(idWorkspace);
            projects = projectController.getProjectsOfWs(idWorkspace);
        }

        projectAdapter = new ProjectRecyclerViewAdapter(getContext(), projectController, projects);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false));
        recyclerView.setAdapter(projectAdapter);
        recyclerView.setItemViewCacheSize(projects.size());

        createListWorkspaces(workspaces);
        tvWorkspace = (TextView) view.findViewById(R.id.tvWorkspace);
        if (idWorkspace.equals("all"))
            tvWorkspace.setText(workspacesName[0]);
        else
            tvWorkspace.setText(workspace.getName());
        tvWorkspace.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showWorkspaceDialog();
            }
        });
        return view;
    }

    private void createListWorkspaces(RealmResults<Workspace> workspaces)
    {
        if (workspaces == null)
        {
            workspacesName = new String[]{"All projects"};
            return;
        }
        workspacesName = new String[workspaces.size() + 1];
        workspacesName[0] = "All projects";
        for (int i = 0; i < workspaces.size(); i++)
            workspacesName[i + 1] = workspaces.get(i).getName();
    }

    private void createProject()
    {
        Intent intent = new Intent(getActivity(), ProjectCreateActivity.class);
        if (workspace == null)
            intent.putExtra("idWorkspace", workspaces.get(0).getId());
        else
            intent.putExtra("idWorkspace", workspace.getId());
        startActivity(intent);
    }

    private void showWorkspaceDialog()
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("Select a workspace").setIcon(R.drawable.workspace);
        builder.setItems(workspacesName, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                loadProjects(i);
            }
        });
        builder.setCancelable(true);
        builder.create();
        builder.show();
    }

    private void loadProjects(int pos)
    {
        this.pos = pos;
        tvWorkspace.setText(workspacesName[pos]);
        if (pos == 0) {
            projects = projectController.getProjects();
            workspace = null;
            idWorkspace = "all";
        }
        else {
            workspace = workspaces.get(pos - 1);
            idWorkspace = workspace.getId();
            projects = projectController.getProjectsOfWs(idWorkspace);
        }

        projectAdapter.setProjects(projects);
        recyclerView.setItemViewCacheSize(projects.size());
        projectAdapter.notifyDataSetChanged();
    }

    private void createWorkspace() {
        final View v = getActivity().getLayoutInflater().inflate(R.layout.dialog_workspace_create, null);
        final TextView tvName = (TextView) v.findViewById(R.id.tvName);
        final TextView tvDesc = (TextView) v.findViewById(R.id.tvDescription);

        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("  Create new workspace!")
                .setIcon(R.drawable.ic_folder_black)
                .setCancelable(true)
                .setPositiveButton("Add workspace", null)
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                    }
                });

        builder.setView(v);
        final AlertDialog alert = builder.create();

        alert.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialogInterface) {
                Button button = ((AlertDialog) alert).getButton(AlertDialog.BUTTON_POSITIVE);
                button.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        String name = tvName.getText().toString();
                        String description = tvDesc.getText().toString();

                        if (TextUtils.isEmpty(name)) {
                            tvName.setError("Name can't be empty!");
                            return;
                        }

                        progressBar.setVisibility(View.VISIBLE);
                        final WorkspaceInput workspace = WorkspaceInput.builder()
                                .ownerId(ownerID)
                                .name(name)
                                .description(description)
                                .build();

                        workspaceController.createWorkspaceApollo(workspace, new ApolloResponse() {
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
                                workspacesName = new String[workspaces.size() + 1];
                                createListWorkspaces(workspaces);
                                loadProjects(workspaces.size());
                            }
                        });
                        alert.dismiss();
                    }
                });
            }
        });
        alert.show();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.project, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.actionCreate) {
            createWorkspace();
            return true;
        }

        if (idWorkspace.equals("all") || idWorkspace.equals(workspaces.get(0).getId()))
        {
            Toast.makeText(getContext(), "Select a workspace", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (id == R.id.actionEdit) {
            Intent intent = new Intent(getActivity(), WorkspaceEditActivity.class);
            intent.putExtra("workspaceID", idWorkspace);
            startActivity(intent);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onResume() {
        createListWorkspaces(workspaceController.getWorkspaces());
        if (pos != 0)
            tvWorkspace.setText(workspacesName[pos]);
        projectAdapter.notifyDataSetChanged();
        super.onResume();
    }
}
