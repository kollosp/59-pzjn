package com.example.a58_androidtest;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.le.AdvertiseCallback;
import android.bluetooth.le.AdvertiseData;
import android.bluetooth.le.AdvertiseSettings;
import android.bluetooth.le.BluetoothLeAdvertiser;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.content.Intent;
import android.os.Build;
import android.os.Handler;
import android.os.ParcelUuid;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import java.io.Serializable;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

import androidx.annotation.RequiresApi;

@RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
public class BTManager implements Serializable {

    //advertising
    AdvertiseSettings advertiserSettings;

    ParcelUuid uuid;
    BluetoothAdapter adapter;
    BluetoothLeAdvertiser advertiser;
    AdvertiseCallback advertisingCallback;

    //discovering
    BluetoothLeScanner mBluetoothLeScanner;
    Handler mHandler = new Handler();
    ScanFilter scanFilter;
    ScanSettings scanSettings;
    ArrayList<ScanFilter> filters = new ArrayList<ScanFilter>();

    Integer advertiserStatus = 0; //0 - free; 1 - busy
    Short deviceAddress;
    ArrayList<String> receivedData = new ArrayList<String>();
    ScanCallback mScanCallback;

    ArrayList<SimulatorData> devicesInRange = new ArrayList<SimulatorData>();

    ArrayList<AdvertiseData> toBeSend = new ArrayList<AdvertiseData>();
    short lastReceiver = 0;
    int lastAskedSimulator = 0;

    //ex: "00000000-0000-1000-8000-00805F9B34FB"
    public BTManager(String _uuid){
        setUuid(_uuid);
    }

    public void setUuid(String _uuid){
        uuid = ParcelUuid.fromString("18150000-0000-1000-8000-00805F9B34FB"); //- working
        //uuid = new ParcelUuid(UUID.nameUUIDFromBytes(byte_name));
    }

    ArrayList<SimulatorData> getDevicesInRange() {
        return devicesInRange;
    }

    public void init(String devName, final Short deviceAddress) throws Exception {
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
        mBluetoothLeScanner = adapter.getBluetoothLeScanner();

        advertiserSettings = new AdvertiseSettings.Builder()
            .setAdvertiseMode( AdvertiseSettings.ADVERTISE_MODE_LOW_LATENCY )
            .setTxPowerLevel( AdvertiseSettings.ADVERTISE_TX_POWER_HIGH )
            .setConnectable( false )
            .build();

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
                    short device = (short)((received[1] << 8) + received[2]);
                    System.out.println("Get ping message from: " + device);
                    System.out.print("Frame: ");
                    for(int i=0;i<received.length;++i)
                        System.out.print(received[i] + " ");
                    System.out.println("");

                    int exists = 0;
                    for(int i=0;i<devicesInRange.size();++i){
                        if(devicesInRange.get(i).getDevice() == device){
                            exists = 1;
                        }
                    }

                    if(exists == 0){
                        devicesInRange.add(new SimulatorData(device));
                    }

                }else{
                    short device = (short)((received[1] << 8) + received[2]);

                    //check if device is a receiver
                    if(device == deviceAddress){
                        System.out.println("Received data: " + device  + "-------------------------------------------------------------------" );
                        for(int i=0;i<received.length;++i) {
                            System.out.print(received[i] & 0xFF);
                            System.out.print(" ");
                        }
                        System.out.println("");

                        device = (short)((received[3] << 8) + received[4]);

                        for(int i=0;i<devicesInRange.size();++i){
                            SimulatorData ss = devicesInRange.get(i);
                            if(ss.getDevice() == device && received.length >= 9){
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

                //receivedData.add(builder.toString());
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

        startDiscovering();

    }

    public void startDiscovering(){
        mBluetoothLeScanner.startScan(filters, scanSettings, mScanCallback);
        /*mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                mBluetoothLeScanner.stopScan(mScanCallback);
            }
        }, 10000);*/
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

    //max frame size is 31 bytes
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

        send(data);
    }


    /**
     * 'A' - bps
     * 'B' - breaths
     * 'D' - walking
     * 'E' - talking
     * 'G' - capillar refill
     * @param type
     */
    public void setParam(short receiverAddress, char type, char value){
        ByteBuffer bb = ByteBuffer.allocate(6);
        bb.put((byte) 1); //add ping code
        bb.putShort(receiverAddress ); //add ping code
        //bb.put((byte) receiverAddress); //add ping code
        bb.put((byte) type); //add ping code
        bb.put((byte) value); //add ping code
        bb.put((byte) '#'); //add ping code

        AdvertiseData data = new AdvertiseData.Builder()
                //.setIncludeDeviceName(true)
                .addServiceUuid(uuid)
                //.addServiceData(uuid, bb.array())
                .addServiceData(uuid, bb.array())
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

    public void send(AdvertiseData data){

        //null only when is run from advertising callback
        if(data != null) {
            toBeSend.add(data);
        }

        advertisingCallback = new AdvertiseCallback() {
            @Override
            public void onStartSuccess(AdvertiseSettings settingsInEffect) {
                super.onStartSuccess(settingsInEffect);
                //stop advertising after frame send

                System.out.println("stop pinging started " + advertiserStatus);

                if(advertiserStatus  == 1) {
                    advertiserStatus = 0;
                    advertiser.stopAdvertising(advertisingCallback);
                    if(toBeSend.size() > 0) {
                        System.out.println("Sending more frames. left: " + toBeSend.size());
                        send(null);
                    }
                }
            }

            @Override
            public void onStartFailure(int errorCode) {
                super.onStartFailure(errorCode);
                System.out.println("onStartFailure: errorCode: " + errorCode);

                if(advertiserStatus  == 1) {
                    advertiserStatus = 0;
                }

                toBeSend.clear();
            }
        };

        //stop advertising after 0.5s
        /*mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {

                askForAllParams(sim.getDevice());
            }
        }, 1000);*/


        System.out.println("send");
        System.out.println(toBeSend.get(0));

        advertiser.startAdvertising(advertiserSettings, toBeSend.get(0), advertisingCallback);
        advertiserStatus = 1;
        toBeSend.remove(0);

    }
}
