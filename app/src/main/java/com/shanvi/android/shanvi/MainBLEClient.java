package com.shanvi.android.shanvi;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanRecord;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Handler;
import android.os.IBinder;
import android.os.ParcelUuid;
import android.text.TextUtils;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.shanvi.android.shanvi.scanner.ScannerFragment;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static android.bluetooth.BluetoothAdapter.STATE_CONNECTED;
import static android.content.Context.BIND_AUTO_CREATE;

/**
 * Created by Debashis on 2/6/2018.
 */

public class MainBLEClient {

    private static final String TAG = MainBLEClient.class.getSimpleName();

    private static final int REQUEST_ENABLE_BT = 1;

    private Activity context;
    private BLEClientListener mListener;
    private BluetoothManager mBluetoothManager;
    private BluetoothAdapter mBluetoothAdapter;

    private BluetoothLeScanner mBluetoothLeScanner;
    private Handler mHandler = new Handler();
    private ArrayList<ScanFilter> filters = new ArrayList<ScanFilter>();
    private ScanSettings settings;
    private String targetDeviceName;
    private BluetoothDevice targetDevice;

    // Code to manage Service lifecycle.
    private ServiceConnection mServiceConnection = null;
    private BluetoothLeService mBluetoothLeService;
    private boolean mConnected = false;
    private String mDeviceAddress;

    private void createServiceConnection() {
        mServiceConnection = new ServiceConnection() {

            @Override
            public void onServiceConnected(ComponentName componentName, IBinder service) {
                mBluetoothLeService = ((BluetoothLeService.LocalBinder) service).getService();
                if (!mBluetoothLeService.initialize()) {
                    Log.e(TAG, "Unable to initialize Bluetooth");
                }
                // Automatically connects to the device upon successful start-up initialization.
                mBluetoothLeService.connect(mDeviceAddress);
            }

            @Override
            public void onServiceDisconnected(ComponentName componentName) {
                mBluetoothLeService = null;
            }
        };
        Log.d(TAG, "sCon " + mDeviceAddress);
        Intent gattServiceIntent = new Intent(context, BluetoothLeService.class);
        boolean status = context.bindService(gattServiceIntent, mServiceConnection, BIND_AUTO_CREATE);
        Log.d(TAG, "bindS " + status);
    }

    private ScanCallback mScanCallback = new ScanCallback() {
        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            super.onScanResult(callbackType, result);
            if( result == null || result.getDevice() == null)
                return;
            BluetoothDevice device = result.getDevice();
            Boolean found = false;
            String name = device.getName();
            Log.d(TAG, "Name = " + name + "Address:" + device.getAddress() + " RSSI:" + result.getRssi());
            if (name != null && name.equals(targetDeviceName)) {
                found = true;
            }
            if (result.getScanRecord() != null) {
                ScanRecord record = result.getScanRecord();
                name = record.getDeviceName();
                Log.d(TAG, "Name = " + name + "Address:" + device.getAddress());
                if (name != null && name.equals(targetDeviceName)) {
                    found = true;
                }
            }
            if (found) {
                onDeviceFound(device);
            }
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

        try {
            mListener = (BLEClientListener) context;
        } catch (final ClassCastException e) {
            throw new ClassCastException(context.toString() + " must implement OnDeviceSelectedListener");
        }

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

    public void disconnect() {
        if (mBluetoothLeService != null)
            mBluetoothLeService.disconnect();
        mBluetoothLeService = null;
        if (mServiceConnection != null)
            context.unbindService(mServiceConnection);
        mServiceConnection = null;
    }

    public void connect(String name) {
        disconnect();
        targetDeviceName = name;
        targetDevice = null;
        scanLeDevice(true);
    }

    public void onDeviceFound(BluetoothDevice device) {
        targetDevice = device;
        mBluetoothLeScanner.stopScan(mScanCallback);
        mDeviceAddress = device.getAddress();
        Log.d(TAG, "Connecting to " + mDeviceAddress);
        createServiceConnection();
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

    // Handles various events fired by the Service.
    // ACTION_GATT_CONNECTED: connected to a GATT server.
    // ACTION_GATT_DISCONNECTED: disconnected from a GATT server.
    // ACTION_GATT_SERVICES_DISCOVERED: discovered GATT services.
    // ACTION_DATA_AVAILABLE: received data from the device.  This can be a result of read
    //                        or notification operations.
    private final BroadcastReceiver mGattUpdateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            if (BluetoothLeService.ACTION_GATT_CONNECTED.equals(action)) {
                mConnected = true;
                mListener.onDeviceConnected();
            } else if (BluetoothLeService.ACTION_GATT_DISCONNECTED.equals(action)) {
                mConnected = false;
                mListener.onDeviceDisconnected();
            } else if (BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED.equals(action)) {
                // Show all the supported services and characteristics on the user interface.
                mListener.onServicesDiscovered(mBluetoothLeService.getSupportedGattServices());
            } else if (BluetoothLeService.ACTION_DATA_AVAILABLE.equals(action)) {
                mListener.onDataAvailable(intent.getByteArrayExtra(BluetoothLeService.EXTRA_DATA));
            }
        }
    };

    public boolean getConnected() { return mConnected; }

    public void writeCharacteristic(UUID uuids, UUID uuidc, byte[] data) {
        BluetoothGattCharacteristic characteristic = mBluetoothLeService.getCharacteristic(uuids, uuidc);
        mBluetoothLeService.writeCharacteristic(characteristic, data);
    }
    protected void readCharacteristic(UUID uuids, UUID uuidc) {
        BluetoothGattCharacteristic characteristic = mBluetoothLeService.getCharacteristic(uuids, uuidc);
        mBluetoothLeService.readCharacteristic(characteristic);
    }
    protected void enableNotification(UUID uuids, UUID uuidc) {
        BluetoothGattCharacteristic characteristic = mBluetoothLeService.getCharacteristic(uuids, uuidc);
        mBluetoothLeService.setCharacteristicNotification(characteristic, true);
    }
    protected void disableNotification(UUID uuids, UUID uuidc) {
        BluetoothGattCharacteristic characteristic = mBluetoothLeService.getCharacteristic(uuids, uuidc);
        mBluetoothLeService.setCharacteristicNotification(characteristic, false);
    }

    public void onResume() {
        context.registerReceiver(mGattUpdateReceiver, makeGattUpdateIntentFilter());
        if (mBluetoothLeService != null) {
            final boolean result = mBluetoothLeService.connect(mDeviceAddress);
            Log.d(TAG, "Connect request result=" + result);
        }
    }

    public void onPause() {
        context.unregisterReceiver(mGattUpdateReceiver);
    }

    public void onDestroy() {
        disconnect();
        clear();
    }

    private static IntentFilter makeGattUpdateIntentFilter() {
        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_CONNECTED);
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_DISCONNECTED);
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED);
        intentFilter.addAction(BluetoothLeService.ACTION_DATA_AVAILABLE);
        return intentFilter;
    }

    public interface BLEClientListener {
        void onDeviceConnected();

        void onDeviceDisconnected();

        void onServicesDiscovered(List<BluetoothGattService> gattServices);

        void onDataAvailable(byte[] data);
    }
}
