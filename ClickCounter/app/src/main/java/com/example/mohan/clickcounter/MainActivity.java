package com.example.mohan.clickcounter;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.app.Activity;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;


public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button clickMeBtn = (Button) findViewById(R.id.button3);
        clickMeBtn.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                public void onClick(View v){

                    myClick(v);
                }
            }
        });
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu){
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }
    public void myClick(View v){
        TextViewtxCounter = (TextView)findViewById(R.id.textView2);
        txCounter.setText("yourtext");


    }
}
