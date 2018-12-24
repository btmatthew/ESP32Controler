package com.matthewbulat.espiot.Fragments;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.matthewbulat.espiot.Objects.Message;
import com.matthewbulat.espiot.Objects.User;
import com.matthewbulat.espiot.R;
import com.matthewbulat.espiot.RetrofitDIR.ApiUtils;
import com.matthewbulat.espiot.RetrofitDIR.Interfaces.IoTAPI;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.observers.DisposableObserver;
import io.reactivex.schedulers.Schedulers;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link FanRemoteControl.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link FanRemoteControl#newInstance} factory method to
 * create an instance of this fragment.
 */
public class FanRemoteControl extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String USER = "user";
    private static final String MESSAGE = "device";


    private IoTAPI ioTAPI;
    private User user;
    private Message device;
    private TextView deviceName;

    private OnFragmentInteractionListener mListener;

    public FanRemoteControl() {
        // Required empty public constructor
    }



    public static FanRemoteControl newInstance(Message message, User user) {
        FanRemoteControl fragment = new FanRemoteControl();
        Bundle args = new Bundle();
        args.putParcelable(USER,user);
        args.putParcelable(MESSAGE, message);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            user = getArguments().getParcelable(USER);
            device = getArguments().getParcelable(MESSAGE);
        }
        ioTAPI = ApiUtils.getIoTService();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_fan_remote_control, container, false);

        deviceName = v.findViewById(R.id.fanRemoteDeviceDescription);
        deviceName.setText(device.getDeviceDescription());

        Button fanPowerButton = v.findViewById(R.id.fanPowerButton);
        Button ionButton = v.findViewById(R.id.ionButton);
        Button fanSpeedButton = v.findViewById(R.id.fanSpeedButton);
        Button quiteModeButton = v.findViewById(R.id.quiteModeButton);
        Button rotationButton = v.findViewById(R.id.rotationButton);

        fanPowerButton.setOnClickListener(v1 -> {
            Message messageToDevice = device;
            messageToDevice.setAction("remoteaction");
            messageToDevice.setRemoteOption(1);
            deviceAction(messageToDevice);
        });

        ionButton.setOnClickListener(v1 -> {
            Message messageToDevice = device;
            messageToDevice.setAction("remoteaction");
            messageToDevice.setRemoteOption(5);
            deviceAction(messageToDevice);
        });

        fanSpeedButton.setOnClickListener(v1 -> {
            Message messageToDevice = device;
            messageToDevice.setAction("remoteaction");
            messageToDevice.setRemoteOption(2);
            deviceAction(messageToDevice);
        });

        quiteModeButton.setOnClickListener(v1 -> {
            Message messageToDevice = device;
            messageToDevice.setAction("remoteaction");
            messageToDevice.setRemoteOption(3);
            deviceAction(messageToDevice);
        });

        rotationButton.setOnClickListener(v1 -> {
            Message messageToDevice = device;
            messageToDevice.setAction("remoteaction");
            messageToDevice.setRemoteOption(4);
            deviceAction(messageToDevice);
        });

        // Inflate the layout for this fragment
        return v;
    }

    public void deviceAction(Message message) {

        CompositeDisposable compositeDisposable = new CompositeDisposable();
        compositeDisposable.add(ioTAPI.remoteAction(message.getAction(), message.getDeviceID(), user.getUserName(), user.getUserToken(), message.getRemoteOption())
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
                                       //finishAPIAction(value);
                                       Log.i("return message",value.encode());
                                       Log.i("Device Action", "Device action request successful ");
                                   }
                               }
                )
        );
    }
    public void updateDeviceDescription(String deviceDecription){
        deviceName.setText(device.getDeviceDescription());
        device.setDeviceDescription(deviceDecription);

    }

    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
        //void updateDeviceDescriptionTextField(String deviceDecription);
    }
}
