package odb234.meetpoll;


import android.Manifest;
import android.app.FragmentManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.icu.util.Calendar;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Build;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.app.AppCompatDialog;
import android.util.Log;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.location.places.AutocompletePrediction;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.PlaceBuffer;
import com.google.android.gms.location.places.Places;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class NewEventActivity extends AppCompatActivity {

    EditText eventName;
    static EditText locText;
    SeekBar radiusSeekBar;
    Button dateBtn;
    Button timeBtn;
    static Button locBtn;
    Spinner locSpinner;
    Spinner typeSpinner;
    LinearLayout typeLayout;
    LinearLayout priceLayout;
    Spinner priceSpinner;
    DatePicker eventDate;
    RatingBar ratingBar;
    Button findPlacesBtn;
    int price;

    public static LocationManager locMan;
    static LocationListener locationListener;

    private final static String tag = "THINGS ARE HAPPENING";
    public double longitude;
    public double latitude;

    public static boolean resetGPS;

    static Geocoder gc;

    private static final String API_KEY = "AIzaSyAAzuLsfoR8fRIrdEkXC8up5KfdbHV3lno";
    private String[] places;
    private ArrayList<MyPlace> resultPlaces;

    protected GoogleApiClient mGoogleApiClient;

    private PlaceAutocompleteAdapter mAdapter;

    private AutoCompleteTextView mAutocompleteView;

    private TextView mPlaceDetailsText;

    private TextView mPlaceDetailsAttribution;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_event);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        resetGPS = false;
        eventName = (EditText) findViewById(R.id.event_name); //Event name edit text box
        locText = (EditText) findViewById(R.id.event_location); // Event location
        locBtn = (Button) findViewById(R.id.location_button); // Find location button

        locBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //dialog call
                Log.d(tag, locText.getText().toString());
                if (!locText.getText().toString().equals("")) {
                    FragmentManager fm = getFragmentManager();
                    LocationDialogFragment ld = new LocationDialogFragment();
                    ld.show(fm, "resetLocation");
                } else
                    resetLocation();
            }
        });


        locMan = (LocationManager) getSystemService(LOCATION_SERVICE);
        dateBtn = (Button) findViewById(R.id.event_date); // set date button
        timeBtn = (Button) findViewById(R.id.event_time); // set time button
        gc = new Geocoder(getApplicationContext(), Locale.ENGLISH);

        mGoogleApiClient = new GoogleApiClient
                .Builder(this)
                .addApi(Places.GEO_DATA_API)
                .addApi(Places.PLACE_DETECTION_API)
                .enableAutoManage(this,0,null) //null maybe bad
                .build();
        mAutocompleteView = (AutoCompleteTextView)
                findViewById(R.id.event_location);
        mAutocompleteView.setOnItemClickListener(mAutocompleteClickListener);
        mAdapter = new PlaceAutocompleteAdapter(this, mGoogleApiClient, null,
                null);
        mAutocompleteView.setAdapter(mAdapter);

