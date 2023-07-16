package com.example.btplayback;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.database.ContentObserver;
import android.database.Cursor;
import android.media.AudioManager;
import android.media.MediaExtractor;
import android.media.MediaPlayer;
import android.media.MediaRouter;
import android.media.VolumeProvider;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.provider.Settings;
import android.util.Log;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.UUID;

public class MainActivity extends AppCompatActivity implements MyAdapter.DeviceSelector, MainInterface{

    private BluetoothAdapter bluetoothAdapter;
    private static final String[] PERMS = {Manifest.permission.ACCESS_FINE_LOCATION,Manifest.permission.ACCESS_COARSE_LOCATION};
    public static final String[] BTPERMS = {Manifest.permission.BLUETOOTH_SCAN};
    private ArrayList<BluetoothDevice> btDevices = new ArrayList<>();
    private BluetoothConnectionService bluetoothConnectionService;
    private RecyclerView recyclerView;
    private MyAdapter adapter;
    private static final String TAG = "MainActivity";
    private BluetoothDevice device;
    private static final UUID MY_INSECURE_UUID = UUID.fromString("8ce255c0-200a-11e0-ac64-0800200c9a66");
    private int sendIndex = 0;
    private ProgressDialog progressDialog;
    private String status;

    public static final String KEY_CLIENT = "client";
    public static final String KEY_SERVER = "server";

    public static final String KEY_OXY = "Oxygen";
    public static final String KEY_TURB = "Turbidity";
    public static final String KEY_TDS = "TDS";
    public static final String KEY_TEMP = "Temperature";
    public static final String KEY_PH = "Ph";

