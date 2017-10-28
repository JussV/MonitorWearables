package smartlife.monitorwearables.entities;

// THIS CODE IS GENERATED BY greenDAO, DO NOT EDIT. Enable "keep" sections if you want to edit. 

import android.content.Context;
import android.content.SharedPreferences;

import java.util.UUID;

/**
 * Entity mapped to table "DEVICE".
 */
public class Device {

    private static String uniqueID = null;
    private static final String PREF_UNIQUE_ID = "PREF_UNIQUE_ID";

    private Long id;
    /** Not-null value. */
    private String name;
    /** Not-null value. */
    private String manufacturer;
    /** Not-null value. */
    private String identifier;
    private int type;
    private String model;

    public Device() {
    }

    public Device(Long id) {
        this.id = id;
    }

    public Device(Long id, String name, String manufacturer, String identifier, int type, String model) {
        this.id = id;
        this.name = name;
        this.manufacturer = manufacturer;
        this.identifier = identifier;
        this.type = type;
        this.model = model;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    /** Not-null value. */
    public String getName() {
        return name;
    }

    /** Not-null value; ensure this value is available before it is saved to the database. */
    public void setName(String name) {
        this.name = name;
    }

    /** Not-null value. */
    public String getManufacturer() {
        return manufacturer;
    }

    /** Not-null value; ensure this value is available before it is saved to the database. */
    public void setManufacturer(String manufacturer) {
        this.manufacturer = manufacturer;
    }

    /** Not-null value. */
    /**
     * The fixed identifier, i.e. MAC address of the device.
     */
    public String getIdentifier() {
        return identifier;
    }

    /** Not-null value; ensure this value is available before it is saved to the database. */
    /**
     * The fixed identifier, i.e. MAC address of the device.
     */
    public void setIdentifier(String identifier) {
        this.identifier = identifier;
    }

    /**
     * The DeviceType key, i.e. the GBDevice's type.
     */
    public int getType() {
        return type;
    }

    /**
     * The DeviceType key, i.e. the GBDevice's type.
     */
    public void setType(int type) {
        this.type = type;
    }

    /**
     * An optional model, further specifying the kind of device-
     */
    public String getModel() {
        return model;
    }

    /**
     * An optional model, further specifying the kind of device-
     */
    public void setModel(String model) {
        this.model = model;
    }

    public synchronized static String getDeviceUniqueId(Context context) {
        if (uniqueID == null) {
            SharedPreferences sharedPrefs = context.getSharedPreferences(
                    PREF_UNIQUE_ID, Context.MODE_PRIVATE);
            uniqueID = sharedPrefs.getString(PREF_UNIQUE_ID, null);

            if (uniqueID == null) {
                uniqueID = UUID.randomUUID().toString();
                SharedPreferences.Editor editor = sharedPrefs.edit();
                editor.putString(PREF_UNIQUE_ID, uniqueID);
                editor.commit();
            }
        }

        return uniqueID;
    }

}
