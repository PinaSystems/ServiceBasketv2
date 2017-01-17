package com.pinasystems.servicebasketv2;

import android.Manifest;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class AddressMapsActivity extends FragmentActivity implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        LocationListener,
        OnMapReadyCallback {

    private double Latitude;
    private double Longitude;
    private static final String TAG = AddressMapsActivity.class.getCanonicalName();
    private final static int CONNECTION_FAILURE_RESOLUTION_REQUEST = 9000;
    private final static int PLAY_SERVICES_RESOLUTION_REQUEST = 1000;
    private String myAddress;
    private String lat;
    private String lang;
    private Button buttonNext;
    private ImageView imageButtonSearch;
    private GoogleMap mMap;
    private EditText editTextSearch;
    private GoogleApiClient mGoogleApiClient;
    private LocationRequest mLocationRequest;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_address_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        if (checkPlayServices()) {

            // Building the GoogleApi client
            buildGoogleApiClient();
        } else {
            Log.e(TAG, "no play services found");
        }
        // Create the LocationRequest object
        mLocationRequest = LocationRequest.create()
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                .setInterval(5 * 1000)        // 10 seconds, in milliseconds
                .setFastestInterval(1 * 1000); // 1 second, in milliseconds
        imageButtonSearch = (ImageView) findViewById(R.id.searchbutton);
        imageButtonSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onSearch();
            }
        });

        Button btnproceedaddress = (Button) findViewById(R.id.proceedaddress);
        btnproceedaddress.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                NextActivity();
            }
        });

        // Load an ad into the AdMob banner view.
        AdView adView = (AdView) findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder()
                .setRequestAgent("android_studio:ad_template").build();
        adView.loadAd(adRequest);
    }

    protected synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API).build();
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        // Add a marker in Sydney and move the camera
        LatLng mumbai = new LatLng(19.0760, 72.8777);
        mMap.addMarker(new MarkerOptions()
                .position(mumbai)
                .title("Drag and drop to the location"));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(mumbai));
        mMap.animateCamera(CameraUpdateFactory.zoomTo(15));
        start();
    }

    private boolean checkPlayServices() {
        int resultCode = GooglePlayServicesUtil
                .isGooglePlayServicesAvailable(this);
        if (resultCode != ConnectionResult.SUCCESS) {
            if (GooglePlayServicesUtil.isUserRecoverableError(resultCode)) {
                GooglePlayServicesUtil.getErrorDialog(resultCode, this,
                        PLAY_SERVICES_RESOLUTION_REQUEST).show();
            } else {
                Toast.makeText(getApplicationContext(),
                        "This device is not supported.", Toast.LENGTH_LONG)
                        .show();
                finish();
            }
            return false;
        }
        return true;
    }

    private void start() {
        mGoogleApiClient.connect();
        if (mGoogleApiClient.isConnected()) {
            StartLocationUpdates();
            Toast.makeText(getApplicationContext(), "Getting your location", Toast.LENGTH_LONG).show();
        } else {
            Log.e(TAG, "apiclient not connected");
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        mGoogleApiClient.connect();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    protected void onPause() {
        super.onPause();

        if (mGoogleApiClient.isConnected()) {
            StopLocationUpdates();
            mGoogleApiClient.disconnect();
        }
    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.e(TAG, String.valueOf(i));
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        /*
         * Google Play services can resolve some errors it detects.
         * If the error has a resolution, try sending an Intent to
         * start a Google Play services activity that can resolve
         * error.
         */
        Log.e(TAG, connectionResult.getErrorMessage());
        if (connectionResult.hasResolution()) {
            try {
                // Start an Activity that tries to resolve the error
                connectionResult.startResolutionForResult(this, CONNECTION_FAILURE_RESOLUTION_REQUEST);
                /*
                 * Thrown if Google Play services canceled the original
                 * PendingIntent
                 */
            } catch (IntentSender.SendIntentException e) {
                // Log the error
                e.printStackTrace();
            }
        } else {
            /*
             * If no resolution is available, display a dialog to the
             * user with the error.
             */
            Log.i(TAG, "Location services connection failed with code " + connectionResult.getErrorCode());
        }
    }

    @Override
    public void onConnected(Bundle bundle) {
        Log.e(TAG, "Connected");
        StartLocationUpdates();
    }

    private final int MY_PERMISSIONS_REQUEST_READ_LOCATION = 1515;

    protected void StartLocationUpdates() {
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            Log.e(TAG,"Permission not granted");
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},
                    MY_PERMISSIONS_REQUEST_READ_LOCATION);
            return;
        }
        Location location = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        if (location == null) {
            Log.e(TAG,"location null");
            LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient,mLocationRequest,this);
        } else {
            Log.e(TAG, location.toString());
            handleNewLocation(location);
            StopLocationUpdates();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_READ_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    start();
                    // permission was granted, yay! Do the
                    // contacts-related task you need to do.

                } else {

                    Toast.makeText(getApplicationContext(),"Permission not granted to access location",Toast.LENGTH_LONG).show();
                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                }
                return;
            }

            // other 'case' lines to check for other
            // permissions this app might request
        }
    }

    @Override
    public void onLocationChanged(Location location) {
        handleNewLocation(location);
    }

    protected  void StopLocationUpdates(){
        LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient,this);
    }

    private void handleNewLocation(Location location) {
        Latitude = location.getLatitude();
        Log.e(TAG,String.valueOf(Latitude));
        Longitude = location.getLongitude();
        Log.e(TAG,String.valueOf(Longitude));
        Marker();
    }
    private void Marker(){
        LatLng latLng = new LatLng(Latitude, Longitude);
        //mMap.addMarker(new MarkerOptions().position(new LatLng(Latitude, Longitude)).title("Current Location"));
        MarkerOptions options = new MarkerOptions()
                .position(latLng)
                .draggable(true)
                .title("Drag and drop the pin to the location");
        mMap.clear();
        mMap.addMarker(options);
        mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
        mMap.animateCamera(CameraUpdateFactory.zoomTo(15));
        mMap.setOnMarkerDragListener(new GoogleMap.OnMarkerDragListener() {
            @Override
            public void onMarkerDragStart(Marker marker) {
                StopLocationUpdates();
            }

            @Override
            public void onMarkerDrag(Marker marker) {

            }

            @Override
            public void onMarkerDragEnd(Marker marker) {
                LatLng position = marker.getPosition();
                Latitude= position.latitude;
                Longitude = position.longitude;
            }
        });

    }

    String pincode , city;
    public void onSearch() {
        editTextSearch= (EditText) findViewById(R.id.search);
        String location = editTextSearch.getText().toString();

        if (TextUtils.isEmpty(location)) {
            editTextSearch.setError("Field required to search");
        } else {
            List<Address> addressList = null;
            Geocoder geocoder = new Geocoder(this);
            try {
                addressList = geocoder.getFromLocationName(location, 1);
            } catch (IOException e) {
                e.printStackTrace();
            }
            Address address;
            if (addressList != null) {
                address = addressList.get(0);
                Latitude = address.getLatitude();
                Longitude = address.getLongitude();
                pincode = address.getPostalCode();
                if(TextUtils.isEmpty(pincode)){
                    pincode = "enter here";
                }
                city = address.getLocality();
                if(TextUtils.isEmpty(city)){
                    city = address.getSubLocality();
                }
                Marker();
            }
        }
    }

    public void NextActivity() {
        //Geocoder define
        Geocoder geocoder = new Geocoder(this, Locale.ENGLISH);
        lat = String.valueOf(Latitude);

        // Get longitude of the current location
        lang = String.valueOf(Longitude);
        try {
            //Place your latitude and longitude
            List<Address> addresses = geocoder.getFromLocation(Latitude,Longitude, 1);
            if (addresses != null) {
                Address fetchedAddress = addresses.get(0);
                StringBuilder strAddress = new StringBuilder();
                for (int i = 0; i < fetchedAddress.getMaxAddressLineIndex(); i++) {
                    strAddress.append(fetchedAddress.getAddressLine(i)).append("\n");
                }
                myAddress = strAddress.toString();
                Intent intent = new Intent(getApplicationContext(), GetAddressDetailsActivity.class);
                ((DataBank)getApplication()).setAddress(myAddress);
                ((DataBank)getApplication()).setLat(lat);
                ((DataBank)getApplication()).setLong(lang);
                intent.putExtra("city",city);
                intent.putExtra("pincode",pincode);
                Log.e("LATITUDE",lat);
                Log.e("LONGITUDE",lang);
                startActivity(intent);
            } else
                myAddress = "No location found..!";
            Toast.makeText(getApplicationContext(),myAddress,Toast.LENGTH_LONG).show();

        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            Toast.makeText(getApplicationContext(), "Could not get address..!", Toast.LENGTH_LONG).show();
        }
    }
}
