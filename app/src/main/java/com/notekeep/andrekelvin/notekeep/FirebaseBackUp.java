package com.notekeep.andrekelvin.notekeep;

public class FirebaseBackUp {

    private String title,note,dateTime,repeatReminder;
    private long  reminder;
    private int pendingIntentId;

    public FirebaseBackUp() {
        //empty constructor needed
    }

    public FirebaseBackUp(String title, String note, String dateTime, long reminder, int pendingIntentId, String repeatReminder) {
        this.title = title;
        this.note = note;
        this.dateTime = dateTime;
        this.reminder = reminder;
        this.pendingIntentId = pendingIntentId;
        this.repeatReminder = repeatReminder;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public String getDateTime() {
        return dateTime;
    }

    public void setDateTime(String dateTime) {
        this.dateTime = dateTime;
    }

    public long getReminder() {
        return reminder;
    }

    public void setReminder(long reminder) {
        this.reminder = reminder;
    }

    public int getPendingIntentId() {
        return pendingIntentId;
    }

    public void setPendingIntentId(int pendingIntentId) {
        this.pendingIntentId = pendingIntentId;
    }

    public String getRepeatReminder() {
        return repeatReminder;
    }

    public void setRepeatReminder(String repeatReminder) {
        this.repeatReminder = repeatReminder;
    }
}
