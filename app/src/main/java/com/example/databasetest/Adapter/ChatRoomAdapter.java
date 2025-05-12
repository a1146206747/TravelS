package com.example.databasetest.Adapter;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.databasetest.Users;
import com.example.databasetest.ChatRoomActivity;
import com.example.databasetest.R;
import com.example.databasetest.ChatRoom;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class ChatRoomAdapter extends RecyclerView.Adapter<ChatRoomAdapter.ChatRoomViewHolder> {
    Context context;
    ArrayList<ChatRoom> chatRoomArrayList;
    FirebaseDatabase db;
    String currentUserId;

    public ChatRoomAdapter(Context context, ArrayList<ChatRoom> chatRoomArrayList) {
        this.context = context;
        this.chatRoomArrayList = chatRoomArrayList;
        // Initialize Firestore
        this.db = FirebaseDatabase.getInstance();
        // Initialize current user ID
        this.currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
    }

    @NonNull
    @Override
    public ChatRoomAdapter.ChatRoomViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.user_item, parent, false);
        return new ChatRoomViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ChatRoomAdapter.ChatRoomViewHolder holder, int position) {
        ChatRoom chatRoom = chatRoomArrayList.get(position);
        if ("true".equals(chatRoom.getIsGroupChat())) {
            // Group chat room
            holder.chatRoomName.setText(chatRoom.getName());
            holder.chatRoomImg.setImageResource(R.drawable.group_icon);
            holder.itemView.setOnClickListener(v -> startChatRoomActivity(chatRoom.getName(), chatRoom.getId()));
        } else {
            // Default to private chat room if isGroupChat is null or false
            String friendId = null;

            // Debug code to print contents of the members HashMap
            Log.d("ChatRoomAdapter", "Members of chat room: " + chatRoom.getMembers().toString());

            for(Map.Entry<String, String> entry : chatRoom.getMembers().entrySet()) {
                if(!entry.getKey().equals(currentUserId)) {
                    friendId = entry.getValue();
                    break;
                }
            }

            // Debug code to print the friend ID
            Log.d("ChatRoomAdapter", "Friend ID: " + friendId);

            if(friendId != null) {
                final String finalFriendId = friendId; // Add this line to create a final variable
                db.getReference("user").child(finalFriendId).get().addOnSuccessListener(datasnapshot -> {
                    Users friend = datasnapshot.getValue(Users.class);
                    if(friend != null) {
                        holder.chatRoomName.setText(friend.getName());
                        Picasso.get().load(friend.getUserImg()).into(holder.chatRoomImg);

                        holder.itemView.setOnClickListener(v -> startChatRoomActivity(friend.getName(), chatRoom.getId(), finalFriendId));
                    } else {
                        Log.w("ChatRoomAdapter", "User with ID: " + finalFriendId + " not found in database.");
                    }
                });
            }
        }
    }

    private void startChatRoomActivity(String chatRoomName, String chatRoomId, String... friendId) {
        Intent intent = new Intent(context, ChatRoomActivity.class);
        intent.putExtra("chatRoomName", chatRoomName);
        intent.putExtra("chatRoomId", chatRoomId);
        if(friendId.length > 0) {
            intent.putExtra("uid", friendId[0]);
        }

        // Add logging here
        Log.d("ChatRoomAdapter", "Starting ChatRoomActivity with chatRoomName: "
                + chatRoomName + " and chatRoomId: " + chatRoomId);

        context.startActivity(intent);
    }

    @Override
    public int getItemCount() {
        return chatRoomArrayList.size();
    }

    public class ChatRoomViewHolder extends RecyclerView.ViewHolder {
        CircleImageView chatRoomImg;
        TextView chatRoomName;

        public ChatRoomViewHolder(@NonNull View itemView) {
            super(itemView);
            chatRoomImg = itemView.findViewById(R.id.userimg);
            chatRoomName = itemView.findViewById(R.id.username);
        }
    }
}