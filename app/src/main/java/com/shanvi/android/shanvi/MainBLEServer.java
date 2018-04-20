package com.shanvi.android.shanvi;

import android.Manifest;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattServer;
import android.bluetooth.BluetoothGattServerCallback;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.bluetooth.le.AdvertiseCallback;
import android.bluetooth.le.AdvertiseData;
import android.bluetooth.le.AdvertiseSettings;
import android.bluetooth.le.BluetoothLeAdvertiser;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.ParcelUuid;
import android.util.Log;
import android.widget.TextView;

import com.google.firebase.iid.FirebaseInstanceId;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashSet;
import java.util.Set;
import java.util.TimeZone;

/**
 * Created by Debashis on 1/27/2018.
 */

public class MainBLEServer {

    private static final String TAG = MainBLEServer.class.getSimpleName();

    /* Local UI */
    private TextView mLocalTextView;
    /* Bluetooth API */
    private BluetoothManager mBluetoothManager;
    private BluetoothGattServer mBluetoothGattServer;
    private BluetoothLeAdvertiser mBluetoothLeAdvertiser;
    /* Collection of notification subscribers */
    private Set<BluetoothDevice> mRegisteredDevices = new HashSet<>();

    private Context context;
    private Boolean support;

    private int alert;

    public MainBLEServer(Context c, BluetoothManager bm) {
        mBluetoothManager = bm;
        context = c;
        support = true;
        alert = 0;
        BluetoothAdapter bluetoothAdapter = mBluetoothManager.getAdapter();
        // We can't continue without proper Bluetooth support
        if (!checkBluetoothSupport(bluetoothAdapter)) {
            support = false;
        }

        if (support) {
            // Register for system Bluetooth events
            IntentFilter filter = new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED);
            context.registerReceiver(mBluetoothReceiver, filter);
            if (!bluetoothAdapter.isEnabled()) {
                Log.d(TAG, "Bluetooth is currently disabled...enabling");
                bluetoothAdapter.enable();
            } else {
                Log.d(TAG, "Bluetooth enabled...starting services");
                startAdvertising();
                startServer();
            }
        }
    }

    /**
     * Verify the level of Bluetooth support provided by the hardware.
     * @param bluetoothAdapter System {@link BluetoothAdapter}.
     * @return true if Bluetooth is properly supported, false otherwise.
     */
    private boolean checkBluetoothSupport(BluetoothAdapter bluetoothAdapter) {

        if (bluetoothAdapter == null) {
            Log.w(TAG, "Bluetooth is not supported");
            return false;
        }

        if (!context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            Log.w(TAG, "Bluetooth LE is not supported");
            return false;
        }

        return true;
    }

    public void clear() {
        if (support) {
            stopServer();
            stopAdvertising();
            context.unregisterReceiver(mBluetoothReceiver);
        }
    }
    /**
     * Listens for Bluetooth adapter events to enable/disable
     * advertising and server functionality.
     */
    private BroadcastReceiver mBluetoothReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.STATE_OFF);

            switch (state) {
                case BluetoothAdapter.STATE_ON:
                    startAdvertising();
                    startServer();
                    break;
                case BluetoothAdapter.STATE_OFF:
                    stopServer();
                    stopAdvertising();
                    break;
                default:
                    // Do nothing
            }

        }
    };

    /**
     * Begin advertising over Bluetooth that this device is connectable
     * and supports the Current Time Service.
     */
    private void startAdvertising() {
        BluetoothAdapter bluetoothAdapter = mBluetoothManager.getAdapter();
        bluetoothAdapter.setName("Shanvi");
        mBluetoothLeAdvertiser = bluetoothAdapter.getBluetoothLeAdvertiser();
        if (mBluetoothLeAdvertiser == null) {
            Log.w(TAG, "Failed to create advertiser");
            return;
        }

        AdvertiseSettings settings = new AdvertiseSettings.Builder()
                .setAdvertiseMode(AdvertiseSettings.ADVERTISE_MODE_BALANCED)
                .setConnectable(true)
                .setTimeout(0)
                .setTxPowerLevel(AdvertiseSettings.ADVERTISE_TX_POWER_MEDIUM)
                .build();

        AdvertiseData data = new AdvertiseData.Builder()
                .setIncludeDeviceName(true)
                .setIncludeTxPowerLevel(false)
                .addServiceUuid(new ParcelUuid(SafetyProfile.SAFETY_SERVICE))
                .build();

        mBluetoothLeAdvertiser
                .startAdvertising(settings, data, mAdvertiseCallback);
    }

    /**
     * Stop Bluetooth advertisements.
     */
    private void stopAdvertising() {
        if (mBluetoothLeAdvertiser == null) return;

        mBluetoothLeAdvertiser.stopAdvertising(mAdvertiseCallback);
    }

    /**
     * Initialize the GATT server instance with the services/characteristics
     * from the Time Profile.
     */
    private void startServer() {
        mBluetoothGattServer = mBluetoothManager.openGattServer(context, mGattServerCallback);
        if (mBluetoothGattServer == null) {
            Log.w(TAG, "Unable to create GATT server");
            return;
        }

        mBluetoothGattServer.addService(SafetyProfile.createSafetyService());
        Log.w(TAG, "create GATT Safety Service");
    }

    /**
     * Shut down the GATT server.
     */
    private void stopServer() {
        if (mBluetoothGattServer == null) return;

        mBluetoothGattServer.close();
    }

    /**
     * Send a time service notification to any devices that are subscribed
     * to the characteristic.
     */
    private void notifyRegisteredDevices(int alert, byte adjustReason) {
        if (mRegisteredDevices.isEmpty()) {
            Log.i(TAG, "No subscribers registered");
            return;
        }
        byte[] data = SafetyProfile.getAlertDataWithReason(alert, adjustReason);

        Log.i(TAG, "Sending update to " + mRegisteredDevices.size() + " subscribers");
        for (BluetoothDevice device : mRegisteredDevices) {
            BluetoothGattCharacteristic timeCharacteristic = mBluetoothGattServer
                    .getService(SafetyProfile.SAFETY_SERVICE)
                    .getCharacteristic(SafetyProfile.ALERT_STATUS);
            timeCharacteristic.setValue(data);
            mBluetoothGattServer.notifyCharacteristicChanged(device, timeCharacteristic, false);
        }
    }

    public void changeAlert() {
        alert ++;
        notifyRegisteredDevices(alert, (byte) 0x02);
    }
    /**
     * Callback to receive information about the advertisement process.
     */
    private AdvertiseCallback mAdvertiseCallback = new AdvertiseCallback() {
        @Override
        public void onStartSuccess(AdvertiseSettings settingsInEffect) {
            Log.i(TAG, "LE Advertise Started.");
        }

        @Override
        public void onStartFailure(int errorCode) {
            Log.w(TAG, "LE Advertise Failed: "+errorCode);
        }
    };

    /**
     * Callback to handle incoming requests to the GATT server.
     * All read/write requests for characteristics and descriptors are handled here.
     */
    private BluetoothGattServerCallback mGattServerCallback = new BluetoothGattServerCallback() {

        @Override
        public void onConnectionStateChange(BluetoothDevice device, int status, int newState) {
            if (newState == BluetoothProfile.STATE_CONNECTED) {
                Log.i(TAG, "BluetoothDevice CONNECTED: " + device);
            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                Log.i(TAG, "BluetoothDevice DISCONNECTED: " + device);
                //Remove device from any active subscriptions
                mRegisteredDevices.remove(device);
            }
        }

        @Override
        public void onCharacteristicReadRequest(BluetoothDevice device, int requestId, int offset,
                                                BluetoothGattCharacteristic characteristic) {

            if (SafetyProfile.ALERT_STATUS.equals(characteristic.getUuid())) {
                Log.i(TAG, "Read Alert Status");
                mBluetoothGattServer.sendResponse(device,
                            requestId,
                            BluetoothGatt.GATT_SUCCESS,
                            0,
                            SafetyProfile.getAlertData(alert));
            } else {
                // Invalid characteristic
                Log.w(TAG, "Invalid Characteristic Read: " + characteristic.getUuid());
                mBluetoothGattServer.sendResponse(device,
                        requestId,
                        BluetoothGatt.GATT_FAILURE,
                        0,
                        null);
            }
        }

        class GPSData {
            public byte type;
            public long deviceid;
            public float lat;
            public float lon;
            public float alt;

            public GPSData(byte[] data) {
                ByteBuffer bb = ByteBuffer.wrap(data);
                bb.order(ByteOrder.LITTLE_ENDIAN);
                type = bb.get();
                deviceid = bb.getLong();
                byte gps_data_type = bb.get(20);
                byte hh = bb.get(21);
                byte mm = bb.get(22);
                byte ssv = bb.get(23);
                byte ssd = bb.get(24);
                short lat_degree = bb.getShort(28);
                byte lat_min_v = bb.get(30);
                int lat_min_d = bb.getInt(32);
                char ns = (char)bb.get(36);
                short lon_degree = bb.getShort(40);
                byte lon_min_v = bb.get(42);
                int lon_min_d = bb.getInt(44);
                char ew = (char)bb.get(48);
                char quality = (char)bb.get(49);
                byte numSV = bb.get(50);
                float HDOP = bb.getFloat(52);
                alt = bb.getFloat(56);
                char uAlt = (char)bb.get(60);
                float sep = bb.getFloat(64);
                char uSep = (char)bb.get(68);
                lat = lat_degree + lat_min_v/60.0f + lat_min_d/100000.0f/60.0f;
                lon = lon_degree + lon_min_v/60.0f + lon_min_d/100000.0f/60.0f;
                if (ns == 'S') { lat = lat * -1; }
                if (ew == 'W') { lon = lon * -1; }
            }
        }

        @Override
        public void onCharacteristicWriteRequest(BluetoothDevice device, int requestId,
                                                 BluetoothGattCharacteristic characteristic, boolean preparedWrite,
                                                 boolean responseNeeded, int offset, byte[] value) {

            if (SafetyProfile.SAFETY_ALERT.equals(characteristic.getUuid())) {
                Log.i(TAG, "Write Safety Alert");
                if (value.length > 0) {

                    final StringBuilder stringBuilder = new StringBuilder(value.length);
                    for (byte byteChar : value)
                        stringBuilder.append(String.format("%02X ", byteChar));
                    Log.d(TAG, "Data : " + stringBuilder.toString());
                    String token = FirebaseInstanceId.getInstance().getToken();
                    String hop = String.format("%02X ", (byte) token.charAt(0));
                    alert = value[0];
                    Log.i(TAG, "Value:" + value[0] + " Length:" + value.length);
                    if (value[0] == 0 && value.length >= 17) {
                        ByteBuffer bb = ByteBuffer.wrap(value);
                        bb.order(ByteOrder.LITTLE_ENDIAN);
                        byte action = bb.get();
                        long deviceid = bb.getLong();
                        long deviceid2 = bb.getLong();
                        int ckey = 0;
                        String rinfo = "01" + hop;
                        if (value.length >= 32) {
                            ckey = bb.getInt();
                            short skip1 = bb.getShort();
                            short skip2 = bb.getShort();
                            byte[] bytes = new byte[bb.remaining()];
                            bb.get(bytes);
                            final StringBuilder sb = new StringBuilder(bytes.length);
                            for (byte byteChar : bytes)
                                sb.append(String.format("%02X", byteChar));
                            rinfo = sb.toString();
                        }
                        String url = "http://www.shanvishield.com/safety/safety.php?go=dTrigger&did=" + deviceid +
                                "&ckey=" + ckey + "&rinfo=" + rinfo;
                        Log.d(TAG, url);
                        MySingleton.processRequest(context, url);
                    }
                    if (value[0] == 1 && value.length >= 17) {
                        ByteBuffer bb = ByteBuffer.wrap(value);
                        bb.order(ByteOrder.LITTLE_ENDIAN);
                        byte action = bb.get();
                        long deviceid = bb.getLong();
                        long deviceid2 = bb.getLong();
                        int ckey = 0;
                        String rinfo = "01" + hop;
                        if (value.length >= 32) {
                            ckey = bb.getInt();
                            short skip1 = bb.getShort();
                            short skip2 = bb.getShort();
                            byte[] bytes = new byte[bb.remaining()];
                            bb.get(bytes);
                            final StringBuilder sb = new StringBuilder(bytes.length);
                            for (byte byteChar : bytes)
                                sb.append(String.format("%02X", byteChar));
                            rinfo = sb.toString();
                        }
                        String url = "http://www.shanvishield.com/safety/safety.php?go=cTrigger&did=" + deviceid +
                                "&ckey=" + ckey + "&rinfo=" + rinfo;
                        Log.d(TAG, url);
                        MySingleton.processRequest(context, url);
                    }
                    if (value[0] == 2 && value.length >= 40) {
                        ByteBuffer bb = ByteBuffer.wrap(value);
                        bb.order(ByteOrder.LITTLE_ENDIAN);
                        byte action = bb.get();
                        long deviceid = bb.getLong();
                        long deviceid2 = bb.getLong();
                        short skip1 = bb.getShort();
                        byte skip2 = bb.get();
                        float lat = bb.getFloat();
                        float lon = bb.getFloat();
                        float alt = bb.getFloat();
                        byte skip = bb.get();
                        String rinfo = "";
                        byte[] bytes = new byte[bb.remaining()];
                        bb.get(bytes);
                        final StringBuilder sb = new StringBuilder(bytes.length);
                        for (byte byteChar : bytes)
                            sb.append(String.format("%02X", byteChar));
                        rinfo = sb.toString();
                        if (value.length >= 72) {
                            GPSData gd = new GPSData(value);
                            action = gd.type;
                            deviceid = gd.deviceid;
                            lat = gd.lat;
                            lon = gd.lon;
                            alt = gd.alt;
                            rinfo = "01" + hop;
                        }
                        String url = "http://www.shanvishield.com/safety/safety.php?go=addDeviceLocation&did=" + deviceid +
                                "&lat="+ lat + "&lon=" + lon + "&alt=" + alt + "&rinfo=" + rinfo;
                        Log.d(TAG, url);
                        MySingleton.processRequest(context, url);
                    }
                }
                if (responseNeeded) {
                    mBluetoothGattServer.sendResponse(device,
                            requestId,
                            BluetoothGatt.GATT_SUCCESS,
                            0,
                            null);
                }
            } else {
                // Invalid characteristic
                Log.w(TAG, "Invalid Characteristic Write: " + characteristic.getUuid());
                mBluetoothGattServer.sendResponse(device,
                        requestId,
                        BluetoothGatt.GATT_FAILURE,
                        0,
                        null);
            }
        }
        @Override
        public void onDescriptorReadRequest(BluetoothDevice device, int requestId, int offset,
                                            BluetoothGattDescriptor descriptor) {
            if (SafetyProfile.CLIENT_CONFIG.equals(descriptor.getUuid())) {
                Log.d(TAG, "Config descriptor read");
                byte[] returnValue;
                if (mRegisteredDevices.contains(device)) {
                    returnValue = BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE;
                } else {
                    returnValue = BluetoothGattDescriptor.DISABLE_NOTIFICATION_VALUE;
                }
                mBluetoothGattServer.sendResponse(device,
                        requestId,
                        BluetoothGatt.GATT_SUCCESS,
                        0,
                        returnValue);
            } else {
                Log.w(TAG, "Unknown descriptor read request");
                mBluetoothGattServer.sendResponse(device,
                        requestId,
                        BluetoothGatt.GATT_FAILURE,
                        0,
                        null);
            }
        }

        @Override
        public void onDescriptorWriteRequest(BluetoothDevice device, int requestId,
                                             BluetoothGattDescriptor descriptor,
                                             boolean preparedWrite, boolean responseNeeded,
                                             int offset, byte[] value) {
            if (SafetyProfile.CLIENT_CONFIG.equals(descriptor.getUuid())) {
                if (Arrays.equals(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE, value)) {
                    Log.d(TAG, "Subscribe device to notifications: " + device);
                    mRegisteredDevices.add(device);
                } else if (Arrays.equals(BluetoothGattDescriptor.DISABLE_NOTIFICATION_VALUE, value)) {
                    Log.d(TAG, "Unsubscribe device from notifications: " + device);
                    mRegisteredDevices.remove(device);
                }

                if (responseNeeded) {
                    mBluetoothGattServer.sendResponse(device,
                            requestId,
                            BluetoothGatt.GATT_SUCCESS,
                            0,
                            null);
                }
            } else {
                Log.w(TAG, "Unknown descriptor write request");
                if (responseNeeded) {
                    mBluetoothGattServer.sendResponse(device,
                            requestId,
                            BluetoothGatt.GATT_FAILURE,
                            0,
                            null);
                }
            }
        }
    };
}
