package odb234.meetpoll;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MenuItem;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

public class SearchResultsActivity extends AppCompatActivity implements OnMapReadyCallback {

    private static final String TAG = "Map activity";
    private GoogleMap mMap;
    private double newLat;
    private double newLng;
    Geocoder gc;
    Intent intent;
    //Current location info
    private String address;
    private String city;
    private String country;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        setContentView(R.layout.activity_search_results);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        intent = getIntent();
        newLat = intent.getDoubleExtra("newLat", 30);
        newLng = intent.getDoubleExtra("newLng", -97);

        try{
            getLocation();
        }catch(IOException e){
            Log.d(TAG, "get location failed");
        }

    }


    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        // Add a marker in Sydney and move the camera
        LatLng youAreHere = new LatLng(newLat, newLng);
        mMap.addMarker(new MarkerOptions().position(youAreHere).title(address + ", " + city));
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(youAreHere, 17));
    }

    public void getLocation() throws IOException{
        gc = new Geocoder(this);
        List<Address> list = gc.getFromLocation(newLat, newLng, 1);

        try{
            address = list.get(0).getAddressLine(0);
            city = list.get(0).getAddressLine(1);
            country = list.get(0).getAddressLine(2);
        }catch(IllegalArgumentException a){
            Log.d(TAG, "illegal argument");
        }
        Log.d(TAG, Arrays.deepToString(list.toArray()));
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId() == android.R.id.home){
            finish();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
