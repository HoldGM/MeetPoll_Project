package odb234.meetpoll;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.telephony.PhoneNumberFormattingTextWatcher;
import android.telephony.PhoneNumberUtils;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;

import com.fasterxml.jackson.databind.util.ISO8601Utils;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.github.pinball83.maskededittext.MaskedEditText;

import java.util.Locale;

public class ProfileActivity extends AppCompatActivity {

    private static String TAG = "ProfileActivity ";

    EditText nameText;
    MaskedEditText phoneText;
    SharedPreferences sp;
    SharedPreferences.Editor editor;
    static Firebase fb;
    static String uid;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        Firebase.setAndroidContext(this);
        fb = new Firebase("https://steadfast-leaf-137323.firebaseio.com/");
        sp = PreferenceManager.getDefaultSharedPreferences(this);
        editor = sp.edit();
        nameText = (EditText) findViewById(R.id.profile_enter_name);
        phoneText = (MaskedEditText) findViewById(R.id.profile_enter_phone);


    }
    public void saveProfile(View v){
        String name = nameText.getText().toString();
        String phone = phoneText.getUnmaskedText().toString();
        editor.putString("name", name);
        editor.putString("phone", phone);
        editor.apply();
        uid = sp.getString("Uid", "");
        fb.child(uid).child("username").setValue(name);
        fb.child(uid).child("phone").setValue(phone);
        fb.child(uid).child("eventCount").setValue(0);
        Intent intent = new Intent(ProfileActivity.this, MainActivity.class);
        startActivity(intent);
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()){
            case android.R.id.home:
                fb.unauth();
                LoginActivity.mAuth.signOut();
                LoginActivity.registerFlag = true;
                finish();
                break;
        }
        return super.onOptionsItemSelected(item);
    }
}
