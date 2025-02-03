//package net.tigerlight.dad.blework;
//
//import android.app.Service;
//import android.bluetooth.BluetoothAdapter;
//import android.content.BroadcastReceiver;
//import android.content.Context;
//import android.content.Intent;
//import android.content.IntentFilter;
//import android.os.IBinder;
//import android.support.annotation.Nullable;
//import android.util.Log;
//
///**
// * Created by indianic on 18/04/17.
// */
//
//public class FromSettingService extends Service {
//    private String TAG=FromSettingService.class.getName();
//
//    @Override
//    public void onCreate() {
//        super.onCreate();
//        Log.e(TAG, "onCreate");
//
//        IntentFilter filter2 = new IntentFilter();
//        filter2.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED);
//        filter2.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
//        filter2.addAction(BluetoothAdapter.ACTION_SCAN_MODE_CHANGED);
//        registerReceiver(mBroadcastReceiver2, filter2);
//        buildGoogleApiClient();
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
//        stopLocationUpdates();
//    }
//
//    @Nullable
//    @Override
//    public IBinder onBind(Intent intent) {
//        return null;
//    }
//    private final BroadcastReceiver mBroadcastReceiver2 = new BroadcastReceiver() {
//
//
//        @Override
//        public void onReceive(Context context, Intent intent) {
//            final String action = intent.getAction();
//
//            if (action.equals(BluetoothAdapter.ACTION_DISCOVERY_STARTED)) {
//                Log.d("TAG_root", "ACTION_DISCOVERY_STARTED");
////                if (CheckDeviceSupportBLE()) {
////                    initialization();
////                }
//
//            }
//
//            if (action.equals(BluetoothAdapter.ACTION_SCAN_MODE_CHANGED)) {
//
//                int mode = intent.getIntExtra(BluetoothAdapter.EXTRA_SCAN_MODE, BluetoothAdapter.ERROR);
//
//                switch (mode) {
//                    case BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE:
//                        Log.d("TAG_2", "SCAN_MODE_CONNECTABLE_DISCOVERABLE");
//
//                        break;
//                    case BluetoothAdapter.SCAN_MODE_CONNECTABLE:
//                        Log.d("TAG_2", "SCAN_MODE_CONNECTABLE");
//
//                        break;
//
//                    case BluetoothAdapter.SCAN_MODE_NONE:
//                        Log.d("TAG_2", "SCAN_MODE_NONE");
//
//                        break;
//                }
//            }
//        }
//    };
//
//}
