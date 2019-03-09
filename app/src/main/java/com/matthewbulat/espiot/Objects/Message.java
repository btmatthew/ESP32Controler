package com.matthewbulat.espiot.Objects;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.Gson;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;

public class Message implements Parcelable {
    @SerializedName("action")
    @Expose
    private String action;
    @SerializedName("deviceDescription")
    @Expose
    private String deviceDescription;
    @SerializedName("deviceID")
    @Expose
    private String deviceID;
    @SerializedName("deviceType")
    @Expose
    private String deviceType;
    @SerializedName("deviceList")
    @Expose
    private ArrayList<Message> deviceList;
    @SerializedName("lampStatus")
    @Expose
    private String lampStatus;
    @SerializedName("relayID")
    @Expose
    private String relayID;
    @SerializedName("remoteOption")
    @Expose
    private int remoteOption;
    @SerializedName("fanStatus")
    @Expose
    private boolean fanStatus;
    @SerializedName("fanSpeed")
    @Expose
    private int fanSpeed;
    @SerializedName("fanMode")
    @Expose
    private int fanMode;
    @SerializedName("rotation")
    @Expose
    private boolean rotation;
    @SerializedName("ion")
    @Expose
    private boolean ion;
    @SerializedName("tvStatus")
    @Expose
    private boolean tvStatus;
    @SerializedName("humidity")
    @Expose
    private float humidity;
    @SerializedName("temperature")
    @Expose
    private float temperature;

    public Message() {
    }

    private Message(Parcel in) {
        this.deviceDescription = in.readString();
        this.deviceID = in.readString();
        this.deviceType = in.readString();
        this.lampStatus = in.readString();
        this.temperature = in.readFloat();
        this.humidity = in.readFloat();
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
        parcel.writeFloat(temperature);
        parcel.writeFloat(humidity);
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

    public boolean isFanStatus() {
        return fanStatus;
    }

    public void setFanStatus(boolean fanStatus) {
        this.fanStatus = fanStatus;
    }

    public int getFanSpeed() {
        return fanSpeed;
    }

    public void setFanSpeed(int fanSpeed) {
        this.fanSpeed = fanSpeed;
    }

    public int getFanMode() {
        return fanMode;
    }

    public void setFanMode(int fanMode) {
        this.fanMode = fanMode;
    }

    public boolean isRotation() {
        return rotation;
    }

    public void setRotation(boolean rotation) {
        this.rotation = rotation;
    }

    public boolean isIon() {
        return ion;
    }

    public void setIon(boolean ion) {
        this.ion = ion;
    }

    public boolean isTvStatus() {
        return tvStatus;
    }

    public void setTvStatus(boolean tvStatus) {
        this.tvStatus = tvStatus;
    }

    public int getRemoteOption() {
        return remoteOption;
    }

    public void setRemoteOption(int remoteOption) {
        this.remoteOption = remoteOption;
    }

    public float getHumidity() {
        return humidity;
    }

    public void setHumidity(float humidity) {
        this.humidity = humidity;
    }

    public float getTemperature() {
        return temperature;
    }

    public void setTemperature(float temperature) {
        this.temperature = temperature;
    }

    public String getRelayID() {
        return relayID;
    }

    public void setRelayID(String relayID) {
        this.relayID = relayID;
    }

    public Message returnRemoteStatus() {
        Message message = new Message();
        message.setFanMode(getFanMode());
        message.setFanSpeed(getFanSpeed());
        message.setFanStatus(isFanStatus());
        message.setTvStatus(isTvStatus());
        message.setRotation(isRotation());
        message.setIon(isIon());
        message.setAction(getAction());
        return message;
    }

    public void setRemoteStatus(Message message) {
        setFanMode(message.getFanMode());
        setFanSpeed(message.getFanSpeed());
        setFanStatus(message.isFanStatus());
        setTvStatus(message.isTvStatus());
        setRotation(message.isRotation());
        setAction(message.getAction());
        setIon(message.isIon());
    }

    public void setSensorStatus(Message message) {
        setHumidity(message.getHumidity());
        setTemperature(message.getTemperature());
        setAction(message.getAction());
    }

    public Message returnSensorStatus() {
        Message message = new Message();
        message.setHumidity(getHumidity());
        message.setTemperature(getTemperature());
        message.setAction(getAction());
        return message;
    }


}
