package com.matthewbulat.espiot.Objects;
import com.google.gson.Gson;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;


public class User {

    @SerializedName("action")
    @Expose
    private String action;
    @SerializedName("userEmail")
    @Expose
    private String userEmail;
    @SerializedName("password")
    @Expose
    private String password;
    @SerializedName("userName")
    @Expose
    private String userName;
    @SerializedName("userToken")
    @Expose
    private String userToken;

    public User() {
    }

//    public  User(String userEmail,String password){
//        this.userEmail=userEmail;
//        this.password=password;
//    }
//
//    public  User(String userEmail,String password,String userName){
//        this.userEmail=userEmail;
//        this.password=password;
//        this.userName=userName;
//    }

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
