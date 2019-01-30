package com.matthewbulat.espiot.RetrofitDIR;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.matthewbulat.espiot.JavaClasses.GlobalApplication;
import com.matthewbulat.espiot.RetrofitDIR.Interfaces.IoTAPI;

public class ApiUtils {


    public static IoTAPI getIoTService() {
        String BASE_URL = String.format("https://%s:80",new ApiUtils().readPreference());
        return APIClient.getClient(BASE_URL).create(IoTAPI.class);
    }

    private String readPreference(){
        Context context = GlobalApplication.getAppContext();
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(context);
        return settings.getString("server_address", "matthewbulat.com");
    }

}
