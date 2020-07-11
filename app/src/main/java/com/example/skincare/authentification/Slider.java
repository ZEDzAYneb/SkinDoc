package com.example.skincare.authentification;

import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager.widget.ViewPager;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;

import com.example.skincare.R;
import com.example.skincare.adapter.SliderAdapter;
import com.example.skincare.authentification.LoginActivity;

public class Slider extends AppCompatActivity {

    ViewPager sliderPager;
    int []layouts;
    Button sliderButton;
    SliderAdapter sliderAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);


        setContentView(R.layout.activity_slider);


        sliderPager = findViewById(R.id.SliderPager);
        sliderButton = findViewById(R.id.sliderButton);

        layouts = new int []{
                R.layout.slider1,
                R.layout.slider2,
                R.layout.slider3
        };

       sliderAdapter = new SliderAdapter(this,layouts);
       sliderPager.setAdapter(sliderAdapter);

        sliderButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(sliderPager.getCurrentItem()+1 < layouts.length){
                    sliderPager.setCurrentItem(sliderPager.getCurrentItem()+1);
                }else {
                    startActivity(new Intent(getApplicationContext(), LoginActivity.class));
                }
            }
        });

        sliderPager.addOnPageChangeListener(viewPagerChangeListener);
    }

        ViewPager.OnPageChangeListener viewPagerChangeListener = new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int i, float v, int i1) {

            }

            @Override
            public void onPageSelected(int i) {
                if(i == layouts.length - 1){
                    sliderButton.setText("Continue");
                }else {
                    sliderButton.setText("Next");
                }
            }

            @Override
            public void onPageScrollStateChanged(int i) {

            }
        };


    }

