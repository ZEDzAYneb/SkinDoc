package com.example.skincare.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.GridLayout;
import android.widget.GridView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.skincare.adapter.DeseaseDbHelper;
import com.example.skincare.models.Desease;
import com.example.skincare.adapter.DeseaseListAdapter;
import com.example.skincare.R;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.ArrayList;
import java.util.List;

import static com.facebook.FacebookSdk.getApplicationContext;

public class SavedFragment extends Fragment {

    List<Desease> deseaseList;
    private GridView gridView;
    private DeseaseDbHelper dbHelper;
    private FirebaseAuth fAuth;
    private String userID;
    private static final String TAG = "savedFragment";

    private DeseaseListAdapter deseaseListAdapter;
    private FloatingActionButton myFab;
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v= inflater.inflate(R.layout.saved_fragment,container,false);

        setHasOptionsMenu(true);

        gridView = (GridView) v.findViewById(R.id.Desease_grid_view);
        myFab =v.findViewById(R.id.myFAB);
        fAuth = FirebaseAuth.getInstance();
        FirebaseUser user = fAuth.getCurrentUser();
        userID = user.getUid();

       // dbHelper = new DeseaseDbHelper(getApplicationContext());
        //deseaseListAdapter = new DeseaseListAdapter(getApplicationContext(),dbHelper.readAllDeseases(),false);

        //this.gridView.setAdapter(new DeseaseListAdapter(getContext(), this.dbHelper.readAllDeseases(userID), false));

        //deseaseList = new ArrayList<>();
        //deseaseList.add(new Desease("skin lesion",R.drawable.download));
        //DeseaseListAdapter adapter = new DeseaseListAdapter(getActivity(), deseaseList);


        myFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FragmentManager fragmentManager = getFragmentManager();
                fragmentManager.beginTransaction().replace(R.id.fragment_container, new HomeFragment()).commit();
            }
        });

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

   // @Override
   /*public void onResume() {
        super.onResume();
        ((CursorAdapter)gridView.getAdapter()).swapCursor(this.dbHelper.readAllDeseases());
    }*/


}
