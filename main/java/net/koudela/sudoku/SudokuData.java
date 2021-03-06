package net.koudela.sudoku;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v4.content.ContextCompat;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static android.util.Log.e;
import static net.koudela.sudoku.SudokuGroups.DIM;

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
    private Deque<Map<Integer, Integer>> history = new ArrayDeque<>();
    private int[] highScore = new int[5];
    private SudokuSolver solver = new SudokuSolver();
    private Sudoku sudoku = Sudoku.getInstance();
    private Playground mainButtonsText = new Playground();
    // plain hints even get calculated if they don't get shown to the user,
    // we need them to find badUserInput and to show actionbar hints
    private Hints hints = new Hints(true, false, false, false, true);
    private Boolean[] isBlocked = new Boolean[DIM * DIM];
    private Boolean[] isAutoInsert = new Boolean[DIM * DIM];
    private int[] textColorHints = new int[DIM * DIM];
    private int[] score = new int[DIM * DIM + 1];
    private int arrIdEasyTouchButton, requestViewId, arrIdLastHint;
    private boolean useAutoInsert1, useAutoInsert2;

    private SudokuData() {}

    static SudokuData getInstance() {
        return Singleton;
    }

    void startBuilder() {
        sudoku.startBuilder();
    }

    final void initPreferences() {
        Context context = MainActivity.getContext();
        SharedPreferences sPrefs = PreferenceManager.getDefaultSharedPreferences(context);
        boolean devOptionsIsSet = sPrefs.getBoolean(PreferencesFragment.
                KEY_PREF_DEVELOPMENT_OPTIONS, false);
        if (sPrefs.getBoolean(PreferencesFragment.KEY_PREF_AUTO_HINT, true)) {
            hints.setUsePlain(true);
            hints.populatePlainHints(mainButtonsText);
        } else hints.setUsePlain(false);
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
        arrIdLastHint = -1;
        for (int i = 0; i < score.length; i++) score[i] = 0;
        setScore();
        history.clear();
    }

    void resetGame(boolean firstRun) {
        if (firstRun) initPreferences();
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

    void goBack() {
        if (history.isEmpty()) return;
        Map<Integer, Integer> map = history.pop();
        Set<Integer> changed = new HashSet<>();
        for (int arrId : map.keySet()) {
            if (arrId < 0) {
                mainButtonsText.set(-1 - arrId, map.get(arrId));
                setMainButtonsContent(-1 - arrId, false);
                changed.addAll(hints.incrementStarGroup(-1 - arrId, map.get(arrId) - 1, mainButtonsText));
            } else if (arrId < DIM * DIM) {
                score[arrId] = 0;
                mainButtonsText.set(arrId, 0);
                setMainButtonsContent(arrId, false);
                changed.addAll(hints.decrementStarGroup(arrId, map.get(arrId) - 1, mainButtonsText));
            } else if (arrId == DIM * DIM) {
                score[DIM * DIM] -= map.get(DIM * DIM);
            } else {
                hints.setUserHint(arrId - DIM * DIM, map.get(arrId));
                changed.add(arrId - DIM * DIM);
            }
        }
        setScore();
        hints.initAdv();
        changed.addAll(hints.updateAdv1(mainButtonsText));
        changed.addAll(hints.updateAdv2(mainButtonsText, false));
        changed.addAll(hints.updateAdv3(mainButtonsText, false));
        for (int arrId : changed) setHelperTextViewContent(arrId);
    }

    boolean isOldGame() {
        return (mainButtonsText.getPopulatedArrIds().size() > 0);
    }

    void setScore() {
        ((TextView) ((MainActivity) MainActivity.getContext()).findViewById(R.id.score))
                .setText((score[DIM * DIM] * DIM) + " " + MainActivity.getContext().getResources().getText(R.string.score));
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
                && sudoku.getLevel() != 5
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

    void suggestField(boolean showMessage) {
        SharedPreferences sPref = PreferenceManager.getDefaultSharedPreferences(MainActivity.getContext());
        if (!sPref.getBoolean(PreferencesFragment.KEY_PREF_HINT, false)) {
            if (showMessage) Toast.makeText(MainActivity.getContext(), "activate hints in settings", Toast.LENGTH_SHORT).show();
            return;
        }
        SuggestField suggestField = new SuggestField(hints, mainButtonsText);
        suggestField.execute();
    }

    void setActionHint(Integer arrId) {
        arrIdLastHint = arrId;
        setEasyTouchArea(arrId);
    }

    private void updateSudoku(Set<Integer> arrIdsChangedHints, Set<Integer> arrIdsChangedValues) {
        solver.init(mainButtonsText, hints, arrIdsChangedHints, arrIdsChangedValues, useAutoInsert1, useAutoInsert2, false);
        solver.updateSudoku(false);
        for (int arrId : arrIdsChangedHints) this.setHelperTextViewContent(arrId);
        for (int arrId : arrIdsChangedValues) {
            isAutoInsert[arrId] = true;
            this.setMainButtonsContent(arrId, false);
        }
        suggestField(false);

    }

    @SuppressLint("UseSparseArrays")
    void updateSudoku(final int number, final int arrId) {
        Set<Integer> arrIdsChangedHints = new HashSet<>();
        Set<Integer> arrIdsChangedValues = new HashSet<>();
        history.push(new HashMap<Integer, Integer>());
        if (number != 0) {
            history.getFirst().put(arrId, number);
            arrIdsChangedHints.addAll(hints.incrementStarGroup(arrId, number - 1, mainButtonsText));
            score[arrId] = arrIdsChangedHints.size();
            for (int num = 0; num < DIM; num++) if (!isHint(arrId, num)) score[arrId]++;
            if (arrIdLastHint == arrId) score[arrId]--;
            history.getFirst().put(DIM * DIM, score[arrId]);
            score[DIM * DIM] += score[arrId];
        }
        else {
            history.getFirst().put(-1 - arrId, mainButtonsText.get(arrId));
            arrIdsChangedHints.addAll(hints.decrementStarGroup(arrId, mainButtonsText.get(arrId) - 1, mainButtonsText));
            hints.initAdv();
            arrIdsChangedHints.add(arrId);
            history.getFirst().put(DIM * DIM, -score[arrId]);
            score[DIM * DIM] -= score[arrId];
            score[arrId] = 0;
        }
        arrIdLastHint = -1;
        mainButtonsText.set(arrId, number);
        this.setMainButtonsContent(arrId, false);
        setScore();
        updateSudoku(arrIdsChangedHints, arrIdsChangedValues);
        for (int tempArrId: arrIdsChangedValues)
            history.getFirst().put(tempArrId, mainButtonsText.get(tempArrId));
    }

    @SuppressLint("UseSparseArrays")
    void updateSudokuHintVersion(final int arrId, final int num) {
        ScoreUserHint scoreUserHint = new ScoreUserHint(arrId, num, hints, mainButtonsText);
        scoreUserHint.execute();
        history.push(new HashMap<Integer, Integer>());
        history.getFirst().put(arrId + DIM * DIM, num);
        HashSet<Integer> arrIdsChangedHints = new HashSet<>();
        HashSet<Integer> arrIdsChangedValues = new HashSet<>();
        updateSudoku(arrIdsChangedHints, arrIdsChangedValues);
        if (!arrIdsChangedValues.isEmpty())
            for (int tempArrId : arrIdsChangedValues)
                history.getFirst().put(tempArrId, mainButtonsText.get(tempArrId));
    }

    void setScore(final int arrId, int points) {
        if (history.getFirst().get(DIM * DIM) != null) {
            e("SudokuData", "setScore is out of sync");
            return;
        }
        if (arrIdLastHint == arrId) points--;
        arrIdLastHint = -1;
        history.getFirst().put(DIM * DIM, points);
        score[DIM * DIM] += points;
        setScore();
    }

    void redrawHints() {
        for (int arrId : new ArrayList<>(mainButtonsText.getNotPopulatedArrIds())) {
            setHelperTextViewContent(arrId);
        }
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

    boolean setRequestViewId(int arrId) {
        requestViewId = arrId;
        return isBlocked[arrId];
    }

    int getRequestViewId() {
        return requestViewId;
    }

    void logInfo() {
        sudoku.logInfo();
    }
}