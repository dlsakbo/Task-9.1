package com.example.sit708_task_91p;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

public class Item_Data extends Activity {

    // UI components
    private TextView itemNameTextView;
    private TextView itemPhoneTextView;
    private TextView itemDescriptionTextView;
    private TextView itemDateTextView;
    private TextView itemLocationTextView;
    private Button removeItemButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_item_details);
        initializeUI();
        displayItemDetails();
        configureRemoveButton();

    }

    // UI components Initialization
    private void initializeUI() {

        itemNameTextView = findViewById(R.id.detailNameTextView);
        itemPhoneTextView = findViewById(R.id.detailPhoneTextView);
        itemDescriptionTextView = findViewById(R.id.detailDescriptionTextView);
        itemDateTextView = findViewById(R.id.detailDateTextView);
        itemLocationTextView = findViewById(R.id.detailLocationTextView);
        removeItemButton = findViewById(R.id.removeItemButton);

    }

    // Output the item details from the intent
    private void displayItemDetails() {

        Intent intent = getIntent();
        String title = intent.getStringExtra("TITLE");
        String phone = intent.getStringExtra("PHONE");
        String description = intent.getStringExtra("DESCRIPTION");
        String date = intent.getStringExtra("DATE");
        String location = intent.getStringExtra("LOCATION");

        itemNameTextView.setText(title);
        itemPhoneTextView.setText("Contact: " + phone);
        itemDescriptionTextView.setText("Description: " + description);
        itemDateTextView.setText("Date: " + date);
        itemLocationTextView.setText("Location: " + location);

    }

    // Remove button configuration
    private void configureRemoveButton() {

        int position = getIntent().getIntExtra("POSITION", -1);
        removeItemButton.setOnClickListener(v -> removeItem(position));

    }

    // Item Removal event handler
    private void removeItem(int position) {

        Intent returnIntent = new Intent();
        returnIntent.putExtra("ITEM_REMOVED", true);
        returnIntent.putExtra("POSITION", position);
        setResult(Activity.RESULT_OK, returnIntent);
        finish();

    }
}
