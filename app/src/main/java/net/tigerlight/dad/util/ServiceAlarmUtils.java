package net.tigerlight.dad.util;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

import net.tigerlight.dad.LocationUpdateService;
import net.tigerlight.dad.registration.util.DadConstant;

public class ServiceAlarmUtils {
    private static final long INTERVAL = 30 * 1000; // 30 seconds

    public static void setupPeriodicService(final Context context) {
        final boolean isLogin = Preference.getInstance().mSharedPreferences.getBoolean(DadConstant.IS_LOGIN, false);
        if (!isLogin) return;

        Intent serviceIntent = new Intent(context, LocationUpdateService.class);
        PendingIntent pendingIntent = PendingIntent.getService(context, 0, serviceIntent, PendingIntent.FLAG_NO_CREATE | PendingIntent.FLAG_IMMUTABLE);

        if (pendingIntent != null) {
            cancelPeriodicService(context);
        }
        pendingIntent = PendingIntent.getForegroundService(context, 0, serviceIntent, PendingIntent.FLAG_CANCEL_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        context.startForegroundService(serviceIntent);

        final AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (alarmManager != null) {
            alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(), INTERVAL, pendingIntent);
        }
    }

    public static void cancelPeriodicService(final Context context) {
        final Intent serviceIntent = new Intent(context, LocationUpdateService.class);
        final PendingIntent pendingIntent = PendingIntent.getService(context, 0, serviceIntent, PendingIntent.FLAG_CANCEL_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        final AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (alarmManager != null) {
            alarmManager.cancel(pendingIntent);
        }
    }
}
