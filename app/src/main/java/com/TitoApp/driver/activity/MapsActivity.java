package com.TitoApp.driver.activity;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.support.annotation.DrawableRes;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.TitoApp.driver.R;
import com.bumptech.glide.Glide;
import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
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
import com.google.firebase.iid.FirebaseInstanceId;

import com.TitoApp.driver.model.DriverModel;
import com.TitoApp.driver.util.PermissionUtils;

import butterknife.BindView;
import butterknife.ButterKnife;


public class MapsActivity extends AppCompatActivity implements GoogleMap.OnMyLocationButtonClickListener,
        GoogleMap.OnMyLocationClickListener,
        OnMapReadyCallback,
        ActivityCompat.OnRequestPermissionsResultCallback,
        NavigationView.OnNavigationItemSelectedListener {


    //init the views with butter knife
    @BindView(R.id.availableSW)
    Switch availableSW;

    private static final String TAG = "google";


    //inti the view
    private RecyclerView.Adapter mAdapter;
    private RecyclerView recyclerView;

    private FrameLayout expand_category_frame;
    private ImageView expand_image;
    private boolean expand = false;
    private Button viewProfileBT, requestPersonBT;

    //for the user detail and the category visibility
    View categ_detail, marker_user_detail;


    //the left menu
    private NavigationView mDrawer;
    private DrawerLayout mDrawerLayout;
    private ImageView menuIV;
    private int mSelectedId;

    //the firebase inital
    // Firebase instance variables
    private FirebaseAuth mFirebaseAuth;
    private FirebaseUser mFirebaseUser;
    private DatabaseReference df, df2;
    // firebase to get username and image
    private DatabaseReference mFirebaseDatabaseReference, df1;


    String orderId;


    ProgressDialog progressDialog;

    /**
     * Request code for location permission request.
     *
     * @see #onRequestPermissionsResult(int, String[], int[])
     */
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;

    /**
     * Flag indicating whether a requested permission has been denied after returning in
     * {@link #onRequestPermissionsResult(int, String[], int[])}.
     */
    private boolean mPermissionDenied = false;

    private GoogleMap mMap;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.map_include);
        ButterKnife.bind(this);


        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("جاري تحديد الموقع...");
        progressDialog.show();

        mFirebaseAuth = FirebaseAuth.getInstance();
        mFirebaseUser = mFirebaseAuth.getCurrentUser();
        df = FirebaseDatabase.getInstance().getReference();
        df1 = FirebaseDatabase.getInstance().getReference();
        df2 = FirebaseDatabase.getInstance().getReference();
        if (mFirebaseUser == null) {
            // Not signed in, launch the Sign In activity
            Intent i = new Intent(MapsActivity.this, LoginActivity.class);
            i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(i);
            return;
        }

        Log.d("google","this is the user id "+mFirebaseUser.getUid());

        df = FirebaseDatabase.getInstance().getReference().child("Drivers").child(mFirebaseUser.getUid());
        ValueEventListener postListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                DriverModel user = dataSnapshot.getValue(DriverModel.class);
                if (user==null){
                    Intent i = new Intent(MapsActivity.this, LoginActivity.class);
                    i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(i);
                    return;
                }
                if (user.getStatus().equals("0")) {
                    Intent i = new Intent(MapsActivity.this, VerifyActivity.class);
                    i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(i);
                }


            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        };
        df.addValueEventListener(postListener);


        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        initViews();

    }


    private boolean checkLogin() {
        return true;
    }

    private static final String GEO_FIRE_DB = "https://tito-c762f.firebaseio.com/";

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
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
                // TODO Auto-generated method stub

