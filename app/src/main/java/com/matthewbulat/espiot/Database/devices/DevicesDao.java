package com.matthewbulat.espiot.Database.devices;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Update;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.Single;

import static android.arch.persistence.room.OnConflictStrategy.REPLACE;

@Dao
public interface DevicesDao {

    @Insert (onConflict = REPLACE)
    void addDevice(DeviceTable deviceTable);

    @Insert (onConflict = REPLACE)
    void addDevices(ArrayList<DeviceTable> deviceTable);

    @Query("select * from devices")
    Single<List<DeviceTable>> getDevices();

    @Delete
    void deleteDevice(DeviceTable deviceTable);

    @Query("DELETE FROM devices")
    void nukeTable();

    @Query("update devices " +
            "set device_description = :newDeviceName " +
            "where device_ID = :deviceId")
    void updateDeviceName(String newDeviceName,String deviceId);

}
