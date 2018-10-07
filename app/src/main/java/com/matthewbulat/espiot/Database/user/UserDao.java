package com.matthewbulat.espiot.Database.user;


import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;

import java.util.List;

@Dao
public interface UserDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void addUser(UserTable userTable);

    @Query("select * from users Limit 1")
    List<UserTable> getUser();
}