//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
//            if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
//                    && checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
//                requestPermissions(new String[]{
//                        Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION
//                }, 11);
//                recreate();
//                return;
//            }
//        }
        locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                Log.d(tag, location.getLatitude() + ", " + location.getLongitude());
            }

            @Override
            public void onStatusChanged(String s, int i, Bundle bundle) {

            }

            @Override
            public void onProviderEnabled(String s) {

            }

            @Override
            public void onProviderDisabled(String s) {
                Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                startActivity(intent);
            }
        };


        locSpinner = (Spinner) findViewById(R.id.location_spinner); // spinner for location type
        ArrayAdapter<CharSequence> locAdapter = ArrayAdapter.createFromResource(this, R.array.location_list, android.R.layout.simple_spinner_dropdown_item);
        locAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        locSpinner.setAdapter(locAdapter); // selection for category of meeting location

        typeLayout = (LinearLayout) findViewById(R.id.type_layout);
        priceLayout = (LinearLayout) findViewById(R.id.price_layout);
        typeSpinner = (Spinner) findViewById(R.id.type_spinner); // location type for meeting
        locSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {

                switch (position) {
                    case 0:
                        ArrayAdapter<CharSequence> typeAdapter = ArrayAdapter.createFromResource(getBaseContext(), R.array.food_types, android.R.layout.simple_spinner_item);
                        typeSpinner.setAdapter(typeAdapter);
                        break;
                    case 1:
                        typeAdapter = ArrayAdapter.createFromResource(getBaseContext(), R.array.entertainment_types, android.R.layout.simple_spinner_item);
                        typeSpinner.setAdapter(typeAdapter);
                        break;
                    case 2:
                        typeAdapter = ArrayAdapter.createFromResource(getBaseContext(), R.array.cultural_types, android.R.layout.simple_spinner_item);
                        typeSpinner.setAdapter(typeAdapter);
                        break;
                    case 3:
                        typeAdapter = ArrayAdapter.createFromResource(getBaseContext(), R.array.outdoors_types, android.R.layout.simple_spinner_item);
                        typeSpinner.setAdapter(typeAdapter);
                        break;
                    case 4:
                        typeAdapter = ArrayAdapter.createFromResource(getBaseContext(), R.array.religious_types, android.R.layout.simple_spinner_item);
                        typeSpinner.setAdapter(typeAdapter);
                        break;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {
                typeLayout.setVisibility(View.GONE);
            }
        });

        priceSpinner = (Spinner) findViewById(R.id.price_spinner); // Price spinner showing price '$' value
        priceSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener(){
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                price = i;
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
                price = 0;
            }
        });
        ArrayAdapter<CharSequence> priceAdapter = ArrayAdapter.createFromResource(this, R.array.price_list, android.R.layout.simple_spinner_dropdown_item);
        priceSpinner.setAdapter(priceAdapter);

        ratingBar = (RatingBar) findViewById(R.id.location_rating); // rating bar for location ratings

        findPlacesBtn = (Button) findViewById(R.id.find_places_btn);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                    && checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{
                        Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION
                }, 11);
//                recreate();
                return;
            }
        }

    }

    public void showTimePickerDialog(View v) {
        TimePickerFragment newFragment = TimePickerFragment.newInstance(v);
        newFragment.show(getFragmentManager(), "timePicker");
    }

    public void showDatePickerDialog(View v) {
        DatePickerFragment newFragment = DatePickerFragment.newInstance(v);
        newFragment.show(getFragmentManager(), "datePicker");
    }

    private AdapterView.OnItemClickListener mAutocompleteClickListener
            = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

            final AutocompletePrediction item = mAdapter.getItem(position);
            final String placeId = item.getPlaceId();
            final CharSequence primaryText = item.getPrimaryText(null);

            PendingResult<PlaceBuffer> placeResult = Places.GeoDataApi
                    .getPlaceById(mGoogleApiClient, placeId);
            placeResult.setResultCallback(mUpdatePlaceDetailsCallback);

//            Toast.makeText(getApplicationContext(), "Clicked: " + primaryText,
//                    Toast.LENGTH_SHORT).show();
            Log.d(tag, placeResult.toString());
            autoCompleteUpdateLocation();
        }
    };

    private ResultCallback<PlaceBuffer> mUpdatePlaceDetailsCallback
            = new ResultCallback<PlaceBuffer>() {
        @Override
        public void onResult(PlaceBuffer places) {
            if (!places.getStatus().isSuccess()) {
                places.release();
                return;
            }

            final Place place = places.get(0);
           /* Place[] results = new Place[10];
            for (int i=0;i<10;i++){
                results[i]=places.get(i);
            }*/

            places.release();
        }
    };

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case 10:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                                && checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                                checkSelfPermission(Manifest.permission.INTERNET) != PackageManager.PERMISSION_GRANTED) {
                            requestPermissions(new String[]{
                                    Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.INTERNET
                            }, 10);
                            return;
                        }
                    }
                    locMan.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, locationListener);
