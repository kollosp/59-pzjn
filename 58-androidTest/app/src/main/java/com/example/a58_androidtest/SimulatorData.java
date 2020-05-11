package com.example.a58_androidtest;

import android.graphics.Color;

public class SimulatorData {
    //1 - red, 2 - green, 3 -yellow, 4 -black
    Integer color = 1;
    String simulatorName;
    Integer breathsPerMinute;
    Integer beatsPerMinute;
    Boolean ableToWalk;
    Boolean executesCommand;
    Integer capillaryRefill;

    int status; //connected or disconnected

    public SimulatorData(String s, Integer breaths, Integer beats, Boolean walking, Integer c){
        simulatorName = s;
        ableToWalk = walking;
        beatsPerMinute = beats;
        breathsPerMinute = breaths;
        color = c;
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

    public Integer getCapillaryRefill(){
        return capillaryRefill;
    }

    public void setCapillaryRefill(Integer b){
        capillaryRefill = b;
    }

    public Integer getConnectionStatus(){
        return status;
    }

    public void setConnectionStatus(Integer s){
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
            case 0:
                return Color.BLACK;
            default:
                return Color.GRAY;
        }
    }
}
