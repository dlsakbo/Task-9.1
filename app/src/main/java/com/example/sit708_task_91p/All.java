// --------------------------------------------MainActivity.java--------------------------------------------

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


// --------------------------------------------CreateAdvert.java--------------------------------------------

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


// --------------------------------------------Item_Data.java--------------------------------------------

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


// --------------------------------------------Location.java--------------------------------------------

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


// --------------------------------------------LostFound.java--------------------------------------------

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


// --------------------------------------------LostFoundAdapter.java--------------------------------------------

package com.example.sit708_task_91p;

import android.content.Context;
import android.content.SharedPreferences;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.gson.Gson;

import java.util.List;

public class LostFoundAdapter extends RecyclerView.Adapter<LostFoundAdapter.ViewHolder> {

    private final List<LostFoundItem> lostFoundItems;
    private final LayoutInflater inflater;
    private final OnItemClickListener clickListener;

    public interface OnItemClickListener {

        void onItemClick(int position);

    }

    // Constructor that is used to initialize the adapter with context, item list, and click listener

    public LostFoundAdapter(Context context, List<LostFoundItem> items, OnItemClickListener listener) {

        this.lostFoundItems = items;
        this.inflater = LayoutInflater.from(context);
        this.clickListener = listener;

    }

    // Creates the new item view and then returns its ViewHolder
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view = inflater.inflate(R.layout.item_lost_found, parent, false);
        return new ViewHolder(view);

    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

        LostFoundItem item = lostFoundItems.get(position);
        holder.titleTextView.setText(item.getTitle());

    }

    // Returns total number of items in list
    @Override
    public int getItemCount() {

        return lostFoundItems.size();

    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        public TextView titleTextView;
        public ViewHolder(View itemView) {
            super(itemView);
            titleTextView = itemView.findViewById(R.id.itemTitleTextView);
            itemView.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION && clickListener != null) {
                    clickListener.onItemClick(position);
                }
            });
        }

    }

    // Remove item from list and adpter is notified
    public void removeItem(int position) {

        lostFoundItems.remove(position);
        notifyItemRemoved(position);
        saveItems(inflater.getContext());

    }

    // Save current items list to the SharedPreferences
    private void saveItems(Context context) {

        SharedPreferences sharedPreferences = context.getSharedPreferences("LostFoundPrefs", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        Gson gson = new Gson();
        String json = gson.toJson(lostFoundItems);
        editor.putString("lostFoundItems", json);
        editor.apply();

    }
}


// --------------------------------------------LostFoundItem.java--------------------------------------------

package com.example.sit708_task_91p;
public class LostFoundItem {

    private String title;
    private String description;
    private String phone;
    private String date;
    private String location;
    private double latitude;
    private double longitude;

    public LostFoundItem(String title, String description, String phone, String date, String location, double latitude, double longitude) {
        this.title = title;
        this.description = description;
        this.phone = phone;
        this.date = date;
        this.location = location;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    // Getters and Setters
    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }
}

// --------------------------------------------MapView.java--------------------------------------------

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
