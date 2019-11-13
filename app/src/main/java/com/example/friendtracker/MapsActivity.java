package com.example.friendtracker;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.firebase.geofire.LocationCallback;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApi;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.internal.GoogleApiAvailabilityCache;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.net.HttpRetryException;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback
{

    private GoogleMap mMap;
    private String uid;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        uid  = getIntent().getExtras().get("friendUid").toString();
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }


    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        GeoFire geoFire = new GeoFire(FirebaseDatabase.getInstance().getReference().child("Locations").child(uid));
        geoFire.getLocation("Location", new LocationCallback() {
            @Override
            public void onLocationResult(String key, final GeoLocation location) {
                if (location != null) {

                    FirebaseDatabase.getInstance().getReference().child("LastUpdated").child(uid).addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            if(dataSnapshot.exists()){

                                final String date = dataSnapshot.child("date").getValue().toString();
                                final String time = dataSnapshot.child("time").getValue().toString();

                                FirebaseDatabase.getInstance().getReference().child("Users").child(uid).addValueEventListener(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                        LatLng lastLocation = new LatLng(location.latitude,location.longitude);
                                        CameraPosition cameraPosition = new CameraPosition.Builder()
                                                .target(lastLocation)
                                                .build();
                                        mMap.addMarker(new MarkerOptions().position(lastLocation)
                                                .title(dataSnapshot.child("username").getValue().toString()).snippet("Last Updated on "+ date + " at time " + time)).showInfoWindow();
                                        mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));

                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError databaseError) {

                                    }
                                });


                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });


                } else {
                    Toast.makeText(MapsActivity.this,"There is no location for key " + key +" in GeoFire",Toast.LENGTH_SHORT);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(MapsActivity.this,"There was an error getting the GeoFire location: " + databaseError,Toast.LENGTH_SHORT);
            }
        });
    }

}
