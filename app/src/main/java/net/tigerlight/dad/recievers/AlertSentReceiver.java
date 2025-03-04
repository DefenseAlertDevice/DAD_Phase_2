package net.tigerlight.dad.recievers;

import net.tigerlight.dad.registration.activity.MainActivity;
import net.tigerlight.dad.registration.util.Constant;
import net.tigerlight.dad.util.Preference;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class AlertSentReceiver extends BroadcastReceiver
{
    public final String TAG = getClass().getSimpleName();

    @Override
    public void onReceive(Context context, Intent intent)
    {
        Preference.getInstance().mSharedPreferences.edit().putBoolean(Constant.IS_TEST_MODE, false).apply();
        Log.d(TAG, "Broadcast received");
        if (intent != null) {
            String jsonObject = intent.getStringExtra(Constant.JSON_OBJECT); // Assuming JSON_OBJECT is passed in the broadcast
            Intent activityIntent = new Intent(context, MainActivity.class);
            activityIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            activityIntent.setAction("OPEN_ALERT_DETAIL_FRAGMENT");
            activityIntent.putExtra(Constant.JSON_OBJECT, jsonObject); // Pass the JSON object
            context.startActivity(activityIntent);
        }
    }
}
