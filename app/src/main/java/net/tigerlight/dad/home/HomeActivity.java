package net.tigerlight.dad.home;

import android.app.AlertDialog;
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.DialogInterface;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;

public class HomeActivity extends AppCompatActivity {

//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_home);
//    }
//
//
//    @Override
//    public void onBackPressed() {
//        if (getLocalFragmentManager().getBackStackEntryCount() > 0) {
//            Util.getInstance().hideSoftKeyboard(this);
//            getLocalFragmentManager().popBackStack();
//        } else {
//            buildAlertMessageExit();
//        }
//    }
//
//    /**
//     * asks user for the confirmation before exiting of the app
//     */
//    private void buildAlertMessageExit() {
//        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
//        builder.setMessage("Are you sure want to exit?").setCancelable(false).setPositiveButton("Yes", new DialogInterface.OnClickListener() {
//            public void onClick(final DialogInterface dialog, final int id) {
//                callToFinish();
//            }
//        }).setNegativeButton("No", new DialogInterface.OnClickListener() {
//            public void onClick(final DialogInterface dialog, final int id) {
//                dialog.cancel();
//            }
//        });
//        final AlertDialog alert = builder.create();
//        alert.show();
//    }
//
//    private void callToFinish() {
//        super.finish();
//    }
//
//    /***
//     * Add new fragment in given container.
//     * <p>
//     * This method will add new fragment in container and hide the current fragment.
//     * And also will add current fragment in backstack.
//     * </p>
//     *
//     * @param newFragment  This parameter will take new fragment name which need to be add.
//     * @param hideFragment This parameter will take fragmnet name which you want to hide.
//     */
//    public void addFragment(final BaseFragment newFragment, final BaseFragment hideFragment) {
//        Util.getInstance().hideSoftKeyboard(this);
//        getLocalFragmentManager()
//                .beginTransaction()
//                .add(R.id.welcome_container, newFragment, newFragment.getClass().getSimpleName())
//                .hide(hideFragment)
//                .addToBackStack(hideFragment.getClass().getSimpleName())
//                .commit();
//    }
//
//    /**
//     * removes current fragment from container and replace with the new Fragment recieves in parameter
//     *
//     * @param newFragment a fragment object that replaces current fragment
//     */
//    public void replaceFragment(final Fragment newFragment) {
//        Util.getInstance().hideSoftKeyboard(this);
//        getLocalFragmentManager()
//                .beginTransaction()
//                .replace(R.id.welcome_container, newFragment, newFragment.getClass().getSimpleName())
//                .commit();
//    }
//
//    /**
//     * removes all fragment from container and add with the new Fragment recieves in parameter
//     *
//     * @param newFragment a fragment object that replaces current fragment
//     */
//    public void replaceFragmentPopBackstack(final Fragment newFragment) {
//        Util.getInstance().hideSoftKeyboard(this);
//        getLocalFragmentManager().popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
//        getLocalFragmentManager()
//                .beginTransaction()
//                .replace(R.id.welcome_container, newFragment, newFragment.getClass().getSimpleName())
//                .commitAllowingStateLoss();
//    }
}
