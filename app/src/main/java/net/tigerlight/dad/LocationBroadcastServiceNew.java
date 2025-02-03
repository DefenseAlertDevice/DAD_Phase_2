package net.tigerlight.dad;

import net.tigerlight.dad.registration.activity.MainActivity;
import net.tigerlight.dad.registration.fragment.ContactFragment;
import net.tigerlight.dad.webservices.WsCallDADTest;
import net.tigerlight.dad.webservices.WsCallSendDanger;
import net.tigerlight.dad.util.CheckForeground;
import net.tigerlight.dad.util.GPSTracker;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationResult;

import android.app.Activity;
import android.app.Dialog;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;

import net.tigerlight.dad.registration.util.Constant;
import net.tigerlight.dad.registration.util.Utills;
import net.tigerlight.dad.webservices.WsCallUpdateLocation;
import net.tigerlight.dad.util.Constants;
import net.tigerlight.dad.util.Preference;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.content.res.AssetFileDescriptor;
import android.location.Location;
import android.media.MediaPlayer;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import static android.bluetooth.BluetoothAdapter.ACTION_DISCOVERY_FINISHED;
import static android.bluetooth.BluetoothAdapter.ACTION_DISCOVERY_STARTED;
import static android.bluetooth.BluetoothAdapter.ACTION_SCAN_MODE_CHANGED;

import static net.tigerlight.dad.util.CheckForeground.getActivity;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;

import java.io.IOException;
import java.util.Arrays;

/**
 * Created on 23/11/16.
 */

public class LocationBroadcastServiceNew extends Service implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, LocationListener {

    private static final int REQUEST_PERMISSIONS_REQUEST_CODE = 1000;
    final static char[] HEX_ARRAY = "0123456789ABCDEF".toCharArray();
    private static final String CHANNEL_ID = "default";
    public final String TAG = getClass().getSimpleName();
    /**
     * The desired interval for location updates. Inexact. Updates may be more or less frequent.
     */
    public static final long UPDATE_INTERVAL_IN_MILLISECONDS = 5 * 1000;

    /**
     * The fastest rate for active location updates. Exact. Updates will never be more frequent
     * than this value.
     */
    public static final long FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS = UPDATE_INTERVAL_IN_MILLISECONDS / 2;

    /**
     * The smallest distance to check for a new location
     */
    public static final float DEFAULT_SMALLEST_DISPLACEMENT_DISTANCE_IN_METERS = 100f;

    private GoogleApiClient googleApiClient;
    private AsyncTaskUpdateLocation asyncTaskUpdateLocation;

    private float mSmallestDisplacementValue = DEFAULT_SMALLEST_DISPLACEMENT_DISTANCE_IN_METERS;
    private FusedLocationProviderClient fusedLocationClient;
    private LocationCallback locationCallback;
    private BluetoothLeScanner bluetoothLeScanner;
    private ScanCallback leScanCallback;
    private Handler mHandler;

    // Stops scanning after 10 seconds.
    private static final long SCAN_PERIOD = 1000;
    private static final long MIN_ALERT_TIME_INTERVEL = 2 * 60 * 1000;
//    public static String TEST_UUID = "E2C56DB5DFFB48D2B060D0F5A71096E0";

    //private SocketClient socketClient;

    @SuppressLint("ForegroundServiceType")
    @Override
    public void onCreate() {
        super.onCreate();
        createNotificationChannel();
        Notification notification = new Notification.Builder(this, CHANNEL_ID)
                .setContentTitle("BleService")
                .setContentText("Running...")
                .setSmallIcon(R.drawable.app_icon)
                .build();
        startForeground(1, notification);
        Log.d(TAG, "onCreate");

        IntentFilter filter2 = new IntentFilter();
        filter2.addAction(ACTION_DISCOVERY_STARTED);
        filter2.addAction(ACTION_DISCOVERY_FINISHED);
        filter2.addAction(ACTION_SCAN_MODE_CHANGED);
        registerReceiver(mBroadcastReceiver2, filter2);
        buildGoogleApiClient();

        //socketClient = new SocketClient();
        //socketClient.initializeSocket();

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(@NonNull LocationResult locationResult) {
                for (Location location : locationResult.getLocations()) {
                    onLocationChanged(location);
                }
            }
        };

