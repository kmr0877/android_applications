package defapp.com.networkapp;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    public void buttonClick(View view)
    {
        if(view.getId() == R.id.linkLayerInfoButton)
        {
            startActivity(new Intent(MainActivity.this, LinkLayerInfoActivity.class));
        }
        else if(view.getId() == R.id.mobilityInfoButton)
        {
            startActivity(new Intent(MainActivity.this, MobilityInfoActivity.class));
        }
    }
}
