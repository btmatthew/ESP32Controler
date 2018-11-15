package com.matthewbulat.espiot.fragments;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.ToggleButton;

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
 * {@link TVRemoteControl.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link TVRemoteControl#newInstance} factory method to
 * create an instance of this fragment.
 */
public class TVRemoteControl extends Fragment {

    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String USER = "user";
    private static final String MESSAGE = "device";

    private IoTAPI ioTAPI;
    private User user;
    private Message device;

    private OnFragmentInteractionListener mListener;

    public TVRemoteControl() {
        // Required empty public constructor
    }

    public static TVRemoteControl newInstance(Message message, User user) {
        TVRemoteControl fragment = new TVRemoteControl();
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
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_tvremote_control, container, false);
        TextView deviceName = v.findViewById(R.id.tvRemoteDeviceDescription);
        deviceName.setText(device.getDeviceDescription());
        ToggleButton tvPower = v.findViewById(R.id.tvPower);
        if (device.isTvStatus()) {
            tvPower.setChecked(true);
        } else {
            tvPower.setChecked(false);
        }
        tvPower.setOnClickListener(v1 -> {
            Message messageToDevice = device;
            messageToDevice.setAction("remoteaction");
            messageToDevice.setRemoteOption(6);
            deviceAction(messageToDevice);

        });

        ImageButton channelUp = v.findViewById(R.id.channelUp);
        ImageButton channelDown = v.findViewById(R.id.channelDown);
        ImageButton volumeUp = v.findViewById(R.id.volumeUp);
        ImageButton volumeDown = v.findViewById(R.id.volumeDown);

        channelUp.setOnClickListener(v1 -> {
            Message messageToDevice = device;
            messageToDevice.setAction("remoteaction");
            messageToDevice.setRemoteOption(9);
            deviceAction(messageToDevice);
        });

        channelDown.setOnClickListener(v1 -> {
            Message messageToDevice = device;
            messageToDevice.setAction("remoteaction");
            messageToDevice.setRemoteOption(10);
            deviceAction(messageToDevice);
        });

        volumeUp.setOnClickListener(v1 -> {
            Message messageToDevice = device;
            messageToDevice.setAction("remoteaction");
            messageToDevice.setRemoteOption(7);
            deviceAction(messageToDevice);
        });

        volumeDown.setOnClickListener(v1 -> {
            Message messageToDevice = device;
            messageToDevice.setAction("remoteaction");
            messageToDevice.setRemoteOption(8);
            deviceAction(messageToDevice);
        });

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
                                       Log.i("Device Action", "Device action request successful");
                                   }
                               }
                )
        );
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
        void onFragmentInteraction(Uri uri);
    }
}
