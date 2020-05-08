package com.example.a58_androidtest;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.LinearLayout.LayoutParams;

import java.util.Set;

public class BluetoothActivity extends AppCompatActivity {

    private final static int REQUEST_ENABLE_BT = 1;

    //broadcast receiver used to scan
    BluetoothAdapter bluetoothAdapter;
    Set<BluetoothDevice> pairedDevices;
    LinearLayout scanLayout;

    private final BroadcastReceiver receiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();

        switch(action){
            case BluetoothDevice.ACTION_FOUND:
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                String deviceName = device.getName();
                String deviceHardwareAddress = device.getAddress(); // MAC address
                addTextToLayout(scanLayout, "Device found: " +  deviceName + " " + deviceHardwareAddress);
                break;
            case BluetoothAdapter.ACTION_DISCOVERY_FINISHED:
                addTextToLayout(scanLayout, "Scan finished");
                break;
            case BluetoothAdapter.ACTION_STATE_CHANGED:
                addTextToLayout(scanLayout, "BT adapter state changed to: " + (new Integer(bluetoothAdapter.getState())).toString());
                break;
            case BluetoothDevice.ACTION_PAIRING_REQUEST:
                addTextToLayout(scanLayout, "Pairing request");
                break;
        }
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bluetooth);

        //ask user for permissions to access bt and scan
        int MY_PERMISSIONS_REQUEST_ACCESS_COARSE_LOCATION = 1;
        ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},
                MY_PERMISSIONS_REQUEST_ACCESS_COARSE_LOCATION);
        ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.BLUETOOTH},
                MY_PERMISSIONS_REQUEST_ACCESS_COARSE_LOCATION);
        ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.BLUETOOTH_ADMIN},
                MY_PERMISSIONS_REQUEST_ACCESS_COARSE_LOCATION);


        scanLayout = (LinearLayout) findViewById(R.id.scanLayout);

        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        //when bluetooth changes from on to off
        filter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);
        //when another device requests pairing with this device
        filter.addAction(BluetoothDevice.ACTION_PAIRING_REQUEST);

        registerReceiver(receiver, filter);

        initBt();
    }

    public void initBt(){
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        //check if device supports bt
        if (bluetoothAdapter == null) {
            // Device doesn't support Bluetooth
            addTextToLayout(scanLayout, "BT is not supported");
        }

        addTextToLayout(scanLayout, "Device supports BT");


    }

    public void listPairedDevices(View view){
        pairedDevices = bluetoothAdapter.getBondedDevices();

        //list paired devices
        if (pairedDevices.size() > 0) {
            addTextToLayout(scanLayout, "Paired devices:");
            for (BluetoothDevice device : pairedDevices) {
                String deviceName = device.getName();
                String deviceHardwareAddress = device.getAddress(); // MAC address

                addTextToLayout(scanLayout, deviceName + " " + deviceHardwareAddress);
            }
        }
    }

    public void addTextToLayout(LinearLayout layout, String text){
        TextView textView = new TextView(BluetoothActivity.this);
        textView.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT));
        textView.setText(text);
        layout.addView(textView);
    }

    public void btOn(View view){
        //ask user to run bt
        if (!bluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);

            addTextToLayout(scanLayout, "Turned on");
        }

        addTextToLayout(scanLayout, "Device is on");
    }

    public void btOff(View view){
        bluetoothAdapter.disable();
        addTextToLayout(scanLayout, "Turned off");
    }

    public void btScan(View view){
        if(bluetoothAdapter.getState() != BluetoothAdapter.STATE_ON){
            addTextToLayout(scanLayout, "Device is turned off");
            return;
        }

        if(bluetoothAdapter.isDiscovering()){
            addTextToLayout(scanLayout, "Already scanning");
            return;
        }

        //start scanning - it takes about 12 s
        if(bluetoothAdapter.startDiscovery()){
            addTextToLayout(scanLayout, "Scanning...");
        }else{
            addTextToLayout(scanLayout, "Scan error");
        }
    }

    public void btMakeDeviceDiscoverable(View view){
        //makes device discoverable for 300s
        Intent discoverableIntent =
                new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
        discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300);
        startActivity(discoverableIntent);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        // Don't forget to unregister the ACTION_FOUND receiver.
        unregisterReceiver(receiver);
    }
}
