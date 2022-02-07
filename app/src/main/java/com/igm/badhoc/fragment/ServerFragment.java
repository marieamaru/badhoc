package com.igm.badhoc.fragment;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

import com.igm.badhoc.R;

import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttException;

public class ServerFragment extends Fragment {

    private String TAG = "ServerFragment";

    private Button btn;
    private TextView txtMessage;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.server_fragment, container, false);
        txtMessage = view.findViewById(R.id.txt_server);
        btn = view.findViewById(R.id.btn);
        btn.setOnClickListener(view1 -> {
            String clientId = MqttClient.generateClientId();
            String hivemqUrl = "tcp://broker.hivemq.com:1883";
            String badzakUrl = "tcp://a162zzet6rcfvu-ats.iot.us-west-2.amazonaws.com:8883";
            MqttAndroidClient client =
                    new MqttAndroidClient(getActivity().getApplicationContext(), badzakUrl,
                            clientId);
            try {
                IMqttToken token = client.connect();
                token.setActionCallback(new IMqttActionListener() {
                    @Override
                    public void onSuccess(IMqttToken asyncActionToken) {
                        // We are connected
                        txtMessage.setText("connected");
                        getActivity().runOnUiThread(() -> Toast.makeText(view1.getContext(), "Success", Toast.LENGTH_SHORT).show());
                        Log.e(TAG, "connected to server");

                    }

                    @Override
                    public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                        // Something went wrong e.g. connection timeout or firewall problems
                        txtMessage.setText("not connected");
                        getActivity().runOnUiThread(() -> Toast.makeText(view1.getContext(), "Not success", Toast.LENGTH_SHORT).show());
                        Log.e(TAG, "not connected to server " + exception.getCause());
                    }
                });
            } catch (MqttException e) {
                e.printStackTrace();
                Log.e(TAG, "error server");
            }
        });
        return view;
    }
}