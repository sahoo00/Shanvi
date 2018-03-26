package com.shanvi.android.shanvi;

import org.json.JSONArray;

import java.io.Serializable;

/**
 * Created by Debashis on 3/17/2018.
 */

public class UserData implements Serializable {
    public String username;
    public String userid;
    public String role;
    public String email;

    public UserData() {

    }

    public static UserData parseUserData(JSONArray array) {
        UserData uData = new UserData();
        try {
            uData.username = array.getString(0);
            uData.email = array.getString(1);
            uData.role = array.getString(2);
            uData.userid = array.getString(3);
        } catch(Exception e) {

        }
        return uData;
    }

    public static UserData parseUserData(String uData) {
        try {
            JSONArray array = new JSONArray(uData);
            return parseUserData(array);
        } catch(Exception e) {
        }
        return null;
    }
}
