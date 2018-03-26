package com.shanvi.android.shanvi;

import android.support.design.widget.TabLayout;
import android.util.Log;

import org.json.JSONArray;

/**
 * Created by Debashis on 3/18/2018.
 */

public class TableRow {
    private static final String TAG = TableRow.class.getSimpleName();

    public String name;
    public String username;
    public String type;
    public String number;
    public String feature;
    public int status;
    public String tid;
    public String pid;
    public String response;

    public TableRow() {
        status = 0;
    }

    public static TableRow parsePurchasedDevices(JSONArray array) {
        if (array.length() < 3) {
            return null;
        }
        try {
            TableRow res = new TableRow();
            res.name = array.getString(0);
            res.type = array.getString(2);
            res.username = res.name;
            res.tid = res.type;
            res.feature = array.getString(1);
            res.number = "";
            return res;
        } catch (Exception e) {
            return null;
        }
    }

    public static TableRow parseSales(JSONArray array) {
        if (array.length() < 4) {
            return null;
        }
        try {
            TableRow res = new TableRow();
            res.name = array.getString(0);
            res.type = array.getString(3);
            res.username = res.name;
            res.tid = res.type;
            res.feature = array.getString(1);
            res.number = array.getString(2);
            return res;
        } catch (Exception e) {
            return null;
        }
    }

    public static TableRow parseTriggers(JSONArray array) {
        return parseMyTriggers(array);
    }

    public static TableRow parseStock(JSONArray array) {
        return parseDevices(array);
    }

    public static TableRow parseLocation(JSONArray array) {
        return parseDevices(array);
    }

    public static TableRow parseDevices(JSONArray array) {
        if (array.length() < 2) {
            return null;
        }
        try {
            TableRow res = new TableRow();
            res.name = array.getString(0);
            res.type = array.getString(1);
            res.username = res.name;
            res.tid = res.type;
            res.feature = "";
            res.number = "";
            return res;
        } catch (Exception e) {
            return null;
        }
    }

    public static TableRow parseResponses(JSONArray array) {
        if (array.length() < 8) {
            return null;
        }
        try {
            TableRow res = new TableRow();
            res.name = array.getString(1) + " " + array.getString(0);
            res.username = array.getString(2);
            res.tid = array.getString(5);
            res.pid = array.getString(6);
            res.response = array.getString(7);
            res.type = array.getString(2) + " " + array.getString(5) + " " + res.response;
            res.feature = "d=" + array.getString(4);
            res.number = "";
            return res;
        } catch (Exception e) {
            return null;
        }
    }

    public static TableRow parseMyTriggers(JSONArray array) {
        if (array.length() < 7) {
            return null;
        }
        try {
            TableRow res = new TableRow();
            res.name = array.getString(1) + " " + array.getString(0);
            res.username = array.getString(2);
            res.tid = array.getString(5);
            res.response = array.getString(6);
            res.type = array.getString(2) + " " + array.getString(5) + " " + array.getString(6);
            res.feature = "d=" + array.getString(4);
            res.number = "";
            return res;
        } catch (Exception e) {
            return null;
        }
    }

    public static TableRow parseCircle(JSONArray array) {
        if (array.length() < 4) {
            return null;
        }
        try {
            TableRow res = new TableRow();
            res.name = array.getString(1) + " " + array.getString(0);
            res.username = array.getString(2);
            res.type = array.getString(2) + " " + array.getString(3);
            if (array.length() > 4) {
                res.feature = array.getString(4);
                if (res.feature.equals("null")) {
                    res.feature = "";
                    res.status = 1;
                }
            }
            else {
                res.feature = "";
            }
            res.number = "";
            res.debugPrint();
            return res;
        } catch (Exception e) {
          return null;
        }
    }

    public void debugPrint() {
        Log.d(TAG, name + " " + type);
    }

}
