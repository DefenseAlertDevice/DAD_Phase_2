package net.tigerlight.dad.blework;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.location.LocationManager;
import net.tigerlight.dad.registration.util.Constant;
import net.tigerlight.dad.util.Preference;

public class MyService extends IntentService {

    //	private LocationClient mLocationClient;
    private String longtd;
    private String lattd;
    private String userId;

    public MyService() {
        super("MyService");
    }

    @Override
    public void onCreate() {
        super.onCreate();
//		mLocationClient = new LocationClient(this, this, this);
        final LocationManager manager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);


        if (Preference.getInstance().mSharedPreferences.getInt(Constant.KEY_REFRESH_LOC, 0) == 0) {
            return;
        }
        if (manager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            userId = Preference.getInstance().mSharedPreferences.getString(Constant.USER_ID, "");
            new LoactionUpdateThread().start();
        }
    }


    private class LoactionUpdateThread extends Thread {

        @Override
        public void run() {
//			ServerResponseHelper serverResponseHelper = new ServerResponseHelper();
//			 serverResponseHelper.requestToupdateLoaction(userId, String.valueOf(((BaseActivity) getApplicationContext()).getLongitude()), String.valueOf(((BaseActivity) getApplicationContext()).getLatitude()));
        }
    }

    @Override
    public void onStart(Intent intent, int startId) {
        super.onStart(intent, startId);
    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);
        return START_STICKY;
    }

//	@Override
//	public void onConnectionFailed(ConnectionResult result) {
//
//	}
//
//	@Override
//	public void onConnected(Bundle connectionHint) {
//		Location lastLocation = mLocationClient.getLastLocation();
//		double longitude = 22;
//		double latitude = 22;
//		if (lastLocation != null) {
//			longitude = lastLocation.getLongitude();
//			latitude = lastLocation.getLatitude();
//		}
//		longtd = Double.toString(longitude);
//		lattd = Double.toString(latitude);
//	}
//
//	@Override
//	public void onDisconnected() {
//		mLocationClient.disconnect();
//	}

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    protected void onHandleIntent(Intent intent) {

    }

}
