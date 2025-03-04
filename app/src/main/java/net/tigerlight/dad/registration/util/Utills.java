package net.tigerlight.dad.registration.util;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.location.Address;
import android.location.Geocoder;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import androidx.core.content.ContextCompat;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;

import net.tigerlight.dad.R;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static net.tigerlight.dad.util.CheckForeground.getActivity;

public class Utills {

    private static final String TAG = Utills.class.getSimpleName();

    public static final int MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE = 123;
    public static Matcher MATCHER;

    public static final String PASSWORD_PATTERN = "((?=.*\\d)(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%]).{8,20})";

    static Pattern PATTERN = Pattern.compile(PASSWORD_PATTERN);

    public static File mFileTemp;
    public static final String TEMP_PHOTO_FILE_NAME = "temp_photo.png";


    public static void displayDialogWithCallbacks(
            final Activity context,
            final String title,
            final String msg,
            final String positiveText,
            final DialogInterface.OnClickListener positiveCallback,
            final String negativeText,
            final DialogInterface.OnClickListener negativeCallback,
            final String neutralText,
            final DialogInterface.OnClickListener neutralCallback) {

        // Create an AlertDialog.Builder
        LayoutInflater inflater = context.getLayoutInflater();
        View customView = inflater.inflate(R.layout.custom_dialog_v2, null);

        // Create the AlertDialog.Builder
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(context);
        dialogBuilder.setView(customView); // Set custom layout
        dialogBuilder.setCancelable(true);

        AlertDialog dialog = dialogBuilder.create();
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }
        // Find and configure buttons in the custom layout
        TextView titleText = customView.findViewById(R.id.dialog_title);
        titleText.setText(title);
        TextView msgText = customView.findViewById(R.id.dialog_message);
        msgText.setText(msg);
        Button positiveButton = customView.findViewById(R.id.dialog_action1);
        Button negativeButton = customView.findViewById(R.id.dialog_action2);
        Button neutralButton = customView.findViewById(R.id.dialog_action3);
        View positiveDivider = customView.findViewById(R.id.dialog_divider1);
        View negativeDivider = customView.findViewById(R.id.dialog_divider2);
        View neutralDivider = customView.findViewById(R.id.dialog_divider3);

        if (positiveText != null && positiveCallback != null) {
            positiveButton.setText(positiveText);
            positiveButton.setVisibility(View.VISIBLE);
            positiveButton.setOnClickListener(v -> {
                positiveCallback.onClick(dialog, DialogInterface.BUTTON_POSITIVE);
                dialog.dismiss(); // Optional: dismiss dialog here
            });
        } else {
            positiveDivider.setVisibility(View.GONE);
            positiveButton.setVisibility(View.GONE);
        }

        if (negativeText != null && negativeCallback != null) {
            negativeButton.setText(negativeText);
            negativeButton.setVisibility(View.VISIBLE);
            negativeButton.setOnClickListener(v -> {
                negativeCallback.onClick(dialog, DialogInterface.BUTTON_NEGATIVE);
                dialog.dismiss(); // Optional: dismiss dialog here
            });
        } else {
            negativeDivider.setVisibility(View.GONE);
            negativeButton.setVisibility(View.GONE);
        }

        if (neutralText != null && neutralCallback != null) {
            neutralButton.setText(neutralText);
            neutralButton.setVisibility(View.VISIBLE);
            neutralButton.setOnClickListener(v -> {
                neutralCallback.onClick(dialog, DialogInterface.BUTTON_NEUTRAL);
                dialog.dismiss(); // Optional: dismiss dialog here
            });
        } else {
            neutralDivider.setVisibility(View.GONE);
            neutralButton.setVisibility(View.GONE);
        }

