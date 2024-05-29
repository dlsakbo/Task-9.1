package com.example.sit708_task_91p;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class LostFound extends AppCompatActivity {

    private RecyclerView lostFoundRecyclerView;
    private LostFoundAdapter lostFoundAdapter;
    private List<LostFoundItem> lostFoundItems;
    private final ActivityResultLauncher<Intent> itemActivityLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    handleActivityResult(result.getData());
                }
            }
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lost_found);
        initializeUI();
        lostFoundItems = retrieveItems();
        configureRecyclerView();
    }

    // UI Components Initialization
    private void initializeUI() {
        lostFoundRecyclerView = findViewById(R.id.lostFoundRecyclerView);
        lostFoundRecyclerView.setLayoutManager(new LinearLayoutManager(this));
    }

    // Fetch Lost And Found Items from the shared preferences
    private List<LostFoundItem> retrieveItems() {
        SharedPreferences sharedPreferences = getSharedPreferences("LostFoundPrefs", MODE_PRIVATE);
        String itemsJson = sharedPreferences.getString("items", "[]");
        Type itemType = new TypeToken<ArrayList<LostFoundItem>>() {}.getType();
        return new Gson().fromJson(itemsJson, itemType);
    }

    // Setup adapter and RecyclerView configuration
    private void configureRecyclerView() {
        lostFoundAdapter = new LostFoundAdapter(this, lostFoundItems, this::openItemDetails);
        lostFoundRecyclerView.setAdapter(lostFoundAdapter);
    }

    // Open Selected Item Details
    private void openItemDetails(int position) {
        Intent intent = new Intent(LostFound.this, Item_Data.class);
        LostFoundItem item = lostFoundItems.get(position);
        intent.putExtra("TITLE", item.getTitle());
        intent.putExtra("DESCRIPTION", item.getDescription());
        intent.putExtra("PHONE", item.getPhone());
        intent.putExtra("DATE", item.getDate());
        intent.putExtra("LOCATION", item.getLocation());
        intent.putExtra("POSITION", position);
        itemActivityLauncher.launch(intent);
    }

    private void handleActivityResult(Intent data) {
        boolean itemRemoved = data.getBooleanExtra("ITEM_REMOVED", false);
        int position = data.getIntExtra("POSITION", -1);
        if (itemRemoved && position != -1) {
            lostFoundAdapter.removeItem(position);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        lostFoundItems.clear();
        lostFoundItems.addAll(retrieveItems());
        lostFoundAdapter.notifyDataSetChanged();
    }
}
