package com.example.a58_androidtest;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;

public class MainActivity extends AppCompatActivity {

    public static final String EXTRA_MESSAGE = "com.example.myfirstapp.MESSAGE";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


    }

    public void goToBTScreen(View view){
        Intent intent = new Intent(this, BluetoothActivity.class);
        startActivity(intent);
    }

    public void goToBTAdvScreen(View view){
        Intent intent = new Intent(this, BluetoothAdvertisingActivity.class);
        startActivity(intent);
    }

    public void goToTechnicalScreen(View view){
        Intent intent = new Intent(this, technicalActivity.class);
        startActivity(intent);
    }
}
