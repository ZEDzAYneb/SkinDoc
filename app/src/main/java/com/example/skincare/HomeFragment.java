package com.example.skincare;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;


public class HomeFragment extends Fragment {
    private ImageButton imageTake;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v= inflater.inflate(R.layout.home_fragment,container,false);

        imageTake = v.findViewById(R.id.imageTake);
        final FragmentManager fragmentManager = getFragmentManager();

        imageTake.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                fragmentManager.beginTransaction().replace(R.id.fragment_container, new ResultFragment()).commit();
            }
        });
        return v;
    }
}
