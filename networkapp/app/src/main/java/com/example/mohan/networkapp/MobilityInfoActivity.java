package defapp.com.networkapp;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

public class MobilityInfoActivity extends AppCompatActivity
{

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mobility_info);
        setTitle("Mobility Info");
    }

    public void buttonClick(View view)
    {
        if(view.getId() == R.id.l4Button)
        {
            Intent intent = new Intent(MobilityInfoActivity.this, L4ConnectivityActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
            startActivity(intent);

        }
        else if(view.getId() == R.id.l3Button)
        {
            Intent intent = new Intent(MobilityInfoActivity.this, L3HandoffActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
            startActivity(intent);

        }
        else if(view.getId() == R.id.l2Button)
        {
            Intent intent = new Intent(MobilityInfoActivity.this, L2HandoffActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
            startActivity(intent);

        }
    }

    @Override
    public void onBackPressed()
    {
        Intent intent = new Intent(MobilityInfoActivity.this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
    }
}
