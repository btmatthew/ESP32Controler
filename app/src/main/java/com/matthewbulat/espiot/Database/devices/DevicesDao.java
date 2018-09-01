package com.matthewbulat.espiot.Database.devices;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;

import com.matthewbulat.espiot.Database.user.UserTable;

import java.util.List;

@Dao
public interface DevicesDao {

    @Insert
    public void addDevice(DeviceTable deviceTable);

    @Query("select * from devices")
    public List<DeviceTable> getDevices();

    @Delete
    public void deleteDevice(DeviceTable deviceTable);

    @Query("DELETE FROM devices")
    public void nukeTable();

}
