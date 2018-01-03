package com.tpu.mobile.timetracker.Workspace;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.apollographql.apollo.ApolloClient;
import com.apollographql.apollo.api.Response;
import com.apollographql.apollo.rx2.Rx2Apollo;
import com.tpu.mobile.timetracker.Database.Controller.ApolloResponse;
import com.tpu.mobile.timetracker.Database.Controller.WorkspaceController;
import com.tpu.mobile.timetracker.Database.Model.Project;
import com.tpu.mobile.timetracker.Database.Model.Workspace;
import com.tpu.mobile.timetracker.R;
import com.tpu.mobile.timetracker.Main.MainActivity;

import java.util.List;

import api.CreateWorkspace;
import api.RemoveWorkspace;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.observers.DisposableObserver;
import io.realm.Realm;
import io.realm.RealmResults;

/**
 * Created by Igorek on 12.10.2017.
 */

public class WorkspaceRecyclerViewAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    Context context;
    WorkspaceController workspaceController;
    List<Workspace> workspaces;

    public WorkspaceRecyclerViewAdapter(Context context, WorkspaceController workspaceController, List<Workspace> workspaces) {
        this.context = context;
        this.workspaces = workspaces;
        this.workspaceController = workspaceController;
    }

    public void setWorkspaces(List<Workspace> workspaces) {
        this.workspaces = workspaces;
        notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        if (workspaces != null)
            return workspaces.size();
        else
            return 0;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new WorkspaceViewHolder(LayoutInflater.from(context).inflate(R.layout.workspace_item_main, parent, false));
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, final int position) {
        final WorkspaceViewHolder vh = (WorkspaceViewHolder) holder;
        final Workspace workspace = workspaces.get(position);
        vh.isOpen = false;
        vh.baseLayout.close(false);

        vh.textName.setText(workspace.getName());
        if (workspace.getDescription() == null || workspace.getDescription().equals(""))
            vh.textDescription.setText("No description");
        else
            vh.textDescription.setText(workspace.getDescription());

        vh.textNumber.setText("Projects: " + workspaceController.getProjectSize(workspace.getId()));

        vh.layoutClickable.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!vh.isOpen) {
                    vh.isOpen = true;
                    vh.baseLayout.open(true);
                }
                else {
                    vh.isOpen = false;
                    vh.baseLayout.close(true);
                }
            }
        });

        vh.bOpen.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(context, MainActivity.class);
                intent.putExtra("workspaceID", workspace.getId());
                intent.putExtra("fragmentID", R.id.nav_project);
                context.startActivity(intent);
            }
        });

        vh.bEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(context, WorkspaceEditActivity.class);
                intent.putExtra("workspaceID", workspace.getId());
                context.startActivity(intent);
            }
        });

        vh.bDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final String id = workspaces.get(position).getId();
                String idDef = workspaces.get(0).getId();
                vh.bDelete.setClickable(false);
                if (!id.equals(idDef)) {
                    workspaceController.removeWorkspaceApollo(id, new ApolloResponse() {
                        @Override
                        public void onNext(boolean success) {
                        }

                        @Override
                        public void onError() {
                            vh.bDelete.setClickable(true);
                        }

                        @Override
                        public void onComplete() {
                            vh.bDelete.setClickable(true);
                            notifyItemRemoved(position);
                            notifyItemRangeChanged(position, workspaces.size());
                        }
                    });
                }
                else
                    Toast.makeText(context, "Default workspace can't be removed.", Toast.LENGTH_LONG).show();
            }
        });
    }
}