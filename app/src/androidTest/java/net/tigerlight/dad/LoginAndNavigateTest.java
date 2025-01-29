package net.tigerlight.dad;

import androidx.test.espresso.Espresso;
import androidx.test.espresso.action.ViewActions;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.rule.ActivityTestRule;
import androidx.test.rule.GrantPermissionRule;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.*;

import android.Manifest;

import net.tigerlight.dad.registration.activity.MainActivity;

@RunWith(AndroidJUnit4.class)
public class LoginAndNavigateTest {

    @Rule
    public GrantPermissionRule grantPermissionRule = GrantPermissionRule.grant(
            android.Manifest.permission.ACCESS_FINE_LOCATION,
            android.Manifest.permission.BLUETOOTH,
            android.Manifest.permission.BLUETOOTH_ADMIN,
            Manifest.permission.BLUETOOTH_CONNECT,
            Manifest.permission.BLUETOOTH_SCAN,
            Manifest.permission.BLUETOOTH_ADVERTISE
    );

    @Rule
    public ActivityTestRule<MainActivity> activityRule = new ActivityTestRule<>(MainActivity.class);

    @Test
    public void testLoginAndNavigate() throws InterruptedException {
        Espresso.onView(withId(R.id.fragment_dad_license_tvAccept))
                .check(matches(isDisplayed()));
        Espresso.onView(withId(R.id.fragment_dad_license_tvAccept))
                    .perform(ViewActions.click());

        Espresso.onView(withId(R.id.fragment_registration_tv_login_to_your_account))
                .perform(ViewActions.click());

        Espresso.onView(withId(R.id.fragment_login_to_your_account_et_user_name))
                .perform(ViewActions.typeText("harshitladdha93+android4@gmail.com"), ViewActions.closeSoftKeyboard());

        Espresso.onView(withId(R.id.fragment_login_to_your_account_et_password))
                .perform(ViewActions.typeText("BUCYA@fA"), ViewActions.closeSoftKeyboard());

        Espresso.onView(withId(R.id.fragment_login_to_your_account_tv_login))
                .perform(ViewActions.click());

        InstrumentationRegistry.getInstrumentation().waitForIdleSync();

        Espresso.onView(isRoot()).perform(new WaitAction(5000));

        Espresso.onView(withId(R.id.viewpager))
                .check(matches(isDisplayed()));

        Espresso.onView(withId(R.id.menu_alerts))
                .perform(ViewActions.click());

        Espresso.onView(isRoot()).perform(new WaitAction(5000));

        Espresso.onView(withId(R.id.fragment_alert_tvSendDanger))
                .check(matches(isDisplayed()))
                .check(matches(isEnabled()))
                .perform(ViewActions.click());

        Espresso.onView(isRoot()).perform(new WaitAction(45000));

        Espresso.onView(withId(R.id.dialog_tvPosButtonn))
                .check(matches(isDisplayed()))
                .perform(ViewActions.click());
    }
}