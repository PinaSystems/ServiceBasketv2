package com.pinasystems.servicebasketv2;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
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
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class EnterProviderActivity extends AppCompatActivity {
    private TextView textViewsubcategory;
    private EditText editTextpostalAddress, editTextPincode,editTextpostalAddress2;
    private String category, subcategory, postal, state, pincode, city,userId,subcategories;
    String[] service;
    boolean[] checkedService;
    ArrayList<String> categories;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_enter_provider);
        Button buttonsubmit = (Button) findViewById(R.id.provider_details_submit);
        textViewsubcategory = (TextView) findViewById(R.id.textviewsubcategory);
        editTextpostalAddress = (EditText) findViewById(R.id.postaladdress);
        editTextPincode = (EditText) findViewById(R.id.pincode);
        editTextpostalAddress2 = (EditText) findViewById(R.id.postaladdress2);
        userId = ((DataBank)getApplication()).getUserId();
        String getcategory = "category";
        getCatandSubcat(getcategory);
        buttonsubmit.setOnClickListener(new View.OnClickListener() {
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
        getStatesAndCities(false);

    }

    public void addSpinner() {
        Spinner spinner = (Spinner) findViewById(R.id.spinner);
        ArrayAdapter<String> dataAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, categories);
        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(dataAdapter);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                category = parent.getItemAtPosition(position).toString();
                getCatandSubcat(category);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }

    public void setSubcategory() {
        Button buttonsubcategory = (Button) findViewById(R.id.buttonsubcategory);
        textViewsubcategory = (TextView) findViewById(R.id.textviewsubcategory);
        buttonsubcategory.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(EnterProviderActivity.this);
                service = subcategories.split(",");
                checkedService = new boolean[]{
                        false,
                        false,
                        false,
                        false,
                        false,
                        false,
                        false,
                        false,
                        false,
                        false,
                        false,
                        false,
                        false,
                        false,
                        false
                };

                final List<String> serviceList = Arrays.asList(service);
                builder.setMultiChoiceItems(service, checkedService, new DialogInterface.OnMultiChoiceClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which, boolean isChecked) {

                        // Update the current focused item's checked status
                        checkedService[which] = isChecked;

                        // Get the current focused item
                        String currentItem = serviceList.get(which);
                        Log.e("SUBCAT", currentItem + " " + isChecked);
                    }
                });

                // Specify the dialog is not cancelable
                builder.setCancelable(true);

                // Set a title for alert dialog
                builder.setTitle("Service Subcategory");

                // Set the positive/yes button click listener
                builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // Do something when click positive button
                        textViewsubcategory.setText("");
                        textViewsubcategory.setError(null);
                        for (int i = 0; i < checkedService.length; i++) {
                            boolean checked = checkedService[i];
                            if (checked) {
                                String subcat = textViewsubcategory.getText().toString();
                                if (subcat.matches("")) {
                                    textViewsubcategory.setText(serviceList.get(i));
                                } else {
                                    textViewsubcategory.setText(subcat + "," + serviceList.get(i));
                                }
                            }
                        }
                    }
                });
                AlertDialog dialog = builder.create();
                // Display the alert dialog on interface
                dialog.show();
            }
        });

    }

    private void getData() {
        View focusView = null;
        boolean terminate = false;
        subcategory = textViewsubcategory.getText().toString().trim();
        if (subcategory.matches("")) {
            textViewsubcategory.setError("Please select subcategories");
            textViewsubcategory.setHint("Please select subcategories");
            focusView = textViewsubcategory;
            terminate = true;
        }
        postal = editTextpostalAddress.getText().toString() + ", " + editTextpostalAddress2.getText().toString();
        pincode = editTextPincode.getText().toString().trim();
        if (TextUtils.isEmpty(postal)) {
            editTextpostalAddress.setError(getString(R.string.error_field_required));
            focusView = editTextpostalAddress;
            terminate = true;
        }
        if (TextUtils.isEmpty(pincode)) {
            editTextPincode.setError(getString(R.string.error_field_required));
            focusView = editTextPincode;
            terminate = true;
        }
        if(!isPincodeValid(pincode)){
            editTextPincode.setError("Invalid Pincode");
            focusView = editTextPincode;
            terminate = true;
        }
        if (terminate) {
            focusView.requestFocus();
        } else {
            sendData();
            storeDataInMemory(subcategory);
            Log.e(AppConfig.SUBCATEGORY,subcategory);
        }
    }

    private boolean isPincodeValid(String pincode){
        return (TextUtils.isDigitsOnly(pincode) && pincode.length() == 6);
    }

    ProgressDialog loading;

    public void sendData() {
        class AddSend extends AsyncTask<Void, Void, String> {
            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                loading = ProgressDialog.show(EnterProviderActivity.this,"Updating","Please wait...",true,false);
                loading.show();
            }

            @Override
            protected String doInBackground(Void... v) {
                HashMap<String, String> params = new HashMap<>();
                params.put(AppConfig.CATEGORY,category);
                params.put(AppConfig.SUBCATEGORY,subcategory);
                params.put(AppConfig.ADDRESS,postal);
                params.put(AppConfig.STATE,state);
                params.put(AppConfig.CITY,city);
                params.put(AppConfig.PINCODE,pincode);
                params.put(AppConfig.USER_ID,userId);
                Requesthandler rh = new Requesthandler();
                return rh.sendPostRequest(AppConfig.SET_PROVIDER_DATA, params);
            }

            @Override
            protected void onPostExecute(String s) {
                super.onPostExecute(s);
                Toast.makeText(EnterProviderActivity.this,s,Toast.LENGTH_LONG).show();
                Log.e(AppConfig.PROFILESTATUS,s);
                changeprofilestatus();
            }
        }
        AddSend ar = new AddSend();
        ar.execute();
    }

    public void changeprofilestatus()
    {
        final String profile_status = "2";
        final String action = "write";

        class AddSend extends AsyncTask<Void, Void, String> {

            @Override
            protected void onPreExecute() {
                super.onPreExecute();
            }

            @Override
            protected String doInBackground(Void... v) {
                HashMap<String, String> params = new HashMap<>();
                params.put(AppConfig.USER_ID,userId);
                params.put(AppConfig.PROFILESTATUS,profile_status);
                params.put(AppConfig.ACTION,action);
                Requesthandler rh = new Requesthandler();
                return rh.sendPostRequest(AppConfig.PROFILE_STATUS, params);
            }

            @Override
            protected void onPostExecute(String s) {
                super.onPostExecute(s);
                loading.dismiss();
                nextActivity();
            }
        }
        AddSend ar = new AddSend();
        ar.execute();
    }

    private void nextActivity(){
        Intent intent = new Intent(getApplicationContext(),ProviderMainActivity.class);
        startActivity(intent);
        finish();
    }

    private void getCatandSubcat(final String action){
        final ProgressDialog loading = ProgressDialog.show(this,"Loading Data", "Please wait...",false,false);

        JsonArrayRequest jsonArrayRequest = new JsonArrayRequest(Request.Method.GET, AppConfig.CATEGORIES_AND_SUBCATEGORIES + action,
                new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray response) {
                        //Dismissing progress dialog
                        loading.dismiss();
                        if(action.equalsIgnoreCase("category")){
                            parseCategory(response);
                        }else{
                            parsesubCategory(response);
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        loading.dismiss();
                        Log.w("ERROR",error.getMessage());
                    }
                });

        //Creating request queue
        RequestQueue requestQueue = Volley.newRequestQueue(this);

        //Adding request to the queue
        requestQueue.add(jsonArrayRequest);
    }

    //This method will parse json data
    private void parseCategory(JSONArray array) {
        JSONObject json;
        try {
            json = array.getJSONObject(0);
            categories = new ArrayList<>();
            JSONArray jsonArray = json.getJSONArray(AppConfig.CATEGORY);
            for (int j = 0; j < jsonArray.length(); j++) {
                categories.add(((String) jsonArray.get(j)));
            }

            addSpinner();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void parsesubCategory(JSONArray array) {
        JSONObject json;
        try {
            json = array.getJSONObject(0);
            subcategories = json.getString(AppConfig.SUBCATEGORY);
            setSubcategory();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void storeDataInMemory(String subcategory){
        SharedPreferences.Editor editor = getSharedPreferences(AppConfig.APP_PREFS_NAME, MODE_PRIVATE).edit();
        editor.putString(AppConfig.PREF_SUBCATEGORY,subcategory);
        editor.apply();
    }

    //Get states from WhizAPI

    private void getStatesAndCities(final boolean getcities){
        String APIKey = "5mzhbascarxg6j8s6vz5llt3";
        String API;
        if(getcities){
            API = "https://www.whizapi.com/api/v2/util/ui/in/indian-city-by-state?stateid="+state_id+"&project-app-key="+APIKey;
        }else{
            API = "https://www.whizapi.com/api/v2/util/ui/in/indian-states-list?project-app-key="+APIKey;
        }
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(API, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                parseStatesandCities(response,getcities);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                error.printStackTrace();
            }
        });

        RequestQueue requestQueue = Volley.newRequestQueue(this);
        requestQueue.add(jsonObjectRequest);
    }

    ArrayList<String> states;
    ArrayList<Integer> states_id;
    ArrayList<String> cities;

    //This method will parse json data
    private void parseStatesandCities(JSONObject object , boolean parsecities) {
        JSONObject json;
        if(parsecities){
            try {
                json = object;
                cities = new ArrayList<>();
                JSONArray jsonArray = json.getJSONArray("Data");
                for (int j = 0; j < jsonArray.length(); j++) {
                    json = jsonArray.getJSONObject(j);
                    cities.add(json.getString("city"));
                }
                addCitiesSpinner();
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }else{
            try {
                json = object;
                states = new ArrayList<>();
                states_id = new ArrayList<>();
                JSONArray jsonArray = json.getJSONArray("Data");
                for (int j = 0; j < jsonArray.length(); j++) {
                    json = jsonArray.getJSONObject(j);
                    states.add(json.getString("Name"));
                    states_id.add(json.getInt("ID"));
                }
                addStatesSpinner();
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    int state_id;

    public void addStatesSpinner() {
        Spinner spinner = (Spinner) findViewById(R.id.statesspinner);
        ArrayAdapter<String> dataAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, states);
        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(dataAdapter);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                state = parent.getItemAtPosition(position).toString();
                state_id = states_id.get(position);
                getStatesAndCities(true);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }


    public void addCitiesSpinner() {
        Spinner spinner = (Spinner) findViewById(R.id.citiesspinner);
        ArrayAdapter<String> dataAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, cities);
        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(dataAdapter);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                city = parent.getItemAtPosition(position).toString();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }
}