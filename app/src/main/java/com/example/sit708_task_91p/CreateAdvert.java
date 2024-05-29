package com.example.sit708_task_91p;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.widget.Autocomplete;
import com.google.android.libraries.places.widget.AutocompleteActivity;
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

public class CreateAdvert extends Activity {

    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1000; // Code for location permission request
    private static final int AUTOCOMPLETE_REQUEST_CODE = 1; // Code for autocomplete activity result

    private double latitude;
    private double longitude;
    private FusedLocationProviderClient fusedLocationClient;
    private RadioGroup adTypeRadioGroup;
    private RadioButton lostRadioButton, foundRadioButton;
    private EditText advertiserNameEditText, advertiserPhoneEditText, adDescriptionEditText, adDateEditText, adLocationEditText;
    private Button saveAdButton, getCurrentLocationButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_advert);
        initializeComponents();
        initializePlacesAPI();
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        configureLocationAutocomplete();
        configureGetCurrentLocationButton();
        configureSaveButton();
    }

    // UI Elements are being initialized
    private void initializeComponents() {
        adTypeRadioGroup = findViewById(R.id.adTypeRadioGroup);
        lostRadioButton = findViewById(R.id.lostRadioButton);
        foundRadioButton = findViewById(R.id.foundRadioButton);
        advertiserNameEditText = findViewById(R.id.advertiserNameEditText);
        advertiserPhoneEditText = findViewById(R.id.advertiserPhoneEditText);
        adDescriptionEditText = findViewById(R.id.adDescriptionEditText);
        adDateEditText = findViewById(R.id.adDateEditText);
        adLocationEditText = findViewById(R.id.adLocationEditText);
        saveAdButton = findViewById(R.id.saveAdButton);
        getCurrentLocationButton = findViewById(R.id.getCurrentLocationButton);
    }

    // Places API is being initialized
    private void initializePlacesAPI() {
        if (!Places.isInitialized()) {
            Places.initialize(getApplicationContext(), "AIzaSyBX_FCzDsxRsA-3umIvO3WO0HEwr_xWd0E");
        }
    }

    // Location Autocomplete Configuration
    private void configureLocationAutocomplete() {
        adLocationEditText.setFocusable(false);
        adLocationEditText.setOnClickListener(v -> startAutocompleteActivity());
    }

    // Location Input Autocomplete
    private void startAutocompleteActivity() {
        List<Place.Field> fields = Arrays.asList(Place.Field.ID, Place.Field.NAME, Place.Field.ADDRESS, Place.Field.LAT_LNG);
        Intent intent = new Autocomplete.IntentBuilder(AutocompleteActivityMode.FULLSCREEN, fields)
                .build(CreateAdvert.this);
        startActivityForResult(intent, AUTOCOMPLETE_REQUEST_CODE);
    }

    // Gets the current location on button click
    private void configureGetCurrentLocationButton() {
        getCurrentLocationButton.setOnClickListener(v -> checkLocationPermission());
    }

    // Prompt for permissions if app doesn't have it
    private void checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    LOCATION_PERMISSION_REQUEST_CODE);
        } else {
            retrieveCurrentLocation();
        }
    }

    // Save Ad Button Configuration
    private void configureSaveButton() {
        saveAdButton.setOnClickListener(v -> saveAdvert());
    }

    // Fetch current location from the location services
    private void retrieveCurrentLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(this, "Location permission is required", Toast.LENGTH_SHORT).show();
            return;
        }

        fusedLocationClient.getLastLocation().addOnSuccessListener(this, location -> {
            if (location != null) {
                try {
                    updateLocationFields(location.getLatitude(), location.getLongitude());
                } catch (IOException e) {
                    showToast("Failed to get address");
                    Log.e("CreateAdvert", "Geocoder failed", e);
                }
            } else {
                showToast("Location not detected");
            }
        });
    }

    // Update the location fields and fetch address using geocoder
    private void updateLocationFields(double lat, double lon) throws IOException {
        latitude = lat;
        longitude = lon;
        Geocoder geocoder = new Geocoder(this, Locale.getDefault());
        List<Address> addresses = geocoder.getFromLocation(lat, lon, 1);

        if (addresses != null && !addresses.isEmpty()) {
            Address address = addresses.get(0);
            String addressLine = address.getMaxAddressLineIndex() >= 0 ? address.getAddressLine(0) : "";
            adLocationEditText.setText(addressLine);
        } else {
            showToast("No address found");
        }
    }

    // Save ad details
    private void saveAdvert() {
        String adType = lostRadioButton.isChecked() ? "Lost" : "Found";
        String name = advertiserNameEditText.getText().toString();
        String phone = advertiserPhoneEditText.getText().toString();
        String description = adDescriptionEditText.getText().toString();
        String date = adDateEditText.getText().toString();
        String location = adLocationEditText.getText().toString();

        if (areFieldsValid(name, phone, description, date, location)) {
            LostFoundItem item = new LostFoundItem(adType + " " + name, description, phone, date, location, latitude, longitude);
            saveAdvertToPreferences(item);
            clearInputFields();
            showToast("Advertisement saved!");
        } else {
            showToast("All fields are required");
        }
    }

    // Checks if the input fields are valid
    private boolean areFieldsValid(String name, String phone, String description, String date, String location) {
        return !name.isEmpty() && !phone.isEmpty() && !description.isEmpty() && !date.isEmpty() && !location.isEmpty();
    }

    // Save ad item to SharedPreferences
    private void saveAdvertToPreferences(LostFoundItem item) {
        SharedPreferences sharedPreferences = getSharedPreferences("LostFoundPrefs", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        String itemsJson = sharedPreferences.getString("items", "[]");
        Type type = new TypeToken<ArrayList<LostFoundItem>>() {}.getType();
        ArrayList<LostFoundItem> itemList = new Gson().fromJson(itemsJson, type);
        itemList.add(item);

        itemsJson = new Gson().toJson(itemList);
        editor.putString("items", itemsJson);
        editor.apply();
    }

    // Clear input fields after saving an ad
    private void clearInputFields() {
        advertiserNameEditText.setText("");
        advertiserPhoneEditText.setText("");
        adDescriptionEditText.setText("");
        adDateEditText.setText("");
        adLocationEditText.setText("");
        adTypeRadioGroup.clearCheck();
    }

    // Show toast message
    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                retrieveCurrentLocation();
            } else {
                showToast("Permission denied");
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == AUTOCOMPLETE_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                Place place = Autocomplete.getPlaceFromIntent(data);
                adLocationEditText.setText(place.getAddress());
                if (place.getLatLng() != null) {
                    latitude = place.getLatLng().latitude;
                    longitude = place.getLatLng().longitude;
                }
            } else if (resultCode == AutocompleteActivity.RESULT_ERROR) {
                Status status = Autocomplete.getStatusFromIntent(data);
                showToast("Error: " + status.getStatusMessage());
            } else if (resultCode == RESULT_CANCELED) {
                showToast("Address selection canceled");
            }
        }
    }
}
