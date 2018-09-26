package com.matthewbulat.espiot;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.arch.persistence.room.Room;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Build;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.matthewbulat.espiot.Database.user.UserDB;
import com.matthewbulat.espiot.Database.user.UserTable;
import com.matthewbulat.espiot.Objects.ConstantValues;
import com.matthewbulat.espiot.Objects.User;
import com.matthewbulat.espiot.Objects.UserCredentials;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class RegisterNewDevice extends AppCompatActivity implements ConstantValues{
    private EditText networkName;
    private EditText networkPassword;
    private EditText deviceDescription;
    private ListView networkListView;
    private Button registerDeviceButton;
    private WifiManager wifiManager;
    private List<ScanResult> wifiList;
    private ArrayList<String> networkList;
    private WifiReceiver wifiReceiver;
    private RegisterDeviceTask registerDeviceTask;
    private View mProgressView;
    private UserDB userDB;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register_new_device);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        TextView deviceNameTextView = findViewById(R.id.deviceNameTextView);
        networkName = findViewById(R.id.networkNameEditText);
        networkPassword = findViewById(R.id.networkPasswordEditText);
        deviceDescription = findViewById(R.id.deviceDescriptionEditText);
        networkListView = findViewById(R.id.networkListView);
        registerDeviceButton = findViewById(R.id.registerDeviceButton);
        mProgressView = findViewById(R.id.registrationProgress);

        userDB = Room.databaseBuilder(getApplicationContext(),UserDB.class,"userdb").allowMainThreadQueries().build();


        wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        wifiReceiver = new WifiReceiver();
        registerReceiver(wifiReceiver, new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));

        networkName.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean b) {
                if (b) {
                    wifiManager.startScan();
                    showListView(true);
                } else {
                    showListView(false);
                }
            }
        });
        networkListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                String selectedItem = (String) adapterView.getItemAtPosition(i);
                networkName.setText(selectedItem);
                networkPassword.requestFocus();
            }
        });

        Intent intent = getIntent();
        String deviceName = String.format("Registering %s", intent.getStringExtra("deviceName"));
        deviceNameTextView.setText(deviceName);

        registerDeviceButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if(networkName.getText().toString().isEmpty() ||
                        networkName.getText().toString().equals("")){
                    networkName.setError(getString(R.string.error_field_required));
                    networkName.requestFocus();
                }
                if(networkPassword.getText().toString().isEmpty() ||
                        networkPassword.getText().toString().equals("")){
                    networkPassword.setError(getString(R.string.error_field_required));
                    networkPassword.requestFocus();
                }

                if(deviceDescription.getText().toString().isEmpty() ||
                        deviceDescription.getText().toString().equals("")){
                    deviceDescription.setError(getString(R.string.error_field_required));
                    deviceDescription.requestFocus();
                }

                if (!networkName.getText().toString().isEmpty() ||
                        !networkName.getText().toString().equals("") ||
                        !networkPassword.getText().toString().isEmpty() ||
                        !networkPassword.getText().toString().equals("") ||
                        !deviceDescription.getText().toString().isEmpty() ||
                        !deviceDescription.getText().toString().equals("")) {


                    UserCredentials userCredentials = new UserCredentials(getApplicationContext());
                    userCredentials.retriveCredentials();
                    List<UserTable> userTables = userDB.userDao().getUser();

                    String userName = userTables.get(0).getUserName();
                    String userToken = userTables.get(0).getUserToken();
                    showProgress(true);
                    registerDeviceTask = new RegisterDeviceTask(networkName.getText().toString(),
                            networkPassword.getText().toString(),
                            userName,userToken,deviceDescription.getText().toString());
                    registerDeviceTask.execute((Void) null);

                }else{
                    Toast.makeText(getApplicationContext(), "Please make sure that all the fields are filled in."
                            , Toast.LENGTH_LONG).show();
                }
            }
        });

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

    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    private void showListView(final boolean show) {
        // On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
        // for very easy animations. If available, use these APIs to fade-in
        // the progress spinner.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
            networkListView.setVisibility(show ? View.VISIBLE : View.GONE);
        } else {
            // The ViewPropertyAnimator APIs are not available, so simply show
            // and hide the relevant UI components.
            networkListView.setVisibility(show ? View.VISIBLE : View.GONE);
        }
    }

    /**
     * Shows the progress UI and hides the login form.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    private void showProgress(final boolean show) {
        // On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
        // for very easy animations. If available, use these APIs to fade-in
        // the progress spinner.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
            int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            mProgressView.animate().setDuration(shortAnimTime).alpha(
                    show ? 1 : 0).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
                }
            });
        } else {
            // The ViewPropertyAnimator APIs are not available, so simply show
            // and hide the relevant UI components.
            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
        }
    }


    public class RegisterDeviceTask extends AsyncTask<Void, Void, User> {

        private final String networkName;
        private final String networkPassword;
        private final String userName;
        private final String userToken;
        private final String deviceDescription;

        RegisterDeviceTask(String networkName, String networkPassword, String userName,
                           String userToken, String deviceDescription) {
            this.networkName = networkName;
            this.networkPassword = networkPassword;
            this.userName = userName;
            this.userToken = userToken;
            this.deviceDescription = deviceDescription;
        }

        @Override
        protected User doInBackground(Void... params) {
            User user=null;
            String text = "unknownError";
            URL url;
            try {
                String stringUrl = String.format("http://%s/wifiSetUP"
                        ,ESP_ADDRESS);

                url = new URL(stringUrl);

                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Content-Type", "application/json;charset=UTF-8");
                conn.setRequestProperty("Accept", "application/json");
                conn.setConnectTimeout(40000);
                conn.setDoOutput(true);
                conn.setDoInput(true);

                JSONObject jsonParam = new JSONObject();
                jsonParam.put("ssid", networkName);
                jsonParam.put("pass", networkPassword);
                jsonParam.put("userName", userName);
                jsonParam.put("userToken", userToken);
                jsonParam.put("description", deviceDescription);

                Log.i("JSON", jsonParam.toString());
                DataOutputStream os = new DataOutputStream(conn.getOutputStream());
                os.writeBytes(jsonParam.toString());
                os.flush();
                os.close();

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
                user = new User().decode(text);

                conn.disconnect();
                return user;

            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (ProtocolException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (JSONException e) {
                e.printStackTrace();
            }
            if(user==null){
                user = new User();
                user.setAction("UnknownError");
            }
            return user;
        }

        @Override
        protected void onPostExecute(final User success) {

            switch (success.getAction()) {
                case "wifiError":
                    Toast.makeText(getApplicationContext(), "Device cannot connect to your wifi," +
                                    "please  check your network configuration and try again."
                            , Toast.LENGTH_LONG).show();
                    break;
                case "deviceRegistrationCompleted":
                    Toast.makeText(getApplicationContext(), "Registration successful"
                            , Toast.LENGTH_LONG).show();

                    Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(intent);
                    finish();
                    break;
                case "registrationUnsuccessful":
                    Toast.makeText(getApplicationContext(), "Please check your fields," +
                                    " and try again."
                            , Toast.LENGTH_LONG).show();
                    break;
                case "databaseError":
                    Toast.makeText(getApplicationContext(), "There is a problem with our system," +
                                    " please wait few moments and try again."
                            , Toast.LENGTH_LONG).show();
                    break;
                case "UnknownError":
                    Toast.makeText(getApplicationContext(), "There is a problem with our system," +
                            " please wait few moments and try again."
                            , Toast.LENGTH_LONG).show();
                    finish();
                    break;
                    default:
                        Log.i("onPostExecute", "default called");
            }
            registerDeviceTask = null;
            showProgress(false);

        }

        @Override
        protected void onCancelled() {
            registerDeviceTask = null;
            showProgress(false);
        }
    }


    public class WifiReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            wifiList = wifiManager.getScanResults();
            networkList = new ArrayList<>();
            for (ScanResult result : wifiList) {
                if(frequencyToChannel(String.valueOf(result.frequency)).contains("2.4ghz")){
                    networkList.add(result.SSID);
                }
            }
            if (networkList.size() != 0) {
                ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(context, android.R.layout.simple_list_item_1, networkList);
                networkListView.setAdapter(arrayAdapter);

            }
            if (networkList.size() == 0) {
                Toast.makeText(getApplicationContext(), "No networks found."
                        , Toast.LENGTH_LONG).show();
            }
        }
    }

    String frequencyToChannel(String frequency) {
        switch(frequency) {
            case "2412" :
                return "ch 1 - 2.4ghz";
            case "2417" :
                return "ch 2 - 2.4ghz";
            case "2422" :
                return "ch 3 - 2.4ghz";
            case "2427" :
                return "ch 4 - 2.4ghz";
            case "2432" :
                return "ch 5 - 2.4ghz";
            case "2437" :
                return "ch 6 - 2.4ghz";
            case "2442" :
                return "ch 7 - 2.4ghz";
            case "2447" :
                return "ch 8 - 2.4ghz";
            case "2452" :
                return "ch 9 - 2.4ghz";
            case "2457" :
                return "ch 10 - 2.4ghz";
            case "2462" :
                return "ch 11 - 2.4ghz";
            case "2467" :
                return "ch 12 - 2.4ghz";
            case "2472" :
                return "ch 13 - 2.4ghz";
            case "2484" :
                return "ch 14 - 2.4ghz";
            case "5035" :
                return "ch 7 - 5.0ghz";
            case "5040" :
                return "ch 8 - 5.0ghz";
            case "5045" :
                return "ch 9 - 5.0ghz";
            case "5055" :
                return "ch 11 - 5.0ghz";
            case "5060" :
                return "ch 12 - 5.0ghz";
            case "5080" :
                return "ch 16 - 5.0ghz";
            case "5170" :
                return "ch 34 - 5.0ghz";
            case "5180" :
                return "ch 36 - 5.0ghz";
            case "5190" :
                return "ch 38 - 5.0ghz";
            case "5200" :
                return "ch 40 - 5.0ghz";
            case "5210" :
                return "ch 42 - 5.0ghz";
            case "5220" :
                return "ch 44 - 5.0ghz";
            case "5230" :
                return "ch 46 - 5.0ghz";
            case "5240" :
                return "ch 48 - 5.0ghz";
            case "5260" :
                return "ch 52 - 5.0ghz";
            case "5280" :
                return "ch 56 - 5.0ghz";
            case "5300" :
                return "ch 60 - 5.0ghz";
            case "5320" :
                return "ch 64 - 5.0ghz";
            case "5500" :
                return "ch 100 - 5.0ghz";
            case "5520" :
                return "ch 104 - 5.0ghz";
            case "5540" :
                return "ch 108 - 5.0ghz";
            case "5560" :
                return "ch 112 - 5.0ghz";
            case "5580" :
                return "ch 116 - 5.0ghz";
            case "5600" :
                return "ch 120 - 5.0ghz";
            case "5620" :
                return "ch 124 - 5.0ghz";
            case "5640" :
                return "ch 128 - 5.0ghz";
            case "5660" :
                return "ch 132 - 5.0ghz";
            case "5680" :
                return "ch 136 - 5.0ghz";
            case "5700" :
                return "ch 140 - 5.0ghz";
            case "5720" :
                return "ch 144 - 5.0ghz";
            case "5745" :
                return "ch 149 - 5.0ghz";
            case "5765" :
                return "ch 153 - 5.0ghz";
            case "5785" :
                return "ch 157 - 5.0ghz";
            case "5805" :
                return "ch 161 - 5.0ghz";
            case "5825" :
                return "ch 165 - 5.0ghz";
            case "4915" :
                return "ch 183 - 5.0ghz";
            case "4920" :
                return "ch 184 - 5.0ghz";
            case "4925" :
                return "ch 185 - 5.0ghz";
            case "4935" :
                return "ch 187 - 5.0ghz";
            case "4940" :
                return "ch 188 - 5.0ghz";
            case "4945" :
                return "ch 189 - 5.0ghz";
            case "4960" :
                return "ch 192 - 5.0ghz";
            case "4980" :
                return "ch 196 - 5.0ghz";
            default:
                return "No channel";
        }
    }

}
