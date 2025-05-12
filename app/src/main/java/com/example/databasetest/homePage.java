package com.example.databasetest;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import androidx.appcompat.app.AppCompatActivity;
import android.widget.Toast;
import android.app.Dialog;
import android.content.Intent;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;

public class homePage extends AppCompatActivity {
    private static final String TAG = "HomePage";
    private static final int ERROR_DIALOG_REQUEST = 9001;
    ImageButton btnLogout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_page);

        btnLogout = findViewById(R.id.btn_logout);

        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            btnLogout.setVisibility(View.VISIBLE);
        } else {
            btnLogout.setVisibility(View.GONE);
        }

        btnLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FirebaseAuth.getInstance().signOut();
                Intent intent = getIntent();
                finish();
                startActivity(intent);
            }
        });
    }

    public void onLeadingButtonClick(View view) {
        Intent intent = new Intent(this, LoginActivity.class);
        startActivity(intent);
        finish();
    }
    public void chatRoomButtonClick(View view) {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            Intent intent = new Intent(this, ChatRoomHomePageActivity.class);
            startActivity(intent);
            finish();
        } else {
            Intent intent = new Intent(this, LoginActivity.class);
            startActivity(intent);
            Toast.makeText(homePage.this, "Please Login first", Toast.LENGTH_SHORT).show();
        }
    }

    public void mapButtonClick(View view) {
        if (isServicesOK()) {
            Intent intent = new Intent(this, mapPageActivity.class);
            startActivity(intent);
            finish();
        }
    }

    public void PlannerButtonClick(View view) {
        if (isServicesOK()) {
            FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
            if (currentUser!=null) {
                Intent intent = new Intent(this, TripPlannerActivity.class);
                startActivity(intent);
                finish();
            } else {
                Intent intent = new Intent(this, LoginActivity.class);
                startActivity(intent);
                Toast.makeText(homePage.this, "Please Login first", Toast.LENGTH_SHORT).show();
            }
        }
    }

    public boolean isServicesOK(){
        Log.d(TAG, "isServicesOK: checking google services version");

        int available = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(homePage.this);

        if(available == ConnectionResult.SUCCESS){
            //everything is fine and the user can make map requests
            Log.d(TAG, "isServicesOK: Google Play Services is working");
            return true;
        }
        else if(GoogleApiAvailability.getInstance().isUserResolvableError(available)){
            //an error occured but we can resolve it
            Log.d(TAG, "isServicesOK: an error occured but we can fix it");
            Dialog dialog = GoogleApiAvailability.getInstance().getErrorDialog(homePage.this, available, ERROR_DIALOG_REQUEST);
            dialog.show();
        }else{
            Toast.makeText(this, "You can't make map requests", Toast.LENGTH_SHORT).show();
        }
        return false;
    }

}
