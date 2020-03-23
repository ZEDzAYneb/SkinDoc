package com.example.skincare;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatTextView;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;


import java.util.HashMap;
import java.util.Map;

public class SignupActivity extends AppCompatActivity{

    private AppCompatTextView textViewLinkLogin;
    private TextInputEditText password;
    private TextInputEditText email;
    private TextInputEditText fullname;
    private TextInputEditText confirmepassword;
    private Button signin;
    private ProgressBar bar;
    private FirebaseAuth fAuth;
    private FirebaseFirestore fStore;
    private String userID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);


        password = findViewById(R.id.S_EditPassword);
        fullname = findViewById(R.id.S_EditName);
        confirmepassword = findViewById(R.id.S_EditConfirmePassword);
        email = findViewById(R.id.S_EditEmail);
        bar = findViewById(R.id.S_Bar);
        signin = findViewById(R.id.S_Button);
        fAuth = FirebaseAuth.getInstance();
        fStore = FirebaseFirestore.getInstance();
        textViewLinkLogin = (AppCompatTextView) findViewById(R.id.S_L_Button);

        if(fAuth.getCurrentUser() != null){
            startActivity(new Intent(getApplicationContext(), MainActivity.class));
            finish();
        }

        textViewLinkLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getApplicationContext(),LoginActivity.class));
            }
        });


        signin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String fullnameT = fullname.getText().toString().trim();
                final String emailT = email.getText().toString().trim();
                final String passwordT = password.getText().toString().trim();
                String confirmeT = confirmepassword.getText().toString().trim();

                if(TextUtils.isEmpty(fullnameT)){
                    fullname.setError("Fullname required");
                    return;
                }

                if(TextUtils.isEmpty(emailT)){
                    email.setError("Email required");
                    return;
                }

                if(TextUtils.isEmpty(passwordT)){
                    password.setError("Password required");
                    return;
                }

                if(passwordT.length() < 6){
                    password.setError("Password length must be more than 6");
                    return;
                }

                if(TextUtils.isEmpty(confirmeT)){
                    confirmepassword.setError("Confirming password required");
                    return;
                }
                if(!passwordT.equals(confirmeT)){
                    confirmepassword.setError("password does not match");
                    return;
                }

                bar.setVisibility(View.VISIBLE);

                fAuth.createUserWithEmailAndPassword(emailT,passwordT).addOnCompleteListener(new OnCompleteListener<AuthResult>(){

                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if(task.isSuccessful()){
                            Toast.makeText(SignupActivity.this,"Account created successfuly",Toast.LENGTH_LONG).show();
                            userID = fAuth.getCurrentUser().getUid();
                            DocumentReference documentReference = fStore.collection("profile").document(userID);
                            Map<String,Object> user = new HashMap<>();
                            user.put("fullname",fullnameT);
                            user.put("email",emailT);
                            documentReference.set(user).addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    Log.d("TAG","Profile created successfuly for user: "+userID );
                                }
                            });
                            documentReference.set(user).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Log.d("TAG","Failed creating profile for user: "+userID );
                                }
                            });
                            startActivity(new Intent(getApplicationContext(),LoginActivity.class));
                        }else{
                                Toast.makeText(SignupActivity.this,"Error: "+ task.getException().getMessage(),Toast.LENGTH_LONG).show();

                            bar.setVisibility(View.GONE);
                        }
                    }
                });
            }
        });
    }
}
