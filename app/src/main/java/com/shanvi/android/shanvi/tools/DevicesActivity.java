package com.shanvi.android.shanvi.tools;

import android.app.Dialog;
import android.app.SearchManager;
import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.widget.SearchView;
import android.text.InputType;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.shanvi.android.shanvi.R;
import com.shanvi.android.shanvi.TableAdapter;
import com.shanvi.android.shanvi.TableRow;

import org.json.JSONArray;

import java.util.ArrayList;

/**
 * Created by Debashis on 3/24/2018.
 */

public class DevicesActivity extends BaseUserActivity {
    private static final String TAG = DevicesActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "Devices Activity");
        mLocalTextView.setText(R.string.title_activity_devices);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.activity_devices_toolbar, menu);
        return true;
    }

    protected void showTable() {
        String userid = uData.userid;
        String url = "http://www.shanvishield.com/safety/safety.php?go=myDevices&pid=" + userid;
        processTableRequest(url, "Devices");
    }

    protected void onTableResponse(JSONArray table, String type) {
        try {
            if (type.equals("Devices")) {
                tableRows = new ArrayList<TableRow>();
                for (int i = 0; i < table.length(); i++) {
                    JSONArray arr = table.getJSONArray(i);
                    TableRow row = TableRow.parseDevices(arr);
                    tableRows.add(row);
                }
                tableAdapter = new TableAdapter(tableRows, this);
                mLocalListView.setAdapter(tableAdapter);
                String str = "Success";
                Toast.makeText(thisActivity, str, Toast.LENGTH_SHORT).show();
            }
            if (type.equals("Add") || type.equals("Remove")) {
                if (table.length() == 2 && table.getInt(0) == 1) {
                    String str = "Success";
                    Toast.makeText(thisActivity, str, Toast.LENGTH_SHORT).show();
                }
                else {
                    String str = "Error";
                    Toast.makeText(thisActivity, str, Toast.LENGTH_SHORT).show();
                }
            }
        } catch (Exception e) {
        }
    }

    public boolean validDevices(String did, String dtype, String ckey) {
        try {
            Long d = Long.parseLong(did);
            if (dtype.equals("TRG1") || dtype.equals("TRG2") || dtype.equals("MN"))
                return true;
        }
        catch (Exception e) {
            return false;
        }
        return false;
    }

    public void onAdd() {
        final Dialog dialog=new Dialog(DevicesActivity.this);
        dialog.setTitle(R.string.location);
        dialog.setContentView(R.layout.input_box_three);
        TextView txtMessage=(TextView)dialog.findViewById(R.id.txtmessage);
        txtMessage.setText(R.string.did);
        txtMessage.setTextColor(Color.parseColor("#ff2222"));
        TextView txtMessage2=(TextView)dialog.findViewById(R.id.txtmessage2);
        txtMessage2.setText(R.string.dtype);
        txtMessage2.setTextColor(Color.parseColor("#ff2222"));
        TextView txtMessage3=(TextView)dialog.findViewById(R.id.txtmessage3);
        txtMessage3.setText(R.string.ckey);
        txtMessage3.setTextColor(Color.parseColor("#ff2222"));
        TextView txtstatus=(TextView)dialog.findViewById(R.id.txtstatus);
        txtstatus.setText(R.string.valid);
        txtstatus.setTextColor(Color.parseColor("#22ff22"));
        final EditText editText=(EditText)dialog.findViewById(R.id.txtinput);
        editText.setText("");
        final EditText editText2=(EditText)dialog.findViewById(R.id.txtinput2);
        editText2.setText("");
        editText2.setInputType(InputType.TYPE_CLASS_TEXT);
        final EditText editText3=(EditText)dialog.findViewById(R.id.txtinput3);
        editText3.setText("");
        editText3.setInputType(InputType.TYPE_CLASS_TEXT);
        Button bt=(Button)dialog.findViewById(R.id.btdone);
        Button btc=(Button)dialog.findViewById(R.id.btcancel);
        bt.setText(R.string.add);
        bt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String did = editText.getText().toString();
                String dtype = editText2.getText().toString();
                String ckey = editText3.getText().toString();
                if (validDevices(did,dtype, ckey)) {
                    TableRow tableRow = new TableRow();
                    tableRow.name = did;
                    tableRow.type = dtype;
                    tableRow.username = did;
                    tableRow.tid = dtype;
                    tableRows.add(tableRow);
                    tableAdapter.notifyDataSetChanged();
                    String userid = uData.userid;
                    String url = "http://www.shanvishield.com/safety/safety.php?go=addDevice&pid=" + userid +
                            "&did=" + tableRow.username + "&dtype=" + dtype + "&ckey=" + ckey;
                    processTableRequest(url, "Add");
                    dialog.dismiss();
                }
                else {
                    txtstatus.setText(R.string.invalid);
                    txtstatus.setTextColor(Color.parseColor("#ff2222"));
                }
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
        final Dialog dialog=new Dialog(DevicesActivity.this);
        dialog.setTitle(R.string.devices);
        dialog.setContentView(R.layout.input_box);
        TextView txtMessage=(TextView)dialog.findViewById(R.id.txtmessage);
        txtMessage.setText(R.string.ckey);
        txtMessage.setTextColor(Color.parseColor("#ff2222"));
        final EditText editText=(EditText)dialog.findViewById(R.id.txtinput);
        editText.setText("");
        Button bt=(Button)dialog.findViewById(R.id.btdone);
        Button btc=(Button)dialog.findViewById(R.id.btcancel);
        bt.setText(R.string.remove);
        bt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String ckey = editText.getText().toString();
                tableRow.type = tableRow.tid + " removed";
                tableAdapter.notifyDataSetChanged();
                String userid = uData.userid;
                String url = "http://www.shanvishield.com/safety/safety.php?go=removeDevice&pid=" + userid +
                        "&did=" + tableRow.username + "&dtype=" + tableRow.tid + "&ckey=" + ckey;
                processTableRequest(url, "Remove");
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

    public void onDestroy() {
        Log.d(TAG, "Destroy!");
        super.onDestroy();
    }
}
