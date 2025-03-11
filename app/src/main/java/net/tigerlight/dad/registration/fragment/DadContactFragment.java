package net.tigerlight.dad.registration.fragment;

import static android.app.Activity.RESULT_CANCELED;
import static net.tigerlight.dad.registration.util.DadUtils.isInternetConnected;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.content.Context;
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

import com.net.tigerlight.dad.R;
import net.tigerlight.dad.home.BaseFragment;
import net.tigerlight.dad.recievers.DadBLEHelper;
import net.tigerlight.dad.registration.activity.DadMainActivity;
import net.tigerlight.dad.registration.adapter.DadReceiveElementAdapter;
import net.tigerlight.dad.registration.util.DadConstant;
import net.tigerlight.dad.registration.util.DadUtils;
import net.tigerlight.dad.webservices.CallDeleteContact;
import net.tigerlight.dad.webservices.CallGetAllContacts;
import net.tigerlight.dad.webservices.CallSendDanger;
import net.tigerlight.dad.swipemenulistview.SwipeMenu;
import net.tigerlight.dad.swipemenulistview.SwipeMenuCreator;
import net.tigerlight.dad.swipemenulistview.SwipeMenuItem;
import net.tigerlight.dad.swipemenulistview.SwipeMenuListView;
import net.tigerlight.dad.util.BitMapHelper;
import net.tigerlight.dad.util.Constants;
import net.tigerlight.dad.util.NetworkAvailability;
import net.tigerlight.dad.util.Preference;
import net.tigerlight.dad.util.WsConstants;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Calendar;
import java.util.TimeZone;

public class DadContactFragment extends BaseFragment implements AdapterView.OnItemClickListener, DadReceiveElementAdapter.OnDeleteItemClickListner, OnContactUpdatedListener {

    private static final String TAG = DadContactFragment.class.getSimpleName();
    private static final int MY_PERMISSIONS_REQUEST_BLUETOOTH = 1002;
    private static final int REQUEST_PERMISSIONS_REQUEST_CODE = 34;

    private TextView tvEmptyView;

    private SwipeMenuListView listView;
    private JSONArray jsonArray;
    private boolean isEditing;
    private JSONObject jsonobjectToChange;
    private DadReceiveElementAdapter recieveElementAdapter;
    private ProgressDialog progressDialog;
    private String timezoneID;
    private static boolean isBTRequestDenied = false;
    private static boolean isGPS_ReqDenied = false;
    private AsyncTaskSendPush asyncTaskSendPush;

    private boolean isDataAvailable = false;
    private boolean isJustDataDeleted = false;

    private DadBLEHelper dadBleHelper;
    private Handler handler;
    public static boolean isServiceCall = false;
    private LinearLayout llMain;

    @Override
    public void onContactUpdated() {
        // Refresh the contact list here
        loadRecieversListUsingThread(true); // Use your existing method to reload the list
    }

    public DadContactFragment() {

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //		mLocationClient.connect();

        final BluetoothManager bluetoothManager;
        if (getActivity() != null) {
            bluetoothManager = (BluetoothManager) getActivity().getSystemService(Context.BLUETOOTH_SERVICE);
            mBluetoothAdapter = bluetoothManager.getAdapter();
        }

        if (mBluetoothAdapter == null) {
            Toast.makeText(getActivity(), R.string.error_bluetooth_not_supported, Toast.LENGTH_SHORT).show();
            buildAlertDialogBLENotSupported();
        }
    }


    @Override
    public void onResume() {
        super.onResume();

        // Check for permissions
        if (getActivity() != null && !checkPermissions()) {
            requestPermissions();
        }

        Calendar cal = Calendar.getInstance();
        TimeZone tz = cal.getTimeZone();
        timezoneID = tz.getID();

        isAllredyShown = false;

        if (getActivity() != null && !DadUtils.isInternetConnected(getActivity())) {
            Toast.makeText(getActivity(), getString(R.string.alert_check_connection), Toast.LENGTH_SHORT).show();
            return;
        }

        dadBleHelper = new DadBLEHelper(DadContactFragment.this, false);

        if (mBluetoothAdapter != null && !mBluetoothAdapter.isEnabled() && !isBTRequestDenied) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
            return;
        }

