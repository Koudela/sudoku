package net.koudela.sudoku;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * Holds the current game state and applies it to the UI. Runs the game logic methods and get called
 * by the game/settings listeners. This Singleton is the only Part of the game that is retained if
 * MainActivity gets destroyed.
 *
 * Warning: The method signatures do not tell if the method implicit relies on the main context or
 * views. They get the main context by a static weak reference and views by a static strong reference.
 *
 * @author Thomas Koudela
 * @version 0.? beta
 */
class SudokuData {
    private static final SudokuData Singleton = new SudokuData();
    private final static int DIM = Sudoku.DIM;
    private Sudoku sudoku = Sudoku.getInstance();
    private Playground mainButtonsText = new Playground();
    // plain hints even get calculated if they don't get shown to the user,
    // we need them to find badUserInput and to show actionbar hints
    private Hints hints = new Hints(true, false, false, false, true);
    private Boolean[] isBlocked = new Boolean[DIM*DIM];
    private Boolean[] isAutoInsert = new Boolean[DIM*DIM];
    private int[] textColorHints = new int[DIM*DIM];
    private int arrIdEasyTouchButton;
    private int requestViewId;
    private boolean useAutoInsert1;
    private boolean useAutoInsert2;

    private SudokuData() {
        initPreferences();
    }

    static SudokuData getInstance() {
        return Singleton;
    }

    void startBuilder() {
        sudoku.startBuilder();
    }

    void initPreferences() {
        Context context = MainActivity.getContext();
        SharedPreferences sPrefs = PreferenceManager.getDefaultSharedPreferences(context);
        boolean devOptionsIsSet = sPrefs.getBoolean(PreferencesFragment.
                KEY_PREF_DEVELOPMENT_OPTIONS, false);
        hints.setUsePlain(sPrefs.getBoolean(PreferencesFragment.KEY_PREF_AUTO_HINT, false));
        hints.setUseAdv1(devOptionsIsSet && sPrefs.getBoolean(PreferencesFragment.
                KEY_PREF_AUTO_HINT_ADV1, false));
        hints.setUseAdv2(devOptionsIsSet && sPrefs.getBoolean(PreferencesFragment.
                KEY_PREF_AUTO_HINT_ADV2, false));
        hints.setUseAdv3(devOptionsIsSet && sPrefs.getBoolean(PreferencesFragment.
                KEY_PREF_AUTO_HINT_ADV3, false));
        useAutoInsert1 = (devOptionsIsSet && sPrefs.getBoolean(PreferencesFragment.
                KEY_PREF_AUTO_INSERT1, false));
        useAutoInsert2 = (devOptionsIsSet && sPrefs.getBoolean(PreferencesFragment.
                KEY_PREF_AUTO_INSERT2, false));
        sudoku.setLevel(sPrefs.getString(PreferencesFragment.KEY_PREF_LEVEL, "1"));
    }

    private void initData() {
        for (int arrId : Sudoku.ALL_ARR_IDS) isAutoInsert[arrId] = false;
        arrIdEasyTouchButton = -1;
        requestViewId = -1;
    }

    void resetGame(boolean firstRun) {
        initData();
        mainButtonsText = sudoku.get();
        hints.init(mainButtonsText, firstRun, false);
        for (int arrId : Sudoku.ALL_ARR_IDS) {
            isBlocked[arrId] = mainButtonsText.isPopulated(arrId);
            setMainButtonsContent(arrId, false);
            setHelperTextViewContent(arrId);
        }
    }

    void newGame() {
        sudoku.getNewSudoku();
        resetGame(false);
    }

    boolean isOldGame() {
        return (mainButtonsText.getPopulatedArrIds().size() > 0);
    }

