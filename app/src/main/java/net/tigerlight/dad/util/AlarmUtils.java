package net.tigerlight.dad.util;

import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

import net.tigerlight.dad.LocationService;
import net.tigerlight.dad.registration.util.Constant;

public class AlarmUtils {
    private static final long INTERVAL = 30 * 1000; // 30 seconds

    @SuppressLint("ShortAlarm")
    public static void setupPeriodicService(final Context context) {
        final boolean isLogin = Preference.getInstance().mSharedPreferences.getBoolean(Constant.IS_LOGIN, false);
        if (!isLogin) return;

        Intent serviceIntent = new Intent(context, LocationService.class);
        PendingIntent pendingIntent = PendingIntent.getService(context, 0, serviceIntent, PendingIntent.FLAG_NO_CREATE | PendingIntent.FLAG_IMMUTABLE);

        if (pendingIntent != null) {
            cancelPeriodicService(context);
        }
        pendingIntent = PendingIntent.getForegroundService(context, 0, serviceIntent, PendingIntent.FLAG_CANCEL_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        context.startForegroundService(serviceIntent);

        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (alarmManager != null) {
            alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(), INTERVAL, pendingIntent);
        }
    }

    public static void cancelPeriodicService(final Context context) {
        Intent serviceIntent = new Intent(context, LocationService.class);
        PendingIntent pendingIntent = PendingIntent.getService(context, 0, serviceIntent, PendingIntent.FLAG_CANCEL_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (alarmManager != null) {
            alarmManager.cancel(pendingIntent);
        }
    }
}
