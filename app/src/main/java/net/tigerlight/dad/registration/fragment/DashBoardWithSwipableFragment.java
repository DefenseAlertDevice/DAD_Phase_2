package net.tigerlight.dad.registration.fragment;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import com.net.tigerlight.dad.R;
import net.tigerlight.dad.home.BaseFragment;
import net.tigerlight.dad.recievers.BLEHelper;
import net.tigerlight.dad.registration.adapter.ViewPagerAdapter;
import net.tigerlight.dad.registration.util.Constant;
import net.tigerlight.dad.registration.util.Utills;

import java.util.Calendar;
import java.util.TimeZone;

public class DashBoardWithSwipableFragment extends BaseFragment {

    private ViewPager2 viewPager;
    private BottomNavigationView bottomNavigationView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_dashboard_with_swipable, container, false);
    }

    @Override
    public void initView(View view) {
        viewPager = view.findViewById(R.id.viewpager);
        bottomNavigationView = view.findViewById(R.id.bottom_navigation);

        setupViewPager();
        setupBottomNavigation();
    }

    private void setupViewPager() {
        ViewPagerAdapter adapter = new ViewPagerAdapter(this);
        adapter.addFragment(new ContactFragment());
        adapter.addFragment(new AlertFragment());
        adapter.addFragment(new AmOkFragmentI());
        adapter.addFragment(new AccountFragment());

        viewPager.setAdapter(adapter);

        // Synchronize ViewPager and BottomNavigationView
        viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                bottomNavigationView.getMenu().getItem(position).setChecked(true);
            }
        });
    }

    @SuppressLint("NonConstantResourceId")
    private void setupBottomNavigation() {
        bottomNavigationView.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.menu_contact) {
                viewPager.setCurrentItem(0);
                return true;
            } else if (itemId == R.id.menu_alerts) {
                viewPager.setCurrentItem(1);
                return true;
            } else if (itemId == R.id.menu_im_ok) {
                viewPager.setCurrentItem(2);
                return true;
            } else if (itemId == R.id.menu_account) {
                viewPager.setCurrentItem(3);
                return true;
            }
            return false;
        });
    }

    @Override
    public void trackScreen() {}

    @Override
    public void initActionBar() {}

    public void updateCount() {

    }
}