        // Show the dialog
        dialog.show();
    }

    public static void displayDefaultDialog(
            final Activity context,
            final String title,
            final String msg,
            final String positiveText,
            final DialogInterface.OnClickListener positiveCallback,
            final String negativeText,
            final DialogInterface.OnClickListener negativeCallback,
            final String neutralText,
            final DialogInterface.OnClickListener neutralCallback) {

        // Create an AlertDialog.Builder
        // Create the AlertDialog.Builder
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(context);
        dialogBuilder.setTitle(title);
        dialogBuilder.setMessage(msg);
        dialogBuilder.setCancelable(true);

        if (positiveText != null && positiveCallback != null) {
            dialogBuilder.setPositiveButton(positiveText, positiveCallback);
        }

        if (negativeText != null && negativeCallback != null) {
            dialogBuilder.setNegativeButton(negativeText, negativeCallback);
        }

        if (neutralCallback != null && neutralText != null) {
            dialogBuilder.setNeutralButton(neutralText, neutralCallback);
        }
        AlertDialog dialog = dialogBuilder.create();
        dialog.show();
    }

    public static void displayDialog(final Activity context, final String title, final String msg, final String strPositiveText, final String strNegativeText,
                                     final boolean isNagativeBtn, final boolean isFinish) {
        final AlertDialog.Builder dialog = new AlertDialog.Builder(context);
        dialog.setTitle(title);
        dialog.setCancelable(false);
        dialog.setMessage(msg);
        dialog.setPositiveButton(strPositiveText, (dialog12, id) -> {
            dialog12.dismiss();
            if (isFinish) {
                context.finish();
            }
        });
        if (isNagativeBtn) {
            dialog.setNegativeButton(strNegativeText, (dialog1, id) -> dialog1.dismiss());
        }
        dialog.show();
    }

    public static void displayCustomDialog(final Context context, final String title, final String msg, final String strPositiveText, final String strNegativeText) {
        final Dialog dialog = new Dialog(getActivity());
        dialog.setContentView(R.layout.custom_dialog);
        final TextView tvTitle = (TextView) dialog.findViewById(R.id.dialog_tvTitle);
        tvTitle.setText(title);
        final TextView tvMessage = (TextView) dialog.findViewById(R.id.dialog_tvMessage);
        tvMessage.setText(msg);
        final TextView tvPosButton = (TextView) dialog.findViewById(R.id.dialog_tvPosButton);
        tvPosButton.setText(strPositiveText);
        final TextView tvNegButton = (TextView) dialog.findViewById(R.id.dialog_tvNegButton);
        tvNegButton.setText(strNegativeText);
        tvNegButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
            }
        });
        dialog.show();
    }


    /**
     * *******************************************************************************
     * <p/>
     * method Name: isOnline
     * Created By: lbamarnani
     * Created Date: 21-aug-15
     * Modified By:
     * Modified Date:
     * Purpose: this method is   used  for  check internet connection.
     * <p/>
     * **********************************************************************************
     */
    public static boolean isOnline(final Activity context, boolean message) {
        ConnectivityManager mConnectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = mConnectivityManager.getActiveNetworkInfo();
        if (netInfo != null) {
            if (netInfo.isConnectedOrConnecting()) {
                return true;
            }
        }
        if (message) {
            final AlertDialog.Builder dialog = new AlertDialog.Builder(context);
            dialog.setTitle(context.getString(R.string.app_name));
            dialog.setCancelable(false);
            dialog.setMessage(context.getString(R.string.TAG_INTERNET_AVAILABILITY));
            dialog.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    dialog.dismiss();
                    context.startActivity(new Intent(android.provider.Settings.ACTION_SETTINGS));
                }
            });
            dialog.setNegativeButton(context.getString(android.R.string.cancel), new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    dialog.dismiss();
                }
            });
            dialog.show();

            return false;
        }
        return false;
    }


    /**
     * *******************************************************************************
     * <p/>
     * method Name: isInternetAvailable
     * Created By: lbamarnani
     * Created Date: 21-aug-15
     * Modified By:
     * Modified Date:
     * Purpose: this method is   used  for  check internet connection.
     * <p/>
     * **********************************************************************************
     */

    public static boolean isInternetAvailable(Context context) {

        boolean haveConnectedWifi = false;
        boolean haveConnectedMobile = false;

        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo[] netInfo = cm.getAllNetworkInfo();
        for (NetworkInfo ni : netInfo) {

            if (ni.getTypeName().equalsIgnoreCase("WIFI")) {

                if (ni.isConnected()) {

                    haveConnectedWifi = true;
                    System.out.println("WIFI CONNECTION AVAILABLE");
                } else {

                    System.out.println("WIFI CONNECTION NOT AVAILABLE");

                }
            }
            if (ni.getTypeName().equalsIgnoreCase("MOBILE")) {

                if (ni.isConnected()) {

                    haveConnectedMobile = true;
                    System.out.println("MOBILE INTERNET CONNECTION AVAILABLE");
                } else {

                    System.out.println("MOBILE INTERNET CONNECTION NOT AVAILABLE");
                }
            }
        }

        Log.v(TAG, "Connection avail WIFI : " + haveConnectedWifi + "\n" + "Mobile : " + haveConnectedMobile);
        return haveConnectedWifi || haveConnectedMobile;
    }

    public static void displayDialogNormalMessage(String title, String msg, final Context context) {

        final AlertDialog.Builder alertDialog = new AlertDialog.Builder(context);
        alertDialog.setTitle(title);
        alertDialog.setCancelable(false);
        alertDialog.setMessage(msg);
        alertDialog.setNeutralButton(context.getString(R.string.ok), new DialogInterface.OnClickListener() {

            public void onClick(DialogInterface dialog, int which) {

                dialog.dismiss();
            }
        });

        final AlertDialog dialog = alertDialog.create();
        if (!((Activity) context).isFinishing()) {

            if (!dialog.isShowing()) {
                alertDialog.show();
            }
        }
    }


    public static boolean isInternetConnected(final Context context) {
        ConnectivityManager connec = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connec.getNetworkInfo(0).getState() == NetworkInfo.State.CONNECTED//
                || connec.getNetworkInfo(0).getState() == NetworkInfo.State.CONNECTING //
                || connec.getNetworkInfo(1).getState() == NetworkInfo.State.CONNECTING//
                || connec.getNetworkInfo(1).getState() == NetworkInfo.State.CONNECTED) {
            return true;
        }

        return false;
    }

    /**
     * @param inputEmail
     * @return
     * @purpose validate email
     */
    public final static boolean isValidEmail(CharSequence inputEmail) {
        if (inputEmail == null) {
            return false;
        } else {
            return android.util.Patterns.EMAIL_ADDRESS.matcher(inputEmail).matches();
        }
    }

    public final static boolean isValidPhone(CharSequence inputEmail) {
        if (inputEmail == null) {
            return false;
        } else {
            return Patterns.PHONE.matcher(inputEmail).matches();
        }
    }


    public final static boolean validatePassword(final String password) {

        MATCHER = PATTERN.matcher(password);
        return MATCHER.matches();
    }

    /**
     * Called to check permission(In Android M and above versions only)
     *
     * @param permission, which we need to pass
     * @return true, if permission is granted else false
     */
    public static boolean checkForPermission(final Context context, final String permission) {
        final int result = ContextCompat.checkSelfPermission(context, permission);
        //If permission is granted then it returns 0 as result
        if (result == PackageManager.PERMISSION_GRANTED) {
            return true;
        } else {
            return false;
        }
    }

