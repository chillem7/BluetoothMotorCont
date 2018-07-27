package com.example.erikkalan.bluetoothcontrol;


import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.net.wifi.aware.PublishConfig;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.widget.SeekBar;
import android.widget.TextView;


import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.UUID;

public class MyBluetoothService {
    private static final UUID M_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    private static final String NAME = "BT_Service";
    private static final String TAG = "myBluetoothLog";
    public static final String KEY_TOAST = "toast";
    private final BluetoothAdapter bluetoothAdapter;
    private final Handler handler; // handler that gets info from Bluetooth service
    private int state;

    private AcceptThread acceptThread;
    private ConnectThread connectThread;
    private ConnectedThread connectedThread;

    public static final int STATE_NONE = 0;
    public static final int STATE_LISTEN = 1;
    public static final int STATE_CONNECTING = 2;
    public static final int STATE_CONNECTED = 3;



    // Defines several constants used when transmitting messages between the
    // service and the UI.
    ///*
    private interface MessageConst{

        public static final int MESSAGE_STATE_CHANGE = 1;
        public static final int MESSAGE_READ = 2;
        public static final int MESSAGE_WRITE = 3;
        public static final int MESSAGE_DEVICE_NAME = 4;
        public static final int MESSAGE_TOAST = 5;




    }
    //*/

    public MyBluetoothService(Context context, Handler handler){
        this.handler = handler;
        state = STATE_NONE;
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

    }

    private synchronized void setState(int state){
        this.state = state;
        handler.obtainMessage(MainActivity.MESSAGE_STATE_CHANGE, state, -1).sendToTarget();

    }

    public synchronized int getState(){
        return state;
    }

    public  synchronized void start() {
        // cancel thread attempting to make connection
        if (connectThread != null ){
            connectThread.cancel();
            connectThread = null;
        }
        // Cancel currently runnung thread
        if (connectedThread != null ){
            connectedThread.cancel();
            connectedThread = null;
        }
        // Cancel currently runnung thread
        if (acceptThread == null ){
            acceptThread = new AcceptThread();
            acceptThread.start();
        }

        setState(STATE_LISTEN);
    }

    public synchronized void connect(BluetoothDevice device){
        // cancel thread attempting to make connection
        if (state == STATE_CONNECTING && connectThread != null ){
            connectThread.cancel();
            connectThread = null;
        }
        // Cancel currently runnung thread
        if (connectedThread != null ){
            connectedThread.cancel();
            connectedThread = null;
        }
        // Start Connection
        connectThread = new ConnectThread(device);
        connectThread.start();
        setState(STATE_CONNECTING);

    }

    public synchronized void connected(BluetoothDevice device, BluetoothSocket socket){
        // cancel thread attempting to make connection
        if (connectThread != null ){
            connectThread.cancel();
            connectThread = null;
        }
        // Cancel currently runnung thread
        if (connectedThread != null ){
            connectedThread.cancel();
            connectedThread = null;
        }
        // Cancel currently runnung thread
        if (acceptThread != null ){
            acceptThread.cancel();
            acceptThread = null;
        }


        // Start Connection
        connectedThread = new ConnectedThread(socket);
        connectedThread.start();

        // Send name of device back to ui
        Message message = handler.obtainMessage(MainActivity.MESSAGE_DEVICE_NAME);
        Bundle bundle = new Bundle();
        bundle.putString(MainActivity.NAME, device.getName());
        message.setData(bundle);
        handler.sendMessage(message);

        setState(STATE_CONNECTING);

    }

    public synchronized void stop() {
        // cancel thread attempting to make connection
        if (connectThread != null) {
            connectThread.cancel();
            connectThread = null;
        }
        // Cancel currently runnung thread
        if (connectedThread != null) {
            connectedThread.cancel();
            connectedThread = null;
        }
        // Cancel currently runnung thread
        if (acceptThread != null) {
            acceptThread.cancel();
            acceptThread = null;
        }
        setState(STATE_NONE);
    }

    public void write(byte[] out) {
        ConnectedThread r;
        synchronized (this){
            if (state != STATE_CONNECTED){
                return;
            }
            r = connectedThread;
        }
        r.write(out);
    }

    public void connectionFailed() {
        setState(STATE_LISTEN);
        Message message = handler.obtainMessage(MainActivity.MESSAGE_TOAST);
        Bundle bundle = new Bundle();
        bundle.putString(MainActivity.KEY_TOAST, "unable to connect to device");
        message.setData(bundle);
        handler.sendMessage(message);
    }

    public void connectionLost() {
        setState(STATE_LISTEN);
        Message message = handler.obtainMessage(MainActivity.MESSAGE_TOAST);
        Bundle bundle = new Bundle();
        bundle.putString(MainActivity.KEY_TOAST, "connection was lost to device");
        message.setData(bundle);
        handler.sendMessage(message);
    }














    private class AcceptThread extends Thread{
        private final BluetoothServerSocket mmServerSocket;

