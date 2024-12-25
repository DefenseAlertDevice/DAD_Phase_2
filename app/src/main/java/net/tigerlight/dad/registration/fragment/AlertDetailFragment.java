package net.tigerlight.dad.registration.fragment;

import net.tigerlight.dad.registration.activity.MainActivity;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;

import net.tigerlight.dad.R;
import net.tigerlight.dad.home.BaseFragment;
import net.tigerlight.dad.registration.model.CountryModel;
import net.tigerlight.dad.registration.util.Constant;
import net.tigerlight.dad.registration.util.Utills;
import net.tigerlight.dad.sqlite.SqlLiteDbHelper;
import net.tigerlight.dad.util.CircleTransform;
import net.tigerlight.dad.util.Preference;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;

import androidx.core.content.ContextCompat;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentTransaction;

import android.util.Log;
import android.view.GestureDetector;
import android.view.GestureDetector.OnGestureListener;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.Calendar;
import java.util.Locale;
import java.util.TimeZone;

import static net.tigerlight.dad.R.id.fragment_alert_detail_tvDial911;
import static net.tigerlight.dad.R.id.fragment_alert_detail_tvUserAddress;
import static net.tigerlight.dad.registration.fragment.AlertFragment.jsonobjectToChange;
import static net.tigerlight.dad.util.WsConstants.ASSETS_DOMAIN;

public class AlertDetailFragment extends BaseFragment implements OnClickListener, OnGestureListener, OnMapReadyCallback {

    private static final String MAP_NOT_AVAILABLE = "Google map not available. Please check some time later fot map availability.";
    private static final String GPS_SERVICE_UNAVAILABLE = "Google play services not available. You need to log in first, to use any of google play services.";
    public static final String TAG_IMAGE = "image";
    private View layout;
    private GestureDetector gestureDetector;
    private boolean isInvisible;
    Double longitude = 0.00;
    Double latitude = 0.00;
    private ImageView arrowImageView;
    private String timezoneID;
    private final String TAG_ALERT_TYPE = "alertType";
    private final String TAG_STATUS = "status";
    private final String TAG_LONG = "longitude";
    private final String TAG_LATITUDE = "latitude";
    private final String TAG_USER_NAME = "username";
    public static final String TAG_ADDRESS = "address";
    private static final String TAG_DATE_TIME = "datetime";
    private Button go_to_googlemap;
    Marker myMarker;
    private LatLng latLongPos;
    private LinearLayout llOkAlert;
    private LinearLayout llRedAlert;
    private LinearLayout llOrangeAlert;
    private LinearLayout llTestAlert;
    private LinearLayout flMapContainer;
    private TextView tvStatus;
    private TextView tvDial;
    private TextView tvBackAlerts;
    private TextView tvTitle;
    private TextView tvUserName;
    private TextView tvUserAddress;
    private TextView imgUserProfile;
    private ImageView fragment_alert_detail_llOkAlert_img;
    private ImageView fragment_alert_detail_img_redalert;
    private ImageView fragment_alert_detail_img_testalert;
    public String imagePath = "";
    String testStr;
    String imgUrl = ASSETS_DOMAIN;

