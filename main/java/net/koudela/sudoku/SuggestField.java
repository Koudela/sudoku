package net.koudela.sudoku;

import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.widget.Toast;

import java.util.Set;

class SuggestField extends AsyncTask<Void, Void, Integer> {
    private Playground mainButtonsText;
    private Hints hints;
    private String message;

    SuggestField(Hints hints, Playground pField) {
        this.hints = hints;
        mainButtonsText = pField;
    }

    @Override
    protected Integer doInBackground(Void... placeholder) {
        SharedPreferences sPref = PreferenceManager.getDefaultSharedPreferences(MainActivity.getContext());
        Set<Integer> arrId;
        if (!sPref.getBoolean(PreferencesFragment.KEY_PREF_HINT, false)) {
            Toast.makeText(MainActivity.getContext(), "activate hints in settings", Toast.LENGTH_SHORT).show();
            return null;
        }
        if (sPref.getBoolean(PreferencesFragment.KEY_PREF_AUTO_INSERT1HINT, false)) {
            Integer[] ai1arr = mainButtonsText.getAutoInsert1(hints);
            if (ai1arr != null) {
                message = "Hint Insert 1";
                return ai1arr[0];
            }
        }
        if (sPref.getBoolean(PreferencesFragment.KEY_PREF_AUTO_INSERT1HINT, false)) {
            Integer[] ai2arr = mainButtonsText.getAutoInsert2(hints);
            if (ai2arr != null) {
                message = "Hint Insert 2";
                return ai2arr[0];
            }
        }
        if (sPref.getBoolean(PreferencesFragment.KEY_PREF_AUTO_ADV1HINT, false)) {
            arrId = hints.setAutoHintsAdv1(mainButtonsText, true);
            if (!arrId.isEmpty()) {
                message = "Hint Advanced 1";
                return (int) arrId.toArray()[0];
            }
        }
        if (sPref.getBoolean(PreferencesFragment.KEY_PREF_AUTO_ADV2HINT, false)) {
            arrId = hints.setAutoHintsAdv2(mainButtonsText, true, true);
            if (!arrId.isEmpty()) {
                message = "Hint Advanced 2";
                return (int) arrId.toArray()[0];
            }
        }
        if (sPref.getBoolean(PreferencesFragment.KEY_PREF_AUTO_ADV3HINT, false)) {
            arrId = hints.setAutoHintsAdv3(mainButtonsText, true, true);
            if (!arrId.isEmpty()) {
                message = "Hint Advanced 3";
                return (int) arrId.toArray()[0];
            }
        }
        if (sPref.getBoolean(PreferencesFragment.KEY_PREF_AUTO_ADV2HINT, false)) {
            arrId = hints.setAutoHintsAdv2(mainButtonsText, true, false);
            if (!arrId.isEmpty()) {
                message = "Hint Advanced 2";
                return (int) arrId.toArray()[0];
            }
        }
        if (sPref.getBoolean(PreferencesFragment.KEY_PREF_AUTO_ADV3HINT, false)) {
            arrId = hints.setAutoHintsAdv3(mainButtonsText, true, false);
            if (!arrId.isEmpty()) {
                message = "Hint Advanced 3";
                return (int) arrId.toArray()[0];
            }
        }
        message = "No Hint available";
        return null;
    }

    @Override
    protected void onPostExecute(Integer arrId) {
        Toast.makeText(MainActivity.getContext(), message, Toast.LENGTH_SHORT).show();
        if (arrId != null) ((MainActivity) MainActivity.getContext()).sudokuData.setActionHint(arrId);
    }
}
