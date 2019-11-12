package com.example.friendtracker;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

import org.w3c.dom.Text;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;

public class CommentsActivity extends AppCompatActivity {
    private ImageButton postCommentButton;
    private EditText inputComment;
    private RecyclerView commentsList;
    private String PostKey;
    private DatabaseReference UserRef,PostsRef;
    private FirebaseAuth mAuth;
    String currentUserId,commentMessage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_comments);
        PostKey = getIntent().getExtras().get("PostKey").toString();

        mAuth = FirebaseAuth.getInstance();
        currentUserId = mAuth.getCurrentUser().getUid();
        UserRef = FirebaseDatabase.getInstance().getReference().child("Users").child(currentUserId);
        PostsRef = FirebaseDatabase.getInstance().getReference().child("Posts").child(PostKey).child("Comments");

        postCommentButton = findViewById(R.id.post_comment_button);
        inputComment = findViewById(R.id.comment_input);
        commentsList = findViewById(R.id.comments_list);

        commentsList.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        commentsList.setLayoutManager(linearLayoutManager);

        postCommentButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                commentMessage = inputComment.getText().toString();

                if (commentMessage.isEmpty()) {
                    Toast.makeText(CommentsActivity.this, "Please enter some text", Toast.LENGTH_SHORT).show();
                } else {

                    UserRef.addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            if(dataSnapshot.exists()) {
                                String username = dataSnapshot.child("username").getValue().toString();
                                Calendar calForDate = Calendar.getInstance();
                                SimpleDateFormat currentDate = new SimpleDateFormat("dd-MMMM-yyyy");
                                String saveCurrentDate = currentDate.format(calForDate.getTime());

                                Calendar calForTime = Calendar.getInstance();
                                SimpleDateFormat currentTime = new SimpleDateFormat("HH:mm:ss");
                                String saveCurrentTime = currentTime.format(calForTime.getTime());

                                String commentRandomKey = currentUserId + saveCurrentDate + saveCurrentTime;

                                HashMap comment = new HashMap();
                                comment.put("uuid",currentUserId);
                                comment.put("comment",commentMessage);
                                comment.put("date",saveCurrentDate);
                                comment.put("time",saveCurrentTime);
                                comment.put("username",username);

                                PostsRef.child(commentRandomKey).updateChildren(comment);

                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });


                }
                inputComment.setText("");
            }
        });

        DisplayComments();

    }

    private void DisplayComments() {
        FirebaseRecyclerOptions<Comment> firebaseRecyclerOptions = new FirebaseRecyclerOptions.Builder<Comment>().setQuery(PostsRef,Comment.class).build();
        FirebaseRecyclerAdapter<Comment,CommentsViewHolder> firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<Comment, CommentsViewHolder>(firebaseRecyclerOptions) {
            @Override
            protected void onBindViewHolder(@NonNull final CommentsViewHolder holder, int position, @NonNull Comment model) {

                holder.commentDate.setText(model.getDate());
                holder.commentTime.setText(model.getTime());
                holder.commentUsername.setText(model.getUsername());
                holder.commentText.setText(model.getComment());
                UserRef.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if(dataSnapshot.exists() && dataSnapshot.hasChild("profileImage")) {

                            Picasso.get().load(dataSnapshot.child("profileImage").getValue().toString()).into(holder.commentsProfileImage);

                        }
                        else {
                            Picasso.get().load(R.drawable.profile).into(holder.commentsProfileImage);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
            }

            @NonNull
            @Override
            public CommentsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.on_comments_layout, parent, false);
                CommentsViewHolder viewHolder = new CommentsViewHolder(view);
                return viewHolder;
            }
        };
        commentsList.setAdapter(firebaseRecyclerAdapter);
        firebaseRecyclerAdapter.startListening();
    }

    public static class CommentsViewHolder extends RecyclerView.ViewHolder{
        CircleImageView commentsProfileImage;
        TextView commentDate,commentTime,commentUsername,commentText;

        public CommentsViewHolder(@NonNull View itemView) {
            super(itemView);
            commentsProfileImage= itemView.findViewById(R.id.comment_profile_image);
            commentDate = itemView.findViewById(R.id.comment_date);
            commentTime = itemView.findViewById(R.id.comment_time);
            commentUsername = itemView.findViewById(R.id.comment_username);
            commentText = itemView.findViewById(R.id.comment_text);
        }
    }
}