    SqlLiteDbHelper dbHelper;
    CountryModel contacts;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_alert_detail, container, false);
    }

    private boolean checkPlayServices() {
        int googlePlayServicesAvailable = GooglePlayServicesUtil.isGooglePlayServicesAvailable(getActivity());
        if (googlePlayServicesAvailable == ConnectionResult.SUCCESS) {
            return true;
        }
        return false;
    }

    private void showDialog(String msg) {

        final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setMessage(msg).setCancelable(false).setPositiveButton(getString(R.string.TAG_OK), new DialogInterface.OnClickListener() {
            public void onClick(final DialogInterface dialog, final int id) {
                dialog.dismiss();
                getActivity().finish();
            }
        });
        final AlertDialog alert = builder.create();
        alert.show();
    }

    private void setDetails(View view) {
        final TextView tvTitle = (TextView) view.findViewById(R.id.fragment_alert_detail_tvTitle);
        final TextView tvUserAddress = (TextView) view.findViewById(fragment_alert_detail_tvUserAddress);
        final TextView tvUsername = (TextView) view.findViewById(R.id.fragment_alert_detail_tvUserName);
        final TextView tvUserDateTime = (TextView) view.findViewById(R.id.fragment_alert_detail_tvDateTime);
        final ImageView imgUserprofile = (ImageView) view.findViewById(R.id.fragment_alert_detail_ivUserProfile);
        imgUserprofile.setOnClickListener(view1 -> showDialog());

        //TODO:  Band-aid (per Rod) for unknown NPE
        final String address = (jsonobjectToChange != null) ? jsonobjectToChange.optString(TAG_ADDRESS) : "";
        tvUserAddress.setText(address);

        //TODO:  Band-aid (per Rod) for unknown NPE
        final String dateTime = (jsonobjectToChange != null) ? jsonobjectToChange.optString(TAG_DATE_TIME) : "";
        tvUserDateTime.setText(dateTime);

        //TODO:  Band-aid (per Rod) for unknown NPE
        final String userName = (jsonobjectToChange != null) ? jsonobjectToChange.optString(TAG_USER_NAME) : "";
        tvTitle.setText(userName);


        if (jsonobjectToChange != null && jsonobjectToChange.optInt("status") == 1) {

            tvUsername.setText(String.format("%s " + getString(R.string.is_safe), userName));
        } else {
//            tvUsername.setText("#f60101");
            tvUsername.setText(String.format("%s " + getString(R.string.is_danger), userName));
            tvUsername.setBackgroundColor(ContextCompat.getColor(getActivity(), R.color.color_allert_bg));
        }

        if (jsonobjectToChange != null) {
            imagePath = jsonobjectToChange.optString(TAG_IMAGE);
        }
        String lastWord = imagePath.substring(imagePath.lastIndexOf("/") + 1);
        ;
        imagePath = imgUrl + lastWord;
        Glide.with(this)
                .load(imagePath).diskCacheStrategy(DiskCacheStrategy.NONE)
                .skipMemoryCache(true).transform(new CircleTransform(getActivity())) // Uri of the picture
                .placeholder(R.drawable.pf_pic)
                .into(imgUserprofile);

    }

    @Override
    public void initView(View view) {

        dbHelper = new SqlLiteDbHelper(getActivity());
        dbHelper.openDataBase();
        contacts = new CountryModel();
        String CC = "";

        final Preference preference = Preference.getInstance();
        if (!preference.mSharedPreferences.getString(Constant.COMMON_LATITUDE, "").equals("") && !preference.mSharedPreferences.getString(Constant.COMMON_LONGITUDE, "").equals("")) {
            CC = Utills.getCountryName(getActivity(), preference.mSharedPreferences.getString(Constant.COMMON_LATITUDE, ""), preference.mSharedPreferences.getString(Constant.COMMON_LONGITUDE, ""));
        }
        contacts = dbHelper.Get_ContactDetails(CC);
        layout = view.findViewById(R.id.fragment_alert_detail_llHeader);
        tvStatus = (TextView) view.findViewById(R.id.fragment_alert_detail_tvStatus);
        tvDial = (TextView) view.findViewById(R.id.fragment_alert_detail_tvDial911);

        if (contacts != null) {

            Log.d("data", "C C=" + contacts.getC_c() + "Name=" + contacts.getC_name() + "e_no=" + contacts.getC_e_no());
            tvDial.setText("Dial " + contacts.getC_e_no());
        }

        tvBackAlerts = (TextView) view.findViewById(R.id.fragment_alert_detail_tvBackAlerts);
        llOkAlert = (LinearLayout) view.findViewById(R.id.fragment_alert_detail_llOkAlert);
        llRedAlert = (LinearLayout) view.findViewById(R.id.fragment_alert_detail_llRedAlert);
        llOrangeAlert = (LinearLayout) view.findViewById(R.id.fragment_alert_detail_llOrangeAlert);
        llTestAlert = (LinearLayout) view.findViewById(R.id.fragment_alert_detail_llTestAlert);
        flMapContainer = (LinearLayout) view.findViewById(R.id.fragment_alert_detail_flMapContainer);
        fragment_alert_detail_llOkAlert_img = (ImageView) view.findViewById(R.id.fragment_alert_detail_llOkAlert_img);
        fragment_alert_detail_img_redalert = (ImageView) view.findViewById(R.id.fragment_alert_detail_img_redalert);
        fragment_alert_detail_img_testalert = (ImageView) view.findViewById(R.id.fragment_alert_detail_img_testalert);
        gestureDetector = new GestureDetector(this);
        tvDial.setOnClickListener(this);

        final Bundle bundle = getArguments();
        if (bundle != null) {
            try {
                String jsonObject = bundle.getString(Constant.JSON_OBJECT);
                JSONObject jsonobjectToChange = new JSONObject(jsonObject);

                if (jsonobjectToChange.optInt(TAG_STATUS) == 1 || jsonobjectToChange.optString(TAG_STATUS).trim().equalsIgnoreCase("1")) {
                    llOkAlert.setVisibility(View.VISIBLE);
                    llRedAlert.setVisibility(View.GONE);
                    llOrangeAlert.setVisibility(View.GONE);
                    llTestAlert.setVisibility(View.GONE);
                    fragment_alert_detail_llOkAlert_img.setBackground(ContextCompat.getDrawable(getActivity(), R.drawable.ok_alerts));
//                    tvStatus.setText(getString(R.string.ok_ok));
                    tvStatus.setTextColor(ContextCompat.getColor(getActivity(), R.color.color_alert_green));

//                    tvStatus.setTextColor(ContextCompat.getColor(getActivity(), R.color.color_alert_green));

                } else {


//                    fragment_alert_detail_img_redalert.setBackground(ContextCompat.getDrawable(getActivity(), R.drawable.danger_alerts));
//                    tvStatus.setText(getString(R.string.danger));

//                    tvStatus.setTextColor(ContextCompat.getColor(getActivity(), R.color.color_alert_red));
                    if (jsonobjectToChange.optInt(TAG_ALERT_TYPE) == 0 || jsonobjectToChange.optString(TAG_ALERT_TYPE).trim().equalsIgnoreCase("0")) {
                        llOkAlert.setVisibility(View.GONE);
                        llRedAlert.setVisibility(View.VISIBLE);
                        fragment_alert_detail_img_redalert.setBackground(ContextCompat.getDrawable(getActivity(), R.drawable.danger_alerts));
                        llTestAlert.setVisibility(View.GONE);
                        llOrangeAlert.setVisibility(View.GONE);

                    } else if (jsonobjectToChange.optInt(TAG_ALERT_TYPE) == 1 || jsonobjectToChange.optString(TAG_ALERT_TYPE).trim().equalsIgnoreCase("1")) {
                        llOkAlert.setVisibility(View.GONE);
                        llRedAlert.setVisibility(View.VISIBLE);
                        fragment_alert_detail_img_redalert.setBackground(ContextCompat.getDrawable(getActivity(), R.drawable.crowd_alerts));
                        llTestAlert.setVisibility(View.GONE);
                        llOrangeAlert.setVisibility(View.GONE);

                    } else if (jsonobjectToChange.optInt(TAG_ALERT_TYPE) == 2 || jsonobjectToChange.optString(TAG_ALERT_TYPE).trim().equalsIgnoreCase("2")) {
                        llOkAlert.setVisibility(View.VISIBLE);
                        fragment_alert_detail_llOkAlert_img.setBackground(ContextCompat.getDrawable(getActivity(), R.drawable.ok_alerts));
                        llRedAlert.setVisibility(View.GONE);
                        llTestAlert.setVisibility(View.GONE);
                        llOrangeAlert.setVisibility(View.GONE);

                    } else if (jsonobjectToChange.optInt(TAG_ALERT_TYPE) == 3 || jsonobjectToChange.optString(TAG_ALERT_TYPE).trim().equalsIgnoreCase("3") || jsonobjectToChange.optInt(TAG_ALERT_TYPE) == 4 || jsonobjectToChange.optString(TAG_ALERT_TYPE).trim().equalsIgnoreCase("4")) {
                        fragment_alert_detail_img_testalert.setBackground(ContextCompat.getDrawable(getActivity(), R.drawable.test_alerts));
//                        tvStatus.setText(getString(R.string.test_test));
                        tvStatus.setTextColor(ContextCompat.getColor(getActivity(), R.color.color_alert_blue));
                        llTestAlert.setVisibility(View.VISIBLE);
                        llOkAlert.setVisibility(View.GONE);
                        llRedAlert.setVisibility(View.GONE);
                        llOrangeAlert.setVisibility(View.GONE);

                    }
                }

            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        Calendar cal = Calendar.getInstance();
        TimeZone tz = cal.getTimeZone();
        timezoneID = tz.getID();

        setDetails(view);
        if (!checkPlayServices()) {
            showDialog(getString(R.string.TAG_GPS_NA));
            return;
        }

        final SupportMapFragment mapFragment = SupportMapFragment.newInstance();
        FragmentTransaction fragmentTransaction = getChildFragmentManager().beginTransaction();
        fragmentTransaction.add(R.id.fragment_alert_detail_flMapContainer, mapFragment);
        fragmentTransaction.commit();
        mapFragment.getMapAsync(this);
//        if (googleMap == null) {
//            showDialog(MAP_NOT_AVAILABLE);
//            return;
//        }

        tvBackAlerts.setOnClickListener(this);


    }

    @Override
    public void trackScreen() {

    }

    @Override
    public void initActionBar() {

    }


    @Override
    public void onClick(View v) {
        final int fragmentId = v.getId();
        if (fragmentId == fragment_alert_detail_tvDial911) {
            final Dialog dialog = new Dialog(getActivity(), R.style.AppDialogTheme);
            dialog.setContentView(R.layout.custom_dialog);

            final TextView tvTitle = (TextView) dialog.findViewById(R.id.dialog_tvTitle);
            final TextView tvMessage = (TextView) dialog.findViewById(R.id.dialog_tvMessage);
            final TextView tvPosButton = (TextView) dialog.findViewById(R.id.dialog_tvPosButton);
            final TextView tvNegButton = (TextView) dialog.findViewById(R.id.dialog_tvNegButton);
            tvTitle.setText(getString(R.string.dialog_dial_title));
            tvMessage.setText(getString(R.string.dialog_dial_msg) + jsonobjectToChange.optString(TAG_USER_NAME) + ". " + getString(R.string.located_at) + " " + jsonobjectToChange.optString(TAG_ADDRESS) + ".");
            tvPosButton.setText(getString(R.string.dialog_dial_pos_button));
            tvNegButton.setText(getString(R.string.fragment_create_account_tv_cancel));

            tvPosButton.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View view) {
                    dialog.dismiss();

                    Intent callIntent = new Intent(Intent.ACTION_CALL);
                    callIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_NO_USER_ACTION);


                    if (Preference.getInstance().mSharedPreferences.getString(Constant.C_CODE, "").equals("US")) {

                        callIntent.setData(Uri.parse("tel:" + 911));
                        startActivity(callIntent);
                    } else if (Preference.getInstance().mSharedPreferences.getString(Constant.C_CODE, "").equals("FR")) {
                        callIntent.setData(Uri.parse("tel:" + 112));
                        startActivity(callIntent);

                    } else {
                        callIntent.setData(Uri.parse("tel:" + 112));
                        startActivity(callIntent);
                    }

                }
            });
            tvNegButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    dialog.dismiss();
                }
            });
            dialog.show();
        } else if (fragmentId == R.id.fragment_alert_detail_tvBackAlerts) {
            if (requireActivity().getSupportFragmentManager().getBackStackEntryCount() > 0) {
                requireActivity().getSupportFragmentManager().popBackStack();
            } else {
                // Open AlertFragment directly
                MainActivity activity = (MainActivity) getActivity();
                DashBoardWithSwipableFragment fragment = new DashBoardWithSwipableFragment();
                activity.replaceFragment(fragment);
            }
        }
    }

    private void startAlertListScreen() {
        //Intent i = new Intent(this, AlertFragment.class);
        //startActivity(i);
    }

    private void startSettingScreen() {
        //Intent i = new Intent(this, AlertFragment.class);
        //startActivity(i);
    }

