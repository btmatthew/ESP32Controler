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
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.matthewbulat.espiot.Database.devices.DeviceDB;
import com.matthewbulat.espiot.Database.devices.DeviceTable;
import com.matthewbulat.espiot.Database.user.UserDB;
import com.matthewbulat.espiot.Database.user.UserTable;
import com.matthewbulat.espiot.Objects.ConstantValues;
import com.matthewbulat.espiot.Objects.Message;
import com.matthewbulat.espiot.Objects.User;
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
import java.util.function.Consumer;

import io.reactivex.Completable;
import io.reactivex.CompletableObserver;
import io.reactivex.Single;
import io.reactivex.SingleObserver;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.observers.DisposableObserver;
import io.reactivex.schedulers.Schedulers;
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
    private List<UserTable> userTables;

    //todo add long press to remove a device
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getUserDetails();
        Log.i("onStart", "onStart called in MainActivity");

    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    private void setup() {
        Log.i("MethodCall", "Setup method called");
        ioTAPI = ApiUtils.getIoTService();
        if (userTables.size() == 0) {
            Log.i("MethodCall", "User table is empty");
            Intent intent = new Intent(MainActivity.this, LoginActivity.class);
            getApplicationContext().startActivity(intent);
        } else {
            Log.i("MethodCall", "User table contains values.");

            Toolbar toolbar = findViewById(R.id.toolbar);
            setSupportActionBar(toolbar);

            DrawerLayout drawer = findViewById(R.id.drawer_layout);

            ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                    this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
            drawer.addDrawerListener(toggle);
            toggle.syncState();

            NavigationView navigationView = findViewById(R.id.nav_view);
            navigationView.setNavigationItemSelectedListener(this);
            View headerView = navigationView.getHeaderView(0);
            TextView navUsername = headerView.findViewById(R.id.user_name_text_field);
            navUsername.setText(userTables.get(0).getUserName());
            getDevicesDetails();
        }
    }


    private void getUserDetails() {
        Log.i("User Database request", "getUserMethodCalled");
        UserDB userDB = Room.databaseBuilder(getApplicationContext(), UserDB.class, "userdb").build();
        Single<List<UserTable>> single = userDB.userDao().getUser();
        single.subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new SingleObserver<List<UserTable>>() {
                    @Override
                    public void onSubscribe(Disposable d) {
                        // add it to a CompositeDisposable
                    }

                    @Override
                    public void onSuccess(List<UserTable> users) {
                        Log.i("User Database request", "user account requested");
                        userTables = users;
                        Log.i("User Database request", String.format("the size of the table is %d",userTables.size()));
                        setup();
                    }

                    @Override
                    public void onError(Throwable e) {
                        Log.e("Database", e.getMessage());
                    }
                });
    }

    private void getDevicesDetails() {
        deviceDB = Room.databaseBuilder(getApplicationContext(), DeviceDB.class, "devicedb").build();
        Single<List<DeviceTable>> single = deviceDB.devicesDao().getDevices();
        single.subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new SingleObserver<List<DeviceTable>>() {
                    @Override
                    public void onSubscribe(Disposable d) {
                    }

                    @Override
                    public void onSuccess(List<DeviceTable> devices) {
                        Log.i("device database ",String.format("device table size is %d",devices.size()));
                        if (devices.size() == 0) {
                            loadDeviceList(userTables.get(0).getUserName(), userTables.get(0).getUserToken(), "requestDevicesList");
                        } else {
                            arrayListOfDevices = new ArrayList<>();
                            for (DeviceTable deviceTable : devices) {
                                Message message = new Message();
                                message.setDeviceID(deviceTable.getDeviceID());
                                message.setDeviceType(deviceTable.getDeviceType());
                                message.setDeviceDescription(deviceTable.getDeviceDescription());
                                arrayListOfDevices.add(message);
                            }
                            deviceRecyclerView = findViewById(R.id.device_recycler_view);
                            deviceRecyclerViewAdapter = new DeviceRecyclerViewAdapter(arrayListOfDevices, getApplicationContext(),userTables);
                            deviceRecyclerView.setAdapter(deviceRecyclerViewAdapter);
                            deviceRecyclerView.setLayoutManager(new LinearLayoutManager(getApplicationContext()));

                        }

                        swipeContainer = findViewById(R.id.swipeContainer);
                        swipeContainer.setOnRefreshListener(() -> {
                            // Your code to refresh the list here.
                            // Make sure you call swipeContainer.setRefreshing(false)
                            // once the network request has completed successfully.
                            loadDeviceList(userTables.get(0).getUserName(), userTables.get(0).getUserToken(), "requestDevicesList");
                            //https://guides.codepath.com/android/implementing-pull-to-refresh-guide#overview
                        });
                    }

                    @Override
                    public void onError(Throwable e) {
                        Log.e("Database", e.getMessage());
                    }
                });
    }

    @Override
    public void onResume() {
        if (swipeContainer != null) {
            swipeContainer.setRefreshing(true);
            loadDeviceList(userTables.get(0).getUserName(), userTables.get(0).getUserToken(), "requestDevicesList");
        }
        super.onResume();
    }


    @Override
    public void onBackPressed() {
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        switch(item.getItemId()){
            case R.id.addNewDevice:
                startActivity(new Intent(MainActivity.this, AddNewDevice.class));
                break;
                case R.id.application_settings:
                    startActivity(new Intent(MainActivity.this, Settings.class));
                    break;
        }

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }


    public void loadDeviceList(String userName, String userToken, String lampAction) {
        Log.i("method call","loadDeviceList method called");
        CompositeDisposable compositeDisposable = new CompositeDisposable();
        compositeDisposable.add(ioTAPI.deviceList(userName, userToken, lampAction)
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
                                       Log.i("rxjava called","loadDeviceList rxjava request successful");
                                       insertDeviceToDatabase(value);
                                   }
                               }
                )
        );
    }

    public void insertDeviceToDatabase(Message message) {

        ArrayList<DeviceTable> deviceTableArrayList = new ArrayList<>();
        message.getDeviceList().forEach(message1 -> {
            Log.i("Device",message1.getDeviceID());
            DeviceTable deviceTable = new DeviceTable();
            deviceTable.setDeviceDescription(message1.getDeviceDescription());
            deviceTable.setDeviceID(message1.getDeviceID());
            deviceTable.setDeviceType(message1.getDeviceType());
            deviceTableArrayList.add(deviceTable);
        });

        Completable.fromAction(() -> deviceDB.devicesDao().addDevices(deviceTableArrayList))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        new CompletableObserver() {
                            @Override
                            public void onSubscribe(Disposable d) {

                            }

                            @Override
                            public void onComplete() {
                                arrayListOfDevices = message.getDeviceList();
                                deviceRecyclerView = findViewById(R.id.device_recycler_view);
                                if (deviceRecyclerViewAdapter != null) {
                                    deviceRecyclerViewAdapter.clear();
                                    deviceRecyclerViewAdapter.addAll(arrayListOfDevices);
                                } else {
                                    deviceRecyclerViewAdapter = new DeviceRecyclerViewAdapter(arrayListOfDevices, getApplicationContext(),userTables);
                                }
                                deviceRecyclerView.setAdapter(deviceRecyclerViewAdapter);
                                deviceRecyclerView.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
                                swipeContainer.setRefreshing(false);
                            }
                            @Override
                            public void onError(Throwable e) {
                                Log.e("Database", e.getMessage());
                            }
                        }
                );
    }
}
