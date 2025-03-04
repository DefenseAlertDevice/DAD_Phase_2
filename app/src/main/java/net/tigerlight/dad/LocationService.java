package net.tigerlight.dad;

import static android.bluetooth.BluetoothAdapter.ACTION_DISCOVERY_FINISHED;
import static android.bluetooth.BluetoothAdapter.ACTION_DISCOVERY_STARTED;
import static android.bluetooth.BluetoothAdapter.ACTION_SCAN_MODE_CHANGED;
import static android.content.pm.ServiceInfo.FOREGROUND_SERVICE_TYPE_CONNECTED_DEVICE;
import static android.content.pm.ServiceInfo.FOREGROUND_SERVICE_TYPE_LOCATION;
import static android.content.pm.ServiceInfo.FOREGROUND_SERVICE_TYPE_SHORT_SERVICE;
import static net.tigerlight.dad.util.CheckForeground.getActivity;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;
import androidx.work.Data;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;

import net.tigerlight.dad.registration.activity.MainActivity;
import net.tigerlight.dad.registration.fragment.ContactFragment;
import net.tigerlight.dad.registration.util.Constant;
import net.tigerlight.dad.registration.util.Utills;
import net.tigerlight.dad.webservices.PushAlertWorker;
import net.tigerlight.dad.webservices.TestAlertWorker;
import net.tigerlight.dad.webservices.UpdateLocationWorker;
import net.tigerlight.dad.util.CheckForeground;
import net.tigerlight.dad.util.Constants;
import net.tigerlight.dad.util.Preference;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Arrays;
import java.util.List;
import java.util.TimeZone;

public class LocationService extends Service implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, LocationListener {

    private static final int PERMISSION_REQUEST_CODE = 1002;
    public static final long UPDATE_INTERVAL_IN_MILLISECONDS = 5 * 1000;
    public static final long FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS = UPDATE_INTERVAL_IN_MILLISECONDS / 2;
    public static final float DEFAULT_SMALLEST_DISPLACEMENT_DISTANCE_IN_METERS = 100f;

    public final String TAG = getClass().getSimpleName();
    private static final long MIN_ALERT_TIME_INTERVAL = 2 * 60 * 1000;
    private static final long SCAN_PERIOD = 1000;
    final static char[] HEX_ARRAY = "0123456789ABCDEF".toCharArray();
    private static final String CHANNEL_ID = "default";
    private boolean hasStarted = false;

    private FusedLocationProviderClient fusedLocationClient;

    private LocationCallback locationCallback;
    private BluetoothLeScanner bluetoothLeScanner;
    private ScanCallback leScanCallback;

    private final BroadcastReceiver mBroadcastReceiver2 = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            if (action != null && action.equals(ACTION_DISCOVERY_STARTED)) {
                searchIBeaconAvailability();
                Log.d("DIS_START", "ACTION_DISCOVERY_STARTED");
            }
            if (action != null && action.equals(ACTION_DISCOVERY_FINISHED)) {
                Log.d("DIS_STOP", "ACTION_DISCOVERY_FINISHED");
            }
        }
    };
    private boolean isScanning = false;

    @SuppressLint("ForegroundServiceType")
    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "onCreate");
        createNotificationChannel();
        IntentFilter filter2 = new IntentFilter();
        filter2.addAction(ACTION_DISCOVERY_STARTED);
        filter2.addAction(ACTION_DISCOVERY_FINISHED);
        filter2.addAction(ACTION_SCAN_MODE_CHANGED);
        registerReceiver(mBroadcastReceiver2, filter2);
        startForegroundService();
    }

    private void createNotificationChannel() {
        NotificationChannel serviceChannel = new NotificationChannel(
                CHANNEL_ID,
                "The SoulDefendHER™ App",
                NotificationManager.IMPORTANCE_DEFAULT
        );
        NotificationManager manager = getSystemService(NotificationManager.class);
        if (manager != null) {
            manager.createNotificationChannel(serviceChannel);
        }
    }

    private void startForegroundService() {
        if (hasStarted) {
            return;
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED ||
                    ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED ||
                    ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions((Activity) getApplicationContext(), new String[]{
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.BLUETOOTH_CONNECT,
                        Manifest.permission.BLUETOOTH_SCAN
                }, PERMISSION_REQUEST_CODE);
                return;
            }
        } else {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions((Activity) getApplicationContext(), new String[]{
                        Manifest.permission.ACCESS_FINE_LOCATION
                }, PERMISSION_REQUEST_CODE);
                return;
            }
        }

