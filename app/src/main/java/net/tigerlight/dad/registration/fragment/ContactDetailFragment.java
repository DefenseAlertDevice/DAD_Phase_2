package net.tigerlight.dad.registration.fragment;

/**
 * Created on 10/11/16.
 */

public class ContactDetailFragment  {
//
//        private GPSTracker gpsTracker;
//    private String lattdLastKnown;
//    private String longtdLastKnown;
//    private final String TAG_FIRST_NAME = "firstname";
//    private final String TAG_LAST_NAME = "lastname";
//    private final String TAG_EMAIL = "email";
//    private final String TAG_PHONE = "phone";
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
//
//
//
//    @Override
//    public void initView(View view) {
//        view.findViewById(R.id.detailEdit).setOnClickListener(this);
//        view.findViewById(R.id.detailBack).setOnClickListener(this);
//        String firstName = "";
//        String lastName = "";
//        String email = "";
//        String phone = "";
//
//        firstName = ContactFragment.jsonobjectToChange.optString(TAG_FIRST_NAME);
//        lastName = ContactFragment.jsonobjectToChange.optString(TAG_LAST_NAME);
//        email = ContactFragment.jsonobjectToChange.optString(TAG_EMAIL);
//        phone = ContactFragment.jsonobjectToChange.optString(TAG_PHONE);
//
//        ((TextView) view.findViewById(R.id.detailname)).setText("" + firstName + " " + lastName);
//        ((TextView) view.findViewById(R.id.detailEmail)).setText("" + email);
//        ((TextView) view.findViewById(R.id.detailPhone)).setText("" + phone);
//
//        view.findViewById(R.id.buttonContactImage).setSelected(false);
//        view.findViewById(R.id.buttonAlerts).setSelected(false);
//        view.findViewById(R.id.buttonSetting).setSelected(false);
//
//        ImageView imageView = (ImageView) view.findViewById(R.id.imageViewPicDetails);
//        Bitmap imageFromStorage = BitMapHelper.loadImageFromStorage(getActivity(), "" + email, Preference.getInstance().mSharedPreferences.getString(email, ""));
//        if (imageFromStorage == null) {
//            imageView.setBackgroundDrawable(getResources().getDrawable(R.drawable.pf_pic));
//        } else {
//            imageView.setBackgroundDrawable(new BitmapDrawable(BitMapHelper.getCircleBitmap(imageFromStorage)));
//        }
//    }
//
//    @Override
//    public void trackScreen() {
//
//    }
//
//    @Override
//    public void initActionBar() {
//
//    }
}
