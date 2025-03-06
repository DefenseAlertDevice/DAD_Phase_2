package net.tigerlight.dad.registration.fragment;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.TextView;

import com.net.tigerlight.dad.R;
import net.tigerlight.dad.home.BaseFragment;
import net.tigerlight.dad.registration.activity.MainActivity;
import net.tigerlight.dad.registration.util.Constant;
import net.tigerlight.dad.registration.util.Utills;
import net.tigerlight.dad.util.Preference;

import java.util.Locale;

/**
 * Created on 25/11/16.
 */

public class DADLicenseFragment extends BaseFragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_dad_licence, container, false);
    }

    @SuppressLint("SetJavaScriptEnabled")
    @Override
    public void initView(View view) {
        TextView tvDoNotAccept = (TextView) view.findViewById(R.id.fragment_dad_license_tvDoNotAccept);
        TextView tvAccept = (TextView) view.findViewById(R.id.fragment_dad_license_tvAccept);
        final WebView webView = (WebView) view.findViewById(R.id.fragment_dad_license_wvTerms);
        webView.getSettings().setJavaScriptEnabled(true);

        webView.setWebViewClient(new WebViewClient());
        final Locale locale = getResources().getConfiguration().locale;
        if (locale.getLanguage().equals("ar")) {
            webView.loadUrl("file:///android_asset/termsandconditionsofusearbic.html");
        } else {
            webView.loadUrl("file:///android_asset/termsandconditionsofuse.html");
        }

        tvAccept.setOnClickListener(this);
        tvDoNotAccept.setOnClickListener(this);
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

        if (fragmentId == R.id.fragment_dad_license_tvDoNotAccept) {
            Utills.displayDialog(getActivity(), getString(R.string.dialog_eula_title), getString(R.string.dialog_eula_msg), getString(R.string.ok), "", false, false);
        } else if (fragmentId == R.id.fragment_dad_license_tvAccept) {
            Preference.getInstance().savePreferenceData(Constant.IS_ACCEPT, true);
            ((MainActivity) getActivity()).replaceFragment(new RegistartionFragment());
        }
    }
}
