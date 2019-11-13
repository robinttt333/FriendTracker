package com.example.friendtracker;

public class Posts {

    public String uuid,time,date,postImage,description,fullname,profileImage;

    public Posts(){}

    public Posts(String uuid, String time, String date, String postImage, String description, String fullname,String profileImage) {
        this.uuid = uuid;
        this.time = time;
        this.date = date;
        this.postImage = postImage;
        this.profileImage = profileImage;
        this.description = description;
        this.fullname = fullname;
    }

    public String getProfileImage() {
        return this.profileImage;
    }

    public void setProfileImage(String profileImage) {
        this.profileImage = profileImage;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public String getTime() {
        return time.substring(0,time.lastIndexOf(":"));
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getPostImage() {
        return postImage;
    }

    public void setPostImage(String postImage) {
        this.postImage = postImage;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getFullname() {
        return fullname;
    }

    public void setFullname(String fullname) {
        this.fullname = fullname;
    }
}