    void setMainButtonsContent(final int arrId, boolean isInit) {
        int number = mainButtonsText.get(arrId);
        // is (now) empty
        if (number == 0) {
            // we don't know witch advanced hints are invalid
            if (!isInit) hints.initAdv();
            MainActivity.mainButtons[arrId].setText("");
            // making the hint 'visible'; (hint is the background for button!)
            if (!isInit) MainActivity.helperTextViews[arrId].setTextColor(textColorHints[arrId]);
        }
        // is (now) populated
        else {
            MainActivity.mainButtons[arrId].setText(String.valueOf(number));
            updateMainButtonColor(arrId);
        }
        setBackgroundWithRespectToEasyTouchArea(arrId);
    }

    private void setBackgroundWithRespectToEasyTouchArea(final int arrId) {
        Context context = MainActivity.getContext();
        if (arrId == arrIdEasyTouchButton) {
            MainActivity.helperTextViews[arrId].setBackgroundColor(ContextCompat.getColor(context, R.color.backgroundTouched));
            if (mainButtonsText.isPopulated(arrId)) {
                // making the hint 'invisible'; (hint is the background for button!)
                MainActivity.helperTextViews[arrId].setTextColor(ContextCompat.getColor(context, R.color.backgroundTouched));
            }
        } else {
            MainActivity.helperTextViews[arrId].setBackgroundColor(ContextCompat.getColor(context, R.color.backgroundUntouched));
            if (mainButtonsText.isPopulated(arrId)) {
                // making the hint 'invisible'; (hint is the background for button!)
                MainActivity.helperTextViews[arrId].setTextColor(ContextCompat.getColor(context, R.color.backgroundUntouched));
            }
        }
    }

    void setEasyTouchArea(final int arrId) {
        Context context = MainActivity.getContext();
        if (arrIdEasyTouchButton != arrId && arrIdEasyTouchButton >= 0) {
            MainActivity.helperTextViews[arrIdEasyTouchButton].setBackgroundColor(ContextCompat.getColor(context, R.color.backgroundUntouched));
            if (mainButtonsText.isPopulated(arrIdEasyTouchButton))
                MainActivity.helperTextViews[arrIdEasyTouchButton].setTextColor(ContextCompat.getColor(context, R.color.backgroundUntouched));
        }
        if (arrId != -1 && isBlocked[arrId]) arrIdEasyTouchButton = -1;
        else arrIdEasyTouchButton = arrId;
        if (arrIdEasyTouchButton != -1) {
            MainActivity.helperTextViews[arrIdEasyTouchButton].setBackgroundColor(ContextCompat.getColor(context, R.color.backgroundTouched));
            if (mainButtonsText.isPopulated(arrIdEasyTouchButton))
                MainActivity.helperTextViews[arrIdEasyTouchButton].setTextColor(ContextCompat.getColor(context, R.color.backgroundTouched));
        }
    }

    void updateMainButtonColor(final int arrId) {
        if (!mainButtonsText.isPopulated(arrId)) return;
        Context context = MainActivity.getContext();
        if (isBlocked[arrId]) MainActivity.mainButtons[arrId].setTextColor(ContextCompat.getColor(context, R.color.textColorIsBlocked));
        else if (hints.getPlainHint(arrId, mainButtonsText.get(arrId) - 1) > 1) MainActivity.mainButtons[arrId].setTextColor(ContextCompat.getColor(context, R.color.textColorBadUserInput));
        else if (PreferenceManager.getDefaultSharedPreferences(context).getBoolean(PreferencesFragment.KEY_PREF_MARK_ERROR, false)
            && mainButtonsText.get(arrId) != sudoku.get(0).get(arrId))
            MainActivity.mainButtons[arrId].setTextColor(ContextCompat.getColor(context, R.color.textColorMarkError));
        else if (isAutoInsert[arrId]) MainActivity.mainButtons[arrId].setTextColor(ContextCompat.getColor(context, R.color.textColorAutoInsert));
        else MainActivity.mainButtons[arrId].setTextColor(ContextCompat.getColor(context, R.color.textColorUserInput));
    }

