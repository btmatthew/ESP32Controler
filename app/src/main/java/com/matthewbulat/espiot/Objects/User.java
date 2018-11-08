package com.matthewbulat.espiot.Objects;
import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.Gson;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;


public class User implements Parcelable {

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

    private User(Parcel in) {
        userName=in.readString();
        userToken=in.readString();
    }

    public static final Creator<User> CREATOR = new Creator<User>() {
        @Override
        public User createFromParcel(Parcel in) {
            return new User(in);
        }

        @Override
        public User[] newArray(int size) {
            return new User[size];
        }
    };

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


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(userName);
        parcel.writeString(userToken);
    }
}
