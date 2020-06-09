package com.example.skincare;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class SavedFragment extends Fragment {

    List<Desease> deseaseList;
    RecyclerView recyclerView;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v= inflater.inflate(R.layout.saved_fragment,container,false);

        recyclerView = (RecyclerView) v.findViewById(R.id.desease_recycleView);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        //initializing the productlist
        deseaseList = new ArrayList<>();

        deseaseList.add(
                new Desease(1,"skin lesion","sikness",R.drawable.download));

        DeseaseListAdapter adapter = new DeseaseListAdapter(getContext(), deseaseList);
        recyclerView.setAdapter(adapter);


        return v;
    }
}
