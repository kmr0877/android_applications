package com.example.mohan.wifiscanner;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.InputType;
import android.text.format.Formatter;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class MainActivity extends AppCompatActivity
{
    private ListView resultsListView;
    private WifiManager mWifiManager;
    private List<ScanResult> apResults;
    private List<String> results;

    private String password;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        resultsListView = (ListView) findViewById(R.id.resultsListView);
        resultsListView.setOnItemClickListener(new AdapterView.OnItemClickListener()
        {
            @Override
            public void onItemClick(AdapterView<?> av, View v, int pos, long id)
            {
                connectToANetwork(pos);
            }
        });

    }

    public void buttonClick(View view)
    {
        scanForAP();
    }

    public void scanForAP()
    {
        if(getApplicationContext().checkCallingOrSelfPermission("android.permission.ACCESS_COARSE_LOCATION") == PackageManager.PERMISSION_DENIED)
        {
            ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION,
                    Manifest.permission.ACCESS_WIFI_STATE, Manifest.permission.CHANGE_WIFI_STATE}, 1);
        }
        else if(getApplicationContext().checkCallingOrSelfPermission(Manifest.permission.ACCESS_WIFI_STATE) == PackageManager.PERMISSION_GRANTED)
        {
            mWifiManager = (WifiManager) this.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
            if(mWifiManager.getWifiState() != WifiManager.WIFI_STATE_ENABLING)
            {
                mWifiManager.setWifiEnabled(true);
                startScan();
            }
            else
            {
                startScan();
            }
        }
    }

    void startScan()
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
                        //connected to a Wi-Fi Network
                        Toast.makeText(getApplicationContext(),"Connected",Toast.LENGTH_SHORT).show();
                        showConnectedToWiFi();
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
            if(!uniqueRes.contains(apResults.get(i).SSID))
            {
                uniqueRes.add(apResults.get(i).SSID);
            }
        }

        for(int i=0;i<uniqueRes.size();i++)
        {
            List<ScanResult> tmpList = new ArrayList<ScanResult>();
            for(int j=0;j<apResults.size();j++)
            {
                if(apResults.get(j).SSID.equals(uniqueRes.get(i)))
                {
                    tmpList.add(apResults.get(j));
                }
            }
            Collections.sort(tmpList, new Comparator<ScanResult>() {

                public int compare(ScanResult o1, ScanResult o2) {
                    if(o2.level > o1.level)
                        return 1;
                    return 0;
                }
            });
            for(int k=0;k<(tmpList.size()>4?4:tmpList.size());k++)
            {
                String networkType = "";
                if(tmpList.get(k).capabilities.toUpperCase().contains("WPA2"))
                {
                    networkType = "WPA2";
                }
                else if(tmpList.get(k).capabilities.toUpperCase().contains("WPA"))
                {
                    networkType = "WPA";
                }
                else if(tmpList.get(k).capabilities.toUpperCase().contains("WEP"))
                {
                    networkType = "WEP";
                }
                else
                {
                    networkType = "Open";
                }
                results.add(tmpList.get(k).SSID.toString() +  " " + tmpList.get(k).level + " - " +
                        tmpList.get(k).BSSID.toString() + " - " + networkType);
            }
        }

        ArrayAdapter arrayAdapter=new ArrayAdapter(this,android.R.layout.simple_list_item_1, android.R.id.text1, results);
        resultsListView.setAdapter(arrayAdapter);
    }

    void connectToANetwork(int pos)
    {
        mWifiManager.setWifiEnabled(true);
        if(apResults.get(pos).capabilities.toUpperCase().contains("WPA") ||
                apResults.get(pos).capabilities.toUpperCase().contains("WEP"))
        {
            password = "";
            getPassword(pos);
        }
        else
        {
            WifiConfiguration wifiConfig = new WifiConfiguration();
            wifiConfig.SSID = String.format("\"%s\"", apResults.get(pos).SSID);
            int netId = mWifiManager.addNetwork(wifiConfig);
            mWifiManager.disconnect();
            mWifiManager.enableNetwork(netId, true);
            mWifiManager.reconnect();
        }


    }

    void connectWiFi(int pos)
    {
        if(password.length() > 0)
        {
            WifiConfiguration wifiConfig = new WifiConfiguration();
            wifiConfig.SSID = String.format("\"%s\"", apResults.get(pos).SSID);
            wifiConfig.preSharedKey = String.format("\"%s\"", password);
            int netId = mWifiManager.addNetwork(wifiConfig);
            mWifiManager.disconnect();
            mWifiManager.enableNetwork(netId, true);
            mWifiManager.reconnect();
        }
    }

    void showConnectedToWiFi()
    {
        WifiInfo mWifiInfo = mWifiManager.getConnectionInfo();
        final AlertDialog.Builder dlgAlert  = new AlertDialog.Builder(this);
        dlgAlert.setMessage("IP Address: " + Formatter.formatIpAddress(mWifiInfo.getIpAddress()));
        dlgAlert.setTitle("Connected to " + mWifiInfo.getSSID());
        dlgAlert.setPositiveButton("Ok",
                new DialogInterface.OnClickListener()
                {
                    public void onClick(DialogInterface dialog, int which)
                    {
                        //dismiss the dialog
                    }
                });
        dlgAlert.setCancelable(true);
        dlgAlert.create().show();
    }

    void getPassword(final int pos)
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("\tEnter Password");
        final EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        builder.setView(input);
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener()
        {
            @Override
            public void onClick(DialogInterface dialog, int which)
            {
                password = input.getText().toString();
                connectWiFi(pos);
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener()
        {
            @Override
            public void onClick(DialogInterface dialog, int which)
            {
                dialog.cancel();
            }
        });

        builder.show();
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
                    scanForAP();
                }
                return;
            }
            default:
                return;
        }
    }


}
