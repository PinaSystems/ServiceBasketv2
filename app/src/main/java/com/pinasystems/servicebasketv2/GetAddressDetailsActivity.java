package com.pinasystems.servicebasketv2;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
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

import java.util.HashMap;
import java.util.Map;

public class GetAddressDetailsActivity extends AppCompatActivity {

    String[] addresslabels;

    String address_label;
    String userId;
    EditText editTextaddress,editTextcity,editTextpincode,editTextlabel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_get_address_details);
        userId = ((DataBank)getApplication()).getUserId();
        addSpinner();

        editTextaddress = (EditText)
                findViewById(R.id.address);
        String address = ((DataBank)getApplication()).getAddress();
        editTextaddress.setText(address);

        Button btnsaveaddress = (Button)
                findViewById(R.id.saveaddress);
        btnsaveaddress.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getData();
            }
        });

        // Load an ad into the AdMob banner view.
        AdView adView = (AdView) findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder()
                .setRequestAgent("android_studio:ad_template").build();
        adView.loadAd(adRequest);
    }
    public void addSpinner() {
        Spinner spinner = (Spinner) findViewById(R.id.spinner);
        addresslabels = getApplicationContext().getResources().getStringArray(R.array.address_labels);
        ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item,addresslabels);
        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(dataAdapter);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String label = parent.getItemAtPosition(position).toString();
                getLabel(label);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }

    boolean custom_label = false;

    public void getLabel(String label){

        editTextlabel = (EditText) findViewById(R.id.other);

        if(label.equalsIgnoreCase("other"))
        {
            editTextlabel.setVisibility(View.VISIBLE);
            custom_label = true;
        }else
        {
            editTextlabel.setVisibility(View.INVISIBLE);
            address_label = label;
            custom_label = false;
        }
    }

    public void getData(){
        editTextcity = (EditText) findViewById(R.id.city);
        editTextpincode = (EditText) findViewById(R.id.pincode);

        String city = editTextcity.getText().toString().trim();
        String pincode = editTextpincode.getText().toString().trim();
        String address = editTextaddress.getText().toString().trim();

        if(custom_label){
            address_label = editTextlabel.getText().toString().trim();
        }

        boolean terminate = false;

        if(TextUtils.isEmpty(city)){
            terminate = true;
            editTextcity.setError("Field Required");
        }
        if(TextUtils.isEmpty(pincode)){
            terminate = true;
            editTextpincode.setError("Field Required");
        }
        if(TextUtils.isEmpty(address)){
            terminate = true;
            editTextaddress.setError("Field Required");
        }
        if(!isPincodeValid(pincode)){
            terminate = true;
            editTextpincode.setError("Please enter a valid 6 digit pincode");
        }
        if(TextUtils.isEmpty(address_label)){
            terminate = true;
            editTextlabel.setError("Field Required");
        }
        if(!terminate) {

            String latitude = ((DataBank)getApplication()).getLat();
            String longitude = ((DataBank)getApplication()).getLong();
            Log.e("LATITUDE",latitude);
            Log.e("LONGITUDE",longitude);
            sendData(address, city, pincode , latitude , longitude);

        }
    }

    private boolean isPincodeValid(String pincode){
        return (TextUtils.isDigitsOnly(pincode) && pincode.length() == 6);
    }

    private void sendData(final String address , final String city , final String pincode , final String latitude , final String longitude) {
        final ProgressDialog loading = ProgressDialog.show(this,"Loading Data", "Please wait...",false,false);

        StringRequest stringRequest = new StringRequest(Request.Method.POST, AppConfig.SAVE_ADDRESS, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                loading.dismiss();
                Toast.makeText(getApplicationContext(),response,Toast.LENGTH_LONG)
                        .show();
                storeDataInMemory(address_label);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                loading.dismiss();
                Toast.makeText(getApplicationContext(),"Error connecting to internet...",Toast.LENGTH_LONG)
                        .show();
                Log.e("ERROR",error.getMessage());

            }
        }) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError
            {
                Map<String, String> params = new HashMap<>();
                params.put(AppConfig.POSTAL_ADDRESS, address);
                params.put(AppConfig.ADDRESS_LABEL,address_label);
                params.put(AppConfig.CITY,city);
                params.put(AppConfig.PINCODE,pincode);
                params.put(AppConfig.LATITUDE,latitude);
                params.put(AppConfig.LONGITUDE,longitude);
                params.put(AppConfig.USER_ID,userId);

                return params;
            }

        };

        RequestQueue requestQueue = Volley.newRequestQueue(this);
        requestQueue.add(stringRequest);
    }

    private void storeDataInMemory(String pref_address){
        SharedPreferences.Editor editor = getSharedPreferences(AppConfig.APP_PREFS_NAME, MODE_PRIVATE).edit();
        editor.putString(AppConfig.PREF_ADDRESS_LABEL,pref_address);
        editor.apply();
        Intent intent = new Intent(getApplicationContext(),NewRequestActivity.class);
        startActivity(intent);
        finish();
    }

}