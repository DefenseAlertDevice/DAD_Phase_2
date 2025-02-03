//package net.tigerlight.dad.recievers;
//
//import android.app.ProgressDialog;
//import android.content.ActivityNotFoundException;
//import android.content.ContentUris;
//import android.content.Context;
//import android.content.Intent;
//import android.content.pm.PackageManager;
//import android.database.Cursor;
//import android.graphics.Bitmap;
//import android.graphics.BitmapFactory;
//import android.graphics.Matrix;
//import android.net.Uri;
//import android.os.AsyncTask;
//import android.os.Bundle;
//import android.os.Environment;
//import android.provider.ContactsContract;
//import android.provider.ContactsContract.Contacts;
//import android.provider.MediaStore;
//import android.util.Log;
//import android.view.LayoutInflater;
//import android.view.View;
//import android.view.ViewGroup;
//import android.view.inputmethod.InputMethodManager;
//import android.widget.EditText;
//import android.widget.ImageView;
//import android.widget.TextView;
//import android.widget.Toast;
//import net.tigerlight.dad.R;
//import net.tigerlight.dad.home.BaseActivity;
//import net.tigerlight.dad.settings.webservices.WsCallAddreceiver;
//import net.tigerlight.dad.util.BitMapHelper;
//import net.tigerlight.dad.util.CheckForeground;
//import net.tigerlight.dad.util.Preference;
//import java.io.ByteArrayInputStream;
//import java.io.File;
//import java.io.FileOutputStream;
//import java.io.IOException;
//import java.io.InputStream;
//import java.io.OutputStream;
//import java.util.ArrayList;
//import java.util.Calendar;
//import java.util.HashMap;
//import java.util.TimeZone;
//
//public class ReceiverAddNew extends BaseActivity {
//
//    private EditText nickNameEdit;
//    private EditText firstNameEdit;
//    private EditText lastNameEdit;
//    private EditText emailEdit;
//    private EditText phoneEdit;
//    private String nickName;
//    private String firstName;
//    private String lastName;
//    private String email;
//    private String phone;
//    private ImageView tosetPicOnImageView;
//    private boolean isVisisble;
//    private EditText addressEdit;
//    private String address;
//
//    //    private JSONObject jsonObjectMain;
//    //    private JSONArray jsonArray;
//    private View imageSelectorView;
//    private boolean isPicChooserCreated;
//
//    private static final String KEY_SUCCESS = "success";
//    private static final int SELECT_PICTURE_FROM_GALLERY = 101;
//    private static final int SELECT_PICTURE_FROM_CAMERA = 102;
//    private static final int REQUEST_CONTACT_NUMBER = 104;
//    public static boolean IS_UPDATED = false;
//
//    private int ID_LAYOUT = 0;
//    private ArrayList<Boolean> picStatusList = new ArrayList<Boolean>();
//    private ArrayList<Bitmap> bitmapArrayList = new ArrayList<Bitmap>();
//    private String timezoneID;
//    private ProgressDialog progressDialog;
//
//    private final String TAG_FIELD_USER_ID = "fld_user_id";
//    private final String TAG_USER_LIST = "userlist";
//
//    private final String TAG_FIRST_NAME = "firstname";
//    private final String TAG_LAST_NAME = "lastname";
//    private final String TAG_NICKNAME = "nickname";
//    private final String TAG_EMAIL = "email";
//    private final String TAG_ADDRESS = "address";
//    private final String TAG_PHONE = "phone";
//    private final String USERID_Reciever = "userid";
//
//    @Override
//    protected void onCreate(Bundle arg0) {
//        super.onCreate(arg0);
//
//        setContentView(R.layout.reciever_add_new);
//        findViewById(R.id.recieverBack).setOnClickListener(this);
//        findViewById(R.id.recieverDone).setOnClickListener(this);
//        findViewById(R.id.addMore).setOnClickListener(this);
//
//        ViewGroup contacts = (ViewGroup) findViewById(R.id.contacts);
//
//        View childContact0 = contacts.getChildAt(0);
//
//        childContact0.setId(ID_LAYOUT);
//        picStatusList.add(false);
//        bitmapArrayList.add(null);
//        ID_LAYOUT++;
//
//        nickNameEdit = ((EditText) childContact0.findViewById(R.id.recievernickname));
//        firstNameEdit = ((EditText) childContact0.findViewById(R.id.recieverfirstname));
//        lastNameEdit = ((EditText) childContact0.findViewById(R.id.recieverlastName));
//        emailEdit = (EditText) childContact0.findViewById(R.id.recieverEmail);
//        phoneEdit = (EditText) childContact0.findViewById(R.id.recieverPhone);
//        addressEdit = (EditText) childContact0.findViewById(R.id.recieverAddress);
//
//        childContact0.findViewById(R.id.imageViewPic).setOnClickListener(this);
//        childContact0.findViewById(R.id.buttonContact).setOnClickListener(this);
//        childContact0.findViewById(R.id.buttonDelete).setVisibility(View.INVISIBLE);
//
//        findViewById(R.id.layoutAlert).setOnClickListener(this);
//        findViewById(R.id.layoutContact).setOnClickListener(this);
//        findViewById(R.id.layoutSettings).setOnClickListener(this);
//        findViewById(R.id.layoutIamOK).setOnClickListener(this);
//
//        int alertCount = Preference.getInstance().mSharedPreferences.getInt(C.ALERT_COUNT, 0);
//        ((TextView) findViewById(R.id.alertCount)).setText("" + alertCount);
//
//        initializemTempFile();
//
//    }
//
//    @Override
//    public void onResume() {
//        super.onResume();
//        hideSoftKeyboard();
//        Calendar cal = Calendar.getInstance();
//        TimeZone tz = cal.getTimeZone();
//        timezoneID = tz.getID();
//        CheckForeground.onResume(ReceiverAddNew.this);
//        int alertCount = Preference.getInstance().mSharedPreferences.getInt(C.ALERT_COUNT, 0);
//        ((TextView) findViewById(R.id.alertCount)).setText("" + alertCount);
//    }
//
//    @Override
//    protected void onPause() {
//        super.onPause();
//        CheckForeground.onPause();
//    }
//
//    @Override
//    public void onClick(View view) {
//        switch (view.getId()) {
//            case R.id.recieverDone:
//                hideSoftKeyboard();
//                if (!Utils.isInternetConnected(this)) {
//                    Toast.makeText(this, Utils.NO_INTERNET, Toast.LENGTH_SHORT).show();
//                    return;
//                }
//                progressDialog = ProgressDialog.show(ReceiverAddNew.this, "", "Please wait....");
//                progressDialog.setCancelable(false);
//                addRecieverList();
//                break;
//
//            case R.id.recieverBack:
//                finish();
//                break;
//
//            case R.id.addMore:
//                onAddMoreClick();
//                break;
//
//            case R.id.buttonDelete:
//                deleteContact(view);
//                break;
//
//            case R.id.buttonContact:
//                View parent = (View) ((View) view.getParent()).getParent();
//                setFieldsToEdit(parent);
//                showcontacts();
//
//                break;
//
//            case R.id.imageViewPic:
//                hideSoftKeyboard();
//                tosetPicOnImageView = (ImageView) view;
//                if (isPicChooserCreated) {
//                    return;
//                }
//                addPicchooserView(view);
//                break;
//
//            case R.id.btncamera:
//                selectFromcamera();
//                break;
//
//            case R.id.btnGallery:
//                isPicChooserCreated = false;
//                selectfromGallery();
//                break;
//
//            case R.id.btncancel:
//                removeImagePickedDialog(view);
//                break;
//
//            case R.id.layoutContact:
//                findViewById(R.id.buttonContactImage).setSelected(true);
//                findViewById(R.id.buttonAlerts).setSelected(false);
//                findViewById(R.id.buttonSetting).setSelected(false);
//                break;
//
//            case R.id.layoutAlert:
//                findViewById(R.id.buttonAlerts).setSelected(true);
//                findViewById(R.id.buttonContactImage).setSelected(false);
//                findViewById(R.id.buttonSetting).setSelected(false);
//                startAlertListScreen();
//                break;
//
//            case R.id.layoutSettings:
//                findViewById(R.id.buttonSetting).setSelected(true);
//                findViewById(R.id.buttonContactImage).setSelected(false);
//                findViewById(R.id.buttonAlerts).setSelected(false);
//                startSettingScreen();
//
//            case R.id.layoutIamOK:
//                if (!Preference.getInstance().mSharedPreferences.getBoolean(Utils.isPinCreated, false)) {
//                    Toast.makeText(this, C.SORRY_NO_PIN, Toast.LENGTH_SHORT).show();
//                    return;
//                }
//                findViewById(R.id.buttonIamOK).setSelected(true);
//                findViewById(R.id.buttonSetting).setSelected(false);
//                findViewById(R.id.buttonContactImage).setSelected(false);
//                findViewById(R.id.buttonAlerts).setSelected(false);
//                showIamOkDialog();
//                break;
//
//            default:
//                break;
//        }
//    }
//
//    private void hideSoftKeyboard() {
//        if (getCurrentFocus() != null) {
//            InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
//            inputMethodManager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
//        }
//    }
//
//    private void startAlertListScreen() {
//        Intent i = new Intent(this, AlertListScreen.class);
//        startActivity(i);
//
//    }
//
//    private void setFieldsToEdit(View parent) {
//        nickNameEdit = ((EditText) parent.findViewById(R.id.recievernickname));
//        firstNameEdit = ((EditText) parent.findViewById(R.id.recieverfirstname));
//        lastNameEdit = ((EditText) parent.findViewById(R.id.recieverlastName));
//        emailEdit = (EditText) parent.findViewById(R.id.recieverEmail);
//        phoneEdit = (EditText) parent.findViewById(R.id.recieverPhone);
//        addressEdit = (EditText) parent.findViewById(R.id.recieverAddress);
//        tosetPicOnImageView = (ImageView) parent.findViewById(R.id.imageViewPic);
//    }
//
//    private void startSettingScreen() {
//        Intent i = new Intent(this, SettingScreen.class);
//        startActivity(i);
//        finish();
//    }
//
//    private void selectFromcamera() {
//        isPicChooserCreated = false;
//        ((ViewGroup) imageSelectorView.getParent()).removeView(imageSelectorView);
//        if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA_ANY)) {
//            Toast.makeText(this, "Device does not have camera.", Toast.LENGTH_SHORT).show();
//            return;
//        }
//        selectFromcameraNew();
//    }
//
//    private void selectFromcameraNew() {
//        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
//
//        intent.putExtra(MediaStore.EXTRA_OUTPUT, MediaStore.Images.Media.EXTERNAL_CONTENT_URI.toString());
//        try {
//            intent.putExtra("return-data", true);
//            startActivityForResult(intent, SELECT_PICTURE_FROM_CAMERA);
//
//        } catch (ActivityNotFoundException e) {
//        }
//    }
//
//    private void selectfromGallery() {
//
//        {
//            ((ViewGroup) imageSelectorView.getParent()).removeView(imageSelectorView);
//            Intent photoPickerIntent = new Intent(Intent.ACTION_PICK);
//            photoPickerIntent.setType("image/*");
//            startActivityForResult(photoPickerIntent, SELECT_PICTURE_FROM_GALLERY);
//        }
//
//    }
//
//    private void removeImagePickedDialog(View view) {
//        isPicChooserCreated = false;
//        View parent = (View) view.getParent();
//        ((ViewGroup) parent.getParent()).removeView(parent);
//    }
//
//    private void showcontacts() {
//        Intent intent = new Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI);
//        startActivityForResult(intent, REQUEST_CONTACT_NUMBER);
//    }
//
//    private void addPicchooserView(View view) {
//
//        ViewGroup contacts = (ViewGroup) findViewById(R.id.imagePickerLayout);
//        View childContact = LayoutInflater.from(getApplicationContext()).inflate(R.layout.footer_image, null);
//        imageSelectorView = childContact;
//        childContact.findViewById(R.id.btncamera).setOnClickListener(this);
//        childContact.findViewById(R.id.btnGallery).setOnClickListener(this);
//        childContact.findViewById(R.id.btncancel).setOnClickListener(this);
//        contacts.addView(childContact);
//        isPicChooserCreated = true;
//
//    }
//
//    @Override
//    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
//        super.onActivityResult(requestCode, resultCode, data);
//        if (resultCode == RESULT_OK) {
//            if (requestCode == SELECT_PICTURE_FROM_GALLERY) {
//                setfromCropped(data);
//                return;
//            }
//            if (requestCode == SELECT_PICTURE_FROM_CAMERA) {
//                toSetAnImageFromCamera(data);
//                return;
//            }
//            if (requestCode == REQUEST_CONTACT_NUMBER) {
//                toSetContactSelectedAjay(data);
//                return;
//            }
//            if (requestCode == Utils.REQUEST_CODE_CROP_IMAGE) {
//                String path = data.getStringExtra(CropImage.IMAGE_PATH);
//                if (path == null) {
//                    return;
//                }
//
//                Bitmap bitmap = BitmapFactory.decodeFile(Utils.mFileTemp.getPath());
//                if (bitmap == null) {
//                    return;
//                }
//                Bitmap croppedBitmap = BitMapHelper.getCircleBitmap(bitmap);
//                setPicListStatus(croppedBitmap);
//                tosetPicOnImageView.setImageBitmap(croppedBitmap);
//            }
//        }
//
//    }
//
//    @SuppressWarnings("deprecation")
//    private void toSetContactSelectedAjay(Intent data) {
//
//        Uri uriContact = data.getData();
//        String contactName = null;
//        Cursor cursor = getContentResolver().query(uriContact, null, null, null, null);
//        if (cursor.moveToFirst()) {
//            contactName = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));
//        }
//        cursor.close();
//        firstNameEdit.setText(contactName);
//
//        String contactNumber = null;
//        Cursor cursorID = getContentResolver().query(uriContact, new String[]{ContactsContract.Contacts._ID}, null, null, null);
//        String contactID = null;
//        if (cursorID.moveToFirst()) {
//            contactID = cursorID.getString(cursorID.getColumnIndex(ContactsContract.Contacts._ID));
//        }
//        cursorID.close();
//
//        Cursor cursorPhone = getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, new String[]{ContactsContract.CommonDataKinds.Phone.NUMBER},
//                ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ? AND " + ContactsContract.CommonDataKinds.Phone.TYPE + " = " + ContactsContract.CommonDataKinds.Phone.TYPE_MOBILE, new String[]{contactID}, null);
//        if (cursorPhone.moveToFirst()) {
//            contactNumber = cursorPhone.getString(cursorPhone.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
//        }
//        phoneEdit.setText(contactNumber);
//        cursorPhone.close();
//
//        InputStream openPhoto = openPhoto(Long.parseLong(contactID));
//        Bitmap bitmap = BitmapFactory.decodeStream(openPhoto);
//        if (bitmap == null) {
//            tosetPicOnImageView.setBackgroundDrawable(getResources().getDrawable(R.drawable.pf_pic));
//        } else {
//            Bitmap circleBitmap = BitMapHelper.getCircleBitmap(bitmap);
//            setPicListStatus(circleBitmap);
//            tosetPicOnImageView.setImageBitmap(circleBitmap);
//        }
//
//        // Bitmap thumbnailID = new QuickContactHelper(this,
//        // contactNumber).addThumbnail(this);
//        // if (thumbnailID == null) {
//        // tosetPicOnImageView.setBackgroundDrawable(getResources().getDrawable(R.drawable.pf_pic));
//        // } else {
//        // setPicListStatus(thumbnailID);
//        // tosetPicOnImageView.setImageBitmap(thumbnailID);
//        // }
//
//        String contactEmail = null;
//        Cursor cursorEmail = getContentResolver().query(ContactsContract.CommonDataKinds.Email.CONTENT_URI, new String[]{ContactsContract.CommonDataKinds.Email.DATA},
//                ContactsContract.CommonDataKinds.Email.CONTACT_ID + " = ? AND " + ContactsContract.CommonDataKinds.Email.TYPE + " = " + ContactsContract.CommonDataKinds.Email.TYPE, new String[]{contactID}, null);
//        if (cursorEmail.moveToFirst()) {
//            contactEmail = cursorEmail.getString(cursorEmail.getColumnIndex(ContactsContract.CommonDataKinds.Email.DATA));
//        }
//        cursorEmail.close();
//        emailEdit.setText(contactEmail);
//
//        String nickName2 = getNickName(contactID);
//        nickNameEdit.setText(nickName2);
//
//        addressEdit.setText(getAddress(contactID));
//    }
//
//    public InputStream openPhoto(long contactId) {
//        Uri contactUri = ContentUris.withAppendedId(Contacts.CONTENT_URI, contactId);
//        Uri photoUri = Uri.withAppendedPath(contactUri, Contacts.Photo.CONTENT_DIRECTORY);
//        Cursor cursor = getContentResolver().query(photoUri, new String[]{Contacts.Photo.PHOTO}, null, null, null);
//        if (cursor == null) {
//            return null;
//        }
//        try {
//            if (cursor.moveToFirst()) {
//                byte[] data = cursor.getBlob(0);
//                if (data != null) {
//                    return new ByteArrayInputStream(data);
//                }
//            }
//        } finally {
//            cursor.close();
//        }
//        return null;
//    }
//
//    private String getNickName(String id) {
//        Uri URI_NICK_NAME = ContactsContract.Data.CONTENT_URI;
//        String SELECTION_NICK_NAME = ContactsContract.Data.CONTACT_ID + " = ? AND " + ContactsContract.Data.MIMETYPE + " = ?";
//        String[] SELECTION_ARRAY_NICK_NAME = new String[]{id, ContactsContract.CommonDataKinds.Nickname.CONTENT_ITEM_TYPE};
//
//        Cursor cursor = getContentResolver().query(URI_NICK_NAME, null, SELECTION_NICK_NAME, SELECTION_ARRAY_NICK_NAME, null);
//
//        int indexNickName = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Nickname.NAME);
//        String nickNameStr = "";
//        if (cursor.moveToNext()) {
//            nickNameStr = cursor.getString(indexNickName);
//        }
//        cursor.close();
//        return nickNameStr;
//    }
//
//    private String getAddress(String id) {
//        Uri URI_ADDRESS = ContactsContract.Data.CONTENT_URI;
//        String SELECTION_ADDRESS = ContactsContract.Data.CONTACT_ID + " = ? AND " + ContactsContract.Data.MIMETYPE + " = ?";
//        String[] SELECTION_ARRAY_ADDRESS = new String[]{id, ContactsContract.CommonDataKinds.StructuredPostal.CONTENT_ITEM_TYPE};
//
//        Cursor cursor = getContentResolver().query(URI_ADDRESS, null, SELECTION_ADDRESS, SELECTION_ARRAY_ADDRESS, null);
//        int indexAddType = cursor.getColumnIndex(ContactsContract.CommonDataKinds.StructuredPostal.TYPE);
//        int indexStreet = cursor.getColumnIndex(ContactsContract.CommonDataKinds.StructuredPostal.STREET);
//
//        String addressvalue = "";
//        if (cursor.getCount() > 0) {
//            HashMap<Integer, String> addressMap = new HashMap<Integer, String>();
//            while (cursor.moveToNext()) {
//
//                String typeStr = cursor.getString(indexAddType);
//                addressvalue = cursor.getString(indexStreet);
//            }
//        }
//        cursor.close();
//        return addressvalue;
//    }
//
//    void getEmailFromCursor(Cursor cr, String id) {
//
//        Uri URI_EMAIL = ContactsContract.CommonDataKinds.Email.CONTENT_URI;
//        String SELECTION_EMAIL = ContactsContract.CommonDataKinds.Email.CONTACT_ID + " = ?";
//        String[] SELECTION_ARRAY_EMAIL = new String[]{id};
//
//        Cursor emailCur = getContentResolver().query(URI_EMAIL, null, SELECTION_EMAIL, SELECTION_ARRAY_EMAIL, null);
//        int indexEmail = emailCur.getColumnIndex(ContactsContract.CommonDataKinds.Email.DATA);
//        int indexEmailType = emailCur.getColumnIndex(ContactsContract.CommonDataKinds.Email.TYPE);
//
//        if (emailCur.getCount() > 0) {
//
//            HashMap<Integer, String> emailMap = new HashMap<Integer, String>();
//
//            while (emailCur.moveToNext()) {
//                // This would allow you get several email addresses,
//                // if the email addresses were stored in an array
//                String emailStr = emailCur.getString(indexEmail);
//                String emailTypeStr = emailCur.getString(indexEmailType);
//                if (emailTypeStr != null && !emailTypeStr.equals("")) {
//                    emailMap.put(Integer.parseInt(emailTypeStr), emailStr);
//                }
//            }
//            // contact.setEmails(emailMap);
//        }
//        emailCur.close();
//    }
//
//    private void toSetAnImageFromCamera(Intent data) {
//
//        Bundle extras = data.getExtras();
//        if (extras != null) {
//            Bitmap photo = extras.getParcelable("data");
//            Bitmap resizedBitmap;
//            if (photo.getWidth() >= photo.getHeight()) {
//                resizedBitmap = Bitmap.createBitmap(photo, photo.getWidth() / 2 - photo.getHeight() / 2, 0, photo.getHeight(), photo.getHeight());
//            } else {
//                resizedBitmap = Bitmap.createBitmap(photo, 0, photo.getHeight() / 2 - photo.getWidth() / 2, photo.getWidth(), photo.getWidth());
//            }
//            setPicListStatus(resizedBitmap);
//            Bitmap circledBitmap = createScaleddBitmapFromFile(resizedBitmap);
//            tosetPicOnImageView.setImageBitmap(circledBitmap);
//        }
//    }
//
//    private Bitmap createScaleddBitmapFromFile(Bitmap bitmap) {
//        Bitmap scaledBitmap = Bitmap.createScaledBitmap(bitmap, bitmap.getWidth(), bitmap.getHeight(), true);
//        Bitmap croppedBitmap = BitMapHelper.getCircleBitmap(scaledBitmap);
//        return croppedBitmap;
//    }
//
//    @SuppressWarnings("unused")
//    private Bitmap getRotatedBmp(Bitmap bitmap) {
//        Matrix matrix = new Matrix();
//        matrix.reset();
//        matrix.postRotate(90);
//        Bitmap newBitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
//        return newBitmap;
//    }
//
//    @SuppressWarnings("unused")
//    private Bitmap decodeSampledBitmapFromFile(String path, int reqWidth, int reqHeight) {
//
//        BitmapFactory.Options options = new BitmapFactory.Options();
//        options.inSampleSize = 8;
//        Bitmap decodeFile = BitmapFactory.decodeFile(path, options);
//        Bitmap scaledBitmap = Bitmap.createScaledBitmap(decodeFile, 50, 50, true);
//        return scaledBitmap;
//
//    }
//
//    private void setPicListStatus(Bitmap scaledBitmap) {
//        // TODO this line is used to get the id of imageview, hense if there
//        // will be any change in layout...this code will also be changed
//        View view = (View) tosetPicOnImageView.getParent().getParent().getParent().getParent();
//        int id = view.getId();
//        picStatusList.set(id, true);
//        bitmapArrayList.set(id, scaledBitmap);
//    }
//
//    private void deleteContact(View v) {
//        View parent = (View) v.getParent();
//        View contact = (View) parent.getParent();
//
//        // picStatusList.set(contact.getId(), false);
//
//        ((ViewGroup) contact.getParent()).removeView(contact);
//
//        ViewGroup contacts = (ViewGroup) findViewById(R.id.contacts);
//        if (contacts.getChildCount() == 1) {
//            View childAt = contacts.getChildAt(0);
//            childAt.findViewById(R.id.buttonDelete).setVisibility(View.INVISIBLE);
//        }
//
//    }
//
//    private void onAddMoreClick() {
//
//        picStatusList.add(false);
//        bitmapArrayList.add(null);
//
//        ViewGroup contacts = (ViewGroup) findViewById(R.id.contacts);
//        if (!isVisisble || contacts.getChildCount() == 1) {
//            isVisisble = true;
//            View childAt = contacts.getChildAt(0);
//            childAt.findViewById(R.id.buttonContact).setVisibility(View.VISIBLE);
//            childAt.findViewById(R.id.buttonContact).setOnClickListener(this);
//            childAt.findViewById(R.id.buttonDelete).setVisibility(View.VISIBLE);
//            childAt.findViewById(R.id.buttonDelete).setOnClickListener(this);
//        }
//        View childContact = LayoutInflater.from(getApplicationContext()).inflate(R.layout.contact, null);
//        childContact.setId(ID_LAYOUT);
//        ID_LAYOUT++;
//        childContact.findViewById(R.id.imageViewPic).setOnClickListener(this);
//        childContact.findViewById(R.id.buttonDelete).setOnClickListener(this);
//        childContact.findViewById(R.id.buttonContact).setOnClickListener(this);
//        contacts.addView(childContact);
//
//    }
//
//    private void addRecieverList() {
//        ViewGroup contacts = (ViewGroup) findViewById(R.id.contacts);
////        int childCount = contacts.getChildCount();
////        jsonObjectMain = new JSONObject();
////        String userId = Preference.getInstance().mSharedPreferences.getString(C.USER_ID, "");
////        try {
//////            jsonObjectMain.put(TAG_FIELD_USER_ID, userId);
////        } catch (JSONException e) {
////            e.printStackTrace();
////            progressDialog.dismiss();
////            return;
////        }
////        jsonArray = new JSONArray();
////        for (int i = 0; i < childCount; i++) {
//        if (!isSuccessFullyAdded(contacts.getChildAt(0))) {
//            progressDialog.dismiss();
//            return;
//        }
////        }
//
////        for (int i = 0; i < childCount; i++) {
//        View childAt = contacts.getChildAt(0);
//        int id = childAt.getId();
//        if (picStatusList.get(id)) {
//            EditText editText = (EditText) childAt.findViewById(R.id.recieverEmail);
//            String email = editText.getText().toString();
//            String bitmappath = BitMapHelper.saveImageAndGetPath(bitmapArrayList.get(id), this, email);
//            Preference.getInstance().savePreferenceData(email, bitmappath);
//        }
////        }
//
////        try {
////            jsonObjectMain.put(TAG_USER_LIST, jsonArray);
////        } catch (JSONException e) {
////            progressDialog.dismiss();
////            e.printStackTrace();
////            return;
////        }
//
//        new AddNewReciewverTask().execute();
//
//    }
//
//    private boolean isSuccessFullyAdded(View view) {
//        nickNameEdit = ((EditText) view.findViewById(R.id.recievernickname));
//        firstNameEdit = ((EditText) view.findViewById(R.id.recieverfirstname));
//        lastNameEdit = ((EditText) view.findViewById(R.id.recieverlastName));
//        emailEdit = (EditText) view.findViewById(R.id.recieverEmail);
//        phoneEdit = (EditText) view.findViewById(R.id.recieverPhone);
//        addressEdit = (EditText) view.findViewById(R.id.recieverAddress);
//
//        nickName = nickNameEdit.getText().toString();
//        firstName = firstNameEdit.getText().toString();
//        lastName = lastNameEdit.getText().toString();
//        email = emailEdit.getText().toString();
//        phone = phoneEdit.getText().toString();
//        address = addressEdit.getText().toString();
//
//        if (isNull(firstName.trim())) {
//            Utils.showToast(this, "Please enter your first name...!!");
//            requestFocus(firstNameEdit);
//            return false;
//        }
//
//        if (isNull(lastName.trim())) {
//            Utils.showToast(this, "Please enter your lastName ...!!");
//            requestFocus(lastNameEdit);
//            return false;
//        }
//
//        if (isNull(email.trim())) {
//            Utils.showToast(this, "Please enter your email address...!!");
//            requestFocus(emailEdit);
//            return false;
//        }
//        if (email.equals(Preference.getInstance().mSharedPreferences.getString(C.KEY_EMAIL, ""))) {
//            Utils.showToast(this, "Sorry, You can not add yourself in alert contacts.");
//            return false;
//        }
//
//        if (isNull(address.trim())) {
//            Utils.showToast(this, "Please enter your address...!!");
//            requestFocus(addressEdit);
//            return false;
//        }
//
//        if (!Utils.isValidEmail(email)) {
//            emailEdit.setError("Please enter correct email ID.");
//            Utils.hideSoftKeyboard(this);
//            return false;
//        }
//
////        JSONObject object = new JSONObject();
////        try {
////            object.put(TAG_FIRST_NAME, firstName);
////            object.put(TAG_LAST_NAME, lastName);
////            object.put(TAG_NICKNAME, nickName);
////            object.put(TAG_EMAIL, email);
////            object.put(TAG_PHONE, phone);
////            object.put(TAG_ADDRESS, address);
//////            jsonArray.put(object);
////        } catch (JSONException e) {
////            e.printStackTrace();
////        }
//        return true;
//    }
//
//    private void requestFocus(View view) {
//        InputMethodManager mgr = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
//        mgr.showSoftInput(view, InputMethodManager.SHOW_IMPLICIT);
//    }
//
//    private class AddNewReciewverTask extends AsyncTask<String, Void, String> {
//
//        private static final String ADDED_SUCCESS_MSG = "Contacts has been added successfully";
//        private int response;
//        private WsCallAddreceiver wsCallAddreceiver;
//
//        @Override
//        protected void onPreExecute() {
//            super.onPreExecute();
//            wsCallAddreceiver = new WsCallAddreceiver(ReceiverAddNew.this);
//        }
//
//        @Override
//        protected String doInBackground(String... params) {
//
//            if (Utils.isInternetConnected(ReceiverAddNew.this)) {
//                wsCallAddreceiver.executeService(firstName, lastName, nickName, email, phone, address);
//                if (wsCallAddreceiver.isSuccess()) {
//                    return KEY_SUCCESS;
//                } else {
//                    response = 2;
//                    return wsCallAddreceiver.getMessage();
//                }
//            } else {
//                response = 1;
//            }
//            return "fail";
//
//        }
//
//        @Override
//        protected void onPostExecute(String result) {
//            progressDialog.dismiss();
//            switch (response) {
//                case 2:
//                    Toast.makeText(ReceiverAddNew.this, "" + result, Toast.LENGTH_SHORT).show();
//                    break;
//
//                case 1:
//                    Toast.makeText(ReceiverAddNew.this, "Email or password is incorrect.", Toast.LENGTH_SHORT).show();
//                    break;
//
//                default:
//                    if (result.equals("fail")) {
//                        Toast.makeText(ReceiverAddNew.this, Utils.SERVER_ERROR, Toast.LENGTH_SHORT).show();
//                    } else {
//                        Toast.makeText(ReceiverAddNew.this, ADDED_SUCCESS_MSG, Toast.LENGTH_SHORT).show();
//                        setUpdated();
//                        finish();
//                    }
//                    break;
//            }
//        }
//
//        private void setUpdated() {
//            IS_UPDATED = true;
//        }
//
//    }
//
//    private boolean isNull(String value) {
//        return value.trim().matches("");
//    }
//
//    protected void showToast(String message) {
//        Toast.makeText(ReceiverAddNew.this, message, Toast.LENGTH_SHORT).show();
//    }
//
//    @Override
//    public void onSaveInstanceState(Bundle outState) {
//        super.onSaveInstanceState(outState);
//        if (tosetPicOnImageView == null || outState == null) {
//            return;
//        }
//        outState.putInt(C.IMAGEVIEW_ID, tosetPicOnImageView.getId());
//        System.out.println("EditProfileScreen.onSaveInstanceState()");
//    }
//
//    @Override
//    protected void onRestoreInstanceState(Bundle savedInstanceState) {
//        super.onRestoreInstanceState(savedInstanceState);
//        System.out.println("EditProfileScreen.onRestoreInstanceState()");
//        tosetPicOnImageView = (ImageView) findViewById(savedInstanceState.getInt(C.IMAGEVIEW_ID));
//    }
//
//    private void setfromCropped(Intent data) {
//        try {
//
//            InputStream inputStream = getContentResolver().openInputStream(data.getData());
//            FileOutputStream fileOutputStream = new FileOutputStream(Utils.mFileTemp);
//            copyStream(inputStream, fileOutputStream);
//            fileOutputStream.close();
//            inputStream.close();
//            startCropImage();
//
//        } catch (Exception e) {
//
//            Log.e("TAG", "Error while creating temp file", e);
//        }
//    }
//
//    private void startCropImage() {
//
//        Intent intent = new Intent(this, CropImage.class);
//        intent.putExtra(CropImage.IMAGE_PATH, Utils.mFileTemp.getPath());
//        intent.putExtra(CropImage.SCALE, true);
//
//        intent.putExtra(CropImage.ASPECT_X, 1);
//        intent.putExtra(CropImage.ASPECT_Y, 1);
//        intent.putExtra(CropImage.OUTPUT_X, 256);
//        intent.putExtra(CropImage.OUTPUT_Y, 256);
//
//        startActivityForResult(intent, Utils.REQUEST_CODE_CROP_IMAGE);
//    }
//
//    private void copyStream(InputStream input, OutputStream output) throws IOException {
//
//        byte[] buffer = new byte[1024];
//        int bytesRead;
//        while ((bytesRead = input.read(buffer)) != -1) {
//            output.write(buffer, 0, bytesRead);
//        }
//    }
//
//    private void initializemTempFile() {
//        String state = Environment.getExternalStorageState();
//        if (Environment.MEDIA_MOUNTED.equals(state)) {
//            Utils.mFileTemp = new File(Environment.getExternalStorageDirectory(), Utils.TEMP_PHOTO_FILE_NAME);
//        } else {
//            Utils.mFileTemp = new File(getFilesDir(), Utils.TEMP_PHOTO_FILE_NAME);
//        }
//
//    }
//
//}
