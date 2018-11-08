package com.matthewbulat.espiot.Database.user;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;
import android.support.annotation.NonNull;

import com.matthewbulat.espiot.Objects.User;

@Entity(tableName = "users")
public class UserTable {

    @PrimaryKey
    @ColumnInfo(name = "user_name")
    @NonNull
    private String userName;

    @ColumnInfo(name = "user_token")
    private String userToken;

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getUserToken() {
        return userToken;
    }

    public void setUserToken(String userToken) {
        this.userToken = userToken;
    }

    public User returnUserObject(){
        User user = new User();
        user.setUserName(this.userName);
        user.setUserToken(this.userToken);
        return user;
    }
}
