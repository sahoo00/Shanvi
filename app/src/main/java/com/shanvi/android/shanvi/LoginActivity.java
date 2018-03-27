package com.shanvi.android.shanvi;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.app.LoaderManager.LoaderCallbacks;

import android.content.CursorLoader;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;

import android.os.Build;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/**
 * A login screen that offers login via email/password.
 */
public class LoginActivity extends AppCompatActivity {
    private static final String TAG = LoginActivity.class.getSimpleName();

    public static final String EXTRA_RESPONSE = "EXTRA_RESPONSE";

    private static final String USERNAME_PATTERN = "^[a-z0-9_-]{3,15}$";
    private Pattern pattern = Pattern.compile(USERNAME_PATTERN);
    /**
     * Keep track of the login task to ensure we can cancel it if requested.
     */
    private UserLoginTask mAuthTask = null;

    private Activity thisActivity = this;

    // UI references.
    private EditText mUsernameView;
    private EditText mPasswordView;
    private String sha1;
    private View mProgressView;
    private View mLoginFormView;

    private CheckBox saveLoginCheckBox;
    private SharedPreferences loginPreferences;
    private SharedPreferences.Editor loginPrefsEditor;
    private Boolean saveLogin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setupSignIn();
    }

    void setupSignIn() {
        setContentView(R.layout.activity_login);
        setupActionBar();
        // Set up the login form.
        mUsernameView = (EditText) findViewById(R.id.username);
        mPasswordView = (EditText) findViewById(R.id.password);
        saveLoginCheckBox = (CheckBox)findViewById(R.id.saveLogin);
        loginPreferences = getSharedPreferences("loginPrefs", MODE_PRIVATE);
        loginPrefsEditor = loginPreferences.edit();
        saveLogin = loginPreferences.getBoolean("saveLogin", false);
        if (saveLogin == true) {
            mUsernameView.setText(loginPreferences.getString("username", ""));
            sha1 = loginPreferences.getString("password", "");
            mPasswordView.setText(sha1);
            saveLoginCheckBox.setChecked(true);
        }
        mPasswordView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {
                if (id == EditorInfo.IME_ACTION_DONE || id == EditorInfo.IME_NULL) {
                    attemptLogin();
                    return true;
                }
                return false;
            }
        });

        Button mSignInButton = (Button) findViewById(R.id.sign_in_button);
        mSignInButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                attemptLogin();
            }
        });

        mLoginFormView = findViewById(R.id.login_form);
        mProgressView = findViewById(R.id.login_progress);
    }

    void setupRegister() {
        setContentView(R.layout.activity_register);
        mUsernameView = (EditText) findViewById(R.id.username);
        mPasswordView = (EditText) findViewById(R.id.password1);
        saveLoginCheckBox = (CheckBox)findViewById(R.id.saveLogin);
        loginPreferences = getSharedPreferences("loginPrefs", MODE_PRIVATE);
        loginPrefsEditor = loginPreferences.edit();
        saveLogin = loginPreferences.getBoolean("saveLogin", false);

        Button mSignInButton = (Button) findViewById(R.id.register_button);
        mSignInButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                attemptRegister();
            }
        });
        mLoginFormView = findViewById(R.id.register_form);
        mProgressView = findViewById(R.id.register_progress);
    }

    void setupReset() {
        setContentView(R.layout.activity_reset);
        Button mSignInButton = (Button) findViewById(R.id.reset_button);
        mSignInButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                attemptReset();
            }
        });
        mLoginFormView = findViewById(R.id.reset_form);
        mProgressView = findViewById(R.id.reset_progress);
    }

    /**
     * Set up the {@link android.app.ActionBar}, if the API is available.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    private void setupActionBar() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            // Show the Up button in the action bar.
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.activity_login_toolbar, menu);
        return true;
    }

    /**
     * Attempts to sign in or register the account specified by the login form.
     * If there are form errors (invalid email, missing fields, etc.), the
     * errors are presented and no actual login attempt is made.
     */
    private void attemptLogin() {
        if (mAuthTask != null) {
            return;
        }

        // Reset errors.
        mUsernameView.setError(null);
        mPasswordView.setError(null);

        // Store values at the time of the login attempt.
        String username = mUsernameView.getText().toString();
        String password = mPasswordView.getText().toString();

        boolean cancel = false;
        View focusView = null;

        // Check for a valid password, if the user entered one.
        if (!TextUtils.isEmpty(password) && !isPasswordValid(password)) {
            mPasswordView.setError(getString(R.string.error_invalid_password));
            focusView = mPasswordView;
            cancel = true;
        }

        // Check for a valid username
        if (TextUtils.isEmpty(username)) {
            mUsernameView.setError(getString(R.string.error_field_required));
            focusView = mUsernameView;
            cancel = true;
        }

        if (cancel) {
            // There was an error; don't attempt login and focus the first
            // form field with an error.
            focusView.requestFocus();
        } else {
            // Show a progress spinner, and kick off a background task to
            // perform the user login attempt.
            showProgress(true);
            mAuthTask = new UserLoginTask(username, password, sha1, "login");
            mAuthTask.execute((Void) null);
        }
    }

    private void attemptRegister() {
        if (mAuthTask != null) {
            return;
        }
        String info = "";
        boolean cancel = false;
        View focusView = null;

        mUsernameView.setError(null);
        mPasswordView.setError(null);

        // Store values at the time of the login attempt.
        String username = mUsernameView.getText().toString();
        String password = mPasswordView.getText().toString();

        EditText tmp = (EditText) findViewById(R.id.firstname);
        String firstname = tmp.getText().toString();
        tmp = (EditText) findViewById(R.id.lastname);
        String lastname = tmp.getText().toString();
        tmp = (EditText) findViewById(R.id.password2);
        String password2 = tmp.getText().toString();
        tmp = (EditText) findViewById(R.id.address);
        String address = tmp.getText().toString();
        tmp = (EditText) findViewById(R.id.city);
        String city = tmp.getText().toString();
        tmp = (EditText) findViewById(R.id.state);
        String state = tmp.getText().toString();
        tmp = (EditText) findViewById(R.id.country);
        String country = tmp.getText().toString();
        tmp = (EditText) findViewById(R.id.zipcode);
        String zipcode = tmp.getText().toString();

        EditText mEmail = (EditText) findViewById(R.id.email);
        String email = mEmail.getText().toString();

        info = "FirstName=" + firstname + "&LastName=" + lastname +
                "&username=" + username + "&Email=" + email + "&Address=" + address +
                "&City=" + city + "&State=" + state + "&Country=" + country +
                "&Zipcode=" + zipcode;

        mEmail.setError(null);
        // Check for a valid email
        if (!isEmailValid(email)) {
            mEmail.setError(getString(R.string.error_field_required));
            focusView = mEmail;
            cancel = true;
        }
        if (!isUsernameValid(username)) {
            tmp = (EditText) findViewById(R.id.username);
            tmp.setError(getString(R.string.error_invalid_username));
            focusView = tmp;
            cancel = true;
        }
        if (!isPasswordValid(password)) {
            tmp = (EditText) findViewById(R.id.password);
            tmp.setError(getString(R.string.error_invalid_password));
            focusView = tmp;
            cancel = true;
        }
        if (!isPasswordValid(password2)) {
            tmp = (EditText) findViewById(R.id.password2);
            tmp.setError(getString(R.string.error_invalid_password));
            focusView = tmp;
            cancel = true;
        }
        if (!password2.equals(password)) {
            tmp = (EditText) findViewById(R.id.password2);
            tmp.setError(getString(R.string.error_nomatch_password));
            focusView = tmp;
            cancel = true;
        }

        if (cancel) {
            // There was an error; don't attempt login and focus the first
            // form field with an error.
            focusView.requestFocus();
        } else {
            // Show a progress spinner, and kick off a background task to
            // perform the user register attempt.
            showProgress(true);
            mAuthTask = new UserLoginTask(username, password, email, info, "register");
            mAuthTask.execute((Void) null);
        }
    }

    private void attemptReset() {
        if (mAuthTask != null) {
            return;
        }
        EditText mEmail = (EditText) findViewById(R.id.email);
        String email = mEmail.getText().toString();
        mEmail.setError(null);
        boolean cancel = false;
        View focusView = null;
        // Check for a valid email
        if (!isEmailValid(email)) {
            mEmail.setError(getString(R.string.error_field_required));
            focusView = mEmail;
            cancel = true;
        }
        if (cancel) {
            // There was an error; don't attempt login and focus the first
            // form field with an error.
            focusView.requestFocus();
        } else {
            // Show a progress spinner, and kick off a background task to
            // perform the user register attempt.
            showProgress(true);
            mAuthTask = new UserLoginTask(email, "reset");
            mAuthTask.execute((Void) null);
        }
    }

    private boolean isEmailValid(String email) {
        return email.contains("@");
    }

    private boolean isPasswordValid(String password) {
        return password.length() > 6;
    }

    private boolean isUsernameValid(String username) {
        return pattern.matcher(username).matches();
    }
    /**
     * Shows the progress UI and hides the login form.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    private void showProgress(final boolean show) {
        // On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
        // for very easy animations. If available, use these APIs to fade-in
        // the progress spinner.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
            int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

            mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
            mLoginFormView.animate().setDuration(shortAnimTime).alpha(
                    show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
                }
            });

            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            mProgressView.animate().setDuration(shortAnimTime).alpha(
                    show ? 1 : 0).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
                }
            });
        } else {
            // The ViewPropertyAnimator APIs are not available, so simply show
            // and hide the relevant UI components.
            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
        }
    }

    public UserData getLastActiveUserData() {
        SharedPreferences lpf = getSharedPreferences("loginPrefs", MODE_PRIVATE);
        String uData = lpf.getString("uData", null);
        return UserData.parseUserData(uData);
    }

    /**
     * Represents an asynchronous login/registration task used to authenticate
     * the user.
     */
    public class UserLoginTask extends AsyncTask<Void, Void, Boolean> {

        private String mUsername;
        private String mPassword;
        private String sha1;
        private String type;
        private String email;
        private String info;

        UserLoginTask(String username, String password, String sha1, String type) {
            mUsername = username;
            mPassword = password;
            this.sha1 = sha1;
            this.type = type;
            try {
                if (sha1 == null || !sha1.equals(password)) {
                    this.sha1 = MySingleton.SHA1(mPassword);
                }
            } catch (Exception e) {
            }
        }

        UserLoginTask(String email, String type) {
            this.type = type;
            this.email = email;
        }

        UserLoginTask(String username, String password, String email, String info, String type) {
            this.type = type;
            this.email = email;
            mUsername = username;
            mPassword = password;
            this.info = info;
            try {
                this.sha1 = MySingleton.SHA1(mPassword);
            } catch (Exception e) {
            }
        }

        private void doLogin() throws Exception {
            String random = MySingleton.getRandomString(16);
            String url = "http://www.shanvishield.com/safety/auth.php?username=" + mUsername +
                    "&password1=" + random + "&sha1=" + sha1 + "&op=login&type=json";
            Log.d(TAG, url);
            JsonArrayRequest jsonObjectRequest = new JsonArrayRequest
                    (Request.Method.GET, url, null, new Response.Listener<JSONArray>() {

                        @Override
                        public void onResponse(JSONArray response) {
                            try {
                                if (mAuthTask != null && response.length() > 4) {
                                    boolean status = response.getBoolean(1);
                                    String uData = response.getString(2);
                                    String error = response.getString(3);
                                    mAuthTask.onResponse(status, error, uData);
                                }
                            } catch (Exception e) {
                                if (mAuthTask != null)
                                    mAuthTask.onResponse(false, "JSON Parse error", null);
                            }
                        }
                    }, new Response.ErrorListener() {

                        @Override
                        public void onErrorResponse(VolleyError error) {
                            // TODO: Handle error
                            if (mAuthTask != null)
                                mAuthTask.onResponse(false, "Network error", null);
                        }
                    });

            // Access the RequestQueue through your singleton class.
            MySingleton.getInstance(thisActivity).addToRequestQueue(jsonObjectRequest);
        }

        private void doReset() throws Exception {
            String url = "http://www.shanvishield.com/safety/auth.php?email=" + email +
                    "&op=reset&type=json";
            Log.d(TAG, url);
            JsonArrayRequest jsonObjectRequest = new JsonArrayRequest
                    (Request.Method.GET, url, null, new Response.Listener<JSONArray>() {
                        @Override
                        public void onResponse(JSONArray response) {
                            try {
                                if (mAuthTask != null && response.length() > 4) {
                                    boolean status = response.getBoolean(0);
                                    String uData = response.getString(2);
                                    String error = response.getString(3);
                                    mAuthTask.onResponse(status, error, uData);
                                }
                            } catch (Exception e) {
                                if (mAuthTask != null)
                                    mAuthTask.onResponse(false, "JSON Parse error", null);
                            }
                        }
                    }, new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            // TODO: Handle error
                            if (mAuthTask != null)
                                mAuthTask.onResponse(false, "Network error", null);
                        }
                    });

            // Access the RequestQueue through your singleton class.
            MySingleton.getInstance(thisActivity).addToRequestQueue(jsonObjectRequest);
        }

        private void doRegister() throws Exception {
            String random = MySingleton.getRandomString(16);
            String url = "http://www.shanvishield.com/safety/auth.php?" + info +
                    "&password1=" + random + "&password2=" + random + "&sha1=" + sha1 +
                    "&op=signup&type=json";
            Log.d(TAG, url);
            JsonArrayRequest jsonObjectRequest = new JsonArrayRequest
                    (Request.Method.GET, url, null, new Response.Listener<JSONArray>() {
                        @Override
                        public void onResponse(JSONArray response) {
                            try {
                                if (mAuthTask != null && response.length() > 4) {
                                    boolean status = response.getBoolean(1);
                                    String uData = response.getString(2);
                                    String error = response.getString(3);
                                    mAuthTask.onResponse(status, error, uData);
                                }
                            } catch (Exception e) {
                                if (mAuthTask != null)
                                    mAuthTask.onResponse(false, "JSON Parse error", null);
                            }
                        }
                    }, new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            // TODO: Handle error
                            if (mAuthTask != null)
                                mAuthTask.onResponse(false, "Network error", null);
                        }
                    });

            // Access the RequestQueue through your singleton class.
            MySingleton.getInstance(thisActivity).addToRequestQueue(jsonObjectRequest);
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            try {
                if (type.equals("login")) {
                    doLogin();
                }
                if (type.equals("reset")) {
                    doReset();
                }
                if (type.equals("register")) {
                    doRegister();
                }
            } catch (Exception e) {
                return false;
            }
            return true;
        }

        @Override
        protected void onPostExecute(final Boolean success) {
        }

        @Override
        protected void onCancelled() {
            mAuthTask = null;
            showProgress(false);
        }

        private boolean isVerifiedUser(String ud) {
            UserData uData = UserData.parseUserData(ud);
            if (uData != null && (uData.role.equals("admin") || uData.role.equals("verified"))) {
                return true;
            }
            return false;
        }

        private void processLogin (final Boolean success, String error, String uData) {
            if (success) {
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(mUsernameView.getWindowToken(), 0);

                if (saveLoginCheckBox.isChecked()) {
                    loginPrefsEditor.putBoolean("saveLogin", true);
                    loginPrefsEditor.putString("username", mUsername);
                    loginPrefsEditor.putString("password", sha1);
                    loginPrefsEditor.putString("uData", uData);
                    loginPrefsEditor.commit();
                } else {
                    loginPrefsEditor.clear();
                    loginPrefsEditor.commit();
                }
                if (isVerifiedUser(uData)) {
                    Intent intent = new Intent();
                    intent.putExtra(EXTRA_RESPONSE, uData);
                    setResult(RESULT_OK, intent);
                    finish();
                }
                else {
                    mUsernameView.setError("Username is not verified yet");
                    mUsernameView.requestFocus();
                }
            } else {
                mPasswordView.setError(error);
                mPasswordView.requestFocus();
            }
        }

        private void processReset (final Boolean success, String error, String uData) {
            if (success) {
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(mUsernameView.getWindowToken(), 0);
                Toast.makeText(thisActivity, "Reset Successful", Toast.LENGTH_SHORT).show();
            } else {
                EditText mEmail = (EditText) findViewById(R.id.email);
                mEmail.setError(error);
                mEmail.requestFocus();
            }
        }

        private void processRegister (final Boolean success, String error, String uData) {
            if (success) {
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(mUsernameView.getWindowToken(), 0);

                if (saveLoginCheckBox.isChecked()) {
                    loginPrefsEditor.putBoolean("saveLogin", true);
                    loginPrefsEditor.putString("username", mUsername);
                    loginPrefsEditor.putString("password", sha1);
                    loginPrefsEditor.putString("uData", uData);
                    loginPrefsEditor.commit();
                } else {
                    loginPrefsEditor.clear();
                    loginPrefsEditor.commit();
                }
                if (isVerifiedUser(uData)) {
                    Intent intent = new Intent();
                    intent.putExtra(EXTRA_RESPONSE, uData);
                    setResult(RESULT_OK, intent);
                    finish();
                }
                else {
                    mUsernameView.setError("Successfully Registered! Wait for Username to be verified");
                    mUsernameView.requestFocus();
                }
            } else {
                EditText mEmail = (EditText) findViewById(R.id.email);
                mEmail.setError(error);
                mEmail.requestFocus();
            }
        }

        private void onResponse(final Boolean success, String error, String uData) {
            mAuthTask = null;
            showProgress(false);
            if (type.equals("login")) {
                processLogin(success, error, uData);
            }
            if (type.equals("reset")) {
                processReset(success, error, uData);
            }
            if (type.equals("register")) {
                processRegister(success, error, uData);
            }
        }
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
            case R.id.menu_sign_in:
                setupSignIn();
                break;
            case R.id.menu_register:
                setupRegister();
                break;
            case R.id.menu_reset:
                setupReset();
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

