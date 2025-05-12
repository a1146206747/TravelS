package com.example.databasetest;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.icu.text.SimpleDateFormat;
import android.media.Image;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.tabs.TabLayout;

import java.util.Date;

public class DetailsActivity extends AppCompatActivity {



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_details);

        Button addPlanBtn = findViewById(R.id.addPlanBtn);
        addPlanBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("DetailsActivity", "Button clicked");
                Toast.makeText(DetailsActivity.this, "Button clicked", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(DetailsActivity.this, PlanningDetailsActivity.class);
                startActivity(intent);
            }
        });
    }
}