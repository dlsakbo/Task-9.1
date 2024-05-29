package com.example.sit708_task_91p;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class MainActivity extends Activity {

    private Button createAdButton;
    private Button showItemsButton;
    private Button showOnMapButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initializeUI();
        setupClickListeners();
    }

    // UI Components Initialization
    private void initializeUI() {

        createAdButton = findViewById(R.id.createAdButton);
        showItemsButton = findViewById(R.id.showItemsButton);
        showOnMapButton = findViewById(R.id.showOnMapButton);

    }

    private void setupClickListeners() {

        createAdButton.setOnClickListener(view -> openActivity(CreateAdvert.class));
        showItemsButton.setOnClickListener(view -> openActivity(LostFound.class));
        showOnMapButton.setOnClickListener(view -> openActivity(MapView.class));

    }

    private void openActivity(Class<?> activityClass) {

        Intent intent = new Intent(MainActivity.this, activityClass);
        startActivity(intent);

    }
}
