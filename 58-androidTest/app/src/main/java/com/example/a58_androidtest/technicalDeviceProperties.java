package com.example.a58_androidtest;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toolbar;

import java.nio.ByteBuffer;

@RequiresApi(api = Build.VERSION_CODES.O)
public class technicalDeviceProperties extends AppCompatActivity {

    BTManager2 bluetoothManager = BTManager2.getInstance();

    SimulatorData simulator;
    EditText talkable;
    EditText walkable;
    EditText breaths;
    EditText beats;
    EditText capillarRefill;
    EditText device;
    EditText color;

    TextView message;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent intent = getIntent();
        simulator = (SimulatorData)intent.getSerializableExtra(technicalActivity.DEVICE);

        setContentView(R.layout.activity_technical_device_properties);

        message = (TextView) findViewById(R.id.textMessage);
        message.setText(new String(""));

        System.out.println(simulator);
        talkable = (EditText) findViewById(R.id.ableToTalk);
        talkable.setText(simulator.getExecutesCommand() ? "1" : "0");
        walkable = (EditText) findViewById(R.id.ableToWalk);

        walkable.setText(simulator.getAbleToWalk() ? "1" : "0");

        breaths = (EditText) findViewById(R.id.breaths);
        breaths.setText(simulator.getBreathsPerMinute().toString());
        beats = (EditText) findViewById(R.id.beatsperseconds);
        beats.setText(simulator.getBeatsPerMinute().toString());

        capillarRefill = (EditText) findViewById(R.id.capillarRefill);
        capillarRefill.setText(String.valueOf(simulator.getCapillaryRefill()));

        device = (EditText) findViewById(R.id.device);
        device.setText(String.valueOf(simulator.getDevice()));

        color = (EditText) findViewById(R.id.color);
        color.setText(String.valueOf(simulator.getColor()));
    }

    public void saveNewParams(View view) {
        System.out.println("Saving data in device");
        try {
            message.setText(new String("Wydano dyspozycję zmiany parametrów"));

            bluetoothManager.setParam(simulator.getDevice(),
                    Integer.valueOf(talkable.getText().toString()) > 0,
                    Integer.valueOf(walkable.getText().toString()) > 0,
                    Integer.valueOf(breaths.getText().toString()),
                    Integer.valueOf(beats.getText().toString()),
                    (int) (Float.valueOf(capillarRefill.getText().toString()) * 10),
                    Integer.valueOf(color.getText().toString()),
                    Short.valueOf(device.getText().toString()));
        }catch (Exception e){

        }
    }

}
