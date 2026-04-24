package com.example.movementtracker;

import android.Manifest;
import android.app.*;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Build;
import android.os.IBinder;

import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;

import com.google.android.gms.location.*;

public class LocationService extends Service {

    FusedLocationProviderClient fusedLocationProviderClient;
    LocationCallback locationCallback;

    @Override
    public void onCreate() {
        super.onCreate();

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);

        startForeground(1, createNotification());

        LocationRequest request = LocationRequest.create();
        request.setInterval(3000);
        request.setFastestInterval(2000);
        request.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {

                if (locationResult == null) return;

                Location location = locationResult.getLastLocation();
                if (location == null) return;

                double lat = location.getLatitude();
                double lon = location.getLongitude();

                // 👉 You can reuse DB logic here if needed
                // Example: Log or save
                System.out.println("Background Location: " + lat + ", " + lon);
            }
        };

        if (ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {

            fusedLocationProviderClient.requestLocationUpdates(
                    request,
                    locationCallback,
                    null
            );
        }
    }

    private Notification createNotification() {

        String channelId = "location_channel";

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    channelId,
                    "Location Tracking",
                    NotificationManager.IMPORTANCE_LOW
            );
            getSystemService(NotificationManager.class).createNotificationChannel(channel);
        }

        return new NotificationCompat.Builder(this, channelId)
                .setContentTitle("Movement Tracker")
                .setContentText("Tracking location in background...")
                .setSmallIcon(R.mipmap.ic_launcher)
                .build();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
