package com.example.a58_androidtest;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toolbar;

@RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
public class technicalDeviceProperties extends AppCompatActivity {

    BTManager bluetoothManager = new BTManager("00000000-0000-1000-8000-00805F9B34FB");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent intent = getIntent();
        SimulatorData simulator = (SimulatorData)intent.getSerializableExtra(technicalActivity.DEVICE);

        setContentView(R.layout.activity_technical_device_properties);
        System.out.println(simulator);
        EditText talkable = (EditText) findViewById(R.id.ableToTalk);
        //talkable.setText(simulator.getExecutesCommand() ? "Tak" : "Nie");
        EditText walkable = (EditText) findViewById(R.id.ableToWalk);

        //walkable.setText(simulator.getAbleToWalk() ? "Tak" : "Nie");

        EditText breaths = (EditText) findViewById(R.id.breaths);
        //breaths.setText(simulator.getBreathsPerMinute().toString());
        EditText beats = (EditText) findViewById(R.id.beatsperseconds);
        //beats.setText(simulator.getBeatsPerMinute().toString());

        EditText capillarRefill = (EditText) findViewById(R.id.capillarRefill);
        //capillarRefill.setText(simulator.getCapillaryRefill());

        EditText device = (EditText) findViewById(R.id.device);
        //device.setText(simulator.getDevice());
    }

    public void saveNewParams(View view) {
    }

}
