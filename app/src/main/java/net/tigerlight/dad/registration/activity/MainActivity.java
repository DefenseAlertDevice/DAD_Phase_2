package net.tigerlight.dad.registration.activity;

import net.tigerlight.dad.R;
import net.tigerlight.dad.home.BaseActivity;
import net.tigerlight.dad.home.BaseFragment;
import net.tigerlight.dad.registration.fragment.AlertDetailFragment;
import net.tigerlight.dad.registration.fragment.DADLicenseFragment;
import net.tigerlight.dad.registration.fragment.DashBoardWithSwipableFragment;
import net.tigerlight.dad.registration.fragment.RegistartionFragment;
import net.tigerlight.dad.registration.util.Constant;
import net.tigerlight.dad.util.Preference;
import net.tigerlight.dad.util.Util;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.le.AdvertiseCallback;
import android.bluetooth.le.BluetoothLeAdvertiser;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.bluetooth.le.AdvertiseData;
import android.bluetooth.le.AdvertiseSettings;

import androidx.core.app.ActivityCompat;

public class MainActivity extends BaseActivity {

    private static final int REQUEST_ENABLE_BT = 10;

    public static byte[] hexStringToByteArray(String hexString) {
        int length = hexString.length();
        byte[] data = new byte[length / 2];
        for (int i = 0; i < length; i += 2) {
            data[i / 2] = (byte) ((Character.digit(hexString.charAt(i), 16) << 4)
                    + Character.digit(hexString.charAt(i + 1), 16));
        }
        return data;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registration);

        final boolean isAccepted = Preference.getInstance().mSharedPreferences.getBoolean(Constant.IS_ACCEPT, false);
        boolean isLogin = Preference.getInstance().mSharedPreferences.getBoolean(Constant.IS_LOGIN, false);

