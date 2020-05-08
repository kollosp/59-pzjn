package com.example.a58_androidtest;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.util.Timer;
import java.util.TimerTask;

public class DisplayMessageActivity extends AppCompatActivity {

    Timer timer;
    Integer seconds = 0;
    Integer mode = 1; //1- run, 0 - pause

    Button pause;
    Button reset;

    @Override
    protected void onDestroy() {
        timer.cancel();
        super.onDestroy();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_display_message);

        // Get the Intent that started this activity and extract the string
        Intent intent = getIntent();
        String message = intent.getStringExtra(MainActivity.EXTRA_MESSAGE);

        // Capture the layout's TextView and set the string as its text
        TextView textView = findViewById(R.id.textView);
        textView.setText(message);

        pause = findViewById(R.id.pause);
        reset = findViewById(R.id.reset);

        pause.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view){
                pause();
            }
        });

        reset.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view){
                reset();
            }
        });

        initTimer();
    }

    public void pause(){
        switch(mode){
            case 0:
                pause.setText("Pause");
                mode = 1;
                break;
            case 1:
                pause.setText("Start");
                mode = 0;
                break;
        }


        updateTimer();
    }

    public void reset(){
        seconds = 0;
        updateTimer();
    }


    public void initTimer() {

        class RemindTask extends TimerTask {
            public void run() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if(mode == 1) {
                            seconds += 1;
                            updateTimer();
                        }
                    }
                });
            }
        }

        timer = new Timer();
        timer.scheduleAtFixedRate(new RemindTask(),0, 1000);
    }

    public void updateTimer(){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                TextView textView = findViewById(R.id.timerText);
                textView.setText(seconds.toString());
            }
        });
    }

}
