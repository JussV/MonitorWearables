package smartlife.monitorwearables;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.support.test.InstrumentationRegistry;
import android.support.test.espresso.Espresso;
import android.support.test.espresso.action.ViewActions;
import android.support.test.espresso.assertion.ViewAssertions;
import android.support.test.espresso.intent.Intents;
import android.support.test.espresso.intent.matcher.IntentMatchers;
import android.support.test.espresso.intent.rule.IntentsTestRule;
import android.support.test.espresso.matcher.RootMatchers;
import android.support.test.espresso.matcher.ViewMatchers;
import android.support.test.filters.LargeTest;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;

import org.hamcrest.CoreMatchers;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.concurrent.TimeUnit;

import smartlife.monitorwearables.activities.CollectionDemoActivity;
import smartlife.monitorwearables.activities.SignUpActivity;
import smartlife.monitorwearables.activities.SignUpCompletedActivity;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Instrumentation test, which will execute on an Android device.
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
@RunWith(AndroidJUnit4.class)
@LargeTest
public class SignUpActivityInstrumentedTest {
    private static Resources resources = null;

    @Rule
    public ActivityTestRule<SignUpActivity> activityTestRule = new ActivityTestRule<SignUpActivity>(SignUpActivity.class, false, false) {};


    @BeforeClass
    public static void setUp() throws Exception{
        resources = InstrumentationRegistry.getTargetContext().getResources();
        Intents.init();
    }

    @Before
    public void launchActivity(){
        Context targetContext = InstrumentationRegistry.getInstrumentation()
                .getTargetContext();
        Intent result = new Intent(targetContext, SignUpActivity.class);
        activityTestRule.launchActivity(result);
    }

    @Test
    public void useAppContext() throws Exception {
        // Context of the app under test.
        Context appContext = InstrumentationRegistry.getTargetContext();

        assertEquals("smartlife.monitorwearables", appContext.getPackageName());
    }

    @Test
    public void signUpUserWithShortPassword() {
        final String email = "xyz@gmail.com";
        final String password ="pass";
        final String username = "zzz";

        Espresso.onView(ViewMatchers.withId(R.id.et_email)).check(ViewAssertions.matches(ViewMatchers.withHint(R.string.email)));
        Espresso.onView(ViewMatchers.withId(R.id.et_email)).perform(ViewActions.typeText(email), ViewActions.closeSoftKeyboard());
        Espresso.onView(ViewMatchers.withId(R.id.et_username)).check(ViewAssertions.matches(ViewMatchers.withHint(R.string.username)));
        Espresso.onView(ViewMatchers.withId(R.id.et_username)).perform(ViewActions.typeText(username), ViewActions.closeSoftKeyboard());
        Espresso.onView(ViewMatchers.withId(R.id.et_pass)).check(ViewAssertions.matches(ViewMatchers.withHint(R.string.password)));
        Espresso.onView(ViewMatchers.withId(R.id.et_pass)).perform(ViewActions.typeText(password), ViewActions.closeSoftKeyboard());
        Espresso.onView(ViewMatchers.withId(R.id.btn_signup)).perform(ViewActions.click());
       /* Espresso.onView(ViewMatchers.withText(R.string.user_is_not_created))
                .inRoot(RootMatchers.withDecorView(CoreMatchers.not(CoreMatchers.is(activityTestRule.getActivity().getWindow().getDecorView()))))
                .check(ViewAssertions.matches(ViewMatchers.isDisplayed()));*/
        Espresso.onView(ViewMatchers.withId(R.id.et_pass)).check(ViewAssertions.matches(ViewMatchers.hasErrorText(resources.getString(R.string.short_pass))));
    }

    @Test
    public void userEmailValidation() {
        final String email = "jveljanoska4gmail.com";
        final String password ="passpass1";
        final String username = "joanav";

        Espresso.onView(ViewMatchers.withId(R.id.et_email)).check(ViewAssertions.matches(ViewMatchers.withHint(R.string.email)));
        Espresso.onView(ViewMatchers.withId(R.id.et_email)).perform(ViewActions.typeText(email), ViewActions.closeSoftKeyboard());
        Espresso.onView(ViewMatchers.withId(R.id.et_username)).check(ViewAssertions.matches(ViewMatchers.withHint(R.string.username)));
        Espresso.onView(ViewMatchers.withId(R.id.et_username)).perform(ViewActions.typeText(username), ViewActions.closeSoftKeyboard());
        Espresso.onView(ViewMatchers.withId(R.id.et_pass)).check(ViewAssertions.matches(ViewMatchers.withHint(R.string.password)));
        Espresso.onView(ViewMatchers.withId(R.id.et_pass)).perform(ViewActions.typeText(password), ViewActions.closeSoftKeyboard());
        Espresso.onView(ViewMatchers.withId(R.id.btn_signup)).perform(ViewActions.click());
        Espresso.onView(ViewMatchers.withId(R.id.et_email)).check(ViewAssertions.matches(ViewMatchers.hasErrorText(resources.getString(R.string.invalid_email))));

    }

    @Test
    public void requiredFieldsValidation() {
        final String email = "";
        final String password ="";
        final String username = "";

        Espresso.onView(ViewMatchers.withId(R.id.et_email)).check(ViewAssertions.matches(ViewMatchers.withHint(R.string.email)));
        Espresso.onView(ViewMatchers.withId(R.id.et_email)).perform(ViewActions.typeText(email), ViewActions.closeSoftKeyboard());
        Espresso.onView(ViewMatchers.withId(R.id.et_username)).check(ViewAssertions.matches(ViewMatchers.withHint(R.string.username)));
        Espresso.onView(ViewMatchers.withId(R.id.et_username)).perform(ViewActions.typeText(username), ViewActions.closeSoftKeyboard());
        Espresso.onView(ViewMatchers.withId(R.id.et_pass)).check(ViewAssertions.matches(ViewMatchers.withHint(R.string.password)));
        Espresso.onView(ViewMatchers.withId(R.id.et_pass)).perform(ViewActions.typeText(password), ViewActions.closeSoftKeyboard());
        Espresso.onView(ViewMatchers.withId(R.id.btn_signup)).perform(ViewActions.click());
        Espresso.onView(ViewMatchers.withId(R.id.et_email)).check(ViewAssertions.matches(ViewMatchers.hasErrorText(resources.getString(R.string.required_email))));
        Espresso.onView(ViewMatchers.withId(R.id.et_username)).check(ViewAssertions.matches(ViewMatchers.hasErrorText(resources.getString(R.string.required_username))));
        Espresso.onView(ViewMatchers.withId(R.id.et_pass)).check(ViewAssertions.matches(ViewMatchers.hasErrorText(resources.getString(R.string.required_password))));
    }


}
