package com.igm.badhoc.service;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;

import com.bridgefy.sdk.client.BFEngineProfile;
import com.bridgefy.sdk.client.Bridgefy;
import com.bridgefy.sdk.client.Message;
import com.igm.badhoc.R;
import com.igm.badhoc.model.Tag;

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

public class ServerService extends Service {

    private final String TAG = "ServerService";
    private MqttAndroidClient client;
    private final Intent intent = new Intent(Tag.INTENT_SERVER_SERVICE.value);
    private Timer timer;
    private String messageJson = "{}";
    private final String subscribeTopic = "#";
    private final String publishTopic = "nodekeepalive";
    private final String url = "ssl://a162zzet6rcfvu-ats.iot.us-west-2.amazonaws.com:8883";
    private String messageReceived;
    private boolean doReconnect;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        messageJson = intent.getStringExtra(Tag.ACTION_UPDATE_NODE_INFO.value);
        return START_NOT_STICKY;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        registerReceiver(receiver, new IntentFilter(Tag.INTENT_MAIN_ACTIVITY.value));
        client = new MqttAndroidClient(getApplicationContext(), url,
                MqttClient.generateClientId());
        doReconnect = true;
        connect();
        initializeTimerForPublish();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        doReconnect = false;
        timer.cancel();
        unregisterReceiver(receiver);
        sendBroadcast(intent.putExtra(Tag.ACTION_CHANGE_TITLE.value, "Notifications from dominant"));
        try {
            client.disconnect();
            Log.e(TAG, "server disconnected after destroy service");
        } catch (MqttException e) {
            e.printStackTrace();
            Log.e(TAG, "error server disconnect");
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    MqttCallback mqttCallback = new MqttCallback() {
        @Override
        public void connectionLost(Throwable cause) {
            Log.e(TAG, "connection lost");
            if(doReconnect){
                connect();
            }
            sendBroadcast(intent.putExtra(Tag.ACTION_CHANGE_TITLE.value, "Notifications from dominant"));
        }

        @Override
        public void messageArrived(String topic, MqttMessage message) throws Exception {
            Log.i(TAG, "topic: " + topic + ", msg: " + new String(message.getPayload()));
            messageReceived = new String(message.getPayload());
            sendBroadcast(intent.putExtra(Tag.ACTION_MESSAGE_RECEIVED.value, messageReceived));
            broadcastMessageFromServer(messageReceived);
        }

        @Override
        public void deliveryComplete(IMqttDeliveryToken token) {
            Log.i(TAG, "msg delivered");
        }
    };

    IMqttActionListener mqttConnectActionListener = new IMqttActionListener() {
        @Override
        public void onSuccess(IMqttToken asyncActionToken) {
            // We are connected
            Log.e(TAG, "connected to server " + client.isConnected());
            subscribeToTopic(subscribeTopic);
            sendBroadcast(intent.putExtra(Tag.ACTION_CHANGE_TITLE.value, "Notifications from server"));
        }

        @Override
        public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
            // Something went wrong e.g. connection timeout or firewall problems
            Log.e(TAG, "not connected to server " + exception);
        }
    };

    private void connect() {
        InputStream caCrtFile = getApplicationContext().getResources().openRawResource(R.raw.ca);
        InputStream crtFile = getApplicationContext().getResources().openRawResource(R.raw.cert);
        InputStream keyFile = getApplicationContext().getResources().openRawResource(R.raw.key);

        MqttConnectOptions options = new MqttConnectOptions();
        try {
            options.setUserName("uge");
            options.setPassword("badzak".toCharArray());
            //options.setAutomaticReconnect(true);
            options.setKeepAliveInterval(10);
            SSLSocketFactory sslSocketFactory = setCertificate(caCrtFile, crtFile, keyFile);
            options.setSocketFactory(sslSocketFactory);
        } catch (CertificateException e) {
            e.printStackTrace();
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

    public void publishMessage(final String publishTopic, final String messageJson) {
        Log.e(TAG, "in publish " + messageJson + client.isConnected());
        if (client.isConnected() && !messageJson.equals("{}")) {
            Log.e(TAG, "in publish and message is updated " + messageJson);
            try {
                MqttMessage message = new MqttMessage();
                message.setPayload(messageJson.getBytes());
                client.publish(publishTopic, message, null, new IMqttActionListener() {
                    @Override
                    public void onSuccess(IMqttToken asyncActionToken) {
                        Log.e(TAG, "publish succeed!");
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

    public void subscribeToTopic(final String subTopic) {
        Log.e(TAG, "in subscribe");
        if (client.isConnected()) {
            try {
                client.subscribe(subTopic, 0, null, new IMqttActionListener() {
                    @Override
                    public void onSuccess(IMqttToken asyncActionToken) {
                        Log.e(TAG, "Successfully subscribed to topic " + subTopic);
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

    private final BroadcastReceiver receiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            Log.e(TAG, "in receiver of service");
            messageJson = intent.getStringExtra(Tag.ACTION_UPDATE_NODE_INFO.value);
        }
    };

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

    private void initializeTimerForPublish() {
        TimerTask timerTask = new TimerTask() {
            @Override
            public void run() {
                publishMessage(publishTopic, messageJson);
            }
        };
        timer = new Timer();
        timer.scheduleAtFixedRate(timerTask, 30000, 60000);
    }

    private void broadcastMessageFromServer(String messageFromServer) {
        HashMap<String, Object> content = new HashMap<>();
        content.put(Tag.PAYLOAD_TEXT.value, messageFromServer);
        content.put(Tag.PAYLOAD_DEVICE_NAME.value, Build.MANUFACTURER + " " + Build.MODEL);
        content.put(Tag.PAYLOAD_FROM_SERVER.value, "true");

        Message.Builder builder = new Message.Builder();
        builder.setContent(content);

        Bridgefy.sendBroadcastMessage(builder.build(),
                BFEngineProfile.BFConfigProfileLongReach);
    }

}
