package net.koudela.sudoku;

import android.content.Context;
import android.util.Log;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.List;

import static net.koudela.sudoku.SudokuGroups.ALL_ARR_IDS;
import static net.koudela.sudoku.SudokuGroups.getGroupedGroup;
import static net.koudela.sudoku.SudokuGroups.getHorizontalGroup;
import static net.koudela.sudoku.SudokuGroups.getRandomizedArrIds;
import static net.koudela.sudoku.SudokuGroups.getStarGroup;
import static net.koudela.sudoku.SudokuGroups.getVerticalGroup;

/**
 * Singleton responsible for loading, saving and building sudokus. (The building process has its own
 * worker thread.)
 *
 * @author Thomas Koudela
 * @version 0.? beta
 */
@SuppressWarnings({"WeakerAccess", "unused"})
class SudokuBuilder extends Thread {
    private static final SudokuBuilder Singleton = new SudokuBuilder();
    private List<PField[]> sudokuStack = new ArrayList<>();
    private int opened = 0;
    private int closed = 0;
    private SudokuSolver solver = new SudokuSolver();

    private SudokuBuilder() {
        readSudokuStackFromFile();
        // seeding, we always wanna have something useful in stack
        if (sudokuStack.size() == 0) {
            sudokuStack.add(new PField[]{
                    new PField(Sudoku.SOLUTION),
                    new PField(Sudoku.LEVEL1),
                    new PField(Sudoku.LEVEL2),
                    new PField(Sudoku.LEVEL3),
                    new PField(Sudoku.LEVEL4),
                    new PField(Sudoku.LEVEL5),
                    new PField(Sudoku.LEVEL6),
            });
            init(sudokuStack.get(0));
        }
    }

    /**
     * Shuffle all levels with the same relation. This method disguises the fact, that we are always
     * seeding with the same constant arrays.
     * @param sudokuLevels the sudokus to initialize
     */
    static void init(PField[] sudokuLevels) {
        List<List<Integer>> shuffleRelations = PField.getShuffleRelations();
        for (PField level : sudokuLevels)
            level.shuffle(shuffleRelations);
    }

    static SudokuBuilder getInstance() {
        return Singleton;
    }

    int getCntOpened() {
        return opened;
    }

    int getCntClosed() {
        return closed;
    }

    int getStackSize() {
        return sudokuStack.size();
    }

    /**
     * @param pos the position in the sudoku stack
     * @return a collection of sudoku levels
     */
    Playground[] getResult(final int pos) {
        PField[] pFields = sudokuStack.get(pos);
        return new Playground[]{
                new Playground(pFields[0].getPField()),
                new Playground(pFields[1].getPField()),
                new Playground(pFields[2].getPField()),
                new Playground(pFields[3].getPField()),
                new Playground(pFields[4].getPField()),
                new Playground(pFields[5].getPField()),
                new Playground(pFields[6].getPField()),
        };
    }

    /**
     * Used to construct level 1 sudokus. ({@link #makeLevelOneSudoku})
     * @param arrId the id of the field to inspect
     * @param hint the plain hints
     * @return true if all hints are set (and thus removing the value leaves the sudoku solvable by
     *         auto insert 1), false otherwise
     */
    private static boolean isAutoInsert1Solvable(final int arrId, final Hint hint) {
        for (int num = 0; num < Sudoku.DIM; num++)
            if (!hint.isHint(arrId, num)) return false;
        return true;
    }

    /**
     * Builds a level 1 sudoku constructed by using auto insert 1 logic solely.
     * @param startingSudoku a valid sudoku solution
     * @param verbose if true, this method logs its progress
     * @return a random level 1 sudoku
     */
    private static Playground makeLevelOneSudoku(final Playground startingSudoku, final boolean verbose) {
        Playground level1Sudoku = new Playground(startingSudoku);
        if (verbose) Log.v("makeLevelOneSudoku","start");
        Hint hint =  new Hint();
        for (int arrId : ALL_ARR_IDS)
            for (int tempArrId : getStarGroup(arrId))
                // always true for real solutions, added for robustness
                if (level1Sudoku.isPopulated(arrId))  hint.increment(tempArrId, level1Sudoku.get(arrId) - 1);
        for (int arrId : getRandomizedArrIds())
            // always true for real solutions, added for robustness
            if (level1Sudoku.isPopulated(arrId)
                    && isAutoInsert1Solvable(arrId, hint)) {
                if (verbose) Log.v("removed", arrId + " (" + level1Sudoku.get(arrId) + ")");
                for (int tempArrId : getStarGroup(arrId)) hint.decrement(tempArrId, level1Sudoku.get(arrId) - 1);
                level1Sudoku.set(arrId, 0);
            }
        if (verbose) Log.d("level1Sudoku", level1Sudoku.toString());
        return level1Sudoku;
    }

