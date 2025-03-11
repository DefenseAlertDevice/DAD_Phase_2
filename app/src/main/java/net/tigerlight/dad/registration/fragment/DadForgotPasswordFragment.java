package net.tigerlight.dad.registration.fragment;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.net.tigerlight.dad.R;
import net.tigerlight.dad.home.BaseFragment;
import net.tigerlight.dad.registration.util.DadUtils;
import net.tigerlight.dad.registration.webservices.CallForgotPassword;

public class DadForgotPasswordFragment extends BaseFragment {

    private static final String TAG = "LoginToYourAccountFragment";
    private static final String ARG_EMAIL_ID = "email_id";
    private View view;
    private EditText etEmailId;
    private Button tvSubmit;
    private TextView tvCancel;
    private AsyncTaskForgotPassword asyncTaskResetPassword;

    public static DadForgotPasswordFragment newInstance(String emailId) {
        DadForgotPasswordFragment fragment = new DadForgotPasswordFragment();
        Bundle args = new Bundle();
        args.putString(ARG_EMAIL_ID, emailId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_forgot_password, container, false);
        return view;
    }

    @Override
    public void initView(View view) {

        etEmailId = view.findViewById(R.id.fragment_forgot_password_et_email_id);
        tvSubmit = view.findViewById(R.id.fragment_forgot_password_tv_submit);
        tvCancel = view.findViewById(R.id.fragment_forgot_password_tv_cancel);
        tvSubmit.setOnClickListener(this);
        tvCancel.setOnClickListener(this);

        if (getArguments() != null) {
            String emailId = getArguments().getString(ARG_EMAIL_ID);
            if (emailId != null && !emailId.isEmpty()) {
                etEmailId.setText(emailId);
                if (DadUtils.isValidEmail(emailId.trim())) {
                    resetPassword();
                }
            }
        }
//       double lat= ((BaseActivity) getActivity()).getLatitude();
//       double lon=((BaseActivity) getActivity()).getLongitude();
//
//


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
        if (v.getId() == tvSubmit.getId()) {
            validateFields();

        } else if (v.getId() == tvCancel.getId() && getActivity() != null) {
            getActivity().onBackPressed();
        }


    }

    private void validateFields() {

        if (etEmailId.getText().toString().trim().equalsIgnoreCase("")) {
            DadUtils.displayDialog(getActivity(), getString(R.string.app_name), getString(R.string.TAG_EMAIL_ID), getString(R.string.ok), "", false, false);
            etEmailId.requestFocus();
        } else if (!DadUtils.isValidEmail(etEmailId.getText().toString().trim())) {
            DadUtils.displayDialog(getActivity(), getString(R.string.app_name), getString(R.string.TAG_ENTER_VALID_EMAIL), getString(R.string.ok), "", false, false);
            etEmailId.requestFocus();
        } else if (getActivity() != null) {
            if (DadUtils.isOnline(getActivity(), true)) {
                resetPassword();
                //  Utils.displayDialog(this, getString(R.string.app_name), "We've sent a password reset link to email address", getString(android.R.string.ok), "", false, true);
            } else {
                DadUtils.displayDialog(getActivity(), getString(R.string.app_name), getString(R.string.TAG_INTERNET_AVAILABILITY), getString(R.string.ok), "", false, false);
            }
        }
    }

    private void resetPassword() {
        if (getActivity() != null && DadUtils.isInternetConnected(getActivity())) {
            if (asyncTaskResetPassword != null && asyncTaskResetPassword.getStatus() == AsyncTask.Status.PENDING) {
                asyncTaskResetPassword.execute();
            } else if (asyncTaskResetPassword == null || asyncTaskResetPassword.getStatus() == AsyncTask.Status.FINISHED) {
                asyncTaskResetPassword = new AsyncTaskForgotPassword();
                asyncTaskResetPassword.execute();
            }
        } else {
            DadUtils.displayDialogNormalMessage(getString(R.string.app_name), getString(R.string.TAG_INTERNET_AVAILABILITY), getActivity());
        }

    }

    private class AsyncTaskForgotPassword extends AsyncTask<Void, Void, Void> {

        private CallForgotPassword wsForgetPassword;
        private ProgressDialog progressDialog;
        private String email = "";

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog = ProgressDialog.show(getActivity(), "", getString(R.string.TAG_Loading));
            progressDialog.setCancelable(false);
            email = etEmailId.getText().toString().trim();
            wsForgetPassword = new CallForgotPassword(getActivity());
        }

        @Override
        protected Void doInBackground(Void... voids) {
            wsForgetPassword.executeService(email);
            return null;
        }


        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);
            if (progressDialog != null && progressDialog.isShowing()) {
                progressDialog.dismiss();
            }
            if (!isCancelled()) {
                if (wsForgetPassword.isSuccess()) {
                    displayDialog(getActivity(), getString(R.string.app_name), getString(R.string.TAG_EMAIL_SENT), getString(R.string.TAG_OK));

                } else {
                    if (!wsForgetPassword.getMessage().trim().isEmpty()) {
                        DadUtils.displayDialog(getActivity(), getString(R.string.app_name), wsForgetPassword.getMessage(), getString(R.string.TAG_OK), "", false, false);
                    } else {
                        DadUtils.displayDialog(getActivity(), getString(R.string.app_name), getString(R.string.SOME_WENT_WRONG_MSG), getString(R.string.TAG_OK), "", false, false);

                    }
                }
            }
        }

        private void displayDialog(final Activity context, final String title, final String msg, final String strPositiveText) {
            final AlertDialog.Builder dialog = new AlertDialog.Builder(context);
            dialog.setTitle(title);
            dialog.setCancelable(false);
            dialog.setMessage(msg);
            dialog.setPositiveButton(strPositiveText, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    dialog.dismiss();
                    if (getActivity() != null) {
                        getActivity().onBackPressed();
                    }
                }
            });
            dialog.show();
        }


    }
}
