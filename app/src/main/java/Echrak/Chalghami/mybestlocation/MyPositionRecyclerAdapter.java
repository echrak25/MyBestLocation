package Echrak.Chalghami.mybestlocation;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class MyPositionRecyclerAdapter extends RecyclerView.Adapter<MyPositionRecyclerAdapter.MyViewHolder> {

    private final Context context;
    private ArrayList<Position> positionList;
    private static final String TAG = "MyPositionRecyclerAdapter";

    public MyPositionRecyclerAdapter(Context context, ArrayList<Position> positionList) {
        this.context = context;
        this.positionList = positionList;
        Log.d(TAG, "Adapter initialized with " + positionList.size() + " positions.");
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.my_view_position, parent, false);
        Log.d(TAG, "onCreateViewHolder called");
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        Position positionItem = positionList.get(position);
        holder.tvNum.setText(positionItem.getNumero());
        holder.tvPseudo.setText(positionItem.getPseudo());
        holder.tvLatitude.setText("Latitude: " + positionItem.getLatitude());
        holder.tvLongitude.setText("Longitude: " + positionItem.getLongitude());

        Log.d(TAG, "Binding view holder at position: " + position);

        // Delete item
        holder.imDelete.setOnClickListener(v -> {
            Log.d(TAG, "Delete clicked for position ID: " + positionItem.getIdposition());
            new DeletePositionTask(positionItem.getIdposition()).execute();
        });

        // Edit item
        holder.imEdit.setOnClickListener(v -> {
            Log.d(TAG, "Edit clicked for position ID: " + positionItem.getIdposition());
            showEditPositionDialog(positionItem);  // Show edit dialog when the edit button is clicked
        });
        // Open map on image click
        // Set up the ImageView click listener to open map
        holder.imageView.setOnClickListener(v -> {
            Intent mapIntent = new Intent(context, MapsActivity2.class);
            mapIntent.putExtra("latitude", positionItem.getLatitude());
            mapIntent.putExtra("longitude", positionItem.getLongitude());
            context.startActivity(mapIntent);
        });
    }

    @Override
    public int getItemCount() {
        Log.d(TAG, "getItemCount called, item count: " + positionList.size());
        return positionList.size();
    }

    public static class MyViewHolder extends RecyclerView.ViewHolder {
        TextView tvNum, tvPseudo, tvLatitude, tvLongitude;
        ImageView imDelete, imEdit,imageView;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            tvNum = itemView.findViewById(R.id.tvnum_position);
            tvPseudo = itemView.findViewById(R.id.tvpseudo_position);
            tvLatitude = itemView.findViewById(R.id.tvlatitude_position);
            tvLongitude = itemView.findViewById(R.id.tvlongitude_position);
            imDelete = itemView.findViewById(R.id.imageSupprime_position);
            imEdit = itemView.findViewById(R.id.imageEdit_position);
            imageView=itemView.findViewById(R.id.imageView);
        }
    }

    // AsyncTask to handle deleting a position from the database
    // AsyncTask to handle deleting a position from the database
    private class DeletePositionTask extends AsyncTask<Void, Void, Boolean> {

        private final int positionIdToDelete;

        public DeletePositionTask(int positionId) {
            this.positionIdToDelete = positionId;
            Log.d(TAG, "DeletePositionTask initialized for ID: " + positionIdToDelete);
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            try {
                String requestUrl = Config.URL_DELETE + "/" + positionIdToDelete;
                Log.d(TAG, "Delete request URL: " + requestUrl);

                // Using Volley for the DELETE request
                RequestQueue queue = Volley.newRequestQueue(context);
                StringRequest stringRequest = new StringRequest(Request.Method.DELETE, requestUrl,
                        response -> {
                            try {
                                JSONObject jsonResponse = new JSONObject(response);
                                boolean success = jsonResponse.getBoolean("success");
                                Log.d(TAG, "Delete response success: " + success);
                                if (success) {
                                    // Remove the position from the list and notify adapter
                                    for (int i = 0; i < positionList.size(); i++) {
                                        if (positionList.get(i).getIdposition() == positionIdToDelete) {
                                            positionList.remove(i);
                                            notifyItemRemoved(i);
                                            break;
                                        }
                                    }
                                    Toast.makeText(context, "Position deleted successfully", Toast.LENGTH_SHORT).show();
                                } else {
                                    Log.e(TAG, "Failed to delete position");
                                    Toast.makeText(context, "Failed to delete position", Toast.LENGTH_SHORT).show();
                                }
                            } catch (Exception e) {
                                Log.e(TAG, "Error parsing delete response", e);
                            }
                        },
                        error -> {
                            Log.e(TAG, "Delete request error: " + error.getMessage());
                            Toast.makeText(context, "Failed to delete position", Toast.LENGTH_SHORT).show();
                        });

                // Add the request to the queue
                queue.add(stringRequest);
            } catch (Exception e) {
                Log.e(TAG, "Error in delete operation", e);
                return false;
            }
            return true;
        }
    }

    // Method to show the edit dialog
    private void showEditPositionDialog(Position positionToEdit) {
        Log.d(TAG, "Showing edit dialog for position ID: " + positionToEdit.getIdposition());

        AlertDialog.Builder alert = new AlertDialog.Builder(context);
        alert.setTitle("Edit Position");

        LayoutInflater inflater = LayoutInflater.from(context);
        View editView = inflater.inflate(R.layout.view_dialog, null);

        EditText editPseudo = editView.findViewById(R.id.editTextPseudo);
        EditText editNumero = editView.findViewById(R.id.editTextNumber);
        EditText editLatitude = editView.findViewById(R.id.editTextLatitude);
        EditText editLongitude = editView.findViewById(R.id.editTextLongitude);

        // Pre-fill the fields with the current position values
        editPseudo.setText(positionToEdit.getPseudo());
        editNumero.setText(positionToEdit.getNumero());
        editLatitude.setText(String.valueOf(positionToEdit.getLatitude()));
        editLongitude.setText(String.valueOf(positionToEdit.getLongitude()));

        alert.setView(editView);
        alert.setPositiveButton("Save", (dialog, which) -> {
            Log.d(TAG, "Save button clicked, updating position");
            updatePosition(positionToEdit, editPseudo, editNumero, editLatitude, editLongitude);
        });

        alert.setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());

        alert.create().show();
    }

    private void updatePosition(Position position, EditText editPseudo, EditText editNumero, EditText editLatitude, EditText editLongitude) {
        position.setPseudo(editPseudo.getText().toString());
        position.setNumero(editNumero.getText().toString());
        position.setLatitude(editLatitude.getText().toString());
        position.setLongitude(editLongitude.getText().toString());

        // Use the PUT method for updating the position (or POST if needed)
        String requestUrl = Config.URL_EDIT + "/" + position.getIdposition();

        StringRequest stringRequest = new StringRequest(Request.Method.PUT, requestUrl,
                response -> {
                    try {
                        JSONObject jsonResponse = new JSONObject(response);
                        boolean success = jsonResponse.getBoolean("success");
                        Log.d(TAG, "Edit response success: " + success);
                        if (success) {
                            // Update the position in the local list (adapter)
                            for (int i = 0; i < positionList.size(); i++) {
                                if (positionList.get(i).getIdposition() == position.getIdposition()) {
                                    positionList.set(i, position); // Update the position in the list
                                    notifyItemChanged(i); // Notify adapter that the item at index i has changed
                                    break;
                                }
                            }
                            Toast.makeText(context, "Position updated successfully", Toast.LENGTH_SHORT).show();
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "Error during edit operation", e);
                    }
                },
                error -> {
                    Log.e(TAG, "Error: " + error.getMessage());
                    Toast.makeText(context, "Failed to update position", Toast.LENGTH_SHORT).show();
                }) {

            // Create the JSON body to send in the request
            @Override
            public byte[] getBody() {
                try {
                    // Create the JSON object for the body
                    JSONObject jsonParams = new JSONObject();
                    jsonParams.put("longitude", position.getLongitude());
                    jsonParams.put("latitude", position.getLatitude());
                    jsonParams.put("numero", position.getNumero());
                    jsonParams.put("pseudo", position.getPseudo());

                    // Return the JSON as a byte array
                    return jsonParams.toString().getBytes("utf-8");
                } catch (Exception e) {
                    Log.e(TAG, "Error creating JSON params", e);
                    return new byte[0]; // Return an empty byte array in case of error
                }
            }

            // Set the content type to JSON
            @Override
            public String getBodyContentType() {
                return "application/json; charset=utf-8";
            }
        };

        // Add the request to the request queue
        RequestQueue queue = Volley.newRequestQueue(context);
        queue.add(stringRequest);
    }

}
