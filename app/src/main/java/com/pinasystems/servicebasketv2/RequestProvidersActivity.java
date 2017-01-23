package com.pinasystems.servicebasketv2;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

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

public class RequestProvidersActivity extends AppCompatActivity {

    private RecyclerView.Adapter adapter;
    private List<Providers> listproviders;
    String URL;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_request_providers);
        Intent intent = getIntent();
        String reqid = intent.getStringExtra("id");
        URL = AppConfig.ROOT_URL + "getproviders.php?id="+reqid;

        // Load an ad into the AdMob banner view.
        AdView adView = (AdView) findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder()
                .setRequestAgent("android_studio:ad_template").build();
        adView.loadAd(adRequest);

        //---------------------Card View ---------------------------------------------------------//
        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.recyclerView);
        recyclerView.setHasFixedSize(true);
        listproviders = new ArrayList<>();
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
        adapter =new ProviderCardAdapter(listproviders);
        recyclerView.setAdapter(adapter);
        Context context = getApplicationContext();
        recyclerView.addOnItemTouchListener(
                new RecyclerItemClickListener(context, new RecyclerItemClickListener.OnItemClickListener() {
                    @Override
                    public void onItemClick(View view, int position) {
                        Intent intent = new Intent(getApplicationContext(), ViewProviderDetailsActivity.class);
                        intent.putExtra("id", listproviders.get(position).getId());
                        intent.putExtra("est_date", listproviders.get(position).getEst_date());
                        intent.putExtra("est_time", listproviders.get(position).getEst_time());
                        intent.putExtra("comment", listproviders.get(position).getComment());
                        intent.putExtra("min_price", listproviders.get(position).getMin_price());
                        intent.putExtra("max_price" , listproviders.get(position).getMax_price());
                        startActivity(intent);

                    }
                })
        );
        Toast.makeText(getApplicationContext(),"Refreshing list",Toast.LENGTH_LONG).show();
        getData();
    }

    //=================================Get data From Server ======================================//

    private void getData(){
        //Showing a progress dialog
        final ProgressDialog loading = ProgressDialog.show(this,"Loading Data", "Please wait...",false,false);

        //Creating a json array request
        JsonArrayRequest jsonArrayRequest = new JsonArrayRequest(URL,
                new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray response) {
                        //Dismissing progress dialog
                        loading.dismiss();

                        //calling method to parse json array
                        parseData(response);
                        Log.e("RESPONSE",response.toString());
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.e("VOLLEY",error.getMessage());
                        loading.dismiss();
                    }
                });

        //Creating request queue
        RequestQueue requestQueue = Volley.newRequestQueue(this);

        //Adding request to the queue
        requestQueue.add(jsonArrayRequest);
    }

    //This method will parse json data
    private void parseData(JSONArray array){
        for(int i = 0; i<array.length(); i++) {
            Providers provider = new Providers();
            JSONObject json;
            try {
                json = array.getJSONObject( i);
                provider.setEst_date(json.getString("est_date"));
                provider.setEst_time(json.getString("est_time"));
                provider.setMax_price(json.getString("max_price"));
                provider.setMin_price(json.getString("min_price"));
                provider.setName(json.getString("name"));
                provider.setId(json.getString("user_id"));
                provider.setComment(json.getString("comment"));
                provider.setRating(json.getString("rating"));
            } catch (JSONException e) {
                e.printStackTrace();
            }
            listproviders.add(provider);
        }
        adapter.notifyDataSetChanged();
    }
}
