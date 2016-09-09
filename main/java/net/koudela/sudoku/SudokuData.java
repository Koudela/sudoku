package net.koudela.sudoku;

import android.content.Context;
import android.preference.PreferenceManager;
import android.support.v4.content.ContextCompat;
import android.widget.Button;
import android.widget.TextView;

public class SudokuData {
    private static final SudokuData Singleton = new SudokuData();
    public final static int DIM = 9;
    protected Boolean[] isAutoInsert = new Boolean[DIM*DIM];
    protected int[] mainButtonsText = new int[DIM*DIM];
    protected int[] textColorHints = new int[DIM*DIM];
    protected Boolean[] isBlocked = new Boolean[DIM*DIM];
    protected Boolean[][] isUserHint = new Boolean[DIM*DIM][DIM];
    protected int[][] autoHint = new int[DIM*DIM][DIM];
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
                mainButtonsText[arrId] = 0;
                isBlocked[arrId] = false;
                for (int k = 0; k < DIM; k++) {
                    isUserHint[arrId][k] = false;
                    autoHint[arrId][k] = 0;
                    helperBadUserInput[arrId][k] = 0;
                }
            }
        }
        arrIdEasyTouchButton = -1;
        requestViewId = -1;
    }

    public boolean isHint(int number, int arrId) {
        return (isUserHint[arrId][number-1] || autoHint[arrId][number-1] > 0);
    }

    protected void setAutoHints (int number, int arrId, Button[] mainButtons, TextView[] helperTextViews, Context context, boolean isDeletion) {
        // it does not matter that autoHint++/-- is called thrice for arrId:
        // 1. the call is symmetric and only > 0 is evaluated
        // 2. the field is populated
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
        // get
        if (number < 0) number = mainButtonsText[arrId];
        // set
        else {
            boolean autoHint = PreferenceManager.getDefaultSharedPreferences(context).getBoolean(PreferencesFragment.KEY_PREF_AUTO_HINT, false);
            // unset
            if (mainButtonsText[arrId] > 0) {
                countInput(mainButtonsText[arrId], arrId, mainButtons, context, true);
                if (autoHint) setAutoHints(mainButtonsText[arrId], arrId, mainButtons, helperTextViews, context, true);
            }
            countInput(number, arrId, mainButtons, context, false);
            mainButtonsText[arrId] = number;
            // we need to set mainButtonsText[arrId] pre in case autoInsert chains
            if (autoHint && number > 0) setAutoHints(number, arrId, mainButtons, helperTextViews, context, false);
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
            updateMainButtonColor(number, arrId, mainButtons, context);
            // SUB auto insert 2 looks at the whole playground, at the end of setMainButtonsText it has a new state
            if (PreferenceManager.getDefaultSharedPreferences(context).getBoolean(PreferencesFragment.KEY_PREF_AUTO_INSERT2, false))
                autoInsert2(mainButtons, helperTextViews, context);
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

    protected void updateMainButtonColor(int number, int arrId, Button[] mainButtons, Context context) {
        if (isBlocked[arrId]) mainButtons[arrId].setTextColor(ContextCompat.getColor(context, R.color.textColorIsBlocked));
        else if (helperBadUserInput[arrId][number-1] > 1) mainButtons[arrId].setTextColor(ContextCompat.getColor(context, R.color.textColorBadUserInput));
        else if (isAutoInsert[arrId]) mainButtons[arrId].setTextColor(ContextCompat.getColor(context, R.color.textColorAutoInsert));
        else mainButtons[arrId].setTextColor(ContextCompat.getColor(context, R.color.textColorUserInput));
    }

    protected void countInput(int number, int arrId, Button[] mainButtons, Context context, boolean isDeletion) {
        if (number == 0) return;
        for (int tempArrId: SudokuGroups.getStarGroup(arrId)) {
            helperBadUserInput[tempArrId][number-1] += isDeletion?-1:1;
            if (number == mainButtonsText[tempArrId] && helperBadUserInput[tempArrId][number-1] < 3) updateMainButtonColor(number, tempArrId, mainButtons, context);
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
            text += (isUserHint[arrId][i] || autoHint[arrId][i] != 0) ? (i + 1) : "-";
            if (isUserHint[arrId][i] && !userHints) userHints = true;
            if (autoHint[arrId][i] != 0 && !autoHints) autoHints = true;
            if (i % 3 == 2) text += "\n";
        }
        helperTextViews[arrId].setText(text);
        if (userHints) textColorHints[arrId] = ContextCompat.getColor(context, R.color.userHints);
        else if (autoHints) textColorHints[arrId] = ContextCompat.getColor(context, R.color.autoHints);
        else textColorHints[arrId] = ContextCompat.getColor(context, R.color.noHints);
        if (helperTextViews[arrId].getCurrentTextColor() != ContextCompat.getColor(context, R.color.backgroundUntouched)) {
            helperTextViews[arrId].setTextColor(textColorHints[arrId]);
        }
        // SUB: auto insert 1 uses hints, if a hint is set setHelperTextViewText is called
        if (!preventButtonUpdate && mainButtonsText[arrId] == 0) {
            if (PreferenceManager.getDefaultSharedPreferences(context).getBoolean(PreferencesFragment.KEY_PREF_AUTO_INSERT1, false))
                searchAndInsert1(arrId, mainButtons, helperTextViews, context);
        }
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

