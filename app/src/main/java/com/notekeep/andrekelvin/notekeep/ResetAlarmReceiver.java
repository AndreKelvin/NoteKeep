package com.notekeep.andrekelvin.notekeep;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Build;
import android.widget.Toast;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

public class ResetAlarmReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        if (Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction())) {
            //Toast.makeText(context, "Alarm Restarted", Toast.LENGTH_LONG).show();
            /*
            Select all Note Reminder from Db
            Get the Time Milli seconds and convert to Time
            Check if the Time is equal or after current mobile time
            Set Alarm
            */
            NoteDB noteDB = new NoteDB(context);
            Calendar calendar = new GregorianCalendar();
            Date currentDate = new Date();

            noteDB.open();

            Cursor cursor = noteDB.getAllReminderNotes();
            while (cursor.moveToNext()) {
                calendar.setTimeInMillis(cursor.getLong(4));
                if (calendar.getTime().after(currentDate) || calendar.getTime().equals(currentDate)) {

                    if (cursor.getString(6).contentEquals("Don't Repeat")) {
                        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
                        Intent intentReminder = new Intent(context, ReminderReceiver.class);
                        intentReminder.putExtra("INSERTED_NOTE_ID", cursor.getLong(0));
                        intentReminder.putExtra("TITLE", cursor.getString(1));
                        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, cursor.getInt(5), intentReminder, 0);
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                            alarmManager.setExact(AlarmManager.RTC_WAKEUP, cursor.getLong(4), pendingIntent);
                        } else {
                            alarmManager.set(AlarmManager.RTC_WAKEUP, cursor.getLong(4), pendingIntent);
                        }
                    }
                    else if (cursor.getString(6).contentEquals("Daily")) {
                        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
                        Intent intentReminder = new Intent(context, ReminderReceiver.class);
                        intentReminder.putExtra("INSERTED_NOTE_ID", cursor.getLong(0));
                        intentReminder.putExtra("TITLE", cursor.getString(1));
                        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, cursor.getInt(5), intentReminder, 0);
                        alarmManager.setInexactRepeating(AlarmManager.RTC_WAKEUP, cursor.getLong(4), AlarmManager.INTERVAL_DAY, pendingIntent);
                    }
                    else if (cursor.getString(6).contentEquals("Weekly")) {
                        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
                        Intent intentReminder = new Intent(context, ReminderReceiver.class);
                        intentReminder.putExtra("INSERTED_NOTE_ID", cursor.getLong(0));
                        intentReminder.putExtra("TITLE", cursor.getString(1));
                        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, cursor.getInt(5), intentReminder, 0);
                        alarmManager.setInexactRepeating(AlarmManager.RTC_WAKEUP, cursor.getLong(4), (AlarmManager.INTERVAL_DAY * 7), pendingIntent);
                    }
                    else if (cursor.getString(6).contentEquals("Monthly")) {
                        // get today's date
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

                        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
                        Intent intentReminder = new Intent(context, ReminderReceiver.class);
                        intentReminder.putExtra("INSERTED_NOTE_ID", cursor.getLong(0));
                        intentReminder.putExtra("TITLE", cursor.getString(1));
                        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, cursor.getInt(5), intentReminder, 0);
                        alarmManager.setInexactRepeating(AlarmManager.RTC_WAKEUP, cursor.getLong(4), monthInterval, pendingIntent);
                    }
                    else if (cursor.getString(6).contentEquals("Yearly")) {
                        calendar.setTimeInMillis(System.currentTimeMillis());
                        int year = calendar.get(Calendar.YEAR);
                        calendar.set(Calendar.YEAR, year + 1);

                        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
                        Intent intentReminder = new Intent(context, ReminderReceiver.class);
                        intentReminder.putExtra("INSERTED_NOTE_ID",cursor.getLong(0));
                        intentReminder.putExtra("TITLE",cursor.getString(1));
                        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, cursor.getInt(5), intentReminder, 0);
                        alarmManager.setInexactRepeating(AlarmManager.RTC_WAKEUP, cursor.getLong(4), 0, pendingIntent);
                    }

                }
            }
            noteDB.close();
        }
    }
}
