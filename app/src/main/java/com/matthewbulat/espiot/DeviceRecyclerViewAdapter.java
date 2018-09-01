package com.matthewbulat.espiot;

import android.arch.persistence.room.Room;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

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
import java.util.ArrayList;
import java.util.List;

public class DeviceRecyclerViewAdapter extends RecyclerView.Adapter<DeviceRecyclerViewAdapter.ViewHolder> {
    private ArrayList<Message> deviceList;
    private Context mContext;
    private boolean itemSelected;

    public DeviceRecyclerViewAdapter(ArrayList<Message> deviceList, Context mContext) {
        this.deviceList = deviceList;
        this.mContext = mContext;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.layoutdevicelist, parent, false);
        ViewHolder holder = new ViewHolder(view);
        return holder;
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position) {
        switch (deviceList.get(position).getDeviceType()) {
            case "Lamp":
                holder.deviceType.setImageResource(R.drawable.ic_flare_black_24dp);
                break;
        }
        holder.deviceDescription.setText(deviceList.get(position).getDeviceDescription());
        holder.parentLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!itemSelected) {
                    DeviceStatus deviceListRequest = new DeviceStatus(holder.parentLayout.getContext(), deviceList.get(position));
                    deviceListRequest.execute((Void) null);
                }
            }
        });
    }

    public void clear() {
        deviceList.clear();
        notifyDataSetChanged();
    }

    public void addAll(List<Message> list) {
        deviceList.addAll(list);
        notifyDataSetChanged();
    }


    @Override
    public int getItemCount() {
        return deviceList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        ImageView deviceType;
        TextView deviceDescription;
        RelativeLayout parentLayout;

        public ViewHolder(View itemView) {
            super(itemView);

            deviceType = itemView.findViewById(R.id.image);
            deviceDescription = itemView.findViewById(R.id.deviceNameTextView);
            parentLayout = itemView.findViewById(R.id.parent_layout);
        }
    }

    public class DeviceStatus extends AsyncTask<Void, Void, Message> implements ConstantValues {

        private Message message;
        private Context context;


        DeviceStatus(Context context, Message message) {
            this.context = context;
            this.message = message;
            itemSelected=true;
        }

        @Override
        protected Message doInBackground(Void... params) {
            UserDB userDB = Room.databaseBuilder(context, UserDB.class, "userdb").allowMainThreadQueries().build();
            List<UserTable> user = userDB.userDao().getUser();

            String stringUrl = String.format("http://%s/lampAction?" +
                            "deviceId=%s&" +
                            "userName=%s&" +
                            "userToken=%s&" +
                            "lampAction=lampStatus"
                    , SYSTEM_DOMAIN,
                    message.getDeviceID(),
                    user.get(0).getUserName(),
                    user.get(0).getUserToken());

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

                Message temp = new Message().decode(text);

                if (temp.getAction().equals("lampStatus")) {
                    message.setAction(temp.getAction());
                    message.setLampStatus(temp.getLampStatus());
                } else {
                    message.setAction(temp.getAction());
                }


            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return message;
        }

        @Override
        protected void onPostExecute(final Message message) {
            itemSelected=false;
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
                case "lampStatus":
                    Intent intent = new Intent(context, DeviceActions.class);
                    intent.putExtra("device", message);
                    context.startActivity(intent);
                    break;
            }
        }


        @Override
        protected void onCancelled() {
            itemSelected=false;
        }
    }


}
