package com.example.subway_for_pregnant;

import androidx.appcompat.app.AppCompatActivity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.Intent;


public class BluetoothActivity extends AppCompatActivity {

    BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
/*
    if (bluetoothAdapter == null) {
        // Device doesn't support Bluetooth
    }
    if (!bluetoothAdapter.isEnabled()) {
        Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
        startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
    }
 */
}
