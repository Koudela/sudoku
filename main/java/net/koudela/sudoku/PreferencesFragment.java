package net.koudela.sudoku;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceFragment;

/**
 * This fragment defines the handling of the settings UI for {@link  PreferencesActivity}.
 *
 * @author Thomas Koudela
 * @version 0.? stable
 */
public class PreferencesFragment extends PreferenceFragment implements SharedPreferences.OnSharedPreferenceChangeListener {
    public static final String KEY_PREF_EASY_TOUCH = "pref_easy_touch";
    public static final String KEY_PREF_LEVEL = "pref_level";
    public static final String KEY_PREF_AUTO_HINT = "pref_auto_hint";
    public static final String KEY_PREF_HINT = "pref_hint";
    public static final String KEY_PREF_AUTO_INSERT1HINT = "pref_auto_insert1hint";
    public static final String KEY_PREF_AUTO_INSERT2HINT = "pref_auto_insert2hint";
    public static final String KEY_PREF_AUTO_ADV1HINT = "pref_auto_adv1hint";
    public static final String KEY_PREF_AUTO_ADV2HINT = "pref_auto_adv2hint";
    public static final String KEY_PREF_AUTO_ADV3HINT = "pref_auto_adv3hint";
    public static final String KEY_PREF_MARK_ERROR = "pref_mark_error";
    public static final String KEY_PREF_FONT_SIZE_MAIN = "pref_font_size_main";
    public static final String KEY_PREF_FONT_SIZE_HELPER = "pref_font_size_helper";
    public static final String KEY_PREF_FONT_SIZE_INPUT = "pref_font_size_input";
    public static final String KEY_PREF_DEVELOPMENT_OPTIONS = "pref_development_options";
    public static final String KEY_PREF_AUTO_HINT_ADV1 = "pref_auto_hint_adv1";
    public static final String KEY_PREF_AUTO_HINT_ADV2 = "pref_auto_hint_adv2";
    public static final String KEY_PREF_AUTO_HINT_ADV3 = "pref_auto_hint_adv3";
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
        SudokuData sudokuData = SudokuData.getInstance();
        switch (key) {
            case (PreferencesFragment.KEY_PREF_LEVEL):
                sudokuData.initPreferences();
                sudokuData.resetGame(false);
                break;
            case (PreferencesFragment.KEY_PREF_MARK_ERROR):
                for (int arrId : sudokuData.getPopulatedArrIds())
                    sudokuData.updateMainButtonColor(arrId);
                break;
            case (PreferencesFragment.KEY_PREF_FONT_SIZE_MAIN):
                MainActivity.setTextSizeMainButtons();
                break;
            case (PreferencesFragment.KEY_PREF_FONT_SIZE_HELPER):
                MainActivity.setTextSizeHelperTextViews();
                break;
            case (PreferencesFragment.KEY_PREF_AUTO_HINT):
                sudokuData.initPreferences();
                sudokuData.redrawHints();
                break;
            case (PreferencesFragment.KEY_PREF_AUTO_HINT_ADV1):
                sudokuData.initPreferences();
                sudokuData.initAdv1();
                sudokuData.redrawHints();
                break;
            case (PreferencesFragment.KEY_PREF_AUTO_HINT_ADV2):
                sudokuData.initPreferences();
                sudokuData.initAdv2();
                sudokuData.redrawHints();
                break;
            case (PreferencesFragment.KEY_PREF_AUTO_HINT_ADV3):
                sudokuData.initPreferences();
                sudokuData.initAdv3();
                sudokuData.redrawHints();
                break;
            case (PreferencesFragment.KEY_PREF_DEVELOPMENT_OPTIONS):
                sudokuData.initPreferences();
                sudokuData.initAdv();
                sudokuData.redrawHints();
                break;
            case (PreferencesFragment.KEY_PREF_AUTO_INSERT1):
            case (PreferencesFragment.KEY_PREF_AUTO_INSERT1HINT):
            case (PreferencesFragment.KEY_PREF_AUTO_INSERT2):
            case (PreferencesFragment.KEY_PREF_AUTO_INSERT2HINT):
                sudokuData.initPreferences();
                break;
        }
    }
}
