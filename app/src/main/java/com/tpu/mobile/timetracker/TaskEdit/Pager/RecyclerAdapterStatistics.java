package com.tpu.mobile.timetracker.TaskEdit.Pager;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.NumberPicker;
import android.widget.TextView;
import android.widget.TimePicker;

import com.apollographql.apollo.ApolloClient;
import com.apollographql.apollo.api.Response;
import com.apollographql.apollo.rx2.Rx2Apollo;
import com.tpu.mobile.timetracker.Database.Controller.ApolloResponse;
import com.tpu.mobile.timetracker.Database.Controller.ProjectController;
import com.tpu.mobile.timetracker.Database.Controller.TaskController;
import com.tpu.mobile.timetracker.Database.Model.StatTask;
import com.tpu.mobile.timetracker.Database.Model.Task;
import com.tpu.mobile.timetracker.Main.MainActivity;
import com.tpu.mobile.timetracker.R;


import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import api.UpdateTimeEntry;
import api.type.TimeEntryInput;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.observers.DisposableObserver;
import io.realm.Realm;


/**
 * Created by Igorek on 01.11.2017.
 */

public class RecyclerAdapterStatistics extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    ApolloClient client;
    Realm realm;
    TaskController taskController;
    Context context;
    List<StatTask> stats;
    boolean[] changeStats;


    public RecyclerAdapterStatistics(Context context, TaskController taskController, List<StatTask> stats) {
        this.context = context;
        this.stats = stats;
        this.taskController = taskController;
        changeStats = new boolean[stats.size()];
        for (int i = 0; i < changeStats.length; i++)
            changeStats[i] = false;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new TaskStatViewHolder(LayoutInflater.from(context).inflate(R.layout.task_item_stat, parent, false), context);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, final int position) {
        final TaskStatViewHolder vh = (TaskStatViewHolder)holder;
        final StatTask stat = stats.get(position);

        vh.init(stat.getStart(), stat.getEnd());
        vh.tvStartDate.setText(vh.calculateDate(stat.getStart()));
        vh.tvEndDate.setText(vh.calculateDate(stat.getEnd()));
        vh.tvTime.setText(vh.calculateTime(stat.getDuration()));

        if ((position == stats.size() - 1) && (stat.getTask().getState() == Task.TASK_RUNNING))
        {
            vh.tvStartDate.setEnabled(false);
            vh.tvEndDate.setEnabled(false);
            vh.tvTime.setEnabled(false);
            vh.layoutTracker.setAlpha(0.5f);
            return;
        }

        vh.etDescription.setText(stat.getNote());
        vh.tvNote.setText("Note (" + (200 - vh.etDescription.length()) + "):");

        vh.tvStartDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showDateDialog(vh, "start", stat, position);
            }
        });

        vh.tvEndDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showDateDialog(vh, "end", stat, position);
            }
        });

        vh.tvTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showTimeDialog(vh, stat, position);
            }
        });

        vh.etDescription.addTextChangedListener(new TextWatcher() {
            @Override
            public void afterTextChanged(Editable s) {
                vh.tvNote.setText("Note (" + (200 - vh.etDescription.length()) + "):");
            }

            @Override
            public void onTextChanged(CharSequence s, int st, int b, int c)
            { }
            @Override
            public void beforeTextChanged(CharSequence s, int st, int c, int a)
            { }
        });

        vh.etDescription.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean hasFocus) {
                if (!hasFocus)
                    updateDescriptionTask(stat, vh.etDescription.getText().toString());
            }
        });

        vh.etDescription.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int actionId, KeyEvent keyEvent) {
                if (actionId == EditorInfo.IME_ACTION_SEARCH ||
                        actionId == EditorInfo.IME_ACTION_DONE)
                    updateDescriptionTask(stat, vh.etDescription.getText().toString());
                return false;
            }
        });

        vh.etDescription.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View view, int i, KeyEvent keyEvent) {
                if (keyEvent.getKeyCode() == KeyEvent.KEYCODE_BACK)
                    updateDescriptionTask(stat, vh.etDescription.getText().toString());
                return false;
            }
        });
    }

    private void updateDescriptionTask(StatTask statistics, String note)
    {
        if (note.equals(statistics.getNote()))
            return;
        else
        {
           // changeStat.setDescription(note);
            //realm.beginTransaction();
           // statistics.setNote(note);
            //realm.commitTransaction();
        }
    }

    private void showDateDialog(final TaskStatViewHolder vh, final String time,
                                final StatTask stat, final int position) {
        final AlertDialog.Builder builder = new AlertDialog.Builder(context);
        View view = LayoutInflater.from(context).inflate(R.layout.dialog_date_time, null, false);
        builder.setView(view);
        final DatePicker datePicker = (DatePicker) view.findViewById(R.id.datePicker);
        final TimePicker timePicker = (TimePicker) view.findViewById(R.id.timePicker);
        timePicker.setIs24HourView(true);

        Button bDate = (Button) view.findViewById(R.id.bDate);
        Button bTime = (Button) view.findViewById(R.id.bTime);

        bDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                timePicker.setVisibility(View.INVISIBLE);
                datePicker.setVisibility(View.VISIBLE);
            }
        });

        bTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                timePicker.setVisibility(View.VISIBLE);
                datePicker.setVisibility(View.INVISIBLE);
            }
        });

        Calendar calendar = Calendar.getInstance();
        if (time.equals("start"))
            calendar.setTimeInMillis(stat.getStart());
        else
            calendar.setTimeInMillis(stat.getEnd());

        datePicker.init(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH), null);

        timePicker.setCurrentHour(calendar.get(Calendar.HOUR_OF_DAY));
        timePicker.setCurrentMinute(calendar.get(Calendar.MINUTE));

        builder
                .setTitle("Set the " + time + " Date")
                .setPositiveButton("Done", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        int day = datePicker.getDayOfMonth();
                        int month = datePicker.getMonth();
                        int year = datePicker.getYear();
                        int hour = timePicker.getCurrentHour();
                        int min = timePicker.getCurrentMinute();
                        String date = String.format("%02d.%02d.%02d %02d:%02d", day, (month + 1), year, hour, min);

                        Calendar calendar = Calendar.getInstance();
                        calendar.set(year, month, day, hour, min);

                        long dateLong = calendar.getTimeInMillis();
                        if (time.equals("start")) {
                            vh.tvStartDate.setText(date);
                            vh.start = dateLong;

                            changeStats[position] = true;
                            //realm.beginTransaction();
                            stat.setStart(dateLong);
                            //realm.commitTransaction();
                        }
                        else {
                            vh.tvEndDate.setText(date);
                            vh.end = calendar.getTimeInMillis();

                            changeStats[position] = true;
                            //realm.beginTransaction();
                            stat.setEnd(dateLong);
                            //realm.commitTransaction();
                        }

                        long duration = vh.end - vh.start;
                        //realm.beginTransaction();
                        if (duration > 0) {
                            changeStats[position] = true;
                            stat.setDuration(vh.end - vh.start);
                        }
                        else {
                            changeStats[position] = true;
                            stat.setDuration(0);
                        }
                        //realm.commitTransaction();
                        vh.calculateTime();
                    }
                });
        builder.create();
        builder.show();
    }

    private void showTimeDialog(final TaskStatViewHolder vh,
                                final StatTask stat, final int position) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        View view = LayoutInflater.from(context).inflate(R.layout.dialog_time_setting, null, false);
        final NumberPicker numHour = (NumberPicker)view.findViewById(R.id.numHour);
        final NumberPicker numMin = (NumberPicker)view.findViewById(R.id.numMin);
        final NumberPicker numSec = (NumberPicker)view.findViewById(R.id.numSec);
        //Если слишком много часов не удобно листать. Максимально 100 пунктов.
        if (vh.hourTask / 100 > 0)
        {
            String[] values = new String[101];
            int step = vh.hourTask / 100;
            for (int i = 0; i < values.length; i++) {
                values[i] = Integer.toString(step * i);
            }
            numHour.setMaxValue(100);
            numHour.setDisplayedValues(values);
        }
        else
        {
            String[] values = new String[vh.hourTask + 1];
            for (int i = 0; i < values.length; i++) {
                values[i] = Integer.toString(i);
            }
            numHour.setMaxValue(vh.hourTask);
            numHour.setDisplayedValues(values);
        }
        if (vh.hourTask > 0)
            numMin.setMaxValue(59);
        else
            numMin.setMaxValue(vh.minTask);
        numSec.setMaxValue(59);
        builder.setView(view);
        builder.setTitle("Set time")
                .setPositiveButton("Done", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        int h = Integer.valueOf(numHour.getDisplayedValues()[numHour.getValue()]);
                        int m = numMin.getValue();
                        int s = numSec.getValue();
                        String time = String.format("%02d:%02d:%02d", h, m, s);
                        vh.tvTime.setText(time);

                        long dateLong = (long)(h * 3600 + m * 60 + s) * 1000;
                        Log.d("myLog", "DateLong = " + dateLong + "; h = " + (h * 3600) + m + "(m * 60)");

                        changeStats[position] = true;
                        //realm.beginTransaction();
                        stat.setDuration(dateLong);
                        //realm.commitTransaction();

                    }
                });
        builder.create();
        builder.show();
    }


    @Override
    public int getItemCount() {
        return stats.size();
    }


    public void Save()
    {

        final List<StatTask> statits = new ArrayList<StatTask>();
        for (int i = 0; i < changeStats.length; i++) {
            if (changeStats[i])
                statits.add(stats.get(i));
        }

        final boolean[] completed = new boolean[statits.size()];

        if (statits.size() == 0)
        {
            Intent intent = new Intent(context, MainActivity.class);
            context.startActivity(intent);
        }

        for (int i = 0; i < statits.size(); i++)
        {
            final StatTask st = statits.get(i);
            TimeEntryInput timeEntry = TimeEntryInput.builder()
                    .startDate(Long.toString(statits.get(i).getStart()))
                    .endDate(Long.toString(statits.get(i).getEnd()))
                    .duration((int)statits.get(i).getDuration())
                    .build();
            final int I = i;
            taskController.updateStat(st, timeEntry, new ApolloResponse() {
                @Override
                public void onNext(boolean success) {
                    completed[I] = true;
                }

                @Override
                public void onError() {
                    for (boolean ok : completed)
                        if (!ok) return;
                    Intent intent = new Intent(context, MainActivity.class);
                    context.startActivity(intent);
                }

                @Override
                public void onComplete() {
                    for (boolean ok : completed)
                        if (!ok) return;
                    Intent intent = new Intent(context, MainActivity.class);
                    context.startActivity(intent);
                }
            });
        }
    }
}
