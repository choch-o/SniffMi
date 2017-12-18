package com.chocho.sniffmi;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattServer;
import android.bluetooth.BluetoothGattServerCallback;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Build;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.UUID;

import static android.bluetooth.BluetoothGattDescriptor.PERMISSION_READ;
import static android.bluetooth.BluetoothGattDescriptor.PERMISSION_WRITE;
import static android.webkit.ConsoleMessage.MessageLevel.LOG;

public class MainActivity extends AppCompatActivity {
    private static final int PERMISSIONS_REQUEST_BLUETOOTH = 1;
    private static final int PERMISSIONS_REQUEST_BLUETOOTH_ADMIN = 2;

    BluetoothAdapter adapter;
    BluetoothDevice device;
    Context mContext;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (this.checkSelfPermission(Manifest.permission.BLUETOOTH) != PackageManager.PERMISSION_GRANTED)
                requestPermissions(new String[]{Manifest.permission.BLUETOOTH}, PERMISSIONS_REQUEST_BLUETOOTH);
            if (this.checkSelfPermission(Manifest.permission.BLUETOOTH_ADMIN) != PackageManager.PERMISSION_GRANTED)
                requestPermissions(new String[]{Manifest.permission.BLUETOOTH_ADMIN}, PERMISSIONS_REQUEST_BLUETOOTH_ADMIN);
        }

        BluetoothManager mBluetoothManager = (BluetoothManager) getSystemService(BLUETOOTH_SERVICE);
        adapter = mBluetoothManager.getAdapter();

        mContext = getApplicationContext();

        // Setup pair button
        Button pairBtn = findViewById(R.id.pair_btn);
        pairBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                device = adapter.getRemoteDevice(getString(R.string.miband_addr));
                pairDevice(device);
            }
        });

        // Setup unpair button
        Button unpairBtn = findViewById(R.id.unpair_btn);
        unpairBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                device = adapter.getRemoteDevice(getString(R.string.miband_addr));
                unpairDevice(device);
            }
        });


        // Register broadcast receiver
        IntentFilter intent = new IntentFilter(BluetoothDevice.ACTION_BOND_STATE_CHANGED);
        registerReceiver(mPairReceiver, intent);
    }


    // Broadcast receiver to keep track of device's bond state
    private final BroadcastReceiver mPairReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            Log.d("Br", "Received");

            if (BluetoothDevice.ACTION_BOND_STATE_CHANGED.equals(action)) {
                final int state        = intent.getIntExtra(BluetoothDevice.EXTRA_BOND_STATE, BluetoothDevice.ERROR);
                final int prevState    = intent.getIntExtra(BluetoothDevice.EXTRA_PREVIOUS_BOND_STATE, BluetoothDevice.ERROR);

                Log.d("Br", "Bond state changed");
                Log.d("State", Integer.toString(state));
                Log.d("prevState", Integer.toString(prevState));

                switch (state) {
                    case BluetoothDevice.BOND_NONE: {
                        Log.d("BD", "BOND_NONE");
                        break;
                    }
                    case BluetoothDevice.BOND_BONDING: {
                        Log.d("BD", "BOND_BONDING");
                        break;
                    }
                    case BluetoothDevice.BOND_BONDED: {
                        Log.d("BD", "BOND_BONDED");
                        break;
                    }
                }


                if (state == BluetoothDevice.BOND_BONDED && prevState == BluetoothDevice.BOND_BONDING) {
                    Log.d("BT", "Paired");
                } else if (state == BluetoothDevice.BOND_NONE && prevState == BluetoothDevice.BOND_BONDED){
                    Log.d("BT", "UnPaired");
                }

            }
        }
    };

    private void pairDevice(BluetoothDevice device) {
        try {
            Method method = device.getClass().getMethod("createBond", (Class[]) null);
            method.invoke(device, (Object[]) null);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void unpairDevice(BluetoothDevice device) {
        try {
            Method method = device.getClass().getMethod("removeBond", (Class[]) null);
            method.invoke(device, (Object[]) null);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
