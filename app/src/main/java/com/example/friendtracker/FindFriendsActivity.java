package com.example.friendtracker;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;
import io.grpc.InternalNotifyOnServerBuild;

public class FindFriendsActivity extends AppCompatActivity {

    private Toolbar mToolBar;
    private ImageButton SearchButton;
    private EditText SearchInputText;

    private RecyclerView SearchResultList;
    private DatabaseReference FindFriendsref;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_find_friends);

        mAuth = FirebaseAuth.getInstance();
        FindFriendsref = FirebaseDatabase.getInstance().getReference().child("Users");

        mToolBar = findViewById(R.id.find_friends_appbar_layout);
        setSupportActionBar(mToolBar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Find Friends");

        SearchResultList = findViewById(R.id.search_results_list);
        SearchResultList.setHasFixedSize(true);
        SearchResultList.setLayoutManager(new LinearLayoutManager(this));

        SearchButton = findViewById(R.id.search_people_button);
        SearchInputText = findViewById(R.id.search_box_input);

        SearchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String query = SearchInputText.getText().toString();
                if(query.isEmpty()){
                    Toast.makeText(FindFriendsActivity.this,"Please enter something",Toast.LENGTH_SHORT).show();
                }
                else {
                    SearchPeopleAndFriends(query);
                }
            }
        });


    }

    private void SearchPeopleAndFriends(String query){

        Toast.makeText(FindFriendsActivity.this,"Searching...",Toast.LENGTH_LONG).show();

        Query searchPeopleAndFriends = FindFriendsref.orderByChild("fullname").startAt(query).endAt(query+"\uf8ff");

        FirebaseRecyclerOptions<FindFriends> options=new FirebaseRecyclerOptions.Builder<FindFriends>().setQuery(searchPeopleAndFriends,FindFriends.class).build();

        FirebaseRecyclerAdapter<FindFriends,FindFriendsViewHolder> firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<FindFriends, FindFriendsViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull FindFriendsViewHolder holder, final int position, @NonNull FindFriends model) {
                final String uuid = getRef(position).getKey();
                holder.username.setText(model.getFullname());
                holder.status.setText(model.getStatus());
                Picasso.get().load(model.getProfileImage()).placeholder(R.drawable.profile).into(holder.profileimage);

                holder.linearLayout.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        sendUserToPersonProfileActivity(uuid);
                    }
                });

            }

            @NonNull
            @Override
            public FindFriendsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view= LayoutInflater.from(parent.getContext()).inflate(R.layout.all_users_display_layout,parent,false);
                FindFriendsViewHolder viewHolder=new FindFriendsViewHolder(view);
                return viewHolder;
            }
        };

        SearchResultList.setAdapter(firebaseRecyclerAdapter);
        firebaseRecyclerAdapter.startListening();

    }

    public static class FindFriendsViewHolder extends RecyclerView.ViewHolder{
        TextView username, status;
        CircleImageView profileimage;
        LinearLayout linearLayout;

        public FindFriendsViewHolder(@NonNull View itemView) {
            super(itemView);
            username = itemView.findViewById(R.id.all_users_profile_name);
            status = itemView.findViewById(R.id.all_users_status);
            profileimage = itemView.findViewById(R.id.all_users_profile_image);
            linearLayout = itemView.findViewById(R.id.click_find_friends);
        }
    }

    private void sendUserToPersonProfileActivity(String uuid){

        Intent sendUserToPersonProfileActivityIntent = new Intent(this,PersonProfileActivity.class);
        sendUserToPersonProfileActivityIntent.putExtra("uuid",uuid);
        startActivity(sendUserToPersonProfileActivityIntent);
    }

}
