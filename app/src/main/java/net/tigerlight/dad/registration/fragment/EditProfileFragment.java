package net.tigerlight.dad.registration.fragment;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.target.BitmapImageViewTarget;
import com.bumptech.glide.request.transition.Transition;

import net.tigerlight.dad.DADApplication;
import net.tigerlight.dad.R;
import net.tigerlight.dad.cropimage.CropImage;
import net.tigerlight.dad.home.BaseFragment;
import net.tigerlight.dad.registration.model.GetUserInfoModel;
import net.tigerlight.dad.registration.util.Constant;
import net.tigerlight.dad.registration.util.Utills;
import net.tigerlight.dad.registration.webservices.WsCallForgotPassword;
import net.tigerlight.dad.webservices.WsCallChangePassword;
import net.tigerlight.dad.webservices.WsCallUpdateAccount;
import net.tigerlight.dad.webservices.WsGetUserData;
import net.tigerlight.dad.webservices.WsUploadImage;
import net.tigerlight.dad.simplecropping.CameraUtil;
import net.tigerlight.dad.simplecropping.Constants;
import net.tigerlight.dad.util.CircleTransform;
import net.tigerlight.dad.util.Preference;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;

import androidx.annotation.NonNull;
import androidx.core.graphics.drawable.RoundedBitmapDrawable;
import androidx.core.graphics.drawable.RoundedBitmapDrawableFactory;
import android.text.method.PasswordTransformationMethod;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.Locale;

import static android.app.Activity.RESULT_OK;
import static net.tigerlight.dad.util.WsConstants.ASSETS_DOMAIN;

public class EditProfileFragment extends BaseFragment {

    private DADApplication dadApplication;
    private View view;
    private TextView tvCancel;
    private TextView tvsave;
    private TextView tvForgotPassword;
    private TextView tvChangeLanguage;
    private TextView tvDefaultLanguage;
    private ImageView ivProfile;
    private EditText etUserName;
    private EditText etPhoneNo;
    private EditText etCurrentPassword;
    private EditText etNewPassword;
    private EditText etConfirmPassword;
    private CheckBox cbToggle;
    //    private CheckBox cbDa;
//    private CheckBox cbNb;
//    private CheckBox cbSv;
//    private CheckBox cbEng;
    private ProgressDialog progressDialog;
    private GetUserInfoModel profileModel;
    private AsyncTaskEditProfile asyncTaskEditProfile;
    private AsyncTaskForgotPassword asyncTaskForgotPassword;
    private AsyncTaskGetUserInfo asyncTaskGetUserInfo;
    private AsyncTaskUpdatePassword asyncTaskUpdatePassword;

    private static final String TAG = "CreateAccountFragment";
    private String userChoosenTask;
    //keep track of camera capture intent

    boolean result = true;

    private double lat;
    private double log;
    private String email = "";
    private String croppedFile;
    //    String imgUrl = "http://52.33.140.142/admin/uploads/user_image/user_image_";
    String imgUrl = ASSETS_DOMAIN + "user_image_";
    private boolean isImageUpdated;


    private String path;


