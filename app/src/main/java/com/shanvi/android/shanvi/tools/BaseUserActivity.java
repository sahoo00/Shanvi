package com.shanvi.android.shanvi.tools;

import android.annotation.TargetApi;
import android.app.Activity;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.Volley;
import com.shanvi.android.shanvi.MySingleton;
import com.shanvi.android.shanvi.R;
import com.shanvi.android.shanvi.TableAdapter;
import com.shanvi.android.shanvi.TableRow;
import com.shanvi.android.shanvi.UserData;

import org.json.JSONArray;

import java.util.ArrayList;

/**
 * Created by Debashis on 3/22/2018.
 */

public class BaseUserActivity extends AppCompatActivity implements ListView.OnItemClickListener {
    private static final String TAG = BaseUserActivity.class.getSimpleName();

    public static final String EXTRA_DATA = "EXTRA_DATA";

    protected TextView mLocalTextView;
    protected ListView mLocalListView;

    protected TableAdapter tableAdapter;
    protected ArrayList<TableRow> tableRows;

    protected UserData uData;

    protected Activity thisActivity = this;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user);
        setupActionBar();

        mLocalTextView = findViewById(R.id.mText2);
        mLocalListView = findViewById(R.id.mList2);

        uData = (UserData) getIntent().getSerializableExtra(EXTRA_DATA);
        Log.d(TAG, "BaseUserActivity");
        mLocalTextView.setText(R.string.title_activity_base);
        mLocalListView.setOnItemClickListener(this);
        showTable();
    }

    protected void setupActionBar() {
        ActionBar ab = getSupportActionBar();
        if (ab != null) {
            ab.setDisplayHomeAsUpEnabled(true);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.activity_user_toolbar, menu);
        return true;
    }

    protected void showTable() {
    }

    protected void onTableResponse(JSONArray table, String type) {

    }

    public void onClick(TableRow tableRow, TableAdapter tableAdapter) {

    }

    @Override
    public void onItemClick(AdapterView<?> arg0, View arg1, int position, long id) {
        TableRow tableRow = (TableRow) tableAdapter.getItem(position);
        onClick(tableRow, tableAdapter);
    }

    public void processTableRequest(String url, String type) {
        RequestQueue queue = Volley.newRequestQueue(this);
        JsonArrayRequest jsonObjectRequest = new JsonArrayRequest
                (Request.Method.GET, url, null, new Response.Listener<JSONArray>() {

                    @Override
                    public void onResponse(JSONArray response) {
                        Log.d(TAG, response.toString());
                        String str = "Error!";
                        try {
                            onTableResponse(response, type);
                            return;

                        } catch(Exception e) {
                        }
                        Toast.makeText(thisActivity, str, Toast.LENGTH_SHORT).show();
                    }
                }, new Response.ErrorListener() {

                    @Override
                    public void onErrorResponse(VolleyError error) {
                        String str = "Error!";
                        Toast.makeText(thisActivity, str, Toast.LENGTH_SHORT).show();
                    }
                });

        // Access the RequestQueue through your singleton class.
        MySingleton.getInstance(this).addToRequestQueue(jsonObjectRequest);
    }

    protected void onDestroy() {
        Log.d(TAG, "Destroy!");
        super.onDestroy();
    }

    public void onAdd() {

    }

    public boolean onOptionsItemSelected (MenuItem item) {
        int id = item.getItemId();
        switch(item.getItemId()) {
            case R.id.home:
                onBackPressed();
                break;
            case R.id.homeAsUp:
                onBackPressed();
                break;
            case R.id.action_reload:
                recreate();
                break;
            case R.id.action_add:
                onAdd();
                break;
            case android.R.id.home:
                onBackPressed();
                break;
            default:
                return super.onOptionsItemSelected(item);
        }
        return true;
    }

}
