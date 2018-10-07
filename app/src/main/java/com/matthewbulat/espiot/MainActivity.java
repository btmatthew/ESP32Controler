package com.matthewbulat.espiot;

import android.arch.persistence.room.Room;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.matthewbulat.espiot.Database.devices.DeviceDB;
import com.matthewbulat.espiot.Database.devices.DeviceTable;
import com.matthewbulat.espiot.Database.user.UserDB;
import com.matthewbulat.espiot.Database.user.UserTable;
import com.matthewbulat.espiot.Objects.ConstantValues;
import com.matthewbulat.espiot.Objects.Message;
import com.matthewbulat.espiot.RetrofitDIR.ApiUtils;
import com.matthewbulat.espiot.RetrofitDIR.Interfaces.IoTAPI;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, ConstantValues {

    DeviceRecyclerViewAdapter deviceRecyclerViewAdapter;
    private ArrayList<Message> arrayListOfDevices;
    private RecyclerView deviceRecyclerView;
    private DeviceDB deviceDB;
    private SwipeRefreshLayout swipeContainer;
    private IoTAPI ioTAPI;
    List<UserTable> userTables;

//todo add long press to remove a device
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        UserDB userDB = Room.databaseBuilder(getApplicationContext(), UserDB.class, "userdb").allowMainThreadQueries().build();
        deviceDB = Room.databaseBuilder(getApplicationContext(), DeviceDB.class, "devicedb").allowMainThreadQueries().build();
        ioTAPI = ApiUtils.getIoTService();
        userTables = userDB.userDao().getUser();
        List<DeviceTable> deviceTables;
        if (userTables.size() == 0) {
            Intent intent = new Intent(MainActivity.this, LoginActivity.class);
            getApplicationContext().startActivity(intent);
        } else {

            setContentView(R.layout.activity_main);
            Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
            setSupportActionBar(toolbar);


            DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);


            ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                    this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
            drawer.addDrawerListener(toggle);
            toggle.syncState();

            NavigationView navigationView = findViewById(R.id.nav_view);
            navigationView.setNavigationItemSelectedListener(this);
            View headerView = navigationView.getHeaderView(0);
            TextView navUsername = headerView.findViewById(R.id.user_name_text_field);
            navUsername.setText(userDB.userDao().getUser().get(0).getUserName());


            deviceTables = deviceDB.devicesDao().getDevices();
            if (deviceTables.size() == 0) {
                loadDeviceList(userTables.get(0).getUserName(),userTables.get(0).getUserToken(),"requestDevicesList","fresh");
            } else {
                arrayListOfDevices = new ArrayList<>();
                for (DeviceTable deviceTable : deviceTables) {
                    Message message = new Message();
                    message.setDeviceID(deviceTable.getDeviceID());
                    message.setDeviceType(deviceTable.getDeviceType());
                    message.setDeviceDescription(deviceTable.getDeviceDescription());
                    arrayListOfDevices.add(message);
                }
                deviceRecyclerView = findViewById(R.id.device_recycler_view);
                deviceRecyclerViewAdapter = new DeviceRecyclerViewAdapter(arrayListOfDevices, getApplicationContext());
                deviceRecyclerView.setAdapter(deviceRecyclerViewAdapter);
                deviceRecyclerView.setLayoutManager(new LinearLayoutManager(getApplicationContext()));

            }

            swipeContainer = findViewById(R.id.swipeContainer);
            swipeContainer.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
                @Override
                public void onRefresh() {
                    // Your code to refresh the list here.
                    // Make sure you call swipeContainer.setRefreshing(false)
                    // once the network request has completed successfully.
                    loadDeviceList(userTables.get(0).getUserName(),userTables.get(0).getUserToken(),"requestDevicesList","refresh");
                    //https://guides.codepath.com/android/implementing-pull-to-refresh-guide#overview
                }
            });

        }
    }

    @Override
    public void onResume() {
        if(swipeContainer != null){
            swipeContainer.setRefreshing(true);
            loadDeviceList(userTables.get(0).getUserName(),userTables.get(0).getUserToken(),"requestDevicesList","refresh");
        }


        super.onResume();
    }


    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.addNewDevice) {
            Intent intent = new Intent(MainActivity.this, AddNewDevice.class);
            startActivity(intent);
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }


    public void loadDeviceList(String userName, String userToken, String lampAction, final String typeOfRequest){
        ioTAPI.deviceList(userName,userToken,lampAction).enqueue(new Callback<Message>() {
            @Override
            public void onResponse(Call<Message> call, Response<Message> response) {

                if(response.isSuccessful()) {
                    switch(typeOfRequest){
                        case "fresh":
                            arrayListOfDevices = response.body().getDeviceList();
                            deviceRecyclerView = findViewById(R.id.device_recycler_view);
                            deviceRecyclerViewAdapter = new DeviceRecyclerViewAdapter(arrayListOfDevices, getApplicationContext());
                            deviceRecyclerView.setAdapter(deviceRecyclerViewAdapter);
                            deviceRecyclerView.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
                            for (Message message1 : arrayListOfDevices) {
                                DeviceTable deviceTable = new DeviceTable();
                                deviceTable.setDeviceDescription(message1.getDeviceDescription());
                                deviceTable.setDeviceID(message1.getDeviceID());
                                deviceTable.setDeviceType(message1.getDeviceType());
                                deviceDB.devicesDao().addDevice(deviceTable);
                            }
                            break;
                        case "refresh":
                            arrayListOfDevices = response.body().getDeviceList();
                            deviceRecyclerView = findViewById(R.id.device_recycler_view);
                            if(deviceRecyclerViewAdapter!=null){
                                deviceRecyclerViewAdapter.clear();
                                deviceRecyclerViewAdapter.addAll(arrayListOfDevices);
                            }else{
                                deviceRecyclerViewAdapter = new DeviceRecyclerViewAdapter(arrayListOfDevices, getApplicationContext());
                            }
                            deviceRecyclerView.setAdapter(deviceRecyclerViewAdapter);
                            deviceRecyclerView.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
                            swipeContainer.setRefreshing(false);
                            deviceDB.devicesDao().nukeTable();
                            for (Message message1 : arrayListOfDevices) {
                                DeviceTable deviceTable = new DeviceTable();
                                deviceTable.setDeviceDescription(message1.getDeviceDescription());
                                deviceTable.setDeviceID(message1.getDeviceID());
                                deviceTable.setDeviceType(message1.getDeviceType());
                                deviceDB.devicesDao().addDevice(deviceTable);
                            }
                            break;
                    }
                }else {
                    swipeContainer.setRefreshing(false);
                }
            }

            @Override
            public void onFailure(Call<Message> call, Throwable t) {
                swipeContainer.setRefreshing(false);
            }
        });
    }


