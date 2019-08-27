package com.notekeep.andrekelvin.notekeep;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class NoteDB {

    //DB Columns
    private final String NOTE_ID = "id";
    private final String TITLE = "title";
    private final String NOTE = "note";
    private final String DATE_TIME = "date_time";
    private final String REMINDER = "reminder";
    private final String PENDING_INTENT_ID = "pending_intent_id";
    private final String REPEAT_REMINDER = "repeat_reminder";

    //Database Name
    private final String DATABASE_NAME = "NoteDb";

    //Database Table
    private static final String DATABASE_TABLE = "Notes";

    //Database Version
    private static final int DATABASE_VERSION = 3;

    //Create Table Statement to be executed when Db is created
    private static final String DATABASE_CREATE = "create table Notes (" +
            "id integer  primary key autoincrement," +
            "title text not null," +
            "note text not null," +
            "date_time text not null," +
            "reminder int," +
            "pending_intent_id int," +
            "repeat_reminder text)";

    final Context context;
    private SQLiteDatabase db;
    private NoteDbHelper dbHelper;

    public NoteDB(Context context) {
        this.context = context;
        dbHelper = new NoteDbHelper(context);
    }

    private class NoteDbHelper extends SQLiteOpenHelper {

        public NoteDbHelper(Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            try {
                db.execSQL(DATABASE_CREATE);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            db.execSQL("DROP TABLE IF EXISTS Notes");
            onCreate(db);
        }
    }

    public NoteDbHelper open() throws SQLException {
        db = dbHelper.getWritableDatabase();
        return dbHelper;
    }

    public void close() {
        db.close();
    }

    public long insertNote(String title, String note, String date_time) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(TITLE, title);
        contentValues.put(NOTE, note);
        contentValues.put(DATE_TIME, date_time);
        return db.insert(DATABASE_TABLE, null, contentValues);
    }

    public long insertNoteReminder(String title, String note, String date_time, long reminder, int pending_intent_id, String repeat_reminder) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(TITLE, title);
        contentValues.put(NOTE, note);
        contentValues.put(DATE_TIME, date_time);
        contentValues.put(REMINDER, reminder);
        contentValues.put(PENDING_INTENT_ID, pending_intent_id);
        contentValues.put(REPEAT_REMINDER, repeat_reminder);
        return db.insert(DATABASE_TABLE, null, contentValues);
    }

    public Cursor getAllNotes() {
        return db.query(DATABASE_TABLE,
                new String[]{NOTE_ID, TITLE, NOTE, DATE_TIME, REMINDER, PENDING_INTENT_ID, REPEAT_REMINDER},
                null,
                null,
                null,
                null,
                NOTE_ID + " DESC");
    }

    public Cursor getAllReminderNotes() {
        return db.query(DATABASE_TABLE,
                new String[]{NOTE_ID, TITLE, NOTE, DATE_TIME, REMINDER, PENDING_INTENT_ID, REPEAT_REMINDER},
                REMINDER,
                null,
                null,
                null,
                NOTE_ID + " DESC");
    }

    public boolean deleteNote(int noteId) {
        return db.delete(DATABASE_TABLE, NOTE_ID + "=" + noteId, null) > 0;
    }

    public Cursor getDeletingPendingIntentID(int noteId) throws SQLException {
        Cursor cursor = db.query(DATABASE_TABLE,
                new String[]{PENDING_INTENT_ID},
                NOTE_ID + "=" + noteId,
                null,
                null,
                null,
                null,
                null);
        if (cursor != null) {
            cursor.moveToFirst();
        }
        return cursor;
    }

    public Cursor getSelectedNote(int noteId) throws SQLException {
        Cursor cursor = db.query(
                DATABASE_TABLE,
                new String[]{NOTE_ID, TITLE, NOTE, DATE_TIME, REMINDER, PENDING_INTENT_ID, REPEAT_REMINDER},
                NOTE_ID + "=" + noteId,
                null,
                null,
                null,
                null,
                null);
        if (cursor != null) {
            cursor.moveToFirst();
        }
        return cursor;
    }

    public boolean updateNote(int noteId, String title, String note, String date_time, long reminder, int pending_intent_id, String repeat_reminder) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(TITLE, title);
        contentValues.put(NOTE, note);
        contentValues.put(DATE_TIME, date_time);
        contentValues.put(REMINDER, reminder);
        contentValues.put(PENDING_INTENT_ID, pending_intent_id);
        contentValues.put(REPEAT_REMINDER, repeat_reminder);
        return db.update(DATABASE_TABLE, contentValues, NOTE_ID + "=" + noteId, null) > 0;
    }

    //When Save Icon Button is clicked
    public boolean updateNoteReminder(int noteId, long reminder, int pending_intent_id, String repeat_reminder) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(REMINDER, reminder);
        contentValues.put(PENDING_INTENT_ID, pending_intent_id);
        contentValues.put(REPEAT_REMINDER, repeat_reminder);
        return db.update(DATABASE_TABLE, contentValues, NOTE_ID + "=" + noteId, null) > 0;
    }

    public boolean updateNoteReminderToNull(int noteId) {
        ContentValues contentValues = new ContentValues();
        contentValues.putNull(REMINDER);
        contentValues.putNull(PENDING_INTENT_ID);
        contentValues.putNull(REPEAT_REMINDER);
        return db.update(DATABASE_TABLE, contentValues, NOTE_ID + "=" + noteId, null) > 0;
    }

}
