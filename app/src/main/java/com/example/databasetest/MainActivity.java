package com.example.databasetest;

import android.content.Intent;
import android.os.Bundle;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseError;

public class MainActivity extends AppCompatActivity {

    // Create Firebase authorization object
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize Firebase authorization object
        mAuth = FirebaseAuth.getInstance();

        final TextView travelSenceTextView = findViewById(R.id.travelSenceTextView);

        Animation shineAnimation = AnimationUtils.loadAnimation(this, R.anim.rotate_animation);

        shineAnimation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            // Check if the user is logged in at the end of the animation
            @Override
            public void onAnimationEnd(Animation animation) {

                FirebaseUser currentUser = mAuth.getCurrentUser();

                if (currentUser != null) {
                    // If the user is logged in, jump to the homePage
                    Intent homePageIntent = new Intent(MainActivity.this, homePage.class);
                    startActivity(homePageIntent);
                }
                else {
                    // If the user is not logged in, just jump to the homePage without loading the friend list
                    Intent homePageIntent = new Intent(MainActivity.this, homePage.class);
                    startActivity(homePageIntent);
                }
                finish();
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
            }
        });
        travelSenceTextView.startAnimation(shineAnimation);
    }
}