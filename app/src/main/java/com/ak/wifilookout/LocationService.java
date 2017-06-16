package com.ak.wifilookout;

import android.Manifest;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Vibrator;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;

import java.util.HashSet;
import java.util.Set;

public class LocationService extends Service implements
        GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    private GoogleApiClient mGoogleApiClient;
    private Location mLastLocation;
    private String mSSID;
    private int mID;

    @Override
    public void onCreate() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        mSSID = intent.getStringExtra(WifiReceiver.EXTRA_SSID);

        mGoogleApiClient.connect();

        return START_NOT_STICKY;
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            stopSelf();
            return;
        }
        mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        if (mLastLocation != null) {
            verifyLocation();
        }
        stopSelf();
    }

    public void verifyLocation() {
        SharedPreferences sharedPref = this.getSharedPreferences(
                getString(R.string.saved_ssid_locations), Context.MODE_PRIVATE);
        Set<String> savedLocations = sharedPref.getStringSet(mSSID, new HashSet<String>());
        if (savedLocations.isEmpty() || !isWithinRange(savedLocations, 100)) {
            Vibrator vibrator = (Vibrator) this.getSystemService(Context.VIBRATOR_SERVICE);
            vibrator.vibrate(500);
            // Toast.makeText(this, "Unknown location!", Toast.LENGTH_SHORT).show();
            showNotification(mSSID, "Unknown location!");
        }
    }

    public void showNotification(String title, String content) {
        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(this)
                        .setSmallIcon(R.drawable.ic_perm_scan_wifi)
                        .setContentTitle(title)
                        .setContentText(content);

        String location = String.format("%f+%f", mLastLocation.getLatitude(), mLastLocation.getLongitude());

        Intent resultIntent = new Intent(this, ShowDialogActivity.class);
        resultIntent.putExtra("SSID", mSSID);
        resultIntent.putExtra("DIALOG", true);
        resultIntent.putExtra("LOCATION", location);

        TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
        stackBuilder.addParentStack(MainActivity.class);
        stackBuilder.addNextIntent(resultIntent);
        PendingIntent resultPendingIntent = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);

        mBuilder.setContentIntent(resultPendingIntent);
        mBuilder.setAutoCancel(true);
        mBuilder.setPriority(NotificationCompat.PRIORITY_HIGH);
        mBuilder.setDefaults(Notification.DEFAULT_ALL);

        NotificationManager mNotificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        mNotificationManager.notify(mID, mBuilder.build());
    }

    @Override
    public void onConnectionSuspended(int i) {
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        mGoogleApiClient.disconnect();
    }


    public boolean isWithinRange(Set<String> savedCoordinates, int range) {
        boolean result = false;
        for (String coordinates : savedCoordinates) {
            if (distance(coordinates) < range)
                result = true;
        }
        return result;
    }

    public float distance(String coordinates) {
        double lat = Double.valueOf(coordinates.split("\\+")[0]);
        double lon = Double.valueOf(coordinates.split("\\+")[1]);
        Location location = new Location("Saved");
        location.setLatitude(lat);
        location.setLongitude(lon);
        // distanceTo returns distance in meters
        return location.distanceTo(mLastLocation);
    }
}

