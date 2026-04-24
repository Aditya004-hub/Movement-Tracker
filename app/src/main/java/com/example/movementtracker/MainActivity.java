package com.example.movementtracker;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Build;
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
    Button historyButton, stopButton;

    FusedLocationProviderClient fusedLocationProviderClient;
    LocationRequest locationRequest;
    LocationCallback locationCallback;

    DatabaseHelper dbHelper;

    String lastLocationName = "";
    double lastLat = 0.0;
    double lastLon = 0.0;
    long startTime = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        latitudeText = findViewById(R.id.latitudeText);
        longitudeText = findViewById(R.id.longitudeText);
        locationNameText = findViewById(R.id.locationNameText);
        historyButton = findViewById(R.id.historyButton);
        stopButton = findViewById(R.id.stopButton);

        dbHelper = new DatabaseHelper(this);

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);

        historyButton.setOnClickListener(v ->
                startActivity(new Intent(MainActivity.this, HistoryActivity.class))
        );

        stopButton.setOnClickListener(v ->
                stopService(new Intent(MainActivity.this, LocationService.class))
        );

        locationRequest = LocationRequest.create();
        locationRequest.setInterval(2000);
        locationRequest.setFastestInterval(1500);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {

                if (locationResult == null) return;

                Location location = locationResult.getLastLocation();
                if (location == null) return;

                double latitude = location.getLatitude();
                double longitude = location.getLongitude();

                latitudeText.setText("Latitude: " + latitude);
                longitudeText.setText("Longitude: " + longitude);

                getAddressAndStore(latitude, longitude);
            }
        };

        requestPermission();
    }

    // ✅ Step 1: Ask only FINE LOCATION
    private void requestPermission() {

        if (ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    1);

        } else {
            startLocationUpdates();
            startBackgroundService();
        }
    }

    // ✅ Step 2: Handle permission result properly
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == 1) {

            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                startLocationUpdates();
                startBackgroundService();

                // ✅ Step 3: Ask background permission separately
                requestBackgroundPermission();
            }
        }
    }

    // ✅ Start Foreground Service
    private void startBackgroundService() {

        Intent serviceIntent = new Intent(this, LocationService.class);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(serviceIntent);
        } else {
            startService(serviceIntent);
        }
    }

    // ✅ Step 3: Ask BACKGROUND LOCATION separately
    private void requestBackgroundPermission() {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {

            if (ActivityCompat.checkSelfPermission(this,
                    Manifest.permission.ACCESS_BACKGROUND_LOCATION)
                    != PackageManager.PERMISSION_GRANTED) {

                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_BACKGROUND_LOCATION},
                        2);
            }
        }
    }

    private void startLocationUpdates() {

        if (ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        fusedLocationProviderClient.requestLocationUpdates(
                locationRequest,
                locationCallback,
                null
        );
    }

    private void getAddressAndStore(double lat, double lon) {

        Geocoder geocoder = new Geocoder(this, Locale.getDefault());

        try {

            if (!Geocoder.isPresent()) return;

            List<Address> addresses = geocoder.getFromLocation(lat, lon, 1);

            if (addresses != null && !addresses.isEmpty()) {

                Address address = addresses.get(0);

                String street = address.getThoroughfare();
                String city = address.getLocality();
                String state = address.getAdminArea();

                StringBuilder builder = new StringBuilder();

                if (street != null && !street.isEmpty()) builder.append(street);

                if (city != null && !city.isEmpty()) {
                    if (builder.length() > 0) builder.append(", ");
                    builder.append(city);
                }

                if (state != null && !state.isEmpty()) {
                    if (builder.length() > 0) builder.append(", ");
                    builder.append(state);
                }

                String locationName = builder.toString();

                if (locationName.isEmpty()) locationName = "Unknown Location";

                locationNameText.setText(locationName);

                if (!locationName.equals(lastLocationName)) {

                    long endTime = System.currentTimeMillis();

                    if (!lastLocationName.equals("")) {

                        dbHelper.insertLocation(
                                lastLocationName,
                                String.valueOf(lastLat),
                                String.valueOf(lastLon),
                                getCurrentDate(),
                                formatTime(startTime),
                                formatTime(endTime)
                        );
                    }

                    lastLocationName = locationName;
                    lastLat = lat;
                    lastLon = lon;
                    startTime = System.currentTimeMillis();
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private String formatTime(long millis) {
        return new SimpleDateFormat("HH:mm:ss", Locale.getDefault())
                .format(new Date(millis));
    }

    private String getCurrentDate() {
        return new SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
                .format(new Date());
    }
}
