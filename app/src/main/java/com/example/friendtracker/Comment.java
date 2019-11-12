package com.example.friendtracker;

import android.widget.ImageView;

public class Comment {
    String date;
    String time;
    String uuid;
    String username;
    String comment;

    public Comment(){}

    public Comment(String date, String time, String uuid, String username, String comment) {
        this.date = date;
        this.time = time;
        this.uuid = uuid;
        this.username = username;
        this.comment = comment;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }
}
