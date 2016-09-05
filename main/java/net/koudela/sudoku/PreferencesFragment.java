package net.koudela.sudoku;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceFragment;
import android.util.Log;

public class PreferencesFragment extends PreferenceFragment implements SharedPreferences.OnSharedPreferenceChangeListener {
    public static final String KEY_PREF_EASY_TOUCH = "pref_easy_touch";
    public static final String KEY_PREF_AUTO_HINT = "pref_auto_hint";
    public static final String KEY_PREF_FONT_SIZE_MAIN = "pref_font_size_main";
    public static final String KEY_PREF_FONT_SIZE_HELPER = "pref_font_size_helper";
    public static final String KEY_PREF_FONT_SIZE_INPUT = "pref_font_size_input";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preferences);
    }
    @Override
    public void onResume() {
        super.onResume();
        getPreferenceScreen().getSharedPreferences()
                .registerOnSharedPreferenceChangeListener(this);
    }
    @Override
    public void onPause() {
        super.onPause();
        getPreferenceScreen().getSharedPreferences()
                .unregisterOnSharedPreferenceChangeListener(this);
    }
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        switch (key) {
            case (PreferencesFragment.KEY_PREF_EASY_TOUCH):
                Log.v(key, "" + sharedPreferences.getBoolean(key, false));
                break;
            case (PreferencesFragment.KEY_PREF_AUTO_HINT):
                Log.v(key, "" + sharedPreferences.getBoolean(key, false));
                break;
            case (PreferencesFragment.KEY_PREF_FONT_SIZE_MAIN):
                Log.v(key, sharedPreferences.getString(key, ""));
                break;
            case (PreferencesFragment.KEY_PREF_FONT_SIZE_HELPER):
                Log.v(key, sharedPreferences.getString(key, ""));
                break;
            case (PreferencesFragment.KEY_PREF_FONT_SIZE_INPUT):
                Log.v(key, sharedPreferences.getString(key, ""));
                break;
        }
    }
}
