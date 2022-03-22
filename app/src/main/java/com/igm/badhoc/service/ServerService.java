package com.igm.badhoc.service;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import com.bridgefy.sdk.client.BFEngineProfile;
import com.bridgefy.sdk.client.Bridgefy;
import com.bridgefy.sdk.client.Message;
import com.igm.badhoc.R;
import com.igm.badhoc.model.Tag;
import com.igm.badhoc.util.ParserUtil;

import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.openssl.PEMKeyPair;
import org.bouncycastle.openssl.PEMParser;
import org.bouncycastle.openssl.jcajce.JcaPEMKeyConverter;
import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;

import java.io.BufferedInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.security.KeyPair;
import java.security.KeyStore;
import java.security.Security;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.HashMap;
import java.util.Timer;
import java.util.TimerTask;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManagerFactory;

/**
 * Service implementing the connexion to the remote MQTT server
 */
public class ServerService extends Service {
    /**
     * Debug Tag used in logging
     */
    private final String TAG = "ServerService";
    /**
     * Intent object to communicate with the notification fragment
     */
    private final Intent intent = new Intent(Tag.INTENT_SERVER_SERVICE.value);
    /**
     * URL of the remote server to connect to
     */
    private static final String url = "ssl://a162zzet6rcfvu-ats.iot.us-west-2.amazonaws.com:8883";
    /**
     * Recurring timer that publishes a message
     */
    private Timer timer;
    /**
     * Message to publish on the nodekeepalive topic
     */
    private String messageJson = "{}";
    /**
     * MQTT client used to connect to the remote server
     */
    private MqttAndroidClient client;
    /**
     * Boolean indicating if the service should try to reconnect to the server
     */
    private boolean doReconnect;

