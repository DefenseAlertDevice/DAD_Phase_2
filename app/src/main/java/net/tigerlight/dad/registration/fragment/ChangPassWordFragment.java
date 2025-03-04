package net.tigerlight.dad.registration.fragment;

import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import net.tigerlight.dad.R;
import net.tigerlight.dad.home.BaseFragment;
import net.tigerlight.dad.registration.util.Utills;
import net.tigerlight.dad.webservices.WsCallChangePassword;

/**
 * Created by indianic on 21/10/16.
 */
public class ChangPassWordFragment extends BaseFragment {

    private View view;
    private EditText etOldPassword;
    private EditText etNewPassword;
    private EditText etConfirmPassword;
    private TextView tvCancel;
    private Button btnSave;
    private AsyncTaskChangePwd asyncTaskChangePwd;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_change_password, container, false);
        return view;
    }

    @Override
    public void initView(View view) {

        etOldPassword = (EditText) view.findViewById(R.id.fragment_change_password_et_old_password);
        etNewPassword = (EditText) view.findViewById(R.id.fragment_change_password_et_new_password);
        tvCancel = (TextView) view.findViewById(R.id.fragement_change_password_tv_cancel);
        etConfirmPassword = (EditText) view.findViewById(R.id.fragment_change_password_et_confirm_password);
        btnSave = (Button) view.findViewById(R.id.fragment_change_password_btn_save);
        btnSave.setOnClickListener(this);
        tvCancel.setOnClickListener(this);


    }

    @Override
    public void trackScreen() {

    }

    @Override
    public void initActionBar() {

    }

    @Override
    public void onClick(View v) {
        super.onClick(v);

        if (v.getId() == R.id.fragment_change_password_btn_save) {
            validateFragment();
        } else if (v.getId() == R.id.fragement_change_password_tv_cancel) {
            getActivity().onBackPressed();
        }

    }

    private void validateFragment() {

        if (etOldPassword.getText().toString().trim().equalsIgnoreCase("")) {
            Utills.displayDialog(getActivity(), getString(R.string.app_name), getString(R.string.old_pwd), getString(R.string.ok), "", false, false);
            etOldPassword.requestFocus();

        } else if (etNewPassword.getText().toString().trim().equalsIgnoreCase("")) {
            Utills.displayDialog(getActivity(), getString(R.string.app_name), getString(R.string.new_pwd), getString(R.string.ok), "", false, false);
            etNewPassword.requestFocus();
        } else if (etConfirmPassword.getText().toString().trim().equalsIgnoreCase("")) {
            Utills.displayDialog(getActivity(), getString(R.string.app_name), getString(R.string.confirm), getString(R.string.ok), "", false, false);
            etConfirmPassword.requestFocus();
        } /*else if (!Utills.validatePassword(etOldPassword.getText().toString())) {
            Utills.displayDialog(getActivity(), getString(R.string.app_name), getString(R.string.valid_msg), getString(android.R.string.ok), "", false, false);
            etOldPassword.requestFocus();
        } else if (!Utills.validatePassword(etNewPassword.getText().toString())) {
            Utills.displayDialog(getActivity(), getString(R.string.app_name), getString(R.string.valid_msg), getString(android.R.string.ok), "", false, false);
            etNewPassword.requestFocus();
        }*/ /*else if (!Utills.validatePassword(etConfirmPassword.getText().toString())) {
            Utills.displayDialog(getActivity(), getString(R.string.app_name), getString(R.string.valid_msg), getString(android.R.string.ok), "", false, false);
            etConfirmPassword.requestFocus();
        }*/ else if (!etNewPassword.getText().toString().trim().equalsIgnoreCase("") && !etConfirmPassword.getText().toString().trim().equalsIgnoreCase("")) {
            if (checkOldAndConfirmPassword(etNewPassword.getText().toString().trim(), etConfirmPassword.getText().toString().trim())) {
                Log.d("From here", "Call service");
                if (Utills.isOnline(getActivity(), true)) {
                    Toast.makeText(getActivity(), getString(R.string.TAG_READY_TO_GO), Toast.LENGTH_SHORT).show();
                    changePwd();
                    // Utils.displayDialog(this, getString(R.string.app_name), "Account has been created", getString(android.R.string.ok), "", false, true);
                } else {
                    Utills.displayDialog(getActivity(), getString(R.string.app_name), getString(R.string.TAG_INTERNET_AVAILABILITY), getString(R.string.ok), "", false, false);
                }

            } else {
                Utills.displayDialog(getActivity(), getString(R.string.app_name), getString(R.string.same_value), getString(R.string.ok), "", false, false);
                etNewPassword.requestFocus();
            }

        }
    }

    public boolean checkOldAndConfirmPassword(String password, String confirmPassword) {
        boolean pstatus = false;
        if (confirmPassword != null && password != null) {
            if (password.equals(confirmPassword)) {
                pstatus = true;
            }
        }
        return pstatus;
    }

    private void changePwd() {
        if (Utills.isInternetAvailable(getActivity())) {
            if (asyncTaskChangePwd != null && asyncTaskChangePwd.getStatus() == AsyncTask.Status.PENDING) {
                asyncTaskChangePwd.execute();
            } else if (asyncTaskChangePwd == null || asyncTaskChangePwd.getStatus() == AsyncTask.Status.FINISHED) {
                asyncTaskChangePwd = new AsyncTaskChangePwd();
                asyncTaskChangePwd.execute();
            }
        } else {
            Utills.displayDialogNormalMessage(getString(R.string.app_name), getString(R.string.TAG_INTERNET_AVAILABILITY), getActivity());
        }
    }

    private class AsyncTaskChangePwd extends AsyncTask<Void, Void, Void> {

        private WsCallChangePassword wsCallChangePassword;
        private ProgressDialog progressDialog;

        private String strOldPassword = etOldPassword.getText().toString().trim();
        private String strNewPassword = etNewPassword.getText().toString().trim();


        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog = ProgressDialog.show(getActivity(), "", getString(R.string.TAG_Loading));

            progressDialog.show();
//            progressDialog.setContentView(R.layout.progress_layout);
            progressDialog.setCancelable(false);
        }

        @Override
        protected Void doInBackground(Void... voids) {

            wsCallChangePassword = new WsCallChangePassword(getActivity());

            wsCallChangePassword.executeService(strOldPassword, strNewPassword);
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);

            if (progressDialog != null && progressDialog.isShowing()) {
                progressDialog.dismiss();
            }
            if (!isCancelled()) {
                if (wsCallChangePassword.isSuccess()) {


//                    final Constant mConstants = new Constant();
//                    final Preference preference = Preference.getInstance();
//                    preference.savePreferenceData(mConstants.USER_ID, wsCallChangePassword.getUserid());


//                    Preference.getInstance().savePreferenceData(PrefConstant.PREFERENCE_USER_NAME, userName);
//                        Preference.getInstance().savePreferenceData(PrefConstant.PREFERENCE_USER_NAME, userName);
//                        Preference.getInstance().savePreferenceData(PrefConstant.PREFERENCE_USER_NAME, password);
//                    creatopiaApp.getSharedPreferences().edit().putString(Const.PREFERENCE_USERNAME, userName).commit();
//                    creatopiaApp.getSharedPreferences().edit().putString(Const.PREFERENCE_USER_ID, wsLogin.getUserID()).commit();
//                    creatopiaApp.getSharedPreferences().edit().putString(Const.PREFERENCE_ACCESS_TOKEN, wsLogin.getAccessToken()).commit();
                    Utills.displayDialog(getActivity(), getString(R.string.app_name), getString(R.string.pwd_change_msg), getString(R.string.ok), "", false, false);
//                    openDashBoardFragment();


                } else {
                    Utills.displayDialog(getActivity(), getString(R.string.app_name), wsCallChangePassword.getMessage(), getString(R.string.ok), "", false, false);
                }
            }
        }


    }


    @Override
    public void onDestroy() {
        super.onDestroy();

        // cancel async task if any pending.
        if (asyncTaskChangePwd != null && asyncTaskChangePwd.getStatus() == AsyncTask.Status.RUNNING) {
            asyncTaskChangePwd.cancel(true);
        }
        if (asyncTaskChangePwd != null && asyncTaskChangePwd.getStatus() == AsyncTask.Status.RUNNING) {
            asyncTaskChangePwd.cancel(true);
        }

    }
}
