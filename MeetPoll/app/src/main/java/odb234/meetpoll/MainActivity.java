package odb234.meetpoll;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.CursorAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.firebase.client.Firebase;
import com.firebase.ui.database.FirebaseListAdapter;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class MainActivity extends AppCompatActivity {

    ListView eventList;
    DatabaseConnector dbc;

    private static final String tag = "permissions";

    CursorAdapter ca;
    Firebase mRef;
    com.firebase.client.Query qRef;
    private static final String[] GPS_PERMS = {Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION};
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        eventList = (ListView) findViewById(R.id.event_list);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            requestPermissions(new String[]{Manifest.permission.INTERNET}, 10);
        }
        //--------------------------------
        Firebase.setAndroidContext(this);
        mRef = new Firebase("https://steadfast-leaf-137323.firebaseio.com/");
        //--------------------------------
//        Query query = mRef.child("events");
//        dbc = new DatabaseConnector(this);
//        Log.d(tag, query.toString());
        DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference().child("events");
//        dbRef.getDatabase();
        FirebaseListAdapter adapter = new FirebaseListAdapter<Event>(this, Event.class, R.layout.cell_view, dbRef){
            @Override
            protected void populateView(View view, Event event, int i){
                ((TextView)view.findViewById(R.id.list_event_host)).setText(event.getHostName());
                ((TextView)view.findViewById(R.id.list_event_date)).setText(event.getEventDate());
                ((TextView)view.findViewById(R.id.list_event_name)).setText(event.getEventName());
                ((TextView)view.findViewById(R.id.list_location)).setText(event.getEventLocation());
                ((TextView)view.findViewById(R.id.list_event_date)).setText(event.getEventDate());
                ((TextView)view.findViewById(R.id.list_time)).setText(event.getEventTime());
            }
        };
        eventList.setAdapter(adapter);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, NewEventActivity.class);
                startActivity(intent);
            }
        });

    }

    @Override
    public void onBackPressed() {
//        super.onBackPressed();
        ExitDialogFragment ef = new ExitDialogFragment();
        ef.show(getFragmentManager(), "exitFragment");
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item){
        switch(item.getItemId()){
            case R.id.delete_event:
                dbc.deleteAllEvents();
                recreate();
                return true;
            case R.id.settings:
                Intent intent = new Intent(MainActivity.this,SettingsActivity.class);
                startActivity(intent);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu){
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu,menu);
        return true;
    }

    @Override
    public void onResume()
    {
        super.onResume();
        populateList();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch(requestCode){
            case 10:
                if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    //do firebase stuff
                }
                return;
        }
    }

    private void populateList() {
//        String[] from = new String[] {"host_name", "event_name", "event_location", "date", "time"};
//        int[] to = new int[] {R.id.list_event_host,R.id.list_event_name, R.id.list_location, R.id.list_event_date, R.id.list_time};
//        Cursor c = dbc.getCursor();
//        ca = new SimpleCursorAdapter(MainActivity.this, R.layout.cell_view, c, from, to);
//        eventList.setAdapter(ca);
    }

}
