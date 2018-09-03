package defapp.com.networkapp;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.TimeUnit;

public class L2HandoffActivity extends AppCompatActivity
{
    private TextView timeTakenTextView, timeLostTextView, timeConnectTextView, otherL2InfoTextView;

    private WifiManager mWifiManager;
    private boolean havePermissions = false;
    private WifiInfo mLastConnected, mCurrentConnected;
    private Calendar mCal = Calendar.getInstance();
    private Date mCurrTime, mLastTime;
    private APHelper apHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_l2_handoff);
        timeTakenTextView = (TextView) findViewById(R.id.timeTakenL3TextView);
        timeLostTextView = (TextView) findViewById(R.id.timeLostL3TextView);
        timeConnectTextView = (TextView) findViewById(R.id.timeConnectL3TextView);
        otherL2InfoTextView = (TextView) findViewById(R.id.otherL2HandoffInfoTextView);
        setTitle("L2 Handoff");
        getPermissions();
        mWifiManager = (WifiManager) this.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        apHelper = APHelper.getInstance();
    }

    void getPermissions()
    {
        if(getApplicationContext().checkCallingOrSelfPermission("android.permission.ACCESS_COARSE_LOCATION") == PackageManager.PERMISSION_DENIED)
        {
            ActivityCompat.requestPermissions(L2HandoffActivity.this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION,
                    Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_WIFI_STATE,
                    Manifest.permission.CHANGE_WIFI_STATE, Manifest.permission.INTERNET}, 1);
        }
        else
        {
            havePermissions = true;
        }
    }

    public void buttonClick(View view)
    {
        if(view.getId() == R.id.chkL2HandoffButton)
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
                            mLastConnected = mCurrentConnected;
                            mCal = Calendar.getInstance();
                            mLastTime = mCal.getTime();
                            SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                            String formattedDate = df.format(mCal.getTime());
                            timeLostTextView.setText("Disconnected at : " + formattedDate);
                            connectToBestNetwork(apHelper.getAPList().get(0));
                        }
                        else if(networkInfo.toString().toUpperCase().contains("CONNECT") &&
                                !networkInfo.toString().toUpperCase().contains("DISCONNECT"))
                        {

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

    void registerReceiver()
    {

    }

    void setCurrentConnectedDetails()
    {
        if (mWifiManager.getConnectionInfo().getNetworkId() != -1)
        {
            mCurrentConnected = mWifiManager.getConnectionInfo();
            otherL2InfoTextView.setText("Currently Connected to: " + mCurrentConnected.getSSID()
                    + " " + mCurrentConnected.getMacAddress());
            mCal = Calendar.getInstance();
            SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            String formattedDate = df.format(mCal.getTime());
            mCurrTime = mCal.getTime();
            timeConnectTextView.setText("Connected at : " + formattedDate);
            if (mLastConnected != null && mLastTime != null) {
                long difference = mCurrTime.getTime() - mLastTime.getTime();
                difference = TimeUnit.MILLISECONDS.toSeconds(difference);
                timeTakenTextView.setText("Time taken : " + difference + "s.");
                otherL2InfoTextView.setText("Connected to : " + mCurrentConnected.getSSID()
                        + " -- " + mCurrentConnected.getMacAddress()
                        + " from "
                        + mLastConnected.getSSID() + " -- " + mLastConnected.getMacAddress());
            }
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
                    getPermissions();
                }
                return;
            }
            default:
                return;
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
        Intent intent = new Intent(L2HandoffActivity.this, MobilityInfoActivity.class);
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
