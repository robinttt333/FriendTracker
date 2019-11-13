package com.example.friendtracker;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
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
import java.util.Set;

import de.hdodenhof.circleimageview.CircleImageView;

public class SetupActivity extends AppCompatActivity {

    private EditText FullName, UserName, CountryName;
    private Button SaveInformationButton;
    private CircleImageView ProfileImage;
    private FirebaseAuth mAuth;
    private DatabaseReference UsersRef,FriendsRef;
    String currentUserId;
    private StorageReference UserProfileImageRef;
    private ProgressDialog loadingBar;
    final static int GalleryPick = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setup);

        loadingBar = new ProgressDialog(this);
        mAuth = FirebaseAuth.getInstance();
        currentUserId = mAuth.getCurrentUser().getUid();
        UsersRef = FirebaseDatabase.getInstance().getReference().child("Users").child(currentUserId);
        FriendsRef = FirebaseDatabase.getInstance().getReference().child("Friends");
        UserProfileImageRef = FirebaseStorage.getInstance().getReference().child("Profile Images");
        UserName = findViewById(R.id.setup_username);
        FullName = findViewById(R.id.setup_full_name);
        CountryName = findViewById(R.id.setup_country_name);
        SaveInformationButton = findViewById(R.id.setup_information_button);
        ProfileImage = findViewById(R.id.setup_profile_image);

        SaveInformationButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SaveAccountSetupInformation();
            }
        });

        ProfileImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent galleryIntent = new Intent();
                galleryIntent.setAction(Intent.ACTION_GET_CONTENT);
                galleryIntent.setType("image/*");
                startActivityForResult(galleryIntent, GalleryPick);
            }
        });

        UsersRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists() && dataSnapshot.hasChild("profileImage")) {

                    String image = dataSnapshot.child("profileImage").getValue().toString();
                    Picasso.get().load(image).placeholder(R.drawable.profile).into(ProfileImage);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

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
                            Toast.makeText(SetupActivity.this,"Profile Image uploaded successfully",Toast.LENGTH_SHORT).show();

                            Task<Uri> result = task.getResult().getMetadata().getReference().getDownloadUrl();
                            result.addOnSuccessListener(new OnSuccessListener<Uri>() {
                                @Override
                                public void onSuccess(Uri uri) {
                                    final String downloadUri = uri.toString();
                                    UsersRef.child("profileImage").setValue(downloadUri).addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if(task.isSuccessful()){

                                                Intent setupActivityIntent = new Intent(SetupActivity.this,SetupActivity.class);
                                                startActivity(setupActivityIntent);

                                                Toast.makeText(SetupActivity.this,"Profile Image stored",Toast.LENGTH_SHORT).show();
                                            }
                                            else {
                                                Toast.makeText(SetupActivity.this,"Error Occurred: "+task.getException().getMessage(),Toast.LENGTH_SHORT).show();

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
                Toast.makeText(SetupActivity.this,"Error Occurred",Toast.LENGTH_SHORT).show();
                loadingBar.dismiss();
            }
        }

    }

    private void SaveAccountSetupInformation() {
        String username = UserName.getText().toString();
        String fullname = FullName.getText().toString();
        String country = CountryName.getText().toString();

        if (TextUtils.isEmpty(username)) {
            Toast.makeText(this, "Username is empty", Toast.LENGTH_SHORT).show();
        } else if (TextUtils.isEmpty(fullname)) {
            Toast.makeText(this, "Fullname is empty", Toast.LENGTH_SHORT).show();
        }
        if (TextUtils.isEmpty(country)) {
            Toast.makeText(this, "Country is empty", Toast.LENGTH_SHORT).show();
        } else {

            loadingBar.setTitle("Saving Profile Info");
            loadingBar.setMessage("Please wait while save your data");
            loadingBar.show();
            loadingBar.setCanceledOnTouchOutside(true);

            HashMap usermap = new HashMap();
            usermap.put("username", username);
            usermap.put("fullname", fullname);
            usermap.put("country", country);
            usermap.put("status", "Hey there...I am using here");
            usermap.put("gender", "none");
            usermap.put("dob", "");
            UsersRef.updateChildren(usermap).addOnCompleteListener(new OnCompleteListener() {
                @Override
                public void onComplete(@NonNull Task task) {
                    if (task.isSuccessful()) {
                        HashMap friendsMap = new HashMap();
                        friendsMap.put("friend",true);
                        FriendsRef.child(currentUserId).child(currentUserId).updateChildren(friendsMap).addOnCompleteListener(new OnCompleteListener() {
                            @Override
                            public void onComplete(@NonNull Task task) {
                                if(task.isSuccessful()){
                                    SendUserToMainActivity();
                                    Toast.makeText(SetupActivity.this, "Profile created successfully", Toast.LENGTH_SHORT).show();

                                }
                            }
                        });

                    } else {
                        Toast.makeText(SetupActivity.this, "Error occurred: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();

                    }
                    loadingBar.dismiss();
                }
            });
        }

    }

    private void SendUserToMainActivity() {
        Intent MainActivityIntent = new Intent(this, MainActivity.class);
        startActivity(MainActivityIntent);
        finish();
    }

}
