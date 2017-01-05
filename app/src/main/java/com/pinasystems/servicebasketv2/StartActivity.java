package com.pinasystems.servicebasketv2;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.provider.ContactsContract;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.auth.api.Auth;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class StartActivity extends AppCompatActivity {

    private Handler mHandler = new Handler();
    SharedPreferences sharedPreferences;
    String restoredText,userId;
    boolean isloggedin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);
        mHandler.postDelayed(new Runnable() {
            public void run() {
                Initialize();
            }
        }, 5000);
    }

    private void Initialize(){
        sharedPreferences = getSharedPreferences(AppConfig.APP_PREFS_NAME, MODE_PRIVATE);
        restoredText = sharedPreferences.getString(AppConfig.PREF_DATA, null);

        if (restoredText != null) {
            userId =sharedPreferences.getString(AppConfig.USER_ID,"No userId");
            ((DataBank)getApplication()).setUserId(userId);
            isloggedin = sharedPreferences.getBoolean(AppConfig.PREF_LOGIN_STATUS,false);
            String loginwith = sharedPreferences.getString(AppConfig.PREF_TEMP,"Notag");
            ((DataBank)getApplication()).setTag(loginwith);
            loggedIn(isloggedin);
        }else{
            gotoLogin();
        }
    }

    private void loggedIn(boolean isuserloggedin){
        if(isuserloggedin){
            getGeneralData(userId);
        }else{
            gotoLogin();
        }
    }

    private void getGeneralData(final String user_id) {

        String URL = AppConfig.ROOT_URL + "getgeneraldata.php";

        StringRequest stringRequest = new StringRequest(Request.Method.POST, URL, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                JSONArray array = extractJSON(response);
                parseData(array);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(getApplicationContext(),error.getLocalizedMessage(),Toast.LENGTH_LONG).show();
            }
        }){
            @Override
            protected HashMap<String , String> getParams() throws AuthFailureError {
                HashMap<String , String> params = new HashMap<>();
                params.put("id",user_id);
                return params;
            }
        };

        RequestQueue requestQueue = Volley.newRequestQueue(this);
        requestQueue.add(stringRequest);
    }

    private JSONArray extractJSON(String jsondata){
        JSONArray jsonArray = null;
        try {
            JSONObject jsonObject = new JSONObject(jsondata);
            jsonArray = jsonObject.getJSONArray(AppConfig.JSON_ARRAY);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return jsonArray;
    }

    private void parseData(JSONArray array) {
        JSONObject json = null;
        try {
            json = array.getJSONObject(0);
            String email = json.getString("email");
            String telno = json.getString("telno");
            String name = json.getString("name");
            ((DataBank)getApplication()).setName(name);
            ((DataBank)getApplication()).setTelno(telno);
            ((DataBank)getApplication()).setEmail(email);
            directTo();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void directTo(){
        ((DataBank)getApplication()).setAccount(restoredText);
        if (restoredText.equalsIgnoreCase("requester")){
            Intent intent = new Intent(getApplicationContext(),RequesterMainActivity.class);
            startActivity(intent);
            finish();
        }else if (restoredText.equalsIgnoreCase("provider")){
                checkProviderStatus(userId);
        }else if(restoredText.equalsIgnoreCase("new")){
            Intent intent = new Intent(getApplicationContext(),AccountActivity.class);
            startActivity(intent);
            finish();
        }
    }

    private void gotoLogin(){
        Intent intent = new Intent(getApplicationContext(),LoginActivity.class);
        startActivity(intent);
        finish();
    }

    private void checkProviderStatus(final String userId) {
        final String action = "read";
        StringRequest stringRequest = new StringRequest(Request.Method.POST, AppConfig.PROFILE_STATUS,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        if (response.equalsIgnoreCase("1")) {
                            getProviderDetails(userId);
                        } else {
                            Intent intent = new Intent(getApplicationContext(), ProviderMainActivity.class);
                            startActivity(intent);
                            finish();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(getApplicationContext(), "Error connecting to internet.", Toast.LENGTH_LONG).show();
                    }
                }) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<>();
                //Adding parameters to request
                params.put(AppConfig.USER_ID, userId);
                params.put(AppConfig.ACTION,action);
                //returning parameter
                return params;
            }
        };

        //Adding the string request to the queue
        RequestQueue requestQueue = Volley.newRequestQueue(this);
        requestQueue.add(stringRequest);
    }

    private void getProviderDetails(String userId) {
        final String uUserId = userId;
        StringRequest stringRequest = new StringRequest(Request.Method.POST, AppConfig.GET_PROVIDER_DETAILS,

                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        storeDataInMemory(response);
                        Intent intent = new Intent(getApplicationContext(), EnterProviderActivity.class);
                        startActivity(intent);
                        finish();
                    }
                },

                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(getApplicationContext(), "Error connecting to internet.", Toast.LENGTH_LONG).show();
                    }
                }) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<>();
                //Adding parameters to request
                params.put(AppConfig.USER_ID, uUserId);
                //returning parameter
                return params;
            }
        };

        //Adding the string request to the queue
        RequestQueue requestQueue = Volley.newRequestQueue(this);
        requestQueue.add(stringRequest);
    }

    private void storeDataInMemory(String subcategory){
        SharedPreferences.Editor editor = getSharedPreferences(AppConfig.APP_PREFS_NAME, MODE_PRIVATE).edit();
        editor.putString(AppConfig.PREF_SUBCATEGORY,subcategory);
        editor.apply();
    }
}