package com.example.a58_androidtest;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.le.AdvertiseData;
import android.bluetooth.le.AdvertisingSet;
import android.bluetooth.le.AdvertisingSetCallback;
import android.bluetooth.le.AdvertisingSetParameters;
import android.bluetooth.le.BluetoothLeAdvertiser;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.os.Build;
import android.os.Handler;
import android.os.ParcelUuid;
import android.text.TextUtils;
import android.util.Log;

import java.io.Serializable;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import androidx.annotation.RequiresApi;

@RequiresApi(api = Build.VERSION_CODES.O)
public class BTManager2  implements Serializable {
    private static final String LOG_TAG = "log";

    ParcelUuid uuid;

    BluetoothAdapter adapter;
    BluetoothLeAdvertiser advertiser;
    AdvertisingSetParameters parameters;
    AdvertisingSetCallback advCallback;

    //discovering
    BluetoothLeScanner mBluetoothLeScanner;
    Handler mHandler = new Handler();
    ScanFilter scanFilter;
    ScanSettings scanSettings;
    ArrayList<ScanFilter> filters = new ArrayList<ScanFilter>();


    //current adv set used to modify advertised data
    AdvertisingSet currentAdvertisingSet = null;

    short deviceAddress;
    short lastReceiver = 0;
    short lastAsked = 0;
    String devName = "device";

    // Create the instance
    private static BTManager2 instance;
    public static BTManager2 getInstance()
    {
        if (instance== null) {
            synchronized(BTManager2.class) {
                if (instance == null)
                    instance = new BTManager2();
            }
        }
        // Return the instance
        return instance;
    }



    ArrayList<SimulatorData> devicesInRange = new ArrayList<SimulatorData>();
    ScanCallback mScanCallback;

    ArrayList<SimulatorData> getDevicesInRange() {
        return devicesInRange;
    }

    ArrayList<AdvertiseData> toBeSend = new ArrayList<AdvertiseData>();
    int busy = 0;

    private BTManager2(){
        setUuid();
    }

    public void setUuid(){
        uuid = ParcelUuid.fromString("18150000-0000-1000-8000-00805F9B34FB"); //- working
    }

