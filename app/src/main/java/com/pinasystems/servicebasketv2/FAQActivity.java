package com.pinasystems.servicebasketv2;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.widget.Toast;

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

public class FAQActivity extends AppCompatActivity {

    private RequestQueue requestQueue;
    private RecyclerView.Adapter adapter;
    private List<FAQ> listfaq;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_faq);

        // Load an ad into the AdMob banner view.
        AdView adView = (AdView) findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder()
                .setRequestAgent("android_studio:ad_template").build();
        adView.loadAd(adRequest);

        //---------------------Card View ---------------------------------------------------------//
        listfaq = new ArrayList<>();
        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.recyclerView);
        recyclerView.setHasFixedSize(true);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
        adapter =new FAQCardAdapter(listfaq);
        recyclerView.setAdapter(adapter);
        requestQueue = Volley.newRequestQueue(this);
        getData();
    }
    //This method will get data from the web api

    private void getData() {
        requestQueue.add(getDataFromServer());
    }

    String subcategory;

    private JsonArrayRequest getDataFromServer() {
        final ProgressDialog loading = ProgressDialog.show(this, "Loading Data", "Please wait...", false, false);

        //JsonArrayRequest of volley
        return new JsonArrayRequest(Request.Method.POST,AppConfig.ROOT_URL + "getfaqs.php",
                new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray response) {
                        //Calling method parseData to parse the json response
                        parseData(response);
                        Log.e("ARRAY",response.toString());
                        //Hiding the progressbar
                        loading.dismiss();
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        loading.dismiss();
                        error.printStackTrace();
                        Toast.makeText(getApplicationContext(),"Error connecting to internet",Toast.LENGTH_LONG).show();
                    }
                });

    }

    //This method will parse json data
    private void parseData(JSONArray array){
        for(int i = 0; i<array.length(); i++) {
            FAQ faq = new FAQ();
            JSONObject json;
            try {
                json = array.getJSONObject( i);
                faq.setQuestion(json.getString("question"));
                faq.setAnswer(json.getString("answer"));
            } catch (JSONException e) {
                e.printStackTrace();
            }
            listfaq.add(faq);
        }
        adapter.notifyDataSetChanged();
    }
}