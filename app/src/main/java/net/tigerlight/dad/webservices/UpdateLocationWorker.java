package net.tigerlight.dad.webservices;

import android.content.Context;
import android.util.Log;
import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

public class UpdateLocationWorker extends Worker {
    private static final String TAG = "LocationUpdateWorker";
    private Context mContext;

    public UpdateLocationWorker(@NonNull Context context, @NonNull WorkerParameters params) {
        super(context, params);
        mContext = context;
    }

    @NonNull
    @Override
    public Result doWork() {
        // Get the input data
        double latitude = getInputData().getDouble("latitude", 0.0);
        double longitude = getInputData().getDouble("longitude", 0.0);

        // Perform the location update
        boolean success = updateLocation(latitude, longitude);

        // Return the result
        return success ? Result.success() : Result.failure();
    }

    private boolean updateLocation(double latitude, double longitude) {
        // Your logic to update the location
        Log.d(TAG, "Updating location: lat=" + latitude + ", long=" + longitude);
        // Simulate network call or database operation
        WsCallUpdateLocation wsCallUpdateLocation = new WsCallUpdateLocation(mContext);
        wsCallUpdateLocation.executeService(latitude, longitude);
        return wsCallUpdateLocation.isSuccess();
    }
}