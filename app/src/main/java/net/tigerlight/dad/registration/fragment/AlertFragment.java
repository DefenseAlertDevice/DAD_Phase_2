package net.tigerlight.dad.registration.fragment;

import net.tigerlight.dad.R;
import net.tigerlight.dad.home.BaseFragment;
import net.tigerlight.dad.registration.adapter.AlertAdapter;
import net.tigerlight.dad.registration.util.Constant;
import net.tigerlight.dad.registration.util.Utills;
import net.tigerlight.dad.webservices.WsCallDADTest;
import net.tigerlight.dad.webservices.WsCallDeleteAlert;
import net.tigerlight.dad.webservices.WsCallGetAlertCount;
import net.tigerlight.dad.webservices.WsCallSendDanger;
import net.tigerlight.dad.webservices.WsCrowdAlert;
import net.tigerlight.dad.webservices.WsResetCount;
import net.tigerlight.dad.swipemenulistview.SwipeMenuCreator;
import net.tigerlight.dad.swipemenulistview.SwipeMenuItem;
import net.tigerlight.dad.swipemenulistview.SwipeMenuListView;
import net.tigerlight.dad.util.CheckForeground;
import net.tigerlight.dad.util.Constants;
import net.tigerlight.dad.util.Preference;
import net.tigerlight.dad.util.Util;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;

import androidx.appcompat.widget.SwitchCompat;
import androidx.fragment.app.Fragment;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.content.res.AssetFileDescriptor;
import android.graphics.drawable.ColorDrawable;
import android.media.MediaPlayer;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.lang.ref.WeakReference;

/**
 * AlertFragment : all alert listing
 */
public class AlertFragment extends BaseFragment implements AdapterView.OnItemClickListener, CompoundButton.OnCheckedChangeListener {

    private static final String TAG = AlertFragment.class.getSimpleName();

    public static final int MAXIMUM_ACCURACY_WAIT = 30000;
    private static final int REQUEST_PERMISSIONS_REQUEST_CODE = 1000;

    private TextView tvEmptyAlert;
    @SuppressLint("UseSwitchCompatOrMaterialCode")
    private SwitchCompat swCrowdALert;
    @SuppressLint("UseSwitchCompatOrMaterialCode")
    private SwitchCompat swTestMode;
    private SwipeMenuListView lvAlerts;
    private AlertAdapter alertAdapter;
    private ProgressDialog progressDialog;
    private static final String SUCCESS = "success";
    private boolean isDataAvailable = false;
    private boolean isJustDataDeleted = false;
    private String timezoneID;
    public static JSONObject jsonobjectToChange;
    public static boolean isEditing;
    private JSONArray jsonArray;
    private AsyncTaskSendPush asyncTaskSendPush;
    private AsyncTaskTestMode asyncTaskTestMode;
    private AsyncCrowdAlertModeOn asyncTaskCrowdAlertOn;
    private AsyncCrowdAlertModeOff asyncTaskCrowdAlertOff;
    private AsyncTaskResetCount asyncTaskResetCount;
    public static int count = 0;
    private boolean mIsSentAlertReceiverRegistered = false;

    private DashBoardWithSwipableFragment dashBoardWithSwipableFragment;

    public AlertFragment() {

    }

