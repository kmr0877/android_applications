package defapp.com.networkapp;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.TrafficStats;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Handler;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class LinkLayerInfoActivity extends AppCompatActivity
{

    private WifiManager mWifiManager;
    private boolean havePermissions = false;
    private TextView infoTextView, speedTextView, noOfAPTextView, connectedAPTextView;
    private ListView resultsListView;

    final double [] RXOld = new double [1];
    private List<ScanResult> apResults;
    List<ScanResult> tmpList;
    private List<String> results;
    private APHelper apHelper;
    private boolean connectedFirstTime;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_link_layer_info);
        infoTextView = (TextView) findViewById(R.id.infoTextView);
        speedTextView = (TextView) findViewById(R.id.speedTextView);
        noOfAPTextView = (TextView) findViewById(R.id.noOfAPTextView);
        connectedAPTextView = (TextView) findViewById(R.id.connectedAPTextView);
        resultsListView = (ListView) findViewById(R.id.apResultsListView);
        setTitle("Link Layer Info");
        getPermissions();
        mWifiManager = (WifiManager) this.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        apHelper = APHelper.getInstance();

    }

    void getPermissions()
    {
        if(getApplicationContext().checkCallingOrSelfPermission("android.permission.ACCESS_COARSE_LOCATION") == PackageManager.PERMISSION_DENIED)
        {
            ActivityCompat.requestPermissions(LinkLayerInfoActivity.this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION,
                    Manifest.permission.ACCESS_WIFI_STATE, Manifest.permission.CHANGE_WIFI_STATE}, 1);
        }
        else
        {
            havePermissions = true;
        }
    }

    public void buttonClick(View view)
    {
        if(havePermissions)
        {
            if(view.getId() == R.id.fetchInfoButton)
            {
                if (mWifiManager.isWifiEnabled())
                {
                    if (mWifiManager.getConnectionInfo().getNetworkId() == -1)
                    {
                        Toast.makeText(getApplicationContext(), "WiFi is not connected to a network.", Toast.LENGTH_SHORT).show();
                    }
                    else
                    {
                        showNetworkSpeed();
                        startTasks();
                        startScanAP();
                    }
                }
                else
                {
                    Toast.makeText(getApplicationContext(), "Enable WiFi and connect to a network.", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    void  startTasks()
    {
        final Handler mHandler = new Handler();
        mHandler.postDelayed(new Runnable()
        {
            @Override
            public void run()
            {
                int linkSpeed = mWifiManager.getConnectionInfo().getLinkSpeed();
                int freq = mWifiManager.getConnectionInfo().getFrequency();
                int rssi = mWifiManager.getConnectionInfo().getRssi();
                int level = WifiManager.calculateSignalLevel(rssi, 100);
                String distance = calculateDistance(rssi, mWifiManager.getConnectionInfo().getFrequency());
                infoTextView.setText("Protocol : " + getProtocol(linkSpeed, freq) + "\n"
                        + "Signal Strength : "  + rssi + " dBm ("+ level + "%) \n" + "Distance : " + distance + "m\n"
                        + "Frequency : " + mWifiManager.getConnectionInfo().getFrequency() + " MHz" + "\n");

                mHandler.postDelayed(this, 1000);
            }
        }, 1000 );

    }

    public String calculateDistance(double signalLevelInDb, double freqInMHz)
    {
        DecimalFormat df = new DecimalFormat("###.##");
        double exp = (27.55 - (20 * Math.log10(freqInMHz)) + Math.abs(signalLevelInDb)) / 20.0;
        return df.format(Math.pow(10.0, exp));
    }

    String getProtocol(int speed, int freq)
    {
        String protocol = "";
        if(speed <= 11)
        {
            protocol = "802.11b";
        }
        else if(speed <= 54)
        {
            protocol = "802.11a";
            if(freq < 2500)
            {
                protocol = "802.11g";
            }
        }
        else if(speed <= 310)
        {
            protocol = "802.11n";
        }
        else if(speed <= 470)
        {
            protocol = "802.11ac";
        }

        return protocol;
    }

    void showNetworkSpeed()
    {
        final Handler handler = new Handler();
        handler.postDelayed(new Runnable()
        {
            @Override
            public void run()
            {
                double overallTraffic = TrafficStats.getTotalRxBytes();

                double currentDataRate = overallTraffic - RXOld [0];
                currentDataRate /= 1024;

                speedTextView.setText("Data Rate : " + currentDataRate + " kbps.");

                RXOld [0] = overallTraffic;

                handler.postDelayed(this, 1000);
            }
        }, 1000 );

    }

    void startScanAP()
    {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION);
        intentFilter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
        registerReceiver(new BroadcastReceiver()
        {
            @Override
            public void onReceive(Context c, Intent intent)
            {

                if (intent.getExtras().containsKey(ConnectivityManager.EXTRA_NETWORK_INFO))
                {
                    Bundle extras = intent.getExtras();
                    String networkInfo = extras.get("networkInfo").toString();

                    if(intent.getIntExtra(ConnectivityManager.EXTRA_NETWORK_TYPE,-1) == 1 &&
                            !networkInfo.toUpperCase().contains("DISCONNECT"))
                    {
                        WifiInfo mWifiInfo = mWifiManager.getConnectionInfo();
                        connectedAPTextView.setText("Connected to: " + mWifiInfo.getSSID());;
                    }

                }
                else
                {
                    apResults = mWifiManager.getScanResults();

                    setAPResultsListView();
                }

            }
        }, intentFilter);
        mWifiManager.startScan();
    }

    void setAPResultsListView()
    {
        results = new ArrayList<String>();

        // Add only 4 higher strength AP
        List<String> uniqueRes = new ArrayList<String>();

        for(int i=0;i<apResults.size();i++)
        {
            if(!uniqueRes.contains(apResults.get(i).SSID) && apResults.get(i).SSID.length() > 0)
            {
                uniqueRes.add(apResults.get(i).SSID);
            }
        }
        tmpList = new ArrayList<ScanResult>();
        for(int i=0;i<uniqueRes.size();i++)
        {
            for (int j = 0; j < apResults.size(); j++)
            {
                if (apResults.get(j).SSID.equals(uniqueRes.get(i)))
                {
                    tmpList.add(apResults.get(j));
                }
            }
        }
        Collections.sort(tmpList, new Comparator<ScanResult>()
        {

            public int compare(ScanResult o1, ScanResult o2)
            {
                if(o2.level > o1.level)
                    return 1;
                return 0;
            }
        });
        noOfAPTextView.setText("Number of access points available : " + tmpList.size());
        for(int k=0;k<tmpList.size();k++)
        {

            results.add(tmpList.get(k).SSID.toString() +  " " + tmpList.get(k).level + " - " +
                    tmpList.get(k).BSSID.toString());
        }

        ArrayAdapter arrayAdapter=new ArrayAdapter(this,android.R.layout.simple_list_item_1, android.R.id.text1, results);
        resultsListView.setAdapter(arrayAdapter);
        apHelper.setAPList(tmpList);

        if(connectedFirstTime == false)
        {
            connectToBestNetwork(tmpList.get(0));
            connectedFirstTime = true;
        }
    }

    void connectToBestNetwork(ScanResult bestAP)
    {
        mWifiManager.setWifiEnabled(true);
        if(bestAP.capabilities.toUpperCase().contains("WPA") ||
                bestAP.capabilities.toUpperCase().contains("WEP"))
        {

        }
        else
        {
            WifiConfiguration wifiConfig = new WifiConfiguration();
            wifiConfig.SSID = String.format("\"%s\"", bestAP.SSID);
            int netId = mWifiManager.addNetwork(wifiConfig);
            mWifiManager.disconnect();
            mWifiManager.enableNetwork(netId, true);
            mWifiManager.reconnect();
        }


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
