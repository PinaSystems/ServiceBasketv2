package com.pinasystems.servicebasketv2;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

public class AddressOnMapActivity extends AppCompatActivity implements OnMapReadyCallback{


    private GoogleMap mMap;

    Double latitude,longitude;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_address_on_map);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        // Load an ad into the AdMob banner view.
        AdView adView = (AdView) findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder()
                .setRequestAgent("android_studio:ad_template").build();
        adView.loadAd(adRequest);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        // Add a marker in Sydney and move the camera
        LatLng sydney = new LatLng(-34, 151);
        mMap.addMarker(new MarkerOptions().position(sydney).title("Marker in Sydney"));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));
        Intent intent = getIntent();
        latitude = intent.getDoubleExtra("latitude",-34);
        longitude = intent.getDoubleExtra("longitude",151);
        String address = intent.getStringExtra("address");
        TextView textViewaddress = (TextView) findViewById(R.id.address);
        if(address == null){
            textViewaddress.setText("No address given");
        }else{
            textViewaddress.setText(address);
        }
        Marker(latitude,longitude);
    }

    private void Marker(Double Latitude , Double Longitude){
        LatLng latLng = new LatLng(Latitude, Longitude);
        //mMap.addMarker(new MarkerOptions().position(new LatLng(Latitude, Longitude)).title("Current Location"));
        MarkerOptions options = new MarkerOptions()
                .position(latLng)
                .draggable(true)
                .title("Request Location");
        mMap.clear();
        mMap.addMarker(options);
        mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
        mMap.animateCamera(CameraUpdateFactory.zoomTo(15));
    }
}
