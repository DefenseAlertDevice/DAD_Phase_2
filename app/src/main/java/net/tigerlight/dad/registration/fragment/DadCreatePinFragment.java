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

import com.net.tigerlight.dad.R;
import net.tigerlight.dad.home.BaseFragment;
import net.tigerlight.dad.registration.util.DadUtils;
import net.tigerlight.dad.webservices.CreatePin;

/**
 * Created by indianic on 21/10/16.
 */
public class DadCreatePinFragment extends BaseFragment {


    private View view;
    private TextView tvCancel;
    private EditText etNewPin;
    private EditText etConfirmPin;
    private Button btnSave;
    private AsyncTaskCreatePin asyncTaskCreatePin;
    private ProgressDialog progressDialog;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_creat_pin, container, false);
        return view;
    }

    @Override
    public void initView(View view) {

        tvCancel = (TextView) view.findViewById(R.id.fragment_create_pin_tv_cancel);
        etNewPin = (EditText) view.findViewById(R.id.fragment_create_pin_et_new_pin);
        etConfirmPin = (EditText) view.findViewById(R.id.fragment_create_pin_et_confirm_pin);
        btnSave = (Button) view.findViewById(R.id.fragment_create_pin_bn_save);

        tvCancel.setOnClickListener(this);
        btnSave.setOnClickListener(this);

    }

    @Override
    public void onClick(View v) {
        super.onClick(v);

        final int fragmentId = v.getId();

        if (fragmentId == R.id.fragment_create_pin_tv_cancel) {
            getActivity().onBackPressed();
        } else if (fragmentId == R.id.fragment_create_pin_bn_save) {
            validateFields();
        }
    }


    @Override
    public void trackScreen() {

    }

    @Override
    public void initActionBar() {

    }

    private void createPin() {


        if (DadUtils.isInternetAvailable(getActivity())) {

            Log.d("START", "internet availavble");

            if (asyncTaskCreatePin != null && asyncTaskCreatePin.getStatus() == AsyncTask.Status.PENDING) {

                asyncTaskCreatePin.execute();
            } else if (asyncTaskCreatePin == null || asyncTaskCreatePin.getStatus() == AsyncTask.Status.FINISHED) {

                asyncTaskCreatePin = new AsyncTaskCreatePin(etConfirmPin.getText().toString());
                asyncTaskCreatePin.execute();
            }
        } else {

            DadUtils.displayDialogNormalMessage(getString(R.string.app_name), getString(R.string.TAG_INTERNET_AVAILABILITY), getActivity());
        }
    }


    private void validateFields() {
        if (etNewPin.getText().toString().trim().equalsIgnoreCase("")) {
            DadUtils.displayDialog(getActivity(), getString(R.string.app_name), getString(R.string.TAG_ENTER_NEW_PIN), getString(R.string.ok), "", false, false);
            etNewPin.requestFocus();

        } else if (etConfirmPin.getText().toString().trim().equalsIgnoreCase("")) {
            DadUtils.displayDialog(getActivity(), getString(R.string.app_name), getString(R.string.confirm_pin), getString(R.string.ok), "", false, false);
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
            }

        } else {
            DadUtils.displayDialog(getActivity(), getString(R.string.app_name), getString(R.string.TAG_SAME_VAL), getString(R.string.ok), "", false, false);
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


    private class AsyncTaskCreatePin extends AsyncTask<String, Void, String> {
        private CreatePin createPin;
        private String pin;


        public AsyncTaskCreatePin(String pin) {
            this.pin = pin;

            createPin = new CreatePin(getActivity());


        }


        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog = ProgressDialog.show(getActivity(), "", getString(R.string.TAG_Loading));

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

                    DadUtils.displayDialog(getActivity(), getString(R.string.app_name), getString(R.string.TAG_NEW_PIN_CREATED_MSG), getString(R.string.ok), "", false, false);
//                    removeFragmet();
                    closefragment();


                } else {
                    DadUtils.displayDialog(getActivity(), getString(R.string.app_name), createPin.getMessage(), getString(android.R.string.ok), "", false, false);
                }


            }


        }


    }

    private void closefragment() {
        getActivity().getSupportFragmentManager().beginTransaction().remove(this).commit();
    }


}
