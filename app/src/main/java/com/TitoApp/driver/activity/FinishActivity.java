package com.TitoApp.driver.activity;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RatingBar;
import android.widget.TextView;

import com.TitoApp.driver.R;
import com.TitoApp.driver.model.OrderModel;
import com.TitoApp.driver.model.UserRate;
import com.TitoApp.driver.model.WalletModel;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MapStyleOptions;
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

public class FinishActivity extends AppCompatActivity implements
        OnMapReadyCallback {

    //init the views
    @BindView(R.id.paidPriceBtn)
    Button paidPriceBtn;
    @BindView(R.id.priceTV)
    TextView priceTV;
    // here to add the price parent
    @BindView(R.id.enterPriceParent)
    CardView enterPriceParent;
    @BindView(R.id.priceParent)
    CardView priceParent;
    @BindView(R.id.priceET)
    EditText priceET;
    @BindView(R.id.confirmPriceBtn)
    Button confirmPriceBtn;
    // to confirm the price
    @BindView(R.id.paidPriceTV)
    TextView paidPriceTV;
    @BindView(R.id.finishPriceBtn)
    Button finishPriceBtn;
    @BindView(R.id.editPriceBtn)
    Button editPriceBtn;
    @BindView(R.id.confirmParent)
    CardView confirmParent;
    // the rate
    @BindView(R.id.rateParent)
    CardView rateParent;
    @BindView(R.id.rateRB)
    RatingBar rateRB;

    private GoogleMap mMap;
    private static final String TAG = "google";
    private int firstZoom;
    ProgressDialog progressDialog;
    private String orderStatus;
    OrderModel order;

    //init the firebase
    private FirebaseAuth mFirebaseAuth;
    private FirebaseUser mFirebaseUser;
    private DatabaseReference dfUpdateOrder;

    //here to get the data from the intent
    private String orderId;
    private double tripPrice;

    UserRate userRate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_finish);
        ButterKnife.bind(this);
        dfUpdateOrder = FirebaseDatabase.getInstance().getReference();

        priceParent.setVisibility(View.VISIBLE);
        enterPriceParent.setVisibility(View.GONE);


        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("جاري تحديد الموقع...");
