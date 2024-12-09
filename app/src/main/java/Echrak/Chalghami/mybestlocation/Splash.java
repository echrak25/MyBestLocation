package Echrak.Chalghami.mybestlocation;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import Echrak.Chalghami.mybestlocation.R;

public class Splash extends AppCompatActivity {

    private static final int SPLASH_DISPLAY_LENGTH = 4000; // Duration of splash screen
    private static final int LETTER_DELAY = 150; // Delay between letters in milliseconds

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_splash);

        // Handle system insets for a better appearance
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Set up the logo rotation animation
        ImageView splashLogo = findViewById(R.id.splash_logo);
        Animation rotateAnimation = AnimationUtils.loadAnimation(this, R.anim.rotate);
        splashLogo.startAnimation(rotateAnimation);

        // Prepare the app name TextView
        TextView appName = findViewById(R.id.app_name);
        appName.setVisibility(TextView.VISIBLE);

        // Animate the app name letter by letter
        String fullText = "Chouchou's my best location"; // Your app name
        animateText(appName, fullText);

        // Delay for splash screen and then transition to MainActivity
        new Handler().postDelayed(() -> {
            Intent intent = new Intent(Splash.this, MainActivity.class);
            startActivity(intent);
            finish(); // Finish the splash activity
        }, SPLASH_DISPLAY_LENGTH);
    }

    private void animateText(TextView textView, String text) {
        textView.setText(""); // Clear the text initially

        // Iterate over each character in the text and display them with a delay
        new Handler().postDelayed(new Runnable() {
            int index = 0;

            @Override
            public void run() {
                if (index < text.length()) {
                    textView.append(String.valueOf(text.charAt(index))); // Append current character
                    index++;
                    new Handler().postDelayed(this, LETTER_DELAY); // Delay for the next character
                }
            }
        }, LETTER_DELAY); // Start after initial delay
    }
}
