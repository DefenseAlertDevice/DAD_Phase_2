package net.tigerlight.dad.registration.fragment;

import android.app.DialogFragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.TextView;

import net.tigerlight.dad.R;

import java.util.Locale;

/**
 * TermAndConditionFragment : terms and conditions
 */
public class TermAndConditionFragment extends DialogFragment {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(DialogFragment.STYLE_NO_TITLE, R.style.AppDialogTheme);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_terms_and_condition, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initView(view);
    }

    private void initView(View view) {

        final TextView tvCancel = (TextView) view.findViewById(R.id.fragment_terms_and_condition_tv_cancel);
        //final TextView tvAccept = (TextView) view.findViewById(R.id.fragment_terms_and_condition_cb_accept);
        //final TextView tvNotAccept = (TextView) view.findViewById(R.id.fragment_terms_and_condition_cb_not_accept);
        final WebView webView = (WebView) view.findViewById(R.id.webView_tearms);
        webView.getSettings().setJavaScriptEnabled(true);

        webView.setWebViewClient(new WebViewClient());
        final Locale locale = getResources().getConfiguration().locale;
        if (locale.getLanguage().equals("en")) {
            webView.loadUrl("file:///android_asset/termsandconditionsofuse.html");
        }

        else if (locale.getLanguage().equals("da")) {

            webView.loadUrl("file:///android_asset/termsandconditionsofuse.html");
//            webView.loadUrl("file:///android_asset/termsandconditionsda.html");
        } else if (locale.getLanguage().equals("nb")) {

            webView.loadUrl("file:///android_asset/termsandconditionsofuse.html");
//            webView.loadUrl("file:///android_asset/termsandconditionnbe.html");
        } else {
            webView.loadUrl("file:///android_asset/termsandconditionsofuse.html");
//            webView.loadUrl("file:///android_asset/termsandconditionnbe.html");
        }

        tvCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dismiss();
            }
        });

//        tvAccept.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                final Preference preference = Preference.getInstance();
//                preference.savePreferenceData(Constant.IS_ACCEPT, true);
//                ((MainActivity) getActivity()).addFragment(new DashBoardWithSwipableFragment(), TermAndConditionFragment.this);
//            }
//
//        });
//
//        tvNotAccept.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                final Preference preference = Preference.getInstance();
//                preference.savePreferenceData(Constant.IS_ACCEPT, false);
//                getLocalFragmentManager().popBackStack();
//            }
//        });
    }

}
