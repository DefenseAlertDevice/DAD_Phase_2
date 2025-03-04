//package net.tigerlight.dad.blework;
//
//import android.annotation.SuppressLint;
//import android.app.Service;
//import android.bluetooth.BluetoothAdapter;
//import android.bluetooth.BluetoothManager;
//import android.content.Context;
//import android.content.Intent;
//import android.os.Build;
//import android.os.Handler;
//import android.os.IBinder;
//import android.support.annotation.Nullable;
//import android.util.Log;
//import android.widget.Toast;
//
//import net.tigerlight.dad.R;
//import net.tigerlight.dad.recievers.BLEHelper;
//import net.tigerlight.dad.registration.util.Constant;
//import net.tigerlight.dad.registration.util.Utills;
//import net.tigerlight.dad.util.Preference;
//
//import java.util.Calendar;
//import java.util.TimeZone;
//
//import static net.tigerlight.dad.registration.util.Utills.isInternetConnected;
//
///**
// * Created by indianic on 17/04/17.
// */
//
//public class BleServiceScan extends Service {
//
//    private String TAG=BleServiceScan.class.getName();
//    private String timezoneID;
//    private Handler handler;
//
//
//    public BleServiceScan() {
//
//        handler = new Handler();
//    }
//
//    @Override
//    public void onCreate() {
//        super.onCreate();
//        Log.e(TAG, "onCreate");
//
//
//        if (isInternetConnected(getApplicationContext())) {
//
//            if (!Preference.getInstance().mSharedPreferences.getBoolean(Constant.ISLOGEDD_OUT, false)) {
//                serchiBeaconAvailability();
//            }
//        }
//        Calendar cal = Calendar.getInstance();
//        TimeZone tz = cal.getTimeZone();
//        timezoneID = tz.getID();
//
//
//
//        //socketClient = new SocketClient();
//        //socketClient.initializeSocket();
//    }
//
//
//    @Override
//    public int onStartCommand(Intent intent, int flags, int startId) {
//        Log.e(TAG, "onStartCommand");
////        Utills.writeFile("\n\n" + "AT " + new Date() + "   " + "Service has been started ", this);
//        return super.onStartCommand(intent, flags, startId);
//
//    }
//
//
//    @Override
//    public void onDestroy() {
//        super.onDestroy();
//        Log.e(TAG, "onDestroy");
//
//    }
//
//    @Nullable
//    @Override
//    public IBinder onBind(Intent intent) {
//        return null;
//    }
//    private boolean isBleSupported;
//    private BLEHelper bleHelper;
//
//    /**
//     * @author Ambujesh Tripathi - To be used to check the availability of BLE
//     */
//    @SuppressLint("NewApi")
//    private void serchiBeaconAvailability() {
//        if (!Utills.isInternetConnected(this)) {
//            Toast.makeText(this, getString(R.string.alert_check_connection), Toast.LENGTH_SHORT).show();
//            return;
//        }
//
//        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.JELLY_BEAN_MR1) {
//            isBleSupported = false;
//            return;
//        }
//
//        final BluetoothManager bluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
//        mBluetoothAdapter = bluetoothManager.getAdapter();
//        isBleSupported = true;
//        bleHelper = new BLEHelper(this);
//        mHandler = new Handler();
//
//
//        if (mBluetoothAdapter != null && isBleSupported && !mBluetoothAdapter.isEnabled()) {
//            return;
//        }
//
//        scanLeDevice(true);
//    }
//
//
//    private BluetoothAdapter mBluetoothAdapter;
//    private Handler mHandler;
//    // Stops scanning after 10 seconds.
//    private static final long SCAN_PERIOD = 1000;
//    private static final long MIN_ALERT_TIME_INTERVEL = 2 * 60 * 1000;
////    public static String TEST_UUID = "E2C56DB5DFFB48D2B060D0F5A71096E0";
//@SuppressLint("NewApi")
//private void scanLeDevice(final boolean enable) {
//    if (enable) {
//        // Stops scanning after a pre-defined scan period. Please Note that
//        // this period should be same time as it is defined in Recieving
//        // list screen for bleReciever.
//        mHandler.postDelayed(new Runnable() {
//            @Override
//            public void run() {
//                if (mBluetoothAdapter != null) {
//                    mBluetoothAdapter.stopLeScan(bleHelper.getmLeScanCallback());
//                }
//            }
//
//        }, 2 * 60 * SCAN_PERIOD);
//        if (mBluetoothAdapter != null) {
//            mBluetoothAdapter.startLeScan(bleHelper.getmLeScanCallback());
//        }
//    } else {
//        if (mBluetoothAdapter != null) {
//            mBluetoothAdapter.stopLeScan(bleHelper.getmLeScanCallback());
//        }
//    }
//}
//
//
//}
//
//
