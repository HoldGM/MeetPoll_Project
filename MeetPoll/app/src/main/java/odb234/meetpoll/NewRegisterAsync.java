package odb234.meetpoll;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.util.Log;

import com.firebase.client.Firebase;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

/**
 * Created by Otis on 8/22/2016.
 */
public class NewRegisterAsync extends AsyncTask<Void, Void, Void> {
    DatabaseReference dbref;
    SharedPreferences sp;
    Context mContext;
    private static final String TAG = "Register ASYNC";
    public NewRegisterAsync(Context context){
        mContext = context;
    }
    @Override
    protected void onPreExecute() {
        dbref = FirebaseDatabase.getInstance().getReference();
        sp = PreferenceManager.getDefaultSharedPreferences(mContext);
        dbref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for(DataSnapshot child : dataSnapshot.getChildren()){
                    if(!child.getKey().equals(sp.getString("Uid", ""))) {
                        for (DataSnapshot eachEvent : child.child("events").getChildren()) {
                            for(DataSnapshot eachInvite : eachEvent.child("inviteList").getChildren()){
                                if(eachInvite.child("phone").getValue(String.class).equals(sp.getString("phone", ""))){
                                    DatabaseReference tempRef = dbref.child(sp.getString("Uid", "")).child("invited-events").getRef().push();
                                    Log.d(TAG, eachInvite.child("name").getValue(String.class));
                                    Firebase tempBase = new Firebase(dbref.child(child.getKey().toString()).child(eachEvent.getKey().toString()).getRef().toString());
                                    Log.d(TAG, dbref.child(child.getKey().toString()).child(eachEvent.getKey().toString()).getRef().toString());
                                    tempRef.setValue(tempBase.getPath());
                                    eachInvite.getRef().child("invitePath").setValue(tempRef.toString());
                                }
                            }
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
    protected Void doInBackground(Void... voids) {

        return null;
    }

    @Override
    protected void onPostExecute(Void aVoid) {
        super.onPostExecute(aVoid);
    }
}
