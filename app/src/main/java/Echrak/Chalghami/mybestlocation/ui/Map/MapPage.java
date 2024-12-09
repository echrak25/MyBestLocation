package Echrak.Chalghami.mybestlocation.ui.Map;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.android.volley.Request;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import Echrak.Chalghami.mybestlocation.Config;
import Echrak.Chalghami.mybestlocation.MapsActivity;
import Echrak.Chalghami.mybestlocation.R;

public class MapPage extends Fragment {

    private EditText phoneEditText, pseudoEditText, longitudeEditText, latitudeEditText;
    private FusedLocationProviderClient mClient;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;
    private Location lastLocation = null;
    private static final double UPDATE_THRESHOLD = 10.0; // 10 meters
    private static final long UPDATE_INTERVAL = 1 * 60 * 1000; // 1 minute update interval
    private Handler handler = new Handler();
    private boolean isUpdatingLocation = false; // Flag to track location update status

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.activity_map_page, container, false);

        // Initialize the UI components
        phoneEditText = rootView.findViewById(R.id.phoneEditText);
        pseudoEditText = rootView.findViewById(R.id.pseudoEditText);
        longitudeEditText = rootView.findViewById(R.id.longitudeEditText);
        latitudeEditText = rootView.findViewById(R.id.latitudeEditText);
        Button backButton = rootView.findViewById(R.id.backButton);
        Button mapButton = rootView.findViewById(R.id.mapButton);
        Button addButton = rootView.findViewById(R.id.addButton);
        Button getPositionButton = rootView.findViewById(R.id.getPositionButton);

        // Initialize FusedLocationProviderClient
        mClient = LocationServices.getFusedLocationProviderClient(getActivity());

        // Fetch current location
        getPositionButton.setOnClickListener(v -> getCurrentLocation());

        // Handle back button navigation
        backButton.setOnClickListener(v -> {
            NavController navController = Navigation.findNavController(v);
            navController.navigate(R.id.nav_home); // Navigate to HomeFragment
        });

        // Open map activity
        mapButton.setOnClickListener(v -> openMapActivity());

        // Add position to the server
        addButton.setOnClickListener(v -> addPosition());

        // Start location updates every minute or after 10 meters
        startLocationUpdates();

        return rootView;
    }

    private void startLocationUpdates() {
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (!isUpdatingLocation) {  // Check if update is already in progress
                    getCurrentLocation();
                }
                handler.postDelayed(this, UPDATE_INTERVAL); // Repeat every minute
            }
        }, UPDATE_INTERVAL);
    }

    private void getCurrentLocation() {
        if (ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION_REQUEST_CODE);
        } else {
            fetchLocation();
        }
    }

    private void fetchLocation() {
        if (ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            isUpdatingLocation = true;  // Mark location update as in progress
            LocationRequest locationRequest = LocationRequest.create();
            locationRequest.setInterval(5000); // 5 seconds
            locationRequest.setFastestInterval(2000); // 2 seconds
            locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

            mClient.requestLocationUpdates(locationRequest, new LocationCallback() {
                @Override
                public void onLocationResult(LocationResult locationResult) {
                    if (locationResult != null && locationResult.getLocations().size() > 0) {
                        Location location = locationResult.getLastLocation();
                        if (location != null) {
                            double latitude = location.getLatitude();
                            double longitude = location.getLongitude();
                            latitudeEditText.setText(String.valueOf(latitude));
                            longitudeEditText.setText(String.valueOf(longitude));

                            // Check if location has changed significantly (10 meters) or time has passed (1 minute)
                            if (lastLocation == null || location.distanceTo(lastLocation) >= UPDATE_THRESHOLD) {
                                lastLocation = location;
                                updatePosition(String.valueOf(latitude), String.valueOf(longitude));  // Update the position on the server
                            }
                        }
                        isUpdatingLocation = false; // Mark location update as complete
                    }
                }
            }, Looper.getMainLooper());
        } else {
            Toast.makeText(getActivity(), "Location permission not granted", Toast.LENGTH_SHORT).show();
            isUpdatingLocation = false; // Ensure the flag is reset in case of failure
        }
    }

    private void updatePosition(String newLatitude, String newLongitude) {
        // First, make a GET request to retrieve the current position
        String requestUrl = Config.URL_GET_POS + "2";  // Assuming position ID is 2, adjust as needed

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, requestUrl, null,
                response -> {
                    try {
                        // Parse the response to extract the 'numero' and 'pseudo'
                        if (response.getInt("success") == 1) {
                            // Assuming the positions array has only one object
                            JSONObject position = response.getJSONArray("positions").getJSONObject(0);
                            String numero = position.getString("numero");
                            String pseudo = position.getString("pseudo");

                            // Now create the updated object with the new latitude and longitude
                            Map<String, String> params = new HashMap<>();
                            params.put("numero", numero);
                            params.put("pseudo", pseudo);
                            params.put("longitude", newLongitude);
                            params.put("latitude", newLatitude);
                            JSONObject updatedPosition = new JSONObject(params);

                            // Make the PUT request to update the position
                            String updateUrl = Config.URL_EDIT + "/2"; // URL for updating the position
                            JsonObjectRequest updateRequest = new JsonObjectRequest(Request.Method.PUT, updateUrl, updatedPosition,
                                    response1 -> Log.d("Position Update", "Position updated successfully"),
                                    error -> Toast.makeText(getActivity(), "Failed to update position", Toast.LENGTH_SHORT).show());

                            // Add the PUT request to the Volley request queue
                            Volley.newRequestQueue(getActivity()).add(updateRequest);
                        } else {
                            Toast.makeText(getActivity(), "Failed to retrieve position data", Toast.LENGTH_SHORT).show();
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        Toast.makeText(getActivity(), "Error parsing position data", Toast.LENGTH_SHORT).show();
                    }
                },
                error -> Toast.makeText(getActivity(), "Failed to fetch position data", Toast.LENGTH_SHORT).show());

        // Add the GET request to the Volley request queue
        Volley.newRequestQueue(getActivity()).add(jsonObjectRequest);
    }

    private void addPosition() {
        String numero = phoneEditText.getText().toString();
        String pseudo = pseudoEditText.getText().toString();
        String longitude = longitudeEditText.getText().toString();
        String latitude = latitudeEditText.getText().toString();

        if (numero.isEmpty() || pseudo.isEmpty() || longitude.isEmpty() || latitude.isEmpty()) {
            Toast.makeText(getActivity(), "Please fill in all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        Map<String, String> params = new HashMap<>();
        params.put("numero", numero);
        params.put("pseudo", pseudo);
        params.put("longitude", longitude);
        params.put("latitude", latitude);
        JSONObject jsonObject = new JSONObject(params);

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, Config.URL_ADD, jsonObject,
                response -> {
                    // Clear inputs after successful position addition
                    phoneEditText.setText("");
                    pseudoEditText.setText("");
                    longitudeEditText.setText("");
                    latitudeEditText.setText("");
                    Toast.makeText(getActivity(), "Position added successfully", Toast.LENGTH_SHORT).show();
                },
                error -> Toast.makeText(getActivity(), "Failed to add position", Toast.LENGTH_SHORT).show());

        Volley.newRequestQueue(getActivity()).add(jsonObjectRequest);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            fetchLocation(); // Retry fetching location if permission is granted
        } else {
            Toast.makeText(getActivity(), "Permission denied", Toast.LENGTH_SHORT).show();
        }
    }

    private void openMapActivity() {
        Intent intent = new Intent(getActivity(), MapsActivity.class);
        startActivity(intent);
    }
}