        final LocationManager manager;
        if (getActivity() != null) {
            manager = (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);
            if (!manager.isProviderEnabled(LocationManager.GPS_PROVIDER) && !isGPS_ReqDenied) {
                buildAlertMessageNoGps();
            }
        }
    }

    private boolean checkPermissions() {
        if (getActivity() == null) {
            return false;
        }
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
        if (getActivity() == null) {
            return;
        }
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
        final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setMessage(DadConstant.NO_BEACON_FUNCTION).setCancelable(false).setPositiveButton(getString(R.string.TAG_OK), (dialog, id) -> dialog.dismiss());
        final AlertDialog alert = builder.create();
        alert.show();
    }

    private void buildAlertMessageNoGps() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setMessage(getString(R.string.TAG_GPS_ENABLE_MSG)).setCancelable(false).setPositiveButton(getString(R.string.TAG_YES), (dialog, id) -> startActivity(new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS))).setNegativeButton(getString(R.string.TAG_NO), (dialog, id) -> {
            dialog.cancel();
            isGPS_ReqDenied = true;
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
        listView = view.findViewById(R.id.listReciever);
        ImageView ivAddMore = view.findViewById(R.id.fragment_contact_iv_add_more);
        tvEmptyView = view.findViewById(R.id.fragment_contact_tvEmptyView);
        llMain = view.findViewById(R.id.fragment_contact_llMain);
        if (getActivity() != null && !DadUtils.isInternetConnected(getActivity())) {
            Toast.makeText(getActivity(), getString(R.string.alert_check_connection), Toast.LENGTH_SHORT).show();
            return;
        }

        setSwipeMenu();
        loadRecieversListUsingThread(true);
        listView.setEmptyView(tvEmptyView);
        listView.setOnItemClickListener(this);
        ivAddMore.setOnClickListener(this);
    }

    public boolean isEditing() {
        return isEditing;
    }

    private void loadRecieversListUsingThread(boolean showProgress) {
        handler = new Handler();
        if (showProgress) {
            progressDialog = ProgressDialog.show(getActivity(), "", getString(R.string.TAG_Loading));
        }

        new Thread(() -> {
            final CallGetAllContacts callGetAllContacts = new CallGetAllContacts(getActivity());
            JSONObject recieverList = callGetAllContacts.executeService();
            if (recieverList != null) {
                if (callGetAllContacts.isSuccess()) {
                    isDataAvailable = true;
                    jsonArray = recieverList.optJSONArray("data");
                } else {
                    isDataAvailable = false;
                }
            }

            handler.post(() -> {
                try {
                    if ((isDataAvailable || isJustDataDeleted) && jsonArray != null) {
                        isJustDataDeleted = false;
                        recieveElementAdapter = new DadReceiveElementAdapter(getActivity(), DadContactFragment.this, jsonArray, isDataAvailable);
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

                        listView.setOnItemClickListener(DadContactFragment.this);
                    }
                } catch (Exception ex) {
                    Log.e(TAG, "error getting all contacts");
                }
                if (progressDialog != null && progressDialog.isShowing()) {
                    progressDialog.dismiss();
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
            openAddMoreFragment(new DadAddMoreFragment());
        }
    }

    private void openAddMoreFragment(DadAddMoreFragment addMoreFragment) {
        addMoreFragment.setOnContactUpdatedListener(this);
        addMoreFragment.show(getParentFragmentManager(), DadAddMoreFragment.class.getSimpleName());
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        if (jsonArray != null) {
            try {
                jsonobjectToChange = (JSONObject) jsonArray.get(position);
                if (isEditing()) {
                    return;
                }
                final DadAddMoreFragment addMoreFragment = new DadAddMoreFragment();
                final Bundle bundle = new Bundle();
                final String jsonObject = jsonobjectToChange.toString();
                bundle.putString(DadConstant.JSON_OBJECT, jsonObject);
                addMoreFragment.setArguments(bundle);
                openAddMoreFragment(addMoreFragment);
            } catch (JSONException e) {
                Log.e(TAG, "on item click");
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
            Log.e(TAG, "on delete item click");
        }
        deleteByThread();
    }

    Handler handlerDelete = new Handler();

    private void deleteByThread() {
        progressDialog = ProgressDialog.show(getActivity(), "", getString(R.string.TAG_Loading));
        new Thread(() -> {
            String contact_user_id = jsonobjectToChange.optString("userid");
            CallDeleteContact callDeleteContact = new CallDeleteContact(getActivity());
            callDeleteContact.executeService(contact_user_id);
            if (callDeleteContact.isSuccess()) {
                String email = jsonobjectToChange.optString(new WsConstants().PARAMS_EMAIL);
                BitMapHelper.deleteImageFromStorage(getActivity(), email, Preference.getInstance().mSharedPreferences.getString(email, ""));
                isJustDataDeleted = true;
            }

            handlerDelete.post(() -> {
                if (isJustDataDeleted) {
                    if (getActivity() != null && !isInternetConnected(getActivity())) {
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

    // ///////////////////// BLE Scanning//////////////////////

    private BluetoothAdapter mBluetoothAdapter;
    private static final int REQUEST_ENABLE_BT = 1;
    // Stops scanning after 10 seconds.
    private static final long SCAN_PERIOD = 10000;
    public static String TEST_UUID_PREVIOUS = Constants.OLD_UUID;

    public static String TEST_UUID = Constants.NEW_UUID;

    private boolean isAllredyShown;

    private class PushForReciever extends Thread {
        @Override
        public void run() {
            callSenDangerServiceRecievingListScreen();
        }
    }

    private void callSenDangerServiceRecievingListScreen() {
        if (getActivity() != null && NetworkAvailability.isOnline(getActivity(), true, true, true)) {
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
        if (getActivity() != null && ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.BLUETOOTH_SCAN)
                == PackageManager.PERMISSION_GRANTED) {
            isAllredyShown = true;
            Toast.makeText(getActivity(), getString(R.string.TAG_SENDING_ALERT), Toast.LENGTH_SHORT).show();
            ((DadMainActivity) getActivity()).updateLatLong();
            new PushForReciever().start();
            if (mBluetoothAdapter != null) {
                mBluetoothAdapter.stopLeScan(dadBleHelper.getmLeScanCallback());
            }
        } else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                requestPermissions(new String[]{Manifest.permission.BLUETOOTH_SCAN}, MY_PERMISSIONS_REQUEST_BLUETOOTH);
            }
        }
    }

    private class AsyncTaskSendPush extends AsyncTask<Void, Void, Void> {

        private CallSendDanger callSendDanger;
        String lat = Preference.getInstance().mSharedPreferences.getString(DadConstant.COMMON_LATITUDE, "0.01");
        String log = Preference.getInstance().mSharedPreferences.getString(DadConstant.COMMON_LONGITUDE, "0.01");
        int accuracy = Preference.getInstance().mSharedPreferences.getInt(DadConstant.COMMON_ACCURACY, 0);

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected Void doInBackground(Void... params) {
            callSendDanger = new CallSendDanger(getActivity());
            callSendDanger.executeService(Double.parseDouble(lat), Double.parseDouble(log), timezoneID, accuracy);
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            if (!isCancelled()) {
                if (callSendDanger.isSuccess()) {
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
                swipeMenuItemDelete.setWidth(DadUtils.dpToPx(getActivity(), 100));
                swipeMenuItemDelete.setIcon(R.drawable.img_notification_delete);
                swipeMenuItemDelete.setTitleColor(ContextCompat.getColor(getActivity(), R.color.colorWhite));
                menu.addMenuItem(swipeMenuItemDelete);
            }
        };
        listView.setMenuCreator(creator);
        listView.setOnMenuItemClickListener((position, menu, index) -> {
            try {
                jsonobjectToChange = (JSONObject) jsonArray.get(position);
            } catch (JSONException e) {
                Log.e(TAG, "set swipe menu");
            }
            if (index == 0) {
                if (menu.getMenuItems().size() == 1) {
                    displayDeleteDialog(getActivity(), getString(R.string.app_name), getString(R.string.TAG_IS_SURE_MSG), getString(R.string.TAG_YES), getString(R.string.fragment_create_account_tv_cancel));
                }
            }
            return false;
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
        dialog.setPositiveButton(strPositiveText, (dialog12, id) -> {
            dialog12.dismiss();
            if (getActivity() != null && DadUtils.isOnline(getActivity(), true)) {
                deleteByThread();
            } else {
                DadUtils.displayDialogNormalMessage(getString(R.string.app_name), getString(R.string.TAG_INTERNET_AVAILABILITY), getActivity());
            }
        });
        dialog.setNegativeButton(strNegativeText, (dialog1, id) -> dialog1.dismiss());
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
}
