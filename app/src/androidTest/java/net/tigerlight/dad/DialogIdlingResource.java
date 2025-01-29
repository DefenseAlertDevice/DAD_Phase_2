package net.tigerlight.dad;

import android.app.Activity;

import androidx.test.espresso.IdlingResource;

public class DialogIdlingResource implements IdlingResource {
    private final Activity activity;
    private ResourceCallback callback;

    public DialogIdlingResource(Activity activity) {
        this.activity = activity;
    }

    @Override
    public String getName() {
        return DialogIdlingResource.class.getName();
    }

    @Override
    public boolean isIdleNow() {
        boolean isIdle = activity.findViewById(R.id.viewpager) == null;
        if (isIdle && callback != null) {
            callback.onTransitionToIdle();
        }
        return isIdle;
    }

    @Override
    public void registerIdleTransitionCallback(ResourceCallback callback) {
        this.callback = callback;
    }
}