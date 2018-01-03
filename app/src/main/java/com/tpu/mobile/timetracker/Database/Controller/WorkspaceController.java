package com.tpu.mobile.timetracker.Database.Controller;

import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.apollographql.apollo.ApolloCall;
import com.apollographql.apollo.ApolloClient;
import com.apollographql.apollo.api.Response;
import com.apollographql.apollo.rx2.Rx2Apollo;
import com.tpu.mobile.timetracker.Database.Model.Project;
import com.tpu.mobile.timetracker.Database.Model.Task;
import com.tpu.mobile.timetracker.Database.Model.Workspace;
import com.tpu.mobile.timetracker.Workspace.WorkspaceEditActivity;

import java.util.List;

import api.CreateWorkspace;
import api.GetWorkspaces;
import api.RemoveWorkspace;
import api.UpdateWorkspace;
import api.type.WorkspaceInput;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.observers.DisposableObserver;
import io.realm.Realm;
import io.realm.RealmResults;
import io.realm.Sort;

/**
 * Created by Igorek on 27.12.2017.
 */

public class WorkspaceController {
    Realm realm;
    ApolloClient client;

    public WorkspaceController(Realm realm, ApolloClient client)
    {
        this.realm = realm;
        this.client = client;
    }

    public void createWorkspace(String id, WorkspaceInput workspaceInput)
    {
        Workspace workspace = new Workspace();
        workspace.setId(id);
        workspace.setName(workspaceInput.name());
        workspace.setDescription(workspaceInput.description());
        workspace.setTimeCreated(System.currentTimeMillis());
        realm.beginTransaction();
        realm.copyToRealm(workspace);
        realm.commitTransaction();
    }

    public RealmResults<Workspace> createWorkspaces(List<GetWorkspaces.Workspace> workspaces)
    {
        if (workspaces == null) return null;
        for (int i = 0; i < workspaces.size(); i++)
        {
            Workspace workspace = new Workspace();
            workspace.setId(workspaces.get(i).id());
            workspace.setName(workspaces.get(i).name());
            workspace.setDescription(workspaces.get(i).description());
            workspace.setTimeCreated(workspaces.get(i).crdate());
            realm.beginTransaction();
            realm.copyToRealm(workspace);
            realm.commitTransaction();
        }
        return realm.where(Workspace.class).findAllSorted("create", Sort.ASCENDING);
    }

    public Workspace getWorkspace(String id){
        return realm.where(Workspace.class).equalTo("id", id).findFirst();
    }

    public RealmResults<Workspace> getWorkspaces()
    {
        return realm.where(Workspace.class).findAllSorted("create", Sort.ASCENDING);
    }

    public int getProjectSize(String id)
    {
        return realm.where(Project.class).equalTo("workspace.id", id).findAll().size();
    }

    public void removeWorkspace(String id)
    {
        realm.beginTransaction();
        RealmResults<Project> projects = realm.where(Project.class).equalTo("workspace.id", id).findAll();
        for (int i = 0; i < projects.size(); i++)
            realm.where(Task.class).equalTo("project.id", projects.get(i).getId()).findAll().deleteAllFromRealm();
        realm.where(Project.class).equalTo("workspace.id", id).findAll().deleteAllFromRealm();
        realm.where(Workspace.class).equalTo("id", id).findFirst().deleteFromRealm();
        realm.commitTransaction();
    }

    public void updateWorkspace(String id, String name, String description)
    {
        Workspace workspace = realm.where(Workspace.class).equalTo("id", id).findFirst();
        realm.beginTransaction();
        workspace.setName(name);
        workspace.setDescription(description);
        realm.commitTransaction();
    }

    public void createWorkspaceApollo(final WorkspaceInput workspaceInput, final ApolloResponse apolloResponse)
    {
        Rx2Apollo.from(client.mutate(new CreateWorkspace(workspaceInput)))
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(new DisposableObserver<Response<CreateWorkspace.Data>>() {
                    @Override
                    public void onNext(Response<CreateWorkspace.Data> dataResponse) {
                        if (dataResponse.errors().isEmpty())
                            createWorkspace(dataResponse.data().createWorkspace(), workspaceInput);
                        Log.d("myLog", "createWorkspace-data:" + dataResponse.data());
                        Log.d("myLog", "createWorkspace-error:" + dataResponse.errors());
                        apolloResponse.onNext(dataResponse.errors().isEmpty());
                    }

                    @Override
                    public void onError(Throwable e) {
                        e.printStackTrace();
                        apolloResponse.onError();
                    }

                    @Override
                    public void onComplete() {
                        apolloResponse.onComplete();
                    }
                });
    }

    public void updateWorkspaceApollo(final String id, final WorkspaceInput workspace, final ApolloResponse apolloResponse)
    {
        Rx2Apollo.from(client.mutate(new UpdateWorkspace(id, workspace)))
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(new DisposableObserver<Response<UpdateWorkspace.Data>>() {
                    @Override
                    public void onNext(Response<UpdateWorkspace.Data> dataResponse) {
                        if (dataResponse.errors().isEmpty()) {
                            if (dataResponse.data().updateWorkspace())
                                updateWorkspace(id, workspace.name(), workspace.description());
                        }
                        Log.d("myLog", "workspaceUpdate-data: " + dataResponse.errors());
                        Log.d("myLog", "workspaceUpdate-error: " + dataResponse.data().updateWorkspace());
                        apolloResponse.onNext(dataResponse.errors().isEmpty());
                    }

                    @Override
                    public void onError(Throwable e) {
                        e.printStackTrace();
                        apolloResponse.onError();
                    }

                    @Override
                    public void onComplete() {
                        apolloResponse.onComplete();
                    }
                });
    }

    public void removeWorkspaceApollo(final String id, final ApolloResponse apolloResponse)
    {
        Rx2Apollo.from(client.mutate(new RemoveWorkspace(id)))
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(new DisposableObserver<Response<RemoveWorkspace.Data>>() {
                    @Override
                    public void onNext(Response<RemoveWorkspace.Data> dataResponse) {
                        if (dataResponse.errors().isEmpty())
                            if (dataResponse.data().removeWorkspace()) removeWorkspace(id);
                        Log.d("myLog", "removeWorkspace-data:" + dataResponse.data());
                        Log.d("myLog", "removeWorkspace-error:" + dataResponse.errors());
                        apolloResponse.onNext(dataResponse.errors().isEmpty());
                    }

                    @Override
                    public void onError(Throwable e) {
                        e.printStackTrace();
                        apolloResponse.onError();
                    }

                    @Override
                    public void onComplete() {
                        apolloResponse.onComplete();
                    }
                });
    }

    public void loadWorkspaces(String ownerID, final ApolloResponse apolloResponse)
    {
        ApolloCall<GetWorkspaces.Data> response = client.query(new GetWorkspaces(ownerID));
        Rx2Apollo.from(response)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(new DisposableObserver<Response<GetWorkspaces.Data>>() {
                    @Override
                    public void onNext(Response<GetWorkspaces.Data> dataResponse) {
                        if (dataResponse.errors().isEmpty())
                            createWorkspaces(dataResponse.data().workspaces());
                        Log.d("myLog", "loadWs-data:" + dataResponse.data());
                        Log.d("myLog", "loadWs-error:" + dataResponse.errors());
                        apolloResponse.onNext(dataResponse.errors().isEmpty());
                    }

                    @Override
                    public void onError(Throwable e) {
                        e.printStackTrace();
                        apolloResponse.onError();
                    }

                    @Override
                    public void onComplete() {
                        apolloResponse.onComplete();
                    }
                });
    }
}
