package net.tigerlight.dad.registration.fragment;

import static net.tigerlight.dad.util.ServiceAlarmUtils.setupPeriodicService;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.SystemClock;
import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.TextView;

import com.net.tigerlight.dad.R;

import net.tigerlight.dad.blework.DadAlarmReceiver;
import net.tigerlight.dad.blework.DadBleReceiver;
import net.tigerlight.dad.home.BaseActivity;
import net.tigerlight.dad.home.BaseFragment;
import net.tigerlight.dad.registration.activity.DadMainActivity;
import net.tigerlight.dad.registration.model.UserInfoModel;
import net.tigerlight.dad.registration.util.DadConstant;
import net.tigerlight.dad.registration.util.DadUtils;
import net.tigerlight.dad.registration.webservices.CallLogin;
import net.tigerlight.dad.util.Preference;
import net.tigerlight.dad.webservices.GetUserData;

public class DadLoginToYourAccountFragment extends BaseFragment implements CompoundButton.OnCheckedChangeListener {

    private static final String ARG_EMAIL_ID = "email_id";
    private static final String ARG_PASSWORD = "password";
    private static final String TAG = DadLoginToYourAccountFragment.class.getSimpleName();
    private View view;
    private EditText etUserName;
    private EditText etPassword;
    boolean isChecked;
    private AsyncTaskLocalLogin asyncTaskLocalLogin;
    private ProgressDialog progressDialog;
    private TextView tvCancel;
    private Context context;
    private double lat;
    private double log;
    private SharedPreferences loginPreferences;
    private SharedPreferences.Editor loginPrefsEditor;
    private Boolean saveLogin;
    private AlarmManager alarmManager;
    private PendingIntent broadcast;
    private final String TAG_REFRESH_LOC = "resfresh_Location";
    private UserInfoModel profileModel;
    private AsyncTaskGetUserInfo asyncTaskGetUserInfo;
    //gcm
    private String deviceToken;
    public static final String PROPERTY_REG_ID = "registration_id";
    private static final String PROPERTY_APP_VERSION = "appVersion";
    /**
     * Substitute you own sender ID here. This is the project number you got
     * from the API Console, as described in "Getting Started."
     */
//    private String SENDER_ID = "308732044105";
    private final String SENDER_ID = "32989397760";

