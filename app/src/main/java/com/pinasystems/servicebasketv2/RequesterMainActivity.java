package com.pinasystems.servicebasketv2;

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

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;

import java.util.ArrayList;
import java.util.List;

public class RequesterMainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener,GoogleApiClient.ConnectionCallbacks {

    private List<Categories> categoriesList;

    private RecyclerView.Adapter adapter;
    private GoogleApiClient mGoogleApiClient;

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

        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.recyclerView);
        recyclerView.setHasFixedSize(true);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
        //Finally initializing our adapter
        adapter = new CategoryCardAdapter(categoriesList);

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

        getDataFromMemory();

        //For Google sign out
        if(auth.equalsIgnoreCase("google")){
            connectGoogleApiClient();
        }

    }
    String auth;

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

    //============================== Get Data From Memory(SharedPref) =============================//

    private void getDataFromMemory(){
        SharedPreferences sharedPreferences = getSharedPreferences(AppConfig.APP_PREFS_NAME, MODE_PRIVATE);
        auth = sharedPreferences.getString("AUTH","static");
    }
    //=========================== On Back pressed ================================================//
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

//-------------------------------------------------------------------------------------------------//


    private int [] imagefiles = {R.drawable.category_cleaning, R.drawable.category_beauty,
            R.drawable.category_finance, R.drawable.category_travel};

    String cleaning = "-> Car \n-> Carpet \n-> Home \n-> Appliances\n & more";
    String beauty = "-> Bleaching \n-> Facial \n-> Cleanup \n-> Hair-Care\n & more";
    String finance = "-> Tax \n-> Financial Planner \n-> Insurance";
    String travel = "-> Agents \n-> Tours \n-> Driver \n-> Cab or Auto";
    private String [] categorydesc = {cleaning , beauty , finance , travel};

    private String [] categorynames = {"Cleaning","Beauty","Finance","Travel"};

    private void getData(){
        int j = 0;
        for (int image: imagefiles) {
            Categories categories = new Categories();
            categories.setImagefile(image);
            categories.setDescription(categorydesc[j]);
            categories.setCategory(categorynames[j]);
            j = j + 1;
            categoriesList.add(categories);
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
