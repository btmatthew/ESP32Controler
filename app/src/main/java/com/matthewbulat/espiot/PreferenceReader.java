package com.matthewbulat.espiot;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.matthewbulat.espiot.JavaClasses.GlobalApplication;

public class PreferenceReader {


    public String readPreference(String preferenceID) {
        Context context = GlobalApplication.getAppContext();
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(context);

        return settings.getString(preferenceID, returnDefault(preferenceID));
    }

    public String returnDefault(String preferenceID) {
        switch (preferenceID) {
            case "api_address_prefix":
                return "https://";
            case "server_address":
                return "matthewbulat.com";
            case "port_number":
                return "8080";
            case "port_number_web_server":
                return "80";
        }
        return "";
    }

}
