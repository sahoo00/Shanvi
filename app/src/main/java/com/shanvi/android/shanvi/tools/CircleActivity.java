package com.shanvi.android.shanvi.tools;

import android.app.Dialog;
import android.app.SearchManager;
import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.widget.SearchView;
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
 * Created by Debashis on 3/22/2018.
 */

public class CircleActivity extends BaseUserActivity implements SearchView.OnQueryTextListener {
    private static final String TAG = CircleActivity.class.getSimpleName();

    SearchView searchView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "CircleActivity");
        mLocalTextView.setText(R.string.title_activity_circle);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.activity_circle_toolbar, menu);

        // Associate searchable configuration with the SearchView
        SearchManager searchManager =
                (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        searchView =
                (SearchView) menu.findItem(R.id.action_search).getActionView();
        searchView.setOnQueryTextListener(this);

        return true;
    }

    @Override
    public boolean onQueryTextSubmit(String query) {
        Log.d(TAG, query);
        String userid = uData.userid;
        String url = "http://www.shanvishield.com/safety/safety.php?go=searchCircle&pid=" + userid + "&search=" + query;
        processTableRequest(url, "Circle");
        //searchView.clearFocus();
        return false;
    }

    @Override
    public boolean onQueryTextChange(String newText) {
        return true;
    }

    protected void showTable() {
        String userid = uData.userid;
        String url = "http://www.shanvishield.com/safety/safety.php?go=getCircle&pid=" + userid;
        processTableRequest(url, "Circle");
    }

    protected void onTableResponse(JSONArray table, String type) {
        try {
            if (type.equals("Circle")) {
                tableRows = new ArrayList<TableRow>();
                for (int i = 0; i < table.length(); i++) {
                    JSONArray arr = table.getJSONArray(i);
                    TableRow row = TableRow.parseCircle(arr);
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
        Log.d(TAG, tableRow.type);
        final Dialog dialog=new Dialog(CircleActivity.this);
        dialog.setTitle(R.string.title_inputbox);
        dialog.setContentView(R.layout.input_box);
        TextView txtMessage=(TextView)dialog.findViewById(R.id.txtmessage);
        txtMessage.setText(R.string.relationship);
        txtMessage.setTextColor(Color.parseColor("#ff2222"));
        final EditText editText=(EditText)dialog.findViewById(R.id.txtinput);
        editText.setText(tableRow.feature);
        Button bt=(Button)dialog.findViewById(R.id.btdone);
        Button btc=(Button)dialog.findViewById(R.id.btcancel);
        bt.setText(R.string.add);
        if (tableRow.status == 0) {
            editText.setText(tableRow.name);
            editText.setEnabled(false);
            bt.setText(R.string.remove);
        }
        bt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (tableRow.status == 1) {
                    tableRow.feature = editText.getText().toString();
                    tableAdapter.notifyDataSetChanged();
                    String userid = uData.userid;
                    String url = "http://www.shanvishield.com/safety/safety.php?go=addCircle&pid=" + userid +
                            "&username=" + tableRow.username + "&relationship=" + tableRow.feature;
                    processTableRequest(url, "Add");
                }
                else {
                    String userid = uData.userid;
                    String url = "http://www.shanvishield.com/safety/safety.php?go=removeCircle&pid=" + userid +
                            "&username=" + tableRow.username + "&relationship=" + tableRow.feature;
                    processTableRequest(url, "Remove");
                }
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
