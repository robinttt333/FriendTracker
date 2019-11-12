package com.example.friendtracker;

import android.webkit.GeolocationPermissions;

import com.firebase.geofire.GeoLocation;
import com.google.android.gms.maps.model.LatLng;
import com.google.maps.android.clustering.ClusterItem;

public class MyItem implements ClusterItem {
    private String username,profileImage;
    private GeoLocation location;

    public MyItem(String username, String profileImage, GeoLocation location) {
        this.username = username;
        this.profileImage = profileImage;
        this.location = location;
    }

    public String getUsername() {
        return username;
    }

    public String getProfileImage() {
        return profileImage;
    }

    public GeoLocation getLocation() {
        return location;
    }

    @Override
    public LatLng getPosition() {
        return new LatLng(location.latitude,location.longitude);
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