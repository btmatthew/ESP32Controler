package com.matthewbulat.espiot;

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

import com.matthewbulat.espiot.Database.user.UserTable;
import com.matthewbulat.espiot.Objects.Message;
import com.matthewbulat.espiot.Objects.User;
import com.matthewbulat.espiot.RetrofitDIR.Interfaces.IoTAPI;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.observers.DisposableObserver;
import io.reactivex.schedulers.Schedulers;

public class DeviceRecyclerViewAdapter extends RecyclerView.Adapter<DeviceRecyclerViewAdapter.ViewHolder> {
    private ArrayList<Message> deviceList;
    private Context mContext;
    private IoTAPI ioTAPI;
    private List<UserTable> userTables;

    DeviceRecyclerViewAdapter(ArrayList<Message> deviceList, Context mContext, List<UserTable> userTables, IoTAPI ioTAPI) {
        this.deviceList = deviceList;
        this.mContext = mContext;
        this.ioTAPI = ioTAPI;
        this.userTables = userTables;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.layoutdevicelist, parent, false);
        ViewHolder holder = new ViewHolder(view);
        return holder;
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position) {
        Message message = deviceList.get(position);
        switch (deviceList.get(position).getDeviceType()) {
            case "Lamp":
                holder.deviceType.setImageResource(R.drawable.ic_desk_lamp);
                message.setAction("lampstatus");
                break;
            case "IrRemote":
                holder.deviceType.setImageResource(R.drawable.ic_baseline_settings_remote_24px);
                message.setAction("deviceStatus");
                break;
            case "TempSensor":
                holder.deviceType.setImageResource(R.drawable.ic_baseline_blur_on_24px);
                message.setAction("deviceStatus");
                break;
            default:
                holder.deviceType.setImageResource(R.drawable.ic_baseline_error_outline_24px);

        }
        holder.deviceDescription.setText(deviceList.get(position).getDeviceDescription());
        holder.parentLayout.setOnClickListener((view) -> {
                        getDeviceStatus(message, userTables.get(0).returnUserObject());
                }
        );
    }

    void clear() {
        deviceList.clear();
        notifyDataSetChanged();
    }

    void addAll(List<Message> list) {
        deviceList.addAll(list);
        notifyDataSetChanged();
    }


    @Override
    public int getItemCount() {
        return deviceList.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        ImageView deviceType;
        TextView deviceDescription;
        RelativeLayout parentLayout;

        ViewHolder(View itemView) {
            super(itemView);

            deviceType = itemView.findViewById(R.id.image);
            deviceDescription = itemView.findViewById(R.id.deviceNameTextView);
            parentLayout = itemView.findViewById(R.id.parent_layout);
        }
    }


    private void getDeviceStatus(Message message, User user) {
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
                                       message.setAction(value.getAction());
                                       switch (message.getDeviceType()) {
                                           case "Lamp":
                                               message.setLampStatus(value.getLampStatus());
                                               responseAction(message, user);
                                               break;
                                           case "IrRemote":
                                               message.setRemoteStatus(value.returnRemoteStatus());
                                               responseAction(message, user);
                                               break;
                                           case "TempSensor":
                                               message.setSensorStatus(value.returnSensorStatus());
                                               responseAction(message, user);
                                               break;
                                           default:
                                               //todo include default reply
                                       }
                                       Log.i("DeviceStatus", "Device status request successful");
                                   }
                               }
                )
        );
    }

    private void responseAction(Message message, User user) {
        switch (message.getAction()) {
            case "deviceNotConnectedToSystem":
                Toast.makeText(mContext, "Your ESP device is disconnected from the network."
                        , Toast.LENGTH_LONG).show();
                break;
            case "CommunicationError":
                Toast.makeText(mContext, "Internal network error, please try again in few moments."
                        , Toast.LENGTH_LONG).show();
                break;
            case "IncorrectCredentials":
                Toast.makeText(mContext, "Please try again in few moments."
                        , Toast.LENGTH_LONG).show();
                break;
            case "lampstatus":
                Intent intent = new Intent(mContext, LampActivity.class);
                message.setDeviceID(message.getDeviceID());
                message.setDeviceDescription(message.getDeviceDescription());
                Log.i("returnMessage", message.encode());
                intent.putExtra("device", message);
                intent.putExtra("user", user);
                mContext.startActivity(intent);
                break;
            case "deviceStatus":
                switch (message.getDeviceType()) {
                    case "IrRemote":
                        Intent remoteIntent = new Intent(mContext, RemoteActivity.class);
                        message.setDeviceID(message.getDeviceID());
                        message.setDeviceDescription(message.getDeviceDescription());
                        Log.i("returnMessage", message.encode());
                        remoteIntent.putExtra("device", message);
                        remoteIntent.putExtra("user", user);
                        mContext.startActivity(remoteIntent);
                        break;
                    case "TempSensor":
                        Intent sensorIntent = new Intent(mContext, TemperatureActivity.class);
                        message.setDeviceID(message.getDeviceID());
                        message.setDeviceDescription(message.getDeviceDescription());
                        Log.i("returnMessage", message.encode());
                        sensorIntent.putExtra("device", message);
                        sensorIntent.putExtra("user", user);
                        mContext.startActivity(sensorIntent);
                        break;
                }

                break;
        }
    }
}
