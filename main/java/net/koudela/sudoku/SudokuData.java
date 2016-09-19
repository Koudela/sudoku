package net.koudela.sudoku;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

public class SudokuData {
    private static final SudokuData Singleton = new SudokuData();
    private final static int DIM = Sudoku.DIM;
    private Sudoku sudoku = Sudoku.getInstance();
    private Playground mainButtonsText = new Playground();
    // Hints.hint get even calculated if they don't get shown to the user,
    // we need them to find badUserInput and to show actionbar hints
    private Hints hints = new Hints();
    private Boolean[] isBlocked = new Boolean[DIM*DIM];
    private Boolean[] isAutoInsert = new Boolean[DIM*DIM];
    private int[] textColorHints = new int[DIM*DIM];
    private int arrIdEasyTouchButton;
    private int requestViewId;
    private boolean useAutoInsert1;
    private boolean useAutoInsert2;

    private SudokuData() {
        initPreferences();
        initData();
    }

    public void startBuilder() {
        sudoku.startBuilder();
    }

    public static SudokuData getInstance() {
        return Singleton;
    }

    public void initPreferences() {
        Context context = MainActivity.getContext();
        SharedPreferences sPrefs = PreferenceManager.getDefaultSharedPreferences(context);
        boolean devOptionsIsSet = sPrefs.getBoolean(PreferencesFragment.KEY_PREF_DEVELOPMENT_OPTIONS, false);
        hints.setUsePlain(sPrefs.getBoolean(PreferencesFragment.KEY_PREF_AUTO_HINT, false));
        hints.setUseAdv1(devOptionsIsSet && sPrefs.getBoolean(PreferencesFragment.KEY_PREF_AUTO_HINT_ADV1, false));
        hints.setUseAdv2(devOptionsIsSet && sPrefs.getBoolean(PreferencesFragment.KEY_PREF_AUTO_HINT_ADV2, false));
        hints.setUseAdv3(devOptionsIsSet && sPrefs.getBoolean(PreferencesFragment.KEY_PREF_AUTO_HINT_ADV3, false));
        useAutoInsert1 = (devOptionsIsSet && sPrefs.getBoolean(PreferencesFragment.KEY_PREF_AUTO_INSERT1, false));
        useAutoInsert2 = (devOptionsIsSet && sPrefs.getBoolean(PreferencesFragment.KEY_PREF_AUTO_INSERT2, false));
        sudoku.setLevel(Integer.valueOf(sPrefs.getString(PreferencesFragment.KEY_PREF_LEVEL, "1")));
    }

    public void initData() {
        initIsAutoInsert();
        arrIdEasyTouchButton = -1;
        requestViewId = -1;
    }

    public void initIsAutoInsert() {
        for (int arrId : Sudoku.ALL_ARR_IDS) isAutoInsert[arrId] = false;
    }

    public void resetGame(final Button[] mainButtons, final TextView[] helperTextViews) {
        initData();
        mainButtonsText = sudoku.get();
        hints.init(mainButtonsText);
        for (int arrId : Sudoku.ALL_ARR_IDS) isBlocked[arrId] = (mainButtonsText.isPopulated(arrId));
        for (int arrId : Sudoku.ALL_ARR_IDS) {
            setMainButtonsContent(arrId, mainButtons, helperTextViews);
            setHelperTextViewContent(arrId, helperTextViews);
        }
    }

    public void newGame(final Button[] mainButtons, final TextView[] helperTextViews) {
        sudoku.getNewSudoku();
        resetGame(mainButtons, helperTextViews);
    }

    public void setMainButtonsContent(final int arrId, Button[] mainButtons, TextView[] helperTextViews) {
        int number = mainButtonsText.get(arrId);
        Log.v("setMainBC", number + ";" + arrId);
        // is (now) empty
        if (number == 0) {
            // we don't know witch advanced hints are invalid
            hints.initAdv();
            mainButtons[arrId].setText("");
            // making the hint 'visible'; (hint is the background for button!)
            helperTextViews[arrId].setTextColor(textColorHints[arrId]);
        }
        // is (now) populated
        else {
            mainButtons[arrId].setText(String.valueOf(number));
            updateMainButtonColor(arrId, mainButtons);
        }
        setBackgroundWithRespectToEasyTouchArea(arrId, helperTextViews);
    }

