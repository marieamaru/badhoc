package com.igm.badhoc.activity;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.bridgefy.sdk.client.BFBleProfile;
import com.bridgefy.sdk.client.BFEnergyProfile;
import com.bridgefy.sdk.client.BFEngineProfile;
import com.bridgefy.sdk.client.Bridgefy;
import com.bridgefy.sdk.client.BridgefyClient;
import com.bridgefy.sdk.client.Config;
import com.bridgefy.sdk.client.Message;
import com.bridgefy.sdk.client.RegistrationListener;
import com.google.android.material.badge.BadgeDrawable;
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
import com.igm.badhoc.util.DeviceUtil;

import java.util.HashMap;
import java.util.Timer;
import java.util.TimerTask;

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
     * Recurring task updating the device status
     */
    private Timer timer;
    /**
     * Badge notifying a new broadcast message is available on the fragment display
     */
    private BadgeDrawable badgeDrawableBroadcast;
    /**
     * Badge notifying a new private chat message is available on the fragment display
     */
    private BadgeDrawable badgeDrawablePrivateChat;
    /**
     * OnPause flag to enable or disable push notifications
     */
    private boolean onPause;


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

        fragmentManager.beginTransaction().add(R.id.fl_fragment, aroundMeFragment, TAG).commit();
        fragmentManager.beginTransaction().add(R.id.fl_fragment, broadcastChatFragment, TAG).hide(broadcastChatFragment).commit();
        fragmentManager.beginTransaction().add(R.id.fl_fragment, privateChatFragment, TAG).hide(privateChatFragment).commit();
        fragmentManager.beginTransaction().add(R.id.fl_fragment, notificationFragment, TAG).hide(notificationFragment).commit();

        stateListener = new StateListenerImpl(this);
        messageListener = new MessageListenerImpl(this);

        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_nav);
        bottomNavigationView.setOnItemSelectedListener(this);

        badgeDrawableBroadcast = bottomNavigationView.getOrCreateBadge(R.id.action_broadcast);
        badgeDrawablePrivateChat = bottomNavigationView.getOrCreateBadge(R.id.action_private_chat);
        badgeDrawableBroadcast.setVisible(false);
        badgeDrawablePrivateChat.setVisible(false);

        onPause = false;
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
                badgeDrawableBroadcast.setVisible(false);
                return true;
            case R.id.action_private_chat:
                loadFragment(aroundMeFragment);
                badgeDrawablePrivateChat.setVisible(false);
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
     * On pause of the main activity : set the onPause flag to true to enable push notifications
     */
    @Override
    protected void onPause() {
        super.onPause();
        onPause = true;
    }

    /**
     * On resume of the main activity : set the onPause flag to false to disable push notifications
     */
    @Override
    protected void onResume() {
        super.onResume();
        onPause = false;
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
        Config.Builder builder = new Config.Builder();
        builder.setAntennaType(Config.Antenna.BLUETOOTH_LE);
        builder.setAutoConnect(true);
        builder.setEncryption(true);
        builder.setMaxConnectionRetries(3);
        builder.setBleProfile(BFBleProfile.BACKWARDS_COMPATIBLE);
        builder.setEnergyProfile(BFEnergyProfile.HIGH_PERFORMANCE);
        builder.setEngineProfile(BFEngineProfile.BFConfigProfileLongReach);
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
        node.setIsDominant(Status.DOMINATED.value);
        node.setMacAddress(DeviceUtil.getMacAddress());
        node.setRssi(DeviceUtil.getRssi(this));
        getLocation();
        DeviceUtil.getLteSignal(this, this.node);
        determinesIfDominant();
        Log.i(TAG, "mac : " + node.getMacAddress() + " position : " + node.getLatitude() + " " + node.getLongitude() + " rssi " + node.getRssi());
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
            if (DeviceUtil.isConnectedToInternet(getApplicationContext()) && node.isDominant() == Status.DOMINATED.value && node.getDominant() == null) {
                node.setIsDominant(Status.DOMINATING.value);
                broadcastIntentAction(Tag.ACTION_CONNECT.value, "connect");
                broadcastMessageToNeighbors(Tag.PAYLOAD_POTENTIAL_DOMINANT.value);
                Log.i(TAG, "I am dominant");
            }
            if (!DeviceUtil.isConnectedToInternet(getApplicationContext()) && node.isDominant() == Status.DOMINATING.value) {
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

    /**
     * If the application is onPause, displays a push notification for certain events
     *
     * @param str message to print on notifications
     */
    private void printNotify(String str) {
        if (onPause) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                String NOTIFICATION_CHANNEL_ID = "com.igm.badhoc";
                String channelName = "Message channel";
                NotificationChannel channel = new NotificationChannel(NOTIFICATION_CHANNEL_ID, channelName, NotificationManager.IMPORTANCE_DEFAULT);
                NotificationManager manager = getSystemService(NotificationManager.class);
                assert manager != null;
                manager.createNotificationChannel(channel);
                NotificationCompat.Builder builder = new NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID);
                builder.setContentTitle("New Message")
                        .setContentText(str)
                        .setSmallIcon(R.drawable.badhoc)
                        .setAutoCancel(true);
                NotificationManagerCompat managerCompat = NotificationManagerCompat.from(this);
                managerCompat.notify(1, builder.build());
            }
        }
    }

    /**
     * Displays a visual red dot when a private or broadcast message is available,
     * and if the application is onPause, print a push notification
     *
     * @param id tab to display the red badge on
     */
    public void displayNotificationBadge(String id) {
        switch (id) {
            case "broadcast":
                if (currentFragment != broadcastChatFragment)
                    badgeDrawableBroadcast.setVisible(true);
                printNotify(getResources().getString(R.string.push_notification_public));
                break;
            case "private_chat":
                badgeDrawablePrivateChat.setVisible(true);
                printNotify(getResources().getString(R.string.push_notification_private));
                break;
        }
    }

    /**
     * Method that calls the LocationService to recuperate the position and speed of the device
     */
    private void getLocation() {
        LocationService locationService = new LocationService(this);
        if (locationService.canGetLocation()) {
            node.setPosition(String.valueOf(locationService.getLongitude()), String.valueOf(locationService.getLatitude()));
            node.setSpeed(String.valueOf(locationService.getSpeed()));
        } else {
            Log.e(TAG, "cannot get location");
        }
    }

}
