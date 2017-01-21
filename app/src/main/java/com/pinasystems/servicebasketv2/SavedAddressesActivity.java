package com.pinasystems.servicebasketv2;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class SavedAddressesActivity extends AppCompatActivity {

    private List<Addresses> addressesList;

    private RecyclerView.Adapter adapter;

    String userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_saved_addresses);
        addressesList = new ArrayList<>();
        userId = ((DataBank)getApplication()).getUserId();
        Log.e("USERID",userId);
        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.recyclerView);
        recyclerView.setHasFixedSize(true);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
        //Finally initializing our adapter
        adapter = new AddressCardAdapter(addressesList);

        //Adding adapter to recyclerview
        recyclerView.setAdapter(adapter);
        Context context = getApplicationContext();
        recyclerView.addOnItemTouchListener(
                new RecyclerItemClickListener(context, new RecyclerItemClickListener.OnItemClickListener() {
                    @Override public void onItemClick(View view, int position) {
                        storeDataInMemory(addressesList.get(position).getLabel());
                        Intent intent=new Intent(getApplicationContext(),NewRequestActivity.class);
                        startActivity(intent);
                        finish();
                    }
                })
        );
        //Initializing our superheroes list

        //Calling method to get data
        getData();

        Button btnaddaddress = (Button) findViewById(R.id.addaddress);
        btnaddaddress.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), AddressMapsActivity.class);
                startActivity(intent);
            }
        });

        // Load an ad into the AdMob banner view.
        AdView adView = (AdView) findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder()
                .setRequestAgent("android_studio:ad_template").build();
        adView.loadAd(adRequest);
    }

    private void getData(){
        //Showing a progress dialog
        final ProgressDialog loading = ProgressDialog.show(this,"Loading Data", "Please wait...",false,false);
        String URL = AppConfig.GET_ADDRESS + userId;
        Log.e("URL",URL);
        //Creating a json array request
        JsonArrayRequest jsonArrayRequest = new JsonArrayRequest(Request.Method.GET,URL,
                new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray response) {
                        //Dismissing progress dialog
                        loading.dismiss();
                        Log.e("TAG",response.toString());
                        //calling method to parse json array
                        parseData(response);
                        if(response.isNull(0)){
                            TextView textViewdefault = (TextView) findViewById(R.id.defaulttext);
                            textViewdefault.setVisibility(View.VISIBLE);
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.e("ERROR",error.getMessage());
                    }
                });

        //Creating request queue
        RequestQueue requestQueue = Volley.newRequestQueue(this);

        //Adding request to the queue
        requestQueue.add(jsonArrayRequest);
    }

    //This method will parse json data
    private void parseData(JSONArray array) {
        for (int i = 0; i < array.length(); i++) {
            Addresses address = new Addresses();
            JSONObject json;
            try {
                json = array.getJSONObject(i);
                address.setLabel(json.getString(AppConfig.ADDRESS_LABEL));
                address.setAddress(json.getString(AppConfig.ADDRESS));
            } catch (JSONException e) {
                e.printStackTrace();
            }
            addressesList.add(address);
        }
        adapter.notifyDataSetChanged();
    }

    private void storeDataInMemory(String pref_address){
        SharedPreferences.Editor editor = getSharedPreferences(AppConfig.APP_PREFS_NAME, MODE_PRIVATE).edit();
        editor.putString(AppConfig.PREF_ADDRESS_LABEL,pref_address);
        editor.apply();
    }

}
