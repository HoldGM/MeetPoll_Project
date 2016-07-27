package odb234.meetpoll;

import android.*;
import android.Manifest;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.provider.ContactsContract;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;

import java.util.ArrayList;

public class ContactsListActivity extends AppCompatActivity {

//    https://developer.android.com/training/contacts-provider/retrieve-names.html
    ListView inviteList;
    ContentResolver cr;
    ArrayList<Contact> contacts;
    ListAdapter la;
    String hostPhone;
    private static final String TAG = "Contact List Activity";

    Firebase fdb;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contacts_list);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        Firebase.setAndroidContext(this);
        fdb = new Firebase("https://steadfast-leaf-137323.firebaseio.com/");
        inviteList = (ListView) findViewById(R.id.contact_list);
        contacts = new ArrayList<>();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if(checkSelfPermission(Manifest.permission.READ_CONTACTS) == PackageManager.PERMISSION_GRANTED)
                populateList();
            else
                requestPermissions(new String[]{Manifest.permission.READ_CONTACTS}, 10);
            if(checkSelfPermission(Manifest.permission.READ_PHONE_STATE) !=  PackageManager.PERMISSION_GRANTED){
                requestPermissions(new String[]{Manifest.permission.READ_PHONE_STATE}, 15);
            }else{
                TelephonyManager tMgr = (TelephonyManager)getApplicationContext().getSystemService(Context.TELEPHONY_SERVICE);
                hostPhone = tMgr.getLine1Number();
            }
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
                Intent intent = getIntent();
                Firebase eventRef = fdb.child("events").child(intent.getBundleExtra("bundle").getString("hostName") + "_" + intent.getBundleExtra("bundle").getString("eventName"));
                ArrayList<Contact> invitees = collectInvites();
                Event event = new Event(intent.getBundleExtra("bundle").getString("hostName"),
                        hostPhone,
                        intent.getBundleExtra("bundle").getString("eventName"),
                        intent.getBundleExtra("bundle").getString("eventLocation"),
                        intent.getBundleExtra("bundle").getString("date"),
                        intent.getBundleExtra("bundle").getString("time"),
                        intent.getBundleExtra("bundle").getDouble("rating"),
                        intent.getBundleExtra("bundle").getString("locationType"),
                        intent.getBundleExtra("bundle").getString("locationSubtype"),
                        intent.getStringArrayListExtra("ids"), invitees);
                eventRef.setValue(event, new Firebase.CompletionListener(){
                    @Override
                    public void onComplete(FirebaseError firebaseError, Firebase firebase) {
                        if(firebaseError != null){
                            Log.d(TAG, "data could not be saved: "  + firebaseError.getMessage());
                        }else{
                            Log.d(TAG, "Firebase worked");
                        }
                    }
                });
                startActivity(new Intent(ContactsListActivity.this, MainActivity.class));
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
                contacts.add(new Contact(name, number, false));
                Log.d(TAG, name + ", " + number);
            }
        }while(cur.moveToNext());

        Log.d(TAG, "Contacts List size: " + contacts.size());
        la = new ContactsAdapter(this, contacts);
        inviteList.setAdapter(la);
        getListItems();
    }

    public void getListItems() {

    }

    private ArrayList<Contact> collectInvites(){
        int listCount = inviteList.getAdapter().getCount();
        ArrayList<Contact> invites = new ArrayList<>();
        for(int i = 0; i< listCount; i++){
            View listView = inviteList.getAdapter().getView(i, null, null);
            if(((CheckBox)listView.findViewById(R.id.contact_select)).isChecked())
                invites.add(((ContactsAdapter)inviteList.getAdapter()).getContact(i));
            Log.d(TAG, "Contact name: " + ((TextView)listView.findViewById(R.id.contact_name)).getText().toString());
        }
        return invites;
    }

    private Contact getContact(int position) {
        return (Contact) inviteList.getAdapter().getItem(position);
    }

    private class ContactsAdapter extends BaseAdapter{
        ArrayList<Contact> contactsList;
        LayoutInflater inflater;
        public ContactsAdapter(Context context, ArrayList<Contact> list){
            contactsList = list;
            inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }
        @Override
        public int getCount() {
            return contactsList.size();
        }
        @Override
        public long getItemId(int i) { return i; }
        @Override
        public Object getItem(int i) {
            return contactsList.get(i);
        }
        public Contact getContact(int i){
            return contactsList.get(i);
        }
        @Override
        public View getView(int i, View view, ViewGroup viewGroup) {
            View currentView = view;
            if(currentView == null){
                currentView = inflater.inflate(R.layout.contact_cell_view, viewGroup, false);
            }
            contactsList.get(i).toString();
            TextView textView = (TextView)currentView.findViewById(R.id.contact_name);
            textView.setText(contactsList.get(i).getName());
            textView = (TextView)currentView.findViewById(R.id.contact_phone);
            textView.setText(contactsList.get(i).getPhone());
            CheckBox checkBox = (CheckBox)currentView.getTag();
            if(checkBox == null)
            {
                checkBox = (CheckBox) currentView.findViewById(R.id.contact_select);
                currentView.setTag(checkBox);
                CompoundButton.OnCheckedChangeListener l = new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                        Integer itemPosition = (Integer) compoundButton.getTag();
                        Contact contact = getContact(itemPosition);
                        contact.setState(b);
                    }
                };
                checkBox.setOnCheckedChangeListener(l);
            }

            Contact c = getContact(i);
            checkBox.setTag(i);
            checkBox.setChecked(c.getState());
            return currentView;
        }
    }


}