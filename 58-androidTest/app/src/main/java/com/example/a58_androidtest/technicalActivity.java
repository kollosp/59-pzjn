package com.example.a58_androidtest;

import android.os.Bundle;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;


public class technicalActivity extends AppCompatActivity {

    ArrayList<SimulatorData> simulators = new ArrayList<SimulatorData>();
    LinearLayout simulatorWrapper;

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


        simulators.add(new SimulatorData("Sim1", 12, 60, true));
        simulators.add(new SimulatorData("Sim1", 12, 60, false));
        simulators.add(new SimulatorData("Sim3", 12, 60, true));
        simulators.add(new SimulatorData("Sim4", 12, 60, false));

        render();
    }

    void render(){

        if(simulatorWrapper.getChildCount() > 0)
            simulatorWrapper.removeAllViews();

        for(SimulatorData sim : simulators){
            LinearLayout container = new LinearLayout(this);
            container.setOrientation(LinearLayout.VERTICAL);

            LinearLayout row2 = new LinearLayout(this);
            row2.setOrientation(LinearLayout.HORIZONTAL);

            TextView name = new TextView(this);
            name.setText(sim.getSimulatorName());

            TextView walk = new TextView(this);
            walk.setText("Walking:" + sim.getAbleToWalk());

            TextView breath = new TextView(this);
            breath.setText("breaths: " + sim.getBreathsPerMinute());

            TextView beats = new TextView(this);
            beats.setText("beats: " + sim.getBeatsPerMinute());

            row2.addView(walk);
            row2.addView(breath);
            row2.addView(beats);

            container.addView(name);
            container.addView(row2);
            simulatorWrapper.addView(container);
        }
    }
}
