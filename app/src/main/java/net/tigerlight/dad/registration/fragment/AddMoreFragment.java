package net.tigerlight.dad.registration.fragment;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.ContentUris;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.provider.ContactsContract;
import android.provider.MediaStore;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.core.graphics.drawable.RoundedBitmapDrawable;
import androidx.core.graphics.drawable.RoundedBitmapDrawableFactory;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.target.BitmapImageViewTarget;
import com.bumptech.glide.request.transition.Transition;

import net.tigerlight.dad.R;
import net.tigerlight.dad.cropimage.CropImage;
import net.tigerlight.dad.home.BaseFragment;
import net.tigerlight.dad.registration.util.Constant;
import net.tigerlight.dad.registration.util.Utills;
import net.tigerlight.dad.webservices.WsCallAddreceiver;
import net.tigerlight.dad.webservices.WsCallUpdateContact;
import net.tigerlight.dad.simplecropping.CameraUtil;
import net.tigerlight.dad.simplecropping.Constants;
import net.tigerlight.dad.util.BitMapHelper;
import net.tigerlight.dad.util.Preference;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;

public class AddMoreFragment extends BaseFragment {

    private static final String TAG = "AddMoreFragment";
    private final String TAG_USER_ID = "userid";
    private final String TAG_FIRST_NAME = "firstname";
    private final String TAG_LAST_NAME = "lastname";
    private final String TAG_EMAIL = "email";
    private final String TAG_PHONE = "phone";
    private final String TAG_NICKNAME = "nickname";
    private static final int REQUEST_READ_CONTACTS_PERMISSION = 100;
    private static final int CAMERA_PERMISSION_REQUEST_CODE = 101;
    private String userChoosenTask;


    //TO check whether image taken or not
    private boolean isImageUpdated;
    //To store the cropped path
    private String path;

    private View view;
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
    private String emailPreviouus = "";
    private String firstName = "";
    private String lastName = "";
    private String phone = "";
    private String nickname = "";
    private String email = "";
    private Bitmap thePic;
    private ContactFragment contactFragment;
    private File imageFile;
    private Intent yourIntentData;


