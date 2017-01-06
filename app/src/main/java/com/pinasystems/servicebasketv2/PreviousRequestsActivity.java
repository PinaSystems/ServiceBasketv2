package com.pinasystems.servicebasketv2;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources.Theme;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.ThemedSpinnerAdapter;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class PreviousRequestsActivity extends AppCompatActivity {

    int request_status;
    String user_id;
    private RecyclerView recyclerView;
    private RecyclerView.LayoutManager layoutManager;
    private RecyclerView.Adapter adapter;
    private List<PrevRequests> listrequests;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_previous_requests);
        //Temp value to check module
        listrequests = new ArrayList<>();
        user_id = ((DataBank)getApplication()).getUserId();
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        // Setup spinner
        Spinner spinner = (Spinner) findViewById(R.id.spinner);
        spinner.setAdapter(new MyAdapter(
                toolbar.getContext(),
                new String[]{
                        "Open Requests",
                        "Closed Requests",
                        "Cancelled Requests",
                }));

        spinner.setOnItemSelectedListener(new OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                // When the given dropdown item is selected, show its contents in the
                // container view.
                //TODO URL and refetch data + clear adapter data
                listrequests.clear();
                if(position == 0){
                    request_status = 0;
                    String URL = "http://pinasystemsdb.890m.com/sbv2php/getmyrequests.php?id=" + user_id +"&status=" + request_status;
                    Log.e("URL",URL);
                    getData(URL);
                }else if (position == 1){
                    request_status = 1;
                    String URL = "http://pinasystemsdb.890m.com/sbv2php/getmyrequests.php?id=" + user_id +"&status=" + request_status;
                    Log.e("URL",URL);
                    getData(URL);
                }else if(position == 2){
                    request_status = 2;
                    String URL = "http://pinasystemsdb.890m.com/sbv2php/getmyrequests.php?id=" + user_id +"&status=" + request_status;
                    Log.e("URL",URL);
                    getData(URL);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        //---------------------Card View ---------------------------------------------------------//
        recyclerView = (RecyclerView) findViewById(R.id.recyclerView);
        recyclerView.setHasFixedSize(true);
        layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
        adapter =new PrevRequestCardAdapter(listrequests , this);
        recyclerView.setAdapter(adapter);
        Context context = getApplicationContext();
        recyclerView.addOnItemTouchListener(
                new RecyclerItemClickListener(context, new RecyclerItemClickListener.OnItemClickListener() {
                    @Override public void onItemClick(View view, int position) {
                        Intent intent = new Intent(getApplicationContext(),MyRequestsDetailsActivity.class);
                        intent.putExtra("reqid",listrequests.get(position).getReqid());
                        startActivity(intent);
                    }
                })
        );
        Toast.makeText(getApplicationContext(),"Refreshing list",Toast.LENGTH_LONG).show();
    }

    private static class MyAdapter extends ArrayAdapter<String> implements ThemedSpinnerAdapter {
        private final ThemedSpinnerAdapter.Helper mDropDownHelper;

        public MyAdapter(Context context, String[] objects) {
            super(context, android.R.layout.simple_list_item_1, objects);
            mDropDownHelper = new ThemedSpinnerAdapter.Helper(context);
        }

        @Override
        public View getDropDownView(int position, View convertView, ViewGroup parent) {
            View view;

            if (convertView == null) {
                // Inflate the drop down using the helper's LayoutInflater
                LayoutInflater inflater = mDropDownHelper.getDropDownViewInflater();
                view = inflater.inflate(android.R.layout.simple_list_item_1, parent, false);
            } else {
                view = convertView;
            }

            TextView textView = (TextView) view.findViewById(android.R.id.text1);
            textView.setText(getItem(position));

            return view;
        }

        @Override
        public Theme getDropDownViewTheme() {
            return mDropDownHelper.getDropDownViewTheme();
        }

        @Override
        public void setDropDownViewTheme(Theme theme) {
            mDropDownHelper.setDropDownViewTheme(theme);
        }
    }


    //=================================Get data From Server ======================================//

    private void getData(String URL){
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
            PrevRequests request = new PrevRequests();
            JSONObject json = null;
            try {
                json = array.getJSONObject(i);
                request.setSubcategory(json.getString(AppConfig.CATEGORY) + " / " + json.getString(AppConfig.SUBCATEGORY));
                request.setDate(json.getString("created_at"));
                request.setReqid(json.getString(AppConfig.USER_ID));
                request.setDescription(json.getString(AppConfig.DESCRIPTION));
            } catch (JSONException e) {
                e.printStackTrace();
            }
            listrequests.add(request);
        }
        adapter.notifyDataSetChanged();
    }

}
