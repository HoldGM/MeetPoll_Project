package odb234.meetpoll;

import android.*;
import android.Manifest;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.FragmentManager;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.text.InputType;
import android.util.Log;
import android.util.LruCache;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.firebase.client.Firebase;
import com.github.pinball83.maskededittext.MaskedEditText;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.zip.Inflater;

/**
 * Created by rspiegel on 7/17/16.
 */
public class SettingsActivity extends AppCompatActivity {
    private static TextView nameTV;
    private static SharedPreferences sp;
    private static SharedPreferences.Editor editor;
    private static FragmentManager fm;
    private static TextView phoneTV;
    private static LayoutInflater inflate;

    private static final String TAG = "Settings Activity";

    static Firebase fb;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        inflate = getLayoutInflater();
        nameTV = (TextView)findViewById(R.id.your_name);
        phoneTV = (TextView)findViewById(R.id.your_phone);
        sp = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
        nameTV.setText(sp.getString("name",nameTV.getText().toString()));
        String phone = sp.getString("phone", "");
        Log.d(TAG,"Phone : " + phone);
        if(!phone.equals(""))
            phoneTV.setText("(" + phone.substring(0,3) + ")" + phone.substring(3,6) + "-" + phone.substring(6));
        else
            phoneTV.setText("Change Me");
        editor = sp.edit();
        fm = getFragmentManager();
        fb = new Firebase(getString(R.string.firebase_path) + "/" + sp.getString("Uid",""));
        Log.d(TAG,fb.toString());

    }

    public void changeName(View v)
    {
        nameFragment nf = new nameFragment();
        nf.show(fm, "nameFragment");
    }

    public static class nameFragment extends DialogFragment
    {
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState)
        {
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            final EditText userInput = new EditText(getActivity());
            userInput.setText(nameTV.getText());
            userInput.setSelection(userInput.length());
            userInput.setInputType(InputType.TYPE_TEXT_FLAG_CAP_WORDS);
         
            builder.setView(userInput);
            builder.setMessage("Change your name?")
                    .setPositiveButton("Save", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            String input = userInput.getText().toString();
                            nameTV.setText(input);
                            editor.putString("name", input);
                            editor.apply();
                            fb.child("username").setValue(input);
                            dismiss();
                        }
                    })
                    .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            dismiss();
                        }
                    });
            return builder.create();
        }
    }
    public void changePhone(View v) {
        phoneFragment pf = new phoneFragment();
        pf.show(fm, "phoneFragment");
    }
    public static class phoneFragment extends DialogFragment
    {
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState)
        {
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            final View v = inflate.inflate(R.layout.fragment_phone,null);
            final MaskedEditText userInput = (MaskedEditText)v.findViewById(R.id.settings_phone);
            userInput.setMaskedText(sp.getString("phone", ""));

            builder.setView(v);
            builder.setMessage("Change your phone number?")
                    .setPositiveButton("Save", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            String input = userInput.getText().toString();
                            phoneTV.setText(input);
                            editor.putString("phone", userInput.getUnmaskedText());
                            editor.apply();
                            fb.child("phone").setValue(userInput.getUnmaskedText());
                            dismiss();
                        }
                    })
                    .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            dismiss();
                        }
                    });
            return builder.create();
        }
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId() == android.R.id.home){
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

}
