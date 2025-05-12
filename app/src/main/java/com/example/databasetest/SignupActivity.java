package com.example.databasetest;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class SignupActivity extends AppCompatActivity {

    EditText signupName, signupEmail, signupPassword, signupPhone;
    TextView loginRedirectText;
    Button signupButton, btn_back;
    FirebaseDatabase database;
    DatabaseReference reference;
    FirebaseAuth auth;

    String defaultImageUrl = "https://firebasestorage.googleapis.com/v0/b/testing-840d8.appspot.com/o/Def_userIma.png?alt=media&token=d6695f20-126c-4692-808c-28ddb8f5d893";



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);
        auth = FirebaseAuth.getInstance();
        signupName = findViewById(R.id.signup_name);
        signupEmail = findViewById(R.id.signup_email);
        signupPassword = findViewById(R.id.signup_password);
        signupPhone = findViewById(R.id.signup_phone);
        signupButton = findViewById(R.id.signup_button);
        loginRedirectText = findViewById(R.id.loginRedirectText);

        signupButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                database = FirebaseDatabase.getInstance();
                reference = database.getReference("user");

                String name = signupName.getText().toString();
                String email = signupEmail.getText().toString();
                String password = signupPassword.getText().toString();
                String phone = signupPhone.getText().toString();

                if (TextUtils.isEmpty(name) || TextUtils.isEmpty(email) || TextUtils.isEmpty(password) || TextUtils.isEmpty(phone)) {
                    Toast.makeText(SignupActivity.this, "All information must be entered!", Toast.LENGTH_SHORT).show();
                    return;
                }
                auth.createUserWithEmailAndPassword(email, password)
                        .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                if (task.isSuccessful()) {
                                    String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();

                                    Users users = new Users(uid, name, email, password, phone, defaultImageUrl); // Include default image URL in the constructor
                                    reference.child(uid).setValue(users);

                                    Toast.makeText(SignupActivity.this, "You have signed up successfully!", Toast.LENGTH_SHORT).show();
                                    Intent intent = new Intent(SignupActivity.this, LoginActivity.class);
                                    startActivity(intent);

                                } else {

                                    Toast.makeText(SignupActivity.this, "Authentication failed.", Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
            }
        });


        loginRedirectText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(SignupActivity.this, LoginActivity.class);
                startActivity(intent);
            }
        });
    }
}