package com.pinasystems.servicebasketv2;

import android.content.SharedPreferences;
import android.util.Log;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.FirebaseInstanceIdService;

import java.util.HashMap;

public class FirebaseInstanceIDService extends FirebaseInstanceIdService {

    private static final String TAG = "MyFirebaseIIDService";

    @Override
    public void onTokenRefresh() {

        //Getting registration token
        String refreshedToken = FirebaseInstanceId.getInstance().getToken();
        storeDataInMemory(refreshedToken);
        //Displaying token on logcat
        Log.d(TAG, "Refreshed token: " + refreshedToken);

        //calling the method store token and passing token
        String id = ((DataBank)getApplication()).getUserId();

        if(id == null){
            Log.e("FCM", "new user");
        }else{
            storeToken(refreshedToken , id);
        }
    }

    private void storeDataInMemory(String fcm_token){
        SharedPreferences.Editor editor = getSharedPreferences(AppConfig.APP_PREFS_NAME, MODE_PRIVATE).edit();
        editor.putString(AppConfig.PREF_FCM_TOKEN,fcm_token);
        editor.apply();
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
                Toast.makeText(getApplicationContext(),error.getLocalizedMessage(),Toast.LENGTH_LONG).show();
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
}