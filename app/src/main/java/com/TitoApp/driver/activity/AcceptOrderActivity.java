package com.TitoApp.driver.activity;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.TitoApp.driver.R;
import com.bumptech.glide.Glide;
import com.directions.route.Route;
import com.directions.route.RouteException;
import com.directions.route.Routing;
import com.directions.route.RoutingListener;
import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import com.TitoApp.driver.model.OrderModel;
import com.TitoApp.driver.model.User;
import com.TitoApp.driver.util.PermissionUtils;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class AcceptOrderActivity extends AppCompatActivity implements GoogleMap.OnMyLocationButtonClickListener,
        GoogleMap.OnMyLocationClickListener,
        OnMapReadyCallback , RoutingListener {


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
    LinearLayout startParent;
    @BindView(R.id.endParent)
    LinearLayout endParent;

    @BindView(R.id.routeInfoTV)
    TextView routeInfoTV;

    private GoogleMap mMap;


    private static final String TAG = "google";


    private FirebaseAuth mFirebaseAuth;
    private FirebaseUser mFirebaseUser;


    private String orderId;


    private DatabaseReference df, df1, dfUpdateOrder;

    private String userImageString="";

    private String orderStatus="";


    private int firstZoom ;

    private String triptime="",tripDistance="2";
    //the route
    private List<Polyline> polylines;
    private static final int[] COLORS = new int[]{R.color.new_color_dark,R.color.new_color_1,R.color.new_color_1,R.color.colorAccent,R.color.primary_dark_material_light};



    double clat,clang;
    double tolat,tolang;

ProgressDialog progressDialog;

    OrderModel order;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_accept_order);
        ButterKnife.bind(this);

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


        if (orderId == null)
            return;


        getOrderData();




        orderStatus ="";

    }


    /**
     * here to get the data from the firebase for the order and listen if any com.TitoApp.com.TitoApp.driver is accept it or if the com.TitoApp.com.TitoApp.driver is refused
     */
    private void getOrderData() {

//todo here to check the status of the order and change it

        df = FirebaseDatabase.getInstance().getReference().child("Orders").child(orderId);
        ValueEventListener postListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                order = dataSnapshot.getValue(OrderModel.class);

                if (order == null)
                    return;

                Log.d("hazem", "here in the order " + order.getStatus());


                if (order.getStatus().equals(orderStatus)){
                    return;
                }


                if (orderStatus.equals("1")){
                    if (order.getStatus().equals("0"))
                        return;
                }

                if (orderStatus.equals("2")){
                    if (order.getStatus().equals("0"))
                        return;
                    if (order.getStatus().equals("1"))
                        return;
                }

                //todo start from here order
                orderStatus = order.getStatus();


                if (orderStatus.equals("0")) {
                    orderParent.setVisibility(View.VISIBLE);
                    canceledParent.setVisibility(View.GONE);
                } else if (orderStatus.equals("1")) {


                    if (order.getDriverId().equals(mFirebaseUser.getUid())) {

                        orderParent.setVisibility(View.GONE);
                        startParent.setVisibility(View.VISIBLE);

                        //show the user location on the map
                                              CameraUpdate center = CameraUpdateFactory.newLatLng(new LatLng(Double.parseDouble(order.getLat()),
                                Double.parseDouble(order.getLang())));

                        CameraUpdate zoom = CameraUpdateFactory.zoomTo(16);

                        mMap.moveCamera(center);


                        MarkerOptions marker1 = new MarkerOptions()
                                .position(new LatLng(Double.parseDouble(order.getLat()),
                                        Double.parseDouble(order.getLang())))
                                .icon(BitmapDescriptorFactory.fromBitmap(getMarkerBitmapFromView(userImageString)));

                        mMap.addMarker(marker1);


                    } else {
                        orderParent.setVisibility(View.GONE);
                        canceledParent.setVisibility(View.VISIBLE);
                    }
                }else if (orderStatus.equals("2")){
                    if (order.getDriverId().equals(mFirebaseUser.getUid())) {

                        orderParent.setVisibility(View.GONE);
                        startParent.setVisibility(View.GONE);
                        endParent.setVisibility(View.VISIBLE);


                        //show the user location on the map
                        CameraUpdate center = CameraUpdateFactory.newLatLng(new LatLng(Double.parseDouble(order.getToLat()),
                                Double.parseDouble(order.getToLang())));

                        CameraUpdate zoom = CameraUpdateFactory.zoomTo(16);

                        mMap.moveCamera(center);


                        MarkerOptions marker1 = new MarkerOptions()
                                .position(new LatLng(Double.parseDouble(order.getToLat()),
                                        Double.parseDouble(order.getToLang())))
                                .icon(BitmapDescriptorFactory.fromBitmap(getMarkerBitmapFromView("")));

                        mMap.addMarker(marker1);




                        clat = Double.parseDouble(order.getLat());
                        clang = Double.parseDouble(order.getLang());

                        tolat = Double.parseDouble(order.getToLat());
                        tolang = Double.parseDouble(order.getToLang());
                        LatLng start = new LatLng(clat, clang);
                        LatLng waypoint= new LatLng(tolat, tolang);
                        LatLng end = new LatLng(tolat, tolang);

                        Routing routing = new Routing.Builder()
                                .travelMode(Routing.TravelMode.DRIVING)
                                .withListener(AcceptOrderActivity.this)
                                .waypoints(start, waypoint, end)
                                .key("AIzaSyBVHgr4vi1pi5DRuQj8TjJxBPZ67DP4LSg")
                                .build();
                        routing.execute();


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
                User user = dataSnapshot.getValue(User.class);

                if (user == null)
                    return;


                if (user.getPhotoUrl() == null) {
                    defaultImage.setVisibility(View.VISIBLE);
                    userImageString = "";
                } else if (user.getPhotoUrl().isEmpty()) {
                    defaultImage.setVisibility(View.VISIBLE);
                    userImageString = "";
                } else {
                    userImageString = user.getPhotoUrl();
                    defaultImage.setVisibility(View.GONE);
                    if (!AcceptOrderActivity.this.isDestroyed())
                        Glide.with(AcceptOrderActivity.this).load(user.getPhotoUrl()).into(userImage);
                }

                userNameTV.setText(user.getName());

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        };
        df1.addValueEventListener(postListener);

    }


    private Bitmap getMarkerBitmapFromView(String userImage) {

        View customMarkerView = ((LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.view_custom_marker, null);
        ImageView markerImageView = customMarkerView.findViewById(R.id.ivHostImage);
        ImageView markerImageViewdefault = customMarkerView.findViewById(R.id.ivHostImageDefault);
        ImageView ivHostImageDes = customMarkerView.findViewById(R.id.ivHostImageDes);
//        markerImageView.setImageResource(resId);


        if (!userImage.isEmpty()){
            if (userImageString.isEmpty()) {
                markerImageViewdefault.setVisibility(View.VISIBLE);
            } else {
                markerImageViewdefault.setVisibility(View.GONE);
                if (!AcceptOrderActivity.this.isDestroyed())
                    Glide.with(AcceptOrderActivity.this).load(userImageString).into(markerImageView);
            }
        }else {
            ivHostImageDes.setVisibility(View.VISIBLE);
        }
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

        order.setStartTime(startTime+"");

        dfUpdateOrder.child("Orders")
                .child(orderId).child("startTime").setValue(startTime + "");


        dfUpdateOrder.child("Orders")
                .child(orderId).child("status").setValue("2");


    }

    @OnClick(R.id.endBtn)
    void endAction() {

        dfUpdateOrder = FirebaseDatabase.getInstance().getReference();


        long endTime = System.currentTimeMillis();

        order.setEndTime(endTime +"");


        dfUpdateOrder.child("Orders")
                .child(orderId).child("endTime").setValue(endTime  + "");


        long tripeTimeMin = Long.parseLong(order.getEndTime())-Long.parseLong(order.getStartTime());
        tripeTimeMin = (tripeTimeMin/(60*1000));


        triptime = tripeTimeMin+"";


        int numOfMin = (int) tripeTimeMin;

        double numOfkilo = Double.parseDouble(tripDistance);
        double trpPrice = (numOfMin*.3)+7+(2*numOfkilo);
        trpPrice = Math.round(trpPrice);
        if (trpPrice < 10){
            trpPrice = 10.0;
        }

        dfUpdateOrder.child("Orders")
                .child(orderId).child("tripDistance").setValue(tripDistance);


        triptime = tripeTimeMin+"";
        dfUpdateOrder.child("Orders")
                .child(orderId).child("tripTime").setValue(triptime);


        dfUpdateOrder.child("Orders")
                .child(orderId).child("tripPrice").setValue(trpPrice+"");


        dfUpdateOrder.child("Orders")
                .child(orderId).child("status").setValue("4");


        finish();


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

                if (firstZoom==0){
                    float zoomLevel = (float) 15.0;
                    progressDialog.dismiss();
                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(arg0.getLatitude(), arg0.getLongitude()), zoomLevel));

                    firstZoom =1;
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
    }

    /**
     * Displays a dialog with error message explaining that the location permission is missing.
     */

    private void showMissingPermissionError() {
        PermissionUtils.PermissionDeniedDialog
                .newInstance(true).show(getSupportFragmentManager(), "dialog");
    }



    //todo here the route
    @Override
    public void onRoutingFailure(RouteException e) {
        Log.d("google","failed          "+e.getMessage()+e.toString());
    }

    @Override
    public void onRoutingStart() {
        Log.d("google","start");
    }

    @Override
    public void onRoutingSuccess(ArrayList<Route> route, int i1) {

        CameraUpdate center = CameraUpdateFactory.newLatLng(new LatLng(clat, clang));
        CameraUpdate zoom = CameraUpdateFactory.zoomTo(16);

        mMap.moveCamera(center);


        if(polylines.size()>0) {
            for (Polyline poly : polylines) {
                poly.remove();
            }
        }

        polylines = new ArrayList<>();
        //add route(s) to the map.
        for (int i = 0; i <route.size(); i++) {

            //In case of more than 5 alternative routes
            int colorIndex = i % COLORS.length;

            PolylineOptions polyOptions = new PolylineOptions();
            polyOptions.color(getResources().getColor(COLORS[colorIndex]));
            polyOptions.width(10 + i * 3);
            polyOptions.addAll(route.get(i).getPoints());
            Polyline polyline = mMap.addPolyline(polyOptions);
            polylines.add(polyline);

            Toast.makeText(getApplicationContext(),"Route "+ (i+1) +": distance - "+ route.get(i).getDistanceValue()+": duration - "+ route.get(i).getDurationValue(),Toast.LENGTH_SHORT).show();


            routeInfoTV.setText("distance - "+ route.get(i).getDistanceValue()+" : duration - "+ route.get(i).getDurationValue());

            triptime = route.get(i).getDurationValue()+"";
            Toast.makeText(this, "distance: "+tripDistance, Toast.LENGTH_SHORT).show();
            tripDistance =  route.get(i).getDistanceValue()+"";


        }

        // Start marker


    }

    @Override
    public void onRoutingCancelled() {
        Log.d("google","canceled ");
    }


}
