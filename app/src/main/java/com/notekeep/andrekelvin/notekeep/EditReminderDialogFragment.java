package com.notekeep.andrekelvin.notekeep;


import android.app.Activity;
import android.app.AlarmManager;
import android.app.DatePickerDialog;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;


/**
 * A simple {@link Fragment} subclass.
 */
public class EditReminderDialogFragment extends DialogFragment {

    private TextView textTime, textDate;
    private Spinner spinner;
    private List<String> spinnerItem;
    private Calendar calendar;
    private ReminderValues reminderValues;
    private Button buttonSaveChanges, buttonCancel;

    public EditReminderDialogFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_dialog_edit_reminder, container, false);

        textTime = view.findViewById(R.id.textEditSelectTime);
        textDate = view.findViewById(R.id.textEditSelectDate);
        spinner = view.findViewById(R.id.spinnerEdit);
        buttonCancel = view.findViewById(R.id.buttonEditCancelReminder);
        buttonSaveChanges = view.findViewById(R.id.buttonSaveEditReminder);

        spinnerItem = new ArrayList<>();
        spinnerItem.add("Don't Repeat");
        spinnerItem.add("Daily");
        spinnerItem.add("Weekly");
        spinnerItem.add("Monthly");
        spinnerItem.add("Yearly");
        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_dropdown_item, spinnerItem);
        spinner.setAdapter(arrayAdapter);

        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                //Remove Date when Daily,Weekly,Monthly or Year is clicked
                if (position == 1) {
                    textDate.setText("");
                    textDate.setVisibility(View.GONE);
                } else if (position == 2) {
                    textDate.setText("");
                    textDate.setVisibility(View.GONE);
                } else if (position == 3) {
                    textDate.setText("");
                    textDate.setVisibility(View.GONE);
                } else if (position == 4) {
                    textDate.setText("");
                    textDate.setVisibility(View.GONE);
                } else {
                    textDate.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        //Get saved Reminder values from Edit Activity Bundle
        //if reminders where not saved(save button is not clicked) all this will be null
        String time = getArguments().getString(EditNoteActivity.TIME);
        String date = getArguments().getString(EditNoteActivity.DATE);
        int repeat = getArguments().getInt(EditNoteActivity.REMINDER_REPEAT);
        final int selectedNotePendingIntentID = getArguments().getInt(EditNoteActivity.SELECTED_NOTE_PENDING_INTENT_ID);
        final int selectedNoteID=getArguments().getInt(EditNoteActivity.SELECTED_NOTE_ID);

        textTime.setText(time);
        textDate.setText(date);
        spinner.setSelection(repeat);

        calendar = Calendar.getInstance();

        textTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                int currentHour = calendar.get(Calendar.HOUR_OF_DAY);
                int currentMinute = calendar.get(Calendar.MINUTE);

                TimePickerDialog timePickerDialog = new TimePickerDialog(getContext(), new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker timePicker, int hourOfDay, int minutes) {
                        calendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
                        calendar.set(Calendar.MINUTE, minutes);
                        calendar.set(Calendar.SECOND, 0);

                        boolean isPm = (hourOfDay >= 12);
                        textTime.setText(String.format("%02d:%02d %s", (hourOfDay == 12 || hourOfDay == 0) ? 12 : hourOfDay % 12, minutes, isPm ? "PM" : "AM"));
                    }
                }, currentHour, currentMinute, false);
                timePickerDialog.show();
            }
        });

        textDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int currentYear = calendar.get(Calendar.YEAR);
                int currentMonth = calendar.get(Calendar.MONTH);
                int currentDay = calendar.get(Calendar.DAY_OF_MONTH);

                DatePickerDialog datePickerDialog = new DatePickerDialog(getContext(), new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                        calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                        calendar.set(Calendar.MONTH, month);
                        calendar.set(Calendar.YEAR, year);

                        textDate.setText(dayOfMonth + "/" + (month + 1) + "/" + year);
                    }
                }, currentYear, currentMonth, currentDay);
                datePickerDialog.show();
            }
        });

        buttonSaveChanges.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (textDate.getVisibility() == View.GONE) {
                    /*
                    since date is only visible when "Don't Repeat" is selected
                    (Daily,Weekly,Monthly,Yearly Repeat Reminders wouldn't involve Selected Date)
                    that's when all reminder values is pass to Edit Activity
                    else all values is passed expect Selected Date
                     */
                    if (textTime.getText().toString().isEmpty()) {
                        Toast.makeText(getContext(), "Invalid Input... Time not Selected", Toast.LENGTH_SHORT).show();
                    } else {
                        reminderValues.onReminderValues(textTime.getText().toString(),
                                "",
                                calendar.getTimeInMillis(),
                                (int) System.currentTimeMillis(),
                                spinner.getSelectedItemPosition(),
                                spinner.getSelectedItem().toString(),
                                calendar);

                        getDialog().dismiss();
                    }
                } else {
                    if (textTime.getText().toString().isEmpty() || textDate.getText().toString().isEmpty()) {
                        Toast.makeText(getContext(), "Invalid Input... Time or Date not Selected", Toast.LENGTH_SHORT).show();
                    } else {
                        reminderValues.onReminderValues(textTime.getText().toString(),
                                textDate.getText().toString(),
                                calendar.getTimeInMillis(),
                                (int) System.currentTimeMillis(),
                                spinner.getSelectedItemPosition(),
                                spinner.getSelectedItem().toString(),
                                calendar);

                        getDialog().dismiss();
                    }
                }
            }
        });

        buttonCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Update the reminder values back to null
                NoteDB noteDB=new NoteDB(getContext());
                noteDB.open();
                noteDB.updateNoteReminderToNull(selectedNoteID);
                noteDB.close();

                AlarmManager alarmManager = (AlarmManager) getContext().getSystemService(Context.ALARM_SERVICE);
                Intent intent = new Intent(getContext(), ReminderReceiver.class);
                PendingIntent pendingIntent = PendingIntent.getBroadcast(getContext(), selectedNotePendingIntentID, intent, 0);
                alarmManager.cancel(pendingIntent);
                Toast.makeText(getContext(), "Reminder Canceled", Toast.LENGTH_SHORT).show();

                reminderValues.onReminderValues("", "", 0, 0, 0, "", calendar);

                getDialog().dismiss();
            }
        });

        return view;
    }

    /**
     * This interface is used to pass data to the Edit Activity
     * (Fragment to Activity Communication)
     */
    public interface ReminderValues {
        void onReminderValues(String time, String date, long calendarTimeMillieSec,
                              int pendingIntentID, int reminderRepeatIndex,
                              String reminderRepeatItem, Calendar calendar);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        Activity activity = (Activity) context;
        try {
            reminderValues = (ReminderValues) activity;
        } catch (Exception e) {

        }
    }

}
