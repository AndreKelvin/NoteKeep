package com.notekeep.andrekelvin.notekeep;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.method.ScrollingMovementMethod;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;

public class EditNoteActivity extends AppCompatActivity implements EditReminderDialogFragment.ReminderValues {

    private EditText textTitle, textNote;
    private TextView textViewTitle, textViewNote, textTime, textReminderDate;
    private LinearLayout linearLayout;
    private NoteDB noteDB;
    private int selectedNoteId;
    private Menu menu;
    private FloatingActionButton fab;
    private InputMethodManager inputMethodManager;
    private Bundle reminderValuesBundle;
    private Calendar calendar;
    private String time, date, reminderRepeatItem;
    private int reminderRepeatIndex, pendingIntentID;
    private long calendarTimeMillieSec;
    public static final String TIME = "time";
    public static final String DATE = "date";
    public static final String REMINDER_REPEAT = "repeat";
    public static final String SELECTED_NOTE_PENDING_INTENT_ID = "selectedNotePendingIntentID";
    public static final String SELECTED_NOTE_ID = "selectedNoteID";
    private int selectedNotePendingIntentID;
    private ImageView reminderEditIcon;
    private String selectedTitle, selectedNote, selectedNoteRepeatStatus;

    /*@Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        int pendingIntentNoteID;

        if (intent != null) {
            Bundle extra=intent.getExtras();
            pendingIntentNoteID = extra.getInt("NOTE_ID");
            //System.out.println(pendingIntentNoteID);
        } else {
            pendingIntentNoteID = 0;
        }
        Toast.makeText(this, "The ID "+pendingIntentNoteID, Toast.LENGTH_LONG).show();
    }*/

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_note);

        textTitle = findViewById(R.id.editTitle);
        textNote = findViewById(R.id.editNote);
        textViewTitle = findViewById(R.id.textViewTitle);
        textViewNote = findViewById(R.id.textViewNote);
        textTime = findViewById(R.id.textTime);
        textReminderDate = findViewById(R.id.textViewReminderDate);
        fab = findViewById(R.id.fabEditNote);
        linearLayout = findViewById(R.id.editLinearLayout);
        reminderEditIcon = findViewById(R.id.reminderEditIcon);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        setTitle("Edit Note");

        noteDB = new NoteDB(this);
        noteDB.open();

        reminderValuesBundle = new Bundle();

        Bundle extra = getIntent().getExtras();
        selectedNoteId = extra.getInt("NOTE_ID");
        //onNewIntent(getIntent());

        Cursor cursor = noteDB.getSelectedNote(selectedNoteId);
        if (cursor.moveToFirst()) {
            selectedTitle = cursor.getString(1);
            selectedNote = cursor.getString(2);
            if (cursor.getLong(4) != 0) {
                textTitle.setText(selectedTitle);
                textViewTitle.setText(selectedTitle);
                textNote.setText(selectedNote);
                textViewNote.setText(selectedNote);
                textTime.setText(cursor.getString(3));
                selectedNotePendingIntentID = cursor.getInt(5);
                linearLayout.setVisibility(View.VISIBLE);

                //if reminder was set to be repeated, convert only the time.
                //and set the reminderRepeatIndex for the spinner
                SimpleDateFormat dateFormat;
                if (cursor.getString(6).equals("Don't Repeat")) {
                    reminderRepeatItem = cursor.getString(6);
                    dateFormat = new SimpleDateFormat("hh:mm a dd/MMM/yy", Locale.UK);
                    textReminderDate.setText(dateFormat.format(new Date(cursor.getLong(4))));

                    //this change the notification bell icon
                    //when the reminder time has passed or equals to the current time
                    Calendar calendar = new GregorianCalendar();
                    calendar.setTimeInMillis(cursor.getLong(4));
                    Date currentDate = new Date();
                    if (calendar.getTime().before(currentDate) || calendar.getTime().equals(currentDate)) {
                        reminderEditIcon.setImageResource(R.drawable.ic_notifications_off);
                    }
                } else if (cursor.getString(6).equals("Daily")) {
                    dateFormat = new SimpleDateFormat("hh:mm a", Locale.UK);
                    reminderRepeatItem = cursor.getString(6);
                    reminderRepeatIndex = 1;
                    textReminderDate.setText(dateFormat.format(new Date(cursor.getLong(4))) + " " + reminderRepeatItem);
                } else if (cursor.getString(6).equals("Weekly")) {
                    dateFormat = new SimpleDateFormat("hh:mm a", Locale.UK);
                    reminderRepeatItem = cursor.getString(6);
                    reminderRepeatIndex = 2;
                    textReminderDate.setText(dateFormat.format(new Date(cursor.getLong(4))) + " " + reminderRepeatItem);
                } else if (cursor.getString(6).equals("Monthly")) {
                    dateFormat = new SimpleDateFormat("hh:mm a", Locale.UK);
                    reminderRepeatItem = cursor.getString(6);
                    reminderRepeatIndex = 3;
                    textReminderDate.setText(dateFormat.format(new Date(cursor.getLong(4))) + " " + reminderRepeatItem);
                } else if (cursor.getString(6).equals("Yearly")) {
                    dateFormat = new SimpleDateFormat("hh:mm a", Locale.UK);
                    reminderRepeatItem = cursor.getString(6);
                    reminderRepeatIndex = 4;
                    textReminderDate.setText(dateFormat.format(new Date(cursor.getLong(4))) + " " + reminderRepeatItem);
                }
            } else {
                textTitle.setText(selectedTitle);
                textViewTitle.setText(selectedTitle);
                textNote.setText(selectedNote);
                textViewNote.setText(selectedNote);
                textTime.setText(cursor.getString(3));
            }
        }
        noteDB.close();

        textViewTitle.setMovementMethod(new ScrollingMovementMethod());
        textViewNote.setMovementMethod(new ScrollingMovementMethod());

        inputMethodManager = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);

    }

    //Replace the Action Bar Menu when ever this Activity is visible
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.edit_note, menu);
        this.menu = menu;
        return true;
    }

    //Save Note when the save icon is selected on the Menu Bar
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.edit_note:
                String title = textTitle.getText().toString().trim();
                String note = textNote.getText().toString().trim();
                SimpleDateFormat dateFormat = new SimpleDateFormat("hh:mm a dd/MMM/yy", Locale.UK);
                String dateTime = dateFormat.format(new Date());


                //Hide Keyboard
                inputMethodManager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);

                if (title.isEmpty() || note.isEmpty()) {
                    Toast.makeText(EditNoteActivity.this, "Empty Note... Insert Your Title and Note ", Toast.LENGTH_SHORT).show();
                } else {
                    noteDB.open();
                    //if linearLayout visibility is INVISIBLE that means no reminder was saved
                    //just insert notes and date to db (reminder columns are nullable)
                    //else insert notes,date and reminder values and start alarm
                    /*if (linearLayout.getVisibility() == View.INVISIBLE) {
                        noteDB.updateNote(selectedNoteId,textTitle.getText().toString(),textNote.getText().toString(),dateTime);

                        finish();
                        Toast.makeText(EditNoteActivity.this, "Note Edited", Toast.LENGTH_SHORT).show();
                    }else {*/
                    noteDB.updateNote(selectedNoteId, textTitle.getText().toString(), textNote.getText().toString(), dateTime, calendarTimeMillieSec, pendingIntentID, reminderRepeatItem);
                    //noteDB.updateNoteReminder(selectedNoteId,calendarTimeMillieSec,pendingIntentID,reminderRepeatItem);
                    Toast.makeText(EditNoteActivity.this, "Note Edited", Toast.LENGTH_SHORT).show();

                    if (reminderRepeatItem != null) {
                        //Cancel the Selected Note Reminder
                        //Pass Selected Note ID to broadcast receiver class
                        //Where the notification has Pending Intent that is going to open this Activity
                        //And broadcast receiver is going to pass this ID as Bundle to this Activity

                        AlarmManager cancelAlarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
                        Intent cancelIntent = new Intent(EditNoteActivity.this, ReminderReceiver.class);
                        PendingIntent cancelPendingIntent = PendingIntent.getBroadcast(EditNoteActivity.this, selectedNotePendingIntentID, cancelIntent, 0);
                        cancelAlarmManager.cancel(cancelPendingIntent);

                        //Don't Repeat
                        if (reminderRepeatItem.contentEquals("Don't Repeat")) {
                            AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
                            Intent intent = new Intent(EditNoteActivity.this, ReminderReceiver.class);
                            intent.putExtra("INSERTED_NOTE_ID", (long) selectedNoteId);
                            intent.putExtra("TITLE", textTitle.getText().toString());
                            PendingIntent pendingIntent = PendingIntent.getBroadcast(EditNoteActivity.this, pendingIntentID, intent, 0);
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                                alarmManager.setExact(AlarmManager.RTC_WAKEUP, calendarTimeMillieSec, pendingIntent);
                            } else {
                                alarmManager.set(AlarmManager.RTC_WAKEUP, calendarTimeMillieSec, pendingIntent);
                            }
                            Toast.makeText(EditNoteActivity.this, "Note and Reminder Edited", Toast.LENGTH_SHORT).show();
                        }
                        //Daily Repeat
                        else if (reminderRepeatItem.contentEquals("Daily")) {
                            AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
                            Intent intent = new Intent(EditNoteActivity.this, ReminderReceiver.class);
                            intent.putExtra("INSERTED_NOTE_ID", (long) selectedNoteId);
                            intent.putExtra("TITLE", textTitle.getText().toString());
                            PendingIntent pendingIntent = PendingIntent.getBroadcast(EditNoteActivity.this, pendingIntentID, intent, 0);
                            alarmManager.setInexactRepeating(AlarmManager.RTC_WAKEUP, calendarTimeMillieSec, AlarmManager.INTERVAL_DAY, pendingIntent);
                            Toast.makeText(EditNoteActivity.this, "Note and Reminder Edited. Will Repeat Daily", Toast.LENGTH_SHORT).show();
                        }
                        //Weekly Repeat
                        else if (reminderRepeatItem.contentEquals("Weekly")) {
                            AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
                            Intent intent = new Intent(EditNoteActivity.this, ReminderReceiver.class);
                            intent.putExtra("INSERTED_NOTE_ID", (long) selectedNoteId);
                            intent.putExtra("TITLE", textTitle.getText().toString());
                            PendingIntent pendingIntent = PendingIntent.getBroadcast(EditNoteActivity.this, pendingIntentID, intent, 0);
                            alarmManager.setInexactRepeating(AlarmManager.RTC_WAKEUP, calendarTimeMillieSec, (AlarmManager.INTERVAL_DAY * 7), pendingIntent);
                            Toast.makeText(EditNoteActivity.this, "Note and Reminder Edited. Will Repeat Weekly", Toast.LENGTH_SHORT).show();
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
                            Intent intent = new Intent(EditNoteActivity.this, ReminderReceiver.class);
                            intent.putExtra("INSERTED_NOTE_ID", (long) selectedNoteId);
                            intent.putExtra("TITLE", textTitle.getText().toString());
                            PendingIntent pendingIntent = PendingIntent.getBroadcast(EditNoteActivity.this, pendingIntentID, intent, 0);
                            alarmManager.setInexactRepeating(AlarmManager.RTC_WAKEUP, calendarTimeMillieSec, monthInterval, pendingIntent);
                            Toast.makeText(EditNoteActivity.this, "Note and Reminder Edited. Will Repeat Monthly", Toast.LENGTH_SHORT).show();
                        }
                        //Yearly Repeat
                        else if (reminderRepeatItem.contentEquals("Yearly")) {
                            calendar.setTimeInMillis(System.currentTimeMillis());
                            int year = calendar.get(Calendar.YEAR);
                            calendar.set(Calendar.YEAR, year + 1);

                            AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
                            Intent intent = new Intent(EditNoteActivity.this, ReminderReceiver.class);
                            intent.putExtra("INSERTED_NOTE_ID", (long) selectedNoteId);
                            intent.putExtra("TITLE", textTitle.getText().toString());
                            PendingIntent pendingIntent = PendingIntent.getBroadcast(EditNoteActivity.this, pendingIntentID, intent, 0);
                            alarmManager.setInexactRepeating(AlarmManager.RTC_WAKEUP, calendarTimeMillieSec, 0, pendingIntent);
                            Toast.makeText(EditNoteActivity.this, "Note and Reminder Edited. Will Repeat Yearly", Toast.LENGTH_SHORT).show();
                        }

                        //}
                    }
                    noteDB.close();
                    finish();
                }
                return true;
            case R.id.editReminder:
                int theLength = textReminderDate.getText().length();
                if (theLength != 0) {
                    /*
                    /To make sure Time,Date,Spinner has values the first time Dialog is opened
                    /because when Dialog Fragment is open the first time
                    /the interface that is used to Communicate hasn't transfer data from
                    /the Dialog to the Activity. So the Time and Date TextView will be empty
                    /and Spinner item will be "Don't Repeat"
                    /Since Time and Date is concatenated in the same textView
                    /Extract the Time and Date part and pass each part
                    /reminderRepeatIndex has already been assigned in db query
                     */
                    time = textReminderDate.getText().subSequence(0, 8).toString();
                    date = textReminderDate.getText().subSequence(9, theLength).toString();
                    reminderValuesBundle.putString(TIME, time);
                    reminderValuesBundle.putString(DATE, date);
                    reminderValuesBundle.putInt(REMINDER_REPEAT, reminderRepeatIndex);
                } else {
                    //Pass the saved reminder values back to dialog fragment
                    reminderValuesBundle.putString(TIME, time);
                    reminderValuesBundle.putString(DATE, date);
                    reminderValuesBundle.putInt(REMINDER_REPEAT, reminderRepeatIndex);
                }

                //Pass selected note pending intent id to able to cancel reminder
                reminderValuesBundle.putInt(SELECTED_NOTE_PENDING_INTENT_ID, selectedNotePendingIntentID);
                reminderValuesBundle.putInt(SELECTED_NOTE_ID, selectedNoteId);
                EditReminderDialogFragment editReminder = new EditReminderDialogFragment();
                editReminder.setArguments(reminderValuesBundle);
                editReminder.show(getSupportFragmentManager(), "Edit Reminder");
                //hide keyboard
                inputMethodManager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public void editNote(View view) {
        textTitle.setVisibility(View.VISIBLE);
        textNote.setVisibility(View.VISIBLE);
        menu.findItem(R.id.edit_note).setVisible(true);
        menu.findItem(R.id.editReminder).setVisible(true);

        textViewTitle.setVisibility(View.GONE);
        textViewNote.setVisibility(View.GONE);
        fab.hide();

        textTitle.requestFocus();
        //show keyboard
        inputMethodManager.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
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
            reminderEditIcon.setImageResource(R.drawable.ic_notifications);
            this.time = time;
            this.date = date;
            this.calendarTimeMillieSec = calendarTimeMillieSec;
            this.pendingIntentID = pendingIntentID;
            this.reminderRepeatIndex = reminderRepeatIndex;
            this.reminderRepeatItem = reminderRepeatItem;
            this.calendar = calendar;
            //if Daily,Weekly,Monthly,Yearly Repeat Reminders is selected
            //text view will be Time and Repeat value
            if (date.isEmpty()) {
                textReminderDate.setText(time + " " + reminderRepeatItem);
            } else {
                textReminderDate.setText(time + " " + date);
            }
            //Log.d("MY_TAG", calendarTimeMillieSec + "\n" + pendingIntentID + "\n" + reminderRepeatIndex);
        }
    }
}