    private final BroadcastReceiver mBroadcastReceiver1 = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if(action.equals(BluetoothAdapter.ACTION_STATE_CHANGED)){
                int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE,BluetoothAdapter.ERROR);
                switch (state){
                    case BluetoothAdapter.STATE_OFF:{
                        Log.d(TAG, "onReceive: STATE_OFF");
                        break;
                    }
                    case BluetoothAdapter.STATE_ON:{
                        Log.d(TAG, "onReceive: STATE_ON");
                        enableDiscoverable();
                        break;
                    }
                    case BluetoothAdapter.STATE_TURNING_OFF:{
                        Log.d(TAG, "onReceive: STATE_TURNING_OFF");
                        break;
                    }
                    case BluetoothAdapter.STATE_TURNING_ON:{
                        Log.d(TAG, "onReceive: STATE_TURNING_ON");
                        break;
                    }
                }
            }

        }
    };

    private final BroadcastReceiver mBroadcastReceiver2 = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if(action.equals(BluetoothAdapter.ACTION_SCAN_MODE_CHANGED)){
                int state = intent.getIntExtra(BluetoothAdapter.EXTRA_SCAN_MODE,BluetoothAdapter.ERROR);
                switch (state) {
                    case BluetoothAdapter.SCAN_MODE_CONNECTABLE:{
                        Log.d(TAG, "onReceive: SCAN_MODE_CONNECTABLE");
                        break;
                    }
                    case BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE:{
                        Log.d(TAG, "onReceive: SCAN_MODE_CONNECTABLE_DISCOVERABLE");
                        requestPerm();
                        bluetoothAdapter.startDiscovery();
                        break;
                    }
                    case BluetoothAdapter.SCAN_MODE_NONE:{
                        Log.d(TAG, "onReceive: SCAN_MODE_NONE");
                        break;
                    }
                    case BluetoothAdapter.STATE_CONNECTING:{
                        Log.d(TAG, "onReceive: STATE_CONNECTING");
                        break;
                    }
                    case BluetoothAdapter.STATE_CONNECTED:{
                        Log.d(TAG, "onReceive: STATE_CONNECTED");
                        break;
                    }
                }
            }
        }
    };

    private final BroadcastReceiver mBroadcastReceiver3 = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if(action.equals(BluetoothDevice.ACTION_FOUND)){
                Log.d(TAG, "onReceive: found a device");
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                btDevices.add(device);
                adapter.notifyDataSetChanged();
            }
        }
    };

    private final BroadcastReceiver mBroadcastReceiver4 = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if(action.equals(BluetoothDevice.ACTION_BOND_STATE_CHANGED)){
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                if(device.getBondState() == BluetoothDevice.BOND_BONDED){
                    Log.d(TAG, "onReceive: BOND_BONDED");
                }else if(device.getBondState() == BluetoothDevice.BOND_BONDING){
                    Log.d(TAG, "onReceive: BOND_BONDING");
                }else if(device.getBondState() == BluetoothDevice.BOND_NONE){
                    Log.d(TAG, "onReceive: BOND_NONE");
                }
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        recyclerView = findViewById(R.id.recyclerView);
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        registerReceivers();
        initRecyclerView();

        if (!bluetoothAdapter.isEnabled()){
            enableBT();
        }else if(!bluetoothAdapter.isDiscovering()){
            enableDiscoverable();
        }
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        byteArrayOutputStream.toByteArray();
    }

    private void enableBT(){
        Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
        startActivity(intent);
    }

    private void enableDiscoverable(){
        Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
        intent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION,200);
        startActivity(intent);
    }

    private void registerReceivers(){
        IntentFilter filter1 = new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED);
        registerReceiver(mBroadcastReceiver1,filter1);

        IntentFilter filter2 = new IntentFilter(BluetoothAdapter.ACTION_SCAN_MODE_CHANGED);
        registerReceiver(mBroadcastReceiver2,filter2);

        IntentFilter filter3 = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        registerReceiver(mBroadcastReceiver3,filter3);

        IntentFilter filter4 = new IntentFilter(BluetoothDevice.ACTION_BOND_STATE_CHANGED);
        registerReceiver(mBroadcastReceiver4,filter4);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        try {
            unregisterReceiver(mBroadcastReceiver1);
            unregisterReceiver(mBroadcastReceiver2);
            unregisterReceiver(mBroadcastReceiver3);
            unregisterReceiver(mBroadcastReceiver4);
        } catch (Exception e) {
            Log.d(TAG, "onDestroy: "+e.getMessage());
        }
    }

    private void initRecyclerView(){
        adapter = new MyAdapter(btDevices,this,this);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);
    }

    @Override
    public void onDeviceSelected(int position) {
        Log.d(TAG, "onDeviceSelected: selected");
        bluetoothAdapter.cancelDiscovery();
        device = btDevices.get(position);
        device.createBond();
        bluetoothConnectionService = new BluetoothConnectionService(this,this);
        getSupportFragmentManager().beginTransaction().replace(R.id.main_container,new SelectorFragment()).commit();
        btDevices.clear();
        adapter.notifyDataSetChanged();
    }

    private void requestPerm(){
        if(!(ContextCompat.checkSelfPermission(this,Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) ||
                !(ContextCompat.checkSelfPermission(this,Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED)){
            requestPermissions(PERMS,0);
        }

        if(!(ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_SCAN) == PackageManager.PERMISSION_GRANTED)){
            requestPermissions(BTPERMS, 1);
        }
    }

    @Override
    public void onAuxSelected() {
        status = KEY_SERVER;
        bluetoothConnectionService.startConnectThread(device,MY_INSECURE_UUID);
    }

    @Override
    public void onBtSelected() {
        status = KEY_CLIENT;
        progressDialog = ProgressDialog.show(this,"Fetching songs","Please Wait..",true);
    }

    @Override
    public void onBTConnected() {
        Log.d(TAG, "onBTConnected: " + status);
        if(status == null){
            getSupportFragmentManager().beginTransaction().replace(R.id.main_container, new WriteFragment()).commit();
        }else{
            getSupportFragmentManager().beginTransaction().replace(R.id.main_container, new SongListFragment(), "songlist").commit();
        }
    }

    @Override
    public void sendData(String data) {
        bluetoothConnectionService.write(data.getBytes(Charset.defaultCharset()));
    }

    @Override
    public void updateTemp(String val) {
        ((SongListFragment) getSupportFragmentManager().findFragmentByTag("songlist")).updateTemp(val);
    }

    @Override
    public void updatePh(String val) {
        ((SongListFragment) getSupportFragmentManager().findFragmentByTag("songlist")).updateph(val);
    }

    @Override
    public void updateOxy(String val) {
        ((SongListFragment) getSupportFragmentManager().findFragmentByTag("songlist")).updateoxy(val);
    }

    @Override
    public void updateTurb(String val) {
        ((SongListFragment) getSupportFragmentManager().findFragmentByTag("songlist")).updateturb(val);
    }

    @Override
    public void updatetds(String val) {
        ((SongListFragment) getSupportFragmentManager().findFragmentByTag("songlist")).updateTds(val);
    }
}