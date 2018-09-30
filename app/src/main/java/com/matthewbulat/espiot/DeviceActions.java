package com.matthewbulat.espiot;


import android.app.AlertDialog;
import android.arch.persistence.room.Room;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.matthewbulat.espiot.Database.devices.DeviceDB;
import com.matthewbulat.espiot.Database.devices.DeviceTable;
import com.matthewbulat.espiot.Database.user.UserDB;
import com.matthewbulat.espiot.Database.user.UserTable;
import com.matthewbulat.espiot.RetrofitDIR.ApiUtils;
import com.matthewbulat.espiot.RetrofitDIR.Interfaces.IoTAPI;
import com.matthewbulat.espiot.Objects.ConstantValues;
import com.matthewbulat.espiot.Objects.Message;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class DeviceActions extends AppCompatActivity implements ConstantValues {

    private ToggleButton lampControl;
    private Message device;
    private UserDB userDB;
    private TextView deviceName;
    private IoTAPI ioTAPI;
    private UserTable user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ioTAPI = ApiUtils.getIoTService();
        setContentView(R.layout.activity_device_actions);
        device = getIntent().getParcelableExtra("device");
        userDB = Room.databaseBuilder(getApplicationContext(), UserDB.class, "userdb").allowMainThreadQueries().build();
        user = userDB.userDao().getUser().get(0);
        deviceName = findViewById(R.id.deviceDescriptionTextView);
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
                if (lampControl.isChecked()) {
                    action = "lampon";
                } else {
                    action = "lampoff";
                }

                deviceAction(device.getDeviceID(),user.getUserName(),user.getUserToken(),action);

            }
        });

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.device_options, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection

        switch (item.getItemId()) {
            case R.id.removedevice:
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setMessage(R.string.remove_device_dialog_message)
                        .setTitle(R.string.remove_device_dialog_title);
                builder.setPositiveButton(R.string.delete, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        Toast.makeText(getApplicationContext(), "Starting removal of device."
                                , Toast.LENGTH_SHORT).show();
                        deviceAction(device.getDeviceID(),user.getUserName(),user.getUserToken(),"removedevice");
                    }
                });
                builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        Toast.makeText(getApplicationContext(), "Starting removal of device canceled."
                                , Toast.LENGTH_LONG).show();
                    }
                });
                AlertDialog dialog = builder.create();
                dialog.show();
                return true;
            case R.id.deviceInformation:
                AlertDialog.Builder builder1 = new AlertDialog.Builder(this);

                List<UserTable> userTable = userDB.userDao().getUser();
                builder1 = new AlertDialog.Builder(this);
                builder1.setMessage(String.format("Your device token is: %s\n" +
                                "Your user name is: %s\n" +
                                "Your user token is: %s",
                        device.getDeviceID(),
                        userTable.get(0).getUserName(),
                        userTable.get(0).getUserToken()))
                        .setTitle(R.string.device_information_title);

                builder1.setPositiveButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.dismiss();
                    }
                });
                AlertDialog dialog1 = builder1.create();
                dialog1.show();
                return true;
            case R.id.updateName:
                AlertDialog.Builder builder2 = new AlertDialog.Builder(this);
                final EditText edittext = new EditText(this);
                builder2.setMessage("Please type device name:");
                builder2.setTitle("Change Device Name");

                builder2.setView(edittext);

                builder2.setPositiveButton("Update", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        String newName = edittext.getText().toString();
                        if (edittext.length() > 32) {
                            edittext.setError("Device name cannot be longer than 32 characters, current name is: " + edittext.length());
                        } else if (edittext.length() == 0) {
                            edittext.setError("Device name is empty.");
                        } else {
                            List<UserTable> user = userDB.userDao().getUser();
                            updateDeviceDescription(device.getDeviceID(),user.get(0).getUserName(),user.get(0).getUserToken(),"updatedevicedescription",newName);
                        }
                    }
                });

                builder2.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                    }
                });
                builder2.show();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public void deviceAction(String deviceID,String userName,String userToken,String lampAction){
        ioTAPI.lampActions(deviceID,userName,userToken,lampAction).enqueue(new Callback<Message>() {
            @Override
            public void onResponse(Call<Message> call, Response<Message> response) {

                if(response.isSuccessful()) {

                    System.out.println(response.body().getAction());
                    finishAPIAction(response.body());
                }else {
                    //todo test this
                }

            }

            @Override
            public void onFailure(Call<Message> call, Throwable t) {

            }
        });
    }

    public void updateDeviceDescription(String deviceID, String userName, String userToken, String lampAction, String newDeviceDecription){
        ioTAPI.updateDeviceDescription(deviceID,userName,userToken,lampAction,newDeviceDecription).enqueue(new Callback<Message>() {
            @Override
            public void onResponse(Call<Message> call, Response<Message> response) {

                if(response.isSuccessful()) {

                    System.out.println(response.body().getAction());
                    finishAPIAction(response.body());
                }else {
                    //todo test this
                }
            }

            @Override
            public void onFailure(Call<Message> call, Throwable t) {

            }
        });
    }

    private void finishAPIAction(Message message){
        Context context = getApplicationContext();
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
            case "deviceNameUpdated": {
                DeviceDB deviceDB = Room.databaseBuilder(context, DeviceDB.class, "devicedb").allowMainThreadQueries().build();
                DeviceTable deviceTable = new DeviceTable();
                Toast.makeText(context, "Device updated successfully."
                        , Toast.LENGTH_LONG).show();
                deviceName.setText(message.getDeviceDescription());
                deviceTable.setDeviceID(device.getDeviceID());//todo include device ID in the http reply
                deviceTable.setDeviceDescription(message.getDeviceDescription());
                deviceTable.setDeviceType(message.getDeviceType());
                deviceDB.devicesDao().deleteDevice(deviceTable);
            }
            break;
            case "deviceremoved": {
                DeviceDB deviceDB = Room.databaseBuilder(context, DeviceDB.class, "devicedb").allowMainThreadQueries().build();
                DeviceTable deviceTable = new DeviceTable();
                Toast.makeText(context, "Device removed successfully."
                        , Toast.LENGTH_LONG).show();
                deviceTable.setDeviceID(message.getDeviceID());
                deviceTable.setDeviceDescription(message.getDeviceDescription());
                deviceTable.setDeviceType(message.getDeviceType());

                deviceDB.devicesDao().deleteDevice(deviceTable);

                Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
                finish();
            }
            break;
        }
    }



