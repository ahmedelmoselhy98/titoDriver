package com.NaqelApp.driver.activity;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.location.Geocoder;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.os.Looper;
import android.support.annotation.DrawableRes;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.NaqelApp.driver.R;
import com.NaqelApp.driver.model.CategoryModel;
import com.NaqelApp.driver.model.OrderModel;
import com.NaqelApp.driver.model.User;
import com.NaqelApp.driver.util.PermissionUtils;
import com.bumptech.glide.Glide;
import com.directions.route.Route;
import com.directions.route.RouteException;
import com.directions.route.Routing;
import com.directions.route.RoutingListener;
import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import de.hdodenhof.circleimageview.CircleImageView;

public class AcceptOrderActivity extends AppCompatActivity implements GoogleMap.OnMyLocationButtonClickListener,
        GoogleMap.OnMyLocationClickListener,
        OnMapReadyCallback, RoutingListener {


    //init the views
    @BindView(R.id.defaultImage)
    ImageView defaultImage;
    @BindView(R.id.userImage)
    ImageView userImage;
    @BindView(R.id.userNameTV)
    TextView userNameTV;
    @BindView(R.id.orderParent)
    LinearLayout orderParent;
    @BindView(R.id.canceledParent)
    LinearLayout canceledParent;
    @BindView(R.id.startParent)
    CardView startParent;

    //here to add the client data
    @BindView(R.id.clientIV)CircleImageView clientIV;
    @BindView(R.id.clientNameTV)TextView clientNameTV;
    @BindView(R.id.callClientBtn)FloatingActionButton callClientBtn;
    @BindView(R.id.defaultImage1)
    ImageView defaultImage1;
    @BindView(R.id.directionClientBtn)Button directionClientBtn;



    //todo here to add the during trip
    @BindView(R.id.destinationBTn)Button destinationBTn;
    @BindView(R.id.userGuideTV)TextView userGuideTV;
    @BindView(R.id.endParent)CardView endParent;



    @BindView(R.id.routeInfoTV)
    TextView routeInfoTV;

    private GoogleMap mMap;


    private static final String TAG = "google";

    private FirebaseAuth mFirebaseAuth;
    private FirebaseUser mFirebaseUser;


    private String orderId;


    private DatabaseReference df, df1, dfUpdateOrder;

    private String userImageString = "";

    private String orderStatus = "";


    private int firstZoom;

    private String triptime = "", tripDistance = "2";
    //the route
    private List<Polyline> polylines;
    private static final int[] COLORS = new int[]{R.color.colorPrimary, R.color.colorPrimary, R.color.colorPrimary, R.color.colorPrimary, R.color.colorPrimary};


    private LocationRequest mLocationRequest;

    private long UPDATE_INTERVAL = 10 * 1000;  /* 10 secs */
    private long FASTEST_INTERVAL = 2000; /* 2 sec */


    double clat, clang;
    double tolat, tolang;

    ProgressDialog progressDialog;

    OrderModel order;

    long tripeTimeMin;


    //the user model
    User user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_accept_order);
        ButterKnife.bind(this);

        startLocationUpdates();


        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("جاري تحديد الموقع...");
        progressDialog.show();

        firstZoom = 0;

        polylines = new ArrayList<>();

        mFirebaseAuth = FirebaseAuth.getInstance();
        mFirebaseUser = mFirebaseAuth.getCurrentUser();


        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        orderId = getIntent().getStringExtra("order");


        Log.d("google", "order id  فففف   " + orderId);


        if (orderId == null)
            return;

        Log.d("google", "order id ثثث    " + orderId);
        getOrderData();


        orderStatus = "";

    }


    /**
     * here to get the data from the firebase for the order and listen if any com.NaqelApp.com.NaqelApp.driver is accept it or if the com.NaqelApp.com.NaqelApp.driver is refused
     */
    private void getOrderData() {

// here to check the status of the order and change it
        Log.d("google", "order id بيسبسي    " + orderId);

        DatabaseReference df = FirebaseDatabase.getInstance().getReference().child("Orders").child(orderId);
        ValueEventListener postListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                order = dataSnapshot.getValue(OrderModel.class);


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
                    orderParent.setVisibility(View.GONE);
                    canceledParent.setVisibility(View.VISIBLE);
                } else if (orderStatus.equals("0")) {
                    orderParent.setVisibility(View.VISIBLE);
                    canceledParent.setVisibility(View.GONE);
                } else if (orderStatus.equals("1")) {


                    if (order.getDriverId().equals(mFirebaseUser.getUid())) {

                        orderParent.setVisibility(View.GONE);
                        startParent.setVisibility(View.VISIBLE);

                        //todo here to add the route between driver and the start trip


//                        clang = passData.getClang();
//                        clat = passData.getClat();
                        tolang = Double.parseDouble(order.getLang());
                        tolat = Double.parseDouble(order.getLat());
                        final LatLng start = new LatLng(clat, clang);
                        LatLng waypoint = new LatLng(clat, clang);
                        LatLng end = new LatLng(tolat, tolang);

                        Routing routing = new Routing.Builder()
                                .travelMode(Routing.TravelMode.DRIVING)
                                .withListener(AcceptOrderActivity.this)
                                .waypoints(start, waypoint, end)
                                .key("AIzaSyDCRPQbPfAo9dAWVKIOIQ3jKYHLf68oF_g")
                                .build();
                        routing.execute();

                        directionClientBtn.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                Intent intent = new Intent(android.content.Intent.ACTION_VIEW,
                                        Uri.parse("http://maps.google.com/maps?saddr=" + clat + "," + clang + "&daddr=" + tolat + "," + tolang));
                                startActivity(intent);
                            }
                        });




                    } else {
                        orderParent.setVisibility(View.GONE);
                        canceledParent.setVisibility(View.VISIBLE);
                    }
                } else if (orderStatus.equals("2")) {
                    if (order.getDriverId().equals(mFirebaseUser.getUid())) {

                        orderParent.setVisibility(View.GONE);
                        startParent.setVisibility(View.GONE);
                        endParent.setVisibility(View.VISIBLE);
                        clat = Double.parseDouble(order.getLat());
                        clang = Double.parseDouble(order.getLang());

                        tolat = Double.parseDouble(order.getToLat());
                        tolang = Double.parseDouble(order.getToLang());
                        LatLng start = new LatLng(clat, clang);
                        LatLng waypoint = new LatLng(tolat, tolang);
                        LatLng end = new LatLng(tolat, tolang);


                        Routing routing = new Routing.Builder()
                                .travelMode(Routing.TravelMode.DRIVING)
                                .withListener(AcceptOrderActivity.this)
                                .waypoints(start, waypoint, end)
                                .key("AIzaSyDCRPQbPfAo9dAWVKIOIQ3jKYHLf68oF_g")
                                .build();
                        routing.execute();

                        if (tolat==0||tolang==0){
                            destinationBTn.setVisibility(View.GONE);
                            userGuideTV.setVisibility(View.VISIBLE);
                            mMap.clear();

                        }else {
                            destinationBTn.setVisibility(View.VISIBLE);
                            userGuideTV.setVisibility(View.GONE);

                        }
                        destinationBTn.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                Intent intent = new Intent(android.content.Intent.ACTION_VIEW,
                                        Uri.parse("http://maps.google.com/maps?saddr=" + clat + "," + clang + "&daddr=" + tolat + "," + tolang));
                                startActivity(intent);
                            }
                        });


                    }
                }


                getUserData(order.getUserId());


            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        };
        df.addValueEventListener(postListener);
    }


    /**
     * here to get the data of the user who created the order
     *
     * @param userId the user id of who created the order
     */
    private void getUserData(final String userId) {
        df1 = FirebaseDatabase.getInstance().getReference().child("users").child(userId);
        ValueEventListener postListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                user = dataSnapshot.getValue(User.class);

                if (user == null)
                    return;


                if (user.getPhotoUrl() == null) {
                    defaultImage.setVisibility(View.VISIBLE);
                    defaultImage1.setVisibility(View.VISIBLE);
                    userImageString = "";
                } else if (user.getPhotoUrl().isEmpty()) {
                    defaultImage.setVisibility(View.VISIBLE);
                    defaultImage1.setVisibility(View.VISIBLE);


                    userImageString = "";
                } else {
                    userImageString = user.getPhotoUrl();
                    defaultImage.setVisibility(View.GONE);
                    if (!AcceptOrderActivity.this.isDestroyed()) {
                        Glide.with(AcceptOrderActivity.this).load(user.getPhotoUrl()).into(userImage);
                        Glide.with(AcceptOrderActivity.this).load(user.getPhotoUrl()).into(clientIV);
                    }
                }

                userNameTV.setText(user.getName());
                clientNameTV.setText(user.getName());
                //todo here to put the rate ya m3lem






            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        };
        df1.addValueEventListener(postListener);

    }


    private Bitmap getMarkerBitmapFromView(@DrawableRes int resId) {

        @SuppressLint("InflateParams") View customMarkerView = ((LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.view_custom_marker, null);
        ImageView markerImageView = customMarkerView.findViewById(R.id.markerIV);
        markerImageView.setImageResource(resId);
        customMarkerView.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);
        customMarkerView.layout(0, 0, customMarkerView.getMeasuredWidth(), customMarkerView.getMeasuredHeight());
        customMarkerView.buildDrawingCache();
        Bitmap returnedBitmap = Bitmap.createBitmap(customMarkerView.getMeasuredWidth(), customMarkerView.getMeasuredHeight(),
                Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(returnedBitmap);
        canvas.drawColor(Color.WHITE, PorterDuff.Mode.SRC_IN);
        Drawable drawable = customMarkerView.getBackground();
        if (drawable != null)
            drawable.draw(canvas);
        customMarkerView.draw(canvas);
        return returnedBitmap;
    }


    /**
     * action the button who create the order
     */
    @OnClick(R.id.acceptBtn)
    void acceptAction() {

        dfUpdateOrder = FirebaseDatabase.getInstance().getReference();


        dfUpdateOrder.child("Orders")
                .child(orderId).child("driverId").setValue(mFirebaseUser.getUid());

//        dfUpdateOrder.child("Orders")
//                .child(orderId).child("startTime").setValue(System.currentTimeMillis() + "");


        dfUpdateOrder.child("Orders")
                .child(orderId).child("status").setValue("1");


    }


    /**
     * action the start trip button
     */
    @OnClick(R.id.startBtn)
    void startAction() {

        dfUpdateOrder = FirebaseDatabase.getInstance().getReference();


        long startTime = System.currentTimeMillis();

        order.setStartTime(startTime + "");

        dfUpdateOrder.child("Orders")
                .child(orderId).child("startTime").setValue(startTime + "");

        dfUpdateOrder.child("Orders")
                .child(orderId).child("lat").setValue(clat);
        dfUpdateOrder.child("Orders")
                .child(orderId).child("lang").setValue(clang);


        dfUpdateOrder.child("Orders")
                .child(orderId).child("status").setValue("2");


    }

    /**
     * action the call button before start the trip and after accept the trip
     */
    @OnClick(R.id.callClientBtn)void callClientAction(){
        Intent intent = new Intent(Intent.ACTION_CALL);

        // here to get the data of the client
        intent.setData(Uri.parse("tel:" + user.getPhone()));
        if (ActivityCompat.checkSelfPermission(AcceptOrderActivity.this, android.Manifest.permission.CALL_PHONE)
                != PackageManager.PERMISSION_GRANTED) {
            // : Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        startActivity(intent);


    }

    @OnClick(R.id.endBtn)
    void endAction() {

        dfUpdateOrder = FirebaseDatabase.getInstance().getReference();


        long endTime = System.currentTimeMillis();

        order.setEndTime(endTime + "");



        dfUpdateOrder.child("Orders")
                .child(orderId).child("toLat").setValue(clat);
        dfUpdateOrder.child("Orders")
                .child(orderId).child("toLang").setValue(clang);

        dfUpdateOrder.child("Orders")
                .child(orderId).child("endTime").setValue(endTime + "");


        tripeTimeMin = Long.parseLong(order.getEndTime()) - Long.parseLong(order.getStartTime());
        tripeTimeMin = (tripeTimeMin / (60 * 1000));


        triptime = tripeTimeMin + "";


        if (order.getCatag().isEmpty() | order.getCatag() == null) {
            return;
        }
        FirebaseDatabase.getInstance().getReference().child("Category").child(order.getCatag()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    CategoryModel model = dataSnapshot.getValue(CategoryModel.class);


                    int numOfMin = (int) tripeTimeMin;
                    double numOfkilo = Double.parseDouble(tripDistance);
                    double trpPrice = (numOfMin * model.getMinutePrice()) + model.getOpenPrice() + (model.getKiloPrice() * numOfkilo);
                    trpPrice = Math.round(trpPrice);
                    trpPrice = trpPrice + (trpPrice * (model.getCompanyPercent() / 100));
                    if (trpPrice < model.getMinimumPrice()) {
                        trpPrice = model.getMinimumPrice();
                    }
                    dfUpdateOrder.child("Orders")
                            .child(orderId).child("tripDistance").setValue(tripDistance);


                    triptime = tripeTimeMin + "";
                    dfUpdateOrder.child("Orders")
                            .child(orderId).child("tripTime").setValue(triptime);


                    dfUpdateOrder.child("Orders")
                            .child(orderId).child("tripPrice").setValue(trpPrice + "");


                    dfUpdateOrder.child("Orders")
                            .child(orderId).child("status").setValue("4");

                    //todo here to go to the finish activity

                    Intent intent = new Intent(AcceptOrderActivity.this,FinishActivity.class);


                    intent.putExtra("price",trpPrice);
                    intent.putExtra("order",orderId);

                    startActivity(intent);

                    finish();

                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e("error", databaseError.getMessage());
            }
        });
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
//        float zoomLevel = (float) 14.0;
//        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(40.2081336,29.0457663), zoomLevel));

//        mMap.setMyLocationEnabled(true);
        mMap.setOnMyLocationButtonClickListener(this);
        mMap.setOnMyLocationClickListener(this);

//        mMap.setOnMyLocationCha


//
        enableMyLocation();

        mMap.setOnMyLocationChangeListener(new GoogleMap.OnMyLocationChangeListener() {

            @Override
            public void onMyLocationChange(Location arg0) {

//                mMap.addMarker(new MarkerOptions().position(new LatLng(arg0.getLatitude(), arg0.getLongitude())).title("It's Me!"));

                if (firstZoom == 0) {
                    float zoomLevel = (float) 15.0;

                    progressDialog.dismiss();
                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(arg0.getLatitude(), arg0.getLongitude()), zoomLevel));

                    clat = arg0.getLatitude();
                    clang = arg0.getLongitude();
                    Toast.makeText(AcceptOrderActivity.this, "clat:"+clat, Toast.LENGTH_SHORT).show();
                    firstZoom = 1;
                }


                String GEO_FIRE_REF = GEO_FIRE_DB + "/GeoFire";
                GeoFire geoFire = new GeoFire(FirebaseDatabase.getInstance().getReference().child("GeoFire"));
                geoFire.setLocation(mFirebaseUser.getUid(), new GeoLocation(arg0.getLatitude(), arg0.getLatitude()));


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

    @Override
    public boolean onMyLocationButtonClick() {
//        Toast.makeText(this, "MyLocation button clicked", Toast.LENGTH_SHORT).show();
        // Return false so that we don't consume the event and the default behavior still occurs
        // (the camera animates to the user's current position).
//        float zoomLevel = (float) 14.0;
//        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(mMap.getMyLocation().getLatitude(),mMap.getMyLocation().getLongitude()), zoomLevel));

        return false;
    }

    @Override
    public void onMyLocationClick(@NonNull Location location) {
//        Toast.makeText(this, "Current location:\n" + location, Toast.LENGTH_LONG).show();
        float zoomLevel = (float) 14.0;
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(location.getLatitude(), location.getLongitude()), zoomLevel));
        Toast.makeText(this, "aaa", Toast.LENGTH_SHORT).show();

    }

    /**
     * Displays a dialog with error message explaining that the location permission is missing.
     */

    private void showMissingPermissionError() {
        PermissionUtils.PermissionDeniedDialog
                .newInstance(true).show(getSupportFragmentManager(), "dialog");
    }


    // here the route
    @Override
    public void onRoutingFailure(RouteException e) {
        Log.d("google", "failed          " + e.getMessage() + e.toString());
    }

    @Override
    public void onRoutingStart() {
        Log.d("google", "start");
    }

    @Override
    public void onRoutingSuccess(ArrayList<Route> route, int i1) {

        mMap.clear();
        if (polylines.size() > 0) {
            for (Polyline poly : polylines) {
                poly.remove();
            }
        }

        polylines = new ArrayList<>();
        //add route(s) to the map.

        for (int i = 0; i < route.size(); i++) {

            //In case of more than 5 alternative routes
            int colorIndex = i % COLORS.length;

            PolylineOptions polyOptions = new PolylineOptions();
            polyOptions.color(getResources().getColor(COLORS[colorIndex]));
            polyOptions.width(10 + i * 3);
            polyOptions.addAll(route.get(i).getPoints());
            Polyline polyline = mMap.addPolyline(polyOptions);
            polylines.add(polyline);

            triptime = route.get(i).getDurationValue() + "";
            tripDistance = (route.get(i).getDistanceValue() / 1000) + "";



//            Log.d("google","this is the distance and duration  "+ route.get(i).getDistanceText()+ route.get(i).getDurationText());


//            Toast.makeText(getApplicationContext(),"Route "+ (i+1) +": distance - "+ route.get(i).getDistanceValue()+": duration - "+ route.get(i).getDurationValue(),Toast.LENGTH_SHORT).show();
        }



//        Log.d("google", "this is the distance and duration00000000  " + route.get(0).getDistanceValue() + "       " + route.get(0).getDurationValue());
////        route.get(0).getDistanceText();
//        Log.d("google", "this is the distance and duration   " + (route.get(0).getDistanceValue() / 1000.0) + "       " + (route.get(0).getDurationValue() / 60));
//        Log.d("google", "this is the distance and duration00000000  " + route.get(0).getDistanceText() + "       " + route.get(0).getDurationText());


//        routeDuration = (route.get(0).getDurationValue() / 60);
//        routeDistance = (route.get(0).getDistanceValue() / 1000.0);
//        getEstimatedPrice();





        LatLng from_Latlng=new LatLng(clat,clang);
        LatLng to_Latlong=new LatLng(tolat,tolang);

        LatLngBounds.Builder builder = new LatLngBounds.Builder();
        builder.include(from_Latlng);
        builder.include(to_Latlong);
        LatLngBounds bounds = builder.build();
        mMap.animateCamera(CameraUpdateFactory.newLatLngBounds(route.get(0).getLatLgnBounds(), 10), 2000, null);



        // Start marker


        MarkerOptions marker1 = new MarkerOptions()
                .position(new LatLng(clat, clang))
                .icon(BitmapDescriptorFactory.fromBitmap(getMarkerBitmapFromView(R.mipmap.ic_launcher)));

        mMap.addMarker(marker1);


        MarkerOptions marker2 = new MarkerOptions()
                .position(new LatLng(tolat, tolang))
                .icon(BitmapDescriptorFactory.fromBitmap(getMarkerBitmapFromView(R.drawable.descccc)));

        mMap.addMarker(marker2);


        // Start marker


    }

    @Override
    public void onRoutingCancelled() {
        Log.d("google", "canceled ");
    }


    // Trigger new location updates at interval
    protected void startLocationUpdates() {
//        if(!gpsTracker.canGetLocation()) {
//
//            Log.d("google","cannot get location");
//            gpsTracker.showSettingsAlert();
//            return;
//        }
//        Log.d("google","can get location");

        // Create the location request to start receiving updates
        mLocationRequest = new LocationRequest();
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mLocationRequest.setInterval(UPDATE_INTERVAL);
        mLocationRequest.setFastestInterval(FASTEST_INTERVAL);

        // Create LocationSettingsRequest object using location request
        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder();
        builder.addLocationRequest(mLocationRequest);
        final LocationSettingsRequest locationSettingsRequest = builder.build();

        // Check whether location settings are satisfied
        // https://developers.google.com/android/reference/com/google/android/gms/location/SettingsClient
        SettingsClient settingsClient = LocationServices.getSettingsClient(this);
        Task<LocationSettingsResponse> task = settingsClient.checkLocationSettings(locationSettingsRequest);


        task.addOnSuccessListener(this, new OnSuccessListener<LocationSettingsResponse>() {
            @Override
            public void onSuccess(LocationSettingsResponse locationSettingsResponse) {
                // All location settings are satisfied. The client can initialize
                // location requests here.
                // ...
                Log.d("google", "can get loaction");
//                locationSettingsResponse.getLocationSettingsStates().isGpsPresent()
                if (!locationSettingsResponse.getLocationSettingsStates().isGpsUsable()) {


                }
            }
        });

        task.addOnFailureListener(this, new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                if (e instanceof ResolvableApiException) {
                    // Location settings are not satisfied, but this can be fixed
                    // by showing the user a dialog.
                    try {
                        // Show the dialog by calling startResolutionForResult(),
                        // and check the result in onActivityResult().
                        ResolvableApiException resolvable = (ResolvableApiException) e;
                        resolvable.startResolutionForResult(AcceptOrderActivity.this,
                                2);

                        Log.d("google", "this dialog didnot work exception ");
                    } catch (IntentSender.SendIntentException sendEx) {
                        // Ignore the error.
                        Log.d("google", "location exception " + sendEx.getMessage());
                    }
                }
            }
        });


        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // : Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.

            return;
        }

        // new Google API SDK v11 uses getFusedLocationProviderClient(this)
        LocationServices.getFusedLocationProviderClient(this).requestLocationUpdates(mLocationRequest, new LocationCallback() {
                    @Override
                    public void onLocationResult(LocationResult locationResult) {
                        // do work here
//                        onLocationChanged(locationResult.getLastLocation());
                    }
                },
                Looper.myLooper());
    }


    @OnClick(R.id.backBtn)
    void backBtn() {
        finish();
    }


    //todo here to accept the order
    @OnClick(R.id.navigateBtn)
    void navigateBtn() {


        Intent intent = new Intent(android.content.Intent.ACTION_VIEW,
                Uri.parse("http://maps.google.com/maps?saddr=" + clat + "," + clang + "&daddr=" + tolat + "," + tolang));
        startActivity(intent);


    }

}
