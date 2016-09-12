package net.koudela.sudoku;

import android.content.Context;
import android.preference.PreferenceManager;
import android.support.v4.content.ContextCompat;
import android.widget.Button;
import android.widget.TextView;

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
    protected int[][] helperBadUserInput = new int[DIM*DIM][DIM];
    protected int arrIdEasyTouchButton;
    protected int requestViewId;

    private SudokuData() {
        initData();
    }

    public static SudokuData getInstance() {
        return Singleton;
    }

    public void initData() {
        for (int i = 0; i < DIM; i++) {
            for (int j = 0; j < DIM; j++) {
                int arrId = i * DIM + j;
                isAutoInsert[arrId] = false;
                for (int k = 0; k < DIM; k++) {
                    isUserHint[arrId][k] = false;
                    autoHint[arrId][k] = 0;
                    autoHintAdv1[arrId][k] = 0;
                    autoHintAdv2[arrId][k] = 0;
                    helperBadUserInput[arrId][k] = 0;
                }
            }
        }
        arrIdEasyTouchButton = -1;
        requestViewId = -1;
    }

    public void resetGame(Button[] mainButtons, TextView[] helperTextViews, Context context) {
        initData();
        sudoku.setLevel(PreferenceManager.getDefaultSharedPreferences(context).getString(PreferencesFragment.KEY_PREF_LEVEL, "1"));
        mainButtonsText = sudoku.getSudoku();
        for (int arrId : Sudoku.ALL_ARR_IDS) isBlocked[arrId] = (mainButtonsText[arrId] != 0);
        for (int arrId : Sudoku.ALL_ARR_IDS) {
            setMainButtonsText(-1, arrId, mainButtons, helperTextViews, context);
            setHelperTextViewText(arrId, mainButtons, helperTextViews, context, true);
        }

    }

    public void newGame(Button[] mainButtons, TextView[] helperTextViews, Context context) {
        sudoku.init();
        resetGame(mainButtons, helperTextViews, context);
    }

    public boolean isHint(int number, int arrId) {
        return (isUserHint[arrId][number-1] || autoHint[arrId][number-1] > 0 || autoHintAdv1[arrId][number-1] > 0 || autoHintAdv2[arrId][number-1] > 0);
    }

    public void resetAutoHintsAdv1() {
        for (int arrId = 0; arrId < DIM*DIM; arrId++) for (int number = 0; number < DIM; number++) autoHintAdv1[arrId][number] = 0;
    }

    public void resetAutoHintsAdv2() {
        for (int arrId = 0; arrId < DIM*DIM; arrId++) for (int number = 0; number < DIM; number++) autoHintAdv2[arrId][number] = 0;
    }

    // if a number is group wise bounded to a specific row/column, in the same row/column other groups get hints
    public void autoHintsAdv1(Button[] mainButtons, TextView[] helperTextViews, Context context) {
        for (int number = 1; number <= DIM; number++) {
            for (int cnt = 0; cnt < DIM; cnt++) {
                // check vertical
                int tempArrId = -1;
                for (int arrId : SudokuGroups.GROUPED_GROUPS[cnt]) if (!isHint(number, arrId) && mainButtonsText[arrId] == 0) {
                    if (tempArrId == -1) tempArrId = arrId;
                    else if (arrId != tempArrId + 1 && arrId != tempArrId + 2) {
                        tempArrId = -1;
                        break;
                    }
                }
                if (tempArrId != -1) {
                    // add hints vertical
                    for (int arrId : SudokuGroups.getComplementVerticalGroup(tempArrId)) {
                        autoHintAdv1[arrId][number-1]++;
                    }
                }
                // check horizontal
                tempArrId = -1;
                for (int arrId : SudokuGroups.GROUPED_GROUPS[cnt]) if (!isHint(number, arrId) && mainButtonsText[arrId] == 0) {
                    if (tempArrId == -1) tempArrId = arrId;
                    else if (arrId != tempArrId + 9 && arrId != tempArrId + 18) {
                        tempArrId = -1;
                        break;
                    }
                }
                if (tempArrId != -1) {
                    // add hints vertical
                    for (int arrId : SudokuGroups.getComplementHorizontalGroup(tempArrId)) {
                        autoHintAdv1[arrId][number-1]++;
                    }
                }

            }
        }
        updateDisplayHintsAll(mainButtons, helperTextViews, context, false);
    }

    // if in a group/row/column are n-fields with the same 9 minus n hints, hints get set in the other fields of the same group/row/column
    public void autoHintsAdv2(Button[] mainButtons, TextView[] helperTextViews, Context context) {

    }

    // if in a group/row/column are n-fields with the same n hints or less missing, hints get set on the other numbers on the same fields
    public void autoHintsAdv3(Button[] mainButtons, TextView[] helperTextViews, Context context) {

    }

    protected void updateDisplayHintsAll(Button[] mainButtons, TextView[] helperTextViews, Context context, Boolean preventButtonUpdate) {
        for (int arrId = 0; arrId < DIM*DIM; arrId++) {
            setHelperTextViewText(arrId, mainButtons, helperTextViews, context, preventButtonUpdate);
        }
    }

    protected void setAutoHints (int number, int arrId, Button[] mainButtons, TextView[] helperTextViews, Context context, boolean isDeletion) {
        // it does not matter that autoHint++/-- is called thrice for arrId:
        // 1. the call is symmetric and only > 0 is evaluated
        // 2. the field is populated
        if (number == 0) return;
        for (int tempArrId: SudokuGroups.getStarGroup(arrId)) {
            autoHint[tempArrId][number-1] += isDeletion?-1:1;
            setHelperTextViewText(tempArrId, mainButtons, helperTextViews, context, false);
        }
    }

    public  void initAutoHints (Button[] mainButtons, TextView[] helperTextViews, Context context) {
        for (int arrId = 0; arrId < DIM*DIM; arrId++) for (int k = 0; k < DIM; k++) autoHint[arrId][k] = 0;
        for (int arrId = 0; arrId < DIM*DIM; arrId++)
            setAutoHints(mainButtonsText[arrId], arrId, mainButtons, helperTextViews, context, false);
    }

    public void setMainButtonsText(int number, int arrId, Button[] mainButtons, TextView[] helperTextViews, Context context) {
        boolean readonly = true;
        // get
        if (number < 0) number = mainButtonsText[arrId];
        // set
        else {
            readonly = false;
            boolean autoHint = PreferenceManager.getDefaultSharedPreferences(context).getBoolean(PreferencesFragment.KEY_PREF_AUTO_HINT, false);
            // unset
            if (mainButtonsText[arrId] > 0) {
                countInput(mainButtonsText[arrId], arrId, mainButtons, context, true);
                if (autoHint) setAutoHints(mainButtonsText[arrId], arrId, mainButtons, helperTextViews, context, true);
            }
            countInput(number, arrId, mainButtons, context, false);
            mainButtonsText[arrId] = number;
            // we need to set mainButtonsText[arrId] pre in case autoInsert chains
            if (autoHint) setAutoHints(number, arrId, mainButtons, helperTextViews, context, false);
        }
        boolean autoHintAdv1 = PreferenceManager.getDefaultSharedPreferences(context).getBoolean(PreferencesFragment.KEY_PREF_AUTO_HINT_ADV1, false);
        // is (now) empty
        if (number == 0) {
            mainButtons[arrId].setText("");
            // making the hint 'visible'; (hint is the background for button!)
            helperTextViews[arrId].setTextColor(textColorHints[arrId]);
            if (autoHintAdv1 && !readonly) {
                resetAutoHintsAdv1();
                autoHintsAdv1(mainButtons, helperTextViews, context);
            }
        }
        // is (now) populated
        else {
            mainButtons[arrId].setText(String.valueOf(number));
            updateMainButtonColor(arrId, mainButtons, context);
            if (!readonly) {
                if (autoHintAdv1) autoHintsAdv1(mainButtons, helperTextViews, context);
                // SUB auto insert 2 looks at the whole playground, at the end of setMainButtonsText it has a new state
                if (PreferenceManager.getDefaultSharedPreferences(context).getBoolean(PreferencesFragment.KEY_PREF_AUTO_INSERT2, false))
                    autoInsert2(mainButtons, helperTextViews, context);
            }
        }
        // easy touch -> TODO: cleanup, do in own method + make whole easy touch code more readable
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

    public void updateMainButtonColor(int arrId, Button[] mainButtons, Context context) {
        if (isBlocked[arrId]) mainButtons[arrId].setTextColor(ContextCompat.getColor(context, R.color.textColorIsBlocked));
        else if (PreferenceManager.getDefaultSharedPreferences(context).getBoolean(PreferencesFragment.KEY_PREF_MARK_ERROR, false)
            && mainButtonsText[arrId] != sudoku.getSolutionField(arrId)) mainButtons[arrId].setTextColor(ContextCompat.getColor(context, R.color.textColorMarkError));
        else if (helperBadUserInput[arrId][mainButtonsText[arrId]-1] > 1) mainButtons[arrId].setTextColor(ContextCompat.getColor(context, R.color.textColorBadUserInput));
        else if (isAutoInsert[arrId]) mainButtons[arrId].setTextColor(ContextCompat.getColor(context, R.color.textColorAutoInsert));
        else mainButtons[arrId].setTextColor(ContextCompat.getColor(context, R.color.textColorUserInput));
    }

    protected void countInput(int number, int arrId, Button[] mainButtons, Context context, boolean isDeletion) {
        if (number == 0) return;
        for (int tempArrId: SudokuGroups.getStarGroup(arrId)) {
            helperBadUserInput[tempArrId][number-1] += isDeletion?-1:1;
            if (number == mainButtonsText[tempArrId] && helperBadUserInput[tempArrId][number-1] < 3) updateMainButtonColor(tempArrId, mainButtons, context);
        }
        helperBadUserInput[arrId][number-1] += isDeletion?+2:-2;
    }

    public void setUserHint(int number, int arrId, Button[] mainButtons, TextView[] helperTextViews, Context context) {
        isUserHint[arrId][number-1] = !isUserHint[arrId][number-1];
        setHelperTextViewText(arrId, mainButtons, helperTextViews, context, false);
        if (PreferenceManager.getDefaultSharedPreferences(context).getBoolean(PreferencesFragment.KEY_PREF_AUTO_INSERT2, false))
            autoInsert2(mainButtons, helperTextViews, context);
    }

    public void setHelperTextViewText(int arrId, Button[] mainButtons, TextView[] helperTextViews, Context context, boolean preventButtonUpdate) {
        String text = "";
        boolean userHints = false;
        boolean autoHints = false;
        for (int i=0; i<DIM; i++) {
            text += (isUserHint[arrId][i] || autoHint[arrId][i] > 0 || autoHintAdv1[arrId][i] > 0 || autoHintAdv2[arrId][i] > 0) ? (i + 1) : "-";
            if (isUserHint[arrId][i] && !userHints) userHints = true;
            if (!autoHints && (autoHint[arrId][i] > 0 || autoHintAdv1[arrId][i] > 0 || autoHintAdv2[arrId][i] > 0)) autoHints = true;
            if (i % 3 == 2) text += "\n";
        }
        helperTextViews[arrId].setText(text);
        if (userHints) textColorHints[arrId] = ContextCompat.getColor(context, R.color.userHints);
        else if (autoHints) textColorHints[arrId] = ContextCompat.getColor(context, R.color.autoHints);
        else textColorHints[arrId] = ContextCompat.getColor(context, R.color.noHints);
        if (helperTextViews[arrId].getCurrentTextColor() != ContextCompat.getColor(context, R.color.backgroundUntouched))
            helperTextViews[arrId].setTextColor(textColorHints[arrId]);
        // SUB: auto insert 1 uses hints, if a hint is set setHelperTextViewText is called
        if (!preventButtonUpdate && mainButtonsText[arrId] == 0
            && PreferenceManager.getDefaultSharedPreferences(context).getBoolean(PreferencesFragment.KEY_PREF_AUTO_INSERT1, false))
                searchAndInsert1(arrId, mainButtons, helperTextViews, context);
    }

    public void searchAndInsert1(int arrId, Button[] mainButtons, TextView[] helperTextViews, Context context) {
        int number = 0;
        for (int k = 0; k < DIM; k++) {
            if (autoHint[arrId][k] == 0  && !isUserHint[arrId][k]) {
                if (number != 0) {
                    number = 0;
                    break;
                } else number = k+1;
            }
        }
        if (number != 0) {
            isAutoInsert[arrId] = true;
            setMainButtonsText(number, arrId, mainButtons, helperTextViews, context);
        }
    }

    public void autoInsert2(Button[] mainButtons, TextView[] helperTextViews, Context context) {
        for (int i = 0; i < DIM; i++) {
            searchAndInsert2Sub(SudokuGroups.VERTICAL_GROUPS[i], mainButtons, helperTextViews, context);
            searchAndInsert2Sub(SudokuGroups.HORIZONTAL_GROUPS[i], mainButtons, helperTextViews, context);
            searchAndInsert2Sub(SudokuGroups.GROUPED_GROUPS[i], mainButtons, helperTextViews, context);
        }
    }

    public void searchAndInsert2Sub(int[] group, Button[] mainButtons, TextView[] helperTextViews, Context context) {
        for (int number = 0; number < DIM; number++) {
            int count = 0;
            int tempArrId = -1;
            for (int arrId : group) {
                if (mainButtonsText[arrId] != 0 || autoHint[arrId][number] > 0  || isUserHint[arrId][number]) count++;
                else if (tempArrId == -1) tempArrId = arrId;
                else break;
            }
            if (count == 8) {
                isAutoInsert[tempArrId] = true;
                setMainButtonsText(number + 1, tempArrId, mainButtons, helperTextViews, context);
            }
        }
    }

    public boolean isBlocked(int arrId) {
        return isBlocked[arrId];
    }

    public void setRequestViewId(int arrId) {
        requestViewId = arrId;
    }

    public int getRequestViewId() {
        return requestViewId;
    }
}

