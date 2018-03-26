package com.shanvi.android.shanvi.tools;

import android.app.Dialog;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.shanvi.android.shanvi.R;
import com.shanvi.android.shanvi.TableAdapter;
import com.shanvi.android.shanvi.TableRow;

import org.json.JSONArray;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Debashis on 3/24/2018.
 */

public class MakeDevicesActivity extends BaseUserActivity {
    private static final String TAG = MakeDevicesActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "Make Devices Activity");
        mLocalTextView.setText(R.string.title_activity_mdevs);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.activity_devices_toolbar, menu);
        return true;
    }

    protected void showTable() {
        String userid = uData.userid;
        String url = "http://www.shanvishield.com/safety/safety.php?go=getStock&pid=" + userid;
        processTableRequest(url, "Make");
    }

    protected void onTableResponse(JSONArray table, String type) {
        try {
            if (type.equals("Make")) {
                tableRows = new ArrayList<TableRow>();
                for (int i = 0; i < table.length(); i++) {
                    JSONArray arr = table.getJSONArray(i);
                    TableRow row = TableRow.parseStock(arr);
                    tableRows.add(row);
                }
                tableAdapter = new TableAdapter(tableRows, this);
                mLocalListView.setAdapter(tableAdapter);
                String str = "Success";
                Toast.makeText(thisActivity, str, Toast.LENGTH_SHORT).show();
            }
            if (type.equals("Add") || type.equals("Remove")) {
                if (table.length() <= 0 || (table.length() == 2 && table.getInt(0) == 0)) {
                    String str = "Error!";
                    Toast.makeText(thisActivity, str, Toast.LENGTH_SHORT).show();
                }
                else {
                    String str = "Success";
                    Toast.makeText(thisActivity, str, Toast.LENGTH_SHORT).show();
                }
            }
        } catch (Exception e) {
        }
    }

    public void onAdd() {
        final Dialog dialog = new Dialog(MakeDevicesActivity.this);
        dialog.setTitle(R.string.make);
        dialog.setContentView(R.layout.input_box_select);
        TextView txtMessage = (TextView) dialog.findViewById(R.id.txtmessage);
        txtMessage.setText(R.string.make);
        txtMessage.setTextColor(Color.parseColor("#ff2222"));
        TextView txtstatus = (TextView) dialog.findViewById(R.id.txtstatus);
        txtstatus.setText(R.string.valid);
        txtstatus.setTextColor(Color.parseColor("#22ff22"));
        final Spinner spinner = (Spinner) dialog.findViewById(R.id.txtinput);
        List<String> categories = new ArrayList<String>();
        categories.add("TRG1");
        categories.add("TRG2");
        categories.add("MN");
        // Creating adapter for spinner
        ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, categories);
        // Drop down layout style - list view with radio button
        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // attaching data adapter to spinner
        spinner.setAdapter(dataAdapter);
        Button bt = (Button) dialog.findViewById(R.id.btdone);
        Button btc = (Button) dialog.findViewById(R.id.btcancel);
        bt.setText(R.string.make);
        bt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String userid = uData.userid;
                String dtype = spinner.getSelectedItem().toString();
                String url = "http://www.shanvishield.com/safety/safety.php?go=makeDevice&pid=" + userid +
                        "&dtype=" + dtype;
                processTableRequest(url, "Add");
                dialog.dismiss();
            }
        });
        btc.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

        dialog.show();
    }

    public void onAddEditText() {
        final Dialog dialog = new Dialog(MakeDevicesActivity.this);
        dialog.setTitle(R.string.make);
        dialog.setContentView(R.layout.input_box);
        TextView txtMessage = (TextView) dialog.findViewById(R.id.txtmessage);
        txtMessage.setText(R.string.make);
        txtMessage.setTextColor(Color.parseColor("#ff2222"));
        TextView txtstatus = (TextView) dialog.findViewById(R.id.txtstatus);
        txtstatus.setText(R.string.valid);
        txtstatus.setTextColor(Color.parseColor("#22ff22"));
        final EditText editText = (EditText) dialog.findViewById(R.id.txtinput);
        editText.setText("");
        Button bt = (Button) dialog.findViewById(R.id.btdone);
        Button btc = (Button) dialog.findViewById(R.id.btcancel);
        bt.setText(R.string.make);
        bt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String userid = uData.userid;
                String dtype = editText.getText().toString();
                String url = "http://www.shanvishield.com/safety/safety.php?go=makeDevice&pid=" + userid +
                        "&dtype=" + dtype;
                processTableRequest(url, "Add");
                dialog.dismiss();
            }
        });
        btc.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

        dialog.show();
    }

    public void onClick(TableRow tableRow, TableAdapter tableAdapter) {
    }

    public void onDestroy() {
        Log.d(TAG, "Destroy!");
        super.onDestroy();
    }
}
