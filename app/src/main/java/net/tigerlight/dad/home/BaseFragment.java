package net.tigerlight.dad.home;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import net.tigerlight.dad.util.Constants;
import net.tigerlight.dad.util.Util;

import android.os.Bundle;
import android.os.SystemClock;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

/**
 * Base Fragment for all fragments used in the application.
 */
public abstract class BaseFragment extends Fragment implements View.OnClickListener {
    private long mLastClickTime = 0;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return super.onCreateView(inflater, container, savedInstanceState);
    }

    public abstract void initView(View view);

    public abstract void trackScreen();

    public abstract void initActionBar();

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initView(view);
        trackScreen();
        initActionBar();
    }

    /**
     * Gets the fragment manager object of activity required for fragment transaction.
     *
     * @return object of {@link androidx.fragment.app.FragmentManager}.
     */
    public FragmentManager getLocalFragmentManager() {
        return requireActivity().getSupportFragmentManager();
    }

    /**
     * Gets the child fragment manager object of the fragment required for fragment transaction.
     *
     * @return object of {@link androidx.fragment.app.FragmentManager}.
     */
    public FragmentManager getLocalChildFragmentManager() {
        return getChildFragmentManager();
    }

    @Override
    public void onClick(View v) {
        Util.getInstance().hideSoftKeyboard(requireActivity());
        /**
         * Logic to prevent the launch of the fragment twice if the user makes
         * the tap (click) very fast.
         */
        if (SystemClock.elapsedRealtime() - mLastClickTime < Constants.MAX_CLICK_INTERVAL) {
            return;
        }
        mLastClickTime = SystemClock.elapsedRealtime();
    }

    /**
     * Removes the current fragment from the container and replaces it with the new fragment received as a parameter.
     *
     * @param newFragment  a fragment object that replaces the current fragment
     * @param containerId  ID of the container in which you want to replace the fragment
     */
    public void replaceChildFragment(final Fragment newFragment, final int containerId) {
        getLocalChildFragmentManager()
                .beginTransaction()
                .replace(containerId, newFragment, newFragment.getClass().getSimpleName())
                .commit();
    }
}