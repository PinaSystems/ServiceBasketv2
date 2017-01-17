package com.pinasystems.servicebasketv2;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TableLayout;
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
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.InterstitialAd;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

public class NewRequestActivity extends AppCompatActivity {


    private Calendar calendar;
    private TextView dateView, textViewaddresslabel, textViewaddress, textViewcity, textViewpincode;
    private int year, month, day;
    private Button btnsetdateandtime;
    String category;
    String userid;
    String address;
    String city;
    String pincode;
    String latitude;
    String longitude;
    TableLayout tableLayoutaddress;
    Button chgaddress;
    Button btnsubmitrequest;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_request);
        tableLayoutaddress = (TableLayout) findViewById(R.id.addresstable);
        chgaddress = (Button) findViewById(R.id.changeaddress);
        category = ((DataBank) getApplication()).getCategory();
        Log.w("Category", category);
        getDatafromMemory();
        userid = ((DataBank) getApplication()).getUserId();
        ((DataBank) getApplication()).setCategory(category);
        dateView = (TextView) findViewById(R.id.date_selected);
        calendar = Calendar.getInstance();
        year = calendar.get(Calendar.YEAR);

        month = calendar.get(Calendar.MONTH);
        day = calendar.get(Calendar.DAY_OF_MONTH);
        showDate(year, month + 1, day);

        btnsetdateandtime = (Button) findViewById(R.id.setdate);
        btnsetdateandtime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setDate();
            }
        });

        addTimeDetails();

        mInterstitialAd = newInterstitialAd();
        loadInterstitial();

        btnsubmitrequest = (Button) findViewById(R.id.submitrequest);
        btnsubmitrequest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getData();

            }
        });

        // Load an ad into the AdMob banner view.
        AdView adView = (AdView) findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder()
                .setRequestAgent("android_studio:ad_template").build();
        adView.loadAd(adRequest);
    }

    InterstitialAd mInterstitialAd;
    boolean proceed = false;

    private InterstitialAd newInterstitialAd() {
        InterstitialAd interstitialAd = new InterstitialAd(this);
        interstitialAd.setAdUnitId(getString(R.string.video_ad_unit_id));
        interstitialAd.setAdListener(new AdListener() {
            @Override
            public void onAdLoaded() {
                btnsubmitrequest.setEnabled(true);
            }

            @Override
            public void onAdFailedToLoad(int errorCode) {
                btnsubmitrequest.setEnabled(true);
            }

            @Override
            public void onAdClosed() {
                nextActivity(proceed);
            }
        });
        return interstitialAd;
    }

    private void showInterstitial() {
        // Show the ad if it's ready. Otherwise toast and reload the ad.
        if (mInterstitialAd != null && mInterstitialAd.isLoaded()) {
            mInterstitialAd.show();
            Toast.makeText(getApplicationContext(),"Submitting data",Toast.LENGTH_LONG).show();
        } else {
            Toast.makeText(this, "Ad did not load", Toast.LENGTH_SHORT).show();
        }
    }

    private void loadInterstitial() {
        // Disable the next level button and load the ad.
        AdRequest adRequest = new AdRequest.Builder()
                .setRequestAgent("android_studio:ad_template").build();
        mInterstitialAd.loadAd(adRequest);
    }


    private void getDatafromMemory() {
        SharedPreferences sharedPreferences = getSharedPreferences(AppConfig.APP_PREFS_NAME, MODE_PRIVATE);
        String pref_address = sharedPreferences.getString(AppConfig.PREF_ADDRESS_LABEL, null);
        textViewaddress = (TextView) findViewById(R.id.address);
        textViewaddresslabel = (TextView) findViewById(R.id.addresslabel);
        textViewcity = (TextView) findViewById(R.id.city);
        textViewpincode = (TextView) findViewById(R.id.pincode);
        if (pref_address != null) {
            String address_label = pref_address + ":";
            textViewaddresslabel.setText(address_label);
            getAddress(pref_address);
            chgaddress.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(getApplicationContext(),SavedAddressesActivity.class);
                    startActivity(intent);
                }
            });

        } else {
            getSubcat();
            tableLayoutaddress.setShrinkAllColumns(true);
            Toast.makeText(getApplicationContext(), "No saved Address", Toast.LENGTH_LONG).show();
            chgaddress.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(getApplicationContext(),AddressMapsActivity.class);
                    startActivity(intent);
                }
            });
            TableLayout tableLayout = (TableLayout) findViewById(R.id.addresstable);
            tableLayout.setVisibility(View.GONE);
            chgaddress.setText(R.string.addaddress);
        }
    }


    private void getAddress(final String address_label) {
        final ProgressDialog loading = ProgressDialog.show(this, "Getting Address", "Please wait...", false, false);
        StringRequest stringRequest = new StringRequest(Request.Method.POST, AppConfig.GET_DEFAULT_ADDRESS, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                extractJSON(response);
                Log.e("response", response);
                getSubcat();
                loading.dismiss();
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                loading.dismiss();
            }
        }) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                HashMap<String, String> params = new HashMap<>();
                params.put(AppConfig.USER_ID, userid);
                params.put(AppConfig.ADDRESS_LABEL, address_label);
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
            storeData(array);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void storeData(JSONArray address_array) {
        try {
            JSONObject jsonObject = address_array.getJSONObject(0);
            address = jsonObject.getString(AppConfig.ADDRESS);
            city = jsonObject.getString(AppConfig.CITY);
            pincode = jsonObject.getString(AppConfig.PINCODE);
            latitude = jsonObject.getString(AppConfig.LATITUDE);
            longitude = jsonObject.getString(AppConfig.LONGITUDE);

            Log.e("LATITUDE AND LONGITUDE", latitude + " and " + longitude);
            textViewaddress.setText(address);
            textViewpincode.setText(pincode);
            textViewcity.setText(city);

        } catch (JSONException e) {
            e.printStackTrace();
        }
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
        if (id == 111) {
            final Calendar c = Calendar.getInstance();
            int hour = c.get(Calendar.HOUR_OF_DAY);
            int minute = c.get(Calendar.MINUTE);

            //Create and return a new instance of TimePickerDialog
            return new TimePickerDialog(this, myTimeListener, hour, minute,
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
                    showDate(arg1, arg2 + 1, arg3);
                }
            };

    private void showDate(int year, int month, int day) {
        dateView.setText(new StringBuilder().append(day).append("/")
                .append(month).append("/").append(year));
    }

    private TimePickerDialog.OnTimeSetListener myTimeListener = new
            TimePickerDialog.OnTimeSetListener() {
                @Override
                public void onTimeSet(TimePicker timePicker, int i, int i1) {
                    showTime(i, i1);
                }
            };

    //onTimeSet() callback method
    public void showTime(int hourOfDay, int minute) {
        //Do something with the user chosen time
        //Get reference of host activity (XML Layout File) TextView widget
        TextView tv = (TextView) findViewById(R.id.time_selected);

        String aMpM = "AM";
        if (hourOfDay > 11) {
            aMpM = "PM";
        }

        //Make the 24 hour time format to 12 hour time format
        int currentHour;
        if (hourOfDay > 11) {
            currentHour = hourOfDay - 12;
        } else {
            currentHour = hourOfDay;
        }
        String currentMinute;
        if (minute >= 10) {
            currentMinute = String.valueOf(minute);
        } else {
            currentMinute = "0" + String.valueOf(minute);
        }

        //Set a message for user
        //Display the user changed time on TextView
        tv.setText(String.valueOf(currentHour)
                + ":" + String.valueOf(currentMinute) + " " + aMpM);
    }

    private void getSubcat() {
        String URL = AppConfig.ROOT_URL + "getsubcategories.php?category=" + category;
        Log.e("URL", URL);
        final ProgressDialog loading = ProgressDialog.show(this, "Loading Data", "Please wait...", false, false);
        JsonArrayRequest jsonArrayRequest = new JsonArrayRequest(URL,
                new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray response) {
                        //Dismissing progress dialog
                        loading.dismiss();
                        parseSubCategory(response);
                        Log.e("ARRAY", response.toString());
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                                          error.printStackTrace();
                        loading.dismiss();
                    }
                });
        RequestQueue requestQueue = Volley.newRequestQueue(this);
        //Adding request to the queue
        requestQueue.add(jsonArrayRequest);
    }

    ArrayList<String> subcategories;

    private void parseSubCategory(JSONArray array) {
        JSONObject json;
        try {
            json = array.getJSONObject(0);
            subcategories = new ArrayList<>();
            JSONArray jsonArray = json.getJSONArray(AppConfig.SUBCATEGORY);
            for (int j = 0; j < jsonArray.length(); j++) {
                subcategories.add(((String) jsonArray.get(j)));
            }

            addSpinner();
        } catch (JSONException e) {
            Log.e("PARSING", e.getMessage());
        }
    }

    String subcategory;

    public void addSpinner() {
        Spinner spinner = (Spinner) findViewById(R.id.spinner);
        ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, subcategories);
        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(dataAdapter);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                subcategory = parent.getItemAtPosition(position).toString();
                Log.e("SUBAT", subcategory);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }

    public void addTimeDetails() {
        RadioButton radioButtonasap, radioButtonmanual;
        radioButtonasap = (RadioButton) findViewById(R.id.asap);
        radioButtonmanual = (RadioButton) findViewById(R.id.timemanual);
        radioButtonasap.setChecked(true);
        radioButtonasap.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                btnsetdateandtime.setEnabled(false);
            }
        });

        radioButtonmanual.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                btnsetdateandtime.setEnabled(true);
            }
        });
    }

    String estdate;
    String esttime;
    String maxprice;

    private void getData() {
        EditText editTextdescription = (EditText) findViewById(R.id.description);
        String description = editTextdescription.getText().toString();

        boolean terminate = false;
        if (TextUtils.isEmpty(description)) {
            terminate = true;
            editTextdescription.setError("Field required");
        }

        RadioGroup radioGroup = (RadioGroup) findViewById(R.id.radiogroup);
        int selected = radioGroup.getCheckedRadioButtonId();
        RadioButton radioButton = (RadioButton) findViewById(selected);

        if (radioButton != null) {
            String choice = radioButton.getText().toString();
            Log.e("TIME", choice);
            if (choice.equalsIgnoreCase("As soon as possible")) {
                estdate = dateView.getText().toString();
                esttime = "As soon as possible";
            } else {
                TextView textViewdate = (TextView) findViewById(R.id.date_selected);
                TextView textViewtime = (TextView) findViewById(R.id.time_selected);
                estdate = textViewdate.getText().toString();
                esttime = textViewtime.getText().toString();
                if (TextUtils.isEmpty(textViewtime.getText().toString())) {
                    terminate = true;
                    Toast.makeText(getApplicationContext(), "Please set the time for the request", Toast.LENGTH_LONG)
                            .show();
                }
            }
        } else {
            Toast.makeText(getApplicationContext(), "Please select the time and date", Toast.LENGTH_LONG).show();
            terminate = true;
        }


        RadioGroup radioGroupprice = (RadioGroup) findViewById(R.id.radiogroupprice);
        int selectedprice = radioGroupprice.getCheckedRadioButtonId();
        RadioButton radioButtonprice = (RadioButton) findViewById(selectedprice);
        if (radioButtonprice != null) {
            String choice = radioButtonprice.getText().toString();
            Log.e("PRICE", choice);
            if (choice.equalsIgnoreCase("Market price:")) {
                maxprice = "1111";
            } else {
                EditText editTextmaxprice = (EditText) findViewById(R.id.max_price);
                maxprice = editTextmaxprice.getText().toString().trim();
                if (TextUtils.isEmpty(maxprice)) {
                    terminate = true;
                    editTextmaxprice.setError("Field Required.");
                }
                if (!TextUtils.isDigitsOnly(maxprice)) {
                    terminate = true;
                    editTextmaxprice.setError("Please enter a valid max price.");
                }
            }
        } else {
            Toast.makeText(getApplicationContext(), "Please select maximum price", Toast.LENGTH_LONG).show();
            terminate = true;
        }
        if (!terminate) {
            showInterstitial();
            sendData(description);
        }
    }

    String request_id;

    private void sendData(final String description) {

        StringRequest stringRequest = new StringRequest(Request.Method.POST, AppConfig.CREATE_REQUEST, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                if (TextUtils.isDigitsOnly(response)) {
                    Log.e("RESPONSE", response);
                    Toast.makeText(getApplicationContext(), "Request id is:" + response, Toast.LENGTH_LONG).show();
                    request_id = response;
                    ((DataBank) getApplication()).setRequest_id(request_id);
                    proceed = true;
                } else {
                    proceed = false;
                    mInterstitialAd = new InterstitialAd(getApplicationContext());
                    loadInterstitial();
                    Toast.makeText(getApplicationContext(), response, Toast.LENGTH_LONG).show();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                error.printStackTrace();
            }
        }) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<>();
                params.put(AppConfig.USER_ID, userid);
                params.put(AppConfig.CATEGORY, category);
                params.put(AppConfig.SUBCATEGORY, subcategory);
                params.put(AppConfig.DESCRIPTION, description);
                params.put(AppConfig.ADDRESS, address);
                params.put(AppConfig.CITY, city);
                params.put(AppConfig.PINCODE, pincode);
                params.put(AppConfig.ESTDATE, estdate);
                params.put(AppConfig.ESTTIME, esttime);
                params.put(AppConfig.MAXPRICE, maxprice);
                params.put(AppConfig.LATITUDE, latitude);
                params.put(AppConfig.LONGITUDE, longitude);

                return params;
            }
        };

        RequestQueue requestQueue = Volley.newRequestQueue(this);
        requestQueue.add(stringRequest);
    }

    private void nextActivity(boolean proceed) {
        if (proceed) {
            Intent intent = new Intent(getApplicationContext(),UploadsActivity.class);
            startActivity(intent);
            finish();
        }
    }
}
