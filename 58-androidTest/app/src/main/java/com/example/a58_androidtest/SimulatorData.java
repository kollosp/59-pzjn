package com.example.a58_androidtest;

import android.graphics.Color;

import java.io.Serializable;

public class SimulatorData implements Serializable {
    //1 - red, 2 - green, 3 -yellow, 4 -black
    short device;
    Integer color = 0;
    String simulatorName = "";
    Integer breathsPerMinute = 0;
    Integer beatsPerMinute = 0;
    Boolean ableToWalk = false;
    Boolean executesCommand = false;
    double capillaryRefill = 0.0;
    long updateTime = 0;

    final public static int TIME_TO_DISCONNECT = 15 * 1000; //30s

    int status = 0; //connected or disconnected

    public SimulatorData(short device){
        this.device = device;
    }

    public SimulatorData(String s, Integer breaths, Integer beats, Boolean walking, Integer c){
        simulatorName = s;
        ableToWalk = walking;
        beatsPerMinute = beats;
        breathsPerMinute = breaths;
        color = c;
    }

    public short getDevice(){
        return device;
    }

    public void setDevice(short dev){
        this.device = dev;
    }

    public Integer getColor(){
        return color;
    }

    public void setColor(Integer c){
        color = c;
    }

    public String getSimulatorName(){
        return simulatorName;
    }

    public void setSimulatorName(String s){
        simulatorName = s;
    }

    public Integer getBreathsPerMinute(){
        return breathsPerMinute;
    }

    public void setBreathsPerMinute(Integer i){
        breathsPerMinute = i;
    }

    public Integer getBeatsPerMinute(){
        return beatsPerMinute;
    }

    public void setBeatsPerMinute(Integer i){
        beatsPerMinute = i;
    }

    public Boolean getAbleToWalk(){
        return ableToWalk;
    }

    public void setAbleToWalk(Boolean b){
        ableToWalk = b;
    }

    public Boolean getExecutesCommand(){
        return executesCommand;
    }

    public void setExecutesCommand(Boolean b){
        executesCommand = b;
    }

    public double getCapillaryRefill(){
        return capillaryRefill;
    }

    public void setCapillaryRefill(double b){
        capillaryRefill = b;
    }

    public Integer getConnectionStatus(){

        if(status == 1 && (System.currentTimeMillis() - updateTime) < TIME_TO_DISCONNECT){
            return 1;
        }else{
            status = 0;
            return 0;
        }
    }

    public void setConnectionStatus(Integer s){

        updateTime = System.currentTimeMillis();
        status = s;
    }

    public Integer getColorHex(){
        switch(color){
            case 1:
                return Color.RED;
            case 2:
                return Color.GREEN;
            case 3:
                return Color.YELLOW;
            case 4:
                return Color.BLACK;
            default:
                return Color.GRAY;
        }
    }
}
