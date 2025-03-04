package net.tigerlight.dad.blework;

import android.content.Context;
import android.content.Intent;
import androidx.legacy.content.WakefulBroadcastReceiver;
import androidx.core.content.ContextCompat; // Add this import
import android.util.Log;

public class BleReceiver extends WakefulBroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {
		Intent myServiceIntent = new Intent(context, BleService.class);
		ContextCompat.startForegroundService(context, myServiceIntent); // Updated method
        Log.d(this.getClass().getSimpleName(), "BleReceiver.onReceive()");
	}
}
