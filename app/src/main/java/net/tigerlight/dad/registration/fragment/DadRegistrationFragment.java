package net.tigerlight.dad.registration.fragment;

import com.net.tigerlight.dad.R;
import net.tigerlight.dad.home.BaseFragment;
import net.tigerlight.dad.registration.activity.DadMainActivity;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import java.util.Locale;

/**
 * RegistartionFragment : user can register or login
 */
public class DadRegistrationFragment extends BaseFragment {


    private static final String TAG = DadRegistrationFragment.class.getSimpleName();

    private ActivityResultLauncher<String[]> locationPermissionLauncher;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        locationPermissionLauncher = registerForActivityResult(
                new ActivityResultContracts.RequestMultiplePermissions(),
                permissions -> {
                    var isGranted = false;
                    for (int i = 0; i < permissions.size(); i++) {
                        final var coarseLocationPermissionGranted = permissions.getOrDefault(Manifest.permission.ACCESS_COARSE_LOCATION, false);
                        final var fineLocationPermissionGranted = permissions.getOrDefault(Manifest.permission.ACCESS_FINE_LOCATION, false);
                        isGranted = Boolean.TRUE.equals(coarseLocationPermissionGranted) && Boolean.TRUE.equals(fineLocationPermissionGranted);
                    }
                });
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_registration_new, container, false);
    }

    @Override
    public void initView(View view) {
        final Button tvCreateAccount = (Button) view.findViewById(R.id.fragment_registration_tv_create_account);
        final Button tvLoginToYourAccount = (Button) view.findViewById(R.id.fragment_registration_tv_login_to_your_account);
        final TextView tvShowEula = (TextView) view.findViewById(R.id.fragment_registration_tv_show_eula);
        tvCreateAccount.setOnClickListener(this);
        tvLoginToYourAccount.setOnClickListener(this);
        tvShowEula.setOnClickListener(this);

        locationPermissionLauncher.launch(new String[] {Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION});

        final TextView tvBuildVersion = (TextView) view.findViewById(R.id.fragment_registration_tv_build_version);
        try {
            Context context = getContext();
            Activity activity = getActivity();
            if (activity != null && context != null) {
                PackageInfo packageInfo = activity.getPackageManager().getPackageInfo(activity.getPackageName(), 0);

                tvBuildVersion.setText(String.format(Locale.US, getString(R.string.build_no), packageInfo.versionCode, packageInfo.versionName));
                activity.getWindow().setStatusBarColor(ContextCompat.getColor(context, R.color.colorBlack));
            }
        }
        catch (PackageManager.NameNotFoundException e) {
            Log.e(TAG, "Package Not found." + e.getMessage());
        }
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
        if (getActivity() instanceof DadMainActivity) {
            // Only call replaceFragment if parent is MainActivity
            if (fragmentId == R.id.fragment_registration_tv_create_account) {
                ((DadMainActivity) getActivity()).addFragment(new DadCreateAccountFragment(), DadRegistrationFragment.this);
            } else if (fragmentId == R.id.fragment_registration_tv_login_to_your_account) {
                ((DadMainActivity) getActivity()).addFragment(new DadLoginToYourAccountFragment(), DadRegistrationFragment.this);
            } else if (fragmentId == R.id.fragment_registration_tv_show_eula) {
                ((DadMainActivity) getActivity()).addFragment(new DADLicenseFragment(), DadRegistrationFragment.this);
            }
        }
    }

}
