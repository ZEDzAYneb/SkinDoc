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

import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FacebookAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class LoginActivity extends AppCompatActivity {

    static final int GOOGLE_SIGN_IN = 123;
    private static final String EMAIL = "email";


    private AppCompatTextView textViewLinkRegister;
    private AppCompatTextView textViewLinkForgot;
    private TextInputEditText password;
    private TextInputEditText email;
    private Button login;
    private ProgressBar bar;
    private FirebaseAuth fAuth;
    private FirebaseFirestore fStore;
    private String userID;
    private GoogleSignInClient signInClient;
    private SignInButton google_button;
    private LoginButton facebook_button;
    private CallbackManager callbackManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        password = findViewById(R.id.L_EditPassword);
        email = findViewById(R.id.L_EditEmail);
        bar = findViewById(R.id.L_Bar);
        login = findViewById(R.id.L_Button);
        fAuth = FirebaseAuth.getInstance();
        fStore = FirebaseFirestore.getInstance();

        textViewLinkRegister = (AppCompatTextView) findViewById(R.id.L_S_Button);
        textViewLinkForgot = (AppCompatTextView) findViewById(R.id.L_Forgot_Button);

        google_button = findViewById(R.id.Google);
        facebook_button = findViewById(R.id.Facebook);


        if(fAuth.getCurrentUser() != null){
            startActivity(new Intent(getApplicationContext(), MainActivity.class));
            finish();
        }

        // link to regestration page ***************************************************************
        textViewLinkRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getApplicationContext(),SignupActivity.class));
            }
        });

        // link to forgot password page ************************************************************
        textViewLinkForgot.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getApplicationContext(),ForgotPasswordActivity.class));
            }
        });

        // login function **************************************************************************
        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String emailT = email.getText().toString().trim();
                String passwordT = password.getText().toString().trim();


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


                bar.setVisibility(View.VISIBLE);

                fAuth.signInWithEmailAndPassword(emailT,passwordT).addOnCompleteListener(new OnCompleteListener<AuthResult>(){

                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if(task.isSuccessful()){
                            Toast.makeText(LoginActivity.this,"Logged in successfuly",Toast.LENGTH_LONG).show();
                            startActivity(new Intent(getApplicationContext(),MainActivity.class));
                        }else{
                            Toast.makeText(LoginActivity.this,"Error"+ task.getException().getMessage(),Toast.LENGTH_LONG).show();
                            bar.setVisibility(View.GONE);
                        }
                    }
                });


            }
        });




        // login with google functions **************************************************************
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        signInClient = GoogleSignIn.getClient(this,gso);

        google_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SignInGoogle();
            }
        });


        // login with facebook function ************************************************************
      /*  callbackManager = CallbackManager.Factory.create();
        facebook_button.setReadPermissions("email", "public_profile");
        facebook_button.registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                Log.d("TAG", "facebook:onSuccess:" + loginResult);
                Toast.makeText(LoginActivity.this, "facebook:onSuccess",
                        Toast.LENGTH_SHORT).show();
                handleFacebookAccessToken(loginResult.getAccessToken());
            }

            @Override
            public void onCancel() {
                Log.d("TAG", "facebook:onCancel");
                Toast.makeText(LoginActivity.this, "facebook:onCancel",
                        Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onError(FacebookException error) {
                Log.d("TAG", "facebook:onError", error);
                Toast.makeText(LoginActivity.this, "facebook:onError",
                        Toast.LENGTH_SHORT).show();
            }
        });
*/
    }

 // google functions *******************************************************************************
    public void SignInGoogle() {
        bar.setVisibility(View.VISIBLE);
        Intent signInIntent = signInClient.getSignInIntent();
        startActivityForResult(signInIntent, GOOGLE_SIGN_IN);
    }

    private void firebaseAuthWithGoogle(GoogleSignInAccount acct) {
        Log.d("TAG", "firebaseAuthWithGoogle:" + acct.getId());

        AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
        fAuth.signInWithCredential(credential).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                    if (task.isSuccessful()) {
                        Log.d("TAG", "signInWithCredential:success");

                        Toast.makeText(LoginActivity.this, "Authentication successful.",
                                Toast.LENGTH_SHORT).show();
                        FirebaseUser user = fAuth.getCurrentUser();
                        updateUI(user);
                        startActivity(new Intent(getApplicationContext(),MainActivity.class));
                    } else {
                        Log.w("TAG", "signInWithCredential:failure", task.getException());

                        Toast.makeText(LoginActivity.this, "Authentication failed.",
                                Toast.LENGTH_SHORT).show();

                    }
                    bar.setVisibility(View.INVISIBLE);
                }
        });
    }

    private void updateUI(FirebaseUser user) {
        if (user != null) {

            userID = user.getUid();
            DocumentReference documentReference = fStore.collection("profile").document(userID);
            Map<String,Object> userp = new HashMap<>();

            String nameT = user.getDisplayName();
            String emailT = user.getEmail();

            userp.put("fullname",nameT);
            userp.put("email",emailT);
            documentReference.set(userp).addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void aVoid) {
                    Log.d("TAG","Profile created successfuly for user: "+userID );
                }
            });
            documentReference.set(userp).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Log.d("TAG","Failed creating profile for user: "+userID );
                }
            });
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == GOOGLE_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                GoogleSignInAccount account = task.getResult(ApiException.class);
                if (account != null) firebaseAuthWithGoogle(account);
            } catch (ApiException e) {
                Log.w("TAG", "Google sign in failed", e);
                Toast.makeText(LoginActivity.this, "Google sign in failed",
                        Toast.LENGTH_SHORT).show();
            }

        }else{
            callbackManager.onActivityResult(requestCode, resultCode, data);
        }
    }

// facebool function *******************************************************************************
    private void handleFacebookAccessToken(AccessToken token) {
        Log.d("TAG", "handleFacebookAccessToken:" + token);
        bar.setVisibility(View.VISIBLE);

        AuthCredential credential = FacebookAuthProvider.getCredential(token.getToken());
        fAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d("TAG", "signInWithCredential:success");
                            Toast.makeText(LoginActivity.this, "Authentication successful.",
                                    Toast.LENGTH_SHORT).show();
                            FirebaseUser user = fAuth.getCurrentUser();
                            updateUI(user);

                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w("TAG", "signInWithCredential:failure", task.getException());
                            Toast.makeText(LoginActivity.this, "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();
                        }
                        bar.setVisibility(View.GONE);
                    }
                });
    }
}
