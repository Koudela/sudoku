package net.koudela.sudoku;

import android.content.Context;
import android.preference.PreferenceManager;
import android.support.v4.content.ContextCompat;
import android.widget.Button;
import android.widget.TextView;

public class SudokuData {
    private static final SudokuData Singleton = new SudokuData();
    protected final static int DIM = 9;
    protected int[] mainButtonsText = new int[DIM*DIM];
    protected int[] textColorHints = new int[DIM*DIM];
    protected Boolean[] isBlocked = new Boolean[DIM*DIM];
    protected Boolean[][] isUserHint = new Boolean[DIM*DIM][DIM];
    protected int[][] autoHint = new int[DIM*DIM][DIM];
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
                mainButtonsText[arrId] = 0;
                isBlocked[arrId] = false;
                for (int k = 0; k < DIM; k++) {
                    isUserHint[arrId][k] = false;
                    autoHint[arrId][k] = 0;
                }
            }
        }
        arrIdEasyTouchButton = 0;
        requestViewId = -1;
    }

    public void updateAutoHint(int number, int arrId, Button[] mainButtons, TextView[] helperTextViews, Context context, boolean isDeletion) {
        if (isDeletion) autoHint[arrId][number-1]--;
        else autoHint[arrId][number-1]++;
        setHelperTextViewText(arrId, mainButtons, helperTextViews, context, false);
    }

    protected void setAutoHints (int number, int arrId, Button[] mainButtons, TextView[] helperTextViews, Context context, boolean isDeletion) {
        int ii = arrId / DIM;
        int jj = arrId % DIM;
        for (int tempArrId :SudokuGroups.getGroupedGroup(arrId)) {
            updateAutoHint(number, tempArrId, mainButtons, helperTextViews, context, isDeletion);
        }
        for (int i=0; i<DIM; i++) {
            updateAutoHint(number, i * DIM + jj, mainButtons, helperTextViews, context, isDeletion);
        }
        for (int j=0; j<DIM; j++) {
            updateAutoHint(number, ii * DIM + j, mainButtons, helperTextViews, context, isDeletion);
        }
    }

    public  void initAutoHints (Button[] mainButtons, TextView[] helperTextViews, Context context) {
        for (int i = 0; i < DIM; i++) {
            for (int j = 0; j < DIM; j++) {
                int arrId = i * DIM + j;
                for (int k = 0; k < DIM; k++) {
                    autoHint[arrId][k] = 0;
                }
            }
        }
        for (int i = 0; i < DIM; i++) {
            for (int j = 0; j < DIM; j++) {
                int arrId = i * DIM + j;
                setAutoHints(mainButtonsText[arrId], arrId, mainButtons, helperTextViews, context, false);
            }
        }
    }

    public void setMainButtonsText(int number, int arrId, Button[] mainButtons, TextView[] helperTextViews, Context context) {
        // get value
        if (number < 0) number = mainButtonsText[arrId];
        // set value
        else {
            boolean autoHint = PreferenceManager.getDefaultSharedPreferences(context).getBoolean(PreferencesFragment.KEY_PREF_AUTO_HINT, false);
            if (autoHint && mainButtonsText[arrId] > 0) setAutoHints(mainButtonsText[arrId], arrId, mainButtons, helperTextViews, context, true);
            mainButtonsText[arrId] = number;
            // we need to change mainButtonsText[arrId] pre in case autoInsert chains
           if (autoHint && number > 0) setAutoHints(number, arrId, mainButtons, helperTextViews, context, false);
        }
        if (number == 0) {
            mainButtons[arrId].setText("");
            // making the hint 'visible'; (hint is the background for button!)
            helperTextViews[arrId].setTextColor(textColorHints[arrId]);
        }
        else {
            mainButtons[arrId].setText(String.valueOf(number));
            // making the hint 'invisible'; (hint is the background for button!)
            helperTextViews[arrId].setTextColor(ContextCompat.getColor(context, R.color.backgroundUntouched));
            if (PreferenceManager.getDefaultSharedPreferences(context).getBoolean(PreferencesFragment.KEY_PREF_AUTO_INSERT2, false))
                autoInsert2(mainButtons, helperTextViews, context);
        }
    }

    public void setUserHint(int number, int arrId, Button[] mainButtons, TextView[] helperTextViews, Context context) {
        isUserHint[arrId][number-1] = !isUserHint[arrId][number-1];
        setHelperTextViewText(arrId, mainButtons, helperTextViews, context, false);
        if (PreferenceManager.getDefaultSharedPreferences(context).getBoolean(PreferencesFragment.KEY_PREF_AUTO_INSERT2, false))
            autoInsert2(mainButtons, helperTextViews, context);
    }

    public void setHelperTextViewText(int arrId, Button[] mainButtons, TextView[] helperTextViews, Context context, boolean preventButtonUpdate) {
        if (!preventButtonUpdate && mainButtonsText[arrId] == 0) {
            if (PreferenceManager.getDefaultSharedPreferences(context).getBoolean(PreferencesFragment.KEY_PREF_AUTO_INSERT1, false))
                searchAndInsert1(arrId, mainButtons, helperTextViews, context);
        }
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
    }

    // if we would use Observers or Listeners we could prevent calling a changed value again
    // with threads we could even notify all as soon as auto insert is activated, but thread safety may be an issue
    // for the sake of simplicity we call one by one
    public void initAutoInsert1(Button[] mainButtons, TextView[] helperTextViews, Context context) {
        for (int i = 0; i < DIM; i++) {
            for (int j = 0; j < DIM; j++) {
                int arrId = i * DIM + j;
                searchAndInsert1(arrId, mainButtons, helperTextViews, context);
            }
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
        if (number != 0) setMainButtonsText(number, arrId, mainButtons, helperTextViews, context);
    }

    public void autoInsert2(Button[] mainButtons, TextView[] helperTextViews, Context context) {
        for (int i = 0; i < DIM; i++) {
            searchAndInsert2Sub(SudokuGroups.HORIZONTAL_GROUPS[i], mainButtons, helperTextViews, context);
            searchAndInsert2Sub(SudokuGroups.VERTICAL_GROUPS[i], mainButtons, helperTextViews, context);
            searchAndInsert2Sub(SudokuGroups.GROUPED_GROUPS[i], mainButtons, helperTextViews, context);
        }
    }

    public void searchAndInsert2Sub(int[] group, Button[] mainButtons, TextView[] helperTextViews, Context context) {
        for (int number = 0; number < DIM; number++) {
            int count = 0;
            int tempArrId = -1;
            for (int arrId : group) {
                if (autoHint[arrId][number] > 0  || isUserHint[arrId][number]) count++;
                else if (tempArrId == -1) tempArrId = arrId;
                else break;
            }
            if (count == 8) setMainButtonsText(number+1, tempArrId, mainButtons, helperTextViews, context);
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

