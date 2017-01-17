package com.pinasystems.servicebasketv2;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.NonNull;
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

public class RequesterMainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private List<Categories> categoriesList;

    //Creating Views
    private RecyclerView recyclerView;
    private RecyclerView.LayoutManager layoutManager;
    private RecyclerView.Adapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_requester_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

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

        categoriesList = new ArrayList<>();

        recyclerView = (RecyclerView) findViewById(R.id.recyclerView);
        recyclerView.setHasFixedSize(true);
        layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
        //Finally initializing our adapter
        adapter = new CategoryCardAdapter(categoriesList, this);

        //Adding adapter to recyclerview
        recyclerView.setAdapter(adapter);
        Context context = getApplicationContext();
        recyclerView.addOnItemTouchListener(
                new RecyclerItemClickListener(context, new RecyclerItemClickListener.OnItemClickListener() {
                    @Override public void onItemClick(View view, int position) {
                        Log.i("CATEGORY",categoriesList.get(position).getCategory());
                        Intent intent=new Intent(getApplicationContext(),NewRequestActivity.class);
                        ((DataBank)getApplication()).setCategory(categoriesList.get(position).getCategory());
                        startActivity(intent);
                    }
                })
        );
        //Initializing our superheroes list

        //Calling method to get data
        getData();
        // Load an ad into the AdMob banner view.
        AdView adView = (AdView) findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder()
                .setRequestAgent("android_studio:ad_template").build();
        adView.loadAd(adRequest);
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
            Intent intent = new Intent(getApplicationContext(),PreviousRequestsActivity.class);
            startActivity(intent);

        } else if (id == R.id.nav_faq) {
            Intent intent = new Intent(getApplicationContext(),FAQActivity.class);
            startActivity(intent);

        } else if (id == R.id.nav_feedback) {
            Intent intent = new Intent(getApplicationContext(),AppFeedbackActivity.class);
            intent.putExtra("isrequester",true);
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

    private void loginStatus(boolean isloggedin) {
        SharedPreferences.Editor editor = getSharedPreferences(AppConfig.APP_PREFS_NAME, MODE_PRIVATE).edit();
        editor.putBoolean(AppConfig.PREF_LOGIN_STATUS, isloggedin);
        editor.apply();
        Intent intent = new Intent(getApplicationContext(),LoginActivity.class);
        startActivity(intent);
        finish();
    }

//-------------------------------------------------------------------------------------------------//


    private void getData(){
        //Showing a progress dialog
        final ProgressDialog loading = ProgressDialog.show(this,"Loading Data", "Please wait...",false,false);

        //Creating a json array request
        JsonArrayRequest jsonArrayRequest = new JsonArrayRequest(AppConfig.GET_CATEGORIES,
                new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray response) {
                        //Dismissing progress dialog
                        loading.dismiss();

                        //calling method to parse json array
                        parseData(response);
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        error.printStackTrace();
                        loading.dismiss();
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
            Categories category = new Categories();
            JSONObject json = null;
            try {
                json = array.getJSONObject(i);
                category.setImageUrl(json.getString(AppConfig.IMAGE_URL));
                category.setCategory(json.getString(AppConfig.CATEGORY));
            } catch (JSONException e) {
                Log.e("JSON",e.getMessage());
                e.printStackTrace();
            }
            categoriesList.add(category);
        }
        adapter.notifyDataSetChanged();
    }
}
