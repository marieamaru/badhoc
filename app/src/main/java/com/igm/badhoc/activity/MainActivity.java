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

import com.bridgefy.sdk.client.BFEngineProfile;
import com.bridgefy.sdk.client.Bridgefy;
import com.bridgefy.sdk.client.BridgefyClient;
import com.bridgefy.sdk.client.Message;
import com.bridgefy.sdk.client.RegistrationListener;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;
import com.igm.badhoc.R;
import com.igm.badhoc.fragment.AroundMeFragment;
import com.igm.badhoc.fragment.BroadcastChatFragment;
import com.igm.badhoc.fragment.NotificationFragment;
import com.igm.badhoc.fragment.PrivateChatFragment;
import com.igm.badhoc.listener.MessageListenerImpl;
import com.igm.badhoc.listener.StateListenerImpl;
import com.igm.badhoc.model.Node;
import com.igm.badhoc.model.Status;
import com.igm.badhoc.model.Tag;
import com.igm.badhoc.service.LocationService;

import java.net.NetworkInterface;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;

/**
 * Main activity generating the different fragments, and initializing the main node and its parameters
 */
public class MainActivity extends AppCompatActivity implements NavigationBarView.OnItemSelectedListener {

    /**
     * Debug Tag used in logging
     */
    private final String TAG = "MainActivity";
    /**
     * Intent used to communicated with NotificationFragment
     */
    private final Intent intent = new Intent(Tag.INTENT_MAIN_ACTIVITY.value);
    /**
     * Object representing the current device and its characteristics
     */
    private Node node;
    /**
     * Fragment representing the public chat interface
     */
    private BroadcastChatFragment broadcastChatFragment;
    /**
     * Fragment representing the interface with the list of users around
     */
    private AroundMeFragment aroundMeFragment;
    /**
     * Fragment representing the private chat interface
     */
    private PrivateChatFragment privateChatFragment;
    /**
     * Fragment representing the notifications interface
     */
    private NotificationFragment notificationFragment;
    /**
     * Fragment manager used to handle the different fragments of the activity
     */
    private FragmentManager fragmentManager;
    /**
     * Object designating the current fragment to display
     */
    private Fragment currentFragment;
    /**
     * Bridgefy listener observing the connection changes
     */
    private StateListenerImpl stateListener;
    /**
     * Bridgefy listener observing the message exchanges
     */
    private MessageListenerImpl messageListener;
    /**
     * Service used to define the position and speed of the device
     */
    private LocationService locationService;
    /**
     * Recurring task updating the device status
     */
    private Timer timer;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        fragmentManager = getSupportFragmentManager();
        broadcastChatFragment = new BroadcastChatFragment();
        aroundMeFragment = new AroundMeFragment();
        privateChatFragment = new PrivateChatFragment();
        notificationFragment = new NotificationFragment();

        currentFragment = aroundMeFragment;

