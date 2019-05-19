package com.NaqelApp.driver.activity;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.widget.ProgressBar;

import com.NaqelApp.driver.R;
import com.NaqelApp.driver.adapter.MyOrdersAdapter;
import com.NaqelApp.driver.model.OrderModel;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class MyOrdersActivity extends AppCompatActivity {



    //init views

    @BindView(R.id.recycler)
    RecyclerView recycler;
    @BindView(R.id.loading)
    ProgressBar loading;

    // init order adapter
    MyOrdersAdapter adapter;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_trips);
        ButterKnife.bind(this);


        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        adapter = new MyOrdersAdapter(this,new ArrayList<OrderModel>());
        recycler.setLayoutManager(layoutManager);
        recycler.setAdapter(adapter);

    }

    @OnClick(R.id.back_image)
    void back__() {
        finish();
    }

}
