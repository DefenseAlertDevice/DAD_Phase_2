package net.tigerlight.dad.registration.fragment;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Bundle;

import androidx.activity.result.ActivityResultLauncher;
import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.text.InputType;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.net.tigerlight.dad.R;
import net.tigerlight.dad.home.BaseFragment;
import net.tigerlight.dad.registration.util.DadConstant;
import net.tigerlight.dad.registration.util.DadUtils;
import net.tigerlight.dad.webservices.CallSendOk;
import net.tigerlight.dad.webservices.CreatePin;
import net.tigerlight.dad.webservices.ForgotPin;
import net.tigerlight.dad.webservices.HasPin;
import net.tigerlight.dad.util.DisplayDialog;
import net.tigerlight.dad.util.GPSTracker;
import net.tigerlight.dad.util.Preference;

import org.json.JSONException;
import org.json.JSONObject;

public class DadAmOkFragmentI extends BaseFragment {

    private static final int MY_PERMISSIONS_REQUEST_LOCATION = 1001;

    private EditText etPin;
    private EditText etOldPin;
    private EditText etNewPin;
    private EditText etReEnterPin;
    private EditText etMainNewPin;
    private EditText etMainConfirmPin;
    private ImageView etPinIcon;
    private ImageView etOldPinIcon;
    private ImageView etNewPinIcon;
    private ImageView etReEnterPinIcon;
    private ImageView etMainNewPinIcon;
    private ImageView etMainConfirmPinIcon;

    private LinearLayout llMain;
    private LinearLayout llFirst;
    private LinearLayout llSecond;

    private Button tvSavePin;
    private Button tvMainSavePin;

    private AsyncSendOk asyncSendOk;
    private AsyncTaskCreatePin asyncTaskCreatePin;
    private AsyncTaskHasPin asyncTaskHasPin;
    private String lattdLastKnown;
    private String longtdLastKnown;
    private ActivityResultLauncher<String> requestPermissionLauncher;

