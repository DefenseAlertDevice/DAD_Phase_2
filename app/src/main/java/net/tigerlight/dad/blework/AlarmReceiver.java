package net.tigerlight.dad.blework;

import android.content.Context;
import android.content.Intent;
import androidx.core.content.ContextCompat;
import androidx.legacy.content.WakefulBroadcastReceiver;

public class AlarmReceiver extends WakefulBroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {
		Intent myServiceIntent = new Intent(context, MyService.class);
		ContextCompat.startForegroundService(context, myServiceIntent);
	}

}
