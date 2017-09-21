package com.example.mohan.gyroscope;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity implements SensorEventListener
{
    private TextView readingsTextView, rotationTextView;
    private SensorManager mSensorManager;
    private Sensor gyroSensor, acceSensor, magnetSensor;

    private static final float NS2S = 1.0f / 1000000000.0f;
    private float timestamp;
    final float currentRotVector[] =  { 1, 0, 0, 0 };
    private boolean initState = true;

    private boolean isShowingValues = false;

    public static final float EPSILON = 0.000000001f;
    public static final float FILTER_COEFFICIENT = 0.98f;

    private final float[] deltaRotationVector = new float[4];
    float RotAngle = 0f;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        gyroSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
        magnetSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
        acceSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        readingsTextView = (TextView) findViewById(R.id.gyroReadingTextView);
        rotationTextView = (TextView) findViewById(R.id.rotationTextView);
    }

    public void buttonClick(View view)
    {
        if(view.getId() == R.id.startButton)
        {
            if(gyroSensor == null)
            {
                Toast.makeText(getApplicationContext(),"Gyroscope not available.",Toast.LENGTH_SHORT).show();
                if(acceSensor == null)
                {
                    Toast.makeText(getApplicationContext(),"Accelerometer not available.",Toast.LENGTH_SHORT).show();
                    if(magnetSensor == null)
                    {
                        Toast.makeText(getApplicationContext(),"Magnetic sensor not available.",Toast.LENGTH_SHORT).show();
                    }
                }
                return;

            }
            else
            {
                mSensorManager.registerListener(this,gyroSensor,SensorManager.SENSOR_DELAY_NORMAL);
                //mSensorManager.registerListener(this,acceSensor,SensorManager.SENSOR_DELAY_NORMAL);
                //mSensorManager.registerListener(this,magnetSensor,SensorManager.SENSOR_DELAY_NORMAL);
                isShowingValues = true;
            }
        }
        else if(view.getId() == R.id.stooButton)
        {
            mSensorManager.unregisterListener(this);
        }
        else if(view.getId() == R.id.offsetButton)
        {
            RotAngle = 0;
            rotationTextView.setText("0.00000");
        }
    }


    @Override
    public void onSensorChanged(SensorEvent event)
    {
        float[] values = event.values;
        values[0] = (float) Math.round(Math.toDegrees(values[0]));
        values[1] = (float) Math.round(Math.toDegrees(values[1]));
        values[2] = (float) Math.round(Math.toDegrees(values[2]));
        float X = event.values[0], Y = event.values[1], Z = event.values[2];
        readingsTextView.setText("Rate of Rotation(in degree/s) \n " + " X: " + values[0] + " Y: " + values[1] + " Z: " + values[2]);

        if (timestamp != 0)
        {
            final float dT = (event.timestamp - timestamp) * NS2S;
            // Axis of the rotation sample, not normalized yet.

            // Calculate the angular speed of the sample
            float omegaMagnitude = (float) Math.sqrt(X * X + Y * Y + Z * Z);

            // Normalize the rotation vector if it's big enough to get the axis
            if (omegaMagnitude > EPSILON) {
                X /= omegaMagnitude;
                Y /= omegaMagnitude;
                Z /= omegaMagnitude;
            }

            // Integrate around this axis with the angular speed by the timestep
            // in order to get a delta rotation from this sample over the timestep
            // We will convert this axis-angle representation of the delta rotation
            // into a quaternion before turning it into the rotation matrix.
            float thetaOverTwo = dT * omegaMagnitude / 2.0f;
            float sinThetaOverTwo = (float) Math.sin(thetaOverTwo);
            float cosThetaOverTwo = (float) Math.cos(thetaOverTwo);
            deltaRotationVector[0] = cosThetaOverTwo;
            deltaRotationVector[1] = sinThetaOverTwo * X;
            deltaRotationVector[2] = sinThetaOverTwo * Y;
            deltaRotationVector[3] = sinThetaOverTwo * Z;

    /* quaternion multiplication
        Reference: http://www.cprogramming.com/tutorial/3d/quaternions.html
    */

            currentRotVector[0] = deltaRotationVector[0] * currentRotVector[0] -
                    deltaRotationVector[1] * currentRotVector[1] -
                    deltaRotationVector[2] * currentRotVector[2] -
                    deltaRotationVector[3] * currentRotVector[3];

            currentRotVector[1] = deltaRotationVector[0] * currentRotVector[1] +
                    deltaRotationVector[1] * currentRotVector[0] +
                    deltaRotationVector[2] * currentRotVector[3] -
                    deltaRotationVector[3] * currentRotVector[2];

            currentRotVector[2] = deltaRotationVector[0] * currentRotVector[2] -
                    deltaRotationVector[1] * currentRotVector[3] +
                    deltaRotationVector[2] * currentRotVector[0] +
                    deltaRotationVector[3] * currentRotVector[1];

            currentRotVector[3] = deltaRotationVector[0] * currentRotVector[3] +
                    deltaRotationVector[1] * currentRotVector[2] -
                    deltaRotationVector[2] * currentRotVector[1] +
                    deltaRotationVector[3] * currentRotVector[0];
            final float rad2deg = (float) (180.0f / Math.PI);
            if(values[2]!=0)
            {
                RotAngle += (currentRotVector[0] * rad2deg);
            }
            float axisX,axisY,axisZ;
            axisX = currentRotVector[1];
            axisY = currentRotVector[2];
            axisZ = currentRotVector[3];

            rotationTextView.setText("Rotation : " + RotAngle);


        }
        timestamp = event.timestamp;

    }


    private float[] matrixMultiplication(float[] A, float[] B)
    {
        float[] result = new float[9];

        result[0] = A[0] * B[0] + A[1] * B[3] + A[2] * B[6];
        result[1] = A[0] * B[1] + A[1] * B[4] + A[2] * B[7];
        result[2] = A[0] * B[2] + A[1] * B[5] + A[2] * B[8];

        result[3] = A[3] * B[0] + A[4] * B[3] + A[5] * B[6];
        result[4] = A[3] * B[1] + A[4] * B[4] + A[5] * B[7];
        result[5] = A[3] * B[2] + A[4] * B[5] + A[5] * B[8];

        result[6] = A[6] * B[0] + A[7] * B[3] + A[8] * B[6];
        result[7] = A[6] * B[1] + A[7] * B[4] + A[8] * B[7];
        result[8] = A[6] * B[2] + A[7] * B[5] + A[8] * B[8];

        return result;
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }

    @Override
    public void onResume()
    {
        super.onResume();
        if(isShowingValues == true)
        {
            mSensorManager.registerListener(this,gyroSensor,SensorManager.SENSOR_DELAY_NORMAL);
        }
    }

    @Override
    public void onPause()
    {
        super.onPause();
        mSensorManager.unregisterListener(this);
    }

}
