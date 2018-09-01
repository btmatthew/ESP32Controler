package com.matthewbulat.espiot.Objects;

import android.content.Context;
import android.content.ContextWrapper;
import android.content.SharedPreferences;

public class UserCredentials extends ContextWrapper implements ConstantValues{

    private String userName;
    private String userToken;

    public UserCredentials(Context base) {
        super(base);
    }

    public void retriveCredentials(){
        SharedPreferences prefs = getSharedPreferences(PREFERENCE_NAME, MODE_PRIVATE);
        userName = prefs.getString(USER_NAME, null);
        userToken = prefs.getString(USER_TOKEN, null);
    }

    public void setCredentials(String userName, String userToken){
        SharedPreferences.Editor editor = getSharedPreferences(PREFERENCE_NAME, MODE_PRIVATE).edit();
        editor.putString(USER_NAME, userName);
        editor.putString(USER_TOKEN, userToken);
        editor.apply();
    }

    public boolean credentialsAvailable(){
        return userName != null || userToken != null;
    }

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
}