//    public class DeviceActivation extends AsyncTask<Void, Void, Message> implements ConstantValues {
//
//        private String action;
//        private String deviceID;
//        private Context context;
//        private String newDeviceName;
//
//
//        DeviceActivation(String action, String deviceID) {
//            this.action = action;
//            this.deviceID = deviceID;
//            context = getApplicationContext();
//        }
//
//        DeviceActivation(String action, String deviceID, String newDeviceName) {
//            this.action = action;
//            this.deviceID = deviceID;
//            this.newDeviceName = newDeviceName;
//            context = getApplicationContext();
//        }
//
//        @Override
//        protected Message doInBackground(Void... params) {
//            Message message = null;
//
//            List<UserTable> user = userDB.userDao().getUser();
//            String stringUrl;
//            switch (action) {
//                case "updatedevicedescription":
//                    stringUrl = String.format("https://%s/lampAction?" +
//                                    "deviceId=%s&" +
//                                    "userName=%s&" +
//                                    "userToken=%s&" +
//                                    "lampAction=%s&" +
//                                    "newDeviceDescription=%s"
//                            , SYSTEM_DOMAIN,
//                            deviceID,
//                            user.get(0).getUserName(),
//                            user.get(0).getUserToken(),
//                            action,
//                            newDeviceName);
//                    break;
//                default:
//                    stringUrl = String.format("https://%s/lampAction?" +
//                                    "deviceId=%s&" +
//                                    "userName=%s&" +
//                                    "userToken=%s&" +
//                                    "lampAction=%s"
//                            , SYSTEM_DOMAIN,
//                            deviceID,
//                            user.get(0).getUserName(),
//                            user.get(0).getUserToken(),
//                            action);
//            }
//
//            URL url;
//            try {
//                url = new URL(stringUrl);
//
//
//                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
//
//                String text;
//                BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
//                StringBuilder sb = new StringBuilder();
//                String line;
//
//                // Read Server Response
//                while ((line = reader.readLine()) != null) {
//                    // Append server response in string
//                    sb.append(line).append("\n");
//                }
//                reader.close();
//                text = sb.toString();
//                conn.disconnect();
//
//                message = new Message().decode(text);
//
//            } catch (MalformedURLException e) {
//                e.printStackTrace();
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//            return message;
//        }
//
//        @Override
//        protected void onPostExecute(final Message message) {
//
//
//            switch (message.getAction()) {
//                case "deviceNotConnectedToSystem":
//                    Toast.makeText(context, "Your ESP device is disconnected from the network."
//                            , Toast.LENGTH_LONG).show();
//                    break;
//                case "communicationError":
//                    Toast.makeText(context, "Internal network error, please try again in few moments."
//                            , Toast.LENGTH_LONG).show();
//                    break;
//                case "IncorrectCredentials":
//                    Toast.makeText(context, "Please try again in few moments."
//                            , Toast.LENGTH_LONG).show();
//                    break;
//                case "deviceNameUpdated": {
//                    DeviceDB deviceDB = Room.databaseBuilder(context, DeviceDB.class, "devicedb").allowMainThreadQueries().build();
//                    DeviceTable deviceTable = new DeviceTable();
//                    Toast.makeText(context, "Device updated successfully."
//                            , Toast.LENGTH_LONG).show();
//                    deviceName.setText(newDeviceName);
//                    deviceTable.setDeviceID(deviceID);
//                    deviceTable.setDeviceDescription(newDeviceName);
//                    deviceTable.setDeviceType(message.getDeviceType());
//                    deviceDB.devicesDao().deleteDevice(deviceTable);
//                }
//                break;
//                case "deviceremoved": {
//                    DeviceDB deviceDB = Room.databaseBuilder(context, DeviceDB.class, "devicedb").allowMainThreadQueries().build();
//                    DeviceTable deviceTable = new DeviceTable();
//                    Toast.makeText(context, "Device removed successfully."
//                            , Toast.LENGTH_LONG).show();
//                    deviceTable.setDeviceID(deviceID);
//                    deviceTable.setDeviceDescription(message.getDeviceDescription());
//                    deviceTable.setDeviceType(message.getDeviceType());
//
//                    deviceDB.devicesDao().deleteDevice(deviceTable);
//
//                    Intent intent = new Intent(getApplicationContext(), MainActivity.class);
//                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
//                    startActivity(intent);
//                    finish();
//                }
//                break;
//            }
//        }
//        //showProgress(false);
//
//
//        @Override
//        protected void onCancelled() {
//            //showProgress(false);
//        }
//    }


}
