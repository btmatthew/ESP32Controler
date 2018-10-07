package com.matthewbulat.espiot.RetrofitDIR.Interfaces;

import com.matthewbulat.espiot.Objects.Message;
import com.matthewbulat.espiot.Objects.User;

import io.reactivex.Observable;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Headers;
import retrofit2.http.POST;
import retrofit2.http.Query;

public interface IoTAPI {

    @GET("/lampAction")
    Call<Message> lampActions(@Query("deviceId") String deviceId,
                              @Query("userName") String userName,
                              @Query("userToken") String userToken,
                              @Query("lampAction") String lampAction);

    @GET("/lampAction")
    Call<Message> deviceList(@Query("userName") String userName,
                    @Query("userToken") String userToken,
                    @Query("lampAction") String lampAction);

    @GET("/lampAction")
    Call<Message> updateDeviceDescription(
            @Query("deviceId") String deviceId,
            @Query("userName") String userName,
            @Query("userToken") String userToken,
            @Query("lampAction") String lampAction,
            @Query("newDeviceDescription") String newDeviceDescription);

    @POST("/userLogin")
    @Headers("Content-Type: application/json")
    Observable<User> userLogin(@Body User body);

    @POST("/userRegister")
    @Headers("Content-Type: application/json")
    Observable<User> userRegister(@Body User body);

}
