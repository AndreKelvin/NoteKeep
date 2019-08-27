package com.notekeep.andrekelvin.notekeep;

public class NoteItems {

    private String title, note, dateTime;
    private int id;

    public NoteItems(int id, String title, String note, String dateTime) {
        this.id = id;
        this.title = title;
        this.note = note;
        this.dateTime = dateTime;
    }

    public int getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getNote() {
        return note;
    }

    public String getDateTime() {
        return dateTime;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public void setDateTime(String dateTime) {
        this.dateTime = dateTime;
    }
}