    public void init(String devName, final Short deviceAddress) throws Exception {

        System.out.println("Init!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!1111111");

        adapter = BluetoothAdapter.getDefaultAdapter();
        this.deviceAddress = deviceAddress;

        //check if device supports bt
        if (adapter == null) {
            // Device doesn't support Bluetooth
            throw(new Exception("Device doesn't support Bluetooth"));
        }

        //ask user to run bt
        if (!adapter.isEnabled()) {
            throw(new Exception("Bluetooth is not enabled"));
        }

        adapter.setName(devName);
        advertiser = adapter.getBluetoothLeAdvertiser();

        parameters = (new AdvertisingSetParameters.Builder())
            .setLegacyMode(true) // True by default, but set here as a reminder.
            .setConnectable(false)
            .setInterval(AdvertisingSetParameters.INTERVAL_HIGH)
            .setTxPowerLevel(AdvertisingSetParameters.TX_POWER_MEDIUM)
            .build();

        advCallback = new AdvertisingSetCallback() {
            @Override
            public void onAdvertisingSetStarted(AdvertisingSet advertisingSet, int txPower, int status) {
                System.out.println("onAdvertisingSetStarted(): txPower:" + txPower + " , status: "
                        + status);
                currentAdvertisingSet = advertisingSet;
                sendPing();
            }

            @Override
            public void onAdvertisingDataSet(AdvertisingSet advertisingSet, int status) {
                System.out.println( "onAdvertisingDataSet() :status:" + status);
            }

            @Override
            public void onScanResponseDataSet(AdvertisingSet advertisingSet, int status) {
                System.out.println("onScanResponseDataSet(): status:" + status);
            }

            @Override
            public void onAdvertisingSetStopped(AdvertisingSet advertisingSet) {
                System.out.println( "onAdvertisingSetStopped():");
            }
        };

        AdvertiseData data = (new AdvertiseData.Builder()).setIncludeDeviceName(false).build();
        advertiser.startAdvertisingSet(parameters, data, null, null, null, advCallback);

        //###################################################################################################################

        mBluetoothLeScanner = adapter.getBluetoothLeScanner();

        scanFilter = new ScanFilter.Builder()
                .setServiceUuid(uuid)
                .build();

        filters.add(scanFilter);

        scanSettings = new ScanSettings.Builder()
                .setScanMode( ScanSettings.SCAN_MODE_LOW_LATENCY )
                .build();

        mScanCallback = new ScanCallback() {
            @Override
            public void onScanResult(int callbackType, ScanResult result) {
                super.onScanResult(callbackType, result);
                if (result == null
                        || result.getDevice() == null
                        || TextUtils.isEmpty(result.getDevice().getName()))
                    return;

                //StringBuilder builder = new StringBuilder("dev: ");
                //builder.append(result.getDevice().getName()).append(", d: ").append(new String(result.getScanRecord().getServiceData(result.getScanRecord().getServiceUuids().get(0)), Charset.forName("UTF-8")));



                //System.out.println(result);
                byte[] received = result.getScanRecord().getServiceData(result.getScanRecord().getServiceUuids().get(0));

                if(received == null || received.length < 3){
                    System.out.println("Received broken frame");
                    return;
                }

                //check if ping message
                if(received[0] == 1){
                    short device = (short)((received[1] << 8) + (0xFF & received[2]));
                    System.out.println("Get ping message from: " + device);
                    System.out.print("Frame: ");
                    for(int i=0;i<received.length;++i)
                        System.out.print(received[i] + " ");
                    System.out.println("");

                    int exists = 0;
                    for(int i=0;i<devicesInRange.size();++i){
                        if(devicesInRange.get(i).getDevice() == device){
                            exists = 1;
                            devicesInRange.get(i).setConnectionStatus(1);
                        }
                    }

                    if(exists == 0){
                        SimulatorData d = new SimulatorData(device);
                        d.setConnectionStatus(1);
                        devicesInRange.add(d);
                    }

                }else{
                    if(received.length < 10) return;

                    short device = (short)((received[1] << 8) + (0xFF & received[2]));

                    //check if device is a receiver
                    if(device == deviceAddress){
                        System.out.println("Received data: " + device  + "-------------------------------------------------------------------" );
                        for(int i=0;i<received.length;++i) {
                            System.out.print(received[i] & 0xFF);
                            System.out.print(" ");
                        }
                        System.out.println("");

                        device = (short)((received[3] << 8) + (0xFF & received[4]));

                        for(int i=0;i<devicesInRange.size();++i){
                            SimulatorData ss = devicesInRange.get(i);
                            if(ss.getDevice() == device && received.length >= 9){
                                ss.setConnectionStatus(1);
                                ss.setBeatsPerMinute((int)(received[5] & 0xFF));
                                ss.setBreathsPerMinute((int)(received[6] & 0xFF));
                                ss.setAbleToWalk((received[7] & 0x1) > 0 ? true : false);
                                ss.setExecutesCommand((received[7] & 0x2) > 0 ? true : false);
                                ss.setCapillaryRefill((received[8] / 10.0));

                                // in simulator data: 1 - red, 2 - green, 3 -yellow, 4 -black
                                //in arduino: 0- no, 1 - green, 2 - yellow, 3 -red, 4 -black

                                switch (received[9]){
                                    case 1: ss.setColor(2); break;
                                    case 2: ss.setColor(3); break;
                                    case 3: ss.setColor(1); break;
                                    case 4: ss.setColor(4); break;
                                    default: ss.setColor(0); break;
                                }
                            }
                        }
                    }
                }
            }

            @Override
            public void onBatchScanResults(List<ScanResult> results) {
                super.onBatchScanResults(results);
            }

            @Override
            public void onScanFailed(int errorCode) {
                super.onScanFailed(errorCode);
            }
        };
        mBluetoothLeScanner.startScan(filters, scanSettings, mScanCallback);

        //##############################################################################################
        class RemindTask extends TimerTask {
            public void run() {

                if(busy == 2){
                    busy = 0;
                    resetCommunication();
                    return;
                }

                if(busy > 0){
                    busy ++;
                    return;
                }

                if(devicesInRange.size() > 0 && busy == 0){
                    lastAsked += 1;
                    if(lastAsked >= devicesInRange.size()) lastAsked = 0;
                    askForAllParams(devicesInRange.get(lastAsked).getDevice());

                    System.out.println("########ask for all " + lastAsked + " " + devicesInRange.get(lastAsked).getDevice());
                }
            }
        }

        Timer timer = new Timer();
        timer.scheduleAtFixedRate(new RemindTask(),0, 3000);
    }