    /**
     * Method called on start of the service :
     * recuperates the content of the message from the main activity to publish to the nodekeepalive topic
     */
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        messageJson = intent.getStringExtra(Tag.ACTION_UPDATE_NODE_INFO.value);
        return START_NOT_STICKY;
    }

    /**
     * Method called when the service is created
     */
    @Override
    public void onCreate() {
        super.onCreate();
        registerReceiver(receiver, new IntentFilter(Tag.INTENT_MAIN_ACTIVITY.value));
        client = new MqttAndroidClient(getApplicationContext(), url,
                MqttClient.generateClientId());
        doReconnect = true;
        handleApiAbove26();
        connect();
        initializeTimerForPublish();
    }

    /**
     * Method called for devices using android sdk above 26, the service needs to be started as a foreground service
     */
    private void handleApiAbove26() {
        if (Build.VERSION.SDK_INT >= 26) {
            String NOTIFICATION_CHANNEL_ID = "com.igm.badhoc";
            String channelName = "Server Service";
            NotificationChannel chan = new NotificationChannel(NOTIFICATION_CHANNEL_ID, channelName, NotificationManager.IMPORTANCE_NONE);
            chan.setLightColor(Color.BLUE);
            chan.setLockscreenVisibility(Notification.VISIBILITY_PRIVATE);
            NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            assert manager != null;
            manager.createNotificationChannel(chan);

            NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID);
            Notification notification = notificationBuilder.setOngoing(true)
                    .setSmallIcon(R.drawable.badhoc)
                    .setContentTitle("App is running in background")
                    .setPriority(NotificationManager.IMPORTANCE_MIN)
                    .setCategory(Notification.CATEGORY_SERVICE)
                    .build();
            startForeground(1, notification);
        }
    }

    /**
     * Method called on the destroyment of the service to close all resources
     */
    @Override
    public void onDestroy() {
        super.onDestroy();
        doReconnect = false;
        timer.cancel();
        unregisterReceiver(receiver);
        sendBroadcast(intent.putExtra(Tag.ACTION_CHANGE_TITLE.value, Tag.TITLE_NOT_DOMINANT.value));
        try {
            client.disconnect();
            Log.i(TAG, "Destroy service");
        } catch (MqttException | NullPointerException e) {
            e.printStackTrace();
            Log.e(TAG, "error server disconnect");
        }
    }


    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    /**
     * MqttCallback object used to listen for the loss of connection, the arrival of a message, and
     * the successful delivery of a message
     */
    MqttCallback mqttCallback = new MqttCallback() {
        /**
         * Method called if the connection is lost
         * @param cause cause of the end of the connec<stion
         */
        @Override
        public void connectionLost(Throwable cause) {
            Log.e(TAG, "Connection lost");
            if (doReconnect) {
                connect();
            }
            sendBroadcast(intent.putExtra(Tag.ACTION_CHANGE_TITLE.value, Tag.TITLE_NOT_DOMINANT.value));
        }

        /**
         * Method called when a message is received from a subscribed topic
         * @param topic topic from where the message comes from
         * @param message message received from the topic
         */
        @Override
        public void messageArrived(String topic, MqttMessage message) {
            Log.i(TAG, "topic: " + topic + ", msg: " + new String(message.getPayload()));
            String messageReceived = new String(message.getPayload());
            String parsedResponse = ParserUtil.parseTopicNotifsResponse(messageReceived);
            sendBroadcast(intent.putExtra(Tag.ACTION_NOTIFICATION_RECEIVED.value, parsedResponse));
            broadcastMessageFromServer(parsedResponse);
        }

        /**
         * Method called when a message is successfully delivered to a topic
         */
        @Override
        public void deliveryComplete(IMqttDeliveryToken token) {
            Log.i(TAG, "msg delivered");
        }
    };

    /**
     * Listener for the establishment of the connection
     */
    IMqttActionListener mqttConnectActionListener = new IMqttActionListener() {
        /**
         * Method called if the connection is successful :
         * the service then subscribes to the topic and updates the title of the notification fragment
         */
        @Override
        public void onSuccess(IMqttToken asyncActionToken) {
            // We are connected
            Log.i(TAG, "Connected to server " + client.isConnected());
            subscribeToTopic(Tag.TOPIC_NOTIFS.value);
            sendBroadcast(intent.putExtra(Tag.ACTION_CHANGE_TITLE.value, Tag.TITLE_DOMINANT.value));
        }

        /**
         * Method called on failure of the connection :
         * the service tries to reconnect
         */
        @Override
        public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
            // Something went wrong e.g. connection timeout or firewall problems
            connect();
            Log.e(TAG, "not connected to server " + exception);
        }
    };

    /**
     * Method to connect to the remote server. Implements the options of the connection
     */
    private void connect() {
        InputStream caCrtFile = getApplicationContext().getResources().openRawResource(R.raw.ca);
        InputStream crtFile = getApplicationContext().getResources().openRawResource(R.raw.cert);
        InputStream keyFile = getApplicationContext().getResources().openRawResource(R.raw.key);

        MqttConnectOptions options = new MqttConnectOptions();
        try {
            options.setUserName("uge");
            options.setPassword("badzak".toCharArray());
            options.setKeepAliveInterval(10);
            SSLSocketFactory sslSocketFactory = setCertificate(caCrtFile, crtFile, keyFile);
            options.setSocketFactory(sslSocketFactory);
        } catch (CertificateException e) {
            e.printStackTrace();
            Log.e(TAG, "error certificate");
        }

        client.setCallback(mqttCallback);
        try {
            IMqttToken token = client.connect(options);
            token.setActionCallback(mqttConnectActionListener);
        } catch (MqttException e) {
            e.printStackTrace();
            Log.e(TAG, "error server");
        }
    }

    /**
     * Method called to publish a message on a topic
     *
     * @param publishTopic topic to publish on
     * @param messageJson  message to publish
     */
    public void publishMessage(final String publishTopic, final String messageJson) {
        if (client.isConnected() && !messageJson.equals("{}")) {
            try {
                MqttMessage message = new MqttMessage();
                message.setPayload(messageJson.getBytes());
                client.publish(publishTopic, message, null, new IMqttActionListener() {
                    @Override
                    public void onSuccess(IMqttToken asyncActionToken) {
                        Log.i(TAG, "publish succeed!");
                    }

                    @Override
                    public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                        Log.i(TAG, "publish failed! : " + client.getResultData());
                    }
                });
            } catch (MqttException e) {
                e.printStackTrace();
                Log.e(TAG, "Failed to publish to topic." + e);
            }
        }
    }

    /**
     * Method called to subscribe to a topic
     *
     * @param subTopic topic to subscribe to
     */
    public void subscribeToTopic(final String subTopic) {
        if (client.isConnected()) {
            try {
                client.subscribe(subTopic, 0, null, new IMqttActionListener() {
                    @Override
                    public void onSuccess(IMqttToken asyncActionToken) {
                        Log.i(TAG, "Successfully subscribed to topic " + subTopic);
                    }

                    @Override
                    public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                        Log.e(TAG, "Failed to subscribed to topic." + exception);
                    }
                });
            } catch (MqttException e) {
                Log.e(TAG, "in subscribe error " + e);
                e.printStackTrace();
            }
        }
    }

    /**
     * BroadcastReceiver object that listens for updates of the node content from the main activity
     * or for a message to send to the server
     */
    private final BroadcastReceiver receiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            if (intent.getStringExtra(Tag.ACTION_UPDATE_NODE_INFO.value) != null) {
                messageJson = intent.getStringExtra(Tag.ACTION_UPDATE_NODE_INFO.value);
            }
            if (intent.getStringExtra(Tag.ACTION_SEND_MESSAGE_TO_SERVER.value) != null) {
                String messageForServerJson = intent.getStringExtra(Tag.ACTION_SEND_MESSAGE_TO_SERVER.value);
                publishMessage(Tag.TOPIC_TO_SERVER.value, messageForServerJson);
            }
        }
    };

    /**
     * Method that sets the certificate for the SSL connection
     *
     * @param caCrtFile CA certificate
     * @param crtFile   client certificate
     * @param keyFile   client private key
     * @return SSLSocketFactory for the connection
     */
    private SSLSocketFactory setCertificate(InputStream caCrtFile, InputStream crtFile, InputStream keyFile) throws CertificateException {
        Security.addProvider(new BouncyCastleProvider());
        // Load CAs from an InputStream
        try {
            // load CA certificate
            X509Certificate caCert = null;

            BufferedInputStream bis = new BufferedInputStream(caCrtFile);
            CertificateFactory cf = CertificateFactory.getInstance("X.509");

            while (bis.available() > 0) {
                caCert = (X509Certificate) cf.generateCertificate(bis);
            }

            // load client certificate
            bis = new BufferedInputStream(crtFile);
            X509Certificate cert = null;
            while (bis.available() > 0) {
                cert = (X509Certificate) cf.generateCertificate(bis);
            }

            // load client private cert
            PEMParser pemParser = new PEMParser(new InputStreamReader(keyFile));
            Object object = pemParser.readObject();
            JcaPEMKeyConverter converter = new JcaPEMKeyConverter();
            KeyPair key = converter.getKeyPair((PEMKeyPair) object);

            KeyStore caKs = KeyStore.getInstance(KeyStore.getDefaultType());
            caKs.load(null, null);
            caKs.setCertificateEntry("cert-certificate", caCert);
            TrustManagerFactory tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
            tmf.init(caKs);

            KeyStore ks = KeyStore.getInstance(KeyStore.getDefaultType());
            ks.load(null, null);
            ks.setCertificateEntry("certificate", cert);
            ks.setKeyEntry("private-cert", key.getPrivate(), "".toCharArray(),
                    new java.security.cert.Certificate[]{cert});
            KeyManagerFactory kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
            kmf.init(ks, "".toCharArray());

            SSLContext context = SSLContext.getInstance("TLSv1.2");
            context.init(kmf.getKeyManagers(), tmf.getTrustManagers(), null);
            return context.getSocketFactory();
        } catch (Exception e) {
            Log.e(TAG, "Error generating the certificate: " + e);
            return null;
        }
    }

    /**
     * Method that defines the recurring task that publishes a message to the topic
     * every 60 seconds
     */
    private void initializeTimerForPublish() {
        TimerTask timerTask = new TimerTask() {
            @Override
            public void run() {
                publishMessage(Tag.TOPIC_KEEP_ALIVE.value, messageJson);
            }
        };
        timer = new Timer();
        timer.scheduleAtFixedRate(timerTask, 30000, 60000);
    }

    /**
     * Method that sends a broadcast message to devices connected using Bridgefy
     *
     * @param messageFromServer message to send to the other devices
     */
    private void broadcastMessageFromServer(String messageFromServer) {
        HashMap<String, Object> content = new HashMap<>();
        content.put(Tag.PAYLOAD_TEXT.value, messageFromServer);
        content.put(Tag.PAYLOAD_DEVICE_NAME.value, Build.MANUFACTURER + " " + Build.MODEL);
        content.put(Tag.PAYLOAD_BROADCAST_TYPE.value, Tag.PAYLOAD_FROM_SERVER.value);
        Message.Builder builder = new Message.Builder();
        builder.setContent(content);

        Bridgefy.sendBroadcastMessage(builder.build(),
                BFEngineProfile.BFConfigProfileLongReach);
    }


}
