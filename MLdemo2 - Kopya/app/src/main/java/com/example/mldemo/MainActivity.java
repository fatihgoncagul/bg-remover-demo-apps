package com.example.mldemo;


import androidx.appcompat.app.AppCompatActivity;


import android.content.Intent;

import android.os.Bundle;

import android.view.View;

import com.example.mldemo.helpers.ImageHelperActivity;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    public  void onGotoImageActivity(View view){
        //START IMAGE HELPER ACTIVITY
        Intent intent = new Intent(this, ImageHelperActivity.class);
        startActivity(intent);

    }




}