package com.example.friendtracker;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;

public class ProfileActivity extends AppCompatActivity {

    private CircleImageView profileImage;
    private TextView username,userProfileName,userStatus,userCountry,userGender,userDob;
    private FirebaseAuth myAuth;
    private DatabaseReference UserRef;
    private String currentUserId;

    @Override


    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        username = findViewById(R.id.my_username);
        userProfileName = findViewById(R.id.my_profile_full_name);
        userStatus = findViewById(R.id.my_profile_status);
        userCountry = findViewById(R.id.my_country);
        userGender = findViewById(R.id.my_gender);
        userDob = findViewById(R.id.my_dob);
        profileImage = findViewById(R.id.my_profile_pic);
        myAuth = FirebaseAuth.getInstance();
        currentUserId = myAuth.getCurrentUser().getUid();
        UserRef = FirebaseDatabase.getInstance().getReference().child("Users").child(currentUserId);

        UserRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()) {

                    username.setText(dataSnapshot.child("username").getValue().toString());
                    userProfileName.setText(dataSnapshot.child("fullname").getValue().toString());
                    userStatus.setText(dataSnapshot.child("status").getValue().toString());
                    userCountry.setText(dataSnapshot.child("country").getValue().toString());
                    userGender.setText(dataSnapshot.child("gender").getValue().toString());
                    userDob.setText(dataSnapshot.child("dob").getValue().toString());
                    if(dataSnapshot.hasChild("profileImage"))Picasso.get().load(dataSnapshot.child("profileImage").getValue().toString()).into(profileImage);
                    else Picasso.get().load(R.drawable.profile).into(profileImage);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });


    }
}
