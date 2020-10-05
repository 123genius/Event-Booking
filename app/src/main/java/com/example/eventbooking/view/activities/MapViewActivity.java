package com.example.eventbooking.view.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import com.example.eventbooking.R;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.Objects;

public class MapViewActivity extends AppCompatActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private double latitude, longitude;
    private String name;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map_view);
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.mapView);
        Objects.requireNonNull(mapFragment).getMapAsync(this);

        latitude = Double.parseDouble(Objects.requireNonNull(getIntent().getStringExtra("lat")));
        longitude = Double.parseDouble(Objects.requireNonNull(getIntent().getStringExtra("longitude")));
        name = getIntent().getStringExtra("name");

    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        LatLng loc = new LatLng(latitude, longitude);

        mMap.addMarker(new MarkerOptions().position(loc).title(name));
        mMap.getMaxZoomLevel();
        mMap.moveCamera(CameraUpdateFactory.newLatLng(loc));
    }
}
