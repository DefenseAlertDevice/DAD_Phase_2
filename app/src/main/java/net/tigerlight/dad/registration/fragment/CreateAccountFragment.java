package net.tigerlight.dad.registration.fragment;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.target.BitmapImageViewTarget;
import com.bumptech.glide.request.transition.Transition;

import net.tigerlight.dad.R;
import net.tigerlight.dad.blework.BleReceiver;
import net.tigerlight.dad.cropimage.CropImage;
import net.tigerlight.dad.home.BaseFragment;
import net.tigerlight.dad.registration.activity.MainActivity;
import net.tigerlight.dad.registration.util.Constant;
import net.tigerlight.dad.registration.util.Utills;
import net.tigerlight.dad.registration.webservices.WsCallRegistrer;
import net.tigerlight.dad.webservices.WsUploadImage;
import net.tigerlight.dad.simplecropping.CameraUtil;
import net.tigerlight.dad.simplecropping.Constants;
import net.tigerlight.dad.util.Preference;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.media.Image;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.SystemClock;
import android.provider.MediaStore;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.core.graphics.drawable.RoundedBitmapDrawable;
import androidx.core.graphics.drawable.RoundedBitmapDrawableFactory;

import android.text.InputType;
import android.text.method.PasswordTransformationMethod;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;

/**
 * CreateAccountFragment : new user can register
 */
public class CreateAccountFragment extends BaseFragment {

    private static final String TAG = "CreateAccountFragment";
    private static final int CAMERA_PERMISSION_REQUEST_CODE = 101;

    //TO check whether image taken or not
    private boolean isImageUpdated;
    //To store the cropped path
    private String path;

    //for lat and long
    private double lat;
    private double log;
    boolean result = true;

    private TextView tvCancel;
    private TextView tvSave;
    private ImageView imProfile;

    private EditText etUserName;
    private EditText etPhoneNo;
    private EditText etEmailId;
    private EditText etPassword;
    private EditText etRePassword;

    private AsyncTaskSignUp asyncTaskSignUp;
    private ProgressDialog progressDialog;
    String croppedFile;

