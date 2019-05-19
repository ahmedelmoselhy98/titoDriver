package com.NaqelApp.driver.activity;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.NaqelApp.driver.R;
import com.NaqelApp.driver.model.DriverModel;
import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.vansuita.pickimage.bean.PickResult;
import com.vansuita.pickimage.bundle.PickSetup;
import com.vansuita.pickimage.dialog.PickImageDialog;
import com.vansuita.pickimage.listeners.IPickResult;

import java.io.ByteArrayOutputStream;
import java.util.Random;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import de.hdodenhof.circleimageview.CircleImageView;

public class UpdateProfileActivity extends AppCompatActivity implements IPickResult {

    //init the views
    @BindView(R.id.profile_image)CircleImageView profile_image;
    @BindView(R.id.take_photo_profile)CircleImageView takePhoto;
    @BindView(R.id.loading)ProgressBar loading;
    @BindView(R.id.fnameET)EditText fnameET;
    @BindView(R.id.lnameET)EditText lnameET;
    @BindView(R.id.phoneET)EditText phoneET;
    @BindView(R.id.addressET)EditText addressET;



    // Firebase instance variables
    private FirebaseAuth mFirebaseAuth;
    private FirebaseUser mFirebaseUser;
    private DatabaseReference mFirebaseDatabaseReference, df;
    private StorageReference mStorageRef;

    //for upload the image
    Bitmap image;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update_profile);
        ButterKnife.bind(this);

        mFirebaseDatabaseReference = FirebaseDatabase.getInstance().getReference();
        df = FirebaseDatabase.getInstance().getReference();
        // Initialize Firebase Auth
        mFirebaseAuth = FirebaseAuth.getInstance();
        mFirebaseUser = mFirebaseAuth.getCurrentUser();
        //init the storage
        mStorageRef = FirebaseStorage.getInstance().getReference();



        df = FirebaseDatabase.getInstance().getReference().child("Drivers").child(mFirebaseUser.getUid());
        ValueEventListener postListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                DriverModel user = dataSnapshot.getValue(DriverModel.class);

                if (user.getProfileImage().isEmpty())
                    profile_image.setImageDrawable(getResources().getDrawable(R.drawable.account));
                else {
                    if (!UpdateProfileActivity.this.isDestroyed())
                        Glide.with(UpdateProfileActivity.this).load(user.getProfileImage()).into(profile_image);
                }
                fnameET.setText(user.getFname());
                lnameET.setText(user.getLname());
                phoneET.setText(user.getPhone());
                addressET.setText(user.getAddress());


            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        };
        df.addValueEventListener(postListener);



    }

    /**
     * to update the profile image
     */
    @OnClick(R.id.take_photo_profile) void updateProfile(){
        PickImageDialog.build(new PickSetup()).show(UpdateProfileActivity.this);
    }

    /**
     * update the data
     */
    @OnClick(R.id.EditProfileBtn)void editProfile(){

        if (lnameET.getText().toString().isEmpty()||lnameET.getText().toString().isEmpty()
                ||phoneET.getText().toString().isEmpty()||addressET.getText().toString().isEmpty()){
            Toast.makeText(UpdateProfileActivity.this, "من فضلك اكمل جميع البيانات", Toast.LENGTH_LONG).show();
            return;
        }

        mFirebaseDatabaseReference.child("Drivers")
                .child(mFirebaseUser.getUid()).child("phone").setValue(phoneET.getText().toString());

        mFirebaseDatabaseReference.child("Drivers")
                .child(mFirebaseUser.getUid()).child("fname").setValue(fnameET.getText().toString());
        mFirebaseDatabaseReference.child("Drivers")
                .child(mFirebaseUser.getUid()).child("lname").setValue(lnameET.getText().toString());
        mFirebaseDatabaseReference.child("Drivers")
                .child(mFirebaseUser.getUid()).child("address").setValue(addressET.getText().toString());


        Toast.makeText(UpdateProfileActivity.this, "تم تعديل البيانات بنجاح", Toast.LENGTH_LONG).show();


    }
    /**
     * press the back button
     */
    @OnClick(R.id.back_image)
    void back__() {
        finish();
    }



    /**
     * here to get the image from the gallery or from the camera
     *
     * @param pickResult the object of the result
     */
    @Override
    public void onPickResult(PickResult pickResult) {
        if (pickResult.getError() == null) {
            takePhoto.setImageBitmap(null);
            profile_image.setImageBitmap(pickResult.getBitmap());
            image = pickResult.getBitmap();

            loading.setVisibility(View.VISIBLE);
            StorageReference tripsRef = mStorageRef.child("Drivers/" + random() + ".jpg");
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            image.compress(Bitmap.CompressFormat.JPEG, 100, baos);
            byte[] data = baos.toByteArray();

            UploadTask uploadTask = tripsRef.putBytes(data);
            uploadTask.addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception exception) {
                    // Handle unsuccessful uploads
                    Toast.makeText(UpdateProfileActivity.this, "حدث خطأ", Toast.LENGTH_LONG).show();
                    loading.setVisibility(View.GONE);
                }
            }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    // taskSnapshot.getMetadata() contains file metadata such as size, content-type, and download URL.
//                    String imageUrl = taskSnapshot.getDownloadUrl().toString();

                    //todo here the image url
                    // taskSnapshot.getMetadata() contains file metadata such as size, content-type, and download URL.
                    String imageUrl = taskSnapshot.getUploadSessionUri().toString();

                    Log.d("google",imageUrl);

                    Log.d("google",taskSnapshot.getUploadSessionUri().getPath());


//                    mFirebaseDatabaseReference.child("Drivers")
//                            .child(mFirebaseUser.getUid()).child("profileImage").setValue(imageUrl);
//                    loading.setVisibility(View.GONE);
//                    Toast.makeText(UpdateProfileActivity.this, "تم تعديل الصوره الشخصية", Toast.LENGTH_LONG).show();
                }
            });
        } else {
            //Handle possible errors
            Toast.makeText(this, pickResult.getError().getMessage(), Toast.LENGTH_LONG).show();

        }
    }



    /**
     * to create the random string
     *
     * @return random string
     */
    protected String random() {
        String SALTCHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ1234567890";
        StringBuilder salt = new StringBuilder();
        Random rnd = new Random();
        while (salt.length() < 18) {
            int index = (int) (rnd.nextFloat() * SALTCHARS.length());
            salt.append(SALTCHARS.charAt(index));
        }
        String saltStr = salt.toString();
        return saltStr;

    }

}
