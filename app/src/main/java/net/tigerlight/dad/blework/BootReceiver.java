package net.tigerlight.dad.blework;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.SystemClock;
import net.tigerlight.dad.registration.util.Constant;
import net.tigerlight.dad.util.Preference;

public class BootReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        if (Preference.getInstance().mSharedPreferences.getBoolean(Constant.ISLOGEDD_OUT, false)) {
            return;
        }
        AlarmManager alarmManagerForBLE = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (alarmManagerForBLE != null) {
            Intent bleIntent = new Intent(context, BleReceiver.class);
            PendingIntent broadcastIntentBle = PendingIntent.getBroadcast(context, 0, bleIntent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
            alarmManagerForBLE.setInexactRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP, SystemClock.elapsedRealtime(), 2 * 60 * 1000, broadcastIntentBle);
        }
    }

}
