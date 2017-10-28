/*  Copyright (C) 2015-2017 Andreas Shimokawa, Carsten Pfeiffer, Daniele
    Gobbetti, Normano64

    This file is part of Gadgetbridge.

    Gadgetbridge is free software: you can redistribute it and/or modify
    it under the terms of the GNU Affero General Public License as published
    by the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    Gadgetbridge is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU Affero General Public License for more details.

    You should have received a copy of the GNU Affero General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>. */
package smartlife.monitorwearables;

import android.app.Application;
import android.app.NotificationManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.facebook.stetho.Stetho;
import com.firebase.jobdispatcher.Driver;
import com.firebase.jobdispatcher.FirebaseJobDispatcher;
import com.firebase.jobdispatcher.GooglePlayDriver;
import com.firebase.jobdispatcher.Job;
import com.firebase.jobdispatcher.Lifetime;
import com.firebase.jobdispatcher.Trigger;

import java.util.Calendar;
import java.util.HashSet;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import smartlife.monitorwearables.devices.DeviceManager;
import smartlife.monitorwearables.impl.GBDeviceService;
import smartlife.monitorwearables.model.DeviceService;
import smartlife.monitorwearables.service.jobs.DeleteDBJobService;
import smartlife.monitorwearables.util.GBPrefs;
import smartlife.monitorwearables.util.LimitedQueue;
import smartlife.monitorwearables.util.Prefs;

/**
 * Main Application class that initializes and provides access to certain things like
 * logging and DB access.
 */
public class GBApplication extends Application {
    // Since this class must not log to slf4j, we use plain android.util.Log
    private static final String TAG = "GBApplication";
    public static final String DATABASE_NAME = "Gadgetbridge";
    public static final String DELETE_DB_JOB_TAG = "deleteDDBJob";

    private static GBApplication context;
    private static final Lock dbLock = new ReentrantLock();
    private static DeviceService deviceService;
    private static SharedPreferences sharedPrefs;
    private static final String PREFS_VERSION = "shared_preferences_version";
    //if preferences have to be migrated, increment the following and add the migration logic in migratePrefs below; see http://stackoverflow.com/questions/16397848/how-can-i-migrate-android-preferences-with-a-new-version
    private static final int CURRENT_PREFS_VERSION = 2;
    private static LimitedQueue mIDSenderLookup = new LimitedQueue(16);
    private static Prefs prefs;
    private static GBPrefs gbPrefs;

    /**
     * Note: is null on Lollipop and Kitkat
     */
    private static NotificationManager notificationManager;

    public static final String ACTION_QUIT = "smartlife.monitorwearables.gbapplication.action.quit";

    public GBApplication() {
        context = this;
    }
    private static GBApplication app;

    private DeviceManager deviceManager;

    protected DeviceService createDeviceService() {
        return new GBDeviceService(this);
    }

    @Override
    public void onCreate() {
        app = this;
        super.onCreate();
        Stetho.initializeWithDefaults(this);
        sharedPrefs = PreferenceManager.getDefaultSharedPreferences(context);
        prefs = new Prefs(sharedPrefs);
        gbPrefs = new GBPrefs(prefs);

        /*if (!GBEnvironment.isEnvironmentSetup()) {
            GBEnvironment.setupEnvironment(GBEnvironment.createDeviceEnvironment());
            // setup db after the environment is set up, but don't do it in test mode
            // in test mode, it's done individually, see TestBase
            setupDatabase();
        }


        if (getPrefsFileVersion() != CURRENT_PREFS_VERSION) {
            migratePrefs(getPrefsFileVersion());
        }
*/

        deviceManager = new DeviceManager(this);
        deviceService = createDeviceService();

        Driver driver = new GooglePlayDriver(context);
        FirebaseJobDispatcher firebaseJobDispatcher = new FirebaseJobDispatcher(driver);

       /* final int periodicity = (int) TimeUnit.HOURS.toSeconds(24); // Every 12 hours periodicity expressed as seconds
        final int toleranceInterval = (int) TimeUnit.MINUTES.toSeconds(5); // a small(ish) window of time when triggering is OK*/
        Calendar now = Calendar.getInstance();
        Calendar midNight = Calendar.getInstance();
        midNight.setTimeInMillis(System.currentTimeMillis());
        midNight.set(Calendar.HOUR_OF_DAY, 24);
        midNight.set(Calendar.MINUTE, 0);
        midNight.set(Calendar.SECOND, 0);
        midNight.set(Calendar.MILLISECOND, 0);
       // midNight.setTimeZone(TimeZone.getDefault());
      //  midNight.set(Calendar.AM_PM, Calendar.AM);

        long diff = midNight.getTimeInMillis() -  now.getTimeInMillis();
        int startSeconds = (int) (diff / 1000); // tell the start seconds
        int endSeconds = startSeconds + 300; // within Five minutes

        int startSecondsTest = 60;
        int endSecondsTest = 120;

        Job deleteLocalDBJob = firebaseJobDispatcher.newJobBuilder()
                .setService(DeleteDBJobService.class)
                .setTag(DELETE_DB_JOB_TAG)
                .setLifetime(Lifetime.FOREVER)
                .setRecurring(true)
              // .setTrigger(Trigger.executionWindow(periodicity, periodicity + toleranceInterval))
                .setTrigger(Trigger.executionWindow(startSeconds, endSeconds))
                .setReplaceCurrent(true)
                .build();

        firebaseJobDispatcher.schedule(deleteLocalDBJob);

      /*  if (isRunningMarshmallowOrLater()) {
            notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            //the following will ensure the notification manager is kept alive
            startService(new Intent(this, NotificationCollectorMonitorService.class));
        }*/
    }

    public static Context getContext() {
        return context;
    }

    /**
     * Returns the facade for talking to devices. Devices are managed by
     * an Android Service and this facade provides access to its functionality.
     *
     * @return the facade for talking to the service/devices.
     */
    public static DeviceService deviceService() {
        return deviceService;
    }

    private static HashSet<String> blacklist = null;

    private int getPrefsFileVersion() {
        try {
            return Integer.parseInt(sharedPrefs.getString(PREFS_VERSION, "0")); //0 is legacy
        } catch (Exception e) {
            //in version 1 this was an int
            return 1;
        }
    }


    public static LimitedQueue getIDSenderLookup() {
        return mIDSenderLookup;
    }

    public static Prefs getPrefs() {
        return prefs;
    }

    public static GBPrefs getGBPrefs() {
        return gbPrefs;
    }

    public DeviceManager getDeviceManager() {
        return deviceManager;
    }

    public static GBApplication app() {
        return app;
    }

}