    private boolean isPinCreated = false;
    protected static final String SUCCESS = "success";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_iamok, container, false);
    }

    @Override
    public void initView(View view) {

        llMain = view.findViewById(R.id.fragment_iamok_llMain);
        llFirst = view.findViewById(R.id.fragment_iamok_llFirst);
        llSecond = view.findViewById(R.id.fragment_iamok_llSecond);

        etPin = view.findViewById(R.id.fragment_iamok_etPin);
        etPinIcon = view.findViewById(R.id.fragment_iamok_etPinIcon);
        etOldPin = view.findViewById(R.id.fragment_iamok_etOldPin);
        etOldPinIcon = view.findViewById(R.id.fragment_iamok_etOldPinIcon);
        etNewPin = view.findViewById(R.id.fragment_iamok_etNewPin);
        etNewPinIcon = view.findViewById(R.id.fragment_iamok_etNewPinIcon);
        etReEnterPin = view.findViewById(R.id.fragment_iamok_etReEnterPin);
        etReEnterPinIcon = view.findViewById(R.id.fragment_iamok_etReEnterPinIcon);
        etMainNewPin = view.findViewById(R.id.fragment_iamok_llMain_etNewPin);
        etMainNewPinIcon = view.findViewById(R.id.fragment_iamok_llMain_etNewPinIcon);
        etMainConfirmPin = view.findViewById(R.id.fragment_iamok_llMain_etReenterPin);
        etMainConfirmPinIcon = view.findViewById(R.id.fragment_iamok_llMain_etReenterPinIcon);

        Button tvSendImOkMessage = view.findViewById(R.id.fragment_iamok_tvSendIamokMsg);
        TextView tvResetPin = view.findViewById(R.id.fragment_iamok_tvResetPin);
        TextView tvForgotPin = view.findViewById(R.id.fragment_iamok_tvForgotPin);

        tvSavePin = view.findViewById(R.id.fragment_iamok_tvSavePin);
        tvMainSavePin = view.findViewById(R.id.fragment_iamok_llMain_tvSavePin);

        final TextView tvCancel = view.findViewById(R.id.fragment_iamok_tvCancel);

        isPinCreated = Preference.getInstance().mSharedPreferences.getBoolean(DadConstant.IS_PIN_CREATED, false);
        setupInitialView();

        tvSendImOkMessage.setOnClickListener(this);
        tvResetPin.setOnClickListener(this);
        tvForgotPin.setOnClickListener(this);
        tvSavePin.setOnClickListener(this);
        tvMainSavePin.setOnClickListener(this);
        tvCancel.setOnClickListener(this);
        etPinIcon.setOnClickListener(this);
        etOldPinIcon.setOnClickListener(this);
        etNewPinIcon.setOnClickListener(this);
        etReEnterPinIcon.setOnClickListener(this);
        etMainNewPinIcon.setOnClickListener(this);
        etMainConfirmPinIcon.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        super.onClick(v);
        final int fragmentId = v.getId();
        if (fragmentId == R.id.fragment_iamok_llMain_tvSavePin) {
            ValidateNewAndConfirmField(true);
        } else if (fragmentId == R.id.fragment_iamok_tvSendIamokMsg) {
            if (!TextUtils.isEmpty(etPin.getText().toString())) {
                if (etPin.getText().length() == 4) {
                    updateLatLong();
                    callSendOkService(etPin.getText().toString());
                } else {
                    DisplayDialog.getInstance().displayMessageDialog(getActivity(), getString(R.string.TAG_PING_SHORT_MSG));
                }
            } else {
                DisplayDialog.getInstance().displayMessageDialog(getActivity(), getString(R.string.TAG_PIN_NOT_EMPTY_MSG));
            }
        } else if (fragmentId == R.id.fragment_iamok_tvResetPin) {
            llFirst.setVisibility(View.GONE);
            llSecond.setVisibility(View.VISIBLE);
        } else if (fragmentId == R.id.fragment_iamok_tvForgotPin) {
            forgotPin();
        } else if (fragmentId == R.id.fragment_iamok_tvSavePin) {
            ValidateOldNewAndConfirmField();
//            callCreatePinService(false);
        } else if (getActivity() != null && fragmentId == R.id.fragment_iamok_tvCancel) {
            etOldPin.setText("");
            etNewPin.setText("");
            etReEnterPin.setText("");
            llSecond.setVisibility(View.GONE);
            llFirst.setVisibility(View.VISIBLE);
        } else if (fragmentId == R.id.fragment_iamok_etPinIcon) {
            togglePasswordInput(etPin, etPinIcon);
        } else if (fragmentId == R.id.fragment_iamok_etOldPinIcon) {
            togglePasswordInput(etOldPin, etOldPinIcon);
        }else if (fragmentId == R.id.fragment_iamok_etNewPinIcon) {
            togglePasswordInput(etNewPin, etNewPinIcon);
        } else if (fragmentId == R.id.fragment_iamok_etReEnterPinIcon) {
            togglePasswordInput(etReEnterPin, etReEnterPinIcon);
        }else if (fragmentId == R.id.fragment_iamok_llMain_etNewPinIcon) {
            togglePasswordInput(etPin, etPinIcon);
        } else if (fragmentId == R.id.fragment_iamok_llMain_etReenterPinIcon) {
            togglePasswordInput(etMainConfirmPin, etMainConfirmPinIcon);
        }
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    private void togglePasswordInput(EditText etPassword, ImageView icon) {
        if (etPassword.getInputType() == (InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_VARIATION_PASSWORD)) {
            // Show password
            etPassword.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_VARIATION_NORMAL);
            icon.setImageDrawable(getResources().getDrawable(R.drawable.ic_eye_on));
        } else {
            // Hide password
            etPassword.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_VARIATION_PASSWORD);
            icon.setImageDrawable(getResources().getDrawable(R.drawable.ic_eye_off));
        }
        // Move cursor to the end
        etPassword.setSelection(etPassword.length());
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

    private void callCreatePinService(final boolean isMainScreenOrNot) {
        if (getActivity() != null && DadUtils.isInternetAvailable(getActivity())) {
            if (asyncTaskCreatePin != null && asyncTaskCreatePin.getStatus() == AsyncTask.Status.PENDING) {
                asyncTaskCreatePin.execute();
            } else if (asyncTaskCreatePin == null || asyncTaskCreatePin.getStatus() == AsyncTask.Status.FINISHED) {
                if (isMainScreenOrNot) {
                    asyncTaskCreatePin = new AsyncTaskCreatePin(etMainConfirmPin.getText().toString());
                } else {
                    asyncTaskCreatePin = new AsyncTaskCreatePin(etReEnterPin.getText().toString());
                }
                asyncTaskCreatePin.execute();
            }
        } else {
            DadUtils.displayDialogNormalMessage(getString(R.string.app_name), getString(R.string.TAG_INTERNET_AVAILABILITY), getActivity());
        }
    }

    @SuppressLint("StaticFieldLeak")
    private class AsyncTaskHasPin extends AsyncTask<String, Void, String> {
        private HasPin hasPin;
        private ProgressDialog progressDialog;


        public AsyncTaskHasPin() {
            hasPin = new HasPin(getActivity());
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
                isPinCreated = hasPin.isSuccess();
                Preference.getInstance().savePreferenceData(DadConstant.IS_PIN_CREATED, hasPin.isSuccess());
                setupInitialView();
            }
        }
    }

    private void setupInitialView() {
        if (isPinCreated) {
            llMain.setVisibility(View.GONE);
            llFirst.setVisibility(View.VISIBLE);
        } else {
            callHasPinService();
            llMain.setVisibility(View.VISIBLE);
            llFirst.setVisibility(View.GONE);
        }
    }

    @SuppressLint("StaticFieldLeak")
    private class AsyncTaskCreatePin extends AsyncTask<String, Void, String> {
        private final CreatePin createPin;
        private final String pin;
        private ProgressDialog progressDialog;


        public AsyncTaskCreatePin(String pin) {
            this.pin = pin;
            createPin = new CreatePin(getActivity());
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog = ProgressDialog.show(getActivity(), "", getString(R.string.TAG_Loading));

            progressDialog.show();
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

            if (!isCancelled() && isAdded()) {
                if (createPin.isSuccess()) {
                    DadUtils.displayDialog(getActivity(), getString(R.string.app_name), getString(R.string.TAG_NEW_PIN_CREATED_MSG), getString(R.string.ok), "", false, false);
                    isPinCreated = true;
                    Preference.getInstance().savePreferenceData(DadConstant.IS_PIN_CREATED, isPinCreated);
                    llMain.setVisibility(View.GONE);
                    llSecond.setVisibility(View.GONE);
                    llFirst.setVisibility(View.VISIBLE);

                } else {
                    if (!createPin.getMessage().trim().isEmpty()) {
                        DadUtils.displayDialog(getActivity(), getString(R.string.app_name), createPin.getMessage(), getString(R.string.ok), "", false, false);
                    } else {
                        DadUtils.displayDialog(getActivity(), getString(R.string.app_name), getString(R.string.TAG_SOME_WENT_WRONG_MSG), getString(R.string.ok), "", false, false);
                    }
                }
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        // Check if the request code matches the one you used for requesting permissions
        if (requestCode == MY_PERMISSIONS_REQUEST_LOCATION) {
            // Check if the permission was granted
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission was granted, call your method
                updateLatLong();
            } else {
                // Permission denied, handle accordingly (e.g., show a message or disable functionality)
                Toast.makeText(getActivity(), "Permission denied", Toast.LENGTH_SHORT).show();
            }
        }
    }

    public void updateLatLong() {
        if (getActivity() != null && (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED ||
            ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_COARSE_LOCATION)
                == PackageManager.PERMISSION_GRANTED)) {
            GPSTracker gpsTracker = new GPSTracker(getActivity());
            if (gpsTracker.canGetLocation()) {
                //lattdLastKnown = "" + gpsTracker.getLatitude();
                //longtdLastKnown = "" + gpsTracker.getLongitude();

                lattdLastKnown = Preference.getInstance().mSharedPreferences.getString(DadConstant.COMMON_LATITUDE, "0.01");
                longtdLastKnown = Preference.getInstance().mSharedPreferences.getString(DadConstant.COMMON_LONGITUDE, "0.01");

            }
        } else {
            requestPermissions(new String[]{
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION,
            }, MY_PERMISSIONS_REQUEST_LOCATION);
        }
    }

    private void callSendOkService(final String pin) {
        if (getActivity() != null && DadUtils.isInternetAvailable(getActivity())) {
            if (asyncSendOk != null && asyncSendOk.getStatus() == AsyncTask.Status.PENDING) {
                asyncSendOk.execute(pin);

            } else if (asyncSendOk == null || asyncSendOk.getStatus() == AsyncTask.Status.FINISHED) {
                asyncSendOk = new AsyncSendOk();
                asyncSendOk.execute(pin);
            }
        } else {
            DadUtils.displayDialogNormalMessage(getString(R.string.app_name), getString(R.string.TAG_INTERNET_AVAILABILITY), getActivity());
        }
    }

    @SuppressLint("StaticFieldLeak")
    private class AsyncSendOk extends AsyncTask<String, Void, JSONObject> {

        private CallSendOk callSendOk;
        private ProgressDialog progressDialog;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            callSendOk = new CallSendOk(getActivity());
            progressDialog = ProgressDialog.show(getActivity(), "", getString(R.string.TAG_Loading));
            progressDialog.setCancelable(false);
        }

        @Override
        protected JSONObject doInBackground(String... params) {
            if (params.length > 0 && lattdLastKnown != null && longtdLastKnown != null) {
                return callSendOk.executeService(params[0], Double.parseDouble(lattdLastKnown), Double.parseDouble(longtdLastKnown));
            } else {
                return null;
            }
        }

        @Override
        protected void onPostExecute(JSONObject jsonObject) {
            super.onPostExecute(jsonObject);
            progressDialog.cancel();
            if (!isCancelled() && isAdded()) {
                if (jsonObject != null) {
                    if (callSendOk.isSuccess()) {
                        DadUtils.displayDialog(getActivity(), getString(R.string.app_name), getString(R.string.TAG_IM_OK_ALERT_SENT), getString(R.string.ok), "", false, false);
                        etPin.setText("");
                    } else {
                        DadUtils.displayDialog(getActivity(), getString(R.string.app_name), getString(R.string.TAG_RECORDED_PIN_NOT_MATCH), getString(R.string.ok), "", false, false);

                    }
                }
            }
        }
    }

    private void ValidateNewAndConfirmField(final boolean b) {
        if (etMainNewPin.getText().toString().trim().equalsIgnoreCase("")) {
            DadUtils.displayDialog(getActivity(), getString(R.string.app_name), getString(R.string.TAG_ENTER_NEW_PIN), getString(R.string.ok), "", false, false);
            etMainNewPin.requestFocus();

        } else if (etMainConfirmPin.getText().toString().trim().equalsIgnoreCase("")) {
            DadUtils.displayDialog(getActivity(), getString(R.string.app_name), getString(R.string.TAG_CONFIRM_PINF), getString(R.string.ok), "", false, false);
            etMainConfirmPin.requestFocus();

        } else if (etMainNewPin.getText().toString().length() < 4) {
            DadUtils.displayDialog(getActivity(), getString(R.string.app_name), getString(R.string.TAG_PIN_VALIDATION), getString(R.string.ok), "", false, false);
            etMainNewPin.requestFocus();

        } else if (etMainConfirmPin.getText().toString().length() < 4) {
            DadUtils.displayDialog(getActivity(), getString(R.string.app_name), getString(R.string.TAG_PIN_VALIDATION), getString(R.string.ok), "", false, false);
            etMainConfirmPin.requestFocus();

        } else if (etMainNewPin.getText().toString().length() > 4) {
            DadUtils.displayDialog(getActivity(), getString(R.string.app_name), getString(R.string.TAG_PIN_NOT_FOUR), getString(R.string.ok), "", false, false);
            etMainNewPin.requestFocus();

        } else if (etMainConfirmPin.getText().toString().length() > 4) {
            DadUtils.displayDialog(getActivity(), getString(R.string.app_name), getString(R.string.TAG_PIN_NOT_FOUR), getString(R.string.ok), "", false, false);
            etMainConfirmPin.requestFocus();

        } else if (!etMainNewPin.getText().toString().trim().equalsIgnoreCase("") && !etMainConfirmPin.getText().toString().trim().equalsIgnoreCase("")) {
            if (getActivity() != null && checkPassWordAndConfirmPassword(etMainNewPin.getText().toString().trim(), etMainConfirmPin.getText().toString().trim())) {
//
                if (b) {
                    if (DadUtils.isOnline(getActivity(), true)) {
                        callCreatePinService(true);
                    } else {
                        DadUtils.displayDialog(getActivity(), getString(R.string.app_name), getString(R.string.TAG_INTERNET_AVAILABILITY), getString(R.string.ok), "", false, false);
                    }
                } else {
                    DadUtils.displayDialog(getActivity(), getString(R.string.app_name), getString(R.string.TAG_CORRECT_PIN), getString(R.string.ok), "", false, false);
                    tvMainSavePin.setTextColor(ContextCompat.getColor(getActivity(), R.color.color_blue));
                }
            } else {
                DadUtils.displayDialog(getActivity(), getString(R.string.app_name), getString(R.string.TAG_INCORRECT_PIN), getString(R.string.ok), "", false, false);
                etMainNewPin.requestFocus();
            }
        }
    }

    private void ValidateOldNewAndConfirmField() {
        if (etOldPin.getText().toString().trim().isBlank()) {
            DadUtils.displayDialog(getActivity(), getString(R.string.app_name), getString(R.string.TAG_ENTER_OLD_TEMP_PIN), getString(R.string.ok), "", false, false);
            etOldPin.requestFocus();
            return;
        }

        if (etNewPin.getText().toString().trim().isBlank()) {
            DadUtils.displayDialog(getActivity(), getString(R.string.app_name), getString(R.string.TAG_ENTER_NEW_PIN), getString(R.string.ok), "", false, false);
            etNewPin.requestFocus();
            return;
        }

        if (etReEnterPin.getText().toString().trim().isBlank()) {
            DadUtils.displayDialog(getActivity(), getString(R.string.app_name), getString(R.string.TAG_CONFIRM_PINF), getString(R.string.ok), "", false, false);
            etReEnterPin.requestFocus();
            return;
        }

        if (etOldPin.getText().toString().length() < 4) {
            DadUtils.displayDialog(getActivity(), getString(R.string.app_name), getString(R.string.TAG_PIN_VALIDATION), getString(R.string.ok), "", false, false);
            etOldPin.requestFocus();
            return;
        }

        if (etNewPin.getText().toString().length() < 4) {
            DadUtils.displayDialog(getActivity(), getString(R.string.app_name), getString(R.string.TAG_PIN_VALIDATION), getString(android.R.string.ok), "", false, false);
            etNewPin.requestFocus();
            return;
        }

        if (etReEnterPin.getText().toString().length() < 4) {
            DadUtils.displayDialog(getActivity(), getString(R.string.app_name), getString(R.string.TAG_PIN_NOT_FOUR), getString(R.string.ok), "", false, false);
            etReEnterPin.requestFocus();
            return;
        }

        if (etOldPin.getText().toString().length() > 4) {
            DadUtils.displayDialog(getActivity(), getString(R.string.app_name), getString(R.string.TAG_PIN_NOT_FOUR), getString(R.string.ok), "", false, false);
            etOldPin.requestFocus();
            return;
        }

        if (etNewPin.getText().toString().length() > 4) {
            DadUtils.displayDialog(getActivity(), getString(R.string.app_name), getString(R.string.TAG_PIN_NOT_FOUR), getString(R.string.ok), "", false, false);
            etNewPin.requestFocus();
            return;
        }

        if (etReEnterPin.getText().toString().length() > 4) {
            DadUtils.displayDialog(getActivity(), getString(R.string.app_name), getString(R.string.TAG_PIN_NOT_FOUR), getString(R.string.ok), "", false, false);
            etReEnterPin.requestFocus();
            return;
        }

        if (!etNewPin.getText().toString().trim().isBlank() && !etReEnterPin.getText().toString().trim().isBlank()) {
            if (checkPassWordAndConfirmPassword(etNewPin.getText().toString().trim(), etReEnterPin.getText().toString().trim())) {
                if (getActivity() != null && DadUtils.isOnline(getActivity(), true)) {
                    callCreatePinService(false);
                } else {
                    DadUtils.displayDialog(getActivity(), getString(R.string.app_name), getString(R.string.TAG_INTERNET_AVAILABILITY), getString(R.string.ok), "", false, false);
                }
            } else {
                DadUtils.displayDialog(getActivity(), getString(R.string.app_name), getString(R.string.TAG_INCORRECT_PIN), getString(R.string.ok), "", false, false);
                etNewPin.requestFocus();
            }
        }
    }

    public boolean checkPassWordAndConfirmPassword(String password, String confirmPassword) {
        boolean pstatus = false;
        if (confirmPassword != null && password != null) {
            if (password.equals(confirmPassword)) {
                pstatus = true;
//                Utills.displayDialog(getActivity(), getString(R.string.app_name), "New PIN And Confirm PIN Have Been Matched,Click On Save PIN To Create New PIN", getString(R.string.ok), "", false, false);
            }
        }
        return pstatus;
    }

    private void forgotPin() {
        AlertDialog.Builder dialog = new AlertDialog.Builder(getActivity());
        dialog.setTitle((getString(R.string.TAG_FORGOT_PIN)));
        dialog.setCancelable(false);
        dialog.setMessage(getString(R.string.TAG_AUTO_GEN_PIN));
        dialog.setPositiveButton(getString(R.string.TAG_YES), new DialogInterface.OnClickListener() {

            public void onClick(DialogInterface dialog, int id) {
                new ForGotPinTask().execute();

            }

        });

        dialog.setNegativeButton(getString(R.string.TAG_NO), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                dialog.dismiss();
            }
        });
        dialog.show();
    }

    @SuppressLint("StaticFieldLeak")
    private class ForGotPinTask extends AsyncTask<String, String, String> {

        int response = 3;
        ProgressDialog dialog;
        private ForgotPin forgotPin;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            forgotPin = new ForgotPin(getActivity());
            dialog = new ProgressDialog(getActivity());
            dialog.show();
            dialog.setCancelable(false);
            dialog.setMessage(getString(R.string.TAG_WAIT));

        }

        @Override
        protected String doInBackground(String... params) {
            if (getActivity() != null && DadUtils.isInternetConnected(getActivity())) {

                JSONObject loginjson = forgotPin.executeService();
                try {
                    if (loginjson == null) {
                        return "fail";
                    }
                    String msg = forgotPin.getMessage();
                    if (forgotPin.isSuccess()) {
                        response = 1;
                        return SUCCESS;
                    } else if (loginjson.getInt(SUCCESS) == 2) {
                        response = 2;
                        return msg;
                    } else if (loginjson.getInt(SUCCESS) == 0) {
                        response = 0;
                        return msg;
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            } else {
                response = 3;
            }
            return "fail";

        }

        @Override
        protected void onPostExecute(String result) {
            dialog.cancel();
            if (!isCancelled() && isAdded()) {
                switch (response) {
                    case 0:
                        Toast.makeText(getActivity(), getString(R.string.TAG_PIN_CAN_NOT_GET), Toast.LENGTH_SHORT).show();
                        break;

                    case 1:
                        Toast.makeText(getActivity(), getString(R.string.TAG_EMAIL_HAS_SENT_MSG), Toast.LENGTH_SHORT).show();
                        break;

                    case 2:
                        Toast.makeText(getActivity(), getString(R.string.TAG_SOME_WENT_WRONG_MSG), Toast.LENGTH_SHORT).show();
                        break;

                    default:
                        break;
                }
            }
        }
    }


    @Override
    public void trackScreen() {
    }

    @Override
    public void initActionBar() {
    }
}
