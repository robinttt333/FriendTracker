package com.example.friendtracker;

import com.google.android.gms.maps.model.LatLng;
import com.google.maps.android.clustering.ClusterItem;

public class MyItem implements ClusterItem {
    public String name;
    public int profilePhoto;
    private LatLng mPosition;

    public MyItem(LatLng position, String name, int pictureResource) {
        this.name = name;
        profilePhoto = pictureResource;
        mPosition = position;
    }

    @Override
    public LatLng getPosition() {
        return mPosition;
    }

    public String getName() {
        return name;
    }

    public int getProfilePhoto() {
        return profilePhoto;
    }

    public LatLng getmPosition() {
        return mPosition;
    }

    @Override
    public String getTitle() {
        return null;
    }

    @Override
    public String getSnippet() {
        return null;
    }
}