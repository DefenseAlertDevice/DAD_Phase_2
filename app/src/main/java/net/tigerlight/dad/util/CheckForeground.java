package net.tigerlight.dad.util;

import android.app.Activity;

public class CheckForeground {

    private static boolean isInForeGround = false;
    private static Activity activity;
    private static boolean isThreatScreenVisible;

    public static void onPause() {
        isInForeGround = false;
    }

    public static void onResume(Activity activity) {
        CheckForeground.activity = activity;
        isInForeGround = true;
    }

    public static boolean isInForeGround() {
        return isInForeGround;
    }

    public static Activity getActivity() {
        return activity;
    }

    public static boolean isThreatScreenVisible() {
        return isThreatScreenVisible;
    }

    public static void setThreatScreenVisible(boolean isThreatScreenVisible) {
        CheckForeground.isThreatScreenVisible = isThreatScreenVisible;
    }
}