//                mMap.addMarker(new MarkerOptions().position(new LatLng(arg0.getLatitude(), arg0.getLongitude())).title("It's Me!"));
                float zoomLevel = (float) 15.0;
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(arg0.getLatitude(), arg0.getLongitude()), zoomLevel));

                progressDialog.dismiss();
                String GEO_FIRE_REF = GEO_FIRE_DB + "/GeoFire";
                GeoFire geoFire = new GeoFire(FirebaseDatabase.getInstance().getReference().child("GeoFire"));
                geoFire.setLocation(mFirebaseUser.getUid(), new GeoLocation(arg0.getLatitude(), arg0.getLongitude()));


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
            PermissionUtils.requestPermission(MapsActivity.this, LOCATION_PERMISSION_REQUEST_CODE,
                    Manifest.permission.ACCESS_FINE_LOCATION, true);
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
        Toast.makeText(this, "Current location:\n" + location, Toast.LENGTH_LONG).show();
        float zoomLevel = (float) 14.0;
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(location.getLatitude(), location.getLongitude()), zoomLevel));
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        if (requestCode != LOCATION_PERMISSION_REQUEST_CODE) {
            return;
        }

        if (PermissionUtils.isPermissionGranted(permissions, grantResults,
                Manifest.permission.ACCESS_FINE_LOCATION)) {
            // Enable the my location layer if the permission has been granted.
            enableMyLocation();
        } else {
            // Display the missing permission error dialog when the fragments resume.
            mPermissionDenied = true;
        }
    }

    @Override
    protected void onResumeFragments() {
        super.onResumeFragments();
        if (mPermissionDenied) {
            // Permission was not granted, display error dialog.
            showMissingPermissionError();
            mPermissionDenied = false;
        }
    }

    /**
     * Displays a dialog with error message explaining that the location permission is missing.
     */
    private void showMissingPermissionError() {
        PermissionUtils.PermissionDeniedDialog
                .newInstance(true).show(getSupportFragmentManager(), "dialog");
    }

    private Bitmap getMarkerBitmapFromView(@DrawableRes int resId) {


        @SuppressLint("InflateParams") View customMarkerView = ((LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.view_custom_marker, null);
//        TextView markerImageView =  customMarkerView.findViewById(R.id.ivHostImage);
//        markerImageView.setImageResource(resId);
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


    //here we begin to inti the views
    private void initViews() {


        Log.d("google","the user is null 1");

        mDrawer = findViewById(R.id.nvView);
        mDrawerLayout = findViewById(R.id.drawer_layout);
        menuIV = findViewById(R.id.menuIV);
        mDrawer.setNavigationItemSelectedListener(this);

        //to get username and image in left menu
        View hView = mDrawer.getHeaderView(0);
        final TextView nav_user = hView.findViewById(R.id.userNameNav);
        final ImageView nav_image = hView.findViewById(R.id.image);

        mFirebaseDatabaseReference = FirebaseDatabase.getInstance().getReference().child("Drivers").child(mFirebaseUser.getUid());
        ValueEventListener postListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                DriverModel user = dataSnapshot.getValue(DriverModel.class);

                Log.d("google","the user is null 2");

                if (user == null) {

                    Log.d("google","the user is null 3");
                    return;
                }
                if (user.getProfileImage() == null || user.getProfileImage().isEmpty())
                    nav_image.setImageDrawable(getResources().getDrawable(R.drawable.account));
                else if (!MapsActivity.this.isDestroyed())
                    Glide.with(MapsActivity.this).load(user.getProfileImage()).into(nav_image);

                nav_user.setText(user.getFname() + "  " + user.getLname());

                nav_user.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        startActivity(new Intent(MapsActivity.this, ProfileActivity.class));
                    }
                });
                nav_image.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        startActivity(new Intent(MapsActivity.this, ProfileActivity.class));
                    }
                });


                if (user.getAvailable() == 1) {
                    availableSW.setText("نشط");
                    availableSW.setChecked(true);
                } else {
                    availableSW.setText("غير نشط");
                    availableSW.setChecked(false);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

                Log.d("google","the user is null 4  "+databaseError.toString());
            }
        };
        mFirebaseDatabaseReference.addValueEventListener(postListener);


        menuIV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mDrawerLayout.isDrawerVisible(GravityCompat.START)) {
                    mDrawerLayout.closeDrawer(GravityCompat.START);
                } else {
                    mDrawerLayout.openDrawer(GravityCompat.START);
                }
            }
        });


        availableSW.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                // do something, the isChecked will be
                // true if the switch is in the On position

                //todo add the token

                DatabaseReference ref = FirebaseDatabase.getInstance().getReference().child("Tokens");

                String token = FirebaseInstanceId.getInstance().getToken();

                ref.child(mFirebaseUser.getUid()).child("token").setValue(token);


                if (isChecked) {
//                    availableSW.setText("  متاح  ");

                    ref.child(mFirebaseUser.getUid()).child("token").setValue(token);

                    df1.child("Drivers")
                            .child(mFirebaseUser.getUid()).child("available").setValue(1);


                    String GEO_FIRE_REF = GEO_FIRE_DB + "/GeoFire";
                    GeoFire geoFire = new GeoFire(FirebaseDatabase.getInstance().getReferenceFromUrl(GEO_FIRE_REF));

                    if (mMap != null)
                       try {
                        if (mMap.getMyLocation() != null)
                               geoFire.setLocation(mFirebaseUser.getUid(), new GeoLocation(mMap.getMyLocation().getLatitude(), mMap.getMyLocation().getLongitude()));
                       }catch (Exception e){}


                } else {
//                    availableSW.setText("  غير متاح  ");
                    ref.child(mFirebaseUser.getUid()).child("token").setValue(" ");

                    df1.child("Drivers")
                            .child(mFirebaseUser.getUid()).child("available").setValue(0);
                }
            }
        });


    }

    @SuppressLint("WrongConstant")
    private void itemSelection(int mSelectedId) {

        //IntentDetail data;
        switch (mSelectedId) {


            case R.id.logout_lm:
                mDrawerLayout.closeDrawer(GravityCompat.START);


                //to remove the token from the backend if he is not login
                DatabaseReference ref = FirebaseDatabase.getInstance().getReference().child("Tokens");
                ref.child(mFirebaseUser.getUid()).child("token").setValue("");


                Intent i = new Intent(MapsActivity.this, LoginActivity.class);
                i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(i);
                mFirebaseAuth.signOut();
                break;
            case R.id.profile_lm:
                mDrawerLayout.closeDrawer(GravityCompat.START);
                startActivity(new Intent(MapsActivity.this, ProfileActivity.class));
                break;
            case R.id.wallet_lm:
                mDrawerLayout.closeDrawer(GravityCompat.START);
                startActivity(new Intent(MapsActivity.this, WalletActivity.class));
                break;
        }

    }


    /**
     * Called when an item in the navigation menu is selected.
     *
     * @param ,menuItem The selected item
     * @return true to display the item as the selected item
     */
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
        menuItem.setChecked(false);
        mSelectedId = menuItem.getItemId();
        itemSelection(mSelectedId);
        return true;
    }

    @Override
    public void onSaveInstanceState(Bundle outState, PersistableBundle outPersistentState) {
        super.onSaveInstanceState(outState, outPersistentState);
        //save selected item so it will remains same even after orientation change
        outState.putInt("SELECTED_ID", mSelectedId);
    }


}


