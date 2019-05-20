package com.NaqelApp.driver.activity;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Toast;

import com.NaqelApp.driver.R;
import com.NaqelApp.driver.util.SharedPrefDueDate;


public class Splash extends AppCompatActivity {

    SharedPrefDueDate pref;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);


        pref =  new SharedPrefDueDate(this);


        if (!pref.getOrderId().isEmpty()){
            Intent intent = new Intent(Splash.this, AcceptOrderActivity.class);
            intent.putExtra("order", pref.getOrderId());
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            return;
        }

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
