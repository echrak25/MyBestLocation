package Echrak.Chalghami.mybestlocation.ui.slideshow;

import android.telephony.SmsManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import Echrak.Chalghami.mybestlocation.databinding.FragmentSlideshowBinding;

public class SlideshowFragment extends Fragment {

    private FragmentSlideshowBinding binding;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout using ViewBinding
        binding = FragmentSlideshowBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        // Retrieve UI elements
        final Button btnSendSms = binding.btnEnvSmsHome; // Ensure ID matches the layout XML
        final EditText phoneInput = binding.phoneHome;

        // Add click listener to the button
        btnSendSms.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String phoneNumber = phoneInput.getText().toString().trim();
                if (phoneNumber.isEmpty()) {
                    Toast.makeText(getContext(), "Veuillez entrer un numéro de téléphone", Toast.LENGTH_SHORT).show();
                } else {
                    //envoie SMS
                    SmsManager manager = SmsManager.getDefault();
                    manager.sendTextMessage(phoneNumber,null,"Find friends : envoie votre position",null,null);

                }
            }
        });
        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
