package net.tigerlight.dad.blework;

import net.tigerlight.dad.R;
import net.tigerlight.dad.recievers.BLEHelper;
import net.tigerlight.dad.registration.util.Constant;
import net.tigerlight.dad.registration.util.Utills;
import net.tigerlight.dad.webservices.WsCallDADTest;
import net.tigerlight.dad.webservices.WsCallSendDanger;
import net.tigerlight.dad.util.CheckForeground;
import net.tigerlight.dad.util.Constants;
import net.tigerlight.dad.util.GPSTracker;
import net.tigerlight.dad.util.Preference;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Dialog;
import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.os.IBinder;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Calendar;
import java.util.List;
import java.util.TimeZone;

import static net.tigerlight.dad.registration.util.Utills.isInternetConnected;
import static net.tigerlight.dad.util.CheckForeground.getActivity;

import androidx.core.app.ActivityCompat;

public class BleService extends IntentService {
    private static final String CHANNEL_ID = "default";
    private String TAG = BleService.class.getName();
    private String latitude;
    private String longitude;
    private int accuracy;
    private String timezoneID;
    private Handler handler;
    private AsyncTaskSendCrowdAlert asyncTaskSendPush;
    private AsyncTaskTestMode asyncTaskTestMode;
    private AsyncTestMode asyncTestMode;
    private BluetoothLeScanner bluetoothLeScanner;
    private ScanCallback leScanCallback;