    private File imageFile;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getUserInfo();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_edit_profile, container, false);
        return view;
    }

    @Override
    public void initView(View view) {
        dadApplication = (DADApplication) getActivity().getApplication();
//        lat = ((BaseActivity) getActivity()).getLatitude();
//        log = ((BaseActivity) getActivity()).getLongitude();

        profileModel = new GetUserInfoModel();
        tvCancel = (TextView) view.findViewById(R.id.fragment_edit_profile_tv_cancel);
        tvsave = (TextView) view.findViewById(R.id.fragment_edit_profile_tv_save);
        tvForgotPassword = (TextView) view.findViewById(R.id.fragment_edit_profile_tv_forgot_password);
        ivProfile = (ImageView) view.findViewById(R.id.fragment_edit_profile_im_pf);
        tvChangeLanguage = (TextView) view.findViewById(R.id.fragment_edit_profile_tv_change_language);
//        tvDefaultLanguage = (TextView) view.findViewById(R.id.fragment_edit_profile_tv_eng);


        etUserName = (EditText) view.findViewById(R.id.fragment_edit_profile_et_user_name);
        etPhoneNo = (EditText) view.findViewById(R.id.fragment_edit_profile_et_ph_no);
        etCurrentPassword = (EditText) view.findViewById(R.id.fragment_edit_profile_et_current_password);
        etNewPassword = (EditText) view.findViewById(R.id.fragment_edit_profile_et_new_password);
        etConfirmPassword = (EditText) view.findViewById(R.id.fragment_edit_profile_et_confirm_password);
        cbToggle = (CheckBox) view.findViewById(R.id.fragment_edit_profile_toggle_cb);
//        cbDa = (CheckBox) view.findViewById(R.id.custom_dialog_select_lang_da);
//        cbNb = (CheckBox) view.findViewById(R.id.custom_dialog_select_lang_nb);
//        cbSv = (CheckBox) view.findViewById(R.id.custom_dialog_select_lang_sv);
//        cbEng = (CheckBox) view.findViewById(R.id.custom_dialog_select_lang_en);
        ivProfile.setOnClickListener(this);
        tvCancel.setOnClickListener(this);
        tvsave.setOnClickListener(this);
        tvForgotPassword.setOnClickListener(this);
        tvChangeLanguage.setOnClickListener(this);
//        tvDefaultLanguage.setOnClickListener(this);


        final String uset_id = Preference.getInstance().mSharedPreferences.getString(Constant.USER_ID, "") + ".png";

        imgUrl = imgUrl + uset_id;

        Glide.with(EditProfileFragment.this)
                .load(imgUrl)
                .diskCacheStrategy(DiskCacheStrategy.NONE)
                .skipMemoryCache(true).transform(new CircleTransform(getActivity()))
                .placeholder(R.drawable.pf_pic)
                .into(ivProfile);


        cbToggle.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                int start, end;
                Log.i("inside checkbox chnge", "" + isChecked);
                if (!isChecked) {
                    cbToggle.setText(getString(R.string.show));
                    start = etCurrentPassword.getSelectionStart();
                    end = etCurrentPassword.getSelectionEnd();
                    etCurrentPassword.setTransformationMethod(new PasswordTransformationMethod());
                    etCurrentPassword.setSelection(start, end);
                } else {
                    cbToggle.setText(getString(R.string.hide));
                    start = etCurrentPassword.getSelectionStart();
                    end = etCurrentPassword.getSelectionEnd();
                    etCurrentPassword.setTransformationMethod(null);
                    etCurrentPassword.setSelection(start, end);
                }
            }
        });


    }

    @Override
    public void onClick(View v) {
        super.onClick(v);

        final int fragmentId = v.getId();

        if (fragmentId == R.id.fragment_edit_profile_tv_save) {
            validateEditSection();
        } else if (fragmentId == R.id.fragment_edit_profile_tv_cancel) {
            getActivity().onBackPressed();
        } else if (fragmentId == R.id.fragment_edit_profile_tv_forgot_password) {
            validateUpdatePasswordSection();
        } else if (fragmentId == R.id.fragment_edit_profile_tv_change_language) {
            openDialogBox();
        } else if (fragmentId == R.id.fragment_edit_profile_im_pf) {
            selectImage();
        }
    }

    private void openDialogBox() {
        final Dialog dialog = new Dialog(getActivity(), R.style.AppDialogThemeNonTras);
        dialog.setContentView(R.layout.custom_dialog_select_language);
        final CheckBox cbDa = (CheckBox) dialog.findViewById(R.id.custom_dialog_select_lang_da);
        final CheckBox cbNb = (CheckBox) dialog.findViewById(R.id.custom_dialog_select_lang_nb);
        final CheckBox cbSv = (CheckBox) dialog.findViewById(R.id.custom_dialog_select_lang_sv);
        final CheckBox cbEng = (CheckBox) dialog.findViewById(R.id.custom_dialog_select_lang_en);
        final String str = Locale.getDefault().getDisplayLanguage();
        Log.d("Lag", str);
        if (str.equalsIgnoreCase("english")) {
            cbDa.setChecked(false);
            cbNb.setChecked(false);
            cbSv.setChecked(false);
            cbEng.setChecked(true);
            Preference.getInstance().savePreferenceData(Constant.IS_ENG, true);
            Preference.getInstance().savePreferenceData(Constant.IS_DA, false);
            Preference.getInstance().savePreferenceData(Constant.IS_SV, false);
            Preference.getInstance().savePreferenceData(Constant.IS_NB, false);


        } else if (str.equalsIgnoreCase("dansk")) {
            cbDa.setChecked(true);
            cbNb.setChecked(false);
            cbSv.setChecked(false);
            cbEng.setChecked(false);


            Preference.getInstance().savePreferenceData(Constant.IS_DA, true);
            Preference.getInstance().savePreferenceData(Constant.IS_ENG, false);
            Preference.getInstance().savePreferenceData(Constant.IS_SV, false);
            Preference.getInstance().savePreferenceData(Constant.IS_NB, false);


        } else if (str.equalsIgnoreCase("svenska")) {
            cbDa.setChecked(false);
            cbNb.setChecked(false);
            cbSv.setChecked(true);
            cbEng.setChecked(false);
            Preference.getInstance().savePreferenceData(Constant.IS_SV, true);
            Preference.getInstance().savePreferenceData(Constant.IS_DA, false);
            Preference.getInstance().savePreferenceData(Constant.IS_ENG, false);
            Preference.getInstance().savePreferenceData(Constant.IS_NB, false);


        } else if (str.equalsIgnoreCase("norsk bokmål")) {
            cbDa.setChecked(false);
            cbNb.setChecked(true);
            cbSv.setChecked(false);
            cbEng.setChecked(false);

            Preference.getInstance().savePreferenceData(Constant.IS_NB, true);
            Preference.getInstance().savePreferenceData(Constant.IS_SV, false);
            Preference.getInstance().savePreferenceData(Constant.IS_DA, false);
            Preference.getInstance().savePreferenceData(Constant.IS_ENG, false);

        } else {
            cbDa.setChecked(false);
            cbNb.setChecked(false);
            cbSv.setChecked(false);
            cbEng.setChecked(true);

            Preference.getInstance().savePreferenceData(Constant.IS_NB, false);
            Preference.getInstance().savePreferenceData(Constant.IS_SV, false);
            Preference.getInstance().savePreferenceData(Constant.IS_DA, false);
            Preference.getInstance().savePreferenceData(Constant.IS_ENG, true);

        }


//        if (Preference.getInstance().mSharedPreferences.getBoolean(Constant.IS_DA, false)) {
//            cbDa.setChecked(true);
//            cbNb.setChecked(false);
//            cbSv.setChecked(false);
//            cbEng.setChecked(false);
//
//        } else if (Preference.getInstance().mSharedPreferences.getBoolean(Constant.IS_NB, false)) {
//            cbDa.setChecked(false);
//            cbNb.setChecked(true);
//            cbSv.setChecked(false);
//            cbEng.setChecked(false);
//        } else if (Preference.getInstance().mSharedPreferences.getBoolean(Constant.IS_SV, false)) {
//            cbDa.setChecked(false);
//            cbNb.setChecked(false);
//            cbSv.setChecked(true);
//            cbEng.setChecked(false);
//        } else if (Preference.getInstance().mSharedPreferences.getBoolean(Constant.IS_ENG, false)) {
//            cbDa.setChecked(false);
//            cbNb.setChecked(false);
//            cbSv.setChecked(false);
//            cbEng.setChecked(true);
//
//
//        }
//        else
//        {
//            cbDa.setChecked(true);
//            cbNb.setChecked(false);
//            cbSv.setChecked(false);
//            cbEng.setChecked(false);
//        }


        cbDa.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {

                if (b) {
                    Preference.getInstance().savePreferenceData(Constant.IS_LANG_ID, "da");
                    Preference.getInstance().savePreferenceData(Constant.IS_DA, b);
                    Preference.getInstance().savePreferenceData(Constant.IS_NB, false);
                    Preference.getInstance().savePreferenceData(Constant.IS_SV, false);
                    Preference.getInstance().savePreferenceData(Constant.IS_ENG, false);
                } else {
                    Preference.getInstance().savePreferenceData(Constant.IS_NB, false);
                    Preference.getInstance().savePreferenceData(Constant.IS_SV, false);
                    Preference.getInstance().savePreferenceData(Constant.IS_ENG, false);
                }


                dadApplication.configLanguage(getActivity(), getString(R.string.pref_key_language_da));
                restartActivity();

            }
        });

        cbNb.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