//    public class DeviceListRequest extends AsyncTask<Void, Void, Message> {
//
//        private String typeOfRequest;
//
//        DeviceListRequest(String typeOfRequest) {
//            this.typeOfRequest = typeOfRequest;
//        }
//
//        @Override
//        protected Message doInBackground(Void... params) {
//            Message devices = null;
//            URL url;
//            try {
//                List<UserTable> user = userDB.userDao().getUser();
//                String stringUrl = String.format("https://%s/lampAction?" +
//                                "userName=%s&" +
//                                "userToken=%s&" +
//                                "lampAction=requestDevicesList"
//                        , SYSTEM_DOMAIN,
//                        user.get(0).getUserName(),
//                        user.get(0).getUserToken());
//
//                url = new URL(stringUrl);
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
//                devices = new Message().decode(text);
//
//            } catch (MalformedURLException e) {
//                e.printStackTrace();
//            } catch (ProtocolException e) {
//                e.printStackTrace();
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//            return devices;
//        }
//
//        @Override
//        protected void onPostExecute(final Message message) {
//            if(message != null) {
//                if (message.getDeviceList() != null) {
//                    if (!message.getDeviceList().isEmpty()) {
//                        if (typeOfRequest.equals("fresh")) {
//                            arrayListOfDevices = message.getDeviceList();
//                            deviceRecyclerView = findViewById(R.id.device_recycler_view);
//                            deviceRecyclerViewAdapter = new DeviceRecyclerViewAdapter(arrayListOfDevices, getApplicationContext());
//                            deviceRecyclerView.setAdapter(deviceRecyclerViewAdapter);
//                            deviceRecyclerView.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
//                            for (Message message1 : arrayListOfDevices) {
//                                DeviceTable deviceTable = new DeviceTable();
//                                deviceTable.setDeviceDescription(message1.getDeviceDescription());
//                                deviceTable.setDeviceID(message1.getDeviceID());
//                                deviceTable.setDeviceType(message1.getDeviceType());
//                                deviceDB.devicesDao().addDevice(deviceTable);
//                            }
//                        } else if (typeOfRequest.equals("refresh")) {
//                            arrayListOfDevices = message.getDeviceList();
//                            deviceRecyclerView = findViewById(R.id.device_recycler_view);
//                            if(deviceRecyclerViewAdapter!=null){
//                                deviceRecyclerViewAdapter.clear();
//                                deviceRecyclerViewAdapter.addAll(arrayListOfDevices);
//                            }else{
//                                deviceRecyclerViewAdapter = new DeviceRecyclerViewAdapter(arrayListOfDevices, getApplicationContext());
//                            }
//                            deviceRecyclerView.setAdapter(deviceRecyclerViewAdapter);
//                            deviceRecyclerView.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
//                            swipeContainer.setRefreshing(false);
//                            deviceDB.devicesDao().nukeTable();
//                            for (Message message1 : arrayListOfDevices) {
//                                DeviceTable deviceTable = new DeviceTable();
//                                deviceTable.setDeviceDescription(message1.getDeviceDescription());
//                                deviceTable.setDeviceID(message1.getDeviceID());
//                                deviceTable.setDeviceType(message1.getDeviceType());
//                                deviceDB.devicesDao().addDevice(deviceTable);
//                            }
//                        }
//
//                    } else {
//                        deviceRecyclerView = findViewById(R.id.device_recycler_view);
//                        deviceRecyclerView.setAdapter(deviceRecyclerViewAdapter);
//                        deviceRecyclerView.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
//                        swipeContainer.setRefreshing(false);
//                        deviceDB.devicesDao().nukeTable();
//
//                    }
//                }
//                swipeContainer.setRefreshing(false);
//            }
//        }
//
//        @Override
//        protected void onCancelled() {
//            swipeContainer.setRefreshing(false);
//        }
//    }
}
