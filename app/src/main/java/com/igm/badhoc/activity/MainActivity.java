package com.igm.badhoc.activity;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.bridgefy.sdk.client.Bridgefy;
import com.bridgefy.sdk.client.BridgefyClient;
import com.bridgefy.sdk.client.RegistrationListener;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;
import com.igm.badhoc.R;
import com.igm.badhoc.fragment.BroadcastFragment;
import com.igm.badhoc.fragment.NeighborsFragment;
import com.igm.badhoc.fragment.PrivateChatFragment;
import com.igm.badhoc.fragment.ServerFragment;
import com.igm.badhoc.listener.MessageListenerImpl;
import com.igm.badhoc.listener.StateListenerImpl;
import com.igm.badhoc.model.Neighbor;
import com.igm.badhoc.model.Status;

import java.net.NetworkInterface;
import java.util.Collections;
import java.util.List;

public class MainActivity extends AppCompatActivity implements NavigationBarView.OnItemSelectedListener, LocationListener {

    private final String TAG = "MainActivity";
    private Neighbor node;
    private BroadcastFragment broadcastFragment;
    private NeighborsFragment neighborsFragment;
    private PrivateChatFragment privateChatFragment;
    private ServerFragment serverFragment;
    private Fragment currentFragment;
    private FragmentManager fragmentManager;
    private Bundle bundle;
    private String currentSenderId;
    private StateListenerImpl stateListener;
    private MessageListenerImpl messageListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        bundle = new Bundle();
        fragmentManager = getSupportFragmentManager();
        broadcastFragment = BroadcastFragment.newInstance(bundle);
        neighborsFragment = NeighborsFragment.newInstance(bundle);
        privateChatFragment = PrivateChatFragment.newInstance(bundle);
        serverFragment = new ServerFragment();

        currentFragment = neighborsFragment;

        fragmentManager.beginTransaction().add(R.id.fl_fragment, broadcastFragment, TAG).hide(broadcastFragment).commit();
        fragmentManager.beginTransaction().add(R.id.fl_fragment, neighborsFragment, TAG).commit();
        fragmentManager.beginTransaction().add(R.id.fl_fragment, privateChatFragment, TAG).hide(privateChatFragment).commit();
        fragmentManager.beginTransaction().add(R.id.fl_fragment, serverFragment, TAG).hide(serverFragment).commit();
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_nav);
        bottomNavigationView.setOnItemSelectedListener(this);
        stateListener = new StateListenerImpl(this);
        messageListener = new MessageListenerImpl(this);

        initializeBridgefy();
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_broadcast:
                loadFragment(broadcastFragment, "");
                return true;
            case R.id.action_private_chat:
                loadFragment(neighborsFragment, "");
                return true;
            case R.id.action_server:
                loadFragment(serverFragment, "");
                return true;
        }
        return false;
    }

    RegistrationListener registrationListener = new RegistrationListener() {
        @Override
        public void onRegistrationSuccessful(BridgefyClient bridgefyClient) {
            Log.i(TAG, "onRegistrationSuccessful: current userId is: " + bridgefyClient.getUserUuid());
            Log.i(TAG, "Device Rating " + bridgefyClient.getDeviceProfile().getRating());
            Log.i(TAG, "Device Evaluation " + bridgefyClient.getDeviceProfile().getDeviceEvaluation());
            // Start the Bridgefy
            initializeNode(bridgefyClient);
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


    private void loadFragment(Fragment fragment, String currentSenderId) {
        fragmentManager.beginTransaction().hide(currentFragment).show(fragment).commit();
        currentFragment = fragment;
        this.currentSenderId = currentSenderId;

    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
    }


    /**
     * BRIDGEFY METHODS
     */
    private void startBridgefy() {
        Bridgefy.start(messageListener, stateListener);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            startBridgefy();
            Log.e(TAG, "in reque perm : " + node.getLatitude() + node.getLongitude());
        } else if (grantResults[0] == PackageManager.PERMISSION_DENIED) {
            Toast.makeText(this, "Location permissions needed to start peers discovery.", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    /**
     * RECYCLER VIEW CLASSES
     */

    private void initializeNode(BridgefyClient bridgefyClient) {
        node = Neighbor.builder(bridgefyClient.getUserUuid(), Build.MODEL + " " + Build.MANUFACTURER).build();
        node.setType(1); //smartphone
        node.setSpeed(0);
        node.setStatus(Status.UNDEFINED.value);
        node.setMacAddress(getMacAddress());
        node.setRssi(getRssi());
        setNodePosition();
        Log.e(TAG, "mac : " + node.getMacAddress() + " position : " + node.getLatitude() + " " + node.getLongitude() + "rssi " + node.getRssi());
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

    private void setNodePosition() {
        LocationManager lm = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        try {
            if (ContextCompat.checkSelfPermission(this,
                    Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
                Location location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                node.setPosition((long) location.getLatitude(), (long) location.getLongitude());
                Log.e(TAG, "in reque perm : " + node.getLatitude() + node.getLongitude());
            }
            lm.requestLocationUpdates(LocationManager.GPS_PROVIDER, 30, 2, this);
        } catch (Exception e) {
            node.setPosition(0, 0);
            Log.e(TAG, "in reque perm : " + node.getLatitude() + node.getLongitude());
        }
    }

    private float getRssi() {
        WifiManager manager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        WifiInfo info = manager.getConnectionInfo();
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 0);
        }
        return info.getRssi();
    }

    public void onItemClick(String neighborId) {
        privateChatFragment.setMessageBadhocs(neighborId);
        privateChatFragment.setConversationId(neighborId);
        loadFragment(privateChatFragment, neighborId);
    }

    public Neighbor getNode() {
        return node;
    }

    public NeighborsFragment getNeighborsFragment() {
        return neighborsFragment;
    }

    public PrivateChatFragment getPrivateChatFragment() {
        return privateChatFragment;
    }

    public BroadcastFragment getBroadcastFragment() {
        return broadcastFragment;
    }

    @Override
    public void onLocationChanged(@NonNull Location location) {
        if (location == null) {
            // if you can't get speed because reasons :)
            Log.e(TAG, "cant get location speed");
        } else {
            int speed = (int) ((location.getSpeed() * 3600) / 1000);
            node.setSpeed(speed);
            Log.e(TAG, "speed : " + speed + " km/h");
        }
    }


}
