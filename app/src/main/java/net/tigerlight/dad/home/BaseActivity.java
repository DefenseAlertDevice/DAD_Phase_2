package net.tigerlight.dad.home;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;

import net.tigerlight.dad.R;
import net.tigerlight.dad.util.CheckForeground;
import net.tigerlight.dad.util.Util;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.os.SystemClock;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentManager;

import android.util.Log;
import android.view.View;
import android.widget.Toast;

public class BaseActivity extends AppCompatActivity implements View.OnClickListener {

    private static final int MY_PERMISSIONS_REQUEST_LOCATION = 1001;
    private static final int MY_PERMISSIONS_REQUEST_BLUETOOTH = 1002;

    private long mLastClickTime = 0;
    private int MAX_CLICK_INTERVAL = 500;

    public static final long UPDATE_INTERVAL_IN_MILLISECONDS = 1 * 1000;
    public static final long FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS = UPDATE_INTERVAL_IN_MILLISECONDS / 2;

    protected static final String TAG = "location-updates-sample";
    protected final static String LOCATION_KEY = "location-key";

    protected LocationRequest mLocationRequest;
    protected Location mCurrentLocation;

    private double latitude;
    private double longitude;
    private boolean isLogin;

    private String longtdLastKnown;
    private String lattdLastKnown;
    private LocationSettingsRequest.Builder builder;
    private String currentlyVisibleFragmentName = "";

    /**
     * Provides the entry point to Google Play services.
     */
    private GoogleApiClient googleApiClient;

    /**
     * Request code for Location settings dialog
     */
    private final int LOCATION_REQUEST_CHECK_SETTINGS = 1000;

    /**
     * Get the latitude of user's last known location
     *
     * @return latitude of user's last known location
     */
    public double getLatitude() {
        return latitude;
    }

    /**
     * Get the longitude of user's last known location
     *
     * @return longitude of user's last known location
     */
    public double getLongitude() {
        return longitude;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //updateValuesFromBundle(savedInstanceState);
        //buildGoogleApiClient();
        //createLocationRequest();
        //checkGPS();
        //updateLatLong();

        buildGoogleApiClient();

       /* getLocalFragmentManager().addOnBackStackChangedListener(new FragmentManager.OnBackStackChangedListener() {
            @Override
            public void onBackStackChanged() {
                Fragment fragment = getLocalFragmentManager().findFragmentById(R.id.activity_registartion_fl_container);

                if (fragment != null) {
                    currentlyVisibleFragmentName = fragment.getTag();

                    if (fragment instanceof BaseFragment) {
                        fragment.onResume();
                    }
                }

            }
        });*/
    }

    /**
     * Builds a GoogleApiClient. Uses the {@code #addApi} method to request the
     * LocationServices API.
     */
    protected synchronized void buildGoogleApiClient() {
        googleApiClient = new GoogleApiClient.Builder(this)
                .addApi(LocationServices.API)
                .build();
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.BLUETOOTH_CONNECT},
                        MY_PERMISSIONS_REQUEST_BLUETOOTH);
            }
        }
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    MY_PERMISSIONS_REQUEST_LOCATION);
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        } else {
//            createLocationRequest();
        }
    }


    @Override
    public void onClick(View v) {
        Util.getInstance().hideSoftKeyboard(this);
        /**
         * Logic to Prevent the Launch of the Fragment Twice if User makes
         * the Tap(Click) very Fast.
         */
        if (SystemClock.elapsedRealtime() - mLastClickTime < MAX_CLICK_INTERVAL) {
            return;
        }
        mLastClickTime = SystemClock.elapsedRealtime();
    }

    /**
     * Updates fields based on data stored in the bundle.
     *
     * @param savedInstanceState The activity state saved in the Bundle.
     */
    private void updateValuesFromBundle(Bundle savedInstanceState) {
        Log.i(TAG, "Updating values from bundle");
        if (savedInstanceState != null) {
            if (savedInstanceState.keySet().contains(LOCATION_KEY)) {
                mCurrentLocation = savedInstanceState.getParcelable(LOCATION_KEY);
                if (mCurrentLocation != null) {
                    longitude = mCurrentLocation.getLongitude();
                    latitude = mCurrentLocation.getLatitude();
                }
            }
        }
    }


    public void updateLatLong() {
//        gpsTracker = new GPSTracker(BaseActivity.this);
//        if (gpsTracker.canGetLocation()) {
//            lattdLastKnown = "" + gpsTracker.getLatitude();
//            longtdLastKnown = "" + gpsTracker.getLongitude();
//            latitude=gpsTracker.getLatitude();
//            longitude=gpsTracker.getLongitude();
//        }
    }

