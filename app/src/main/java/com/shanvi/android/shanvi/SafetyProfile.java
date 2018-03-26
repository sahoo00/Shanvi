package com.shanvi.android.shanvi;

import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;

import java.util.Calendar;
import java.util.UUID;

/**
 * Created by Debashis on 1/28/2018.
 */

public class SafetyProfile {
    private static final String TAG = SafetyProfile.class.getSimpleName();

    /* Safety Service UUID */
    public static UUID SAFETY_SERVICE = UUID.fromString("deba0000-cfa5-49e8-8b63-03096e931dc6");
    /* Safety Alert Characteristic */
    public static UUID SAFETY_ALERT    = UUID.fromString("deba0010-cfa5-49e8-8b63-03096e931dc6");
    /* Alert Status Characteristic */
    public static UUID ALERT_STATUS    = UUID.fromString("deba0011-cfa5-49e8-8b63-03096e931dc6");
    /* Mandatory Client Characteristic Config Descriptor */
    public static UUID CLIENT_CONFIG = UUID.fromString("00002902-0000-1000-8000-00805f9b34fb");

    /* Safety Service UUID */
    public static UUID TRG2_SAFETY_SERVICE = UUID.fromString("deba0001-cfa5-49e8-8b63-03096e931dc6");
    /* TRG2 CMD Characteristic */
    public static UUID CMD_CHARSTIC    = UUID.fromString("deba0012-cfa5-49e8-8b63-03096e931dc6");
    /* TRG2 Signal Characteristic */
    public static UUID SIG_CHARSTIC    = UUID.fromString("deba0013-cfa5-49e8-8b63-03096e931dc6");

    /**
     * Return a configured {@link BluetoothGattService} instance for the
     * Current Time Service.
     */
    public static BluetoothGattService createSafetyService() {
        BluetoothGattService service = new BluetoothGattService(SAFETY_SERVICE,
                BluetoothGattService.SERVICE_TYPE_PRIMARY);

        // Safety Alert characteristic
        BluetoothGattCharacteristic alertStatus = new BluetoothGattCharacteristic(ALERT_STATUS,
                //Read-only characteristic, supports notifications
                BluetoothGattCharacteristic.PROPERTY_READ | BluetoothGattCharacteristic.PROPERTY_NOTIFY,
                BluetoothGattCharacteristic.PERMISSION_READ);
        BluetoothGattDescriptor configDescriptor = new BluetoothGattDescriptor(CLIENT_CONFIG,
                //Read/write descriptor
                BluetoothGattDescriptor.PERMISSION_READ | BluetoothGattDescriptor.PERMISSION_WRITE);
        alertStatus.addDescriptor(configDescriptor);

        // AlertStatus characteristic
        BluetoothGattCharacteristic safetyAlert = new BluetoothGattCharacteristic(SAFETY_ALERT,
                //Read-only characteristic
                BluetoothGattCharacteristic.PROPERTY_WRITE,
                BluetoothGattCharacteristic.PERMISSION_WRITE);

        service.addCharacteristic(alertStatus);
        service.addCharacteristic(safetyAlert);

        return service;
    }

    /**
     * Construct the field values for a Current Time characteristic
     * from the given epoch timestamp and adjustment reason.
     */
    public static byte[] getAlertData(int data) {
        byte[] field = new byte[5];

        field[0] = (byte) (data & 0xFF);
        field[1] = (byte) ((data >> 8) & 0xFF);
        field[2] = (byte) ((data >> 16) & 0xFF);
        field[3] = (byte) ((data >> 24) & 0xFF);
        field[4] = (byte) (0x01);

        return field;
    }

    /**
     * Construct the field values for a Current Time characteristic
     * from the given epoch timestamp and adjustment reason.
     */
    public static byte[] getAlertDataWithReason(int data, byte reason) {
        byte[] field = new byte[5];

        field[0] = (byte) (data & 0xFF);
        field[1] = (byte) ((data >> 8) & 0xFF);
        field[2] = (byte) ((data >> 16) & 0xFF);
        field[3] = (byte) ((data >> 24) & 0xFF);
        field[4] = reason;

        return field;
    }
}