        final Intent intent = getIntent();
        String jsonObject = intent.getStringExtra(Constant.JSON_OBJECT);
        // boolean openAlertFragmentDirectly = intent.getBooleanExtra("OPEN_ALERT_FRAGMENT_DIRECTLY", false);
        Log.d("notification", "oncreate ----json object:" + intent.getStringExtra(Constant.JSON_OBJECT));
        if (jsonObject != null) {
            if (getSupportFragmentManager().getBackStackEntryCount() > 0) {
                Log.d("notification", "oncreate if " + intent.getStringExtra(Constant.JSON_OBJECT));
                // Get the Back Entry
                final FragmentManager.BackStackEntry backEntry = getSupportFragmentManager().getBackStackEntryAt(getSupportFragmentManager().getBackStackEntryCount() - 1);
                // Find the Fragment from the Back Entry by it's Tag
                final Fragment fragment = getSupportFragmentManager().findFragmentByTag(backEntry.getName());
                if (fragment != null) {
                    Log.d("notification", "fragment " + fragment);
                    // Fetch the Fragment currently added in the Stack
                    final BaseFragment currentFragment = (BaseFragment) getSupportFragmentManager().findFragmentById(R.id.activity_registartion_fl_container);
                    if (currentFragment != null) {
                        Log.d("notification", "currentFragment " + fragment);
                        addFragment(new AlertDetailFragment(), currentFragment);
                    }
                }
            } else {
                Log.d("notification", "oncreate else " + intent.getStringExtra(Constant.JSON_OBJECT));
                final AlertDetailFragment alertDetailFragment = new AlertDetailFragment();
                final Bundle bundle = new Bundle();
                bundle.putString(Constant.JSON_OBJECT, jsonObject);
                alertDetailFragment.setArguments(bundle);
                replaceFragment(alertDetailFragment);
            }
        } else {
            if (isLogin) {
                //if (isAccepted) {
                replaceFragment(new DashBoardWithSwipableFragment());
                //}
//            else if (!isAccepted) {
//                replaceFragment(new TermAndConditionFragment());
//            }
            } else {
                if (isAccepted) {
                    replaceFragment(new RegistartionFragment());
                } else {
                    replaceFragment(new DADLicenseFragment());
                }
            }
        }

//        if(jsonObject!=null){
//            Log.e("notification","oncreate inside"+intent.getStringExtra(Constant.JSON_OBJECT));
//            BaseFragment fragment = (BaseFragment) getSupportFragmentManager().findFragmentById(R.id.activity_registartion_fl_container);
//            if(fragment!=null){
//                Log.e("notification","if oncreate inside inside"+intent.getStringExtra(Constant.JSON_OBJECT));
//                final Bundle  bundle = new Bundle();
//                bundle.putString(Constant.JSON_OBJECT,jsonObject);
//                fragment.setArguments(bundle);
//                addFragment(new AlertDetailFragment(),fragment);
//            }else {
//                Log.e("notification","else oncreate inside inside"+intent.getStringExtra(Constant.JSON_OBJECT));
//                AlertDetailFragment alertDetailFragment = new AlertDetailFragment();
//                final Bundle  bundle = new Bundle();
//                bundle.putString(Constant.JSON_OBJECT,jsonObject);
//                alertDetailFragment.setArguments(bundle);
//                replaceAlertDetailFragment(alertDetailFragment);
//            }
//        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            // Check for necessary permissions
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_ADVERTISE) != PackageManager.PERMISSION_GRANTED ||
                    ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {

                // Request permissions if not already granted
                ActivityCompat.requestPermissions(
                        this,
                        new String[]{Manifest.permission.BLUETOOTH_ADVERTISE, Manifest.permission.BLUETOOTH_CONNECT},
                        REQUEST_ENABLE_BT
                );
            } else {
                // Initialize Bluetooth
                initializeBluetooth();
            }
        } else {
            // For API < 31, initialize directly
            initializeBluetooth();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == REQUEST_ENABLE_BT) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted, initialize Bluetooth
                initializeBluetooth();
            } else {
                Log.e("MainActivity", "Bluetooth permissions not granted. Unable to start Bluetooth operations.");
            }
        }
    }

    @SuppressLint("MissingPermission")
    private void initializeBluetooth() {
        BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (bluetoothAdapter == null || !bluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
            return;
        }

        BluetoothLeAdvertiser advertiser = bluetoothAdapter.getBluetoothLeAdvertiser();
        if (advertiser == null) {
            Log.e("BLE", "BLE advertising not supported on this device");
            return;
        }

        String uuidString = "FD8C0AA6D40411E5AB30625662870761";
        byte[] uuidBytes = hexStringToByteArray(uuidString);

        AdvertiseSettings settings = new AdvertiseSettings.Builder()
                .setAdvertiseMode(AdvertiseSettings.ADVERTISE_MODE_LOW_LATENCY)
                .setTxPowerLevel(AdvertiseSettings.ADVERTISE_TX_POWER_HIGH)
                .setConnectable(false)
                .build();

        AdvertiseData data = new AdvertiseData.Builder()
                .addManufacturerData(0x004C, uuidBytes) // 0x004C is Apple's manufacturer ID, for example
                .build();

        advertiser.startAdvertising(settings, data, new AdvertiseCallback() {
            @Override
            public void onStartSuccess(AdvertiseSettings settingsInEffect) {
                Log.i("BLE", "Advertising started successfully");
            }

            @Override
            public void onStartFailure(int errorCode) {
                Log.e("BLE", "Advertising failed with error code: " + errorCode);
            }
        });
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        Log.d("notification", "called" + intent.getStringExtra(Constant.JSON_OBJECT));
        // BaseFragment fragment = (BaseFragment) getSupportFragmentManager().findFragmentById(R.id.activity_registartion_fl_container);
        //if (fragment != null) {
        final AlertDetailFragment alertDetailFragment = new AlertDetailFragment();
        String jsonObject = intent.getStringExtra(Constant.JSON_OBJECT);
        final Bundle bundle = new Bundle();
        bundle.putString(Constant.JSON_OBJECT, jsonObject);
        alertDetailFragment.setArguments(bundle);
        Log.d("notification", " ifcalled" + intent.getStringExtra(Constant.JSON_OBJECT));
        addFragment(alertDetailFragment);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        if (getLocalFragmentManager().getBackStackEntryCount() > 0) {
            Util.getInstance().hideSoftKeyboard(this);
            getLocalFragmentManager().popBackStack();
        } else {
            buildAlertMessageExit();
        }
    }

    /**
     * asks user for the confirmation before exiting of the app
     */
    private void buildAlertMessageExit() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(getString(R.string.TAG_EXIT_WARN_MSG)).setCancelable(false).setPositiveButton(getString(R.string.TAG_YES), (dialog, id) -> callToFinish()).setNegativeButton(getString(R.string.TAG_NO), (dialog, id) -> dialog.cancel());
        final AlertDialog alert = builder.create();
        alert.show();
    }

    private void callToFinish() {
        super.finish();
    }

    /***
     * Add new fragment in given container.
     * <p>
     * This method will add new fragment in container and hide the current fragment.
     * And also will add current fragment in backstack.
     * </p>
     *
     * @param newFragment  This parameter will take new fragment name which need to be add.
     * @param hideFragment This parameter will take fragmnet name which you want to hide.
     */
    public void addFragment(final BaseFragment newFragment, final BaseFragment hideFragment) {
        Util.getInstance().hideSoftKeyboard(this);
        newFragment.setTargetFragment(hideFragment, 1);
        getLocalFragmentManager()
                .beginTransaction()
                .add(R.id.activity_registartion_fl_container, newFragment, newFragment.getClass().getSimpleName())
                .hide(hideFragment)
                .addToBackStack(hideFragment.getClass().getSimpleName())
                .commit();
    }


    public void addFragment(final BaseFragment newFragment) {
        Util.getInstance().hideSoftKeyboard(this);
        getLocalFragmentManager()
                .beginTransaction()
                .add(R.id.activity_registartion_fl_container, newFragment, newFragment.getClass().getSimpleName())
                .addToBackStack(newFragment.getClass().getSimpleName())
                .commit();
    }

    /**
     * removes current fragment from container and replace with the new Fragment recieves in parameter
     *
     * @param newFragment a fragment object that replaces current fragment
     */
    public void replaceFragment(final Fragment newFragment) {
        Util.getInstance().hideSoftKeyboard(this);
        getLocalFragmentManager()
                .beginTransaction()
                .replace(R.id.activity_registartion_fl_container, newFragment, newFragment.getClass().getSimpleName())
                .commit();
    }

}
