package com.example.skincare.authentification;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatTextView;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.example.skincare.R;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;

public class ForgotPasswordActivity extends AppCompatActivity {

    private AppCompatTextView textViewLinkLogin;
    private TextInputEditText email;
    private Button send;

    private FirebaseAuth fAuth;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password);


        email = findViewById(R.id.F_EditEmail);
        send = findViewById(R.id.F_Button);
        fAuth = FirebaseAuth.getInstance();

        textViewLinkLogin = (AppCompatTextView) findViewById(R.id.F_L_Button);

        textViewLinkLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getApplicationContext(), LoginActivity.class));
            }
        });

        send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String emailT = email.getText().toString().trim();
                fAuth.sendPasswordResetEmail(emailT).addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Toast.makeText(ForgotPasswordActivity.this,"Reset email has been sent",Toast.LENGTH_LONG).show();

                    }
                });
                fAuth.sendPasswordResetEmail(emailT).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(ForgotPasswordActivity.this,"Emailprovided not found, "+e.getMessage(),Toast.LENGTH_LONG).show();

                    }
                });
            }
        });

    }
}
