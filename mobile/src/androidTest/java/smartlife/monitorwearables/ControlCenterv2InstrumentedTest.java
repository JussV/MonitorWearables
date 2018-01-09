package smartlife.monitorwearables;

import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.support.test.InstrumentationRegistry;
import android.support.test.espresso.Espresso;
import android.support.test.espresso.assertion.ViewAssertions;
import android.support.test.espresso.contrib.DrawerActions;
import android.support.test.espresso.contrib.DrawerMatchers;
import android.support.test.espresso.contrib.NavigationViewActions;
import android.support.test.espresso.intent.Intents;
import android.support.test.espresso.matcher.ViewMatchers;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.view.Gravity;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.regex.Pattern;

import smartlife.monitorwearables.activities.ControlCenterv2;
import smartlife.monitorwearables.activities.SignUpActivity;

import static org.junit.Assert.assertEquals;

/**
 * Instrumentation test, which will execute on an Android device.
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
@RunWith(AndroidJUnit4.class)
public class ControlCenterv2InstrumentedTest {

    private static Resources resources = null;

    @Rule
    public ActivityTestRule<ControlCenterv2> activityTestRule = new ActivityTestRule<ControlCenterv2>(ControlCenterv2.class, false, false) {};


    @BeforeClass
    public static void setUp() throws Exception{
        resources = InstrumentationRegistry.getTargetContext().getResources();
        Intents.init();
    }

    @Before
    public void launchActivity(){
        Context targetContext = InstrumentationRegistry.getInstrumentation()
                .getTargetContext();
        Intent result = new Intent(targetContext, ControlCenterv2.class);
        activityTestRule.launchActivity(result);
    }

    @Test
    public void useAppContext() throws Exception {
        // Context of the app under test.
        Context appContext = InstrumentationRegistry.getTargetContext();

        assertEquals("smartlife.monitorwearables", appContext.getPackageName());
    }

    @Test
    public void testOpenAndCloseDrawer() {
        // Open drawer
        Espresso.onView(ViewMatchers.withId(R.id.drawer_layout)).perform(DrawerActions.open());
        // Check if drawer is open
        Espresso.onView(ViewMatchers.withId(R.id.drawer_layout))
                .check(ViewAssertions.matches(DrawerMatchers.isOpen(Gravity.LEFT))); // Left drawer is open open.
        // Close drawer
        Espresso.onView(ViewMatchers.withId(R.id.drawer_layout)).perform(DrawerActions.close());
        // Check if drawer is closed
        Espresso.onView(ViewMatchers.withId(R.id.drawer_layout))
                .check(ViewAssertions.matches(DrawerMatchers.isClosed()));

    }
    @Test
    public void selectItemFromDrawer() {
        Espresso.onView(ViewMatchers.withId(R.id.drawer_layout)).perform(DrawerActions.open());
        // Check if drawer is open
        Espresso.onView(ViewMatchers.withId(R.id.drawer_layout))
                .check(ViewAssertions.matches(DrawerMatchers.isOpen(Gravity.LEFT))); // Left drawer is open open.
        Espresso.onView(ViewMatchers.withId(R.id.nav_view))
                .perform(NavigationViewActions.navigateTo(R.id.action_signup));
    }




}
