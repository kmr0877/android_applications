package com.example.mohan.myfirstapp;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;

public class MainActivity extends BroadcastReceiver {
    WifiApp1Activity wifiDemo;
    private Context WifiDemo;

    public MainActivity(WifiApp1Activity wifiDemo) {
        super();
        this.wifiDemo = wifiDemo;

    }

    @Override
    public void onRecieve(Context c, Intent intent) {
        List<ScanResult> results = wifiDemo.wifi.getScanResults();
        ScanResult bestSignal = null;
        for (ScanResult result : results) {
            if (bestSignal == null
                    || WifiManager.compareSignalLevel(bestSignal.level, result.level) < 0)
                bestSignal = result;
        }
        String message = String.format("%s networks found. %s is th strongest",
                results.size(), bestSignal.SSID);
        Toast.makeText(WifiDemo, message, Toast.LENGTH_LONG).show();
    }

}
}

public class WifiApp1Activity extends Activity implements OnClickListener {
    WifiManager wifi;
    BroadcastReceiver receiver;
    TextView textStatus;
    Button buttonScan;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        textStatus = (TextView) findViewById(R.id.Status);
        buttonScan = (Button) findViewById(R.id.scan);
        buttonScan.setOnClickListener(this);

        wifi = (WifiManager) getSystemService(Context.WIFI_SERVICE);

        WifiInfo info = wifi.getConnectionInfo();
        textStatus.append("\n\nWifi Status:" + info.toString());

        List<WifiConfiguration> configs = wifi.getConfiguredNetworks();
        for (WifiConfiguration config : configs) {
            textStatus.append("\n\n" + config.toString());
        }
        if (receiver == null)
            receiver = new WiFiScanReceiver(this);
        registerReceiver(receiver, new IntentFilter(
                WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));
    }

    public void onClick(View view) {
        Toast.makeText(this, "Scanning.....",
                Toast > Toast.LENGTH_LONG).show();

        wifi.startScan();
    }

}