        // Initialize BluetoothLeScanner
        final BluetoothManager bluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        BluetoothAdapter mBluetoothAdapter = bluetoothManager.getAdapter();
        mHandler = new Handler();
        if (mBluetoothAdapter != null) {
            bluetoothLeScanner = mBluetoothAdapter.getBluetoothLeScanner();
            leScanCallback = new ScanCallback() {
                @Override
                public void onScanResult(int callbackType, ScanResult result) {
                    super.onScanResult(callbackType, result);
                    // Handle scan result
                    handleScanResult(result);
                }

                @Override
                public void onScanFailed(int errorCode) {
                    super.onScanFailed(errorCode);
                    // Handle scan failure
                }
            };
        } else {
            Log.e(TAG, "Bluetooth is not supported on this device.");
        }
    }

    private void createNotificationChannel() {
        NotificationChannel serviceChannel = new NotificationChannel(
                CHANNEL_ID,
                "BleService Channel",
                NotificationManager.IMPORTANCE_DEFAULT
        );
        NotificationManager manager = getSystemService(NotificationManager.class);
        if (manager != null) {
            manager.createNotificationChannel(serviceChannel);
        }
    }

    private String convertBytesToHex(byte[] bytes) {
        char[] hex = new char[bytes.length * 2];
        for (int i = 0; i < bytes.length; i++) {
            int v = bytes[i] & 0xFF;
            hex[i * 2] = HEX_ARRAY[v >>> 4];
            hex[i * 2 + 1] = HEX_ARRAY[v & 0x0F];
        }

        return new String(hex);
    }

    private void handleScanResult(ScanResult result) {
        if (ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        String msg = "";

        byte[] scanRecord = result.getScanRecord().getBytes();
        BluetoothDevice device = result.getDevice();
        int rssi = result.getRssi();
        for (byte b : scanRecord)
            msg += String.format("%02x ", b);

        msg = msg.replaceAll("\\s+", "");
        ContactFragment.TEST_UUID = ContactFragment.TEST_UUID.toLowerCase();
        ContactFragment.TEST_UUID_PREVIOUS = ContactFragment.TEST_UUID_PREVIOUS.toLowerCase();


         Log.v("rss", "" + rssi);
         Log.v("Device", "" + device);
        int serialNumber = (scanRecord[25] & 0xFF) << 24 | (scanRecord[26] & 0xFF) << 16 | (scanRecord[27] & 0xFF) << 8 | scanRecord[28] & 0xFF;
        Log.e("serial number", "Serial Number is " + serialNumber);
        String UUIDHex = convertBytesToHex(Arrays.copyOfRange(scanRecord, 9, 25));
        Log.d("UUID", UUIDHex);

        if (UUIDHex.equalsIgnoreCase(Constants.NEW_UUID)) {
            Log.d("tigerlight", "found");

            //if (UUIDHex.equals(GELO_UUID)) {
            // Bytes 25 and 26 of the advertisement packet represent
            // the major value
            int major = (scanRecord[25] << 8) | (scanRecord[26]);
            //Log.e("Major", "Serial Number is " + major);


            // Bytes 27 and 28 of the advertisement packet represent
            // the minor
            int minor = ((scanRecord[27] & 0xFF) << 8) | (scanRecord[28] & 0xFF);
            Log.d("TAG", "device" + device + " Serial Number is " + serialNumber + " major" + major + " minor" + minor + " rssi" + rssi);

            Preference.getInstance().savePreferenceData(Constants.Preferences.Keys.UUID_KEY, String.valueOf(UUIDHex));
            Preference.getInstance().savePreferenceData(Constants.Preferences.Keys.MAJOR_KEY, String.valueOf(major));
            Preference.getInstance().savePreferenceData(Constants.Preferences.Keys.MINOR_KEY, String.valueOf(minor));

            if (getActivity() != null) //TODO:  Band-aid (per Rod) for unknown NPE
            {
                sendPushNotification();
                getActivity().startActivity(new Intent(getActivity(), MainActivity.class));
            } else {
                Log.e(TAG, "getActivity() = null");
            }
        } else if (UUIDHex.equalsIgnoreCase(Constants.OLD_UUID) && Constants.LAIRD_BEACON_LABEL.equals(device.getName())) {
            //if (UUIDHex.equalsIgnoreCase(Constants.OLD_UUID)) {

            Preference.getInstance().mSharedPreferences.getString("IsSecond", "true");


            Log.d("tigerlight", "found");


            //if (UUIDHex.equals(GELO_UUID)) {
            // Bytes 25 and 26 of the advertisement packet represent
            // the major value
            int major = (scanRecord[25] << 8) | (scanRecord[26]);
            Log.e("Major", "Serial Number is " + major);

            // Bytes 27 and 28 of the advertisement packet represent
            // the minor value
            int minor = ((scanRecord[27] & 0xFF) << 8) | (scanRecord[28] & 0xFF);
            Log.d("TAG", "device" + device + " Serial Number is " + serialNumber + " major" + major + " minor" + minor + " rssi" + rssi);

            Preference.getInstance().savePreferenceData(Constants.Preferences.Keys.UUID_KEY, String.valueOf(UUIDHex));
            Preference.getInstance().savePreferenceData(Constants.Preferences.Keys.MAJOR_KEY, String.valueOf(major));
            Preference.getInstance().savePreferenceData(Constants.Preferences.Keys.MINOR_KEY, String.valueOf(minor));

            if (getActivity() != null) {
                sendPushNotification();
                getActivity().startActivity(new Intent(getActivity(), MainActivity.class));
            } else {
                Log.e(TAG, "getActivity() = null");
            }
        }
    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "onStartCommand");
//        Utills.writeFile("\n\n" + "AT " + new Date() + "   " + "Service has been started ", this);
        mSmallestDisplacementValue = DEFAULT_SMALLEST_DISPLACEMENT_DISTANCE_IN_METERS;
        if (intent != null) {
            mSmallestDisplacementValue = intent.getFloatExtra(Constants.Extras.SMALLEST_DISPLACEMENT_VALUE, DEFAULT_SMALLEST_DISPLACEMENT_DISTANCE_IN_METERS);
        }

        super.onStartCommand(intent, flags, startId);
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.e(TAG, "onDestroy");
        stopLocationUpdates();
        try {
            unregisterReceiver(mBroadcastReceiver2);
        } catch (IllegalArgumentException e) {
            Log.e(TAG, "Receiver not registered", e);
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onTaskRemoved(Intent rootIntent) {
        super.onTaskRemoved(rootIntent);
        Log.d(TAG, "onTaskRemoved");
    }

    private synchronized void buildGoogleApiClient() {
        googleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
        googleApiClient.connect();
    }


    @Override
    public void onConnected(Bundle connectionHint) {

//        Utills.writeFile("\n\n" + "AT " + new Date() + "   " + "onConnected ", this);
        startListeningForLocationRequests();
    }

    private void startListeningForLocationRequests() {
        final LocationRequest locationRequest = createLocationRequest();
        // Use FusedLocationProviderClient instead of deprecated FusedLocationApi
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, null);
    }

    @Override
    public void onConnectionSuspended(int cause) {
//        Utills.writeFile("\n\n" + "AT " + new Date() + "   " + "onConnectionSuspended", this);
        Log.d(TAG, "Connection to Google API suspended");
        googleApiClient.connect();
    }


    @Override
    public void onConnectionFailed(ConnectionResult result) {
//        Utills.writeFile("\n\n" + "AT " + new Date() + "   " + "onConnectionFailed", this);
        // Refer to the javadoc for ConnectionResult to see what error codes might be returned in
        // onConnectionFailed.
        Log.i(TAG, "Connection failed: ConnectionResult.getErrorCode() = " + result.getErrorCode());
    }

    @Override
    public void onLocationChanged(@NonNull Location location) {
        sendLocationUpdate(location);
    }

    private LocationRequest createLocationRequest() {
        final LocationRequest locationRequest = new LocationRequest();
        locationRequest.setInterval(UPDATE_INTERVAL_IN_MILLISECONDS);
        locationRequest.setFastestInterval(FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS);
        locationRequest.setSmallestDisplacement(mSmallestDisplacementValue);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        /*Log.d(TAG, String.format(Locale.US, "createLocationRequest: smallestDisplacement = %1$f, interval = %2$d", locationRequest.getSmallestDisplacement(), locationRequest.getInterval()));*/

        return locationRequest;
    }


    private void stopLocationUpdates() {
//        Utills.writeFile("\n\n" + "AT " + new Date() + "   " + "stopLocationUpdates", this);
        if (googleApiClient.isConnected()) {
            //Log.d(TAG, "Stopping Location Updates");
            fusedLocationClient.removeLocationUpdates(locationCallback);
            googleApiClient.disconnect();
        }
    }

    private void sendLocationUpdate(final Location location) {
        if (location != null) {
            callLocationUpdateService(location.getLatitude(), location.getLongitude());
            Log.d(TAG, "sendLocationUpdate()----------Location sent" + "lat:" + location.getLatitude() + "long:" + location.getLongitude() + ", accuracy: " + location.getAccuracy());
//            final String cName = getCountryName(location.getLatitude(), location.getLongitude());
//            Preference.getInstance().savePreferenceData(Constant.C_CODE, cName);

            Preference.getInstance().savePreferenceData(Constant.COMMON_LATITUDE, String.valueOf(location.getLatitude()));
            Preference.getInstance().savePreferenceData(Constant.COMMON_LONGITUDE, String.valueOf(location.getLongitude()));
            Preference.getInstance().savePreferenceData(Constant.COMMON_ACCURACY, (int) location.getAccuracy());

//            String cName = Utills.getCountryName(this, Preference.getInstance().mSharedPreferences.getString(Constant.COMMON_LONGITUDE, ""), Preference.getInstance().mSharedPreferences.getString(Constant.COMMON_LONGITUDE, ""));
//            Preference.getInstance().savePreferenceData(Constant.C_CODE, cName);

        } else {
            Log.e(TAG, "sendLocationUpdate()----------Location Null");
        }
    }

    public void callLocationUpdateService(double latitude, double longitude) {
        if (Utills.isInternetConnected(this)) {
            if (asyncTaskUpdateLocation != null && asyncTaskUpdateLocation.getStatus() == AsyncTask.Status.PENDING) {
                asyncTaskUpdateLocation.execute();
            } else if (asyncTaskUpdateLocation == null || asyncTaskUpdateLocation.getStatus() == AsyncTask.Status.FINISHED) {
                asyncTaskUpdateLocation = new AsyncTaskUpdateLocation(latitude, longitude);
                asyncTaskUpdateLocation.execute();
            }
        } else {
//            Utills.writeFile("\n\n" + "AT " + new Date() + "   " + "Internet is not enabled ", this);

//            Toast.makeText(this, "Internet is not connected", Toast.LENGTH_SHORT).show();
        }
    }

    private class AsyncTaskUpdateLocation extends AsyncTask<Void, Void, Void> {
        private WsCallUpdateLocation wsCallUpdateLocation;
        double latitude;
        double longitude;


        public AsyncTaskUpdateLocation(double latitude, double longitude) {
            this.latitude = latitude;
            this.longitude = longitude;
        }

        @Override
        protected Void doInBackground(Void... params) {
            wsCallUpdateLocation = new WsCallUpdateLocation(LocationBroadcastServiceNew.this);
            wsCallUpdateLocation.executeService(latitude, longitude);
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            if (!isCancelled()) {
                if (wsCallUpdateLocation.isSuccess()) {
                    Log.d("@@@", "" + latitude + "long:" + longitude + "userid:" + Preference.getInstance().mSharedPreferences.getString(Constant.USER_ID, ""));
//                    Utills.writeFile("\n\n" + "AT " + new Date() + "   " + "updated service:lat=" + latitude + "log=" + longitude, LocationBroadcastServiceNew.this);

                } else {
                    Log.e("update", "Not Updated");

                }
            }
        }
    }


    private final BroadcastReceiver mBroadcastReceiver2 = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();

            if (action.equals(ACTION_DISCOVERY_STARTED)) {


                serchiBeaconAvailability();
                Log.d("DIS_START", "ACTION_DISCOVERY_STARTED");
            }
            if (action.equals(ACTION_DISCOVERY_FINISHED)) {
                Log.d("DIS_STOP", "ACTION_DISCOVERY_FINISHED");
            }

            if (action.equals(ACTION_SCAN_MODE_CHANGED)) {
                serchiBeaconAvailability();
            }


        }
    };

    /**
     * @author Ambujesh Tripathi - To be used to check the availability of BLE
     */
    @SuppressLint("NewApi")
    private void serchiBeaconAvailability() {
        if (!Utills.isInternetConnected(this)) {
            Toast.makeText(this, getString(R.string.alert_check_connection), Toast.LENGTH_SHORT).show();
            return;
        }

        final BluetoothManager bluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        mHandler = new Handler();
        if (bluetoothManager != null) {
            if (bluetoothLeScanner != null) {
                scanLeDevice(true);
            }
        }
    }

    @SuppressLint("NewApi")
    private void scanLeDevice(final boolean enable) {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        if (enable) {
            // Stops scanning after a pre-defined scan period. Please Note that
            // this period should be same time as it is defined in Recieving
            // list screen for bleReciever.
            mHandler.postDelayed(() -> {
                bluetoothLeScanner.stopScan(leScanCallback);
            }, 2 * 60 * SCAN_PERIOD);
            bluetoothLeScanner.startScan(leScanCallback);
        } else {
            bluetoothLeScanner.stopScan(leScanCallback);
        }
    }

    private AsyncTaskSendPush asyncTaskSendPush;
    private AsyncTaskTestMode asyncTaskTestMode;
    private AsyncTestMode asyncTestMode;

    private void getLatLong() {
        GPSTracker gpsTracker = new GPSTracker(getApplicationContext());
        if (gpsTracker.canGetLocation()) {
            String latitude = String.valueOf(gpsTracker.getLatitude());
            String longitude = String.valueOf(gpsTracker.getLongitude());
            int accuracy = (int) gpsTracker.getAccuracy();

            Log.d(TAG, latitude);
            Log.d(TAG, longitude);
            Log.d(TAG, String.valueOf(accuracy));
        }

    }

    private class PushForReciever extends Thread {
        @Override
        public void run() {
            String userId = Preference.getInstance().mSharedPreferences.getString(Constant.USER_ID, "");
//            new ServerResponseHelper().requestToPush(, longitude, userId, timezoneID);
            callSenDangerServiceRecievingListScreen();
        }

    }

    private class PushForCrowdAlert extends Thread {
        @Override
        public void run() {
//            String userId = Preference.getInstance().mSharedPreferences.getString(C.USER_ID, "");
//            new ServerResponseHelper().requestToPushForCrowdAlert(latitude, longitude, userId, timezoneID);
            callSenDangerServiceRecievingListScreen();

        }
    }


    private class TestModeService extends Thread {
        @Override
        public void run() {
//            String userId = Preference.getInstance().mSharedPreferences.getString(C.USER_ID, "");
//            new ServerResponseHelper().requestToPushForCrowdAlert(latitude, longitude, userId, timezoneID);
//            callTestModeService();
            callTestMode();
        }
    }


    @SuppressLint("NewApi")
    public void sendPushNotification() {
        if (Preference.getInstance().mSharedPreferences.getBoolean(Constant.IS_TEST_MODE, false)) {

            if (Preference.getInstance().mSharedPreferences.getBoolean(Constant.IS_ON_SETTING, false)) {
                return;
            }

            if (!isAMinuteOver()) {
                return;
            }

            Preference.getInstance().savePreferenceData(Constant.LAST_LOG_TIME, "" + System.currentTimeMillis());

            new TestModeService().start();
//            new PushForCrowdAlert().start();

            if (bluetoothLeScanner != null) {
                if (ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.BLUETOOTH_SCAN) == PackageManager.PERMISSION_GRANTED) {
                    bluetoothLeScanner.stopScan(leScanCallback);
                }

            }

            if (!CheckForeground.isInForeGround()) {
                return;
            }
            mHandler.post(() -> Toast.makeText(getApplicationContext(), getString(R.string.TAG_SENDING_ALERT), Toast.LENGTH_SHORT).show());

        } else {

            if (Preference.getInstance().mSharedPreferences.getBoolean(Constant.IS_ON_SETTING, false)) {
                return;
            }

            if (!isAMinuteOver()) {
                return;
            }

            Preference.getInstance().savePreferenceData(Constant.LAST_LOG_TIME, "" + System.currentTimeMillis());

            new PushForReciever().start();
//            new PushForCrowdAlert().start();

            if (bluetoothLeScanner != null) {
                bluetoothLeScanner.stopScan(leScanCallback);
            }

            if (!CheckForeground.isInForeGround()) {
                return;
            }
            mHandler.post(() -> Toast.makeText(getApplicationContext(), getString(R.string.TAG_SENDING_ALERT), Toast.LENGTH_SHORT).show());
        }


    }

    private void callTestModeService() {
        if (Utills.isInternetConnected((Activity) getApplicationContext())) {
            if (asyncTaskTestMode != null && asyncTaskTestMode.getStatus() == AsyncTask.Status.PENDING) {
                asyncTaskTestMode.execute();
            } else if (asyncTaskTestMode == null || asyncTaskTestMode.getStatus() == AsyncTask.Status.FINISHED) {
                asyncTaskTestMode = new AsyncTaskTestMode();
                asyncTaskTestMode.execute();
            }
        } else {
            Utills.displayDialogNormalMessage(getString(R.string.app_name), getString(R.string.TAG_INTERNET_AVAILABILITY), (Activity) getApplicationContext());
        }

    }

    private class AsyncTaskTestMode extends AsyncTask<Void, Void, Void> {
        private WsCallDADTest wsCallDADTest;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            wsCallDADTest = new WsCallDADTest((Activity) getApplicationContext());
        }

        @Override
        protected Void doInBackground(Void... params) {
            wsCallDADTest.executeService();
            return null;
        }


        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            if (!isCancelled()) {
                if (wsCallDADTest.isSuccess()) {
                    // From here do further logic
                    //Toast.makeText((Activity) getApplicationContext(), "Successfully ON Test Mode ", Toast.LENGTH_SHORT).show();
                    Utills.displayDialog((Activity) getApplicationContext(), getString(R.string.app_name), getString(R.string.TAG_TEST_MODE_ON), getString(R.string.ok), "", false, false);
                } else {
                    Toast.makeText((Activity) getApplicationContext(), getString(R.string.TAG_SOME_WENT_WRONG_MSG), Toast.LENGTH_SHORT).show();
                }
            }
        }


    }


    private boolean isAMinuteOver() {
        long currentTimeMillis = System.currentTimeMillis();
        String lastLoggedTimeString = Preference.getInstance().mSharedPreferences.getString(Constant.LAST_LOG_TIME, "");
        long lastLoggedTime = 0;
        try {
            lastLoggedTime = Long.parseLong(lastLoggedTimeString);
        } catch (Exception ignored) {
        }
        if ((currentTimeMillis - lastLoggedTime) <= MIN_ALERT_TIME_INTERVEL) {
            return false;
        }
        Log.d("BleService", "isAMinuteOver()");

        return true;
    }


    private void callSenDangerServiceRecievingListScreen() {

//        asyncTaskSendPush = new AsyncTaskSendCrowdAlert();

        if (asyncTaskSendPush != null && asyncTaskSendPush.getStatus() == AsyncTask.Status.PENDING) {
            asyncTaskSendPush.execute();
        } else if (asyncTaskSendPush == null || asyncTaskSendPush.getStatus() == AsyncTask.Status.FINISHED) {
            asyncTaskSendPush = new AsyncTaskSendPush();
            asyncTaskSendPush.execute();
        }
    }

    private void callTestMode() {

//        asyncTaskSendPush = new AsyncTaskSendCrowdAlert();

        if (asyncTestMode != null && asyncTestMode.getStatus() == AsyncTask.Status.PENDING) {
            asyncTestMode.execute();
        } else if (asyncTestMode == null || asyncTestMode.getStatus() == AsyncTask.Status.FINISHED) {
            asyncTestMode = new AsyncTestMode();
            asyncTestMode.execute();
        }
    }

    private void playAlarmSound() {
        final AssetFileDescriptor audioFile = getApplicationContext().getResources().openRawResourceFd(R.raw.tigerlightsound);

        Thread thread = new Thread(() -> {
            MediaPlayer mediaPlayer = new MediaPlayer();
            try {
                mediaPlayer.setDataSource(audioFile.getFileDescriptor(), audioFile.getStartOffset(), audioFile.getLength());

                mediaPlayer.prepare();
                mediaPlayer.setOnCompletionListener(MediaPlayer::release);
                mediaPlayer.start();
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
        thread.start();
    }


    @SuppressLint("StaticFieldLeak")
    class AsyncTaskSendPush extends AsyncTask<Void, Void, Void> {

        private WsCallSendDanger wsCallSendDanger;
        String lat = Preference.getInstance().mSharedPreferences.getString(Constant.COMMON_LATITUDE, "0.01");
        String log = Preference.getInstance().mSharedPreferences.getString(Constant.COMMON_LONGITUDE, "0.01");
        int accuracy = Preference.getInstance().mSharedPreferences.getInt(Constant.COMMON_ACCURACY, 0);
//        private ProgressDialog progressDialog;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected Void doInBackground(Void... params) {
            wsCallSendDanger = new WsCallSendDanger(getApplicationContext());
            wsCallSendDanger.executeService(Double.parseDouble(lat), Double.parseDouble(log), "", accuracy);
            playAlarmSound();
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            if (!isCancelled()) {
                if (wsCallSendDanger.isSuccess())
                {
                    // From here do further logic

                    if (getApplicationContext() != null) //TODO:  Band-aid (per Rod) for unknown NPE
                    {
                        final Dialog dialog = new Dialog(getApplicationContext(), R.style.AppDialogTheme);
                        dialog.setContentView(R.layout.custom_progress_layout);
                        final TextView tvTitlee = (TextView) dialog.findViewById(R.id.dialog_tvTitlee);
                        final TextView tvMessagee = (TextView) dialog.findViewById(R.id.dialog_tvMessagee);
                        final TextView tvMsgLeve = (TextView) dialog.findViewById(R.id.dialog_tvMsgLevel);
                        final TextView tvPosButtonn = (TextView) dialog.findViewById(R.id.dialog_tvPosButtonn);

                        tvTitlee.setText(getString(R.string.custom_progess_dialog_tv_title));
                        tvMessagee.setText(getString(R.string.custom_progess_dialog_tv_msg));
                        tvPosButtonn.setText(getString(R.string.custom_progess_dialog_tv_ok));

                        Log.d("Al_UUID", Preference.getInstance().mSharedPreferences.getString("UUIDHex", ""));
                        Preference preference = Preference.getInstance();
                        if (preference != null)
                        {
                            if (Preference.getInstance().mSharedPreferences.getString(Constants.Preferences.Keys.NEW_UUID_KEY, "").equalsIgnoreCase(Constants.NEW_UUID))
                            {

                                final String major = preference.mSharedPreferences.getString(Constants.Preferences.Keys.NEW_MAJOR_KEY, "");
                                final String minor = preference.mSharedPreferences.getString(Constants.Preferences.Keys.NEW_MINOR_KEY, "");

                                if (!minor.isEmpty() && !major.isEmpty())
                                {
                                    double doubleminor = Double.parseDouble(minor) / 1000;
                                    double doublemajor = Double.parseDouble(major) / 1000;

                                    if (doubleminor >= -3.0 && doublemajor >= 2.5)
                                    {

                                        tvMsgLeve.setText(getString(R.string.battery_level_good));
                                        tvMsgLeve.setBackgroundColor(getResources().getColor(R.color.color_green));
                                        preference.clearPreferenceItem(Constants.Preferences.Keys.NEW_UUID_KEY);
                                        preference.clearPreferenceItem(Constants.Preferences.Keys.NEW_MAJOR_KEY);
                                        preference.clearPreferenceItem(Constants.Preferences.Keys.NEW_MINOR_KEY);

                                    }
                                    else if (doubleminor >= -2.499 && doublemajor >= 2.0)
                                    {

                                        tvMsgLeve.setText(getString(R.string.battery_level_low));
                                        tvMsgLeve.setBackgroundColor(getResources().getColor(R.color.color_yello));
                                        preference.clearPreferenceItem(Constants.Preferences.Keys.NEW_UUID_KEY);
                                        preference.clearPreferenceItem(Constants.Preferences.Keys.NEW_MAJOR_KEY);
                                        preference.clearPreferenceItem(Constants.Preferences.Keys.NEW_MINOR_KEY);
                                    }
                                    else if (doubleminor < 2.0 && doublemajor < 2.0)
                                    {
                                        tvMsgLeve.setText(getString(R.string.battery_level_replace));
                                        tvMsgLeve.setBackgroundColor(getResources().getColor(R.color.color_alert_red));
                                        preference.clearPreferenceItem(Constants.Preferences.Keys.NEW_UUID_KEY);
                                        preference.clearPreferenceItem(Constants.Preferences.Keys.NEW_MAJOR_KEY);
                                        preference.clearPreferenceItem(Constants.Preferences.Keys.NEW_MINOR_KEY);
                                    }
                                    else
                                    {
                                        tvMsgLeve.setText(getString(R.string.battery_level_good));
                                        tvMsgLeve.setBackgroundColor(getResources().getColor(R.color.color_green));
                                        preference.clearPreferenceItem(Constants.Preferences.Keys.NEW_UUID_KEY);
                                        preference.clearPreferenceItem(Constants.Preferences.Keys.NEW_MAJOR_KEY);
                                        preference.clearPreferenceItem(Constants.Preferences.Keys.NEW_MINOR_KEY);
                                    }


                                }
                            }
                        }

                        tvPosButtonn.setOnClickListener(view -> {
                            dialog.dismiss();
                            sendAlertBroadcast();

                        });

                        dialog.show();
                    }
                }
            }
        }

    }

    class AsyncTestMode extends AsyncTask<Void, Void, Void> {

        private WsCallDADTest wsCallDADTest;
//        private ProgressDialog progressDialog;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            playAlarmSound();
//            progressDialog = new ProgressDialog((Activity) getApplicationContext());
//            progressDialog.show();
//            progressDialog.setContentView(R.layout.progress_layout);
//            progressDialog.setCancelable(false);
        }

        @Override
        protected Void doInBackground(Void... params) {
            wsCallDADTest = new WsCallDADTest(getApplicationContext());
            wsCallDADTest.executeService();
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
//            if (progressDialog != null && progressDialog.isShowing()) {
//                progressDialog.dismiss();
//            }
            if (!isCancelled()) {
                if (wsCallDADTest.isSuccess()) {
                    // From here do further logic
                    sendAlertBroadcast();
                }
            }
        }

    }

    private void sendAlertBroadcast()
    {
        Intent intent = new Intent();
        intent.setAction(Constants.Actions.SENT_ALERT_ACTION);
        sendBroadcast(intent);
    }
}
