package net.tigerlight.dad;

import static androidx.test.espresso.matcher.ViewMatchers.isRoot;

import android.view.View;
import androidx.test.espresso.UiController;
import androidx.test.espresso.ViewAction;
import org.hamcrest.Matcher;

public class WaitAction implements ViewAction {
    private final long millis;

    public WaitAction(long millis) {
        this.millis = millis;
    }

    @Override
    public Matcher<View> getConstraints() {
        return isRoot();
    }

    @Override
    public String getDescription() {
        return "Wait for " + millis + " milliseconds.";
    }

    @Override
    public void perform(UiController uiController, View view) {
        uiController.loopMainThreadForAtLeast(millis);
    }
}