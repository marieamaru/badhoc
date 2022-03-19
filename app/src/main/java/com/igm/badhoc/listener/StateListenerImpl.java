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

/**
 * Implementation of the Bridgefy state listener
 */
public class StateListenerImpl extends StateListener {
    /**
     * Debug Tag used in logging
     */
    private final String TAG = "StateListener";
    /**
     * Main activity object
     */
    private final MainActivity mainActivity;

    /**
     * Constructor for the StateListener class
     *
     * @param mainActivity main activity object
     */
    public StateListenerImpl(final MainActivity mainActivity) {
        this.mainActivity = mainActivity;
    }

    /**
     * Method that processes the first handshake from a new connected device
     *
     * @param device  device object connected
     * @param session Bridgefy session corresponding
     */
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

    /**
     * Method that registers that a device is no longer connected, removes it from the neighbors list
     *
     * @param device device lost
     */
    @Override
    public void onDeviceLost(Device device) {
        String lostDevice = device.getUserId();
        mainActivity.getAroundMeFragment().removeNeighborFromConversations(device);
        mainActivity.getNode().removeFromNeighborhood(lostDevice);
        mainActivity.getNode().removeFromDominating(lostDevice);
        if (mainActivity.getNode().getDominant() != null) {
            if (mainActivity.getNode().getDominant().getId().equals(lostDevice)) {
                mainActivity.getNode().removeDominant();
            }
        }
        Log.i(TAG, "onDeviceLost: " + lostDevice + " \n");
    }

    @Override
    public void onDeviceDetected(Device device) {

    }

    @Override
    public void onDeviceUnavailable(Device device) {

    }

    /**
     * Method that notifies if there was an error in the start process of Bridgefy
     */
    @Override
    public void onStartError(String message, int errorCode) {
        Log.e(TAG, "onStartError: " + message);
        if (errorCode == StateListener.INSUFFICIENT_PERMISSIONS) {
            ActivityCompat.requestPermissions(mainActivity,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 0);
        }
    }

    /**
     * Method that notifies if Bridgefy has successfully started
     */
    @Override
    public void onStarted() {
        super.onStarted();
        Log.i(TAG, "onStarted: Bridgefy started");
        mainActivity.runOnUiThread(() -> Toast.makeText(mainActivity, "Bridgefy started.", Toast.LENGTH_LONG).show());
    }

}
