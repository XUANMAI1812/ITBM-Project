package com.example.group13.model;

import com.google.firebase.firestore.Exclude;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class Reminder {

    @Exclude
    private String id;

    private String title;
    private String note;
    private String priority;
    private long deadlineMillis;
    private boolean done;
    private String userId;

    public Reminder() {}

    public Reminder(String title, String note, long deadlineMillis, String priority, String userId) {
        this.title = title;
        this.note = note;
        this.deadlineMillis = deadlineMillis;
        this.priority = priority;
        this.userId = userId;
        this.done = false;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getTitle() { return title; }
    public String getNote() { return note; }
    public String getPriority() { return priority; }
    public long getDeadlineMillis() { return deadlineMillis; }
    public boolean isDone() { return done; }
    public String getUserId() { return userId; }

    public void setTitle(String title) { this.title = title; }
    public void setNote(String note) { this.note = note; }
    public void setPriority(String priority) { this.priority = priority; }
    public void setDeadlineMillis(long deadlineMillis) { this.deadlineMillis = deadlineMillis; }
    public void setDone(boolean done) { this.done = done; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getDate() {
        return new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                .format(new Date(deadlineMillis));
    }

    public String getTime() {
        return new SimpleDateFormat("HH:mm", Locale.getDefault())
                .format(new Date(deadlineMillis));
    }
}
