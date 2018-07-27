package com.example.erikkalan.bluetoothcontrol;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;

public class MyBluetoothTransmision {

    private static MyBluetoothTransmision instance = null;

    private static BluetoothSocket mmSocket;
    private static BluetoothDevice device;

    private MyBluetoothTransmision(){
    }

    public static MyBluetoothTransmision getInstance()
    {
        if (instance == null)
            instance = new MyBluetoothTransmision();

        return instance;
    }



    public static BluetoothSocket getMmSocket() {
        return mmSocket;
    }

    public static void setMmSocket(BluetoothSocket mmSocket) {
        MyBluetoothTransmision.mmSocket = mmSocket;
    }

    public static BluetoothDevice getDevice() {
        return device;
    }

    public static void setDevice(BluetoothDevice device) {
        MyBluetoothTransmision.device = device;
    }


}
