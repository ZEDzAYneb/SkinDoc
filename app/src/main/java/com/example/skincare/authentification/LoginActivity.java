package com.example.skincare.authentification;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatTextView;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.example.skincare.MainActivity;
import com.example.skincare.R;
import com.example.skincare.adapter.UserDbhelper;
import com.example.skincare.models.Users;
import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.GoogleApiClient;
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

public class LoginActivity extends AppCompatActivity implements GoogleApiClient.OnConnectionFailedListener,
        View.OnClickListener{

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
   // private CallbackManager callbackManager;

    private UserDbhelper userDbhelper;


    private static final String TAG = "SignInActivity";
    private static final int RC_SIGN_IN = 9001;

    private GoogleApiClient mGoogleApiClient;
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private CallbackManager mCallbackManager;




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

        userDbhelper = new UserDbhelper(getApplicationContext());

        textViewLinkRegister = (AppCompatTextView) findViewById(R.id.L_S_Button);
        textViewLinkForgot = (AppCompatTextView) findViewById(R.id.L_Forgot_Button);

       // google_button = findViewById(R.id.Google);
        //facebook_button = findViewById(R.id.Facebook);


        if(fAuth.getCurrentUser() != null){
            startActivity(new Intent(getApplicationContext(), MainActivity.class));
            finish();
        }

        // link to regestration page ***************************************************************
        textViewLinkRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getApplicationContext(), SignupActivity.class));
            }
        });

        // link to forgot password page ************************************************************
        textViewLinkForgot.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getApplicationContext(), ForgotPasswordActivity.class));
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


        FacebookSdk.sdkInitialize(getApplicationContext());
        mCallbackManager = CallbackManager.Factory.create();

        facebook_button = findViewById(R.id.Facebook);
        facebook_button.setReadPermissions("email", "public_profile", "user_birthday", "user_friends");

        facebook_button.registerCallback(mCallbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                Log.d(TAG, "facebook:onSuccess:" + loginResult);
                firebaseAuthWithFacebook(loginResult.getAccessToken());
            }

            @Override
            public void onCancel() {
                Log.d(TAG, "facebook:onCancel");
            }

            @Override
            public void onError(FacebookException error) {
                Log.d(TAG, "facebook:onError", error);
            }
        });

         google_button = findViewById(R.id.Google);


        // Set click listeners
        google_button.setOnClickListener(this);

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this /* FragmentActivity */, this /* OnConnectionFailedListener */)
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build();

        // Initialize FirebaseAuth
        mAuth = FirebaseAuth.getInstance();

        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user != null) {
                    // User is signed in
                    Log.d(TAG, "onAuthStateChanged:signed_in:" + user.getUid());
                } else {
                    // User is signed out
                    Log.d(TAG, "onAuthStateChanged:signed_out");
                }
            }
        };

        // login with google functions **************************************************************
     /*   GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        signInClient = GoogleSignIn.getClient(this,gso);

        google_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SignInGoogle();
            }
        });*/


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

    @Override
    public void onStart() {
        super.onStart();
        mAuth.addAuthStateListener(mAuthListener);
    }

    @Override
    public void onStop() {
        super.onStop();
        if (mAuthListener != null) {
            mAuth.removeAuthStateListener(mAuthListener);
        }
    }
    private void firebaseAuthWithGoogle(GoogleSignInAccount acct) {
        Log.d(TAG, "firebaseAuthWithGooogle:" + acct.getId());
        AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        Log.d(TAG, "signInWithCredential:onComplete:" + task.isSuccessful());

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
    private void firebaseAuthWithFacebook(AccessToken token) {
        Log.d(TAG, "handleFacebookAccessToken:" + token);

        final AuthCredential credential = FacebookAuthProvider.getCredential(token.getToken());
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        Log.d(TAG, "signInWithCredential:onComplete:" + task.isSuccessful());
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

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.Google:
                signIn();
                break;
            default:
                return;
        }
    }

    private void signIn() {
        bar.setVisibility(View.VISIBLE);
        Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient);
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        mCallbackManager.onActivityResult(requestCode, resultCode, data);

        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            if (result.isSuccess()) {
                // Google Sign In was successful, authenticate with Firebase
                GoogleSignInAccount account = result.getSignInAccount();
                firebaseAuthWithGoogle(account);
            } else {
                // Google Sign In failed
                Log.e(TAG, "Google Sign In failed.");
            }
        }
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        // An unresolvable error has occurred and Google APIs (including Sign-In) will not
        // be available.
        Log.d(TAG, "onConnectionFailed:" + connectionResult);
        Toast.makeText(this, "Google Play Services error.", Toast.LENGTH_SHORT).show();
    }

    // google functions *******************************************************************************
  /*  public void SignInGoogle() {
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
*/
    private void updateUI(FirebaseUser user) {
        if (user != null) {

            userID = user.getUid();
            DocumentReference documentReference = fStore.collection("profile").document(userID);
            Map<String,Object> userp = new HashMap<>();

            String nameT = user.getDisplayName();
            String emailT = user.getEmail();

            userp.put("fullname",nameT);
            userp.put("email",emailT);
            userp.put("sex","");
            userp.put("birthday","");
            userp.put("password","");
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
/*
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

        }
    }*/

// facebool function *******************************************************************************
  /*  private void handleFacebookAccessToken(AccessToken token) {
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
    }*/

    public void ShowPass(View view){
        if(view.getId()==R.id.L_show_pass){

            if(password.getTransformationMethod().equals(PasswordTransformationMethod.getInstance())){
                ((ImageView)(view)).setImageResource(R.drawable.ic_close_eye);

                //Show Password
                password.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
            }
            else{
                ((ImageView)(view)).setImageResource(R.drawable.ic_open_eye);

                //Hide Password
                password.setTransformationMethod(PasswordTransformationMethod.getInstance());

            }
        }
    }
}
