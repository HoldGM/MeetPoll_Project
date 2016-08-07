package odb234.meetpoll;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Switch;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
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
import java.math.BigDecimal;
import java.util.ArrayList;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link ResultsMapFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link ResultsMapFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ResultsMapFragment extends Fragment{
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private OnFragmentInteractionListener mListener;

    ProgressDialog pd;
    private static final String TAG = "Map activity";
    private GoogleMap mMap;
    MapView mapView;
    Intent intent;
    public static final int LIST_SEND = 101;
    //Current location info
    //------------------------------------------
    private double newLat;
    private double newLng;
    private String eventName;
    private String searchLoaction;
    private int radius;
    private String eventDate;
    private String eventTime;
    private String locationType;
    private String locationSubtype;
    private Double eventRating;
    private ArrayList<String> ids;

    private Switch aSwitch;

    private ArrayList<MapMarker> mapMarkers;
    private JSONArray resArray;

    private GoogleApiClient mGoogleApiClient;

    public ResultsMapFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment ResultsMapFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static ResultsMapFragment newInstance(String param1, String param2) {
        ResultsMapFragment fragment = new ResultsMapFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
        pd = new ProgressDialog(getActivity(), ProgressDialog.STYLE_SPINNER);
        Intent intent = getActivity().getIntent();
        newLat = intent.getDoubleExtra("newLat", 30);
        newLng = intent.getDoubleExtra("newLng", -97);
        eventName = intent.getStringExtra("eventName");
        searchLoaction = intent.getStringExtra("eventLocation");
        radius = intent.getIntExtra("radius", 50000);
        Log.d(TAG, "Search Radius: " + radius);
        eventDate = intent.getStringExtra("date");
        eventTime = intent.getStringExtra("time");
        locationType = intent.getStringExtra("locationType");
        locationSubtype = intent.getStringExtra("locationSubtype");
        eventRating = intent.getDoubleExtra("rating", 0.0);

        new findNearby().execute();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_results_map, container, false);
        mapMarkers = new ArrayList<>();
        mapView = (MapView)rootView.findViewById(R.id.map_view);
        mapView.onCreate(savedInstanceState);
        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();
        mapView.onResume();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mapView.onDestroy();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapView.onLowMemory();
    }


    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Void aVoid) {
        if (mListener != null) {
            mListener.onResultsMapFragmentInteraction(aVoid);
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p/>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onResultsMapFragmentInteraction(Void aVoid);
    }

    private class findNearby extends AsyncTask<Void, Place, Void> {
        @Override
        protected void onPreExecute() {
            pd.setMessage("Searching for places...");
            pd.setCancelable(false);
            pd.show();
        }

        @Override
        protected Void doInBackground(Void... voids) {

            Log.d(TAG, "Starting nearby places search");

            PlacesService service = new PlacesService(getString(R.string.google_maps_key));
            try{
                String urlString = service.makeUrl(newLat, newLng, locationType, locationSubtype, radius);
                Log.d(TAG, "JSON String: " + urlString);
                DefaultHttpClient client = new DefaultHttpClient();
                HttpGet req = new HttpGet(urlString);
                HttpResponse res = client.execute(req);
                HttpEntity jsonEntity = res.getEntity();
                InputStream in = jsonEntity.getContent();
                JSONObject jsonObj = new JSONObject(convertStreamtoString(in));
                resArray = jsonObj.getJSONArray("results");
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
            ids = new ArrayList<>();
            mapMarkers = new ArrayList<>();
            ArrayList<MapMarker> tempMarkers = new ArrayList<>();
            for(int i = 0; i < jsonArray.length() && i < 10; i++){
                try {
                    double lat = jsonArray.getJSONObject(i).getJSONObject("geometry").getJSONObject("location").getDouble("lat");
                    double lng = jsonArray.getJSONObject(i).getJSONObject("geometry").getJSONObject("location").getDouble("lng");
                    String  address = jsonArray.getJSONObject(i).getString("vicinity");
                    String id = jsonArray.getJSONObject(i).getString("place_id");
                    String name = jsonArray.getJSONObject(i).getString("name");
                    float rating;
                    try {
                        rating = BigDecimal.valueOf(jsonArray.getJSONObject(i).getDouble("rating")).floatValue();
                    }catch(JSONException e){
                        Log.d(TAG, name + " rating not available");
                        rating = 0;
                    }
                    if(rating >= eventRating) {
                        Log.d(TAG, name + " rating: " + rating);
                        mapMarkers.add(new MapMarker(new LatLng(lat, lng), address, id, rating, name));
                        ids.add(id);
                    }else{
                        tempMarkers.add(new MapMarker(new LatLng(lat, lng), address, id, rating, name));
                    }
                }catch (JSONException e){
                    Log.d(TAG, "invalid json element");
                }
            }
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);

            mapView.getMapAsync(new OnMapReadyCallback() {
                @Override
                public void onMapReady(GoogleMap googleMap) {
                    mMap = googleMap;
                    mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
                        @Override
                        public boolean onMarkerClick(Marker marker) {
                            marker.showInfoWindow();
                            mMap.animateCamera(CameraUpdateFactory.newLatLng(marker.getPosition()));
                            return false;
                        }
                    });
                    LatLngBounds.Builder bound = new LatLngBounds.Builder();
                    if(mapMarkers.size() != 0) {
                        for (int i = 0; i < mapMarkers.size(); i++) {
                            mMap.addMarker(new MarkerOptions().position(mapMarkers.get(i).getLatLng()).title(mapMarkers.get(i).getName()).icon(BitmapDescriptorFactory.defaultMarker(195)));
                        }
                        for (int i = 0; i < mapMarkers.size(); i++) {
                            bound.include(mapMarkers.get(i).getLatLng());
                        }
                        mMap.moveCamera(CameraUpdateFactory.newLatLngBounds(bound.build(), 100));
                    }else {
                        Toast.makeText(getActivity(), "No results found. Please refine search criteria.", Toast.LENGTH_LONG).show();
                    }

                }
            });
            pd.dismiss();
            ((TabbedSearchResultsActivity) getActivity()).setIds(ids, mapMarkers);
            Log.d(TAG, ids.toString());
        }
    }
}
