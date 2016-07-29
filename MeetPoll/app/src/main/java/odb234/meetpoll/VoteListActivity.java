package odb234.meetpoll;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Geocoder;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CompoundButton;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.ui.database.FirebaseListAdapter;
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
    int previousVote = -1;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_vote_list);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        Intent intent = getIntent();
        String path = intent.getStringExtra("eventName");

        gc = new Geocoder(getApplicationContext(), Locale.ENGLISH);

        voteList = (ListView) findViewById(R.id.vote_list);
        places = new ArrayList<>();
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
        String uid = sp.getString("Uid", "");
        mIdsRef = mRootRef.child(uid).child("events").child(path).child("places");
        ListAdapter adapter = new FirebaseListAdapter<LocationListing>(this, LocationListing.class, R.layout.vote_list_cell, mIdsRef) {
            @Override
            protected void populateView(View v, LocationListing model, int position) {
                ((TextView)v.findViewById(R.id.vote_name)).setText(model.getName());
                ((TextView)v.findViewById(R.id.vote_address)).setText(model.getAddress());
                ((RatingBar)v.findViewById(R.id.vote_rating)).setRating(model.getRating());
                RadioButton btn = (RadioButton)v.getTag();
                if(btn == null) {
                    btn = (RadioButton) v.findViewById(R.id.vote_radio_btn);
                    v.setTag(btn);
                    CompoundButton.OnCheckedChangeListener l = new CompoundButton.OnCheckedChangeListener() {
                        @Override
                        public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                            Integer itemPosition = (Integer) compoundButton.getTag();
                            resetRadio(itemPosition);
                            LocationListing ll = (LocationListing)voteList.getAdapter().getItem(itemPosition);
                            ll.setState(b);
                        }
                    };
                    btn.setOnCheckedChangeListener(l);
                }
                btn.setTag(position);
            }
        };

        voteList.setAdapter(adapter);
    }

    private void resetRadio(int j){
        if(previousVote != -1) {
            LocationListing l = (LocationListing) voteList.getAdapter().getItem(previousVote);
            l.setState(false);
            View v = voteList.getChildAt(previousVote);
            ((RadioButton) v.findViewById(R.id.vote_radio_btn)).setChecked(false);
        }
        previousVote = j;
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
                return true;
            case R.id.settings:
                Intent intent2 = new Intent(VoteListActivity.this,SettingsActivity.class);
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
