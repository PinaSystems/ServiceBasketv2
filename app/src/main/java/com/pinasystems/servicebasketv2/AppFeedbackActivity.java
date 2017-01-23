package com.pinasystems.servicebasketv2;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;

import java.util.HashMap;

public class AppFeedbackActivity extends AppCompatActivity {

    boolean isreqeuster;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_app_feedback);
      
        // Load an ad into the AdMob banner view.
        AdView adView = (AdView) findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder()
                .setRequestAgent("android_studio:ad_template").build();
        adView.loadAd(adRequest);

        Intent intent = getIntent();
        isreqeuster = intent.getBooleanExtra("isrequester",true);

        Button submit = (Button) findViewById(R.id.submitfeedback);
        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getData();
            }
        });
    }

    private void getData() {
        boolean cancel = false;
        View focusView = null;

        String user_id = ((DataBank) getApplication()).getUserId();
        EditText editTextfeedback = (EditText) findViewById(R.id.feedback);
        String feedback = editTextfeedback.getText().toString().trim();

        if (TextUtils.isEmpty(feedback)) {
            cancel = true;
            focusView = editTextfeedback;
            editTextfeedback.setError("Field Required");
        }

        if (cancel) {
            focusView.requestFocus();
        } else {
            sendData(user_id, feedback);
        }
    }

    private void sendData(final String id, final String feedback) {

        String URL = AppConfig.ROOT_URL + "appfeedback.php";

        StringRequest stringRequest = new StringRequest(Request.Method.POST, URL, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                if(response.equalsIgnoreCase("success")){
                    messageDialog();
                }else {
                    Log.e("FeedbackApp",response);
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                error.printStackTrace();
            }
        }) {
            @Override
            protected HashMap<String, String> getParams() throws AuthFailureError {
                HashMap<String, String> params = new HashMap<>();
                params.put("id", id);
                params.put("feedback", feedback);
                return params;
            }
        };

        RequestQueue requestQueue = Volley.newRequestQueue(this);
        requestQueue.add(stringRequest);
    }



    private void messageDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Thank You");
        builder.setMessage("We have received your feedback. Thank you for helping us.");
        builder.setNegativeButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                nextActivity();
            }
        });

        AlertDialog alertDialog = builder.create();
        alertDialog.show();

    }

    private void nextActivity(){
        Intent intent;
        if(isreqeuster){
            intent = new Intent(getApplicationContext(),RequesterMainActivity.class);
        }else{
            intent = new Intent(getApplicationContext(),ProviderMainActivity.class);
        }
        startActivity(intent);
        finish();
    }
}
