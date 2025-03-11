package net.tigerlight.dad.registration.fragment;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import com.net.tigerlight.dad.R;
import net.tigerlight.dad.home.BaseFragment;
import net.tigerlight.dad.registration.adapter.DadViewPagerAdapter;

public class DadDashBoardWithSwipeableFragment extends BaseFragment {

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
        DadViewPagerAdapter adapter = new DadViewPagerAdapter(this);
        adapter.addFragment(new DadContactFragment());
        adapter.addFragment(new DadAlertFragment());
        adapter.addFragment(new DadAmOkFragmentI());
        adapter.addFragment(new DadAccountFragment());

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
