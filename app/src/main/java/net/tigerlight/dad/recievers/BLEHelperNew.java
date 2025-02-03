//package net.tigerlight.dad.recievers;
//
//import android.annotation.SuppressLint;
//import android.app.Activity;
//import android.bluetooth.BluetoothAdapter;
//import android.bluetooth.BluetoothDevice;
//import android.bluetooth.BluetoothGatt;
//import android.bluetooth.BluetoothGattCharacteristic;
//import android.bluetooth.BluetoothGattService;
//import android.content.Intent;
//import android.util.Log;
//
//import net.tigerlight.dad.DADApplication;
//import net.tigerlight.dad.blework.BleServiceNew;
//import net.tigerlight.dad.home.BaseFragment;
//import net.tigerlight.dad.registration.activity.MainActivity;
//import net.tigerlight.dad.registration.fragment.ContactFragment;
//import net.tigerlight.dad.util.Preference;
//
//import java.util.Arrays;
//import java.util.UUID;
//
//import static com.crashlytics.android.core.CrashlyticsCore.TAG;
//import static net.tigerlight.dad.util.CheckForeground.getActivity;
//
//public class BLEHelperNew {
//
//    private BluetoothAdapter.LeScanCallback mLeScanCallback;
//
//    // private LeDeviceListAdapter mLeDeviceListAdapter;
//    final static char[] HEX_ARRAY = "0123456789ABCDEF".toCharArray();
//    public static final UUID BATTERY_SERVICE_UUID =
//            UUID.fromString("0000180f-0000-1000-8000-00805f9b34fb");
//    public static final UUID BATTERY_LEVEL_CHARACTER_UUID =
//            UUID.fromString("00002a19-0000-1000-8000-00805f9b34fb");
//
//
//    @SuppressLint("NewApi")
//    public BLEHelperNew(final BaseFragment settingScreen, final boolean isFromSetting) {
//
//
//        mLeScanCallback = new BluetoothAdapter.LeScanCallback() {
//
//            @Override
//            public void onLeScan(final BluetoothDevice device, int rssi, final byte[] scanRecord) {
//
//
//                ((Activity) settingScreen.getActivity()).runOnUiThread(new Runnable() {
//
//                    @Override
//                    public void run() {
//
//                        String msg = "";
//
//
//                        for (byte b : scanRecord)
//                            msg += String.format("%02x ", b);
//
//                        msg = msg.replaceAll("\\s+", "");
//                        ContactFragment.TEST_UUID = ContactFragment.TEST_UUID.toLowerCase();
//                        ContactFragment.TEST_UUID_PREVIOUS = ContactFragment.TEST_UUID_PREVIOUS.toLowerCase();
//
//
//                        if (msg.contains(ContactFragment.TEST_UUID)) {
//                            if (isFromSetting) {
////                                restartActivity();
//                            } else {
//                                ((ContactFragment) settingScreen).sendPushNotification();
//                            }
//                        }
//
//                        if (msg.contains(ContactFragment.TEST_UUID_PREVIOUS)) {
//                            if (isFromSetting) {
////                                restartActivity();
////                                ((SettingScreen) settingScreen).bleFound();
//                            } else {
//                                ((ContactFragment) settingScreen).sendPushNotification();
//                            }
//                        }
//                    }
//
//                });
//            }
//
//        };
//    }
//
//    @SuppressLint("NewApi")
//    public BLEHelperNew(final BleServiceNew bleService) {
//
//        mLeScanCallback = new BluetoothAdapter.LeScanCallback() {
//
//            @Override
//            public void onLeScan(final BluetoothDevice device, int rssi, final byte[] scanRecord) {
//
//                String msg = "";
//
//                for (byte b : scanRecord)
//                    msg += String.format("%02x ", b);
//
//                msg = msg.replaceAll("\\s+", "");
//                ContactFragment.TEST_UUID = ContactFragment.TEST_UUID.toLowerCase();
//                ContactFragment.TEST_UUID_PREVIOUS = ContactFragment.TEST_UUID_PREVIOUS.toLowerCase();
//
//
//                // Log.v("rss", "" + String.valueOf(rssi));

