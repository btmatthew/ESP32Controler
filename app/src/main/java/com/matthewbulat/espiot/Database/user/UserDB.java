package com.matthewbulat.espiot.Database.user;

import android.arch.persistence.room.Database;
import android.arch.persistence.room.RoomDatabase;



@Database(entities = {UserTable.class},version =1)
public abstract class UserDB extends RoomDatabase{

    public abstract UserDao userDao();

}