//                    gpsAlert();
                return;
        }
    }

    public void resetLocation() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                    || checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{
                        Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION
                }, 10);
                return;
            }
        }
            Location location = locMan.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            Address address = new Address(Locale.ENGLISH);
            try {
                if(!gc.getFromLocation(location.getLatitude(), location.getLongitude(), 1).get(0).equals(""))
                    address = gc.getFromLocation(location.getLatitude(), location.getLongitude(), 1).get(0);
            } catch (IOException e) {
                Log.d(tag, "Gecoding failed");
            }
            locText.setText(address.getAddressLine(0) + ", " + address.getAddressLine(1));
            longitude = location.getLongitude();
            latitude = location.getLatitude();
            Log.d(tag, "Before recreate: " + latitude + ", " + longitude);
            this.recreate();

    }

    public void saveEvent(View v) {
        String str = "";
        if(eventName.getText().toString().equals(""))
            str += "Event Name, ";
        if(locText.getText().toString().equals(""))
            str += "Search Location, ";
        if(dateBtn.getText().toString().equals(""))
            str += "Event Date, ";
        if(timeBtn.getText().toString().equals(""))
            str += "Event Time.";
        if(!str.equals("")) {
            Toast.makeText(getApplicationContext(), "Missing information for " + str, Toast.LENGTH_LONG).show();
            return;
        }

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
            if(checkSelfPermission(Manifest.permission.INTERNET) != PackageManager.PERMISSION_GRANTED){
                requestPermissions(new String[]{Manifest.permission.INTERNET}, 5);
                return;
            }
        }
        //---------------------------------------------------------------
//        new GetPlaces().execute();
        //---------------------------------------------------------------

        Log.d(tag, "Before try: " + latitude + ", " + longitude);
        Log.d(tag, locText.getText().toString());

        if( latitude == 0.0 || longitude == 0.0 ){
            List<Address> tempAdd = null;
            try {
                Log.d(tag, "Inside try start");
                while(gc.isPresent()) {
                    tempAdd = gc.getFromLocationName(locText.getText().toString(), 5);
                    Log.d(tag, "Inside Try: " + tempAdd.get(0).toString());
                    if(tempAdd.size() > 0 && tempAdd != null) {
                        Address a = tempAdd.get(0);
                        latitude = a.getLatitude();
                        longitude = a.getLongitude();
                        break;
                    }
                }
            }catch(Exception e){
                Log.e(tag, "Catch: " + e);
            }
        }
        
        Log.d(tag, "After try: " + latitude + ", " + longitude);
        Intent intent = new Intent(this, SearchResultsActivity.class);
        intent.putExtra("newLat", latitude);
        intent.putExtra("newLng", longitude);
        intent.putExtra("eventName", eventName.getText().toString());
        intent.putExtra("eventLocation", locText.getText().toString());
        intent.putExtra("date", dateBtn.getText().toString());
        intent.putExtra("time", timeBtn.getText().toString());
        intent.putExtra("locationType", locSpinner.getSelectedItem().toString());
        intent.putExtra("locationSubtype", typeSpinner.getSelectedItem().toString());
        intent.putExtra("price", price);
        intent.putExtra("rating", (int) Math.floor(ratingBar.getRating()));

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, 15);
            Log.d(tag, "permissions not granted");
            return;
        }
        locMan.removeUpdates(locationListener);
        startActivity(intent);

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId() == android.R.id.home){
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }


    private class GetPlaces extends AsyncTask<Void, com.google.android.gms.location.places.Place, Void> {


        @Override
        protected Void doInBackground(Void... arg0) {
            resultPlaces = new ArrayList<>();
            PlacesService service = new PlacesService(API_KEY);
            try {
                resultPlaces = service.findPlaces(longitude, latitude);
            } catch (JSONException e) {
                Log.d(tag, "json fail :/");
            }


            for (int i = 0; i < resultPlaces.size(); i++) {

                MyPlace placeDetail = resultPlaces.get(i);
                Log.e(tag, "places : " + placeDetail.getName());
            }
            return null;
        }
    }

    private void autoCompleteUpdateLocation(){
        List<Address> tempAdd = null;
        try {
            Log.d(tag, "Updating autocomplete location");
            if(gc.isPresent()) {
                tempAdd = gc.getFromLocationName(locText.getText().toString(), 1);
                Address a = tempAdd.get(0);
                latitude = a.getLatitude();
                longitude = a.getLongitude();
                Log.d(tag, "New LatLng: " + latitude + ", " + longitude);
            }
        }catch(Exception e){
            Log.e(tag, "Catch: " + e);
        }
    }

    public void newEventClearText(View v) {
        locText.getText().clear();
    }

}
