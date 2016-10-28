package com.meri_sg.places_finder;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.preference.SwitchPreference;

/**
 * Created on 16-Aug-16.
 */

//handle configurations screen
public class Preference extends PreferenceActivity {
    SharedPreferences prefs;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.prefs);

        prefs = PreferenceManager.getDefaultSharedPreferences(this);

        ListPreference prefunit =(ListPreference) findPreference("unit");
        prefunit.setSummary(prefs.getString("unit",getResources().getString(R.string.kilometers_miles)));
        prefunit.setEntries(new String[]{"Kilometers","Miles"});
        prefunit.setEntryValues(new String[]{"Kilometers","Miles"});
        prefunit.setOnPreferenceChangeListener(new android.preference.Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(android.preference.Preference preference, Object newValue) {
                preference.setSummary((String)newValue);
                return true;
            }
        });


        ListPreference prefradius =(ListPreference) findPreference("radius");
        prefradius.setSummary(prefs.getString("radius","500"));
        prefradius.setEntries(new String[]{"200","500","1000","2000","3000"});
        prefradius.setEntryValues(new String[]{"200","500","1000","2000","3000"});
        prefradius.setOnPreferenceChangeListener(new android.preference.Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(android.preference.Preference preference, Object newValue) {
                preference.setSummary((String)newValue);
                return true;
            }
        });


        SwitchPreference delete=(SwitchPreference)findPreference("deleteall");
        delete.setChecked(false);

        android.preference.Preference done=findPreference("done");
        done.setOnPreferenceClickListener(new android.preference.Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(android.preference.Preference preference) {

                Boolean deleteall=prefs.getBoolean("deleteall",false);
                if (deleteall){
                    DbHelper helper = new DbHelper(Preference.this);
                    helper.getWritableDatabase().delete(PlacesContract.PlacesFromApi.TABLE_NAME,PlacesContract.PlacesFromApi.FAVORIT+"='save'",null);
                }
                Preference.this.getContentResolver().notifyChange(PlacesContract.PlacesFromApi.CONTENT_URI,null);
                finish();
                return true;
            }
        });

    }//end of onCreate

}//end class
