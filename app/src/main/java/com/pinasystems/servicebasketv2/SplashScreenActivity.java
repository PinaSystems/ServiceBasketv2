package com.pinasystems.servicebasketv2;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;


public class SplashScreenActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);
        Handler mHandler = new Handler();
        mHandler.postDelayed(new Runnable() {
            public void run() {
                initialize();
            }
        }, 2000);
    }

    //First Check

    private void initialize(){
        SharedPreferences sharedPreferences = getSharedPreferences(AppConfig.APP_PREFS_NAME, MODE_PRIVATE);
        String restoredText = sharedPreferences.getString(AppConfig.PREF_DATA, null);

        if (restoredText != null) {
            String userId =sharedPreferences.getString(AppConfig.USER_ID,"No userId");
            ((DataBank)getApplication()).setUserId(userId);
            if(restoredText.equalsIgnoreCase("new")){
                newUser();
            }else {
                String loginwith = sharedPreferences.getString(AppConfig.PREF_TEMP, "Notag");
                ((DataBank) getApplication()).setTag(loginwith);
                boolean isloggedin = sharedPreferences.getBoolean(AppConfig.PREF_LOGIN_STATUS, false);
                getGeneralData(userId);
                if (isloggedin) {
                    redirectTo(restoredText,userId);
                }else{
                    gotoLogin();
                }
            }
        }else{
            gotoLogin();
        }
    }

    // If account is new
    private void newUser(){
        Intent intent = new Intent(getApplicationContext(),AccountActivity.class);
        startActivity(intent);
        finish();
    }

    // If user is logged out or First time user

    private void gotoLogin(){
        Intent intent = new Intent(getApplicationContext(),LoginActivity.class);
        startActivity(intent);
        finish();
    }

    // If logged in

    private void redirectTo(String account,String userId) {
        ((DataBank) getApplication()).setAccount(account);
        if (account.equalsIgnoreCase("requester")) {
            Intent intent = new Intent(getApplicationContext(), RequesterMainActivity.class);
            startActivity(intent);
            finish();
        } else if (account.equalsIgnoreCase("provider")) {
            checkProviderStatus(userId);
        }
    }

    // General data of user

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
                error.printStackTrace();
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
        JSONObject json;
        try {
            json = array.getJSONObject(0);
            String email = json.getString("email");
            String telno = json.getString("telno");
            String name = json.getString("name");
            ((DataBank) getApplication()).setName(name);
            ((DataBank) getApplication()).setTelno(telno);
            ((DataBank) getApplication()).setEmail(email);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    // Provider status check (if details filled or details needed)

    private void checkProviderStatus(final String userId) {
        final String action = "read";
        StringRequest stringRequest = new StringRequest(Request.Method.POST, AppConfig.PROFILE_STATUS,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        if (response.equalsIgnoreCase("1")) {
                            Intent intent = new Intent(getApplicationContext(),EnterProviderActivity.class);
                            startActivity(intent);
                            finish();
                        } else {
                            getProviderDetails(userId);
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

    // If provider logged in and details known

    private void getProviderDetails(String userId) {
        final String uUserId = userId;
        StringRequest stringRequest = new StringRequest(Request.Method.POST, AppConfig.GET_PROVIDER_DETAILS,

                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        storeDataInMemory(response);
                        Intent intent = new Intent(getApplicationContext(), ProviderMainActivity.class);
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


    // Store provider subcategory

    private void storeDataInMemory(String subcategory){
        SharedPreferences.Editor editor = getSharedPreferences(AppConfig.APP_PREFS_NAME, MODE_PRIVATE).edit();
        editor.putString(AppConfig.PREF_SUBCATEGORY,subcategory);
        editor.apply();
    }

}