    private File imageFile;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_create_account, container, false);
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public void initView(View view) {
        tvCancel = (TextView) view.findViewById(R.id.fragment_create_account_tv_cancel);
        tvSave = (TextView) view.findViewById(R.id.fragment_create_account_tv_save);
        etUserName = (EditText) view.findViewById(R.id.fragment_create_account_et_user_name);
        etEmailId = (EditText) view.findViewById(R.id.fragment_create_account_custom_et_email_id);
        etPhoneNo = (EditText) view.findViewById(R.id.fragment_create_account_custom_et_phone_no);
        etPassword = (EditText) view.findViewById(R.id.fragment_login_to_your_account_et_pwd);
        etRePassword = (EditText) view.findViewById(R.id.fragment_login_to_your_account_et_re_password);
        imProfile = (ImageView) view.findViewById(R.id.fragment_create_account_custom_iv_user_profile);
        imProfile.setImageResource(R.drawable.ic_pf_pic);

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

        imProfile.setOnClickListener(this);
        tvCancel.setOnClickListener(this);
        tvSave.setOnClickListener(this);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
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
        if (v.getId() == tvSave.getId()) {
            validateFragment();
        } else if (v.getId() == tvCancel.getId()) {
            getActivity().onBackPressed();
        } else if (v.getId() == imProfile.getId()) {
            selectImage();
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode != Activity.RESULT_OK) {
            return;
        }
        switch (requestCode) {
            case Constants.REQUEST_CODE_GALLERY:
                try {
                    final InputStream inputStream = getActivity().getContentResolver().openInputStream(data.getData());
                    final FileOutputStream fileOutputStream = new FileOutputStream(imageFile);
                    CameraUtil.copyStream(inputStream, fileOutputStream);
                    fileOutputStream.close();
                    if (inputStream != null) {
                        inputStream.close();
                    }
                    startCropImage();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;
            case Constants.REQUEST_CODE_TAKE_PICTURE:
                startCropImage();
                break;
            case Constants.REQUEST_CODE_CROP_IMAGE:
                path = data.getStringExtra(CropImage.IMAGE_PATH);
                if (path == null) {
                    return;
                }
                imageFile = new File(path);
                if (imageFile.exists()) {
                    Glide.with(this)
                            .asBitmap()  // Ensure it's loading as Bitmap
                            .load(imageFile.getAbsolutePath())
                            .skipMemoryCache(true)
                            .diskCacheStrategy(DiskCacheStrategy.NONE)
                            .centerCrop()
                            .into(new BitmapImageViewTarget(imProfile) {
                                @Override
                                protected void setResource(Bitmap resource) {
                                    if (resource != null) {
                                        RoundedBitmapDrawable circularBitmapDrawable =
                                                RoundedBitmapDrawableFactory.create(getResources(), resource);
                                        circularBitmapDrawable.setCircular(true);
                                        imProfile.setImageDrawable(circularBitmapDrawable);
                                        isImageUpdated = true;
                                    }
                                }

                                @Override
                                public void onResourceReady(@NonNull Bitmap resource, Transition<? super Bitmap> transition) {
                                    super.onResourceReady(resource, transition); // Call the super to trigger setResource
                                    Log.d("Glide", "Bitmap resource is ready");
                                }
                            });
                }
                break;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void selectImage() {
        final CharSequence[] items = {getString(R.string.TAG_TAKE_PHOTO), getString(R.string.TAG_CHOOSE_FROM_GALLERY),
                getString(R.string.fragment_create_account_tv_cancel)};

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(getString(R.string.TAG_ADD_Photo));
        builder.setItems(items, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int item) {

                if (items[item].equals(getString(R.string.TAG_TAKE_PHOTO))) {
                    gotoCamera();
                } else if (items[item].equals(getString(R.string.TAG_CHOOSE_FROM_GALLERY))) {
                    gotoGallery();
                } else if (items[item].equals(getString(R.string.fragment_create_account_tv_cancel))) {
                    dialog.dismiss();
                }
            }
        });
        builder.show();
    }

    private void startCameraActivity() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        try {
            imageFile = CameraUtil.getOutputMediaFile(1); // Your method to create the file
            if (imageFile != null) {
                Uri photoURI = FileProvider.getUriForFile(
                        getActivity(),
                        getActivity().getApplicationContext().getPackageName() + ".provider",
                        imageFile
                );
                intent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                intent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION); // Grant URI permissions
                startActivityForResult(intent, Constants.REQUEST_CODE_TAKE_PICTURE);
            }
        } catch (ActivityNotFoundException e) {
            Log.e(TAG, "Cannot take picture", e);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == CAMERA_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted, start the camera activity
                startCameraActivity();
            } else {
                Toast.makeText(getContext(), "Camera permission is required to take a photo", Toast.LENGTH_SHORT).show();
            }
        }
    }

    public void gotoCamera() {
        if (ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                    getActivity(),
                    new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    CAMERA_PERMISSION_REQUEST_CODE
            );
        } else {
            // Start the camera activity
            startCameraActivity();
        }
    }

    public void gotoGallery() {
        imageFile = CameraUtil.getOutputMediaFile(1);
        final Intent photoPickerIntent = new Intent(Intent.ACTION_PICK);
        photoPickerIntent.setType("image/*");
        startActivityForResult(photoPickerIntent, Constants.REQUEST_CODE_GALLERY);
    }

    // Crop Image
    private void startCropImage() {
        if (imageFile != null) {
            final int rotation = CameraUtil.checkExIfInfo(imageFile.getPath());
            if (rotation != 0) {
                CameraUtil.rotateImage(imageFile.getPath(), rotation);
            }
            final Intent intent = new Intent(getActivity(), CropImage.class);
            intent.putExtra(CropImage.IMAGE_PATH, imageFile.getPath());
            intent.putExtra(CropImage.SCALE, true);
            intent.putExtra(CropImage.ASPECT_X, 2);
            intent.putExtra(CropImage.ASPECT_Y, 2);
            startActivityForResult(intent, Constants.REQUEST_CODE_CROP_IMAGE);
        }
    }

    private void validateFragment() {
        if (etUserName.getText().toString().trim().equalsIgnoreCase("")) {
            Utills.displayDialog(getActivity(), getString(R.string.app_name), getString(R.string.TAG_FIRSTNAME_EMPTYMSG), getString(R.string.TAG_OK), "", false, false);
            etUserName.requestFocus();
        } else if (etPhoneNo.getText().toString().trim().equalsIgnoreCase("")) {
            Utills.displayDialog(getActivity(), getString(R.string.app_name), getString(R.string.TAG_PHONE_NO_EMPTYMSG), getString(R.string.TAG_OK), "", false, false);
            etPhoneNo.requestFocus();
        } else if (etEmailId.getText().toString().trim().equals("")) {
            Utills.displayDialog(getActivity(), getString(R.string.app_name), getString(R.string.TAG_EMAIL_ID), getString(R.string.TAG_OK), "", false, false);
            etEmailId.requestFocus();
        } else if (!Utills.isValidEmail(etEmailId.getText().toString().trim())) {
            Utills.displayDialog(getActivity(), getString(R.string.app_name), getString(R.string.TAG_ENTER_VALID_EMAIL), getString(R.string.TAG_OK), "", false, false);
            etEmailId.requestFocus();
        } else if (etPassword.getText().toString().trim().equalsIgnoreCase("")) {
            Utills.displayDialog(getActivity(), getString(R.string.app_name), getString(R.string.TAG_PASSWORD_EMPTYMSG), getString(R.string.TAG_OK), "", false, false);
            etPassword.requestFocus();
        } else if (etPassword.getText().toString().trim().length() < 7) {
            Utills.displayDialog(getActivity(), getString(R.string.app_name), getString(R.string.TAG_PASSWORD_LENGTHMSG), getString(R.string.TAG_OK), "", false, false);
            etPassword.requestFocus();
        } else if (etRePassword.getText().toString().trim().equalsIgnoreCase("")) {
            Utills.displayDialog(getActivity(), getString(R.string.app_name), getString(R.string.TAG_RE_PASSWORD_EMPTYMSG), getString(R.string.TAG_OK), "", false, false);
            etRePassword.requestFocus();
        } else if (!etPassword.getText().toString().trim().equalsIgnoreCase("") && !etRePassword.getText().toString().trim().equalsIgnoreCase("")) {
            if (checkPassWordAndConfirmPassword(etPassword.getText().toString().trim(), etRePassword.getText().toString().trim())) {
                Log.d("From here", "Call service");
                if (Utills.isOnline(getActivity(), true)) {
                    signUp();
                } else {
                    Utills.displayDialog(getActivity(), getString(R.string.app_name), getString(R.string.TAG_INTERNET_AVAILABILITY), getString(R.string.TAG_OK), "", false, false);
                }
            } else {
                Utills.displayDialog(getActivity(), getString(R.string.app_name), getString(R.string.TAG_PWD_RE_PWD_EMPTYMSG), getString(R.string.TAG_OK), "", false, false);
                etPassword.requestFocus();
            }
        }
    }

    private void signUp() {
        if (Utills.isInternetAvailable(getActivity())) {
            if (asyncTaskSignUp != null && asyncTaskSignUp.getStatus() == AsyncTask.Status.PENDING) {
                asyncTaskSignUp.execute();
            } else if (asyncTaskSignUp == null || asyncTaskSignUp.getStatus() == AsyncTask.Status.FINISHED) {
                asyncTaskSignUp = new AsyncTaskSignUp();
                asyncTaskSignUp.execute();
            }
        } else {
            Utills.displayDialogNormalMessage(getString(R.string.app_name), getString(R.string.TAG_INTERNET_AVAILABILITY), getActivity());
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

    private class AsyncTaskSignUp extends AsyncTask<Void, Void, Void> {

        private WsCallRegistrer wsCreateAccount;
        private String etUserNameStr = etUserName.getText().toString().trim();
        private String etPhoneNoStr = etPhoneNo.getText().toString().trim();
        private String etEmailIdStr = etEmailId.getText().toString().trim();
        private String etPasswordStr = etPassword.getText().toString().trim();

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog = ProgressDialog.show(getActivity(), "", getString(R.string.TAG_Loading));
            progressDialog.setCancelable(false);
        }

        @Override
        protected Void doInBackground(Void... voids) {
            wsCreateAccount = new WsCallRegistrer(getActivity());
            wsCreateAccount.executeService(etEmailIdStr, etPasswordStr, String.valueOf(lat), String.valueOf(log), etUserNameStr, etPhoneNoStr);
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);
            progressDialog.cancel();
            if (!isCancelled()) {
                if (wsCreateAccount.isSuccess()) {
                    final Preference preference = Preference.getInstance();
                    Preference.getInstance().savePreferenceData(Constant.KEY_EMAIL, etEmailIdStr);
                    Preference.getInstance().savePreferenceData(Constant.KEY_PASSWORD, etPasswordStr);
                    preference.savePreferenceData(Constant.USER_ID, wsCreateAccount.getUserid());
                    preference.savePreferenceData(Constant.IS_FIRST_ACCOUNT, true);
                    preference.savePreferenceData(Constant.USER_NAME, etUserNameStr);
                    Preference.getInstance().savePreferenceData(Constant.IS_LOGIN, true);
                    Preference.getInstance().saveEncryptedPreferenceData(Constant.ACCESS_TOKEN, wsCreateAccount.getAccessToken());
                    Preference.getInstance().saveEncryptedPreferenceData(Constant.REFRESH_TOKEN, wsCreateAccount.getRefreshToken());
                    Preference.getInstance().mSharedPreferences.edit().putLong(Constant.EXPIRES_IN, wsCreateAccount.getExpiresIn()).apply();

//                    startBackgroundThreadForBLE();

                    long time = 1000 * 3;  //For repiting 30 second

//                    if (!Utills.isMyServiceRunning(LocationBroadcastServiceNew.class, getActivity())) {
//                        Intent serviceIntent = new Intent(getActivity(), LocationBroadcastServiceNew.class);
//                        PendingIntent pendingIntent = PendingIntent.getService(getActivity(), 1001, serviceIntent, PendingIntent.FLAG_CANCEL_CURRENT | PendingIntent.FLAG_IMMUTABLE);
//                        AlarmManager alarmManager = (AlarmManager) getActivity().getSystemService(Context.ALARM_SERVICE);
//                        alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(), time, pendingIntent);
//                    }

                    if (isImageUpdated) {
                        new updateProfilePicture().execute();
                    } else {
                        Toast.makeText(getActivity(), getString(R.string.TAG_REG_SUC_MSG), Toast.LENGTH_SHORT).show();
                        ((MainActivity) getActivity()).replaceFragment(new DashBoardWithSwipableFragment());
                    }
                } else {
                    if (progressDialog != null && progressDialog.isShowing()) {
                        progressDialog.dismiss();
                    }
                    Utills.displayDialog(getActivity(), getString(R.string.app_name), getString(R.string.TAG_UNABLE_CREATE_ACCOUNT), getString(R.string.ok), "", false, false);
                }
            }
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        // cancel async task if any pending.
        if (asyncTaskSignUp != null && asyncTaskSignUp.getStatus() == AsyncTask.Status.RUNNING) {
            asyncTaskSignUp.cancel(true);
        }
    }

    private class updateProfilePicture extends AsyncTask<Void, Void, Void> {
        private static final String KEY_SUCCESS = "success";
        private int response;
        private WsUploadImage wsUploadImage;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog = ProgressDialog.show(getActivity(), "", getString(R.string.TAG_Loading));
            progressDialog.setCancelable(false);
            wsUploadImage = new WsUploadImage(getActivity());
        }

        @Override
        protected Void doInBackground(Void... params) {
            if (Utills.isInternetConnected(getActivity())) {
                wsUploadImage.executeService(path);
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            progressDialog.dismiss();

            if (progressDialog != null && progressDialog.isShowing()) {
                progressDialog.dismiss();
            }

            if (!isCancelled()) {
                if (wsUploadImage.isSuccess()) {
                    ((MainActivity) getActivity()).replaceFragment(new DashBoardWithSwipableFragment());
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
                requireActivity().getSupportFragmentManager().popBackStack();
            }
        });
        dialog.show();
    }

    private static final long SCAN_PERIOD = 1000;

    private void startBackgroundThreadForBLE() {
        AlarmManager alarmManagerForBLE = (AlarmManager) getActivity().getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(getActivity(), BleReceiver.class);
        PendingIntent broadcastIntentBle = PendingIntent.getBroadcast(getActivity(), 0, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        alarmManagerForBLE.setInexactRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP, SystemClock.elapsedRealtime(), 2 * 60 * SCAN_PERIOD, broadcastIntentBle);
    }
}