    public void sendPing(){
        ByteBuffer bb = ByteBuffer.allocate(3);
        bb.put((byte) 1); //add ping code
        bb.putShort(deviceAddress);
        //ParcelUuid _uuid = ParcelUuid.fromString("00000000-0000-1000-8000-00805F9B34FB");

        AdvertiseData data = new AdvertiseData.Builder()
            //.setIncludeDeviceName(true)
            .addServiceUuid(uuid)
            .addServiceData(uuid,bb.array())
            .build();

        Map<ParcelUuid, byte[]> m = data.getServiceData();
        System.out.println(data);
        System.out.println(Arrays.toString(m.get(uuid)));

        currentAdvertisingSet.setAdvertisingData(data);

    }

    public void sendConfirmFrame(){
        ByteBuffer bb = ByteBuffer.allocate(3);
        bb.put((byte) 4); //add ping code

        bb.putShort(lastReceiver);

        AdvertiseData data = new AdvertiseData.Builder()
                //.setIncludeDeviceName(true)
                .addServiceUuid(uuid)
                .addServiceData(uuid,bb.array())
                .build();

        send(data);
    }

    public void askForAllParams(short receiverAddress){
        ByteBuffer bb = ByteBuffer.allocate(5);
        bb.put((byte) 3); //add ask code
        bb.putShort(receiverAddress); //add ping code
        bb.putShort(deviceAddress); //add ping code

        lastReceiver = receiverAddress;

        System.out.println("########ask for all");
        System.out.println(receiverAddress);

        AdvertiseData data = new AdvertiseData.Builder()
                //.setIncludeDeviceName(true)
                .addServiceUuid(uuid)
                //.addServiceData(uuid, bb.array())
                .addServiceData(uuid, bb.array())
                .build();

        send(data);
    }

    private void send(AdvertiseData data){

        currentAdvertisingSet.setAdvertisingData(data);
    }

    /**
     * 'A' - bps
     * 'B' - breaths
     * 'D' - walking
     * 'E' - talking
     * 'G' - capillar refill
     */
    public void setParam(short receiverAddress, boolean ableToTalk,boolean ableToWalk, int breaths,  int beats, int capillar, int color, short newDevAddress){
        ByteBuffer bb = ByteBuffer.allocate(10);
        bb.put((byte) 4); //add ping code
        bb.putShort(receiverAddress); //add ping code

        bb.put((byte) beats); //add ping code
        bb.put((byte) breaths); //add ping code

        byte b = 0;
        b += (ableToWalk == true ? 1 : 0);
        b += (ableToTalk == true ? 2 : 0);

        bb.put(b); //add ping code

        bb.put((byte) capillar); //add ping code
        byte c = 0;
        switch (color){
            case 1: c = 3; break;
            case 2: c = 2; break;
            case 3: c = 1; break;
            case 4: c = 4; break;
        }
        bb.put(c); //add ping code
        bb.putShort(newDevAddress); //add ping code



        AdvertiseData data = new AdvertiseData.Builder()
            //.setIncludeDeviceName(true)
            .addServiceUuid(uuid)
            //.addServiceData(uuid, bb.array())
            .addServiceData(uuid, bb.array())
            .build();

        busy = 1;
        send(data);
    }


    private void resetCommunication(){
        busy = 0;
        sendPing();
        System.out.println("############resetCommunication###################");
    }
}
