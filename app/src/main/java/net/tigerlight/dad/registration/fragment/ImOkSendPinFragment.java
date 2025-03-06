package net.tigerlight.dad.registration.fragment;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewFlipper;

import com.net.tigerlight.dad.R;

/**
 * Created by indianic on 24/10/16.
 */

public class ImOkSendPinFragment extends Fragment implements View.OnClickListener {

    private View view;
    private EditText etPin;
    private TextView tvSend;
    private ViewFlipper mViewFlipper;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
//        return super.onCreateView(inflater, container, savedInstanceState);


        view = inflater.inflate(R.layout.fragment_i_am_ok_send_pin, container, false);
        return view;
    }


//    @Override
//    public void initView(View view) {
//
//        etPin = (EditText) view.findViewById(R.id.fragment_i_m_ok_send_pin_et_pin);
//        tvSend = (TextView) view.findViewById(R.id.fragment_i_m_ok_send_pin_tv_send);
//        tvSend.setOnClickListener(this);
//
//    }
//
//    @Override
//    public void trackScreen() {
//
//    }
//
//    @Override
//    public void initActionBar() {
//    }


    @Override
    public void onClick(View v) {
        final int fragmentId = v.getId();
        if (fragmentId == R.id.fragment_i_m_ok_send_pin_tv_send) {
            Toast.makeText(getActivity(), "Not Implemented Yet", Toast.LENGTH_SHORT).show();
        }
    }
}
