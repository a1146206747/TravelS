package com.example.databasetest;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import com.example.databasetest.Adapter.FriendsAdapter;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class ManageFriendRequests extends AppCompatActivity {

    FirebaseAuth auth;
    RecyclerView chatRoomRecycleView;
    FriendsAdapter adapter;
    FirebaseDatabase db;
    ArrayList<Users> friendsArrayList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_room_home_page);

        db = FirebaseDatabase.getInstance();
        auth = FirebaseAuth.getInstance();
        String currentUserID = auth.getCurrentUser().getUid();
        DatabaseReference friendRequestsRef = FirebaseDatabase.getInstance().getReference().child("friends").child(currentUserID).child("FriendRequests");

        friendsArrayList = new ArrayList<>();

        friendRequestsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                friendsArrayList.clear();
                for(DataSnapshot snapshot: dataSnapshot.getChildren()){
                    String requestId = snapshot.getKey();
                    DatabaseReference userReference = FirebaseDatabase.getInstance().getReference().child("user").child(requestId);
                    userReference.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot userSnapshot) {
                            Users user = userSnapshot.getValue(Users.class);
                            if (user != null) {
                                friendsArrayList.add(user);
                                adapter.notifyDataSetChanged();
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {
                        }
                    });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });


        chatRoomRecycleView = findViewById(R.id.chatRoomRecycleView);
        chatRoomRecycleView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new FriendsAdapter(ManageFriendRequests.this, friendsArrayList);
        chatRoomRecycleView.setAdapter(adapter);

        ImageButton btnProfile = findViewById(R.id.btn_profile);
        ImageButton btnHomePage = findViewById(R.id.btn_homePage);
        ImageButton btnLogout = findViewById(R.id.btn_logout);
        ImageButton btnChatRoomCtrl = findViewById(R.id.btn_chatRoomCtorl);

        btnProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ManageFriendRequests.this, ProfileActivity.class);
                startActivity(intent);
            }
        });

        btnHomePage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Replace HomeActivity with your actual home page activity
                Intent intent = new Intent(ManageFriendRequests.this, homePage.class);
                startActivity(intent);
            }
        });

        btnLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FirebaseAuth.getInstance().signOut();
                Intent intent = new Intent(ManageFriendRequests.this, homePage.class);
                startActivity(intent);
                finish();
            }
        });

        if (auth.getCurrentUser() == null) {
            Intent intent = new Intent(ManageFriendRequests.this, LoginActivity.class);
            startActivity(intent);
            finish();
        }
    }

}
