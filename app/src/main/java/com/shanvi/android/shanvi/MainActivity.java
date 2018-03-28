package com.shanvi.android.shanvi;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.bluetooth.BluetoothManager;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.hardware.TriggerEvent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TabLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.firebase.iid.FirebaseInstanceId;
import com.shanvi.android.shanvi.tools.CircleActivity;
import com.shanvi.android.shanvi.tools.DevicesActivity;
import com.shanvi.android.shanvi.tools.LocationActivity;
import com.shanvi.android.shanvi.tools.MakeDevicesActivity;
import com.shanvi.android.shanvi.tools.MyTriggersActivity;
import com.shanvi.android.shanvi.tools.PDevsActivity;
import com.shanvi.android.shanvi.tools.ResponsesActivity;
import com.shanvi.android.shanvi.tools.SalesActivity;
import com.shanvi.android.shanvi.tools.TriggersActivity;
import com.shanvi.android.shanvi.tools.UsersActivity;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.net.CookieHandler;
import java.net.CookieManager;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {
    private static final String TAG = MainActivity.class.getSimpleName();

    private static final int REQUEST_RESPONSE = 1;

    private BluetoothManager mBluetoothManager;
    private MainBLEServer bleServer;
    private MainBLEClient bleClient;

    /* Local UI */
    private TextView mLocalTextView;
    private ListView mLocalListView;
    private ArrayAdapter<String> adapter;
    private List<String> myStringArray = new ArrayList<String>();
    private TableAdapter tableAdapter;
    private ArrayList<TableRow> tableRows;
    private MenuItem mdevs_menu;
    private MenuItem users_menu;

    private boolean loggedIn = false;
    private UserData uData;

    private Activity thisActivity = this;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "Create MainActivity");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mLocalTextView = findViewById(R.id.mText1);
        mLocalListView = findViewById(R.id.mList1);
        adapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_list_item_1, myStringArray);
        mLocalListView.setAdapter(adapter);
        if (uData != null) {
            String lText = uData.username + " logged in";
            mLocalTextView.setText(lText);
        }

        CookieHandler.setDefault(new CookieManager());

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        Activity thisActivity = this;
        // Make sure we have access coarse location enabled, if not, prompt the user to enable it
        if (ContextCompat.checkSelfPermission(thisActivity, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            new AlertDialog.Builder(this, android.R.style.Theme_Material_Dialog_Alert)
                    .setTitle("Location Access")
                    .setMessage("Please grant")
                    .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            ActivityCompat.requestPermissions(thisActivity,
                                    new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, 1);
                        }
                    })
                    .show();
        }

        // Use this check to determine whether BLE is supported on the device. Then
        // you can selectively disable BLE-related features.
        if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            Toast.makeText(this, "BLE not supported", Toast.LENGTH_SHORT).show();
            //finish();
        }

        mBluetoothManager = (BluetoothManager) getSystemService(BLUETOOTH_SERVICE);

        String token = FirebaseInstanceId.getInstance().getToken();
        Log.d(TAG, "FCM token 1 : " + token);
    }

    @Override
    public void onBackPressed() {
        Log.d(TAG, "Back Pressed!");
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        mdevs_menu = menu.findItem(R.id.action_mdevs);
        users_menu = menu.findItem(R.id.action_users);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case R.id.action_users:
                manageUsers(null); break;
            case R.id.action_mdevs:
                makeDevices(null); break;
            case R.id.action_pdevs:
                purchasedDevices(null); break;
            case R.id.action_circle:
                safetyCircle(null); break;
            case R.id.action_mytriggers:
                myTriggers(null); break;
            case R.id.action_location:
                location(null); break;
            case R.id.action_devices:
                devices(null); break;
            case R.id.action_responses:
                responses(null); break;
            case R.id.action_triggers:
                triggersToRespond(null); break;
            case R.id.action_buy:
                buyDevices(null); break;
            default:
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        switch(id) {
            case R.id.nav_client:
                if (bleClient == null) {
                    bleClient = new MainBLEClient(this, mBluetoothManager);
                }
                break;
            case R.id.nav_server:
                if (bleServer == null) {
                    bleServer = new MainBLEServer(this, mBluetoothManager);
                }
                break;
            case R.id.nav_manage:
                if (bleServer != null) {
                    bleServer.changeAlert();
                }
                break;
            case R.id.nav_clear:
                if (bleServer != null) {
                    bleServer.clear();
                    bleServer = null;
                }
                if (bleClient != null) {
                    bleClient.clear();
                    bleClient = null;
                }
                break;
            case R.id.nav_signal: {
                Intent myIntent = new Intent(MainActivity.this,
                        SignalActivity.class);
                startActivity(myIntent);
            } break;
            case R.id.nav_login: {
                Intent myIntent = new Intent(MainActivity.this,
                        LoginActivity.class);
                startActivityForResult(myIntent, REQUEST_RESPONSE);
            } break;
            default:
                break;
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            if (requestCode == REQUEST_RESPONSE) {
                String ud = data.getStringExtra(LoginActivity.EXTRA_RESPONSE);
                uData = UserData.parseUserData(ud);
                if (uData != null && (uData.role.equals("admin") || uData.role.equals("verified"))) {
                    Toast.makeText(this, uData.username, Toast.LENGTH_SHORT).show();
                    String lText = uData.username + " logged in";
                    mLocalTextView.setText(lText);
                    loggedIn = true;
                    if (uData.role.equals("admin")) {
                        Button btn = (Button) findViewById(R.id.button11);
                        btn.setVisibility(View.VISIBLE);
                        mdevs_menu.setVisible(true);
                        users_menu.setVisible(true);
                    }
                }
                else {
                    uData = null;
                }
            }
        }
    }

    public void startScanning(View v) {
        if (bleClient != null) {
            bleClient.scanLeDevice(true);
        }
    }

    public void startTest(View v) {

        try {
            // Instantiate the RequestQueue.
            RequestQueue queue = Volley.newRequestQueue(this);
            String sha1 = MySingleton.SHA1("sahoo123");
            String random = MySingleton.getRandomString(16);
            String url = "http://www.shanvishield.com/safety/auth.php?username=sahoo00&password1=" + random +
                    "&sha1=" + sha1 + "&op=login&type=json";

            JsonArrayRequest jsonObjectRequest = new JsonArrayRequest
                    (Request.Method.GET, url, null, new Response.Listener<JSONArray>() {

                        @Override
                        public void onResponse(JSONArray response) {
                            myStringArray.clear();
                            try {
                                for (int i = 0; i < response.length(); i++) {
                                    myStringArray.add(response.getString(i));
                                }
                            } catch (Exception e) { }
                            adapter.notifyDataSetChanged();
                        }
                    }, new Response.ErrorListener() {

                        @Override
                        public void onErrorResponse(VolleyError error) {
                            // TODO: Handle error
                            myStringArray.clear();
                            myStringArray.add("That didn't work!");
                            adapter.notifyDataSetChanged();
                        }
                    });

            // Access the RequestQueue through your singleton class.
            MySingleton.getInstance(this).addToRequestQueue(jsonObjectRequest);


            if (false) {
                // Request a string response from the provided URL.
                StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                        new Response.Listener<String>() {
                            @Override
                            public void onResponse(String response) {
                                // Display the first 500 characters of the response string.
                                String str = response;
                                myStringArray.clear();
                                myStringArray.add(str);
                                adapter.notifyDataSetChanged();
                            }
                        }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        myStringArray.clear();
                        myStringArray.add("That didn't work!");
                        adapter.notifyDataSetChanged();
                    }
                });

                // Add the request to the RequestQueue.
                queue.add(stringRequest);
            }

        }
        catch (Exception e) {
            myStringArray.clear();
            myStringArray.add("That didn't work!");
            adapter.notifyDataSetChanged();
        }
    }

    public void processRequest(String url) {
        RequestQueue queue = Volley.newRequestQueue(this);
        JsonArrayRequest jsonObjectRequest = new JsonArrayRequest
                (Request.Method.GET, url, null, new Response.Listener<JSONArray>() {

                    @Override
                    public void onResponse(JSONArray response) {
                        Log.d(TAG, response.toString());
                        String str = "Error!";
                        try {
                            if (response.length() > 1) {
                                str = response.getString(1);
                                Toast.makeText(thisActivity, str, Toast.LENGTH_SHORT).show();
                                return;
                            }
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

    public void startTrigger(View v) {
        if (uData != null) {
            String userid = uData.userid;
            String url = "http://www.shanvishield.com/safety/safety.php?go=Trigger&pid=" + userid;
            Log.d(TAG, url);
            processRequest(url);
        }
    }

    public void cancelTrigger(View v) {
        if (uData != null) {
            String userid = uData.userid;
            String url = "http://www.shanvishield.com/safety/safety.php?go=closeTrigger&pid=" + userid;
            processRequest(url);
        }
    }

    public void logout(View v) {
        mLocalTextView.setText(R.string.suggest_login);
        loggedIn = false;
        uData = null;
    }

    public void onTableResponse(JSONArray table, String type) {
        try {
            tableRows = new ArrayList<TableRow>();
            for (int i = 0; i < table.length(); i++) {
                JSONArray arr = table.getJSONArray(i);
                TableRow row = null;
                if (type.equals("Circle")) {
                    row = TableRow.parseCircle(arr);
                }
                if (row != null) {
                    tableRows.add(row);
                }
            }
            tableAdapter = new TableAdapter(tableRows, this);
            mLocalListView.setAdapter(tableAdapter);
        } catch (Exception e) {
        }
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
                            if (response.length() > 0) {
                                str = "Success";
                                onTableResponse(response, type);
                                Toast.makeText(thisActivity, str, Toast.LENGTH_SHORT).show();
                                return;
                            }
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

    public void safetyCircle(View v) {
        if (uData != null) {
            Intent myIntent = new Intent(this, CircleActivity.class);
            myIntent.putExtra(CircleActivity.EXTRA_DATA, uData);
            startActivity(myIntent);
        }
    }

    public void myTriggers(View v) {
        if (uData != null) {
            Intent myIntent = new Intent(this, MyTriggersActivity.class);
            myIntent.putExtra(MyTriggersActivity.EXTRA_DATA, uData);
            startActivity(myIntent);
        }
    }

    public void location(View v) {
        if (uData != null) {
            Intent myIntent = new Intent(this, LocationActivity.class);
            myIntent.putExtra(LocationActivity.EXTRA_DATA, uData);
            startActivity(myIntent);
        }
    }

    public void devices(View v) {
        if (uData != null) {
            Intent myIntent = new Intent(this, DevicesActivity.class);
            myIntent.putExtra(DevicesActivity.EXTRA_DATA, uData);
            startActivity(myIntent);
        }
    }

    public void buyDevices(View v) {
        if (uData != null) {
            Intent myIntent = new Intent(this, SalesActivity.class);
            myIntent.putExtra(SalesActivity.EXTRA_DATA, uData);
            startActivity(myIntent);
        }
    }

    public void purchasedDevices(View v) {
        if (uData != null) {
            Intent myIntent = new Intent(this, PDevsActivity.class);
            myIntent.putExtra(PDevsActivity.EXTRA_DATA, uData);
            startActivity(myIntent);
        }
    }

    public void makeDevices(View v) {
        if (uData != null) {
            Intent myIntent = new Intent(this, MakeDevicesActivity.class);
            myIntent.putExtra(MakeDevicesActivity.EXTRA_DATA, uData);
            startActivity(myIntent);
        }
    }

    public void responses(View v) {
        if (uData != null) {
            Intent myIntent = new Intent(this, ResponsesActivity.class);
            myIntent.putExtra(ResponsesActivity.EXTRA_DATA, uData);
            startActivity(myIntent);
        }
    }

    public void triggersToRespond(View v) {
        if (uData != null) {
            Intent myIntent = new Intent(this, TriggersActivity.class);
            myIntent.putExtra(TriggersActivity.EXTRA_DATA, uData);
            startActivity(myIntent);
        }
    }

    public void manageUsers(View v) {
        if (uData != null) {
            Intent myIntent = new Intent(this, UsersActivity.class);
            myIntent.putExtra(UsersActivity.EXTRA_DATA, uData);
            startActivity(myIntent);
        }
    }

    public void onDestroy() {
        Log.d(TAG, "Destroy!");
        super.onDestroy();
    }

}
