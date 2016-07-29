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
import android.widget.BaseAdapter;
import android.widget.CompoundButton;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.TextView;

import com.google.android.gms.location.places.Place;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Locale;

public class VoteListActivity extends AppCompatActivity {

    private static final String TAG = "Voting Activity";

    DatabaseReference mRootRef  = FirebaseDatabase.getInstance().getReference();
    DatabaseReference mIdsRef;
    Geocoder gc;
    ListView voteList;
    ArrayList<LocationListing> places;
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

        mIdsRef = mRootRef.child("events").child(path).child("places");
        mIdsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(com.google.firebase.database.DataSnapshot dataSnapshot) {
                places =(ArrayList<LocationListing>) dataSnapshot.getValue();

                for(LocationListing ll : places){
                    Log.d(TAG, ll.toString());
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        ListAdapter adapter = new PlaceAdapter(this, places);
        voteList.setAdapter(adapter);
    }

    private void resetRadio(int position){
        int count = voteList.getAdapter().getCount();
        Log.d(TAG, "Location count: " + count);
    }


    private class PlaceAdapter extends BaseAdapter{
        ArrayList<LocationListing> list;
        LayoutInflater inflater;

        public PlaceAdapter(Context context, ArrayList<LocationListing> l){
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

        public LocationListing getListing(int i){ return list.get(i); }

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
                        LocationListing listing = getListing(itemPosition);
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

            LocationListing c = getListing(i);
            btn.setTag(i);
            btn.setChecked(c.getState());
            return currentView;
        }
    }
}