/*
    private void clearBackStack() {
    mFragmentManager.popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
        FragmentManager manager = getSupportFragmentManager();
        if (manager.getBackStackEntryCount() > 0) {
            FragmentManager.BackStackEntry first = manager.getBackStackEntryAt(0);
            manager.popBackStack(first.getId(), FragmentManager.POP_BACK_STACK_INCLUSIVE);
        }
    }*/

   /* public final static boolean validCellPhone(String number)
    {
        return android.util.Patterns.PHONE.matcher(number).matches();
    }

    public static final boolean isValidPhoneNumber(CharSequence target) {
        if (target == null || TextUtils.isEmpty(target)) {
            return false;
        } else {
            return android.util.Patterns.PHONE.matcher(target).matches();
        }
    }*/

    /**
     * For convert values from Dp to Px
     *
     * @param context
     * @param dp
     * @return
     */
    public static int dpToPx(final Context context, final int dp) {
        final DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();
        return Math.round(dp * (displayMetrics.xdpi / DisplayMetrics.DENSITY_DEFAULT));
    }

    public static boolean isMyServiceRunning(Class<?> serviceClass, Context ctx) {
        final ActivityManager manager = (ActivityManager) ctx.getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }


    public static String getCountryName(Context context, double latitude, double longitude) {
        Geocoder geocoder = new Geocoder(context, Locale.getDefault());
        List<Address> addresses = null;

        try {
            addresses = geocoder.getFromLocation(Double.valueOf(latitude), Double.valueOf(longitude), 1);
            Address result;

            if (addresses != null && !addresses.isEmpty()) {

                return addresses.get(0).getCountryCode();
            }
            return null;
        } catch (IOException ignored) {

            //do something

//            Toast.makeText(context, "Country code not found", Toast.LENGTH_SHORT).show();
        }
        return String.valueOf(addresses);

    }


    public static String getAddress(Context context, double latitude, double longitude) {


        StringBuilder result = new StringBuilder();
        try {
            Geocoder geocoder = new Geocoder(context, Locale.getDefault());
            List<Address> addresses = geocoder.getFromLocation(latitude, longitude, 1);
            if (addresses.size() > 0) {
                Address address = addresses.get(0);
                result.append(address.getAddressLine(0));
                result.append(address.getLocality());
                result.append(address.getPostalCode());
                result.append(address.getCountryName());
            }
        } catch (IOException e) {
            Log.e("tag", e.getMessage());
        }

        return result.toString();





//        Geocoder geocoder = new Geocoder(context, Locale.getDefault());
//        List<Address> addresses = null;
//
//        try {
//            addresses = geocoder.getFromLocation(Double.valueOf(latitude), Double.valueOf(longitude), 1);
//            Address result;
//            String str;
//
//            if (addresses != null && !addresses.isEmpty()) {
//
//                String address = addresses.get(0).getAddressLine(0); // If any additional address line present than only, check with max available address lines by getMaxAddressLineIndex()
//                String city = addresses.get(0).getLocality();
//                String state = addresses.get(0).getAdminArea();
//                String country = addresses.get(0).getCountryName();
//                String postalCode = addresses.get(0).getPostalCode();
////                String knownName = addresses.get(0).getFeatureName(); // Only if available else return NULL
//
//
//
//                return address;
//            }
//            return null;
//        } catch (IOException ignored) {
//
//            //do something
//
////            Toast.makeText(context, "Country code not found", Toast.LENGTH_SHORT).show();
//        }
//        return String.valueOf(addresses);

    }


    public static String getCountryName(Context context, String latitude, String longitude) {
        Geocoder geocoder = new Geocoder(context, Locale.getDefault());
        List<Address> addresses = null;

        try {
            addresses = geocoder.getFromLocation(Double.valueOf(latitude), Double.valueOf(longitude), 1);
            Address result;

            if (addresses != null && !addresses.isEmpty()) {

                return addresses.get(0).getCountryCode();


            }
            return null;
        } catch (IOException ignored) {

            //do something

//            Toast.makeText(context, "Country code not found", Toast.LENGTH_SHORT).show();
        }
        return String.valueOf(addresses);

    }

    public static void writeFile(String content, Context context) {
        Log.d("Time Change", " " + content.replace("\n", ""));


        content = "\n\n" + content;


        String filename = "DAD";
        String filecontent = content;
//        FileOperations fop = new FileOperations();
//        fop.write(filename, filecontent);


    }

    public static String fixEncoding(String latin1) {
        try {
            byte[] bytes = latin1.getBytes("ISO-8859-1");
            if (!validUTF8(bytes))
                return latin1;
            return new String(bytes, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            // Impossible, throw unchecked
            throw new IllegalStateException("No Latin1 or UTF-8: " + e.getMessage());
        }

    }

    public static boolean validUTF8(byte[] input) {
        int i = 0;
        // Check for BOM
        if (input.length >= 3 && (input[0] & 0xFF) == 0xEF
                && (input[1] & 0xFF) == 0xBB & (input[2] & 0xFF) == 0xBF) {
            i = 3;
        }

        int end;
        for (int j = input.length; i < j; ++i) {
            int octet = input[i];
            if ((octet & 0x80) == 0) {
                continue; // ASCII
            }

            // Check for UTF-8 leading byte
            if ((octet & 0xE0) == 0xC0) {
                end = i + 1;
            } else if ((octet & 0xF0) == 0xE0) {
                end = i + 2;
            } else if ((octet & 0xF8) == 0xF0) {
                end = i + 3;
            } else {
                // Java only supports BMP so 3 is max
                return false;
            }

            while (i < end) {
                i++;
                octet = input[i];
                if ((octet & 0xC0) != 0x80) {
                    // Not a valid trailing byte
                    return false;
                }
            }
        }
        return true;
    }


}
