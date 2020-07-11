package com.example.skincare.fragment;

import android.content.Intent;
import android.graphics.Bitmap;
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

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.example.skincare.R;


public class HomeFragment extends Fragment {
    private ImageButton imageTake;
    private static final int CAN_REQUEST =1313;
    private ImageView imageView;
    private LinearLayout buttonLayout;
    private RelativeLayout buttonLayout2;
    private Button continuePic;
    private Button discardPic;

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


        final FragmentManager fragmentManager = getFragmentManager();

        imageTake.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                startActivityForResult(intent, CAN_REQUEST);
            }
        });
              //  fragmentManager.beginTransaction().replace(R.id.fragment_container, new ResultFragment()).commit();


        return v;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data){
        super.onActivityResult(requestCode,resultCode,data);
        if(requestCode == CAN_REQUEST ){
            Bitmap bitmap =(Bitmap) data.getExtras().get("data");
            imageView.setVisibility(View.VISIBLE);
            imageView.setImageBitmap(bitmap);
            buttonLayout.setVisibility(View.VISIBLE);
            buttonLayout2.setVisibility(View.INVISIBLE);


        }
    }

}
