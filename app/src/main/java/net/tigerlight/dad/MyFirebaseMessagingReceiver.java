package net.tigerlight.dad;

import net.tigerlight.dad.registration.activity.MainActivity;
import net.tigerlight.dad.registration.fragment.AlertDetailFragment;
import net.tigerlight.dad.registration.util.Constant;
import net.tigerlight.dad.util.CheckForeground;
import net.tigerlight.dad.util.Util;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.media.AudioAttributes;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import androidx.core.app.NotificationCompat;

import static net.tigerlight.dad.registration.fragment.AlertFragment.jsonobjectToChange;

import java.io.InputStream;

public class MyFirebaseMessagingReceiver extends BroadcastReceiver { // Changed from WakefulBroadcastReceiver

    public static final int DELAY_MILLIS = 5000;

    /*
              Sample intent data:
              Bundle[
          {
            google.sent_time=1523459319099,
            google.ttl=3600,
            gcm.notification.alert=Test User 1 is in Danger at 123 Main St, City Name, ST 12345, USA http://maps.google.com/?q=34.77957,-119.0335347&zoom=17 estimated accuracy is 20 meters.,
            gcm.notification.badge=1,
            gcm.notification.sound=default,
            from=32989397760,
            google.message_id=0:1523459319105669%230ce0ddf9fd7ecd,
            gcm.notification.data={"datetime":null,"alertType":"0","address":"123 Main St, City Name, City Name, ST 12345, USA","phone":"1115551212","latitude":"34.77957","testStatus":"false","userid":"1234","email":"TestUser1@test.com","longitude":"-119.0335347","username":"Test User 1","status":"0"}}]

             */
    private final String TAG_USER_NAME = "username";
    private String data = "";

    @Override
    public void onReceive(Context context, Intent intent) {
        if (CheckForeground.isInForeGround() && !CheckForeground.isThreatScreenVisible()) {
            updateInFront(context, intent);
            return;
        } else {
            showNotification(context, intent);
        }
    }

    private void updateInFront(Context context, Intent intent) {
        Bundle extras = intent.getExtras();
        if (extras == null) {
            return;
        }
        jsonobjectToChange = bundleToJson(extras);
        // Pass the data to MainActivity
        Intent activityIntent = new Intent(context, MainActivity.class);
        activityIntent.putExtra(Constant.JSON_OBJECT, jsonobjectToChange.toString());
        activityIntent.putExtra("SHOW_ALERT_FRAGMENT", true);
        activityIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        context.startActivity(activityIntent);
    }

    private JSONObject bundleToJson(Bundle extras) {
        JSONObject json = new JSONObject();
        for (String key : extras.keySet()) {
            Object value = extras.get(key);
            try {
                json.put(key, JSONObject.wrap(value));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return json;
    }

    private boolean isValidSoundUri(Context context, Uri soundUri) {
        try {
            // Try to open the URI with a ContentResolver
            InputStream inputStream = context.getContentResolver().openInputStream(soundUri);
            if (inputStream != null) {
                inputStream.close(); // Close the stream after validation
                return true;
            }
        } catch (Exception e) {
            e.printStackTrace(); // Log any exception (e.g., file not found)
        }
        return false;
    }

    private void showNotification(Context context, Intent intentData) {
        Bundle extras = intentData.getExtras();
        if (extras == null) {
            return;
        }

        JSONObject jsonFromBundle = bundleToJson(extras);
        final String jsonObject = jsonFromBundle.toString();

        String sound = extras.getString("gcm.notification.sound");
        String sound2 = extras.getString("gcm.notification.sound2");

        String userName = "";
        String safeDangerString = " is in danger!";
        userName = jsonFromBundle.optString(TAG_USER_NAME);
        if (jsonFromBundle.optInt("status") == 1) {
            safeDangerString = userName + " is safe.";
        } else {
            safeDangerString = userName + " is in danger!";
        }

        final Intent intent = new Intent(context, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.putExtra(Constant.JSON_OBJECT, jsonObject);
        PendingIntent contentIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(context, "default_channel_id")
                .setSmallIcon(R.drawable.app_icon)
                .setContentTitle("The SoulDefendHER™ Danger Alert")
                .setContentText(safeDangerString)
                .setContentIntent(contentIntent)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setAutoCancel(true);

        NotificationManager mNotificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        NotificationChannel channel = new NotificationChannel("default_channel_id", "Default Channel", NotificationManager.IMPORTANCE_DEFAULT);
        if ((sound != null && !"default".equals(sound)) || (sound2 != null && !"default".equals(sound2))) {
            Uri soundUri = Uri.parse("android.resource://" + context.getPackageName() + "/" + R.raw.tigerlightsound);
            if (isValidSoundUri(context, soundUri)) {
                mBuilder.setSound(soundUri);

                AudioAttributes audioAttributes = new AudioAttributes.Builder()
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .setUsage(AudioAttributes.USAGE_NOTIFICATION)
                    .build();

                channel.setSound(soundUri, audioAttributes);
            } else {
                // Fallback to default sound if URI is invalid
                mBuilder.setDefaults(Notification.DEFAULT_SOUND);
            }
        } else {
            mBuilder.setDefaults(Notification.DEFAULT_SOUND);
        }

        mNotificationManager.createNotificationChannel(channel);
        mNotificationManager.notify(1, mBuilder.build());
    }
}
