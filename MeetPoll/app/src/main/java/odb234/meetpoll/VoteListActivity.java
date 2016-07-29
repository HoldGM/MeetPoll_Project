package odb234.meetpoll;

import android.content.Context;
import android.content.Intent;
import android.location.Geocoder;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.CompoundButton;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.RatingBar;
import android.widget.TextView;

import com.firebase.client.ChildEventListener;
import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.Query;
import com.firebase.ui.database.FirebaseListAdapter;
import com.google.android.gms.location.places.Place;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.GenericTypeIndicator;
import com.google.firebase.database.ValueEventListener;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Array;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

public class VoteListActivity extends AppCompatActivity {

    private static final String TAG = "Voting Activity";

    DatabaseReference mRootRef  = FirebaseDatabase.getInstance().getReference();
    DatabaseReference mIdsRef;
    Geocoder gc;
    ListView voteList;
    ArrayList<VotingListing> places;
    ArrayList<String> ids;
    ListAdapter adapter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_vote_list);
        Intent intent = getIntent();
        String path = intent.getStringExtra("eventName");

        gc = new Geocoder(getApplicationContext(), Locale.ENGLISH);

        voteList = (ListView) findViewById(R.id.vote_list);
        places = new ArrayList<>();

        mIdsRef = mRootRef.child("events").child(path).child("ids");
        mIdsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(com.google.firebase.database.DataSnapshot dataSnapshot) {
                ids =(ArrayList<String>) dataSnapshot.getValue();

                for(String s : ids){
                    Log.d(TAG, s);
                }
                new PlaceList().execute();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void resetRadio(int position){
        int count = voteList.getAdapter().getCount();
        Log.d(TAG, "Location count: " + count);
    }


    class PlaceList extends AsyncTask<Void, com.google.android.gms.location.places.Place, Void>{
        @Override
        protected Void doInBackground(Void... aVoid) {
            Log.d(TAG, "IDs length: " + ids.size());
            try {
                for(int i = 0; i < ids.size(); i++) {
                    String id = ids.get(i);
                    String urlString = "https://maps.googleapis.com/maps/api/place/details/json?placeid=" + id;
                    urlString += "&key=" + getString(R.string.google_maps_key);
                    Log.d(TAG, urlString);
                    DefaultHttpClient client = new DefaultHttpClient();
                    HttpGet req = new HttpGet(urlString);
                    HttpResponse res = client.execute(req);
                    HttpEntity jsonEntity = res.getEntity();
                    InputStream in = jsonEntity.getContent();
                    JSONObject jsonObject = new JSONObject(convertStreamtoString(in));
                    JSONObject resObj = jsonObject.getJSONObject("result");
                    String name = resObj.getString("name");
                    String address = resObj.getString("vicinity");
                    float rating = BigDecimal.valueOf(resObj.getDouble("rating")).floatValue();
                    double lat = resObj.getJSONObject("geometry").getJSONObject("location").getDouble("lat");
                    double lng = resObj.getJSONObject("geometry").getJSONObject("location").getDouble("lng");
                    places.add(new VotingListing(name, address, rating, false, lat, lng, id));
                    Log.d(TAG, "Name: " + name + ", Address: " + address + ", Rating: " + rating + ", ID: " + id);
                }
            }catch(Exception e){
                Log.e(TAG, e.toString());
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

        @Override
        protected void onProgressUpdate(Place... values) {
            super.onProgressUpdate(values);
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            adapter = new PlaceAdapter(getApplicationContext(), places);
            voteList.setAdapter(adapter);
        }
    }

    private class PlaceAdapter extends BaseAdapter{
        ArrayList<VotingListing> list;
        LayoutInflater inflater;

        public PlaceAdapter(Context context, ArrayList<VotingListing> l){
            inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            list = l;
        }

        @Override
        public int getCount() {
            return list.size();
        }

        @Override
        public long getItemId(int i) {
            return i;
        }

        @Override
        public Object getItem(int i) {
            return list.get(i);
        }

        public VotingListing getListing(int i){ return list.get(i); }

        @Override
        public View getView(int i, View view, ViewGroup viewGroup) {
            View currentView = view;
            if(currentView == null){
                currentView = inflater.inflate(R.layout.vote_list_cell, viewGroup, false);
            }
            TextView textView = (TextView)currentView.findViewById(R.id.vote_name);
            textView.setText(list.get(i).getName());
            textView = (TextView)currentView.findViewById(R.id.vote_address);
            textView.setText(list.get(i).getAddress());
            RadioButton btn = (RadioButton)currentView.getTag();
            if(btn == null){
                btn = (RadioButton)currentView.findViewById(R.id.vote_radio_btn);
                currentView.setTag(btn);
                CompoundButton.OnCheckedChangeListener l = new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                        Integer itemPosition = (Integer) compoundButton.getTag();
                        VotingListing listing = getListing(itemPosition);
                        listing.setState(b);
                    }
                };
                btn.setOnCheckedChangeListener(l);
            }

            currentView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    Log.d(TAG, "Place Name: "+ ((TextView)view.findViewById(R.id.vote_name)).getText().toString() );
//                    Intent intent = new Intent(getApplicationContext(), LocationSelectMap.class);
//                    startActivity(intent);
                }
            });

            VotingListing c = getListing(i);
            btn.setTag(i);
            btn.setChecked(c.getState());
            return currentView;
        }
    }
}
