package mohan.com.accelerometerapp;

import android.content.res.Configuration;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

public class OrientationActivity extends AppCompatActivity implements SensorEventListener
{
    private SensorManager sensorManager;
    private float[] lastMagFields = new float[3];;
    private float[] lastAccels = new float[3];;
    private float[] rotationMatrix = new float[16];
    private float[] orientation = new float[4];

    private TextView orientationTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_orientation);
        sensorManager = (SensorManager)getSystemService(SENSOR_SERVICE);
        orientationTextView = (TextView) findViewById(R.id.orientationTextView);
    }

    @Override
    protected void onResume()
    {
        super.onResume();
        sensorManager.registerListener(this, sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD), SensorManager.SENSOR_DELAY_GAME);
        sensorManager.registerListener(this, sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER), SensorManager.SENSOR_DELAY_NORMAL);
    }

    @Override
    protected void onPause()
    {
        super.onPause();
        sensorManager.unregisterListener(this);
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy)
    {

    }

    public void onSensorChanged(SensorEvent event)
    {
        switch (event.sensor.getType())
        {
            case Sensor.TYPE_ACCELEROMETER:
                System.arraycopy(event.values, 0, lastAccels, 0, 3);
                break;
            case Sensor.TYPE_MAGNETIC_FIELD:
                System.arraycopy(event.values, 0, lastMagFields, 0, 3);
                break;
            default:
                return;
        }

        if (SensorManager.getRotationMatrix(rotationMatrix, null, lastAccels, lastMagFields))
        {
            SensorManager.getOrientation(rotationMatrix, orientation);

            float xAxis = (float) Math.toDegrees(orientation[1]);
            float yAxis = (float) Math.toDegrees(orientation[2]);

            if ((yAxis <= 0) && (xAxis >= 75))
            {
                orientationTextView.setText("Upside Down");
            }
            else if ((yAxis <= 25) && (yAxis >= -25) && (xAxis >= -160))
            {
                orientationTextView.setText("Default" );
            }
            else if ((yAxis < -25) && (xAxis >= -20))
            {
                orientationTextView.setText("Left" );
            }
            else if ((yAxis > 25) && (xAxis >= -20))
            {
                orientationTextView.setText("Right");
            }

        }
    }

}
