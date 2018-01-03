package com.tpu.mobile.timetracker.Workspace;

import android.os.Bundle;
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
import com.tpu.mobile.timetracker.Database.Controller.ApolloResponse;
import com.tpu.mobile.timetracker.Database.Controller.WorkspaceController;
import com.tpu.mobile.timetracker.Database.Model.Workspace;
import com.tpu.mobile.timetracker.MainApplication;
import com.tpu.mobile.timetracker.R;

import api.CreateWorkspace;
import api.UpdateWorkspace;
import api.type.WorkspaceInput;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.observers.DisposableObserver;
import io.realm.Realm;

/**
 * Created by Igorek on 06.11.2017.
 */

public class WorkspaceEditActivity extends AppCompatActivity {
    ApolloClient client;
    Realm realm;
    WorkspaceController workspaceController;
    Workspace workspace;
    EditText etName, etDescription;
    Button tvSave;
    String ownerId;
    String id;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.workspace_activity_edit);
        client = ((MainApplication)getApplication()).getApolloClient();
        realm = ((MainApplication)getApplication()).getRealm();
        workspaceController = new WorkspaceController(realm, client);
        id = getIntent().getStringExtra("workspaceID");
        ownerId = ((MainApplication)getApplication()).getPreferences().getString("idOwner", "");
        workspace = realm.where(Workspace.class).equalTo("id", id).findFirst();

        etName = (EditText) findViewById(R.id.etName);
        etName.setText(workspace.getName());

        etDescription = (EditText) findViewById(R.id.etDescription);
        etDescription.setText(workspace.getDescription());

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
                editProject();
            }
        });
    }

    private void editProject()
    {
        final String name = etName.getText().toString();
        final String description = etDescription.getText().toString();

        final WorkspaceInput workspaceInput = WorkspaceInput.builder()
                .ownerId(ownerId)
                .name(name)
                .description(description)
                .build();

        workspaceController.updateWorkspaceApollo(workspace.getId(), workspaceInput, new ApolloResponse() {
            @Override
            public void onNext(boolean success) {
                if (!success)
                    Toast.makeText(WorkspaceEditActivity.this,
                            "Workspace with this name already exist!", Toast.LENGTH_SHORT);
            }

            @Override
            public void onError() {
                tvSave.setEnabled(true);
                tvSave.setClickable(true);
            }

            @Override
            public void onComplete() {
                tvSave.setEnabled(true);
                tvSave.setClickable(true);
                onBackPressed();
            }
        });
    }
}
