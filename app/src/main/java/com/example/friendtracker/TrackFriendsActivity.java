package com.example.friendtracker;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class TrackFriendsActivity extends AppCompatActivity {
    private Toolbar mToolBar;
    private RecyclerView TrackFriendsList;
    private FirebaseAuth mAuth;
    private DatabaseReference UsersRef, FriendsRef;
    String currentUserId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_track_friends);


        mToolBar = findViewById(R.id.track_friends_appbar);
        setSupportActionBar(mToolBar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle("Select a friend to Track");

        mAuth = FirebaseAuth.getInstance();
        currentUserId = mAuth.getCurrentUser().getUid();
        FriendsRef = FirebaseDatabase.getInstance().getReference().child("Friends").child(currentUserId);
        UsersRef = FirebaseDatabase.getInstance().getReference().child("Users");

        TrackFriendsList = findViewById(R.id.track_friends_list);
        TrackFriendsList.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setReverseLayout(true);
        linearLayoutManager.setStackFromEnd(true);

        TrackFriendsList.setLayoutManager(linearLayoutManager);
        FirebaseRecyclerOptions<Friends> options = new FirebaseRecyclerOptions.Builder<Friends>().setQuery(FriendsRef, Friends.class).build();

        FirebaseRecyclerAdapter<Friends, TrackFriendsActivity.FriendsViewHolder> firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<Friends, TrackFriendsActivity.FriendsViewHolder>(options) {

            @Override
            protected void onBindViewHolder(@NonNull final TrackFriendsActivity.FriendsViewHolder holder, final int position, @NonNull final Friends model) {

                final String frienduuid = getRef(position).getKey();
                UsersRef.child(frienduuid).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        final String username = dataSnapshot.child("username").getValue().toString();
                        holder.country.setText(dataSnapshot.child("country").getValue().toString());
                        holder.status.setText(dataSnapshot.child("status").getValue().toString());
                        holder.dob.setText(dataSnapshot.child("dob").getValue().toString());
                        holder.fullname.setText(dataSnapshot.child("fullname").getValue().toString());
                        holder.username.setText(username);
                        holder.setProfileImage(frienduuid);


                        holder.mainContainer.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                sendUserToMapsActivity(frienduuid);
                            }
                        });
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });

            }

            @NonNull
            @Override
            public TrackFriendsActivity.FriendsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.friends_layout, parent, false);
                TrackFriendsActivity.FriendsViewHolder viewHolder = new TrackFriendsActivity.FriendsViewHolder(view);
                return viewHolder;
            }
        };
        TrackFriendsList.setAdapter(firebaseRecyclerAdapter);
        firebaseRecyclerAdapter.startListening();

    }


    public static class FriendsViewHolder extends RecyclerView.ViewHolder {
        TextView username, dob, fullname, country, status;
        CircleImageView profileImage;
        DatabaseReference UsersRef;
        LinearLayout mainContainer;

        public FriendsViewHolder(View itemView) {
            super(itemView);

            username = itemView.findViewById(R.id.friends_layout_username);
            fullname = itemView.findViewById(R.id.friends_layout_fullname);
            dob = itemView.findViewById(R.id.friends_layout_dob);
            profileImage = itemView.findViewById(R.id.friends_layout_profile_picture);
            status = itemView.findViewById(R.id.friends_layout_status);
            country = itemView.findViewById(R.id.friends_layout_country);
            mainContainer = itemView.findViewById(R.id.friends_layout_main_container);
        }

        public void setProfileImage(String uuid) {
            UsersRef = FirebaseDatabase.getInstance().getReference().child("Users").child(uuid);

            UsersRef.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    Picasso.get().load(R.drawable.profile);
                    if (dataSnapshot.exists() && dataSnapshot.hasChild("profileImage")) {
                        Picasso.get().load(dataSnapshot.child("profileImage").getValue().toString()).into(profileImage);
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
        }

    }

    private void sendUserToMapsActivity(String uid) {
        Intent sendUserToMapsActivityIntent = new Intent(TrackFriendsActivity.this, MapsActivity.class);
        sendUserToMapsActivityIntent.putExtra("friendUid", uid);
        startActivity(sendUserToMapsActivityIntent);

    }
}

