package com.example.friendtracker;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Criteria;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.database.collection.LLRBEmptyNode;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;

public class MainActivity extends AppCompatActivity {
    private NavigationView navigationView;
    private DrawerLayout drawerLayout;
    private ActionBarDrawerToggle actionBarDrawerToggle;
    private RecyclerView postList;
    private CircleImageView navbarHeaderProfileImage;
    private TextView navbarHeaderFullname;
    private Toolbar mToolbar;
    private String currentUser;
    private FirebaseAuth mAuth;
    private DatabaseReference UsersRef, PostsRef, LikesRef;
    private ImageButton addNewPostButton;
    private FusedLocationProviderClient fusedLocationClient;
    Boolean LikeChecker = false;
    private LocationManager locationManager;
    private GPSLocationListener locationListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        checkForPermissions();
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);


        navigationView = findViewById(R.id.navigation_view);
        View navView = navigationView.inflateHeaderView(R.layout.navigation_header);
        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser().getUid();
        UsersRef = FirebaseDatabase.getInstance().getReference().child("Users");
        PostsRef = FirebaseDatabase.getInstance().getReference().child("Posts");
        LikesRef = FirebaseDatabase.getInstance().getReference().child("Likes");
        mToolbar = findViewById(R.id.main_page_toolbar);
        navbarHeaderProfileImage = navView.findViewById(R.id.nav_profile_image);
        navbarHeaderFullname = navView.findViewById(R.id.nav_user_full_name);
        addNewPostButton = findViewById(R.id.add_new_post_button);

        addNewPostButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendUserToPostActivity();
            }
        });


        UsersRef.child(currentUser).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    String fullname, image;
                    if (dataSnapshot.hasChild("fullname")) {
                        fullname = dataSnapshot.child("fullname").getValue().toString();
                        navbarHeaderFullname.setText(fullname);
                    }
                    if (dataSnapshot.hasChild("profileImage")) {
                        image = dataSnapshot.child("profileImage").getValue().toString();
                        Picasso.get().load(image).placeholder(R.drawable.profile).into(navbarHeaderProfileImage);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });


        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("Home");
        drawerLayout = findViewById(R.id.drawer_layout);
        actionBarDrawerToggle = new ActionBarDrawerToggle(this, drawerLayout, R.string.drawer_open, R.string.drawer_close);
        actionBarDrawerToggle.syncState();
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        drawerLayout.addDrawerListener(actionBarDrawerToggle);


        postList = findViewById(R.id.all_users_post_list);
        postList.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setReverseLayout(true);
        linearLayoutManager.setStackFromEnd(true);


        postList.setLayoutManager(linearLayoutManager);

        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                UserMenuSelector(menuItem);
                return false;
            }
        });

        DisplayAllPosts();
        if(!currentUser.isEmpty())
            addLocationListener();

    }

    public void addLocationListener() {
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        Criteria criteria = new Criteria();
        criteria.setAccuracy(Criteria.ACCURACY_FINE);
        final String provider = locationManager.getBestProvider(criteria, true);
        this.locationListener = new GPSLocationListener(this);
        try {
            this.locationManager.requestLocationUpdates(provider, 5, 0, locationListener);
        } catch (SecurityException e) {
            Log.e("security exceptison", "addLocationListener: ");
        }
    }

    protected void checkForPermissions(){
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED)
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 10);

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED)
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE }, 15);

    }


    public class PostAdaptor extends FirebaseRecyclerAdapter<Posts, PostsViewHolder>{

        public PostAdaptor(@NonNull FirebaseRecyclerOptions<Posts> options) {
            super(options);
        }

        @Override
        protected void onBindViewHolder(@NonNull PostsViewHolder holder, final int position, @NonNull Posts model) {

            holder.username.setText(model.getFullname());
            holder.time.setText("" + model.getTime());
            holder.date.setText("" + model.getDate());
            holder.description.setText("" + model.getDescription());
            Picasso.get().load(model.getProfileImage()).into(holder.user_post_image);
            Picasso.get().load(model.getPostImage()).into(holder.postImage);
            holder.setLikeButtonStatus(getRef(position).getKey());
            holder.mview.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent ClickPostIntent = new Intent(MainActivity.this, ClickPostActivity.class);
                    ClickPostIntent.putExtra("PostKey", getRef(position).getKey());

                    startActivity(ClickPostIntent);
                }
            });

            holder.likePostButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    LikeChecker = true;

                    LikesRef.addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            if (LikeChecker.equals(true)) {
                                if (dataSnapshot.child(getRef(position).getKey()).hasChild(currentUser)) {
                                    LikesRef.child(getRef(position).getKey()).child(currentUser).removeValue();
                                } else {
                                    LikesRef.child(getRef(position).getKey()).child(currentUser).setValue(true);
                                }
                                LikeChecker = false;
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });
                }
            });

            holder.commentPostButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    sendToCommentsActivity(getRef(position).getKey());
                }
            });
        }

        @NonNull
        @Override
        public PostsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.all_posts_layout, parent, false);
            PostsViewHolder viewHolder = new PostsViewHolder(view);
            return viewHolder;
        }

        public void deletePost(int position){
            getSnapshots().getSnapshot(position).getRef().removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if(task.isSuccessful()){
                        Toast.makeText(MainActivity.this,"Successfully deleted post",Toast.LENGTH_SHORT);
                    }
                }
            });
        }

    };
    private void DisplayAllPosts() {
        Query SortPostsInDescendingOrder = PostsRef.orderByChild("date");
        FirebaseRecyclerOptions<Posts> options = new FirebaseRecyclerOptions.Builder<Posts>().setQuery(SortPostsInDescendingOrder, Posts.class).build();

        final PostAdaptor firebaseRecyclerAdapter = new PostAdaptor(options);
        postList.setAdapter(firebaseRecyclerAdapter);
        new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(0,ItemTouchHelper.RIGHT) {
            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                firebaseRecyclerAdapter.deletePost(viewHolder.getAdapterPosition());
            }
        }).attachToRecyclerView(postList);

        firebaseRecyclerAdapter.startListening();

    }

    private void sendToCommentsActivity(String PostKey) {
        Intent CommentsActivityIntent = new Intent(this, CommentsActivity.class);
        CommentsActivityIntent.putExtra("PostKey", PostKey);
        startActivity(CommentsActivityIntent);
    }

    public static class PostsViewHolder extends RecyclerView.ViewHolder {
        TextView username, date, time, description, displayNumberOfLikes;
        CircleImageView user_post_image;
        ImageView postImage;
        ImageButton likePostButton, commentPostButton;
        int countLikes;
        String currentUserId;
        DatabaseReference LikesRef;

        View mview;

        public PostsViewHolder(View itemView) {
            super(itemView);
            mview = itemView;
            username = itemView.findViewById(R.id.post_user_name);
            date = itemView.findViewById(R.id.post_date);
            time = itemView.findViewById(R.id.post_time);
            description = itemView.findViewById(R.id.post_description);
            postImage = itemView.findViewById(R.id.post_image);
            user_post_image = itemView.findViewById(R.id.post_profile_image);
            likePostButton = itemView.findViewById(R.id.like_button);
            commentPostButton = itemView.findViewById(R.id.comment_button);
            displayNumberOfLikes = itemView.findViewById(R.id.display_number_of_likes);
            LikesRef = FirebaseDatabase.getInstance().getReference().child("Likes");
            currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        }


        public void setLikeButtonStatus(final String PostKey) {

            LikesRef.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if (dataSnapshot.child(PostKey).hasChild(currentUserId)) {
                        countLikes = (int) dataSnapshot.child(PostKey).getChildrenCount();
                        likePostButton.setImageResource(R.drawable.like);
                        displayNumberOfLikes.setText(Integer.toString(countLikes));
                    } else {
                        countLikes = (int) dataSnapshot.child(PostKey).getChildrenCount();
                        likePostButton.setImageResource(R.drawable.dislike);
                        displayNumberOfLikes.setText(Integer.toString(countLikes));
                    }

                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });

        }
    }

    @Override
    protected void onStart() {
        super.onStart();

        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            sendUserToLoginActivity();
        } else {
            CheckUserExistance();
        }
    }

    private void sendUserToPostActivity() {
        Intent sendToPostActivity = new Intent(MainActivity.this, PostActivity.class);
        startActivity(sendToPostActivity);
    }

    private void sendUserToLoginActivity() {
        Intent loginIntent = new Intent(this, LoginActivity.class);
        loginIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(loginIntent);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (actionBarDrawerToggle.onOptionsItemSelected(item)) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void UserMenuSelector(MenuItem menuitem) {
        switch (menuitem.getItemId()) {
            case R.id.nav_settings:
                sendUserToSettingsActivity();
                break;
            case R.id.nav_post:
                sendUserToPostActivity();
                break;
            case R.id.nav_profile:
                sendUserToProfileActivity();
                break;
            case R.id.nav_find_friends:
                sendUserToFindFriendsActivity();
                break;
            case R.id.nav_track_friends:
                sendUserToTrackFriendsActivity();
                break;
            case R.id.nav_track_friends_all:
                sendUserToTrackFriendsAllMapsActivity();
                break;
            case R.id.nav_logout:
                mAuth.signOut();
                sendUserToLoginActivity();
                break;
            case R.id.nav_friends:
                sendUserToFriendsActivity();

        }
    }

    private void sendUserToTrackFriendsActivity() {
        Intent sendUserToTrackFriendsIntent = new Intent(MainActivity.this, TrackFriendsActivity.class);
        startActivity(sendUserToTrackFriendsIntent);
    }

    private void sendUserToFriendsActivity() {
        Intent FriendsActivityIntent = new Intent(this, FriendsActivity.class);
        startActivity(FriendsActivityIntent);
    }

    private void sendUserToFindFriendsActivity() {

        Intent FindFriendsActivityIntent = new Intent(this, FindFriendsActivity.class);
        startActivity(FindFriendsActivityIntent);

    }

    private void CheckUserExistance() {
        final String current_user_id = mAuth.getCurrentUser().getUid();

        UsersRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (!dataSnapshot.hasChild(current_user_id)) {
                    sendUserToSetupActivity();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }


    private void sendUserToSetupActivity() {
        Intent SetupActivityIntent = new Intent(this, SetupActivity.class);
        SetupActivityIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(SetupActivityIntent);
        finish();
    }

    private void sendUserToSettingsActivity() {
        Intent SettingsActivityIntent = new Intent(this, SettingsActivity.class);
        startActivity(SettingsActivityIntent);
    }

    private void sendUserToProfileActivity() {
        Intent ProfileActivityIndent = new Intent(this, ProfileActivity.class);
        startActivity(ProfileActivityIndent);
    }

    private void sendUserToTrackFriendsAllMapsActivity() {
        Intent TrackFriendsAllIntent = new Intent(this, TrackFriendsAllMaps.class);
        startActivity(TrackFriendsAllIntent);
    }


}