    public AlertFragment(DashBoardWithSwipableFragment dashBoardWithSwipableFragment) {

        this.dashBoardWithSwipableFragment = dashBoardWithSwipableFragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_alert, container, false);
    }

    @Override
    public void initView(View view) {
        callResetCount();

        TextView tvSendDanger = view.findViewById(R.id.fragment_alert_tvSendDanger);
        tvEmptyAlert = view.findViewById(R.id.fragment_alert_tvEmptyView);
        swCrowdALert = view.findViewById(R.id.fragment_alert_swCrowdAlert);

        swCrowdALert.setChecked(Preference.getInstance().mSharedPreferences.getBoolean(Constant.IS_CHECKED, false));

        swTestMode = view.findViewById(R.id.fragment_alert_swTestMode);
        lvAlerts = view.findViewById(R.id.fragment_alert_lvAlerts);

        tvSendDanger.setOnClickListener(this);
        swCrowdALert.setOnCheckedChangeListener(this);
        //swTestMode.setOnCheckedChangeListener(mTestModeOnCheckChangeListener);
        lvAlerts.setOnItemClickListener(this);
        lvAlerts.setEmptyView(tvEmptyAlert);
        isEditing = false;
        setSwipeMenu();

        if (getActivity() != null && !Utills.isInternetConnected(getActivity())) {
            Toast.makeText(getActivity(), getString(R.string.alert_check_connection), Toast.LENGTH_SHORT).show();
            return;
        }
        progressDialog = ProgressDialog.show(getActivity(), "", getString(R.string.TAG_Loading));
        progressDialog.show();

        new AlertListLoaderThread().start();


    }

    @Override
    public void onAttach(@NonNull Context context)
    {
        super.onAttach(context);
        if (!mIsSentAlertReceiverRegistered) {
            IntentFilter intentFilter = new IntentFilter(Constants.Actions.SENT_ALERT_ACTION);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) { // Android 13 and above
                context.registerReceiver(mAlertSentReceiver, intentFilter, Context.RECEIVER_NOT_EXPORTED);
            } else { // For earlier versions
                context.registerReceiver(mAlertSentReceiver, intentFilter);
            }
            mIsSentAlertReceiverRegistered = true;
        }
    }

    @Override
    public void onDetach()
    {
        super.onDetach();

        if (getActivity() != null && mIsSentAlertReceiverRegistered)
        {
            getActivity().unregisterReceiver(mAlertSentReceiver);
            mIsSentAlertReceiverRegistered = false;
        }

        if (progressDialog != null) {
            progressDialog.dismiss();
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
        if (fragmentId == R.id.fragment_alert_tvSendDanger) {
            if (Preference.getInstance().mSharedPreferences.getBoolean(Constant.IS_TEST_MODE, false)) {
                callTestModeService();
            } else {
                callSenDangerServiceRecievingListScreen();
            }
        }
    }


    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
        if (jsonArray != null) {
            try {
                jsonobjectToChange = (JSONObject) jsonArray.get(position);
                if (isEditing) {
                    return;
                }

                final AlertDetailFragment alertDetailFragment = new AlertDetailFragment();
                final Bundle bundle = new Bundle();
                final String jsonObject = jsonobjectToChange.toString();
                bundle.putString(Constant.JSON_OBJECT, jsonObject);
                alertDetailFragment.setArguments(bundle);
//                ((MainActivity)getActivity()).addFragment(alertDetailFragment,AlertFragment.this);
                loadFragment(alertDetailFragment, AlertDetailFragment.class.getSimpleName());

            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {


        if (getActivity() != null && !Utills.isInternetConnected(getActivity())) {
            swCrowdALert.setChecked(!isChecked);
            Toast.makeText(getActivity(), getString(R.string.alert_check_connection), Toast.LENGTH_SHORT).show();
            return;
        }

        if (isChecked) {
            Preference.getInstance().savePreferenceData(Constant.IS_CHECKED, true);

            callCrowdAlertModeServiceON();
        } else {
            Preference.getInstance().savePreferenceData(Constant.IS_CHECKED, false);

            callCrowdAlertModeServiceOFF();

        }
    }

    private final CompoundButton.OnCheckedChangeListener mTestModeOnCheckChangeListener = (compoundButton, b) -> {
        if (getActivity() != null && b) {
            final Dialog dialog = new Dialog(getActivity(), R.style.AppDialogTheme);
            dialog.setContentView(R.layout.custom_dialog_test_mode);
            final TextView tvTitle = dialog.findViewById(R.id.dialog_tvTitle);
            final TextView tvMessage = dialog.findViewById(R.id.dialog_tvMessage);
            final TextView tvPosButton = dialog.findViewById(R.id.dialog_tvPosButton);
            final TextView tvNegButton = dialog.findViewById(R.id.dialog_tvNegButton);


            tvTitle.setText(getString(R.string.dialog_test_mode_title));
            tvMessage.setText(getString(R.string.dialog_test_mode_msg));
            tvPosButton.setText(getString(R.string.dialog_test_mode_pos_btn));
            tvNegButton.setText(getString(R.string.dialog_test_mode_neg_btn));

            tvPosButton.setOnClickListener(view -> {
                dialog.dismiss();
                updateTestModeValue(true);
                Utills.displayDialog(getActivity(), getString(R.string.app_name), getString(R.string.TAG_TEST_MODE_ON), getString(R.string.ok), "", false, false);
//                            callTestModeService();


//                            Toast.makeText(getActivity(), "positive", Toast.LENGTH_SHORT).show();

            });

            tvNegButton.setOnClickListener(new View.OnClickListener() {
                @SuppressLint("CommitPrefEdits")
                @Override
                public void onClick(View view) {
                    Preference.getInstance().savePreferenceData(Constant.IS_TEST_MODE, false);
                    dialog.dismiss();
//                            Utills.displayDialog(getActivity(), getString(R.string.app_name), getString(R.string.TAG_TEST_MODE_OFF), getString(R.string.ok), "", false, false);
                    Preference.getInstance().mSharedPreferences.edit().putBoolean(Constant.IS_TEST_MODE, false);
                    updateTestModeSwitch();
//                            Toast.makeText(getActivity(), "Negative", Toast.LENGTH_SHORT).show();
                }
            });

            dialog.show();


        } else {

            Preference.getInstance().savePreferenceData(Constant.IS_TEST_MODE, false);
            Utills.displayDialog(getActivity(), getString(R.string.app_name), getString(R.string.TAG_TEST_MODE_OFF), getString(R.string.ok), "", false, false);
        }

    };


    @Override
    public void onViewStateRestored(Bundle savedInstanceState)
    {
        super.onViewStateRestored(savedInstanceState);
        updateTestModeSwitch();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (asyncTaskSendPush != null && asyncTaskSendPush.getStatus() == AsyncTask.Status.RUNNING) {
            asyncTaskSendPush.cancel(true);
        }
        if (asyncTaskTestMode != null && asyncTaskTestMode.getStatus() == AsyncTask.Status.RUNNING) {
            asyncTaskTestMode.cancel(true);
        }
        if (asyncTaskCrowdAlertOn != null && asyncTaskCrowdAlertOn.getStatus() == AsyncTask.Status.RUNNING) {
            asyncTaskCrowdAlertOn.cancel(true);
        }
        if (asyncTaskCrowdAlertOff != null && asyncTaskCrowdAlertOff.getStatus() == AsyncTask.Status.RUNNING) {
            asyncTaskCrowdAlertOff.cancel(true);
        }

    }

    private class AlertListLoaderThread extends Thread {
        @Override
        public void run() {
            try {
                final WsCallGetAlertCount wsCallGetAlertCount;
                wsCallGetAlertCount = new WsCallGetAlertCount(getActivity());
                String email = Preference.getInstance().mSharedPreferences.getString(Constant.KEY_EMAIL, "");
                JSONObject jsonRecieved = wsCallGetAlertCount.executeService(email, "" + 0);
                if (jsonRecieved != null) {

                    if (jsonRecieved.getInt(SUCCESS) == 1) {
                        isDataAvailable = true;
                        jsonArray = jsonRecieved.getJSONArray("data");
                        if (getActivity() != null) {
                            getActivity().runOnUiThread(new AlertListDataHandler(jsonRecieved));
                        }
                    } else {
                        isDataAvailable = false;
                    }
                }

            } catch (JSONException e) {
                e.printStackTrace();
            }
            if (getActivity() != null) {
                getActivity().runOnUiThread(new AlertListDataHandler(null));
            }
        }
    }

    private class AlertListDataHandler implements Runnable {

        private final JSONObject result;

        public AlertListDataHandler(JSONObject result) {
            this.result = result;
        }

        @Override
        public void run() {
            if ((isDataAvailable || isJustDataDeleted) && result != null) {
                try {
                    isJustDataDeleted = false;
                    jsonArray = result.getJSONArray("data");
                    int alertCount = jsonArray.length();
                    count = jsonArray.length();
                    if (alertCount < 0) {
                        alertCount = 0;
                    }
                    Preference.getInstance().savePreferenceData("alert_count", alertCount);

                    alertAdapter = new AlertAdapter(getActivity(), AlertFragment.this, jsonArray);
                    lvAlerts.setAdapter(alertAdapter);
                    lvAlerts.setOnItemClickListener(AlertFragment.this);


                    if (jsonArray.length() == 0) {
                        tvEmptyAlert.setVisibility(View.VISIBLE);
                        tvEmptyAlert.setText(getString(R.string.TAG_ALERTS_NA_MSG));
                        lvAlerts.setVisibility(View.GONE);
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            if (progressDialog != null && progressDialog.isShowing()) {
                progressDialog.dismiss();
            }
        }

    }

    private void deleteUsingThread(final int position) {
        final Handler handler = new Handler();
        final ProgressDialog progressDialog = ProgressDialog.show(getActivity(), "", getString(R.string.TAG_DELETING));
        progressDialog.show();
        new Thread(new Runnable() {

            private int response = 5;

            @Override
            public void run() {
                String helpId = jsonobjectToChange.optString("fld_help_id");
                WsCallDeleteAlert wsCallDeleteAlert = new WsCallDeleteAlert(getActivity());
                wsCallDeleteAlert.executeService(helpId);
                if (wsCallDeleteAlert.isSuccess()) {
                    isJustDataDeleted = true;
                    response = 1;
                } else {
                    response = 2;
                }

                handler.post(new Runnable() {

                    @Override
                    public void run() {
                        if (response == 2) {
                            Toast.makeText(getActivity(), getString(R.string.TAG_COULD_NOT_DELETE_MSG), Toast.LENGTH_SHORT).show();
                            progressDialog.dismiss();
                            return;
                        }

                        if (response == 1) {
                            isJustDataDeleted = false;
                            Toast.makeText(getActivity(), getString(R.string.dialog_delete_alert_title), Toast.LENGTH_SHORT).show();
                            updateJsonArray(position);
                            int alertCount = Preference.getInstance().mSharedPreferences.getInt("total_count", 0);

                            alertCount = alertCount - 1;
                            Preference.getInstance().savePreferenceData("total_count", alertCount);
                            if (dashBoardWithSwipableFragment != null) {
                                dashBoardWithSwipableFragment.updateCount();
                            }
                            alertAdapter.remove(position);
                            progressDialog.dismiss();
                        }
                    }
                });
            }
        }).start();
    }

    private void updateJsonArray(int position) {
        JSONArray newArray = new JSONArray();
        for (int i = 0; i < jsonArray.length(); i++) {
            if (i == position) {
                continue;
            }
            try {
                newArray.put(jsonArray.get(i));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        jsonArray = null;
        jsonArray = new JSONArray();
        jsonArray = newArray;
    }


    /**
     * To add fragment in container
     *
     * @param newFragment
     * @param tagStr
     */
    private void loadFragment(final Fragment newFragment, final String tagStr) {
        Util.getInstance().hideSoftKeyboard(getActivity());
        getLocalFragmentManager()
                .beginTransaction()
                .add(R.id.activity_registartion_fl_container, newFragment, newFragment.getClass().getSimpleName())
                .addToBackStack(newFragment.getClass().getSimpleName())
                .commit();
    }

    private void callSenDangerServiceRecievingListScreen() {
        if (getActivity() != null && Utills.isInternetConnected(getActivity())) {
            if (asyncTaskSendPush != null && asyncTaskSendPush.getStatus() == AsyncTask.Status.PENDING) {
                asyncTaskSendPush.execute();
//                sendAlert(asyncTaskSendPush);
            } else if (asyncTaskSendPush == null || asyncTaskSendPush.getStatus() == AsyncTask.Status.FINISHED) {
                asyncTaskSendPush = new AsyncTaskSendPush();
                sendAlert(asyncTaskSendPush);
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

    @SuppressLint("StaticFieldLeak")
    private class AsyncTaskResetCount extends AsyncTask<Void, Void, Void> {
        private WsResetCount wsResetCount;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        private final WeakReference<Activity> activityRef = new WeakReference<>(getActivity());

        @Override
        protected Void doInBackground(Void... params) {
            wsResetCount = new WsResetCount(activityRef.get());
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
                    if (activityRef.get() != null && isAdded() && !isRemoving() && isResumed()) {
                        Toast.makeText(activityRef.get(), getString(R.string.TAG_SOME_WENT_WRONG_MSG), Toast.LENGTH_SHORT).show();
                    }
                }
            }
        }
    }

    @SuppressLint("StaticFieldLeak")
    private class AsyncTaskSendPush extends AsyncTask<Void, Void, Void> {
        private WsCallSendDanger wsCallSendDanger;
        //double log =((MainActivity) getActivity()).getLongitude();
        //double lat = ((MainActivity) getActivity()).getLatitude();
        String lat = Preference.getInstance().mSharedPreferences.getString(Constant.COMMON_LATITUDE, "0.01");
        String log = Preference.getInstance().mSharedPreferences.getString(Constant.COMMON_LONGITUDE, "0.01");
        int accuracy = Preference.getInstance().mSharedPreferences.getInt(Constant.COMMON_ACCURACY, 0);

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected Void doInBackground(Void... params) {
            wsCallSendDanger = new WsCallSendDanger(getActivity());
            wsCallSendDanger.executeService(Double.parseDouble(lat), Double.parseDouble(log), timezoneID, accuracy);
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            if (getActivity() != null && !isCancelled()) {
                if (wsCallSendDanger.isSuccess()) {
                    progressDialog.dismiss();
                    playAlarmSound();
                    new AlertListLoaderThread().start();


                    final Dialog dialog = new Dialog(getActivity(), R.style.AppDialogTheme);
                    dialog.setContentView(R.layout.custom_progress_layout);
                    final TextView tvTitle = (TextView) dialog.findViewById(R.id.dialog_tvTitlee);
                    final TextView tvMessage = (TextView) dialog.findViewById(R.id.dialog_tvMessagee);
                    final TextView tvMsgLeve = (TextView) dialog.findViewById(R.id.dialog_tvMsgLevel);
                    final TextView tvPosButton = (TextView) dialog.findViewById(R.id.dialog_tvPosButtonn);
                    tvTitle.setText(getString(R.string.custom_progess_dialog_tv_title));
                    tvMessage.setText(getString(R.string.custom_progess_dialog_tv_msg));
                    tvPosButton.setText(getString(R.string.custom_progess_dialog_tv_ok));
                    tvPosButton.setOnClickListener(view -> dialog.dismiss());

                    dialog.show();
                } else {
//                    Utills.displayDialog(getActivity(), getString(R.string.app_name), getString(R.string.TAG_SOME_WENT_WRONG_MSG), getString(R.string.ok), "", false, false);
                    Toast.makeText(getActivity(), getString(R.string.TAG_SOME_WENT_WRONG_MSG), Toast.LENGTH_SHORT).show();
                    progressDialog.dismiss();
                }
            }
        }
    }


    private void callTestModeService() {
        if (getActivity() != null && Utills.isInternetConnected(getActivity())) {
            if (asyncTaskTestMode != null && asyncTaskTestMode.getStatus() == AsyncTask.Status.PENDING) {
                asyncTaskTestMode.execute();
            } else if (asyncTaskTestMode == null || asyncTaskTestMode.getStatus() == AsyncTask.Status.FINISHED) {
                asyncTaskTestMode = new AsyncTaskTestMode();
                sendAlert(asyncTaskTestMode);
            }
        } else {
            Utills.displayDialogNormalMessage(getString(R.string.app_name), getString(R.string.TAG_INTERNET_AVAILABILITY), getActivity());
        }

    }

    private void sendAlert(final AsyncTask<Void, Void, Void> task)
    {
        progressDialog = ProgressDialog.show(getActivity(), "", getString(R.string.TAG_SENDING_ALERT));
        progressDialog.show();

        if (!checkPermissions()) {
            requestPermissions();

        }
        final CountDownTimer countDownTimer = new CountDownTimer(MAXIMUM_ACCURACY_WAIT, 1000)
        {
            @Override
            public void onTick(long millisUntilFinished)
            {
                int accuracy = Preference.getInstance().mSharedPreferences.getInt(Constant.COMMON_ACCURACY, 0);
                //Log.d(TAG, "Location Accuracy = " + accuracy);

                if (accuracy >= Constants.MINIMUM_ACCEPTABLE_ACCURACY)
                {
                    cancel();
                    onFinish();
                }
            }

            @Override
            public void onFinish()
            {
                if (task.getStatus() == AsyncTask.Status.FINISHED)
                {
                    Log.e(TAG, "Trying to execute duplicate task.");
                }
                else
                {
                    task.execute();
                }
            }
        };

        countDownTimer.start();
    }

    @SuppressLint("StaticFieldLeak")
    private class AsyncTaskTestMode extends AsyncTask<Void, Void, Void> {
        private WsCallDADTest wsCallDADTest;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            wsCallDADTest = new WsCallDADTest(getActivity());
        }

        @Override
        protected Void doInBackground(Void... params) {
            wsCallDADTest.executeService();
            return null;
        }


        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            if (!isCancelled()) {
                if (wsCallDADTest.isSuccess()) {
                    updateTestModeValue(false);

                    if (CheckForeground.isInForeGround()) {
                        displayTestMessageDialog();
                    }
                } else {
                    Toast.makeText(getActivity(), getString(R.string.TAG_SOME_WENT_WRONG_MSG), Toast.LENGTH_SHORT).show();
                }
                progressDialog.dismiss();
            }
        }
    }

    private void displayTestMessageDialog() {
        playAlarmSound();
        new AlertListLoaderThread().start();
        if (getActivity() != null) {
            androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(getActivity());
            builder.setTitle(R.string.test_message_title)
                    .setMessage(R.string.test_message_body)
                    .setPositiveButton(R.string.nice, (dialog, which) -> dialog.dismiss()).show();
        }
    }

    private void playAlarmSound() {
        if (getActivity() != null) {
            final AssetFileDescriptor audioFile = getActivity().getResources().openRawResourceFd(R.raw.tigerlightsound);
            Thread thread = new Thread(() -> {
                MediaPlayer mediaPlayer = new MediaPlayer();
                try {
                    mediaPlayer.setDataSource(audioFile.getFileDescriptor(), audioFile.getStartOffset(), audioFile.getLength());

                    mediaPlayer.prepare();
                    mediaPlayer.setOnCompletionListener(MediaPlayer::release);
                    mediaPlayer.start();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
            thread.start();
            try {
                audioFile.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void callCrowdAlertModeServiceON() {
        if (getActivity() != null && Utills.isInternetConnected(getActivity())) {
            if (asyncTaskCrowdAlertOn != null && asyncTaskCrowdAlertOn.getStatus() == AsyncTask.Status.PENDING) {
                asyncTaskCrowdAlertOn.execute(1);
            } else if (asyncTaskCrowdAlertOn == null || asyncTaskCrowdAlertOn.getStatus() == AsyncTask.Status.FINISHED) {
                asyncTaskCrowdAlertOn = new AsyncCrowdAlertModeOn();
                asyncTaskCrowdAlertOn.execute(1);
            }
        } else {
            Utills.displayDialogNormalMessage(getString(R.string.app_name), getString(R.string.TAG_INTERNET_AVAILABILITY), getActivity());
        }

    }

    @SuppressLint("StaticFieldLeak")
    private class AsyncCrowdAlertModeOn extends AsyncTask<Integer, Void, Void> {

        private WsCrowdAlert wsCrowdAlert;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            wsCrowdAlert = new WsCrowdAlert(getActivity());
        }

        @Override
        protected Void doInBackground(Integer... integers) {

            int status = integers[0];
            wsCrowdAlert.executeService(status);
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);

            if (wsCrowdAlert.isSuccess()) {
                Utills.displayDialog(getActivity(), getString(R.string.app_name), getString(R.string.TAG_CROWD_ALERT_ON), getString(R.string.ok), "", false, false);
            }
        }
    }


    private void callCrowdAlertModeServiceOFF() {
        if (getActivity() != null && Utills.isInternetConnected(getActivity())) {
            if (asyncTaskCrowdAlertOff != null && asyncTaskCrowdAlertOff.getStatus() == AsyncTask.Status.PENDING) {
                asyncTaskCrowdAlertOff.execute(0);
            } else if (asyncTaskCrowdAlertOff == null || asyncTaskCrowdAlertOff.getStatus() == AsyncTask.Status.FINISHED) {
                asyncTaskCrowdAlertOff = new AsyncCrowdAlertModeOff();
                asyncTaskCrowdAlertOff.execute(0);
            }
        } else {
            Utills.displayDialogNormalMessage(getString(R.string.app_name), getString(R.string.TAG_INTERNET_AVAILABILITY), getActivity());
        }
    }

    @SuppressLint("StaticFieldLeak")
    private class AsyncCrowdAlertModeOff extends AsyncTask<Integer, Void, Void> {

        private WsCrowdAlert wsCrowdAlert;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            wsCrowdAlert = new WsCrowdAlert(getActivity());
        }

        @Override
        protected Void doInBackground(Integer... integers) {

            int status = integers[0];
            wsCrowdAlert.executeService(status);

            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);

            if (wsCrowdAlert.isSuccess()) {
                Utills.displayDialog(getActivity(), getString(R.string.app_name), getString(R.string.TAG_CROWD_ALERT_OFF), getString(R.string.ok), "", false, false);
            }
        }
    }

    /**
     * Setup swipe menu on listview and apply click event on it
     */
    private void setSwipeMenu() {
        final SwipeMenuCreator creator = menu -> {
            // create "delete" item
            final SwipeMenuItem swipeMenuItemDelete = new SwipeMenuItem(getActivity());
            if (getActivity() != null) {
                swipeMenuItemDelete.setBackground(new ColorDrawable(ContextCompat.getColor(getActivity(), R.color.color_alert_red)));
                swipeMenuItemDelete.setWidth(Utills.dpToPx(getActivity(), 100));
                swipeMenuItemDelete.setIcon(R.drawable.img_notification_delete);
                swipeMenuItemDelete.setTitleColor(ContextCompat.getColor(getActivity(), R.color.colorWhite));
                menu.addMenuItem(swipeMenuItemDelete);
            }
        };
        lvAlerts.setMenuCreator(creator);
        lvAlerts.setOnMenuItemClickListener((position, menu, index) -> {
            try {
                jsonobjectToChange = (JSONObject) jsonArray.get(position);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            if (index == 0) {
                if (menu.getMenuItems().size() == 1) {
                    deleteUsingThread(position);
                }
            }
            return false;
        });
        lvAlerts.setOnSwipeListener(new SwipeMenuListView.OnSwipeListener() {
            @Override
            public void onSwipeStart(int position) {

                Log.d("Swipe", "Start");
            }

            @Override
            public void onSwipeEnd(int position) {

            }
        });
    }

    private void displayDeleteDialog(final Activity context, final String title, final String msg, final String strPositiveText, final String strNegativeText, final int position) {
        final AlertDialog.Builder dialog = new AlertDialog.Builder(context);
        dialog.setTitle(title);
        dialog.setCancelable(false);
        dialog.setMessage(msg);
        dialog.setPositiveButton(strPositiveText, (dialog1, id) -> {
            dialog1.dismiss();
            if (getActivity() != null && Utills.isOnline(getActivity(), true)) {
                deleteUsingThread(position);
                //callDeleteNotificationService(position, notificationListDataModel.getMType(), notificationListDataModel.getMMemberMessageBoardId());
            } else {
                Utills.displayDialogNormalMessage(getString(R.string.app_name), getString(R.string.TAG_INTERNET_AVAILABILITY), getActivity());
            }
        });
        dialog.setNegativeButton(strNegativeText, (dialog12, id) -> dialog12.dismiss());
        dialog.show();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (getActivity() != null && getContext() != null) {
            getActivity().getWindow().setStatusBarColor(ContextCompat.getColor(getContext(), R.color.colorGray));
        }
    }

    private final BroadcastReceiver mAlertSentReceiver = new BroadcastReceiver()
    {
        @Override
        public void onReceive(Context context, Intent intent)
        {
            updateTestModeValue(false);
            updateTestModeSwitch();
        }
    };

    private void updateTestModeValue(boolean isChecked)
    {
        Preference.getInstance().mSharedPreferences.edit().putBoolean(Constant.IS_TEST_MODE, isChecked).apply();
        updateTestModeSwitch();
    }

    private void updateTestModeSwitch()
    {
        boolean isInTestMode =  Preference.getInstance().mSharedPreferences.getBoolean(Constant.IS_TEST_MODE, false);
        swTestMode.setOnCheckedChangeListener(null);
        swTestMode.setChecked(isInTestMode);
        swTestMode.setOnCheckedChangeListener(mTestModeOnCheckChangeListener);
    }

    private boolean checkPermissions() {
        if (getContext() != null && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            return ContextCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED &&
                    ContextCompat.checkSelfPermission(getContext(), Manifest.permission.FOREGROUND_SERVICE) == PackageManager.PERMISSION_GRANTED &&
                    ContextCompat.checkSelfPermission(getContext(), Manifest.permission.BLUETOOTH_SCAN) == PackageManager.PERMISSION_GRANTED &&
                    ContextCompat.checkSelfPermission(getContext(), Manifest.permission.BLUETOOTH_CONNECT) == PackageManager.PERMISSION_GRANTED;
        } else if (getContext() != null && Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            return ContextCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED &&
                    ContextCompat.checkSelfPermission(getContext(), Manifest.permission.FOREGROUND_SERVICE) == PackageManager.PERMISSION_GRANTED &&
                    ContextCompat.checkSelfPermission(getContext(), Manifest.permission.BLUETOOTH) == PackageManager.PERMISSION_GRANTED &&
                    ContextCompat.checkSelfPermission(getContext(), Manifest.permission.BLUETOOTH_ADMIN) == PackageManager.PERMISSION_GRANTED;
        } else if (getContext() != null) {
            return ContextCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED &&
                    ContextCompat.checkSelfPermission(getContext(), Manifest.permission.BLUETOOTH) == PackageManager.PERMISSION_GRANTED &&
                    ContextCompat.checkSelfPermission(getContext(), Manifest.permission.BLUETOOTH_ADMIN) == PackageManager.PERMISSION_GRANTED;
        }
        return false;
    }

    private void requestPermissions() {
        if (getActivity() == null) {
            return;
        }
        String[] permissions;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            permissions = new String[]{
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.FOREGROUND_SERVICE,
                    Manifest.permission.BLUETOOTH_SCAN,
                    Manifest.permission.BLUETOOTH_CONNECT
            };
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            permissions = new String[]{
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.FOREGROUND_SERVICE,
                    Manifest.permission.BLUETOOTH,
                    Manifest.permission.BLUETOOTH_ADMIN,
            };
        } else {
            permissions = new String[]{
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.BLUETOOTH,
                    Manifest.permission.BLUETOOTH_ADMIN,
            };
        }

        ActivityCompat.requestPermissions(getActivity(), permissions, REQUEST_PERMISSIONS_REQUEST_CODE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_PERMISSIONS_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
//                startLocationBroadcastService();
            } else {
                Toast.makeText(getActivity(), "Permissions denied", Toast.LENGTH_SHORT).show();
            }
        }
    }

//    private void startLocationBroadcastService() {
//        final Intent serviceIntent = new Intent(getActivity(), LocationBroadcastServiceNew.class);
//        serviceIntent.putExtra(Constants.Extras.SMALLEST_DISPLACEMENT_VALUE, 0f);
//
//        getActivity().stopService(serviceIntent);
//        getActivity().startService(serviceIntent);
//
//        Handler handler = new Handler();
//        handler.postDelayed(() -> getActivity().stopService(serviceIntent), 3000);
//    }
}
