package com.example.sit708_task_91p;

import android.os.Bundle;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

public class Location extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setupEdgeToEdgeDisplay();
        loadLayout();
        
    }

    private void setupEdgeToEdgeDisplay() {

        EdgeToEdge.enable(this);

    }

    private void loadLayout() {

        setContentView(R.layout.activity_location);

    }
}