//                // Log.v("Device", "" + device);
//                int serialNumber = (scanRecord[25] & 0xFF) << 24 | (scanRecord[26] & 0xFF) << 16 | (scanRecord[27] & 0xFF) << 8 | scanRecord[28] & 0xFF;
//
//                //Log.e("serial number", "Serial Number is " + serialNumber);
//
//                String UUIDHex = convertBytesToHex(Arrays.copyOfRange(scanRecord, 9, 25));
//                Log.d("UUID", UUIDHex);
//
//
//                if (UUIDHex.equalsIgnoreCase("FD8C0AA6D40411E5AB30625662870761")) {
//
//                    Log.d("tigerlight", "found");
//
//                    //if (UUIDHex.equals(GELO_UUID)) {
//                    // Bytes 25 and 26 of the advertisement packet represent
//                    // the major value
//                    int major = (scanRecord[25] << 8) | (scanRecord[26] << 0);
//                    //Log.e("Major", "Serial Number is " + major);
//
//
//                    // Bytes 27 and 28 of the advertisement packet represent
//                    // the minor
//                    int minor = ((scanRecord[27] & 0xFF) << 8) | (scanRecord[28] & 0xFF);
//                    Log.e("TAG", "device" + device + " Serial Number is " + serialNumber + " major" + major + " minor" + minor + " rssi" + rssi);
//
//                    Preference.getInstance().savePreferenceData("uuid", major);
//                    Preference.getInstance().savePreferenceData("major", major);
//                    Preference.getInstance().savePreferenceData("minor", minor);
//
////                    restartActivity();
//
//
//                    if (!DADApplication.IS_APP_OPEN) {
//
//                        getActivity().startActivity(new Intent(getActivity(), MainActivity.class));
//                        Log.d("ss","1");
//
//                    } else {
//
//                        if (msg.contains(ContactFragment.TEST_UUID) || msg.contains(ContactFragment.TEST_UUID)) {
//                            bleService.sendPushNotification();
//                            Log.d("ss","2");
//
//                        }
//                        Log.d("ss","3");
//
//
//                    }
//
//                    Log.d("ss","4");
//
//
//                }
//
//                if (UUIDHex.equalsIgnoreCase("E2C56DB5DFFB48D2B060D0F5A71096E0")) {
//
//                    Log.d("tigerlight", "found");
//
//                    //if (UUIDHex.equals(GELO_UUID)) {
//                    // Bytes 25 and 26 of the advertisement packet represent
//                    // the major value
//                    int major = (scanRecord[25] << 8) | (scanRecord[26] << 0);
//                    //Log.e("Major", "Serial Number is " + major);
//
//                    // Bytes 27 and 28 of the advertisement packet represent
//                    // the minor value
//                    int minor = ((scanRecord[27] & 0xFF) << 8) | (scanRecord[28] & 0xFF);
//                    Log.e("TAG", "device" + device + " Serial Number is " + serialNumber + " major" + major + " minor" + minor + " rssi" + rssi);
//
//
//                    Preference.getInstance().savePreferenceData("major_previous", major);
//                    Preference.getInstance().savePreferenceData("minor_previous", minor);
//                    getBattery();
//
//
////                    restartActivity();
//
//
//                    if (!DADApplication.IS_APP_OPEN) {
//
//                        getActivity().startActivity(new Intent(getActivity(), MainActivity.class));
//
//                    } else {
//                        if (msg.contains(ContactFragment.TEST_UUID_PREVIOUS) || msg.contains(ContactFragment.TEST_UUID_PREVIOUS)) {
//                            bleService.sendPushNotification();
//                        }
//                    }
//
//
//                }
//
//
//                //  showNotification("Becon Detected", "Happy shopping !!!");
//                // mLeDeviceListAdapter.addDevice(device);
//                // mLeDeviceListAdapter.notifyDataSetChanged();
//
////
////                if (msg.contains(ContactFragment.TEST_UUID) || msg.contains(ContactFragment.TEST_UUID)) {
////                    bleService.sendPushNotification();
////                }
//
////
////                if (msg.contains(ContactFragment.TEST_UUID_PREVIOUS) || msg.contains(ContactFragment.TEST_UUID_PREVIOUS)) {
////                    bleService.sendPushNotification();
////                }
//            }
//
//        };
//
//    }
//
//
//    @SuppressLint("NewApi")
////    public BLEHelper(final BleServiceScan bleServiceScan) {
////
////        mLeScanCallback = new BluetoothAdapter.LeScanCallback() {
////
////            @Override
////            public void onLeScan(final BluetoothDevice device, int rssi, final byte[] scanRecord) {
////
////                String msg = "";
////
////                for (byte b : scanRecord)
////                    msg += String.format("%02x ", b);
////
////                msg = msg.replaceAll("\\s+", "");
////                ContactFragment.TEST_UUID = ContactFragment.TEST_UUID.toLowerCase();
////                ContactFragment.TEST_UUID_PREVIOUS = ContactFragment.TEST_UUID_PREVIOUS.toLowerCase();
////
////
////                // Log.v("rss", "" + String.valueOf(rssi));
////
////                // Log.v("Device", "" + device);
////                int serialNumber = (scanRecord[25] & 0xFF) << 24 | (scanRecord[26] & 0xFF) << 16 | (scanRecord[27] & 0xFF) << 8 | scanRecord[28] & 0xFF;
////
////                //Log.e("serial number", "Serial Number is " + serialNumber);
////
////                String UUIDHex = convertBytesToHex(Arrays.copyOfRange(scanRecord, 9, 25));
////                Log.d("UUID", UUIDHex);
////
////
////                if (UUIDHex.equalsIgnoreCase("FD8C0AA6D40411E5AB30625662870761")) {
////
////                    Log.d("tigerlight", "found");
////
////                    //if (UUIDHex.equals(GELO_UUID)) {
////                    // Bytes 25 and 26 of the advertisement packet represent
////                    // the major value
////                    int major = (scanRecord[25] << 8) | (scanRecord[26] << 0);
////                    //Log.e("Major", "Serial Number is " + major);
////
////
////                    // Bytes 27 and 28 of the advertisement packet represent
////                    // the minor
////                    int minor = ((scanRecord[27] & 0xFF) << 8) | (scanRecord[28] & 0xFF);
////                    Log.e("TAG", "device" + device + " Serial Number is " + serialNumber + " major" + major + " minor" + minor + " rssi" + rssi);
////
////                    Preference.getInstance().savePreferenceData("uuid", major);
////                    Preference.getInstance().savePreferenceData("major", major);
////                    Preference.getInstance().savePreferenceData("minor", minor);
////
//////                    restartActivity();
////
////
////                    if (!DADApplication.IS_APP_OPEN) {
////
////                        getActivity().startActivity(new Intent(getActivity(), MainActivity.class));
////
////                    } else {
////
////                        if (msg.contains(ContactFragment.TEST_UUID) || msg.contains(ContactFragment.TEST_UUID)) {
//////                            bleServiceScan.sendPushNotification();
////                        }
////
////                    }
////
////
////                }
////
////                if (UUIDHex.equalsIgnoreCase("E2C56DB5DFFB48D2B060D0F5A71096E0")) {
////
////                    Log.d("tigerlight", "found");
////
////                    //if (UUIDHex.equals(GELO_UUID)) {
////                    // Bytes 25 and 26 of the advertisement packet represent
////                    // the major value
////                    int major = (scanRecord[25] << 8) | (scanRecord[26] << 0);
////                    //Log.e("Major", "Serial Number is " + major);
////
////                    // Bytes 27 and 28 of the advertisement packet represent
////                    // the minor value
////                    int minor = ((scanRecord[27] & 0xFF) << 8) | (scanRecord[28] & 0xFF);
////                    Log.e("TAG", "device" + device + " Serial Number is " + serialNumber + " major" + major + " minor" + minor + " rssi" + rssi);
////
////
////                    Preference.getInstance().savePreferenceData("major_previous", major);
////                    Preference.getInstance().savePreferenceData("minor_previous", minor);
////                    getBattery();
////
////
//////                    restartActivity();
////
////
////                    if (!DADApplication.IS_APP_OPEN) {
////
////                        getActivity().startActivity(new Intent(getActivity(), MainActivity.class));
////
////                    } else {
////                        if (msg.contains(ContactFragment.TEST_UUID_PREVIOUS) || msg.contains(ContactFragment.TEST_UUID_PREVIOUS)) {
//////                            bleService.sendPushNotification();
////                        }
////                    }
////
////
////                }
////
////
////                //  showNotification("Becon Detected", "Happy shopping !!!");
////                // mLeDeviceListAdapter.addDevice(device);
////                // mLeDeviceListAdapter.notifyDataSetChanged();
////
//////
//////                if (msg.contains(ContactFragment.TEST_UUID) || msg.contains(ContactFragment.TEST_UUID)) {
//////                    bleService.sendPushNotification();
//////                }
////
//////
//////                if (msg.contains(ContactFragment.TEST_UUID_PREVIOUS) || msg.contains(ContactFragment.TEST_UUID_PREVIOUS)) {
//////                    bleService.sendPushNotification();
//////                }
////            }
////
////        };
////
////    }
//
//
//    public BluetoothAdapter.LeScanCallback getmLeScanCallback() {
//        return mLeScanCallback;
//    }
//
//    private String convertBytesToHex(byte[] bytes) {
//        char[] hex = new char[bytes.length * 2];
//        for (int i = 0; i < bytes.length; i++) {
//            int v = bytes[i] & 0xFF;
//            hex[i * 2] = HEX_ARRAY[v >>> 4];
//            hex[i * 2 + 1] = HEX_ARRAY[v & 0x0F];
//        }
//
//        return new String(hex);
//    }
//
//    public void getBattery() {
//
//        BluetoothGatt mBluetoothGatt = null;
//
//        if (mBluetoothGatt == null) {
//            Log.e(TAG, "lost connection");
//        }
//
//        BluetoothGattService batteryService = mBluetoothGatt.getService(BATTERY_SERVICE_UUID);
//        if (batteryService == null) {
//            Log.d(TAG, "Battery service not found!");
//            return;
//        }
//
//        BluetoothGattCharacteristic batteryLevel = batteryService.getCharacteristic(BATTERY_LEVEL_CHARACTER_UUID);
//        if (batteryLevel == null) {
//            Log.d(TAG, "Battery level not found!");
//            return;
//        }
//
//        mBluetoothGatt.readCharacteristic(batteryLevel);
//    }
//
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
