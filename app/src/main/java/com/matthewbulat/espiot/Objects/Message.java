package com.matthewbulat.espiot.Objects;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.Gson;

import java.util.ArrayList;

public class Message implements Parcelable {

    private String action;
    private String deviceDescription;
    private String deviceID;
    private String deviceType;

    private ArrayList<Message> deviceList;

    private String lampStatus;

    public Message() {
    }

    protected Message(Parcel in) {
        deviceDescription = in.readString();
        deviceID = in.readString();
        deviceType = in.readString();
        lampStatus = in.readString();
    }

    public static final Creator<Message> CREATOR = new Creator<Message>() {
        @Override
        public Message createFromParcel(Parcel in) {
            return new Message(in);
        }

        @Override
        public Message[] newArray(int size) {
            return new Message[size];
        }
    };

    public String encode() {
        return new Gson().toJson(this);
    }

    public Message decode(String s) {
        return new Gson().fromJson(s, Message.class);
    }



    public String getDeviceDescription() {
        return deviceDescription;
    }

    public void setDeviceDescription(String deviceDescription) {
        this.deviceDescription = deviceDescription;
    }

    public String getDeviceID() {
        return deviceID;
    }

    public void setDeviceID(String deviceID) {
        this.deviceID = deviceID;
    }

    public String getDeviceType() {
        return deviceType;
    }

    public void setDeviceType(String deviceType) {
        this.deviceType = deviceType;
    }

    public ArrayList<Message> getDeviceList() {
        return deviceList;
    }

    public void setDeviceList(ArrayList<Message> deviceList) {
        this.deviceList = deviceList;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(deviceDescription);
        parcel.writeString(deviceID);
        parcel.writeString(deviceType);
        parcel.writeString(lampStatus);
    }

    public String getLampStatus() {
        return lampStatus;
    }

    public void setLampStatus(String lampStatus) {
        this.lampStatus = lampStatus;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }
}
