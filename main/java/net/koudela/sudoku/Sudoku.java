package net.koudela.sudoku;

import android.util.Log;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Singleton designed to maintain the different sudoku levels and the {@link SudokuBuilder}
 *
 * @author Thomas Koudela
 * @version 0.? beta
 */
@SuppressWarnings({"WeakerAccess", "unused"})
class Sudoku extends SudokuExptimeFunctions {
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

    void startBuilder() {
        if (!sudokuBuilder.isAlive())
           sudokuBuilder.start();
    }

    static Sudoku getInstance() {
        return Singleton;
    }

    Sudoku setLevel(final String level) {
        switch (level) {
            case "1":
            case "very easy":
                this.level = 1;
                break;
            case "2":
            case "easy":
                this.level = 2;
                break;
            case "3":
            case "moderate":
                this.level = 3;
                break;
            case "4":
            case "hard":
                this.level = 4;
                break;
            case "5":
            case "freestyle":
                this.level = 5;
                break;
            default:
                throw new IllegalArgumentException("level must be within [1, 5]; level was " + level);
        }
        return Singleton;
    }

    public Playground get(final int level) {
        if (level == 5) return new Playground();
        return new Playground(sudoku[level]);
    }

    public Playground get() {
        return get(level);
    }

    boolean getNewSudoku() {
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
        sudoku = sudokuBuilder.getResult(lastTrueStackPosition);
        SudokuBuilder.init(sudoku);
        return false;
    }

    void logInfo() {
        Log.w("solution(" + sudoku[0].getSizePopulatedArrIds() + ")", sudoku[0].toString());
        for (int i = 1; i <= 5; i++)
            Log.i("level" + i + "sudoku(" + sudoku[i].getSizePopulatedArrIds() + ")", sudoku[i].toString());
        Log.w("level", "" + level);
        String wPS = "";
        for (int i = 0; i < workbenchPopSize.size(); i++) {
            wPS += "\n"+Arrays.toString(workbenchPopSize.get(i));
        }
        Log.w("workbenchPopSize", wPS);
        Log.w("SudokuBuilder", "isRunning: " + sudokuBuilder.isAlive());
        Log.w("SudokuBuilder", "("  + lastStackPosition + ";" + lastTrueStackPosition + ")\n"
                + "opened: " + sudokuBuilder.getCntOpened() + "; closed: " + sudokuBuilder.getCntClosed()
                + "; stackSize:" + sudokuBuilder.getStackSize() + ")");
    }

    private String toString(final int level) {
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
