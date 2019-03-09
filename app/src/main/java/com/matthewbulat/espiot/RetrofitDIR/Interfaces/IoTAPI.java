package com.matthewbulat.espiot.RetrofitDIR.Interfaces;

import com.matthewbulat.espiot.Objects.Message;
import com.matthewbulat.espiot.Objects.User;

import io.reactivex.Observable;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Headers;
import retrofit2.http.POST;
import retrofit2.http.Query;

public interface IoTAPI {

    @GET("/lampAction")
    Observable<Message> lampActions(
            @Query("deviceId") String deviceId,
            @Query("userName") String userName,
            @Query("userToken") String userToken,
            @Query("lampAction") String lampAction);

    @GET("/lampAction")
    Observable<Message> deviceList(
            @Query("userName") String userName,
            @Query("userToken") String userToken,
            @Query("lampAction") String lampAction);

    @GET("/lampAction")
    Observable<Message> updateDeviceDescription(
            @Query("deviceId") String deviceId,
            @Query("userName") String userName,
            @Query("userToken") String userToken,
            @Query("lampAction") String lampAction,
            @Query("newDeviceDescription") String newDeviceDescription);

    @GET("/lampAction")
    Observable<Message> lampActions(
            @Query("deviceId") String deviceId,
            @Query("userName") String userName,
            @Query("userToken") String userToken,
            @Query("lampAction") String lampAction,
            @Query("relayID") String relayID);

    @GET("/remoteAction")
    Observable<Message> remoteAction(
            @Query("action") String action,
            @Query("deviceId") String deviceId,
            @Query("userName") String userName,
            @Query("userToken") String userToken,
            @Query("remoteOption") int fanOption);

    @GET("/sensorAction")
    Observable<Message> sensorAction(
            @Query("action") String action,
            @Query("deviceId") String deviceId,
            @Query("userName") String userName,
            @Query("userToken") String userToken);

    @POST("/userLogin")
    @Headers("Content-Type: application/json")
    Observable<User> userLogin(@Body User body);

    @POST("/userRegister")
    @Headers("Content-Type: application/json")
    Observable<User> userRegister(@Body User body);

}
