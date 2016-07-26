package odb234.meetpoll;

import android.*;
import android.Manifest;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
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
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.zip.Inflater;

public class ContactsListActivity extends AppCompatActivity {

//    https://developer.android.com/training/contacts-provider/retrieve-names.html
    ListView inviteList;
    ContentResolver cr;
    ArrayList<Contact> contacts;
    ListAdapter la;
    private static final String TAG = "Contact List Activity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contacts_list);
        getSupportActionBar().setDefaultDisplayHomeAsUpEnabled(true);

        inviteList = (ListView) findViewById(R.id.contact_list);
        contacts = new ArrayList<>();
//        http://stackoverflow.com/questions/12562151/android-get-all-contacts

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if(checkSelfPermission(Manifest.permission.READ_CONTACTS) == PackageManager.PERMISSION_GRANTED)
                populateList();
            else
                requestPermissions(new String[]{Manifest.permission.READ_CONTACTS}, 10);
        }else{
            populateList();
        }
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
                        populateList();
                else
                    Toast.makeText(this, "Permission to access contacts required.", Toast.LENGTH_LONG).show();
        }

    }


    public void populateList(){
        cr = getContentResolver();
        Uri uri = ContactsContract.CommonDataKinds.Phone.CONTENT_URI;

        String[] projection = new String[]{ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME, ContactsContract.CommonDataKinds.Phone.NUMBER,
                                            ContactsContract.CommonDataKinds.Phone.PHOTO_THUMBNAIL_URI};

        Cursor cur = cr.query(uri,projection, null, null, null);
        int indexName = cur.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME);
        int indexNumber = cur.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER);
        cur.moveToFirst();
        do{
            if(cur.getString(indexNumber).length() > 7) {
                String name = cur.getString(indexName);
                String number = cur.getString(indexNumber);
//            byte[] photo = cur.getBlob(indexPhoto);
                contacts.add(new Contact(name, number, false));
                Log.d(TAG, name + ", " + number);
            }
        }while(cur.moveToNext());
        
        la = new ContactsAdapter(this, contacts);
        inviteList.setAdapter(la);

    }

    public class ContactsAdapter extends BaseAdapter{
        ArrayList<Contact> contacts;
        LayoutInflater inflater;
        public ContactsAdapter(Context context, ArrayList<Contact> c){
            contacts = c;
            inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }
        @Override
        public int getCount() {
            return contacts.size();
        }

        @Override
        public View getView(int i, View view, ViewGroup viewGroup) {
            View currentView = view;
            if(currentView == null){
                currentView = inflater.inflate(R.layout.contact_cell_view, viewGroup, false);
            }

            TextView textView = (TextView)findViewById(R.id.contact_name);
            textView.setText(contacts.get(i).getName());
            textView = (TextView)findViewById(R.id.contact_phone);
            textView.setText(contacts.get(i).getPhone());
            CheckBox checkBox = (CheckBox)findViewById(R.id.contact_select);
            checkBox.setChecked(contacts.get(i).getState());
            return currentView;
        }

        @Override
        public long getItemId(int i) {
            return i;
        }

        @Override
        public Object getItem(int i) {
            return null;
        }
    }

    private class Contact{
        private String name;
        private String phone;
        private boolean safeState;

        private Contact(String n, String p, boolean s){
            name = n;
            phone = p;
            safeState = s;
        }

        public String getName(){
            return name;
        }
        public String getPhone(){
            return phone;
        }
        public boolean getState(){
            return safeState;
        }
        public String toString(){
            return name + ", " + phone;
        }
    }
}
