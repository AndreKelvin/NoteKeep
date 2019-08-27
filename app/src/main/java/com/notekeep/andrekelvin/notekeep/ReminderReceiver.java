package com.notekeep.andrekelvin.notekeep;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;

import static android.content.Context.NOTIFICATION_SERVICE;

public class ReminderReceiver extends BroadcastReceiver {

    private final String CHANNEL_ID = "Channel ID";
    private final Uri soundUri= RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
    private int NOTIFICATION_ID = 7;
    private final String GROUP_NOTIFICATION="com.example.andrekelvin.notekeep";

    @Override
    public void onReceive(Context context, Intent intent) {
        int numberOfNoti = 0;
        long insertedNoteID;
        String noteTitle,noteTitleFor6Below;

        Bundle extra=intent.getExtras();
        insertedNoteID=extra.getLong("INSERTED_NOTE_ID");
        noteTitle=extra.getString("TITLE");
        noteTitleFor6Below=extra.getString("TITLE");
        //Toast.makeText(context, "Inserted ID "+insertedNoteID, Toast.LENGTH_LONG).show();

        /*
        to make sure if the notification is more than one, Group it
        and the NOTIFICATION_ID value increments to make sure different notification
        and title is displayed.
        this SharedPreference will return all the saved NumberOfNotification,NotificationID,NoteTitle
        whereby NumberOfNotification and NotificationID will be incremented by 1 and assigned back to them
        and NoteTitle will be adding the newly passed note title then assign it back to noteTitle String
        after that, put the newly assigned values to SharedPreference
         */
        SharedPreferences sharedPreferences= PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor=sharedPreferences.edit();
        if (sharedPreferences.getInt("NumberOfNotification",0)==0){
            editor.putInt("NumberOfNotification",1);
            editor.putInt("NotificationID",1);
            editor.putString("NoteTitle",noteTitle);
            editor.apply();
        }
        else {
            numberOfNoti = sharedPreferences.getInt("NumberOfNotification", 0)+1;
            NOTIFICATION_ID = sharedPreferences.getInt("NotificationID", 0)+1;
            //noteTitle = sharedPreferences.getString("NoteTitle","")+","+noteTitle;
            editor.putInt("NumberOfNotification",numberOfNoti);
            editor.putInt("NotificationID",NOTIFICATION_ID);
            editor.putString("NoteTitle",noteTitle);
            editor.apply();
        }

        Intent reminderIntent = new Intent(context,EditNoteActivity.class);
        reminderIntent.putExtra("NOTE_ID",(int)insertedNoteID);
        reminderIntent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent =PendingIntent.getActivity(context,(int)System.currentTimeMillis(),reminderIntent,PendingIntent.FLAG_UPDATE_CURRENT);


        //Create Channel for Android 8 above
        if (Build.VERSION.SDK_INT>=Build.VERSION_CODES.O){
            CharSequence channelName="Reminder Channel";
            String description="Reminder description";

            NotificationChannel notificationChannel = new NotificationChannel(CHANNEL_ID, channelName, NotificationManager.IMPORTANCE_DEFAULT);
            notificationChannel.setDescription(description);

            NotificationManager notificationManager = (NotificationManager) context.getSystemService(NOTIFICATION_SERVICE);
            notificationManager.createNotificationChannel(notificationChannel);
        }

        /*
        so because Android 6 below can't expand Grouped notification
        i wouldn't Group it because each notification needs to open Edit Activity
        to edit a particular note
        even if it's Grouped, when the notification is clicked it will just open pending intent directly
        not like from Android 7 above where Grouped notification is clicked and it expends to display
        each notification then clicking each notification opens pending intent.
         */
        if (Build.VERSION.SDK_INT<=Build.VERSION_CODES.M){
            NotificationManagerCompat notificationManagerCompat = NotificationManagerCompat.from(context);

            NotificationCompat.Builder notiBuilder=new NotificationCompat.Builder(context,CHANNEL_ID);
            notiBuilder.setSmallIcon(R.drawable.ic_notifications);
            notiBuilder.setContentTitle("Note Reminder");
            notiBuilder.setContentText(noteTitleFor6Below);
            notiBuilder.setSound(soundUri);
            //Remove the notification when its tapped
            notiBuilder.setAutoCancel(true);
            //open an activity when notification is tapped
            notiBuilder.setContentIntent(pendingIntent);
            //For android 8 below you can set Priority by using the "setPriority()" method
            notiBuilder.setPriority(NotificationCompat.PRIORITY_DEFAULT);
            //Group the notification
            //notiBuilder.setGroup(GROUP_NOTIFICATION);

            //if Notification is more than 1, Group it
            /*if (numberOfNoti>1){
                NotificationCompat.Builder builderSummary=new NotificationCompat.Builder(context,CHANNEL_ID);
                builderSummary.setSmallIcon(R.drawable.ic_notifications);
                builderSummary.setStyle(new NotificationCompat.InboxStyle()
                                        .setBigContentTitle(numberOfNoti+" Note Reminders")
                                        .setSummaryText(numberOfNoti+" Note Reminders")
                                        .addLine(noteTitle));
                TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
                stackBuilder.addNextIntent(reminderIntent);
                PendingIntent summaryIntent = stackBuilder.getPendingIntent((int)System.currentTimeMillis(), PendingIntent.FLAG_UPDATE_CURRENT);
                builderSummary.setContentIntent(summaryIntent);
                builderSummary.setPriority(NotificationCompat.PRIORITY_DEFAULT);
                builderSummary.setGroup(GROUP_NOTIFICATION);
                builderSummary.setSound(soundUri);
                builderSummary.setGroupSummary(true);
                builderSummary.setAutoCancel(true);
                builderSummary.build();
                notificationManagerCompat.notify(100, builderSummary.build());
            }*/
            //To display the notification
            notificationManagerCompat.notify(NOTIFICATION_ID, notiBuilder.build());
        }else {
            NotificationManagerCompat notificationManagerCompat = NotificationManagerCompat.from(context);

            NotificationCompat.Builder notiBuilder=new NotificationCompat.Builder(context,CHANNEL_ID);
            notiBuilder.setSmallIcon(R.drawable.ic_notifications);
            notiBuilder.setContentTitle("Note Reminder");
            notiBuilder.setContentText(noteTitle);
            notiBuilder.setSound(soundUri);
            //Remove the notification when its tapped
            notiBuilder.setAutoCancel(true);
            //open an activity when notification is tapped
            notiBuilder.setContentIntent(pendingIntent);
            //For android 8 below you can set Priority by using the "setPriority()" method
            notiBuilder.setPriority(NotificationCompat.PRIORITY_DEFAULT);
            //Group the notification
            notiBuilder.setGroup(GROUP_NOTIFICATION);

            //if Notification is more than 1, Group it
            if (numberOfNoti>1){
                NotificationCompat.Builder builderSummary=new NotificationCompat.Builder(context,CHANNEL_ID);
                builderSummary.setSmallIcon(R.drawable.ic_notifications);
                builderSummary.setStyle(new NotificationCompat.InboxStyle()
                                                            .setBigContentTitle(numberOfNoti+" Note Reminders")
                                                            .setSummaryText(numberOfNoti+" Note Reminders")
                                                            .addLine(noteTitle));
                builderSummary.setPriority(NotificationCompat.PRIORITY_DEFAULT);
                builderSummary.setGroup(GROUP_NOTIFICATION);
                builderSummary.setSound(soundUri);
                builderSummary.setGroupSummary(true);
                builderSummary.setAutoCancel(true);
                builderSummary.build();
                notificationManagerCompat.notify(100, builderSummary.build());
            }
            //To display the notification
            notificationManagerCompat.notify(NOTIFICATION_ID, notiBuilder.build());
        }
    }
}
