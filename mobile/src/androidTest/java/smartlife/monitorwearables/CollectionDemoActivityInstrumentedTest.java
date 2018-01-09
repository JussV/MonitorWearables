package smartlife.monitorwearables;

import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.support.annotation.DrawableRes;
import android.support.test.InstrumentationRegistry;
import android.support.test.espresso.Espresso;
import android.support.test.espresso.action.ViewActions;
import android.support.test.espresso.assertion.ViewAssertions;
import android.support.test.espresso.matcher.ViewMatchers;
import android.support.test.filters.LargeTest;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.view.View;

import org.hamcrest.Matcher;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.hamcrest.Matchers.allOf;
import smartlife.monitorwearables.activities.CollectionDemoActivity;
import smartlife.monitorwearables.fragments.miband.TabFragment1;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Instrumentation test, which will execute on an Android device.
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
@RunWith(AndroidJUnit4.class)
@LargeTest
public class CollectionDemoActivityInstrumentedTest {
    private static Resources resources = null;
    private static final int DEVICE_TYPE = 11;
    private static final String SNACKBAR_TXT = "Start syncing heart rates remotely ";
    private static final String TAB_DAILY_HR = "  DAILY HR";
    private static final String TAB_MEASURE_HR = "  MEASURE HR";

    @Rule
    public ActivityTestRule<CollectionDemoActivity> activityTestRule = new ActivityTestRule<CollectionDemoActivity>(CollectionDemoActivity.class, false, false) {};

    @BeforeClass
    public static void setUp() throws Exception{
        resources = InstrumentationRegistry.getTargetContext().getResources();
    }

    @Before
    public void launchActivity(){
        Context targetContext = InstrumentationRegistry.getInstrumentation()
                .getTargetContext();
        Intent result = new Intent(targetContext, CollectionDemoActivity.class);
        result.putExtra(Constants.DEVICE_TYPE, DEVICE_TYPE);
        activityTestRule.launchActivity(result);
    }

    @Test
    public void testUIComponents() throws Exception {
        Espresso.onView(ViewMatchers.withId(R.id.toolbar)).check(ViewAssertions.matches(ViewMatchers.isDisplayed()));
        Espresso.onView(ViewMatchers.withId(R.id.tab_layout)).check(ViewAssertions.matches(ViewMatchers.isDisplayed()));
        Matcher<View> tab1 = allOf(ViewMatchers.withText(TAB_DAILY_HR),
                ViewMatchers.withEffectiveVisibility(ViewMatchers.Visibility.VISIBLE));
        Espresso.onView(tab1).perform(ViewActions.click());
        Matcher<View> tab2MeasureHR = allOf(ViewMatchers.withText(TAB_MEASURE_HR),
                ViewMatchers.withEffectiveVisibility(ViewMatchers.Visibility.VISIBLE));
        Espresso.onView(tab2MeasureHR).perform(ViewActions.click());
        Matcher<View> tab3 = allOf(ViewMatchers.withContentDescription("Settings"),
                ViewMatchers.withEffectiveVisibility(ViewMatchers.Visibility.VISIBLE));
        Espresso.onView(tab3).perform(ViewActions.click());
    }

    @Test
    public void openDailyHRTab() throws Exception {
        Espresso.onView(ViewMatchers.withId(R.id.tab_layout)).check(ViewAssertions.matches(ViewMatchers.isDisplayed()));
        Matcher<View> tab1 = allOf(ViewMatchers.withText(TAB_DAILY_HR),
                ViewMatchers.withEffectiveVisibility(ViewMatchers.Visibility.VISIBLE));
        Espresso.onView(tab1).perform(ViewActions.click());
        Espresso.onView(ViewMatchers.withId(R.id.rv_heart_rates))
                .check(ViewAssertions.matches(ViewMatchers.isDisplayed()));
    }

    @Test
    public void openMeasureHRTab() throws Exception {
        Espresso.onView(ViewMatchers.withId(R.id.tab_layout)).check(ViewAssertions.matches(ViewMatchers.isDisplayed()));
        Matcher<View> tab2 = allOf(ViewMatchers.withText(TAB_MEASURE_HR),
                ViewMatchers.withEffectiveVisibility(ViewMatchers.Visibility.VISIBLE));
        Espresso.onView(tab2).perform(ViewActions.click());
        Espresso.onView(ViewMatchers.withId(R.id.tv_live_hr))
                .check(ViewAssertions.matches(ViewMatchers.isDisplayed()));
    }

    @Test
    public void openSettingsTab() throws Exception {
        Espresso.onView(ViewMatchers.withId(R.id.tab_layout)).check(ViewAssertions.matches(ViewMatchers.isDisplayed()));
        Matcher<View> tab3 = allOf(ViewMatchers.withContentDescription("Settings"),
                ViewMatchers.withEffectiveVisibility(ViewMatchers.Visibility.VISIBLE));
        Espresso.onView(tab3).perform(ViewActions.click());
        Espresso.onView(ViewMatchers.withId(R.id.switch_heartrate_monitor))
                .check(ViewAssertions.matches(ViewMatchers.isDisplayed()));
    }

    @Test
    public void syncData() throws Exception {
        Espresso.onView(ViewMatchers.withId(R.id.iv_sync_hr)).perform(ViewActions.click());
        Espresso.onView(ViewMatchers.withText(SNACKBAR_TXT))
                .check(ViewAssertions.matches(ViewMatchers.withEffectiveVisibility(
                        ViewMatchers.Visibility.VISIBLE
                )));
    }

    @Test
    public void testBackButton() throws Exception {
        Espresso.onView(ViewMatchers.withContentDescription("Navigate up")).perform(ViewActions.click());
        assertTrue(activityTestRule.getActivity().isFinishing());
    }

}
