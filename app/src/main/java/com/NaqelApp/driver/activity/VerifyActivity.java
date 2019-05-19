package com.NaqelApp.driver.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import com.NaqelApp.driver.R;


public class VerifyActivity extends AppCompatActivity {




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_verify);






    }

    public void Home(View view) {
        startActivity(new Intent(VerifyActivity.this,MapsActivity.class));
    }
}
