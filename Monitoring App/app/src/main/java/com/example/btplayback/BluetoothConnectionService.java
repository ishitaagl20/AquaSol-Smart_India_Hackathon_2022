package com.example.btplayback;

import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.util.UUID;

public class BluetoothConnectionService {

    private BluetoothAdapter bluetoothAdapter;
    private BluetoothDevice mDevice;
    private static final UUID MY_INSECURE_UUID = UUID.fromString("8ce255c0-200a-11e0-ac64-0800200c9a66");
    private Context mContext;
    private AcceptThread mAcceptThread;
    private ProgressDialog progressDialog;
    private static final String APPNAME = "MYAPP";
    private UUID mUUID;
    private ConnectThread mConnectThread;
    private ConnectedThread mConnectedThread;
    private static final String RECEIVED_SONG = "Song Received";
    private static final String RECEIVED_ALL_SONGS = "Received all songs";
    private MainInterface mainInterface;
    private static final String PLAYSONG = "Play Song: ";
    private static final String VOLUME_CHANGE = "Volume: ";
    private static final String PAUSEPLAY = "pause_play";

    private static final String TAG = "BLuetoothConnectionServ";

    public BluetoothConnectionService(Context mContext,MainInterface mainInterface){
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        this.mContext = mContext;
        this.mainInterface = mainInterface;
        initiateAcceptThread();
    }

    public class AcceptThread extends Thread{

        private final BluetoothServerSocket mmServerSocket;

        public AcceptThread(){
            BluetoothServerSocket tmp = null;
            try {
                tmp = bluetoothAdapter.listenUsingInsecureRfcommWithServiceRecord(APPNAME, MY_INSECURE_UUID);
                Log.d(TAG, "AcceptThread: setting up server using: "+ MY_INSECURE_UUID);
            } catch (IOException e) {
                Log.d(TAG, "AcceptThread: "+e.getMessage());
            }
            mmServerSocket = tmp;

        }

        public void run(){
            Log.d(TAG, "run: Accept Thread is running");
            BluetoothSocket socket = null;
            try {
                Log.d(TAG, "run: RFCOM server socket start");
                socket = mmServerSocket.accept();
                Log.d(TAG, "run: RFCOM server socket accepted connection");
            } catch (IOException e) {
                Log.d(TAG, "run: "+e.getMessage());
            }

            if(socket != null){
                connected(socket,mDevice);
            }
        }

        public void cancel(){
            Log.d(TAG, "cancel: Cancelling Accept Thread");

            try {
                mmServerSocket.close();
            } catch (IOException e) {
                Log.d(TAG, "cancel: Close of AcceptThread ServerSocket failed "+e.getMessage());
            }
        }
    }
    
    public class ConnectThread extends Thread{
        private BluetoothSocket socket;
        
        public ConnectThread(BluetoothDevice device,UUID uuid){
            Log.d(TAG, "ConnectThread: started");
            mDevice = device;
            mUUID = uuid;
        }
        
        public void run(){
            BluetoothSocket tmp = null;
            Log.d(TAG, "run: connectThread si running");

            try {
                Log.d(TAG, "run: trying to create insecurerfcomsocket usin UUID: " + MY_INSECURE_UUID);
                tmp = mDevice.createRfcommSocketToServiceRecord(mUUID);
            } catch (IOException e) {
                Log.d(TAG, "run: could not create rfcom socket "+e.getMessage());
            }

            socket = tmp;
            bluetoothAdapter.cancelDiscovery();

            try {
                socket.connect();
                Log.d(TAG, "run: connect thread connected");
            } catch (IOException e) {
                try {
                    socket.close();
                } catch (IOException ioException) {
                    Log.d(TAG, "run: unable to close connection in socket "+ioException.getMessage());
                }
                Log.d(TAG, "run: socket was closed");
                Log.d(TAG, "run: could not connect to uuid: "+MY_INSECURE_UUID);
            }
            connected(socket,mDevice);
        }

        public void cancel(){
            try {
                Log.d(TAG, "cancel: closing client socket");
                socket.close();
            } catch (IOException e) {
                Log.d(TAG, "cancel: close of socket in connectThreead failed "+e.getMessage());
            }
        }
    }

    public class ConnectedThread extends Thread{
        private BluetoothSocket socket;
        private InputStream inputStream;
        private OutputStream outputStream;

        public ConnectedThread(BluetoothSocket socket){
            Log.d(TAG, "Connectedthread: starting");

            this.socket = socket;
            InputStream tmpIn = null;
            OutputStream tmpOut = null;

            try {
                progressDialog.dismiss();
            } catch (Exception e) {
                Log.d(TAG, "Connectedthread: nullPointerException");
            }

            try {
                tmpIn = socket.getInputStream();
                tmpOut = socket.getOutputStream();
            } catch (IOException e) {
                Log.d(TAG, "Connectedthread: ");
            }

            inputStream = tmpIn;
            outputStream = tmpOut;
        }

        public void run(){
            byte[] buffer = new byte[1024];
            int bytes;

            while (true){
                try {
                    bytes = inputStream.read(buffer);
                    String incomingMessage = new String(buffer,0,bytes);
                    Log.d(TAG, "run: incomingMessage: "+incomingMessage);
                    switch (incomingMessage.charAt(0)){
                        case '1':{
                            mainInterface.updateTemp(incomingMessage.substring(1));
                            break;
                        }
                        case '2':{
                            mainInterface.updatePh(incomingMessage.substring(1));
                            break;
                        }
                        case '3':{
                            mainInterface.updateTurb(incomingMessage.substring(1));
                            break;
                        }
                        case '4':{
                            mainInterface.updateOxy(incomingMessage.substring(1));
                            break;
                        }
                        case '5':{
                            mainInterface.updatetds(incomingMessage.substring(1));
                            break;
                        }
                    }

                } catch (IOException e) {
                    Log.d(TAG, "run: error reading input stream");
                    break;
                }
            }
        }

        public void write(byte[] bytes){
            String text = new String(bytes, Charset.defaultCharset());
            Log.d(TAG, "write: writing to output stream: "+text);
            try {
                outputStream.write(bytes);
            } catch (IOException e) {
                Log.d(TAG, "write: error writing to output stream "+e.getMessage());
            }
        }

        public void cancel(){
            try {
                socket.close();
            } catch (IOException e) {
                Log.d(TAG, "cancel: "+e.getMessage());
            }
        }
    }

    public void write(byte[] bytes){
        mConnectedThread.write(bytes);
    }

    private void connected(BluetoothSocket socket,BluetoothDevice device){
        mConnectedThread = new ConnectedThread(socket);
        mConnectedThread.start();
        mainInterface.onBTConnected();
    }

    public synchronized void initiateAcceptThread(){
        Log.d(TAG, "initiateAcceptThread: started");

        if(mConnectThread != null){
            mConnectThread.cancel();
            mConnectThread = null;
        }

        if(mAcceptThread == null){
            mAcceptThread = new AcceptThread();
            mAcceptThread.start();
        }
    }

    public void startConnectThread(BluetoothDevice device,UUID uuid){
        Log.d(TAG, "startConnectThread: started");
        progressDialog = ProgressDialog.show(mContext,"Connecting Bluetooth","Please Wait..",true);
        mConnectThread = new ConnectThread(device,uuid);
        mConnectThread.start();
    }

}
