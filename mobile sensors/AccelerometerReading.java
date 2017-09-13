package mohan.com.accelerometerapp;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

public class AccelerometerReading extends AppCompatActivity implements SensorEventListener
{

    private TextView reading1TextView, reading2TextView;
    private SensorManager mSensorManager;
    private Sensor mAcceleometer;
    private double[] gravity = {0.0,0.0,0.0};
    private double[] linear_acceleration = {0.0,0.0,0.0};

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_accelerometer_reading);

        setTitle("Accelerometer Reading");
        reading1TextView = (TextView) findViewById(R.id.reading1TextView);
        reading2TextView = (TextView) findViewById(R.id.reading2TextView);

        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        mAcceleometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);

    }

    @Override
    public final void onSensorChanged(SensorEvent event)
    {
        reading1TextView.setText("Acceleration force including gravity \n X: " + event.values[0] + "\n Y: " + event.values[1] + "\n Z: " + event.values[2]);
        final float alpha = 0.8f;

        gravity[0] = alpha * gravity[0] + (1 - alpha) * event.values[0];
        gravity[1] = alpha * gravity[1] + (1 - alpha) * event.values[1];
        gravity[2] = alpha * gravity[2] + (1 - alpha) * event.values[2];

        linear_acceleration[0] = event.values[0] - gravity[0];
        linear_acceleration[1] = event.values[1] - gravity[1];
        linear_acceleration[2] = event.values[2] - gravity[2];
        reading2TextView.setText("Acceleration force without gravity \n X: " + linear_acceleration[0] + "\n Y: " + linear_acceleration[1] + "\n Z: " + linear_acceleration[2]);
    }

    @Override
    public final void onAccuracyChanged(Sensor sensor, int accuracy)
    {

    }

    @Override
    protected void onResume()
    {
        super.onResume();
        mSensorManager.registerListener(this, mAcceleometer, SensorManager.SENSOR_DELAY_NORMAL);
    }

    @Override
    protected void onPause() {
        super.onPause();
        mSensorManager.unregisterListener(this);
    }

}
