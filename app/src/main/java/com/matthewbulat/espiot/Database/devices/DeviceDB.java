package com.matthewbulat.espiot.Database.devices;


import android.arch.persistence.room.Database;
import android.arch.persistence.room.RoomDatabase;

@Database(entities = {DeviceTable.class},version =1)
public abstract class DeviceDB extends RoomDatabase {

    public abstract DevicesDao devicesDao();

}
