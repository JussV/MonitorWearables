/*  Copyright (C) 2015-2017 Andreas Shimokawa, Carsten Pfeiffer, Daniele
    Gobbetti, ivanovlev, Kasha, Lem Dulfo, Steffen Liebergeld

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
package smartlife.monitorwearables.activities;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.RemoteInput;
import android.support.v4.content.LocalBroadcastManager;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import java.util.ArrayList;

import smartlife.monitorwearables.GBApplication;
import smartlife.monitorwearables.R;
import smartlife.monitorwearables.impl.GBDevice;
import smartlife.monitorwearables.model.DeviceService;
import smartlife.monitorwearables.model.NotificationSpec;
import smartlife.monitorwearables.util.GB;


public class DebugActivity extends GBActivity {
    //private static final Logger LOG = LoggerFactory.getLogger(DebugActivity.class);

    private static final String EXTRA_REPLY = "reply";
    private static final String ACTION_REPLY
            = "smartlife.monitorwearables.DebugActivity.action.reply";

    private Spinner sendTypeSpinner;
    private Button sendButton;
    private Button testNotificationButton;
    private Button HeartRateButton;

    private EditText editContent;
    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            switch (intent.getAction()) {
                case ACTION_REPLY: {
                    Bundle remoteInput = RemoteInput.getResultsFromIntent(intent);
                    CharSequence reply = remoteInput.getCharSequence(EXTRA_REPLY);
                 //   LOG.info("got wearable reply: " + reply);
                    GB.toast(context, "got wearable reply: " + reply, Toast.LENGTH_SHORT, GB.INFO);
                    break;
                }
                case DeviceService.ACTION_HEARTRATE_MEASUREMENT: {
                    int hrValue = intent.getIntExtra(DeviceService.EXTRA_HEART_RATE_VALUE, -1);
                    GB.toast(DebugActivity.this, "Heart Rate measured: " + hrValue, Toast.LENGTH_LONG, GB.INFO);
                    break;
                }
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_debug);

        IntentFilter filter = new IntentFilter();
        filter.addAction(ACTION_REPLY);
        filter.addAction(DeviceService.ACTION_HEARTRATE_MEASUREMENT);
        LocalBroadcastManager.getInstance(this).registerReceiver(mReceiver, filter);
        registerReceiver(mReceiver, filter); // for ACTION_REPLY

        editContent = (EditText) findViewById(R.id.editContent);

        ArrayList<String> spinnerArray = new ArrayList<>();
     /*   for (NotificationType notificationType : NotificationType.values()) {
            spinnerArray.add(notificationType.name());
        }*/
        ArrayAdapter<String> spinnerArrayAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, spinnerArray);
        sendTypeSpinner = (Spinner) findViewById(R.id.sendTypeSpinner);
        sendTypeSpinner.setAdapter(spinnerArrayAdapter);

        sendButton = (Button) findViewById(R.id.sendButton);
        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                NotificationSpec notificationSpec = new NotificationSpec();
                String testString = editContent.getText().toString();
                notificationSpec.phoneNumber = testString;
                notificationSpec.body = testString;
                notificationSpec.sender = testString;
                notificationSpec.subject = testString;
              //  notificationSpec.type = NotificationType.values()[sendTypeSpinner.getSelectedItemPosition()];
                notificationSpec.id = -1;
                GBApplication.deviceService().onNotification(notificationSpec);
            }
        });


        HeartRateButton = (Button) findViewById(R.id.HearRateButton);
        HeartRateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                GB.toast("Measuring heart rate, please wait...", Toast.LENGTH_LONG, GB.INFO);
                GBApplication.deviceService().onHeartRateTest();
            }
        });

        testNotificationButton = (Button) findViewById(R.id.testNotificationButton);
        testNotificationButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                testNotification();
            }
        });

    }

    private void testNotification() {
        Intent notificationIntent = new Intent(getApplicationContext(), DebugActivity.class);
        notificationIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(getApplicationContext(), 0,
                notificationIntent, 0);

        NotificationManager nManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

        RemoteInput remoteInput = new RemoteInput.Builder(EXTRA_REPLY)
                .build();

        Intent replyIntent = new Intent(ACTION_REPLY);

        PendingIntent replyPendingIntent = PendingIntent.getBroadcast(this, 0, replyIntent, 0);

        NotificationCompat.Action action =
                new NotificationCompat.Action.Builder(android.R.drawable.ic_input_add, "Reply", replyPendingIntent)
                        .addRemoteInput(remoteInput)
                        .build();

        NotificationCompat.WearableExtender wearableExtender = new NotificationCompat.WearableExtender().addAction(action);

        NotificationCompat.Builder ncomp = new NotificationCompat.Builder(this)
                .setContentTitle(getString(R.string.test_notification))
                .setContentText(getString(R.string.this_is_a_test_notification_from_gadgetbridge))
                .setTicker(getString(R.string.this_is_a_test_notification_from_gadgetbridge))
                .setSmallIcon(android.support.v7.appcompat.R.drawable.notification_template_icon_bg)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent)
                .extend(wearableExtender);

        nManager.notify((int) System.currentTimeMillis(), ncomp.build());
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                NavUtils.navigateUpFromSameTask(this);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mReceiver);
        unregisterReceiver(mReceiver);
    }

    public interface DeviceSelectionCallback {
        void invoke(GBDevice device);
    }
}
