package net.koudela.sudoku;

import android.util.Log;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Sudoku extends SudokuExptimeFunctions {
    private static final Sudoku Singleton = new Sudoku();
    private SudokuBuilder sudokuBuilder = SudokuBuilder.getInstance();
    private Playground sudoku[];
    private int level = 1;
    private int lastStackPosition = 0;
    private int lastTrueStackPosition = 0;
    private List<int[]> workbenchPopSize = new ArrayList<>(); // for development use, i.e. deeper inspection of probability

    private Sudoku() {
        sudoku = sudokuBuilder.getResult(lastTrueStackPosition);
        getNewSudoku();
    }

    public void startBuilder() {
        sudokuBuilder.start();
    }

    public static Sudoku getInstance() {
        return Singleton;
    }

    public Sudoku setLevel(final int level) {
        if (level < 0 || level > 5)
            throw new IllegalArgumentException("level must be within [0, 5]; level was " + level);
        this.level = level;
        return Singleton;
    }

    private static boolean isAutoInsert1Solvable(final int arrId, final Hint hint) {
        for (int num = 0; num < DIM; num++)
            if (!hint.isHint(arrId, num)) return false;
        return true;
    }

    // a level one sudoku gets constructed by solely using AutoInsert1 logic
    public static Playground makeLevelOneSudoku(final Playground startingSudoku, final boolean verbose) {
        Playground level1Sudoku = new Playground(startingSudoku);
        if (verbose) Log.d("makeLevelOneSudoku","start");
        Hint hint =  new Hint();
        for (int arrId : ALL_ARR_IDS)
            for (int tempArrId : getStarGroup(arrId))
                // always true for real solutions, added for robustness
                if (level1Sudoku.isPopulated(arrId))  hint.increment(tempArrId, level1Sudoku.get(arrId) - 1);
        for (int arrId : getRandomizedArrIds())
            // always true for real solutions, added for robustness
            if (level1Sudoku.isPopulated(arrId)
                    && isAutoInsert1Solvable(arrId, hint)) {
                if (verbose) Log.d("removed", arrId + " (" + level1Sudoku.get(arrId) + ")");
                for (int tempArrId : getStarGroup(arrId)) hint.decrement(tempArrId, level1Sudoku.get(arrId) - 1);
                level1Sudoku.set(arrId, 0);
            }
        if (verbose) Log.i("level1Sudoku", level1Sudoku.toString());
        return level1Sudoku;
    }

    private static boolean isAutoInsert2Solvable(final int arrId, final Hint hint, final Playground sudoku) {
        return (isAutoInsert2SolvableSub(getVerticalGroup(arrId), arrId, hint, sudoku)
                || isAutoInsert2SolvableSub(getHorizontalGroup(arrId), arrId, hint, sudoku)
                || isAutoInsert2SolvableSub(getGroupedGroup(arrId), arrId, hint, sudoku));
    }

    private static boolean isAutoInsert2SolvableSub(final Integer[] group, final int arrId, final Hint hint, final Playground sudoku) {
        for (int tempArrId : group)
            if (!sudoku.isPopulated(tempArrId) && hint.get(tempArrId, sudoku.get(arrId) - 1) == 1) return false;
        return true;
    }

    // a level two sudoku gets constructed by using AutoInsert1 and AutoInsert2 logic
    public static Playground makeLevelTwoSudoku(final Playground startingSudoku, final boolean verbose) {
        if (verbose) Log.d("makeLevelTwoSudoku","start");
        Playground level2Sudoku = new Playground(startingSudoku);
        Hint hint = new Hint();
        for (int arrId : ALL_ARR_IDS) for (int tempArrId : getStarGroup(arrId)) if (level2Sudoku.isPopulated(arrId)) hint.increment(tempArrId, level2Sudoku.get(arrId) - 1);
        for (int arrId : getRandomizedArrIds())
            if (level2Sudoku.isPopulated(arrId)
                    && isAutoInsert2Solvable(arrId, hint, level2Sudoku)) {
                if (verbose) Log.d("removed", arrId + " (" + level2Sudoku.get(arrId) + ")");
                for (int tempArrId : getStarGroup(arrId)) hint.decrement(tempArrId, level2Sudoku.get(arrId) - 1);
                level2Sudoku.set(arrId, 0);
            }
        if (verbose) Log.i("level2Sudoku", level2Sudoku.toString());
        return level2Sudoku;
    }

    // a level three sudoku is solvable by using AutoInsert1, AutoInsert2
    public static Playground makeLevelThreeSudoku(final Playground startingSudoku, final boolean verbose) {
        if (verbose) Log.d("makeLevelThreeSudoku","start");
        Playground level3Sudoku = new Playground(startingSudoku);
        for (int arrId : getRandomizedArrIds())
            if (level3Sudoku.isPopulated(arrId)) {
                if (verbose) Log.d("inspect", ""+arrId);
                if(isSolvableSudoku(arrId, level3Sudoku, true, true, false, false, false, verbose)) {
                    if (verbose) Log.d("removed", arrId + " (" + level3Sudoku.get(arrId) + ")");
                    level3Sudoku.set(arrId, 0);
                }
            }
        if (verbose) Log.i("level3Sudoku", level3Sudoku.toString());
        return level3Sudoku;
    }

    // a level four sudoku is solvable by using AutoInsert1, AutoInsert2, AutoHintAdv1, AutoHintAdv3
    public static Playground makeLevelFourSudoku(final Playground startingSudoku, final boolean verbose) {
        if (verbose) Log.d("makeLevelFourSudoku","start");
        Playground level4Sudoku = new Playground(startingSudoku);
        for (int arrId : getRandomizedArrIds())
            if (level4Sudoku.isPopulated(arrId)) {
                if (verbose) Log.d("inspect", ""+arrId);
                if(isSolvableSudoku(arrId, level4Sudoku, true, true, true, true, true, verbose)) {
                    if (verbose) Log.d("removed", arrId + " (" + level4Sudoku.get(arrId) + ")");
                    level4Sudoku.set(arrId, 0);
                }
            }
        if (verbose) Log.i("level4Sudoku", level4Sudoku.toString());
        return level4Sudoku;
    }


    public Playground get(final int level) {
        if (level == 5) return new Playground();
        return new Playground(sudoku[level]);
    }

    public Playground get() {
        return get(level);
    }

    public boolean getNewSudoku() {
        if (lastStackPosition == sudokuBuilder.getStackSize() - 1) {
            SudokuBuilder.init(sudoku);
            return false;
        } else while (lastStackPosition < sudokuBuilder.getStackSize() - 1) {
            lastStackPosition++;
            sudoku = sudokuBuilder.getResult(lastStackPosition);
            workbenchPopSize.add(new int[]{ // for development use, i.e. deeper inspection of probability
                    sudoku[1].getSizePopulatedArrIds(),
                    sudoku[2].getSizePopulatedArrIds(),
                    sudoku[3].getSizePopulatedArrIds(),
                    sudoku[4].getSizePopulatedArrIds(),
                    sudoku[5].getSizePopulatedArrIds()});
            if (sudoku[3].getSizePopulatedArrIds() > sudoku[4].getSizePopulatedArrIds()) {
                lastTrueStackPosition = lastStackPosition;
                sudoku = sudokuBuilder.getResult(lastTrueStackPosition);
                return true;
            }
        }
        sudokuBuilder.getResult(lastTrueStackPosition);
        SudokuBuilder.init(sudoku);
        return false;
    }

    public void logInfo() {
        Log.w("solution(" + sudoku[0].getSizePopulatedArrIds() + ")", sudoku[0].toString());
        for (int i = 1; i <= 5; i++)
            Log.i("level" + i + "sudoku(" + sudoku[i].getSizePopulatedArrIds() + ")", sudoku[i].toString());
        Log.w("level", "" + level);
        Log.w("SudokuBuilder", "("  + lastStackPosition + ";" + lastTrueStackPosition + ")\n"
                + "opened: " + sudokuBuilder.getCntOpened() + "; closed: " + sudokuBuilder.getCntClosed()
                + "; stackSize:" + sudokuBuilder.getStackSize() + ")");
        String wPS = "";
        for (int i = 0; i < workbenchPopSize.size(); i++) {
            wPS += "\n"+Arrays.toString(workbenchPopSize.get(i));
        }
        Log.w("workbenchPopSize", wPS);
    }

    public String toString(final int level) {
        if (level < 0 || level > 5)
            throw new IllegalArgumentException("level must be within [0, 5]; level was " + level);
        if (level == 5) return (new Playground()).toString();
        return sudoku[level].toString();
    }

    @Override
    public String toString() {
        return toString(level);
    }
}
