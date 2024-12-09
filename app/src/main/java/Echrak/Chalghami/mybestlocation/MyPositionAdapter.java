package Echrak.Chalghami.mybestlocation;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

public class MyPositionAdapter extends BaseAdapter {
    private Context context;
    private ArrayList<Position> data;

    public MyPositionAdapter(Context context, ArrayList<Position> data) {
        this.context = context;
        this.data = data;
    }

    @Override
    public int getCount() {
        return data.size();
    }

    @Override
    public Object getItem(int position) {
        return data.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // Inflate the custom layout for each position item
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.my_view_position, parent, false);

        // Initialize views from the layout
        ImageView profileImage = view.findViewById(R.id.imageView);
        TextView tvNumero = view.findViewById(R.id.tvnum_position);
        TextView tvPseudo = view.findViewById(R.id.tvpseudo_position);
        TextView tvLatitude = view.findViewById(R.id.tvlatitude_position);
        TextView tvLongitude = view.findViewById(R.id.tvlongitude_position);
        ImageView imDelete = view.findViewById(R.id.imageSupprime_position);
        ImageView imEdit = view.findViewById(R.id.imageEdit_position);

        // Get position data for the current item
        Position positionData = data.get(position);

        // Set data to views
        tvNumero.setText(positionData.getNumero());
        tvPseudo.setText(positionData.getPseudo());
        tvLatitude.setText(positionData.getLatitude());
        tvLongitude.setText(positionData.getLongitude());

        // Set profile image (if needed)
        profileImage.setImageResource(R.drawable.profil); // Example placeholder

        // Handling the delete event
        imDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                data.remove(position); // Remove from the list
                notifyDataSetChanged(); // Refresh the list
                Toast.makeText(context, "Position deleted", Toast.LENGTH_SHORT).show();
            }
        });

        // Handling the call event (e.g., dial number from the contact)
        imEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder alert=new AlertDialog.Builder(context);
                alert.setTitle("edition");
                LayoutInflater inf=LayoutInflater.from(context);
                View v=inf.inflate(R.layout.view_dialog,null);

                Toast.makeText(context, "Edit contact functionality", Toast.LENGTH_SHORT).show();
            }
        });

        return view;
    }
}
