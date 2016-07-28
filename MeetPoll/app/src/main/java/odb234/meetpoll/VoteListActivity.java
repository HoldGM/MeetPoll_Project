package odb234.meetpoll;

import android.content.Intent;
import android.location.Geocoder;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ListAdapter;
import android.widget.ListView;

import com.firebase.client.ChildEventListener;
import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.Query;
import com.firebase.ui.database.FirebaseListAdapter;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

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
import java.util.List;
import java.util.Locale;

public class VoteListActivity extends AppCompatActivity {

    private static final String TAG = "Voting Activity";

    Firebase fb;
    Geocoder gc;
    ListView voteList;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_vote_list);
        Intent intent = getIntent();
        String path = intent.getStringExtra("eventName");

        gc = new Geocoder(getApplicationContext(), Locale.ENGLISH);

        voteList = (ListView) findViewById(R.id.vote_list);

        fb = new Firebase("https://steadfast-leaf-137323.firebaseio.com/");
        DatabaseReference query = FirebaseDatabase.getInstance().getReference().child("events").child(path).child("ids");

        ListAdapter adapter = new FirebaseListAdapter<String>(this, String.class, R.layout.vote_list_cell, query) {
            @Override
            protected void populateView(View v, String model, int position) {
                Log.d(TAG, model);
                new PlaceList().execute(model);
            }

        };
        voteList.setAdapter(adapter);

        Log.d(TAG, intent.getStringExtra("eventName"));
    }

    public class PlaceList extends AsyncTask<String, com.google.android.gms.location.places.Place, Void>{
        @Override
        protected Void doInBackground(String... id) {
            PlacesService ps = new PlacesService(getString(R.string.google_api_key));
            try {
                String urlString = "https://maps.googleapis.com/maps/api/place/details/json?placeid=" + id;
                urlString += ps;

                DefaultHttpClient client = new DefaultHttpClient();
                HttpGet req = new HttpGet(urlString);
                HttpResponse res = client.execute(req);
                HttpEntity jsonEntity = res.getEntity();
                InputStream in = jsonEntity.getContent();
                JSONObject jsonObject = new JSONObject(convertStreamtoString(in));
                JSONArray resArray = jsonObject.getJSONArray("result");
                String name = resArray.getJSONObject(0).getString("name");
                Log.d(TAG, name);
            }catch(Exception e){

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

    }
}
