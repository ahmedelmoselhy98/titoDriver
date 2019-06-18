package com.TitoApp.driver.activity;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.TitoApp.driver.R;
import com.TitoApp.driver.model.DriverModel;
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

public class UserPaperActivity extends AppCompatActivity implements IPickResult {


    //init the views
    @BindView(R.id.loading)
    ProgressBar loading;
    //user id
    @BindView(R.id.favorImage)
    ImageView favorImage;
    //user licence
    @BindView(R.id.favorImage1)
    ImageView favorImage1;


    @BindView(R.id.take_photo_profile1)
    ImageView take_photo_profile1;
    @BindView(R.id.take_photo_profile)
    ImageView take_photo_profile;


    // Firebase instance variables
    private FirebaseAuth mFirebaseAuth;
    private FirebaseUser mFirebaseUser;

    private DatabaseReference df;



    //init the firebase
    private DatabaseReference mFirebaseDatabaseReference;
    private StorageReference mStorageRef;

    //for upload the image
    Bitmap image, image1;



    private int x;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_paper);
        ButterKnife.bind(this);

        //init the storage
        mStorageRef = FirebaseStorage.getInstance().getReference();


        mFirebaseDatabaseReference = FirebaseDatabase.getInstance().getReference();

        //init the firebase
        mFirebaseAuth = FirebaseAuth.getInstance();
        mFirebaseUser = mFirebaseAuth.getCurrentUser();
        df = FirebaseDatabase.getInstance().getReference();

        df = FirebaseDatabase.getInstance().getReference().child("Drivers").child(mFirebaseUser.getUid());
        ValueEventListener postListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                DriverModel user = dataSnapshot.getValue(DriverModel.class);


                if (!UserPaperActivity.this.isDestroyed()){
                    Glide.with(UserPaperActivity.this).load(user.getIdImage()).into(favorImage);
                    Glide.with(UserPaperActivity.this).load(user.getLicenceImage()).into(favorImage1);
                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        };
        df.addValueEventListener(postListener);

    }




    /**
     * here to upload the images in the server
     */
    @OnClick(R.id.continueBtn)
    void uploadImages() {


        loading.setVisibility(View.VISIBLE);


        StorageReference tripsRef = mStorageRef.child("Drivers/IdImages/" + random() + ".png");
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        image.compress(Bitmap.CompressFormat.PNG, 100, baos);
        byte[] data = baos.toByteArray();

        UploadTask uploadTask = tripsRef.putBytes(data);
        uploadTask.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                // Handle unsuccessful uploads
                Toast.makeText(UserPaperActivity.this, "حدث خطأ عند رفع الصوره ", Toast.LENGTH_LONG).show();
                loading.setVisibility(View.GONE);
            }
        }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                // taskSnapshot.getMetadata() contains file metadata such as size, content-type, and download URL.
                //todo here the image url
                // taskSnapshot.getMetadata() contains file metadata such as size, content-type, and download URL.
                String imageUrl = taskSnapshot.getUploadSessionUri().toString();

                Log.d("google",imageUrl);

                Log.d("google",taskSnapshot.getUploadSessionUri().getPath());


//                String imageUrl = taskSnapshot.getDownloadUrl().toString();
//
//                mFirebaseDatabaseReference.child("Drivers")
//                        .child(mFirebaseUser.getUid()).child("idImage").setValue(imageUrl);
//
//                uploadLicence();
            }
        });


    }


    private void uploadLicence(){
        StorageReference tripsRef = mStorageRef.child("Drivers/LicencesImages/" + random() + ".png");
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        image1.compress(Bitmap.CompressFormat.PNG, 100, baos);
        byte[] data = baos.toByteArray();

        UploadTask uploadTask = tripsRef.putBytes(data);
        uploadTask.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                // Handle unsuccessful uploads
                Toast.makeText(UserPaperActivity.this, "حدث خطأ عند رفع الصوره ", Toast.LENGTH_LONG).show();
                loading.setVisibility(View.GONE);
            }
        }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                // taskSnapshot.getMetadata() contains file metadata such as size, content-type, and download URL.
                //todo here the image url
                // taskSnapshot.getMetadata() contains file metadata such as size, content-type, and download URL.
                String imageUrl = taskSnapshot.getUploadSessionUri().toString();

                Log.d("google",imageUrl);

                Log.d("google",taskSnapshot.getUploadSessionUri().getPath());


//                String imageUrl = taskSnapshot.getDownloadUrl().toString();
//
//                mFirebaseDatabaseReference.child("Drivers")
//                        .child(mFirebaseUser.getUid()).child("licenceImage").setValue(imageUrl);
//                loading.setVisibility(View.GONE);
//
//
//                Intent i = new Intent(UserPaperActivity.this, VerifyActivity.class);
//                i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
//                startActivity(i);

            }
        });
    }


    /**
     * here to select image from gallery or camera for id image
     */
    @OnClick(R.id.imageParent)
    void selectImage() {
        x = 1;
        PickImageDialog.build(new PickSetup()).show(UserPaperActivity.this);
    }


    /**
     * here to select image from gallery or camera for licence image
     */
    @OnClick(R.id.imageParent1)
    void selectImageLicence() {
        x = 2;
        PickImageDialog.build(new PickSetup()).show(UserPaperActivity.this);
    }


    /**
     * to select from gallery or camera
     *
     * @param pickResult the result from the gallery or camera
     */
    @Override
    public void onPickResult(PickResult pickResult) {
        if (pickResult.getError() == null) {
            //If you want the Uri.
            //Mandatory to refresh image from Uri.
            //getImageView().setImageURI(null);

            //Setting the real returned image.
            //getImageView().setImageURI(r.getUri());

            //If you want the Bitmap.
            if (x == 1) {
                favorImage.setImageBitmap(pickResult.getBitmap());
                image = pickResult.getBitmap();
                take_photo_profile.setVisibility(View.GONE);
            } else if (x == 2) {
                favorImage1.setImageBitmap(pickResult.getBitmap());
                image1 = pickResult.getBitmap();
                take_photo_profile1.setVisibility(View.GONE);
            }


            //Image path
            //r.getPath();
        } else {
            //Handle possible errors
            //TODO: do what you have to do with r.getError();
            Toast.makeText(this, pickResult.getError().getMessage(), Toast.LENGTH_LONG).show();
        }
    }


    /**
     * to get ids for the firebase
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


    /**
     * press the back button
     */
    @OnClick(R.id.back_image)
    void back__() {
        finish();
    }
}
