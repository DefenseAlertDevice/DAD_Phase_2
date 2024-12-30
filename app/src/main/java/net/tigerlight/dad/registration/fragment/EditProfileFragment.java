package net.tigerlight.dad.registration.fragment;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.target.BitmapImageViewTarget;
import com.bumptech.glide.request.transition.Transition;

import net.tigerlight.dad.DADApplication;
import net.tigerlight.dad.R;
import net.tigerlight.dad.cropimage.CropImage;
import net.tigerlight.dad.registration.model.GetUserInfoModel;
import net.tigerlight.dad.registration.util.Constant;
import net.tigerlight.dad.registration.util.Utills;
import net.tigerlight.dad.registration.webservices.WsCallForgotPassword;
import net.tigerlight.dad.webservices.WsCallChangePassword;
import net.tigerlight.dad.webservices.WsCallDeleteAccount;
import net.tigerlight.dad.webservices.WsCallUpdateAccount;
import net.tigerlight.dad.webservices.WsGetUserData;
import net.tigerlight.dad.webservices.WsUploadImage;
import net.tigerlight.dad.simplecropping.CameraUtil;
import net.tigerlight.dad.simplecropping.Constants;
import net.tigerlight.dad.util.CircleTransform;
import net.tigerlight.dad.util.Preference;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.core.graphics.drawable.RoundedBitmapDrawable;
import androidx.core.graphics.drawable.RoundedBitmapDrawableFactory;
import androidx.fragment.app.DialogFragment;

import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;

import static android.app.Activity.RESULT_OK;
import static net.tigerlight.dad.util.WsConstants.ASSETS_DOMAIN;

public class EditProfileFragment extends DialogFragment implements View.OnClickListener {

    private static final int CAMERA_PERMISSION_REQUEST_CODE = 101;
    private static final int GALLERY_REQUEST_CODE = 102;

    private ImageView ivProfile;
    private EditText etUserName;
    private EditText etPhoneNo;
    private EditText etCurrentPassword;
    private EditText etNewPassword;
    private EditText etConfirmPassword;
    private ImageView cbToggle;
    private ImageView cnToggle;
    private ImageView npToggle;
    private ActivityResultLauncher<Intent> photoPickerLauncher;
    private ProgressDialog progressDialog;
    private GetUserInfoModel profileModel;
    private AsyncTaskEditProfile asyncTaskEditProfile;
    private AsyncTaskForgotPassword asyncTaskForgotPassword;
    private AsyncTaskGetUserInfo asyncTaskGetUserInfo;
    private AsyncTaskUpdatePassword asyncTaskUpdatePassword;
    private AsyncTaskDeleteAccount asyncTaskDeleteAccount;

    private static final String TAG = "EditProfileFragment";

    private String email = "";
    String imgUrl = ASSETS_DOMAIN + "user_image_";
    private boolean isImageUpdated;


    private String path;


