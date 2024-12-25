package net.tigerlight.dad.registration.fragment;

import static android.app.Activity.RESULT_CANCELED;
import static net.tigerlight.dad.registration.util.Utills.isInternetConnected;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import androidx.fragment.app.Fragment;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.drawable.ColorDrawable;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import net.tigerlight.dad.R;
import net.tigerlight.dad.home.BaseFragment;
import net.tigerlight.dad.recievers.BLEHelper;
import net.tigerlight.dad.registration.activity.MainActivity;
import net.tigerlight.dad.registration.adapter.RecieveElementAdapter;
import net.tigerlight.dad.registration.util.Constant;
import net.tigerlight.dad.registration.util.Utills;
import net.tigerlight.dad.webservices.WsCallDeleteContact;
import net.tigerlight.dad.webservices.WsCallGetAlertCount;
import net.tigerlight.dad.webservices.WsCallGetAllContacts;
import net.tigerlight.dad.webservices.WsCallSendDanger;
import net.tigerlight.dad.webservices.WsResetCount;
import net.tigerlight.dad.swipemenulistview.SwipeMenu;
import net.tigerlight.dad.swipemenulistview.SwipeMenuCreator;
import net.tigerlight.dad.swipemenulistview.SwipeMenuItem;
import net.tigerlight.dad.swipemenulistview.SwipeMenuListView;
import net.tigerlight.dad.util.BitMapHelper;
import net.tigerlight.dad.util.Constants;
import net.tigerlight.dad.util.NetworkAvailability;
import net.tigerlight.dad.util.Preference;
import net.tigerlight.dad.util.Util;
import net.tigerlight.dad.util.WsConstants;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Calendar;
import java.util.TimeZone;

public class ContactFragment extends BaseFragment implements AdapterView.OnItemClickListener, RecieveElementAdapter.OnDeleteItemClickListner {

    private static final String TAG = ContactFragment.class.getSimpleName();
    private static final int MY_PERMISSIONS_REQUEST_BLUETOOTH = 1002;
    private static final int REQUEST_PERMISSIONS_REQUEST_CODE = 34;

    private TextView tvRestoreFromTheServer;
    private TextView tvEmptyView;
    private ImageView ivAddMore;

    private SwipeMenuListView listView;
    private JSONArray jsonArray;
    private boolean isEditing;
    private JSONObject jsonobjectToChange;
    private RecieveElementAdapter recieveElementAdapter;
    private ProgressDialog progressDialog;
    private String timezoneID;
    private static boolean isBTRequestDenied = false;
    private static boolean isGPS_ReqDenied = false;
    private AsyncTaskSendPush asyncTaskSendPush;
    private AsyncTaskResetCount asyncTaskResetCount;

    private boolean isDataAvailable = false;
    private int listSize = 0;
    private boolean isJustDataDeleted = false;

    private final String SUCCESS = "success";
    private BLEHelper bleHelper;
    private boolean isBleSupported;
    private Handler handler;
    private static boolean isBleDialogShown;
    public static boolean isServiceCall = false;
    private LinearLayout llMain;
    private LinearLayout llEmptyView;


    private DashBoardWithSwipableFragment dashBoardWithSwipableFragment;

    public ContactFragment() {

    }

    public ContactFragment(DashBoardWithSwipableFragment dashBoardWithSwipableFragment) {

        this.dashBoardWithSwipableFragment = dashBoardWithSwipableFragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //		mLocationClient.connect();

        callResetCount();
        mHandler = new Handler();

        final BluetoothManager bluetoothManager = (BluetoothManager) getActivity().getSystemService(Context.BLUETOOTH_SERVICE);
        mBluetoothAdapter = bluetoothManager.getAdapter();

        if (mBluetoothAdapter == null) {
            Toast.makeText(getActivity(), R.string.error_bluetooth_not_supported, Toast.LENGTH_SHORT).show();
            buildAlertDialogBLENotSupported();
            return;
        }
        new AlertListLoaderThread().start();
//        dashBoardWithSwipableFragment.updateCount();
    }


