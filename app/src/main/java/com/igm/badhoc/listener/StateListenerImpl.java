package com.igm.badhoc.listener;

import android.Manifest;
import android.util.Log;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;

import com.bridgefy.sdk.client.Device;
import com.bridgefy.sdk.client.Session;
import com.bridgefy.sdk.client.StateListener;
import com.igm.badhoc.activity.MainActivity;
import com.igm.badhoc.model.Tag;

import java.util.HashMap;

public class StateListenerImpl extends StateListener {

    private final MainActivity mainActivity;

    private final String TAG = "StateListener";

    public StateListenerImpl(final MainActivity mainActivity) {
        this.mainActivity = mainActivity;
    }

    @Override
    public void onDeviceConnected(final Device device, Session session) {
        Log.i(TAG, "onDeviceConnected: " + device.getUserId());
        // send our information to the Device
        HashMap<String, Object> map = new HashMap<>();
        map.put(Tag.PAYLOAD_DEVICE_NAME.value, mainActivity.getNode().getDeviceName());
        map.put(Tag.PAYLOAD_MAC_ADDRESS.value, mainActivity.getNode().getMacAddress());
        map.put(Tag.PAYLOAD_RSSI.value, String.valueOf(mainActivity.getNode().getRssi()));
        map.put(Tag.PAYLOAD_IS_DOMINANT.value, String.valueOf(mainActivity.getNode().isDominant()));
        mainActivity.runOnUiThread(() -> Toast.makeText(mainActivity, "device found.", Toast.LENGTH_LONG).show());
        device.sendMessage(map);
    }

    @Override
    public void onDeviceLost(Device device) {
        Log.w(TAG, "onDeviceLost: " + device.getUserId());
        mainActivity.getNeighborsFragment().removeNeighborFromConversations(device);
        mainActivity.getNode().removeFromNeighborhood(device.getUserId());
        //device.getDeviceAddress()
    }

    @Override
    public void onDeviceDetected(Device device) {

    }

    @Override
    public void onDeviceUnavailable(Device device) {

    }

    @Override
    public void onStartError(String message, int errorCode) {
        Log.e(TAG, "onStartError: " + message);
        if (errorCode == StateListener.INSUFFICIENT_PERMISSIONS) {
            ActivityCompat.requestPermissions(mainActivity,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 0);
        }
    }

    @Override
    public void onStarted() {
        super.onStarted();
        Log.e(TAG, "onStarted: Bridgefy started");
        mainActivity.runOnUiThread(() -> Toast.makeText(mainActivity, "Bridgefy started.", Toast.LENGTH_LONG).show());
    }

}
