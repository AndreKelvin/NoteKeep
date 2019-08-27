package com.notekeep.andrekelvin.notekeep;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class AddNoteActivity extends AppCompatActivity implements AddReminderDialogFragment.ReminderValues {

    private EditText textTitle, textNote;
    private TextView textReminderTimeDate;
    private NoteDB noteDB;
    private LinearLayout linearLayout;
    private String time, date, reminderRepeatItem;
    private int reminderRepeatIndex,pendingIntentID;
    private long calendarTimeMillieSec;
    public static final String TIME = "time";
    public static final String DATE = "date";
    public static final String REMINDER_REPEAT = "repeat";
    private Bundle reminderValuesBundle;
    private Calendar calendar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_note);

        //getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_save);
        setTitle("Add Note");

        textTitle = findViewById(R.id.editTextTitle);
        textNote = findViewById(R.id.editTextNote);
        textReminderTimeDate = findViewById(R.id.textReminderTimeDate);
        linearLayout = findViewById(R.id.linearLayout);
        Toolbar toolbar=findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        noteDB = new NoteDB(this);

        reminderValuesBundle = new Bundle();
    }

    //Replace the Action Bar Menu when ever this Activity is visible
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.add_note, menu);
        return true;
    }

    //Save Note when the save icon is selected on the Menu Bar
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.saveNote:
                String title = textTitle.getText().toString().trim();
                String note = textNote.getText().toString().trim();
                SimpleDateFormat dateFormat = new SimpleDateFormat("hh:mm a dd/MMM/yy", Locale.UK);
                String dateTime = dateFormat.format(new Date());

                if (title.isEmpty() || note.isEmpty()) {
                    Toast.makeText(AddNoteActivity.this, "Empty Note... Insert Your Title and Note", Toast.LENGTH_SHORT).show();
                } else {
                    noteDB.open();
                    //if linearLayout visibility is INVISIBLE that means no reminder was saved
                    //just insert notes and date to db (reminder columns are nullable)
                    //else insert notes,date and reminder values and start alarm
                    if (linearLayout.getVisibility() == View.INVISIBLE) {
                        noteDB.insertNote(textTitle.getText().toString(), textNote.getText().toString(), dateTime);

                        finish();
                        Toast.makeText(AddNoteActivity.this, "Note Saved", Toast.LENGTH_SHORT).show();
                    } else {
                        long insertedID = noteDB.insertNoteReminder(
                                textTitle.getText().toString(),
                                textNote.getText().toString(),
                                dateTime,
                                calendarTimeMillieSec,
                                pendingIntentID,
                                reminderRepeatItem);

                        //Get inserted Note ID to be passed to broadcast receiver class
                        //Where the notification has Pending Intent that is going to open EditNoteActivity
                        //And broadcast receiver is going to pass this ID as Bundle to EditNoteActivity

                        //Don't Repeat
                        if (reminderRepeatItem.contentEquals("Don't Repeat")) {
                            AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
                            Intent intent = new Intent(AddNoteActivity.this, ReminderReceiver.class);
                            intent.putExtra("INSERTED_NOTE_ID",insertedID);
                            intent.putExtra("TITLE",textTitle.getText().toString());
                            PendingIntent pendingIntent = PendingIntent.getBroadcast(AddNoteActivity.this, pendingIntentID, intent, 0);
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                                alarmManager.setExact(AlarmManager.RTC_WAKEUP, calendarTimeMillieSec, pendingIntent);
                            } else {
                                alarmManager.set(AlarmManager.RTC_WAKEUP, calendarTimeMillieSec, pendingIntent);
                            }
                            Toast.makeText(AddNoteActivity.this, "Note and Reminder Saved", Toast.LENGTH_SHORT).show();
                        }
                        //Daily Repeat
                        else if (reminderRepeatItem.contentEquals("Daily")) {
                            AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
                            Intent intent = new Intent(AddNoteActivity.this, ReminderReceiver.class);
                            intent.putExtra("INSERTED_NOTE_ID",insertedID);
                            intent.putExtra("TITLE",textTitle.getText().toString());
                            PendingIntent pendingIntent = PendingIntent.getBroadcast(AddNoteActivity.this, pendingIntentID, intent, 0);
                            alarmManager.setInexactRepeating(AlarmManager.RTC_WAKEUP, calendarTimeMillieSec, AlarmManager.INTERVAL_DAY, pendingIntent);
                            Toast.makeText(AddNoteActivity.this, "Note and Reminder Saved. Will Repeat Daily", Toast.LENGTH_SHORT).show();
                        }
                        //Weekly Repeat
                        else if (reminderRepeatItem.contentEquals("Weekly")) {
                            AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
                            Intent intent = new Intent(AddNoteActivity.this, ReminderReceiver.class);
                            intent.putExtra("INSERTED_NOTE_ID",insertedID);
                            intent.putExtra("TITLE",textTitle.getText().toString());
                            PendingIntent pendingIntent = PendingIntent.getBroadcast(AddNoteActivity.this, pendingIntentID, intent, 0);
                            alarmManager.setInexactRepeating(AlarmManager.RTC_WAKEUP, calendarTimeMillieSec, (AlarmManager.INTERVAL_DAY * 7), pendingIntent);
                            Toast.makeText(AddNoteActivity.this, "Note and Reminder Saved. Will Repeat Weekly", Toast.LENGTH_SHORT).show();
                        }
                        //Monthly Repeat
                        else if (reminderRepeatItem.contentEquals("Monthly")) {
                            // get todays date
                            Calendar cal = Calendar.getInstance();
                            // get current month
                            int currentMonth = calendar.get(Calendar.MONTH);

                            // move month ahead
                            currentMonth++;
                            // check if has not exceeded threshold of december

                            if (currentMonth > Calendar.DECEMBER) {
                                // alright, reset month to january and forward year by 1 e.g from 2013 to 2014
                                currentMonth = Calendar.JANUARY;
                                // Move year ahead as well
                                cal.set(Calendar.YEAR, cal.get(Calendar.YEAR) + 1);
                            }

                            // reset calendar to next month
                            cal.set(Calendar.MONTH, currentMonth);
                            // get the maximum possible days in this month
                            int maximumDay = cal.getActualMaximum(Calendar.DAY_OF_MONTH);

                            // set the calendar to maximum day (e.g in case of fEB 28th, or leap 29th)
                            cal.set(Calendar.DAY_OF_MONTH, maximumDay);
                            long monthInterval = cal.getTimeInMillis(); // this is time one month ahead

                            AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
                            Intent intent = new Intent(AddNoteActivity.this, ReminderReceiver.class);
                            intent.putExtra("INSERTED_NOTE_ID",insertedID);
                            intent.putExtra("TITLE",textTitle.getText().toString());
                            PendingIntent pendingIntent = PendingIntent.getBroadcast(AddNoteActivity.this, pendingIntentID, intent, 0);
                            alarmManager.setInexactRepeating(AlarmManager.RTC_WAKEUP, calendarTimeMillieSec, monthInterval, pendingIntent);
                            Toast.makeText(AddNoteActivity.this, "Note and Reminder Saved. Will Repeat Monthly", Toast.LENGTH_SHORT).show();
                        }
                        //Yearly Repeat
                        else if (reminderRepeatItem.contentEquals("Yearly")) {
                            calendar.setTimeInMillis(System.currentTimeMillis());
                            int year = calendar.get(Calendar.YEAR);
                            calendar.set(Calendar.YEAR, year + 1);

                            AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
                            Intent intent = new Intent(AddNoteActivity.this, ReminderReceiver.class);
                            intent.putExtra("INSERTED_NOTE_ID",insertedID);
                            intent.putExtra("TITLE",textTitle.getText().toString());
                            PendingIntent pendingIntent = PendingIntent.getBroadcast(AddNoteActivity.this, pendingIntentID, intent, 0);
                            alarmManager.setInexactRepeating(AlarmManager.RTC_WAKEUP, calendarTimeMillieSec, 0, pendingIntent);
                            Toast.makeText(AddNoteActivity.this, "Note and Reminder Saved. Will Repeat Yearly", Toast.LENGTH_SHORT).show();
                        }

                        finish();
                    }
                    noteDB.close();
                }
                return true;
            case R.id.addReminder:
                //Pass the saved reminder values back to dialog fragment
                reminderValuesBundle.putString(TIME, time);
                reminderValuesBundle.putString(DATE, date);
                reminderValuesBundle.putInt(REMINDER_REPEAT, reminderRepeatIndex);

                AddReminderDialogFragment addReminder = new AddReminderDialogFragment();
                addReminder.setArguments(reminderValuesBundle);
                addReminder.show(getSupportFragmentManager(), "Add Reminder");
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onReminderValues(String time, String date, long calendarTimeMillieSec, int pendingIntentID, int reminderRepeatIndex, String reminderRepeatItem, Calendar calendar) {
        if (time.isEmpty()) {
            linearLayout.setVisibility(View.INVISIBLE);
            reminderValuesBundle.clear();
            this.time = time;
            this.date = date;
            this.calendarTimeMillieSec = calendarTimeMillieSec;
            this.pendingIntentID = pendingIntentID;
            this.reminderRepeatIndex = reminderRepeatIndex;
            this.reminderRepeatItem = reminderRepeatItem;
        } else {
            linearLayout.setVisibility(View.VISIBLE);
            this.time = time;
            this.date = date;
            this.calendarTimeMillieSec = calendarTimeMillieSec;
            this.pendingIntentID = pendingIntentID;
            this.reminderRepeatIndex = reminderRepeatIndex;
            this.reminderRepeatItem = reminderRepeatItem;
            this.calendar = calendar;
            //if Daily,Weekly,Monthly,Yearly Repeat Reminders is selected
            //text value will be Time and Repeat value
            if (date.isEmpty()) {
                textReminderTimeDate.setText(time + " " + reminderRepeatItem);
            } else {
                textReminderTimeDate.setText(time + " " + date);
            }
            //Log.d("MY_TAG", calendarTimeMillieSec + "\n" + pendingIntentID + "\n" + reminderRepeatIndex);
        }
    }
}