    public void setBackgroundWithRespectToEasyTouchArea(final int arrId, TextView[] helperTextViews) {
        Context context = MainActivity.getContext();
        if (arrId == arrIdEasyTouchButton) {
            helperTextViews[arrId].setBackgroundColor(ContextCompat.getColor(context, R.color.backgroundTouched));
            if (mainButtonsText.isPopulated(arrId)) {
                // making the hint 'invisible'; (hint is the background for button!)
                helperTextViews[arrId].setTextColor(ContextCompat.getColor(context, R.color.backgroundTouched));
            }
        } else {
            helperTextViews[arrId].setBackgroundColor(ContextCompat.getColor(context, R.color.backgroundUntouched));
            if (mainButtonsText.isPopulated(arrId)) {
                // making the hint 'invisible'; (hint is the background for button!)
                helperTextViews[arrId].setTextColor(ContextCompat.getColor(context, R.color.backgroundUntouched));
            }
        }
    }

    public void setEasyTouchArea(final int arrId, TextView[] helperTextViews) {
        Context context = MainActivity.getContext();
        if (arrIdEasyTouchButton != arrId && arrIdEasyTouchButton >= 0) {
            helperTextViews[arrIdEasyTouchButton].setBackgroundColor(ContextCompat.getColor(context, R.color.backgroundUntouched));
            if (mainButtonsText.isPopulated(arrIdEasyTouchButton))
                helperTextViews[arrIdEasyTouchButton].setTextColor(ContextCompat.getColor(context, R.color.backgroundUntouched));
        }
        if (arrId != -1 && isBlocked[arrId]) arrIdEasyTouchButton = -1;
        else arrIdEasyTouchButton = arrId;
        if (arrIdEasyTouchButton != -1) {
            helperTextViews[arrIdEasyTouchButton].setBackgroundColor(ContextCompat.getColor(context, R.color.backgroundTouched));
            if (mainButtonsText.isPopulated(arrIdEasyTouchButton))
                helperTextViews[arrIdEasyTouchButton].setTextColor(ContextCompat.getColor(context, R.color.backgroundTouched));
        }
    }

    public void updateMainButtonColor(final int arrId, Button[] mainButtons) {
        if (!mainButtonsText.isPopulated(arrId)) return;
        Context context = MainActivity.getContext();
        if (isBlocked[arrId]) mainButtons[arrId].setTextColor(ContextCompat.getColor(context, R.color.textColorIsBlocked));
        else if (hints.getPlainHint(arrId, mainButtonsText.get(arrId) - 1) > 1) mainButtons[arrId].setTextColor(ContextCompat.getColor(context, R.color.textColorBadUserInput));
        else if (PreferenceManager.getDefaultSharedPreferences(context).getBoolean(PreferencesFragment.KEY_PREF_MARK_ERROR, false)
            && mainButtonsText.get(arrId) != sudoku.get(0).get(arrId))
            mainButtons[arrId].setTextColor(ContextCompat.getColor(context, R.color.textColorMarkError));
        else if (isAutoInsert[arrId]) mainButtons[arrId].setTextColor(ContextCompat.getColor(context, R.color.textColorAutoInsert));
        else mainButtons[arrId].setTextColor(ContextCompat.getColor(context, R.color.textColorUserInput));
    }

    public void setHelperTextViewContent(final int arrId, TextView[] helperTextViews) {
        Log.v("setHelperTVC", arrId+"");
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
        helperTextViews[arrId].setText(text);
        Context context = MainActivity.getContext();
        if (userHints) textColorHints[arrId] = ContextCompat.getColor(context, R.color.userHints);
        else if (autoHints) textColorHints[arrId] = ContextCompat.getColor(context, R.color.autoHints);
        else textColorHints[arrId] = ContextCompat.getColor(context, R.color.noHints);
        if (helperTextViews[arrId].getCurrentTextColor() != ContextCompat.getColor(context, R.color.backgroundUntouched))
            helperTextViews[arrId].setTextColor(textColorHints[arrId]);
    }

