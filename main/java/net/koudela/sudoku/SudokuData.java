package net.koudela.sudoku;

import android.content.Context;
import android.preference.PreferenceManager;
import android.support.v4.content.ContextCompat;
import android.widget.Button;
import android.widget.TextView;

import java.util.LinkedHashSet;
import java.util.Set;

public class SudokuData {
    private static final SudokuData Singleton = new SudokuData();
    public final static int DIM = Sudoku.DIM;
    protected Sudoku sudoku = Sudoku.getInstance();
    protected Boolean[] isAutoInsert = new Boolean[DIM*DIM];
    protected int[] mainButtonsText = new int[DIM*DIM];
    protected int[] textColorHints = new int[DIM*DIM];
    protected Boolean[] isBlocked = new Boolean[DIM*DIM];
    protected Boolean[][] isUserHint = new Boolean[DIM*DIM][DIM];
    protected int[][] autoHint = new int[DIM*DIM][DIM];
    protected int[][] autoHintAdv1 = new int[DIM*DIM][DIM];
    protected int[][] autoHintAdv2 = new int[DIM*DIM][DIM];
    protected int[][] autoHintAdv3 = new int[DIM*DIM][DIM];
    protected int arrIdEasyTouchButton;
    protected int requestViewId;

    private SudokuData() {
        initData();
        initAutoHints();
        initAutoHintsAdv1();
        initAutoHintsAdv2();
        initAutoHintsAdv3();
    }

    public static SudokuData getInstance() {
        return Singleton;
    }

    public void initData() {
        initIsAutoInsert();
        initIsUserHint();
        arrIdEasyTouchButton = -1;
        requestViewId = -1;
    }

    public void resetGame(Button[] mainButtons, TextView[] helperTextViews, Context context) {
        initData();
        sudoku.setLevel(PreferenceManager.getDefaultSharedPreferences(context).getString(PreferencesFragment.KEY_PREF_LEVEL, "1"));
        mainButtonsText = sudoku.getSudoku();
        resetAutoHints(context);
        resetAutoHintsAdv1(context);
        resetAutoHintsAdv2(context);
        resetAutoHintsAdv3(context);
        for (int arrId : Sudoku.ALL_ARR_IDS) isBlocked[arrId] = (mainButtonsText[arrId] != 0);
        for (int arrId : Sudoku.ALL_ARR_IDS) {
            setMainButtonsContent(-1, arrId, mainButtons, helperTextViews, context, true);
            setHelperTextViewContent(arrId, mainButtons, helperTextViews, context, true);
        }
    }

    public void newGame(Button[] mainButtons, TextView[] helperTextViews, Context context) {
        sudoku.init();
        resetGame(mainButtons, helperTextViews, context);
    }

    // autoHints get even calculated if they don't get shown to the user, we need them to find badUserInput and to show actionbar hints
    public boolean isHint(int number, int arrId, boolean includeAutoHints) {
        return (isUserHint[arrId][number - 1]
                || includeAutoHints && autoHint[arrId][number - 1] > 0
                || autoHintAdv1[arrId][number - 1] > 0
                || autoHintAdv2[arrId][number - 1] > 0
                || autoHintAdv3[arrId][number - 1] > 0);
    }