        public AcceptThread() {
            BluetoothServerSocket temp = null;

            try {
                temp = bluetoothAdapter.listenUsingRfcommWithServiceRecord(NAME, M_UUID);
            }catch (IOException e){

            }
            mmServerSocket = temp;
        }


        public void cancel(){
            try {
                mmServerSocket.close();
            } catch (IOException e){

            }
        }

        public void run (){
            setName("AcceptThread");
            BluetoothSocket socket = null;
            //Listen to the server socket to see if connected
            while(state != STATE_CONNECTED){
                try{
                    //blocking call to return on connection
                    socket = mmServerSocket.accept();
                }catch (IOException e){
                    break;
                }
                //if connection was accepted
                if (socket != null ){
                    synchronized (MyBluetoothService.this) {
                        switch (state) {
                            case STATE_LISTEN: case STATE_CONNECTING:
                                // Normal Connect Thread
                                connected(socket.getRemoteDevice(), socket);
                                break;
                            case STATE_NONE: case STATE_CONNECTED:
                                // not ready or already connected
                                try {
                                    socket.close();

                                }catch (IOException e){

                                }
                                break;

                        }
                    }
                }
            }
        }

    }

    private class ConnectThread extends Thread{

        private final BluetoothSocket mmSocket;
        private final BluetoothDevice mmDevice;


        public ConnectThread(BluetoothDevice device){

            mmDevice = device;

            BluetoothSocket temp = null;
            // Get Socket for given bluetooth device
            try {
                temp = device.createRfcommSocketToServiceRecord(M_UUID);

            }catch (IOException e){

            }
            mmSocket = temp;
        }

        public void cancel(){
            try {
                mmSocket.close();
            } catch (IOException e){

            }
        }

        public void run(){
            setName("ConnectThread");
            // Always Cancel discoveries
            bluetoothAdapter.cancelDiscovery();
            // Make connection to the bluetooth socket
            try{
                mmSocket.connect();
            }catch (IOException e){
                connectionFailed();
                try{
                    mmSocket.close();

                }catch (IOException ee){

                }
                // Start the service over again
                MyBluetoothService.this.start();
                return;
            }
            // Reset the connect thread becuse done with it
            synchronized (MyBluetoothService.this){
                connectThread = null;
            }
        }



    }

    private class ConnectedThread extends Thread {
        private final BluetoothSocket mmSocket;
        private final InputStream mmInStream;
        private final OutputStream mmOutStream;
        private byte[] mmBuffer; // mmBuffer store for the stream

        //public ConnectedThread(){}

        public ConnectedThread(BluetoothSocket socket) {
            mmSocket = socket;
            InputStream tmpIn = null;
            OutputStream tmpOut = null;


            // Get the input and output streams; using temp objects because
            // member streams are final.
            try {
                tmpIn = socket.getInputStream();
            } catch (IOException e) {
                Log.e(TAG, "Error occurred when creating input stream", e);
            }
            try {
                tmpOut = socket.getOutputStream();
            } catch (IOException e) {
                Log.e(TAG, "Error occurred when creating output stream", e);
            }

            mmInStream = tmpIn;
            mmOutStream = tmpOut;
        }

        public void run() {
            setName("ConnectedThread");
            mmBuffer = new byte[1024];
            int numBytes; // bytes returned from read()

            // Keep listening to the InputStream until an exception occurs.
            while (true) {
                try {
                    // Read from the InputStream.
                    numBytes = mmInStream.read(mmBuffer);
                    // Send the obtained bytes to the UI activity.
                    Message readMsg = handler.obtainMessage(
                            MessageConst.MESSAGE_READ, numBytes, -1,
                            mmBuffer);
                    readMsg.sendToTarget();
                } catch (IOException e) {
                    Log.d(TAG, "Input stream was disconnected", e);
                    break;
                }
            }
        }

        // Call this from the main activity to send data to the remote device.
        public void write(byte[] bytes) {
            try {
                mmOutStream.write(bytes);


            } catch (IOException e) {
                Log.e(TAG, "Error occurred when sending data", e);

                // Send a failure message back to the activity.
                Message writeErrorMsg =
                        handler.obtainMessage(MainActivity.MESSAGE_TOAST);
                Bundle bundle = new Bundle();
                bundle.putString(KEY_TOAST,
                        "Couldn't send data to the other device");
                writeErrorMsg.setData(bundle);
                handler.sendMessage(writeErrorMsg);
            }
        }



        // Call this method from the main activity to shut down the connection.
        public void cancel() {
            try {
                mmSocket.close();
            } catch (IOException e) {
                Log.e(TAG, "Could not close the connect socket", e);
            }
        }
    }/*
    public MyBluetoothService(){}

    public MyBluetoothService(BluetoothSocket socket){
        connectedThread = new ConnectedThread(socket);
        //connectedThread.start();
    }

    public ConnectedThread getConnectedThread() {
        return connectedThread;
    }*/


}