    /**
     * Used to construct level 2 sudokus. ({@link #makeLevelTwoSudoku})
     * @param arrId the id of the field to inspect
     * @param hint the plain hints
     * @param sudoku the (partly) populated sudoku grid
     * @return true if setting the field to 0 can be reversed by using the auto insert 2 method,
     *         false otherwise
     */
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

    /**
     * Builds a level 2 sudoku constructed by using auto insert 1 logic solely.
     * @param startingSudoku  a (partly) populated weaker valid sudoku grid
     * @param verbose if true, this method logs its progress
     * @return a random level 2 sudoku
     */
    private static Playground makeLevelTwoSudoku(final Playground startingSudoku, final boolean verbose) {
        if (verbose) Log.v("makeLevelTwoSudoku","start");
        Playground level2Sudoku = new Playground(startingSudoku);
        Hint hint = new Hint();
        for (int arrId : ALL_ARR_IDS) for (int tempArrId : getStarGroup(arrId)) if (level2Sudoku.isPopulated(arrId)) hint.increment(tempArrId, level2Sudoku.get(arrId) - 1);
        for (int arrId : getRandomizedArrIds())
            if (level2Sudoku.isPopulated(arrId)
                    && isAutoInsert2Solvable(arrId, hint, level2Sudoku)) {
                if (verbose) Log.v("removed", arrId + " (" + level2Sudoku.get(arrId) + ")");
                for (int tempArrId : getStarGroup(arrId)) hint.decrement(tempArrId, level2Sudoku.get(arrId) - 1);
                level2Sudoku.set(arrId, 0);
            }
        if (verbose) Log.d("level2Sudoku", level2Sudoku.toString());
        return level2Sudoku;
    }

    /**
     * Builds a level 3 sudoku, witch is solvable by using auto insert 1 and 2 solely.
     * @param startingSudoku  a (partly) populated weaker valid sudoku grid
     * @param verbose if true, this method logs its progress
     * @return a random level 3 sudoku
     */
    private Playground makeLevelThreeSudoku(final Playground startingSudoku, final boolean verbose) {
        if (verbose) Log.v("makeLevelThreeSudoku","start");
        Playground level3Sudoku = new Playground(startingSudoku);
        for (int arrId : getRandomizedArrIds())
            if (level3Sudoku.isPopulated(arrId)) {
                if (verbose) Log.v("inspect", ""+arrId);
                if(solver.isSolvableSudoku(arrId, level3Sudoku, true, true, false, false, false, true)) {
                    if (verbose) Log.v("removed", arrId + " (" + level3Sudoku.get(arrId) + ")");
                    level3Sudoku.set(arrId, 0);
                }
            }
        if (verbose) Log.d("level3Sudoku", level3Sudoku.toString());
        return level3Sudoku;
    }

    /**
     * Builds a level 4 sudoku, witch is solvable by using auto insert 1, auto insert 2 and auto
     * hint adv 1/2/3 (2/3 in the relaxed versions).
     * @param startingSudoku  a (partly) populated weaker valid sudoku grid
     * @param verbose if true, this method logs its progress
     * @return a random level 4 sudoku
     */
    private Playground makeLevelFourSudoku(final Playground startingSudoku, final boolean verbose) {
        if (verbose) Log.v("makeLevelFourSudoku","start");
        Playground level4Sudoku = new Playground(startingSudoku);
        for (int arrId : getRandomizedArrIds())
            if (level4Sudoku.isPopulated(arrId)) {
                if (verbose) Log.v("inspect", ""+arrId);
                if(solver.isSolvableSudoku(arrId, level4Sudoku, true, true, true, true, true, true)) {
                    if (verbose) Log.v("removed", arrId + " (" + level4Sudoku.get(arrId) + ")");
                    level4Sudoku.set(arrId, 0);
                }
            }
        if (verbose) Log.d("level4Sudoku", level4Sudoku.toString());
        return level4Sudoku;
    }