    private File imageFile;
    private String isPhotoEdited;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        isPhotoEdited = null;
        setStyle(DialogFragment.STYLE_NORMAL, R.style.FullScreenDialogStyle);
        photoPickerLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                        Uri selectedImageUri = result.getData().getData();
                        if (selectedImageUri != null) {
                            handleSelectedImage(selectedImageUri);
                        } else {
                            Toast.makeText(getActivity(), "No image selected", Toast.LENGTH_SHORT).show();
                        }
                    }
                }
        );
        getUserInfo();
    }

    private String getRealPathFromURI(Uri uri) {
        Cursor cursor = null;
        try {
            String[] projection = {MediaStore.Images.Media.DATA};
            if (getActivity() != null) {
                cursor = getActivity().getContentResolver().query(uri, projection, null, null, null);
            }
            if (cursor != null && cursor.moveToFirst()) {
                int columnIndex = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
                return cursor.getString(columnIndex);
            }
        } catch (Exception e) {
            Log.e("PhotoPicker", "Error getting real path", e);
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return null;
    }

    private void handleSelectedImage(Uri imageUri) {
        // Use the URI to display or process the selected image
        try {
            if (imageUri != null) {
                String path = getRealPathFromURI(imageUri);
                if (path != null) {
                    imageFile = new File(path);
                    startCropImage();
                }
            } else {
                Toast.makeText(getActivity(), "Failed to retrieve the image", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            Toast.makeText(getActivity(), "Error handling the selected image", Toast.LENGTH_SHORT).show();
            Log.e("PhotoPicker", "Error processing image", e);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_edit_profile, container, false);
        initView(view);
        return view;
    }

    public void initView(View view) {
        if (getActivity() == null) {
            return;
        }
        profileModel = new GetUserInfoModel();
        TextView tvCancel = view.findViewById(R.id.fragment_edit_profile_tv_cancel);
        Button tvsave = view.findViewById(R.id.fragment_edit_profile_tv_save);
        Button tvChangePassword = view.findViewById(R.id.fragment_edit_profile_tv_change_password);
        TextView tvForgotPassword = view.findViewById(R.id.fragment_edit_profile_tv_forgot_password);
        TextView tvDeleteAccount = view.findViewById(R.id.fragment_edit_profile_tv_delete_account);
        ivProfile = view.findViewById(R.id.fragment_edit_profile_im_pf);
        etUserName = view.findViewById(R.id.fragment_edit_profile_et_user_name);
        etPhoneNo = view.findViewById(R.id.fragment_edit_profile_et_ph_no);
        etCurrentPassword = view.findViewById(R.id.fragment_edit_profile_et_current_password);
        etNewPassword = view.findViewById(R.id.fragment_edit_profile_et_new_password);
        etConfirmPassword = view.findViewById(R.id.fragment_edit_profile_et_confirm_password);
        cbToggle = view.findViewById(R.id.fragment_edit_profile_toggle_cb);
        cnToggle = view.findViewById(R.id.fragment_edit_profile_toggle_cn);
        npToggle = view.findViewById(R.id.fragment_edit_profile_toggle_np);
        ivProfile.setOnClickListener(this);
        tvCancel.setOnClickListener(this);
        tvsave.setOnClickListener(this);
        tvChangePassword.setOnClickListener(this);
        tvForgotPassword.setOnClickListener(this);
        tvDeleteAccount.setOnClickListener(this);

        final String uset_id = Preference.getInstance().mSharedPreferences.getString(Constant.USER_ID, "") + ".png";

        imgUrl = imgUrl + uset_id;

        Glide.with(EditProfileFragment.this)
                .load(imgUrl)
                .diskCacheStrategy(DiskCacheStrategy.NONE)
                .skipMemoryCache(true).transform(new CircleTransform(getActivity()))
                .placeholder(R.drawable.pf_pic)
                .into(ivProfile);


        cbToggle.setOnClickListener(this);
        cnToggle.setOnClickListener(this);
        npToggle.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        final int fragmentId = v.getId();

        if (fragmentId == R.id.fragment_edit_profile_tv_save) {
            validateEditSection();
        } else if (fragmentId == R.id.fragment_edit_profile_tv_cancel) {
            dismiss();
        } else if (fragmentId == R.id.fragment_edit_profile_tv_change_password) {
            validateUpdatePasswordSection();
        } else if (fragmentId == R.id.fragment_edit_profile_im_pf) {
            selectImage();
        } else if (fragmentId == R.id.fragment_edit_profile_toggle_cb) {
            togglePasswordInput(etCurrentPassword, cbToggle);
        } else if (fragmentId == R.id.fragment_edit_profile_toggle_cn) {
            togglePasswordInput(etConfirmPassword, cnToggle);
        } else if (fragmentId == R.id.fragment_edit_profile_toggle_np) {
            togglePasswordInput(etNewPassword, npToggle);
        } else if (fragmentId == R.id.fragment_edit_profile_tv_forgot_password) {
            forgotPassword();
        } else if (fragmentId == R.id.fragment_edit_profile_tv_delete_account) {
            deleteAccount();
        }
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    private void togglePasswordInput(EditText etPassword, ImageView icon) {
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
    }

    private String resolveFilePath(Uri uri) {
        if (getActivity() == null) {
            return null;
        }

        if ("content".equalsIgnoreCase(uri.getScheme())) {
            String[] projection = {MediaStore.Images.Media.DATA};
            try (Cursor cursor = getActivity().getContentResolver().query(uri, projection, null, null, null)) {
                if (cursor != null && cursor.moveToFirst()) {
                    int columnIndex = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
                    return cursor.getString(columnIndex);
                }
            } catch (Exception e) {
                Log.e(TAG, "Error resolving file path from content URI", e);
            }
        } else if ("file".equalsIgnoreCase(uri.getScheme())) {
            return uri.getPath();
        }
        return null;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (resultCode != RESULT_OK) {
            return;
        }
        switch (requestCode) {
            case GALLERY_REQUEST_CODE:
                try {
                    Uri selectedImageUri = data.getData();
                    if (selectedImageUri != null) {
                        String filePath = resolveFilePath(selectedImageUri);
                        if (filePath != null) {
                            imageFile = new File(filePath);
                            Log.d(TAG, "File Path: " + filePath); // Add logging
                            startCropImage();
                        } else {
                            Toast.makeText(getActivity(), "Error resolving file path.", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(getActivity(), "Error: Invalid file selection.", Toast.LENGTH_SHORT).show();
                    }
                } catch (Exception e) {
                    Log.e(TAG, "Error processing selected file", e);
                    Toast.makeText(getActivity(), "Error getting selected files.", Toast.LENGTH_SHORT).show();
                }
                break;

            case Constants.REQUEST_CODE_TAKE_PICTURE:
                startCropImage();
                break;

            case Constants.REQUEST_CODE_CROP_IMAGE:
                String croppedImagePath = data.getStringExtra(CropImage.IMAGE_PATH);
                if (croppedImagePath != null) {
                    imageFile = new File(croppedImagePath);
                    isPhotoEdited = croppedImagePath;
                    updateProfileImage(imageFile);
                } else {
                    Toast.makeText(getActivity(), "Error cropping image.", Toast.LENGTH_SHORT).show();
                }
                break;

            default:
                super.onActivityResult(requestCode, resultCode, data);
        }
    }

    private void updateProfileImage(File imageFile) {
        if (imageFile.exists()) {
            Glide.with(this)
                    .asBitmap()  // Ensure it's loading as Bitmap
                    .load(imageFile.getAbsolutePath())
                    .skipMemoryCache(true)
                    .diskCacheStrategy(DiskCacheStrategy.NONE)
                    .centerCrop()
                    .into(new BitmapImageViewTarget(ivProfile) {
                        @Override
                        protected void setResource(Bitmap resource) {
                            if (resource != null) {
                                RoundedBitmapDrawable circularBitmapDrawable =
                                        RoundedBitmapDrawableFactory.create(getResources(), resource);
                                circularBitmapDrawable.setCircular(true);
                                ivProfile.setImageDrawable(circularBitmapDrawable);
                            }
                        }

                        @Override
                        public void onResourceReady(@NonNull Bitmap resource, Transition<? super Bitmap> transition) {
                            super.onResourceReady(resource, transition); // Call the super to trigger setResource
                            Log.d("Glide", "Bitmap resource is ready");
                        }
                    });
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
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

    public void gotoCamera() {
        Activity activity = getActivity();
        if (activity != null) {
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
        } else {
            // Start the camera activity
            startCameraActivity();
        }
    }

    private void startCameraActivity() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        try {
            imageFile = CameraUtil.getOutputMediaFile(1); // Your method to create the file
            Activity activity = getActivity();
            if (activity != null && imageFile != null) {
                Uri photoURI = FileProvider.getUriForFile(
                        activity,
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

    public void gotoGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");

        // Use Photo Picker API for Android 13+
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            intent = new Intent(MediaStore.ACTION_PICK_IMAGES);
        }

        // Launch the picker
        photoPickerLauncher.launch(intent);
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


    private void setData(GetUserInfoModel getUserInfoModel) {
        etUserName.setText(getUserInfoModel.getUsername());
        etPhoneNo.setText(getUserInfoModel.getPhone_no());
        email = getUserInfoModel.getEmail();
        Preference.getInstance().savePreferenceData(Constant.USER_NAME, etUserName.getText().toString());
    }

    private void validateUpdatePasswordSection() {
        String currentPwd = Preference.getInstance().mSharedPreferences.getString(Constant.KEY_PASSWORD, "");
        if (etCurrentPassword.getText().toString().trim().equalsIgnoreCase("")) {
            Utills.displayDialog(getActivity(), getString(R.string.app_name), getString(R.string.CURRENT_PWD_REQ_MSG), getString(R.string.TAG_OK), "", false, false);
            etCurrentPassword.requestFocus();

        } else if (etNewPassword.getText().toString().trim().equalsIgnoreCase("")) {
            Utills.displayDialog(getActivity(), getString(R.string.app_name), getString(R.string.TAG_NEW_PWD_REQ_MSG), getString(R.string.TAG_OK), "", false, false);
            etNewPassword.requestFocus();

        } else if (etNewPassword.getText().toString().trim().length() < 7) {
            Utills.displayDialog(getActivity(), getString(R.string.app_name), getString(R.string.fragment_login_to_your_account_tv_pwd_hint), getString(R.string.TAG_OK), "", false, false);
            etNewPassword.requestFocus();

        } else if (etConfirmPassword.getText().toString().trim().equalsIgnoreCase("")) {
            Utills.displayDialog(getActivity(), getString(R.string.app_name), getString(R.string.TAG_CONFIRM_PWD_REQ_MSG), getString(R.string.TAG_OK), "", false, false);
            etNewPassword.requestFocus();

        } else if (!etNewPassword.getText().toString().trim().equalsIgnoreCase("") && !etConfirmPassword.getText().toString().trim().equalsIgnoreCase("")) {
            if (checkPassWordAndConfirmPassword(etNewPassword.getText().toString().trim(), etConfirmPassword.getText().toString().trim())) {
                if (checkPassWordAndConfirmPassword(etCurrentPassword.getText().toString().trim(), currentPwd.trim())) {
                    Log.d("From here", "Call service");
                    if (getActivity() != null && Utills.isOnline(getActivity(), true)) {
                        updatePassword();
                        // Utils.displayDialog(this, getString(R.string.app_name), "Account has been created", getString(android.R.string.ok), "", false, true);
                    } else {
                        Utills.displayDialog(getActivity(), getString(R.string.app_name), getString(R.string.TAG_INTERNET_AVAILABILITY), getString(R.string.TAG_OK), "", false, false);
                    }
                } else {
                    Utills.displayDialog(getActivity(), getString(R.string.app_name), getString(R.string.TAG_ENTER_CURRECT_PWD), getString(R.string.TAG_OK), "", false, false);
                }

            } else {
                Utills.displayDialog(getActivity(), getString(R.string.app_name), getString(R.string.TAG_SAME_MSG), getString(R.string.TAG_OK), "", false, false);
                etNewPassword.requestFocus();
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

    private void validateEditSection() {
        if (etUserName.getText().toString().trim().equalsIgnoreCase("")) {
            Utills.displayDialog(getActivity(), getString(R.string.app_name), getString(R.string.TAG_FIRSTNAME_EMPTYMSG), getString(R.string.TAG_OK), "", false, false);
            etUserName.requestFocus();

        } else if (etPhoneNo.getText().toString().trim().equalsIgnoreCase("")) {
            Utills.displayDialog(getActivity(), getString(R.string.app_name), getString(R.string.TAG_PHONE_NO_EMPTYMSG), getString(R.string.TAG_OK), "", false, false);
            etPhoneNo.requestFocus();

        } else {
            if (getActivity() != null && Utills.isOnline(getActivity(), true)) {
                editProfile();
                //  Utils.displayDialog(this, getString(R.string.app_name), "We've sent a password reset link to email address", getString(android.R.string.ok), "", false, true);
            } else {
                Utills.displayDialog(getActivity(), getString(R.string.app_name), getString(R.string.TAG_INTERNET_AVAILABILITY), getString(R.string.TAG_OK), "", false, false);
            }
        }

    }

    private void updatePassword() {
        if (getActivity() != null && Utills.isInternetConnected(getActivity())) {
            if (asyncTaskUpdatePassword != null && asyncTaskUpdatePassword.getStatus() == AsyncTask.Status.PENDING) {
                asyncTaskUpdatePassword.execute();
            } else if (asyncTaskUpdatePassword == null || asyncTaskUpdatePassword.getStatus() == AsyncTask.Status.FINISHED) {
                asyncTaskUpdatePassword = new AsyncTaskUpdatePassword();
                asyncTaskUpdatePassword.execute();
            }
        } else {
            Utills.displayDialogNormalMessage(getString(R.string.app_name), getString(R.string.TAG_INTERNET_AVAILABILITY), getActivity());
        }
    }

    private void editProfile() {
        if (getActivity() != null && Utills.isInternetAvailable(getActivity())) {
            if (asyncTaskEditProfile != null && asyncTaskEditProfile.getStatus() == AsyncTask.Status.PENDING) {
                asyncTaskEditProfile.execute();
            } else if (asyncTaskEditProfile == null || asyncTaskEditProfile.getStatus() == AsyncTask.Status.FINISHED) {
                asyncTaskEditProfile = new AsyncTaskEditProfile();
                asyncTaskEditProfile.execute();
            }
        } else {
            Utills.displayDialogNormalMessage(getString(R.string.app_name), getString(R.string.TAG_INTERNET_AVAILABILITY), getActivity());
        }

    }

    private void forgotPassword() {

        if (getActivity() != null && Utills.isInternetAvailable(getActivity())) {
            if (asyncTaskForgotPassword != null && asyncTaskForgotPassword.getStatus() == AsyncTask.Status.PENDING) {
                asyncTaskForgotPassword.execute();
            } else if (asyncTaskForgotPassword == null || asyncTaskForgotPassword.getStatus() == AsyncTask.Status.FINISHED) {
                asyncTaskForgotPassword = new AsyncTaskForgotPassword();
                asyncTaskForgotPassword.execute();
            }
        } else {
            Utills.displayDialogNormalMessage(getString(R.string.app_name), getString(R.string.TAG_INTERNET_AVAILABILITY), getActivity());
        }
    }

    private void deleteAccount() {

        if (getActivity() != null && Utills.isInternetAvailable(getActivity())) {
            if (asyncTaskDeleteAccount != null && asyncTaskDeleteAccount.getStatus() == AsyncTask.Status.PENDING) {
                asyncTaskDeleteAccount.execute();
            } else if (asyncTaskDeleteAccount == null || asyncTaskDeleteAccount.getStatus() == AsyncTask.Status.FINISHED) {
                asyncTaskDeleteAccount = new AsyncTaskDeleteAccount();
                asyncTaskDeleteAccount.execute();
            }
        } else {
            Utills.displayDialogNormalMessage(getString(R.string.app_name), getString(R.string.TAG_INTERNET_AVAILABILITY), getActivity());
        }
    }

    private void getUserInfo() {
        if (getActivity() != null && Utills.isInternetAvailable(getActivity())) {
            if (asyncTaskGetUserInfo != null && asyncTaskGetUserInfo.getStatus() == AsyncTask.Status.PENDING) {
                asyncTaskGetUserInfo.execute();
            } else if (asyncTaskGetUserInfo == null || asyncTaskGetUserInfo.getStatus() == AsyncTask.Status.FINISHED) {
                asyncTaskGetUserInfo = new AsyncTaskGetUserInfo();
                asyncTaskGetUserInfo.execute();
            }
        } else {
            Utills.displayDialogNormalMessage(getString(R.string.app_name), getString(R.string.TAG_INTERNET_AVAILABILITY), getActivity());
        }
    }

    @SuppressLint("StaticFieldLeak")
    private class AsyncTaskGetUserInfo extends AsyncTask<Void, Void, Void> {

        private WsGetUserData wsGetUserData;
        private ProgressDialog progressDialog;
        private int user_id;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog = ProgressDialog.show(getActivity(), "", getString(R.string.TAG_Loading));
            progressDialog.show();
            progressDialog.setCancelable(false);
        }

        @Override
        protected Void doInBackground(Void... voids) {
            wsGetUserData = new WsGetUserData(getActivity());
            wsGetUserData.executeService();
            return null;
        }


        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            if (progressDialog != null && progressDialog.isShowing()) {
                progressDialog.dismiss();
            }
            if (!isCancelled()) {
                if (wsGetUserData.isSuccess()) {
                    profileModel = wsGetUserData.getGetUserInfoModel();
                    setData(profileModel);

                } else {
                    Utills.displayDialogNormalMessage(getString(R.string.app_name), wsGetUserData.getMessage(), getActivity());
                }
            }
        }
    }

    @SuppressLint("StaticFieldLeak")
    private class AsyncTaskEditProfile extends AsyncTask<Void, Void, Void> {

        private WsCallUpdateAccount wsCallUpdateAccount;
        private ProgressDialog progressDialog;
        private String emailStr = "";
        private String phonelStr = "";

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog = ProgressDialog.show(getActivity(), "", getString(R.string.TAG_Loading));
            progressDialog.setCancelable(false);
            emailStr = etUserName.getText().toString().trim();
            phonelStr = etPhoneNo.getText().toString().trim();
            wsCallUpdateAccount = new WsCallUpdateAccount(getActivity());
        }

        @Override
        protected Void doInBackground(Void... voids) {

            wsCallUpdateAccount.executeService(Preference.getInstance().mSharedPreferences.getString(Constant.COMMON_LATITUDE, ""), Preference.getInstance().mSharedPreferences.getString(Constant.COMMON_LONGITUDE, ""), emailStr, phonelStr);
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);
            progressDialog.dismiss();
//            if (progressDialog != null && progressDialog.isShowing()) {
//                progressDialog.dismiss();
//            }
            if (!isCancelled()) {
                if (wsCallUpdateAccount.isSuccess()) {
                    Preference.getInstance().savePreferenceData(Constant.USER_NAME, etUserName.getText().toString());
                    if (isPhotoEdited != null) {
                        new AsynTaskUploadProfilePicEditProfile().execute();
                    } else {
                        displayDialog(getActivity(), getString(R.string.app_name), getString(R.string.TAG_PROFILE_UPDATED_MSG), getString(R.string.TAG_OK));
                    }

                } else {
                    Utills.displayDialog(getActivity(), getString(R.string.app_name), wsCallUpdateAccount.getMessage(), getString(R.string.TAG_OK), "", false, false);
                }
            }
        }
    }

    @SuppressLint("StaticFieldLeak")
    private class AsyncTaskForgotPassword extends AsyncTask<Void, Void, Void> {

        private WsCallForgotPassword wsCallForgotPassword;
        private ProgressDialog progressDialog;


        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog = ProgressDialog.show(getActivity(), "", getString(R.string.TAG_Loading));
            progressDialog.setCancelable(false);

            wsCallForgotPassword = new WsCallForgotPassword(getActivity());

        }

        @Override
        protected Void doInBackground(Void... voids) {


            wsCallForgotPassword.executeService(email);

            return null;
        }


        @Override
        protected void onPostExecute(Void result) {

            super.onPostExecute(result);
            if (progressDialog != null && progressDialog.isShowing()) {
                progressDialog.dismiss();
            }
            if (!isCancelled()) {

                if (wsCallForgotPassword.isSuccess()) {

                    Utills.displayDialog(getActivity(), getString(R.string.app_name), getString(R.string.TAG_EMAIL_HAS_SENT_MSG), getString(R.string.TAG_OK), "", false, false);

                } else {
                    Utills.displayDialog(getActivity(), getString(R.string.app_name), wsCallForgotPassword.getMessage(), getString(R.string.TAG_OK), "", false, false);
                }

            }
        }

    }

    @SuppressLint("StaticFieldLeak")
    private class AsyncTaskDeleteAccount extends AsyncTask<Void, Void, Void> {

        private WsCallDeleteAccount wsCallDeleteAccount;
        private ProgressDialog progressDialog;


        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog = ProgressDialog.show(getActivity(), "", getString(R.string.TAG_Loading));
            progressDialog.setCancelable(false);
            wsCallDeleteAccount = new WsCallDeleteAccount(getActivity());
        }

        @Override
        protected Void doInBackground(Void... voids) {
            wsCallDeleteAccount.executeService();
            return null;
        }


        @Override
        protected void onPostExecute(Void result) {

            super.onPostExecute(result);
            if (progressDialog != null && progressDialog.isShowing()) {
                progressDialog.dismiss();
            }
            if (!isCancelled()) {

                if (wsCallDeleteAccount.isSuccess()) {
                    // TODO: add logout action here
                    dismiss();
                } else {
                    Utills.displayDialog(getActivity(), getString(R.string.app_name), wsCallDeleteAccount.getMessage(), getString(R.string.TAG_OK), "", false, false);
                }

            }
        }

    }

    @SuppressLint("StaticFieldLeak")
    private class AsyncTaskUpdatePassword extends AsyncTask<Void, Void, Void> {

        private WsCallChangePassword wsCallChangePassword;
        private String oldPwdStr = "";
        private String newPwdStr = "";

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            oldPwdStr = etCurrentPassword.getText().toString();
            newPwdStr = etNewPassword.getText().toString();
            progressDialog = ProgressDialog.show(getActivity(), "", getString(R.string.TAG_Loading));
            progressDialog.setCancelable(false);
            wsCallChangePassword = new WsCallChangePassword(getActivity());
        }

        @Override
        protected Void doInBackground(Void... voids) {
            wsCallChangePassword.executeService(oldPwdStr, newPwdStr);
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);
            if (progressDialog != null && progressDialog.isShowing()) {
                progressDialog.dismiss();
            }
            if (!isCancelled()) {
                if (wsCallChangePassword.isSuccess()) {
                    Preference.getInstance().savePreferenceData(Constant.KEY_PASSWORD, newPwdStr);
                    Utills.displayDialog(getActivity(), getString(R.string.app_name), getString(R.string.TAG_PWD_UPDATED_MSG), getString(R.string.TAG_OK), "", false, false);
                } else {
                    Utills.displayDialog(getActivity(), getString(R.string.app_name), wsCallChangePassword.getMessage(), getString(R.string.TAG_OK), "", false, false);
                }
            }
        }
    }

    @SuppressLint("StaticFieldLeak")
    private class AsynTaskUploadProfilePicEditProfile extends AsyncTask<Void, Void, Void> {
        private WsUploadImage wsUploadImage;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog = ProgressDialog.show(getActivity(), "", getString(R.string.TAG_Loading));
            progressDialog.setCancelable(false);
            wsUploadImage = new WsUploadImage(getActivity());
        }

        @Override
        protected Void doInBackground(Void... voids) {
            if (isPhotoEdited != null) {
                wsUploadImage.executeService(isPhotoEdited);
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            if (progressDialog != null && progressDialog.isShowing()) {
                progressDialog.dismiss();
            }

            if (!isCancelled()) {
                if (wsUploadImage.isSuccess()) {
                    displayDialog(getActivity(), getString(R.string.app_name), getString(R.string.TAG_PROFILE_UPDATED_MSG), getString(R.string.TAG_OK));
                }
            }
        }
    }

    private void displayDialog(final Activity context, final String title, final String msg, final String strPositiveText) {
        final AlertDialog.Builder dialog = new AlertDialog.Builder(context);
        dialog.setTitle(title);
        dialog.setCancelable(false);
        dialog.setMessage(msg);
        dialog.setPositiveButton(strPositiveText, (dialog1, id) -> {
            dialog1.dismiss();
            dismiss();
        });
        dialog.show();
    }

}
