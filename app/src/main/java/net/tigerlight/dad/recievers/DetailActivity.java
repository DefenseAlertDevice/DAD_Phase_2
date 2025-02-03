//package net.tigerlight.dad.recievers;
//
//import android.content.Intent;
//import android.graphics.Bitmap;
//import android.graphics.drawable.BitmapDrawable;
//import android.os.Bundle;
//import android.view.View;
//import android.view.View.OnClickListener;
//import android.widget.ImageView;
//import android.widget.TextView;
//import android.widget.Toast;
//
//import com.ftt.dad.R;
//import com.ftt.dad.activity.BaseActivity;
//import com.ftt.dad.beacon.AlertListScreen;
//import com.ftt.dad.settings.SettingScreen;
//import com.ftt.dad.util.Preference;
//import com.ftt.dad.utils.BitMapHelper;
//import com.ftt.dad.utils.C;
//import com.ftt.dad.utils.CheckForeground;
//import com.ftt.dad.utils.GPSTracker;
//import com.ftt.dad.utils.Utils;
//
//public class DetailActivity extends BaseActivity implements OnClickListener {
//
//    private GPSTracker gpsTracker;
//    private String lattdLastKnown;
//    private String longtdLastKnown;
//    private final String TAG_FIRST_NAME = "firstname";
//    private final String TAG_LAST_NAME = "lastname";
//    private final String TAG_EMAIL = "email";
//    private final String TAG_PHONE = "phone";
//
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        setContentView(R.layout.detail_layout);
//
//        findViewById(R.id.detailEdit).setOnClickListener(this);
//        findViewById(R.id.detailBack).setOnClickListener(this);
//        String firstName = "";
//        String lastName = "";
//        String email = "";
//        String phone = "";
//
//        firstName = RecievingListScreen.jsonobjectToChange.optString(TAG_FIRST_NAME);
//        lastName = RecievingListScreen.jsonobjectToChange.optString(TAG_LAST_NAME);
//        email = RecievingListScreen.jsonobjectToChange.optString(TAG_EMAIL);
//        phone = RecievingListScreen.jsonobjectToChange.optString(TAG_PHONE);
//
//        ((TextView) findViewById(R.id.detailname)).setText("" + firstName + " " + lastName);
//        ((TextView) findViewById(R.id.detailEmail)).setText("" + email);
//        ((TextView) findViewById(R.id.detailPhone)).setText("" + phone);
//
//        findViewById(R.id.buttonContactImage).setSelected(false);
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
//        ImageView imageView = (ImageView) findViewById(R.id.imageViewPicDetails);
//        Bitmap imageFromStorage = BitMapHelper.loadImageFromStorage(this, "" + email, Preference.getInstance().mSharedPreferences.getString(email, ""));
//        if (imageFromStorage == null) {
//            imageView.setBackgroundDrawable(getResources().getDrawable(R.drawable.pf_pic));
//        } else {
//            imageView.setBackgroundDrawable(new BitmapDrawable(BitMapHelper.getCircleBitmap(imageFromStorage)));
//        }
//    }
//
//    @Override
//    public void onClick(View v) {
//        switch (v.getId()) {
//            case R.id.detailEdit:
//                Intent i = new Intent(this, EditRecieverScreen.class);
//                startActivity(i);
//                finish();
//                break;
//
//            case R.id.detailBack:
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
//
//        }
//    }
//
//    private void startAlertListScreen() {
//        Intent i = new Intent(this, AlertListScreen.class);
//        startActivity(i);
//
//    }
//
//    private void startSettingScreen() {
//        Intent i = new Intent(this, SettingScreen.class);
//        startActivity(i);
//    }
//
//    @Override
//    public void onResume() {
//        super.onResume();
//        CheckForeground.onResume(DetailActivity.this);
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
//}
