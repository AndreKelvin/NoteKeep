package com.notekeep.andrekelvin.notekeep;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.preference.PreferenceManager;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.net.URL;
import java.net.URLConnection;

public class BackupReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(final Context context, Intent intent) {
        final NoteDB noteDB = new NoteDB(context);

        final DatabaseReference databaseRef = FirebaseDatabase.getInstance().getReference("backup");
        final FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
        final FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);

        /*Map<String, Object> updates = new HashMap<>();

        final List<String> noteIDList = new ArrayList<>();
        final List<String> firebaseNoteIDList = new ArrayList<>();*/

        boolean autoBackup = sharedPreferences.getBoolean(BackUpActivity.AUTO_BACKUP, false);

        if (autoBackup) {

            if (isNetworkAvailable(context)) {
                //Toast.makeText(context, "Network Available Do operations", Toast.LENGTH_LONG).show();
                System.out.println("Network Available");

                if (firebaseUser != null) {
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
                        databaseRef.child(firebaseUser.getUid() + "/" + cursor.getInt(0)).setValue(backUp);
                        noteDB.updateNoteBackUpStatus(cursor.getInt(0), 1);
                    }
                    System.out.println("Notes backed up");
                    noteDB.close();
                }

                //noteIDList.clear();

            /*while (cursor.moveToNext()) {
                //check if back_up value is 0(which means it's not backed up)
                //then add data to firebase and change back_up value to 1(which means it's backed up)
                noteIDList.add(String.valueOf(cursor.getInt(0)));
                if (cursor.getInt(7) == 0) {
                    System.out.println("Backing up added note to firebase");
                    //String backUpID=addNoteActivity.databaseRef.push().getKey();
                    FirebaseBackUp backUp = new FirebaseBackUp(
                            cursor.getString(1),
                            cursor.getString(2),
                            cursor.getString(3),
                            cursor.getLong(4),
                            cursor.getInt(5),
                            cursor.getString(6));
                    databaseRef.child(firebaseUser.getUid() + "/" + cursor.getInt(0)).setValue(backUp);
                    noteDB.updateNoteBackUpStatus(cursor.getInt(0), 1);
                }
                //check if back_up value is 2(which means it was edited)
                //then update firebase data and change back_up value to 1(which means it's backed up)
                else if (cursor.getInt(7) == 2) {
                    System.out.println("Backing up edited note to firebase");
                    //String backUpID=addNoteActivity.databaseRef.push().getKey();
                    updates.put("title", cursor.getString(1));
                    updates.put("note", cursor.getString(2));
                    updates.put("dateTime", cursor.getString(3));
                    updates.put("reminder", cursor.getLong(4));
                    updates.put("pendingIntentId", cursor.getInt(5));
                    updates.put("repeatReminder", cursor.getString(6));
                    databaseRef.child(firebaseUser.getUid()).child(String.valueOf(cursor.getInt(0))).updateChildren(updates);
                    noteDB.updateNoteBackUpStatus(cursor.getInt(0), 1);
                }
            }*/
                //Log.d("MY_TAG","NoteID:"+noteIDList.toString());

                //Get note id from firebase Then check if noteIDList contains any of those id
                //If it doesn't contain, delete note data from firebase
            /*if (firebaseUser != null) {
                valueEventListener=databaseRef.child(firebaseUser.getUid()).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        firebaseNoteIDList.clear();
                        for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
                            postSnapshot.getValue(FirebaseBackUp.class);
                            //System.out.println("NoteID:" + postSnapshot.getKey());
                            firebaseNoteIDList.add(postSnapshot.getKey());
                            *//*if (!noteIDList.contains(postSnapshot.getKey())) {
                                System.out.println("Deleting note from firebase");
                                databaseRef.child(firebaseAuth.getUid() + "/" + postSnapshot.getKey()).removeValue();
                            }*//*
                            count++;
                        }

                        *//*System.out.println("NoteID:"+noteIDList.toString());*//*
                        //Collections.reverse(firebaseNoteIDList);
                        Collections.reverse(firebaseNoteIDList);
                        Log.d("MY_TAG","FirebaseID:"+firebaseNoteIDList.toString());
                        for (int i = 0; i <= firebaseNoteIDList.size()-1; i++) {
                            Log.d("MY_TAG","in loop");
                            if (noteIDList.contains(firebaseNoteIDList.get(i))) {
                                Log.d("MY_TAG","Note Contains "+firebaseNoteIDList.get(i));
                            }else {
                                Log.d("MY_TAG","Deleting note "+firebaseNoteIDList.get(i)+" from firebase");
                                //databaseRef.child(firebaseAuth.getUid() + "/" + firebaseNoteIDList.get(i)).removeValue();
                            }
                        }
                        databaseRef.child(firebaseUser.getUid()).removeEventListener(valueEventListener);
                        *//*Log.d("MY_TAG","Child Count "+dataSnapshot.getChildrenCount());

                        if (count>=dataSnapshot.getChildrenCount()){
                            Log.d("MY_TAG","Count "+count);
                            count=0;
                        }*//*
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {}
                });
            }*/

            /*for (int i = 0; i <= firebaseNoteIDList.size()-1; i++) {
                System.out.println("In Delete Note Loop");
                if (!noteIDList.contains(firebaseNoteIDList.get(i))) {
                    System.out.println("Deleting note from firebase");
                    databaseRef.child(firebaseAuth.getUid() + "/" + firebaseNoteIDList.get(i)).removeValue();
                }
            }*/

            /*final Handler handler = new Handler();
            new Thread(new Runnable() {
                @Override
                public void run() {
                    final boolean isConnected = isAbleToConnect();
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            if (isConnected) {
                                Toast.makeText(context, "You are ONLINE!", Toast.LENGTH_LONG).show();
                                System.out.println("You are ONLINE!");

                                noteIDList.clear();
                                noteDB.open();
                                final Cursor cursor=noteDB.getAllNotes();

                                while (cursor.moveToNext()) {
                                    //check if back_up value is 0(which means it's not backed up)
                                    //then add data to firebase and change back_up value to 1(which means it's backed up)
                                    if (cursor.getInt(7)==0){
                                        //String backUpID=addNoteActivity.databaseRef.push().getKey();
                                        FirebaseBackUp backUp = new FirebaseBackUp(
                                                cursor.getString(1),
                                                cursor.getString(2),
                                                cursor.getString(3),
                                                cursor.getLong(4),
                                                cursor.getInt(5),
                                                cursor.getString(6));
                                        databaseRef.child(firebaseUser.getUid()+"/"+cursor.getInt(0)).setValue(backUp,
                                                new DatabaseReference.CompletionListener() {
                                                    @Override
                                                    public void onComplete(@Nullable DatabaseError databaseError, @NonNull DatabaseReference databaseReference) {
                                                        if (databaseError==null){
                                                            noteDB.updateNoteBackUpStatus(cursor.getInt(0),1);
                                                        }
                                                    }
                                                });
                                    }
                                    //check if back_up value is 2(which means it was edited)
                                    //then update firebase data and change back_up value to 1(which means it's backed up)
                                    else if (cursor.getInt(7) == 2) {
                                        //String backUpID=addNoteActivity.databaseRef.push().getKey();
                                        updates.put("title",cursor.getString(1));
                                        updates.put("note",cursor.getString(2));
                                        updates.put("dateTime",cursor.getString(3));
                                        updates.put("reminder",cursor.getLong(4));
                                        updates.put("pendingIntentId",cursor.getInt(5));
                                        updates.put("repeatReminder",cursor.getString(6));
                                        databaseRef.child(firebaseUser.getUid()).child(String.valueOf(cursor.getInt(0))).updateChildren(updates,
                                                new DatabaseReference.CompletionListener() {
                                                    @Override
                                                    public void onComplete(@Nullable DatabaseError databaseError, @NonNull DatabaseReference databaseReference) {
                                                        if (databaseError==null){
                                                            noteDB.updateNoteBackUpStatus(cursor.getInt(0),1);
                                                        }
                                                    }
                                                });
                                    }
                                    noteIDList.add(String.valueOf(cursor.getInt(0)));
                                }

                                //Get note id from firebase, save to list
                                //Then check if any of does id contains noteIDList
                                //If it doesn't contain, delete note data from firebase
                                databaseRef.addValueEventListener(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                        for (DataSnapshot postSnapshot : dataSnapshot.getChildren()){
                                            postSnapshot.getValue(FirebaseBackUp.class);
                                            firebaseNoteIDList.add(postSnapshot.getKey());
                                        }
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError databaseError) {}
                                });

                                for (int i = 0; i < firebaseNoteIDList.size() ; i++) {
                                    if (!noteIDList.contains(firebaseNoteIDList.get(i))){
                                        databaseRef.child(firebaseAuth.getUid()+"/"+firebaseNoteIDList.get(i)).removeValue();
                                    }
                                }

                                noteDB.close();
                            }
                            else {
                                Toast.makeText(context, "You are OFFLINE!", Toast.LENGTH_LONG).show();
                                System.out.println("You are OFFLINE!");
                            }
                        }
                    });

                }
            }).start();*/
            }
        }
    }

    //This only checks if the network interface is available
    //doesn't guarantee a particular network service is available
    // for example, there could be low signal or no data to access the internet
    public boolean isNetworkAvailable(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = cm.getActiveNetworkInfo();
        return networkInfo != null && networkInfo.isConnected();
    }

    //This makes a real connection to an url and checks if you can connect to this url
    // this needs to be wrapped in a background thread
    private boolean isAbleToConnect() {
        try {
            URL myUrl = new URL("http://www.google.com");
            URLConnection connection = myUrl.openConnection();
            connection.setConnectTimeout(1000);
            connection.connect();
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
