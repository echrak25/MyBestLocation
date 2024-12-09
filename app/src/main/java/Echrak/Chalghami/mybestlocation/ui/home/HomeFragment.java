package Echrak.Chalghami.mybestlocation.ui.home;

import android.app.AlertDialog;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SearchView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.GridLayoutManager;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import Echrak.Chalghami.mybestlocation.Config;
import Echrak.Chalghami.mybestlocation.JSONParser;
import Echrak.Chalghami.mybestlocation.MyPositionRecyclerAdapter;
import Echrak.Chalghami.mybestlocation.Position;
import Echrak.Chalghami.mybestlocation.R;
import Echrak.Chalghami.mybestlocation.databinding.FragmentHomeBinding;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

public class HomeFragment extends Fragment {

    private ArrayList<Position> data = new ArrayList<>();
    private ArrayList<Position> filteredData = new ArrayList<>();
    private FragmentHomeBinding binding;
    private MyPositionRecyclerAdapter adapter;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentHomeBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        // Set up RecyclerView
        adapter = new MyPositionRecyclerAdapter(getContext(), filteredData);
        binding.rv.setLayoutManager(new GridLayoutManager(getContext(), 1));
        binding.rv.setAdapter(adapter);

        // Set up SearchView for filtering by numero and pseudo
        SearchView searchView = binding.searchView;
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                // Optionally handle search submit if needed
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                filterPositions(newText); // Filter positions based on search input
                return true;
            }
        });

        // Set up the SwipeRefreshLayout
        binding.swipeRefreshLayout.setOnRefreshListener(() -> {
            // Refresh data when user swipes down
            new Download().execute();
        });
binding.searchButton.setOnClickListener(v -> {
    // Get the NavController from the activity or fragment
    NavController navController = Navigation.findNavController(v);
    navController.navigate(R.id.nav_map);
});
        // Download data on button click
        binding.btndownload.setOnClickListener(view -> new Download().execute());

        // Initial data load
        new Download().execute();

        return root;
    }

    // Filter positions based on query in both numero and pseudo
    private void filterPositions(String query) {
        filteredData.clear(); // Clear filtered data to avoid duplication
        for (Position position : data) {
            if (position.getPseudo().toLowerCase().contains(query.toLowerCase()) ||
                    position.getNumero().toLowerCase().contains(query.toLowerCase())) {
                filteredData.add(position); // Add position if it matches the query
            }
        }
        adapter.notifyDataSetChanged();  // Update the adapter with filtered data
    }

    // Method to refresh data manually
    public void refreshData() {
        new Download().execute();  // This will reload the data and refresh the RecyclerView
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    class Download extends AsyncTask<Void, Void, Void> {

        private AlertDialog alert;

        @Override
        protected void onPreExecute() {
            // Show progress dialog if not refreshing
            if (!binding.swipeRefreshLayout.isRefreshing()) {
                AlertDialog.Builder dialog = new AlertDialog.Builder(getActivity());
                dialog.setTitle("Téléchargement");
                dialog.setMessage("Veuillez patienter...");
                alert = dialog.create();
                alert.show();
            }
        }

        @Override
        protected Void doInBackground(Void... voids) {
            try {
                Thread.sleep(1000); // Simulate delay
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }

            JSONParser parser = new JSONParser();
            JSONObject response = parser.makeRequest(Config.URL_GETALL);
            if (response != null) {
                Log.d("Download", "Response received: " + response.toString());
            } else {
                Log.e("Download", "Failed to get a valid response");
                return null;
            }

            try {
                int success = response.getInt("success");
                if (success == 1) {
                    JSONArray tableau = response.getJSONArray("positions");
                    data.clear(); // Clear the old data to prevent duplicates
                    for (int i = 0; i < tableau.length(); i++) {
                        JSONObject ligne = tableau.getJSONObject(i);
                        data.add(new Position(
                                ligne.getInt("idposition"),
                                ligne.getString("pseudo"),
                                ligne.getString("numero"),
                                ligne.getString("longitude"),
                                ligne.getString("latitude")
                        ));
                    }
                    filteredData.clear(); // Clear the filtered data
                    filteredData.addAll(data); // Initially, show all data
                }
            } catch (JSONException e) {
                Log.e("Download", "JSON parsing error: ", e);
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            // Update RecyclerView adapter
            adapter.notifyDataSetChanged();
            // Dismiss progress dialog
            if (alert != null && alert.isShowing()) {
                alert.dismiss();
            }
            // Stop the SwipeRefreshLayout's loading indicator
            if (binding.swipeRefreshLayout.isRefreshing()) {
                binding.swipeRefreshLayout.setRefreshing(false);
            }
        }
    }
}
