package mohan.com.accelerometerapp;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

public class MainActivity extends AppCompatActivity
{

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setTitle("Home");
    }

    public void buttonClick(View view)
    {
        if(view.getId() == R.id.allSensorsButton)
        {
            startActivity(new Intent(MainActivity.this,AllSensorsActivity.class));
        }
        else if(view.getId() == R.id.accelerometerReadingButton)
        {
            startActivity(new Intent(MainActivity.this,AccelerometerReading.class));
        }
        else if(view.getId() == R.id.orientationButton)
        {
            startActivity(new Intent(MainActivity.this,OrientationActivity.class));
        }

    }
}
