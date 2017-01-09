package com.pinasystems.servicebasketv2;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.app.Dialog;
import android.app.LoaderManager.LoaderCallbacks;
import android.app.ProgressDialog;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static android.Manifest.permission.READ_CONTACTS;

/**
 * A login screen that offers login via email/password.
 */
public class LoginActivity extends AppCompatActivity implements LoaderCallbacks<Cursor> {

    /**
     * Id to identity READ_CONTACTS permission request.
     */
    private static final int REQUEST_READ_CONTACTS = 0;

    // UI references.
    private AutoCompleteTextView mUsernameView;
    private EditText mPasswordView;
    private View mProgressView;
    private View mLoginFormView;
    private RadioButton radioButtonlogin,radioButtonregister;
    private String URL;
    boolean islogin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        mUsernameView = (AutoCompleteTextView) findViewById(R.id.username);
        populateAutoComplete();

        mPasswordView = (EditText) findViewById(R.id.password);

        final Button mEmailSignInButton = (Button) findViewById(R.id.email_sign_in_button);
        mEmailSignInButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                attemptLogin();
            }
        });

        mLoginFormView = findViewById(R.id.login_form);
        mProgressView = findViewById(R.id.login_progress);
        radioButtonlogin = (RadioButton) findViewById(R.id.radioButton1);
        radioButtonregister = (RadioButton) findViewById(R.id.radioButton2);
        radioButtonlogin.setChecked(true);
        URL = AppConfig.LOGIN;
        radioButtonregister.setChecked(false);
        radioButtonlogin.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                radioButtonregister.setChecked(false);
                URL = AppConfig.LOGIN;
                mEmailSignInButton.setText(getApplicationContext().getResources().getString(R.string.action_sign_in_short));
                islogin = true;
            }
        });

        radioButtonregister.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                radioButtonlogin.setChecked(false);
                URL = AppConfig.REGISTER;
                mEmailSignInButton.setText(getApplicationContext().getResources().getString(R.string.action_register));
                islogin = false;
            }
        });

        //Forgort Password Button

        Button btnforgotpass = (Button) findViewById(R.id.forgotpassbtn);
        btnforgotpass.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                String username = mUsernameView.getText().toString().trim();
                if(username.isEmpty()){
                    mUsernameView.setError("Please enter the username to reset the password");
                }else{
                    passReset(username);
                }

            }
        });

        // Load an ad into the AdMob banner view.
        AdView adView = (AdView) findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder()
                .setRequestAgent("android_studio:ad_template").build();
        adView.loadAd(adRequest);
    }

    //Send email parameter for forgot password

    private void passReset(final String username){
        final ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Loading");
        progressDialog.setMessage("Please wait.....");
        progressDialog.setCancelable(true);
        progressDialog.setIndeterminate(false);
        progressDialog.show();
        boolean emailgiven = false;

        if (isTelnoValid(username)) {
            emailgiven = false;
        }else if (isEmailValid(username)){
            emailgiven = true;
        }

        if(emailgiven){
            final String action = "email";
            StringRequest stringRequest = new StringRequest(Request.Method.POST, AppConfig.RESET_PASSWORD,
                    new Response.Listener<String>() {
                        @Override
                        public void onResponse(String response) {
                            progressDialog.dismiss();
                            messageDialog(response);
                        }
                    },
                    new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            progressDialog.dismiss();
                            messageDialog(error.getMessage());
                        }
                    }){
                @Override
                protected HashMap<String , String> getParams() throws AuthFailureError
                {
                    HashMap<String , String> params = new HashMap<>();
                    params.put(AppConfig.USERNAME , username);
                    params.put(AppConfig.ACTION , action);
                    return params;
                }
            };
            RequestQueue requestQueue = Volley.newRequestQueue(this);
            requestQueue.add(stringRequest);
        }else{
            final String action = "telno";
            StringRequest stringRequest = new StringRequest(Request.Method.POST, AppConfig.RESET_PASSWORD,
                    new Response.Listener<String>() {
                        @Override
                        public void onResponse(String response) {
                            progressDialog.dismiss();
                            messageDialog(response);
                        }
                    },
                    new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            progressDialog.dismiss();
                            messageDialog(error.getMessage());
                        }
                    }){
                @Override
                protected HashMap<String , String> getParams() throws AuthFailureError
                {
                    HashMap<String , String> params = new HashMap<>();
                    params.put(AppConfig.USERNAME , username);
                    params.put(AppConfig.ACTION , action);
                    return params;
                }
            };
            RequestQueue requestQueue = Volley.newRequestQueue(this);
            requestQueue.add(stringRequest);
        }
    }

    private void messageDialog(String message){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Password Reset");
        builder.setMessage(message);
        builder.setNegativeButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

            }
        });

        Dialog messagedialog = builder.create();
        messagedialog.show();
    }

    private void populateAutoComplete() {
        if (!mayRequestContacts()) {
            return;
        }

        getLoaderManager().initLoader(0, null, this);
    }

    private boolean mayRequestContacts() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            return true;
        }
        if (checkSelfPermission(READ_CONTACTS) == PackageManager.PERMISSION_GRANTED) {
            return true;
        }
        if (shouldShowRequestPermissionRationale(READ_CONTACTS)) {
            Snackbar.make(mUsernameView, R.string.permission_rationale, Snackbar.LENGTH_INDEFINITE)
                    .setAction(android.R.string.ok, new View.OnClickListener() {
                        @Override
                        @TargetApi(Build.VERSION_CODES.M)
                        public void onClick(View v) {
                            requestPermissions(new String[]{READ_CONTACTS}, REQUEST_READ_CONTACTS);
                        }
                    });
        } else {
            requestPermissions(new String[]{READ_CONTACTS}, REQUEST_READ_CONTACTS);
        }
        return false;
    }

    /**
     * Callback received when a permissions request has been completed.
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        if (requestCode == REQUEST_READ_CONTACTS) {
            if (grantResults.length == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                populateAutoComplete();
            }
        }
    }


    /**
     * Attempts to sign in or register the account specified by the login form.
     * If there are form errors (invalid email, missing fields, etc.), the
     * errors are presented and no actual login attempt is made.
     */

    boolean loginwithemail = true;
    String loginwith = "email";
    boolean cancel = false;

    private void attemptLogin() {
        // Reset errors.
        mUsernameView.setError(null);
        mPasswordView.setError(null);

        // Store values at the time of the login attempt.
        String username = mUsernameView.getText().toString();
        String password = mPasswordView.getText().toString();

        View focusView = null;

        // Check for a valid password, if the user entered one.
        if (TextUtils.isEmpty(password) && !isPasswordValid(password)) {
            mPasswordView.setError(getString(R.string.error_invalid_password));
            focusView = mPasswordView;
            cancel = true;
        }

        // Check for a valid email address.
        if (TextUtils.isEmpty(username)) {
            mUsernameView.setError(getString(R.string.error_field_required));
            focusView = mUsernameView;
            cancel = true;
        } else if (!isEmailValid(username)) {
            if (!isTelnoValid(username)){
                mUsernameView.setError("Invalid Mobile Number");
                focusView = mUsernameView;
                cancel = true;
            }else{
                ((DataBank)getApplication()).setTelno(username);
            }
            loginwithemail = false;
        }else{
            ((DataBank)getApplication()).setEmail(username);
        }

        if (cancel) {
            // There was an error; don't attempt login and focus the first
            // form field with an error.
            focusView.requestFocus();
        } else {
            // Show a progress spinner, and kick off a background task to
            // perform the user login attempt.
            showProgress(true);
            if(!loginwithemail){
                loginwith = "telno";
            }
            LoginTask(username,password,URL);
        }
    }

    private boolean isEmailValid(String email) {
        return email.contains("@");
    }

    private boolean isPasswordValid(String password) {
        return password.length() > 4;
    }

    private boolean isTelnoValid(String email) {
        return (TextUtils.isDigitsOnly(email) && email.length() == 10);
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

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        return new CursorLoader(this,
                // Retrieve data rows for the device user's 'profile' contact.
                Uri.withAppendedPath(ContactsContract.Profile.CONTENT_URI,
                        ContactsContract.Contacts.Data.CONTENT_DIRECTORY), ProfileQuery.PROJECTION,

                // Select only email addresses.
                ContactsContract.Contacts.Data.MIMETYPE +
                        " = ?", new String[]{ContactsContract.CommonDataKinds.Email
                .CONTENT_ITEM_TYPE},

                // Show primary email addresses first. Note that there won't be
                // a primary email address if the user hasn't specified one.
                ContactsContract.Contacts.Data.IS_PRIMARY + " DESC");
    }

    @Override
    public void onLoadFinished(Loader<Cursor> cursorLoader, Cursor cursor) {
        List<String> emails = new ArrayList<>();
        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            emails.add(cursor.getString(ProfileQuery.ADDRESS));
            cursor.moveToNext();
        }

        addEmailsToAutoComplete(emails);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> cursorLoader) {

    }

    private void addEmailsToAutoComplete(List<String> emailAddressCollection) {
        //Create adapter to tell the AutoCompleteTextView what to show in its dropdown list.
        ArrayAdapter<String> adapter =
                new ArrayAdapter<>(LoginActivity.this,
                        android.R.layout.simple_dropdown_item_1line, emailAddressCollection);

        mUsernameView.setAdapter(adapter);
    }


    private interface ProfileQuery {
        String[] PROJECTION = {
                ContactsContract.CommonDataKinds.Email.ADDRESS,
                ContactsContract.CommonDataKinds.Email.IS_PRIMARY,
        };

        int ADDRESS = 0;
    }

    /**
     * Represents an asynchronous login/registration task used to authenticate
     * the user.
     */
    private void LoginTask(final String username, String password,String URL){
        final String uUsername = username;
        final String uPassword = password;
        final String uURL = URL;
        StringRequest stringRequest = new StringRequest(Request.Method.POST,uURL,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        //If we are getting success from server
                        ((DataBank)getApplication()).setTag(loginwith);
                        extractJSON(response);
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        //You can handle error here if you want
                        showProgress(false);
                        Toast.makeText(getApplicationContext(),error.getMessage(),Toast.LENGTH_LONG).show();
                    }
                }){
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String,String> params = new HashMap<>();
                //Adding parameters to request
                params.put(AppConfig.USERNAME, uUsername);
                params.put(AppConfig.PASSWORD, uPassword);
                params.put(AppConfig.ACTION,loginwith);

                //returning parameter
                return params;
            }
        };

        //Adding the string request to the queue
        RequestQueue requestQueue = Volley.newRequestQueue(this);
        requestQueue.add(stringRequest);
    }

    private void extractJSON(String jsondata){
        try {
            JSONObject jsonObject = new JSONObject(jsondata);
            JSONArray result = jsonObject.getJSONArray(AppConfig.JSON_ARRAY);
            parseData(result);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    String subcategory;
    private void parseData(JSONArray array) {
        JSONObject json = null;
        try {
            json = array.getJSONObject(0);

            String userId = json.getString(AppConfig.USER_ID);
            Log.i("ID",userId);
            ((DataBank)getApplication()).setUserId(userId);

            String account = json.getString(AppConfig.ACCOUNT);
            ((DataBank)getApplication()).setAccount(account);
            Log.i("ACCOUNT",account);

            if(account.equalsIgnoreCase("provider")){
                subcategory = json.getString(AppConfig.SUBCATEGORY);
            }else{
                subcategory = "requester only";
            }
            if(islogin){
                if(!TextUtils.isEmpty(json.getString("email"))){
                    ((DataBank)getApplication()).setEmail(json.getString("email"));
                }
                if (!TextUtils.isEmpty(json.getString("name"))){
                    ((DataBank)getApplication()).setName(json.getString("name"));
                }
                if(!TextUtils.isEmpty(json.getString("telno"))){
                    ((DataBank)getApplication()).setTelno(json.getString("telno"));
                }
            }
            if (userId.equalsIgnoreCase("Error")) {
                MessageDialog(account);
                showProgress(false);
            } else {
                ((DataBank) getApplication()).setUserId(userId);
                String fcm_token = getFCMFromMemory();
                storeToken(fcm_token,userId);
                nextActivity(account, userId);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }


    private String getFCMFromMemory(){
        SharedPreferences sharedPreferences = getSharedPreferences(AppConfig.APP_PREFS_NAME, MODE_PRIVATE);
        return sharedPreferences.getString(AppConfig.PREF_FCM_TOKEN,"No token");
    }

    public void storeToken(final String token, final String id) {

        final String URL = AppConfig.STORE_FCM_TOKEN;
        StringRequest stringRequest = new StringRequest(Request.Method.POST, URL, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Log.i("TOKEN_RESPONSE",response);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e("FCMTOKEN",error.getMessage());
            }
        }){
            @Override
            protected HashMap<String , String > getParams() throws AuthFailureError{
                HashMap<String , String> params = new HashMap<>();
                params.put(AppConfig.USER_ID,id);
                params.put(AppConfig.FCM_TOKEN,token);
                return params;
            }
        };

        RequestQueue requestQueue = Volley.newRequestQueue(this);
        requestQueue.add(stringRequest);
    }

    private void MessageDialog(String message){
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Error Logging in");
        builder.setMessage(message);
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                Toast.makeText(getApplicationContext(),"Login again",Toast.LENGTH_LONG).show();
            }
        });

        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }


    private void nextActivity(String account , String userId){
        storeDataInMemory(account, userId);
        showProgress(false);
        if(account.equalsIgnoreCase("provider")){
            Toast.makeText(getApplicationContext(),"Data Stored",Toast.LENGTH_LONG).show();
            CheckProfileStatus(userId);
        }else{
            if(account.equalsIgnoreCase("requester")){
                Toast.makeText(getApplicationContext(),"Data Stored",Toast.LENGTH_LONG).show();
                Intent intent = new Intent(getApplicationContext(),RequesterMainActivity.class);
                startActivity(intent);
                finish();
            }else{
                if(account.equalsIgnoreCase("new")){
                    Toast.makeText(getApplicationContext(),"Data Stored",Toast.LENGTH_LONG).show();
                    Intent intent = new Intent (getApplicationContext(),AccountActivity.class);
                    startActivity(intent);
                    finish();
                }else{
                    Toast.makeText(getApplicationContext(),account,Toast.LENGTH_LONG).show();
                }
            }
        }
    }

    private void storeDataInMemory(String account,String userId){
        SharedPreferences.Editor editor = getSharedPreferences(AppConfig.APP_PREFS_NAME, MODE_PRIVATE).edit();
        editor.putString(AppConfig.PREF_DATA,account);
        editor.putString(AppConfig.USER_ID,userId);
        editor.putBoolean(AppConfig.PREF_LOGIN_STATUS,true);
        editor.putString(AppConfig.PREF_TEMP,loginwith);
        editor.putString(AppConfig.PREF_SUBCATEGORY,subcategory);
        editor.apply();
    }

    private void CheckProfileStatus(final String userId){
        final String action = "read";
        final String uUserId = userId;
        StringRequest stringRequest = new StringRequest(Request.Method.POST,AppConfig.PROFILE_STATUS,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        showProgress(false);
                        if(response.equalsIgnoreCase("1")){
                            Intent intent = new Intent(getApplicationContext(),EnterProviderActivity.class);
                            startActivity(intent);
                            finish();
                        }else {
                            Intent intent = new Intent(getApplicationContext(),ProviderMainActivity.class);
                            startActivity(intent);
                            finish();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        //You can handle error here if you want
                        showProgress(false);
                        Toast.makeText(getApplicationContext(),"Error connecting to internet.",Toast.LENGTH_LONG).show();
                    }
                }){
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String,String> params = new HashMap<>();
                //Adding parameters to request
                params.put(AppConfig.ACTION, action);
                params.put(AppConfig.USER_ID,uUserId);
                //returning parameter
                return params;
            }
        };

        //Adding the string request to the queue
        RequestQueue requestQueue = Volley.newRequestQueue(this);
        requestQueue.add(stringRequest);
    }
}