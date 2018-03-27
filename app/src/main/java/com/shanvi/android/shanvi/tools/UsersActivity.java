package com.shanvi.android.shanvi.tools;

import android.app.Activity;
import android.app.Dialog;
import android.graphics.Color;
import android.graphics.Point;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.shanvi.android.shanvi.MySingleton;
import com.shanvi.android.shanvi.R;
import com.shanvi.android.shanvi.TableAdapter;
import com.shanvi.android.shanvi.TableRow;

import org.json.JSONArray;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Debashis on 3/24/2018.
 */

public class UsersActivity extends BaseUserActivity {
    private static final String TAG = UsersActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "Users Activity");
        mLocalTextView.setText(R.string.title_activity_users);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.activity_devices_toolbar, menu);
        return true;
    }

    protected void showTable() {
        String userid = uData.userid;
        String url = "http://www.shanvishield.com/safety/safety.php?go=getUsers&pid=" + userid;
        processTableRequest(url, "Users");
    }

    protected void onTableResponse(JSONArray table, String type) {
        try {
            if (type.equals("Users")) {
                tableRows = new ArrayList<TableRow>();
                for (int i = 0; i < table.length(); i++) {
                    JSONArray arr = table.getJSONArray(i);
                    TableRow row = TableRow.parseUsers(arr);
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
            if (type.equals("Delete") || type.equals("register")) {
                if (table.length() > 1 && table.getBoolean(0)) {
                    String str = "Success";
                    Toast.makeText(thisActivity, str, Toast.LENGTH_SHORT).show();
                }
                else {
                    String str = "Error!";
                    Toast.makeText(thisActivity, str, Toast.LENGTH_SHORT).show();
                }
            }
        } catch (Exception e) {
        }
    }

    public static int getScreenWidth(Activity activity) {
        Point size = new Point();
        activity.getWindowManager().getDefaultDisplay().getSize(size);
        return size.x;
    }

    public void onAdd() {
        final Dialog dialog = new Dialog(UsersActivity.this);
        dialog.setTitle(R.string.users);
        dialog.setContentView(R.layout.activity_register);
        dialog.getWindow()
                .setLayout((int) (getScreenWidth(thisActivity) * .9), ViewGroup.LayoutParams.MATCH_PARENT);
        CheckBox saveLogin = (CheckBox) dialog.findViewById(R.id.saveLogin);
        saveLogin.setVisibility(View.GONE);
        EditText firstNameView = (EditText)  dialog.findViewById(R.id.firstname);
        EditText lastNameView = (EditText)  dialog.findViewById(R.id.lastname);
        EditText userNameView = (EditText)  dialog.findViewById(R.id.username);
        EditText emailView = (EditText)  dialog.findViewById(R.id.email);
        EditText p1View = (EditText)  dialog.findViewById(R.id.password1);
        EditText p2View = (EditText)  dialog.findViewById(R.id.password2);
        EditText addressView = (EditText)  dialog.findViewById(R.id.address);
        EditText cityView = (EditText)  dialog.findViewById(R.id.city);
        EditText stateView = (EditText)  dialog.findViewById(R.id.state);
        EditText countryView = (EditText)  dialog.findViewById(R.id.country);
        EditText zipcodeView = (EditText)  dialog.findViewById(R.id.zipcode);
        Button bt = (Button) dialog.findViewById(R.id.register_button);
        bt.setText(R.string.add);
        Button btc = (Button) dialog.findViewById(R.id.cancel_button);
        btc.setVisibility(View.VISIBLE);
        bt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String firstname = firstNameView.getText().toString();
                String lastname = lastNameView.getText().toString();
                String username = userNameView.getText().toString();
                String email = emailView.getText().toString();
                String password1 = p1View.getText().toString();
                String password2 = p2View.getText().toString();
                String address = addressView.getText().toString();
                String city = cityView.getText().toString();
                String state = stateView.getText().toString();
                String country = countryView.getText().toString();
                String zipcode = zipcodeView.getText().toString();
                if (!password1.equals(password2)) {
                    p2View.setError(getString(R.string.error_nomatch_password));
                    p2View.requestFocus();
                    return;
                }
                try {
                    String random = MySingleton.getRandomString(16);
                    String sha1 = MySingleton.SHA1(password1);
                    String url = "http://www.shanvishield.com/safety/auth.php?" +
                            "FirstName=" + firstname + "&LastName=" + lastname + "&sha1=" + sha1 +
                            "&username=" + username + "&Email=" + email + "&Address=" + address +
                            "&City=" + city + "&State=" + state + "&Country=" + country +
                            "&Zipcode=" + zipcode +
                            "&password1=" + random + "&password2=" + random + "&sha1=" + sha1 +
                            "&op=signup&type=json";
                    Log.d(TAG, url);
                    processTableRequest(url, "register");
                    dialog.dismiss();
                }
                catch (Exception e) {
                    userNameView.setError(getString(R.string.error_add_user));
                    userNameView.requestFocus();
                    return;
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
        final Dialog dialog = new Dialog(UsersActivity.this);
        dialog.setTitle(R.string.users);
        dialog.setContentView(R.layout.input_box_select);
        TextView txtMessage = (TextView) dialog.findViewById(R.id.txtmessage);
        txtMessage.setText(R.string.role);
        txtMessage.setTextColor(Color.parseColor("#ff2222"));
        TextView txtstatus = (TextView) dialog.findViewById(R.id.txtstatus);
        txtstatus.setText(R.string.status);
        txtstatus.setTextColor(Color.parseColor("#A569BD"));
        final Spinner spinner = (Spinner) dialog.findViewById(R.id.txtinput);
        List<String> categories = new ArrayList<String>();
        categories.add("verified");
        categories.add("user");
        categories.add("admin");
        // Creating adapter for spinner
        ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, categories);
        // Drop down layout style - list view with radio button
        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // attaching data adapter to spinner
        spinner.setAdapter(dataAdapter);
        Button bt = (Button) dialog.findViewById(R.id.btdone);
        Button btc = (Button) dialog.findViewById(R.id.btcancel);
        Button btd = (Button) dialog.findViewById(R.id.btdelete);
        bt.setText(R.string.set);
        bt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String username = tableRow.username;
                String role = spinner.getSelectedItem().toString();
                String url = "http://www.shanvishield.com/safety/safety.php?go=updateRole&username=" + username +
                        "&role=" + role;
                Log.d(TAG, url);
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
        btd.setVisibility(View.VISIBLE);
        btd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String username = tableRow.username;
                String url = "http://www.shanvishield.com/safety/auth.php?op=unregister&username=" + username +
                        "&type=json";
                Log.d(TAG, url);
                processTableRequest(url, "Delete");
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
