package com.TitoApp.driver.activity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.TitoApp.driver.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;


import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class LoginActivity extends AppCompatActivity {

    //init the views

    @BindView(R.id.emailET)EditText emailET;
    @BindView(R.id.passET)EditText passET;
    @BindView(R.id.loading)
    ProgressBar loading;



    //firebase
    private FirebaseAuth mAuth;
    private FirebaseUser mFirebaseUser;
    private DatabaseReference mFirebaseDatabaseReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        ButterKnife.bind(this);

        mFirebaseDatabaseReference = FirebaseDatabase.getInstance().getReference();


        //firebase intial
        mAuth = FirebaseAuth.getInstance();
    }


    @OnClick(R.id.signInBtn)void login_(){

        if (!validate()){
            Toast.makeText(LoginActivity.this,"من فضلك قم بملئ جميع البيانات",Toast.LENGTH_LONG).show();
            return;
        }


        loading.setVisibility(View.VISIBLE);

        login();

    }


    /**
     * here to login if he is register
     */
    private void login(){

        mAuth.signInWithEmailAndPassword(emailET.getText().toString(), passET.getText().toString())
                .addOnCompleteListener(LoginActivity.this, new OnCompleteListener<AuthResult>() {
                    @SuppressLint("WrongConstant")
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (!task.isSuccessful()) {
                            loading.setVisibility(View.GONE);
                            Toast.makeText(getApplicationContext(), "لا يمكن الدخول الرجاء التاكد من البيانات", Toast.LENGTH_LONG)
                                    .show();


                        } else {

                            DatabaseReference ref =   mFirebaseDatabaseReference.child("Tokens");

                            String token = FirebaseInstanceId.getInstance().getToken();
                            ref.child(task.getResult().getUser().getUid()).child("token").setValue(token);

                            loading.setVisibility(View.GONE);
                            Intent i = new Intent(LoginActivity.this, MapsActivity.class);
                            i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            startActivity(i);
                        }

                    }
                });
    }

    /**
     * to go to the register screen
     */
    @OnClick(R.id.signUpBtn)void register(){
        startActivity(new Intent(LoginActivity.this,RegisterActivity.class));
    }

    /**
     * to validate the input from the user
     * @return true if all data entered false otherwise
     */
    private boolean validate(){
        return !(emailET.getText().toString().isEmpty() || passET.getText().toString().isEmpty());
    }
}
