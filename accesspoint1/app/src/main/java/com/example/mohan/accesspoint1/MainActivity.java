package com.thechalakas.jay.accesspoint1;

import android.content.Context;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

public class MainActivity extends AppCompatActivity
{

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button buttonGetMacId = (Button) findViewById(R.id.buttonGetMacId);

        buttonGetMacId.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                Log.i("MainActivity","buttonGetMacId Entered");

                String tempBSSID = getMacId();

                Log.i("MainActivity","the BSSID is - " + tempBSSID);

                Log.i("MainActivity","buttonGetMacId Leaving");
            }
        });//end of buttonGetMacId listener

    }//end of oncreate

    public String getMacId() {

        //WifiManager wifiManager = (WifiManager) getSystemService(getApplicationContext().getSystemService(WIFI_SERVICE));
        WifiManager wifiManager1 = (WifiManager) getApplicationContext().getSystemService(WIFI_SERVICE);
        //WifiInfo wifiInfo = wifiManager.getConnectionInfo();
        WifiInfo wifiInfo1 = wifiManager1.getConnectionInfo();
        return  wifiInfo1.getBSSID();
    }
}
