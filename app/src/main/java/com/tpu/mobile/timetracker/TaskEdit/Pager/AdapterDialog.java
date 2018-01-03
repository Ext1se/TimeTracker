package com.tpu.mobile.timetracker.TaskEdit.Pager;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.tpu.mobile.timetracker.Database.Model.Project;
import com.tpu.mobile.timetracker.R;

import java.util.List;

/**
 * Created by Igorek on 31.10.2017.
 */

public class AdapterDialog extends ArrayAdapter {
    private Context context;
    private List<Project> projects;

    public AdapterDialog(Context context, int recourse,
                         List<Project> projects) {
        super(context, recourse, projects);
        this.context = context;
        this.projects = projects;
    }
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        return getCustomView(position, convertView, parent);
    }

    public View getCustomView(int position, View convertView,
                              ViewGroup parent) {
        View row = LayoutInflater.from(context).inflate(R.layout.dialog_project_row, parent, false);
        Project project = projects.get(position);
        TextView name = (TextView) row.findViewById(R.id.tvNamePerson);
        TextView color = (TextView) row.findViewById(R.id.tvColor);
        name.setText(project.getName());
        color.setBackgroundColor(project.getColor());
        return row;
    }

    @Override
    public View getDropDownView(int position, View convertView, ViewGroup parent) {
        return getCustomView(position, convertView, parent);
    }
}