package com.example.friendtracker;

import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.FragmentActivity;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.webkit.GeolocationPermissions;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.transition.Transition;
import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.firebase.geofire.LocationCallback;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.maps.android.clustering.Cluster;
import com.google.maps.android.clustering.ClusterItem;
import com.google.maps.android.clustering.ClusterManager;
import com.google.maps.android.clustering.view.DefaultClusterRenderer;
import com.google.maps.android.ui.IconGenerator;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import org.json.JSONException;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class TrackFriendsAllMaps extends FragmentActivity implements OnMapReadyCallback, ClusterManager.OnClusterClickListener<MyItem>, ClusterManager.OnClusterInfoWindowClickListener<MyItem>, ClusterManager.OnClusterItemClickListener<MyItem>, ClusterManager.OnClusterItemInfoWindowClickListener<MyItem> {

    private GoogleMap mMap;
    private ClusterManager<MyItem> mClusterManager;
    private List<MyItem> myItemsList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_track_friends_all_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map_all_friends);
        mapFragment.getMapAsync(this);
        myItemsList = new ArrayList<MyItem>();

    }


    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        startDemo();

    }

    @Override
    public void onClusterInfoWindowClick(Cluster<MyItem> cluster) {

    }

    @Override
    public void onClusterItemInfoWindowClick(MyItem myItem) {

    }

    private class MyItemRenderer extends DefaultClusterRenderer<MyItem> {
        private IconGenerator mIconGenerator = new IconGenerator(getApplicationContext());
        private IconGenerator mClusterIconGenerator = new IconGenerator(getApplicationContext());
        private ImageView mImageView;
        private ImageView mClusterImageView;
        private int mDimension;

        public MyItemRenderer() {
            super(getApplicationContext(), mMap, mClusterManager);

            View multiProfile = getLayoutInflater().inflate(R.layout.multi_profile, null);
            mClusterIconGenerator.setContentView(multiProfile);
            mClusterImageView = (ImageView) multiProfile.findViewById(R.id.image);

            mImageView = new ImageView(getApplicationContext());
            mDimension = (int) getResources().getDimension(R.dimen.custom_profile_image);
            mImageView.setLayoutParams(new ViewGroup.LayoutParams(mDimension, mDimension));
            int padding = (int) getResources().getDimension(R.dimen.custom_profile_padding);
            mImageView.setPadding(padding, padding, padding, padding);
            mIconGenerator.setContentView(mImageView);
        }

        @Override
        protected void onBeforeClusterItemRendered(MyItem item, MarkerOptions markerOptions) {
            Bitmap icon = mIconGenerator.makeIcon();
            markerOptions.icon(BitmapDescriptorFactory.fromBitmap(icon));
        }

        @Override
        protected void onClusterItemRendered(final MyItem clusterItem, final Marker marker) {
            Glide.with(TrackFriendsAllMaps.this)
                    .load(clusterItem.getProfileImage())
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .thumbnail(0.1f)
                    .into(new CustomTarget<Drawable>() {
                        @Override
                        public void onResourceReady(@NonNull Drawable resource, @Nullable Transition<? super Drawable> transition) {
                            mImageView.setImageDrawable(resource);
                            marker.setIcon(BitmapDescriptorFactory.fromBitmap(mIconGenerator.makeIcon()));
                            marker.setTitle(clusterItem.getUsername());
                            marker.setSnippet("Last located on " + clusterItem.getDate() + " at time " + clusterItem.getTime());
                            if(clusterItem.getUsername().equals("You"))marker.showInfoWindow();
                        }

                        @Override
                        public void onLoadCleared(@Nullable Drawable placeholder) {

                        }
                    });
        }

        @Override
        protected void onBeforeClusterRendered(Cluster<MyItem> cluster, MarkerOptions markerOptions) {
            super.onBeforeClusterRendered(cluster, markerOptions);
        }

        @Override
        protected boolean shouldRenderAsCluster(Cluster cluster) {
            return cluster.getSize() > 1;
        }
    }

    @Override
    public boolean onClusterClick(Cluster<MyItem> cluster) {
        LatLngBounds.Builder builder = LatLngBounds.builder();
        for (ClusterItem item : cluster.getItems()) {
            builder.include(item.getPosition());
        }
        final LatLngBounds bounds = builder.build();
        try {
            mMap.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, 100));
        } catch (Exception e) {
            e.printStackTrace();
        }

        return true;
    }

    @Override
    public boolean onClusterItemClick(MyItem item) {
        // Does nothing, but you could go into the user's profile page, for example.
        return false;
    }

    protected void startDemo() {
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(20, 70), 3));

        mClusterManager = new ClusterManager<MyItem>(this, mMap);
        mClusterManager.setRenderer(new MyItemRenderer());
        mMap.setOnCameraIdleListener(mClusterManager);
        mMap.setOnMarkerClickListener(mClusterManager);
        mMap.setOnInfoWindowClickListener(mClusterManager);
        mClusterManager.setOnClusterClickListener(this);
        mClusterManager.setOnClusterItemClickListener(this);
        addItems();
    }

    private void addItems() {
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference FriendsRef = FirebaseDatabase.getInstance().getReference().child("Friends").child(uid);
        FriendsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                if (dataSnapshot.exists()) {
                    for (DataSnapshot user : dataSnapshot.getChildren()) {

                        final String friendUid = user.getKey();
                        DatabaseReference UserRef = FirebaseDatabase.getInstance().getReference("Users").child(friendUid);
                        UserRef.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                if (dataSnapshot.exists()) {
                                    final String friendUserName = dataSnapshot.child("username").getValue().toString();
                                    final String friendProfileImage;
                                    if (dataSnapshot.hasChild("profileImage")) {
                                        friendProfileImage = dataSnapshot.child("profileImage").getValue().toString();
                                    } else friendProfileImage = "";

                                    DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Locations").child(friendUid);
                                    GeoFire geoFire = new GeoFire(ref);
                                    geoFire.getLocation("Location", new LocationCallback() {
                                        @Override
                                        public void onLocationResult(String key, GeoLocation location) {
                                            if (location != null) {
                                                final GeoLocation lastLocation = location;
                                                FirebaseDatabase.getInstance().getReference().child("LastUpdated").child(friendUid).addValueEventListener(new ValueEventListener() {
                                                    @Override
                                                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                                        if (dataSnapshot.exists()) {
                                                            final String date = dataSnapshot.child("date").getValue().toString();
                                                            final String time = dataSnapshot.child("time").getValue().toString();
                                                            MyItem item;
                                                            if(!FirebaseAuth.getInstance().getCurrentUser().getUid().equals(friendUid))item = new MyItem(friendUserName, friendProfileImage, date, time, lastLocation);
                                                            else item = new MyItem("You", friendProfileImage, date, time, lastLocation);
                                                            mClusterManager.addItem(item);
                                                            mClusterManager.cluster();

                                                        }
                                                    }

                                                    @Override
                                                    public void onCancelled(@NonNull DatabaseError databaseError) {

                                                    }
                                                });

                                            }
                                        }

                                        @Override
                                        public void onCancelled(DatabaseError databaseError) {

                                        }
                                    });


                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {

                            }
                        });


                    }

                } else {
                    Toast.makeText(TrackFriendsAllMaps.this, "You don't have any friends as of now", Toast.LENGTH_SHORT);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }


}