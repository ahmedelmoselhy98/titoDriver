package com.TitoApp.driver.activity;

import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.TitoApp.driver.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class ForgotPasswordActivity extends AppCompatActivity {

    //init the views

    @BindView(R.id.emailET)EditText emailET;
    @BindView(R.id.loading)
    ProgressBar loading;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password);
        ButterKnife.bind(this);
    }


    @OnClick(R.id.signInBtn)void login_(){

        if (!validate()){
            Toast.makeText(ForgotPasswordActivity.this,"من فضلك قم بملئ جميع البيانات",Toast.LENGTH_LONG).show();
            return;
        }


        loading.setVisibility(View.VISIBLE);

        forgotPass();

    }


    /**
     * here to login if he is register
     */
    private void forgotPass(){
        FirebaseAuth.getInstance().sendPasswordResetEmail(emailET.getText().toString())
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Log.d("google", "Email sent.");

                            loading.setVisibility(View.GONE);

                            //todo here to add the dialog

                            new AlertDialog.Builder(ForgotPasswordActivity.this)
                                    .setTitle("تم ارسال ايميل ")
                                    .setMessage("برجاء التوجه الي البريد الالكتروني لتغير كلمه المرور")

                                    // Specifying a listener allows you to take an action before dismissing the dialog.
                                    // The dialog is automatically dismissed when a dialog button is clicked.
                                    .setPositiveButton("نعم", new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int which) {
                                            // Continue with delete operation
                                            finish();
                                        }
                                    })

                                    // A null listener allows the button to dismiss the dialog and take no further action.
                                    .setNegativeButton("الغاء", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            finish();
                                        }
                                    })
                                    .setIcon(R.drawable.looogooo)
                                    .show();


                        }
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                loading.setVisibility(View.GONE);
                new AlertDialog.Builder(ForgotPasswordActivity.this)
                        .setTitle("لم يتم ارسال ")
                        .setMessage("برجاء التاكد من البريد الالكتروني لا يوجد سائق مسجل بهذا البريد")

                        // Specifying a listener allows you to take an action before dismissing the dialog.
                        // The dialog is automatically dismissed when a dialog button is clicked.
                        .setPositiveButton("نعم", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                // Continue with delete operation

                            }
                        })

                        // A null listener allows the button to dismiss the dialog and take no further action.
                        .setNegativeButton("الغاء", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                            }
                        })
                        .setIcon(R.drawable.looogooo)
                        .show();
            }
        });

    }

    /**
     * to validate the input from the user
     * @return true if all data entered false otherwise
     */
    private boolean validate(){
        return !(emailET.getText().toString().isEmpty());
    }
}
