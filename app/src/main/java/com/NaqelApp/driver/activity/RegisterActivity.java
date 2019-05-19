package com.NaqelApp.driver.activity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.NaqelApp.driver.R;
import com.NaqelApp.driver.model.DriverModel;
import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.vansuita.pickimage.bean.PickResult;
import com.vansuita.pickimage.bundle.PickSetup;
import com.vansuita.pickimage.dialog.PickImageDialog;
import com.vansuita.pickimage.listeners.IPickResult;

import org.json.JSONException;

import java.io.ByteArrayOutputStream;
import java.util.Random;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class RegisterActivity extends AppCompatActivity implements IPickResult {


    //init the views
    @BindView(R.id.fnameET)
    EditText fnameET;
    @BindView(R.id.lnameET)
    EditText lnameET;
    @BindView(R.id.emailET)
    EditText emailET;
    @BindView(R.id.passET)
    EditText passET;
    @BindView(R.id.phoneET)
    EditText phoneET;
    @BindView(R.id.addressET)
    EditText addressET;


    @BindView(R.id.loading)
    ProgressBar loading;
    @BindView(R.id.take_photo_profile)
    ImageView take_photo_profile;
    @BindView(R.id.favorImage)
    ImageView favorImage;


    //init the firebase
    private DatabaseReference mFirebaseDatabaseReference;
    private StorageReference mStorageRef;

    //for upload the image
    Bitmap image;


    //firebase
    private FirebaseAuth mAuth;
    private FirebaseUser mFirebaseUser;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        ButterKnife.bind(this);


        mFirebaseDatabaseReference = FirebaseDatabase.getInstance().getReference();

        //init the storage
        mStorageRef = FirebaseStorage.getInstance().getReference();

        //firebase intial
        mAuth = FirebaseAuth.getInstance();


    }


    /**
     * here to select image from gallery or camera
     */
    @OnClick(R.id.imageParent)
    void selectImage() {
        PickImageDialog.build(new PickSetup()).show(RegisterActivity.this);
    }


    /**
     * to register
     */

    @OnClick(R.id.signUpBtn)
    void signUp() {

        if (!validate()) {
            Toast.makeText(RegisterActivity.this, "من فضبلك تاكد من ادخال كل البيانات صحيحه", Toast.LENGTH_LONG).show();
            return;
        }


        if (image == null) {
            Toast.makeText(RegisterActivity.this, "برجاء اختيار صورة ", Toast.LENGTH_LONG).show();
            return;
        }


        loading.setVisibility(View.VISIBLE);


        mAuth.createUserWithEmailAndPassword(emailET.getText().toString(), passET.getText().toString())
                .addOnCompleteListener(RegisterActivity.this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {

                        if (!task.isSuccessful()) {
//                            Toast.makeText(LoginActivity
//                                            .this, "Can't Create account",
//                                    Toast.LENGTH_SHORT).show();
                            loading.setVisibility(View.GONE);


                            Log.d("google","error ---     "+task.getException().getMessage());

                            Toast.makeText(getApplicationContext(), "لا يمكن التسجيل الرجاء التاكد من البيانات", Toast.LENGTH_LONG)
                                    .show();
                            //go to the login
//                            login();
                        } else {
//                            loading.setVisibility(View.GONE);
                            mFirebaseUser = mAuth.getCurrentUser();


                            final StorageReference tripsRef = mStorageRef.child("Drivers/" + random() + ".png");
                            ByteArrayOutputStream baos = new ByteArrayOutputStream();
                            image.compress(Bitmap.CompressFormat.PNG, 100, baos);
                            byte[] data = baos.toByteArray();

                            final UploadTask uploadTask = tripsRef.putBytes(data);
                            uploadTask.addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception exception) {
                                    // Handle unsuccessful uploads
                                    Toast.makeText(RegisterActivity.this, "حدث خطأ عند رفع الصوره ", Toast.LENGTH_LONG).show();
                                    loading.setVisibility(View.GONE);
                                }
                            }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                                @Override
                                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                    // taskSnapshot.getMetadata() contains file metadata such as size, content-type, and download URL.

                                    //todo to get the downloaded url
                                    Task<Uri> urlTask = uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                                        @Override
                                        public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                                            if (!task.isSuccessful()) {
                                                Toast.makeText(RegisterActivity.this, "حدث خطأ عند رفع الصوره ", Toast.LENGTH_LONG).show();
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

                                                try {
                                                    addNewDriver(mFirebaseUser.getUid(), downloadUri+"");
                                                } catch (JSONException e) {
                                                    e.printStackTrace();
                                                    Toast.makeText(RegisterActivity.this, "حدث خطأ عند رفع الصوره ", Toast.LENGTH_LONG).show();
                                                    loading.setVisibility(View.GONE);
                                                }

                                            } else {
                                                // Handle failures
                                                // ...
                                                Toast.makeText(RegisterActivity.this, "حدث خطأ عند رفع الصوره ", Toast.LENGTH_LONG).show();
                                                loading.setVisibility(View.GONE);
                                            }
                                        }
                                    });
                                }
                            });


                        }

                    }
                });


    }

    /**
     * to back to the sign in screen
     */
    @OnClick(R.id.signInBtn)
    void signin() {
        finish();
    }


    /**
     * here to add the data of the com.NaqelApp.com.NaqelApp.driver to the database after uploaded the image to the storage
     */
    private void addNewDriver(String userId, String image) throws JSONException {


        //set the criminal data;
        DriverModel driver = new DriverModel();

        driver.setId(userId);
        driver.setFname(fnameET.getText().toString());
        driver.setLname(lnameET.getText().toString());
        driver.setEmail(emailET.getText().toString());
        driver.setPhone(phoneET.getText().toString());
        driver.setAddress(addressET.getText().toString());
        driver.setStatus("0");
        driver.setProfileImage(image);




        // here to add the geofire and add the data to the firebase


//                        GeoFire geoFire = new GeoFire(FirebaseDatabase.getInstance().getReferenceFromUrl(GEO_FIRE_REF));
        GeoFire geoFire = new GeoFire(FirebaseDatabase.getInstance().getReference().child("GeoFire"));

        geoFire.setLocation(userId, new GeoLocation(30.543338954230222, 31.138000488281254), new GeoFire.CompletionListener() {
            @Override
            public void onComplete(String key, DatabaseError error) {
//                                Log.d("google",error.toString());
            }
        });

        mFirebaseDatabaseReference.child("Drivers")
                .child(driver.getId()).setValue(driver);


        DatabaseReference ref = FirebaseDatabase.getInstance().getReference().child("Tokens");

        String token = FirebaseInstanceId.getInstance().getToken();

        ref.child(userId).child("token").setValue(token);


        loading.setVisibility(View.GONE);


        Intent i = new Intent(RegisterActivity.this, PaperActivity.class);
        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(i);

    }


    /**
     * check if the user enter all data or not
     *
     * @return true if all data is entered false otherwise
     */
    private boolean validate() {
        return !(fnameET.getText().toString().isEmpty() || lnameET.getText().toString().isEmpty() ||
                emailET.getText().toString().isEmpty() || passET.getText().toString().isEmpty() ||
                phoneET.getText().toString().isEmpty() || addressET.getText().toString().isEmpty());
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
            favorImage.setImageBitmap(pickResult.getBitmap());
            image = pickResult.getBitmap();
            take_photo_profile.setVisibility(View.GONE);

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
