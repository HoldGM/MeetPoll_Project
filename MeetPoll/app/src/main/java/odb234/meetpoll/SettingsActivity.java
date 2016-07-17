package odb234.meetpoll;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.EditText;

/**
 * Created by rspiegel on 7/13/16.
 */
public class SettingsActivity extends PreferenceActivity {
    private static final String tag = "settings";

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        getFragmentManager().beginTransaction().replace(android.R.id.content, new MySettingsFragment()).commit();
    }

    public static class MySettingsFragment extends PreferenceFragment {
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.preferences);

            final EditTextPreference et = (EditTextPreference)findPreference("name_preference");
            et.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                                                 public boolean onPreferenceChange(Preference pref, Object newV) {
                                                     Log.d(tag, newV.toString());
                                                     et.setSummary(newV.toString());
                                                     return true;
                                                 }
                                             }
            );
        }
    }
}