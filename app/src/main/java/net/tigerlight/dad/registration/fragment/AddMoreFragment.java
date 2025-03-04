package net.tigerlight.dad.registration.fragment;

import static net.tigerlight.dad.util.WsConstants.ASSETS_DOMAIN;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.provider.MediaStore;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.core.graphics.drawable.RoundedBitmapDrawable;
import androidx.core.graphics.drawable.RoundedBitmapDrawableFactory;
import androidx.fragment.app.DialogFragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.target.BitmapImageViewTarget;
import com.bumptech.glide.request.transition.Transition;
import com.google.firebase.installations.Utils;

import net.tigerlight.dad.R;
import net.tigerlight.dad.cropimage.CropImage;
import net.tigerlight.dad.registration.util.Constant;
import net.tigerlight.dad.registration.util.Utills;
import net.tigerlight.dad.util.CircleTransform;
import net.tigerlight.dad.util.Util;
import net.tigerlight.dad.webservices.WsCallAddreceiver;
import net.tigerlight.dad.webservices.WsCallUpdateContact;
import net.tigerlight.dad.simplecropping.CameraUtil;
import net.tigerlight.dad.simplecropping.Constants;
import net.tigerlight.dad.util.BitMapHelper;
import net.tigerlight.dad.webservices.WsUploadContactImage;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;

public class AddMoreFragment extends DialogFragment implements View.OnClickListener {

    private static final String TAG = AddMoreFragment.class.getSimpleName();
    private static final int REQUEST_READ_CONTACTS_PERMISSION = 100;
    private static final int CAMERA_PERMISSION_REQUEST_CODE = 101;
    private static final int GALLERY_REQUEST_CODE = 102;

    private OnContactUpdatedListener contactUpdatedListener;

    private EditText etUserName;
    private EditText etPhoneNo;
    private EditText etFirstName;
    private EditText etLastName;
    private EditText etEmail;
    private ImageView ivProfilePic;

    private AsyncTaskSaveAddress asyncTaskSaveAddress;
    private ProgressDialog progressDialog;
    private boolean isEditOrSave = false;
    private String userId = "";
    private String firstName = "";
    private String lastName = "";
    private String phone = "";
    private String nickname = "";
    private String email = "";
    private File imageFile;
    private Intent yourIntentData;
    private ActivityResultLauncher<Intent> photoPickerLauncher;
    private String isImportedPhoto;
    private String isPhotoEdited;
    private boolean isUploadingPhoto = false;

