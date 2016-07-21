package odb234.meetpoll;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class SearchResultsActivity extends AppCompatActivity implements OnMapReadyCallback, GoogleApiClient.OnConnectionFailedListener {

    private static final String TAG = "Map activity";
    private GoogleMap mMap;
    Intent intent;
    //Current location info
    //------------------------------------------
    private double newLat;
    private double newLng;
    private String eventName;
    private String searchLoaction;
    private String eventDate;
    private String eventTime;
    private String locationType;
    private String locationSubtype;
    private int eventRating;
    private String[] ids;

    private ArrayList<MapMarker> mapMarkers;





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
        eventRating = intent.getIntExtra("rating", 0);

        mGoogleApiClient = new GoogleApiClient
                .Builder(this)
                .addApi(Places.GEO_DATA_API)
                .addApi(Places.PLACE_DETECTION_API)
                .enableAutoManage(this, this)
                .build();

        new findNearby().execute();
    }

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

        mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {
                marker.showInfoWindow();
                Log.d(TAG, "Marker selected" + marker.getTitle().toString());
                return true;
            }
        });
        LatLng youAreHere = new LatLng(newLat, newLng);
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(youAreHere, 17));
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
                dbc.insertEvent(hostName, eventName, searchLoaction, eventDate, eventTime, locationType, locationSubtype, eventRating, ids);
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

    private class findNearby extends AsyncTask<Void, com.google.android.gms.location.places.Place, Void>{
        @Override
        protected Void doInBackground(Void... voids) {

            Log.d(TAG, "Starting nearby places search");

            PlacesService service = new PlacesService(getString(R.string.google_maps_key));
            try{
                String urlString = service.makeUrl(newLat, newLng, locationSubtype);
                Log.d(TAG, "JSON String: " + urlString);
                DefaultHttpClient client = new DefaultHttpClient();
                HttpGet req = new HttpGet(urlString);
                HttpResponse res = client.execute(req);
                HttpEntity jsonEntity = res.getEntity();
                InputStream in = jsonEntity.getContent();
                JSONObject jsonObj = new JSONObject(convertStreamtoString(in));
                JSONArray resArray = jsonObj.getJSONArray("results");
                addMarkers(resArray);
                if(resArray.length() > 0){
                    for(int i = 0; i < resArray.length(); i++){
                        double lat = resArray.getJSONObject(i).getJSONObject("geometry").getJSONObject("location").getDouble("lat");
                        double lng = resArray.getJSONObject(i).getJSONObject("geometry").getJSONObject("location").getDouble("lng");
                        Log.d(TAG, resArray.getJSONObject(i).getString("name") + "  lat:" + lat + ", lng: " + lng);
                    }
                }
            }catch(JSONException e){
                Log.d(TAG, "JSON failed");
            }catch(IOException e) {
                Log.d(TAG, "HTTP Request Failed");
            }


             return null;
        }

        private String convertStreamtoString(InputStream in){
            BufferedReader br = new BufferedReader(new InputStreamReader(in));
            StringBuilder jsonStr = new StringBuilder();
            String line;
            try{
                while((line = br.readLine())!= null){
                    String t = line +"\n";
                    jsonStr.append(t);
                }
                br.close();
            }catch(IOException e){
                e.printStackTrace();
            }
            return jsonStr.toString();
        }

        private void addMarkers(JSONArray jsonArray){
            ids = new String[10];
            mapMarkers = new ArrayList<>();
            ArrayList<MapMarker> tempMarkers = new ArrayList<>();
            for(int i = 0; i < jsonArray.length() && i < 10; i++){
                try {
                    double lat = jsonArray.getJSONObject(i).getJSONObject("geometry").getJSONObject("location").getDouble("lat");
                    double lng = jsonArray.getJSONObject(i).getJSONObject("geometry").getJSONObject("location").getDouble("lng");
                    Double rating = jsonArray.getJSONObject(i).getDouble("rating");
                    String  address = jsonArray.getJSONObject(i).getString("vicinity");
                    String id = jsonArray.getJSONObject(i).getString("id");
                    String name = jsonArray.getJSONObject(i).getString("name");
                    Log.d(TAG, "Place name: " + name);

                    if(rating >= eventRating) {
                        mapMarkers.add(new MapMarker(new LatLng(lat, lng), address, id, rating, name));
                        ids[i] = id;
                    }else{
                        tempMarkers.add(new MapMarker(new LatLng(lat, lng), address, id, rating, name));
                    }
                }catch (JSONException e){
                    Log.d(TAG, "invalid json element");
                }
            }
            for(int i = 0; mapMarkers.size() <= 10 && i < tempMarkers.size(); i++){
                mapMarkers.add(tempMarkers.get(i));
                ids[mapMarkers.size()-1] = tempMarkers.get(i).getId();
            }
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            for(int i = 0; i < mapMarkers.size(); i++){
                Log.d(TAG, mapMarkers.get(i).getName());
                mMap.addMarker(new MarkerOptions().position(mapMarkers.get(i).getLatLng()).title(mapMarkers.get(i).getName()).snippet(mapMarkers.get(i).getAddress()));
            }

            if(mapMarkers.size() != 0) {
                LatLngBounds.Builder bounds = new LatLngBounds.Builder();
                for (int i = 0; i < mapMarkers.size(); i++) {
                    bounds.include(mapMarkers.get(i).getLatLng());
                }
                mMap.moveCamera(CameraUpdateFactory.newLatLngBounds(bounds.build(), 100));
            }else {
                Toast.makeText(getApplicationContext(), "No results found. Please refine search criteria.", Toast.LENGTH_LONG).show();
                finish();
            }

            Log.d(TAG, Arrays.toString(ids));
        }
    }



}
