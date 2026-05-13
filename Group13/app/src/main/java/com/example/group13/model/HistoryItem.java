package com.example.group13.model;

public class HistoryItem {
    private String type;
    private String date;

    public HistoryItem() { }

    public HistoryItem(String type, String date) {
        this.type = type;
        this.date = date;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }
}