//        progressDialog.show();

        firstZoom = 0;


        mFirebaseAuth = FirebaseAuth.getInstance();
        mFirebaseUser = mFirebaseAuth.getCurrentUser();


        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        // here the order id
        orderId = getIntent().getStringExtra("order");
        tripPrice = getIntent().getDoubleExtra("price", 0);


        userRate = new UserRate();

        if (orderId == null)
            return;

        priceTV.setText(tripPrice + " جنيه ");

        getOrderData();

        orderStatus = "";

    }


    private void getUserRate() {
        dfUpdateOrder.child("userRate").child(order.getUserId()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                if (dataSnapshot.exists()) {
                    userRate = dataSnapshot.getValue(UserRate.class);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });


    }

    /**
     * here to get the data from the firebase for the order and listen if any com.TitoApp.com.TitoApp.driver
     * is accept it or if the com.TitoApp.com.TitoApp.driver is refused
     */
    private void getOrderData() {

// here to check the status of the order and change it
        Log.d("google", "order id بيسبسي    " + orderId);

        DatabaseReference df = FirebaseDatabase.getInstance().getReference().child("Orders").child(orderId);
        ValueEventListener postListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                order = dataSnapshot.getValue(OrderModel.class);
                getUserRate();


                Log.d("google", "order id   قصثثثصهفهفهفهفهفهفهف  " + orderId);
                if (order == null)
                    return;

                Log.d("google", "here in the order " + order.getStatus());


//                if (order.getStatus()==null){
//                    return;
//                }

                if (order.getStatus().equals(orderStatus)) {
                    return;
                }


                if (orderStatus.equals("1")) {
                    if (order.getStatus().equals("0"))
                        return;
                }

                if (orderStatus.equals("2")) {
                    if (order.getStatus().equals("0"))
                        return;
                    if (order.getStatus().equals("1"))
                        return;
                }

                // start from here order
                orderStatus = order.getStatus();


                if (orderStatus.equals("-1")) {

                } else if (orderStatus.equals("0")) {

                } else if (orderStatus.equals("1")) {


                    if (order.getDriverId().equals(mFirebaseUser.getUid())) {


                    } else {

                    }
                } else if (orderStatus.equals("2")) {
                    if (order.getDriverId().equals(mFirebaseUser.getUid())) {

                    }
                }


            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        };
        df.addValueEventListener(postListener);
    }

    private static final String GEO_FIRE_DB = "https://tito-c762f.firebaseio.com/";

    @SuppressLint("MissingPermission")
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        try {
            // Customise the styling of the base map using a JSON object defined
            // in a raw resource file.
            boolean success = googleMap.setMapStyle(
                    MapStyleOptions.loadRawResourceStyle(
                            this, R.raw.style_json));
            if (!success) {
                Log.e(TAG, "Style parsing failed.");
            }
        } catch (Resources.NotFoundException e) {
            Log.e(TAG, "Can't find style. Error: ", e);
        }
        enableMyLocation();
        mMap.setOnMyLocationChangeListener(new GoogleMap.OnMyLocationChangeListener() {

            @Override
            public void onMyLocationChange(Location arg0) {
                if (firstZoom == 0) {
                    float zoomLevel = (float) 15.0;
                    progressDialog.dismiss();
                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(arg0.getLatitude(), arg0.getLongitude()), zoomLevel));
                    firstZoom = 1;
                }
            }
        });


    }

    /**
     * Enables the My Location layer if the fine location permission has been granted.
     */
    private void enableMyLocation() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            // Permission to access the location is missing.
        } else if (mMap != null) {
            // Access to the location has been granted to the app.
            mMap.setMyLocationEnabled(true);
        }
    }

    // action the paid button
    @OnClick(R.id.paidPriceBtn)
    void paidAction() {
        priceParent.setVisibility(View.GONE);
        enterPriceParent.setVisibility(View.VISIBLE);
    }

    @OnClick(R.id.confirmPriceBtn)
    void cofirmPriceAction() {
        if (priceET.getText().toString().isEmpty())
            return;
        confirmParent.setVisibility(View.VISIBLE);
        paidPriceTV.setText(priceET.getText() + " جنيه ");
    }

    @OnClick(R.id.finishPriceBtn)
    void finishActionBtn() {

        rateParent.setVisibility(View.VISIBLE);
        confirmParent.setVisibility(View.GONE);

    }

    @OnClick(R.id.editPriceBtn)
    void editPriceAction() {
        confirmParent.setVisibility(View.GONE);
    }


    //todo here to add the rate
    @OnClick(R.id.rateBtn)
    void RateAction() {

        int numrate = Integer.parseInt(userRate.getNumrate());
        double rate = Double.parseDouble(userRate.getRate());
        double totalrate = Double.parseDouble(userRate.getTotalrate());

        numrate++;
        totalrate = totalrate + rateRB.getRating();

        rate = totalrate / numrate;



        dfUpdateOrder.child("userRate")
                .child(order.getUserId()).child("id").setValue(order.getUserId());

        dfUpdateOrder.child("userRate")
                .child(order.getUserId()).child("numrate").setValue("" + numrate);

        dfUpdateOrder.child("userRate")
                .child(order.getUserId()).child("totalrate").setValue("" + totalrate);


        dfUpdateOrder.child("userRate")
                .child(order.getUserId()).child("rate").setValue("" + rate);


        dfUpdateOrder.child("Orders")
                .child(orderId).child("status").setValue("5");



        //todo here to add to the wallet
        WalletModel wallet = new WalletModel();

        wallet.setTripId(orderId);
        wallet.setTime(""+System.currentTimeMillis());
        wallet.setClientId(order.getUserId());
        wallet.setDriverId(order.getDriverId());
        wallet.setPaidPrice(priceET.getText().toString());
        wallet.setPrice(order.getTripPrice());
        dfUpdateOrder.child("Wallet").child(wallet.getTripId()).setValue(wallet);

        finish();

    }

}