    /**
     * Builds a level 5 sudoku, witch is solvable by using auto insert 1, auto insert 2 and auto
     * hint adv 1/2/3 (2/3 in the EXPTIME versions).
     * @param startingSudoku  a (partly) populated weaker valid sudoku grid
     * @param verbose if true, this method logs its progress
     * @return a random level 5 sudoku
     */
    private Playground makeLevelFiveSudoku(final Playground startingSudoku, final boolean verbose) {
        if (verbose) Log.v("makeLevelFiveSudoku","start");
        Playground level4Sudoku = new Playground(startingSudoku);
        for (int arrId : getRandomizedArrIds())
            if (level4Sudoku.isPopulated(arrId)) {
                if (verbose) Log.v("inspect", ""+arrId);
                if(solver.isSolvableSudoku(arrId, level4Sudoku, true, true, true, true, true, false)) {
                    if (verbose) Log.v("removed", arrId + " (" + level4Sudoku.get(arrId) + ")");
                    level4Sudoku.set(arrId, 0);
                }
            }
        if (verbose) Log.d("level5Sudoku", level4Sudoku.toString());
        return level4Sudoku;
    }

    /**
     * saves the current sudoku stack to disk
     */
    private void writeSudokuStackToFile() {
        String filename = "SudokuBuilder.SudokuStack.sav";
        FileOutputStream fos = null;
        ObjectOutputStream oos = null;
        ArrayList<ArrayList<int[]>> serializableList = new ArrayList<>();
        int count = -1;
        for (PField[] pFamily : sudokuStack) {
            serializableList.add(++count, new ArrayList<int[]>());
            for (PField playground : pFamily)
                if (playground != null)
                    serializableList.get(count).add(playground.getPField());
        }
        try {
            Context context = MainActivity.getContext();
            fos = context.openFileOutput(filename, Context.MODE_PRIVATE);
            oos = new ObjectOutputStream(fos);
            oos.writeObject(serializableList);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (oos != null) oos.close();
                if (fos != null) fos.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * reads the current sudoku stack from disk
     */
    private void readSudokuStackFromFile() {
        String filename = "SudokuBuilder.SudokuStack.sav";
        FileInputStream fis = null;
        ObjectInputStream ois = null;
        try {
            Context context = MainActivity.getContext();
            fis = context.openFileInput(filename);
            ois = new ObjectInputStream(fis);
            @SuppressWarnings("unchecked")
            ArrayList<ArrayList<int[]>> serializableList = (ArrayList<ArrayList<int[]>>) ois.readObject();
            int count = -1;
            for (ArrayList<int[]> pFamily : serializableList) {
                sudokuStack.add(++count, new PField[7]);
                int i = 0;
                for (int[] playground : pFamily) sudokuStack.get(count)[i++] = new PField(playground);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (ois != null) ois.close();
                if (fis != null) fis.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * As long as the current sudoku stack is small, this thread computes additional sudoku
     * collections
     */
    @Override
    public void run() {
        boolean verbose = false;
        while (sudokuStack.size() < 10000) {
            opened++;
            Playground[] workbench = new Playground[7];
            //workbench[0] = new Playground(Sudoku.TRUE_GRID);
            //workbench[0].shuffle();
            workbench[0] = Sudoku.makeRandomTrueGridByBacktracking(verbose);
            workbench[1] = makeLevelOneSudoku(workbench[0], verbose);
            workbench[2] = makeLevelTwoSudoku(workbench[1], verbose);
            workbench[3] = makeLevelThreeSudoku(workbench[2], verbose);
            workbench[6] = Sudoku.makeMinimalSudokuByBacktrackingOutOfTrueGrid(workbench[3].getPField(), verbose);
            if (workbench[3].getSizePopulatedArrIds() == workbench[6].getSizePopulatedArrIds()
                    || solver.isSolvableSudoku(-1, workbench[6], true, true, true, true, true, true)) {
                workbench[4] = new Playground(workbench[6]);
                workbench[5] = new Playground(workbench[6]);
            } else {
                workbench[4] = makeLevelFourSudoku(workbench[3], verbose);
                if (solver.isSolvableSudoku(-1, workbench[6], true, true, true, true, true, false))
                    workbench[5] = new Playground(workbench[6]);
                else workbench[5] = makeLevelFiveSudoku(workbench[4], verbose);
            }
            sudokuStack.add(workbench);
            writeSudokuStackToFile();
            closed++;
        }
    }
}