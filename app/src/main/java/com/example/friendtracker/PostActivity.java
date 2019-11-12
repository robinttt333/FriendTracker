package com.example.friendtracker;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.media.Image;
import android.media.audiofx.AudioEffect;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.widget.Toolbar;

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
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;

import javax.security.auth.Destroyable;

import okhttp3.internal.http2.Header;

public class PostActivity extends AppCompatActivity {

    private Toolbar mToolBar;
    private ImageView SelectPostImage;
    private Button UpdatePostButton;
    private EditText PostDescription;
    private int Gallery_Pick = 1;
    private Uri ImageUri;
    String Description;
    private StorageReference PostImagesReference;
    private String saveCurrentDate, saveCurrentTime, postRandomName;
    private String downloadUrl;
    private DatabaseReference UsersReference, PostsReference;
    private FirebaseAuth mAuth;
    private ProgressDialog loadingBar;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post);

        PostImagesReference = FirebaseStorage.getInstance().getReference();
        mToolBar = findViewById(R.id.update_post_page_toolbar);
        SelectPostImage = findViewById(R.id.select_post_image);
        UpdatePostButton = findViewById(R.id.update_post_button);
        PostDescription = findViewById(R.id.post_description);
        setSupportActionBar(mToolBar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle("Add New Post");
        loadingBar = new ProgressDialog(this);


        UsersReference = FirebaseDatabase.getInstance().getReference().child("Users");
        PostsReference = FirebaseDatabase.getInstance().getReference().child("Posts");
        mAuth = FirebaseAuth.getInstance();
        SelectPostImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                OpenGallery();

            }
        });


        UpdatePostButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ValidatePostInfo();
            }
        });

    }

    private void ValidatePostInfo() {
        Description = PostDescription.getText().toString();
        if (ImageUri == null) {
            Toast.makeText(this, "Please enter an image", Toast.LENGTH_SHORT).show();
        } else if (TextUtils.isEmpty(Description)) {
            Toast.makeText(this, "Please enter a description", Toast.LENGTH_SHORT).show();
        } else {

            StoreImageToFirebaseStorage();
        }


    }

    private void StoreImageToFirebaseStorage() {

        loadingBar.setTitle("Creating Post");
        loadingBar.setMessage("Please wait while we create your post");
        loadingBar.show();
        loadingBar.setCanceledOnTouchOutside(true);


        Calendar calForDate = Calendar.getInstance();
        SimpleDateFormat currentDate = new SimpleDateFormat("dd-MMMM-yyyy");
        saveCurrentDate = currentDate.format(calForDate.getTime());

        Calendar calForTime = Calendar.getInstance();
        SimpleDateFormat currentTime = new SimpleDateFormat("HH:mm:ss");
        saveCurrentTime = currentTime.format(calForTime.getTime());

        postRandomName = saveCurrentDate + saveCurrentTime;
        StorageReference filepath = PostImagesReference.child("Post Images").child(ImageUri.getLastPathSegment() + postRandomName + ".jpg");

        filepath.putFile(ImageUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                if (task.isSuccessful()) {

                    Task<Uri> result = task.getResult().getMetadata().getReference().getDownloadUrl();

                    result.addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {
                            downloadUrl = uri.toString();
                            SavePostDataToDataBase();
                        }
                    });

                    Toast.makeText(PostActivity.this, "Image uploaded successfully to storage", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(PostActivity.this, "Error Occurred: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        });

    }

    private void SavePostDataToDataBase() {

        UsersReference.child(mAuth.getCurrentUser().getUid()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {

                    String fullname = dataSnapshot.child("fullname").getValue().toString();
                    String image = dataSnapshot.child("profileImage").getValue().toString();

                    HashMap postMap = new HashMap();
                    postMap.put("uuid",mAuth.getCurrentUser().getUid());
                    postMap.put("profileImage",image);
                    postMap.put("fullname",fullname);
                    postMap.put("description",Description);
                    postMap.put("time",saveCurrentTime);
                    postMap.put("date",saveCurrentDate);
                    postMap.put("postImage",downloadUrl);

                    PostsReference.child(mAuth.getCurrentUser().getUid() +  postRandomName).updateChildren(postMap).addOnCompleteListener(new OnCompleteListener() {
                        @Override
                        public void onComplete(@NonNull Task task) {
                            if(task.isSuccessful()) {
                                sendUserToMainActivity();
                                Toast.makeText(PostActivity.this,"Post Updated successfully",Toast.LENGTH_SHORT).show();
                            }
                            else {
                                Toast.makeText(PostActivity.this,"Error Occurred: "+task.getException().getMessage(),Toast.LENGTH_SHORT).show();

                            }
                            loadingBar.dismiss();
                        }
                    });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });


    }

    private void OpenGallery() {
        Intent galleryIntent = new Intent();
        galleryIntent.setAction(Intent.ACTION_GET_CONTENT);
        galleryIntent.setType("image/*");
        startActivityForResult(galleryIntent, Gallery_Pick);

    }


    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        if (item.getItemId() == android.R.id.home) {
            sendUserToMainActivity();
        }

        return super.onOptionsItemSelected(item);
    }


    private void sendUserToMainActivity() {
        Intent sendToMainActivity = new Intent(this, MainActivity.class);
        startActivity(sendToMainActivity);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == Gallery_Pick && resultCode == RESULT_OK && data != null) {
            ImageUri = data.getData();
            SelectPostImage.setImageURI(ImageUri);
        }

    }

}