//        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED ||
//                ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED ||
//                ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED) {
//
//            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
//                ActivityCompat.requestPermissions((Activity) getApplicationContext(), new String[]{
//                        Manifest.permission.ACCESS_FINE_LOCATION,
//                        Manifest.permission.BLUETOOTH_CONNECT,
//                        Manifest.permission.BLUETOOTH_SCAN
//                }, PERMISSION_REQUEST_CODE);
//            } else {
//                ActivityCompat.requestPermissions((Activity) getApplicationContext(), new String[]{
//                        Manifest.permission.ACCESS_FINE_LOCATION
//                }, PERMISSION_REQUEST_CODE);
//            }
//            return;
//        }
        Intent notificationIntent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, PendingIntent.FLAG_IMMUTABLE);

        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("The SoulDefendHER™ App")
                .setContentText("Running location services")
                .setSmallIcon(R.drawable.app_icon)
                .setContentIntent(pendingIntent);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            startForeground(1, notificationBuilder.build(), FOREGROUND_SERVICE_TYPE_LOCATION | FOREGROUND_SERVICE_TYPE_CONNECTED_DEVICE | FOREGROUND_SERVICE_TYPE_SHORT_SERVICE);
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            startForeground(1, notificationBuilder.build(), FOREGROUND_SERVICE_TYPE_LOCATION | FOREGROUND_SERVICE_TYPE_CONNECTED_DEVICE);
        } else {
            startForeground(1, notificationBuilder.build());
        }
        hasStarted = true;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "Service started");
        // Perform your tasks here
        performTasks();
        return START_STICKY;
    }

    private void performTasks() {
        // Your core logic here
        Log.d(TAG, "Performing tasks");
        setupLocationUpdates();
        setupBluetoothScans();
    }

    private void setupBluetoothScans() {
        if (ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        final BluetoothManager bluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        BluetoothAdapter mBluetoothAdapter = bluetoothManager.getAdapter();
        if (mBluetoothAdapter != null) {
            if (bluetoothLeScanner == null) {
                bluetoothLeScanner = mBluetoothAdapter.getBluetoothLeScanner();
            }
            if (mBluetoothAdapter.isEnabled() && leScanCallback == null) {
                leScanCallback = new ScanCallback() {
                    @Override
                    public void onScanResult(int callbackType, ScanResult result) {
                        super.onScanResult(callbackType, result);
                        // Handle scan result
                        handleScanResult(result);
                    }

                    @Override
                    public void onBatchScanResults(List<ScanResult> results) {
                        for (ScanResult result : results) {
                            handleScanResult(result);
                        }
                    }

                    @Override
                    public void onScanFailed(int errorCode) {
                        super.onScanFailed(errorCode);
                        // Handle scan failure
                    }
                };
            }
            searchIBeaconAvailability();
        } else {
            Log.e(TAG, "Bluetooth is not supported on this device.");
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
        StringBuilder msg = new StringBuilder();

        byte[] scanRecord = result.getScanRecord().getBytes();
        BluetoothDevice device = result.getDevice();
        int rssi = result.getRssi();
        for (byte b : scanRecord)
            msg.append(String.format("%02x ", b));

        msg = new StringBuilder(msg.toString().replaceAll("\\s+", ""));
        ContactFragment.TEST_UUID = ContactFragment.TEST_UUID.toLowerCase();
        ContactFragment.TEST_UUID_PREVIOUS = ContactFragment.TEST_UUID_PREVIOUS.toLowerCase();

//        Log.v(TAG, "msg:" + msg);
//        Log.v(TAG, "rss:" + rssi);
//        Log.v(TAG, "device:" + device);
        int serialNumber = (scanRecord[25] & 0xFF) << 24 | (scanRecord[26] & 0xFF) << 16 | (scanRecord[27] & 0xFF) << 8 | scanRecord[28] & 0xFF;
//        Log.e(TAG, "Serial Number is " + serialNumber);
        String UUIDHex = convertBytesToHex(Arrays.copyOfRange(scanRecord, 9, 25));
//        Log.d(TAG, "UUID: " + UUIDHex);

        if (UUIDHex.equalsIgnoreCase(Constants.NEW_UUID)) {
            Log.d(TAG, "found beacon");

            //if (UUIDHex.equals(GELO_UUID)) {
            // Bytes 25 and 26 of the advertisement packet represent
            // the major value
            int major = (scanRecord[25] << 8) | (scanRecord[26]);
            //Log.e("Major", "Serial Number is " + major);


            // Bytes 27 and 28 of the advertisement packet represent
            // the minor
            int minor = ((scanRecord[27] & 0xFF) << 8) | (scanRecord[28] & 0xFF);
//            Log.d("TAG", "device" + device + " Serial Number is " + serialNumber + " major" + major + " minor" + minor + " rssi" + rssi);

            Preference.getInstance().savePreferenceData(Constants.Preferences.Keys.UUID_KEY, UUIDHex);
            Preference.getInstance().savePreferenceData(Constants.Preferences.Keys.IDENTIFIER_KEY, device.getName());
            Preference.getInstance().savePreferenceData(Constants.Preferences.Keys.MAJOR_KEY, String.valueOf(major));
            Preference.getInstance().savePreferenceData(Constants.Preferences.Keys.MINOR_KEY, String.valueOf(minor));

            if (getActivity() != null) //TODO:  Band-aid (per Rod) for unknown NPE
            {
                sendPushNotification();
            } else {
                Log.e(TAG, "getActivity() = null");
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
        if ((currentTimeMillis - lastLoggedTime) <= MIN_ALERT_TIME_INTERVAL) {
            return false;
        }
        Log.d("BleService", "isAMinuteOver()");

        return true;
    }

    public void startScan() {
        try {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED || isScanning) {
                return;
            }
            bluetoothLeScanner.startScan(leScanCallback);
            isScanning = true;
        } catch (Exception ignored) {}
    }

    public void stopScan() {
        try {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED || !isScanning) {
                return;
            }
            bluetoothLeScanner.stopScan(leScanCallback);
            leScanCallback = null;
            isScanning = false;
        } catch (Exception ignored) {

        }
    }

    @SuppressLint("NewApi")
    public void sendPushNotification() {
        new Handler().postDelayed(this::stopScan, 250);
        if (!isAMinuteOver()) {
            return;
        }
        Preference.getInstance().savePreferenceData(Constant.LAST_LOG_TIME, "" + System.currentTimeMillis());
        if (CheckForeground.isInForeGround()) {
            Handler mHandler = new Handler();
            mHandler.post(() -> Toast.makeText(getApplicationContext(), getString(R.string.TAG_SENDING_ALERT), Toast.LENGTH_SHORT).show());
        }
        if (Preference.getInstance().mSharedPreferences.getBoolean(Constant.IS_TEST_MODE, false)) {
            callTestAlert();
        } else {
            callPushAlert();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "Service destroyed");
        // Clean up resources here
        try {
            if (fusedLocationClient != null) {
                fusedLocationClient.removeLocationUpdates(locationCallback);
            }
            unregisterReceiver(mBroadcastReceiver2);
        } catch (IllegalArgumentException e) {
            Log.e(TAG, "Receiver not registered", e);
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @SuppressLint("NewApi")
    private void searchIBeaconAvailability() {
        if (!Utills.isInternetConnected(this)) {
            Toast.makeText(this, getString(R.string.alert_check_connection), Toast.LENGTH_SHORT).show();
            return;
        }

        if (bluetoothLeScanner != null) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED) {
                return;
            }
            Handler mHandler = new Handler();
            mHandler.postDelayed(this::stopScan, 2 * 60 * SCAN_PERIOD);
            mHandler.postDelayed(this::startScan, 500);
        }
    }

    private LocationRequest createLocationRequest() {
        final LocationRequest locationRequest = new LocationRequest();
        locationRequest.setInterval(UPDATE_INTERVAL_IN_MILLISECONDS);
        locationRequest.setFastestInterval(FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS);
        locationRequest.setSmallestDisplacement(DEFAULT_SMALLEST_DISPLACEMENT_DISTANCE_IN_METERS);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        /*Log.d(TAG, String.format(Locale.US, "createLocationRequest: smallestDisplacement = %1$f, interval = %2$d", locationRequest.getSmallestDisplacement(), locationRequest.getInterval()));*/

        return locationRequest;
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
    public void onConnected(Bundle connectionHint) {
        startListeningForLocationRequests();
    }

    @Override
    public void onConnectionSuspended(int cause) {
//        Utills.writeFile("\n\n" + "AT " + new Date() + "   " + "onConnectionSuspended", this);
        Log.d(TAG, "Connection to Google API suspended");
    }

    @Override
    public void onConnectionFailed(ConnectionResult result) {
//        Utills.writeFile("\n\n" + "AT " + new Date() + "   " + "onConnectionFailed", this);
        // Refer to the javadoc for ConnectionResult to see what error codes might be returned in
        // onConnectionFailed.
        Log.i(TAG, "Connection failed: ConnectionResult.getErrorCode() = " + result.getErrorCode());
    }

    private void setupLocationUpdates() {
        if (ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        if (fusedLocationClient == null) {
            fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
            locationCallback = new LocationCallback() {
                @Override
                public void onLocationResult(@NonNull LocationResult locationResult) {
                    for (Location location : locationResult.getLocations()) {
                        onLocationChanged(location);
                    }
                }
            };
            startListeningForLocationRequests();
        }
    }

    @Override
    public void onLocationChanged(@NonNull Location location) {
        callLocationUpdateService(location.getLatitude(), location.getLongitude());
        Log.d(TAG, "sendLocationUpdate()----------Location sent" + "lat:" + location.getLatitude() + "long:" + location.getLongitude() + ", accuracy: " + location.getAccuracy());
        Preference.getInstance().savePreferenceData(Constant.COMMON_LATITUDE, String.valueOf(location.getLatitude()));
        Preference.getInstance().savePreferenceData(Constant.COMMON_LONGITUDE, String.valueOf(location.getLongitude()));
        Preference.getInstance().savePreferenceData(Constant.COMMON_ACCURACY, (int) location.getAccuracy());
    }

    public void callLocationUpdateService(double latitude, double longitude) {
        if (Utills.isInternetConnected(this)) {
            Data inputData = new Data.Builder()
                    .putDouble("latitude", latitude)
                    .putDouble("longitude", longitude)
                    .build();

            OneTimeWorkRequest locationUpdateWorkRequest = new OneTimeWorkRequest.Builder(UpdateLocationWorker.class)
                    .setInputData(inputData)
                    .build();

            WorkManager.getInstance(this).enqueue(locationUpdateWorkRequest);
        }
    }

    public void callTestAlert() {
        if (Utills.isInternetConnected(this)) {
            String uuid = Preference.getInstance().mSharedPreferences.getString(Constants.Preferences.Keys.UUID_KEY, "");
            String identifier = Preference.getInstance().mSharedPreferences.getString(Constants.Preferences.Keys.IDENTIFIER_KEY, "");
            Data inputData = new Data.Builder()
                    .putString("uuid", uuid)
                    .putString("identifier", identifier)
                    .build();

            OneTimeWorkRequest workRequest = new OneTimeWorkRequest.Builder(TestAlertWorker.class)
                    .setInputData(inputData)
                    .build();

            WorkManager.getInstance(this).enqueue(workRequest);
        }
    }

    public void callPushAlert() {
        if (Utills.isInternetConnected(this)) {
            String lat = Preference.getInstance().mSharedPreferences.getString(Constant.COMMON_LATITUDE, "0.01");
            String log = Preference.getInstance().mSharedPreferences.getString(Constant.COMMON_LONGITUDE, "0.01");
            String uuid = Preference.getInstance().mSharedPreferences.getString(Constants.Preferences.Keys.UUID_KEY, "");
            String identifier = Preference.getInstance().mSharedPreferences.getString(Constants.Preferences.Keys.IDENTIFIER_KEY, "");
            int accuracy = Preference.getInstance().mSharedPreferences.getInt(Constant.COMMON_ACCURACY, 0);
            Data inputData = new Data.Builder()
                    .putDouble("latitude", Double.parseDouble(lat))
                    .putDouble("longitude", Double.parseDouble(log))
                    .putString("timezoneId", TimeZone.getDefault().getID())
                    .putString("uuid", uuid)
                    .putString("identifier", identifier)
                    .putInt("accuracy", accuracy)
                    .build();

            OneTimeWorkRequest workRequest = new OneTimeWorkRequest.Builder(PushAlertWorker.class)
                    .setInputData(inputData)
                    .build();

            WorkManager.getInstance(this).enqueue(workRequest);
            sendAlertBroadcast();
        }
    }

    private void sendAlertBroadcast()
    {
        Intent intent = new Intent();
        intent.setAction(Constants.Actions.SENT_ALERT_ACTION);
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("key1", "value1"); // Add your key-value pairs here
            jsonObject.put("key2", "value2");
            // Add other required fields
        } catch (JSONException e) {
            e.printStackTrace();
        }

        // Add the JSON object as a string extra
        intent.putExtra(Constant.JSON_OBJECT, jsonObject.toString());
        sendBroadcast(intent);
    }
}
