package odb234.meetpoll;

import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

public class LocationSelectMap extends AppCompatActivity implements OnMapReadyCallback {

    private GoogleMap mMap;

    private LatLng marker;
    private Marker locationSelection;
    private Intent newIntent;

    private LatLng center;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_location_select_map);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        newIntent = getIntent();
        double lat = newIntent.getDoubleExtra("lat", 0.0);
        double lng = newIntent.getDoubleExtra("lng", 0.0);

        center = new LatLng(lat, lng);
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

        mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {
                if(locationSelection != null)
                    locationSelection.remove();
                locationSelection = mMap.addMarker(new MarkerOptions().position(latLng));
                marker = latLng;
            }
        });

        // Add a marker in Sydney and move the camera
        mMap.moveCamera(CameraUpdateFactory.newLatLng(center));
        mMap.moveCamera(CameraUpdateFactory.zoomTo(15));
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch(item.getItemId()){
            case android.R.id.home:
                Intent intentNull = new Intent();
                setResult(0, intentNull);
                finish();
                return true;
            case R.id.pick_location:
                if(marker == null){
                    Toast.makeText(getApplicationContext(), "No location selected.", Toast.LENGTH_LONG).show();
                    return true;
                }
                Intent intent = new Intent();
                intent.putExtra("lat", marker.latitude);
                intent.putExtra("lng", marker.longitude);
                setResult(12, intent);
                finish();
                return true;
            case R.id.settings:
                Intent intent2 = new Intent(LocationSelectMap.this,SettingsActivity.class);
                startActivity(intent2);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu){
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.map_menu2, menu);
        return true;
    }
}
