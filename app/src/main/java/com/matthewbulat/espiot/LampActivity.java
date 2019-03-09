package com.matthewbulat.espiot;


import android.app.AlertDialog;
import android.arch.persistence.room.Room;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.matthewbulat.espiot.Database.devices.DeviceDB;
import com.matthewbulat.espiot.Database.devices.DeviceTable;
import com.matthewbulat.espiot.Objects.User;
import com.matthewbulat.espiot.RetrofitDIR.ApiUtils;
import com.matthewbulat.espiot.RetrofitDIR.Interfaces.IoTAPI;
import com.matthewbulat.espiot.Objects.ConstantValues;
import com.matthewbulat.espiot.Objects.Message;

import io.reactivex.Completable;
import io.reactivex.CompletableObserver;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.observers.DisposableObserver;
import io.reactivex.schedulers.Schedulers;

public class LampActivity extends AppCompatActivity implements ConstantValues {
    private ToggleButton lampControl;
    private ToggleButton lampControl1;
    private ToggleButton lampControl2;
    private ToggleButton lampControlAll;
    private Message device;
    private TextView deviceName;
    private IoTAPI ioTAPI;
    private User user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ioTAPI = new ApiUtils().getIoTService();
        device = getIntent().getParcelableExtra("device");
        user = getIntent().getParcelableExtra("user");
        switch (device.getDeviceType()) {
            case "Lamp":
                setContentView(R.layout.activity_lamp_actions);
                setupLayoutForLamp();
                break;
            case "2InputRelayLamp":
                setContentView(R.layout.activity_lamp_actions2);
                setupLayoutFor2RelayLamp();
                break;
            case "3InputRelayLamp":
                setContentView(R.layout.activity_lamp_actions3);
                setupLayoutFor3RelayLamp();
                break;
        }
    }

    private void setupLayoutForLamp(){
        Log.i("deviceDetails", String.format("device description is %s", device.getDeviceDescription()));
        Log.i("userDetails", String.format("user name is %s, user token is %s", user.getUserName(), user.getUserToken()));

        deviceName = findViewById(R.id.deviceDescriptionTextView);
        deviceName.setText(device.getDeviceDescription());
        lampControl = findViewById(R.id.turnLampButton);
        if (device.getLampStatus().equals("on")) {
            lampControl.setChecked(true);
        } else {
            lampControl.setChecked(false);
        }
        lampControl.setOnClickListener(view -> {
            String action;
            if (lampControl.isChecked()) {
                action = "lampon";
            } else {
                action = "lampoff";
            }
            Message message = new Message();
            message.setAction(action);
            message.setDeviceID(device.getDeviceID());
            deviceAction(message);

        });
    }

    private void setupLayoutFor2RelayLamp(){
        Log.i("deviceDetails", String.format("device description is %s", device.getDeviceDescription()));
        Log.i("userDetails", String.format("user name is %s, user token is %s", user.getUserName(), user.getUserToken()));

        //deviceName = findViewById(R.id.deviceDescriptionTextView);
        //deviceName.setText(device.getDeviceDescription());
        lampControl = findViewById(R.id.turnLampButton1);
        lampControl1 = findViewById(R.id.turnLampButton2);
        lampControlAll = findViewById(R.id.turnLampButton3);
        String[] lampsStatus = device.getLampStatus().split(",");

        if (lampsStatus[0].equals("on")) {
            lampControl.setChecked(true);
        } else {
            lampControl.setChecked(false);
        }

        if (lampsStatus[1].equals("on")) {
            lampControl1.setChecked(true);
        } else {
            lampControl1.setChecked(false);
        }

        if (lampsStatus[0].equals("on") && lampsStatus[1].equals("on")) {
            lampControlAll.setChecked(true);
        } else {
            lampControlAll.setChecked(false);
        }

        lampControlAll.setOnClickListener(view -> {
            String action;
            if (lampControlAll.isChecked()) {
                action = "lampon";
            } else {
                action = "lampoff";
            }
            Message message = new Message();
            message.setAction(action);
            message.setDeviceID(device.getDeviceID());
            message.setRelayID("0");
            deviceActionRelay(message);
        });

        lampControl.setOnClickListener(view -> {
            String action;
            if (lampControl.isChecked()) {
                action = "lampon";
            } else {
                action = "lampoff";
            }
            Message message = new Message();
            message.setAction(action);
            message.setDeviceID(device.getDeviceID());
            message.setRelayID("1");
            deviceActionRelay(message);
        });

        lampControl1.setOnClickListener(view -> {
            String action;
            if (lampControl.isChecked()) {
                action = "lampon";
            } else {
                action = "lampoff";
            }
            Message message = new Message();
            message.setAction(action);
            message.setDeviceID(device.getDeviceID());
            message.setRelayID("2");
            deviceActionRelay(message);
        });
    }

    private void setupLayoutFor3RelayLamp(){
        Log.i("deviceDetails", String.format("device description is %s", device.getDeviceDescription()));
        Log.i("userDetails", String.format("user name is %s, user token is %s", user.getUserName(), user.getUserToken()));

        //deviceName = findViewById(R.id.deviceDescriptionTextView);
        //deviceName.setText(device.getDeviceDescription());
        lampControl = findViewById(R.id.turnLampButton4);
        lampControl1 = findViewById(R.id.turnLampButton5);
        lampControl2 = findViewById(R.id.turnLampButton6);
        lampControlAll = findViewById(R.id.turnLampButton7);
        String[] lampsStatus = device.getLampStatus().split(",");

        if (lampsStatus[0].equals("on")) {
            lampControl.setChecked(true);
        } else {
            lampControl.setChecked(false);
        }

        if (lampsStatus[1].equals("on")) {
            lampControl1.setChecked(true);
        } else {
            lampControl1.setChecked(false);
        }

        if (lampsStatus[0].equals("on") && lampsStatus[1].equals("on")) {
            lampControlAll.setChecked(true);
        } else {
            lampControlAll.setChecked(false);
        }

        lampControlAll.setOnClickListener(view -> {
            String action;
            if (lampControlAll.isChecked()) {
                action = "lampon";
            } else {
                action = "lampoff";
            }
            Message message = new Message();
            message.setAction(action);
            message.setDeviceID(device.getDeviceID());
            message.setRelayID("0");
            deviceActionRelay(message);
        });

        lampControl.setOnClickListener(view -> {
            String action;
            if (lampControl.isChecked()) {
                action = "lampon";
            } else {
                action = "lampoff";
            }
            Message message = new Message();
            message.setAction(action);
            message.setDeviceID(device.getDeviceID());
            message.setRelayID("1");
            deviceActionRelay(message);
        });

        lampControl1.setOnClickListener(view -> {
            String action;
            if (lampControl.isChecked()) {
                action = "lampon";
            } else {
                action = "lampoff";
            }
            Message message = new Message();
            message.setAction(action);
            message.setDeviceID(device.getDeviceID());
            message.setRelayID("2");
            deviceActionRelay(message);
        });
        lampControl2.setOnClickListener(view -> {
            String action;
            if (lampControl2.isChecked()) {
                action = "lampon";
            } else {
                action = "lampoff";
            }
            Message message = new Message();
            message.setAction(action);
            message.setDeviceID(device.getDeviceID());
            message.setRelayID("3");
            deviceActionRelay(message);
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
                        Message message = new Message();
                        message.setDeviceID(device.getDeviceID());
                        message.setAction("removedevice");
                        deviceAction(message);
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


                builder1 = new AlertDialog.Builder(this);
                builder1.setMessage(String.format("Your device token is: %s\n" +
                                "Your user name is: %s\n" +
                                "Your user token is: %s",
                        device.getDeviceID(),
                        user.getUserName(),
                        user.getUserToken()))
                        .setTitle(R.string.device_information_title);

                builder1.setPositiveButton(R.string.cancel, (dialog12, id) -> dialog12.dismiss());
                AlertDialog dialog1 = builder1.create();
                dialog1.show();
                return true;
            case R.id.updateName:
                AlertDialog.Builder builder2 = new AlertDialog.Builder(this);
                final EditText edittext = new EditText(this);
                builder2.setMessage("Please type device name:");
                builder2.setTitle("Change Device Name");

                builder2.setView(edittext);

                builder2.setPositiveButton("Update", (dialog22, whichButton) -> {
                    String newName = edittext.getText().toString();
                    if (edittext.length() > 32) {
                        edittext.setError("Device name cannot be longer than 32 characters, current name is: " + edittext.length());
                    } else if (edittext.length() == 0) {
                        edittext.setError("Device name is empty.");
                    } else {
                        Message message = new Message();
                        message.setDeviceID(device.getDeviceID());
                        message.setAction("updatedevicedescription");
                        message.setDeviceDescription(newName);
                        updateDeviceDescription(message, user);
                    }
                });

                builder2.setNegativeButton("Cancel", (dialog2, whichButton) -> {
                });
                builder2.show();
                return true;
            case R.id.applicationSettings:
                startActivity(new Intent(LampActivity.this, Settings.class));
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public void deviceAction(Message message) {
        CompositeDisposable compositeDisposable = new CompositeDisposable();
        compositeDisposable.add(ioTAPI.lampActions(message.getDeviceID(), user.getUserName(), user.getUserToken(), message.getAction())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(new DisposableObserver<Message>() {
                                   @Override
                                   public void onComplete() {
                                       compositeDisposable.dispose();
                                   }

                                   @Override
                                   public void onError(Throwable e) {
                                       Log.e("Login", e.getMessage());
                                   }

                                   @Override
                                   public void onNext(Message value) {
                                       finishAPIAction(value);
                                       Log.i("Device Action", "Device action request successful");
                                   }
                               }
                )
        );
    }

    public void deviceActionRelay(Message message) {
        CompositeDisposable compositeDisposable = new CompositeDisposable();
        compositeDisposable.add(ioTAPI.lampActions(message.getDeviceID(), user.getUserName(), user.getUserToken(), message.getAction(),message.getRelayID())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(new DisposableObserver<Message>() {
                                   @Override
                                   public void onComplete() {
                                       compositeDisposable.dispose();
                                   }

                                   @Override
                                   public void onError(Throwable e) {
                                       Log.e("Login", e.getMessage());
                                   }

                                   @Override
                                   public void onNext(Message value) {
                                       finishAPIAction(value);
                                       Log.i("Device Action", "Device action request successful");
                                   }
                               }
                )
        );
    }

    public void updateDeviceDescription(Message message, User user) {
        CompositeDisposable compositeDisposable = new CompositeDisposable();
        compositeDisposable.add(ioTAPI.updateDeviceDescription(message.getDeviceID(), user.getUserName(), user.getUserToken(), message.getAction(), message.getDeviceDescription())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(new DisposableObserver<Message>() {
                                   @Override
                                   public void onComplete() {
                                       compositeDisposable.dispose();
                                   }

                                   @Override
                                   public void onError(Throwable e) {
                                       Log.e("Login", e.getMessage());
                                   }

                                   @Override
                                   public void onNext(Message value) {
                                       Log.i("Device Action", "Device description changed successfully");
                                       finishAPIAction(value);
                                   }
                               }
                )
        );
    }

    private void finishAPIAction(Message message) {
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
                getDevicesDetails(message.getDeviceDescription(), device.getDeviceID());
            }
            break;
            case "deviceremoved": {
                DeviceTable deviceTable = new DeviceTable();
                deviceTable.setDeviceID(message.getDeviceID());
                deviceTable.setDeviceDescription(message.getDeviceDescription());
                deviceTable.setDeviceType(message.getDeviceType());
                deleteDeviceFromSystem(deviceTable);
            }
            break;
        }
    }


    private void deleteDeviceFromSystem(DeviceTable deviceTable) {
        DeviceDB deviceDB = Room.databaseBuilder(getApplicationContext(), DeviceDB.class, "devicedb").build();

        Completable.fromAction(() -> deviceDB.devicesDao().deleteDevice(deviceTable))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new CompletableObserver() {
                    @Override
                    public void onSubscribe(Disposable d) {

                    }

                    @Override
                    public void onComplete() {
                        deviceDB.close();
                        Toast.makeText(getApplicationContext(), "Device removed successfully."
                                , Toast.LENGTH_LONG).show();
                        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(intent);
                        finish();
                    }

                    @Override
                    public void onError(Throwable e) {
                        deviceDB.close();
                        Log.e("Database", e.getMessage());
                    }
                });
    }

    private void getDevicesDetails(String newDeviceName, String deviceID) {
        DeviceDB deviceDB = Room.databaseBuilder(getApplicationContext(), DeviceDB.class, "devicedb").build();

        Completable.fromAction(() -> deviceDB.devicesDao().updateDeviceName(newDeviceName, deviceID))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new CompletableObserver() {
                    @Override
                    public void onSubscribe(Disposable d) {

                    }

                    @Override
                    public void onComplete() {
                        deviceDB.close();
                        Toast.makeText(getApplicationContext(), "Device updated successfully."
                                , Toast.LENGTH_LONG).show();
                        deviceName.setText(newDeviceName);
                    }

                    @Override
                    public void onError(Throwable e) {
                        deviceDB.close();
                        Log.e("Database", e.getMessage());
                    }
                });
    }
}
