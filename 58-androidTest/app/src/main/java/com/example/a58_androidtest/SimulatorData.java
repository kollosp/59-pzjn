package com.example.a58_androidtest;

public class SimulatorData {
    //red, green, yellow, black
    Integer color;
    String simulatorName;
    Integer breathsPerMinute;
    Integer beatsPerMinute;
    Boolean ableToWalk;
    Boolean executesCommand;
    Boolean capillaryRefill;

    int status; //connected or disconnected

    public SimulatorData(String s, Integer breaths, Integer beats, Boolean walking){
        simulatorName = s;
        ableToWalk = walking;
        beatsPerMinute = beats;
        breathsPerMinute = breaths;
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

    public Boolean getCapillaryRefill(){
        return capillaryRefill;
    }

    public void setCapillaryRefill(Boolean b){
        capillaryRefill = b;
    }
}
