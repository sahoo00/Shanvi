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
    private final static String SIGNAL_SCORE = "signal_score";
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
    private int mScore = 0;
    private int mCounter = 0;

    int[] array = new int[64];
    int indexArray = 0;
    int countArray = 0;
    int totalArray = 50;
    int window = 5;

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
        Log.d(TAG, "ShowGraph");
        mGraphView = mLineGraph.getView(this);
        ViewGroup layout = findViewById(R.id.graph_signal);
        layout.addView(mGraphView);
        startShowGraph();
    }

    @Override
    protected void onStart() {
        Log.d(TAG, "Start");
        super.onStart();
        mBluetoothManager = (BluetoothManager) getSystemService(BLUETOOTH_SERVICE);
    }

    @Override
    protected void onDestroy() {
        Log.d(TAG, "Destroy");
        super.onDestroy();

        stopShowGraph();
    }

    @Override
    protected void onRestoreInstanceState(@NonNull final Bundle savedInstanceState) {
        Log.d(TAG, "RestoreInstance");
        super.onRestoreInstanceState(savedInstanceState);

        isGraphInProgress = savedInstanceState.getBoolean(GRAPH_STATUS);
        mCounter = savedInstanceState.getInt(GRAPH_COUNTER);
        mValue = savedInstanceState.getInt(SIGNAL_VALUE);
        mScore = savedInstanceState.getInt(SIGNAL_SCORE);

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
        Log.d(TAG, "SaveInstance");
        super.onSaveInstanceState(outState);

        outState.putBoolean(GRAPH_STATUS, isGraphInProgress);
        outState.putInt(GRAPH_COUNTER, mCounter);
        outState.putInt(SIGNAL_VALUE, mValue);
        outState.putInt(SIGNAL_SCORE, mScore);
    }

    private void updateGraph(final int mValue, final int mScore) {
        mCounter++;
        mLineGraph.addValue(new Point(mCounter, mValue), new Point(mCounter, mScore));
        mGraphView.repaint();
    }

    private Runnable mRepeatTask = new Runnable() {
        @Override
        public void run() {
            if (mValue > 0 && mScore > 0)
                updateGraph(mValue, mScore);
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
        Log.d(TAG, "clearGraph");
        mLineGraph.clearGraph();
        mGraphView.repaint();
        mCounter = 0;
        mValue = 0;
        mScore = 0;
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
        Log.d(TAG, "Connected");
        mDeviceConnected = true;
        runOnUiThread(() -> mConnectButton.setText(R.string.action_disconnect));
    }
    protected void onDeviceDisconnected() {
        clearGraph();
        Log.d(TAG, "Disconnected");
        mDeviceConnected = false;
        runOnUiThread(() -> mConnectButton.setText(R.string.action_connect));
    }
    protected void onServicesDiscovered(List<BluetoothGattService> gattServices) {
        enableNotification(SafetyProfile.TRG2_SAFETY_SERVICE, SafetyProfile.SIG_CHARSTIC);
    }

    int getValue(int rssi) {
        array[indexArray] = rssi;
        indexArray ++;
        countArray ++;
        if (indexArray > totalArray) { indexArray = 0; }
        if (countArray > totalArray) { countArray = totalArray; }
        int sum = 0;
        int j = indexArray - 1;
        for (int i =0; i < countArray && i < window; i++) {
            if ( j < 0) {
                j = countArray - 1;
            }
            sum = sum + (150 + array[j]);
            j -- ;
        }
        return sum;
    }

    float getScore(int rssi) {
        if (true) { // Correlation based - TRG2 - cancel
            int[] sum = new int[] {0, 0, 0, 0};
            int j = indexArray - 1;
            for (int i = 0; i < 40; i++) {
                if ( j < 0) {
                    j = countArray - 1;
                }
                sum[i/10] = sum[i/10] + array[j];
                j -- ;
            }
            int m1 = sum[0]; m1 = m1 > sum[3]? sum[3]: m1;
            int m2 = sum[1]; m2 = m2 < sum[2]? sum[2]: m2;
            float score = (m1 - m2)*2.0f*(m1-m2)*(m1-m2)/100;
            if (score < 0) score = 0;
            return score;
        }
        if (false) { // Correlation based - TRG2
            int[] sum = new int[] {0, 0, 0, 0};
            int j = indexArray - 1;
            for (int i = 0; i < 40; i++) {
                if ( j < 0) {
                    j = countArray - 1;
                }
                sum[i/10] = sum[i/10] + array[j];
                j -- ;
            }
            int m1 = sum[0]; m1 = m1 > sum[2]? sum[2]: m1;
            int m2 = sum[1]; m2 = m2 < sum[3]? sum[3]: m2;
            float score = (m1 - m2)*2.0f*(m1-m2)*(m1-m2)/100;
            if (score < 0) score = 0;
            return score;
        }
        return 0;
    }

    protected void onDataAvailable(byte[] data) {

        final StringBuilder stringBuilder = new StringBuilder(data.length);
        for(byte byteChar : data)
            stringBuilder.append(String.format("%02X ", byteChar));
        Log.d(TAG, "Data(" + data.length + ") : " + stringBuilder.toString());

        ByteBuffer bb = ByteBuffer.wrap(data);
        bb.order(ByteOrder.LITTLE_ENDIAN);
        byte type = bb.get();
        byte skip1 = bb.get();
        short skip2 = bb.getShort();
        int trg2c_index = bb.getInt();
        short sum = bb.getShort();
        byte rssi = bb.get();
        byte skip3 = bb.get();
        float score = bb.getFloat();
        int sum_n = getValue(rssi);
        float score_n = getScore(rssi);
        Log.d(TAG, "G" + isGraphInProgress + " Data : " + trg2c_index + " sum = " + sum + ":" + sum_n +
                " rssi=" + rssi + " score=" + score +":" +score_n);
        if (score < 0) score = 0;
        updateGraph((int) sum, (int) score + 300);
    }
}

