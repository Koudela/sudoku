package net.koudela.sudoku;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceFragment;
import android.util.Log;

public class PreferencesFragment extends PreferenceFragment implements SharedPreferences.OnSharedPreferenceChangeListener {
    public static final String KEY_PREF_EASY_TOUCH = "pref_easy_touch";
    public static final String KEY_PREF_LEVEL = "pref_level";
    public static final String KEY_PREF_AUTO_HINT = "pref_auto_hint";
    public static final String KEY_PREF_AUTO_INSERT1HINT = "pref_auto_insert1hint";
    public static final String KEY_PREF_AUTO_INSERT2HINT = "pref_auto_insert2hint";
    public static final String KEY_PREF_MARK_ERROR = "pref_mark_error";
    public static final String KEY_PREF_FONT_SIZE_MAIN = "pref_font_size_main";
    public static final String KEY_PREF_FONT_SIZE_HELPER = "pref_font_size_helper";
    public static final String KEY_PREF_FONT_SIZE_INPUT = "pref_font_size_input";
    public static final String KEY_PREF_AUTO_HINT_ADV1 = "pref_auto_hint_adv1";
    public static final String KEY_PREF_AUTO_HINT_ADV2 = "pref_auto_hint_adv2";
    public static final String KEY_PREF_AUTO_INSERT1 = "pref_auto_insert1";
    public static final String KEY_PREF_AUTO_INSERT2 = "pref_auto_insert2";

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
            case (PreferencesFragment.KEY_PREF_AUTO_HINT):
                Log.v(key, "" + sharedPreferences.getBoolean(key, false));
                break;
            case (PreferencesFragment.KEY_PREF_AUTO_HINT_ADV1):
                Log.v(key, "" + sharedPreferences.getBoolean(key, false));
                break;
            case (PreferencesFragment.KEY_PREF_AUTO_HINT_ADV2):
                Log.v(key, "" + sharedPreferences.getBoolean(key, false));
                break;
            case (PreferencesFragment.KEY_PREF_AUTO_INSERT1):
                Log.v(key, "" + sharedPreferences.getBoolean(key, false));
                break;
            case (PreferencesFragment.KEY_PREF_AUTO_INSERT2):
                Log.v(key, "" + sharedPreferences.getBoolean(key, false));
                break;
        }
    }
}
