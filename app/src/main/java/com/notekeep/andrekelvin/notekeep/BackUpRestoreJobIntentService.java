package com.notekeep.andrekelvin.notekeep;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Build;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v4.app.JobIntentService;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class BackUpRestoreJobIntentService extends JobIntentService {

    public static final int JOB_ID = 123;
    private DatabaseReference databaseRef;
    private FirebaseUser firebaseUser;
    private NoteDB noteDB;
    private ValueEventListener valueEventListener;

    public static void enqueueWork(Context context, Intent work) {
        enqueueWork(context, BackUpRestoreJobIntentService.class, JOB_ID, work);
    }

    @Override
    protected void onHandleWork(@NonNull Intent intent) {
        databaseRef = FirebaseDatabase.getInstance().getReference("backup");
        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
        firebaseUser = firebaseAuth.getCurrentUser();

        noteDB = new NoteDB(this);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence channelName = "Backup Channel";
            String description = "Backup description";

            NotificationChannel notificationChannel = new NotificationChannel("CH", channelName, NotificationManager.IMPORTANCE_DEFAULT);
            notificationChannel.setDescription(description);

            NotificationManager notificationManager = (NotificationManager) this.getSystemService(NOTIFICATION_SERVICE);
            notificationManager.createNotificationChannel(notificationChannel);
        }

        final NotificationCompat.Builder noti = new NotificationCompat.Builder(this, "CH")
                .setProgress(0, 0, true)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setColor(getResources().getColor(R.color.colorPrimary));
        final NotificationManagerCompat notificationManagerCompat = NotificationManagerCompat.from(this);

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        if (sharedPreferences.getString(BackUpActivity.BACK_UP_RESTORE, "").contentEquals("Backup")) {

            noti.setContentTitle("Backing Up Notes");
            noti.setSmallIcon(R.drawable.ic_backup);
            notificationManagerCompat.notify(1, noti.build());

            databaseRef.child(firebaseUser.getUid()).removeValue();

            noteDB.open();
            final Cursor cursor = noteDB.getAllNotes();

            System.out.println("Backing up Notes");
            while (cursor.moveToNext()) {
                FirebaseBackUp backUp = new FirebaseBackUp(
                        cursor.getString(1),
                        cursor.getString(2),
                        cursor.getString(3),
                        cursor.getLong(4),
                        cursor.getInt(5),
                        cursor.getString(6));
                databaseRef.child(firebaseUser.getUid() + "/" + cursor.getInt(0)).setValue(backUp)
                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                System.out.println("on Success");
                                noti.setContentTitle("Note Successfully Backed Up")
                                        .setProgress(100, 100, false);
                                notificationManagerCompat.notify(1, noti.build());
                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                System.out.println("on Failure");
                                noti.setContentTitle("Note Back Up Failed")
                                        .setProgress(0, 0, false);
                                notificationManagerCompat.notify(1, noti.build());
                            }
                        });
                noteDB.updateNoteBackUpStatus(cursor.getInt(0), 1);

                //check if back_up value is 0(which means it's not backed up)
                //then add data to firebase and change back_up value to 1(which means it's backed up)
                /*if (cursor.getInt(7)==0){
                    System.out.println("Backing up Notes");
                    //String backUpID=addNoteActivity.databaseRef.push().getKey();
                    FirebaseBackUp backUp = new FirebaseBackUp(
                            cursor.getString(1),
                            cursor.getString(2),
                            cursor.getString(3),
                            cursor.getLong(4),
                            cursor.getInt(5),
                            cursor.getString(6));
                    databaseRef.child(firebaseUser.getUid()+"/"+cursor.getInt(0)).setValue(backUp);
                    noteDB.updateNoteBackUpStatus(cursor.getInt(0),1);
                }*/
            }
            System.out.println("Notes backed up");
            noteDB.close();
        } else if (sharedPreferences.getString(BackUpActivity.BACK_UP_RESTORE, "").contentEquals("Restore")) {
            //Restore
            if (firebaseUser != null) {
                noti.setContentTitle("Restoring Notes");
                noti.setSmallIcon(R.drawable.ic_cloud_download);
                notificationManagerCompat.notify(1, noti.build());

                valueEventListener = databaseRef.child(firebaseUser.getUid()).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        noteDB.open();
                        for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
                            FirebaseBackUp backUp = postSnapshot.getValue(FirebaseBackUp.class);
                            System.out.println("Restoring up Notes");
                            if (backUp.getReminder() == 0) {
                                noteDB.insertNote(backUp.getTitle(), backUp.getNote(), backUp.getDateTime(), 1);
                            } else {
                                noteDB.insertNoteReminder(backUp.getTitle(), backUp.getNote(), backUp.getDateTime(),
                                        backUp.getReminder(), backUp.getPendingIntentId(), backUp.getRepeatReminder(), 1);
                            }
                        }

                        noti.setContentTitle("Note Successfully Restored");
                        noti.setProgress(100, 100, false);
                        notificationManagerCompat.notify(1, noti.build());

                        //Toast.makeText(BackUpRestoreJobIntentService.this, "Note Restored", Toast.LENGTH_SHORT).show();
                        System.out.println("Note Restored");
                        noteDB.close();
                        databaseRef.child(firebaseUser.getUid()).removeEventListener(valueEventListener);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        noti.setContentTitle("Note Restore Failed");
                        noti.setProgress(0, 0, false);
                        notificationManagerCompat.notify(1, noti.build());
                    }
                });
            }
        }
    }
}
