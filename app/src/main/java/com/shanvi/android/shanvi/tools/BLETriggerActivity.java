package com.shanvi.android.shanvi.tools;


import android.app.SearchManager;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanSettings;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.ParcelUuid;
import android.support.v7.widget.SearchView;
import android.util.Log;
import android.view.Menu;

import com.shanvi.android.shanvi.MainBLEClient;
import com.shanvi.android.shanvi.R;
import com.shanvi.android.shanvi.SafetyProfile;
import com.shanvi.android.shanvi.TableAdapter;
import com.shanvi.android.shanvi.TableRow;

import java.util.List;

public class BLETriggerActivity extends BaseUserActivity implements MainBLEClient.BLEClientListener {
    private static final String TAG = BLETriggerActivity.class.getSimpleName();


    private BluetoothManager mBluetoothManager;
    MainBLEClient client = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "BLETriggerActivity");
        mLocalTextView.setText(R.string.title_activity_bletrigger);

        mBluetoothManager = (BluetoothManager) getSystemService(BLUETOOTH_SERVICE);
        client = new MainBLEClient(this, mBluetoothManager);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.activity_devices_toolbar, menu);
        return true;
    }

    public void onAdd() {
        if (client != null) {
            if (client.getConnected()) {
                client.writeCharacteristic(SafetyProfile.TRG2_SAFETY_SERVICE, SafetyProfile.CMD_CHARSTIC, new byte[]{0x03});
            }
            else {
                client.connect("Sh_TRG2");
            }
        }
    }

    public void onDeviceConnected() {

    }
    public void onDeviceDisconnected() {

    }
    public void onServicesDiscovered(List<BluetoothGattService> gattServices) {
        client.writeCharacteristic(SafetyProfile.TRG2_SAFETY_SERVICE, SafetyProfile.CMD_CHARSTIC, new byte[]{0x03});
    }
    public void onDataAvailable(byte[] data) {

    }

    public void onClick(TableRow tableRow, TableAdapter tableAdapter) {

    }

    @Override
    protected void onResume() {
        super.onResume();
        if (client != null) {
            client.onResume();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (client != null) {
            client.onPause();
        }
    }

    public void onDestroy() {
        Log.d(TAG, "Destroy!");
        if (client != null) {
            client.onDestroy();
        }
        super.onDestroy();
    }
}
