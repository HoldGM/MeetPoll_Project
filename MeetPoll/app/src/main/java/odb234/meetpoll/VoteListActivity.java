package odb234.meetpoll;

import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Geocoder;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;
import com.firebase.ui.database.FirebaseListAdapter;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import java.util.Locale;

public class VoteListActivity extends AppCompatActivity {

    private static final String TAG = "Voting Activity";

    DatabaseReference mRootRef  = FirebaseDatabase.getInstance().getReference();
    DatabaseReference mIdsRef;
    Geocoder gc;
    ListView voteList;
    String uid;
    String path;
    int previousVote = -1;
    SharedPreferences sp;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_vote_list);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        Intent intent = getIntent();
        path = intent.getStringExtra("path");

        gc = new Geocoder(getApplicationContext(), Locale.ENGLISH);
        voteList = (ListView) findViewById(R.id.vote_list);
        sp = PreferenceManager.getDefaultSharedPreferences(this);
        uid = intent.getStringExtra("uid");
        mIdsRef = mRootRef.child(uid).child("events").child(path).child("places");
        ListAdapter adapter = new FirebaseListAdapter<LocationListing>(this, LocationListing.class, R.layout.vote_list_cell, mIdsRef) {
            @Override
            protected void populateView(View v, final LocationListing model, final int position) {
                ((TextView)v.findViewById(R.id.vote_name)).setText(model.getName());
                ((TextView)v.findViewById(R.id.vote_address)).setText(model.getAddress());
                ((RatingBar)v.findViewById(R.id.vote_rating)).setRating(model.getRating());
                Log.d(TAG,model.getName() + " " + position);
                final RadioButton btn = (RadioButton)v.findViewById(R.id.vote_radio_btn);
                btn.setChecked(position == previousVote);
                btn.setTag(position);
                btn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        resetRadio((Integer)view.getTag());
                        notifyDataSetChanged();
                    }
                });
            }
        };

        voteList.setAdapter(adapter);
    }

    private void resetRadio(int j){
        if(previousVote != -1) {
            View v = getViewByPosition(previousVote,voteList);
            RadioButton btn = (RadioButton)v.findViewById(R.id.vote_radio_btn);
            btn.setChecked(false);
        }
        previousVote = j;
    }
    //http://stackoverflow.com/questions/24811536/android-listview-get-item-view-by-position
    public View getViewByPosition(int pos, ListView listView) {
        final int firstListItemPosition = listView.getFirstVisiblePosition();
        final int lastListItemPosition = firstListItemPosition + listView.getChildCount() - 1;

        if (pos < firstListItemPosition || pos > lastListItemPosition ) {
            return listView.getAdapter().getView(pos, null, listView);
        } else {
            final int childIndex = pos - firstListItemPosition;
            return listView.getChildAt(childIndex);
        }
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
                if(checkVote()) {
                    vote();
                    finish();
                }else{
                    Toast.makeText(this, "No selection made.", Toast.LENGTH_LONG).show();
                }
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

    public boolean checkVote(){
        if(previousVote == -1)
            return false;
        else
            return true;
    }

    public void vote(){
        final DatabaseReference q = FirebaseDatabase.getInstance().getReference().child(uid).child("events").child(path).child("places").child(previousVote + "");
        q.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                final long voteCount = (long)dataSnapshot.child("voteCount").getValue();

                (q.getParent().getParent().child("inviteList")).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        for(DataSnapshot child : dataSnapshot.getChildren()){
                            Log.d(TAG, "Child Phone: " + child.child("phone").toString());
                            Log.d(TAG, "Preferences Phone: " + sp.getString("phone", ""));
                            if((child.child("phone").getValue().toString()).equals(sp.getString("phone",""))){
                                Log.d(TAG, "Inside if check");
                                if(!(boolean)child.child("voted").getValue()) {
                                    q.child("voteCount").setValue(voteCount + 1);
                                    (child.child("voted").getRef()).setValue(true);
                                }else{
                                    Toast.makeText(getApplicationContext(), "You have already voted.", Toast.LENGTH_LONG).show();
                                }
                            }
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

}