    public void setOnContactUpdatedListener(OnContactUpdatedListener listener) {
        this.contactUpdatedListener = listener;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
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
    }


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Inflate your layout
        View view = inflater.inflate(R.layout.fragment_add_more, container, false);
        // Call initView method
        initView(view);
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        // Make the dialog full-screen
        if (getDialog() != null && getDialog().getWindow() != null) {
            getDialog().getWindow().setLayout(
                    WindowManager.LayoutParams.MATCH_PARENT,
                    WindowManager.LayoutParams.MATCH_PARENT
            );
        }
    }

    // Handle the permission request response
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_READ_CONTACTS_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted, proceed with your operation
                toSetContactSelectedAjay(yourIntentData);
            } else {
                // Permission denied, handle accordingly
                Toast.makeText(getActivity(), "Permission denied to read contacts", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void initView(View view) {
        isImportedPhoto = null;
        isPhotoEdited = null;
        isUploadingPhoto = false;
        etUserName = view.findViewById(R.id.fragment_add_more_et_user_name);
        etPhoneNo = view.findViewById(R.id.fragment_add_more_et_phone_no);
        etFirstName = view.findViewById(R.id.fragment_add_more_et_first_name);
        etLastName = view.findViewById(R.id.fragment_add_more_et_last_name);
        etEmail = view.findViewById(R.id.fragment_add_more_et_email);
        ivProfilePic = view.findViewById(R.id.fragment_add_more_iv_user_profile);
        final TextView dialogTitle = view.findViewById(R.id.fragment_add_more_dialog_title);
        final TextView tvCancel = view.findViewById(R.id.fragment_add_more_tv_cancel);
        final TextView tvAddressBook = view.findViewById(R.id.fragment_add_more_tv_addressbook);
        final Button tvSave = view.findViewById(R.id.fragment_add_more_tv_save);

        final Bundle bundle = getArguments();
        if (bundle != null) {
            String jsonObject = bundle.getString(Constant.JSON_OBJECT);
            try {
                isEditOrSave = true;
                JSONObject jsonobjectToChange = null;
                if (getActivity() != null && jsonObject != null) {
                    jsonobjectToChange = new JSONObject(jsonObject);
                    String TAG_USER_ID = "userid";
                    userId = jsonobjectToChange.optString(TAG_USER_ID);
                    String TAG_NICKNAME = "nickname";
                    nickname = jsonobjectToChange.optString(TAG_NICKNAME);
                    String TAG_FIRST_NAME = "firstname";
                    firstName = jsonobjectToChange.optString(TAG_FIRST_NAME);
                    String TAG_LAST_NAME = "lastname";
                    lastName = jsonobjectToChange.optString(TAG_LAST_NAME);
                    String TAG_EMAIL = "email";
                    String emailPrevious = jsonobjectToChange.optString(TAG_EMAIL);
                    String TAG_PHONE = "phone";
                    phone = jsonobjectToChange.optString(TAG_PHONE);

                    etUserName.setText(String.format("%s", nickname));
                    etFirstName.setText(String.format("%s", firstName));
                    etLastName.setText(String.format("%s", lastName));
                    etEmail.setText(String.format("%s", emailPrevious));
                    etPhoneNo.setText(String.format("%s", phone));
                }
            } catch (JSONException e) {
                Log.e(TAG, "Error initialising view");
            }
        }
        dialogTitle.setText(getString(isEditOrSave ? R.string.fragment_update_contact_tv_title : R.string.fragment_add_contact_tv_title));
        ivProfilePic.setOnClickListener(this);
        tvCancel.setOnClickListener(this);
        tvAddressBook.setOnClickListener(this);
        tvSave.setOnClickListener(this);
        setProfilePicture();
    }

    private void setProfilePicture() {
        if (userId != null && !userId.isEmpty()) {
            String url = String.format("%scontact_image_%s.png", ASSETS_DOMAIN, userId);
            Glide.with(this)
                    .load(url)
                    .diskCacheStrategy(DiskCacheStrategy.NONE)
                    .skipMemoryCache(true).transform(new CircleTransform(getActivity()))
                    .placeholder(R.drawable.pf_pic)
                    .into(ivProfilePic);
        } else {
            Glide.with(this)
                    .load(R.drawable.pf_pic)
                    .into(ivProfilePic);
        }
    }

    @Override
    public void onClick(View v) {
        if (getActivity() != null) {
            Util.getInstance().hideSoftKeyboard(getActivity());
        }
        final int fragmentId = v.getId();
        if (fragmentId == R.id.fragment_add_more_tv_save) {
            validateFragment();
        } else if (fragmentId == R.id.fragment_add_more_tv_cancel) {
            dismiss();
        } else if (fragmentId == R.id.fragment_add_more_iv_user_profile) {
            selectImage();
        } else if (fragmentId == R.id.fragment_add_more_tv_addressbook) {
            showContacts();
        }
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
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        Log.d(TAG, "onActivityResult: " + data);
        if (resultCode != Activity.RESULT_OK) {
            Toast.makeText(getActivity(), "Error: No file selected or action canceled.", Toast.LENGTH_SHORT).show();
            return;
        }

        switch (requestCode) {
            case GALLERY_REQUEST_CODE:
                try {
                    final Uri selectedImageUri = data.getData();
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
                final String croppedImagePath = data.getStringExtra(CropImage.IMAGE_PATH);
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
                    .into(new BitmapImageViewTarget(ivProfilePic) {
                        @Override
                        protected void setResource(Bitmap resource) {
                            if (resource != null) {
                                RoundedBitmapDrawable circularBitmapDrawable =
                                        RoundedBitmapDrawableFactory.create(getResources(), resource);
                                circularBitmapDrawable.setCircular(true);
                                ivProfilePic.setImageDrawable(circularBitmapDrawable);
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

        // cancel async task if any pending.
        if (asyncTaskSaveAddress != null && asyncTaskSaveAddress.getStatus() == AsyncTask.Status.RUNNING) {
            asyncTaskSaveAddress.cancel(true);
        }
    }

    //User Defined Methods

    private void validateFragment() {
        nickname = etUserName.getText().toString().trim();
        phone = etPhoneNo.getText().toString().trim();
        firstName = etFirstName.getText().toString().trim();
        lastName = etLastName.getText().toString().trim();
        email = etEmail.getText().toString().trim();

        if (getActivity() != null) {
            if (phone.equalsIgnoreCase("")) {
                Utills.displayDialog(getActivity(), getString(R.string.app_name), getString(R.string.TAG_PHONE_NO_EMPTYMSG), getString(R.string.TAG_OK), "", false, false);
                etPhoneNo.requestFocus();
            } else if (!phone.equalsIgnoreCase("")) {
                if (Utills.isValidPhone(phone)) {
                    if (!email.isBlank()) {
                        if (Util.getInstance().isEmailValid(email)) {
                            if (Utills.isOnline(getActivity(), true)) {
                                if (isEditOrSave) {
                                    new UpdateTask().execute();
                                } else {
                                    saveAddressBook();
                                }
                            } else {
                                Utills.displayDialog(getActivity(), getString(R.string.app_name), getString(R.string.TAG_INTERNET_AVAILABILITY), getString(R.string.TAG_OK), "", false, false);
                            }
                        } else  {
                            Toast.makeText(requireContext(), R.string.enter_valid_email, Toast.LENGTH_SHORT).show();
                        }
                    } else  {
                        if (Utills.isOnline(getActivity(), true)) {
                            if (isEditOrSave) {
                                new UpdateTask().execute();
                            } else {
                                saveAddressBook();
                            }
                        } else {
                            Utills.displayDialog(getActivity(), getString(R.string.app_name), getString(R.string.TAG_INTERNET_AVAILABILITY), getString(R.string.TAG_OK), "", false, false);
                        }
                    }
                } else {
                    Toast.makeText(requireContext(), R.string.enter_valid_phone_number, Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    //Content Provider Method
    private void showContacts() {
        Intent intent = new Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI);
        startActivityForResult(intent, Constants.REQUEST_CONTACT_NUMBER);
    }

    @SuppressLint({"Range", "UseCompatLoadingForDrawables"})
    @SuppressWarnings("deprecation")
    private void toSetContactSelectedAjay(Intent data) {

        Uri uriContact = data.getData();
        String contactName = null;
        if (getActivity() != null && uriContact != null) {
            Cursor cursor = getActivity().getContentResolver().query(uriContact, null, null, null, null);
            if (cursor != null) {
                if (cursor.moveToFirst()) {
                    contactName = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));
                }
                if (contactName != null && contactName.contains(" ")) {
                    etFirstName.setText(String.format(" %s", contactName.substring(0, contactName.indexOf(' '))));
                    etLastName.setText(String.format(" %s", contactName.substring(contactName.indexOf(' ') + 1)));
                } else {
                    etFirstName.setText(contactName);
                    etLastName.setText("");
                }
                cursor.close();
            }
            String contactNumber = null;
            Cursor cursorID = getActivity().getContentResolver().query(uriContact, new String[]{ContactsContract.Contacts._ID}, null, null, null);
            String contactID = null;
            if (cursorID != null) {
                if (cursorID.moveToFirst()) {
                    contactID = cursorID.getString(cursorID.getColumnIndex(ContactsContract.Contacts._ID));
                }
                cursorID.close();
            }

            Cursor cursorPhone = getActivity().getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, new String[]{ContactsContract.CommonDataKinds.Phone.NUMBER},
                    ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ? AND " + ContactsContract.CommonDataKinds.Phone.TYPE + " = " + ContactsContract.CommonDataKinds.Phone.TYPE_MOBILE, new String[]{contactID}, null);
            if (cursorPhone != null) {
                if (cursorPhone.moveToFirst()) {
                    contactNumber = cursorPhone.getString(cursorPhone.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)).trim();
//            if (contactNumber.length() > 10) {
//
//                contactNumber = contactNumber.replace(" ","");
//                contactNumber = contactNumber.substring(contactNumber.length() - 10, contactNumber.length());
//            }
                    etPhoneNo.setText(contactNumber);
                }
                cursorPhone.close();
            }

            InputStream openPhoto = null;
            if (contactID != null) {
                openPhoto = openPhoto(Long.parseLong(contactID));
                Bitmap bitmap = BitmapFactory.decodeStream(openPhoto);
                if (bitmap == null) {
                    ivProfilePic.setBackgroundDrawable(getResources().getDrawable(R.drawable.pf_pic));
                } else {
                    Bitmap circleBitmap = BitMapHelper.getCircleBitmap(bitmap);
                    ivProfilePic.setImageBitmap(circleBitmap);
                    isImportedPhoto = BitMapHelper.saveImageAndGetPath(bitmap, getContext(), userId);
                }
            }


            // Bitmap thumbnailID = new QuickContactHelper(this,
            // contactNumber).addThumbnail(this);
            // if (thumbnailID == null) {
            // tosetPicOnImageView.setBackgroundDrawable(getResources().getDrawable(R.drawable.pf_pic));
            // } else {
            // setPicListStatus(thumbnailID);
            // tosetPicOnImageView.setImageBitmap(thumbnailID);
            // }

            String contactEmail = null;
            Cursor cursorEmail = getActivity().getContentResolver().query(ContactsContract.CommonDataKinds.Email.CONTENT_URI, new String[]{ContactsContract.CommonDataKinds.Email.DATA},
                    ContactsContract.CommonDataKinds.Email.CONTACT_ID + " = ? AND " + ContactsContract.CommonDataKinds.Email.TYPE + " = " + ContactsContract.CommonDataKinds.Email.TYPE, new String[]{contactID}, null);
            if (cursorEmail != null && cursorEmail.moveToFirst()) {
                contactEmail = cursorEmail.getString(cursorEmail.getColumnIndex(ContactsContract.CommonDataKinds.Email.DATA));
                cursorEmail.close();
            }
            etEmail.setText(contactEmail);

            String nickName2 = getNickName(contactID);
            etUserName.setText(nickName2);

        }
        // addressEdit.setText(getAddress(contactID));
    }

    public InputStream openPhoto(long contactId) {
        Uri contactUri = ContentUris.withAppendedId(ContactsContract.Contacts.CONTENT_URI, contactId);
        Uri photoUri = Uri.withAppendedPath(contactUri, ContactsContract.Contacts.Photo.CONTENT_DIRECTORY);
        imageFile = CameraUtil.getOutputMediaFile(1);
        //This is big patch to get image from the uri to particular folder where app's images are saved.
        final int chunkSize = 1024;  // We'll read in one kB at a time
        byte[] imageData = new byte[chunkSize];

        try {
            if (getActivity() != null) {
                InputStream in = getActivity().getContentResolver().openInputStream(photoUri);
                OutputStream out = new FileOutputStream(imageFile);  // I'm assuming you already have the File object for where you're writing to
                int bytesRead;
                if (in != null) {
                    while ((bytesRead = in.read(imageData)) > 0) {
                        out.write(Arrays.copyOfRange(imageData, 0, bytesRead));
                    }
                    in.close();
                }
                out.close();
            }
        } catch (Exception ex) {
            Log.e(TAG, "openPhoto error");
        }
// finally {
//
//        }


//        File file = new File(photoUri.getPath());
//        imageFile = CameraUtil.getOutputMediaFile(1);


        if (getActivity() != null) {
            Cursor cursor = getActivity().getContentResolver().query(photoUri, new String[]{ContactsContract.Contacts.Photo.PHOTO}, null, null, null);
            try (cursor) {
                if (cursor == null) {
                    return null;
                }
                if (cursor.moveToFirst()) {
                    byte[] data = cursor.getBlob(0);
                    if (data != null) {
                        return new ByteArrayInputStream(data);
                    }
                }
            }
        }
        return null;
    }

    private String getNickName(String id) {
        Uri URI_NICK_NAME = ContactsContract.Data.CONTENT_URI;
        String SELECTION_NICK_NAME = ContactsContract.Data.CONTACT_ID + " = ? AND " + ContactsContract.Data.MIMETYPE + " = ?";
        String[] SELECTION_ARRAY_NICK_NAME = new String[]{id, ContactsContract.CommonDataKinds.Nickname.CONTENT_ITEM_TYPE};
        String nickNameStr = "";
        Cursor cursor = null;
        if (getActivity() != null) {
            cursor = getActivity().getContentResolver().query(URI_NICK_NAME, null, SELECTION_NICK_NAME, SELECTION_ARRAY_NICK_NAME, null);
            int indexNickName = 0;
            if (cursor != null) {
                indexNickName = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Nickname.NAME);
                if (cursor.moveToNext()) {
                    nickNameStr = cursor.getString(indexNickName);
                }
                cursor.close();
            }
        }

        return nickNameStr;
    }

    //Media Methods

    private void selectImage() {
        final CharSequence[] items = {getString(R.string.TAG_TAKE_PHOTO), getString(R.string.TAG_CHOOSE_FROM_GALLERY),
                getString(R.string.fragment_create_account_tv_cancel)};


        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(getString(R.string.TAG_ADD_Photo));
        builder.setItems(items, (dialog, item) -> {

//                if (Utills.checkForPermission(getActivity(), Constant.STORAGE_PERMISSION)) {
////do whatever you want to do
//                    Log.d("Permission", "Already Given");
//
//
//                    result = true;
//
//
//                } else {
//                    requestForPermissions(Constant.STORAGE_PERMISSION, Constant.PERMISSION_REQUEST_STORAGE_PERMISSION_CODE);
//                }

//                boolean result = Utills.checkForPermission(getActivity(),Constant.STORAGE_PERMISSION), ;


            if (items[item].equals(getString(R.string.TAG_TAKE_PHOTO))) {
                gotoCamera();
//                    selectFromcamera();
//                    try {
//                        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
//                        String imageFilePath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/picture.jpg";
//                        File imageFile = new File(imageFilePath);
//                        picUri = Uri.fromFile(imageFile); // convert path to Uri
//                        takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, picUri);
//                        startActivityForResult(takePictureIntent, SELECT_PICTURE_FROM_CAMERA);
//
//                    } catch (ActivityNotFoundException anfe) {
//                        //display an error message
//                        String errorMessage = "Whoops - your device doesn't support capturing images!";
//                        Toast.makeText(getActivity(), errorMessage, Toast.LENGTH_SHORT).show();
//                    }


            } else if (items[item].equals(getString(R.string.TAG_CHOOSE_FROM_GALLERY))) {
                gotoGallery();
//                    selectfromGallery();
//                    try {
//
//                        Intent i = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
//                        startActivityForResult(i, SELECT_PICTURE_FROM_GALLERY);
//                    } catch (ActivityNotFoundException ax) {
//                        //display an error message
//                        String errorMessage = "Whoops - your device doesn't support capturing images!";
//                        Toast.makeText(getActivity(), errorMessage, Toast.LENGTH_SHORT).show();
//                    }


            } else if (items[item].equals(getString(R.string.fragment_create_account_tv_cancel))) {
                dialog.dismiss();
            }

        });
        builder.show();
    }

    private void startCameraActivity() {
        final Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        try {
            imageFile = CameraUtil.getOutputMediaFile(1); // Your method to create the file
            final Activity activity = getActivity();
            if (activity != null && imageFile != null) {
                Uri photoURI = FileProvider.getUriForFile(
                        activity,
                        activity.getPackageName() + ".provider",
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

    public void gotoCamera() {
        final Activity activity = getActivity();
        if (activity != null) {
            if (ContextCompat.checkSelfPermission(activity, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(
                        activity,
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

    private class AsyncTaskUploadContactPic extends AsyncTask<Void, Void, Void> {
        private WsUploadContactImage wsUploadImage;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog = ProgressDialog.show(getActivity(), "", getString(R.string.TAG_Loading));
            progressDialog.setCancelable(false);
            wsUploadImage = new WsUploadContactImage(getActivity());
        }

        @Override
        protected Void doInBackground(Void... voids) {
            String path = null;
            if (isPhotoEdited != null) {
                path = isPhotoEdited;
            } else if (isImportedPhoto != null) {
                path = isImportedPhoto;
            }
            wsUploadImage.executeService(path, userId);
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            if (progressDialog != null && progressDialog.isShowing()) {
                progressDialog.dismiss();
            }

            if (!isCancelled()) {
                if (wsUploadImage.isSuccess() && isUploadingPhoto) {
                    ContactFragment.isServiceCall = true;
                    if (contactUpdatedListener != null) {
                        contactUpdatedListener.onContactUpdated();
                    }
                    dismiss();
                }
            }
            isUploadingPhoto = false;
        }
    }

    private class UpdateTask extends AsyncTask<String, Void, String> {

        private static final String EDITED_SUCCESSFULLY = "Contact has been edited successfully.";
        private static final String KEY_SUCCESS = "success";
        private final Context context = getContext();
        private int response;
        private WsCallUpdateContact wsCallUpdateContact;

        // private String etAddressstr = tvAddressBook.getText().toString().trim();

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog = ProgressDialog.show(getActivity(), "", getString(R.string.TAG_Loading));
            progressDialog.setCancelable(false);
            wsCallUpdateContact = new WsCallUpdateContact(getActivity());
        }

        @Override
        protected String doInBackground(String... params) {

            if (getActivity() != null && Utills.isInternetConnected(getActivity())) {
                wsCallUpdateContact.executeService(userId, firstName, lastName, nickname, email, phone, "");
                if (wsCallUpdateContact.isSuccess()) {
                    response = 0;
                    return KEY_SUCCESS;
                } else {
                    response = 2;
                    return wsCallUpdateContact.getMessage();
                }
            } else {
                response = 1;
            }
            return "fail";
        }

        @Override
        protected void onPostExecute(String result) {
            progressDialog.cancel();
            switch (response) {
                case 2:
                    Toast.makeText(getActivity(), result, Toast.LENGTH_SHORT).show();
                    break;

                case 1:
                    Toast.makeText(getActivity(), getString(R.string.TAG_PWD_EMAIL_EROR), Toast.LENGTH_SHORT).show();
                    break;

                default:
                    if (result.equals("fail")) {
                        Toast.makeText(getActivity(), getString(R.string.TAG_PWD_FETCH_EROR), Toast.LENGTH_SHORT).show();
                    } else {
                        Log.e(TAG, "success");
                        if (isPhotoEdited != null || isImportedPhoto != null) {
                            isUploadingPhoto = true;
                            new AsyncTaskUploadContactPic().execute();
                        } else {
                            if (contactUpdatedListener != null) {
                                contactUpdatedListener.onContactUpdated();
                            }
                            dismiss();
                        }
                        //Toast.makeText(getActivity(), "Contact has been edited successfully.", Toast.LENGTH_SHORT).show();
                        //setUpdated();
                        //finish();
                    }
                    break;
            }
        }

        private void setUpdated() {
            //IS_UPDATED = true;
        }
    }

    private void copyFile(File sourceFile, File destFile) throws IOException {
        try (InputStream in = new FileInputStream(sourceFile);
             OutputStream out = new FileOutputStream(destFile)) {
            byte[] buffer = new byte[1024];
            int length;
            while ((length = in.read(buffer)) > 0) {
                out.write(buffer, 0, length);
            }
        }
    }

    private void saveAddressBook() {
        if (getActivity() != null && Utills.isInternetAvailable(getActivity())) {
            if (asyncTaskSaveAddress != null && asyncTaskSaveAddress.getStatus() == AsyncTask.Status.PENDING) {
                asyncTaskSaveAddress.execute();
            } else if (asyncTaskSaveAddress == null || asyncTaskSaveAddress.getStatus() == AsyncTask.Status.FINISHED) {
                asyncTaskSaveAddress = new AsyncTaskSaveAddress();
                asyncTaskSaveAddress.execute();
            }
        } else {
            Utills.displayDialogNormalMessage(getString(R.string.app_name), getString(R.string.TAG_INTERNET_AVAILABILITY), getActivity());
        }
    }

    @SuppressLint("StaticFieldLeak")
    private class AsyncTaskSaveAddress extends AsyncTask<Void, Void, Void> {

        private WsCallAddreceiver wsCallAddreceiver;
        private final String etUserNameStr = etUserName.getText().toString().trim();
        private final String etPhoneNoStr = etPhoneNo.getText().toString().trim();
        private final String etFirNamestr = etFirstName.getText().toString().trim();
        private final String etLastnameStr = etLastName.getText().toString().trim();
        private final String etEmailstr = etEmail.getText().toString().trim();
        private final Context context = getContext();
        // private String etAddressstr = tvAddressBook.getText().toString().trim();

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog = ProgressDialog.show(getActivity(), "", getString(R.string.TAG_Loading));
            progressDialog.setCancelable(false);
        }

        @Override
        protected Void doInBackground(Void... voids) {
            wsCallAddreceiver = new WsCallAddreceiver(getActivity());
            wsCallAddreceiver.executeService(etFirNamestr, etLastnameStr, etUserNameStr, etEmailstr, etPhoneNoStr, "");
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            if (progressDialog != null && progressDialog.isShowing()) {
                progressDialog.dismiss();
            }

            if (!isCancelled()) {
                if (wsCallAddreceiver.isSuccess()) {
//                    Utills.displayDialog(getActivity(), getString(R.string.app_name), getString(R.string.TAG_CONT_ADDED_SUCCESS), getString(android.R.string.ok), "", false, false);
                    userId = wsCallAddreceiver.getUserId();
                    if (isPhotoEdited != null || isImportedPhoto != null) {
                        isUploadingPhoto = true;
                        new AsyncTaskUploadContactPic().execute();
                    } else {
                        ContactFragment.isServiceCall = true;
                        if (contactUpdatedListener != null) {
                            contactUpdatedListener.onContactUpdated();
                        }
                        dismiss();
                    }
                } else {
                    Utills.displayDialog(getActivity(), getString(R.string.app_name), getString(R.string.TAG_CONT_UNABLE_ADDED), getString(android.R.string.ok), "", false, false);
                }
            }

        }

    }


}



