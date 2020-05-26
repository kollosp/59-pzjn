package com.example.a58_androidtest;

import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;


@RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
public class technicalActivity extends AppCompatActivity {

    ArrayList<SimulatorData> simulators = new ArrayList<SimulatorData>();
    LinearLayout simulatorWrapper;
    BTManager bluetoothManager = new BTManager("00000000-0000-1000-8000-00805F9B34FB");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_technical);
        simulatorWrapper =  (LinearLayout) findViewById(R.id.simulatorWrapper);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        for(int i=0;i<4;++i) {
            int k = i*4;
            simulators.add(new SimulatorData("Sim" + (k+1), 12, 60, true, 1));
            simulators.add(new SimulatorData("Sim" + (k+2), 12, 60, false, 2));
            simulators.add(new SimulatorData("Sim" + (k+3), 12, 60, true, 3));
            simulators.add(new SimulatorData("Sim" + (k+4), 12, 60, false, 4));
        }


        try {
            bluetoothManager.init("techApp", (short)0x103);
        } catch (Exception e) {

            //first enable bt
            Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
        }

        initTimer();
        render();
    }

    public void initTimer() {

        class RemindTask extends TimerTask {
            public void run() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        simulators = bluetoothManager.getDevicesInRange();

                        for(SimulatorData sim : simulators){
                            bluetoothManager.askForAllParams(sim.getDevice());
                        }

                        render();
                    }
                });
            }
        }

        Timer timer = new Timer();
        timer.scheduleAtFixedRate(new RemindTask(),0, 5000);
    }

    void render(){

        if(simulatorWrapper.getChildCount() > 0)
            simulatorWrapper.removeAllViews();

        for(SimulatorData sim : simulators){
            LinearLayout container = new LinearLayout(this);
            container.setOrientation(LinearLayout.VERTICAL);
            container.setPadding(0,0, 0, 15);
            LinearLayout row2 = new LinearLayout(this);
            row2.setOrientation(LinearLayout.HORIZONTAL);

            TextView name = setTextViewAttributes(new TextView(this));
            name.setText("#" + sim.getDevice() + " " + (sim.getConnectionStatus() == 1 ? "connected" : "disconnected"));

            TextView walk = setTextViewAttributes(new TextView(this));
            walk.setText("Walking:" + sim.getAbleToWalk());

            TextView breath = setTextViewAttributes(new TextView(this));
            breath.setText("breaths: " + sim.getBreathsPerMinute());

            TextView beats = setTextViewAttributes(new TextView(this));
            beats.setText("beats: " + sim.getBeatsPerMinute());


            row2.addView(createColorRect(sim.getColorHex()));
            row2.addView(walk);
            row2.addView(breath);
            row2.addView(beats);

            container.addView(name);
            container.addView(row2);
            container.addView(createDivider());
            simulatorWrapper.addView(container);

            container.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                for (int i = 0; i < simulatorWrapper.getChildCount(); ++i){
                    if (simulatorWrapper.getChildAt(i) == v) {
                        Toast.makeText(getApplicationContext(), simulators.get(i).getDevice() + " clicked", Toast.LENGTH_SHORT).show();
                        openWindow(simulators.get(i));
                    }
                }
                }
            });
        }
    }

    public void openWindow(SimulatorData device){
        Intent intent = new Intent(this, DisplayMessageActivity.class);
        intent.putExtra("BluetoothManager", bluetoothManager);
        intent.putExtra("Device", device);
        startActivity(intent);
    }

    public void sendPingMessage(View view){

        Toast.makeText(getApplicationContext(), "Ping", Toast.LENGTH_SHORT).show();
        bluetoothManager.setParam((short)0x100, 'B', (char)65);
        //bluetoothManager.sendPing();
    }

    TextView setTextViewAttributes(TextView tv){
        tv.setPadding(5,2,5,2);
        return  tv;
    }

    TextView createColorRect(int color){
        TextView t = new TextView(this);
        t.setText("color");
        t.setTextColor(color);
        t.setBackgroundColor(color);
        return  t;
    }

    LinearLayout createDivider(){
        LinearLayout container = new LinearLayout(this);
        container.setBackgroundColor(Color.GRAY);
        int dividerHeight = 1;
        container.setLayoutParams(new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dividerHeight));

        return container;
    }
}
