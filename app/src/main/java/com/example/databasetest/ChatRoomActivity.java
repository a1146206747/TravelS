package com.example.databasetest;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.example.databasetest.Adapter.msgAdapter;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.ArrayList;
import java.util.Date;

public class ChatRoomActivity extends AppCompatActivity {

    String reciverUid, reciverName, senderUid, reciverimg, reciverPhone,reciverEmail;
    ImageButton btn_back,btn_reciverFile;
    CardView btn_sendMsg;
    EditText text_msg;
    FirebaseAuth mAuth;
    FirebaseDatabase db;
    String senderRoom, reciverRoom;
    RecyclerView messagesAdapter;
    ArrayList<msgModel> msgModelArrayList;
    msgAdapter msgAdapter;
    TextView TV_recivername;
    public static String senderImg;
    public static String reciverIImg;
    private static final int PICK_IMAGE_REQUEST = 1;
    private Uri imageUri;
    private StorageReference mStorageRef;
    String chatRoomId;
    boolean isGroupChat;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_room);

        mAuth = FirebaseAuth.getInstance();
        messagesAdapter = findViewById(R.id.msgAdapter);

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setStackFromEnd(true);
        msgModelArrayList = new ArrayList<>();
        messagesAdapter.setLayoutManager(linearLayoutManager);

        reciverName = getIntent().getStringExtra("nameeee");
        TV_recivername = findViewById(R.id.recivername);
        TV_recivername.setText(reciverName);

        reciverEmail= getIntent().getStringExtra("email");
        reciverUid = getIntent().getStringExtra("uid");
        Log.d("ChatRoomActivity", "Receiver UID: " + reciverUid);
        reciverimg = getIntent().getStringExtra("reciverImg");
        reciverPhone = getIntent().getStringExtra("phone");

        btn_sendMsg = findViewById(R.id.btn_sendMsg);
        btn_back = findViewById(R.id.btn_back);
        btn_reciverFile =findViewById(R.id.btn_reciverFile);
        text_msg = findViewById(R.id.text_msg);

        senderUid = FirebaseAuth.getInstance().getUid();
        senderRoom =  senderUid + "/" + reciverUid;
        reciverRoom = reciverUid + "/" + senderUid;
//        msgAdapter = new msgAdapter(ChatRoomActivity.this, msgModelArrayList,senderUid,isGroupChat);
        messagesAdapter.setAdapter(msgAdapter);

        db = FirebaseDatabase.getInstance();
        chatRoomId = getIntent().getStringExtra("chatRoomId");
        Log.d("ChatRoomActivity", "chatRoomId: " + chatRoomId);
        DatabaseReference chatRoomRef = db.getReference().child("chatRooms").child(chatRoomId);
        chatRoomRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                ChatRoom chatRoom = dataSnapshot.getValue(ChatRoom.class);
                isGroupChat = chatRoom != null && "true".equals(chatRoom.getIsGroupChat());
                msgAdapter = new msgAdapter(ChatRoomActivity.this, msgModelArrayList, senderUid, isGroupChat);
                messagesAdapter.setAdapter(msgAdapter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Handle error
            }
        });

        DatabaseReference chatReference = db.getReference().child("chats").child(chatRoomId).child("messages");

        mStorageRef = FirebaseStorage.getInstance().getReference("uploads");

        chatReference.addValueEventListener(new ValueEventListener() {
            @SuppressLint("NotifyDataSetChanged")
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                msgModelArrayList.clear();
                for (DataSnapshot dbSnapshot : snapshot.getChildren()) {
                    msgModel message = dbSnapshot.getValue(msgModel.class);
                    msgModelArrayList.add(message);
                }
                if (msgAdapter != null) {
                    msgAdapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });

        btn_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ChatRoomActivity.this, ChatRoomHomePageActivity.class);
                startActivity(intent);
                finish();
            }
        });

        btn_sendMsg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendMessage();
            }
        });

        text_msg.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (event.getAction() == KeyEvent.ACTION_DOWN && keyCode == KeyEvent.KEYCODE_ENTER) {
                    sendMessage();
                    return true;
                }
                return false;
            }
        });

        btn_reciverFile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ChatRoomActivity.this, ReciverProfileActivity.class);
                intent.putExtra("reciverName", reciverName);
                intent.putExtra("phone", reciverPhone);
                intent.putExtra("email", reciverEmail);
                startActivity(intent);
            }
        });

        // image upload button click event
        CardView uploadImageButton = findViewById(R.id.uploadImageButton);
        uploadImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openFileChooser();
            }
        });
    }

    void openFileChooser() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            Uri selectedImageUri = data.getData();
            sendImageMessage(selectedImageUri);
        }
    }
    void sendImageMessage(Uri imageUri) {
        final Date date = new Date();
        final StorageReference fileReference = mStorageRef.child(System.currentTimeMillis() + ".jpg"); // or other image extension
        fileReference.putFile(imageUri)
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        fileReference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                            @Override
                            public void onSuccess(Uri uri) {
                                String imageUrl = uri.toString();
                                // Here, don't include the 'msg' when creating the msgModel for an image message
                                msgModel imageMessage = new msgModel("", senderUid, imageUrl, date.getTime());
                                // send the image message
                                sendMessage(imageMessage);
                            }
                        });
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(ChatRoomActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }
    void sendMessage(msgModel message) {
        db.getReference().child("chats").child(chatRoomId).child("messages").push()
                .setValue(message).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {

                    }
                });
    }

    void sendMessage() {
        final String msg = text_msg.getText().toString();
        if (msg.isEmpty() && imageUri == null) {
            Toast.makeText(ChatRoomActivity.this, "Enter The Message First", Toast.LENGTH_SHORT).show();
        }
        else {
            text_msg.setText("");
            final Date date = new Date();
            if (imageUri != null) {
                // Implement your method to upload image to Firebase and send message
                final StorageReference fileReference = mStorageRef.child(System.currentTimeMillis() + ".jpg"); // or other image extension
                fileReference.putFile(imageUri)
                        .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                            @Override
                            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                fileReference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                    @Override
                                    public void onSuccess(Uri uri) {
                                        String imageUrl = uri.toString();
                                        // Here, don't include the 'msg' when creating the msgModel for an image message
                                        msgModel imageMessage = new msgModel("", senderUid, imageUrl, date.getTime());
                                        // send the image message
                                        sendMessage(imageMessage);
                                        imageUri = null; // reset the imageUri after sending
                                    }
                                });
                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Toast.makeText(ChatRoomActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        });
            }
            else {
                // No image to send, just a regular message
                msgModel messages = new msgModel(msg, senderUid, date.getTime());
                // send the text message
                sendMessage(messages);
            }
        }
    }
    }