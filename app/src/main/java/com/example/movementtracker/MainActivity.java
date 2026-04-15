package com.example.movementtracker;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.gms.location.*;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    TextView latitudeText, longitudeText, locationNameText;
    Button historyButton;

    FusedLocationProviderClient fusedLocationProviderClient;
    LocationRequest locationRequest;
    LocationCallback locationCallback;

    DatabaseHelper dbHelper;

    String lastLocationName = "";
    long startTime = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        latitudeText = findViewById(R.id.latitudeText);
        longitudeText = findViewById(R.id.longitudeText);
        locationNameText = findViewById(R.id.locationNameText);
        historyButton = findViewById(R.id.historyButton);

        dbHelper = new DatabaseHelper(this);

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);

        historyButton.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, HistoryActivity.class);
            startActivity(intent);
        });

        locationRequest = LocationRequest.create();
        locationRequest.setInterval(2000);
        locationRequest.setFastestInterval(1500);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {

                Location location = locationResult.getLastLocation();
                if (location == null) return;

                double latitude = location.getLatitude();
                double longitude = location.getLongitude();

                latitudeText.setText("Latitude: " + latitude);
                longitudeText.setText("Longitude: " + longitude);

                Geocoder geocoder = new Geocoder(MainActivity.this, Locale.getDefault());

                try {

                    List<Address> addresses = geocoder.getFromLocation(latitude, longitude, 1);

                    if (addresses != null && !addresses.isEmpty()) {

                        Address address = addresses.get(0);

                        String street = address.getThoroughfare();
                        String city = address.getLocality();
                        String state = address.getAdminArea();

                        StringBuilder locationBuilder = new StringBuilder();

                        if (street != null && !street.isEmpty()) {
                            locationBuilder.append(street);
                        }

                        if (city != null && !city.isEmpty()) {
                            if (locationBuilder.length() > 0) locationBuilder.append(", ");
                            locationBuilder.append(city);
                        }

                        if (state != null && !state.isEmpty()) {
                            if (locationBuilder.length() > 0) locationBuilder.append(", ");
                            locationBuilder.append(state);
                        }

                        String locationName = locationBuilder.toString();

                        if (locationName.isEmpty()) {
                            locationName = "Unknown Location";
                        }

                        locationNameText.setText(locationName);

                        // Save only when location changes
                        if (!locationName.equals(lastLocationName)) {

                            long endTime = System.currentTimeMillis();

                            if (!lastLocationName.equals("")) {
                                dbHelper.insertLocation(
                                        lastLocationName,
                                        formatTime(startTime),
                                        formatTime(endTime)
                                );
                            }

                            lastLocationName = locationName;
                            startTime = System.currentTimeMillis();
                        }
                    }

                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        };

        requestPermission();
    }

    private void requestPermission() {

        if (ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this,
                    new String[]{
                            Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.ACCESS_COARSE_LOCATION
                    }, 1);

        } else {

            fusedLocationProviderClient.requestLocationUpdates(
                    locationRequest,
                    locationCallback,
                    null
            );
        }
    }

    private String formatTime(long millis) {
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss", Locale.getDefault());
        return sdf.format(new Date(millis));
    }
}