    public void suggestField(TextView[] helperTextViews) {
        SharedPreferences sPref = PreferenceManager.getDefaultSharedPreferences(MainActivity.getContext());
        if (!sPref.getBoolean(PreferencesFragment.KEY_PREF_HINT, false)) {
            Toast.makeText(MainActivity.getContext(), "activate hints in settings", Toast.LENGTH_SHORT).show();
            return;
        }
        if (sPref.getBoolean(PreferencesFragment.KEY_PREF_AUTO_INSERT1HINT, false)) {
            Integer[] ai1arr = mainButtonsText.getAutoInsert1(hints);
            if (ai1arr != null) {
                setEasyTouchArea(ai1arr[0], helperTextViews);
                Toast.makeText(MainActivity.getContext(), "Hint Insert 1", Toast.LENGTH_SHORT).show();
                return;
            }
        }
        if (sPref.getBoolean(PreferencesFragment.KEY_PREF_AUTO_INSERT1HINT, false)) {
            Integer[] ai2arr = mainButtonsText.getAutoInsert2(hints);
            if (ai2arr != null) {
                setEasyTouchArea(ai2arr[0], helperTextViews);
                Toast.makeText(MainActivity.getContext(), "Hint Insert 2", Toast.LENGTH_SHORT).show();
                return;
            }
        }
        int arrId;
        if (sPref.getBoolean(PreferencesFragment.KEY_PREF_AUTO_ADV1HINT, false)) {
            arrId = hints.setAutoHintsAdv1(mainButtonsText, true);
            if (arrId != -1) {
                setEasyTouchArea(arrId, helperTextViews);
                Toast.makeText(MainActivity.getContext(), "Hint Advanced 1", Toast.LENGTH_SHORT).show();
                return;
            }
        }
        if (sPref.getBoolean(PreferencesFragment.KEY_PREF_AUTO_ADV2HINT, false)) {
            arrId = hints.setAutoHintsAdv2(mainButtonsText, true);
            if (arrId != -1) {
                setEasyTouchArea(arrId, helperTextViews);
                Toast.makeText(MainActivity.getContext(), "Hint Advanced 2", Toast.LENGTH_SHORT).show();
                return;
            }
        }
        if (sPref.getBoolean(PreferencesFragment.KEY_PREF_AUTO_ADV3HINT, false)) {
            arrId = hints.setAutoHintsAdv3(mainButtonsText, true);
            if (arrId != -1) {
                setEasyTouchArea(arrId, helperTextViews);
                Toast.makeText(MainActivity.getContext(), "Hint Advanced 3", Toast.LENGTH_SHORT).show();
                return;
            }
        }
        Toast.makeText(MainActivity.getContext(), "No Hint available", Toast.LENGTH_SHORT).show();
    }

    private void updateSudoku(Set<Integer> arrIdsChangedHints, Set<Integer> arrIdsChangedValues, Button[] mainButtons, TextView[] helperTextViews) {
        Sudoku.updateSudokuStart(arrIdsChangedHints, arrIdsChangedValues, mainButtonsText, hints, useAutoInsert1, useAutoInsert2);
        Log.v("...", arrIdsChangedHints.toString());
        for (int arrId : arrIdsChangedHints) this.setHelperTextViewContent(arrId, helperTextViews);
        for (int arrId : arrIdsChangedValues) {
            isAutoInsert[arrId] = true;
            this.setMainButtonsContent(arrId, mainButtons, helperTextViews);
        }
        suggestField(helperTextViews);

    }
    public void updateSudoku(final int number, final int arrId, Button[] mainButtons, TextView[] helperTextViews) {
        Set<Integer> arrIdsChangedHints = new HashSet<>();
        Set<Integer> arrIdsChangedValues = new HashSet<>();
        if (number != 0) arrIdsChangedHints.addAll(hints.incrementStarGroup(arrId, number - 1, mainButtonsText));
        else {
            arrIdsChangedHints.addAll(hints.decrementStarGroup(arrId, mainButtonsText.get(arrId) - 1, mainButtonsText));
            hints.initAdv();
            arrIdsChangedHints.add(arrId);
        }
        mainButtonsText.set(arrId, number);
        this.setMainButtonsContent(arrId, mainButtons, helperTextViews);
        updateSudoku(arrIdsChangedHints, arrIdsChangedValues, mainButtons, helperTextViews);
    }

    public void updateSudoku(Button[] mainButtons, TextView[] helperTextViews) {
        HashSet<Integer> arrIdsChangedHints = new HashSet<>();
        HashSet<Integer> arrIdsChangedValues = new HashSet<>();
        updateSudoku(arrIdsChangedHints, arrIdsChangedValues, mainButtons, helperTextViews);
    }

    public void redrawHints(TextView[] helperTextViews) {
        for (int arrId : new ArrayList<>(mainButtonsText.getNotPopulatedArrIds()))
            setHelperTextViewContent(arrId, helperTextViews);
    }

    public void initAdv() {
        hints.initAdv();
    }

    public void initAdv1() {
        hints.initAdv1();
    }

    public void initAdv2() {
        hints.initAdv2();
    }

    public void initAdv3() {
        hints.initAdv3();
    }

    public Set<Integer> getPopulatedArrIds() {
        return mainButtonsText.getPopulatedArrIds();
    }

     public void setUserHint(final int arrId, final int num) {
        hints.setUserHint(arrId, num);
    }

    public boolean isHint(final int arrId, final int num) {
        return hints.isHint(arrId, num);
    }

    public int getMainButtonTextValue(final int arrId) {
        return mainButtonsText.get(arrId);
    }

    public int getArrIdEasyTouchButton() {
        return arrIdEasyTouchButton;
    }

    public void setRequestViewId(int arrId) {
        requestViewId = arrId;
    }

    public int getRequestViewId() {
        return requestViewId;
    }

    public void logInfo() {
        sudoku.logInfo();
    }
}