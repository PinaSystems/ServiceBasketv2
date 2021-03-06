package com.pinasystems.servicebasketv2;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class ProviderMainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener,GoogleApiClient.ConnectionCallbacks {

    private RequestQueue requestQueue;
    private RecyclerView.Adapter adapter;
    private List<Requests> listrequests;
    private GoogleApiClient mGoogleApiClient;

    String user_Id;

    @Override

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_provider_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        user_Id = ((DataBank)getApplication()).getUserId();
        getDataFromMemory();

        //For Google sign out
        if(auth.equalsIgnoreCase("google")){
            connectGoogleApiClient();
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);


        //Nav header part
        View navHeaderView= navigationView.getHeaderView(0);
        TextView textViewemail = (TextView) navHeaderView.findViewById(R.id.header_email);
        TextView textViewname = (TextView) navHeaderView.findViewById(R.id.header_name);
        TextView textViewtelno = (TextView) navHeaderView.findViewById(R.id.header_telno);

        textViewemail.setText(((DataBank)getApplication()).getEmail());
        textViewname.setText(((DataBank)getApplication()).getName());
        textViewtelno.setText(((DataBank)getApplication()).getTelno());

        // Load an ad into the AdMob banner view.
        AdView adView = (AdView) findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder()
                .setRequestAgent("android_studio:ad_template").build();
        adView.loadAd(adRequest);

        //---------------------Card View ---------------------------------------------------------//
        listrequests = new ArrayList<>();
        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.recyclerView);
        recyclerView.setHasFixedSize(true);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
        adapter =new RequestCardAdapter(listrequests);
        recyclerView.setAdapter(adapter);
        Context context = getApplicationContext();
        recyclerView.addOnItemTouchListener(
                new RecyclerItemClickListener(context, new RecyclerItemClickListener.OnItemClickListener() {
                    @Override public void onItemClick(View view, int position) {
                        Intent intent = new Intent(getApplicationContext(),ProviderResponseActivity.class);
                        intent.putExtra("reqid",listrequests.get(position).getReqid());
                        startActivity(intent);
                    }
                })
        );
        requestQueue = Volley.newRequestQueue(this);
        Toast.makeText(getApplicationContext(),"Refreshing list",Toast.LENGTH_LONG).show();
        getData();
    }

    //========================Google API client

    protected synchronized void connectGoogleApiClient(){
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this, new GoogleApiClient.OnConnectionFailedListener() {
                    @Override
                    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
                        connectionResult.getErrorMessage();
                    }
                })
                .addConnectionCallbacks(this)
                .addApi(Auth.GOOGLE_SIGN_IN_API)
                .build();
        mGoogleApiClient.connect();
    }


    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_prev_requests) {
            Intent intent = new Intent(getApplicationContext(), PreviousRequestsActivity.class);
            startActivity(intent);
        } else if (id == R.id.nav_faq) {
            Intent intent = new Intent(getApplicationContext(),FAQActivity.class);
            startActivity(intent);

        } else if (id == R.id.nav_feedback) {
            Intent intent = new Intent(getApplicationContext(),AppFeedbackActivity.class);
            intent.putExtra("isrequester",false);
            startActivity(intent);
        } else if (id == R.id.nav_create_req) {
            Intent intent = new Intent(getApplicationContext(),RequesterMainActivity.class);
            startActivity(intent);
        } else if (id == R.id.nav_share) {
            String message = "Hi! I am using Service Basket";
            Intent share = new Intent(Intent.ACTION_SEND);
            share.setType("text/plain");
            share.putExtra(Intent.EXTRA_TEXT, message);

            startActivity(Intent.createChooser(share, "Share this application via"));

        } else if (id == R.id.nav_log_out) {
            logoutDialog();
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }
    //================================Log out =====================================================//


    private void logoutDialog(){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Log out");
        builder.setMessage("Are you sure?");
        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                if (auth.equalsIgnoreCase("google")) {
                    if (mGoogleApiClient.isConnected()) {
                        signOut();
                    }
                }

                loginStatus(false);
            }
        });
        builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

            }
        });

        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    private void signOut() {

        Auth.GoogleSignInApi.signOut(mGoogleApiClient).setResultCallback(
                new ResultCallback<Status>() {
                    @Override
                    public void onResult(@NonNull Status status) {
                        mGoogleApiClient.disconnect();
                    }
                });
    }


    private void loginStatus(boolean isloggedin) {
        SharedPreferences.Editor editor = getSharedPreferences(AppConfig.APP_PREFS_NAME, MODE_PRIVATE).edit();
        editor.putBoolean(AppConfig.PREF_LOGIN_STATUS, isloggedin);
        editor.apply();
        Intent intent = new Intent(getApplicationContext(),LoginActivity.class);
        startActivity(intent);
        finish();
    }



    //============================== Get Data From Memory(SharedPref) =============================//

    String auth;

    private void getDataFromMemory(){
        SharedPreferences sharedPreferences = getSharedPreferences(AppConfig.APP_PREFS_NAME, MODE_PRIVATE);
        subcategory = sharedPreferences.getString(AppConfig.PREF_SUBCATEGORY,"No subCategory");
        auth = sharedPreferences.getString("AUTH","static");
    }


    //--------------------------------------------Card View Code ----------------------------------//

    //This method will get data from the web api

    private void getData() {
        requestQueue.add(getDataFromServer());
    }

    String subcategory;

    private PostJsonRequest getDataFromServer() {
        final ProgressDialog loading = ProgressDialog.show(this, "Loading Data", "Please wait...", false, false);

        HashMap<String, String> params = new HashMap<>();
        params.put("subcategory", subcategory);
        params.put("userid",user_Id);
        //JsonArrayRequest of volley
        return new PostJsonRequest(Request.Method.POST,AppConfig.GET_REQUESTS_FOR_PROVIDER,params,
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
                        Log.e("REQUESTS FETCH ERROR",error.getMessage());
                        Toast.makeText(getApplicationContext(), error.getMessage() ,Toast.LENGTH_LONG).show();
                    }
                });

    }

    //This method will parse json data
    private void parseData(JSONArray array){
        for(int i = 0; i<array.length(); i++) {
            Requests request = new Requests();
            JSONObject json;
            try {
                json = array.getJSONObject( i);
                request.setSubcategory(json.getString(AppConfig.CATEGORY) + " / " + json.getString(AppConfig.SUBCATEGORY));
                request.setDate(json.getString("created_at"));
                request.setReqid(json.getString(AppConfig.USER_ID));
                request.setDescription(json.getString(AppConfig.DESCRIPTION));
                request.setName(json.getString(AppConfig.NAME));
            } catch (JSONException e) {
                e.printStackTrace();
            }
            listrequests.add(request);
        }
        adapter.notifyDataSetChanged();
    }

    //---------------Google API Client Methods

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        Toast.makeText(getApplicationContext(),"Client Connected",Toast.LENGTH_LONG).show();
    }

    @Override
    public void onConnectionSuspended(int i) {

    }
}