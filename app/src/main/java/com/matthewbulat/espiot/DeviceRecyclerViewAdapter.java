package com.matthewbulat.espiot;

import android.arch.persistence.room.Room;
import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.matthewbulat.espiot.Database.user.UserDB;
import com.matthewbulat.espiot.Database.user.UserTable;
import com.matthewbulat.espiot.Objects.Message;
import com.matthewbulat.espiot.RetrofitDIR.ApiUtils;
import com.matthewbulat.espiot.RetrofitDIR.Interfaces.IoTAPI;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class DeviceRecyclerViewAdapter extends RecyclerView.Adapter<DeviceRecyclerViewAdapter.ViewHolder> {
    private ArrayList<Message> deviceList;
    private Context mContext;
    private boolean itemSelected;
    private IoTAPI ioTAPI;

    public DeviceRecyclerViewAdapter(ArrayList<Message> deviceList, Context mContext) {
        this.deviceList = deviceList;
        this.mContext = mContext;
        this.ioTAPI = ApiUtils.getIoTService();
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
                holder.deviceType.setImageResource(R.drawable.ic_desk_lamp);
                break;
        }
        holder.deviceDescription.setText(deviceList.get(position).getDeviceDescription());
        holder.parentLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!itemSelected) {
                    UserDB userDB = Room.databaseBuilder(mContext, UserDB.class, "userdb").allowMainThreadQueries().build();
                    UserTable user = userDB.userDao().getUser().get(0);

                    deviceAction(deviceList.get(position),user.getUserName(),user.getUserToken(),"lampstatus");
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

    public void deviceAction(final Message message, String userName, String userToken, String lampAction){

        ioTAPI.lampActions(message.getDeviceID(),userName,userToken,lampAction).enqueue(new Callback<Message>() {
            @Override
            public void onResponse(Call<Message> call, Response<Message> response) {

                if(response.isSuccessful()) {

                    System.out.println(response.body().getAction());

                    switch (response.body().getAction()) {
                        case "deviceNotConnectedToSystem":
                            Toast.makeText(mContext, "Your ESP device is disconnected from the network."
                                    , Toast.LENGTH_LONG).show();
                            break;
                        case "communicationError":
                            Toast.makeText(mContext, "Internal network error, please try again in few moments."
                                    , Toast.LENGTH_LONG).show();
                            break;
                        case "IncorrectCredentials":
                            Toast.makeText(mContext, "Please try again in few moments."
                                    , Toast.LENGTH_LONG).show();
                            break;
                        case "lampstatus":

                            Intent intent = new Intent(mContext, DeviceActions.class);
                            response.body().setDeviceID(message.getDeviceID());
                            response.body().setDeviceDescription(message.getDeviceDescription());
                            Log.i("returnMessage",response.body().encode());
                            intent.putExtra("device", response.body());
                            mContext.startActivity(intent);
                            break;
                    }
                }else{
                    //todo test this
                }
            }

            @Override
            public void onFailure(Call<Message> call, Throwable t) {

            }
        });
    }
}
