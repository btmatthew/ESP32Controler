package com.matthewbulat.espiot.RetrofitDIR.Interfaces;

import com.matthewbulat.espiot.Objects.Message;
import com.matthewbulat.espiot.Objects.User;

import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
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
    @FormUrlEncoded
    Call<User> userLogin(@Field("userEmail") String userEmail,
                        @Field("password") String password);

}