//                Preference.getInstance().savePreferenceData(Constant.IS_NB, b);

                if (b) {
                    Preference.getInstance().savePreferenceData(Constant.IS_LANG_ID, "nb");
                    Preference.getInstance().savePreferenceData(Constant.IS_NB, b);
                    Preference.getInstance().savePreferenceData(Constant.IS_DA, false);
                    Preference.getInstance().savePreferenceData(Constant.IS_SV, false);
                    Preference.getInstance().savePreferenceData(Constant.IS_ENG, false);

                } else {

                    Preference.getInstance().savePreferenceData(Constant.IS_DA, false);
                    Preference.getInstance().savePreferenceData(Constant.IS_SV, false);
                    Preference.getInstance().savePreferenceData(Constant.IS_ENG, false);

                }

                dadApplication.configLanguage(getActivity(), getString(R.string.pref_key_language_nb));
                restartActivity();


            }
        });

        cbSv.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
//                Preference.getInstance().savePreferenceData(Constant.IS_SV, b);


                if (b) {
                    Preference.getInstance().savePreferenceData(Constant.IS_LANG_ID, "sv");
                    Preference.getInstance().savePreferenceData(Constant.IS_SV, b);
                    Preference.getInstance().savePreferenceData(Constant.IS_DA, false);
                    Preference.getInstance().savePreferenceData(Constant.IS_NB, false);
                    Preference.getInstance().savePreferenceData(Constant.IS_ENG, false);

                } else {

                    Preference.getInstance().savePreferenceData(Constant.IS_DA, false);
                    Preference.getInstance().savePreferenceData(Constant.IS_NB, false);
                    Preference.getInstance().savePreferenceData(Constant.IS_ENG, false);

                }


                dadApplication.configLanguage(getActivity(), getString(R.string.pref_key_language_sv));
                restartActivity();


            }
        });

        cbEng.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {


                if (b) {
                    Preference.getInstance().savePreferenceData(Constant.IS_LANG_ID, "en");
                    Preference.getInstance().savePreferenceData(Constant.IS_ENG, b);
                    Preference.getInstance().savePreferenceData(Constant.IS_DA, false);
                    Preference.getInstance().savePreferenceData(Constant.IS_NB, false);
                    Preference.getInstance().savePreferenceData(Constant.IS_SV, false);

                } else {

                    Preference.getInstance().savePreferenceData(Constant.IS_DA, false);
                    Preference.getInstance().savePreferenceData(Constant.IS_NB, false);
                    Preference.getInstance().savePreferenceData(Constant.IS_SV, false);

                }


                dadApplication.configLanguage(getActivity(), getString(R.string.pref_key_language_eng));
                restartActivity();


            }
        });

        dialog.show();


    }

    private void restartActivity() {
        Intent intent = getActivity().getIntent();
        getActivity().finish();
        intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
        startActivity(intent);
        getActivity().overridePendingTransition(0, 0);
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (resultCode != RESULT_OK) {
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
//                    Utils.displayMessageDialog(this, e.getMessage());
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
                            .into(new BitmapImageViewTarget(ivProfile) {
                                @Override
                                protected void setResource(Bitmap resource) {
                                    if (resource != null) {
                                        RoundedBitmapDrawable circularBitmapDrawable =
                                                RoundedBitmapDrawableFactory.create(getResources(), resource);
                                        circularBitmapDrawable.setCircular(true);
                                        ivProfile.setImageDrawable(circularBitmapDrawable);
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


    @Override
    public void trackScreen() {

    }

    @Override
    public void initActionBar() {
    }

    @Override
    public void onDestroy() {
        super.onDestroy();


//        // cancel async task if any pending.
//        if (asyncTaskEditProfile != null && asyncTaskForgotPassword.getStatus() == AsyncTask.Status.RUNNING && asyncTaskGetUserInfo.getStatus() == AsyncTask.Status.RUNNING && asyncTaskUpdatePassword.getStatus() == AsyncTask.Status.RUNNING) {
//            asyncTaskEditProfile.cancel(true);
//            asyncTaskForgotPassword.cancel(true);
//            asyncTaskGetUserInfo.cancel(true);
//            asyncTaskUpdatePassword.cancel(true);
//
//            Log.d("Cancel", "Here all running asynctas will be cleared");
//        }


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
                    userChoosenTask = getString(R.string.TAG_TAKE_PHOTO);
                    gotoCamera();


                } else if (items[item].equals(getString(R.string.TAG_CHOOSE_FROM_GALLERY))) {
                    userChoosenTask = getString(R.string.TAG_CHOOSE_FROM_GALLERY);
                    gotoGallery();


                } else if (items[item].equals(getString(R.string.fragment_create_account_tv_cancel))) {
                    dialog.dismiss();
                }

            }
        });
        builder.show();
    }

    public void gotoCamera() {
        final Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        try {
            imageFile = CameraUtil.getOutputMediaFile(1);
            final Uri mImageCaptureUri = Uri.fromFile(imageFile);
            intent.putExtra(MediaStore.EXTRA_OUTPUT, mImageCaptureUri);
            intent.putExtra("return-data", true);
            startActivityForResult(intent, Constants.REQUEST_CODE_TAKE_PICTURE);
        } catch (ActivityNotFoundException e) {
            Log.d("TAG", "cannot take picture", e);
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
                    if (Utills.isOnline(getActivity(), true)) {
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
            if (Utills.isOnline(getActivity(), true)) {
                editProfile();
                //  Utils.displayDialog(this, getString(R.string.app_name), "We've sent a password reset link to email address", getString(android.R.string.ok), "", false, true);
            } else {
                Utills.displayDialog(getActivity(), getString(R.string.app_name), getString(R.string.TAG_INTERNET_AVAILABILITY), getString(R.string.TAG_OK), "", false, false);
            }
        }

    }

    private void updatePassword() {
        if (Utills.isInternetConnected(getActivity())) {
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
        if (Utills.isInternetAvailable(getActivity())) {
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

        if (Utills.isInternetAvailable(getActivity())) {
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

    private void getUserInfo() {
        if (Utills.isInternetAvailable(getActivity())) {
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
                    if (isImageUpdated) {
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
            wsUploadImage.executeService(path);
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
        dialog.setPositiveButton(strPositiveText, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                dialog.dismiss();
                Intent intent = new Intent();
                intent.putExtra(net.tigerlight.dad.util.Constants.Extras.FORCE_LOGOUT, true);
                                getTargetFragment().onActivityResult(net.tigerlight.dad.util.Constants.REQUEST_CODES.FORCE_LOGOUT, RESULT_OK, intent);
                requireActivity().getSupportFragmentManager().popBackStack();
            }
        });
        dialog.show();
    }

}