//    public void createLocationRequest()
//    {
//        createLocationRequest(LocationBroadcastServiceNew.UPDATE_INTERVAL_IN_MILLISECONDS, LocationBroadcastServiceNew.FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS, LocationBroadcastServiceNew.DEFAULT_SMALLEST_DISPLACEMENT_DISTANCE_IN_METERS);
//    }

    /**
     * Sets up the location request. Android has two location request settings:
     * {@code ACCESS_COARSE_LOCATION} and {@code ACCESS_FINE_LOCATION}. These settings control
     * the accuracy of the current location. This sample uses ACCESS_FINE_LOCATION, as defined in
     * the AndroidManifest.xml.
     * <p/>
     * When the ACCESS_FINE_LOCATION setting is specified, combined with a fast update
     * interval (5 seconds), the Fused Location Provider API returns location updates that are
     * accurate to within a few feet.
     * <p/>
     * These settings are appropriate for mapping applications that show real-time location
     * updates.
     */
//    public void createLocationRequest(long interval, long fastestInterval, float smallestDisplacement) {
//        final LocationRequest locationRequest = new LocationRequest();
//
//        // Sets the desired interval for active location updates. This interval is
//        // inexact. You may not receive updates at all if no location sources are available, or
//        // you may receive them slower than requested. You may also receive updates faster than
//        // requested if other applications are requesting location at a faster interval.
//        locationRequest.setInterval(interval);
//
//        // Sets the fastest rate for active location updates. This interval is exact, and your
//        // application will never receive updates faster than this value.
//        locationRequest.setFastestInterval(fastestInterval);
//        locationRequest.setSmallestDisplacement(smallestDisplacement);
//
//        //locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
//        final LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder().addLocationRequest(locationRequest);
//        builder.setAlwaysShow(true);
//        final PendingResult<LocationSettingsResult> result = LocationServices.SettingsApi.checkLocationSettings(googleApiClient, builder.build());
//
//        result.setResultCallback(result1 -> {
//            final Status status = result1.getStatus();
//            final LocationSettingsStates state = result1.getLocationSettingsStates();
//            switch (status.getStatusCode()) {
//                case LocationSettingsStatusCodes.SUCCESS:
//                    // All location settings are satisfied. The client can initialize location
//                    // requests here.
//                    break;
//                case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
//                    // Location settings are not satisfied. But could be fixed by showing the user
//                    // a dialog.
//                    try {
//                        // Show the dialog by calling startResolutionForResult(),
//                        // and check the result in onActivityResult().
//                        status.startResolutionForResult(BaseActivity.this, LOCATION_REQUEST_CHECK_SETTINGS);
//                    } catch (IntentSender.SendIntentException e) {
//                        // Ignore the error.
//                    }
//                    break;
//                case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
//                    // Location settings are not satisfied. However, we have no way to fix the
//                    // settings so we won't show the dialog.
//                    break;
//            }
//        });
//
//    }


    /**
     * Submits the request for location updates from google api services
     * <p>this method must be called in onResume method.
     * It is very heave operation so use with caution
     */
    protected void startLocationUpdates() {
        //LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
    }

    /**
     * stops location updates from google api services
     * <p>this method must be called in onPause method.
     * It is very heave operation so must called this method in onPause method.
     */
    protected void stopLocationUpdates() {
        //LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
    }

    @Override
    protected void onStart() {
        super.onStart();
        //mGoogleApiClient.connect();
    }

    @Override
    public void onResume() {
        super.onResume();
        CheckForeground.onResume(this);
//        if (mGoogleApiClient.isConnected()) {
//            startLocationUpdates();
//        }
    }

    @Override
    protected void onPause() {
        super.onPause();
       CheckForeground.onPause();
//        if (mGoogleApiClient.isConnected()) {
//            stopLocationUpdates();
//        }
    }

    @Override
    protected void onStop() {
//        mGoogleApiClient.disconnect();
        super.onStop();
    }

