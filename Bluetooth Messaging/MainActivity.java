package com.example.mohan.bluetoothapplication;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.LauncherApps;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.AbstractList;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class MainActivity extends AppCompatActivity
{

    private static final String TAG = "MainActivity";
    private static final String NAME = "BluetoothChatSecure";
    private static final UUID MY_UUID = UUID.fromString("fa87c0d0-afac-11de-8a39-0800200c9a66");

    private TextView bluetoothStatusTextView;
    private Button discoveryButton, sendButton;
    private EditText msgEditText;
    private ListView bluetoothDevicesListView;

    private boolean havePermissions = false, bluetoothEnabled = false;

    private BluetoothAdapter mBluetoothAdapter;
    private AcceptThread acceptThread;
    private ConnectedThread connectedThread;
    private BluetoothSocket mSocket;
    private Handler mHandler;

    private final BroadcastReceiver mReceiver = new BroadcastReceiver()
    {
        @Override
        public void onReceive(Context context, Intent intent)
        {
            String action = intent.getAction();

            // When discovery finds a device
            if (BluetoothDevice.ACTION_FOUND.equals(action))
            {
                // Get the BluetoothDevice object from the Intent
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                String tmp = device.getName() + "\n" + device.getAddress();
                if(!devicesStringList.contains(tmp))
                {
                    devicesStringList.add(tmp);
                    btDevicesList.add(device);
                }
                setListAdapter();
                bluetoothStatusTextView.setText("Searching.. Devices Found: " + devicesStringList.size());
            }
            else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action))
            {
                mBluetoothAdapter.cancelDiscovery();
                unregisterReceiver(mReceiver);
                if(btDevicesList.size() == 0)
                {
                    bluetoothStatusTextView.setText("No Devices Available!");

                }
                else
                {
                    // bluetoothStatusTextView.setText("Devices Found: " + btDevicesList.size());
                }

            }
        }
    };

    private List<String> devicesStringList;
    private List<BluetoothDevice> btDevicesList;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        bluetoothStatusTextView = (TextView) findViewById(R.id.blueToothStatusTextView);
        discoveryButton = (Button) findViewById(R.id.discoveryButton);
        sendButton = (Button) findViewById(R.id.sendButton);
        msgEditText = (EditText) findViewById(R.id.msgEditText);
        bluetoothDevicesListView = (ListView) findViewById(R.id.bluetoothListView);
        setInitialView();
        requestPermission();
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
    }

    void setInitialView()
    {
        discoveryButton.setEnabled(false);
        sendButton.setEnabled(false);
        msgEditText.setEnabled(false);
        bluetoothDevicesListView.requestFocus();
    }

    public void clickButton(View view)
    {
        if(view.getId() == R.id.bluetoothStatusButton)
        {
            if(havePermissions)
            {
                getBluetoothStatus();
            }
            else
            {
                requestPermission();
            }
        }
        else if(view.getId() == R.id.discoveryButton)
        {
            if(havePermissions)
            {
                searchAllDevices();
            }
            else
            {
                requestPermission();
            }
        }
        else if(view.getId() == R.id.sendButton)
        {
            if(havePermissions)
            {
                sendMessage(msgEditText.getText().toString());
            }
            else
            {
                requestPermission();
            }
        }
    }

    void getBluetoothStatus()
    {
        if(mBluetoothAdapter.isEnabled())
        {
            bluetoothStatusTextView.setText("BLUETOOTH IS ENABLED.");
            bluetoothEnabled = true;
            discoveryButton.setEnabled(true);
        }
        else
        {
            bluetoothStatusTextView.setText("BLUETOOTH IS NOT ENABLED.");
        }
    }

    void searchAllDevices()
    {
        if(mBluetoothAdapter.isEnabled())
        {
            bluetoothStatusTextView.setText("Searching ... ");
            btDevicesList = new ArrayList<BluetoothDevice>();
            devicesStringList = new ArrayList<String>();
            registerListener();
            AsyncTask<Void,Void,Void> acceptThreadTask  = new AsyncTask<Void, Void, Void>() {
                @Override
                protected Void doInBackground(Void... voids)
                {
                    acceptThread = new AcceptThread();
                    acceptThread.run();
                    return null;
                }

                @Override
                protected void onPostExecute(Void result)
                {
                    mBluetoothAdapter.cancelDiscovery();
                    discoveryButton.setEnabled(false);
                    bluetoothDevicesListView.setEnabled(false);
                    msgEditText.setEnabled(true);
                    sendButton.setEnabled(true);
                    startListeningMessages();
                    bluetoothStatusTextView.setText("A device connected to this device.");
                }
            };
            acceptThreadTask.execute();
        }
        else
        {
            Toast.makeText(getApplicationContext(),"Bluetooth is not enabled.",Toast.LENGTH_SHORT).show();
        }
    }

    void startListeningMessages()
    {
        msgEditText.setFocusable(true);
        msgEditText.setFocusableInTouchMode(true);
        mHandler = new Handler()
        {
            @Override
            public void handleMessage(Message msg)
            {
                if(msg.what == MessageConstants.MESSAGE_READ)
                {
                    String toPrint = new String((byte[]) msg.obj);
                    showMessage(toPrint);
                }
            }
        };

        AsyncTask<Void,Void,Void> listenTask = new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... voids)
            {
                connectedThread = new ConnectedThread(mSocket);
                connectedThread.run();
                return null;
            }
        };
        listenTask.execute();
    }

    void registerListener()
    {
        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED);
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        registerReceiver(mReceiver, filter);
        mBluetoothAdapter.startDiscovery();
    }

    void setListAdapter()
    {
        ArrayAdapter arrayAdapter=new ArrayAdapter(this,android.R.layout.simple_list_item_1, android.R.id.text1, devicesStringList);
        bluetoothDevicesListView.setAdapter(arrayAdapter);
        bluetoothDevicesListView.setOnItemClickListener(new AdapterView.OnItemClickListener()
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
        acceptThread.cancel();
        final BluetoothDevice deviceToConnect = btDevicesList.get(pos);
        AsyncTask<BluetoothDevice,Void,Void> connectThreadTask = new AsyncTask<BluetoothDevice, Void, Void>() {
            @Override
            protected Void doInBackground(BluetoothDevice... bluetoothDevices)
            {
                ConnectThread connectThread = new ConnectThread(bluetoothDevices[0]);
                connectThread.run();
                return null;
            }

            @Override
            protected void onPostExecute(Void result)
            {
                mBluetoothAdapter.cancelDiscovery();
                bluetoothStatusTextView.setText("Device Connected to " + deviceToConnect.getName() + ".");
                discoveryButton.setEnabled(false);
                bluetoothDevicesListView.setEnabled(false);
                sendButton.setEnabled(true);
                msgEditText.setEnabled(true);
                startListeningMessages();
            }
        };
        connectThreadTask.execute(deviceToConnect);


    }

    void showMessage(String message)
    {
        bluetoothStatusTextView.setText(message);
    }

    void sendMessage(String msg)
    {
        if(msg.length() > 0)
        {
            try
            {
                byte[] byteString = (msg + " ").getBytes();
                connectedThread.write(byteString);
            }
            catch (Exception e)
            {
                Log.d("BLUETOOTH_COMMS", e.getMessage());
            }
        }
    }

    void requestPermission()
    {
        if(getApplicationContext().checkCallingOrSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_DENIED)
        {
            ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.BLUETOOTH,
                    Manifest.permission.BLUETOOTH_ADMIN, Manifest.permission.ACCESS_COARSE_LOCATION,
                    Manifest.permission.ACCESS_FINE_LOCATION}, 1);
        }
        else
        {
            havePermissions = true;
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

    @Override
    public void onDestroy()
    {
        super.onDestroy();
        unregisterReceiver(mReceiver);
    }

    private class AcceptThread extends Thread
    {
        private final BluetoothServerSocket mmServerSocket;

        public AcceptThread()
        {
            // Use a temporary object that is later assigned to mmServerSocket
            // because mmServerSocket is final.
            BluetoothServerSocket tmp = null;
            try
            {
                // MY_UUID is the app's UUID string, also used by the client code.
                tmp = mBluetoothAdapter.listenUsingRfcommWithServiceRecord(NAME, MY_UUID);
            }
            catch (IOException e)
            {
                Log.e(TAG, "Socket's listen() method failed", e);
            }

            mmServerSocket = tmp;
        }

        public void run()
        {
            BluetoothSocket socket = null;
            // Keep listening until exception occurs or a socket is returned.
            while (true)
            {
                try
                {
                    socket = mmServerSocket.accept();
                }
                catch (IOException e)
                {
                    Log.e(TAG, "Socket's accept() method failed", e);
                    break;
                }

                if (socket != null)
                {
                    // A connection was accepted. Perform work associated with
                    // the connection in a separate thread.
                    mSocket = socket;

                    try
                    {
                        mmServerSocket.close();
                    }
                    catch (IOException e)
                    {
                        e.printStackTrace();
                    }
                    break;
                }
            }
        }

        // Closes the connect socket and causes the thread to finish.
        public void cancel()
        {
            try
            {
                mmServerSocket.close();
            }
            catch (IOException e)
            {
                Log.e(TAG, "Could not close the connect socket", e);
            }
        }
    }

    private class ConnectThread extends Thread
    {
        private final BluetoothSocket mmSocket;
        private final BluetoothDevice mmDevice;

        public ConnectThread(BluetoothDevice device)
        {
            // Use a temporary object that is later assigned to mmSocket
            // because mmSocket is final.
            BluetoothSocket tmp = null;
            mmDevice = device;

            try
            {
                // Get a BluetoothSocket to connect with the given BluetoothDevice.
                // MY_UUID is the app's UUID string, also used in the server code.
                tmp = device.createRfcommSocketToServiceRecord(MY_UUID);
            }
            catch (IOException e)
            {
                Log.e(TAG, "Socket's create() method failed", e);
            }
            mmSocket = tmp;
        }

        public void run()
        {
            // Cancel discovery because it otherwise slows down the connection.
            mBluetoothAdapter.cancelDiscovery();

            try
            {
                // Connect to the remote device through the socket. This call blocks
                // until it succeeds or throws an exception.
                mmSocket.connect();
            }
            catch (IOException connectException)
            {
                // Unable to connect; close the socket and return.
                try
                {
                    mmSocket.close();
                }
                catch (IOException closeException)
                {
                    Log.e(TAG, "Could not close the client socket", closeException);
                }
                return;
            }

            // The connection attempt succeeded. Perform work associated with
            // the connection in a separate thread.
            mSocket = mmSocket;
            // manageMyConnectedSocket(mmSocket);
            // bluetoothStatusTextView.setText("Device Connected to " + mmDevice.getName() + ".");
        }

        // Closes the client socket and causes the thread to finish.
        public void cancel()
        {
            try
            {
                mmSocket.close();
            }
            catch (IOException e)
            {
                Log.e(TAG, "Could not close the client socket", e);
            }
        }
    }

    private class ConnectedThread extends Thread
    {
        private final BluetoothSocket mmSocket;
        private final InputStream mmInStream;
        private final OutputStream mmOutStream;
        private byte[] mmBuffer; // mmBuffer store for the stream

        public ConnectedThread(BluetoothSocket socket)
        {
            mmSocket = socket;
            InputStream tmpIn = null;
            OutputStream tmpOut = null;

            // Get the input and output streams; using temp objects because
            // member streams are final.
            try
            {
                tmpIn = socket.getInputStream();
            }
            catch (IOException e)
            {
                Log.e(TAG, "Error occurred when creating input stream", e);
            }
            try
            {
                tmpOut = socket.getOutputStream();
            }
            catch (IOException e)
            {
                Log.e(TAG, "Error occurred when creating output stream", e);
            }

            mmInStream = tmpIn;
            mmOutStream = tmpOut;
        }

        public void run()
        {
            mmBuffer = new byte[1024];
            int numBytes; // bytes returned from read()

            // Keep listening to the InputStream until an exception occurs.
            while (true)
            {
                try
                {
                    // Read from the InputStream.
                    numBytes = mmInStream.read(mmBuffer);
                    // Send the obtained bytes to the UI activity.
                    Message readMsg = mHandler.obtainMessage(MessageConstants.MESSAGE_READ, numBytes, -1,mmBuffer);
                    readMsg.sendToTarget();
                }
                catch (IOException e)
                {
                    Log.d(TAG, "Input stream was disconnected", e);
                    break;
                }
            }
        }

        // Call this from the main activity to send data to the remote device.
        public void write(byte[] bytes)
        {
            try
            {
                mmOutStream.write(bytes);

                // Share the sent message with the UI activity.
                Message writtenMsg = mHandler.obtainMessage(MessageConstants.MESSAGE_WRITE, -1, -1, mmBuffer);
                writtenMsg.sendToTarget();
            }
            catch (IOException e)
            {
                Log.e(TAG, "Error occurred when sending data", e);

                // Send a failure message back to the activity.
                Message writeErrorMsg =
                        mHandler.obtainMessage(MessageConstants.MESSAGE_TOAST);
                Bundle bundle = new Bundle();
                bundle.putString("toast",
                        "Couldn't send data to the other device");
                writeErrorMsg.setData(bundle);
                mHandler.sendMessage(writeErrorMsg);
            }
        }

        // Call this method from the main activity to shut down the connection.
        public void cancel()
        {
            try
            {
                mmSocket.close();
            }
            catch (IOException e)
            {
                Log.e(TAG, "Could not close the connect socket", e);
            }
        }
    }

    private interface MessageConstants
    {
        public static final int MESSAGE_READ = 0;
        public static final int MESSAGE_WRITE = 1;
        public static final int MESSAGE_TOAST = 2;

    }

}
