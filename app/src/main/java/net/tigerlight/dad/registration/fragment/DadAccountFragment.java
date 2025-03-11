package net.tigerlight.dad.registration.fragment;

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

import net.tigerlight.dad.LocationUpdateService;
import com.net.tigerlight.dad.R;
import net.tigerlight.dad.home.BaseFragment;
import net.tigerlight.dad.registration.activity.DadMainActivity;
import net.tigerlight.dad.registration.util.DadConstant;
import net.tigerlight.dad.registration.util.DadUtils;
import net.tigerlight.dad.util.ServiceAlarmUtils;
import net.tigerlight.dad.util.Constants;
import net.tigerlight.dad.util.Preference;
import net.tigerlight.dad.webservices.Logout;
import net.tigerlight.dad.webservices.ResetCount;

import java.util.Locale;

public class DadAccountFragment extends BaseFragment {

    private static final String TAG = DadAccountFragment.class.getSimpleName();

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
        currentUserName = Preference.getInstance().mSharedPreferences.getString(DadConstant.USER_NAME, "");
        tvWelcome.setText(getString(R.string.TAG_WELCOME) + " " + currentUserName);

//       tvWelcome.setText(String.format("Welcome ", currentUserName));
        tvEditAccount.setOnClickListener(this);
        tvShowEula.setOnClickListener(this);
        tvLogOut.setOnClickListener(this);

        TextView tvBuildVersion = view.findViewById(R.id.tvBuild);
        try {
            final Context context = getContext();
            final Activity activity = getActivity();
            if (activity != null && context != null) {
                PackageInfo packageInfo = activity.getPackageManager().getPackageInfo(activity.getPackageName(), 0);

                tvBuildVersion.setText(String.format(Locale.US, getString(R.string.build_no), packageInfo.versionCode, packageInfo.versionName));
                activity.getWindow().setStatusBarColor(ContextCompat.getColor(context, R.color.colorBlack));
            }
        }
        catch (PackageManager.NameNotFoundException e) {
            Log.e(TAG, "Package Not found." + e.getMessage());
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
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

    private void openEditProfileFragment(final DadEditProfileFragment editProfileFragment) {
        editProfileFragment.setOnEditProfileListener(() -> {
            currentUserName = Preference.getInstance().mSharedPreferences.getString(DadConstant.USER_NAME, "");
            tvWelcome.setText(getString(R.string.TAG_WELCOME) + " " + currentUserName);
        });

        editProfileFragment.show(getParentFragmentManager(), DadEditProfileFragment.class.getSimpleName());
    }

    @Override
    public void onClick(View v) {
        super.onClick(v);
        final int fragmentId = v.getId();
        if (getActivity() != null && fragmentId == R.id.fragment_settings_tvEditAccount) {
            openEditProfileFragment(new DadEditProfileFragment());
        } else if (fragmentId == R.id.fragment_settings_tvLogOut) {
            displayMyDialog(getActivity(), getString(R.string.TAG_LOGOUT_CONFIRMATION), getString(R.string.TAG_LOGOUT_CONFIRMATION_DES), getString(R.string.TAG_OK), getString(R.string.fragment_create_account_tv_cancel));
        } else if (fragmentId == R.id.fragment_settings_tvShowEula) {
            if (getActivity() instanceof DadMainActivity) {
                ((DadMainActivity) getActivity()).replaceFragment(new DADLicenseFragment());
            }
        }
    }

    private void logOut() {
        if (getActivity() != null && DadUtils.isInternetAvailable(getActivity())) {
            if (asyncTaskLogOut != null && asyncTaskLogOut.getStatus() == AsyncTask.Status.PENDING) {
                asyncTaskLogOut.execute();
            } else if (asyncTaskLogOut == null || asyncTaskLogOut.getStatus() == AsyncTask.Status.FINISHED) {
                asyncTaskLogOut = new AsyncTaskLogOut();
                asyncTaskLogOut.execute();
            }
        } else {
            DadUtils.displayDialogNormalMessage(getString(R.string.app_name), getString(R.string.TAG_INTERNET_AVAILABILITY), getActivity());
        }
    }

    private void callResetCount() {
        if (getActivity() != null && DadUtils.isInternetConnected(getActivity())) {
            if (asyncTaskResetCount != null && asyncTaskResetCount.getStatus() == AsyncTask.Status.PENDING) {
                asyncTaskResetCount.execute();
            } else if (asyncTaskResetCount == null || asyncTaskResetCount.getStatus() == AsyncTask.Status.FINISHED) {
                asyncTaskResetCount = new AsyncTaskResetCount();
                asyncTaskResetCount.execute();
            }
        } else {
            DadUtils.displayDialogNormalMessage(getString(R.string.app_name), getString(R.string.TAG_INTERNET_AVAILABILITY), getActivity());
        }

    }

    private class AsyncTaskResetCount extends AsyncTask<Void, Void, Void> {
        private ResetCount resetCount;


        @Override
        protected void onPreExecute() {
            super.onPreExecute();
//

        }

        @Override
        protected Void doInBackground(Void... params) {
            resetCount = new ResetCount(getActivity());
            resetCount.executeService();
            return null;
        }


        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            if (!isCancelled()) {
                if (resetCount.isSuccess()) {
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

        private Logout logout;
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
            logout = new Logout(getActivity());
            logout.executeService();
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            if (progressDialog != null && progressDialog.isShowing()) {
                progressDialog.dismiss();
            }
            if (!isCancelled()) {
                if (logout.isSuccess()) {
                    ServiceAlarmUtils.cancelPeriodicService(requireContext());
                    final var locationServiceIntent= new Intent(requireContext(), LocationUpdateService.class);
                    requireContext().stopService(locationServiceIntent);

                    Preference.getInstance().savePreferenceData(DadConstant.IS_PIN_CREATED, true);
                    final Preference preference = Preference.getInstance();
                    boolean isRemember = preference.mSharedPreferences.getBoolean(DadConstant.IS_REMEMBER, false);
                    String email = "";
                    String pwd = "";
                    if (isRemember) {
                        email = preference.mSharedPreferences.getString(DadConstant.KEY_EMAIL, "");
                        pwd = preference.mSharedPreferences.getString(DadConstant.KEY_PASSWORD, "");
                    }
                    //preference.clearPreferenceData();
                    preference.savePreferenceData(DadConstant.IS_REMEMBER, isRemember);
                    preference.savePreferenceData(DadConstant.USER_NAME, currentUserName);
                    preference.savePreferenceData(DadConstant.KEY_EMAIL, email);
                    preference.savePreferenceData(DadConstant.KEY_PASSWORD, pwd);
                    preference.savePreferenceData(DadConstant.IS_LOGIN, false);
                    preference.savePreferenceData(DadConstant.IS_PIN_CREATED, false);

                    ServiceAlarmUtils.cancelPeriodicService(requireContext());
                    requireContext().stopService(locationServiceIntent);

                    ((DadMainActivity) getActivity()).replaceFragment(new DadRegistrationFragment());

                } else {
//                    Utills.displayDialog(getActivity(), getString(R.string.app_name), wsLogout.getMessage(), getString(R.string.ok), "", false, false);
                    DadUtils.displayDialog(getActivity(), getString(R.string.app_name), getString(R.string.TAG_COULD_LOGOUT), getString(R.string.ok), "", false, false);
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
            currentUserName = Preference.getInstance().mSharedPreferences.getString(DadConstant.USER_NAME, "");
            tvWelcome.setText(String.format(getString(R.string.TAG_WELCOME) + " %s", currentUserName));
        }
    }
}
