package com.example.skincare.fragment;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.example.skincare.R;
import com.example.skincare.adapter.UserDbhelper;
import com.example.skincare.models.Users;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserInfo;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import static com.facebook.FacebookSdk.getApplicationContext;

public class ProfileFragment extends Fragment {


    private FirebaseAuth fAuth;
    private FirebaseFirestore fStore;
    private String userID;

    private TextView user_Pname;
    private TextView user_Pemail;
    private TextView user_Pgender;
    private TextView user_Pbirth;
    private Button verify;

    private String name;
    private String email;
    private String gender;
    private String birth;

    private UserDbhelper userDbhelper;
    private static final String TAG = "profileFragment";


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
       View v= inflater.inflate(R.layout.profile_fragment,container,false);

        setHasOptionsMenu(true);

        fAuth = FirebaseAuth.getInstance();
        FirebaseUser user = fAuth.getCurrentUser();
        userID = user.getUid();
        fStore = FirebaseFirestore.getInstance();

        user_Pname = v.findViewById(R.id.user_Pname);
        user_Pemail = v.findViewById(R.id.user_Pemail);
        user_Pgender = v.findViewById(R.id.user_Pgender);
        user_Pbirth = v.findViewById(R.id.user_Pbirth);
        verify= v.findViewById(R.id.verifieButton);

        /*userDbhelper = new UserDbhelper(getApplicationContext());

        Users users = userDbhelper.getUser(userID);

        user_Pname.setText(users.getFullname());
        user_Pemail.setText(users.getEmail());
        user_Pgender.setText(users.getSex());
        user_Pbirth.setText(users.getBirthday());*/

        DocumentReference docRef = fStore.collection("profile").document(userID);

        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        name = document.getString("fullname");
                        email = document.getString("email");
                        gender = document.getString("sex");
                        birth = document.getString("birthday");
                        user_Pname.setText(name);
                        user_Pemail.setText(email);
                        user_Pgender.setText(gender);
                        user_Pbirth.setText(birth);
                        Log.d("TAG", "DocumentSnapshot data: " + document.getData());
                    } else {
                        Log.d("TAG", "No such document");
                    }
                } else {
                    Log.d("TAG", "get failed with ", task.getException());
                }
            }
        });

        final FirebaseUser fuser = fAuth.getCurrentUser();
        if(!fuser.isEmailVerified()){
            verify.setVisibility(View.VISIBLE);
            }else{
            verify.setVisibility(View.GONE);
        }

        verify.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                fuser.sendEmailVerification().addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Toast.makeText(getActivity(), "Email verification has been sent", Toast.LENGTH_LONG).show();
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d("TAG", "Failed: " + e.getMessage());
                    }
                });

            }
        });
        return v;

    }
    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.profile_menu, menu);
        super.onCreateOptionsMenu(menu,inflater);

        for (UserInfo user: FirebaseAuth.getInstance().getCurrentUser().getProviderData()) {
            if (user.getProviderId().equals("facebook.com") || user.getProviderId().equals("google.com")) {
                MenuItem spassItem = menu.findItem(R.id.nav_EditPass);
                spassItem.setVisible(false);
                }
            }
        }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        FragmentManager fragmentManager = getFragmentManager();
        switch (item.getItemId()) {
                    case R.id.nav_Editprofile:
                        fragmentManager.beginTransaction().replace(R.id.fragment_container, new EditProfileFragment()).commit();
                        return true;
                    case R.id.nav_EditPass:
                        fragmentManager.beginTransaction().replace(R.id.fragment_container, new EditPassFragment()).commit();
                        return true;

        }

        return false;
    }


    }

