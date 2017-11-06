/*  Copyright (C) 2015-2017 Andreas Shimokawa, Carsten Pfeiffer, Daniele
    Gobbetti, Lem Dulfo

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
package smartlife.monitorwearables.adapter;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.design.widget.Snackbar;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;

import smartlife.monitorwearables.Constants;
import smartlife.monitorwearables.GBApplication;
import smartlife.monitorwearables.R;
import smartlife.monitorwearables.activities.CollectionDemoActivity;
import smartlife.monitorwearables.devices.DeviceManager;
import smartlife.monitorwearables.impl.GBDevice;
import smartlife.monitorwearables.model.DeviceType;
import smartlife.monitorwearables.service.ContinuousMeasureScheduler;
import smartlife.monitorwearables.service.DeviceCommunicationService;
import smartlife.monitorwearables.service.DeviceCoordinator;
import smartlife.monitorwearables.util.DeviceHelper;
import smartlife.monitorwearables.util.GB;
import smartlife.monitorwearables.util.Prefs;

/**
 * Adapter for displaying GBDevice instances.
 */
public class DeviceRecyclerViewAdapter  extends RecyclerView.Adapter<DeviceRecyclerViewAdapter.ViewHolder>{

    private final Context context;
    private List<GBDevice> deviceList;
    private ViewGroup parent;
    private static Prefs prefs;
    private static SharedPreferences sharedPrefs;
    private ContinuousMeasureScheduler scheduler;
   // private SettingsManager mSettingsManager;

    public DeviceRecyclerViewAdapter(Context context, List<GBDevice> deviceList) {
        this.context = context;
        this.deviceList = deviceList;
        scheduler = ContinuousMeasureScheduler.getInstance();
    }

    @Override
    public DeviceRecyclerViewAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        this.parent = parent;
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.device_itemv2, parent, false);
        ViewHolder vh = new ViewHolder(view);
        return vh;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, final int position) {
        final GBDevice device = deviceList.get(position);

        //auto connect to last connected device
        sharedPrefs = PreferenceManager.getDefaultSharedPreferences(context);
        prefs = new Prefs(sharedPrefs);
      //  mSettingsManager = new SettingsManager(context);
        if(!device.isConnected() && device.getAddress().equals(prefs.getString(DeviceCommunicationService.LAST_DEVICE_ADDRESS, ""))){
            GBApplication.deviceService().connect(device);
        }

        holder.container.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                if (device.isInitialized() || device.isConnected()) {
                    showTransientSnackbar(R.string.controlcenter_snackbar_need_longpress);
                } else {
                    showTransientSnackbar(R.string.controlcenter_snackbar_connecting);
                    GBApplication.deviceService().connect(device);
                }
            }
        });

        holder.container.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                if (device.getState() != GBDevice.State.NOT_CONNECTED) {
                    showTransientSnackbar(R.string.controlcenter_snackbar_disconnecting);
                    GBApplication.deviceService().disconnect();
                }
                return true;
            }
        });

        if (device.getType().getKey() == DeviceType.MIBAND2.getKey()) {
            if (device.isInitialized()) {
                holder.deviceImageView.setImageResource(R.drawable.miband);
                int monitorIntervalPos = prefs.getInt(context.getResources().getString(R.string.key_monitor_interval),0);
                String monitorIntervalStr = context.getResources().getStringArray(R.array.hr_interval_array)[monitorIntervalPos];
                int monitorInterval = !monitorIntervalStr.equals("") ? Integer.valueOf(monitorIntervalStr) : 0;
                boolean isContinousHREnabled = prefs.getBoolean(context.getResources().getString(R.string.key_enable_continuous_monitoring), false);
                if(isContinousHREnabled){
                  //  context.startService(new Intent(context, ContinuousMeasureService.class));
                    scheduler.init(isContinousHREnabled);
                }
            }
            else
                holder.deviceImageView.setImageResource(R.drawable.miband2_disabled);
        }

        if (device.getType().getKey() == DeviceType.ANDROIDWEAR_MOTO360SPORT.getKey()) {
            if (device.isInitialized()) {
                holder.deviceImageView.setImageResource(R.drawable.android_wear);
            }
            else
                holder.deviceImageView.setImageResource(R.drawable.android_wear_disabled);
        }
        holder.deviceNameLabel.setText(device.getName());
        holder.deviceMacAddress.setText(device.getAddress());

        if (device.isBusy()) {
            holder.deviceStatusLabel.setText(device.getBusyTask());
            // holder.busyIndicator.setVisibility(View.VISIBLE);
        } else {
            holder.deviceStatusLabel.setText(device.getStateString());
            //   holder.busyIndicator.setVisibility(View.INVISIBLE);
        }

        //remove device on x clicked
        holder.removeDevice.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new AlertDialog.Builder(context)
                        .setIcon(android.R.drawable.ic_delete)
                        .setTitle(context.getString(R.string.delete_device, device.getName()))
                        .setMessage(R.string.delete_device_message)
                        .setCancelable(true)
                        .setPositiveButton(R.string.delete, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                try {
                                    DeviceCoordinator coordinator = DeviceHelper.getInstance().getCoordinator(device);
                                    if (coordinator != null) {
                                        coordinator.deleteDevice(device);
                                    }
                                    DeviceHelper.getInstance().removeBond(device);
                                } catch (Exception ex) {
                                    GB.toast(context, "Error deleting device: " + ex.getMessage(), Toast.LENGTH_LONG, GB.ERROR, ex);
                                } finally {
                                    Intent refreshIntent = new Intent(DeviceManager.ACTION_REFRESH_DEVICELIST);
                                    LocalBroadcastManager.getInstance(context).sendBroadcast(refreshIntent);
                                }
                            }
                        })
                        .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                            }
                        })
                        .show();
            }
        });

        holder.measureHR.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, CollectionDemoActivity.class);
                intent.putExtra(Constants.DEVICE_TYPE, device.getType().getKey());
                context.startActivity(intent);
            }

        });
    }

   // @Override
    public int getItemCount() {
        return deviceList.size();
    }

    private void showTransientSnackbar(int resource) {
        Snackbar snackbar = Snackbar.make(parent, resource, Snackbar.LENGTH_SHORT);
        View snackbarView = snackbar.getView();
        int snackbarTextId = android.support.design.R.id.snackbar_text;
        TextView textView = (TextView) snackbarView.findViewById(snackbarTextId);
        snackbar.show();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {

        CardView container;
        ImageView deviceImageView;
        TextView deviceNameLabel;
        TextView deviceStatusLabel;
        TextView deviceMacAddress;
        ImageView removeDevice;
        ImageView measureHR;

        ViewHolder(View view) {
            super(view);
            container = (CardView) view.findViewById(R.id.card_view);
            deviceImageView = (ImageView) view.findViewById(R.id.device_image);
            deviceNameLabel = (TextView) view.findViewById(R.id.device_name);
            deviceStatusLabel = (TextView) view.findViewById(R.id.device_status);
            deviceMacAddress = (TextView) view.findViewById(R.id.device_mac_address);
            removeDevice = (ImageView) view.findViewById(R.id.delete_device);
            measureHR = (ImageView) view.findViewById(R.id.measure_hr);
        }

    }


}
