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
import android.os.Build;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;

import java.io.IOException;
import java.util.Locale;

public class NewEventActivity extends AppCompatActivity {

    EditText eventName;
    TextView radius;
    static EditText locText;
    SeekBar radiusSeekBar;
    Button dateBtn;
    Button timeBtn;
    static Button locBtn;
    Spinner locSpinner;
    Spinner typeSpinner;
    LinearLayout typeLayout;
    Spinner priceSpinner;
    DatePicker eventDate;
    RatingBar ratingBar;
    Button findPlacesBtn;

    //Database Access variables
    private long row_id;
    private String db_eventName;
    private String db_eventLocation;
    private int db_radius;
    private String db_date;
    private String db_time;
    private String db_locationType;
    private String db_locationSubtype;
    private String db_price;
    private int db_rating;


    public static LocationManager locMan;
    static LocationListener locationListener;

    private final static String tag = "THINGS ARE HAPPENING";
    public static double longitude;
    public static double latitude;

    public static boolean resetGPS;

    static Geocoder gc;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_event);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        resetGPS = false;
        eventName = (EditText) findViewById(R.id.event_name); //Event name edit text box
        locText = (EditText) findViewById(R.id.event_location); // Event location
        locBtn = (Button) findViewById(R.id.location_button); // Find location button
        locMan = (LocationManager) getSystemService(LOCATION_SERVICE);
        dateBtn = (Button) findViewById(R.id.event_date); // set date button
        timeBtn = (Button) findViewById(R.id.event_time); // set time button
        gc = new Geocoder(this);

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

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                    && checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{
                        Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION
                }, 10);
                return;
            }else{
                gpsAlert();
            }
        }else{
            gpsAlert();
        }

        radius= (TextView) findViewById(R.id.radius_value); // radius value,
        radiusSeekBar = (SeekBar) findViewById(R.id.radius_seekbar); // radius seekbar slider
        radiusSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                radius.setText(String.valueOf(i + " mi"));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        locSpinner = (Spinner) findViewById(R.id.location_spinner); // spinner for location type
        ArrayAdapter<CharSequence> locAdapter = ArrayAdapter.createFromResource(this, R.array.location_list, android.R.layout.simple_spinner_dropdown_item);
        locAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        locSpinner.setAdapter(locAdapter);

        typeLayout = (LinearLayout) findViewById(R.id.type_layout);
        typeSpinner = (Spinner) findViewById(R.id.type_spinner); // location for theme type for location selection
        locSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener(){
           @Override
           public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id){

               switch(position){
                   case 0:
                       ArrayAdapter<CharSequence> typeAdapter = ArrayAdapter.createFromResource(getBaseContext(), R.array.restaurant_types, android.R.layout.simple_spinner_item);
                       typeSpinner.setAdapter(typeAdapter);
                       typeLayout.setVisibility(View.VISIBLE);
                       break;
                   case 2:
                       typeAdapter = ArrayAdapter.createFromResource(getBaseContext(), R.array.bar_types, android.R.layout.simple_spinner_item);
                       typeSpinner.setAdapter(typeAdapter);
                       typeLayout.setVisibility(View.VISIBLE);
                       break;
                   case 3:
                       typeAdapter = ArrayAdapter.createFromResource(getBaseContext(), R.array.club_types, android.R.layout.simple_spinner_item);
                       typeSpinner.setAdapter(typeAdapter);
                       typeLayout.setVisibility(View.VISIBLE);
                       break;
                   default:
                       typeLayout.setVisibility(View.GONE);
               }
           }
            @Override
            public void onNothingSelected(AdapterView<?> parentView){
                typeLayout.setVisibility(View.GONE);
            }
        });

        priceSpinner = (Spinner) findViewById(R.id.price_spinner); // Price spinner showing price '$' value
        ArrayAdapter<CharSequence> priceAdapter = ArrayAdapter.createFromResource(this, R.array.price_list, android.R.layout.simple_spinner_dropdown_item);
        priceSpinner.setAdapter(priceAdapter);

        ratingBar = (RatingBar)findViewById(R.id.location_rating); // rating bar for location ratings
        findPlacesBtn = (Button)findViewById(R.id.find_places_btn);

    }

    public void showTimePickerDialog(View v) {
        TimePickerFragment newFragment = TimePickerFragment.newInstance(v);
        newFragment.show(getFragmentManager(),"timePicker");
    }
    public void showDatePickerDialog(View v) {
        DatePickerFragment newFragment = DatePickerFragment.newInstance(v);
        newFragment.show(getFragmentManager(), "datePicker");
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch(requestCode){
            case 10:
                if(grantResults.length>0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)
                    gpsAlert();
                return;
        }
    }

    public void gpsAlert() {
        locBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //dialog call
                FragmentManager fm = getFragmentManager();
                LocationDialogFragment ld = new LocationDialogFragment();
                ld.show(fm, "resetLocation");
            }
        });
    }
    public static void resetLocation(){
        if (resetGPS) {
//            locMan.requestLocationUpdates("gps", 50000, 25, locationListener);
            Location location = locMan.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            Address address = new Address(Locale.ENGLISH);
            try {
                address = gc.getFromLocation(location.getLatitude(), location.getLongitude(), 1).get(0);
            }catch (IOException e){
                Log.d(tag, "Gecoding failed");
            }
            locText.setText(address.getAddressLine(0) + ", " + address.getAddressLine(1));
            longitude = location.getLongitude();
            latitude = location.getLatitude();


        }

    }

    public void saveEvent(View v){
        DatabaseConnector dbc = new DatabaseConnector(this);

        Log.d(tag, "Event inserted into DB");

        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
        String hostName = sp.getString("name", "host");

        db_eventName = eventName.getText().toString();
        db_eventLocation = locText.getText().toString();
        db_radius = radiusSeekBar.getProgress();
        db_date = dateBtn.getText().toString();
        db_time = timeBtn.getText().toString();
        db_locationType = locSpinner.getSelectedItem().toString();
        db_locationSubtype = typeSpinner.getSelectedItem().toString();
        db_price = priceSpinner.getSelectedItem().toString();
        db_rating = (int)Math.floor(ratingBar.getRating());


        dbc.insertEvent(hostName, db_eventName, db_eventLocation, db_radius, db_date, db_time, db_locationType, db_locationSubtype, db_price, db_rating);

        Intent intent = new Intent(this, SearchResultsActivity.class);
        intent.putExtra("newLat", latitude);
        intent.putExtra("newLng", longitude);

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
}
