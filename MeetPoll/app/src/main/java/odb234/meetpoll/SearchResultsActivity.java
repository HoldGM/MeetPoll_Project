package odb234.meetpoll;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Address;
import android.location.Geocoder;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.location.places.ui.PlacePicker;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class SearchResultsActivity extends AppCompatActivity implements OnMapReadyCallback, GoogleApiClient.OnConnectionFailedListener {

    private static final String TAG = "Map activity";
    private GoogleMap mMap;
    Geocoder gc;
    Intent intent;
    //Current location info
    private String address;
    private String city;
    private String country;
    //------------------------------------------
    private double newLat;
    private double newLng;
    private String eventName;
    private String searchLoaction;
    private String eventDate;
    private String eventTime;
    private String locationType;
    private String locationSubtype;
    private String eventPrice;
    private int eventRating;

    private static final String API_KEY = "AIzaSyAAzuLsfoR8fRIrdEkXC8up5KfdbHV3lno";


//    int PLACE_PICKER_REQUEST = 1;
    private GoogleApiClient mGoogleApiClient;
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
        eventName = intent.getStringExtra("eventName");
        searchLoaction = intent.getStringExtra("eventLocation");
        eventDate = intent.getStringExtra("date");
        eventTime = intent.getStringExtra("time");
        locationType = intent.getStringExtra("locationType");
        locationSubtype = intent.getStringExtra("locationSubtype");
        eventPrice = intent.getStringExtra("price");
        eventRating = intent.getIntExtra("rating", 0);


        try{
            getLocation();
        }catch(IOException e){
            Log.d(TAG, "get location failed");
        }

        mGoogleApiClient = new GoogleApiClient
                .Builder(this)
                .addApi(Places.GEO_DATA_API)
                .addApi(Places.PLACE_DETECTION_API)
                .enableAutoManage(this, this)
                .build();
//-----------------Place Picker, may want to use at some point----------------------------------
//        PlacePicker.IntentBuilder builder = new PlacePicker.IntentBuilder();
//
//        try {
//            startActivityForResult(builder.build(this), PLACE_PICKER_REQUEST);
//        } catch (GooglePlayServicesRepairableException e) {
//            e.printStackTrace();
//        } catch (GooglePlayServicesNotAvailableException e) {
//            e.printStackTrace();
//        }
    }

//    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
//        if (requestCode == PLACE_PICKER_REQUEST) {
//            if (resultCode == RESULT_OK) {
//                Place place = PlacePicker.getPlace(data, this);
//                String toastMsg = String.format("Place: %s", place.getName());
//                Toast.makeText(this, toastMsg, Toast.LENGTH_LONG).show();
//            }
//        }
//    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {

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

        Log.d(TAG, newLat + ", " + newLng);
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

        switch(item.getItemId()){
            case android.R.id.home:
                finish();
                return true;
            case R.id.send_event:
                SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
                String hostName = sp.getString("name", "host");
                DatabaseConnector dbc = new DatabaseConnector(this);
                dbc.insertEvent(hostName, eventName, searchLoaction, eventDate, eventTime, locationType, locationSubtype, eventPrice, eventRating);
                Log.d(TAG, "Event inserted into DB");
                Intent intent = new Intent(SearchResultsActivity.this, MainActivity.class);
                startActivity(intent);
                return true;
            case R.id.settings:
                Intent intent2 = new Intent(SearchResultsActivity.this,SettingsActivity.class);
                startActivity(intent2);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu){
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.map_menu,menu);
        return true;
    }


}
