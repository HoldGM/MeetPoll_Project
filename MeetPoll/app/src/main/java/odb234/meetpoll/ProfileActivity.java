package odb234.meetpoll;

import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;

import com.firebase.client.Firebase;

public class ProfileActivity extends AppCompatActivity {

    EditText nameText;
    EditText phoneText;
    SharedPreferences sp;
    SharedPreferences.Editor editor;
    Firebase fb;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        fb = new Firebase("https://steadfast-leaf-137323.firebaseio.com/");
        sp = PreferenceManager.getDefaultSharedPreferences(this);
        editor = sp.edit();
        nameText = (EditText) findViewById(R.id.profile_enter_name);
        phoneText = (EditText) findViewById(R.id.profile_enter_phone);

    }

    public void saveProfile(View v){
        String name = nameText.getText().toString();
        String phone = phoneText.getText().toString();
        editor.putString("name", name);
        editor.putString("phone", phone);
        editor.apply();
        String uid = sp.getString("Uid", "");
        fb.child(uid).child("username").setValue(name);
        fb.child(uid).child("phone").setValue(phone);
        Intent intent = new Intent(ProfileActivity.this, MainActivity.class);
        startActivity(intent);
    }
}
