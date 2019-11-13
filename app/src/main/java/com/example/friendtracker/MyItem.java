package com.example.friendtracker;

import android.webkit.GeolocationPermissions;

import com.firebase.geofire.GeoLocation;
import com.google.android.gms.maps.model.LatLng;
import com.google.maps.android.clustering.ClusterItem;

public class MyItem implements ClusterItem {
    private String username, profileImage, date, time;
    private GeoLocation location;

    public MyItem(String username, String profileImage, String date, String time, GeoLocation location) {
        this.username = username;
        this.profileImage = profileImage;
        this.location = location;
        this.date = date;
        this.time = time;
    }

    public String getUsername() {
        return username;
    }

    public String getDate() {
        return date;
    }

    public String getTime() {
        return time.substring(0,time.lastIndexOf(":"));
    }

    public String getProfileImage() {
        return profileImage;
    }

    public GeoLocation getLocation() {
        return location;
    }

    @Override
    public LatLng getPosition() {
        return new LatLng(location.latitude, location.longitude);
    }

    @Override
    public String getTitle() {
        return username;
    }

    @Override
    public String getSnippet() {
        return null;
    }
}