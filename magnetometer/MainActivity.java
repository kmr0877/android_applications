package com.example.mohan.magnetometer;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.hardware.GeomagneticField;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity implements SensorEventListener
{

    private TextView readingsTextView, headingTextView, northHeadingTextView;
    private SensorManager mSensorManager;
    private Sensor magnetSensor;
    private LocationManager mLocationManager;
    private Location loc;
    private LocationListener mLocationListener = new LocationListener()
    {
        @Override
        public void onLocationChanged(final Location mLoc)
        {
            loc = mLoc;
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

    private boolean havePermissions = false, calcHeading = false, showStrength = false;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        magnetSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
        readingsTextView = (TextView) findViewById(R.id.readDataTextView);
        headingTextView = (TextView) findViewById(R.id.headingTextView);
        northHeadingTextView = (TextView) findViewById(R.id.northHeadingTextView);
        requestPermissions();

    }

    public void buttonClick(View view)
    {
        if(view.getId() == R.id.readButton)
        {
            if(magnetSensor == null)
            {
                Toast.makeText(getApplicationContext(),"Magnetometer not available.",Toast.LENGTH_SHORT).show();
            }
            else
            {
                mSensorManager.registerListener(this,magnetSensor,SensorManager.SENSOR_DELAY_NORMAL);
                showStrength = true;
            }
        }
        else if(view.getId() == R.id.headingButton)
        {
            if(magnetSensor == null)
            {
                Toast.makeText(getApplicationContext(),"Magnetometer not available.",Toast.LENGTH_SHORT).show();
            }
            else
            {
                if(havePermissions)
                {
                    calcHeading = true;
                }
                else
                {
                    requestPermissions();
                }
            }
        }
    }

    void requestPermissions()
    {
        if(getApplicationContext().checkCallingOrSelfPermission("android.permission.ACCESS_COARSE_LOCATION") == PackageManager.PERMISSION_DENIED)
        {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION}, 1);
        }
        else
        {
            havePermissions = true;
            mLocationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
            loc = mLocationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            if(!mLocationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) &&
                    !mLocationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER))
            {
                loc = mLocationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            }
            else if(mLocationManager.isProviderEnabled(LocationManager.GPS_PROVIDER))
            {
                mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 2000, 10, mLocationListener);
            }
            else
            {
                mLocationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 2000, 10, mLocationListener);
            }

        }
    }

    @Override
    public void onResume()
    {
        super.onResume();
        mSensorManager.registerListener(this,magnetSensor,SensorManager.SENSOR_DELAY_NORMAL);
    }

    @Override
    public void onPause()
    {
        super.onPause();
        mSensorManager.unregisterListener(this);
    }

    @Override
    public void onSensorChanged(SensorEvent event)
    {
        float x = event.values[0];
        float y = event.values[1];
        float z = event.values[2];

        if(showStrength == true)
        {
            readingsTextView.setText("X: " + x + " Y: " + y + " Z: " + z);
        }

        if(calcHeading == true)
        {

            float heading = 0;
            if(y>0)
            {
                heading = 90 - (float) Math.toDegrees(Math.atan((x/y)));
            }
            else if(y<0)
            {
                heading = 270 - (float) Math.toDegrees(Math.atan((x/y)));
            }
            else if(y == 0)
            {
                if(x<0)
                {
                    heading = 180;
                }
                else
                {
                    heading = 0;
                }

            }

            headingTextView.setText("Heading: " + heading);

            GeomagneticField geoField;
            if(loc != null)
            {
                geoField = new GeomagneticField( Double.valueOf(loc.getLatitude()).floatValue(),
                        Double.valueOf(loc.getLongitude()).floatValue(),
                        Double.valueOf(loc.getAltitude()).floatValue(),  System.currentTimeMillis());

                float declineAngle = (float) Math.toDegrees(geoField.getDeclination());
                float correctedValue = heading-declineAngle;
                if(correctedValue<0)
                {
                    correctedValue += 360;
                }
                northHeadingTextView.setText("North Heading: " + correctedValue);
            }

        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults)
    {
        switch (requestCode) {
            case 1 : {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)
                {
                    havePermissions = true;
                }
                else
                {
                }
                return;
            }
        }
    }

}
