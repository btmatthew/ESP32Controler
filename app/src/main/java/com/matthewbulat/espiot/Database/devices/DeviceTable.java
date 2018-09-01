package com.matthewbulat.espiot.Database.devices;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;
import android.support.annotation.NonNull;

@Entity(tableName = "devices")
public class DeviceTable {

    @PrimaryKey
    @ColumnInfo(name = "device_ID")
    @NonNull
    private String deviceID;

    @ColumnInfo(name = "device_description")
    private String deviceDescription;

    @ColumnInfo(name = "device_type")
    private String deviceType;

    @NonNull
    public String getDeviceID() {
        return deviceID;
    }

    public void setDeviceID(@NonNull String deviceID) {
        this.deviceID = deviceID;
    }

    public String getDeviceDescription() {
        return deviceDescription;
    }

    public void setDeviceDescription(String deviceDescription) {
        this.deviceDescription = deviceDescription;
    }

    public String getDeviceType() {
        return deviceType;
    }

    public void setDeviceType(String deviceType) {
        this.deviceType = deviceType;
    }
}
