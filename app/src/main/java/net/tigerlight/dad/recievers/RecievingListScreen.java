//package net.tigerlight.dad.recievers;
//
//import android.annotation.SuppressLint;
//import android.app.AlertDialog;
//import android.app.ProgressDialog;
//import android.bluetooth.BluetoothAdapter;
//import android.bluetooth.BluetoothManager;
//import android.content.Context;
//import android.content.DialogInterface;
//import android.content.Intent;
//import android.location.LocationManager;
//import android.os.AsyncTask;
//import android.os.Build;
//import android.os.Bundle;
//import android.os.Handler;
//import android.view.View;
//import android.view.View.OnClickListener;
//import android.widget.AdapterView;
//import android.widget.AdapterView.OnItemClickListener;
//import android.widget.ImageView;
//import android.widget.ListView;
//import android.widget.TextView;
//import android.widget.Toast;
//
//import net.tigerlight.dad.R;
//import net.tigerlight.dad.home.BaseActivity;
//import net.tigerlight.dad.registration.adapter.RecieveElementAdapter;
//import net.tigerlight.dad.settings.webservices.WsCallDeleteContact;
//import net.tigerlight.dad.settings.webservices.WsCallGetAlertCount;
//import net.tigerlight.dad.settings.webservices.WsCallGetAllContacts;
//import net.tigerlight.dad.settings.webservices.WsCallSendDanger;
//import net.tigerlight.dad.util.BitMapHelper;
//import net.tigerlight.dad.util.CheckForeground;
//import net.tigerlight.dad.util.NetworkAvailability;
//import net.tigerlight.dad.util.Preference;
//import net.tigerlight.dad.util.WsConstants;
//import org.json.JSONArray;
//import org.json.JSONException;
//import org.json.JSONObject;
//import java.util.Calendar;
//import java.util.TimeZone;
//
//@SuppressLint({"InflateParams", "NewApi"})
//public class RecievingListScreen extends BaseActivity implements OnClickListener, OnItemClickListener, OnDeleteItemClickListner {
//
//    private ListView listView;
//    private JSONArray jsonArray;
//    private boolean isEditing;
//    public static JSONObject jsonobjectToChange;
//    private RecieveElementAdapter recieveElementAdapter;
//    private ProgressDialog progressDialog;
//    private String timezoneID;
//    private static boolean isBTRequestDenied = false;
//    private static boolean isGPS_ReqDenied = false;
//    private AsyncTaskSendPush asyncTaskSendPush;
//
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        setContentView(R.layout.receiver_list);
//
//        ImageView addMore = (ImageView) findViewById(R.id.addMore);
//        TextView editDetails = (TextView) findViewById(R.id.editDetails);
//        findViewById(R.id.buttonContactImage).setSelected(true);
//        findViewById(R.id.buttonAlerts).setSelected(false);
//        findViewById(R.id.buttonSetting).setSelected(false);
//
//        findViewById(R.id.layoutAlert).setOnClickListener(this);
//        findViewById(R.id.layoutContact).setOnClickListener(this);
//        findViewById(R.id.layoutSettings).setOnClickListener(this);
//        findViewById(R.id.layoutIamOK).setOnClickListener(this);
//
//        listView = (ListView) findViewById(R.id.listReciever);
//        listView.setOnItemClickListener(this);
//        addMore.setOnClickListener(this);
//        editDetails.setOnClickListener(this);
//
//        int alertCount = Preference.getInstance().mSharedPreferences.getInt(C.ALERT_COUNT, 0);
//        ((TextView) findViewById(R.id.alertCount)).setText("" + alertCount);
//
//        //		mLocationClient = new LocationClient(this, this, this);
//
//        // startBackgroundThreadForBLE();
//        if (!Utils.isInternetConnected(this)) {
//            Toast.makeText(this, Utils.NO_INTERNET, Toast.LENGTH_SHORT).show();
//            return;
//        }
//
//        progressDialog = ProgressDialog.show(RecievingListScreen.this, "", "Loading Data ....");
//        new ListCountThread().start();
//        loadRecieversListUsingThread();
//
//    }
//
//    @Override
//    protected void onStart() {
//        super.onStart();
//        //		mLocationClient.connect();
//        mHandler = new Handler();
//
//        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.JELLY_BEAN_MR1) {
//            return;
//        }
//        final BluetoothManager bluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
//        mBluetoothAdapter = bluetoothManager.getAdapter();
//
//        if (mBluetoothAdapter == null) {
//            Toast.makeText(this, R.string.error_bluetooth_not_supported, Toast.LENGTH_SHORT).show();
//            return;
//        }
//    }
//
//    public double getOffset() {
//        TimeZone timezone = TimeZone.getDefault();
//        int seconds = timezone.getOffset(Calendar.ZONE_OFFSET) / 1000;
//        double minutes = seconds / 60;
//        double hours = minutes / 60;
//        return hours;
//    }
//
//    public void onResume() {
//        super.onResume();
//
//        Calendar cal = Calendar.getInstance();
//        TimeZone tz = cal.getTimeZone();
//        timezoneID = tz.getID();
//
//        CheckForeground.onResume(RecievingListScreen.this);
//
//        isAllredyShown = false;
//        int alertCount = Preference.getInstance().mSharedPreferences.getInt(C.ALERT_COUNT, 0);
//        ((TextView) findViewById(R.id.alertCount)).setText("" + alertCount);
//        findViewById(R.id.buttonContactImage).setSelected(true);
//        findViewById(R.id.buttonAlerts).setSelected(false);
//        findViewById(R.id.buttonSetting).setSelected(false);
//
//        if (!Utils.isInternetConnected(this)) {
//            Toast.makeText(this, Utils.NO_INTERNET, Toast.LENGTH_SHORT).show();
//            return;
//        }
//
//        if (ReceiverAddNew.IS_UPDATED || EditRecieverScreen.IS_UPDATED) {
//            ReceiverAddNew.IS_UPDATED = false;
//            EditRecieverScreen.IS_UPDATED = false;
//            loadRecieversListUsingThread();
//        }
//
//        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.JELLY_BEAN_MR1) {
//            isBleSupported = false;
//            if (isBleDialogShown) {
//                return;
//            }
//            buildAlertDialogBLENotSupported();
//            return;
//        }
//        isBleSupported = true;
//        bleHelper = new BLEHelper(this, false);
//
//        if (isBleSupported && mBluetoothAdapter != null && !mBluetoothAdapter.isEnabled() && !isBTRequestDenied) {
//            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
//            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
//            return;
//        }
//
//        final LocationManager manager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
//        if (isBleSupported && !manager.isProviderEnabled(LocationManager.GPS_PROVIDER) && !isGPS_ReqDenied) {
//            buildAlertMessageNoGps();
//            return;
//        }
//
//        if (isBleSupported) {
//            // scanLeDevice(true);
//        }
//
//
//    }
//
//    private void callSenDangerServiceRecievingListScreen() {
//
//        if (NetworkAvailability.isOnline(RecievingListScreen.this, true, true, true)) {
//            if (asyncTaskSendPush != null && asyncTaskSendPush.getStatus() == AsyncTask.Status.PENDING) {
//                asyncTaskSendPush.execute();
//            } else if (asyncTaskSendPush == null || asyncTaskSendPush.getStatus() == AsyncTask.Status.FINISHED) {
//                asyncTaskSendPush = new AsyncTaskSendPush();
//                asyncTaskSendPush.execute();
//            }
//        } else {
//            Utils.showToast(RecievingListScreen.this, "Ineternet is not connected");
//        }
//
//    }
//
//
//    @Override
//    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
//        super.onActivityResult(requestCode, resultCode, data);
//        if (requestCode == REQUEST_ENABLE_BT && resultCode == RESULT_CANCELED) {
//            isBTRequestDenied = true;
//        }
//
//    }
//
//    private boolean isDataAvailable = false;
//    private int listSize = 0;
//    private boolean isJustDataDeleted = false;
//
//    private final String SUCCESS = "success";
//    private BLEHelper bleHelper;
//    private boolean isBleSupported;
//    private Handler handler;
//    private static boolean isBleDialogShown;
//
//    private void loadRecieversListUsingThread() {
//        handler = new Handler();
//        progressDialog.show();
//        new Thread(new Runnable() {
//
//            @Override
//            public void run() {
//
//                final WsCallGetAllContacts wsCallGetAllContacts = new WsCallGetAllContacts(RecievingListScreen.this);
//                JSONObject recieverList = wsCallGetAllContacts.executeService();
//                if (recieverList != null) {
//                    if (wsCallGetAllContacts.isSuccess()) {
//                        isDataAvailable = true;
//                        jsonArray = recieverList.optJSONArray("data");
//                        listSize = jsonArray.length();
//                    } else {
//                        isDataAvailable = false;
//                    }
//                }
//
//                handler.post(new Runnable() {
//
//                    @Override
//                    public void run() {
//                        if ((isDataAvailable || isJustDataDeleted) && jsonArray != null) {
//                            isJustDataDeleted = false;
//                            recieveElementAdapter = new RecieveElementAdapter(RecievingListScreen.this, jsonArray, isDataAvailable);
//                            listView.setAdapter(recieveElementAdapter);
//                            recieveElementAdapter.notifyDataSetChanged();
//                            listView.setOnItemClickListener(RecievingListScreen.this);
//                        }
//                        progressDialog.dismiss();
//                    }
//                });
//
//            }
//        }).start();
//    }
//
//    @Override
//    public void onClick(View v) {
//        switch (v.getId()) {
//            case R.id.addMore:
//                addNewAlertReciever();
//                break;
//
//            case R.id.editDetails:
//                if (isEditing()) {
//                    ((TextView) findViewById(R.id.editDetails)).setText("Delete");
//                    setEditing(false);
//                    if (listSize > 0) {
//                        recieveElementAdapter.notifyDataSetChanged();
//                    }
//                    return;
//                }
//                setToEditDetails();
//                break;
//            case R.id.layoutContact:
//                findViewById(R.id.buttonContactImage).setSelected(true);
//                findViewById(R.id.buttonAlerts).setSelected(false);
//                findViewById(R.id.buttonSetting).setSelected(false);
//                findViewById(R.id.buttonIamOK).setSelected(false);
//                break;
//
//            case R.id.layoutAlert:
//                findViewById(R.id.buttonAlerts).setSelected(true);
//                findViewById(R.id.buttonContactImage).setSelected(false);
//                findViewById(R.id.buttonSetting).setSelected(false);
//                findViewById(R.id.buttonIamOK).setSelected(false);
//                startAlertListScreen();
//                Utils.StartLeftAnim(this);
//                break;
//
//            case R.id.layoutSettings:
//                findViewById(R.id.buttonSetting).setSelected(true);
//                findViewById(R.id.buttonContactImage).setSelected(false);
//                findViewById(R.id.buttonAlerts).setSelected(false);
//                findViewById(R.id.buttonIamOK).setSelected(false);
//                startSettingScreen();
//                Utils.StartLeftAnim(this);
//                break;
//
//            case R.id.layoutIamOK:
//                if (!Preference.getInstance().mSharedPreferences.getBoolean(Utils.isPinCreated, false)) {
//                    showToast(C.SORRY_NO_PIN);
//                    return;
//                }
//                findViewById(R.id.buttonIamOK).setSelected(true);
//                findViewById(R.id.buttonSetting).setSelected(false);
//                findViewById(R.id.buttonContactImage).setSelected(false);
//                findViewById(R.id.buttonAlerts).setSelected(false);
//                showIamOkDialog();
//                break;
//
//            default:
//                break;
//        }
//    }
//
//    private void startAlertListScreen() {
//        Intent i = new Intent(this, AlertListScreen.class);
//        startActivity(i);
//    }
//
//    private void startSettingScreen() {
//        Intent i = new Intent(this, SettingScreen.class);
//        startActivity(i);
//    }
//
//    private void setToEditDetails() {
//        if (!isDataAvailable) {
//            return;
//        }
//        setEditing(true);
//        ((TextView) findViewById(R.id.editDetails)).setText("Done");
//        recieveElementAdapter.notifyDataSetChanged();
//    }
//
//    private void addNewAlertReciever() {
//        Intent intent = new Intent(this, ReceiverAddNew.class);
//        startActivity(intent);
//    }
//
//    private void buildAlertDialogBLENotSupported() {
//        isBleDialogShown = true;
//        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
//        builder.setMessage(C.NO_BEACON_FUNCTION).setCancelable(false).setPositiveButton("OK", new DialogInterface.OnClickListener() {
//            public void onClick(final DialogInterface dialog, final int id) {
//                dialog.dismiss();
//            }
//        });
//        final AlertDialog alert = builder.create();
//        alert.show();
//    }
//
//    private void buildAlertMessageNoGps() {
//
//        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
//        builder.setMessage("Your GPS seems to be disabled, do you want to enable it?").setCancelable(false).setPositiveButton("Yes", new DialogInterface.OnClickListener() {
//            public void onClick(final DialogInterface dialog, final int id) {
//                startActivity(new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS));
//            }
//        }).setNegativeButton("No", new DialogInterface.OnClickListener() {
//            public void onClick(final DialogInterface dialog, final int id) {
//                dialog.cancel();
//                isGPS_ReqDenied = true;
//            }
//        });
//        final AlertDialog alert = builder.create();
//        alert.show();
//    }
//
//    @Override
//    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
//        if (jsonArray != null) {
//            try {
//                jsonobjectToChange = (JSONObject) jsonArray.get(position);
//                if (isEditing()) {
//                    return;
//                }
//                Intent i = new Intent(this, DetailActivity.class);
//                startActivity(i);
//            } catch (JSONException e) {
//                e.printStackTrace();
//            }
//        }
//    }
//
//    @Override
//    public void onBackPressed() {
//        Utils.StartRightAnim(this);
//        Intent splashIntent = new Intent(this, SplashScreen.class);
//        splashIntent.putExtra(C.IS_EXIT, true);
//        startActivity(splashIntent);
//        finish();
//    }
//
//    @Override
//    public void onDeleteItemClick(int position) {
//        if (isEditing()) {
//            try {
//                jsonobjectToChange = (JSONObject) jsonArray.get(position);
//            } catch (JSONException e) {
//                e.printStackTrace();
//            }
//            deleteByThread();
//        }
//    }
//
//    Handler handlerDelete = new Handler();
//
//    private void deleteByThread() {
//        final ProgressDialog progressDialog = ProgressDialog.show(RecievingListScreen.this, "", "Deleting ....");
//        progressDialog.show();
//        new Thread(new Runnable() {
//
//            @Override
//            public void run() {
//                String contact_user_id = jsonobjectToChange.optString("userid");
//                WsCallDeleteContact wsCallDeleteContact = new WsCallDeleteContact(RecievingListScreen.this);
//                wsCallDeleteContact.executeService(contact_user_id);
//                if (wsCallDeleteContact.isSuccess()) {
//                    String email = jsonobjectToChange.optString(new WsConstants().PARAMS_EMAIL).toString();
//                    BitMapHelper.deleteImageFromStorage(RecievingListScreen.this, "" + email, Preference.getInstance().mSharedPreferences.getString(email, ""));
//                    isJustDataDeleted = true;
//                }
//
//                handlerDelete.post(new Runnable() {
//
//                    @Override
//                    public void run() {
//                        progressDialog.dismiss();
//                        if (isJustDataDeleted) {
//                            if (!Utils.isInternetConnected(RecievingListScreen.this)) {
//                                Toast.makeText(RecievingListScreen.this, Utils.NO_INTERNET, Toast.LENGTH_SHORT).show();
//                                return;
//                            }
//                            loadRecieversListUsingThread();
//                        }
//                    }
//                });
//
//            }
//        }).start();
//
//    }
//
//    private class ListCountThread extends Thread {
//
//        private static final String SUCCESS = "success";
//        private int issuccess;
//        private WsCallGetAlertCount wsCallGetAlertCount;
//
//        @Override
//        public void run() {
//            wsCallGetAlertCount = new WsCallGetAlertCount(RecievingListScreen.this);
//            String email = Preference.getInstance().mSharedPreferences.getString(C.KEY_EMAIL, "");
//            JSONObject recieverList = wsCallGetAlertCount.executeService(email, "0");
//            if (recieverList != null) {
//                if (wsCallGetAlertCount.isSuccess()) {
//                    issuccess = 1;
//                } else {
//                    issuccess = 0;
//                }
//            } else {
//                issuccess = 0;
//            }
//            runOnUiThread(new ListCountHandler(issuccess, recieverList));
//        }
//    }
//
//    private class ListCountHandler implements Runnable {
//
//        private JSONObject result;
//        private int issuccess;
//
//        public ListCountHandler(int issuccess, JSONObject result) {
//            this.issuccess = issuccess;
//            this.result = result;
//        }
//
//        @Override
//        public void run() {
//            if (issuccess == 1) {
//                int alertCount = result.optJSONArray("data").length();
//                if (alertCount < 0) {
//                    alertCount = 0;
//                }
//                Preference.getInstance().savePreferenceData(C.ALERT_COUNT, alertCount);
//            } else if (issuccess == 0) {
//                Preference.getInstance().savePreferenceData(C.ALERT_COUNT, 0);
//                ((TextView) findViewById(R.id.alertCount)).setText("" + 0);
//            }
//        }
//    }
//
//    // ///////////////////// BLE Scanning//////////////////////
//
//    private BluetoothAdapter mBluetoothAdapter;
//    private Handler mHandler;
//    private static final int REQUEST_ENABLE_BT = 1;
//    // Stops scanning after 10 seconds.
//    private static final long SCAN_PERIOD = 10000;
//    public static String TEST_UUID = "E2C56DB5DFFB48D2B060D0F5A71096E0";
//
//    private void scanLeDevice(final boolean enable) {
//        if (enable) {
//            // Stops scanning after a pre-defined scan period.
//            mHandler.postDelayed(new Runnable() {
//                @Override
//                public void run() {
//                    mBluetoothAdapter.stopLeScan(bleHelper.getmLeScanCallback());
//                    invalidateOptionsMenu();
//                }
//            }, SCAN_PERIOD);
//            mBluetoothAdapter.startLeScan(bleHelper.getmLeScanCallback());
//        } else {
//            mBluetoothAdapter.stopLeScan(bleHelper.getmLeScanCallback());
//        }
//    }
//
//    private boolean isAllredyShown;
//
//    @Override
//    protected void onPause() {
//        super.onPause();
//        CheckForeground.onPause();
//    }
//
//    private class PushForReciever extends Thread {
//        @Override
//        public void run() {
//            String userId = Preference.getInstance().mSharedPreferences.getString(C.USER_ID, "");
////            new ServerResponseHelper().requestToPush(getLatitude() + "", getLongitude() + "", userId, timezoneID);
//
//            callSenDangerServiceRecievingListScreen();
//
//
//        }
//
//
//    }
//
//
//    private class PushForCrowdAlert extends Thread {
//        @Override
//        public void run() {
//            String userId = Preference.getInstance().mSharedPreferences.getString(C.USER_ID, "");
////            new ServerResponseHelper().requestToPushForCrowdAlert(getLatitude() + "", getLongitude() + "", userId, timezoneID);
//
//            callSenDangerServiceRecievingListScreen();
//        }
//    }
//
//    void sendPushNotification() {
//        if (isAllredyShown) {
//            return;
//        }
//        isAllredyShown = true;
//        Toast.makeText(RecievingListScreen.this, "Sending Alert...", Toast.LENGTH_SHORT).show();
//        updateLatLong();
//        new PushForReciever().start();
//        new PushForCrowdAlert().start();
//        if (mBluetoothAdapter != null) {
//            mBluetoothAdapter.stopLeScan(bleHelper.getmLeScanCallback());
//        }
//    }
//
//    public boolean isEditing() {
//        return isEditing;
//    }
//
//    public void setEditing(boolean isEditing) {
//        this.isEditing = isEditing;
//    }
//
//    private class AsyncTaskSendPush extends AsyncTask<Void, Void, Void> {
//
//        private WsCallSendDanger wsCallSendDanger;
////        private ProgressDialog progressDialog;
//
//        //        private String strOldPassword = etOldPassword.getText().toString().trim();
////        private String strNewPassword = etNewPassword.getText().toString().trim();
//        double log = getLongitude();
//        double lat = getLatitude();
//
//
//        //        AsyncTaskSendPush(double latitude, double longitude String timezoneID)
////        {
////
////        }
//        @Override
//        protected void onPreExecute() {
//            super.onPreExecute();
////            progressDialog = new ProgressDialog(getActivity());
////            progressDialog.show();
//////            progressDialog.setContentView(R.layout.progress_layout);
////            progressDialog.setCancelable(false);
//
//
//        }
//
//        @Override
//        protected Void doInBackground(Void... params) {
//
//            wsCallSendDanger = new WsCallSendDanger(RecievingListScreen.this);
//            wsCallSendDanger.executeService(String.valueOf(log), String.valueOf(lat), timezoneID);
//            return null;
//        }
//
//
//        @Override
//        protected void onPostExecute(Void aVoid) {
//            super.onPostExecute(aVoid);
//
//
//            if (!isCancelled()) {
//                if (wsCallSendDanger.isSuccess()) {
//                    // From here do further logic
//                }
//            }
//        }
//
//
//    }
//}
//
