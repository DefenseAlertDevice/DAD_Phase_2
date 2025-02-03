package net.tigerlight.dad.home;

import static net.tigerlight.dad.util.AlarmUtils.cancelPeriodicService;
import static net.tigerlight.dad.util.AlarmUtils.setupPeriodicService;

import android.Manifest;

import net.tigerlight.dad.R;
import net.tigerlight.dad.registration.activity.MainActivity;
import net.tigerlight.dad.registration.util.Constant;
import net.tigerlight.dad.util.Preference;
import com.google.firebase.FirebaseApp;
import com.google.firebase.messaging.FirebaseMessaging;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;

import android.util.Log;
import android.widget.Toast;


public class SplashActivity extends AppCompatActivity {

    private static final String PROPERTY_APP_VERSION = "appVersion";
    private static final int REQUEST_PERMISSIONS_REQUEST_CODE = 1000;
    private Handler handler;
    private final int TIME_INTERVAL = 3000;
    private static final int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        askNotificationPermission();
        setHandler();
    }

    private void initialiseFirebase() {
        FirebaseApp.initializeApp(this);
        FirebaseMessaging.getInstance().getToken()
                .addOnCompleteListener(task -> {
                    if (!task.isSuccessful()) {
                        Log.w("firebase", "Fetching FCM registration token failed", task.getException());
                        return;
                    }

                    // Get new FCM registration token
                    String token = task.getResult();
                    storeRegistrationId(token);
                });
    }

    private static int getAppVersion(Context context) {
        try {
            PackageInfo packageInfo = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
            return packageInfo.versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            // should never happen
            throw new RuntimeException("Could not get package name: " + e);
        }
    }

    private void storeRegistrationId(String regId) {
        final int appVersion = getAppVersion(this);
        Preference preference = Preference.getInstance();
        preference.savePreferenceData(preference.KEY_DEVICE_TOKEN, regId);
        preference.savePreferenceData(PROPERTY_APP_VERSION, appVersion);
    }

    /**
     * Handler to start the Runnable Interface after the delay
     * of 3000 seconds
     */
    private void setHandler() {
        handler = new Handler();
        handler.postDelayed(runnable, TIME_INTERVAL);
    }

    /**
     * Starts a Runnable Interface to switch the Splash Screen
     * to Login Screen.
     */
    final Runnable runnable = () -> {
        long time = 1000 * 3;  //For repeating 30 seconds
        boolean isLogin = Preference.getInstance().mSharedPreferences.getBoolean(Constant.IS_LOGIN, false);

        if (isLogin) {
            if (!checkPermissions()) {
                requestPermissions();
                return;
            }
//            startLocationBroadcastService();
//            Intent serviceIntent = new Intent(this, LocationService.class);
//            startService(serviceIntent);
            cancelPeriodicService(this);
            setupPeriodicService(this);

            // Uncomment and update if needed
            // Intent serviceBle = new Intent(getApplicationContext(), BleService.class);
            // PendingIntent pendingIntentBle = PendingIntent.getService(getApplicationContext(), 1001, serviceBle, PendingIntent.FLAG_CANCEL_CURRENT | PendingIntent.FLAG_IMMUTABLE);
            // AlarmManager alarmManagerBle = (AlarmManager) getApplicationContext().getSystemService(Context.ALARM_SERVICE);
            // if (alarmManagerBle != null) {
            //     alarmManagerBle.setRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(), time, pendingIntentBle);
            // }
        }
        final Intent loginIntent = new Intent(SplashActivity.this, MainActivity.class);
        startActivity(loginIntent);
        finish();
    };

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_PERMISSIONS_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
//                startLocationBroadcastService();
            } else {
                Toast.makeText(this, "Permissions denied", Toast.LENGTH_SHORT).show();
            }
        }
    }

//    private void startLocationBroadcastService() {
//        long time = 1000 * 3;  //For repeating 30 seconds
//        Intent serviceIntent = new Intent(this, LocationBroadcastServiceNew.class);
//        PendingIntent pendingIntent = PendingIntent.getService(this, 1001, serviceIntent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
//        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
//        if (alarmManager != null) {
//            alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(), time, pendingIntent);
//        }
//    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        // remove the runnable set with the Handler
        // if the Activity gets destroyed in any case
        if (handler != null) {
            handler.removeCallbacks(runnable);
        }
    }

    private boolean checkPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            return ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED &&
                    ContextCompat.checkSelfPermission(this, Manifest.permission.FOREGROUND_SERVICE) == PackageManager.PERMISSION_GRANTED &&
                    ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_SCAN) == PackageManager.PERMISSION_GRANTED &&
                    ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) == PackageManager.PERMISSION_GRANTED;
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            return ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED &&
                    ContextCompat.checkSelfPermission(this, Manifest.permission.FOREGROUND_SERVICE) == PackageManager.PERMISSION_GRANTED &&
                    ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH) == PackageManager.PERMISSION_GRANTED &&
                    ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_ADMIN) == PackageManager.PERMISSION_GRANTED;
        } else {
            return ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED &&
                    ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH) == PackageManager.PERMISSION_GRANTED &&
                    ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_ADMIN) == PackageManager.PERMISSION_GRANTED;
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

        ActivityCompat.requestPermissions(this, permissions, REQUEST_PERMISSIONS_REQUEST_CODE);
    }

    /**
     * Check the device to make sure it has the Google Play Services APK. If
     * it doesn't, display a dialog that allows users to download the APK from
     * the Google Play Store or enable it in the device's system settings.
     */
    private final ActivityResultLauncher<String> notificationPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (isGranted) {
                    initialiseFirebase();
                }
            });

    private void askNotificationPermission() {
        // This is only necessary for API level >= 33 (TIRAMISU)
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) ==
                PackageManager.PERMISSION_GRANTED) {
            // FCM SDK (and your app) can post notifications.
            initialiseFirebase();
        } else {
            // Directly ask for the permission
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS);
            }
        }
    }

}
