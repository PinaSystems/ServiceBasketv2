package com.pinasystems.servicebasketv2;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RatingBar;
import android.widget.Spinner;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;

public class RequestClosedActivity extends AppCompatActivity {

    RatingBar ratingBar;
    String stars;
    String id;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_request_closed);
        Intent intent = getIntent();
        id = intent.getStringExtra("id");
        Log.e("ID",id);
        addListenerOnRatingBar();

        getProviders(id);
        Button buttonfeedback = (Button) findViewById(R.id.button_submit_feedback);
        buttonfeedback.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getData();
            }
        });

    }
    private void getProviders(String id) {

        String URL = AppConfig.ROOT_URL + "getprovidernames.php?id="+id ;

        final ProgressDialog loading = ProgressDialog.show(this, "Loading Data", "Please wait...", false, false);
        JsonArrayRequest jsonArrayRequest = new JsonArrayRequest(URL,
                new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray response) {
                        //Dismissing progress dialog
                        loading.dismiss();
                        parseSubCategory(response);
                        Log.e("ARRAY", response.toString());
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.e("TAG",error.getMessage());
                        Toast.makeText(getApplicationContext(),error.getLocalizedMessage(),Toast.LENGTH_LONG).show();
                        loading.dismiss();
                    }
                });
        RequestQueue requestQueue = Volley.newRequestQueue(this);
        //Adding request to the queue
        requestQueue.add(jsonArrayRequest);
    }

    JSONArray providerids;

    private void parseSubCategory(JSONArray array) {
        JSONObject json;
        try {
            json = array.getJSONObject(0);
            ArrayList<String> providers = new ArrayList<>();
            JSONArray jsonArray = json.getJSONArray(AppConfig.NAME);
            json = array.getJSONObject(1);
            providerids = json.getJSONArray(AppConfig.USER_ID);
            for (int j = 0; j < jsonArray.length(); j++) {
                providers.add(((String) jsonArray.get(j)));
            }
            Log.e("IDARRAY", id);

            addSpinner(providers);
        } catch (JSONException e) {
            Log.e("PARSING", e.getMessage());
        }
    }

    String provider;
    String prov_id;
    int pos;

    public void addSpinner(ArrayList<String> providers) {
        Spinner spinner = (Spinner) findViewById(R.id.spinner);
        ArrayAdapter<String> dataAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, providers);
        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(dataAdapter);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                provider = parent.getItemAtPosition(position).toString();
                pos = position;
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                provider = "No provider";
            }
        });


    }

    public void addListenerOnRatingBar() {

        ratingBar = (RatingBar) findViewById(R.id.ratingbar);

        //if rating value is changed,
        //display the current rating value in the result (textview) automatically
        ratingBar.setOnRatingBarChangeListener(new RatingBar.OnRatingBarChangeListener() {
            public void onRatingChanged(RatingBar ratingBar, float rating,
                                        boolean fromUser) {
                stars = String.valueOf(rating);
                Toast.makeText(getApplicationContext(),stars,Toast.LENGTH_LONG).show();
            }
        });
    }

    public void getData(){

        try {
            prov_id = providerids.getString(pos);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        EditText editTextfeedback = (EditText) findViewById(R.id.feedback);


        String feedback = editTextfeedback.getText().toString().trim();
        if(TextUtils.isEmpty(feedback)){
            feedback = "No feedback given";
        }
        Log.e("FEEDBACK",feedback);
        if(TextUtils.isEmpty(stars)){
            stars = "3.0";
            Log.e("RATING",stars);
        }else{
            Log.e("RATING",stars);
        }
        Log.e("PROVID",prov_id);
        Log.e("PROVIDER",provider);
        saveData(feedback);

    }

    String URL  = AppConfig.ROOT_URL + "provfeedback.php";

    private void saveData(final String feedback){

        final ProgressDialog progressDialog = ProgressDialog.show(this,"Saving Data","Please Wait",false,false);
        StringRequest stringRequest = new StringRequest(Request.Method.POST, URL, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                if (response.equalsIgnoreCase("success")) {
                    Toast.makeText(getApplicationContext(), "Thank you for the feedback", Toast.LENGTH_LONG).show();
                    nextActivity();
                    progressDialog.dismiss();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                progressDialog.dismiss();
                if(TextUtils.isEmpty(error.getMessage())){
                    Log.e("Error","empty");
                }else{
                    Log.e("Error",error.getMessage());
                }
            }
        }){
            @Override
            protected HashMap<String , String> getParams() throws AuthFailureError {
                HashMap<String , String > params = new HashMap<>();
                params.put("provider",prov_id);
                params.put("rating",stars);
                params.put("feedback", feedback);
                params.put("id",id);
                return params;
            }
        };

        RequestQueue requestQueue = Volley.newRequestQueue(this);
        requestQueue.add(stringRequest);
    }

    private void nextActivity(){
        Intent intent = new Intent(getApplicationContext(),PreviousRequestsActivity.class);
        startActivity(intent);
        finish();
    }
}
