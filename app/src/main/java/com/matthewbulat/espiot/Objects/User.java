package com.matthewbulat.espiot.Objects;
import com.google.gson.Gson;


public class User {


    private String action;
    private String userEmail;
    private String password;
    private String userName;
    private String userToken;

    public User() {
    }

    public String getUserEmail() {
        return userEmail;
    }

    public void setUserEmail(String userEmail) {
        this.userEmail = userEmail;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
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

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public String encode() {
        return new Gson().toJson(this);
    }

    public User decode(String s) {
        return new Gson().fromJson(s, User.class);
    }


}
