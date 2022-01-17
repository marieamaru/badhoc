package com.igm.badhoc.activity;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bridgefy.sdk.client.Bridgefy;
import com.bridgefy.sdk.client.BridgefyClient;
import com.bridgefy.sdk.client.Device;
import com.bridgefy.sdk.client.Message;
import com.bridgefy.sdk.client.MessageListener;
import com.bridgefy.sdk.client.RegistrationListener;
import com.bridgefy.sdk.client.Session;
import com.bridgefy.sdk.client.StateListener;
import com.bridgefy.sdk.framework.exceptions.MessageException;
import com.igm.badhoc.R;
import com.igm.badhoc.model.Node;
import com.igm.badhoc.model.Status;

import java.net.NetworkInterface;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

public class DeviceActivity extends AppCompatActivity {

    private final String TAG = "DevicesActivity";

    RecyclerView devicesRecyclerView;
    DevicesAdapter devicesAdapter;
    List<String> devices;
    Node node;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_device);

        // initialize the DevicesAdapter and the RecyclerView
        devices = new ArrayList<>();
        devicesAdapter = new DevicesAdapter(devices);
        devicesRecyclerView = findViewById(R.id.devices_recycler_view);
        devicesRecyclerView.setAdapter(devicesAdapter);
        devicesRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        // check that we have Location permissions
        if (ContextCompat.checkSelfPermission(getApplicationContext(),
                Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            initializeBridgefy();
        } else {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 0);
        }
        initializeNode();
    }

    /**
     * BRIDGEFY INITIALIZATION
     */

    RegistrationListener registrationListener = new RegistrationListener() {
        @Override
        public void onRegistrationSuccessful(BridgefyClient bridgefyClient) {
            Log.i(TAG, "onRegistrationSuccessful: current userId is: " + bridgefyClient.getUserUuid());
            Log.i(TAG, "Device Rating " + bridgefyClient.getDeviceProfile().getRating());
            Log.i(TAG, "Device Evaluation " + bridgefyClient.getDeviceProfile().getDeviceEvaluation());
            // Start the Bridgefy SDK
            Bridgefy.start(messageListener, stateListener);
        }

        @Override
        public void onRegistrationFailed(int errorCode, String message) {
            Log.e(TAG, "onRegistrationFailed: failed with ERROR_CODE: " + errorCode + ", MESSAGE: " + message);
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(DeviceActivity.this, "Bridgefy registration did not succeed.", Toast.LENGTH_LONG).show();
                }
            });

        }
    };

    private void initializeBridgefy() {
        Bridgefy.initialize(getApplicationContext(), getResources().getString(R.string.api_key), registrationListener);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            initializeBridgefy();
        } else if (grantResults[0] == PackageManager.PERMISSION_DENIED) {
            Toast.makeText(this, "Location permissions needed to start devices discovery.", Toast.LENGTH_SHORT).show();
            finish();
        }

    }

    /**
     * BRIDGEFY WORKFLOW LISTENERS
     */
    private void sendMessage(Device device) {
        HashMap<String, Object> data = new HashMap<>();
        data.put("manufacturer", node.getManufacturer());
        data.put("model", node.getModel());
        WifiManager manager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        WifiInfo info = manager.getConnectionInfo();
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 0);
        }
        String address = info.getMacAddress();
        data.put("MAC address", node.getMacAddress());
        device.sendMessage(data);
        Log.e(TAG, "Message sent! " + node.getMacAddress());
    }

    StateListener stateListener = new StateListener() {
        @Override
        public void onDeviceConnected(Device device, Session session) {
            Log.e(TAG, "Device found: " + device.getUserId());
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(DeviceActivity.this, "device found.", Toast.LENGTH_LONG).show();
                }
            });
            sendMessage(device);
        }

        @Override
        public void onDeviceLost(Device device) {
            Log.w(TAG, "Device lost: " + device.getUserId());
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(DeviceActivity.this, "device lost.", Toast.LENGTH_LONG).show();
                }
            });
        }

        @Override
        public void onDeviceDetected(Device device) {
            Log.e(TAG, "Device detected: " + device.getUserId());
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(DeviceActivity.this, "device detected.", Toast.LENGTH_LONG).show();
                }
            });
            Bridgefy.getInstance().getBridgefyCore().connectDevice(device);
        }

        @Override
        public void onDeviceUnavailable(Device device) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(DeviceActivity.this, "device unavailable.", Toast.LENGTH_LONG).show();
                }
            });
        }


        @Override
        public void onStarted() {
            super.onStarted();
            Log.e(TAG, "onStarted: Bridgefy started");
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(DeviceActivity.this, "Bridgefy started.", Toast.LENGTH_LONG).show();
                }
            });
        }

        @Override
        public void onStartError(String s, int i) {
            super.onStartError(s, i);
            Log.e(TAG, "onStartError: " + s + " " + i);
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(DeviceActivity.this, "Bridgefy started error.", Toast.LENGTH_LONG).show();
                }
            });
        }

        @Override
        public void onStopped() {
            super.onStopped();
            Log.w(TAG, "onStopped: Bridgefy stopped");
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(DeviceActivity.this, "Bridgefy stop.", Toast.LENGTH_LONG).show();
                }
            });
        }
    };

    MessageListener messageListener = new MessageListener() {
        @Override
        public void onMessageReceived(Message message) {
            String s = message.getContent().get("manufacturer") + " " + message.getContent().get("model") + " " + message.getContent().get("MAC address");
            Log.d(TAG, "Message Received: " + message.getSenderId() + ", content: " + s);
            devicesAdapter.addDevice(s);
        }

        @Override
        public void onMessageFailed(Message message, MessageException e) {
            Log.e(TAG, "Message failed", e);
        }

        @Override
        public void onMessageSent(Message message) {
            Log.d(TAG, "Message sent to: " + message.getReceiverId());
        }

        @Override
        public void onMessageReceivedException(String s, MessageException e) {
            Log.e(TAG, e.getMessage());

        }
    };

    /**
     * ADAPTER
     */

    public class DevicesAdapter extends RecyclerView.Adapter<DevicesAdapter.DeviceViewHolder> {
        // the list that holds our incoming devices
        List<String> devices;

        DevicesAdapter(List<String> items) {
            devices = items;
        }

        @Override
        public int getItemCount() {
            return devices.size();
        }

        boolean addDevice(String device) {
            if (!devices.contains(device)) {
                devices.add(device);
                notifyItemInserted(devices.size() - 1);
                return true;
            }

            return false;
        }

        void removeDevice(Device device) {
            int position = devices.indexOf(device);
            if (position > -1) {
                devices.remove(position);
                notifyItemRemoved(position);
            }
        }

        @Override
        public DeviceViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
            View deviceView = LayoutInflater.from(viewGroup.getContext()).
                    inflate(R.layout.device_row, viewGroup, false);
            return new DevicesAdapter.DeviceViewHolder(deviceView);
        }

        @Override
        public void onBindViewHolder(DeviceViewHolder deviceViewHolder, int position) {
            deviceViewHolder.setDevice(devices.get(position));
        }

        class DeviceViewHolder extends RecyclerView.ViewHolder {
            TextView deviceView;

            DeviceViewHolder(View view) {
                super(view);
                deviceView = (TextView) view.findViewById(R.id.txt_device);
            }

            void setDevice(String device) {
                deviceView.setText(device);
            }
        }
    }

    private void initializeNode() {
        /*
        WifiManager manager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        WifiInfo info = manager.getConnectionInfo();
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 0);
        }
        int rssi = info.getRssi();
         */
        String address = getMacAddress();
        node = new Node.Builder(Build.MODEL, Build.MANUFACTURER, "speed", Status.UNDEFINED, getMacAddress(), "lat", "long").build();
        getPosition();
        Log.e(TAG, "mac : " + address + " position : "+node.getLatitude() + " " + node.getLongitude());
    }

    private String getMacAddress() {
        try {
            List<NetworkInterface> all = Collections.list(NetworkInterface.getNetworkInterfaces());
            for (NetworkInterface nif : all) {
                if (!nif.getName().equalsIgnoreCase("wlan0")) continue;
                byte[] macBytes = nif.getHardwareAddress();
                if (macBytes == null) {
                    return "";
                }
                StringBuilder res1 = new StringBuilder();
                for (byte b : macBytes) {
                    res1.append(Integer.toHexString(b & 0xFF)).append(":");
                }
                if (res1.length() > 0) {
                    res1.deleteCharAt(res1.length() - 1);
                }
                return res1.toString().toUpperCase();
            }
        } catch (Exception ex) {
            //handle exception
        }
        return "error";
    }

    private void getPosition() {
        /*
        LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 0);
        }
        Location location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        node.setLatitude(String.valueOf(location.getLatitude()));
        node.setLongitude(String.valueOf(location.getLongitude()));

         */
    }

}
