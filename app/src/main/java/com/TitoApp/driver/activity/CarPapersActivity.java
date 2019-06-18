package com.TitoApp.driver.activity;

import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.TitoApp.driver.R;
import com.TitoApp.driver.adapter.CategAdapter;
import com.TitoApp.driver.model.CarModel;
import com.TitoApp.driver.model.CategLocal;
import com.TitoApp.driver.view.BottomDialog;
import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
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
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.vansuita.pickimage.bean.PickResult;
import com.vansuita.pickimage.bundle.PickSetup;
import com.vansuita.pickimage.dialog.PickImageDialog;
import com.vansuita.pickimage.listeners.IPickResult;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
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


    //todo here is the category
    @BindView(R.id.categTV)
    TextView categoryTV;
    @BindView(R.id.categIV)
    ImageView categoryIV;
    String categId = "-L_wEOtpCYemDnlzh7Ys";

    ArrayList<CategLocal> data = new ArrayList<>();



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
        EventBus.getDefault().register(this);


        mFirebaseDatabaseReference = FirebaseDatabase.getInstance().getReference();

        //init the storage
        mStorageRef = FirebaseStorage.getInstance().getReference();

        //firebase intial
        mAuth = FirebaseAuth.getInstance();
        mFirebaseUser = mAuth.getCurrentUser();


        data = new ArrayList<>();
        data.add(new CategLocal(R.drawable.car0, "تيتو", "تيتو", "-L_wEOtpCYemDnlzh7Ys"));
        data.add(new CategLocal(R.drawable.car1, "تيتو بلس", "تيتو بلس", "-L_wEW_Y_YFhvEJgcccC"));



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

                for (int i = 0;i<data.size();i++){
                    if (car.getCategoryId().equals(data.get(i).getId())){
                        categoryTV.setText(data.get(i).getTitle());
                        categoryIV.setImageResource(data.get(i).getImage());
                        categId = data.get(i).getId();
                    }
                }


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


        final StorageReference tripsRef = mStorageRef.child("Cars/" + random() + ".png");
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        image.compress(Bitmap.CompressFormat.PNG, 100, baos);
        byte[] data = baos.toByteArray();

        final UploadTask uploadTask = tripsRef.putBytes(data);
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


                // to get the downloaded url
                Task<Uri> urlTask = uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                    @Override
                    public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                        if (!task.isSuccessful()) {
                            Toast.makeText(CarPapersActivity.this, "حدث خطأ عند رفع الصوره ", Toast.LENGTH_LONG).show();
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


                            addNewCar(mFirebaseUser.getUid(), downloadUri+"");

                        } else {
                            // Handle failures
                            // ...
                            Toast.makeText(CarPapersActivity.this, "حدث خطأ عند رفع الصوره ", Toast.LENGTH_LONG).show();
                            loading.setVisibility(View.GONE);
                        }
                    }
                });


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
        car.setCategoryId(categId);


        //todo here to add the category id



//        mFirebaseDatabaseReference.child("Cars")
//                .child(car.getId()).setValue(car);

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



    //todo here is the category select
    BottomDialog bottomDialog;

    @OnClick(R.id.selectcategCard)
    void selectcategoryAction() {
        selectcateg();
    }

    private void selectcateg() {
        LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View customView = inflater.inflate(R.layout.post_detail, null);




        final CategAdapter adapter = new CategAdapter(data, CarPapersActivity.this, categId);


        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(CarPapersActivity.this,
                LinearLayoutManager.VERTICAL, false);

        RecyclerView recyclerView = customView.findViewById(R.id.categRV);

        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(layoutManager);


        bottomDialog = new BottomDialog.Builder(CarPapersActivity.this)
                .setCustomView(customView)
                .show();


    }

    //todo here the event bus


    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(CategLocal event) {
        bottomDialog.dismiss();

        categoryTV.setText(event.getTitle());
        categoryIV.setImageResource(event.getImage());
        categId = event.getId();

    }
}
