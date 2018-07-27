package com.example.erikkalan.bluetoothcontrol;


import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
//import com.javacodegeeks.R;
import java.util.Set;



public class ManageBluetooth extends AppCompatActivity {

    // Return Intent extra
    public static String EXTRA_DEVICE_ADDRESS = "device_address";
    // Member fields
    private BluetoothAdapter bluetoothAdapter;
    private ArrayAdapter mPairedDevicesArrayAdapter;
    private ArrayAdapter mNewDevicesArrayAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_bluetooth);


        //btListView = (ListView)findViewById(R.id.btListView);

        // Set result CANCELED incase the user backs out
        // Initialize the button to perform device discovery
        Button visibleBtn = (Button) findViewById(R.id.visibleBtn);
        visibleBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                doDiscovery();
                v.setVisibility(View.GONE);
            }
        });


        // Initialize array adapters. One for already paired devices and
        // one for newly discovered devices
        mPairedDevicesArrayAdapter = new ArrayAdapter(this, R.layout.device_name);
        mNewDevicesArrayAdapter = new ArrayAdapter(this, R.layout.device_name);

        // Find and set up the ListView for paired devices
        ListView pairedListView = (ListView) findViewById(R.id.paired_devices);
        pairedListView.setAdapter(mPairedDevicesArrayAdapter);
        pairedListView.setOnItemClickListener(mDeviceClickListener);

        // Find and set up the ListView for newly discovered devices
        ListView newDevicesListView = (ListView) findViewById(R.id.new_devices);
        newDevicesListView.setAdapter(mNewDevicesArrayAdapter);
        newDevicesListView.setOnItemClickListener(mDeviceClickListener);

            // Register for broadcasts when a device is discovered
        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        this.registerReceiver(mReceiver, filter);

            // Register for broadcasts when discovery has finished
        filter = new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        this.registerReceiver(mReceiver, filter);

            // Get the local Bluetooth adapter
        mBtAdapter = BluetoothAdapter.getDefaultAdapter();
            // Get a set of currently paired devices
        Set pairedDevices = mBtAdapter.getBondedDevices();
            // If there are paired devices, add each one to the ArrayAdapter
        if (pairedDevices.size() > 0) {
            findViewById(R.id.title_paired_devices).setVisibility(View.VISIBLE);
            for (BluetoothDevice device : pairedDevices) {
            }
        } else {
            mPairedDevicesArrayAdapter.add(device.getName() + "\n" + device.getAddress ←􏰀 ());
            String noDevices = getResources().getText(R.string.none_paired).toString();
            mPairedDevicesArrayAdapter.add(noDevices);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Make sure we’re not doing discovery anymore
        if (mBtAdapter != null) {
            mBtAdapter.cancelDiscovery();
        }
        // Unregister broadcast listeners
        this.unregisterReceiver(mReceiver);
    }

    //
    // Start device discover with the BluetoothAdapter
    //
    private void doDiscovery() {

        //setTitle(R.string.scanning);
        // Turn on sub-title for new devices
        findViewById(R.id.title_new_devices).setVisibility(View.VISIBLE);
        // If we’re already discovering, stop it
        if (bluetoothAdapter.isDiscovering()) {
            bluetoothAdapter.cancelDiscovery();
        }
        // Request discover from BluetoothAdapter
        bluetoothAdapter.startDiscovery();
    }


    // The on-click listener for all devices in the ListViews
    private OnItemClickListener mDeviceClickListener = new OnItemClickListener() {
        public void onItemClick(AdapterView av, View v, int arg2, long arg3) {
            // Cancel discovery because it’s costly and we’re about to connect
            bluetoothAdapter.cancelDiscovery();
            // Get the device MAC address, which is the last 17 chars in the View
            String info = ((TextView) v).getText().toString();
            String address = info.substring(info.length() - 17);
            // Create the result Intent and include the MAC address
            Intent intent = new Intent();
            intent.putExtra(EXTRA_DEVICE_ADDRESS, address);
            // Set result and finish this Activity
            setResult(Activity.RESULT_OK, intent);
            finish();
        } };


    // The BroadcastReceiver that listens for discovered devices and
    // changes the title when discovery is finished
    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            // When discovery finds a device
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                // Get the BluetoothDevice object from the Intent
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice. ←􏰀 EXTRA_DEVICE);
                // If it’s already paired, skip it, because it’s been listed already
                if (device.getBondState() != BluetoothDevice.BOND_BONDED) { mNewDevicesArrayAdapter.add(device.getName() + "\n" + device.getAddress ←􏰀
                    ());
                }
                // When discovery is finished, change the Activity title
            } else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
                setProgressBarIndeterminateVisibility(false);
                setTitle(R.string.select_device);
                if (mNewDevicesArrayAdapter.getCount() == 0) {
                    String noDevices = getResources().getText(R.string.none_found).toString ←􏰀 ();
                    mNewDevicesArrayAdapter.add(noDevices);
                }
            } }
    }; }































/*
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Set;


public class ManageBluetooth extends AppCompatActivity {



    private BluetoothAdapter BA;
    private Set<BluetoothDevice> pairedDevices;
    ListView btListView;
    public static String EXTRA_ADDRESS = "device_address";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_bluetooth);

        BA = BluetoothAdapter.getDefaultAdapter();
        btListView = (ListView)findViewById(R.id.btListView);

        Button backBtn = (Button) findViewById(R.id.backBtn);
        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent startIntent = new Intent(getApplicationContext(), MainActivity.class);
                startActivity(startIntent);
            }
        });

        Button btOnBtn = (Button) findViewById(R.id.btOnBtn);
        btOnBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!BA.isEnabled()) {
                    Intent turnOn = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                    startActivityForResult(turnOn, 0);
                    Toast.makeText(getApplicationContext(), "Turned on",Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(getApplicationContext(), "Already on", Toast.LENGTH_LONG).show();
                }
            }
        });

        Button btOffBtn = (Button) findViewById(R.id.btOffBtn);
        btOffBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                BA.disable();
                Toast.makeText(getApplicationContext(), "Turned off" ,Toast.LENGTH_LONG).show();
            }
        });

        Button visibleBtn = (Button) findViewById(R.id.visibleBtn);
        visibleBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent getVisible = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
                startActivityForResult(getVisible, 0);
            }
        });

        Button listDeviceBtn = (Button) findViewById(R.id.listDeviceBtn);
        listDeviceBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
               list(v);
            }
        });


    }



    public void list(View v){
        pairedDevices = BA.getBondedDevices();

        ArrayList list = new ArrayList();

        for(BluetoothDevice bt : pairedDevices) {
            list.add(bt.getName());
        }
        Toast.makeText(getApplicationContext(), "Showing Paired Devices",Toast.LENGTH_SHORT).show();

        final ArrayAdapter adapter = new  ArrayAdapter(this, android.R.layout.simple_list_item_1, list);

        // displays items in the list inside the ListView
        btListView.setAdapter(adapter);
    }

    private AdapterView.OnItemClickListener myListClickListener = new AdapterView.OnItemClickListener() {
        public void onItemClick(AdapterView<?> av, View v, int arg2, long arg3) {
            // Get the device MAC address, the last 17 chars in the View
            String info = ((TextView) v).getText().toString();
            String address = info.substring(info.length() - 17);

            // Make an intent to start next activity.
            Intent i = new Intent(ManageBluetooth.this, MainActivity.class);

            //Change the activity.
            i.putExtra(MainActivity.BT_ADDRESS, address); //this will be received at Main Activity (class) Activity
            startActivity(i);
        }
    };


}
/*