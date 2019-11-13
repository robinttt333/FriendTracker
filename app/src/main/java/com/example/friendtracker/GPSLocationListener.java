package com.example.friendtracker;

import android.app.Activity;
import android.location.Location;
import android.location.LocationListener;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.google.firebase.FirebaseError;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;

public class GPSLocationListener implements LocationListener {
    public Activity activity;

    GPSLocationListener(Activity activity) {
        this.activity = activity;
    }

    @Override
    public void onLocationChanged(Location location) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if(user == null )return;
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference().child("Locations").child(user.getUid());
        GeoFire geoFire = new GeoFire(ref);
        geoFire.setLocation("Location", new GeoLocation(location.getLatitude(), location.getLongitude()));

        DatabaseReference LastLocatedRef = FirebaseDatabase.getInstance().getReference().child("LastUpdated").child(FirebaseAuth.getInstance().getCurrentUser().getUid());
        HashMap lastUpdated = new HashMap();
        Calendar calForDate = Calendar.getInstance();
        SimpleDateFormat currentDate = new SimpleDateFormat("dd-MMMM-yyyy");
        String saveCurrentDate = currentDate.format(calForDate.getTime());

        Calendar calForTime = Calendar.getInstance();
        SimpleDateFormat currentTime = new SimpleDateFormat("HH:mm:ss");
        String saveCurrentTime = currentTime.format(calForTime.getTime());
        lastUpdated.put("time",saveCurrentTime);
        lastUpdated.put("date",saveCurrentDate);

        LastLocatedRef.updateChildren(lastUpdated);

    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }
}