    @Override
    public void onResume() {
        super.onResume();

        // Check for permissions
        if (!checkPermissions()) {
            requestPermissions();
        } else {
            // Permissions are already granted, proceed with your logic
            startForegroundService();
        }

        Calendar cal = Calendar.getInstance();
        TimeZone tz = cal.getTimeZone();
        timezoneID = tz.getID();

        isAllredyShown = false;

        if (!Utills.isInternetConnected(getActivity())) {
            Toast.makeText(getActivity(), getString(R.string.alert_check_connection), Toast.LENGTH_SHORT).show();
            return;
        }

        isBleSupported = true;
        bleHelper = new BLEHelper(ContactFragment.this, false);

        if (isBleSupported && mBluetoothAdapter != null && !mBluetoothAdapter.isEnabled() && !isBTRequestDenied) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
            return;
        }

        final LocationManager manager = (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);
        if (isBleSupported && !manager.isProviderEnabled(LocationManager.GPS_PROVIDER) && !isGPS_ReqDenied) {
            buildAlertMessageNoGps();
            return;
        }

        if (isBleSupported) {
            // scanLeDevice(true);
        }
    }

    private boolean checkPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            return ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED &&
                    ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.FOREGROUND_SERVICE) == PackageManager.PERMISSION_GRANTED &&
                    ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.BLUETOOTH_SCAN) == PackageManager.PERMISSION_GRANTED &&
                    ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.BLUETOOTH_CONNECT) == PackageManager.PERMISSION_GRANTED;
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            return ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED &&
                    ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.FOREGROUND_SERVICE) == PackageManager.PERMISSION_GRANTED &&
                    ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.BLUETOOTH) == PackageManager.PERMISSION_GRANTED &&
                    ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.BLUETOOTH_ADMIN) == PackageManager.PERMISSION_GRANTED;
        } else {
            return ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED &&
                    ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.BLUETOOTH) == PackageManager.PERMISSION_GRANTED &&
                    ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.BLUETOOTH_ADMIN) == PackageManager.PERMISSION_GRANTED;
        }
    }

    private void requestPermissions() {
        String[] permissions;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            permissions = new String[]{
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.FOREGROUND_SERVICE,
                    Manifest.permission.BLUETOOTH_SCAN,
                    Manifest.permission.BLUETOOTH_CONNECT
            };
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            permissions = new String[]{
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.FOREGROUND_SERVICE,
                    Manifest.permission.BLUETOOTH,
                    Manifest.permission.BLUETOOTH_ADMIN,
            };
        } else {
            permissions = new String[]{
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.BLUETOOTH,
                    Manifest.permission.BLUETOOTH_ADMIN,
            };
        }
        ActivityCompat.requestPermissions(getActivity(),
                permissions,
                REQUEST_PERMISSIONS_REQUEST_CODE);
    }

    private void startForegroundService() {
        // Your logic to start the foreground service
    }

    private void buildAlertDialogBLENotSupported() {
        isBleDialogShown = true;
        final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setMessage(Constant.NO_BEACON_FUNCTION).setCancelable(false).setPositiveButton(getString(R.string.TAG_OK), (dialog, id) -> dialog.dismiss());
        final AlertDialog alert = builder.create();
        alert.show();
    }

    private void buildAlertMessageNoGps() {

        final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setMessage(getString(R.string.TAG_GPS_ENABLE_MSG)).setCancelable(false).setPositiveButton(getString(R.string.TAG_YES), new DialogInterface.OnClickListener() {
            public void onClick(final DialogInterface dialog, final int id) {
                startActivity(new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS));
            }
        }).setNegativeButton(getString(R.string.TAG_NO), new DialogInterface.OnClickListener() {
            public void onClick(final DialogInterface dialog, final int id) {
                dialog.cancel();
                isGPS_ReqDenied = true;
            }
        });
        final AlertDialog alert = builder.create();
        alert.show();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_contact, container, false);

    }

    @Override
    public void initView(View view) {
        listView = (SwipeMenuListView) view.findViewById(R.id.listReciever);
        //tvRestoreFromTheServer = (TextView) view.findViewById(R.id.fragment_contact_tv_restore_from_the_server);
        ivAddMore = (ImageView) view.findViewById(R.id.fragment_contact_iv_add_more);
        llEmptyView = (LinearLayout) view.findViewById(R.id.fragment_contact_llEmptyView);
        tvEmptyView = (TextView) view.findViewById(R.id.fragment_contact_tvEmptyView);
        llMain = (LinearLayout) view.findViewById(R.id.fragment_contact_llMain);
        // int alertCount = Preference.getInstance().mSharedPreferences.getInt(C.ALERT_COUNT, 0);
        // ((TextView) view.findViewById(R.id.alertCount)).setText("" + alertCount);

        //		mLocationClient = new LocationClient(this, this, this);

        // startBackgroundThreadForBLE();
        if (!Utills.isInternetConnected(getActivity())) {
            Toast.makeText(getActivity(), getString(R.string.alert_check_connection), Toast.LENGTH_SHORT).show();
            return;
        }

        setSwipeMenu();
//        loadAlertCountUsingThread(true);
        loadRecieversListUsingThread(true);
        listView.setEmptyView(tvEmptyView);
        listView.setOnItemClickListener(this);
        //tvRestoreFromTheServer.setOnClickListener(this);
        ivAddMore.setOnClickListener(this);

    }

    public boolean isEditing() {
        return isEditing;
    }

    public void setEditing(boolean isEditing) {
        this.isEditing = isEditing;
    }

    private void loadRecieversListUsingThread(boolean showProgress) {
        handler = new Handler();
        if (showProgress) {
            progressDialog = ProgressDialog.show(getActivity(), "", getString(R.string.TAG_Loading));
        }

        new Thread(() -> {
            final WsCallGetAllContacts wsCallGetAllContacts = new WsCallGetAllContacts(getActivity());
            JSONObject recieverList = wsCallGetAllContacts.executeService();
            if (recieverList != null) {
                if (wsCallGetAllContacts.isSuccess()) {
                    isDataAvailable = true;
                    jsonArray = recieverList.optJSONArray("data");
                    listSize = jsonArray.length();
                } else {
                    isDataAvailable = false;
                }
            }

            handler.post(() -> {
                try {
                    if ((isDataAvailable || isJustDataDeleted) && jsonArray != null) {
                        isJustDataDeleted = false;
                        recieveElementAdapter = new RecieveElementAdapter(getActivity(), ContactFragment.this, jsonArray, isDataAvailable);
                        listView.setAdapter(recieveElementAdapter);
                        recieveElementAdapter.notifyDataSetChanged();

                        if (jsonArray.length() == 0) {
                            tvEmptyView.setVisibility(View.VISIBLE);
                            tvEmptyView.setText(getString(R.string.TAG_DATA_NA_MSG));
                            llMain.setVisibility(View.GONE);
                        } else {
                            llMain.setVisibility(View.VISIBLE);
                            tvEmptyView.setVisibility(View.GONE);
                        }

                        listView.setOnItemClickListener(ContactFragment.this);
                    }
                    if (progressDialog != null && progressDialog.isShowing()) {
                        progressDialog.dismiss();
                    }
                } catch (Exception ex) {
                    Log.e(TAG, ex.getMessage());
                }
            });
        }).start();
    }

    @Override
    public void trackScreen() {
    }

    @Override
    public void initActionBar() {
    }

    /**
     * Handling Navigation.
     *
     * @param v
     */
    @Override
    public void onClick(View v) {
        super.onClick(v);

        if (v.getId() == R.id.fragment_contact_iv_add_more) {
            loadFragment(new AddMoreFragment(ContactFragment.this), AddMoreFragment.class.getSimpleName());
        }
    }

    /**
     * To add fragment in container
     *
     * @param newFragment
     * @param tagStr
     */
    private void loadFragment(final Fragment newFragment, final String tagStr) {
        Util.getInstance().hideSoftKeyboard(getActivity());
        getLocalFragmentManager()
                .beginTransaction()
                .add(R.id.activity_registartion_fl_container, newFragment, newFragment.getClass().getSimpleName())
                .addToBackStack(newFragment.getClass().getSimpleName())
                .hide(ContactFragment.this)
                .commit();
    }

    private void callResetCount() {
        if (Utills.isInternetConnected(getActivity())) {
            if (asyncTaskResetCount != null && asyncTaskResetCount.getStatus() == AsyncTask.Status.PENDING) {
                asyncTaskResetCount.execute();
            } else if (asyncTaskResetCount == null || asyncTaskResetCount.getStatus() == AsyncTask.Status.FINISHED) {
                asyncTaskResetCount = new AsyncTaskResetCount();
                asyncTaskResetCount.execute();
            }
        } else {
            Utills.displayDialogNormalMessage(getString(R.string.app_name), getString(R.string.TAG_INTERNET_AVAILABILITY), getActivity());
        }

    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        if (jsonArray != null) {
            try {
                jsonobjectToChange = (JSONObject) jsonArray.get(position);
                if (isEditing()) {
                    return;
                }
                final AddMoreFragment addMoreFragment = new AddMoreFragment(ContactFragment.this);
                final Bundle bundle = new Bundle();
                final String jsonObject = jsonobjectToChange.toString();
                bundle.putString(Constant.JSON_OBJECT, jsonObject);
                addMoreFragment.setArguments(bundle);
                loadFragment(addMoreFragment, AddMoreFragment.class.getSimpleName());
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_ENABLE_BT && resultCode == RESULT_CANCELED) {
            isBTRequestDenied = true;
        }
    }

    @Override
    public void onDeleteItemClick(int position) {
        try {
            jsonobjectToChange = (JSONObject) jsonArray.get(position);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        deleteByThread();
    }

    Handler handlerDelete = new Handler();

    private void deleteByThread() {
        progressDialog = ProgressDialog.show(getActivity(), "", getString(R.string.TAG_Loading));
        new Thread(() -> {
            String contact_user_id = jsonobjectToChange.optString("userid");
            WsCallDeleteContact wsCallDeleteContact = new WsCallDeleteContact(getActivity());
            wsCallDeleteContact.executeService(contact_user_id);
            if (wsCallDeleteContact.isSuccess()) {
                String email = jsonobjectToChange.optString(new WsConstants().PARAMS_EMAIL);
                BitMapHelper.deleteImageFromStorage(getActivity(), "" + email, Preference.getInstance().mSharedPreferences.getString(email, ""));
                isJustDataDeleted = true;
            }

            handlerDelete.post(() -> {
                if (isJustDataDeleted) {
                    if (!isInternetConnected(getActivity())) {
                        Toast.makeText(getActivity(), getString(R.string.alert_check_connection), Toast.LENGTH_SHORT).show();
                        return;
                    }
                    loadRecieversListUsingThread(false);
                }
            });

        }).start();

    }

    @Override
    public void onStop() {
        super.onStop();
        //CheckForeground.onPause();
    }

    private class ListCountThread extends Thread {

        private static final String SUCCESS = "success";
        private int issuccess;
        private WsCallGetAlertCount wsCallGetAlertCount;

        @Override
        public void run() {
            wsCallGetAlertCount = new WsCallGetAlertCount(getActivity());
            String email = Preference.getInstance().mSharedPreferences.getString(Constant.KEY_EMAIL, "");
            JSONObject recieverList = wsCallGetAlertCount.executeService(email, "0");
            if (recieverList != null) {
                if (wsCallGetAlertCount.isSuccess()) {
                    issuccess = 1;
                } else {
                    issuccess = 0;
                }
            } else {
                issuccess = 0;
            }
            getActivity().runOnUiThread(new ListCountHandler(issuccess, recieverList));
        }
    }

    private class ListCountHandler implements Runnable {

        private JSONObject result;
        private int issuccess;

        public ListCountHandler(int issuccess, JSONObject result) {
            this.issuccess = issuccess;
            this.result = result;
        }

        @Override
        public void run() {
            if (issuccess == 1) {
                int alertCount = result.optJSONArray("data").length();
                if (alertCount < 0) {
                    alertCount = 0;
                }
            } else if (issuccess == 0) {
            }
        }
    }

    // ///////////////////// BLE Scanning//////////////////////

    private BluetoothAdapter mBluetoothAdapter;
    private Handler mHandler;
    private static final int REQUEST_ENABLE_BT = 1;
    // Stops scanning after 10 seconds.
    private static final long SCAN_PERIOD = 10000;
    public static String TEST_UUID_PREVIOUS = Constants.OLD_UUID;

    public static String TEST_UUID = Constants.NEW_UUID;

    private boolean isAllredyShown;

    private class PushForReciever extends Thread {
        @Override
        public void run() {
            String userId = Preference.getInstance().mSharedPreferences.getString(Constant.USER_ID, "");
            callSenDangerServiceRecievingListScreen();
        }
    }

    private void callSenDangerServiceRecievingListScreen() {
        if (NetworkAvailability.isOnline(getActivity(), true, true, true)) {
            if (asyncTaskSendPush != null && asyncTaskSendPush.getStatus() == AsyncTask.Status.PENDING) {
                asyncTaskSendPush.execute();
            } else if (asyncTaskSendPush == null || asyncTaskSendPush.getStatus() == AsyncTask.Status.FINISHED) {
                asyncTaskSendPush = new AsyncTaskSendPush();
                asyncTaskSendPush.execute();
            }
        } else {
            Toast.makeText(getActivity(), getString(R.string.TAG_INTERNET_AVAILABILITY), Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == MY_PERMISSIONS_REQUEST_BLUETOOTH) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                sendPushNotification();
            } else {
                Toast.makeText(getActivity(), "Bluetooth Permission denied", Toast.LENGTH_SHORT).show();
            }
        } else if (requestCode == REQUEST_PERMISSIONS_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted, proceed with your logic
                startForegroundService();
            } else {
                // Permission denied, show a message to the user
                Toast.makeText(getActivity(), "Location Permission denied", Toast.LENGTH_SHORT).show();
            }
        }
    }

    public void sendPushNotification() {
        if (isAllredyShown) {
            return;
        }
        if (ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.BLUETOOTH_SCAN)
                == PackageManager.PERMISSION_GRANTED) {
            isAllredyShown = true;
            Toast.makeText(getActivity(), getString(R.string.TAG_SENDING_ALERT), Toast.LENGTH_SHORT).show();
            ((MainActivity) getActivity()).updateLatLong();
            new PushForReciever().start();
            if (mBluetoothAdapter != null) {
                mBluetoothAdapter.stopLeScan(bleHelper.getmLeScanCallback());
            }
        } else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                requestPermissions(new String[]{Manifest.permission.BLUETOOTH_SCAN}, MY_PERMISSIONS_REQUEST_BLUETOOTH);
            }
        }
    }

    private class AsyncTaskSendPush extends AsyncTask<Void, Void, Void> {

        private WsCallSendDanger wsCallSendDanger;
        String lat = Preference.getInstance().mSharedPreferences.getString(Constant.COMMON_LATITUDE, "0.01");
        String log = Preference.getInstance().mSharedPreferences.getString(Constant.COMMON_LONGITUDE, "0.01");
        int accuracy = Preference.getInstance().mSharedPreferences.getInt(Constant.COMMON_ACCURACY, 0);

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected Void doInBackground(Void... params) {
            wsCallSendDanger = new WsCallSendDanger(getActivity());
            wsCallSendDanger.executeService(Double.parseDouble(lat), Double.parseDouble(log), timezoneID, accuracy);
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            if (!isCancelled()) {
                if (wsCallSendDanger.isSuccess()) {
                    // From here do further logic
                }
            }
        }
    }

    /**
     * Setup swipe menu on listview and apply click event on it
     */
    private void setSwipeMenu() {
        final SwipeMenuCreator creator = new SwipeMenuCreator() {
            @Override
            public void create(SwipeMenu menu) {
                final SwipeMenuItem swipeMenuItemDelete = new SwipeMenuItem(getActivity());
                swipeMenuItemDelete.setBackground(new ColorDrawable(ContextCompat.getColor(getActivity(), R.color.color_alert_red)));
                swipeMenuItemDelete.setWidth(Utills.dpToPx(getActivity(), 100));
                swipeMenuItemDelete.setIcon(R.drawable.img_notification_delete);
                swipeMenuItemDelete.setTitleColor(ContextCompat.getColor(getActivity(), R.color.colorWhite));
                menu.addMenuItem(swipeMenuItemDelete);
            }
        };
        listView.setMenuCreator(creator);
        listView.setOnMenuItemClickListener(new SwipeMenuListView.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(int position, SwipeMenu menu, int index) {
                try {
                    jsonobjectToChange = (JSONObject) jsonArray.get(position);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                switch (index) {
                    case 0:
                        if (menu.getMenuItems().size() == 1) {
                            displayDeleteDialog(getActivity(), getString(R.string.app_name), getString(R.string.TAG_IS_SURE_MSG), getString(R.string.TAG_YES), getString(R.string.fragment_create_account_tv_cancel));
                        }
                        break;
                }
                return false;
            }
        });
        listView.setOnSwipeListener(new SwipeMenuListView.OnSwipeListener() {
            @Override
            public void onSwipeStart(int position) {
            }

            @Override
            public void onSwipeEnd(int position) {

            }
        });
    }

    private void displayDeleteDialog(final Activity context, final String title, final String msg, final String strPositiveText, final String strNegativeText) {
        final AlertDialog.Builder dialog = new AlertDialog.Builder(context);
        dialog.setTitle(title);
        dialog.setCancelable(false);
        dialog.setMessage(msg);
        dialog.setPositiveButton(strPositiveText, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                dialog.dismiss();
                if (Utills.isOnline(getActivity(), true)) {
                    deleteByThread();
                } else {
                    Utills.displayDialogNormalMessage(getString(R.string.app_name), getString(R.string.TAG_INTERNET_AVAILABILITY), getActivity());
                }
            }
        });
        dialog.setNegativeButton(strNegativeText, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                dialog.dismiss();
            }
        });
        dialog.show();
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        if (!hidden) {
            if (isServiceCall) {
                loadRecieversListUsingThread(true);
            }
        }
    }

    private class AlertListLoaderThread extends Thread {
        @Override
        public void run() {
            try {
                final WsCallGetAlertCount wsCallGetAlertCount;
                wsCallGetAlertCount = new WsCallGetAlertCount(getActivity());
                String email = Preference.getInstance().mSharedPreferences.getString(Constant.KEY_EMAIL, "");
                JSONObject jsonRecieved = wsCallGetAlertCount.executeService(email, "" + 0);
                if (jsonRecieved != null) {
                    if (jsonRecieved.getInt(SUCCESS) == 1) {
                        isDataAvailable = true;
                        int alertCount = jsonRecieved.optJSONArray("data").length();

                        if (alertCount < 0) {
                            alertCount = 0;
                        }
                        Preference.getInstance().savePreferenceData("total_count", alertCount);
                        Preference.getInstance().savePreferenceData("total_count", alertCount);
                    } else {
                        isDataAvailable = false;
                    }
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    private class AsyncTaskResetCount extends AsyncTask<Void, Void, Void> {
        private WsResetCount wsResetCount;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected Void doInBackground(Void... params) {
            wsResetCount = new WsResetCount(getActivity());
            wsResetCount.executeService();
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            if (!isCancelled()) {
                if (wsResetCount.isSuccess()) {
                    Log.d("Count", "Updated");
                } else {
                    Toast.makeText(getActivity(), getString(R.string.TAG_SOME_WENT_WRONG_MSG), Toast.LENGTH_SHORT).show();
                }
            }
        }
    }
}
