package defapp.com.networkapp;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.DhcpInfo;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.format.Formatter;
import android.view.View;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.concurrent.TimeUnit;

public class L4ConnectivityActivity extends AppCompatActivity
{
    private TextView statusTextView, infoTextView;
    private boolean havePermissions = false;
    private WifiInfo mLastConnected, mCurrentConnected;
    private DhcpInfo mLastDhcpInfo, mCurrentDhcpInfo;
    private WifiManager mWifiManager;
    private APHelper apHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_l4_connectivity);
        statusTextView = (TextView) findViewById(R.id.statusL4TextView);
        infoTextView = (TextView) findViewById(R.id.infoL4TextView);
        setTitle("L4 Connectivity");
        getPermissions();
        mWifiManager = (WifiManager) this.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        apHelper = APHelper.getInstance();
    }

    void getPermissions()
    {
        if(getApplicationContext().checkCallingOrSelfPermission("android.permission.ACCESS_COARSE_LOCATION") == PackageManager.PERMISSION_DENIED)
        {
            ActivityCompat.requestPermissions(L4ConnectivityActivity.this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION,
                    Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.INTERNET,
                    Manifest.permission.ACCESS_WIFI_STATE, Manifest.permission.CHANGE_WIFI_STATE}, 1);
        }
        else
        {
            havePermissions = true;
        }
    }

    public void buttonClick(View view)
    {
        if(view.getId() == R.id.chkL4HandoffButton)
        {
            if(havePermissions)
            {
                view.setEnabled(false);
                startScanAP();
            }
        }
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
                    if(networkInfo.toString().contains("WIFI"))
                    {
                        // Toast.makeText(getApplicationContext(),networkInfo.toString(),Toast.LENGTH_LONG).show();
                        if(networkInfo.toString().toUpperCase().contains("DISCONNECT") && mCurrentConnected != null)
                        {
                            infoTextView.setText("");
                            statusTextView.setText("TCP connection lost.");
                            mLastConnected = mCurrentConnected;
                            mLastDhcpInfo = mCurrentDhcpInfo;
                            infoTextView.setText("Last TCP connection at : " + mLastConnected.getSSID() +
                                    "\nIP : " + Formatter.formatIpAddress(mLastConnected.getIpAddress())
                                    + "\nGateway : " + Formatter.formatIpAddress(mLastDhcpInfo.gateway)
                                    + "\nMAC Address : " + mLastConnected.getMacAddress());
                            connectToBestNetwork(apHelper.getAPList().get(0));

                        }
                        else if(networkInfo.toString().toUpperCase().contains("CONNECT") &&
                                !networkInfo.toString().toUpperCase().contains("DISCONNECT"))
                        {
                            setCurrentConnectedDetails();
                        }
                    }
                }
                else
                {


                }

            }
        }, intentFilter);
        mWifiManager.startScan();
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
                    getPermissions();
                }
                return;
            }
            default:
                return;
        }
    }

    void setCurrentConnectedDetails()
    {
        if (mWifiManager.getConnectionInfo().getNetworkId() != -1)
        {
            mCurrentConnected = mWifiManager.getConnectionInfo();
            mCurrentDhcpInfo = mWifiManager.getDhcpInfo();
            if (mCurrentConnected == mLastConnected) {
                statusTextView.setText("TCP connection retained.");
            } else statusTextView.setText("Got TCP connection.");
            infoTextView.setText("TCP Connection at : " + mCurrentConnected.getSSID() +
                    "\nIP : " + Formatter.formatIpAddress(mCurrentConnected.getIpAddress())
                    + "\nGateway : " + Formatter.formatIpAddress(mCurrentDhcpInfo.gateway)
                    + "\nMAC Address : " + mCurrentConnected.getMacAddress());
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
    public void onBackPressed()
    {
        // Toast.makeText(getApplicationContext(),"Calling Back",Toast.LENGTH_SHORT).show();
        this.moveTaskToBack(true);
        Intent intent = new Intent(L4ConnectivityActivity.this, MobilityInfoActivity.class);
        startActivity(intent);
    }

    @Override
    public void onResume()
    {
        super.onResume();
        startScanAP();
        setCurrentConnectedDetails();
    }


}
