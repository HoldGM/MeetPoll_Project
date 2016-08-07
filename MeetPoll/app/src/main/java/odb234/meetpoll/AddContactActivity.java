package odb234.meetpoll;

import android.*;
import android.app.ProgressDialog;
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
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.client.Firebase;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Date;

public class AddContactActivity extends AppCompatActivity {

    private final static String TAG = "Add Contact Activity";
    ListView inviteList;
    ArrayList<Contact> invitees;
    ArrayList<Contact> contacts;
    ContentResolver cr;
    ListAdapter la;
    String path;
    DatabaseReference mRef;

    ProgressDialog pd;

    SharedPreferences sp;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_contact);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        contacts = new ArrayList<>();
        cr = getContentResolver();
        inviteList = (ListView)findViewById(R.id.add_contact_list);
        path = getIntent().getStringExtra("path");
        pd = new ProgressDialog(AddContactActivity.this, ProgressDialog.STYLE_SPINNER);
        sp = PreferenceManager.getDefaultSharedPreferences(this);
        mRef = FirebaseDatabase.getInstance().getReference();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if(checkSelfPermission(android.Manifest.permission.READ_CONTACTS) == PackageManager.PERMISSION_GRANTED)
                populateList();
            else
                requestPermissions(new String[]{android.Manifest.permission.READ_CONTACTS}, 10);
        }else{
            populateList();
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

        la = new ContactsAdapter(this, contacts);
        inviteList.setAdapter(la);
        DatabaseReference lRef = mRef.child((PreferenceManager.getDefaultSharedPreferences(this).getString("Uid", ""))).child("events").child(path).child("inviteList");

        lRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot child : dataSnapshot.getChildren()) {
                    for (int i = 0; i < contacts.size(); i++) {
                        if (child.child("phone").getValue().toString().equals(contacts.get(i).getPhone())) {
                            contacts.get(i).setState(true);
                        }
                    }
                }
                ((ContactsAdapter)inviteList.getAdapter()).newList(contacts);
                inviteList.setAdapter(la);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        Log.d(TAG, "Contacts List size: " + contacts.size());
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch(item.getItemId()){
            case android.R.id.home:
                finish();
                return true;
            case R.id.send_event:
                sendNewInvites();
                return true;
            case R.id.settings:
                Intent intent2 = new Intent(AddContactActivity.this,SettingsActivity.class);
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

    private void collectInvites(){
        int listCount = inviteList.getAdapter().getCount();
        invitees = new ArrayList<>();
        for(int i = 0; i< listCount; i++){
            View listView = inviteList.getAdapter().getView(i, null, null);
            if(((CheckBox)listView.findViewById(R.id.contact_select)).isChecked())
                invitees.add((Contact)(inviteList.getAdapter()).getItem(i));
        }
    }

    private void sendNewInvites(){
        final DatabaseReference lRef = mRef.child((sp.getString("Uid", ""))).child("events").child(path).child("inviteList");
        Log.w(TAG, lRef.toString());

        lRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                collectInvites();
                long count = dataSnapshot.getChildrenCount();
                for(int i = 0; i < invitees.size(); i++) {
                    boolean alreadyInvited = false;
                    final int finI = i;
                    for (DataSnapshot child : dataSnapshot.getChildren()) {
                        if (child.child("phone").getValue().toString().equals(invitees.get(i).getPhone())) {
                            alreadyInvited = true;
                            break;
                        }
                    }
                    if (!alreadyInvited) {
                        DatabaseReference tempRef = FirebaseDatabase.getInstance().getReference();
                        final long c = count;
                        tempRef.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                for (DataSnapshot child : dataSnapshot.getChildren()) {
                                    if (child.child("phone").getValue(String.class).equals(invitees.get(finI).getPhone())) {
                                        String str = "/" + sp.getString("Uid", "") + "/events/" + path;
                                        DatabaseReference temp = child.child("invited-events").getRef().push();
                                        invitees.get(finI).setInvitePath(temp.toString());
                                        temp.setValue(str);
                                    }
                                }
                                lRef.child("" + c).setValue(invitees.get(finI));
                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {

                            }
                        });

                        count++;
                    }
                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
        Toast.makeText(this, "New Invites sent.", Toast.LENGTH_LONG).show();
    }

    private class ContactsAdapter extends BaseAdapter {
        ArrayList<Contact> contactsList;
        LayoutInflater inflater;
        public ContactsAdapter(Context context, ArrayList<Contact> list){
            contactsList = list;
            inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }
        public void newList(ArrayList<Contact> list){
            contactsList = list;
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
                final CheckBox tempCheck = checkBox;
                DatabaseReference ref = FirebaseDatabase.getInstance().getReference().child(sp.getString("Uid", "")).child("events").child(path).child("inivteList");
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
