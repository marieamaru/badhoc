package com.igm.badhoc.service;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import com.igm.badhoc.R;
import com.igm.badhoc.model.Tag;

import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.openssl.PEMKeyPair;
import org.bouncycastle.openssl.PEMParser;
import org.bouncycastle.openssl.jcajce.JcaPEMKeyConverter;
import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttToken;
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

    @Override
    public void onCreate() {
        super.onCreate();

        registerReceiver(receiver, new IntentFilter(Tag.INTENT_MAIN_ACTIVITY.value));

        InputStream caCrtFile = getApplicationContext().getResources().openRawResource(R.raw.ca);
        InputStream crtFile = getApplicationContext().getResources().openRawResource(R.raw.cert);
        InputStream keyFile = getApplicationContext().getResources().openRawResource(R.raw.key);


        if (Build.VERSION.SDK_INT >= 26) {
            String CHANNEL_ID = "my_channel_01";
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID,
                    "Channel human readable title",
                    NotificationManager.IMPORTANCE_DEFAULT);

            ((NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE)).createNotificationChannel(channel);

            Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                    .setContentTitle("")
                    .setContentText("").build();

            startForeground(1, notification);
        }

        String badzakUrl = "ssl://a162zzet6rcfvu-ats.iot.us-west-2.amazonaws.com:8883";
        client = new MqttAndroidClient(getApplicationContext(), badzakUrl,
                MqttClient.generateClientId());
        try {
            MqttConnectOptions options = new MqttConnectOptions();
            options.setUserName("uge");
            options.setPassword("badzak".toCharArray());
            options.setAutomaticReconnect(true);
            SSLSocketFactory sslSocketFactory = setCertificate(caCrtFile, crtFile, keyFile);
            options.setSocketFactory(sslSocketFactory);
            IMqttToken token = client.connect(options);
            token.setActionCallback(new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {
                    // We are connected
                    Log.e(TAG, "connected to server");
                }

                @Override
                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                    // Something went wrong e.g. connection timeout or firewall problems
                    Log.e(TAG, "not connected to server " + exception);
                }
            });

        } catch (MqttException | CertificateException e) {
            e.printStackTrace();
            Log.e(TAG, "error server");
        }
        TimerTask timerTask = new TimerTask() {
            @Override
            public void run() {
                publishMessage("nodekeepalive", messageJson);
            }
        };
        timer = new Timer();
        timer.scheduleAtFixedRate(timerTask, 30000, 20000);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        timer.cancel();
        unregisterReceiver(receiver);
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

    public void publishMessage(final String publishTopic, final String messageJson) {
        if (client.isConnected() && !messageJson.equals("{}")) {
            try {
                MqttMessage message = new MqttMessage();
                message.setPayload(messageJson.getBytes());
                client.publish(publishTopic, message);
                sendBroadcast(intent.putExtra("publish", messageJson));
            } catch (MqttException e) {
                e.printStackTrace();
            }
        }
    }

    private final BroadcastReceiver receiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            messageJson = intent.getStringExtra(Tag.ACTION_UPDATE_NODE_INFO.value);
            publishMessage("nodekeepalive", messageJson);
        }
    };

}
