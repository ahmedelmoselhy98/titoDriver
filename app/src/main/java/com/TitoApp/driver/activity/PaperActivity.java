package com.TitoApp.driver.activity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.TitoApp.driver.R;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
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

public class PaperActivity extends AppCompatActivity implements IPickResult {

    //init the views
    @BindView(R.id.loading)
    ProgressBar loading;
    @BindView(R.id.take_photo_profile)
    ImageView take_photo_profile;
    @BindView(R.id.favorImage)
    ImageView favorImage;

    @BindView(R.id.take_photo_profile1)
    ImageView take_photo_profile1;
    @BindView(R.id.favorImage1)
    ImageView favorImage1;


    //init the firebase
    private DatabaseReference mFirebaseDatabaseReference;
    private StorageReference mStorageRef;

    //for upload the image
    Bitmap image, image1;


    // Firebase instance variables
    private FirebaseAuth mFirebaseAuth;
    private FirebaseUser mFirebaseUser;


    private int x;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_paper);
        ButterKnife.bind(this);

        //init the storage
        mStorageRef = FirebaseStorage.getInstance().getReference();


        mFirebaseAuth = FirebaseAuth.getInstance();
        mFirebaseUser = mFirebaseAuth.getCurrentUser();

        mFirebaseDatabaseReference = FirebaseDatabase.getInstance().getReference();


    }

    /**
     * here to upload the images in the server
     */
    @OnClick(R.id.continueBtn)
    void uploadImages() {


        loading.setVisibility(View.VISIBLE);


        final StorageReference tripsRef = mStorageRef.child("Drivers/IdImages/" + random() + ".png");
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        image.compress(Bitmap.CompressFormat.PNG, 100, baos);
        byte[] data = baos.toByteArray();

        final UploadTask uploadTask = tripsRef.putBytes(data);
        uploadTask.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                // Handle unsuccessful uploads
                Toast.makeText(PaperActivity.this, "حدث خطأ عند رفع الصوره ", Toast.LENGTH_LONG).show();
                loading.setVisibility(View.GONE);
            }
        }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                // taskSnapshot.getMetadata() contains file metadata such as size, content-type, and download URL.
//                String imageUrl = taskSnapshot.getDownloadUrl().toString();
                // to get the downloaded url
                Task<Uri> urlTask = uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                    @Override
                    public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                        if (!task.isSuccessful()) {
                            Toast.makeText(PaperActivity.this, "حدث خطأ عند رفع الصوره ", Toast.LENGTH_LONG).show();
                            loading.setVisibility(View.GONE);
                            throw task.getException();

                        }

                        // Continue with the task to get the download URL
                        return tripsRef.getDownloadUrl();
                    }
                }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                    @Override
                    public void onComplete(@NonNull Task<Uri> task) {
                        if (task.isSuccessful()) {
                            Uri downloadUri = task.getResult();
                            Log.d("google", "this is the downloaded url      " + downloadUri);

                            mFirebaseDatabaseReference.child("Drivers")
                                    .child(mFirebaseUser.getUid()).child("idImage").setValue(downloadUri + "");
                            uploadLicence();

                        } else {
                            // Handle failures
                            // ...
                            Toast.makeText(PaperActivity.this, "حدث خطأ عند رفع الصوره ", Toast.LENGTH_LONG).show();
                            loading.setVisibility(View.GONE);
                        }
                    }
                });

//                mFirebaseDatabaseReference.child("Drivers")
//                        .child(mFirebaseUser.getUid()).child("idImage").setValue(imageUrl);
//
//                uploadLicence();
            }
        });


    }


    private void uploadLicence() {
        final StorageReference tripsRef = mStorageRef.child("Drivers/LicencesImages/" + random() + ".png");
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        image1.compress(Bitmap.CompressFormat.PNG, 100, baos);
        byte[] data = baos.toByteArray();

        final UploadTask uploadTask = tripsRef.putBytes(data);
        uploadTask.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                // Handle unsuccessful uploads
                Toast.makeText(PaperActivity.this, "حدث خطأ عند رفع الصوره ", Toast.LENGTH_LONG).show();
                loading.setVisibility(View.GONE);
            }
        }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                // taskSnapshot.getMetadata() contains file metadata such as size, content-type, and download URL.
//                String imageUrl = taskSnapshot.getDownloadUrl().toString();

                // to get the downloaded url
                Task<Uri> urlTask = uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                    @Override
                    public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                        if (!task.isSuccessful()) {
                            Toast.makeText(PaperActivity.this, "حدث خطأ عند رفع الصوره ", Toast.LENGTH_LONG).show();
                            loading.setVisibility(View.GONE);
                            throw task.getException();

                        }

                        // Continue with the task to get the download URL
                        return tripsRef.getDownloadUrl();
                    }
                }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                    @Override
                    public void onComplete(@NonNull Task<Uri> task) {
                        if (task.isSuccessful()) {
                            Uri downloadUri = task.getResult();
                            Log.d("google", "this is the downloaded url      " + downloadUri);

                            mFirebaseDatabaseReference.child("Drivers")
                                    .child(mFirebaseUser.getUid()).child("licenceImage").setValue(downloadUri+"");
                            loading.setVisibility(View.GONE);





                            Intent i = new Intent(PaperActivity.this, CarPapersActivity.class);
                            i.putExtra("data","1");
                            i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            startActivity(i);





                        } else {
                            // Handle failures
                            // ...
                            Toast.makeText(PaperActivity.this, "حدث خطأ عند رفع الصوره ", Toast.LENGTH_LONG).show();
                            loading.setVisibility(View.GONE);
                        }
                    }
                });


//                mFirebaseDatabaseReference.child("Drivers")
//                        .child(mFirebaseUser.getUid()).child("licenceImage").setValue(imageUrl);
//                loading.setVisibility(View.GONE);
//
//
//                Intent i = new Intent(PaperActivity.this, VerifyActivity.class);
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
        PickImageDialog.build(new PickSetup()).show(PaperActivity.this);
    }


    /**
     * here to select image from gallery or camera for licence image
     */
    @OnClick(R.id.imageParent1)
    void selectImageLicence() {
        x = 2;
        PickImageDialog.build(new PickSetup()).show(PaperActivity.this);
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
            //: do what you have to do with r.getError();
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


}
