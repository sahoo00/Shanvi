package com.shanvi.android.shanvi.tools;

import android.app.Dialog;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
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

public class LocationActivity extends BaseUserActivity {
    private static final String TAG = LocationActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "Location Activity");
        mLocalTextView.setText(R.string.title_activity_location);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.activity_devices_toolbar, menu);
        return true;
    }

    protected void showTable() {
        String userid = uData.userid;
        String url = "http://www.shanvishield.com/safety/safety.php?go=getPersonLocation&pid=" + userid;
        processTableRequest(url, "Loc");
    }

    protected void onTableResponse(JSONArray table, String type) {
        try {
            if (type.equals("Loc")) {
                tableRows = new ArrayList<TableRow>();
                for (int i = 0; i < table.length(); i++) {
                    JSONArray arr = table.getJSONArray(i);
                    TableRow row = TableRow.parseLocation(arr);
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

    public boolean validLocation(String lat, String lon) {
        try {
            float la = Float.parseFloat(lat);
            float lo = Float.parseFloat(lon);
            return true;
        }
        catch (Exception e) {
            return false;
        }
    }

    public void onAdd() {
        TableRow tableRow = null;
        if (tableRows.size() > 0) {
            tableRow = tableRows.get(0);
        }
        onClick(tableRow, tableAdapter);
    }

    public void onClick(TableRow tableRow, TableAdapter tableAdapter) {
        final Dialog dialog=new Dialog(LocationActivity.this);
        dialog.setTitle(R.string.location);
        dialog.setContentView(R.layout.input_box_two);
        TextView txtMessage=(TextView)dialog.findViewById(R.id.txtmessage);
        txtMessage.setText(R.string.lat);
        txtMessage.setTextColor(Color.parseColor("#ff2222"));
        TextView txtMessage2=(TextView)dialog.findViewById(R.id.txtmessage2);
        txtMessage2.setText(R.string.lon);
        txtMessage2.setTextColor(Color.parseColor("#ff2222"));
        TextView txtstatus=(TextView)dialog.findViewById(R.id.txtstatus);
        txtstatus.setText(R.string.valid);
        txtstatus.setTextColor(Color.parseColor("#22ff22"));
        final EditText editText=(EditText)dialog.findViewById(R.id.txtinput);
        editText.setText("");
        final EditText editText2=(EditText)dialog.findViewById(R.id.txtinput2);
        editText2.setText("");
        Button bt=(Button)dialog.findViewById(R.id.btdone);
        Button btc=(Button)dialog.findViewById(R.id.btcancel);
        bt.setText(R.string.add);
        bt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String lat = editText.getText().toString();
                String lon = editText2.getText().toString();
                if (validLocation(lat,lon)) {
                    if (tableRow != null) {
                        tableRow.name = lat;
                        tableRow.type = lon;
                        tableAdapter.notifyDataSetChanged();
                    }
                    String userid = uData.userid;
                    String url = "http://www.shanvishield.com/safety/safety.php?go=addPersonLocation&pid=" + userid +
                            "&lat=" + lat + "&lon=" + lon;
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

    public void onDestroy() {
        Log.d(TAG, "Destroy!");
        super.onDestroy();
    }

}
