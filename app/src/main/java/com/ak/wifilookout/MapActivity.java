package com.ak.wifilookout;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.HashSet;

public class MapActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private HashSet<String> mLocations;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);

        Intent intent = getIntent();
        mLocations = (HashSet<String>)intent.getSerializableExtra("locations");

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        LatLngBounds.Builder builder = new LatLngBounds.Builder();
        LatLng lastLocation = null;
        for (String coordinates : mLocations) {
            double lat = Double.valueOf(coordinates.split("\\+")[0]);
            double lon = Double.valueOf(coordinates.split("\\+")[1]);
            LatLng location = new LatLng(lat, lon);
            lastLocation = location;
            mMap.addMarker(new MarkerOptions().position(location)).setDraggable(true);

            builder.include(location);
        }
       // mMap.addCircle(new CircleOptions().center(location).radius(100).strokeWidth(5));

        LatLngBounds bounds = builder.build();
        int width = getResources().getDisplayMetrics().widthPixels;
        int height = getResources().getDisplayMetrics().heightPixels;
        int padding = (int) (width * 0.10); // offset from edges of the map 10% of screen
        CameraUpdate cameraUpdate;
        if (mLocations.size() == 1)
            cameraUpdate = CameraUpdateFactory.newLatLngZoom(lastLocation, 12F);
        else
            cameraUpdate = CameraUpdateFactory.newLatLngBounds(bounds, width, height, padding);
        mMap.animateCamera(cameraUpdate);
    }
}
