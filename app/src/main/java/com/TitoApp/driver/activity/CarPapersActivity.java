package com.TitoApp.driver.activity;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.TitoApp.driver.R;
import com.TitoApp.driver.model.CarModel;
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

public class CarPapersActivity extends AppCompatActivity implements IPickResult {


    @BindView(R.id.favorImage)
    ImageView favorImage;
    @BindView(R.id.take_photo_profile)
    ImageView take_photo_profile;
    @BindView(R.id.colorET)
    EditText colorET;
    @BindView(R.id.typeET)
    EditText typeET;
    @BindView(R.id.modelET)
    EditText modelET;
    @BindView(R.id.yearET)
    EditText yearET;
    @BindView(R.id.numberET)
    EditText numberET;
    @BindView(R.id.loading)
    ProgressBar loading;


    //init the firebase
    private DatabaseReference mFirebaseDatabaseReference,df;
    private StorageReference mStorageRef;

    //for upload the image
    Bitmap image;


    //firebase
    private FirebaseAuth mAuth;
    private FirebaseUser mFirebaseUser;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_car_papers);
        ButterKnife.bind(this);


        mFirebaseDatabaseReference = FirebaseDatabase.getInstance().getReference();

        //init the storage
        mStorageRef = FirebaseStorage.getInstance().getReference();

        //firebase intial
        mAuth = FirebaseAuth.getInstance();
        mFirebaseUser = mAuth.getCurrentUser();



        df = FirebaseDatabase.getInstance().getReference().child("Cars").child(mFirebaseUser.getUid());
        ValueEventListener postListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                CarModel car = dataSnapshot.getValue(CarModel.class);

                if (car==null)
                    return;

                if (car.getCarLicenceImge().isEmpty())
                    favorImage.setImageDrawable(getResources().getDrawable(R.drawable.camera_party_mode));
                else {
                    if (!CarPapersActivity.this.isDestroyed())
                        Glide.with(CarPapersActivity.this).load(car.getCarLicenceImge()).into(favorImage);
                }
                colorET.setText(car.getColor());
                typeET.setText(car.getCarType());
                modelET.setText(car.getCarModel());
                yearET.setText(car.getCarYear());
                numberET.setText(car.getCarNumber());


            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        };
        df.addValueEventListener(postListener);


    }


    /**
     * here to select image from gallery or camera
     */
    @OnClick(R.id.imageParent)
    void selectImage() {
        PickImageDialog.build(new PickSetup()).show(CarPapersActivity.this);
    }


    /**
     * to register
     */

    @OnClick(R.id.carPaperBtn) void signUp() {

        if (!validate()) {
            Toast.makeText(CarPapersActivity.this, "من فضبلك تاكد من ادخال كل البيانات صحيحه", Toast.LENGTH_LONG).show();
            return;
        }


        if (image == null) {
            Toast.makeText(CarPapersActivity.this, "برجاء اختيار صورة ", Toast.LENGTH_LONG).show();
            return;
        }


        loading.setVisibility(View.VISIBLE);

        mFirebaseUser = mAuth.getCurrentUser();


        StorageReference tripsRef = mStorageRef.child("Cars/" + random() + ".png");
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        image.compress(Bitmap.CompressFormat.PNG, 100, baos);
        byte[] data = baos.toByteArray();

        UploadTask uploadTask = tripsRef.putBytes(data);
        uploadTask.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                // Handle unsuccessful uploads
                Toast.makeText(CarPapersActivity.this, "حدث خطأ عند رفع الصوره ", Toast.LENGTH_LONG).show();
                loading.setVisibility(View.GONE);
            }
        }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                //todo here the image url
                // taskSnapshot.getMetadata() contains file metadata such as size, content-type, and download URL.
                String imageUrl = taskSnapshot.getUploadSessionUri().toString();

                Log.d("google",imageUrl);

                Log.d("google",taskSnapshot.getUploadSessionUri().getPath());

                addNewCar(mFirebaseUser.getUid(), imageUrl);


            }
        });


    }

    /**
     * here to add the data of the com.TitoApp.com.TitoApp.driver to the database after uploaded the image to the storage
     */
    private void addNewCar(String userId, String image) {


        //set the criminal data;
        CarModel car = new CarModel();

        car.setId(userId);
        car.setUserId(userId);
        car.setCarLicenceImge(image);
        car.setColor(colorET.getText().toString());
        car.setCarType(typeET.getText().toString());
        car.setCarModel(modelET.getText().toString());
        car.setCarYear(yearET.getText().toString());
        car.setCarNumber(numberET.getText().toString());


        mFirebaseDatabaseReference.child("Cars")
                .child(car.getId()).setValue(car);

        loading.setVisibility(View.GONE);

        Toast.makeText(CarPapersActivity.this, "تمت اضافة السيارة", Toast.LENGTH_LONG).show();

        finish();

    }


    /**
     * check if the user enter all data or not
     *
     * @return true if all data is entered false otherwise
     */
    private boolean validate() {
        return !(colorET.getText().toString().isEmpty() || typeET.getText().toString().isEmpty() ||
                modelET.getText().toString().isEmpty() || yearET.getText().toString().isEmpty() ||
                numberET.getText().toString().isEmpty());
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


    /**
     * press the back button
     */
    @OnClick(R.id.back_image)
    void back__() {
        finish();
    }

}
