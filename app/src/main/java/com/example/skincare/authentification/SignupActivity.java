package com.example.skincare.authentification;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatTextView;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.example.skincare.MainActivity;
import com.example.skincare.R;
import com.example.skincare.adapter.UserDbhelper;
import com.example.skincare.models.Users;
import com.google.android.gms.tasks.OnCompleteListener;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

public class SignupActivity extends AppCompatActivity{

    private AppCompatTextView textViewLinkLogin;
    private TextInputEditText password;
    private TextInputEditText email;
    private TextInputEditText fullname;
    private TextInputEditText confirmepassword;
    private TextInputEditText birthday;
    private TextInputLayout birth;
    private RadioGroup radioSexGroup;
    private RadioButton radioSexButton;
    private Button signin;
    private ProgressBar bar;
    private FirebaseAuth fAuth;
    private FirebaseFirestore fStore;
    private String userID;
    private UserDbhelper userDbhelper;

    private static final String TAG = "birthday";
    private DatePickerDialog.OnDateSetListener mDateSetListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);


        password = findViewById(R.id.S_EditPassword);
        fullname = findViewById(R.id.S_EditName);
        confirmepassword = findViewById(R.id.S_EditConfirmePassword);
        email = findViewById(R.id.S_EditEmail);
        birthday = findViewById(R.id.S_EditBirth);
        birth = findViewById(R.id.S_birth);
        bar = findViewById(R.id.S_Bar);
        signin = findViewById(R.id.S_Button);
        radioSexGroup = findViewById(R.id.S_sex);

        userDbhelper = new UserDbhelper(getApplicationContext());
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
                startActivity(new Intent(getApplicationContext(), LoginActivity.class));
            }
        });

        birthday.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Calendar cal = Calendar.getInstance();
                int year = cal.get(Calendar.YEAR);
                int month = cal.get(Calendar.MONTH);
                int day = cal.get(Calendar.DAY_OF_MONTH);

                DatePickerDialog dialog = new DatePickerDialog(
                        SignupActivity.this,
                        android.R.style.Theme_Holo_Light_Dialog_MinWidth,
                        mDateSetListener,
                        year,month,day);
                dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                dialog.show();
            }
        });

        mDateSetListener = new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker datePicker, int year, int month, int day) {
                month = month + 1;
                Log.d(TAG, "onDateSet: mm/dd/yyy: " + month + "/" + day + "/" + year);
                String date = month + "/" + day + "/" + year;
                birthday.setText(date);
            }
        };

        signin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String fullnameT = fullname.getText().toString().trim();
                final String emailT = email.getText().toString().trim();
                final String passwordT = password.getText().toString().trim();
                final String confirmeT = confirmepassword.getText().toString().trim();
                final String birthdayT = birthday.getText().toString().trim();

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
                if(TextUtils.isEmpty(birthdayT)){
                    birthday.setError("Birthday required");
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

                int selectedId = radioSexGroup.getCheckedRadioButtonId();
                // find the radiobutton by returned id
                radioSexButton = (RadioButton) findViewById(selectedId);
                final String sexT = radioSexButton.getText().toString().trim();



                bar.setVisibility(View.VISIBLE);

                fAuth.createUserWithEmailAndPassword(emailT,passwordT).addOnCompleteListener(new OnCompleteListener<AuthResult>(){

                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if(task.isSuccessful()) {

                            FirebaseUser fuser = fAuth.getCurrentUser();
                            fuser.sendEmailVerification().addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    Toast.makeText(SignupActivity.this,"Account created successfuly, Email verification has been sent",Toast.LENGTH_LONG).show();
                                }
                            }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Log.d("TAG","Failed: "+e.getMessage() );
                                }
                            });

                            userID = fAuth.getCurrentUser().getUid();
                            DocumentReference documentReference = fStore.collection("profile").document(userID);
                            Map<String,Object> user = new HashMap<>();
                            user.put("fullname",fullnameT);
                            user.put("email",emailT);
                            user.put("password",passwordT);
                            user.put("birthday",birthdayT);
                            user.put("sex",sexT);
                            documentReference.set(user).addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    Log.d("TAG","Profile created successfuly for user: "+userID );

                                    Users adduser =new Users(userID,fullnameT,emailT,passwordT,birthdayT,sexT);
                                    userDbhelper.addUser(adduser);
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
