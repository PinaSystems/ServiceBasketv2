package com.pinasystems.servicebasketv2;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.app.TimePickerDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TimePicker;
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

import java.util.Calendar;
import java.util.HashMap;

public class ProviderResponseActivity extends AppCompatActivity {

    TextView textViewname;
    TextView textViewsubcategory;
    TextView textViewdescription;
    TextView textViewaddress;
    TextView textViewcity;
    TextView textViewpincode;
    TextView textViewdate;
    TextView textViewtime;
    TextView textViewmaxprice;
    TextView textViewprovdate;
    TextView textViewprovtime, textViewuploads;
    EditText editTextminprice, editTextmaxprice,editTextcomment;
    private Calendar calendar;
    private int year, month, day;
    String request_id;

    private static final String GET_REQUEST_DATA_URL = "http://pinasystemsdb.890m.com/sbv2php/getrequestdata.php?reqid=";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_provider_response);
        Intent intent = getIntent();
        String request_id = intent.getStringExtra("reqid");
        user_id = ((DataBank)getApplication()).getUserId();
        getData(request_id);
        textViewsubcategory = (TextView) findViewById(R.id.subcategory);
        textViewtime = (TextView) findViewById(R.id.time);
        textViewaddress = (TextView) findViewById(R.id.address);
        textViewpincode = (TextView) findViewById(R.id.pincode);
        textViewname = (TextView) findViewById(R.id.name);
        textViewmaxprice = (TextView) findViewById(R.id.maxprice);
        textViewdescription = (TextView) findViewById(R.id.description);
        textViewdate = (TextView) findViewById(R.id.date);
        textViewcity = (TextView) findViewById(R.id.city);
        textViewprovdate = (TextView) findViewById(R.id.provdate);
        calendar = Calendar.getInstance();
        year = calendar.get(Calendar.YEAR);

        month = calendar.get(Calendar.MONTH);
        day = calendar.get(Calendar.DAY_OF_MONTH);
        showDate(year, month+1, day);
        textViewprovtime = (TextView) findViewById(R.id.provtime);

        // Load an ad into the AdMob banner view.
        AdView adView = (AdView) findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder()
                .setRequestAgent("android_studio:ad_template").build();
        adView.loadAd(adRequest);

        Button btnsetDateandTime =(Button) findViewById(R.id.setdate);

        btnsetDateandTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setDate();
            }
        });
        textViewuploads = (TextView) findViewById(R.id.viewuploadedfiles);

        textViewuploads.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(both){
                    choiceDailog();
                }else{
                    if (isImage){
                        Intent intent = new Intent(getApplicationContext(),PreviewMediaActivity.class);
                        intent.putExtra("url",image_url);
                        intent.putExtra("isImage",true);
                        startActivity(intent);
                    }else{
                        Intent intent = new Intent(getApplicationContext(),PreviewMediaActivity.class);
                        intent.putExtra("url",video_url);
                        intent.putExtra("isImage",false);
                        startActivity(intent);
                    }
                }
            }
        });
        Button btnmap = (Button) findViewById(R.id.onmap);
        btnmap.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), AddressOnMapActivity.class);
                intent.putExtra("latitude", latitude);
                intent.putExtra("longitude", longitude);
                intent.putExtra("address",address);
                startActivity(intent);
            }
        });

        Button btnacceptrequest = (Button) findViewById(R.id.acceptrequest);
        btnacceptrequest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                checkData();
            }
        });
    }

    private void getData(String request_id) {
        JsonArrayRequest jsonArrayRequest = new JsonArrayRequest(Request.Method.GET, GET_REQUEST_DATA_URL + request_id, new Response.Listener<JSONArray>() {
            @Override
            public void onResponse(JSONArray response) {
                Jsondata(response);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(getApplicationContext(),error.getMessage(),Toast.LENGTH_LONG).show();
                Log.e("TAG",error.getMessage());
            }
        });
        RequestQueue requestQueue = Volley.newRequestQueue(this);
        requestQueue.add(jsonArrayRequest);
    }

    Double latitude, longitude;
    boolean isImage = false, both = false;
    String image_url;
    String video_url,address;

    private void Jsondata(JSONArray response) {
        JSONObject json = null;
        try {
            json = response.getJSONObject(0);
            textViewaddress.setText(json.getString(AppConfig.ADDRESS));
            address = textViewaddress.getText().toString().trim();
            textViewcity.setText(json.getString(AppConfig.CITY));
            textViewdate.setText(json.getString(AppConfig.DATE));
            textViewdescription.setText(json.getString(AppConfig.DESCRIPTION));
            textViewmaxprice.setText(json.getString(AppConfig.MAX_PRICE));
            textViewpincode.setText(json.getString(AppConfig.PINCODE));
            textViewsubcategory.setText(json.getString(AppConfig.CATEGORY) + "/" + json.getString(AppConfig.SUBCATEGORY));
            textViewtime.setText(json.getString(AppConfig.TIME));

            String latitude = json.getString(AppConfig.LATITUDE);
            this.latitude = Double.parseDouble(latitude);
            Log.e("LATITUDE:",latitude);
            String longitude = json.getString(AppConfig.LONGITUDE);
            this.longitude = Double.parseDouble(longitude);
            image_url = json.getString(AppConfig.IMAGE_URL);
            Log.e("IMAGE",image_url);
            video_url = json.getString(AppConfig.VIDEO_URL);
            Log.e("VIDEO",video_url);
            if (image_url.isEmpty()) {
                if (video_url.isEmpty()) {
                    textViewuploads.setVisibility(View.INVISIBLE);
                } else {
                    textViewuploads.setVisibility(View.VISIBLE);
                    isImage = false;
                }
            } else {
                if (video_url.isEmpty()) {
                    isImage = true;
                    textViewuploads.setVisibility(View.VISIBLE);
                } else {
                    both = true;
                    Log.e("URL","value of both is true");
                }
            }
            json = response.getJSONObject(1);
            textViewname.setText(json.getString(AppConfig.NAME));
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void choiceDailog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Choose which file to view:");
        builder.setNeutralButton("View Image", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                Intent intent = new Intent(getApplicationContext(),PreviewMediaActivity.class);
                intent.putExtra("url",image_url);
                intent.putExtra("isImage", true);
                startActivity(intent);
            }
        });

        builder.setPositiveButton("View Video", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                Intent intent = new Intent(getApplicationContext(),PreviewMediaActivity.class);
                intent.putExtra("url",video_url);
                intent.putExtra("isImage", false);
                startActivity(intent);
            }
        });
        Dialog dialog = builder.create();
        dialog.show();
    }


    @SuppressWarnings("deprecation")
    public void setDate() {
        showDialog(999);
    }

    @SuppressWarnings("deprecation")
    @Override
    protected Dialog onCreateDialog(int id) {
        // TODO Auto-generated method stub
        if (id == 999) {
            return new DatePickerDialog(this,
                    myDateListener, year, month, day);
        }
        if(id == 111) {
            final Calendar c = Calendar.getInstance();
            int hour = c.get(Calendar.HOUR_OF_DAY);
            int minute = c.get(Calendar.MINUTE);

            //Create and return a new instance of TimePickerDialog
            return new TimePickerDialog(this,myTimeListener, hour, minute,
                    DateFormat.is24HourFormat(this));
        }
        return null;
    }
    @SuppressWarnings("deprecation")
    private DatePickerDialog.OnDateSetListener myDateListener = new
            DatePickerDialog.OnDateSetListener() {
                @Override
                public void onDateSet(DatePicker arg0,
                                      int arg1, int arg2, int arg3) {
                    // TODO Auto-generated method stub
                    // arg1 = year
                    // arg2 = month
                    // arg3 = day
                    showDialog(111);
                    showDate(arg1, arg2+1, arg3);
                }
            };

    private void showDate(int year, int month, int day) {
        textViewprovdate.setText(new StringBuilder().append(day).append("/")
                .append(month).append("/").append(year));
    }

    private TimePickerDialog.OnTimeSetListener myTimeListener = new
            TimePickerDialog.OnTimeSetListener(){
                @Override
                public void onTimeSet(TimePicker timePicker, int i, int i1) {
                    showTime(i,i1);
                }
            };
    //onTimeSet() callback method
    public void showTime(int hourOfDay, int minute){
        //Do something with the user chosen time
        //Get reference of host activity (XML Layout File) TextView widget
        String aMpM = "AM";
        if(hourOfDay >11)
        {
            aMpM = "PM";
        }

        //Make the 24 hour time format to 12 hour time format
        int currentHour;
        if(hourOfDay>11)
        {
            currentHour = hourOfDay - 12;
        }
        else
        {
            currentHour = hourOfDay;
        }
        String currentMinute;
        if(minute >= 10){
            currentMinute = String.valueOf(minute);
        }
        else
        {
            currentMinute = "0" + String.valueOf(minute);
        }

        //Set a message for user
        //Display the user changed time on TextView
        textViewprovtime.setText(String.valueOf(currentHour)
                + ":" + String.valueOf(currentMinute) + " " + aMpM);
    }

    private void warningDialog(){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Accept Request");
        builder.setMessage("Sending response with a price range or comment can increase the chance of requester to contact you.");
        builder.setPositiveButton("Send without it", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                String max_price = "Not specified";
                String min_price = "Not specified";
                String comment = "No comment submitted by the provider";
                String est_date = textViewprovdate.getText().toString().trim();
                String est_time = textViewprovtime.getText().toString().trim();
                sendData(max_price,min_price,comment,est_date,est_time);
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

            }
        });

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void checkData(){
        editTextmaxprice = (EditText) findViewById(R.id.max_price);
        editTextminprice = (EditText) findViewById(R.id.min_price);
        editTextcomment = (EditText) findViewById(R.id.comment);

        boolean terminate = false;

        String max_price = editTextmaxprice.getText().toString().trim();
        String min_price = editTextminprice.getText().toString().trim();
        String comment = editTextcomment.getText().toString().trim();
        if(max_price.isEmpty() || min_price.isEmpty() || comment.isEmpty()){
            warningDialog();
            terminate = true;
        }

        if(!terminate){
            String est_date = textViewprovdate.getText().toString().trim();
            String est_time = textViewprovtime.getText().toString().trim();
            sendData(max_price,min_price,comment,est_date,est_time);
        }
    }

    String user_id;
    private void sendData(final String max_price, final String min_price, final String comment, final String date, final String time){
        final String URL = "http://pinasystemsdb.890m.com/sbv2php/acceptrequest.php";
        final ProgressDialog progressDialog = ProgressDialog.show(getApplicationContext(),"Sending response","Please Wait....",false,false);
        StringRequest stringRequest = new StringRequest(Request.Method.POST, URL, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Toast.makeText(getApplicationContext(),response,Toast.LENGTH_LONG).show();
                Log.e("RESPONSE",response);
                progressDialog.dismiss();
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                progressDialog.dismiss();
                Toast.makeText(getApplicationContext(),error.getMessage(),Toast.LENGTH_LONG).show();
                Log.e("TAG",error.getMessage());
            }
        }){
            @Override
            protected HashMap<String , String> getParams() throws AuthFailureError {
                HashMap<String , String > params = new HashMap<>();
                params.put(AppConfig.REQ_ID,request_id);
                params.put(AppConfig.MAX_PRICE,max_price);
                params.put(AppConfig.MIN_PRICE,min_price);
                params.put(AppConfig.COMMENT,comment);
                params.put(AppConfig.DATE,date);
                params.put(AppConfig.TIME,time);
                params.put(AppConfig.USER_ID,user_id);
                return params;
            }
        };
        RequestQueue requestQueue = Volley.newRequestQueue(this);
        requestQueue.add(stringRequest);
    }
}