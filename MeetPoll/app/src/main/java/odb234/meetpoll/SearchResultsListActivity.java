package odb234.meetpoll;

import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.CompoundButton;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.Switch;

import java.util.ArrayList;

public class SearchResultsListActivity extends AppCompatActivity {

    ListView resultList;
    Switch aSwitch;
    private static final String TAG = "Search Results List";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_results_list);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        aSwitch = (Switch) findViewById(R.id.switch1);
        aSwitch.setChecked(true);
        aSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if(!b){
                    finish();
                }
                return;
            }
        });
        String[] names = getIntent().getStringArrayExtra("names");
        String[] addresses = getIntent().getStringArrayExtra("addresses");
        String[] ids = getIntent().getStringArrayExtra("ids");
        float[] ratings = getIntent().getFloatArrayExtra("ratings");
        resultList = (ListView)findViewById(R.id.results_list);
//        ListAdapter la = new ResultListAdapter(this, names, addresses, ids, ratings);
//        resultList.setAdapter(la);

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch(item.getItemId()){
            case android.R.id.home:
                finish();
                return true;
            case R.id.send_event:
                setResult(SearchResultsActivity.LIST_SEND);
                finish();
                return true;
            case R.id.settings:
                Intent intent2 = new Intent(SearchResultsListActivity.this,SettingsActivity.class);
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
}
