package com.pinasystems.servicebasketv2;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;

import static android.view.View.GONE;

public class MyRequestsDetailsActivity extends AppCompatActivity {

    String reqid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_requests_details);
        Intent intent = getIntent();
        reqid = intent.getStringExtra("reqid");
        getRequestData(reqid);

        // Load an ad into the AdMob banner view.
        AdView adView = (AdView) findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder()
                .setRequestAgent("android_studio:ad_template").build();
        adView.loadAd(adRequest);

        Button buttonchangestatus = (Button) findViewById(R.id.changestatus);
        buttonchangestatus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.e("REQSTAT","clicked");
                changeDialog();
            }
        });

        Button buttonviewproviders = (Button) findViewById(R.id.view_providers);
        buttonviewproviders.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(),RequestProvidersActivity.class);
                intent.putExtra("id",reqid);
                startActivity(intent);
            }
        });

    }

    private void getRequestData(String id){
        final ProgressDialog progressDialog = ProgressDialog.show(this,"Loading","Please Wait...",false,false);

        String URL = "http://pinasystemsdb.890m.com/sbv2php/myrequestdetails.php?id=";

        JsonArrayRequest jsonArrayRequest = new JsonArrayRequest(URL + id, new Response.Listener<JSONArray>() {
            @Override
            public void onResponse(JSONArray response) {
                parseData(response);
                Log.e("RESPONSE",response.toString());
                progressDialog.dismiss();
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e("TAG",error.getMessage());
                progressDialog.dismiss();
            }
        });

        RequestQueue requestQueue = Volley.newRequestQueue(this);
        requestQueue.add(jsonArrayRequest);

    }

    String date;

    //This method will parse json data
    private void parseData(JSONArray array) {
        JSONObject json;
        TextView textViewcatsubcat,textViewdescription,textViewaddress,textViewcity,textViewpincode,textViewdate,textViewtime,textViewmaxprice;
        textViewaddress = (TextView)findViewById(R.id.address);
        textViewcatsubcat = (TextView)findViewById(R.id.subcategory);
        textViewcity = (TextView) findViewById(R.id.city);
        textViewdate = (TextView) findViewById(R.id.date);
        textViewdescription = (TextView) findViewById(R.id.description);
        textViewmaxprice = (TextView) findViewById(R.id.maxprice);
        textViewpincode = (TextView) findViewById(R.id.pincode);
        textViewtime = (TextView) findViewById(R.id.time);
        try {
            json = array.getJSONObject(0);
            textViewaddress.setText(json.getString(AppConfig.ADDRESS));
            textViewcity.setText(json.getString(AppConfig.CITY));
            date = json.getString(AppConfig.DATE);
            textViewdate.setText(date);
            textViewdescription.setText(json.getString(AppConfig.DESCRIPTION));
            textViewmaxprice.setText(json.getString(AppConfig.MAX_PRICE));
            textViewpincode.setText(json.getString(AppConfig.PINCODE));
            textViewtime.setText(json.getString(AppConfig.TIME));
            String catsubcat = json.getString(AppConfig.CATEGORY) + "/" + json.getString(AppConfig.SUBCATEGORY);
            textViewcatsubcat.setText(catsubcat);

            Button buttonchangestatus = (Button) findViewById(R.id.changestatus);
            Button buttonproviders = (Button) findViewById(R.id.view_providers);
            if(json.getString("status").equalsIgnoreCase("0")){
                buttonchangestatus.setVisibility(View.VISIBLE);
                buttonproviders.setVisibility(View.VISIBLE);
            }else{
                buttonchangestatus.setVisibility(GONE);
                buttonproviders.setVisibility(GONE);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
    AlertDialog dialog;

    private void changeDialog(){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Request status");
        builder.setMessage("Request open since " + date + ".");
        builder.setPositiveButton("Close Request", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                Intent intent = new Intent(getApplicationContext(),RequestClosedActivity.class);
                intent.putExtra("id",reqid);
                startActivity(intent);
            }
        });
        builder.setNegativeButton("Cancel Request", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialog.dismiss();
                cancelDialog();
            }
        });

        dialog = builder.create();
        dialog.show();
    }

    String reason;

    String[] strings = {"No longer needed","Incorrect Subcategory","Lack of responses","Price supplied is high","Found Externally"};

    private void cancelDialog() {

        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        // Set the dialog title
        builder.setTitle("Cancel Request")
                .setSingleChoiceItems(R.array.cancelrequest, -1, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        reason = strings[i];
                    }
                })


                .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        // User clicked OK, so save the mSelectedItems results somewhere
                        // or return them to the component that opened the dialog
                        Toast.makeText(getApplicationContext(), reason, Toast.LENGTH_LONG).show();
                        requestCancel(reason,reqid);
                    }
                })

                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {

                    }
                });

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    String CANCEL_URL = AppConfig.ROOT_URL + "requestcancel.php";

    private void requestCancel(final String reason , final String id){
        StringRequest stringRequest = new StringRequest(Request.Method.POST, CANCEL_URL, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                if(response.equalsIgnoreCase("success")){
                    nextActivity();
                    Toast.makeText(getApplicationContext(),"Request Cancelled",Toast.LENGTH_LONG).show();
                }else{
                    Toast.makeText(getApplicationContext(),response,Toast.LENGTH_LONG).show();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                error.printStackTrace();
            }
        }){
            @Override
            protected HashMap<String , String > getParams() throws AuthFailureError {
                HashMap<String,String> params = new HashMap<>();
                params.put("reason",reason);
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