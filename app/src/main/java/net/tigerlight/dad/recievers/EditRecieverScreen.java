//package net.tigerlight.dad.recievers;
//
//import android.content.ActivityNotFoundException;
//import android.content.Context;
//import android.content.Intent;
//import android.content.pm.PackageManager;
//import android.graphics.Bitmap;
//import android.graphics.BitmapFactory;
//import android.graphics.drawable.BitmapDrawable;
//import android.os.AsyncTask;
//import android.os.Bundle;
//import android.os.Environment;
//import android.provider.MediaStore;
//import android.util.Log;
//import android.view.LayoutInflater;
//import android.view.View;
//import android.view.View.OnClickListener;
//import android.view.ViewGroup;
//import android.view.inputmethod.InputMethodManager;
//import android.widget.Button;
//import android.widget.EditText;
//import android.widget.ImageView;
//import android.widget.TextView;
//import android.widget.Toast;
//import net.tigerlight.dad.R;
//import net.tigerlight.dad.home.BaseActivity;
//import net.tigerlight.dad.util.BitMapHelper;
//import net.tigerlight.dad.util.CheckForeground;
//import net.tigerlight.dad.util.Preference;
//import org.json.JSONException;
//import java.io.File;
//import java.io.FileOutputStream;
//import java.io.IOException;
//import java.io.InputStream;
//import java.io.OutputStream;
//
//public class EditRecieverScreen extends BaseActivity implements OnClickListener {
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
//    private String userIdreciever;
//    private boolean isPicChooserCreated;
//    private ImageView tosetPicOnImageView;
//    private View imageSelectorView;
//    private EditText addressEdit;
//
//    private static final int SELECT_PICTURE_FROM_GALLERY = 101;
//    private static final int SELECT_PICTURE_FROM_CAMERA = 102;
//
//    public static boolean IS_UPDATED;
//    private Bitmap bitmapChanged;
//    private boolean isBitmapchanged;
//    private String address;
//    private String emailPreviouus;
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
//        setContentView(R.layout.edit_reciever);
//        findViewById(R.id.editBack).setOnClickListener(this);
//        findViewById(R.id.editDone).setOnClickListener(this);
//        findViewById(R.id.imageViewPicEdit).setOnClickListener(this);
//
//        nickNameEdit = ((EditText) findViewById(R.id.Editnickname));
//        firstNameEdit = ((EditText) findViewById(R.id.Editfirstname));
//        lastNameEdit = ((EditText) findViewById(R.id.EditlastName));
//        emailEdit = (EditText) findViewById(R.id.EditEmail);
//        phoneEdit = (EditText) findViewById(R.id.EditPhone);
//        addressEdit = (EditText) findViewById(R.id.EditAddress);
//        findViewById(R.id.layoutIamOK).setOnClickListener(this);
//
//        initializemTempFile();
//
//        String nickName = "";
//        String firstName = "";
//        String lastName = "";
//        emailPreviouus = "";
//        String phone = "";
//        String address = "";
//
//        try {
//            nickName = RecievingListScreen.jsonobjectToChange.getString(TAG_NICKNAME);
//            firstName = RecievingListScreen.jsonobjectToChange.getString(TAG_FIRST_NAME);
//            lastName = RecievingListScreen.jsonobjectToChange.getString(TAG_LAST_NAME);
//            emailPreviouus = RecievingListScreen.jsonobjectToChange.getString(TAG_EMAIL);
//            phone = RecievingListScreen.jsonobjectToChange.getString(TAG_PHONE);
//            address = RecievingListScreen.jsonobjectToChange.getString(TAG_ADDRESS);
//            userIdreciever = RecievingListScreen.jsonobjectToChange.getString(USERID_Reciever);
//        } catch (JSONException e) {
//            e.printStackTrace();
//        }
//
//        nickNameEdit.setText(nickName);
//        firstNameEdit.setText(firstName);
//        lastNameEdit.setText(lastName);
//        emailEdit.setText(emailPreviouus);
//        phoneEdit.setText(phone);
//        addressEdit.setText(address);
//
//        findViewById(R.id.buttonContactImage).setSelected(true);
//        findViewById(R.id.buttonAlerts).setSelected(false);
//        findViewById(R.id.buttonSetting).setSelected(false);
//
//        findViewById(R.id.layoutAlert).setOnClickListener(this);
//        findViewById(R.id.layoutContact).setOnClickListener(this);
//        findViewById(R.id.layoutSettings).setOnClickListener(this);
//        findViewById(R.id.layoutIamOK).setOnClickListener(this);
//
//        int alertCount = Preference.getInstance().mSharedPreferences.getInt(C.ALERT_COUNT, 0);
//        ((TextView) findViewById(R.id.alertCount)).setText("" + alertCount);
//
//        ImageView imageView = (ImageView) findViewById(R.id.imageViewPicEdit);
//        Bitmap imageFromStorage = BitMapHelper.loadImageFromStorage(this, "" + emailPreviouus, Preference.getInstance().mSharedPreferences.getString(emailPreviouus, ""));
//        bitmapChanged = BitMapHelper.loadImageFromStorage(this, "" + emailPreviouus, Preference.getInstance().mSharedPreferences.getString(emailPreviouus, ""));
//        if (imageFromStorage == null) {
//            imageView.setBackgroundDrawable(getResources().getDrawable(R.drawable.pf_pic));
//        } else {
//            Bitmap circledBitmap = createScaleddBitmapFromFile(imageFromStorage);
//            imageView.setBackgroundDrawable(new BitmapDrawable(circledBitmap));
//        }
//    }
//
//    @Override
//    public void onClick(View v) {
//
//        switch (v.getId()) {
//
//            case R.id.imageViewPicEdit:
//                hideSoftKeyboard();
//                if (isPicChooserCreated) {
//                    return;
//                }
//                tosetPicOnImageView = (ImageView) v;
//                addPicchooserView(v);
//                break;
//
//            case R.id.editDone:
//                hideSoftKeyboard();
//                checkToUpdate();
//                break;
//
//            case R.id.editBack:
//                finish();
//                break;
//
//            case R.id.layoutContact:
//                findViewById(R.id.buttonContactImage).setSelected(true);
//                findViewById(R.id.buttonAlerts).setSelected(false);
//                findViewById(R.id.buttonSetting).setSelected(false);
//                finish();
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
//                finish();
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
//                removeImagePickedDialog(v);
//                break;
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
//
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
//    private void addPicchooserView(View view) {
//
//        ViewGroup contacts = (ViewGroup) findViewById(R.id.imagePickerLayoutEditReciever);
//        View childContact = LayoutInflater.from(getApplicationContext()).inflate(R.layout.footer_image, null);
//        imageSelectorView = childContact;
//        childContact.findViewById(R.id.btncamera).setOnClickListener(this);
//        Button button = (Button) childContact.findViewById(R.id.btnGallery);
//        button.setOnClickListener(this);
//        childContact.findViewById(R.id.btncancel).setOnClickListener(this);
//        contacts.addView(childContact);
//        isPicChooserCreated = true;
//
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
//        {
//            ((ViewGroup) imageSelectorView.getParent()).removeView(imageSelectorView);
//            Intent photoPickerIntent = new Intent(Intent.ACTION_PICK);
//            photoPickerIntent.setType("image/*");
//            startActivityForResult(photoPickerIntent, SELECT_PICTURE_FROM_GALLERY);
//        }
//    }
//
//    private void removeImagePickedDialog(View view) {
//        isPicChooserCreated = false;
//        View parent = (View) view.getParent();
//        ((ViewGroup) parent.getParent()).removeView(parent);
//    }
//
//    private void startSettingScreen() {
//        Intent i = new Intent(this, SettingScreen.class);
//        startActivity(i);
//    }
//
//    /**
//     * Please note that profile picture of this user is mapped with its email
//     * ID, hanse if you have changed the email id picture will not be associated
//     * with that
//     */
//    private void checkToUpdate() {
//        nickName = nickNameEdit.getText().toString();
//        firstName = firstNameEdit.getText().toString();
//        lastName = lastNameEdit.getText().toString();
//        email = emailEdit.getText().toString();
//        phone = phoneEdit.getText().toString();
//        address = addressEdit.getText().toString();
//
//        if (isNull(firstName)) {
//            Utils.showToast(this, "Please enter your first name...!!");
//            requestFocus(firstNameEdit);
//            return;
//        }
//
//        if (isNull(lastName)) {
//            Utils.showToast(this, "Please enter your last name...!!");
//            requestFocus(lastNameEdit);
//            return;
//        }
//
//        if (isNull(email)) {
//            Utils.showToast(this, "Please enter your email id...!!");
//            requestFocus(emailEdit);
//            return;
//        }
//
//        if (isNull(address.trim())) {
//            Utils.showToast(this, "Please enter your address...!!");
//            requestFocus(addressEdit);
//            return;
//        }
//        if (isBitmapchanged || bitmapChanged != null) {
//            BitMapHelper.deleteImageFromStorage(this, "" + emailPreviouus, Preference.getInstance().mSharedPreferences.getString(emailPreviouus, ""));
//            String bitmappath = BitMapHelper.saveImageAndGetPath(bitmapChanged, this, email);
//            Preference.getInstance().savePreferenceData(email, bitmappath);
//        }
//
//        if (!Utils.isValidEmail(email.trim())) {
//            emailEdit.setError("Please enter correct email ID.");
//            hideSoftKeyboard();
//            return;
//        }
//
//        if (email.trim().equals(Preference.getInstance().mSharedPreferences.getString(C.KEY_EMAIL, ""))) {
//            Utils.showToast(this, "Sorry, You can not add yourself in alert contacts.");
//            return;
//        }
//        new UpdateTask().execute();
//    }
//
//    private void requestFocus(View view) {
//        InputMethodManager mgr = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
//        mgr.showSoftInput(view, InputMethodManager.SHOW_IMPLICIT);
//    }
//
//    private boolean isNull(String value) {
//        return value.trim().matches("");
//    }
//
//    private class UpdateTask extends AsyncTask<String, Void, String> {
//
//        private static final String EDITED_SUCCESSFULLY = "Contact has been edited successfully.";
//        private static final String KEY_SUCCESS = "success";
//        private int response;
//        private WsCallUpdateReceiver wsCallUpdateReceiver;
//
//        @Override
//        protected void onPreExecute() {
//            super.onPreExecute();
//            wsCallUpdateReceiver = new WsCallUpdateReceiver(EditRecieverScreen.this);
//        }
//
//        @Override
//        protected String doInBackground(String... params) {
//
//            if (Utils.isInternetConnected(EditRecieverScreen.this)) {
//                wsCallUpdateReceiver.executeService(userIdreciever, firstName, lastName, nickName, email, phone, address);
//                if (wsCallUpdateReceiver.isSuccess()) {
//                    return KEY_SUCCESS;
//                } else {
//                    response = 2;
//                    return wsCallUpdateReceiver.getMessage();
//                }
//            } else {
//                response = 1;
//            }
//            return "fail";
//        }
//
//        @Override
//        protected void onPostExecute(String result) {
//            switch (response) {
//                case 2:
//                    Toast.makeText(EditRecieverScreen.this, "" + result, Toast.LENGTH_SHORT).show();
//                    break;
//
//                case 1:
//                    Toast.makeText(EditRecieverScreen.this, "Email or password is incorrect.", Toast.LENGTH_SHORT).show();
//                    break;
//
//                default:
//                    if (result.equals("fail")) {
//                        Toast.makeText(EditRecieverScreen.this, Utils.SERVER_ERROR, Toast.LENGTH_SHORT).show();
//                    } else {
//                        Toast.makeText(EditRecieverScreen.this, EDITED_SUCCESSFULLY, Toast.LENGTH_SHORT).show();
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
//
//            if (requestCode == Utils.REQUEST_CODE_CROP_IMAGE) {
//                String path = data.getStringExtra(CropImage.IMAGE_PATH);
//                if (path == null) {
//                    return;
//                }
//
//                Bitmap bitmap = BitmapFactory.decodeFile(Utils.mFileTemp.getPath());
//                bitmapChanged = BitmapFactory.decodeFile(Utils.mFileTemp.getPath());
//                if (bitmapChanged == null) {
//                    return;
//                }
//                Bitmap croppedBitmap = BitMapHelper.getCircleBitmap(bitmap);
//                isBitmapchanged = true;
//                tosetPicOnImageView.setImageBitmap(croppedBitmap);
//
//            }
//        }
//    }
//
//    private void toSetAnImageFromCamera(Intent data) {
//        Bundle extras = data.getExtras();
//        if (extras != null) {
//            Bitmap photo = extras.getParcelable("data");
//
//            Bitmap resizedBitmap;
//            if (photo.getWidth() >= photo.getHeight()) {
//                resizedBitmap = Bitmap.createBitmap(photo, photo.getWidth() / 2 - photo.getHeight() / 2, 0, photo.getHeight(), photo.getHeight());
//            } else {
//                resizedBitmap = Bitmap.createBitmap(photo, 0, photo.getHeight() / 2 - photo.getWidth() / 2, photo.getWidth(), photo.getWidth());
//            }
//            isBitmapchanged = true;
//            bitmapChanged = resizedBitmap;
//            Bitmap circledBitmap = createScaleddBitmapFromFile(resizedBitmap);
//            tosetPicOnImageView.setImageBitmap(circledBitmap);
//
//        }
//    }
//
//    private Bitmap createScaleddBitmapFromFile(Bitmap bitmap) {
//        Bitmap scaledBitmap = Bitmap.createScaledBitmap(bitmap, bitmap.getWidth(), bitmap.getHeight(), true);
//        Bitmap croppedBitmap = BitMapHelper.getCircleBitmap(scaledBitmap);
//        return croppedBitmap;
//    }
//
//
//    @Override
//    public void onResume() {
//        super.onResume();
//        CheckForeground.onResume(EditRecieverScreen.this);
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
//    public void onSaveInstanceState(Bundle outState) {
//        // TODO Auto-generated method stub
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
