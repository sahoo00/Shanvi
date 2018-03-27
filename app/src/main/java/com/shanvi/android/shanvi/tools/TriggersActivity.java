package com.shanvi.android.shanvi.tools;

import android.app.Dialog;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
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

public class TriggersActivity extends BaseUserActivity {
    private static final String TAG = TriggersActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "TriggersActivity");
        mLocalTextView.setText(R.string.title_activity_triggers);
    }

    protected void showTable() {
        String userid = uData.userid;
        String url = "http://www.shanvishield.com/safety/safety.php?go=getTriggers&pid=" + userid;
        processTableRequest(url, "Triggers");
    }

    protected void onTableResponse(JSONArray table, String type) {
        try {
            if (type.equals("Triggers")) {
                tableRows = new ArrayList<TableRow>();
                for (int i = 0; i < table.length(); i++) {
                    JSONArray arr = table.getJSONArray(i);
                    TableRow row = TableRow.parseTriggers(arr);
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

    public void onClick(TableRow tableRow, TableAdapter tableAdapter) {
        if (tableRow.response.equals("1")) {
            return;
        }
        Log.d(TAG, ":" + tableRow.response + ":");
        final Dialog dialog=new Dialog(TriggersActivity.this);
        dialog.setTitle("Respond");
        dialog.setContentView(R.layout.input_box);
        TextView txtMessage=(TextView)dialog.findViewById(R.id.txtmessage);
        txtMessage.setText(R.string.respond);
        txtMessage.setTextColor(Color.parseColor("#ff2222"));
        final EditText editText=(EditText)dialog.findViewById(R.id.txtinput);
        editText.setText(R.string.yes);
        Button bt=(Button)dialog.findViewById(R.id.btdone);
        Button btc=(Button)dialog.findViewById(R.id.btcancel);
        bt.setText(R.string.response);
        bt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                tableRow.type = tableRow.username + " " + tableRow.tid + " " + 1;
                tableAdapter.notifyDataSetChanged();
                String userid = uData.userid;
                String url = "http://www.shanvishield.com/safety/safety.php?go=addResponse&pid=" + userid +
                        "&tid=" + tableRow.tid;
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

    public void onDestroy() {
        Log.d(TAG, "Destroy!");
        super.onDestroy();
    }
}
