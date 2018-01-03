package com.tpu.mobile.timetracker.Database.Controller;

import android.graphics.Color;
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
import com.tpu.mobile.timetracker.Project.ProjectCreateActivity;

import java.util.ArrayList;
import java.util.List;

import api.CreateProject;
import api.GetProjects;
import api.RemoveProject;
import api.UpdateProject;
import api.type.ProjectInput;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.observers.DisposableObserver;
import io.realm.Realm;
import io.realm.RealmResults;
import io.realm.Sort;

import static com.tpu.mobile.timetracker.R.drawable.workspace;

/**
 * Created by Igorek on 25.12.2017.
 */

public class ProjectController {
    Realm realm;
    ApolloClient client;

    public ProjectController(Realm realm, ApolloClient client) {
        this.realm = realm;
        this.client = client;
    }

    public void createProject(String id, String name, String description, int color, String idWs) {
        Project project = new Project();
        project.setName(name);
        project.setDescription(description);
        project.setId(id);
        project.setColor(color);
        project.setWorkspace(realm.where(Workspace.class).equalTo("id", idWs).findFirst());
        project.setStart(System.currentTimeMillis());
        realm.beginTransaction();
        realm.copyToRealm(project);
        realm.commitTransaction();
    }

    public RealmResults<Project> createProjects(List<GetProjects.Project> projects, List<String> ids) {
        if (projects == null) return null;
        for (int i = 0; i < projects.size(); i++) {
            Project project = new Project();
            project.setId(projects.get(i).id());
            project.setStart(projects.get(i).crdate());
            project.setName(projects.get(i).name());
            if (projects.get(i).color() != null)
                project.setColor(projects.get(i).color());
            else
                project.setColor(Color.WHITE);
            project.setWorkspace(realm.where(Workspace.class).equalTo("id", ids.get(i)).findFirst());
            realm.beginTransaction();
            realm.copyToRealm(project);
            realm.commitTransaction();
        }
        return realm.where(Project.class).findAllSorted("start", Sort.ASCENDING);
    }

    public Project getProject(String idProject) {
        return realm.where(Project.class).equalTo("id", idProject).findFirst();
    }

    public RealmResults<Project> getProjects() {
        return realm.where(Project.class).findAllSorted("start");
    }

    public RealmResults<Project> getProjectsOfWs(String idWs) {
        return realm.where(Project.class).equalTo("workspace.id", idWs).findAllSorted("start");
    }

    public int getTaskSize(String id) {
        return realm.where(Task.class).equalTo("project.id", id).findAll().size();
    }

    public void updateProject(String id, String name, String description, int color, String idWs) {
        Project project = realm.where(Project.class).equalTo("id", id).findFirst();
        realm.beginTransaction();
        project.setName(name);
        project.setDescription(description);
        project.setId(id);
        project.setColor(color);
        project.setWorkspace(realm.where(Workspace.class).equalTo("id", idWs).findFirst()); //На сервере не изменяется
        realm.commitTransaction();
    }

    public void removeProject(String id) {
        realm.beginTransaction();
        realm.where(Task.class).equalTo("project.id", id).findAll().deleteAllFromRealm();
        realm.where(Project.class).equalTo("id", id).findFirst().deleteFromRealm();
        realm.commitTransaction();
    }

    public void createProjectApollo(final ProjectInput project, final String description, final String idWs, final ApolloResponse apolloResponse) {
        Rx2Apollo.from(client.mutate(new CreateProject(idWs, project)))
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(new DisposableObserver<Response<CreateProject.Data>>() {
                    @Override
                    public void onNext(Response<CreateProject.Data> dataResponse) {
                        if (dataResponse.errors().isEmpty()) {
                            createProject(dataResponse.data().createProject(),
                                    project.name(), description, project.color(), idWs);
                        }
                        Log.d("myLog", "createProject-data:" + dataResponse.data());
                        Log.d("myLog", "createProject-error:" + dataResponse.errors());
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

    public void updateProjectApollo(final String id, final ProjectInput project, final String description, final String idWs,
                                    final ApolloResponse apolloResponse) {
        Rx2Apollo.from(client.mutate(new UpdateProject(id, project)))
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(new DisposableObserver<Response<UpdateProject.Data>>() {
                    @Override
                    public void onNext(Response<UpdateProject.Data> dataResponse) {
                        if (dataResponse.errors().isEmpty()) {
                            if (dataResponse.data().updateProject())
                                updateProject(id, project.name(), description, project.color(), idWs);
                        }
                        Log.d("myLog", "updateProject-data:" + dataResponse.data());
                        Log.d("myLog", "updateProject-error:" + dataResponse.errors());
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

    public void removeProjectApollo(final String id, final ApolloResponse apolloResponse) {
        Rx2Apollo.from(client.mutate(new RemoveProject(id)))
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(new DisposableObserver<Response<RemoveProject.Data>>() {
                    @Override
                    public void onNext(Response<RemoveProject.Data> dataResponse) {
                        if (dataResponse.errors().isEmpty()) {
                            if (dataResponse.data().removeProject())
                                removeProject(id);
                        }
                        Log.d("myLog", "removeProject-data:" + dataResponse.data());
                        Log.d("myLog", "removeProject-error:" + dataResponse.errors());
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

    public void loadProjects(String ownerID, final ApolloResponse apolloResponse)
    {
        ApolloCall<GetProjects.Data> response = client.query(new GetProjects(ownerID));
        Rx2Apollo.from(response)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(new DisposableObserver<Response<GetProjects.Data>>() {
                    @Override
                    public void onNext(Response<GetProjects.Data> dataResponse) {
                        if (dataResponse.errors().isEmpty()) {
                            List<GetProjects.Project> prjs = new ArrayList<GetProjects.Project>();
                            List<String> ids = new ArrayList<String>();
                            for (int i = 0; i < dataResponse.data().workspaces().size(); i++) {
                                for (int j = 0; j < dataResponse.data().workspaces().get(i).projects().size(); j++) {
                                    prjs.add(dataResponse.data().workspaces().get(i).projects().get(j));
                                    ids.add(dataResponse.data().workspaces().get(i).id());
                                }
                            }
                            createProjects(prjs, ids);
                            Log.d("myLog", "loadProjects-data:" + dataResponse.data());
                            Log.d("myLog", "loadProjects-error:" + dataResponse.errors());
                            apolloResponse.onNext(dataResponse.errors().isEmpty());
                        }
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
