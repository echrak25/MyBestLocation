package Echrak.Chalghami.mybestlocation;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.IBinder;
import android.telephony.SmsManager;
import android.util.Log;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.android.volley.Request;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class MyLocationService extends Service {
    private static final String TAG = "MyLocationService";
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;

    @SuppressLint("MissingPermission")
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        String phone = intent.getStringExtra("phone");
        SmsManager manager = SmsManager.getDefault();
        FusedLocationProviderClient mClient = LocationServices.getFusedLocationProviderClient(this);

        // Check for location permissions before proceeding
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                || ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // If permissions are not granted, stop the service and handle it in MainActivity
            stopSelf();
            return START_NOT_STICKY;
        }

        mClient.getLastLocation().addOnSuccessListener(location -> {
            if (location != null) {
                double longitude = location.getLongitude();
                double latitude = location.getLatitude();
                String message = "Find Friends: ma position est #" + longitude + "#" + latitude;

                // Send the SMS
                manager.sendTextMessage(phone, null, message, null, null);
                Log.d(TAG, "Location sent: " + message);

                // Save location to the database
                addPosition(phone, "friend: " + phone, String.valueOf(longitude), String.valueOf(latitude));
            } else {
                Log.e(TAG, "Location is null. Unable to send location.");
            }
        });

        return START_NOT_STICKY;
    }

    private void addPosition(String numero, String pseudo, String longitude, String latitude) {
        Map<String, String> params = new HashMap<>();
        params.put("numero", numero);
        params.put("pseudo", pseudo);
        params.put("longitude", longitude);
        params.put("latitude", latitude);

        JSONObject jsonObject = new JSONObject(params);

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, Config.URL_ADD, jsonObject,
                response -> Log.d(TAG, "Position added successfully"),
                error -> Log.e(TAG, "Failed to add position", error));

        Volley.newRequestQueue(this).add(jsonObjectRequest);
    }

    @Override
    public IBinder onBind(Intent intent) {
        throw new UnsupportedOperationException("Not yet implemented");
    }
}
