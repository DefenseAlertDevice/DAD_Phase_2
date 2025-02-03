package net.tigerlight.dad.util;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.BitmapImageViewTarget;
import net.tigerlight.dad.R;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.DownloadManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.location.LocationManager;
import android.net.Uri;
import android.provider.Settings;
import androidx.core.graphics.drawable.RoundedBitmapDrawable;
import androidx.core.graphics.drawable.RoundedBitmapDrawableFactory;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Created by M.T. on 7 Oct, 2016.
 * Contains misc day to day functions that android developer need frequently
 */
public class Util {
    private static Util ourInstance = new Util();

    private Util() {
    }

    public static Util getInstance() {
        return ourInstance;
    }

    /**
     * formats date and time in desired format.
     *
     * @param oldDate   {@link String} object that convert into new format
     * @param oldFormat {@link String} format from which oldDate will convert
     * @param newFormat {@link String} format in which oldDate will convert
     * @return {@link String} object of new formatted date.
     */
    public String formatDateTime(String oldDate, String oldFormat, String newFormat) {
        String newTime = "";

        try {
            SimpleDateFormat sdf = new SimpleDateFormat(oldFormat, Locale.US);
            Date newDate = sdf.parse(oldDate);
            sdf.applyPattern(newFormat);
            newTime = sdf.format(newDate);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        return newTime;
    }

    /**
     * checks the sd card available or not
     *
     * @return true if SD card is available otherwise false
     */
    public Boolean checkSDCardAvailability() {
        return android.os.Environment.getExternalStorageState().equals(android.os.Environment.MEDIA_MOUNTED);
    }

    /**
     * checks the device has camera or not
     *
     * @param mActivity object required for get package manager
     * @return true if camera is available otherwise false
     */
    public boolean isCamera(Activity mActivity) {
        if (mActivity != null && !mActivity.isFinishing()) {
            PackageManager packageManager = mActivity.getPackageManager();

            return packageManager.hasSystemFeature(PackageManager.FEATURE_CAMERA);
        } else {
            return false;
        }
    }

    public void getCircleImageView(final Context context, String imgUrl, final ImageView imageView) {
        Glide.with(context).load(imgUrl).centerCrop().into(new BitmapImageViewTarget(imageView) {
            @Override
            protected void setResource(Bitmap resource) {
                RoundedBitmapDrawable circularBitmapDrawable =
                        RoundedBitmapDrawableFactory.create((context).getResources(), resource);
                circularBitmapDrawable.setCircular(true);
                imageView.setImageDrawable(circularBitmapDrawable);
            }
        }.getView());
    }

    /**
     * Hides keyboard from screen if it is showing
     *
     * @param mActivity requires for checking keyboard is open or not
     */
    public void hideSoftKeyboard(Activity mActivity) {
        if (mActivity != null && !mActivity.isFinishing()) {
            final InputMethodManager inputMethodManager = (InputMethodManager) mActivity.getSystemService(Activity.INPUT_METHOD_SERVICE);
            if (inputMethodManager.isActive()) {
                if (mActivity.getCurrentFocus() != null) {
                    inputMethodManager.hideSoftInputFromWindow(mActivity.getCurrentFocus().getWindowToken(), 0);
                }
            }
        }
    }

    /**
     * Gets the Logging Object to print log in Android monitor
     *
     * @return {@link DisplayDialog} Object
     */
    public DisplayDialog getDisplayDialog() {
        return DisplayDialog.getInstance();
    }

    /**
     * checks the GPS is enable or not
     *
     * @param mActivity   object required for get SystemService
     * @param showMessage if true will show enable GPS alert with got to settings option otherwise check silently
     * @return true if location enabled otherwise false
     */
    public boolean checkLocationAccess(final Activity mActivity, boolean showMessage) {
        if (mActivity != null && !mActivity.isFinishing()) {
            final LocationManager locationManager = (LocationManager) mActivity.getSystemService(Context.LOCATION_SERVICE);
            boolean isNetworkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
            boolean isGpsEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);

            if (isNetworkEnabled) {
                return true;
            } else if (isGpsEnabled) {
                return true;
            }

            if (showMessage) {
                final AlertDialog.Builder mDialog = new AlertDialog.Builder(mActivity);
                mDialog.setTitle(mActivity.getString(R.string.app_name));
                mDialog.setCancelable(false);
                mDialog.setMessage(mActivity.getString(R.string.alert_check_gps));

                mDialog.setPositiveButton(mActivity.getString(R.string.settings), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface mDialog, int id) {
                        mDialog.dismiss();
                        mActivity.startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS));
                    }
                });

                mDialog.setNegativeButton(mActivity.getString(android.R.string.cancel), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface mDialog, int id) {
                        mDialog.dismiss();
                    }
                });
                mDialog.show();

                return false;
            }
        }
        return false;
    }

    /**
     * Hides keyboard from screen if it is showing
     *
     * @param mActivity requires for checking keyboard is open or not
     * @param view      view currently in focus
     */
    public void hideSoftKeyboard(Activity mActivity, View view) {
        if (mActivity != null && !mActivity.isFinishing()) {
            final InputMethodManager imm = (InputMethodManager) mActivity.getSystemService(Activity.INPUT_METHOD_SERVICE);
            if (imm != null) {
                imm.hideSoftInputFromWindow(view.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
            }
        }
    }

    /**
     * Opens keyboard on screen
     *
     * @param mActivity activity context
     * @param editText  object currently in focus
     */
    public void openSoftKeyboard(final Activity mActivity, EditText editText) {
        if (mActivity != null && !mActivity.isFinishing()) {
            InputMethodManager inputMethodManager = (InputMethodManager) mActivity.getSystemService(Activity.INPUT_METHOD_SERVICE);
            inputMethodManager.showSoftInput(editText, 0);
        }
    }

    /**
     * For get application current language
     *
     * @return Returns the language code for this Locale or the empty string if no language was set.
     */
    public String getCurrentLanguage() {
        return Locale.getDefault().getLanguage();
    }

    /**
     * This function is used for validate email address
     *
     * @return true if email is valid otherwise false
     */
    public boolean isEmailValid(String email) {
        return !TextUtils.isEmpty(email) && android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }

    /**
     * Gets the device id of the phone
     *
     * @param mActivity object required for get Content resolver
     * @return device unique id if activity is not closed other wise return blank
     */
    public String getDeviceID(Activity mActivity) {
        if (mActivity != null && !mActivity.isFinishing()) {
            return Settings.Secure.getString(mActivity.getContentResolver(), Settings.Secure.ANDROID_ID);
        } else {
            return "";
        }
    }

    public void downloadFile(android.app.Fragment fragment, String url) {
        if (!TextUtils.isEmpty(url)) {
            Util.getInstance().hideSoftKeyboard(fragment.getActivity());
            if (NetworkAvailability.isOnline(fragment.getActivity(), true, false, true)) {
                Uri uri = Uri.parse(url);
                DownloadManager downloadManager = (DownloadManager) fragment.getActivity().getSystemService(Context.DOWNLOAD_SERVICE);
                DownloadManager.Request request = new DownloadManager.Request(uri);
                request.setVisibleInDownloadsUi(true);
                request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
                request.setMimeType("application/" + uri.getLastPathSegment().substring(uri.getLastPathSegment().indexOf(".") + 1));
                //                request.setDescription(fragment.getActivity().getString(R.string.contract_download));
                request.setDescription(uri.getLastPathSegment());
                request.setTitle(uri.getLastPathSegment().replaceAll("-", "_"));
                downloadManager.enqueue(request);
            }
        } else {
//            Util.getInstance().displayDialog(fragment.getActivity(), fragment.getActivity().getString(R.string.app_name), fragment.getActivity().getString(R.string.alert_file_required), fragment.getActivity().getString(R.string.ok), fragment.getActivity().getString(R.string.cancel), true, false, false, false);
        }
    }

    /**
     * @param mContext
     * @return Device current language
     */
    public String deviceCurrentLanguageCode(Context mContext) {
        return mContext.getResources().getConfiguration().locale.getLanguage();
    }

    /**
     * Save Language specific to application using ConfigLocale
     *
     * @param languageCode
     */
    public void saveLanguageSetting(Context mContext, String languageCode) {
        if (!languageCode.equalsIgnoreCase("en") && !languageCode.equalsIgnoreCase("it")) {
            languageCode = "en";
        }

        Locale locale = new Locale(languageCode);
        Locale.setDefault(locale);
        final Configuration config = new Configuration();
        config.locale = locale;
        mContext.getResources().updateConfiguration(config, mContext.getResources().getDisplayMetrics());
        Preference.getInstance().savePreferenceData(Preference.getInstance().KEY_LANG_ID, languageCode.toUpperCase());
    }

    public int dpToPx(Context mContext, int dp) {
        DisplayMetrics displayMetrics = mContext.getResources().getDisplayMetrics();
        int px = Math.round(dp * (displayMetrics.xdpi / DisplayMetrics.DENSITY_DEFAULT));
        return px;
    }

    public static int getResourceId(Context context, String resourceName)
    {
        if (resourceName.contains(".")) {
            resourceName = resourceName.substring(0, resourceName.indexOf('.'));
        }

        return context.getResources().getIdentifier(resourceName, "raw", context.getPackageName());
    }
}