    public boolean isPrefAutoHint(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context).getBoolean(PreferencesFragment.KEY_PREF_AUTO_HINT, false);
    }

    public boolean isPrefAutoHintAdv1(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context).getBoolean(PreferencesFragment.KEY_PREF_AUTO_HINT_ADV1, false)
                && PreferenceManager.getDefaultSharedPreferences(context).getBoolean(PreferencesFragment.KEY_PREF_DEVELOPMENT_OPTIONS, false);
    }

    public boolean isPrefAutoHintAdv2(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context).getBoolean(PreferencesFragment.KEY_PREF_AUTO_HINT_ADV2, false)
                && PreferenceManager.getDefaultSharedPreferences(context).getBoolean(PreferencesFragment.KEY_PREF_DEVELOPMENT_OPTIONS, false);
    }

    public boolean isPrefAutoHintAdv3(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context).getBoolean(PreferencesFragment.KEY_PREF_AUTO_HINT_ADV3, false)
                && PreferenceManager.getDefaultSharedPreferences(context).getBoolean(PreferencesFragment.KEY_PREF_DEVELOPMENT_OPTIONS, false);
    }

    public boolean isPrefAutoInsert1(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context).getBoolean(PreferencesFragment.KEY_PREF_AUTO_INSERT1, false)
                && PreferenceManager.getDefaultSharedPreferences(context).getBoolean(PreferencesFragment.KEY_PREF_DEVELOPMENT_OPTIONS, false);
    }

    public boolean isPrefAutoInsert2(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context).getBoolean(PreferencesFragment.KEY_PREF_AUTO_INSERT2, false)
                && PreferenceManager.getDefaultSharedPreferences(context).getBoolean(PreferencesFragment.KEY_PREF_DEVELOPMENT_OPTIONS, false);
    }


    public void initIsAutoInsert() {
        for (int arrId : Sudoku.ALL_ARR_IDS) isAutoInsert[arrId] = false;
    }

    public void initIsUserHint() {
        for (int arrId : Sudoku.ALL_ARR_IDS) for (int num = 0; num < DIM; num++) isUserHint[arrId][num] = false;
    }

    public int setUserHint(int number, int arrId) {
        isUserHint[arrId][number - 1] = !isUserHint[arrId][number - 1];
        return arrId;
    }

    public void initAutoHints() {
        for (int arrId : Sudoku.ALL_ARR_IDS) for (int num = 0; num < DIM; num++) autoHint[arrId][num] = 0;
    }

    public void resetAutoHints(Context context) {
        initAutoHints();
        if (PreferenceManager.getDefaultSharedPreferences(context).getBoolean(PreferencesFragment.KEY_PREF_AUTO_HINT, false))
            for (int arrId : Sudoku.ALL_ARR_IDS)
                if (mainButtonsText[arrId] != 0)
                    for (int tempArrId : Sudoku.getStarGroup(arrId)) autoHint[tempArrId][mainButtonsText[arrId] - 1]++;
    }

    // returns the arrIds of the manipulated hints
    public Set<Integer> setAutoHints (int number, int arrId, Button[] mainButtons, Context context, boolean isDeletion) {
        // it does not matter that autoHint++/-- is called thrice for arrId:
        // 1. the call is symmetric and only > 0 is evaluated
        // 2. the field is populated
        if (number == 0) return new LinkedHashSet<Integer>(){};
        for (int tempArrId: SudokuGroups.getStarGroup(arrId)) {
            autoHint[tempArrId][number - 1] += isDeletion ? -1 : 1;
            if (number == mainButtonsText[tempArrId]) updateMainButtonColor(tempArrId, mainButtons, context);
        }
        if (isPrefAutoHint(context)) return SudokuGroups.getStarGroup(arrId);
        else return new LinkedHashSet<Integer>(){};
    }

    public void initAutoHintsAdv1() {
        for (int arrId : Sudoku.ALL_ARR_IDS) for (int number = 0; number < DIM; number++) autoHintAdv1[arrId][number] = 0;
    }

    // if a number is group wise bounded to a specific row/column, in the same row/column other groups get hints
    public Set<Integer> resetAutoHintsAdv1(Context context) {
        Set<Integer> returnSet = new LinkedHashSet<>();
        initAutoHintsAdv1();
        if (isPrefAutoHintAdv1(context))
            for (int number = 1; number <= DIM; number++)
                for (int cnt = 0; cnt < DIM; cnt++) {
                    // check vertical
                    int tempArrId = -1;
                    for (int arrId : SudokuGroups.GROUPED_GROUPS[cnt])
                        if (!isHint(number, arrId, isPrefAutoHint(context)) && mainButtonsText[arrId] == 0)
                            if (tempArrId == -1) tempArrId = arrId;
                            else if (arrId != tempArrId + 1 && arrId != tempArrId + 2) {
                                tempArrId = -1;
                                break;
                            }
                    // add hints vertical
                    if (tempArrId != -1) {
                        for (int arrId : SudokuGroups.getComplementVerticalGroup(tempArrId)) autoHintAdv1[arrId][number-1]++;
                        returnSet.addAll(SudokuGroups.getComplementVerticalGroup(tempArrId));
                    }
                    // check horizontal
                    tempArrId = -1;
                    for (int arrId : SudokuGroups.GROUPED_GROUPS[cnt])
                        if (!isHint(number, arrId, isPrefAutoHint(context)) && mainButtonsText[arrId] == 0)
                            if (tempArrId == -1) tempArrId = arrId;
                            else if (arrId != tempArrId + 9 && arrId != tempArrId + 18) {
                                tempArrId = -1;
                                break;
                            }
                    // add hints vertical
                    if (tempArrId != -1) {
                        for (int arrId : SudokuGroups.getComplementHorizontalGroup(tempArrId)) autoHintAdv1[arrId][number-1]++;
                        returnSet.addAll(SudokuGroups.getComplementHorizontalGroup(tempArrId));
                    }
                }
        return returnSet;
    }

    public void initAutoHintsAdv2() {
        for (int arrId : Sudoku.ALL_ARR_IDS) for (int number = 0; number < DIM; number++) autoHintAdv2[arrId][number] = 0;
    }

    public void initAutoHintsAdv3() {
        for (int arrId : Sudoku.ALL_ARR_IDS) for (int number = 0; number < DIM; number++) autoHintAdv3[arrId][number] = 0;
    }

    // if in a group/row/column are n-fields with the same 9 minus n hints, hints get set in the other fields of the same group/row/column
    public Set<Integer> resetAutoHintsAdv2(Context context) {
        initAutoHintsAdv2();
        //TODO...resetAutoHintsAdv2 body
        return Sudoku.ALL_ARR_IDS;
    }

    // if in a group/row/column are n-fields with the same n hints or less missing, hints get set on the other numbers on the same fields
    public Set<Integer> resetAutoHintsAdv3(Context context) {
        initAutoHintsAdv3();
        //TODO...resetAutoHintsAdv3 body
        return Sudoku.ALL_ARR_IDS;
    }



    public void onUnsetMainButtonContent(int arrId, Button[] mainButtons, TextView[] helperTextViews, Context context) {
        Set<Integer> group = setAutoHints(mainButtonsText[arrId], arrId, mainButtons, context, true);
        for (Integer tempArrId : group) setHelperTextViewContent(tempArrId, mainButtons, helperTextViews, context, true);
    }

    public void onSetMainButtonContent(int arrId, Button[] mainButtons, TextView[] helperTextViews, Context context) {
        Set<Integer> group = setAutoHints(mainButtonsText[arrId], arrId, mainButtons, context, false);
        if (isPrefAutoHintAdv1(context))
            group.addAll(resetAutoHintsAdv1(context));
        if (isPrefAutoHintAdv2(context))
            group.addAll(resetAutoHintsAdv2(context));
        if (isPrefAutoHintAdv3(context))
            group.addAll(resetAutoHintsAdv3(context));
        for (Integer tempArrId : group) setHelperTextViewContent(tempArrId, mainButtons, helperTextViews, context, false);
        // SUB auto insert 2 looks at the whole playground, at the end of setMainButtonsContent it has a new state
        if (isPrefAutoInsert2(context)) {
            Integer[] ai2arr = getAutoInsert2(context);
            if (ai2arr != null) {
                isAutoInsert[ai2arr[1]] = true;
                setMainButtonsContent(ai2arr[0], ai2arr[1], mainButtons, helperTextViews, context, false);
            }
        } else if (PreferenceManager.getDefaultSharedPreferences(context).getBoolean(PreferencesFragment.KEY_PREF_AUTO_INSERT2HINT, false)) {
            Integer[] ai2arr = getAutoInsert2(context);
            if (ai2arr != null) setEasyTouchArea(ai2arr[1], helperTextViews, context);
        }
    }

    public void setMainButtonsContent(int number, int arrId, Button[] mainButtons, TextView[] helperTextViews, Context context, boolean preventOnUpdate) {
        // get
        if (number < 0) number = mainButtonsText[arrId];
            // set
        else {
            if (!preventOnUpdate) onUnsetMainButtonContent(arrId, mainButtons, helperTextViews, context);
            mainButtonsText[arrId] = number;
            if (!preventOnUpdate) onSetMainButtonContent(arrId, mainButtons, helperTextViews, context);
        }
        // is (now) empty
        if (number == 0) {
            mainButtons[arrId].setText("");
            // making the hint 'visible'; (hint is the background for button!)
            helperTextViews[arrId].setTextColor(textColorHints[arrId]);
        }
        // is (now) populated
        else {
            mainButtons[arrId].setText(String.valueOf(number));
            updateMainButtonColor(arrId, mainButtons, context);
        }
        setBackgroundWithRespectToEasyTouchArea(arrId, helperTextViews, context);
    }

    public void setBackgroundWithRespectToEasyTouchArea(int arrId, TextView[] helperTextViews, Context context) {
        if (arrId == arrIdEasyTouchButton) {
            helperTextViews[arrId].setBackgroundColor(ContextCompat.getColor(context, R.color.backgroundTouched));
            if (mainButtonsText[arrId] > 0) {
                // making the hint 'invisible'; (hint is the background for button!)
                helperTextViews[arrId].setTextColor(ContextCompat.getColor(context, R.color.backgroundTouched));
            }
        } else {
            helperTextViews[arrId].setBackgroundColor(ContextCompat.getColor(context, R.color.backgroundUntouched));
            if (mainButtonsText[arrId] > 0) {
                // making the hint 'invisible'; (hint is the background for button!)
                helperTextViews[arrId].setTextColor(ContextCompat.getColor(context, R.color.backgroundUntouched));
            }
        }
    }

    public void setEasyTouchArea(int arrId, TextView[] helperTextViews, Context context) {
        if (arrIdEasyTouchButton != arrId && arrIdEasyTouchButton >= 0) {
            helperTextViews[arrIdEasyTouchButton].setBackgroundColor(ContextCompat.getColor(context, R.color.backgroundUntouched));
            if (mainButtonsText[arrIdEasyTouchButton] != 0)
                helperTextViews[arrIdEasyTouchButton].setTextColor(ContextCompat.getColor(context, R.color.backgroundUntouched));
        }
        if (arrId != -1 && isBlocked[arrId]) arrIdEasyTouchButton = -1;
        else arrIdEasyTouchButton = arrId;
        if (arrIdEasyTouchButton != -1) {
            helperTextViews[arrIdEasyTouchButton].setBackgroundColor(ContextCompat.getColor(context, R.color.backgroundTouched));
            if (mainButtonsText[arrIdEasyTouchButton] != 0)
                helperTextViews[arrIdEasyTouchButton].setTextColor(ContextCompat.getColor(context, R.color.backgroundTouched));
        }
    }

    public void updateMainButtonColor(int arrId, Button[] mainButtons, Context context) {
        if (isBlocked[arrId]) mainButtons[arrId].setTextColor(ContextCompat.getColor(context, R.color.textColorIsBlocked));
        else if (autoHint[arrId][mainButtonsText[arrId] - 1] > 1) mainButtons[arrId].setTextColor(ContextCompat.getColor(context, R.color.textColorBadUserInput));
        else if (PreferenceManager.getDefaultSharedPreferences(context).getBoolean(PreferencesFragment.KEY_PREF_MARK_ERROR, false)
            && mainButtonsText[arrId] != sudoku.getSolutionField(arrId)) mainButtons[arrId].setTextColor(ContextCompat.getColor(context, R.color.textColorMarkError));
        else if (isAutoInsert[arrId]) mainButtons[arrId].setTextColor(ContextCompat.getColor(context, R.color.textColorAutoInsert));
        else mainButtons[arrId].setTextColor(ContextCompat.getColor(context, R.color.textColorUserInput));
    }

    public void onUpdateHelperTextViewContent(int arrId, Button[] mainButtons, TextView[] helperTextViews, Context context) {
        // SUB: auto insert 1 uses hints, if a hint is set setHelperTextViewContent is called
        if (mainButtonsText[arrId] == 0) {
            if (isPrefAutoInsert1(context)) {
                Integer[] ai1arr = getAutoInsert1ByField(arrId, context);
                if (ai1arr != null) {
                    isAutoInsert[ai1arr[1]] = true;
                    setMainButtonsContent(ai1arr[0], ai1arr[1], mainButtons, helperTextViews, context, false);
                }
            } else if (PreferenceManager.getDefaultSharedPreferences(context).getBoolean(PreferencesFragment.KEY_PREF_AUTO_INSERT1HINT, false)) {
                Integer[] ai1arr = getAutoInsert1ByField(arrId, context);
                if (ai1arr != null) setEasyTouchArea(ai1arr[1], helperTextViews, context);
            }
        }
    }

    public void setHelperTextViewContent(int arrId, Button[] mainButtons, TextView[] helperTextViews, Context context, boolean preventOnUpdate) {
        String text = "";
        boolean userHints = false;
        boolean autoHints = false;
        for (int num = 0; num < DIM; num++) {
            boolean isHint = isHint(num + 1, arrId, isPrefAutoHint(context));
            text +=  isHint ? (num + 1) : "-";
            if (isUserHint[arrId][num]) {
                if (!userHints) userHints = true;
            } else if (!autoHints && isHint) autoHints = true;
            if (num % 3 == 2) text += "\n";
        }
        helperTextViews[arrId].setText(text);
        if (userHints) textColorHints[arrId] = ContextCompat.getColor(context, R.color.userHints);
        else if (autoHints) textColorHints[arrId] = ContextCompat.getColor(context, R.color.autoHints);
        else textColorHints[arrId] = ContextCompat.getColor(context, R.color.noHints);
        if (helperTextViews[arrId].getCurrentTextColor() != ContextCompat.getColor(context, R.color.backgroundUntouched))
            helperTextViews[arrId].setTextColor(textColorHints[arrId]);
        if (!preventOnUpdate) onUpdateHelperTextViewContent(arrId, mainButtons, helperTextViews, context);
    }

    // if exact 8 hints are displayed it returns the missing number, null otherwise
    public Integer[] getAutoInsert1ByField(int arrId, Context context) {
        if (mainButtonsText[arrId] != 0) return null;
        int number = 0;
        for (int num = 1; num <= DIM; num++) if (!isHint(num, arrId, isPrefAutoHint(context))) {
            if (number != 0) return null;
            else number = num;
        }
        return new Integer[]{number, arrId};
    }

    // if a number is missing only once in 9 hint fields vertical, horizontal or group wise it gets returned
    // a populated field counts as a field with all possible 9 hints set
    // second returned int is the corresponding arrId
    // if nothing is found null gets returned
    public Integer[] getAutoInsert2ByGroup(Integer[] group, Context context) {
        for (int num = 1; num <= DIM; num++) {
            int count = 0;
            int tempArrId = -1;
            for (int arrId : group) {
                if (mainButtonsText[arrId] != 0 || isHint(num, arrId, isPrefAutoHint(context))) count++;
                else if (tempArrId == -1) tempArrId = arrId;
                else break;
            }
            if (count == 8) return new Integer[]{num, tempArrId};
        }
        return null;
    }

    public Integer[] getAutoInsert2(Context context) {
        for (int i = 0; i < DIM; i++) {
            Integer[] ai2arr = getAutoInsert2ByGroup(Sudoku.VERTICAL_GROUPS[i], context);
            if (ai2arr != null) return ai2arr;
            ai2arr = getAutoInsert2ByGroup(SudokuGroups.HORIZONTAL_GROUPS[i], context);
            if (ai2arr != null) return ai2arr;
            ai2arr = getAutoInsert2ByGroup(SudokuGroups.GROUPED_GROUPS[i], context);
            if (ai2arr != null) return ai2arr;
        }
        return null;
    }

    public void setRequestViewId(int arrId) {
        requestViewId = arrId;
    }

    public int getRequestViewId() {
        return requestViewId;
    }
}

