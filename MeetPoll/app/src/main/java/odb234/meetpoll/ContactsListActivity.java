package odb234.meetpoll;

import android.*;
import android.Manifest;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.preference.PreferenceManager;
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
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.google.android.gms.location.places.Place;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.util.ArrayList;

public class ContactsListActivity extends AppCompatActivity {

//    https://developer.android.com/training/contacts-provider/retrieve-names.html
    ListView inviteList;
    ContentResolver cr;
    ArrayList<Contact> contacts;
    ArrayList<LocationListing> places;
    ArrayList<String> ids;
    boolean asyncFinished;
    ListAdapter la;
    String hostPhone;
    ProgressBar progressBar;
    private static final String TAG = "Contact List Activity";
    SharedPreferences sp;
    String uid;
    ArrayList<Contact> invites;
    private static ArrayList<Contact> invitees;
    Firebase fdb;
    DatabaseReference mRoot = FirebaseDatabase.getInstance().getReference();
    DatabaseReference mEventRef;
    private long eventIndex;
    ProgressDialog pd;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contacts_list);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        sp = PreferenceManager.getDefaultSharedPreferences(this);
        pd = new ProgressDialog(ContactsListActivity.this, ProgressDialog.STYLE_SPINNER);
        uid = sp.getString("Uid", "");
        Firebase.setAndroidContext(this);
        fdb = new Firebase("https://steadfast-leaf-137323.firebaseio.com/");
        inviteList = (ListView) findViewById(R.id.contact_list);
        contacts = new ArrayList<>();
        places = new ArrayList<>();
        ids = getIntent().getStringArrayListExtra("ids");

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if(checkSelfPermission(Manifest.permission.READ_CONTACTS) == PackageManager.PERMISSION_GRANTED)
                populateList();
            else
                requestPermissions(new String[]{Manifest.permission.READ_CONTACTS}, 10);
        }else{
            populateList();
        }

        mEventRef = mRoot.child(uid).child("events");
        mEventRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for(DataSnapshot child : dataSnapshot.getChildren()){
                    if(Integer.parseInt(child.getKey()) > eventIndex)
                        eventIndex = Integer.parseInt(child.getKey());
                }
                if(dataSnapshot.getChildrenCount() != 0)
                    eventIndex++;
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

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
                if(pd != null && !pd.isShowing()) {
                    invitees = collectInvites();
                    new PlaceList().execute();
                }
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
                break;
        }

    }


    public void populateList(){
        cr = getContentResolver();
        Uri uri = ContactsContract.CommonDataKinds.Phone.CONTENT_URI;

        String[] projection = new String[]{ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME, ContactsContract.CommonDataKinds.Phone.NUMBER,
                                            ContactsContract.CommonDataKinds.Phone.PHOTO_THUMBNAIL_URI};

        Cursor cur = cr.query(uri,projection, null, null, ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME + " ASC");
        int indexName = cur.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME);
        int indexNumber = cur.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER);
        cur.moveToFirst();
        do{
            if(cur.getString(indexNumber).length() > 7) {
                String name = cur.getString(indexName);
                String number = cur.getString(indexNumber);
                String finNum = "";
                for(int i = 0; i < number.length(); i++){
                    if(number.charAt(i) >= '0' && number.charAt(i) <= '9'){
                        finNum += number.charAt(i);
                    }
                }
                if(finNum.length() > 10){
                    finNum = finNum.substring(1);
                }
                contacts.add(new Contact(name, finNum, false));
                Log.d(TAG, name + ", " + number);
            }
        }while(cur.moveToNext());

        Log.d(TAG, "Contacts List size: " + contacts.size());
        la = new ContactsAdapter(this, contacts);
        inviteList.setAdapter(la);
    }

    private ArrayList<Contact> collectInvites(){
        int listCount = inviteList.getAdapter().getCount();
        invites = new ArrayList<>();
        invites.add(new Contact(sp.getString("name", ""), sp.getString("phone", ""), true));
        for(int i = 0; i< listCount; i++){
            View listView = inviteList.getAdapter().getView(i, null, null);
            if(((CheckBox)listView.findViewById(R.id.contact_select)).isChecked())
                invites.add(((ContactsAdapter)inviteList.getAdapter()).getContact(i));
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
            String phone = contactsList.get(i).getPhone();
            phone = "(" + phone.substring(0,3) + ")" + phone.substring(3,6) + "-" + phone.substring(6);
            textView.setText(phone);
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

    class PlaceList extends AsyncTask<Void, Place, Void> {
        @Override
        protected void onPreExecute() {
            pd.setMessage("Creating Event....");
            pd.setCancelable(false);
            pd.show();
            asyncFinished = false;
        }

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
                    String phone;
                    try {
                        phone = resObj.getString("international_phone_number");
                    }catch(JSONException e){
                        phone = "";
                    }

                    float rating;
                    try {
                        rating = BigDecimal.valueOf(resObj.getDouble("rating")).floatValue();
                    }catch(JSONException e){
                        rating = 0;
                    }
                    double lat = resObj.getJSONObject("geometry").getJSONObject("location").getDouble("lat");
                    double lng = resObj.getJSONObject("geometry").getJSONObject("location").getDouble("lng");
                    places.add(new LocationListing(name, address, phone, rating, false, lat, lng, id));
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
        protected void onPostExecute(Void aVoid) {
            pd.dismiss();
            Intent intent = getIntent();

            Firebase eventRef = fdb.child(uid).child("events").child("" + eventIndex);
            Event event = new Event(intent.getBundleExtra("bundle").getString("hostName"),
                    hostPhone,
                    intent.getBundleExtra("bundle").getString("eventName"),
                    intent.getBundleExtra("bundle").getString("eventLocation"),
                    intent.getBundleExtra("bundle").getString("date"),
                    intent.getBundleExtra("bundle").getString("time"),
                    intent.getBundleExtra("bundle").getDouble("rating"),
                    eventIndex,
                    intent.getBundleExtra("bundle").getString("mainLocationType"),
                    intent.getBundleExtra("bundle").getString("locationSubtype"),
                    places, invitees);
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
            new sendInvites().execute(eventRef.getPath().toString());
        }
    }

    class sendInvites extends AsyncTask<String, Contact, Void> {
        @Override
        protected Void doInBackground(String... path) {
            for(int i=0;i<invitees.size();i++)
            {
                final int finI = i;
                final String finPath = path[0];
                mRoot.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        for(DataSnapshot child : dataSnapshot.getChildren()) {
                            if(child.child("phone").getValue().toString().equals(invitees.get(finI).getPhone()) && !child.child("phone").getValue().toString().equals(sp.getString("phone","")))
                            {
                                child.child("invited-events").getRef().push().setValue(finPath);
                                break;
                            }
                            else
                            {
                                //send sms message
                            }
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            startActivity(new Intent(ContactsListActivity.this, MainActivity.class));
        }
    }


}