    void setHelperTextViewContent(final int arrId) {
        String text = "";
        boolean userHints = false;
        boolean autoHints = false;
        for (int num = 0; num < DIM; num++) {
            boolean isHint = hints.isHint(arrId, num);
            text +=  isHint ? (num + 1) : "-";
            if (hints.isUserHint(arrId, num)) {
                if (!userHints) userHints = true;
            } else if (!autoHints && isHint) autoHints = true;
            if (num % 3 == 2) text += "\n";
        }
        MainActivity.helperTextViews[arrId].setText(text);
        Context context = MainActivity.getContext();
        if (userHints) textColorHints[arrId] = ContextCompat.getColor(context, R.color.userHints);
        else if (autoHints) textColorHints[arrId] = ContextCompat.getColor(context, R.color.autoHints);
        else textColorHints[arrId] = ContextCompat.getColor(context, R.color.noHints);
        if (MainActivity.helperTextViews[arrId].getCurrentTextColor() != ContextCompat.getColor(context, R.color.backgroundUntouched))
            MainActivity.helperTextViews[arrId].setTextColor(textColorHints[arrId]);
    }

    void suggestField() {
        SharedPreferences sPref = PreferenceManager.getDefaultSharedPreferences(MainActivity.getContext());
        Set<Integer> arrId;
        if (!sPref.getBoolean(PreferencesFragment.KEY_PREF_HINT, false)) {
            Toast.makeText(MainActivity.getContext(), "activate hints in settings", Toast.LENGTH_SHORT).show();
            return;
        }
        if (sPref.getBoolean(PreferencesFragment.KEY_PREF_AUTO_INSERT1HINT, false)) {
            Integer[] ai1arr = mainButtonsText.getAutoInsert1(hints);
            if (ai1arr != null) {
                setEasyTouchArea(ai1arr[0]);
                Toast.makeText(MainActivity.getContext(), "Hint Insert 1", Toast.LENGTH_SHORT).show();
                return;
            }
        }
        if (sPref.getBoolean(PreferencesFragment.KEY_PREF_AUTO_INSERT1HINT, false)) {
            Integer[] ai2arr = mainButtonsText.getAutoInsert2(hints);
            if (ai2arr != null) {
                setEasyTouchArea(ai2arr[0]);
                Toast.makeText(MainActivity.getContext(), "Hint Insert 2", Toast.LENGTH_SHORT).show();
                return;
            }
        }
        if (sPref.getBoolean(PreferencesFragment.KEY_PREF_AUTO_ADV1HINT, false)) {
            arrId = hints.setAutoHintsAdv1(mainButtonsText, true);
            if (!arrId.isEmpty()) {
                setEasyTouchArea((int) arrId.toArray()[0]);
                Toast.makeText(MainActivity.getContext(), "Hint Advanced 1", Toast.LENGTH_SHORT).show();
                return;
            }
        }
        if (sPref.getBoolean(PreferencesFragment.KEY_PREF_AUTO_ADV2HINT, false)) {
            arrId = hints.setAutoHintsAdv2(mainButtonsText, true, true);
            if (!arrId.isEmpty()) {
                Log.v("setEasyTouch",Arrays.toString(arrId.toArray()));
                setEasyTouchArea((int) arrId.toArray()[0]);
                Toast.makeText(MainActivity.getContext(), "Hint Advanced 2", Toast.LENGTH_SHORT).show();
                return;
            }
        }
        if (sPref.getBoolean(PreferencesFragment.KEY_PREF_AUTO_ADV3HINT, false)) {
            arrId = hints.setAutoHintsAdv3(mainButtonsText, true, true);
            if (!arrId.isEmpty()) {
                setEasyTouchArea((int) arrId.toArray()[0]);
                Toast.makeText(MainActivity.getContext(), "Hint Advanced 3", Toast.LENGTH_SHORT).show();
                return;
            }
        }
        if (sPref.getBoolean(PreferencesFragment.KEY_PREF_AUTO_ADV2HINT, false)) {
            arrId = hints.setAutoHintsAdv2(mainButtonsText, true, false);
            if (!arrId.isEmpty()) {
                Log.v("setEasyTouch",Arrays.toString(arrId.toArray()));
                setEasyTouchArea((int) arrId.toArray()[0]);
                Toast.makeText(MainActivity.getContext(), "Hint Advanced 2", Toast.LENGTH_SHORT).show();
                return;
            }
        }
        if (sPref.getBoolean(PreferencesFragment.KEY_PREF_AUTO_ADV3HINT, false)) {
            arrId = hints.setAutoHintsAdv3(mainButtonsText, true, false);
            if (!arrId.isEmpty()) {
                setEasyTouchArea((int) arrId.toArray()[0]);
                Toast.makeText(MainActivity.getContext(), "Hint Advanced 3", Toast.LENGTH_SHORT).show();
                return;
            }
        }
        Toast.makeText(MainActivity.getContext(), "No Hint available", Toast.LENGTH_SHORT).show();
    }

