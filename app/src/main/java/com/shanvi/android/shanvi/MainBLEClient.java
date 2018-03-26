package com.shanvi.android.shanvi;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.os.ParcelUuid;
import android.text.TextUtils;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static android.bluetooth.BluetoothAdapter.STATE_CONNECTED;

/**
 * Created by Debashis on 2/6/2018.
 */

public class MainBLEClient {

    private static final String TAG = MainBLEClient.class.getSimpleName();

    private static final int REQUEST_ENABLE_BT = 1;

    private Activity context;
    private BluetoothManager mBluetoothManager;
    private BluetoothAdapter mBluetoothAdapter;

    /* Local UI */
    private TextView mLocalTextView;
    private ListView mLocalListView;
    private ArrayAdapter<String> adapter;
    private List<String> myStringArray = new ArrayList<String>();

    private BluetoothLeScanner mBluetoothLeScanner;
    private Handler mHandler = new Handler();
    private ArrayList<ScanFilter> filters = new ArrayList<ScanFilter>();
    private ScanSettings settings;

    private ScanCallback mScanCallback = new ScanCallback() {
        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            super.onScanResult(callbackType, result);
            if( result == null || result.getDevice() == null)
                return;
            Log.d(TAG, "Address:" + result.getDevice().getAddress() + " RSSI:" + result.getRssi());
        }

        @Override
        public void onBatchScanResults(List<ScanResult> results) {
            super.onBatchScanResults(results);
        }

        @Override
        public void onScanFailed(int errorCode) {
            Log.e( "BLE", "Discovery onScanFailed: " + errorCode );
            super.onScanFailed(errorCode);
        }
    };

    public MainBLEClient(Activity c, BluetoothManager bm) {
        mBluetoothManager = bm;
        context = c;

        mBluetoothAdapter = mBluetoothManager.getAdapter();

        // Ensures Bluetooth is available on the device and it is enabled. If not,
        // displays a dialog requesting user permission to enable Bluetooth.
        if (mBluetoothAdapter == null || !mBluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            context.startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        }
        mHandler = new Handler();
        Log.d(TAG, "BLE Client started\n");

        mBluetoothLeScanner = mBluetoothAdapter.getBluetoothLeScanner();
        ScanFilter filter = new ScanFilter.Builder()
                .setServiceUuid( new ParcelUuid(SafetyProfile.TRG2_SAFETY_SERVICE) )
                .build();
        filters.add( filter );

        settings = new ScanSettings.Builder()
                .setScanMode( ScanSettings.SCAN_MODE_LOW_LATENCY )
                .build();
    }

    private void setupUIdata() {
        mLocalTextView = context.findViewById(R.id.mText1);
        mLocalListView = context.findViewById(R.id.mList1);
        adapter = new ArrayAdapter<String>(context,
                android.R.layout.simple_list_item_1, myStringArray);
        mLocalListView.setAdapter(adapter);
        mLocalTextView.setText("BLE Devices:");
    }

    public void scanLeDevice(final boolean enable) {
        if (enable) {
            Log.d(TAG, "Scan started\n");
            //mBluetoothLeScanner.startScan(filters, settings, mScanCallback);
            mBluetoothLeScanner.startScan(mScanCallback);
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    mBluetoothLeScanner.stopScan(mScanCallback);
                }
            }, 10000);
        } else {
            mBluetoothLeScanner.stopScan(mScanCallback);
        }
    }

    public void clear() {
        mBluetoothLeScanner.stopScan(mScanCallback);
    }

}
