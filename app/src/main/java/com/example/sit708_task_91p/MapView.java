package com.example.sit708_task_91p;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;

public class MapView extends AppCompatActivity implements OnMapReadyCallback, GoogleMap.OnMarkerClickListener {
    private GoogleMap map;
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_map);
        initializeMapFragment();

    }

    // Map fragment initialization
    private void initializeMapFragment() {

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }

    }

    @Override
    public void onMapReady(GoogleMap googleMap) {

        this.map = googleMap;
        enableMapInteractionSettings();
        loadItemsAndPlaceMarkers();

    }

    // Enables the map interaction settings
    private void enableMapInteractionSettings() {

        map.getUiSettings().setZoomControlsEnabled(true);
        map.getUiSettings().setCompassEnabled(true);
        map.getUiSettings().setMyLocationButtonEnabled(true);
        map.getUiSettings().setMapToolbarEnabled(true);
        map.setOnMarkerClickListener(this);

    }

    // Load items from storage and place markers on the map
    // Place the markers on the map and load the items from the storage
    private void loadItemsAndPlaceMarkers() {

        SharedPreferences sharedPreferences = getSharedPreferences("LostFoundPrefs", MODE_PRIVATE);
        String itemsJson = sharedPreferences.getString("items", "[]");
        Type type = new TypeToken<ArrayList<LostFoundItem>>() {}.getType();
        ArrayList<LostFoundItem> itemList = new Gson().fromJson(itemsJson, type);

        if (!itemList.isEmpty()) {
            addMarkers(itemList);
            moveToLocation(itemList.get(0));
        }

    }

    // Move the cam to the location at the start of the list
    private void moveToLocation(LostFoundItem firstItem) {

        LatLng location = new LatLng(firstItem.getLatitude(), firstItem.getLongitude());
        float zoomLevel = 10.0f;
        map.moveCamera(CameraUpdateFactory.newLatLngZoom(location, zoomLevel));

    }

    // Add markers the the map for every items that is in the list
    private void addMarkers(ArrayList<LostFoundItem> itemList) {

        for (LostFoundItem item : itemList) {
            LatLng position = new LatLng(item.getLatitude(), item.getLongitude());
            Marker marker = map.addMarker(new MarkerOptions().position(position).title(item.getTitle()));
            marker.setTag(item);
        }

    }

    @Override
    public boolean onMarkerClick(Marker marker) {

        showItemDetails(marker);
        return true;

    }

    // View details of selected marker
    private void showItemDetails(Marker marker) {

        LostFoundItem item = (LostFoundItem) marker.getTag();
        if (item != null) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle(item.getTitle());
            builder.setMessage("Description: " + item.getDescription() +
                    "\nPhone: " + item.getPhone() +
                    "\nDate: " + item.getDate() +
                    "\nLocation: " + item.getLocation());
            builder.setPositiveButton("OK", null);
            builder.create().show();
        } else {
            Toast.makeText(this, "Details unavailable", Toast.LENGTH_SHORT).show();
        }

    }

    @Override
    public void onPointerCaptureChanged(boolean hasCapture) {

        super.onPointerCaptureChanged(hasCapture);

    }
}
