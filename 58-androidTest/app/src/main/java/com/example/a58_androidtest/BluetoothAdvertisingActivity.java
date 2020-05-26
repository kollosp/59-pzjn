package com.example.a58_androidtest;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.le.AdvertiseCallback;
import android.bluetooth.le.AdvertiseData;
import android.bluetooth.le.AdvertiseSettings;
import android.bluetooth.le.AdvertisingSet;
import android.bluetooth.le.AdvertisingSetCallback;
import android.bluetooth.le.AdvertisingSetParameters;
import android.bluetooth.le.BluetoothLeAdvertiser;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.ParcelUuid;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

@RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
public class BluetoothAdvertisingActivity extends AppCompatActivity {

    private final static int REQUEST_ENABLE_BT = 1;
    private final static String DEVICE_NAME_ADVERTISER = "PZadv";
    private final static String ADVERTISER_DATA_TO_SEND = "some data";
    private final static String DEVICE_NAME_SCANNER = "PZ scanner";
    private static final String LOG_TAG = "Advertising";

    //advertising
    AdvertiseSettings settings;
    AdvertiseData data;
    ParcelUuid puuid = ParcelUuid.fromString("00000000-0000-1000-8000-00805F9B34FB");
    BluetoothAdapter adapter;
    BluetoothLeAdvertiser advertiser;
    AdvertiseCallback advertisingCallback;

    //discovering
    BluetoothLeScanner mBluetoothLeScanner;
    Handler mHandler = new Handler();
    ScanFilter scanFilter;
    ScanSettings scanSettings;
    ArrayList<ScanFilter> filters = new ArrayList<ScanFilter>();

    int currentMode = 0; //0-none, 1-adv., 2-scan

    private ScanCallback mScanCallback;
    LinearLayout scanLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bluetooth_advertising);
        scanLayout = (LinearLayout) findViewById(R.id.scanLayout);

        initBt();
    }

    public void addTextToLayout(LinearLayout layout, String text){
        TextView textView = new TextView(BluetoothAdvertisingActivity.this);
        textView.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT));
        textView.setText(text);
        layout.addView(textView);
    }

    public void initBt(){
        adapter = BluetoothAdapter.getDefaultAdapter();

        //check if device supports bt
        if (adapter == null) {
            // Device doesn't support Bluetooth
            Log.i(LOG_TAG, "Bt not supported");
            return;
        }

        //ask user to run bt
        if (!adapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        }

        Toast.makeText(getApplicationContext(),"BT on", Toast.LENGTH_SHORT).show();

        //Needed for let the protocol work
        adapter.setName(DEVICE_NAME_ADVERTISER);
        advertiser = adapter.getBluetoothLeAdvertiser();
        mBluetoothLeScanner = adapter.getBluetoothLeScanner();

        //Configure advertiser
        settings = new AdvertiseSettings.Builder()
                .setAdvertiseMode( AdvertiseSettings.ADVERTISE_MODE_LOW_LATENCY )
                .setTxPowerLevel( AdvertiseSettings.ADVERTISE_TX_POWER_HIGH )
                .setConnectable( false )
                .build();

        //Uuid is another important part of our protocol
        data = new AdvertiseData.Builder()
                .setIncludeDeviceName(true)
                .addServiceUuid(puuid)
                .addServiceData(puuid, ADVERTISER_DATA_TO_SEND.getBytes(Charset.forName( "UTF-8" )))
                .build();


        advertisingCallback = new AdvertiseCallback() {
            @Override
            public void onStartSuccess(AdvertiseSettings settingsInEffect) {
                Toast.makeText(getApplicationContext(),"Advertising", Toast.LENGTH_SHORT).show();
                super.onStartSuccess(settingsInEffect);
            }

            @Override
            public void onStartFailure(int errorCode) {
                Toast.makeText(getApplicationContext(),"BT adv. error " + errorCode, Toast.LENGTH_SHORT).show();
                super.onStartFailure(errorCode);
            }
        };


        scanFilter = new ScanFilter.Builder()
            .setServiceUuid(puuid)
            .build();

        filters.add(scanFilter);

        scanSettings = new ScanSettings.Builder()
            .setScanMode( ScanSettings.SCAN_MODE_LOW_LATENCY )
            .build();

        mScanCallback = new ScanCallback() {
            @Override
            public void onScanResult(int callbackType, ScanResult result) {
                super.onScanResult(callbackType, result);
                if( result == null
                        || result.getDevice() == null
                        || TextUtils.isEmpty(result.getDevice().getName()) )
                    return;

                StringBuilder builder = new StringBuilder("dev: ");
                builder.append(result.getDevice().getName()).append(", d: ").append(new String(result.getScanRecord().getServiceData(result.getScanRecord().getServiceUuids().get(0)), Charset.forName("UTF-8")));
                addTextToLayout(scanLayout, "Received: " + builder.toString());
            }

            @Override
            public void onBatchScanResults(List<ScanResult> results) {
                Toast.makeText(getApplicationContext(), "batch scan", Toast.LENGTH_SHORT).show();
                super.onBatchScanResults(results);
            }

            @Override
            public void onScanFailed(int errorCode) {
                addTextToLayout(scanLayout, "Discovery onScanFailed: " + errorCode);
                super.onScanFailed(errorCode);
            }
        };

    }

    public void reset(){
        if(currentMode == 2){
            mBluetoothLeScanner.stopScan(mScanCallback);
        }
        else if(currentMode == 1){
            advertiser.stopAdvertising(advertisingCallback);
        }
    }

    public void startAdvertising(View view){
        reset();
        currentMode = 1;

        advertiser.startAdvertising(settings, data, advertisingCallback);
        addTextToLayout(scanLayout, "Advertising...");
        Toast.makeText(getApplicationContext(),"Advertising...", Toast.LENGTH_SHORT).show();

        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                addTextToLayout(scanLayout, "Advertising finished");
                advertiser.stopAdvertising(advertisingCallback);
                currentMode = 0;
            }
        }, 3000);
    }

    public void startDiscovering(View view){
        reset();
        currentMode = 2;
        mBluetoothLeScanner.startScan(filters, scanSettings, mScanCallback);
        addTextToLayout(scanLayout, "Scanning...");

        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                addTextToLayout(scanLayout, "Scan finished");
                mBluetoothLeScanner.stopScan(mScanCallback);
                currentMode = 0;
            }
        }, 10000);
    }


    public void clearLog(View view) {
        scanLayout.removeAllViews();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

    }
}
