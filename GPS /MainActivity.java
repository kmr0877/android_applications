package com.example.mohan.gpsapp;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.Date;

public class MainActivity extends AppCompatActivity
{
    private TextView gpsStatusTextView, locationTextView;
    private boolean havePermissions = false, gpsEnabled = false;
    private LocationManager mLocationManager;

    private String provider = "";

    private LocationListener mLocationListener = new LocationListener()
    {
        @Override
        public void onLocationChanged(final Location loc)
        {
            String finalText = "";
            long time = loc.getTime();
            Date date = new Date(time);
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            finalText += "Date/Time: " + sdf.format(date) + "\n";
            finalText += "Provider: " + provider + "\n";
            finalText += "Accuracy: " + loc.getAccuracy() + "\n";
            finalText += "Longitude: " + loc.getLongitude() + "\n";
            finalText += "Latitude: " + loc.getLatitude() + "\n";
            finalText += "Altitude: " + loc.getAltitude() + "\n";
            finalText += "Speed: " + loc.getSpeed();
            locationTextView.setText(finalText);

        }

        @Override
        public void onProviderEnabled (String provider)
        {

        }

        @Override
        public void onStatusChanged (String provider, int status, Bundle extras)
        {

        }
        @Override
        public void onProviderDisabled (String provider)
        {

        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        gpsStatusTextView = (TextView) findViewById(R.id.gpsTextView);
        locationTextView = (TextView) findViewById(R.id.locationText);
        mLocationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

        requestPermission();
    }

    void requestPermission()
    {
        if(getApplicationContext().checkCallingOrSelfPermission("android.permission.ACCESS_COARSE_LOCATION") == PackageManager.PERMISSION_DENIED)
        {
            ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION,
                    Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.INTERNET}, 1);
        }
        else
        {
            havePermissions = true;
        }
    }

    public void clickButton(View view)
    {
        if(view.getId() == R.id.gpsStatusButton)
        {
            if(havePermissions)
            {
                getGpsStatus();
            }
            else
            {
                requestPermission();
            }
        }
        else if(view.getId() == R.id.getLocationButton)
        {
            if(havePermissions)
            {
                getLocation();
            }
            else
            {
                requestPermission();
            }
        }
    }

    void getLocation()
    {
        if(gpsEnabled)
        {
            if(mLocationManager.isProviderEnabled(LocationManager.GPS_PROVIDER))
            {
                if(getApplicationContext().checkCallingOrSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED)
                {
                    provider = "GPS";
                    mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 2000, 10, mLocationListener);
                }
            }
            else if(mLocationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER))
            {
                if(getApplicationContext().checkCallingOrSelfPermission("android.permission.ACCESS_COARSE_LOCATION") == PackageManager.PERMISSION_GRANTED)
                {
                    provider = "Network";
                    mLocationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 2000, 10, mLocationListener);
                }
            }
        }
        else
        {
            Toast.makeText(getApplicationContext(),"GPS is not enabled.",Toast.LENGTH_SHORT).show();
        }
    }

    void getGpsStatus()
    {
        if (mLocationManager.isProviderEnabled(LocationManager.GPS_PROVIDER))
        {
            gpsStatusTextView.setText("GPS is active.");
            gpsEnabled = true;
        }
        else
        {
            gpsStatusTextView.setText("GPS is not active.");
            showGPSDisabledAlertToUser();
        }

    }

    private void showGPSDisabledAlertToUser()
    {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        alertDialogBuilder.setMessage("GPS is not enabled in your device. Do you want to go to Settings?")
                .setTitle("Setting GPS")
                .setCancelable(false)
                .setPositiveButton("Settings",
                        new DialogInterface.OnClickListener(){
                            public void onClick(DialogInterface dialog, int id){
                                Intent callGPSSettingIntent = new Intent(
                                        android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                                startActivity(callGPSSettingIntent);
                            }
                        });
        alertDialogBuilder.setNegativeButton("Cancel",
                new DialogInterface.OnClickListener(){
                    public void onClick(DialogInterface dialog, int id){
                        dialog.cancel();
                    }
                });
        AlertDialog alert = alertDialogBuilder.create();
        alert.show();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults)
    {
        switch (requestCode)
        {
            case 1:
            {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)
                {
                    // Got Permission
                    havePermissions = true;
                }
                return;
            }
            default:
                return;
        }
    }
}
