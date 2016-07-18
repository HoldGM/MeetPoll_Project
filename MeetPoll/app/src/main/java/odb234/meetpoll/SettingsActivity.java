package odb234.meetpoll;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.FragmentManager;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.text.InputType;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

/**
 * Created by rspiegel on 7/17/16.
 */
public class SettingsActivity extends AppCompatActivity {
    private static TextView tv;
    private static SharedPreferences sp;
    private static SharedPreferences.Editor editor;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        tv = (TextView)findViewById(R.id.your_name);
        sp = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
        tv.setText(sp.getString("name",tv.getText().toString()));
        editor = sp.edit();

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    public void changeName(View v)
    {
        FragmentManager fm = getFragmentManager();
        nameFragment nf = new nameFragment();
        nf.show(fm, "nameFragment");
    }

    public static class nameFragment extends DialogFragment
    {
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState)
        {
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            final EditText userInput = new EditText(getContext());
            userInput.setText(tv.getText());
            userInput.setSelection(userInput.length());
            userInput.setInputType(InputType.TYPE_TEXT_FLAG_CAP_WORDS);
         
            builder.setView(userInput);
            builder.setMessage("Change your name: ")
                    .setPositiveButton("Save", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            String input = userInput.getText().toString();
                            tv.setText(input);
                            editor.putString("name",input);
                            editor.apply();
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
