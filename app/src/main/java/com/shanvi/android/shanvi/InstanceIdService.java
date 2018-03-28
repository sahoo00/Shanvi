package com.shanvi.android.shanvi;

import android.util.Log;

import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.FirebaseInstanceIdService;

import java.io.DataOutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by Debashis Sahoo
 */

public class InstanceIdService extends FirebaseInstanceIdService {
    private static final String TAG = InstanceIdService.class.getSimpleName();

    public InstanceIdService() {
        super();
        Log.d(TAG, "InstanceIdService started");
    }

    @Override
    public void onTokenRefresh() {
        super.onTokenRefresh();
        String token = FirebaseInstanceId.getInstance().getToken();
        Log.d(TAG, "FCM Token = " + token);
        //sends this token to the server
        sendToServer(token);
    }

    private void sendToServer(String token) {

        try {
            URL url = new URL("http://www.shanvishield.com/safety/safety.php");
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();

            connection.setDoOutput(true);
            connection.setDoInput(true);

            connection.setRequestMethod("POST");

            DataOutputStream dos = new DataOutputStream(connection.getOutputStream());

            dos.writeBytes("token=" + token);
            dos.writeBytes("&go=storeToken");

            connection.connect();

            if (connection.getResponseCode() == HttpURLConnection.HTTP_OK) {
                // Do whatever you want after the
                // token is successfully stored on the server
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}