package com.matthewbulat.espiot;


import android.arch.persistence.room.Room;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.matthewbulat.espiot.Database.user.UserDB;
import com.matthewbulat.espiot.Database.user.UserTable;
import com.matthewbulat.espiot.Objects.ConstantValues;
import com.matthewbulat.espiot.Objects.Message;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

public class DeviceActions extends AppCompatActivity implements ConstantValues {

    private ToggleButton lampControl;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_device_actions);

        final Message device = getIntent().getParcelableExtra("device");

        TextView deviceName = findViewById(R.id.deviceDescriptionTextView);
        deviceName.setText(device.getDeviceDescription());
        lampControl = findViewById(R.id.turnLampButton);
        if (device.getLampStatus().equals("on")) {
            lampControl.setChecked(true);
        } else {
            lampControl.setChecked(false);
        }
        lampControl.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String action;
                if(lampControl.isChecked()){
                    action="lampOn";
                }else{
                    action="lampOff";
                }

                DeviceActivation deviceActivation = new DeviceActivation(action,device.getDeviceID());
                deviceActivation.execute((Void) null);
            }
        });

    }

    public class DeviceActivation extends AsyncTask<Void, Void, Message> implements ConstantValues {

        private String action;
        private String deviceID;
        private Context context;


        DeviceActivation(String action,String deviceID) {
            this.action = action;
            this.deviceID = deviceID;
            context= getApplicationContext();
        }

        @Override
        protected Message doInBackground(Void... params) {
            Message message = null;
            UserDB userDB = Room.databaseBuilder(context, UserDB.class, "userdb").allowMainThreadQueries().build();
            List<UserTable> user = userDB.userDao().getUser();

            String stringUrl = String.format("http://%s/lampAction?" +
                            "deviceId=%s&" +
                            "userName=%s&" +
                            "userToken=%s&" +
                            "lampAction=%s"
                    , SYSTEM_DOMAIN,
                    deviceID,
                    user.get(0).getUserName(),
                    user.get(0).getUserToken(),
                    action);

            URL url;
            try {
                url = new URL(stringUrl);


                HttpURLConnection conn = (HttpURLConnection) url.openConnection();

                String text;
                BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                StringBuilder sb = new StringBuilder();
                String line;

                // Read Server Response
                while ((line = reader.readLine()) != null) {
                    // Append server response in string
                    sb.append(line).append("\n");
                }
                reader.close();
                text = sb.toString();
                conn.disconnect();

                message = new Message().decode(text);

            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return message;
        }

        @Override
        protected void onPostExecute(final Message message) {

            switch (message.getAction()) {
                case "deviceNotConnectedToSystem":
                    Toast.makeText(context, "Your ESP device is disconnected from the network."
                            , Toast.LENGTH_LONG).show();
                    break;
                case "communicationError":
                    Toast.makeText(context, "Internal network error, please try again in few moments."
                            , Toast.LENGTH_LONG).show();
                    break;
                case "IncorrectCredentials":
                    Toast.makeText(context, "Please try again in few moments."
                            , Toast.LENGTH_LONG).show();
                    break;
//                case "lampOn":
//                    lampControl.setChecked(true);
//                    break;
//                case "lampOff":
//                    lampControl.setChecked(false);
//                    break;
            }
        }
        //showProgress(false);


        @Override
        protected void onCancelled() {
            //showProgress(false);
        }
    }



}
