package defapp.com.networkapp;

import android.net.DhcpInfo;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiInfo;

import java.util.ArrayList;
import java.util.List;



public class APHelper
{
    private static List<ScanResult> apList;
    private static WifiInfo mLastConnected, mCurrentConnected;
    private static DhcpInfo mLastDhcpInfo, mCurrentDhcpInfo;

    private static APHelper instance = null;
    protected APHelper()
    {

    }
    public static APHelper getInstance()
    {
        if(instance == null)
        {
            instance = new APHelper();
            apList = new ArrayList<ScanResult>();
        }
        return instance;
    }

    public void setAPList(List<ScanResult> tmpList)
    {
        apList = tmpList;
    }

    public List<ScanResult> getAPList()
    {
        return apList;
    }

    public void setmLastConnected(WifiInfo lastConnected)
    {
        mLastConnected = lastConnected;
    }
    public WifiInfo getmLastConnected()
    {
        return mLastConnected;
    }

    public void setmCurrentConnected(WifiInfo currentConnected)
    {
        mCurrentConnected = currentConnected;
    }


}
