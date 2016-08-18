package odb234.meetpoll;


import android.Manifest;
import android.app.FragmentManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
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
import android.view.Menu;
import android.view.MenuInflater;
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
import com.google.android.gms.maps.model.LatLng;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class NewEventActivity extends AppCompatActivity {

    EditText eventName;
    static EditText locText;
    Button dateBtn;
    Button timeBtn;
    static Button locBtn;
    Spinner locSpinner;
    Spinner typeSpinner;
    LinearLayout typeLayout;
    RatingBar ratingBar;
    Button findPlacesBtn;
    SeekBar radiusSeekBar;
    TextView radius;
    int searchRadius;
    String locationType; //Saves location type to search for
    String mainLocationType;

    String dateOut;
    String timeOut;

    public static LocationManager locMan;
    static LocationListener locationListener;

    private final static String tag = "THINGS ARE HAPPENING";
    public double longitude;
    public double latitude;

    public static boolean resetGPS;

    static Geocoder gc;

    private static final String API_KEY = "AIzaSyAAzuLsfoR8fRIrdEkXC8up5KfdbHV3lno";

    protected GoogleApiClient mGoogleApiClient;

    private PlaceAutocompleteAdapter mAdapter;

    private AutoCompleteTextView mAutocompleteView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_event);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        resetGPS = false;
        eventName = (EditText) findViewById(R.id.event_name); //Event name edit text box
        locText = (EditText) findViewById(R.id.event_location); // Event location
        locBtn = (Button) findViewById(R.id.location_button); // Find location button

        InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(eventName.getWindowToken(), 0);
        imm.hideSoftInputFromWindow(locText.getWindowToken(), 1);
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
        radius = (TextView) findViewById(R.id.radius_value);
        radiusSeekBar = (SeekBar) findViewById(R.id.radius_seekbar);
        radiusSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                if (i == 0) {
                    radius.setText("Any");
                }else {
                    radius.setText(String.valueOf(i / 4.0 + " mi"));
                }
                searchRadius = (int) Math.floor((i / 4.0) * 1609);
                Log.d(tag, "Search radius in meters: " + searchRadius);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
        dateBtn = (Button) findViewById(R.id.event_date); // set date button
        timeBtn = (Button) findViewById(R.id.event_time); // set time button
        gc = new Geocoder(getApplicationContext(), Locale.ENGLISH);

        mGoogleApiClient = new GoogleApiClient
                .Builder(this)
                .addApi(Places.GEO_DATA_API)
                .addApi(Places.PLACE_DETECTION_API)
                .enableAutoManage(this, 0, null) //null maybe bad
                .build();
        mAutocompleteView = (AutoCompleteTextView)
                findViewById(R.id.event_location);
        mAutocompleteView.setOnItemClickListener(mAutocompleteClickListener);
        mAdapter = new PlaceAutocompleteAdapter(this, mGoogleApiClient, null,
                null);
        mAutocompleteView.setAdapter(mAdapter);

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
        typeSpinner = (Spinner) findViewById(R.id.type_spinner); // location type for meeting
        locSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {

                switch (position) {
                    case 0:
                        ArrayAdapter<CharSequence> typeAdapter = ArrayAdapter.createFromResource(getBaseContext(), R.array.food_types, android.R.layout.simple_spinner_item);
                        mainLocationType = "restaurant";
                        typeSpinner.setAdapter(typeAdapter);
                        locationType = getString(R.string.locataion_type_restaurant);
                        break;
                    case 1:
                        typeAdapter = ArrayAdapter.createFromResource(getBaseContext(), R.array.entertainment_types, android.R.layout.simple_spinner_item);
                        mainLocationType = "entertainment";
                        typeSpinner.setAdapter(typeAdapter);
                        locationType = getString(R.string.location_type_poi);
                        break;
                    case 2:
                        typeAdapter = ArrayAdapter.createFromResource(getBaseContext(), R.array.cultural_types, android.R.layout.simple_spinner_item);
                        mainLocationType = "cultural";
                        typeSpinner.setAdapter(typeAdapter);
                        locationType = getString(R.string.location_type_poi);
                        break;
                    case 3:
                        typeAdapter = ArrayAdapter.createFromResource(getBaseContext(), R.array.religious_types, android.R.layout.simple_spinner_item);
                        mainLocationType = "religious";
                        typeSpinner.setAdapter(typeAdapter);
                        locationType = getString(R.string.location_type_poi);
                        break;
                    case 4:
                        typeAdapter = ArrayAdapter.createFromResource(getBaseContext(), R.array.outdoor_types , android.R.layout.simple_spinner_item);
                        mainLocationType = "outdoors";
                        typeSpinner.setAdapter(typeAdapter);
                        locationType = getString(R.string.location_type_poi);
                        break;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {
                typeLayout.setVisibility(View.GONE);
            }
        });

        typeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {

            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        ratingBar = (RatingBar) findViewById(R.id.location_rating); // rating bar for location ratings

        findPlacesBtn = (Button) findViewById(R.id.find_places_btn);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                    && checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{
                        Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION
                }, 10);
            } else {
                locMan.requestLocationUpdates(LocationManager.GPS_PROVIDER, 500, 0, locationListener);
            }
        } else {
            locMan.requestLocationUpdates(LocationManager.GPS_PROVIDER, 500, 0, locationListener);
            Log.d(tag, "GPS request started");
        }

    }

    @Override
    protected void onPause() {
        super.onPause();
        locMan.removeUpdates(locationListener);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        locMan.requestLocationUpdates(LocationManager.GPS_PROVIDER, 500, 0, locationListener);
    }

    public void mapSelectLocation(View v){
        Location loc = null;
        if(gc.isPresent()) {
            loc = locMan.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            if(loc == null) {
                loc = locMan.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
            }
        }
        if(loc == null){
            Toast.makeText(getApplicationContext(), "No location found.", Toast.LENGTH_LONG).show();
            return;
        }
        double lat = loc.getLatitude();
        double lng = loc.getLongitude();
        Intent intent = new Intent(NewEventActivity.this, LocationSelectMap.class);
        intent.putExtra("lat", lat);
        intent.putExtra("lng", lng);
        startActivityForResult(intent, 12);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == resultCode) {
            LatLng latLng = new LatLng(data.getDoubleExtra("lat", 0.0), data.getDoubleExtra("lng", 0.0));
            Address address = null;
            try {
                if(gc.isPresent()) {
                    if (!gc.getFromLocation(latLng.latitude, latLng.longitude, 1).get(0).equals(""))
                        address = gc.getFromLocation(latLng.latitude, latLng.longitude, 1).get(0);
                }
            } catch (IOException e) {
                Log.d(tag, "Gecoding failed");
            }
            locText.setText(address.getAddressLine(0) + ", " + address.getAddressLine(1));
            longitude = latLng.longitude;
            latitude = latLng.latitude;
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
    public void hide_keyboard(View v)
    {
        InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
    }

    private AdapterView.OnItemClickListener mAutocompleteClickListener
            = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
                if(checkSelfPermission(Manifest.permission.INTERNET) != PackageManager.PERMISSION_GRANTED){
                    requestPermissions(new String[]{Manifest.permission.INTERNET}, 5);
                    return;
                }
            }
            final AutocompletePrediction item = mAdapter.getItem(position);
            final String placeId = item.getPlaceId();
            PendingResult<PlaceBuffer> placeResult = null;
            if(gc.isPresent()) {
                placeResult = Places.GeoDataApi.getPlaceById(mGoogleApiClient, placeId);
                placeResult.setResultCallback(mUpdatePlaceDetailsCallback);
            }

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

            places.release();
        }
    };

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case 10:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[1] == PackageManager.PERMISSION_GRANTED)
                   locMan.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);
        }
    }

    public void resetLocation() {

        Location location = locMan.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        if(location == null)
            location = locMan.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
        if(location == null) {
            Toast.makeText(getApplicationContext(), "Location Service not available", Toast.LENGTH_SHORT).show();
            return;
        }
        Address address = new Address(Locale.ENGLISH);
        try {
            if(gc.isPresent()) {
                if (!gc.getFromLocation(location.getLatitude(), location.getLongitude(), 5).get(0).equals(""))
                    address = gc.getFromLocation(location.getLatitude(), location.getLongitude(), 1).get(0);
            }
        } catch (IOException e) {
            Log.d(tag, "Gecoding failed");
        }
        locText.setText(address.getAddressLine(0) + ", " + address.getAddressLine(1));
        longitude = location.getLongitude();
        latitude = location.getLatitude();

    }



    public boolean checkDate(){
        final Calendar calendar = java.util.Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH) + 1;
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        Log.d(tag, month + " " + day + " " + year);
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        int min = calendar.get(Calendar.MINUTE);

        String t = "AM";
        if(hour > 12) {
            t = "PM";
            hour = hour%12;
        }
        Log.d(tag, hour + ":" + min + " " + t);

        String dateBtnTxt = dateBtn.getText().toString();
        String timeBtnTxt = timeBtn.getText().toString();

        String[] dateSplit = dateBtnTxt.split("/");
        Log.d(tag, Arrays.deepToString(dateSplit));
        if(Integer.parseInt(dateSplit[2]) < year)
            return false;
        if(Integer.parseInt(dateSplit[0]) < month && Integer.parseInt(dateSplit[2]) == year)
            return false;
        if(Integer.parseInt(dateSplit[2]) == year && Integer.parseInt(dateSplit[0]) == month && Integer.parseInt(dateSplit[1]) < day)
            return false;

        String[] timeSplit = timeBtnTxt.split(":|\\s+");
        Log.d(tag, Arrays.deepToString(timeSplit));
        if(Integer.parseInt(dateSplit[2]) == year && Integer.parseInt(dateSplit[0]) == month && Integer.parseInt(dateSplit[1]) == day) {
            if (t.equals("PM") && timeSplit[2].equals("AM"))
                return false;
            if(t.equals(timeSplit[2]) && Integer.parseInt(timeSplit[0]) < hour)
                return false;
            if(t.equals(timeSplit[2]) && Integer.parseInt(timeSplit[0]) == hour && Integer.parseInt(timeSplit[1]) < min)
                return false;
        }

        return true;
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

        if(!checkDate()) {
            Toast.makeText(getApplicationContext(), "Chosen time or date is invalid.", Toast.LENGTH_LONG).show();
            return;
        }


        Log.d(tag, "Before try: " + latitude + ", " + longitude);
        Log.d(tag, locText.getText().toString());

        if( latitude == 0.0 || longitude == 0.0 ){
            List<Address> tempAdd = null;
            try {
                while(gc.isPresent()) {
                    tempAdd = gc.getFromLocationName(locText.getText().toString(), 5);
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
        
        Intent intent = new Intent(this, TabbedSearchResultsActivity.class);
        intent.putExtra("newLat", latitude);
        intent.putExtra("newLng", longitude);
        intent.putExtra("eventName", eventName.getText().toString());
        intent.putExtra("eventLocation", locText.getText().toString());
        if(searchRadius > 0) {
            intent.putExtra("radius", searchRadius);
        }
        intent.putExtra("dateTime", dateOut + " " + timeOut);
        intent.putExtra("locationType", locationType);
        intent.putExtra("locationSubtype", typeSpinner.getSelectedItem().toString());
        intent.putExtra("rating", Math.floor(ratingBar.getRating()));
        intent.putExtra("mainLocationType", mainLocationType);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, 10);
            Log.d(tag, "permissions not granted");
            return;
        }
        locMan.removeUpdates(locationListener);
        startActivity(intent);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu){
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu,menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch(item.getItemId()){
            case android.R.id.home:
                finish();
                return true;
            case R.id.settings:
                Intent intent2 = new Intent(NewEventActivity.this,SettingsActivity.class);
                startActivity(intent2);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void autoCompleteUpdateLocation(){
        List<Address> tempAdd = null;
        try {
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
