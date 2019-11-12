package com.example.friendtracker;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;

public class SettingsActivity extends AppCompatActivity {

    private Toolbar mToolbar;
    private EditText username, userProfileName, userStatus, userCountry, userGender, userDob;
    private Button updateAccountSettingsButton;
    private CircleImageView userProfileImage;
    private DatabaseReference SettingsUserRef;
    private FirebaseAuth mAuth;
    private String currentUserId;
    private ProgressDialog loadingBar;
    private int GalleryPick = 1;
    private StorageReference UserProfileImageRef;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        mToolbar = findViewById(R.id.settings_toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("Account Settings");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);


        mAuth = FirebaseAuth.getInstance();
        currentUserId = mAuth.getCurrentUser().getUid();
        SettingsUserRef = FirebaseDatabase.getInstance().getReference().child("Users").child(currentUserId);
        UserProfileImageRef = FirebaseStorage.getInstance().getReference().child("Profile Images");

        username = findViewById(R.id.settings_username);
        userProfileName = findViewById(R.id.settings_profile_full_name);
        userStatus = findViewById(R.id.settings_status);
        userCountry = findViewById(R.id.settings_country);
        userGender = findViewById(R.id.settings_gender);
        userDob = findViewById(R.id.settings_dob);

        loadingBar = new ProgressDialog(this);

        updateAccountSettingsButton = findViewById(R.id.update_account_settings_button);
        userProfileImage = findViewById(R.id.settings_profile_image);

        SettingsUserRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {

                    username.setText(dataSnapshot.child("username").getValue().toString());
                    userProfileName.setText(dataSnapshot.child("fullname").getValue().toString());
                    userStatus.setText(dataSnapshot.child("status").getValue().toString());
                    userCountry.setText(dataSnapshot.child("country").getValue().toString());
                    userGender.setText(dataSnapshot.child("gender").getValue().toString());
                    userDob.setText(dataSnapshot.child("dob").getValue().toString());
                    if(dataSnapshot.hasChild("profileImage"))Picasso.get().load(dataSnapshot.child("profileImage").getValue().toString()).placeholder(R.drawable.profile).into(userProfileImage);
                    else Picasso.get().load(R.drawable.profile).into(userProfileImage);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        updateAccountSettingsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                validateProfileInfo();
            }
        });

        userProfileImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent galleryIntent = new Intent();
                galleryIntent.setAction(Intent.ACTION_GET_CONTENT);
                galleryIntent.setType("image/*");
                startActivityForResult(galleryIntent, GalleryPick);

            }
        });

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == GalleryPick && resultCode == RESULT_OK && data != null) {
            CropImage.activity()
                    .setGuidelines(CropImageView.Guidelines.ON)
                    .setAspectRatio(1, 1)
                    .start(this);
        }

        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);

            if(resultCode == RESULT_OK) {
                loadingBar.setTitle("Uploading Image");
                loadingBar.setMessage("Please wait while we upload your image");
                loadingBar.show();
                loadingBar.setCanceledOnTouchOutside(true);

                Uri resultUri = result.getUri();
                StorageReference filePath = UserProfileImageRef.child(currentUserId+".jpg");
                filePath.putFile(resultUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                        if(task.isSuccessful()){
                            Toast.makeText(SettingsActivity.this,"Profile Image uploaded successfully",Toast.LENGTH_SHORT).show();

                            Task<Uri> result = task.getResult().getMetadata().getReference().getDownloadUrl();
                            result.addOnSuccessListener(new OnSuccessListener<Uri>() {
                                @Override
                                public void onSuccess(Uri uri) {
                                    final String downloadUri = uri.toString();
                                    SettingsUserRef.child("profileImage").setValue(downloadUri).addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if(task.isSuccessful()){
                                                Intent selfIntent = new Intent(SettingsActivity.this,SettingsActivity.class);
                                                startActivity(selfIntent);
                                                Toast.makeText(SettingsActivity.this,"Profile Image stored",Toast.LENGTH_SHORT).show();
                                            }
                                            else {
                                                Toast.makeText(SettingsActivity.this,"Error Occurred: "+task.getException().getMessage(),Toast.LENGTH_SHORT).show();

                                            }
                                            loadingBar.dismiss();
                                        }
                                    });
                                }
                            });
                        }
                    }
                });


            }
            else {
                Toast.makeText(SettingsActivity.this,"Error Occurred",Toast.LENGTH_SHORT).show();
                loadingBar.dismiss();
            }
        }

    }



    private void validateProfileInfo() {
        String username = userProfileName.getText().toString(),
                profileName = userProfileName.getText().toString(),
                status = userStatus.getText().toString(),
                country = userCountry.getText().toString(),
                dob = userDob.getText().toString(),
                gender = userGender.getText().toString();

            if(TextUtils.isEmpty(username) || TextUtils.isEmpty(profileName) ||
            TextUtils.isEmpty(status) || TextUtils.isEmpty(country) || TextUtils.isEmpty(gender)
            || TextUtils.isEmpty(dob) ){
                Toast.makeText(SettingsActivity.this,"Please ensure no fields are empty",Toast.LENGTH_SHORT).show();
            }
            else {

                UpdateAccountInfo(username,profileName,status,country,dob,gender);
            }

    }

    private void UpdateAccountInfo(String username,String profileName,String status,String country,String dob,String gender) {
        HashMap profileInfo = new HashMap();
        profileInfo.put("username",username);
        profileInfo.put("fullname",profileName);
        profileInfo.put("country",country);
        profileInfo.put("gender",gender);
        profileInfo.put("dob",dob);
        profileInfo.put("status",status);

        loadingBar.setTitle("Update Profile");
        loadingBar.setMessage("Please wait while we update your profile");
        loadingBar.show();

        SettingsUserRef.updateChildren(profileInfo).addOnCompleteListener(new OnCompleteListener() {

            @Override
            public void onComplete(@NonNull Task task) {

                if(task.isSuccessful()){
                    sendUserToMainActivity();
                    Toast.makeText(SettingsActivity.this,"Successfully updated profile",Toast.LENGTH_SHORT).show();
                }
                else {
                    Toast.makeText(SettingsActivity.this,"Error Occurred: "+task.getException().getMessage(),Toast.LENGTH_SHORT).show();
                }

                loadingBar.dismiss();

            }
        });

    }
    private void sendUserToMainActivity() {
        Intent MainActivityIntent = new Intent(this, MainActivity.class);
        startActivity(MainActivityIntent);
    }

}
