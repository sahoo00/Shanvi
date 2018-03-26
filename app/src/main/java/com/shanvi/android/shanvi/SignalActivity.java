package com.shanvi.android.shanvi;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Point;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.shanvi.android.shanvi.scanner.ScannerFragment;

import org.achartengine.GraphicalView;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.Charset;
import java.util.List;
import java.util.UUID;

/**
 * Created by Debashis on 3/13/2018.
 */

public class SignalActivity extends DeviceControlActivity implements ScannerFragment.OnDeviceSelectedListener {

    private static final String TAG = SignalActivity.class.getSimpleName();

    private final static String GRAPH_STATUS = "graph_status";
    private final static String GRAPH_COUNTER = "graph_counter";
    private final static String SIGNAL_VALUE = "signal_value";
    protected static final int REQUEST_ENABLE_BT = 2;

    private GraphicalView mGraphView;
    private LineGraphView mLineGraph;
    private TextView mText;
    private Button mConnectButton;
    private boolean mDeviceConnected = false;
    private String mDeviceName;
    private BluetoothManager mBluetoothManager;

    private final static int MAX_SIGNAL_VALUE = 65535;
    private final static int MIN_POSITIVE_VALUE = 0;
    private final static int REFRESH_INTERVAL = 1000; // 1 second interval

    private Handler mHandler = new Handler();

    private boolean isGraphInProgress = false;

    private int mValue = 0;
    private int mCounter = 0;

    @Override
    protected final void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // The onCreateView class should... create the view
        onCreateView(savedInstanceState);
    }

    void onCreateView(final Bundle savedInstanceState) {
        setContentView(R.layout.activity_signal);
        setGUI();
    }

    private void setGUI() {
        mLineGraph = LineGraphView.getLineGraphView();
        mText = findViewById(R.id.graph_text);
        mConnectButton = findViewById(R.id.graph_button);
        showGraph();
    }

    private void showGraph() {
        mGraphView = mLineGraph.getView(this);
        ViewGroup layout = findViewById(R.id.graph_signal);
        layout.addView(mGraphView);
    }

    @Override
    protected void onStart() {
        super.onStart();
        mBluetoothManager = (BluetoothManager) getSystemService(BLUETOOTH_SERVICE);
        /*
        byte[] data = {(byte)0xA9, 0x07, 0x00, 0x00, (byte)0xAB, 0x01, (byte)0xC1, 0x00,
                (byte)0xA0, (byte)0xC8, (byte)0xF0, (byte)0x41};
        ByteBuffer bb = ByteBuffer.wrap(data);
        bb.order(ByteOrder.LITTLE_ENDIAN);
        int trg2c_index = bb.getInt();
        short sum = bb.getShort();
        byte rssi = bb.get();
        byte s1 = bb.get();
        float score = bb.getFloat();
        Log.d(TAG, "Data : " + trg2c_index + " sum = " + sum + " rssi=" + rssi + " score=" + score);
        */
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        stopShowGraph();
    }

    @Override
    protected void onRestoreInstanceState(@NonNull final Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);

        isGraphInProgress = savedInstanceState.getBoolean(GRAPH_STATUS);
        mCounter = savedInstanceState.getInt(GRAPH_COUNTER);
        mValue = savedInstanceState.getInt(SIGNAL_VALUE);

        if (mDeviceConnected) {
            mConnectButton.setText(R.string.action_disconnect);
        } else {
            mConnectButton.setText(R.string.action_connect);
        }

        if (isGraphInProgress)
            startShowGraph();
    }

    @Override
    protected void onSaveInstanceState(final Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putBoolean(GRAPH_STATUS, isGraphInProgress);
        outState.putInt(GRAPH_COUNTER, mCounter);
        outState.putInt(SIGNAL_VALUE, mValue);
    }

    private void updateGraph(final int mValue) {
        mCounter++;
        mLineGraph.addValue(new Point(mCounter, mValue));
        mGraphView.repaint();
    }

    private Runnable mRepeatTask = new Runnable() {
        @Override
        public void run() {
            if (mValue > 0)
                updateGraph(mValue);
            if (isGraphInProgress)
                mHandler.postDelayed(mRepeatTask, REFRESH_INTERVAL);
        }
    };

    void startShowGraph() {
        isGraphInProgress = true;
        mRepeatTask.run();
    }

    void stopShowGraph() {
        isGraphInProgress = false;
        mHandler.removeCallbacks(mRepeatTask);
    }

    void setDefaultUI() {
        mText.setText(R.string.not_available);
        clearGraph();
    }

    private void clearGraph() {
        mLineGraph.clearGraph();
        mGraphView.repaint();
        mCounter = 0;
        mValue = 0;
    }

    private boolean isBLEEnabled() {
        final BluetoothManager bluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        final BluetoothAdapter adapter = bluetoothManager.getAdapter();
        return adapter != null && adapter.isEnabled();
    }

    protected UUID getFilterUUID() {
        return SafetyProfile.TRG2_SAFETY_SERVICE;
    }

    /**
     * Shows the scanner fragment.
     *
     * @param filter               the UUID filter used to filter out available devices. The fragment will always show all bonded devices as there is no information about their
     *                             services
     * @see #getFilterUUID()
     */
    private void showDeviceScanningDialog(final UUID filter) {
        runOnUiThread(() -> {
            final ScannerFragment dialog = ScannerFragment.getInstance(filter);
            dialog.show(getSupportFragmentManager(), "scan_fragment");
        });
    }

    /**
     * Called when user press CONNECT or DISCONNECT button. See layout files -> onClick attribute.
     */
    public void onConnectClicked(final View view) {
        if (isBLEEnabled()) {
            if (!mDeviceConnected) {
                setDefaultUI();
                showDeviceScanningDialog(getFilterUUID());
            }
            else {
                disconnect();
            }
        } else {
            showBLEDialog();
        }
    }

    protected void showBLEDialog() {
        final Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
        startActivityForResult(enableIntent, REQUEST_ENABLE_BT);
    }

    @Override
    public void onDeviceSelected(final BluetoothDevice device, final String name) {
        mDeviceName = name;
        connect(device);
        Log.d(TAG, " Device name:" + name + " Address:" + device.getAddress());
    }

    /**
     * Fired when scanner dialog has been cancelled without selecting a device.
     */
    public void onDialogCanceled() {

    }

    protected void onDeviceConnected() {
        mDeviceConnected = true;
        runOnUiThread(() -> mConnectButton.setText(R.string.action_disconnect));
    }
    protected void onDeviceDisconnected() {
        clearGraph();
        mDeviceConnected = false;
        runOnUiThread(() -> mConnectButton.setText(R.string.action_connect));
    }
    protected void onServicesDiscovered(List<BluetoothGattService> gattServices) {
        enableNotification(SafetyProfile.TRG2_SAFETY_SERVICE, SafetyProfile.SIG_CHARSTIC);
    }
    protected void onDataAvailable(byte[] data) {
        /*
        final StringBuilder stringBuilder = new StringBuilder(data.length);
        for(byte byteChar : data)
            stringBuilder.append(String.format("%02X ", byteChar));
        Log.d(TAG, "Data : " + stringBuilder.toString());
        */
        ByteBuffer bb = ByteBuffer.wrap(data);
        bb.order(ByteOrder.LITTLE_ENDIAN);
        int trg2c_index = bb.getInt();
        short sum = bb.getShort();
        byte rssi = bb.get();
        byte s1 = bb.get();
        float score = bb.getFloat();
        Log.d(TAG, "Data : " + trg2c_index + " sum = " + sum + " rssi=" + rssi + " score=" + score);
        updateGraph((int) sum);
    }
}

