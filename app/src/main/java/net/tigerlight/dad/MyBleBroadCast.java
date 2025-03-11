package net.tigerlight.dad;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import net.tigerlight.dad.blework.MyBleService;

/**
 * Created by indianic on 17/04/17.
 */

public class MyBleBroadCast extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {

        long time = 1000 * 5;  //For repeating 5 seconds

        try {
            Intent serviceIntent = new Intent(context, MyBleService.class);
            PendingIntent pendingIntent = PendingIntent.getService(context, 1001, serviceIntent, PendingIntent.FLAG_CANCEL_CURRENT | PendingIntent.FLAG_IMMUTABLE);
            AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
            // Use setExactAndAllowWhileIdle for more precise timing
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + time, pendingIntent);
        } catch (Exception e) {
            Intent serviceIntent = new Intent(context, MyBleService.class);
            context.startService(serviceIntent);
            Log.e("Exception", "Exception occurred", e);  // Log the full stack trace
        }
    }
}
