package net.tigerlight.dad.registration.fragment;

import net.tigerlight.dad.R;
import net.tigerlight.dad.home.BaseFragment;
import net.tigerlight.dad.registration.activity.MainActivity;
import net.tigerlight.dad.registration.util.Constant;
import net.tigerlight.dad.registration.util.Utills;
import net.tigerlight.dad.webservices.WsLogout;
import net.tigerlight.dad.webservices.WsResetCount;
import net.tigerlight.dad.util.Constants;
import net.tigerlight.dad.util.Preference;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentManager;

import java.util.Locale;

public class AccountFragment extends BaseFragment {

    private static final String TAG = AccountFragment.class.getSimpleName();

    private TextView tvWelcome;
    private TextView tvShowEula;
    private Button tvEditAccount;
    private Button tvLogOut;
    private AsyncTaskLogOut asyncTaskLogOut;
    private AsyncTaskResetCount asyncTaskResetCount;

    private String currentUserName;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_settings, container, false);
    }

    @Override
    public void initView(View view) {

        callResetCount();
        tvWelcome = view.findViewById(R.id.fragment_settings_tvWelcome);
        tvEditAccount = view.findViewById(R.id.fragment_settings_tvEditAccount);
        tvShowEula = view.findViewById(R.id.fragment_settings_tvShowEula);
        tvLogOut = view.findViewById(R.id.fragment_settings_tvLogOut);
        currentUserName = Preference.getInstance().mSharedPreferences.getString(Constant.USER_NAME, "");
        tvWelcome.setText(getString(R.string.TAG_WELCOME) + " " + currentUserName);

//       tvWelcome.setText(String.format("Welcome ", currentUserName));
        tvEditAccount.setOnClickListener(this);
        tvShowEula.setOnClickListener(this);
        tvLogOut.setOnClickListener(this);

        TextView tvBuildVersion = view.findViewById(R.id.tvBuild);
        try
        {
            Context context = getContext();
            Activity activity = getActivity();
            if (activity != null && context != null) {
                PackageInfo packageInfo = activity.getPackageManager().getPackageInfo(activity.getPackageName(), 0);

                tvBuildVersion.setText(String.format(Locale.US, getString(R.string.build_no), packageInfo.versionCode, packageInfo.versionName));
                activity.getWindow().setStatusBarColor(ContextCompat.getColor(context, R.color.colorBlack));
            }
        }
        catch (PackageManager.NameNotFoundException e)
        {
            Log.e(TAG, "Package Not found." + e.getMessage());
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        switch (requestCode) {
            case Constants.REQUEST_CODES.FORCE_LOGOUT: {
                if (resultCode == Activity.RESULT_OK) {
                    if (data.hasExtra(Constants.Extras.FORCE_LOGOUT) && data.getBooleanExtra(Constants.Extras.FORCE_LOGOUT, false)) {
                        logOut();
                    }
                }
            }

        }
    }

    private void openEditProfileFragment(EditProfileFragment editProfileFragment) {
        editProfileFragment.show(getParentFragmentManager(), EditProfileFragment.class.getSimpleName());
    }

    @Override
    public void onClick(View v) {
        super.onClick(v);
        final int fragmentId = v.getId();
        if (getActivity() != null && fragmentId == R.id.fragment_settings_tvEditAccount) {
            openEditProfileFragment(new EditProfileFragment());
        } else if (fragmentId == R.id.fragment_settings_tvLogOut) {
            displayMyDialog(getActivity(), getString(R.string.TAG_LOGOUT_CONFIRMATION), getString(R.string.TAG_LOGOUT_CONFIRMATION_DES), getString(R.string.TAG_OK), getString(R.string.fragment_create_account_tv_cancel));
        } else if (fragmentId == R.id.fragment_settings_tvShowEula) {
            if (getActivity() instanceof MainActivity) {
                ((MainActivity) getActivity()).replaceFragment(new DADLicenseFragment());
            }
        }
    }

    private void logOut() {
        if (getActivity() != null && Utills.isInternetAvailable(getActivity())) {
            if (asyncTaskLogOut != null && asyncTaskLogOut.getStatus() == AsyncTask.Status.PENDING) {
                asyncTaskLogOut.execute();
            } else if (asyncTaskLogOut == null || asyncTaskLogOut.getStatus() == AsyncTask.Status.FINISHED) {
                asyncTaskLogOut = new AsyncTaskLogOut();
                asyncTaskLogOut.execute();
            }
        } else {
            Utills.displayDialogNormalMessage(getString(R.string.app_name), getString(R.string.TAG_INTERNET_AVAILABILITY), getActivity());
        }
    }

    private void callResetCount() {
        if (getActivity() != null && Utills.isInternetConnected(getActivity())) {
            if (asyncTaskResetCount != null && asyncTaskResetCount.getStatus() == AsyncTask.Status.PENDING) {
                asyncTaskResetCount.execute();
            } else if (asyncTaskResetCount == null || asyncTaskResetCount.getStatus() == AsyncTask.Status.FINISHED) {
                asyncTaskResetCount = new AsyncTaskResetCount();
                asyncTaskResetCount.execute();
            }
        } else {
            Utills.displayDialogNormalMessage(getString(R.string.app_name), getString(R.string.TAG_INTERNET_AVAILABILITY), getActivity());
        }

    }

    private class AsyncTaskResetCount extends AsyncTask<Void, Void, Void> {
        private WsResetCount wsResetCount;


        @Override
        protected void onPreExecute() {
            super.onPreExecute();
//

        }

        @Override
        protected Void doInBackground(Void... params) {
            wsResetCount = new WsResetCount(getActivity());
            wsResetCount.executeService();
            return null;
        }


        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            if (!isCancelled()) {
                if (wsResetCount.isSuccess()) {
                    Log.d("Count", "Updated");

                } else {
                    try //TODO:  Band-aid (per Rod) for unknown NPE
                    {
                        Toast.makeText(getActivity(), getString(R.string.TAG_SOME_WENT_WRONG_MSG), Toast.LENGTH_SHORT).show();
                    }
                    catch (Exception ex)
                    {
                        Log.e(TAG, ex.getMessage());
                    }

                }
            }
        }


    }

    private class AsyncTaskLogOut extends AsyncTask<Void, Void, Void> {

        private WsLogout wsLogout;
        private ProgressDialog progressDialog;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog = ProgressDialog.show(getActivity(), "", getString(R.string.TAG_Loading));
            progressDialog.show();
            progressDialog.setCancelable(false);

        }

        @Override
        protected Void doInBackground(Void... voids) {
            wsLogout = new WsLogout(getActivity());
            wsLogout.executeService();
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            if (progressDialog != null && progressDialog.isShowing()) {
                progressDialog.dismiss();
            }
            if (!isCancelled()) {
                if (wsLogout.isSuccess()) {
                    Preference.getInstance().savePreferenceData(Constant.IS_PIN_CREATED, true);
                    final Preference preference = Preference.getInstance();
                    boolean isRemember = preference.mSharedPreferences.getBoolean(Constant.IS_REMEMBER, false);
                    String email = "";
                    String pwd = "";
                    if (isRemember) {
                        email = preference.mSharedPreferences.getString(Constant.KEY_EMAIL, "");
                        pwd = preference.mSharedPreferences.getString(Constant.KEY_PASSWORD, "");
                    }
                    //preference.clearPreferenceData();
                    preference.savePreferenceData(Constant.IS_REMEMBER, isRemember);
                    preference.savePreferenceData(Constant.USER_NAME, currentUserName);
                    preference.savePreferenceData(Constant.KEY_EMAIL, email);
                    preference.savePreferenceData(Constant.KEY_PASSWORD, pwd);
                    preference.savePreferenceData(Constant.IS_LOGIN, false);
                    preference.savePreferenceData(Constant.IS_PIN_CREATED, false);
                    ((MainActivity) getActivity()).replaceFragment(new RegistartionFragment());

                } else {
//                    Utills.displayDialog(getActivity(), getString(R.string.app_name), wsLogout.getMessage(), getString(R.string.ok), "", false, false);
                    Utills.displayDialog(getActivity(), getString(R.string.app_name), getString(R.string.TAG_COULD_LOGOUT), getString(R.string.ok), "", false, false);
                }
            }
        }
    }

    private void displayMyDialog(final Activity context, final String title, final String msg, final String strPositiveText, final String strNegativeText) {
        final AlertDialog.Builder dialog = new AlertDialog.Builder(context);
        dialog.setTitle(title);
        dialog.setCancelable(false);
        dialog.setMessage(msg);

        dialog.setPositiveButton(strPositiveText, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                logOut();
            }
        });

        dialog.setNegativeButton(strNegativeText, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                dialog.dismiss();
            }
        });
        dialog.show();
    }

    @Override
    public void trackScreen() {
    }

    @Override
    public void initActionBar() {
    }


    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        if (!hidden) {
            currentUserName = Preference.getInstance().mSharedPreferences.getString(Constant.USER_NAME, "");
            tvWelcome.setText(String.format(getString(R.string.TAG_WELCOME) + " %s", currentUserName));
        }
    }
}