    public AddMoreFragment(ContactFragment contactFragment) {
        this.contactFragment = contactFragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_add_more, container, false);
        return view;
    }

    private void checkAndRequestReadContactsPermission() {
        // Permission has already been granted, proceed with your operation
        if (getActivity() != null && ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.READ_CONTACTS)
                != PackageManager.PERMISSION_GRANTED) {
            // Permission is not granted, request it
            requestPermissions(new String[]{Manifest.permission.READ_CONTACTS},
                    REQUEST_READ_CONTACTS_PERMISSION);
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

    @Override
    public void initView(View view) {
        etUserName = view.findViewById(R.id.fragment_add_more_et_user_name);
        etPhoneNo = view.findViewById(R.id.fragment_add_more_et_phone_no);
        etFirstName = view.findViewById(R.id.fragment_add_more_et_first_name);
        etLastName = view.findViewById(R.id.fragment_add_more_et_last_name);
        etEmail = view.findViewById(R.id.fragment_add_more_et_email);
        ivProfilePic = view.findViewById(R.id.fragment_add_more_iv_user_profile);
        TextView tvCancel = view.findViewById(R.id.fragment_add_more_tv_cancel);
        TextView tvAddressBook = view.findViewById(R.id.fragment_add_more_tv_addressbook);
        Button tvSave = view.findViewById(R.id.fragment_add_more_tv_save);


        final Bundle bundle = getArguments();
        if (bundle != null) {
            String jsonObject = bundle.getString(Constant.JSON_OBJECT);
            try {
                isEditOrSave = true;
                JSONObject jsonobjectToChange = null;
                if (getActivity() != null && jsonObject != null) {
                    jsonobjectToChange = new JSONObject(jsonObject);
                    userId = jsonobjectToChange.optString(TAG_USER_ID);
                    nickname = jsonobjectToChange.optString(TAG_NICKNAME);
                    firstName = jsonobjectToChange.optString(TAG_FIRST_NAME);
                    lastName = jsonobjectToChange.optString(TAG_LAST_NAME);
                    emailPreviouus = jsonobjectToChange.optString(TAG_EMAIL);
                    phone = jsonobjectToChange.optString(TAG_PHONE);

                    etUserName.setText(String.format("%s", nickname));
                    etFirstName.setText(String.format("%s", firstName));
                    etLastName.setText(String.format("%s", lastName));
                    etEmail.setText(String.format("%s", emailPreviouus));
                    etPhoneNo.setText(String.format("%s", phone));


                    File directory = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), "DadApp");


                    Bitmap imageFromStorage = BitMapHelper.loadImageFromStorage(getActivity(), emailPreviouus, directory.toString());
                    //bitmapChanged = BitMapHelper.loadImageFromStorage(getActivity(), "" + emailPreviouus, Preference.getInstance().mSharedPreferences.getString(emailPreviouus, ""));
                    if (imageFromStorage == null) {
                        ivProfilePic.setImageDrawable(ContextCompat.getDrawable(getActivity(), R.drawable.pf_pic));
                    } else {
                        Bitmap circledBitmap = createScaleddBitmapFromFile(imageFromStorage);
                        ivProfilePic.setImageDrawable(new BitmapDrawable(circledBitmap));
                    }
                }

            } catch (JSONException e) {
                Log.e(TAG, "Error initialising view");
            }
        }
        ivProfilePic.setOnClickListener(this);
        tvCancel.setOnClickListener(this);
        tvAddressBook.setOnClickListener(this);
        tvSave.setOnClickListener(this);
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
        if (fragmentId == R.id.fragment_add_more_tv_save) {
            validateFragment();
        } else if (fragmentId == R.id.fragment_add_more_tv_cancel) {
            requireActivity().getSupportFragmentManager().popBackStack();
        } else if (fragmentId == R.id.fragment_add_more_iv_user_profile) {
            selectImage();
        } else if (fragmentId == R.id.fragment_add_more_tv_addressbook) {
            showcontacts();
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
                    if (getActivity() != null && data.getData() != null) {
                        final InputStream inputStream = getActivity().getContentResolver().openInputStream(data.getData());
                        final FileOutputStream fileOutputStream = new FileOutputStream(imageFile);
                        if (inputStream != null) {
                            CameraUtil.copyStream(inputStream, fileOutputStream);
                            inputStream.close();
                            fileOutputStream.close();
                        } else {
                            fileOutputStream.close();
                        }
                        startCropImage();
                    }
                } catch (Exception e) {
                    Log.e(TAG, "Error initialising gallery");
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
                            .into(new BitmapImageViewTarget(ivProfilePic) {
                                @Override
                                protected void setResource(Bitmap resource) {
                                    if (resource != null) {
                                        RoundedBitmapDrawable circularBitmapDrawable =
                                                RoundedBitmapDrawableFactory.create(getResources(), resource);
                                        circularBitmapDrawable.setCircular(true);
                                        ivProfilePic.setImageDrawable(circularBitmapDrawable);
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

            case Constants.REQUEST_CONTACT_NUMBER:
                yourIntentData = data;
                checkAndRequestReadContactsPermission();
                break;


        }
        super.onActivityResult(requestCode, resultCode, data);


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

        if (isImageUpdated) {
            BitMapHelper.deleteImageFromStorage(getActivity(), emailPreviouus, Preference.getInstance().mSharedPreferences.getString(emailPreviouus, ""));
            String bitmappath = BitMapHelper.saveImageAndGetPath(thePic, getActivity(), email);
            Preference.getInstance().savePreferenceData(email, bitmappath);
        }
        if (getActivity() != null) {
            if (etPhoneNo.getText().toString().trim().equalsIgnoreCase("")) {
                Utills.displayDialog(getActivity(), getString(R.string.app_name), getString(R.string.TAG_PHONE_NO_EMPTYMSG), getString(R.string.TAG_OK), "", false, false);
                etPhoneNo.requestFocus();
            } else if (!etPhoneNo.getText().toString().trim().equalsIgnoreCase("")) {
                if (Utills.isOnline(getActivity(), true)) {
                    if (isEditOrSave) {
                        if (imageFile != null) {
                            reName(imageFile.getPath());
                        }
                        new UpdateTask().execute();

                    } else {
                        if (imageFile != null) {
                            reName(imageFile.getPath());
                        }
                        saveAddressBook();
                    }

                } else {
                    Utills.displayDialog(getActivity(), getString(R.string.app_name), getString(R.string.TAG_INTERNET_AVAILABILITY), getString(R.string.TAG_OK), "", false, false);
                }

            }
        }
    }

    //Content Provider Method

    private void showcontacts() {
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

            InputStream openPhoto = openPhoto(Long.parseLong(contactID));

            Bitmap bitmap = BitmapFactory.decodeStream(openPhoto);
            if (bitmap == null) {
                ivProfilePic.setBackgroundDrawable(getResources().getDrawable(R.drawable.pf_pic));
            } else {
                Bitmap circleBitmap = BitMapHelper.getCircleBitmap(bitmap);
                setPicListStatus(circleBitmap);
                ivProfilePic.setImageBitmap(circleBitmap);
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
                        out.write(Arrays.copyOfRange(imageData, 0, Math.max(0, bytesRead)));
                    }
                }
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
            int indexNickName = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Nickname.NAME);
            if (cursor.moveToNext()) {
                nickNameStr = cursor.getString(indexNickName);
            }
            cursor.close();
        }

        return nickNameStr;
    }

    private void setPicListStatus(Bitmap scaledBitmap) {
        // TODO this line is used to get the id of imageview, hense if there
        // will be any change in layout...this code will also be changed
        try {
            //View view = (View) ivProfilePic.getParent().getParent().getParent().getParent().getParent().getParent();
            //int id = view.getId();
            //picStatusList.set(id, true);
            //bitmapArrayList.set(id, scaledBitmap);
            //if (picStatusList.get(id)) {
            thePic = scaledBitmap;
            isImageUpdated = true;
            // String email = etEmail.getText().toString();
            //String bitmappath = BitMapHelper.saveImageAndGetPath(scaledBitmap, getActivity(), email);
            //Preference.getInstance().savePreferenceData(email, bitmappath);
            //}

        } catch (Exception e) {
            Toast.makeText(getActivity(), "" + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
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
                userChoosenTask = getString(R.string.TAG_TAKE_PHOTO);

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
                userChoosenTask = getString(R.string.TAG_CHOOSE_FROM_GALLERY);
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

    public void gotoGallery() {
        imageFile = CameraUtil.getOutputMediaFile(1);
        final Intent photoPickerIntent = new Intent(Intent.ACTION_PICK);
        photoPickerIntent.setType("image/*");
        startActivityForResult(photoPickerIntent, Constants.REQUEST_CODE_GALLERY);
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


    private Bitmap createScaleddBitmapFromFile(Bitmap bitmap) {
        Bitmap scaledBitmap = Bitmap.createScaledBitmap(bitmap, bitmap.getWidth(), bitmap.getHeight(), true);
        return BitMapHelper.getCircleBitmap(scaledBitmap);
    }

    private void reName(String imgPath) {
        File directory = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), "DadApp");
        String filename = imgPath.substring(imgPath.lastIndexOf("/") + 1);
//        String filename = imgPath.substring(path.lastIndexOf("/") + 1);
        File from = new File(directory, filename);
        File to = new File(directory, email + ".jpg");
        if (from.exists())
            from.renameTo(to);

    }


    //Networking Task


    private class UpdateTask extends AsyncTask<String, Void, String> {

        private static final String EDITED_SUCCESSFULLY = "Contact has been edited successfully.";
        private static final String KEY_SUCCESS = "success";
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

            if (Utills.isInternetConnected(getActivity())) {
                wsCallUpdateContact.executeService(userId, firstName, lastName, nickname, email, phone, "");
                if (wsCallUpdateContact.isSuccess()) {
                    ContactFragment.isServiceCall = true;
                    requireActivity().getSupportFragmentManager().popBackStack();
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
                    Toast.makeText(getActivity(), "" + result, Toast.LENGTH_SHORT).show();
                    break;

                case 1:
                    Toast.makeText(getActivity(), getString(R.string.TAG_PWD_EMAIL_EROR), Toast.LENGTH_SHORT).show();
                    break;

                default:
                    if (result.equals("fail")) {
                        Toast.makeText(getActivity(), getString(R.string.TAG_PWD_FETCH_EROR), Toast.LENGTH_SHORT).show();
                    } else {
                        Log.e(TAG, "success");
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

    private class AsyncTaskSaveAddress extends AsyncTask<Void, Void, Void> {

        private WsCallAddreceiver wsCallAddreceiver;
        private String etUserNameStr = etUserName.getText().toString().trim();
        private String etPhoneNoStr = etPhoneNo.getText().toString().trim();
        private String etFirNamestr = etFirstName.getText().toString().trim();
        private String etLastnameStr = etLastName.getText().toString().trim();
        private String etEmailstr = etEmail.getText().toString().trim();
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
                    ContactFragment.isServiceCall = true;
                    requireActivity().getSupportFragmentManager().popBackStack();

                } else {
                    Utills.displayDialog(getActivity(), getString(R.string.app_name), getString(R.string.TAG_CONT_UNABLE_ADDED), getString(android.R.string.ok), "", false, false);
                }
            }

        }

    }


}



