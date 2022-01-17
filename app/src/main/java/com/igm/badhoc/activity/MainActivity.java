package com.igm.badhoc.activity;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bridgefy.sdk.BuildConfig;
import com.bridgefy.sdk.client.Bridgefy;
import com.bridgefy.sdk.client.BridgefyClient;
import com.bridgefy.sdk.client.Device;
import com.bridgefy.sdk.client.Message;
import com.bridgefy.sdk.client.MessageListener;
import com.bridgefy.sdk.client.RegistrationListener;
import com.bridgefy.sdk.client.Session;
import com.bridgefy.sdk.client.StateListener;
import com.igm.badhoc.R;
import com.igm.badhoc.model.Neighbor;
import com.igm.badhoc.model.Node;
import com.igm.badhoc.model.Status;

import java.net.NetworkInterface;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private final String TAG = "MainActivity";

    static final String INTENT_EXTRA_NAME = "peerName";
    static final String INTENT_EXTRA_UUID = "peerUuid";
    static final String INTENT_EXTRA_TYPE = "deviceType";
    static final String INTENT_EXTRA_MSG = "message";
    static final String BROADCAST_CHAT = "Broadcast";

    static final String PAYLOAD_DEVICE_TYPE = "device_type";
    static final String PAYLOAD_DEVICE_NAME = "device_name";
    static final String PAYLOAD_TEXT = "text";

    Node node;

    NeighborsAdapter neighborsAdapter =
            new NeighborsAdapter(new ArrayList<>());


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Configure the Toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setTitle(getTitle());

        RecyclerView recyclerView = findViewById(R.id.peer_list);
        recyclerView.setAdapter(neighborsAdapter);

        if (isThingsDevice(this)) {
            //enabling bluetooth automatically
            BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
            bluetoothAdapter.enable();
        }
        Bridgefy.debug = BuildConfig.DEBUG;
        initializeBridgefy();
        initializeNode();
    }

    RegistrationListener registrationListener = new RegistrationListener() {
        @Override
        public void onRegistrationSuccessful(BridgefyClient bridgefyClient) {
            Log.i(TAG, "onRegistrationSuccessful: current userId is: " + bridgefyClient.getUserUuid());
            Log.i(TAG, "Device Rating " + bridgefyClient.getDeviceProfile().getRating());
            Log.i(TAG, "Device Evaluation " + bridgefyClient.getDeviceProfile().getDeviceEvaluation());
            // Start the Bridgefy SDK
            startBridgefy();
        }

        @Override
        public void onRegistrationFailed(int errorCode, String message) {
            Log.e(TAG, "onRegistrationFailed: failed with ERROR_CODE: " + errorCode + ", MESSAGE: " + message);
            runOnUiThread(() -> Toast.makeText(getBaseContext(), getString(R.string.registration_error),
                    Toast.LENGTH_LONG).show());

        }
    };

    private void initializeBridgefy() {
        Bridgefy.initialize(getApplicationContext(), getResources().getString(R.string.api_key), registrationListener);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (isFinishing())
            Bridgefy.stop();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_broadcast:
                startActivity(new Intent(getBaseContext(), ChatActivity.class)
                        .putExtra(INTENT_EXTRA_NAME, BROADCAST_CHAT)
                        .putExtra(INTENT_EXTRA_UUID, BROADCAST_CHAT));
                return true;
        }
        return false;
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }


    /**
     * BRIDGEFY METHODS
     */
    private void startBridgefy() {
        Bridgefy.start(messageListener, stateListener);
    }

    // TODO change for BridgefyUtils method
    public boolean isThingsDevice(Context context) {
        final PackageManager pm = context.getPackageManager();
        return pm.hasSystemFeature("android.hardware.type.embedded");
    }

    private MessageListener messageListener = new MessageListener() {
        @Override
        public void onMessageReceived(Message message) {
            // direct messages carrying a Device name represent device handshakes
            if (message.getContent().get(PAYLOAD_DEVICE_NAME) != null) {
                Neighbor neighbor = new Neighbor(message.getSenderId(),
                        (String) message.getContent().get(PAYLOAD_DEVICE_NAME));
                neighbor.setNearby(true);
                neighbor.setDeviceType(extractType(message));
                neighborsAdapter.addNeighbor(neighbor);
                Log.d(TAG, "Peer introduced itself: " + neighbor.getDeviceName());

                // any other direct message should be treated as such
            } else {
                String incomingMessage = (String) message.getContent().get("text");
                Log.d(TAG, "Incoming private message: " + incomingMessage);
                LocalBroadcastManager.getInstance(getBaseContext()).sendBroadcast(
                        new Intent(message.getSenderId())
                                .putExtra(INTENT_EXTRA_MSG, incomingMessage));
            }

            // if it's an Android Things device, reply automatically
            if (isThingsDevice(MainActivity.this)) {
                Log.d(TAG, "I'm a bot. Responding message automatically.");
                HashMap<String, Object> content = new HashMap<>();
                content.put("text", "Beep boop. I'm a bot.");
                Message replyMessage = Bridgefy.createMessage(message.getSenderId(), content);
                Bridgefy.sendMessage(replyMessage);
            }
        }

        @Override
        public void onBroadcastMessageReceived(Message message) {
            // we should not expect to have connected previously to the device that originated
            // the incoming broadcast message, so device information is included in this packet
            String incomingMsg = (String) message.getContent().get(PAYLOAD_TEXT);
            String deviceName = (String) message.getContent().get(PAYLOAD_DEVICE_NAME);
            Neighbor.DeviceType deviceType = extractType(message);

            Log.d(TAG, "Incoming broadcast message: " + incomingMsg);
            LocalBroadcastManager.getInstance(getBaseContext()).sendBroadcast(
                    new Intent(BROADCAST_CHAT)
                            .putExtra(INTENT_EXTRA_NAME, deviceName)
                            .putExtra(INTENT_EXTRA_TYPE, deviceType)
                            .putExtra(INTENT_EXTRA_MSG, incomingMsg));
        }
    };

    private Neighbor.DeviceType extractType(Message message) {
        int eventOrdinal;
        Object eventObj = message.getContent().get(PAYLOAD_DEVICE_TYPE);
        if (eventObj instanceof Double) {
            eventOrdinal = ((Double) eventObj).intValue();
        } else {
            eventOrdinal = (Integer) eventObj;
        }
        return Neighbor.DeviceType.values()[eventOrdinal];
    }

    StateListener stateListener = new StateListener() {
        @Override
        public void onDeviceConnected(final Device device, Session session) {
            Log.i(TAG, "onDeviceConnected: " + device.getUserId());
            // send our information to the Device
            HashMap<String, Object> map = new HashMap<>();
            map.put(PAYLOAD_DEVICE_NAME, node.getManufacturer() + " " + node.getModel());
            map.put(PAYLOAD_DEVICE_TYPE, Neighbor.DeviceType.ANDROID.ordinal());
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(MainActivity.this, "device found.", Toast.LENGTH_LONG).show();
                }
            });
            device.sendMessage(map);
        }

        @Override
        public void onDeviceLost(Device device) {
            Log.w(TAG, "onDeviceLost: " + device.getUserId());
            neighborsAdapter.removeNeighbor(device);
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
                ActivityCompat.requestPermissions(MainActivity.this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 0);
            }
        }

        @Override
        public void onStarted() {
            super.onStarted();
            Log.e(TAG, "onStarted: Bridgefy started");
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(MainActivity.this, "Bridgefy started.", Toast.LENGTH_LONG).show();
                }
            });
        }
    };

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            // Start Bridgefy
            startBridgefy();

        } else if (grantResults[0] == PackageManager.PERMISSION_DENIED) {
            Toast.makeText(this, "Location permissions needed to start peers discovery.", Toast.LENGTH_SHORT).show();
            finish();
        }
    }


    /**
     * RECYCLER VIEW CLASSES
     */
    class NeighborsAdapter
            extends RecyclerView.Adapter<NeighborsAdapter.ViewHolder> {

        private final List<Neighbor> neighbors;

        NeighborsAdapter(List<Neighbor> neighbors) {
            this.neighbors = neighbors;
        }

        @Override
        public int getItemCount() {
            return neighbors.size();
        }

        void addNeighbor(Neighbor neighbor) {
            int position = getNeighborPosition(neighbor.getUuid());
            if (position > -1) {
                this.neighbors.set(position, neighbor);
                notifyItemChanged(position);
            } else {
                this.neighbors.add(neighbor);
                notifyItemInserted(this.neighbors.size() - 1);
            }
        }

        void removeNeighbor(Device lostNeighbor) {
            int position = getNeighborPosition(lostNeighbor.getUserId());
            if (position > -1) {
                Neighbor neighbor = this.neighbors.get(position);
                neighbor.setNearby(false);
                this.neighbors.set(position, neighbor);
                notifyItemChanged(position);
            }
        }

        private int getNeighborPosition(String neighborId) {
            for (int i = 0; i < neighbors.size(); i++) {
                if (neighbors.get(i).getUuid().equals(neighborId))
                    return i;
            }
            return -1;
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.peer_row, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(final ViewHolder neighborHolder, int position) {
            neighborHolder.setNeighbor(neighbors.get(position));
        }

        class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
            final TextView mContentView;
            final ImageView mAvatar;
            Neighbor neighbor;

            ViewHolder(View view) {
                super(view);
                mAvatar = view.findViewById(R.id.peerAvatar);
                mContentView = view.findViewById(R.id.peerName);
                view.setOnClickListener(this);
            }

            void setNeighbor(Neighbor neighbor) {
                this.neighbor = neighbor;
                switch (neighbor.getDeviceType()) {
                    case ANDROID:
                        this.mContentView.setText(neighbor.getDeviceName() + " (android)");
                        break;

                    case IPHONE:
                        this.mContentView.setText(neighbor.getDeviceName() + " (iPhone)");
                        break;
                }

                if (neighbor.isNearby()) {
                    this.mAvatar.setImageResource(R.drawable.user_nearby);
                    this.mContentView.setTextColor(Color.BLACK);
                } else {
                    this.mAvatar.setImageResource(R.drawable.user_not_nearby);
                    this.mContentView.setTextColor(Color.LTGRAY);
                }
            }

            public void onClick(View v) {
                startActivity(new Intent(getBaseContext(), ChatActivity.class)
                        .putExtra(INTENT_EXTRA_NAME, neighbor.getDeviceName())
                        .putExtra(INTENT_EXTRA_UUID, neighbor.getUuid()));
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
        Log.e(TAG, "mac : " + address + " position : " + node.getLatitude() + " " + node.getLongitude());
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
