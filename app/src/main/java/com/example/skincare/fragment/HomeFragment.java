package com.example.skincare.fragment;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
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
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.example.skincare.R;
import com.example.skincare.adapter.DeseaseDbHelper;
import com.example.skincare.models.Desease;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import static android.app.Activity.RESULT_OK;


public class HomeFragment extends Fragment {
    private ImageButton imageTake;

    private static final int CAMERA_REQUEST =1313;
    private ImageView imageView;
    private LinearLayout buttonLayout;
    private RelativeLayout buttonLayout2;
    private Button continuePic;
    private Button discardPic;

    private FirebaseAuth fAuth;
    private String userID;

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
            }
        });

        continuePic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Bitmap image = ((BitmapDrawable)imageView.getDrawable()).getBitmap();

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

        if (requestCode == CAMERA_REQUEST && resultCode == RESULT_OK) {
            Bundle extras = data.getExtras();
            Bitmap imageBitmap = (Bitmap) extras.get("data");
            imageView.setImageBitmap(imageBitmap);

            imageView.setVisibility(View.VISIBLE);
            buttonLayout.setVisibility(View.VISIBLE);
            buttonLayout2.setVisibility(View.INVISIBLE);


        }
    }

}
