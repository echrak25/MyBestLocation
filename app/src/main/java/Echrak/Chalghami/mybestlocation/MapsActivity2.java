package Echrak.Chalghami.mybestlocation;

import androidx.fragment.app.FragmentActivity;

import android.os.Bundle;
import android.util.Log;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import Echrak.Chalghami.mybestlocation.databinding.ActivityMaps2Binding;

public class MapsActivity2 extends FragmentActivity implements OnMapReadyCallback {
    private GoogleMap mMap;
    private double latitude;
    private double longitude;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        // Get latitude and longitude from the intent
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            String lonString = extras.getString("longitude");
            String latString = extras.getString("latitude");

            // Ensure to parse correctly
            if (lonString != null && latString != null) {
                try {
                    longitude = Double.parseDouble(lonString);
                    latitude = Double.parseDouble(latString);
                } catch (NumberFormatException e) {
                    Log.e("MapsActivity", "Invalid latitude or longitude", e);
                }
            }
        }

        // Initialize the map
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        // Check if the coordinates are valid
        if (latitude != 0 && longitude != 0) {
            // Set a marker at the received coordinates
            LatLng position = new LatLng(latitude, longitude);
            mMap.addMarker(new MarkerOptions().position(position).title("Your Location"));
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(position, 15));
        } else {
            Log.e("MapsActivity", "Latitude and Longitude not set correctly.");
        }
    }
}