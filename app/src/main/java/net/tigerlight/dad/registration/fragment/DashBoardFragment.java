//package net.tigerlight.dad.registration.fragment;
//
//import android.os.Bundle;
//import android.support.design.widget.TabLayout;
//import android.view.LayoutInflater;
//import android.view.View;
//import android.view.ViewGroup;
//
//import com.net.tigerlight.dad.R;
//import net.tigerlight.dad.home.BaseFragment;
//
///**
// * Created by indianic on 15/10/16.
// */
//
//public class DashBoardFragment extends BaseFragment {
//
//    private View view;
//    private TabLayout tabLayout;
//
//
//    @Override
//    public void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//
//
//    }
//
//    @Override
//    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
////        return super.onCreateView(inflater, container, savedInstanceState);
//
//        view = inflater.inflate(R.layout.fragment_dashborad, container, false);
//        return view;
//    }
//
//    @Override
//    public void initView(View view) {
//
//        tabLayout = (TabLayout) view.findViewById(R.id.fragment_dashboard_tabss);
//        tabLayout.addTab(tabLayout.newTab().setIcon(R.drawable.ic_contact_fade).setText(getString(R.string.dashbord_contact)));
//        tabLayout.addTab(tabLayout.newTab().setIcon(R.drawable.ic_contact_fade).setText(getString(R.string.dashbord_alert)));
//        tabLayout.addTab(tabLayout.newTab().setIcon(R.drawable.ic_contact_fade).setText(getString(R.string.dashbord_i_m_ok)));
//        tabLayout.addTab(tabLayout.newTab().setIcon(R.drawable.ic_contact_fade).setText(getString(R.string.dashbord_account)));
//        tabLayout.setSelectedTabIndicatorHeight(0);
////        tabLayout.setSelectedTabIndicatorColor(Color.parseColor("#FF5722"));
////        tabLayout.setSelectedTabIndicatorHeight((int) (5 * getResources().getDisplayMetrics().density));
////        tabLayout.setTabTextColors(Color.parseColor("#FF5722"), Color.parseColor("#FF5722"));
//
//
//        tabLayout.setOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
//            @Override
//            public void onTabSelected(TabLayout.Tab tab) {
//                if (tabLayout.getSelectedTabPosition() == 0) {
//                    getSupportFragmentManager().beginTransaction().replace(R.id.fragment_dashboard_fl_container, new ContactFragment(), ContactFragment.class.getSimpleName()).commit();
//                } else if (tabLayout.getSelectedTabPosition() == 1) {
//                    getSupportFragmentManager().beginTransaction().replace(R.id.fragment_dashboard_fl_container, new AlertFragment(), AlertFragment.class.getSimpleName()).commit();
//
//                } else if (tabLayout.getSelectedTabPosition() == 2) {
//                    getSupportFragmentManager().beginTransaction().replace(R.id.fragment_dashboard_fl_container, new ImOkFragment(), ImOkFragment.class.getSimpleName()).commit();
//                } else if (tabLayout.getSelectedTabPosition() == 3) {
//                    getSupportFragmentManager().beginTransaction().replace(R.id.fragment_dashboard_fl_container, new AccountFragment(), AccountFragment.class.getSimpleName()).commit();
//                }
//            }
//
//            @Override
//            public void onTabUnselected(TabLayout.Tab tab) {
//
//            }
//
//            @Override
//            public void onTabReselected(TabLayout.Tab tab) {
//
//            }
//        });
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
//
//    }
//}
