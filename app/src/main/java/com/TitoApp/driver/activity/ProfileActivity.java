package com.TitoApp.driver.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;

import com.TitoApp.driver.R;
import com.TitoApp.driver.model.DriverModel;
import com.bumptech.glide.Glide;
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
import de.hdodenhof.circleimageview.CircleImageView;

public class ProfileActivity extends AppCompatActivity {


    //init views
    @BindView(R.id.nameTV)TextView nameTV;
    @BindView(R.id.emailTV)TextView emailTV;
    @BindView(R.id.phoneTV)TextView phoneTV;
    @BindView(R.id.imageProfile)CircleImageView imageProfile;

    // Firebase instance variables
    private FirebaseAuth mFirebaseAuth;
    private FirebaseUser mFirebaseUser;

    private DatabaseReference df;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        ButterKnife.bind(this);


        //init the firebase
        mFirebaseAuth = FirebaseAuth.getInstance();
        mFirebaseUser = mFirebaseAuth.getCurrentUser();
        df = FirebaseDatabase.getInstance().getReference();




        df = FirebaseDatabase.getInstance().getReference().child("Drivers").child(mFirebaseUser.getUid());
        ValueEventListener postListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                DriverModel user = dataSnapshot.getValue(DriverModel.class);

                if (user==null)
                    return;


                if (user.getProfileImage()==null)
                    imageProfile.setImageDrawable(getResources().getDrawable(R.drawable.account));
                else
                    if (user.getProfileImage().isEmpty())
                        imageProfile.setImageDrawable(getResources().getDrawable(R.drawable.account));
                    else {
                        if (!ProfileActivity.this.isDestroyed())
                            Glide.with(ProfileActivity.this).load(user.getProfileImage()).into(imageProfile);
                    }

                nameTV.setText(user.getFname()+" "+user.getLname());
                emailTV.setText(user.getEmail());
                phoneTV.setText(user.getPhone());

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        };
        df.addValueEventListener(postListener);

    }

    /**
     * press the back button
     */
    @OnClick(R.id.back_image)
    void back__() {
        finish();
    }

    /**
     *
     * go to the edit profile
     */
    @OnClick(R.id.ProfileEditBtn)void editProfile(){
        startActivity(new Intent(ProfileActivity.this,UpdateProfileActivity.class));
    }

    /**
     * go to the car paper activity
     */
    @OnClick(R.id.carPaperTV)void carPaper(){
        startActivity(new Intent(ProfileActivity.this,CarPapersActivity.class));
    }
    /**
     * go to the user paper activity
     */
    @OnClick(R.id.userPaperTV)void userPaper(){
        startActivity(new Intent(ProfileActivity.this,UserPaperActivity.class));
    }
}
