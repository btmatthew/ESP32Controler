package com.matthewbulat.espiot.RetrofitDIR;

import com.matthewbulat.espiot.PreferenceReader;
import com.matthewbulat.espiot.RetrofitDIR.Interfaces.IoTAPI;

public class ApiUtils {

    public IoTAPI getIoTService() {
        String BASE_URL = String.format("%s%s:%s",
                new PreferenceReader().readPreference("api_address_prefix"),
                new PreferenceReader().readPreference("server_address"),
                new PreferenceReader().readPreference("port_number_web_server"));
        return new APIClient().getClient(BASE_URL).create(IoTAPI.class);
    }
}