    public BleService() {
        super("MyService");
        handler = new Handler(Looper.getMainLooper());
    }

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
        if (isInternetConnected(getApplicationContext())) {
            getLatLong();
            if (!Preference.getInstance().mSharedPreferences.getBoolean(Constant.ISLOGEDD_OUT, false)) {
                serchiBeaconAvailability();
            }
        }
        Calendar cal = Calendar.getInstance();
        TimeZone tz = cal.getTimeZone();
        timezoneID = tz.getID();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);
        Log.d(TAG, "StartCommand");
        return START_STICKY;
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

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private void getLatLong() {
        GPSTracker gpsTracker = new GPSTracker(getApplicationContext());
        if (gpsTracker.canGetLocation()) {
            latitude = String.valueOf(gpsTracker.getLatitude());
            longitude = String.valueOf(gpsTracker.getLongitude());
            accuracy = (int) gpsTracker.getAccuracy();

            Log.d(TAG, latitude);
            Log.d(TAG, longitude);
            Log.d(TAG, String.valueOf(accuracy));
        }
    }

    @Override
    public void onStart(Intent intent, int startId) {
        super.onStart(intent, startId);
        Log.d(TAG, "onStart");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        // Handle the intent here
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        if (asyncTaskSendPush != null && asyncTaskSendPush.getStatus() == AsyncTask.Status.RUNNING) {
            asyncTaskSendPush.cancel(true);
        }
        if (asyncTaskTestMode != null && asyncTaskTestMode.getStatus() == AsyncTask.Status.RUNNING) {
            asyncTaskTestMode.cancel(true);
        }
        if (asyncTestMode != null && asyncTestMode.getStatus() == AsyncTask.Status.RUNNING) {
            asyncTestMode.cancel(true);
        }
    }

    private boolean isBleSupported;
    private BLEHelper bleHelper;

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
        mBluetoothAdapter = bluetoothManager.getAdapter();
        isBleSupported = true;
        bleHelper = new BLEHelper(this);
        mHandler = new Handler(Looper.getMainLooper());

        if (mBluetoothAdapter != null && isBleSupported && !mBluetoothAdapter.isEnabled()) {
            return;
        }

        bluetoothLeScanner = mBluetoothAdapter.getBluetoothLeScanner();
        leScanCallback = new ScanCallback() {
            @Override
            public void onScanResult(int callbackType, ScanResult result) {
                super.onScanResult(callbackType, result);
                // Handle scan result
            }

            @Override
            public void onBatchScanResults(List<ScanResult> results) {
                super.onBatchScanResults(results);
                // Handle batch scan results
            }

            @Override
            public void onScanFailed(int errorCode) {
                super.onScanFailed(errorCode);
                // Handle scan failure
            }
        };

        scanLeDevice(true);
    }

    private BluetoothAdapter mBluetoothAdapter;
    private Handler mHandler;

    // Stops scanning after 10 seconds.
    private static final long SCAN_PERIOD = 1000;
    private static final long MIN_ALERT_TIME_INTERVEL = 2 * 60 * 1000;

    @SuppressLint("NewApi")
    private void scanLeDevice(final boolean enable) {
        if (enable) {
            mHandler.postDelayed(() -> {
                if (bluetoothLeScanner != null) {
                    if (ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED) {
                        // Handle the case where permission is not granted
                        return;
                    }
                    bluetoothLeScanner.stopScan(leScanCallback);
                }
            }, 2 * 60 * SCAN_PERIOD);

            if (ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.BLUETOOTH_SCAN) == PackageManager.PERMISSION_GRANTED && bluetoothLeScanner != null) {
                ScanSettings settings = new ScanSettings.Builder()
                        .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
                        .build();
                bluetoothLeScanner.startScan(null, settings, leScanCallback);
            }
        } else {
            if (ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.BLUETOOTH_SCAN) == PackageManager.PERMISSION_GRANTED && bluetoothLeScanner != null) {
                bluetoothLeScanner.stopScan(leScanCallback);
            }
        }
    }

    private class PushForReciever extends Thread {
        @Override
        public void run() {
            String userId = Preference.getInstance().mSharedPreferences.getString(Constant.USER_ID, "");
            callSenDangerServiceRecievingListScreen();
        }
    }

    private class PushForCrowdAlert extends Thread {
        @Override
        public void run() {
            callSenDangerServiceRecievingListScreen();
        }
    }

    private class TestModeService extends Thread {
        @Override
        public void run() {
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

            if (mBluetoothAdapter != null) {
                if (ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.BLUETOOTH_SCAN) == PackageManager.PERMISSION_GRANTED) {
                    mBluetoothAdapter.stopLeScan(bleHelper.getmLeScanCallback());
                }
            }

            if (!CheckForeground.isInForeGround()) {
                return;
            }
            handler.post(() -> Toast.makeText(getApplicationContext(), getString(R.string.TAG_SENDING_ALERT), Toast.LENGTH_SHORT).show());

        } else {

            if (Preference.getInstance().mSharedPreferences.getBoolean(Constant.IS_ON_SETTING, false)) {
                return;
            }

            if (!isAMinuteOver()) {
                return;
            }

            Preference.getInstance().savePreferenceData(Constant.LAST_LOG_TIME, "" + System.currentTimeMillis());

            new PushForReciever().start();

            if (mBluetoothAdapter != null) {
                mBluetoothAdapter.stopLeScan(bleHelper.getmLeScanCallback());
            }

            if (!CheckForeground.isInForeGround()) {
                return;
            }
            handler.post(() -> Toast.makeText(getApplicationContext(), getString(R.string.TAG_SENDING_ALERT), Toast.LENGTH_SHORT).show());
        }
    }

    private void callTestModeService() {
        if (Utills.isInternetConnected(getActivity())) {
            if (asyncTaskTestMode != null && asyncTaskTestMode.getStatus() == AsyncTask.Status.PENDING) {
                asyncTaskTestMode.execute();
            } else if (asyncTaskTestMode == null || asyncTaskTestMode.getStatus() == AsyncTask.Status.FINISHED) {
                asyncTaskTestMode = new AsyncTaskTestMode();
                asyncTaskTestMode.execute();
            }
        } else {
            Utills.displayDialogNormalMessage(getString(R.string.app_name), getString(R.string.TAG_INTERNET_AVAILABILITY), getActivity());
        }
    }

    private class AsyncTaskTestMode extends AsyncTask<Void, Void, Void> {
        private WsCallDADTest wsCallDADTest;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            wsCallDADTest = new WsCallDADTest(getActivity());
            if (getActivity() == null) {
                cancel(true);
            }
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
                    if (getActivity() != null) {
                        Utills.displayDialog(getActivity(), getString(R.string.app_name), getString(R.string.TAG_TEST_MODE_ON), getString(R.string.ok), "", false, false);
                    } else {
                        Log.e(TAG, "Activity is null");
                    }
                } else {
                    Toast.makeText(getApplicationContext(), getString(R.string.TAG_SOME_WENT_WRONG_MSG), Toast.LENGTH_SHORT).show();
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
        } catch (Exception e) {
            Log.e(TAG, "Error parsing last logged time", e);
        }
        if ((currentTimeMillis - lastLoggedTime) <= MIN_ALERT_TIME_INTERVEL) {
            return false;
        }
        Log.d("BleService", "isAMinuteOver()");

        return true;
    }

    private void callSenDangerServiceRecievingListScreen() {
        if (asyncTaskSendPush != null && asyncTaskSendPush.getStatus() == AsyncTask.Status.PENDING) {
            asyncTaskSendPush.execute();
        } else if (asyncTaskSendPush == null || asyncTaskSendPush.getStatus() == AsyncTask.Status.FINISHED) {
            asyncTaskSendPush = new AsyncTaskSendCrowdAlert();
            asyncTaskSendPush.execute();
        }
    }

    private void callTestMode() {
        if (asyncTestMode != null && asyncTestMode.getStatus() == AsyncTask.Status.PENDING) {
            asyncTestMode.execute();
        } else if (asyncTestMode == null || asyncTestMode.getStatus() == AsyncTask.Status.FINISHED) {
            asyncTestMode = new AsyncTestMode();
            asyncTestMode.execute();
        }
    }

    class AsyncTaskSendCrowdAlert extends AsyncTask<Void, Void, Void> {
        private WsCallSendDanger wsCallSendDanger;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected Void doInBackground(Void... params) {
            wsCallSendDanger = new WsCallSendDanger(BleService.this);
            wsCallSendDanger.executeService(Double.parseDouble(latitude), Double.parseDouble(longitude), timezoneID, accuracy);
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            if (!isCancelled()) {
                if (wsCallSendDanger.isSuccess()) {
                    if (getActivity() != null) {
                        final Dialog dialog = new Dialog(getActivity(), R.style.AppDialogTheme);
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
                        if (preference != null) {
                            if (Preference.getInstance().mSharedPreferences.getString(Constants.Preferences.Keys.NEW_UUID_KEY, "").equalsIgnoreCase(Constants.NEW_UUID)) {
                                final String major = preference.mSharedPreferences.getString(Constants.Preferences.Keys.NEW_MAJOR_KEY, "");
                                final String minor = preference.mSharedPreferences.getString(Constants.Preferences.Keys.NEW_MINOR_KEY, "");

                                if (!minor.equals("") && !major.equals("")) {
                                    double doubleminor = Double.parseDouble(minor) / 1000;
                                    double doublemajor = Double.parseDouble(major) / 1000;

                                    if (doubleminor >= -3.0 && doublemajor >= 2.5) {
                                        tvMsgLeve.setText(getString(R.string.battery_level_good));
                                        tvMsgLeve.setBackgroundColor(getResources().getColor(R.color.color_green));
                                        preference.clearPreferenceItem(Constants.Preferences.Keys.NEW_UUID_KEY);
                                        preference.clearPreferenceItem(Constants.Preferences.Keys.NEW_MAJOR_KEY);
                                        preference.clearPreferenceItem(Constants.Preferences.Keys.NEW_MINOR_KEY);
                                    } else if (doubleminor >= -2.499 && doublemajor >= 2.0) {
                                        tvMsgLeve.setText(getString(R.string.battery_level_low));
                                        tvMsgLeve.setBackgroundColor(getResources().getColor(R.color.color_yello));
                                        preference.clearPreferenceItem(Constants.Preferences.Keys.NEW_UUID_KEY);
                                        preference.clearPreferenceItem(Constants.Preferences.Keys.NEW_MAJOR_KEY);
                                        preference.clearPreferenceItem(Constants.Preferences.Keys.NEW_MINOR_KEY);
                                    } else if (doubleminor < 2.0 && doublemajor < 2.0) {
                                        tvMsgLeve.setText(getString(R.string.battery_level_replace));
                                        tvMsgLeve.setBackgroundColor(getResources().getColor(R.color.color_alert_red));
                                        preference.clearPreferenceItem(Constants.Preferences.Keys.NEW_UUID_KEY);
                                        preference.clearPreferenceItem(Constants.Preferences.Keys.NEW_MAJOR_KEY);
                                        preference.clearPreferenceItem(Constants.Preferences.Keys.NEW_MINOR_KEY);
                                    } else {
                                        tvMsgLeve.setText(getString(R.string.battery_level_good));
                                        tvMsgLeve.setBackgroundColor(getResources().getColor(R.color.color_green));
                                        preference.clearPreferenceItem(Constants.Preferences.Keys.NEW_UUID_KEY);
                                        preference.clearPreferenceItem(Constants.Preferences.Keys.NEW_MAJOR_KEY);
                                        preference.clearPreferenceItem(Constants.Preferences.Keys.NEW_MINOR_KEY);
                                    }
                                }
                            } else if (Preference.getInstance().mSharedPreferences.getString(Constants.Preferences.Keys.OLD_UUID_KEY, "").equalsIgnoreCase(Constants.OLD_UUID)) {
                                final String major = preference.mSharedPreferences.getString(Constants.Preferences.Keys.OLD_MAJOR_KEY, "");
                                final String minor = preference.mSharedPreferences.getString("old_minor", "");

                                if (!minor.equals("") && !major.equals("")) {
                                    double doubleminor = Double.parseDouble(minor) / 1000;
                                    double doublemajor = Double.parseDouble(major) / 1000;

                                    if (doubleminor >= -3.0 && doublemajor >= 2.5) {
                                    } else if (doubleminor >= -2.499 && doublemajor >= 2.0) {
                                    } else if (doubleminor < 2.0 && doublemajor < 2.0) {
                                    } else {
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

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected Void doInBackground(Void... params) {
            wsCallDADTest = new WsCallDADTest(BleService.this);
            wsCallDADTest.executeService();
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            if (!isCancelled()) {
                if (wsCallDADTest.isSuccess()) {
                    sendAlertBroadcast();
                }
            }
        }
    }

    private void sendAlertBroadcast() {
        Intent intent = new Intent();
        intent.setAction(Constants.Actions.SENT_ALERT_ACTION);
        sendBroadcast(intent);
    }
}
