package net.tigerlight.dad.recievers;

import net.tigerlight.dad.LocationBroadcastServiceNew;
import net.tigerlight.dad.blework.BleService;
import net.tigerlight.dad.home.BaseFragment;
import net.tigerlight.dad.registration.activity.MainActivity;
import net.tigerlight.dad.registration.fragment.ContactFragment;
import net.tigerlight.dad.util.Constants;
import net.tigerlight.dad.util.Preference;

import android.Manifest;
import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.util.Log;

import java.util.Arrays;
import java.util.UUID;

import static net.tigerlight.dad.util.CheckForeground.getActivity;

import androidx.core.app.ActivityCompat;

public class BLEHelper {

    private BluetoothAdapter.LeScanCallback mLeScanCallback;
    // private LeDeviceListAdapter mLeDeviceListAdapter;
    private final String TAG = BLEHelper.class.getName();
    final static char[] HEX_ARRAY = "0123456789ABCDEF".toCharArray();
    public static final UUID BATTERY_SERVICE_UUID =
            UUID.fromString("0000180f-0000-1000-8000-00805f9b34fb");
    public static final UUID BATTERY_LEVEL_CHARACTER_UUID =
            UUID.fromString("00002a19-0000-1000-8000-00805f9b34fb");
    public static final boolean IS_AUTO_CONNECT = true;
    public static final double magor = 0;
    public static final double minor = 0;
    public static boolean IsFirst = true;


