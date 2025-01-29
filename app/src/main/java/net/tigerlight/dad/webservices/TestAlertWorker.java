package net.tigerlight.dad.webservices;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

public class TestAlertWorker extends Worker {
    private static final String TAG = "TestAlertWorker";
    private Context mContext;

    public TestAlertWorker(@NonNull Context context, @NonNull WorkerParameters params) {
        super(context, params);
        mContext = context;
    }

    @NonNull
    @Override
    public Result doWork() {
        String uuid = getInputData().getString("uuid");
        String identifier = getInputData().getString("identifier");

        // Perform the location update
        boolean success = makeServiceCall(uuid, identifier);

        // Return the result
        return success ? Result.success() : Result.failure();
    }

    private boolean makeServiceCall(String uuid, String identifier) {
        // Your logic to update the location
        Log.d(TAG, "Sending test alert");
        // Simulate network call or database operation
        WsCallDADTest wsCall = new WsCallDADTest(mContext);
        wsCall.executeService(uuid, identifier);
        return wsCall.isSuccess();
    }
}