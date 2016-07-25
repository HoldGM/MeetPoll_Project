package odb234.meetpoll;

import android.*;
import android.Manifest;
import android.annotation.SuppressLint;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.preference.PreferenceManager;
import android.provider.ContactsContract;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.CheckBox;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.Toast;

import java.util.Arrays;

public class ContactsListActivity extends AppCompatActivity {

//    https://developer.android.com/training/contacts-provider/retrieve-names.html
    ListView inviteList;
    private static final String TAG = "Contact List Activity";

    @SuppressLint("InlinedApi")
    private final static String[] FROM_COLUMNS = {
            Build.VERSION.SDK_INT
                    >= Build.VERSION_CODES.HONEYCOMB ?
                    ContactsContract.Contacts.DISPLAY_NAME_PRIMARY :
                    ContactsContract.Contacts.DISPLAY_NAME
    };

    private final static int[] TO_IDS = {
            android.R.id.text1
    };

    long mContactId;
    String mContactKey;
    Uri mContactUri;
    private SimpleCursorAdapter mCursorAdapter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contacts_list);
        getSupportActionBar().setDefaultDisplayHomeAsUpEnabled(true);

        inviteList = (ListView) findViewById(R.id.contact_list);
//        http://stackoverflow.com/questions/12562151/android-get-all-contacts
        ContentResolver cr = getContentResolver();
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
            if(checkSelfPermission(Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED){
                requestPermissions(new String[]{Manifest.permission.READ_CONTACTS}, 5);
                return;
            }
        }
        Cursor cur = cr.query(ContactsContract.Contacts.CONTENT_URI,
                null, null, null, null);
        Log.d(TAG, Arrays.deepToString(cur.getColumnNames()));
        String[] from = new String[]{"DISPLAY_NAME", "photo_thumb_uri"};
        int[] to = new int[]{R.id.contact_name, R.id.contact_image};
        mCursorAdapter = new SimpleCursorAdapter(ContactsListActivity.this, R.layout.contact_cell_view, cur, from, to);
        inviteList.setAdapter(mCursorAdapter);

        inviteList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                CheckBox cb = (CheckBox) view.findViewById(R.id.contact_select);
                cb.setChecked(true);
            }
        });
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch(item.getItemId()){
            case android.R.id.home:
                finish();
                return true;
            case R.id.send_event:
                return true;
            case R.id.settings:
                Intent intent2 = new Intent(ContactsListActivity.this,SettingsActivity.class);
                startActivity(intent2);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu){
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.map_menu, menu);
        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case 10:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        if (checkSelfPermission(Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED) {
                            requestPermissions(new String[]{
                                    Manifest.permission.READ_CONTACTS
                            }, 10);
//                            return;
                        }
                    }
        }
    }
}
