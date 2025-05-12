package com.example.databasetest;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.tabs.TabLayout;

public class PlanningDetailsActivity extends AppCompatActivity {

    ImageView imageView;
    static int count = 0;
    static String[] daysOfWeek = {
            "Tue", "Wed", "Thu","Fri", "Sat", "Sun"};


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_planningdetails);

        imageView = findViewById(R.id.back_arrow);


        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        final TabLayout tabLayout = findViewById(R.id.timeTagLayout);
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                if (tab.getText().equals("+")) {
                    if (count < 5) {
                        tab.setText(daysOfWeek[count]);
                        tabLayout.addTab(tabLayout.newTab().setText("+"));
                        count++;
                    } else {
                        count++;
                    }
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) { }

            @Override
            public void onTabReselected(TabLayout.Tab tab) { }
        });
    }
}