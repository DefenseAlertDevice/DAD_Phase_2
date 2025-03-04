package net.tigerlight.dad.util;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.widget.Toast;

import net.tigerlight.dad.R;


/**
 * Created by B.S on 15/04/16.
 * Purpose of this Class is to check internet connection of phone and perform actions on user's input
 */
public class NetworkAvailability {
    /**
     * Checks internet network connection.
     *
     * @param mContext   Activity context
     * @param message    if want to show connection message to user then true, false otherwise.
     * @param isToast    if want to show toast then true else shows alert dialog with buttons.
     * @param goSettings if want to go action setting for connection then true, otherwise only OK button.
     * @return if network connectivity exists or is in the process of being established, false otherwise.
     */
    public static boolean isOnline(final Activity mContext, boolean message, boolean isToast, boolean goSettings) {
        if (!mContext.isFinishing()) {
            final ConnectivityManager mConnectivityManager = (ConnectivityManager) mContext.getSystemService(Context.CONNECTIVITY_SERVICE);
            final NetworkInfo netInfo = mConnectivityManager.getActiveNetworkInfo();

            if (netInfo != null) {
                if (netInfo.isConnectedOrConnecting()) {
                    return true;
                }
            }

            if (message) {
                if (isToast) {
                    Toast.makeText(mContext, mContext.getString(R.string.alert_check_connection), Toast.LENGTH_SHORT).show();
                } else {
                    final AlertDialog.Builder dialog = new AlertDialog.Builder(mContext);

                    dialog.setTitle(mContext.getString(R.string.app_name));
                    dialog.setCancelable(false);
                    dialog.setMessage(mContext.getString(R.string.alert_check_connection));

                    if (goSettings) {
                        dialog.setPositiveButton(mContext.getString(R.string.settings), new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.dismiss();
                                mContext.startActivity(new Intent(android.provider.Settings.ACTION_SETTINGS));
                            }
                        });

                        dialog.setNegativeButton(mContext.getString(R.string.cancel), new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.dismiss();
                            }
                        });

                    } else {
                        dialog.setNeutralButton(mContext.getString(R.string.ok), new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.dismiss();
                            }
                        });
                    }
                    dialog.show();
                }

                return false;
            }
        }
        return false;
    }

    /**
     * Checks if phone is connected to network or not.
     *
     * @param mContext required for creating AlertDialog and checking phone state.
     * @return true if phone is connected to internet otherwise false.
     */
    private boolean isNetworkAvailable(final Context mContext) {

        boolean isNetAvailable = false;
        if (mContext != null) {
            final ConnectivityManager mConnectivityManager = (ConnectivityManager) mContext.getSystemService(Context.CONNECTIVITY_SERVICE);
            if (mConnectivityManager != null) {
                final NetworkInfo activeNetwork = mConnectivityManager.getActiveNetworkInfo();
                isNetAvailable = activeNetwork != null && activeNetwork.isConnected();
            }
        }
        return isNetAvailable;
    }
}