        fragmentManager.beginTransaction().add(R.id.fl_fragment, broadcastChatFragment, TAG).hide(broadcastChatFragment).commit();
        fragmentManager.beginTransaction().add(R.id.fl_fragment, aroundMeFragment, TAG).commit();
        fragmentManager.beginTransaction().add(R.id.fl_fragment, privateChatFragment, TAG).hide(privateChatFragment).commit();
        fragmentManager.beginTransaction().add(R.id.fl_fragment, notificationFragment, TAG).hide(notificationFragment).commit();
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_nav);
        bottomNavigationView.setOnItemSelectedListener(this);
        stateListener = new StateListenerImpl(this);
        messageListener = new MessageListenerImpl(this);
        timer = new Timer();
        initializeBridgefy();

    }

    /**
     * Method handling the fragment to display according to where the user clicked
     */
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_broadcast:
                loadFragment(broadcastChatFragment);
                return true;
            case R.id.action_private_chat:
                loadFragment(aroundMeFragment);
                return true;
            case R.id.action_server:
                loadFragment(notificationFragment);
                return true;
        }
        return false;
    }

    /**
     * Bridgefy listener observing the registration to the Bridgefy client.
     * Initializes the current node if successful, returns an error otherwise.
     */
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

    /**
     * Method to initialize the Bridgefy SDK
     */
    private void initializeBridgefy() {
        Bridgefy.initialize(getApplicationContext(), registrationListener);
    }

    /**
     * On destroy of the main activity : unregisters the broadcast receiver, stops the server service,
     * and stop the Bridgefy client
     */
    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(notificationFragment.getReceiver());
        stopService(notificationFragment.getIntentService());
        if (isFinishing())
            Bridgefy.stop();
    }

    /**
     * Inflates the menu bar with the menu_main layout
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    /**
     * Method to hide the current fragment, load the fragment wanted,
     * and update the current fragment
     */
    private void loadFragment(Fragment fragment) {
        fragmentManager.beginTransaction().hide(currentFragment).show(fragment).commit();
        currentFragment = fragment;
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
    }


    /**
     * Bridgefy method to start the different listeners
     */
    private void startBridgefy() {
        Bridgefy.start(messageListener, stateListener);
    }

    /**
     * Method that verifies if the location permissions were given in order to start peers discovery
     */
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
     * Method that initializes the current node and calls different methods and services to
     * initializes its parameters
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
        Log.i(TAG, "mac : " + node.getMacAddress() + " position : " + node.getLatitude() + " " + node.getLongitude() + " rssi " + node.getRssi());
    }

    /**
     * Method to reach the MAC address of devices below Android 10
     */
    private String getRealMacAddress() {
        try {
            List<NetworkInterface> all = Collections.list(NetworkInterface.getNetworkInterfaces());
            for (NetworkInterface nif : all) {
                if (!nif.getName().equalsIgnoreCase("wlan0")) {
                    continue;
                }
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
            return "";
        }
        return "";
    }

    /**
     * Method to generate a random MAC address for devices above Android 10
     */
    private String generateRandomMacAddress() {
        final String uniqueID = UUID.randomUUID().toString().replaceAll("-", "").toUpperCase();
        final StringBuilder sb = new StringBuilder(uniqueID);
        for (int i = 2; i < 30; i = i + 3) {
            sb.insert(i, ":");
        }
        return sb.substring(0, 17);
    }

    /**
     * Method to recuperate a MAC address from the device
     */
    private String getMacAddress() {
        String res = getRealMacAddress();
        if (res.isEmpty()) {
            res = generateRandomMacAddress();
        }
        return res;
    }

    /**
     * Method that calls the LocationService to recuperate the position and speed of the device
     */
    public void getLocation() {
        locationService = new LocationService(MainActivity.this);
        if (locationService.canGetLocation()) {
            node.setPosition(String.valueOf(locationService.getLongitude()), String.valueOf(locationService.getLatitude()));
            node.setSpeed(String.valueOf(locationService.getSpeed()));
        } else {
            Log.e(TAG, "cannot get location");
        }
    }

    /**
     * Method to recuperate the RSSI signal of the device
     */
    private float getRssi() {
        WifiManager manager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        WifiInfo info = manager.getConnectionInfo();
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 0);
        }
        return info.getRssi();
    }

    /**
     * Method that checks if the device is connected to the Internet
     */
    private boolean isConnectedToInternet() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        return connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE).getState() == NetworkInfo.State.CONNECTED ||
                connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI).getState() == NetworkInfo.State.CONNECTED;
    }

    /**
     * Method to recuperate the LTE signal of the device
     */
    private void getLteSignal(Context context) throws SecurityException {
        TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        String strength = null;
        try {
            List<CellInfo> cellInfo = telephonyManager.getAllCellInfo();   //This will give info of all sims present inside your mobile

            if (cellInfo != null) {
                for (int i = 0; i < cellInfo.size(); i++) {
                    if (cellInfo.get(i).isRegistered()) {
                        if (cellInfo.get(i) instanceof CellInfoWcdma) {
                            CellInfoWcdma cellInfoWcdma = (CellInfoWcdma) cellInfo.get(i);
                            CellSignalStrengthWcdma cellSignalStrengthWcdma = cellInfoWcdma.getCellSignalStrength();
                            strength = String.valueOf(cellSignalStrengthWcdma.getDbm());
                        } else if (cellInfo.get(i) instanceof CellInfoGsm) {
                            CellInfoGsm cellInfogsm = (CellInfoGsm) cellInfo.get(i);
                            CellSignalStrengthGsm cellSignalStrengthGsm = cellInfogsm.getCellSignalStrength();
                            strength = String.valueOf(cellSignalStrengthGsm.getDbm());
                        } else if (cellInfo.get(i) instanceof CellInfoLte) {
                            CellInfoLte cellInfoLte = (CellInfoLte) cellInfo.get(i);
                            CellSignalStrengthLte cellSignalStrengthLte = cellInfoLte.getCellSignalStrength();
                            strength = String.valueOf(cellSignalStrengthLte.getDbm());
                        } else if (cellInfo.get(i) instanceof CellInfoCdma) {
                            CellInfoCdma cellInfoCdma = (CellInfoCdma) cellInfo.get(i);
                            CellSignalStrengthCdma cellSignalStrengthCdma = cellInfoCdma.getCellSignalStrength();
                            strength = String.valueOf(cellSignalStrengthCdma.getDbm());
                        }
                    }
                }
            }
            if (strength != null) {
                this.node.setLteSignal(strength);
            }
        } catch (SecurityException e) {
            Log.e(TAG, "not allowed to get lte");
        }
    }

    /**
     * Method to display the private chat interface corresponding to the name of the device clicked on
     */
    public void onItemClick(String neighborId) {
        privateChatFragment.setBadhocMessages(neighborId);
        privateChatFragment.setConversationId(neighborId);
        loadFragment(privateChatFragment);
    }

    /**
     * Getter for the node object
     */
    public Node getNode() {
        return node;
    }

    /**
     * Getter for the notification fragment object
     */
    public NotificationFragment getNotificationFragment() {
        return notificationFragment;
    }

    /**
     * Getter for the Around Me fragment object
     */
    public AroundMeFragment getAroundMeFragment() {
        return aroundMeFragment;
    }

    /**
     * Getter for the Private Chat fragment object
     */
    public PrivateChatFragment getPrivateChatFragment() {
        return privateChatFragment;
    }

    /**
     * Getter for the broadcast fragment object
     */
    public BroadcastChatFragment getBroadcastFragment() {
        return broadcastChatFragment;
    }

    /**
     * Method to send a broadcast intent from the main activity
     */
    public void broadcastIntentAction(String action, String content) {
        this.intent.putExtra(action, content);
        sendBroadcast(this.intent);
    }

    /**
     * Method that checks if the Service passed is still running
     */
    public boolean isServiceRunning(Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

    /**
     * Method to send a message of type broadcast to other devices connected using Bridgefy
     */
    private void broadcastMessageToNeighbors(final String broadcastType) {
        HashMap<String, Object> content = new HashMap<>();
        content.put(Tag.PAYLOAD_DEVICE_NAME.value, Build.MANUFACTURER + " " + Build.MODEL);
        content.put(Tag.PAYLOAD_BROADCAST_TYPE.value, broadcastType);

        Message.Builder builder = new Message.Builder();
        builder.setContent(content);
        Bridgefy.sendBroadcastMessage(builder.build(),
                BFEngineProfile.BFConfigProfileLongReach);
    }

    /**
     * Timer task object that determines if the current node is dominant or not
     */
    TimerTask timerTask = new TimerTask() {
        @Override
        public void run() {
            if (isConnectedToInternet() && node.isDominant() == Status.DOMINATED.value && node.getDominant() == null) {
                node.setIsDominant(Status.DOMINATING.value);
                broadcastIntentAction(Tag.ACTION_CONNECT.value, "connect");
                broadcastMessageToNeighbors(Tag.PAYLOAD_POTENTIAL_DOMINANT.value);
                Log.i(TAG, "I am dominant");
            }
            if (!isConnectedToInternet() && node.isDominant() == Status.DOMINATING.value) {
                node.setIsDominant(Status.DOMINATED.value);
                broadcastIntentAction(Tag.ACTION_CONNECT.value, "disconnect");
                broadcastMessageToNeighbors(Tag.PAYLOAD_NO_LONGER_DOMINANT.value);
                Log.i(TAG, "I am dominated : no Internet");
            }
        }
    };

    /**
     * Method that starts the timer task object that runs every 10 seconds
     */
    private void determinesIfDominant() {
        timer.scheduleAtFixedRate(timerTask, 0, 10000);
    }

}