    public static DadLoginToYourAccountFragment newInstance(String emailId, String password) {
        DadLoginToYourAccountFragment fragment = new DadLoginToYourAccountFragment();
        Bundle args = new Bundle();
        args.putString(ARG_EMAIL_ID, emailId);
        args.putString(ARG_PASSWORD, password);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragemnt_login_to_your_account, container, false);
        return view;
    }

    @Override
    public void initView(View view) {
        etUserName = view.findViewById(R.id.fragment_login_to_your_account_et_user_name);
        etPassword = view.findViewById(R.id.fragment_login_to_your_account_et_password);
        Button tvLogin = view.findViewById(R.id.fragment_login_to_your_account_tv_login);
        tvCancel = view.findViewById(R.id.fragment_login_to_your_account_tv_cancel);
        Button tvForgotPwd = view.findViewById(R.id.fragment_login_to_your_account_tv_forgot_pwd);
        //Set the click lister
        tvLogin.setOnClickListener(this);
        tvForgotPwd.setOnClickListener(this);
        tvCancel.setOnClickListener(this);

        if (getActivity() != null) {
            lat = ((BaseActivity) getActivity()).getLatitude();
            log = ((BaseActivity) getActivity()).getLongitude();
        }

        setupPasswordField();

//        startRefreshTimeTimer();

        setLogIndetails();

//        /*  This is used for clear text from the edittex when press the cross icoc*/
//        etUserName.setOnTouchListener(new View.OnTouchListener() {
//            @Override
//            public boolean onTouch(View v, MotionEvent event) {
//                final int DRAWABLE_LEFT = 0;
//                final int DRAWABLE_TOP = 1;
//                final int DRAWABLE_RIGHT = 2;
//                final int DRAWABLE_BOTTOM = 3;
//                if (event.getAction() == MotionEvent.ACTION_UP) {
//                    if (event.getRawX() >= (etUserName.getRight() - etUserName.getCompoundDrawables()[DRAWABLE_RIGHT].getBounds().width())) {
//                        // your action here
//                        etUserName.setText("");
//                        return true;
//                    }
//                }
//                return false;
//            }
//        });

        if (getArguments() != null) {
            String emailId = getArguments().getString(ARG_EMAIL_ID);
            String password = getArguments().getString(ARG_PASSWORD);
            if (emailId != null) {
                etUserName.setText(emailId);
            }
            if (password != null) {
                etPassword.setText(password);
            }
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    private void setupPasswordField() {
        etPassword.setOnTouchListener((v, event) -> {
            if (event.getAction() == MotionEvent.ACTION_UP) {
                // Check if the touch was on the drawableEnd
                if (event.getRawX() >= (etPassword.getRight() - etPassword.getCompoundDrawables()[2].getBounds().width())) {
                    if (etPassword.getInputType() == (InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD)) {
                        // Show password
                        etPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
                        etPassword.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_eye_on, 0); // Change icon to "eye open"
                    } else {
                        // Hide password
                        etPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                        etPassword.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_eye_off, 0); // Change icon to "eye closed"
                    }
                    // Move cursor to the end
                    etPassword.setSelection(etPassword.length());
                    return true;
                }
            }
            return false;
        });
    }

    private void setLogIndetails() {
        if (Preference.getInstance().mSharedPreferences.getBoolean(DadConstant.IS_REMEMBER, false)) {
            etUserName.setText(Preference.getInstance().mSharedPreferences.getString(DadConstant.KEY_EMAIL, ""));
            etPassword.setText(Preference.getInstance().mSharedPreferences.getString(DadConstant.KEY_PASSWORD, ""));
        }
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
//        loginPreferences = getActivity().getSharedPreferences("loginPrefs", getActivity().MODE_PRIVATE);
//        loginPrefsEditor = loginPreferences.edit();
//        saveLogin = loginPreferences.getBoolean("saveLogin", false);
//        if (saveLogin == true) {
//            etUserName.setText(loginPreferences.getString("username", ""));
//            etPassword.setText(loginPreferences.getString("password", ""));
//            cbRememberMe.setChecked(true);
//        } else {
//            loginPrefsEditor.clear();
//            loginPrefsEditor.commit();
//        }
    }


    private void startRefreshTimeTimer() {
        if (broadcast != null && alarmManager != null) {
            alarmManager.cancel(broadcast);
        }
        int refreshTimeInterval = Preference.getInstance().mSharedPreferences.getInt(DadConstant.KEY_REFRESH_LOC, 5000);
        if (refreshTimeInterval != 0 && getActivity() != null) {
            alarmManager = (AlarmManager) getActivity().getSystemService(Context.ALARM_SERVICE);
            Intent intent = new Intent(getActivity(), DadAlarmReceiver.class);
            broadcast = PendingIntent.getBroadcast(getActivity(), 0, intent, PendingIntent.FLAG_IMMUTABLE);
            if (alarmManager != null) {
                alarmManager.setInexactRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP, SystemClock.elapsedRealtime(), (long) refreshTimeInterval * 60 * 1000, broadcast);
            }
        }
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
        if (fragmentId == R.id.fragment_login_to_your_account_tv_login) {
            validateFields();
        } else if (fragmentId == R.id.fragment_login_to_your_account_tv_forgot_pwd) {
            if (getActivity() instanceof DadMainActivity) {
                String email = etUserName.getText().toString();
                DadForgotPasswordFragment fragment = DadForgotPasswordFragment.newInstance(email);
                ((DadMainActivity) getActivity()).addFragment(fragment, DadLoginToYourAccountFragment.this);
            }
        } else if (fragmentId == R.id.fragment_login_to_your_account_tv_cancel) {
            getLocalFragmentManager().popBackStack();
        }
    }

    private void validateFields() {
        String email = etUserName.getText().toString().trim();
        String password = etPassword.getText().toString().trim();
        if (getActivity() != null && email.trim().isEmpty()) {
            DadUtils.displayDialog(getActivity(), getString(R.string.app_name), getString(R.string.TAG_VALID_USERNAME), getString(R.string.TAG_OK), "", false, false);
            etUserName.requestFocus();
        } else if (!DadUtils.isValidEmail(etUserName.getText().toString().trim())) {
            DadUtils.displayDialog(getActivity(), getString(R.string.app_name), getString(R.string.TAG_ENTER_VALID_EMAIL), getString(R.string.TAG_OK), "", false, false);
            etUserName.requestFocus();
        } else if (password.trim().isEmpty()) {
            DadUtils.displayDialog(getActivity(), getString(R.string.app_name), getString(R.string.TAG_VALID_PASSWORD), getString(R.string.TAG_OK), "", false, false);
            etPassword.requestFocus();
        } else if (DadUtils.isValidEmail(etUserName.getText().toString().trim())) {
            Log.d("LoginSuceess", "start logintask from here");
            if (getActivity() != null &&  DadUtils.isOnline(getActivity(), true)) {
                startLocalLoginTask(email, password);
            } else {
                DadUtils.displayDialog(getActivity(), getString(R.string.app_name), getString(R.string.TAG_INTERNET_AVAILABILITY), getString(R.string.TAG_OK), "", false, false);
            }
        }
    }

    private void startLocalLoginTask(String email, String password) {
        if (getActivity() != null && DadUtils.isInternetAvailable(getActivity())) {
            if (asyncTaskLocalLogin != null && asyncTaskLocalLogin.getStatus() == AsyncTask.Status.PENDING) {
                asyncTaskLocalLogin.execute();
            } else if (asyncTaskLocalLogin == null || asyncTaskLocalLogin.getStatus() == AsyncTask.Status.FINISHED) {
                asyncTaskLocalLogin = new AsyncTaskLocalLogin(email, password);
                asyncTaskLocalLogin.execute();
            }
        } else {
            DadUtils.displayDialogNormalMessage(getString(R.string.app_name), getString(R.string.TAG_INTERNET_AVAILABILITY), getActivity());
        }
    }

    @Override
    public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
        Preference.getInstance().savePreferenceData(DadConstant.IS_REMEMBER, b);
        if (b) {
            Preference.getInstance().savePreferenceData(DadConstant.KEY_EMAIL, etUserName.getText().toString());
            Preference.getInstance().savePreferenceData(DadConstant.KEY_PASSWORD, etPassword.getText().toString());
        } else {
            Preference.getInstance().savePreferenceData(DadConstant.KEY_EMAIL, "");
            Preference.getInstance().savePreferenceData(DadConstant.KEY_PASSWORD, "");
        }
    }

    @SuppressLint("StaticFieldLeak")
    private class AsyncTaskLocalLogin extends AsyncTask<String, Void, String> {
        private final CallLogin wsLogin;
        private final String userName;
        private final String password;


        public AsyncTaskLocalLogin(String userName, String password) {
            this.userName = userName;
            this.password = password;
            wsLogin = new CallLogin(getActivity());

        }


        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog = ProgressDialog.show(getActivity(), "", getString(R.string.TAG_Loading));
            progressDialog.setCancelable(false);
        }


        @Override
        protected String doInBackground(String... strings) {
            wsLogin.executeService(userName, password, String.valueOf(lat), String.valueOf(log));
            return null;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            if (progressDialog != null && progressDialog.isShowing()) {
                progressDialog.dismiss();
            }
            if (!isCancelled()) {
                if (wsLogin.isSuccess()) {
//                    final boolean isAccepted = Preference.getInstance().mSharedPreferences.getBoolean(Constant.IS_ACCEPT, false);
//                    if(isAccepted){
//
//                    }
                    //Utills.displayDialog(getActivity(), getString(R.string.reday_to_use), wsLogin.getMessage(), getString(android.R.string.ok), "", false, false);
                    Preference.getInstance().savePreferenceData(DadConstant.KEY_EMAIL, userName);
                    Preference.getInstance().savePreferenceData(DadConstant.KEY_PASSWORD, password);
                    Preference.getInstance().savePreferenceData(DadConstant.IS_LOGIN, true);
                    Preference.getInstance().savePreferenceData(DadConstant.USER_ID, wsLogin.getUser_id());
                    Preference.getInstance().saveEncryptedPreferenceData(DadConstant.ACCESS_TOKEN, wsLogin.getAccessToken());
                    Preference.getInstance().saveEncryptedPreferenceData(DadConstant.REFRESH_TOKEN, wsLogin.getRefreshToken());
                    Preference.getInstance().mSharedPreferences.edit().putLong(DadConstant.EXPIRES_IN, wsLogin.getExpiresIn()).apply();

                    Log.d("Login_ID", wsLogin.getMessage());

//                    if (!Utills.isMyServiceRunning(LocationBroadcastServiceNew.class, getActivity())) {
//                        final Intent intent = new Intent(getActivity(), LocationBroadcastServiceNew.class);
//                        getActivity().startService(intent);
//                    }

                    long time = 1000 * 5;  //For repeating 30 second

                    setupPeriodicService(requireContext());

//                    if (!Utills.isMyServiceRunning(LocationBroadcastServiceNew.class, getActivity())) {

//                        Intent serviceIntent = new Intent(getActivity(), LocationBroadcastServiceNew.class);
//                        PendingIntent pendingIntent = PendingIntent.getService(getActivity(), 1001, serviceIntent, PendingIntent.FLAG_CANCEL_CURRENT | PendingIntent.FLAG_IMMUTABLE);
//                        AlarmManager alarmManager = (AlarmManager) getActivity().getSystemService(Context.ALARM_SERVICE);
//                        alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(), time, pendingIntent);


//                getActivity().startService(intent);
//                    }

//                    if(!Utills.isMyServiceRunning(BleService.class, getActivity()))
//                    {
//
//                        Intent serviceIntentBle = new Intent(getActivity(), BleService.class);
//                        PendingIntent pendingIntentBle = PendingIntent.getService(getActivity(), 1001, serviceIntentBle, PendingIntent.FLAG_CANCEL_CURRENT);
//                        AlarmManager alarmManagerble = (AlarmManager) getActivity().getSystemService(Context.ALARM_SERVICE);
//                        alarmManagerble.setRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(), time, pendingIntentBle);
//
//
//                    }


                    getUserInfo();
                    //openDashBoardFragment();
//                    if (cbRememberMe.isChecked()) {
////                        Store the credential here
//                        if (cbRememberMe.isChecked()) {
//                            loginPrefsEditor.putBoolean("saveLogin", true);
//                            loginPrefsEditor.putString("username", etUserName.getText().toString().trim());
//                            loginPrefsEditor.putString("password", etPassword.getText().toString().trim());
//                            loginPrefsEditor.commit();
//                        } else {
//                            loginPrefsEditor.clear();
//                            loginPrefsEditor.commit();
//                        }
//                    }

                } else if (getActivity() != null) {
                    progressDialog.dismiss();
                    String message = getString(R.string.alert_something_wrong);
                    String positiveText = null;
                    String negativeText = getString(R.string.TAG_TRY_AGAIN);
                    DialogInterface.OnClickListener negativeCallback = (dialog, which) -> {
                        dialog.dismiss();
                    };
                    DialogInterface.OnClickListener positiveCallback = null;
                    if (wsLogin.getMessage().equals("email")) {
                        message = getString(R.string.TAG_EMAIL_NOT_FOUND);
                        positiveText = getString(R.string.fragment_registration_create_account);
                        positiveCallback = (dialog, which) -> {
                            Activity activity = getActivity();
                            if (activity instanceof DadMainActivity) {
                                ((DadMainActivity) activity).addFragment(new DadCreateAccountFragment());
                            }
                            dialog.dismiss();
                        };
                    } else if (wsLogin.getMessage().equals("password")) {
                        message = getString(R.string.TAG_INVALID_LOGIN_PASSWORD);
                        positiveText = getString(R.string.TAG_RESETPASSWORD_TXT);
                        positiveCallback = (dialog, which) -> {
                            Activity activity = getActivity();
                            if (activity instanceof DadMainActivity) {
                                DadForgotPasswordFragment fragment = DadForgotPasswordFragment.newInstance(userName);
                                ((DadMainActivity) activity).addFragment(fragment);
                            }
                            dialog.dismiss();
                        };
                    } else {
                        message = getString(R.string.alert_something_wrong);
                    }
                    DadUtils.displayDefaultDialog(
                            getActivity(),
                            getString(R.string.fragment_login_to_your_tv_login),
                            message,
                            positiveText,
                            positiveCallback,
                            negativeText,
                            negativeCallback,
                            null,
                            null
                    );
                }


            }


        }


    }

    private void getUserInfo() {
        if (DadUtils.isInternetAvailable(getActivity())) {
            if (asyncTaskGetUserInfo != null && asyncTaskGetUserInfo.getStatus() == AsyncTask.Status.PENDING) {
                asyncTaskGetUserInfo.execute();
            } else if (asyncTaskGetUserInfo == null || asyncTaskGetUserInfo.getStatus() == AsyncTask.Status.FINISHED) {
                asyncTaskGetUserInfo = new AsyncTaskGetUserInfo();
                asyncTaskGetUserInfo.execute();
            }
        } else {
            DadUtils.displayDialogNormalMessage(getString(R.string.app_name), getString(R.string.TAG_INTERNET_AVAILABILITY), getActivity());
        }
    }

    @SuppressLint("StaticFieldLeak")
    private class AsyncTaskGetUserInfo extends AsyncTask<Void, Void, Void> {

        private GetUserData getUserData;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
//            progressDialog = ProgressDialog.show(getActivity(), "", "Loading, Please wait");
//            progressDialog.setCancelable(false);
        }

        @Override
        protected Void doInBackground(Void... voids) {
            getUserData = new GetUserData(getActivity());
            getUserData.executeService();
            return null;
        }


        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            // if (progressDialog != null && progressDialog.isShowing()) {
            progressDialog.dismiss();
            //}
            if (!isCancelled()) {
                if (getUserData.isSuccess()) {
                    profileModel = getUserData.getGetUserInfoModel();
                    Preference.getInstance().savePreferenceData(DadConstant.USER_NAME, profileModel.getUsername());
                    startBackgroundThreadForBLE();
                    ((DadMainActivity) getActivity()).replaceFragment(new DadDashBoardWithSwipeableFragment());

                } else {
                    if (!getUserData.getMessage().trim().isEmpty()) {
                        DadUtils.displayDialogNormalMessage(getString(R.string.app_name), getUserData.getMessage(), getActivity());
                    } else {
                        DadUtils.displayDialogNormalMessage(getString(R.string.app_name), getString(R.string.alert_something_wrong), getActivity());

                    }
                }
            }
        }
    }


    private static final long SCAN_PERIOD = 1000;

    private void startBackgroundThreadForBLE() {
        AlarmManager alarmManagerForBLE = (AlarmManager) getActivity().getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(getActivity(), DadBleReceiver.class);
        PendingIntent broadcastIntentBle = PendingIntent.getBroadcast(getActivity(), 0, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        alarmManagerForBLE.setInexactRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP, SystemClock.elapsedRealtime(), 2 * 60 * SCAN_PERIOD, broadcastIntentBle);
    }
}