    private void updateSudoku(Set<Integer> arrIdsChangedHints, Set<Integer> arrIdsChangedValues) {
        Sudoku.updateSudokuStart(arrIdsChangedHints, arrIdsChangedValues, mainButtonsText, hints, useAutoInsert1, useAutoInsert2, true);
        Log.v("...", arrIdsChangedHints.toString());
        for (int arrId : arrIdsChangedHints) this.setHelperTextViewContent(arrId);
        for (int arrId : arrIdsChangedValues) {
            isAutoInsert[arrId] = true;
            this.setMainButtonsContent(arrId, false);
        }
        suggestField();

    }
    void updateSudoku(final int number, final int arrId) {
        Set<Integer> arrIdsChangedHints = new HashSet<>();
        Set<Integer> arrIdsChangedValues = new HashSet<>();
        if (number != 0) arrIdsChangedHints.addAll(hints.incrementStarGroup(arrId, number - 1, mainButtonsText));
        else {
            arrIdsChangedHints.addAll(hints.decrementStarGroup(arrId, mainButtonsText.get(arrId) - 1, mainButtonsText));
            hints.initAdv();
            arrIdsChangedHints.add(arrId);
        }
        mainButtonsText.set(arrId, number);
        this.setMainButtonsContent(arrId, false);
        updateSudoku(arrIdsChangedHints, arrIdsChangedValues);
    }

    void updateSudoku() {
        HashSet<Integer> arrIdsChangedHints = new HashSet<>();
        HashSet<Integer> arrIdsChangedValues = new HashSet<>();
        updateSudoku(arrIdsChangedHints, arrIdsChangedValues);
    }

    void redrawHints() {
        for (int arrId : new ArrayList<>(mainButtonsText.getNotPopulatedArrIds()))
            setHelperTextViewContent(arrId);
    }

    void initAdv() {
        hints.initAdv();
    }

    void initAdv1() {
        hints.initAdv1();
    }

    void initAdv2() {
        hints.initAdv2();
    }

    void initAdv3() {
        hints.initAdv3();
    }

    Set<Integer> getPopulatedArrIds() {
        return mainButtonsText.getPopulatedArrIds();
    }

    void setUserHint(final int arrId, final int num) {
        hints.setUserHint(arrId, num);
    }

    boolean isHint(final int arrId, final int num) {
        return hints.isHint(arrId, num);
    }

    int getMainButtonTextValue(final int arrId) {
        return mainButtonsText.get(arrId);
    }

    int getArrIdEasyTouchButton() {
        return arrIdEasyTouchButton;
    }

    void setRequestViewId(int arrId) {
        requestViewId = arrId;
    }

    int getRequestViewId() {
        return requestViewId;
    }

    void logInfo() {
        sudoku.logInfo();
    }
}