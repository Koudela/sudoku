package net.koudela.sudoku;

import android.util.Log;

import java.util.Set;

public class Sudoku extends SudokuExptimeFunctions implements Runnable {
    private static final Sudoku Singleton = new Sudoku();
    public Playground solution = new Playground(TRUE_GRID);
    public Playground level1Sudoku = new Playground();
    public Playground level2Sudoku = new Playground();
    public Playground level3Sudoku = new Playground();
    public Playground level4Sudoku = new Playground();
    public Playground level5Sudoku = new Playground();
    protected int level = 1;
    protected Playground onTheRun = null;

    protected Sudoku() {
        makeNewSolution();
    }

    public static Sudoku getInstance() {
        return Singleton;
    }

    public Sudoku init() {
        makeNewSolution();
        level1Sudoku.init();
        level2Sudoku.init();
        level3Sudoku.init();
        level4Sudoku.init();
        level5Sudoku.init();
        makeLevelOneSudoku();
        makeLevelTwoSudoku();
        makeLevelThreeSudoku();
        makeLevelFourSudoku();
        new Thread(this).start();
        return Singleton;
    }

    public void makeNewSolution() {
        solution.init(TRUE_GRID);
        solution.shuffle();
        Log.i("solution", solution.toString());
    }

    public Sudoku setLevel(int level) {
        if (level < 0 || level > 5)
            throw new IllegalArgumentException("level must be within [0, 5]; level was " + level);
        this.level = level;
        Log.v("Sudoku setLevel", this.level+"");
        return Singleton;
    }

    public String toString(int level) {
        return get(level).toString();
    }

    protected static boolean isAutoInsert1Solvable(int arrId, Hint hint) {
        for (int num = 0; num < DIM; num++)
            if (!hint.isHint(arrId, num)) return false;
        return true;
    }

    // a level one sudoku gets constructed by solely using AutoInsert1 logic
    protected void makeLevelOneSudoku() {
        Log.v("makeLevelOneSudoku","start");
        level1Sudoku = new Playground(solution);
        Hint hint =  new Hint();
        for (int arrId : ALL_ARR_IDS)
            for (int tempArrId : getStarGroup(arrId))
                // always true for real solutions, added for robustness
                if (level1Sudoku.isPopulated(arrId))  hint.increment(tempArrId, level1Sudoku.get(arrId) - 1);
        for (int arrId : getRandomizedArrIds())
            // always true for real solutions, added for robustness
            if (level1Sudoku.isPopulated(arrId)
                    && isAutoInsert1Solvable(arrId, hint)) {
                Log.v("removed", arrId + " (" + level1Sudoku.get(arrId) + ")");
                for (int tempArrId : getStarGroup(arrId)) hint.decrement(tempArrId, level1Sudoku.get(arrId) - 1);
                level1Sudoku.set(arrId, 0);
            }
        Log.i("level1Sudoku", level1Sudoku.toString());
    }

    protected static boolean isAutoInsert2Solvable(int arrId, Hint hint, Playground sudoku) {
        return (isAutoInsert2SolvableSub(getVerticalGroup(arrId), arrId, hint, sudoku)
                || isAutoInsert2SolvableSub(getHorizontalGroup(arrId), arrId, hint, sudoku)
                || isAutoInsert2SolvableSub(getGroupedGroup(arrId), arrId, hint, sudoku));
    }

    protected static boolean isAutoInsert2SolvableSub(Set<Integer> group, int arrId, Hint hint, Playground sudoku) {
        for (int tempArrId : group)
            if (!sudoku.isPopulated(tempArrId)
                    && hint.get(tempArrId, sudoku.get(arrId) - 1) == 1) return false;
        return true;
    }

