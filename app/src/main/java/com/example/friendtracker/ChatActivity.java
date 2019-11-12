package com.example.friendtracker;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;


public class ChatActivity extends AppCompatActivity {
    private Toolbar mToolBar;
    private ImageButton sendImageButton, sendMessageButton;
    private EditText messageText;
    private RecyclerView messageList;
    private List<Messages> messages = new ArrayList<Messages>() {};
    private LinearLayoutManager linearLayoutManager;
    private MessagesAdapter messagesAdapter;
    private String friendUuid, friendUsername, messageSenderId;
    private TextView receiverName;
    private CircleImageView receiverProfileImage;
    private DatabaseReference rootRef;
    FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        friendUuid = getIntent().getExtras().get("uuid").toString();
        friendUsername = getIntent().getExtras().get("username").toString();
        rootRef = FirebaseDatabase.getInstance().getReference();
        mAuth = FirebaseAuth.getInstance();
        messageSenderId = mAuth.getCurrentUser().getUid();

        initializeFields();
        DisplayAppBarInfo();

        sendMessageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendMessage();
            }
        });

        FetchMessages();
    }

    private void FetchMessages() {

        rootRef.child("Messages").child(messageSenderId).child(friendUuid).addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                if(dataSnapshot.exists()) {
                    Messages message = dataSnapshot.getValue(Messages.class);
                    messages.add(message);
                    messagesAdapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void sendMessage() {
        String message = messageText.getText().toString();
        if (message.equals("")) {
            Toast.makeText(ChatActivity.this, "Please enter something", Toast.LENGTH_SHORT);
        } else {

            String MessageSenderRef = "Messages/" + messageSenderId + "/" + friendUuid;
            String MessageReceiverRef = "Messages/" + friendUuid + "/" + messageSenderId;

            DatabaseReference userMessageKeyRef = rootRef.child("Messages").child(messageSenderId).child(friendUuid).push();
            String message_push_id = userMessageKeyRef.getKey();

            Calendar calForDate = Calendar.getInstance();
            SimpleDateFormat currentDate = new SimpleDateFormat("dd-MMMM-yyyy");
            final String saveCurrentDate = currentDate.format(calForDate.getTime());

            Calendar calForTime = Calendar.getInstance();
            SimpleDateFormat currentTime = new SimpleDateFormat("HH:mm:ss");
            final String saveCurrentTime = currentTime.format(calForTime.getTime());

            HashMap messageTextBody = new HashMap();
            messageTextBody.put("message", message);
            messageTextBody.put("time", saveCurrentTime);
            messageTextBody.put("date", saveCurrentDate);
            messageTextBody.put("type", "text");
            messageTextBody.put("from", messageSenderId);

            HashMap messageBody = new HashMap();
            messageBody.put(MessageSenderRef + "/" + message_push_id, messageTextBody);
            messageBody.put(MessageReceiverRef + "/" + message_push_id, messageTextBody);

            rootRef.updateChildren(messageBody).addOnCompleteListener(new OnCompleteListener() {
                @Override
                public void onComplete(@NonNull Task task) {
                    if(task.isSuccessful()) {
                        Toast.makeText(ChatActivity.this,"Message sent successfully",Toast.LENGTH_SHORT);
                        messageText.setText("");

                    }
                }
            });

        }

    }

    private void DisplayAppBarInfo() {
        receiverName.setText(friendUsername);
        rootRef.child("Users").child(friendUuid).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Picasso.get().load(R.drawable.profile).into(receiverProfileImage);

                if (dataSnapshot.exists() && dataSnapshot.hasChild("profileImage")) {
                    Picasso.get().load(dataSnapshot.child("profileImage").getValue().toString()).into(receiverProfileImage);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void initializeFields() {
        mToolBar = findViewById(R.id.chat_app_bar);
        setSupportActionBar(mToolBar);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowCustomEnabled(true);

        LayoutInflater layoutInflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View actionBarView = layoutInflater.inflate(R.layout.chat_custom_bar, null);
        actionBar.setCustomView(actionBarView);


        sendMessageButton = findViewById(R.id.send_message_button);
        sendImageButton = findViewById(R.id.send_image_button);
        messageText = findViewById(R.id.input_message);
        receiverName = findViewById(R.id.custom_profile_name);
        receiverProfileImage = findViewById(R.id.custom_profile_image);


        messagesAdapter = new MessagesAdapter(messages);
        messageList = findViewById(R.id.chat_messages_list);
        linearLayoutManager = new LinearLayoutManager(this);
        messageList.setHasFixedSize(true);
        messageList.setLayoutManager(linearLayoutManager);
        messageList.setAdapter(messagesAdapter);
    }
}
