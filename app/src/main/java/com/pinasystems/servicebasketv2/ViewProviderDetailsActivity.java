package com.pinasystems.servicebasketv2;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.TextView;

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

public class ViewProviderDetailsActivity extends AppCompatActivity {

    String URL = AppConfig.ROOT_URL + "viewproviderdetails.php";
    TextView textViewname , textViewtelno , textViewaddress , textViewcity , textViewpincode , textViewprice , textViewdate , textViewtime;
    TextView textViewcomment;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_provider_details);
        textViewaddress = (TextView) findViewById(R.id.address);
        textViewcity = (TextView) findViewById(R.id.city);
        textViewcomment = (TextView) findViewById(R.id.comment);
        textViewdate = (TextView) findViewById(R.id.date);
        textViewname = (TextView) findViewById(R.id.name);
        textViewpincode = (TextView) findViewById(R.id.pincode);
        textViewprice = (TextView) findViewById(R.id.price);
        textViewtime = (TextView) findViewById(R.id.time);
        textViewtelno = (TextView) findViewById(R.id.telno);
        Intent intent = getIntent();
        String id = intent.getStringExtra("id");
        String est_date = intent.getStringExtra("est_date");
        String est_time = intent.getStringExtra("est_time");
        String min_pirce = intent.getStringExtra("min_price");
        String max_price = intent.getStringExtra("max_price");
        String price_range = min_pirce + " till " + max_price;
        String comment = intent.getStringExtra("comment");

        textViewdate.setText(est_date);
        textViewtime.setText(est_time);
        textViewprice.setText(price_range);
        textViewcomment.setText(comment);

        getData(id);

    }
    private void getData(final String id) {

        StringRequest stringRequest = new StringRequest(Request.Method.POST, URL, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                extractJSON(response);
                Log.e("ERROR",response);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

            }
        }) {
            @Override
            protected HashMap<String, String> getParams() throws AuthFailureError {
                HashMap<String, String> params = new HashMap<>();
                params.put("id", id);
                return params;
            }
        };

        RequestQueue requestQueue = Volley.newRequestQueue(this);
        requestQueue.add(stringRequest);
    }

    private void extractJSON(String jsondata) {
        try {
            JSONObject jsonObject = new JSONObject(jsondata);
            JSONArray array = jsonObject.getJSONArray(AppConfig.JSON_ARRAY);
            parseData(array);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void parseData(JSONArray array) {
        JSONObject json;
        try {
            json = array.getJSONObject(0);
            textViewtelno.setText(json.getString("telno"));
            textViewpincode.setText(json.getString("pincode"));
            textViewaddress.setText(json.getString("address"));
            textViewcity.setText(json.getString("city"));
            textViewname.setText(json.getString("name"));

        } catch (JSONException e) {
            e.printStackTrace();
        }

    }
}