    // a level two sudoku gets constructed by using AutoInsert1 and AutoInsert2 logic
    public void makeLevelTwoSudoku() {
        if (level1Sudoku.getPopulatedArrIds().size() == 0) makeLevelOneSudoku();
        Log.v("makeLevelTwoSudoku","start");
        level2Sudoku = new Playground(level1Sudoku);
        Hint hint = new Hint();
        for (int arrId : ALL_ARR_IDS) for (int tempArrId : getStarGroup(arrId)) if (level2Sudoku.isPopulated(arrId)) hint.increment(tempArrId, level2Sudoku.get(arrId) - 1);
        for (int arrId : getRandomizedArrIds())
            if (level2Sudoku.isPopulated(arrId)
                    && isAutoInsert2Solvable(arrId, hint, level2Sudoku)) {
                Log.v("removed", arrId + " (" + level2Sudoku.get(arrId) + ")");
                for (int tempArrId : getStarGroup(arrId)) hint.decrement(tempArrId, level2Sudoku.get(arrId) - 1);
                level2Sudoku.set(arrId, 0);
            }
        Log.i("level2Sudoku", level2Sudoku.toString());
    }

    // a level three sudoku is solvable by using AutoInsert1, AutoInsert2
    public void makeLevelThreeSudoku() {
        if (level2Sudoku.getPopulatedArrIds().size() == 0) makeLevelTwoSudoku();
        Log.v("makeLevelThreeSudoku","start");
        level3Sudoku = new Playground(level2Sudoku);
        for (int arrId : getRandomizedArrIds())
            if (level3Sudoku.isPopulated(arrId)) {
                Log.v("inspect", ""+arrId);
                if(isSolvableSudoku(arrId, level3Sudoku, true, true, false, false, false)) {
                    Log.v("removed", arrId + " (" + level3Sudoku.get(arrId) + ")");
                    level3Sudoku.set(arrId, 0);
                }
            }
        Log.i("level3Sudoku", level3Sudoku.toString());
    }

    // a level four sudoku is solvable by using AutoInsert1, AutoInsert2, AutoHintAdv1, AutoHintAdv3
    public void makeLevelFourSudoku() {
        if (level3Sudoku.getPopulatedArrIds().size() == 0) makeLevelThreeSudoku();
        Log.v("makeLevelFourSudoku","start");
        level4Sudoku = new Playground(level3Sudoku);
        for (int arrId : getRandomizedArrIds())
            if (level4Sudoku.isPopulated(arrId)) {
                Log.v("inspect", ""+arrId);
                if(isSolvableSudoku(arrId, level4Sudoku, true, true, true, true, true)) {
                    Log.v("removed", arrId + " (" + level4Sudoku.get(arrId) + ")");
                    level4Sudoku.set(arrId, 0);
                }
            }
        Log.i("level4Sudoku", level4Sudoku.toString());
    }

    @Override
    public String toString() {
        return toString(level);
    }

    public Playground get(int level) {
        switch (level) {
            case 0: return solution;
            case 1:
                if (level1Sudoku.getPopulatedArrIds().size() == 0) makeLevelOneSudoku();
                return new Playground(level1Sudoku);
            case 2:
                if (level2Sudoku.getPopulatedArrIds().size() == 0) makeLevelTwoSudoku();
                return new Playground(level2Sudoku);
            case 3:
                if (level3Sudoku.getPopulatedArrIds().size() == 0) makeLevelThreeSudoku();
                return new Playground(level3Sudoku);
            case 4:
                if (level4Sudoku.getPopulatedArrIds().size() == 0) makeLevelFourSudoku();
                return new Playground(level4Sudoku);
            case 5:
                if (level5Sudoku.getPopulatedArrIds().size() == 0) {
                    if (level4Sudoku.getPopulatedArrIds().size() == 0) makeLevelFourSudoku();
                    new Thread(this).start();
                }
                return new Playground(level5Sudoku);
            default: throw new IndexOutOfBoundsException();
        }
    }

    public Playground get() {
        return get(level);
    }

    public void run() {
        try {
            if (onTheRun == null) onTheRun = solution;
            else if (onTheRun.equals(solution)) return;
            else throw new InterruptedException();

            level5Sudoku = makeMinimalSudokuByBruteForceBacktrackingOutOfTrueGrid(level4Sudoku.getPField());
            Log.i("level5Sudoku", level5Sudoku.toString());
        } catch (Exception e) {
            onTheRun = null;
            return;
        }
    }
}
