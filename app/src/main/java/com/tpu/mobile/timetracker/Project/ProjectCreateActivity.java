package com.tpu.mobile.timetracker.Project;

import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.apollographql.apollo.ApolloClient;
import com.apollographql.apollo.api.Response;
import com.apollographql.apollo.rx2.Rx2Apollo;
import com.jaredrummler.android.colorpicker.ColorPickerView;
import com.tpu.mobile.timetracker.Database.Controller.ApolloResponse;
import com.tpu.mobile.timetracker.Database.Controller.ProjectController;
import com.tpu.mobile.timetracker.Database.Controller.WorkspaceController;
import com.tpu.mobile.timetracker.Database.Model.Project;
import com.tpu.mobile.timetracker.Database.Model.Workspace;
import com.tpu.mobile.timetracker.MainApplication;
import com.tpu.mobile.timetracker.R;
import com.tpu.mobile.timetracker.Workspace.WorkspaceEditActivity;

import java.util.ArrayList;
import java.util.List;

import api.CreateProject;
import api.GetProjects;
import api.type.ProjectInput;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.observers.DisposableObserver;
import io.realm.Realm;

/**
 * Created by Igorek on 06.11.2017.
 */

public class ProjectCreateActivity extends AppCompatActivity implements ColorPickerView.OnColorChangedListener {
    ApolloClient client;
    Realm realm;
    WorkspaceController workspaceController;
    ProjectController projectController;
    EditText etName, etDescription, etWorkspace;
    Button tvSave;
    ColorPickerView colorPickerView;
    int color;
    List<Workspace> workspaces;
    Workspace workspace;
    String[] workspacesName;
    String idWorkspace;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.project_activity_create);
        client = ((MainApplication)getApplication()).getApolloClient();
        realm = ((MainApplication)getApplication()).getRealm();
        workspaceController = new WorkspaceController(realm, client);
        projectController = new ProjectController(realm, client);
        idWorkspace = getIntent().getStringExtra("idWorkspace");
        workspaces = workspaceController.getWorkspaces();
        if (idWorkspace == null)
            idWorkspace = ((MainApplication)getApplication()).getPreferences().getString("idDefWorkspace", workspaces.get(0).getId());
        workspace = workspaceController.getWorkspace(idWorkspace);
        workspacesName = new String[workspaces.size()];
        for (int i = 0; i < workspaces.size(); i++)
            workspacesName[i] = workspaces.get(i).getName();
        etName = (EditText) findViewById(R.id.etName);
        etDescription = (EditText) findViewById(R.id.etDescription);
        etWorkspace = (EditText) findViewById(R.id.etWorkspace);
        etWorkspace.setText(workspace.getName());
        colorPickerView = (ColorPickerView) findViewById(R.id.colorView);
        colorPickerView.setOnColorChangedListener(this);
        color = Color.WHITE;
        colorPickerView.setColor(color, true);
        TextView tvBack = (TextView)findViewById(R.id.tvBack);
        tvBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });

        tvSave = (Button) findViewById(R.id.tvSave);
        tvSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (TextUtils.isEmpty(etName.getText().toString())) {
                    etName.setError("Name can't be empty!");
                    return;
                }
                if (etName.getText().toString().length() < 3) {
                    etName.setError("Name can't be less 3 symbols");
                    return;
                }
                tvSave.setEnabled(false);
                tvSave.setClickable(false);
                addProject();
            }
        });

        etWorkspace.setKeyListener(null);
        etWorkspace.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showWorkspaceDialog();
            }
        });

    }

    private void showWorkspaceDialog()
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Select a workspace").setIcon(R.drawable.workspace);
        builder.setItems(workspacesName, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                workspace = workspaces.get(i);
                etWorkspace.setText(workspace.getName());
            }
        });
        builder.setCancelable(true);
        builder.create();
        builder.show();
    }

    private void addProject()
    {
        final String name = etName.getText().toString();
        final String description = etDescription.getText().toString();
        ProjectInput projectInput = ProjectInput.builder()
                .name(name)
                //.description(description)
                .color(color)
                .build();

        projectController.createProjectApollo(projectInput, description, workspace.getId(), new ApolloResponse() {
            @Override
            public void onNext(boolean success) {
                if(!success )
                    Toast.makeText(ProjectCreateActivity.this, "Workspace with this name already exist!", Toast.LENGTH_SHORT);
            }

            @Override
            public void onError() {
                tvSave.setEnabled(true);
                tvSave.setClickable(true);
            }

            @Override
            public void onComplete() {
                onBackPressed();
            }
        });
    }

    @Override
    public void onColorChanged(int newColor) {
        color = colorPickerView.getColor();
    }
}
