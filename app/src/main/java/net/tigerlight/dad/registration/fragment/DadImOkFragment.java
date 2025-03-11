package net.tigerlight.dad.registration.fragment;

import android.app.Activity;
import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewFlipper;

import com.net.tigerlight.dad.R;
import net.tigerlight.dad.home.BaseFragment;
import net.tigerlight.dad.registration.util.DadUtils;
import net.tigerlight.dad.webservices.CreatePin;
import net.tigerlight.dad.webservices.HasPin;

import java.lang.ref.WeakReference;


public class DadImOkFragment extends BaseFragment {

    private View view;
    private ViewFlipper viewFlipper;

    //Here id first view

    private EditText etPin;
    private TextView tvSendImOkMessage;

    //Here Second view
    private EditText etNewPin;
    private EditText etConfirmPin;
    private TextView tvResetPin;
    private TextView tvForgotPin;
    private TextView tvValidatePin;
    private TextView tvSavePin;
    private AsyncTaskCreatePinn asyncTaskCreatePinn;
    private AsyncTaskForgotPin asyncTaskForgotPin;
    private AsyncTaskHasPin asyncTaskHasPin;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_i_am_ok_vf, container, false);
    }


    @Override
    public void initView(View view) {
        viewFlipper = view.findViewById(R.id.viewFlipper);
        //first view binding
        etPin = view.findViewById(R.id.fragment_i_m_ok_send_pin_et_pinn);
        etNewPin = view.findViewById(R.id.fragment_i_m_ok_requiew_pin_et_new_pin);
        etConfirmPin = view.findViewById(R.id.fragment_i_m_ok_requiew_pin_et_confirm_pin);
        tvSendImOkMessage = view.findViewById(R.id.fragment_i_m_ok_send_pin_tv_sendd);
        tvResetPin = view.findViewById(R.id.fragment_i_m_ok_requiew_pin_tv_reset_pin);
        tvForgotPin = view.findViewById(R.id.fragment_i_m_ok_requiew_pin_tv_forgot_pin);
        tvValidatePin = view.findViewById(R.id.fragment_i_m_ok_requiew_pin_tv_validate_pin);
        tvSavePin = view.findViewById(R.id.fragment_i_m_ok_requiew_pin_tv_save_pin);
        tvSendImOkMessage.setOnClickListener(this);
        tvResetPin.setOnClickListener(this);
        tvForgotPin.setOnClickListener(this);
        tvValidatePin.setOnClickListener(this);
        tvSavePin.setOnClickListener(this);
        callHasPinService();
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

        final int fragmentId = v.getId();

        if (fragmentId == R.id.fragment_i_m_ok_send_pin_tv_sendd) {
            Toast.makeText(getActivity(), getString(R.string.TAG_SEND), Toast.LENGTH_SHORT).show();
        } else if (fragmentId == R.id.fragment_i_m_ok_requiew_pin_tv_reset_pin) {
            Toast.makeText(getActivity(), getString(R.string.TAG_RESET), Toast.LENGTH_SHORT).show();
        } else if (fragmentId == R.id.fragment_i_m_ok_requiew_pin_tv_forgot_pin) {
            callForgotPinService();
        } else if (fragmentId == R.id.fragment_i_m_ok_requiew_pin_tv_validate_pin) {
            Toast.makeText(getActivity(), getString(R.string.TAG_VALIDATE), Toast.LENGTH_SHORT).show();
        } else if (fragmentId == R.id.fragment_i_m_ok_requiew_pin_tv_save_pin) {
            ValidateNewAndConfirmField();
        }
    }

    private void ValidateNewAndConfirmField() {
        if (etNewPin.getText().toString().trim().equalsIgnoreCase("")) {
            DadUtils.displayDialog(getActivity(), getString(R.string.app_name), getString(R.string.TAG_ENTER_NEW_PIN), getString(R.string.ok), "", false, false);
            etNewPin.requestFocus();
        } else if (etConfirmPin.getText().toString().trim().equalsIgnoreCase("")) {
            DadUtils.displayDialog(getActivity(), getString(R.string.app_name), getString(R.string.TAG_CONFIRM_PINF), getString(R.string.ok), "", false, false);
            etConfirmPin.requestFocus();
        } else if (etNewPin.getText().toString().length() < 4) {
            DadUtils.displayDialog(getActivity(), getString(R.string.app_name), getString(R.string.TAG_VALIDATE), getString(R.string.ok), "", false, false);
            etNewPin.requestFocus();
        } else if (etConfirmPin.getText().toString().length() < 4) {
            DadUtils.displayDialog(getActivity(), getString(R.string.app_name), getString(R.string.TAG_MIN_FOUR), getString(R.string.ok), "", false, false);
            etConfirmPin.requestFocus();
        } else if (etNewPin.getText().toString().length() > 4) {
            DadUtils.displayDialog(getActivity(), getString(R.string.app_name), getString(R.string.TAG_PIN_NOT_FOUR), getString(R.string.ok), "", false, false);
            etNewPin.requestFocus();
        } else if (etConfirmPin.getText().toString().length() > 4) {
            DadUtils.displayDialog(getActivity(), getString(R.string.app_name), getString(R.string.TAG_PIN_NOT_FOUR), getString(R.string.ok), "", false, false);
            etConfirmPin.requestFocus();
        } else if (!etNewPin.getText().toString().trim().equalsIgnoreCase("") && !etConfirmPin.getText().toString().trim().equalsIgnoreCase("")) {
            if (checkPassWordAndConfirmPassword(etNewPin.getText().toString().trim(), etConfirmPin.getText().toString().trim())) {
                Log.d("From here", "Call service");
                if (DadUtils.isOnline(getActivity(), true)) {
                    createPin();
                    // Utils.displayDialog(this, getString(R.string.app_name), "Account has been created", getString(android.R.string.ok), "", false, true);
                } else {
                    DadUtils.displayDialog(getActivity(), getString(R.string.app_name), getString(R.string.TAG_INTERNET_AVAILABILITY), getString(R.string.ok), "", false, false);
                }
            } else {
                DadUtils.displayDialog(getActivity(), getString(R.string.app_name), getString(R.string.TAG_PWD_RE_PWD_EMPTYMSG), getString(R.string.ok), "", false, false);
                etNewPin.requestFocus();
            }
        }
    }

    public boolean checkPassWordAndConfirmPassword(String password, String confirmPassword) {
        boolean pstatus = false;
        if (confirmPassword != null && password != null) {
            if (password.equals(confirmPassword)) {
                pstatus = true;
            }
        }
        return pstatus;
    }

    private void callForgotPinService() {

        if (DadUtils.isInternetAvailable(requireActivity())) {

            Log.d("START", "internet available");

            if (asyncTaskCreatePinn != null && asyncTaskCreatePinn.getStatus() == AsyncTask.Status.PENDING) {

                asyncTaskCreatePinn.execute();
            } else if (asyncTaskCreatePinn == null || asyncTaskCreatePinn.getStatus() == AsyncTask.Status.FINISHED) {

                asyncTaskCreatePinn = new AsyncTaskCreatePinn(etConfirmPin.getText().toString());
                asyncTaskCreatePinn.execute();
            }
        } else {

            DadUtils.displayDialogNormalMessage(getString(R.string.app_name), getString(R.string.TAG_INTERNET_AVAILABILITY), getActivity());
        }
    }

    private void callHasPinService() {
        if (getActivity() == null) {
            return;
        }
        if (DadUtils.isInternetAvailable(getActivity())) {
            if (asyncTaskHasPin != null && asyncTaskHasPin.getStatus() == AsyncTask.Status.PENDING) {
                asyncTaskHasPin.execute();
            } else if (asyncTaskHasPin == null || asyncTaskHasPin.getStatus() == AsyncTask.Status.FINISHED) {
                asyncTaskHasPin = new AsyncTaskHasPin();
                asyncTaskHasPin.execute();
            }
        } else {

            DadUtils.displayDialogNormalMessage(getString(R.string.app_name), getString(R.string.TAG_INTERNET_AVAILABILITY), getActivity());
        }
    }


    private void createPin() {
        if (DadUtils.isInternetAvailable(getActivity())) {

            Log.d("START", "internet availavble");

            if (asyncTaskCreatePinn != null && asyncTaskCreatePinn.getStatus() == AsyncTask.Status.PENDING) {

                asyncTaskCreatePinn.execute();
            } else if (asyncTaskCreatePinn == null || asyncTaskCreatePinn.getStatus() == AsyncTask.Status.FINISHED) {

                asyncTaskCreatePinn = new AsyncTaskCreatePinn(etConfirmPin.getText().toString());
                asyncTaskCreatePinn.execute();
            }
        } else {

            DadUtils.displayDialogNormalMessage(getString(R.string.app_name), getString(R.string.TAG_INTERNET_AVAILABILITY), getActivity());
        }
    }

    private void setupInitialView(Boolean hasPin) {
        if (hasPin) {
            viewFlipper.setDisplayedChild(0);
            viewFlipper.setDisplayedChild(viewFlipper.indexOfChild(view.findViewById(R.id.first)));
        } else {
            viewFlipper.setDisplayedChild(1);
            viewFlipper.setDisplayedChild(viewFlipper.indexOfChild(view.findViewById(R.id.second)));
        }
    }

    private class AsyncTaskHasPin extends AsyncTask<String, Void, String> {
        private HasPin hasPin;
        private ProgressDialog progressDialog;

        private final WeakReference<Activity> activityRef = new WeakReference<>(getActivity());

        public AsyncTaskHasPin() {
            hasPin = new HasPin(activityRef.get());
        }


        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog = ProgressDialog.show(activityRef.get(), "", getString(R.string.TAG_Loading));


            progressDialog.show();
//            progressDialog.setContentView(R.layout.progress_layout);
            progressDialog.setCancelable(false);
        }


        @Override
        protected String doInBackground(String... strings) {
            hasPin.executeService();
            return null;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            if (progressDialog != null && progressDialog.isShowing()) {
                progressDialog.dismiss();
            }

            if (!isCancelled()) {
                setupInitialView(hasPin.isSuccess());
            }
        }
    }

    private class AsyncTaskCreatePinn extends AsyncTask<String, Void, String> {
        private CreatePin createPin;
        private String pin;
        private ProgressDialog progressDialog;

        private final WeakReference<Activity> activityRef = new WeakReference<>(getActivity());

        public AsyncTaskCreatePinn(String pin) {
            this.pin = pin;
            createPin = new CreatePin(activityRef.get());
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog = ProgressDialog.show(activityRef.get(), "", getString(R.string.TAG_Loading));


            progressDialog.show();
//            progressDialog.setContentView(R.layout.progress_layout);
            progressDialog.setCancelable(false);
        }

        @Override
        protected String doInBackground(String... strings) {

            createPin.executeService(pin);
            return null;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            if (progressDialog != null && progressDialog.isShowing()) {
                progressDialog.dismiss();
            }

            if (!isCancelled()) {
                if (createPin.isSuccess()) {

                    DadUtils.displayDialog(activityRef.get(), getString(R.string.app_name), getString(R.string.TAG_NEW_PIN_CREATED_MSG), getString(R.string.ok), "", false, false);
                    viewFlipper.setDisplayedChild(viewFlipper.indexOfChild(view.findViewById(R.id.first)));
//                    closefragment();


                } else {
                    DadUtils.displayDialog(activityRef.get(), getString(R.string.app_name), createPin.getMessage(), getString(R.string.ok), "", false, false);
                }


            }


        }


    }


    private class AsyncTaskForgotPin extends AsyncTask<Void, Void, Void> {
        @Override
        protected Void doInBackground(Void... voids) {
            return null;
        }
    }
}
