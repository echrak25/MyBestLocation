package Echrak.Chalghami.mybestlocation;

import android.app.AlertDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;
import androidx.fragment.app.FragmentActivity;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import org.json.JSONArray;
import org.json.JSONObject;
import java.util.HashMap;
import java.util.Map;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private RequestQueue queue;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        // Initialize Volley request queue
        queue = Volley.newRequestQueue(this);

        // Initialize the map fragment
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        // Display marker information and show delete confirmation on click
        mMap.setOnMarkerClickListener(marker -> {
            showDeleteMarkerDialog(marker);
            return true;
        });

        // Add new marker on map click
        mMap.setOnMapClickListener(this::showAddMarkerDialog);

        // Fetch and display markers from server
        fetchPositions();
    }

    private void fetchPositions() {
        String url = Config.URL_GETALL;

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, url, null,
                response -> {
                    try {
                        if (response != null && response.getInt("success") == 1) {
                            JSONArray positionsArray = response.getJSONArray("positions");
                            LatLngBounds.Builder boundsBuilder = new LatLngBounds.Builder();

                            for (int i = 0; i < positionsArray.length(); i++) {
                                JSONObject position = positionsArray.getJSONObject(i);
                                int id = position.getInt("idposition");

                                double latitude = position.getDouble("latitude");
                                double longitude = position.getDouble("longitude");
                                String numero = position.getString("numero");
                                String pseudo = position.optString("pseudo", "Unknown");
                                String positionInfo = "Pseudo: " + pseudo + "\nNumero: " + numero +
                                        "\nLatitude: " + latitude + "\nLongitude: " + longitude;

                                LatLng latLng = new LatLng(latitude, longitude);

                                // Check if the user has id = 13, and change the marker color
                                MarkerOptions markerOptions = new MarkerOptions()
                                        .position(latLng)
                                        .title(String.valueOf(id))
                                        .draggable(true);

                                // Set the color based on user ID
                                if (id == 2) {
                                    markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_YELLOW));
                                     } else {
                                    markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)); // Default color
                                }

                                Marker marker = mMap.addMarker(markerOptions);
                                marker.setTag(positionInfo);  // Store positionInfo as the tag of the marker
                                boundsBuilder.include(latLng);
                            }

                            if (positionsArray.length() > 0) {
                                LatLngBounds bounds = boundsBuilder.build();
                                mMap.moveCamera(CameraUpdateFactory.newLatLngBounds(bounds, 50));
                            }
                        } else {
                            Toast.makeText(MapsActivity.this, "No positions found", Toast.LENGTH_SHORT).show();
                        }
                    } catch (Exception e) {
                        Toast.makeText(MapsActivity.this, "Error parsing positions", Toast.LENGTH_SHORT).show();
                    }
                },
                error -> Toast.makeText(MapsActivity.this, "Failed to load positions", Toast.LENGTH_SHORT).show());

        queue.add(jsonObjectRequest);
    }

    private void showAddMarkerDialog(LatLng latLng) {
        LayoutInflater inflater = LayoutInflater.from(this);
        View dialogView = inflater.inflate(R.layout.mark_dialog, null);

        EditText pseudoEditText = dialogView.findViewById(R.id.editTextPseudo);
        EditText numeroEditText = dialogView.findViewById(R.id.editTextNumber);

        new AlertDialog.Builder(this)
                .setTitle("Add Position")
                .setView(dialogView)
                .setPositiveButton("Add", (dialog, which) -> {
                    String pseudo = pseudoEditText.getText().toString();
                    String numero = numeroEditText.getText().toString();
                    addMarkerToMap(latLng, pseudo, numero);
                    savePositionToServer(latLng, pseudo, numero);
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void addMarkerToMap(LatLng latLng, String pseudo, String numero) {
        String positionInfo = "Pseudo: " + pseudo + "\nNumero: " + numero +
                "\nLatitude: " + latLng.latitude + "\nLongitude: " + latLng.longitude;

        // Now correctly setting title and tag
        Marker marker = mMap.addMarker(new MarkerOptions()
                .position(latLng)
                .title(pseudo)  // Use pseudo as the title
                .draggable(true));

        marker.setTag(positionInfo);  // Store the position info in the tag
    }

    private void savePositionToServer(LatLng latLng, String pseudo, String numero) {
        String url = Config.URL_ADD;

        Map<String, String> params = new HashMap<>();
        params.put("latitude", String.valueOf(latLng.latitude));
        params.put("longitude", String.valueOf(latLng.longitude));
        params.put("pseudo", pseudo);
        params.put("numero", numero);

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, url, new JSONObject(params),
                response -> Toast.makeText(this, "Position saved!", Toast.LENGTH_SHORT).show(),
                error -> Toast.makeText(this, "Error saving position", Toast.LENGTH_SHORT).show());

        queue.add(request);
    }

    private void showDeleteMarkerDialog(Marker marker) {
        String positionInfo = (String) marker.getTag(); // Retrieve the marker's info
        new AlertDialog.Builder(this)
                .setTitle("Position")
                .setMessage("Position Information:\n" + positionInfo + "\n\nAre you sure you want to delete this position?")
                .setPositiveButton("Delete", (dialog, which) -> {
                    marker.remove();
                    // Assuming you have the position ID stored as part of the marker's data
                    String positionId = marker.getTitle(); // Use title as a unique identifier
                    deletePositionFromServer(positionId);
                    Toast.makeText(MapsActivity.this, "Position deleted successfully", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void deletePositionFromServer(String positionId) {
        String url = Config.URL_DELETE + positionId;
        Log.d(url, "deletePositionFromServer: "+url);
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.DELETE, url, null,
                response -> Toast.makeText(this, "Position deleted!", Toast.LENGTH_SHORT).show(),
                error -> Toast.makeText(this, "Error deleting position", Toast.LENGTH_SHORT).show());

        queue.add(request);
    }
}
