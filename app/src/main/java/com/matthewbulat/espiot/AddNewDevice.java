package com.matthewbulat.espiot;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

public class AddNewDevice extends AppCompatActivity {


    ConnectToWiFi connectToWiFi;
    Button turnWifiOnBtn;
    Button scanWifiBtn;
    ListView wifiListView;

    WifiManager wifiManager;


    WifiReceiver wifiReceiver;
    List<ScanResult> wifiList;
    ArrayList<String> networkList;

    private View wifiProgress;
    private View wifiForm;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_new_device);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);


        turnWifiOnBtn = findViewById(R.id.turnWifiOnButton);
        scanWifiBtn = findViewById(R.id.scanWifiButton);
        wifiListView = findViewById(R.id.wifiListView);
        wifiForm = findViewById(R.id.wifiLayout);
        wifiProgress = findViewById(R.id.wifiConnection);

        wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        if (wifiManager.isWifiEnabled()) {
            turnWifiOnBtn.setText("Turn Wifi Off");
        } else {
            turnWifiOnBtn.setText("Turn Wifi On");
        }
        turnWifiOnBtn.setOnClickListener((view) -> {
                    if (wifiManager.isWifiEnabled()) {
                        wifiManager.setWifiEnabled(false);
                        turnWifiOnBtn.setText("Turn Wifi On");
                    } else {
                        wifiManager.setWifiEnabled(true);
                        turnWifiOnBtn.setText("Turn Wifi Off");
                    }
                }
        );

        wifiReceiver = new WifiReceiver();

        registerReceiver(wifiReceiver, new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));
        wifiManager.startScan();
        scanWifiBtn.setOnClickListener((view) -> wifiManager.startScan());

        wifiListView.setOnItemClickListener((parent, view, position, id) -> {
                    // Get the selected item text from ListView
                    String selectedItem = (String) parent.getItemAtPosition(position);
                    if (selectedItem.contains("ESP")) {
                        showProgress(true);
                        ConnectToWiFi connectToWiFi = new ConnectToWiFi(selectedItem);
                        connectToWiFi.execute((Void) null);

                    } else {
                        Toast.makeText(getApplicationContext(), "Please only select networks that contain ESP in their name."
                                , Toast.LENGTH_LONG).show();
                    }
                }
        );
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.settings_values, menu);
        return true;
    }

    @Override
    protected void onResume() {

        registerReceiver(wifiReceiver, new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(wifiReceiver);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case android.R.id.home:
                NavUtils.navigateUpFromSameTask(this);
                return true;
            case R.id.applicationSettings:
                startActivity(new Intent(AddNewDevice.this, Settings.class));
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void showProgress(final boolean show) {
        wifiProgress.setVisibility(show ? View.VISIBLE : View.GONE);
        wifiForm.setVisibility(show ? View.GONE : View.VISIBLE);

    }

    public class ConnectToWiFi extends AsyncTask<Void, Void, Boolean> {

        private final String wifiName;

        ConnectToWiFi(String wifiName) {
            this.wifiName = wifiName;
        }

        @Override
        protected Boolean doInBackground(Void... params) {

            WifiConfiguration conf = new WifiConfiguration();
            conf.SSID = String.format("\"%s\"", wifiName);

            conf.preSharedKey = String.format("\"%s\"", "password1");

            int netID = wifiManager.addNetwork(conf);

            wifiManager.disconnect();
            wifiManager.enableNetwork(netID, true);
            wifiManager.reconnect();


            WifiInfo wifiInfo = wifiManager.getConnectionInfo();
            int count = 0;
            while (wifiInfo.getNetworkId() != netID) {
                System.out.println(wifiInfo.getNetworkId());
                if (count == 60) {
                    wifiManager.disconnect();
                    wifiManager.removeNetwork(netID);
                    return false;
                }
                try {
                    Thread.sleep(100);
                    wifiInfo = wifiManager.getConnectionInfo();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                count++;
            }
            return true;
        }

        @Override
        protected void onPostExecute(final Boolean success) {
            connectToWiFi = null;
            showProgress(false);
            if (!success) {
                Toast.makeText(getApplicationContext(), "Unable to connect to device, please try again later."
                        , Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(getApplicationContext(), "Successful connected to the device."
                        , Toast.LENGTH_LONG).show();
                Intent intent = new Intent(AddNewDevice.this, RegisterNewDevice.class);
                intent.putExtra("deviceName", wifiName);
                startActivity(intent);
            }
        }

        @Override
        protected void onCancelled() {
            connectToWiFi = null;
            showProgress(false);
        }
    }

    public class WifiReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            wifiList = wifiManager.getScanResults();
            networkList = new ArrayList<>();
            for (ScanResult result : wifiList) {
                if (result.SSID.contains("ESP")) {
                    networkList.add(result.SSID);
                }
            }
            if (networkList.size() != 0) {
                ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(context, android.R.layout.simple_list_item_1, networkList);
                wifiListView.setAdapter(arrayAdapter);
            }
            if (networkList.size() == 0) {
                Toast.makeText(getApplicationContext(), "No networks found."
                        , Toast.LENGTH_LONG).show();
            }
        }
    }

}
