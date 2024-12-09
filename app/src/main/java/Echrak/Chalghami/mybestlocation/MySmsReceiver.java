package Echrak.Chalghami.mybestlocation;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.telephony.SmsMessage;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;

public class MySmsReceiver extends BroadcastReceiver {

    private static final String CHANNEL_ID = "canal_findfriends";

    @Override
    public void onReceive(Context context, Intent intent) {
        if ("android.provider.Telephony.SMS_RECEIVED".equals(intent.getAction())) {
            Bundle bundle = intent.getExtras();
            if (bundle != null) {
                Object[] pdus = (Object[]) bundle.get("pdus");
                if (pdus != null && pdus.length > 0) {
                    SmsMessage[] messages = new SmsMessage[pdus.length];
                    for (int i = 0; i < pdus.length; i++) {
                        messages[i] = SmsMessage.createFromPdu((byte[]) pdus[i]);
                    }

                    // Process the first SMS message
                    SmsMessage message = messages[0];
                    String messageBody = message.getMessageBody();
                    String phoneNumber = message.getDisplayOriginatingAddress();

                    if (messageBody.contains("Find friends : envoie votre position")) {
                        Intent locationIntent = new Intent(context, MyLocationService.class);
                        locationIntent.putExtra("phone", phoneNumber);
                        context.startService(locationIntent);
                    }

                    if (messageBody.contains("Find Friends: ma position est")) {
                        // Extract longitude and latitude
                        String[] locationData = messageBody.split("#");
                        if (locationData.length >= 3) {
                            String longitude = locationData[1].trim();
                            String latitude = locationData[2].trim();

                            // Create the notification channel if it doesn't already exist
                            createNotificationChannel(context);

                            // Check for notification permission
                            if (ContextCompat.checkSelfPermission(context, android.Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED) {
                                // Create notification
                                Intent mapIntent = new Intent(context, MapsActivity.class);
                                mapIntent.putExtra("longitude", longitude);
                                mapIntent.putExtra("latitude", latitude);

                                PendingIntent pendingIntent = PendingIntent.getActivity(context, 1, mapIntent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_MUTABLE);

                                NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(context, CHANNEL_ID)
                                        .setContentTitle("Position reçue")
                                        .setContentText("Appuyez pour voir la position sur la carte")
                                        .setAutoCancel(true)
                                        .setSmallIcon(android.R.drawable.ic_dialog_map)
                                        .setContentIntent(pendingIntent);

                                // Show notification
                                NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
                                notificationManager.notify(1, notificationBuilder.build());
                            } else {
                                // Handle the case when permission is not granted
                                // This could involve logging, showing a toast, or whatever makes sense in your app
                                // Consider starting an Activity that can request permission
                            }
                        }
                    }
                }
            }
        }
    }

    private void createNotificationChannel(Context context) {
        // Create the notification channel if it doesn't already exist
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    "Canal de l'application FindFriends",
                    NotificationManager.IMPORTANCE_HIGH
            );
            NotificationManager manager = context.getSystemService(NotificationManager.class);
            if (manager != null) {
                manager.createNotificationChannel(channel);
            }
        }
    }
}
