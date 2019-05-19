package com.NaqelApp.driver.activity;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.NaqelApp.driver.R;


public class Splash extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        Thread thread = new Thread(){
            @Override
            public void run() {
                try {

                    sleep(2000);
                    startActivity(new Intent(Splash.this,MapsActivity.class));
                    finish();

                    //Toast.makeText(getContext(),"waked",Toast.LENGTH_SHORT).show();

                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        };


        thread.start();
    }
}
