package com.tpu.mobile.timetracker.User;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Chronometer;
import android.widget.Toast;

import com.apollographql.apollo.ApolloCall;
import com.apollographql.apollo.ApolloClient;
import com.apollographql.apollo.api.Response;
import com.apollographql.apollo.rx2.Rx2Apollo;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.tpu.mobile.timetracker.Database.Controller.UserController;
import com.tpu.mobile.timetracker.Database.Model.User;
import com.tpu.mobile.timetracker.MainApplication;
import com.tpu.mobile.timetracker.R;
import com.tpu.mobile.timetracker.Main.MainActivity;

import api.AuthUser;
import api.CreateProject;
import api.CreateUser;
import api.CreateWorkspace;
import api.GetWorkspace;
import api.GetWorkspaces;
import api.type.ProjectInput;
import api.type.UserInput;
import api.type.WorkspaceInput;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.observers.DisposableObserver;
import io.realm.Realm;

/**
 * Created by Igorek on 30.11.2017.
 */

public class UserActivity extends AppCompatActivity implements View.OnClickListener {
    private static final String TAG = "myLogUserActivity";
    private static final int RC_SIGN_IN = 9002;
    ApolloClient client;
    Realm realm;
    UserController userController;
    Chronometer chronometer;
    SignInButton signInButton;
    GoogleSignInClient mGoogleSignInClient;
    GoogleSignInOptions gso;
    SharedPreferences preferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.autorization_activity);
        client = ((MainApplication)getApplication()).getApolloClient();
        realm = ((MainApplication)getApplication()).getRealm();
        preferences = ((MainApplication)getApplication()).getPreferences();
        userController = new UserController(realm);
        chronometer = (Chronometer)findViewById(R.id.chronometer);
        chronometer.start();
        signInButton = (SignInButton)findViewById(R.id.bSign);
        signInButton.setOnClickListener(this);
        validateServerClientID();
        gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.server_client_id))
                .requestEmail()
                .build();
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.bSign:
                signIn();
                break;
        }
    }

    private void signIn() {
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            handleSignInResult(task);
        }
    }

    private void handleSignInResult(Task<GoogleSignInAccount> completedTask) {
        try {
            GoogleSignInAccount account = completedTask.getResult(ApiException.class);
            authUser(account);
        } catch (ApiException e) {
            Log.w(TAG, ":failed code=" + e.getStatusCode());
        }
    }

    private void validateServerClientID() {
        String serverClientId = getString(R.string.server_client_id);
        String suffix = ".apps.googleusercontent.com";
        if (!serverClientId.trim().endsWith(suffix)) {
            String message = "Invalid server client ID in strings.xml, must end with " + suffix;
            Log.w(TAG, message);
            Toast.makeText(this, message, Toast.LENGTH_LONG).show();
        }
    }

    private void authUser(GoogleSignInAccount account)
    {
        Rx2Apollo.from(client.mutate(new AuthUser(account.getIdToken())))
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(new DisposableObserver<Response<AuthUser.Data>>() {
                    @Override
                    public void onNext(Response<AuthUser.Data> dataResponse) {
                        if (dataResponse.errors().isEmpty()) {
                            preferences.edit().putString("idOwner", dataResponse.data().auth().id()).commit();
                            checkUser(dataResponse.data().auth().id());
                        }
                        Log.d(TAG, "authUser-data:" + dataResponse.data());
                        Log.d(TAG, "authUser-error:" + dataResponse.errors());
                    }
                    @Override
                    public void onError(Throwable e) {
                        e.printStackTrace();
                    }
                    @Override
                    public void onComplete() {
                    }
                });
    }

    private void checkUser(final String id)
    {
        ApolloCall<GetWorkspaces.Data> response = client.query(new GetWorkspaces(id));
        Rx2Apollo.from(response)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(new DisposableObserver<Response<GetWorkspaces.Data>>() {
                    @Override
                    public void onNext(Response<GetWorkspaces.Data> dataResponse) {
                        if (dataResponse.errors().isEmpty())
                        {
                            if(dataResponse.data().workspaces().size() == 0)
                                createWorkspace(id);
                            else
                            {
                                Intent intent = new Intent(UserActivity.this, MainActivity.class);
                                startActivity(intent);
                                finish();
                            }
                        }
                        Log.d(TAG, "checkUser-data:" + dataResponse.data());
                        Log.d(TAG, "checkUser-error:" + dataResponse.errors());
                    }

                    @Override
                    public void onError(Throwable e) {
                        e.printStackTrace();
                    }

                    @Override
                    public void onComplete() {
                    }
                });
    }

    private void createWorkspace(String id) {
        WorkspaceInput workspace = WorkspaceInput.builder()
                .ownerId(id)
                .name("No workspace")
                .description("No description")
                .build();

        Rx2Apollo.from(client.mutate(new CreateWorkspace(workspace)))
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(new DisposableObserver<Response<CreateWorkspace.Data>>() {
                    @Override
                    public void onNext(Response<CreateWorkspace.Data> dataResponse) {
                        if (dataResponse.errors().isEmpty()) {
                            preferences.edit().putString("idDefWorkspace", dataResponse.data().createWorkspace()).commit();
                            createProject(dataResponse.data().createWorkspace());
                        }
                        Log.d(TAG, "createDefWS-data:" + dataResponse.data());
                        Log.d(TAG, "createDefWS-error:" + dataResponse.errors());
                    }

                    @Override
                    public void onError(Throwable e) {
                        e.printStackTrace();
                    }

                    @Override
                    public void onComplete() {
                    }
                });
    }

    private void createProject(String id)
    {
        ProjectInput project = ProjectInput.builder()
                .name("No project")
                .color(Color.GRAY)
                .build();

        Rx2Apollo.from(client.mutate(new CreateProject(id, project)))
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(new DisposableObserver<Response<CreateProject.Data>>() {
                    @Override
                    public void onNext(Response<CreateProject.Data> dataResponse) {
                        if (dataResponse.errors().isEmpty())
                            preferences.edit().putString("idDefProject", dataResponse.data().createProject()).commit();
                        Log.d(TAG, "createDefProject-data:" + dataResponse.data());
                        Log.d(TAG, "createDefProject-error:" + dataResponse.errors());
                    }

                    @Override
                    public void onError(Throwable e) {
                        e.printStackTrace();
                    }

                    @Override
                    public void onComplete() {
                        Intent intent = new Intent(UserActivity.this, MainActivity.class);
                        startActivity(intent);
                        finish();
                    }
                });
    }
}
