//package net.tigerlight.dad.blework;
//
//import android.annotation.SuppressLint;
//import android.app.Dialog;
//import android.app.Service;
//import android.bluetooth.BluetoothAdapter;
//import android.bluetooth.BluetoothManager;
//import android.content.Context;
//import android.content.Intent;
//import android.os.AsyncTask;
//import android.os.Build;
//import android.os.Handler;
//import android.os.IBinder;
//import android.support.annotation.Nullable;
//import android.util.Log;
//import android.view.View;
//import android.widget.TextView;
//import android.widget.Toast;
//
//import com.net.tigerlight.dad.R;
//import net.tigerlight.dad.recievers.BLEHelperNew;
//import net.tigerlight.dad.registration.util.Constant;
//import net.tigerlight.dad.registration.util.Utills;
//import net.tigerlight.dad.settings.webservices.WsCallDADTest;
//import net.tigerlight.dad.settings.webservices.WsCallSendDanger;
//import net.tigerlight.dad.util.CheckForeground;
//import net.tigerlight.dad.util.GPSTracker;
//import net.tigerlight.dad.util.Preference;
//
//import java.util.Calendar;
//import java.util.TimeZone;
//
//import static net.tigerlight.dad.registration.util.Utills.isInternetConnected;
//import static net.tigerlight.dad.util.CheckForeground.getActivity;
//
//public class BleServiceNew extends Service {
//    private String TAG = BleServiceNew.class.getName();
//    private String latitude;
//    private String longitude;
//    private String timezoneID;
//    private Handler handler;
//    private AsyncTaskSendCrowdAlert asyncTaskSendPush;
//    private AsyncTaskTestMode asyncTaskTestMode;
//    private AsyncTestMode asyncTestMode;
//
//
//    public BleServiceNew() {
////        super("MyService");
//        handler = new Handler();
//    }
//
//    @Override
//    public void onCreate() {
//        super.onCreate();
//
//        if (isInternetConnected(getApplicationContext())) {
//            getLatLong();
//            if (!Preference.getInstance().mSharedPreferences.getBoolean(Constant.ISLOGEDD_OUT, false)) {
//                serchiBeaconAvailability();
//            }
//        }
//        Calendar cal = Calendar.getInstance();
//        TimeZone tz = cal.getTimeZone();
//        timezoneID = tz.getID();
//
//
//    }
//
//    private void getLatLong() {
//        GPSTracker gpsTracker = new GPSTracker(getApplicationContext());
//        if (gpsTracker.canGetLocation()) {
//            //latitude = gpsTracker.getLatitude();
//            //longitude = gpsTracker.getLongitude();
//            latitude = Preference.getInstance().mSharedPreferences.getString(Constant.COMMON_LATITUDE, "0.01");
//            longitude = Preference.getInstance().mSharedPreferences.getString(Constant.COMMON_LONGITUDE, "0.01");
//            Log.d(TAG, latitude);
//            Log.d(TAG, longitude);
//        }
//
//    }
//
//    @Override
//    public void onStart(Intent intent, int startId) {
//        super.onStart(intent, startId);
//    }
//
//    @Override
//    public int onStartCommand(Intent intent, int flags, int startId) {
//        super.onStartCommand(intent, flags, startId);
//        return START_STICKY;
//    }
//
//
////    @Override
////    protected void onHandleIntent(Intent intent) {
////
////    }
//
//    @Override
//    public void onDestroy() {
//        super.onDestroy();
//
//        if (asyncTaskSendPush != null && asyncTaskSendPush.getStatus() == AsyncTask.Status.RUNNING) {
//            asyncTaskSendPush.cancel(true);
//        }
//
//    }
//
//    @Nullable
//    @Override
//    public IBinder onBind(Intent intent) {
//        return null;
//    }
//
//
//    private boolean isBleSupported;
//    private BLEHelperNew bleHelper;
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
//        bleHelper = new BLEHelperNew(this);
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
//    private BluetoothAdapter mBluetoothAdapter;
//    private Handler mHandler;
//
//    // Stops scanning after 10 seconds.
//    private static final long SCAN_PERIOD = 1000;
//    private static final long MIN_ALERT_TIME_INTERVEL = 2 * 60 * 1000;
////    public static String TEST_UUID = "E2C56DB5DFFB48D2B060D0F5A71096E0";
//
//    @SuppressLint("NewApi")
//    private void scanLeDevice(final boolean enable) {
//        if (enable) {
//            // Stops scanning after a pre-defined scan period. Please Note that
//            // this period should be same time as it is defined in Recieving
//            // list screen for bleReciever.
//            mHandler.postDelayed(new Runnable() {
//                @Override
//                public void run() {
//                    if (mBluetoothAdapter != null) {
//                        mBluetoothAdapter.stopLeScan(bleHelper.getmLeScanCallback());
//                    }
//                }
//
//            }, 2 * 60 * SCAN_PERIOD);
//            if (mBluetoothAdapter != null) {
//                mBluetoothAdapter.startLeScan(bleHelper.getmLeScanCallback());
//            }
//        } else {
//            if (mBluetoothAdapter != null) {
//                mBluetoothAdapter.stopLeScan(bleHelper.getmLeScanCallback());
//            }
//        }
//    }
//
//    // @SuppressLint("NewApi")
//    // public void sendPushNotification() {
//    // new PushForReciever().start();
//    // new PushForCrowdAlert().start();
//    // if (mBluetoothAdapter != null) {
//    // mBluetoothAdapter.stopLeScan(bleHelper.getmLeScanCallback());
//    // }
//    //
//    // if (!CheckForeground.isInForeGround()) {
//    // return;
//    // }
//    // handler.post(new Runnable() {
//    //
//    // @Override
//    // public void run() {
//    // Toast.makeText(getApplicationContext(), "Sending Alert...",
//    // Toast.LENGTH_SHORT).show();
//    //
//    // }
//    // });
//    //
//    // }
//
//    private class PushForReciever extends Thread {
//        @Override
//        public void run() {
//            String userId = Preference.getInstance().mSharedPreferences.getString(Constant.USER_ID, "");
////            new ServerResponseHelper().requestToPush(, longitude, userId, timezoneID);
//            callSenDangerServiceRecievingListScreen();
//        }
//
//    }
//
//    private class PushForCrowdAlert extends Thread {
//        @Override
//        public void run() {
////            String userId = Preference.getInstance().mSharedPreferences.getString(C.USER_ID, "");
////            new ServerResponseHelper().requestToPushForCrowdAlert(latitude, longitude, userId, timezoneID);
//            callSenDangerServiceRecievingListScreen();
//
//        }
//    }
//
//
//    private class TestModeService extends Thread {
//        @Override
//        public void run() {
////            String userId = Preference.getInstance().mSharedPreferences.getString(C.USER_ID, "");
////            new ServerResponseHelper().requestToPushForCrowdAlert(latitude, longitude, userId, timezoneID);
////            callTestModeService();
//            callTestMode();
//        }
//    }
//
//
//    @SuppressLint("NewApi")
//    public void sendPushNotification() {
//        if (Preference.getInstance().mSharedPreferences.getBoolean(Constant.IS_TEST_MODEE, false)) {
//
//            if (Preference.getInstance().mSharedPreferences.getBoolean(Constant.IS_ON_SETTING, false)) {
//                return;
//            }
//
//            if (!isAMinuteOver()) {
//                return;
//            }
//
//            Preference.getInstance().savePreferenceData(Constant.LAST_LOG_TIME, "" + System.currentTimeMillis());
//
//            new TestModeService().start();
////            new PushForCrowdAlert().start();
//
//            if (mBluetoothAdapter != null) {
//                mBluetoothAdapter.stopLeScan(bleHelper.getmLeScanCallback());
//
//            }
//
//            if (!CheckForeground.isInForeGround()) {
//                return;
//            }
//            handler.post(new Runnable() {
//
//                @Override
//                public void run() {
//                    Toast.makeText(getApplicationContext(), getString(R.string.TAG_SENDING_ALERT), Toast.LENGTH_SHORT).show();
//
//                }
//            });
//
//        } else {
//
//            if (Preference.getInstance().mSharedPreferences.getBoolean(Constant.IS_ON_SETTING, false)) {
//                return;
//            }
//
//            if (!isAMinuteOver()) {
//                return;
//            }
//
//            Preference.getInstance().savePreferenceData(Constant.LAST_LOG_TIME, "" + System.currentTimeMillis());
//
//            new PushForReciever().start();
//            new PushForCrowdAlert().start();
//
//            if (mBluetoothAdapter != null) {
//                mBluetoothAdapter.stopLeScan(bleHelper.getmLeScanCallback());
//            }
//
//            if (!CheckForeground.isInForeGround()) {
//                return;
//            }
//            handler.post(new Runnable() {
//
//                @Override
//                public void run() {
//                    Toast.makeText(getApplicationContext(), getString(R.string.TAG_SENDING_ALERT), Toast.LENGTH_SHORT).show();
//
//                }
//            });
//        }
//
//
//    }
//
//    private void callTestModeService() {
//        if (Utills.isInternetConnected(getActivity())) {
//            if (asyncTaskTestMode != null && asyncTaskTestMode.getStatus() == AsyncTask.Status.PENDING) {
//                asyncTaskTestMode.execute();
//            } else if (asyncTaskTestMode == null || asyncTaskTestMode.getStatus() == AsyncTask.Status.FINISHED) {
//                asyncTaskTestMode = new AsyncTaskTestMode();
//                asyncTaskTestMode.execute();
//            }
//        } else {
//            Utills.displayDialogNormalMessage(getString(R.string.app_name), getString(R.string.TAG_INTERNET_AVAILABILITY), getActivity());
//        }
//
//    }
//
//    private class AsyncTaskTestMode extends AsyncTask<Void, Void, Void> {
//        private WsCallDADTest wsCallDADTest;
//
//        @Override
//        protected void onPreExecute() {
//            super.onPreExecute();
//            wsCallDADTest = new WsCallDADTest(getActivity());
//        }
//
//        @Override
//        protected Void doInBackground(Void... params) {
//            wsCallDADTest.executeService();
//            return null;
//        }
//
//
//        @Override
//        protected void onPostExecute(Void aVoid) {
//            super.onPostExecute(aVoid);
//            if (!isCancelled()) {
//                if (wsCallDADTest.isSuccess()) {
//                    // From here do further logic
//                    //Toast.makeText(getActivity(), "Successfully ON Test Mode ", Toast.LENGTH_SHORT).show();
//                    Utills.displayDialog(getActivity(), getString(R.string.app_name), getString(R.string.TAG_TEST_MODE_ON), getString(R.string.ok), "", false, false);
//                } else {
//                    Toast.makeText(getActivity(), getString(R.string.TAG_SOME_WENT_WRONG_MSG), Toast.LENGTH_SHORT).show();
//                }
//            }
//        }
//
//
//    }
//
//
//    private boolean isAMinuteOver() {
//        long currentTimeMillis = System.currentTimeMillis();
//        String lastLoggedTimeString = Preference.getInstance().mSharedPreferences.getString(Constant.LAST_LOG_TIME, "");
//        long lastLoggedTime = 0;
//        try {
//            lastLoggedTime = Long.parseLong(lastLoggedTimeString);
//        } catch (Exception e) {
//        }
//        if ((currentTimeMillis - lastLoggedTime) <= MIN_ALERT_TIME_INTERVEL) {
//            return false;
//        }
//        System.out.println("BleService.isAMinuteOver()");
//        return true;
//    }
//
//
//    private void callSenDangerServiceRecievingListScreen() {
//
////        asyncTaskSendPush = new AsyncTaskSendCrowdAlert();
//
//        if (asyncTaskSendPush != null && asyncTaskSendPush.getStatus() == AsyncTask.Status.PENDING) {
//            asyncTaskSendPush.execute();
//        } else if (asyncTaskSendPush == null || asyncTaskSendPush.getStatus() == AsyncTask.Status.FINISHED) {
//            asyncTaskSendPush = new AsyncTaskSendCrowdAlert();
//            asyncTaskSendPush.execute();
//        }
//    }
//
//    private void callTestMode() {
//
////        asyncTaskSendPush = new AsyncTaskSendCrowdAlert();
//
//        if (asyncTestMode != null && asyncTestMode.getStatus() == AsyncTask.Status.PENDING) {
//            asyncTestMode.execute();
//        } else if (asyncTestMode == null || asyncTestMode.getStatus() == AsyncTask.Status.FINISHED) {
//            asyncTestMode = new AsyncTestMode();
//            asyncTestMode.execute();
//        }
//    }
//
//
//    class AsyncTaskSendCrowdAlert extends AsyncTask<Void, Void, Void> {
//
//        private WsCallSendDanger wsCallSendDanger;
////        private ProgressDialog progressDialog;
//
//        @Override
//        protected void onPreExecute() {
//            super.onPreExecute();
////            progressDialog = new ProgressDialog(getActivity());
////            progressDialog.show();
////            progressDialog.setContentView(R.layout.progress_layout);
////            progressDialog.setCancelable(false);
//        }
//
//        @Override
//        protected Void doInBackground(Void... params) {
//            wsCallSendDanger = new WsCallSendDanger(BleServiceNew.this);
//            wsCallSendDanger.executeService(Double.parseDouble(latitude), Double.parseDouble(longitude), timezoneID);
//            return null;
//        }
//
//        @Override
//        protected void onPostExecute(Void aVoid) {
//            super.onPostExecute(aVoid);
////            if (progressDialog != null && progressDialog.isShowing()) {
////                progressDialog.dismiss();
////            }
//            if (!isCancelled()) {
//                if (wsCallSendDanger.isSuccess()) {
//                    // From here do further logic
//
//                    final Dialog dialog = new Dialog(getActivity(), R.style.AppDialogTheme);
//                    dialog.setContentView(R.layout.custom_progress_layout);
//                    final TextView tvTitlee = (TextView) dialog.findViewById(R.id.dialog_tvTitlee);
//                    final TextView tvMessagee = (TextView) dialog.findViewById(R.id.dialog_tvMessagee);
//                    final TextView tvMsgLeve = (TextView) dialog.findViewById(R.id.dialog_tvMsgLevel);
//                    final TextView tvPosButtonn = (TextView) dialog.findViewById(R.id.dialog_tvPosButtonn);
//
//
//                    tvTitlee.setText(getString(R.string.custom_progess_dialog_tv_title));
//                    tvMessagee.setText(getString(R.string.custom_progess_dialog_tv_msg));
//                    tvPosButtonn.setText(getString(R.string.custom_progess_dialog_tv_ok));
//
////
////                    float minor = (float) -4.0;
////                    float major = (float) 2.9;
////                    if (minor >= -3.0 && major >= 2.5) {
////
////                        tvMsgLeve.setText("GOOD The SoulDefendHER™ BATTERY");
////                        tvMsgLeve.setBackgroundColor(getResources().getColor(R.color.color_green));
////                    } else if (minor >= -2.499 && major >= 2.0) {
////
////                        tvMsgLeve.setText("LOW The SoulDefendHER™ BATTERY");
////                        tvMsgLeve.setBackgroundColor(getResources().getColor(R.color.color_yello));
////                    } else {
////                        tvMsgLeve.setText("REPLACE The SoulDefendHER™ BATTERY");
////                        tvMsgLeve.setBackgroundColor(getResources().getColor(R.color.color_alert_red));
////                    }
//
//
//                    final int temp = 3;
//
//                    if (temp >= -3.0 && temp >= 2.5) {
//
//                        tvMsgLeve.setText("GOOD The SoulDefendHER™ BATTERY");
//                        tvMsgLeve.setBackgroundColor(getResources().getColor(R.color.color_green));
//                    } else if (temp >= -2.499 && temp >= 2.0) {
//
//                        tvMsgLeve.setText("LOW The SoulDefendHER™ BATTERY");
//                        tvMsgLeve.setBackgroundColor(getResources().getColor(R.color.color_yello));
//                    } else if (temp < 2.0) {
//                        tvMsgLeve.setText("REPLACE The SoulDefendHER™ BATTERY");
//                        tvMsgLeve.setBackgroundColor(getResources().getColor(R.color.color_alert_red));
//                    }
//
//
//                    tvPosButtonn.setOnClickListener(new View.OnClickListener() {
//                        @Override
//                        public void onClick(View view) {
//                            dialog.dismiss();
//
//
//                        }
//                    });
//
//
//                    dialog.show();
//                }
//            }
//        }
//
//    }
//
//    class AsyncTestMode extends AsyncTask<Void, Void, Void> {
//
//        private WsCallDADTest wsCallDADTest;
////        private ProgressDialog progressDialog;
//
//        @Override
//        protected void onPreExecute() {
//            super.onPreExecute();
////            progressDialog = new ProgressDialog(getActivity());
////            progressDialog.show();
////            progressDialog.setContentView(R.layout.progress_layout);
////            progressDialog.setCancelable(false);
//        }
//
//        @Override
//        protected Void doInBackground(Void... params) {
//            wsCallDADTest = new WsCallDADTest(BleServiceNew.this);
//            wsCallDADTest.executeService();
//            return null;
//        }
//
//        @Override
//        protected void onPostExecute(Void aVoid) {
//            super.onPostExecute(aVoid);
////            if (progressDialog != null && progressDialog.isShowing()) {
////                progressDialog.dismiss();
////            }
//            if (!isCancelled()) {
//                if (wsCallDADTest.isSuccess()) {
//                    // From here do further logic
//                }
//            }
//        }
//
//    }
//
////    private void restartActivity() {
////        Intent intent = getActivity().getIntent();
////        getActivity().finish();
////        intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
////        getActivity().startActivity(intent);
////        getActivity().overridePendingTransition(0, 0);
////    }
//
//
//}
//
//
//
//
//
