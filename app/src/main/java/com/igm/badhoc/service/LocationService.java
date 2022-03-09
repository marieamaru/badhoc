package com.igm.badhoc.service;

import android.Manifest;
import android.app.Activity;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;

import androidx.core.app.ActivityCompat;

/**
 * Service that implements the collection of the position and speed of the device
 */
public class LocationService extends Service implements LocationListener {
    /**
     * Context to analyse
     */
    private final Context context;

    /**
     * flag for GPS status
     */
    boolean isGPSEnabled = false;

    /**
     * flag for network status
     */
    boolean isNetworkEnabled = false;

    /**
     * flag for GPS status
     */
    boolean canGetLocation = false;
    /**
     * Location of the device
     */
    private Location location;
    /**
     * Latitude of the device
     */
    private double latitude;
    /**
     * Longitude of the device
     */
    private double longitude;
    /**
     * Speed of the device
     */
    private int speed;

    /**
     * The minimum distance to change Updates in meters
     */
    private static final long MIN_DISTANCE_CHANGE_FOR_UPDATES = 10; // 10 meters

    /**
     * The minimum time between updates in milliseconds
     */
    private static final long MIN_TIME_BW_UPDATES = 1000 * 60; // 1 minute

    /**
     * LocationManager for the collection of the location
     */
    protected LocationManager locationManager;

    public LocationService(Context context) {
        this.context = context;
        getLocation();
    }

    /**
     * Method that collects the location and speed of the device
     */
    public void getLocation() {
        try {
            locationManager = (LocationManager) context.getSystemService(LOCATION_SERVICE);
            isGPSEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
            isNetworkEnabled = locationManager
                    .isProviderEnabled(LocationManager.NETWORK_PROVIDER);

            if (isGPSEnabled || isNetworkEnabled) {
                this.canGetLocation = true;
                // First get location from Network Provider
                if (isNetworkEnabled) {
                    //check the network permission
                    if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                        ActivityCompat.requestPermissions((Activity) context, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, 101);
                    }

                    locationManager.requestLocationUpdates(
                            LocationManager.NETWORK_PROVIDER,
                            MIN_TIME_BW_UPDATES,
                            MIN_DISTANCE_CHANGE_FOR_UPDATES, this);


                    Log.d("Network", "Network");
                    if (locationManager != null) {
                        location = locationManager
                                .getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
                    }
                }

                // if GPS Enabled get lat/long using GPS Services
                if (isGPSEnabled) {
                    if (location == null) {
                        //check the network permission
                        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                            ActivityCompat.requestPermissions((Activity) context, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, 101);
                        }

                        locationManager.requestLocationUpdates(
                                LocationManager.GPS_PROVIDER,
                                MIN_TIME_BW_UPDATES,
                                MIN_DISTANCE_CHANGE_FOR_UPDATES, this);

                        Log.d("GPS Enabled", "GPS Enabled");
                        if (locationManager != null) {
                            location = locationManager
                                    .getLastKnownLocation(LocationManager.GPS_PROVIDER);

                        }
                    }
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Method to get latitude
     */
    public double getLatitude() {
        if (location != null) {
            latitude = location.getLatitude();
        }
        // return latitude
        return latitude;
    }

    /**
     * Method to get longitude
     */
    public double getLongitude() {
        if (location != null) {
            longitude = location.getLongitude();
        }
        return longitude;
    }

    /**
     * Method to get speed
     */
    public int getSpeed() {
        if (location != null) {
            speed = (int) location.getSpeed();
        }
        return speed;
    }

    /**
     * Function to check if GPS/wifi is enabled
     *
     * @return boolean
     */
    public boolean canGetLocation() {
        return this.canGetLocation;
    }

    @Override
    public void onLocationChanged(Location location) {
        speed = (int) ((location.getSpeed() * 3600) / 1000);
    }

    @Override
    public void onProviderDisabled(String provider) {
    }

    @Override
    public void onProviderEnabled(String provider) {
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
    }

    @Override
    public IBinder onBind(Intent arg0) {
        return null;
    }
}
