package mohan.com.accelerometerapp;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.util.List;

public class AllSensorsActivity extends AppCompatActivity
{

    ListView allSensorsListView;
    SensorManager sensorManager;
    List<Sensor> sensorList;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_all_sensors);
        allSensorsListView = (ListView) findViewById(R.id.allSensorsListView);
        setTitle("All Sensors List");
    }

    public void buttonClick(View view)
    {
        if(view.getId() == R.id.showAllSensorsButton)
        {
            showAllSensors();
        }
    }

    private void showAllSensors()
    {
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        sensorList = sensorManager.getSensorList(Sensor.TYPE_ALL);
        allSensorsListView.setAdapter(new ArrayAdapter<Sensor>(this, android.R.layout.simple_list_item_1,  sensorList));
    }
}
