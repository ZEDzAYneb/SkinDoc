package com.example.skincare.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.skincare.models.Desease;
import com.example.skincare.adapter.DeseaseListAdapter;
import com.example.skincare.R;

import java.util.ArrayList;
import java.util.List;

public class SavedFragment extends Fragment {

    List<Desease> deseaseList;
    RecyclerView recyclerView;
    private static final String TAG = "savedFragment";
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v= inflater.inflate(R.layout.saved_fragment,container,false);

        setHasOptionsMenu(true);

        recyclerView = (RecyclerView) v.findViewById(R.id.desease_recycleView);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

        //initializing the productlist
        deseaseList = new ArrayList<>();

        deseaseList.add(new Desease(1,"skin lesion",R.drawable.download));

        DeseaseListAdapter adapter = new DeseaseListAdapter(getActivity(), deseaseList);
        recyclerView.setAdapter(adapter);


        return v;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.saved_menu, menu);
        super.onCreateOptionsMenu(menu,inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        FragmentManager fragmentManager = getFragmentManager();
        switch (item.getItemId()) {
            case R.id.nav_AddDesease:
                fragmentManager.beginTransaction().replace(R.id.fragment_container, new HomeFragment()).commit();
                return true;
        }
        return false;
    }
}