//    @Override
//    public void onConnected(Bundle connectionHint) {
//        Log.i(TAG, "Connected to GoogleApiClient");
//        if (mCurrentLocation == null) {
//            mCurrentLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
//            if (mCurrentLocation != null) {
//                longitude = mCurrentLocation.getLongitude();
//                latitude = mCurrentLocation.getLatitude();
//                if (longitude != 0.0 && latitude != 0.0) {
//                }
//            }
//        }
//        startLocationUpdates();
//    }
//
//    @Override
//    public void onLocationChanged(Location location) {
//        mCurrentLocation = location;
//        longitude = mCurrentLocation.getLongitude();
//        latitude = mCurrentLocation.getLatitude();
//        if (longitude != 0.0 && latitude != 0.0) {
//            callLocationUpdateService(longitude,latitude);
//        }
//    }
//
//    @Override
//    public void onConnectionSuspended(int cause) {
//        Log.i(TAG, "Connection suspended");
//        mGoogleApiClient.connect();
//    }
//
//    @Override
//    public void onConnectionFailed(ConnectionResult result) {
//        Log.i(TAG, "Connection failed: ConnectionResult.getErrorCode() = " + result.getErrorCode());
//    }

    public void onSaveInstanceState(Bundle savedInstanceState) {
        savedInstanceState.putParcelable(LOCATION_KEY, mCurrentLocation);
        super.onSaveInstanceState(savedInstanceState);
    }

//    /**
//     * checks the location is on or not in phone
//     * <p>
//     * This method will check the location is on or not,
//     * and display alert dialog for user's input application reacts
//     * </p>
//     */
//    public void checkGPS() {
//        if (mGoogleApiClient != null) {
//            PendingResult<LocationSettingsResult> result = LocationServices.SettingsApi.checkLocationSettings(mGoogleApiClient, builder.build());
//            result.setResultCallback(new ResultCallback<LocationSettingsResult>() {
//                @Override
//                public void onResult(LocationSettingsResult result) {
//                    final Status status = result.getStatus();
//                    final LocationSettingsStates state = result.getLocationSettingsStates();
//                    switch (status.getStatusCode()) {
//                        case LocationSettingsStatusCodes.SUCCESS:
//                            //                            startLocationService();
//                            createLocationRequest();
//                            //                            startNextActivity();
//                            break;
//                        case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
//                            try {
//                                status.startResolutionForResult(BaseActivity.this, 1000);
//                            } catch (IntentSender.SendIntentException e) {
//                                e.printStackTrace();
//                            }
//                            break;
//                        case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
//                            break;
//                    }
//                }
//            });
//        }
//    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        //final LocationSettingsStates states = LocationSettingsStates.fromIntent(intent);
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {

            case MY_PERMISSIONS_REQUEST_LOCATION:
//                createLocationRequest();
                break;
            case LOCATION_REQUEST_CHECK_SETTINGS:
                switch (resultCode) {
                    case RESULT_OK:
                        // All required changes were successfully made
                        Toast.makeText(this, getString(R.string.TAG_GPS_MSG), Toast.LENGTH_SHORT).show();

                        break;
                    case RESULT_CANCELED:
                        // The user was asked to change settings, but chose not to
                        Toast.makeText(this, getString(R.string.TAG_GPS_ONLINE), Toast.LENGTH_SHORT).show();
                        finish();

                        break;
                    default:
                        break;
                }
                break;
        }
    }

    /**
     * Gets the fragment manager object of activity required for fragment transaction
     * <p>This method can be customised on the need of application,in which it returns {@link FragmentManager} or {@link FragmentManager}</p>
     *
     * @return object of {@link FragmentManager} or {@link FragmentManager}
     */
    protected FragmentManager getLocalFragmentManager() {
        return this.getSupportFragmentManager();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }


}
