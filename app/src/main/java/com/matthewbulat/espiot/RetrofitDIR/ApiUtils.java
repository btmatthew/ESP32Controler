package com.matthewbulat.espiot.RetrofitDIR;

import com.matthewbulat.espiot.RetrofitDIR.Interfaces.IoTAPI;

public class ApiUtils {


    private static final String BASE_URL = "https://matthewbulat.com:80";

    public static IoTAPI getIoTService() {
        return APIClient.getClient(BASE_URL).create(IoTAPI.class);
    }


}
