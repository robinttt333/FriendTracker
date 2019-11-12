package com.example.friendtracker;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;

public class PersonProfileActivity extends AppCompatActivity {

    private CircleImageView profileImage;
    private TextView username, userProfileName, userStatus, userCountry, userGender, userDob;
    private FirebaseAuth myAuth;
    private DatabaseReference UserRef,FriendRequestRef,FriendsRef;
    private String currentUserId,uuid;
    private Button sendFriendRequestButton, declineFriendRequestButton;
    private String STATE = "SEND";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_person_profile);

        uuid = getIntent().getExtras().get("uuid").toString();
        username = findViewById(R.id.person_profile_username);
        userProfileName = findViewById(R.id.person_profile_fullname);
        userStatus = findViewById(R.id.person_profile_status);
        userCountry = findViewById(R.id.person_profile_country);
        userGender = findViewById(R.id.person_profile_gender);
        userDob = findViewById(R.id.person_profile_dob);
        profileImage = findViewById(R.id.person_profile_picture);
        sendFriendRequestButton = findViewById(R.id.person_profile_send_friend_request_button);
        declineFriendRequestButton = findViewById(R.id.person_profile_decline_friend_request_button);

        myAuth = FirebaseAuth.getInstance();
        currentUserId = myAuth.getCurrentUser().getUid();
        UserRef = FirebaseDatabase.getInstance().getReference().child("Users").child(uuid);
        FriendRequestRef = FirebaseDatabase.getInstance().getReference().child("FriendRequests");
        FriendsRef = FirebaseDatabase.getInstance().getReference().child("Friends");

        sendFriendRequestButton.setVisibility(View.VISIBLE);
        sendFriendRequestButton.setEnabled(true);
        declineFriendRequestButton.setVisibility(View.INVISIBLE);
        declineFriendRequestButton.setEnabled(false);


        UserRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {

                    username.setText(dataSnapshot.child("username").getValue().toString());
                    userProfileName.setText(dataSnapshot.child("fullname").getValue().toString());
                    userStatus.setText(dataSnapshot.child("status").getValue().toString());
                    userGender.setText(dataSnapshot.child("gender").getValue().toString());
                    userDob.setText(dataSnapshot.child("dob").getValue().toString());
                    userCountry.setText(dataSnapshot.child("country").getValue().toString());

                    if (dataSnapshot.hasChild("profileImage"))
                        Picasso.get().load(dataSnapshot.child("profileImage").getValue().toString()).into(profileImage);
                    else Picasso.get().load(R.drawable.profile).into(profileImage);

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        if(currentUserId.equals(uuid)){
            declineFriendRequestButton.setVisibility(View.INVISIBLE);
            declineFriendRequestButton.setEnabled(false);
            sendFriendRequestButton.setVisibility(View.INVISIBLE);
            sendFriendRequestButton.setEnabled(false);
        }
        else {

            FriendsRef.child(currentUserId).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if(dataSnapshot.hasChild(uuid)){
                        STATE = "FRIENDS";
                        sendFriendRequestButton.setVisibility(View.VISIBLE);
                        sendFriendRequestButton.setEnabled(true);
                        sendFriendRequestButton.setText("Unfriend User");
                        declineFriendRequestButton.setVisibility(View.INVISIBLE);
                        declineFriendRequestButton.setEnabled(false);
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });

            FriendRequestRef.child(currentUserId+uuid).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if(dataSnapshot.exists()) {
                        STATE = "SENT";
                        sendFriendRequestButton.setEnabled(true);
                        sendFriendRequestButton.setText("CANCEL REQUEST");
                        sendFriendRequestButton.setVisibility(View.VISIBLE);
                        declineFriendRequestButton.setVisibility(View.INVISIBLE);
                        declineFriendRequestButton.setEnabled(false);
                    }

                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
            FriendRequestRef.child(uuid+currentUserId).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if(dataSnapshot.exists()) {
                        STATE = "RECEIVED";
                        sendFriendRequestButton.setVisibility(View.VISIBLE);
                        sendFriendRequestButton.setEnabled(true);
                        sendFriendRequestButton.setText("ACCEPT Friend Request");
                        declineFriendRequestButton.setVisibility(View.VISIBLE);
                        declineFriendRequestButton.setEnabled(true);
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
            sendFriendRequestButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    sendFriendRequestButton.setEnabled(false);
                    if(STATE.equals("SEND")){
                        sendFriendRequest(currentUserId,uuid);
                    }
                    else if(STATE.equals("SENT")) {
                        cancelFriendRequest(currentUserId,uuid);
                    }
                    else if(STATE.equals("RECEIVED")){
                        FriendsRef.child(currentUserId).child(uuid).child("friend").setValue(true).addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if(task.isSuccessful()) {
                                    FriendsRef.child(uuid).child(currentUserId).child("friend").setValue(true).addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if(task.isSuccessful()) {

                                                FriendRequestRef.child(uuid+currentUserId).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                                    @Override
                                                    public void onComplete(@NonNull Task<Void> task) {
                                                        STATE = "FRIENDS";
                                                        sendFriendRequestButton.setVisibility(View.VISIBLE);
                                                        sendFriendRequestButton.setEnabled(true);
                                                        sendFriendRequestButton.setText("Unfriend user");
                                                        declineFriendRequestButton.setVisibility(View.INVISIBLE);
                                                        declineFriendRequestButton.setEnabled(false);
                                                    }
                                                });

                                            }
                                        }
                                    });
                                }
                            }
                        });
                    }
                    else if(STATE.equals("FRIENDS")){
                        FriendsRef.child(uuid).child(currentUserId).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if(task.isSuccessful()) {
                                    FriendsRef.child(currentUserId).child(uuid).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if(task.isSuccessful()){
                                                STATE = "SEND";
                                                declineFriendRequestButton.setVisibility(View.INVISIBLE);
                                                declineFriendRequestButton.setEnabled(false);
                                                sendFriendRequestButton.setVisibility(View.VISIBLE);
                                                sendFriendRequestButton.setEnabled(true);
                                                sendFriendRequestButton.setText("Send Friend request");
                                            }
                                        }
                                    });

                                }
                            }
                        });
                    }
                }
            });

            declineFriendRequestButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    cancelFriendRequest(uuid,currentUserId);
                }
            });
        }




    }

    private void cancelFriendRequest(String sender,String receiver) {
        FriendRequestRef.child(sender+receiver).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful()) {
                    STATE = "SEND";
                    declineFriendRequestButton.setVisibility(View.INVISIBLE);
                    declineFriendRequestButton.setEnabled(false);
                    sendFriendRequestButton.setVisibility(View.VISIBLE);
                    sendFriendRequestButton.setEnabled(true);
                    sendFriendRequestButton.setText("Send Friend request");


                }
            }
        });
    }

    private void sendFriendRequest(String sender,String receiver) {

        HashMap request = new HashMap();
        request.put("sender",sender);
        request.put("receiver",receiver);
        FriendRequestRef.child(sender+receiver).updateChildren(request);


    }
}
