package net.tigerlight.dad;

import net.tigerlight.dad.registration.fragment.AlertDetailFragment;

import android.app.IntentService;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.SystemClock;
import androidx.core.app.NotificationCompat;
import android.util.Log;
import android.widget.Toast;

public class GcmIntentService extends IntentService {

    public static final int NOTIFICATION_ID = 1;
    private static final String TAG = "GCMDemo";
    private NotificationManager mNotificationManager;
    NotificationCompat.Builder builder;

    public GcmIntentService() {
        super("GcmIntentService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Bundle extras = intent.getExtras();

        Toast.makeText(this, "Service started", Toast.LENGTH_LONG).show();

        if (!extras.isEmpty()) {  // has effect of unparcelling Bundle
            // This loop represents the service doing some work.
            for (int i = 0; i < 5; i++) {
                Log.i(TAG, "Working... " + (i + 1)
                        + "/5 @ " + SystemClock.elapsedRealtime());
                try {
                    Thread.sleep(5000);
                } catch (InterruptedException e) {
                }
            }
            Log.i(TAG, "Completed work @ " + SystemClock.elapsedRealtime());
            // Post notification of received message.
            Log.i(TAG, "Received In Service: " + extras.toString());
            sendNotification(extras.toString());
        }
    }

    // Put the message into a notification and post it.
    // This is just one simple example of what you might choose to do with
    // a GCM message.
    private void sendNotification(String msg) {
        Toast.makeText(this, "Notification Created", Toast.LENGTH_LONG).show();
        mNotificationManager = (NotificationManager)
                this.getSystemService(Context.NOTIFICATION_SERVICE);

        PendingIntent contentIntent = PendingIntent.getActivity(this, 0,
                new Intent(this, AlertDetailFragment.class), PendingIntent.FLAG_IMMUTABLE);

        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(this)
                        .setSmallIcon(R.drawable.app_icon)
                        .setContentTitle("ALERT Notification")
                        .setStyle(new NotificationCompat.BigTextStyle()
                                .bigText(msg))
                        .setContentText(msg);

        mBuilder.setContentIntent(contentIntent);
        mNotificationManager.notify(NOTIFICATION_ID, mBuilder.build());
    }

}
