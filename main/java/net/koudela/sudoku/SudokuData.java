package net.koudela.sudoku;

import android.content.Context;
import android.support.v4.content.ContextCompat;
import android.widget.Button;
import android.widget.TextView;

public class SudokuData {
    protected final static int DIM = 9;
    protected int[] mainButtonsText = new int[DIM*DIM];
    protected int[] textColorHints = new int[DIM*DIM];
    protected Boolean[] isBlocked = new Boolean[DIM*DIM];
    protected Boolean[][] isUserHint = new Boolean[DIM*DIM][DIM];
    protected Boolean[][] isAutoHint = new Boolean[DIM*DIM][DIM];
    protected int arrIdEasyTouchButton;
    protected int requestViewId;

    public SudokuData() {
        initData();
    }

    public void initData() {
        for (int i = 0; i < DIM; i++) {
            for (int j = 0; j < DIM; j++) {
                int arrId = i * DIM + j;
                mainButtonsText[arrId] = 0;
                isBlocked[arrId] = false;
                for (int k = 0; k < DIM; k++) {
                    isUserHint[arrId][k] = false;
                    isAutoHint[arrId][k] = false;
                }
            }
        }
        arrIdEasyTouchButton = 0;
        requestViewId = -1;
    }

    public void setMainButtonsText(int number, int arrId, Button button, TextView textView, Context context) {
        if (number < 0) number = mainButtonsText[arrId];
        else mainButtonsText[arrId] = number;
        if (number == 0) {
            button.setText("");
            // making the hint 'visible'; (hint is the background for button!)
            textView.setTextColor(textColorHints[arrId]);
        }
        else {
            button.setText(String.valueOf(number));
            // making the hint 'invisible'; (hint is the background for button!)
            textView.setTextColor(ContextCompat.getColor(context, R.color.backgroundUntouched));
        }
    }

    public void setUserHint(int number, int arrId) {
        isUserHint[arrId][number-1] = !isUserHint[arrId][number-1];
    }
    public void setAutoHint(int number, int arrId, Boolean value) {
        isAutoHint[arrId][number-1] = value;
    }
    public void setHelperTextViewText(int arrId, TextView textView, Context context) {
        String text = "";
        boolean userHints = false;
        boolean autoHints = false;
        for (int i=0; i<DIM; i++) {
            text += (isUserHint[arrId][i] || isAutoHint[arrId][i])?(i+1):"-";
            if (isUserHint[arrId][i] && !userHints) userHints = true;
            if (isAutoHint[arrId][i] && !autoHints) autoHints = true;
            if (i % 3 == 2) text += "\n";
        }
        textView.setText(text);
        if (userHints) textColorHints[arrId] = ContextCompat.getColor(context, R.color.userHints);
        else if (autoHints) textColorHints[arrId] = ContextCompat.getColor(context, R.color.autoHints);
        else textColorHints[arrId] = ContextCompat.getColor(context, R.color.noHints);
        if (textView.getCurrentTextColor() != ContextCompat.getColor(context, R.color.backgroundUntouched)) {
            textView.setTextColor(textColorHints[arrId]);
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

