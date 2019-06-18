package com.TitoApp.driver.activity;

import android.os.Bundle;
import android.os.Process;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;


import com.TitoApp.driver.R;
import com.TitoApp.driver.model.WalletModel;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class WalletActivity extends AppCompatActivity {


    @BindView(R.id.companyMoneyTV)
    TextView companyMoneyTV;
    @BindView(R.id.totalPaidPriceTV)
    TextView totalPaidPriceTV;
    @BindView(R.id.loading)
    ProgressBar loading;

    //init the firebase
    private FirebaseAuth mFirebaseAuth;
    private FirebaseUser mFirebaseUser;
    private DatabaseReference walletRef;
    private DatabaseReference categoryRef;
    private DatabaseReference carRef;


    private String driverId = "";
    private double companyPercent = 0.0;
    private double companyMoney = 0.0;
    private double totalPaidPrice = 0.0;
    private double totalPrice = 0.0;
    private double difference = 0.0;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wallet);
        ButterKnife.bind(this);

        mFirebaseAuth = FirebaseAuth.getInstance();
        mFirebaseUser = mFirebaseAuth.getCurrentUser();

        driverId = mFirebaseUser.getUid();

        walletRef = FirebaseDatabase.getInstance().getReference().child("Wallet");
        categoryRef = FirebaseDatabase.getInstance().getReference().child("Category");
        carRef = FirebaseDatabase.getInstance().getReference().child("Cars");

        carRef.child(driverId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                loading.setVisibility(View.VISIBLE);
                categoryRef.child(dataSnapshot.child("categoryId").getValue().toString()).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                        companyPercent = Double.parseDouble(dataSnapshot.child("companyPercent").getValue().toString());
                        walletRef.addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                if (dataSnapshot.exists()) {
                                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                                        if (snapshot.exists()) {
                                            WalletModel model = snapshot.getValue(WalletModel.class);
                                            if (model.getDriverId().equals(driverId)) {
                                                totalPaidPrice += Double.parseDouble(model.getPaidPrice());
                                                totalPrice += Double.parseDouble(model.getPrice());
                                            }
                                        }
                                    }
                                    difference = totalPaidPrice - totalPrice;
                                    companyMoney = difference + ((companyPercent/100)*totalPrice);

//                                    Toast.makeText(WalletActivity.this, "totalPrice: " + totalPrice, Toast.LENGTH_SHORT).show();
//                                    Toast.makeText(WalletActivity.this, "difference: " + difference, Toast.LENGTH_SHORT).show();
//                                    Toast.makeText(WalletActivity.this, "ال لميته من الزباين يا معلم: " + totalPaidPrice, Toast.LENGTH_SHORT).show();
//                                    Toast.makeText(WalletActivity.this, "فلوس الشركة يسطا: " + companyMoney, Toast.LENGTH_SHORT).show();
                                    loading.setVisibility(View.GONE);
                                    companyMoneyTV.setText(companyMoney+ " EGP");
                                    totalPaidPriceTV.setText(totalPaidPrice + " EGP");
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {

                            }
                        });


                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });


    }


    /**
     * press the back button
     */
    @OnClick(R.id.back_image)
    void back__() {
        finish();
    }
}
