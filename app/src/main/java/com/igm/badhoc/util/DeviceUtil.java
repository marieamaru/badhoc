package com.igm.badhoc.util;

import android.Manifest;
import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
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

import androidx.core.app.ActivityCompat;

import com.igm.badhoc.model.Node;

import java.net.NetworkInterface;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

public class DeviceUtil {

    private final static String TAG = "DeviceUtil";

    /**
     * Method to recuperate a MAC address from the device
     */
    public static String getMacAddress() {
        String res = getRealMacAddress();
        if (res.isEmpty()) {
            res = generateRandomMacAddress();
        }
        return res;
    }

    /**
     * Method to reach the MAC address of devices below Android 10
     */
    public static String getRealMacAddress() {
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
    private static String generateRandomMacAddress() {
        final String uniqueID = UUID.randomUUID().toString().replaceAll("-", "").toUpperCase();
        final StringBuilder sb = new StringBuilder(uniqueID);
        for (int i = 2; i < 30; i = i + 3) {
            sb.insert(i, ":");
        }
        return sb.substring(0, 17);
    }

    /**
     * Method to recuperate the LTE signal of the device
     */
    public static void getLteSignal(Context context, Node node) throws SecurityException {
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
                node.setLteSignal(strength);
            }
        } catch (SecurityException e) {
            Log.e(TAG, "not allowed to get lte");
        }
    }

    /**
     * Method to recuperate the RSSI signal of the device
     */
    public static float getRssi(Context context) {
        WifiManager manager = (WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        WifiInfo info = manager.getConnectionInfo();
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions((Activity) context,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 0);
        }
        return info.getRssi();
    }

    /**
     * Method that checks if the device is connected to the Internet
     */
    public static boolean isConnectedToInternet(Context context) {
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfoMobile = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
        return activeNetworkInfoMobile != null && activeNetworkInfoMobile.isConnected() && activeNetworkInfoMobile.getState() == NetworkInfo.State.CONNECTED ||
                connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI).getState() == NetworkInfo.State.CONNECTED;
    }

    /**
     * Method that checks if the Service passed is still running
     */
    public static boolean isServiceRunning(Class<?> serviceClass, Context context) {
        ActivityManager manager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

}