    @SuppressLint("NewApi")
    public BLEHelper(final BaseFragment settingScreen, final boolean isFromSetting) {

        if (ActivityCompat.checkSelfPermission(settingScreen.getActivity(), Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
            // Permission is not granted, handle accordingly
            return;
        }
        mLeScanCallback = (device, rssi, scanRecord) -> settingScreen.getActivity().runOnUiThread(() -> {

            String msg = "";


            for (byte b : scanRecord)
                msg += String.format("%02x ", b);

            msg = msg.replaceAll("\\s+", "");
            ContactFragment.TEST_UUID = ContactFragment.TEST_UUID.toLowerCase();
            ContactFragment.TEST_UUID_PREVIOUS = ContactFragment.TEST_UUID_PREVIOUS.toLowerCase();


            if (msg.contains(ContactFragment.TEST_UUID)) {
                if (isFromSetting) {
//                                restartActivity();
                } else {
                    ((ContactFragment) settingScreen).sendPushNotification();
                }
            }

            if (msg.contains(ContactFragment.TEST_UUID_PREVIOUS)) {
                if (isFromSetting) {
//                                restartActivity();
//                                ((SettingScreen) settingScreen).bleFound();
                } else {
                    ((ContactFragment) settingScreen).sendPushNotification();
                }
            }
        });
    }

    @SuppressLint("NewApi")
    public BLEHelper(final BleService bleService) {

        if (ActivityCompat.checkSelfPermission(bleService, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
            // Permission is not granted, handle accordingly
            return;
        }
        mLeScanCallback = (device, rssi, scanRecord) -> {

            String msg = "";

            for (byte b : scanRecord)
                msg += String.format("%02x ", b);

            msg = msg.replaceAll("\\s+", "");
            ContactFragment.TEST_UUID = ContactFragment.TEST_UUID.toLowerCase();
            ContactFragment.TEST_UUID_PREVIOUS = ContactFragment.TEST_UUID_PREVIOUS.toLowerCase();


            // Log.v("rss", "" + String.valueOf(rssi));

            // Log.v("Device", "" + device);
            int serialNumber = (scanRecord[25] & 0xFF) << 24 | (scanRecord[26] & 0xFF) << 16 | (scanRecord[27] & 0xFF) << 8 | scanRecord[28] & 0xFF;

            //Log.e("serial number", "Serial Number is " + serialNumber);

            String UUIDHex = convertBytesToHex(Arrays.copyOfRange(scanRecord, 9, 25));
            //Log.d("UUID", UUIDHex);


            if (UUIDHex.equalsIgnoreCase(Constants.NEW_UUID)) {


                Log.d("tigerlight", "found");

                //if (UUIDHex.equals(GELO_UUID)) {
                // Bytes 25 and 26 of the advertisement packet represent
                // the major value
                int major = (scanRecord[25] << 8) | (scanRecord[26] << 0);
                //Log.e("Major", "Serial Number is " + major);


                // Bytes 27 and 28 of the advertisement packet represent
                // the minor
                int minor = ((scanRecord[27] & 0xFF) << 8) | (scanRecord[28] & 0xFF);
                /*Log.d("TAG", "device" + device + " Serial Number is " + serialNumber + " major" + major + " minor" + minor + " rssi" + rssi + "label: " + device.getName());*/
                Preference.getInstance().savePreferenceData(Constants.Preferences.Keys.NEW_UUID_KEY, String.valueOf(UUIDHex));
                Preference.getInstance().savePreferenceData(Constants.Preferences.Keys.NEW_MAJOR_KEY, String.valueOf(major));
                Preference.getInstance().savePreferenceData(Constants.Preferences.Keys.NEW_MINOR_KEY, String.valueOf(minor));


                if (msg.contains(ContactFragment.TEST_UUID) || msg.contains(ContactFragment.TEST_UUID_PREVIOUS)) {
                    bleService.sendPushNotification();
                    //Log.d("ss", "2");

                }


            }

            if (getActivity() != null && ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                return;
            }
            if (UUIDHex.equalsIgnoreCase(Constants.OLD_UUID) && Constants.LAIRD_BEACON_LABEL.equals(device.getName())) {
                //if (UUIDHex.equalsIgnoreCase(Constants.OLD_UUID)) {

                Log.d("Laird iBeacon", "found");
                //if (UUIDHex.equals(GELO_UUID)) {
                // Bytes 25 and 26 of the advertisement packet represent
                // the major value
                int major = (scanRecord[25] << 8) | (scanRecord[26]);
                //Log.e("Major", "Serial Number is " + major);
                // Bytes 27 and 28 of the advertisement packet represent
                // the minor value
                int minor = ((scanRecord[27] & 0xFF) << 8) | (scanRecord[28] & 0xFF);
                /*Log.d("TAG", "device" + device + " Serial Number is " + serialNumber + " major" + major + " minor" + minor + " rssi" + rssi);*/
                Preference.getInstance().savePreferenceData(Constants.Preferences.Keys.OLD_UUID_KEY, String.valueOf(UUIDHex));
                Preference.getInstance().savePreferenceData(Constants.Preferences.Keys.OLD_MAJOR_KEY, String.valueOf(major));
                Preference.getInstance().savePreferenceData(Constants.Preferences.Keys.OLD_MINOR_KEY, String.valueOf(minor));

                if (msg.contains(ContactFragment.TEST_UUID) || msg.contains(ContactFragment.TEST_UUID_PREVIOUS)) {
                    bleService.sendPushNotification();
                }
            }
        };
    }


    @SuppressLint("NewApi")
    public BLEHelper(final LocationBroadcastServiceNew LocationbleService, final boolean isSetting) {
        if (ActivityCompat.checkSelfPermission(LocationbleService, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
            // Permission is not granted, handle accordingly
            return;
        }
        mLeScanCallback = (device, rssi, scanRecord) -> {
            String msg = "";
            IsFirst = isSetting;

            for (byte b : scanRecord)
                msg += String.format("%02x ", b);

            msg = msg.replaceAll("\\s+", "");
            ContactFragment.TEST_UUID = ContactFragment.TEST_UUID.toLowerCase();
            ContactFragment.TEST_UUID_PREVIOUS = ContactFragment.TEST_UUID_PREVIOUS.toLowerCase();


            // Log.v("rss", "" + String.valueOf(rssi));

            // Log.v("Device", "" + device);
            int serialNumber = (scanRecord[25] & 0xFF) << 24 | (scanRecord[26] & 0xFF) << 16 | (scanRecord[27] & 0xFF) << 8 | scanRecord[28] & 0xFF;

            //Log.e("serial number", "Serial Number is " + serialNumber);

            String UUIDHex = convertBytesToHex(Arrays.copyOfRange(scanRecord, 9, 25));
            //Log.d("UUID", UUIDHex);


            if (IsFirst) {
                if (UUIDHex.equalsIgnoreCase(Constants.NEW_UUID)) {
                    IsFirst = false;
                    Log.d("tigerlight", "found");

                    //if (UUIDHex.equals(GELO_UUID)) {
                    // Bytes 25 and 26 of the advertisement packet represent
                    // the major value
                    int major = (scanRecord[25] << 8) | (scanRecord[26] << 0);
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
                        LocationbleService.sendPushNotification();
                        getActivity().startActivity(new Intent(getActivity(), MainActivity.class));
                    } else {
                        Log.e(TAG, "getActivity() = null");
                    }
                }
            }

            if (IsFirst) {

                if (getActivity() != null && ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                    return;
                }
                if (UUIDHex.equalsIgnoreCase(Constants.OLD_UUID) && Constants.LAIRD_BEACON_LABEL.equals(device.getName())) {
                    //if (UUIDHex.equalsIgnoreCase(Constants.OLD_UUID)) {

                    IsFirst = false;
                    Preference.getInstance().mSharedPreferences.getString("IsSecond", "true");


                    Log.d("tigerlight", "found");


                    //if (UUIDHex.equals(GELO_UUID)) {
                    // Bytes 25 and 26 of the advertisement packet represent
                    // the major value
                    int major = (scanRecord[25] << 8) | (scanRecord[26] << 0);
                    //Log.e("Major", "Serial Number is " + major);

                    // Bytes 27 and 28 of the advertisement packet represent
                    // the minor value
                    int minor = ((scanRecord[27] & 0xFF) << 8) | (scanRecord[28] & 0xFF);
                    Log.d("TAG", "device" + device + " Serial Number is " + serialNumber + " major" + major + " minor" + minor + " rssi" + rssi);

                    Preference.getInstance().savePreferenceData(Constants.Preferences.Keys.UUID_KEY, String.valueOf(UUIDHex));
                    Preference.getInstance().savePreferenceData(Constants.Preferences.Keys.MAJOR_KEY, String.valueOf(major));
                    Preference.getInstance().savePreferenceData(Constants.Preferences.Keys.MINOR_KEY, String.valueOf(minor));

                    if (getActivity() != null) {
                        LocationbleService.sendPushNotification();
                        getActivity().startActivity(new Intent(getActivity(), MainActivity.class));
                    } else {
                        Log.e(TAG, "getActivity() = null");
                    }
                }
            }
        };

    }


    public BluetoothAdapter.LeScanCallback getmLeScanCallback() {
        return mLeScanCallback;
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
}
