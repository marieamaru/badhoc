package com.igm.badhoc.activity;

import android.Manifest;
import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.telephony.CellInfo;
import android.telephony.CellInfoCdma;
import android.telephony.CellInfoGsm;
import android.telephony.CellInfoLte;
import android.telephony.CellInfoWcdma;
import android.telephony.CellSignalStrengthCdma;
import android.telephony.CellSignalStrengthGsm;
import android.telephony.CellSignalStrengthLte;
import android.telephony.CellSignalStrengthWcdma;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
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
import com.igm.badhoc.fragment.NotificationFragment;
import com.igm.badhoc.fragment.PrivateChatFragment;
import com.igm.badhoc.listener.MessageListenerImpl;
import com.igm.badhoc.listener.StateListenerImpl;
import com.igm.badhoc.model.Node;
import com.igm.badhoc.model.Status;
import com.igm.badhoc.model.Tag;
import com.igm.badhoc.service.LocationService;
import com.igm.badhoc.service.ServerService;

import java.net.NetworkInterface;
import java.util.Collections;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity implements NavigationBarView.OnItemSelectedListener {

    private final String TAG = "MainActivity";

    private Node node;
    private BroadcastFragment broadcastFragment;
    private NeighborsFragment neighborsFragment;
    private PrivateChatFragment privateChatFragment;
    private NotificationFragment notificationFragment;
    private Fragment currentFragment;
    private FragmentManager fragmentManager;
    private Bundle bundle;
    private boolean isConnectedToInternet;
    private StateListenerImpl stateListener;
    private MessageListenerImpl messageListener;
    private LocationService locationService;
    private Intent intent = new Intent(Tag.INTENT_MAIN_ACTIVITY.value);
    private Timer timer;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        bundle = new Bundle();
        fragmentManager = getSupportFragmentManager();
        broadcastFragment = BroadcastFragment.newInstance(bundle);
        neighborsFragment = NeighborsFragment.newInstance(bundle);
        privateChatFragment = PrivateChatFragment.newInstance(bundle);
        notificationFragment = new NotificationFragment();

        currentFragment = neighborsFragment;

        fragmentManager.beginTransaction().add(R.id.fl_fragment, broadcastFragment, TAG).hide(broadcastFragment).commit();
        fragmentManager.beginTransaction().add(R.id.fl_fragment, neighborsFragment, TAG).commit();
        fragmentManager.beginTransaction().add(R.id.fl_fragment, privateChatFragment, TAG).hide(privateChatFragment).commit();
        fragmentManager.beginTransaction().add(R.id.fl_fragment, notificationFragment, TAG).hide(notificationFragment).commit();
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_nav);
        bottomNavigationView.setOnItemSelectedListener(this);
        stateListener = new StateListenerImpl(this);
        messageListener = new MessageListenerImpl(this);
        timer = new Timer();
        initializeBridgefy();

    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_broadcast:
                loadFragment(broadcastFragment);
                return true;
            case R.id.action_private_chat:
                loadFragment(neighborsFragment);
                return true;
            case R.id.action_server:
                loadFragment(notificationFragment);
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


    private void loadFragment(Fragment fragment) {
        fragmentManager.beginTransaction().hide(currentFragment).show(fragment).commit();
        currentFragment = fragment;
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
        if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            startBridgefy();
        } else if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_DENIED) {
            Toast.makeText(this, "Location permissions needed to start peers discovery.", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    /**
     * RECYCLER VIEW CLASSES
     */

    private void initializeNode(BridgefyClient bridgefyClient) {
        node = Node.builder(bridgefyClient.getUserUuid(), Build.MODEL + " " + Build.MANUFACTURER).build();
        node.setType("1"); //smartphone
        node.setSpeed("0");
        node.setIsDominant(Status.DOMINATED.value);
        node.setMacAddress(getMacAddress());
        node.setRssi(getRssi());
        getLocation();
        getLteSignal(this);
        determinesIfDominant();
        Log.e(TAG, "mac : " + node.getMacAddress() + " position : " + node.getLatitude() + " " + node.getLongitude() + " rssi " + node.getRssi());
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

    public void getLocation() {
        locationService = new LocationService(MainActivity.this);
        if (locationService.canGetLocation()) {
            node.setPosition(String.valueOf(locationService.getLongitude()), String.valueOf(locationService.getLatitude()));
            node.setSpeed(String.valueOf(locationService.getSpeed()));
        } else {
            Log.e(TAG, "cannot get location");
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

    private boolean isConnectedToInternet() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE).getState() == NetworkInfo.State.CONNECTED ||
                connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI).getState() == NetworkInfo.State.CONNECTED) {
            isConnectedToInternet = true;
        } else
            isConnectedToInternet = false;
        return isConnectedToInternet;
    }

    private void getLteSignal(Context context) throws SecurityException {
        TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        String strength = null;
        List<CellInfo> cellInfos = telephonyManager.getAllCellInfo();   //This will give info of all sims present inside your mobile
        if(cellInfos != null) {
            for (int i = 0 ; i < cellInfos.size() ; i++) {
                if (cellInfos.get(i).isRegistered()) {
                    if (cellInfos.get(i) instanceof CellInfoWcdma) {
                        CellInfoWcdma cellInfoWcdma = (CellInfoWcdma) cellInfos.get(i);
                        CellSignalStrengthWcdma cellSignalStrengthWcdma = cellInfoWcdma.getCellSignalStrength();
                        strength = String.valueOf(cellSignalStrengthWcdma.getDbm());
                    } else if (cellInfos.get(i) instanceof CellInfoGsm) {
                        CellInfoGsm cellInfogsm = (CellInfoGsm) cellInfos.get(i);
                        CellSignalStrengthGsm cellSignalStrengthGsm = cellInfogsm.getCellSignalStrength();
                        strength = String.valueOf(cellSignalStrengthGsm.getDbm());
                    } else if (cellInfos.get(i) instanceof CellInfoLte) {
                        CellInfoLte cellInfoLte = (CellInfoLte) cellInfos.get(i);
                        CellSignalStrengthLte cellSignalStrengthLte = cellInfoLte.getCellSignalStrength();
                        strength = String.valueOf(cellSignalStrengthLte.getDbm());
                    } else if (cellInfos.get(i) instanceof CellInfoCdma) {
                        CellInfoCdma cellInfoCdma = (CellInfoCdma) cellInfos.get(i);
                        CellSignalStrengthCdma cellSignalStrengthCdma = cellInfoCdma.getCellSignalStrength();
                        strength = String.valueOf(cellSignalStrengthCdma.getDbm());
                    }
                }
            }
        }
        Log.e(TAG, "lte strengh "+ strength);
        this.node.setLteSignal(strength);
    }

    public void onItemClick(String neighborId) {
        privateChatFragment.setMessageBadhocs(neighborId);
        privateChatFragment.setConversationId(neighborId);
        loadFragment(privateChatFragment);
    }

    public Node getNode() {
        return node;
    }

    public NotificationFragment getNotificationFragment() {
        return notificationFragment;
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

    public void broadcastIntentAction(String action, String content) {
        this.intent.putExtra(action, content);
        sendBroadcast(this.intent);
        Log.e(TAG, "broadcastIntentAction in main activity");
    }

    public boolean isServiceRunning(Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

    TimerTask timerTask = new TimerTask() {
        @Override
        public void run() {
            if (isConnectedToInternet() && node.isDominant() == 0 && node.getDominant() == null) {
                node.setIsDominant(Status.DOMINATING.value);
                broadcastIntentAction(Tag.ACTION_CONNECT.value, "connect");
                Log.e(TAG, "JE SUIS DOMINANT");
            }
            if (!isConnectedToInternet() && node.isDominant() == 1) {
                node.setIsDominant(Status.DOMINATED.value);
                broadcastIntentAction(Tag.ACTION_CONNECT.value, "disconnect");
                Log.e(TAG, "JE SUIS DOMINE CAR JE N'AI PLUS INTERNET");
            }
        }
    };

    private void determinesIfDominant() {
        timer.scheduleAtFixedRate(timerTask, 0, 10000);
    }

}