//    @Override
//    public boolean onTouchEvent(MotionEvent event) {
//        return gestureDetector.onTouchEvent(event);
//    }

    void showDialog() {
        MyDialogFragment newFragment = MyDialogFragment.newInstance();
        // Use the FragmentManager from the parent activity to show the DialogFragment
        newFragment.show(requireActivity().getSupportFragmentManager(), "dialog");
    }

    @Override
    public boolean onDown(MotionEvent e) {
        return true;
    }

    @Override
    public void onShowPress(MotionEvent e) {
        // TODO Auto-generated method stub

    }

    @Override
    public boolean onSingleTapUp(MotionEvent e) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
        if (e2.getY() - e1.getY() > 20) {
            // scroll down
            if (isInvisible) {
                isInvisible = false;
                //arrowImageView.setBackgroundResource(R.drawable.up_arw);
                layout.setVisibility(View.VISIBLE);
            }
        } else if (e1.getY() - e2.getY() > 20) {
            // scroll up
            if (!isInvisible) {
                isInvisible = true;
                //arrowImageView.setBackgroundResource(R.drawable.down_arw);
                layout.setVisibility(View.GONE);
            }
        }
        return false;
    }

    @Override
    public void onLongPress(MotionEvent e) {
        // TODO Auto-generated method stub

    }

    @Override
    public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {

        String userName = null;
        String userAdderss = null;

        try {
            if (jsonobjectToChange != null) {
                latitude = Double.valueOf(jsonobjectToChange.optString(TAG_LATITUDE));
                longitude = Double.valueOf(jsonobjectToChange.optString(TAG_LONG));

//            String  cName= Utills.getCountryName(getActivity(),   48.8588377, 2.2775171);
//            Preference.getInstance().savePreferenceData(Constant.COUNTRY_CODE,cName);

                userName = jsonobjectToChange.optString(TAG_USER_NAME);
                userAdderss = jsonobjectToChange.optString(TAG_ADDRESS);

            }
        } catch (NumberFormatException e) {
            e.printStackTrace();
            return;
        }
        latLongPos = new LatLng(latitude, longitude); // i have chnaged lat long pos , new LatLng(latitude, longitude); bcz values are coming inverse

        googleMap.setMyLocationEnabled(true);

        googleMap.clear();
        googleMap.addMarker(new MarkerOptions().title(userName).snippet(userAdderss).position(latLongPos).
                icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)));

        final CameraPosition cameraPosition = new CameraPosition.Builder()
                .target(new LatLng(latitude, longitude))      // i have chnaged lat long pos , new LatLng(latitude, longitude); bcz values are coming inverse // Sets the center of the map to location user
                .zoom(17)                   // Sets the zoom
                .bearing(90)                // Sets the orientation of the camera to east
                .tilt(40)                   // Sets the tilt of the camera to 30 degrees
                .build();                   // Creates a CameraPosition from the builder

        googleMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
        // googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLongPos, 13));

        googleMap.setOnMarkerClickListener(arg0 -> {
            String uri = String.format(Locale.ENGLISH, "geo:%f,%f", longitude, latitude);// i have chnaged lat long pos , new LatLng(latitude, longitude); bcz values are coming inverse
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(uri));
            startActivity(intent);
            return false;
        });
    }

    public static class MyDialogFragment extends DialogFragment {

        private ImageView ivProfile;

        static MyDialogFragment newInstance() {
            return new MyDialogFragment();
        }

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            Dialog dialog = super.onCreateDialog(savedInstanceState);
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
            dialog.setContentView(R.layout.fragment_dialog);
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.YELLOW));
            dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);

            ivProfile = dialog.findViewById(R.id.fragment_dialog_iv_profile);
            String imagePathPart = jsonobjectToChange.optString(TAG_IMAGE);

            Glide.with(this)
                    .load(imagePathPart)
                    .diskCacheStrategy(DiskCacheStrategy.NONE)
                    .fitCenter()
                    .skipMemoryCache(true)
                    .placeholder(R.drawable.pf_pic)
                    .into(ivProfile);

            return dialog;
        }
    }


}
