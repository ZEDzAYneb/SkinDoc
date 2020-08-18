package com.example.skincare.fragment;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.example.skincare.R;
import com.example.skincare.adapter.DeseaseDbHelper;
import com.example.skincare.models.Desease;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import static android.app.Activity.RESULT_OK;
import static com.facebook.FacebookSdk.getApplicationContext;


public class HomeFragment extends Fragment {
    private ImageButton imageTake;

    private static final int CAMERA_REQUEST =1;
    private ImageView imageView;
    private LinearLayout buttonLayout;
    private RelativeLayout buttonLayout2;
    private Button continuePic;
    private Button discardPic;

    private FirebaseAuth fAuth;

    private static final String TAG = "UploadActivity";
    private StorageReference reference;
    private FirebaseStorage storage;
    private String userID;

    private Uri filePath;

    private Desease desease;
    private DeseaseDbHelper deseaseDbHelper;




    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v= inflater.inflate(R.layout.home_fragment,container,false);

        imageTake = v.findViewById(R.id.imageTake);
        imageView = v.findViewById(R.id.imageView);
        buttonLayout = v.findViewById(R.id.buttonLayout);
        buttonLayout2 = v.findViewById(R.id.buttonLayout2);
        continuePic = v.findViewById(R.id.continuePic);
        discardPic = v.findViewById(R.id.discardPic);

        deseaseDbHelper = new DeseaseDbHelper(getActivity());
        fAuth = FirebaseAuth.getInstance();
        FirebaseUser user = fAuth.getCurrentUser();
        userID = user.getUid();

        storage = FirebaseStorage.getInstance();
        reference = storage.getReference();

        final FragmentManager fragmentManager = getFragmentManager();

        imageTake.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                if (takePictureIntent.resolveActivity(getActivity().getPackageManager()) != null) {
                    startActivityForResult(takePictureIntent, CAMERA_REQUEST);
                }
            }
        });

        discardPic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                if (takePictureIntent.resolveActivity(getActivity().getPackageManager()) != null) {
                    startActivityForResult(takePictureIntent, CAMERA_REQUEST);
                }
              //  dispatchTakePictureIntent();
            }
        });

        continuePic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Bitmap image = ((BitmapDrawable)imageView.getDrawable()).getBitmap();

                if (filePath != null) {
                    //displaying a progress dialog while upload is going on
                    final ProgressDialog progressDialog = new ProgressDialog(getApplicationContext());
                    progressDialog.setTitle("Uploading");
                    progressDialog.show();

                    StorageReference riversRef = reference.child("images/pic.jpg");
                    riversRef.putFile(filePath)
                            .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                                @Override
                                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                    //if the upload is successfull
                                    //hiding the progress dialog
                                    progressDialog.dismiss();

                                    //and displaying a success toast
                                    Toast.makeText(getApplicationContext(), "File Uploaded ", Toast.LENGTH_LONG).show();
                                }
                            })
                            .addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception exception) {
                                    //if the upload is not successfull
                                    //hiding the progress dialog
                                    progressDialog.dismiss();

                                    //and displaying error message
                                    Toast.makeText(getApplicationContext(), exception.getMessage(), Toast.LENGTH_LONG).show();
                                }
                            })
                            .addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                                @Override
                                public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                                    //calculating progress percentage
                                    double progress = (100.0 * taskSnapshot.getBytesTransferred()) / taskSnapshot.getTotalByteCount();

                                    //displaying percentage in progress dialog
                                    progressDialog.setMessage("Uploaded " + ((int) progress) + "%...");
                                }
                            });
                } else {
                    Toast.makeText(getApplicationContext(), "filepath empty", Toast.LENGTH_LONG).show();

                }

                try{
                      desease = new Desease(userID, "desease",image);
                 //   deseaseDbHelper.addDesease(desease);
                 //   Toast.makeText(getActivity(), "Added successfully", Toast.LENGTH_LONG).show();

                    ResultFragment resultFragment = new ResultFragment ();
                    Bundle args = new Bundle();
                    args.putParcelable("Desease", desease);
                    resultFragment.setArguments(args);
                    FragmentManager fragmentManager = getFragmentManager();
                    fragmentManager.beginTransaction().replace(R.id.fragment_container, resultFragment).commit();
                        // fragmentManager.beginTransaction().replace(R.id.fragment_container, new SavedFragment()).commit();

                }catch (Exception e){
                    Toast.makeText(getActivity(), e.getMessage(), Toast.LENGTH_LONG).show();
                }

            }
        });
        return v;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == CAMERA_REQUEST && resultCode == RESULT_OK && data != null ) {

            Bundle extras = data.getExtras();
            Bitmap imageBitmap = (Bitmap) extras.get("data");
            imageView.setImageBitmap(imageBitmap);


            imageView.setVisibility(View.VISIBLE);
            buttonLayout.setVisibility(View.VISIBLE);
            buttonLayout2.setVisibility(View.INVISIBLE);


        }
    }



}
