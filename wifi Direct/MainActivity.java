package com.example.mohan.wifidirect;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.net.NetworkInfo;
import android.net.wifi.WpsInfo;
import android.net.wifi.p2p.WifiP2pConfig;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pDeviceList;
import android.net.wifi.p2p.WifiP2pInfo;
import android.net.wifi.p2p.WifiP2pManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.widget.TextViewCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.view.menu.MenuAdapter;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity
{
    private TextView checkStatusTextView;
    private ListView peersListView;

    private boolean isAvailable = false, havePermissions = false;

    private WifiP2pManager p2pManager;
    private WifiP2pManager.Channel mChannel;
    private BroadcastReceiver mReceiver;
    private IntentFilter mIntentFilter;
    private WifiP2pManager.PeerListListener mPeerListListener;
    private WifiP2pManager.ConnectionInfoListener mConnectionInfoListener;
    private List<String> peers;
    private List<WifiP2pDevice> deviceList;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        checkStatusTextView = (TextView) findViewById(R.id.checkTextView);
        peersListView = (ListView) findViewById(R.id.peersListView);
        getPermissions();

        mPeerListListener = new WifiP2pManager.PeerListListener()
        {
            @Override
            public void onPeersAvailable(WifiP2pDeviceList peerList)
            {
                peers = new ArrayList<String>();
                deviceList = new ArrayList<WifiP2pDevice>();
                deviceList.addAll(peerList.getDeviceList());
                for(int i = 0; i < deviceList.size(); i++)
                {
                    peers.add(deviceList.get(i).deviceName + " " + deviceList.get(i).deviceAddress);
                }

                if (peers.size() == 0)
                {
                    Toast.makeText(getApplicationContext(),"No devices found.",Toast.LENGTH_SHORT).show();
                    return;
                }
                else
                {
                    setPeersList();
                }
            }
        };
        mConnectionInfoListener = new WifiP2pManager.ConnectionInfoListener()
        {
            @Override
            public void onConnectionInfoAvailable(WifiP2pInfo wifiP2pInfo)
            {
                Toast.makeText(getApplicationContext(), "Connected. " /*+ wifiP2pInfo.groupOwnerAddress.getHostName() + "."*/,Toast.LENGTH_SHORT).show();
            }
        };
        p2pManager = (WifiP2pManager) getSystemService(Context.WIFI_P2P_SERVICE);
        mChannel = p2pManager.initialize(this, getMainLooper(), null);
        mReceiver = new WiFiDirectBroadcastReceiver(p2pManager, mChannel, mPeerListListener, mConnectionInfoListener, this);
        mIntentFilter = new IntentFilter();
        mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION);
        mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION);
        mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION);
        mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION);
    }

    void getPermissions()
    {
        if(getApplicationContext().checkCallingOrSelfPermission("android.permission.ACCESS_COARSE_LOCATION") == PackageManager.PERMISSION_DENIED)
        {
            ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION,
                    Manifest.permission.ACCESS_WIFI_STATE, Manifest.permission.CHANGE_WIFI_STATE,
                    Manifest.permission.INTERNET, Manifest.permission.ACCESS_NETWORK_STATE,
                    Manifest.permission.CHANGE_NETWORK_STATE}, 1);
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
            if(view.getId() == R.id.checkButton)
            {
                if(p2pManager.WIFI_P2P_STATE_ENABLED != 2)
                {
                    checkStatusTextView.setText("Wi-Fi Direct is not avaiable.");
                }
                else
                {
                    isAvailable = true;
                    checkStatusTextView.setText("Wi-Fi Direct is avaiable.");
                }
            }
            else if(view.getId() == R.id.peersButton)
            {
                if(isAvailable)
                {
                    searchForPeers();
                }
            }
        }
        else
        {
            getPermissions();
        }

    }

    void searchForPeers()
    {
        p2pManager.discoverPeers(mChannel, new WifiP2pManager.ActionListener()
        {
            @Override
            public void onSuccess()
            {
                Toast.makeText(getApplicationContext(),"Peer discovery started.",Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onFailure(int reasonCode)
            {
                Toast.makeText(getApplicationContext(),"Unable to search peers.",Toast.LENGTH_SHORT).show();
            }
        });
    }

    void setPeersList()
    {
        ArrayAdapter arrayAdapter=new ArrayAdapter(this,android.R.layout.simple_list_item_1, android.R.id.text1, peers);
        peersListView.setAdapter(arrayAdapter);
        peersListView.setOnItemClickListener(new AdapterView.OnItemClickListener()
        {
            @Override
            public void onItemClick(AdapterView<?> av, View v, int pos, long id)
            {
                connectToADevice(pos);
            }
        });
    }

    void connectToADevice(int pos)
    {
        final WifiP2pDevice device = deviceList.get(pos);

        WifiP2pConfig config = new WifiP2pConfig();
        config.deviceAddress = device.deviceAddress;
        config.wps.setup = WpsInfo.PBC;

        p2pManager.connect(mChannel, config, new WifiP2pManager.ActionListener() {

            @Override
            public void onSuccess()
            {
                // WiFiDirectBroadcastReceiver will notify us.

            }

            @Override
            public void onFailure(int reason)
            {
                Toast.makeText(MainActivity.this, "Connect failed. Retry.",
                        Toast.LENGTH_SHORT).show();
            }
        });
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

    /* register the broadcast receiver with the intent values to be matched */
    @Override
    protected void onResume()
    {
        super.onResume();
        registerReceiver(mReceiver, mIntentFilter);
    }
    /* unregister the broadcast receiver */
    @Override
    protected void onPause()
    {
        super.onPause();
        unregisterReceiver(mReceiver);
    }


}
