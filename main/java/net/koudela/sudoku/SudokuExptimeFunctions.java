package net.koudela.sudoku;

import android.annotation.SuppressLint;
import android.util.Log;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * A collection of static functions using the backtracking solver as well as the backtracking solver
 * itself. Since backtracking resides in EXPTIME. All this functions resides there.
 *
 * @author Thomas Koudela
 * @version 0.? beta
 */
@SuppressWarnings({"WeakerAccess", "unused"})
class SudokuExptimeFunctions extends SudokuStaticFunctions {

    /**
     * A backtracking sudoku solver, using the auto insert and auto hint methods of the Playground
     * class and the Hints class for a faster and more reliable forward branching.
     * Be careful, this algorithm resides in EXPTIME
     * @param sudoku the (partly) populated sudoku playground
     * @param countDown if true the values get tested from 9 to 1, otherwise from 1 to 9, thus can be
     *                  tested by running this algorithm twice, whether a solution is unique or not.
     * @param computedSolution input must be null or a clone of sudoku, holds the calculated solution
     * @return -1 if sudoku is not solvable, 0 if sudoku may have more than one possible solution,
     *         1 if sudoku is a valid sudoku
     */
    private static int solveByBacktracking(final Playground sudoku, final boolean countDown,
                                           Playground computedSolution) {
        Hints hints = new Hints(true, true, true, true, false);
        Set<Integer> arrIdsChangedHints = new HashSet<>();
        Set<Integer> arrIdsChangedValues = new HashSet<>();
        int initValue = countDown?9:1;
        // initial check
        // we don't wanna init the advanced hints yet (if we use them),
        // updateSudokuStart takes care of that
        hints.populatePlainHints(sudoku);
        for (int arrId : sudoku.getPopulatedArrIds())
             // -> Sudoku is not solvable
             if (hints.getPlainHint(arrId, sudoku.get(arrId) - 1) > 1) return -1;
        // start the main computing
        if (computedSolution == null) computedSolution = new Playground(sudoku);
        updateSudokuStart(arrIdsChangedHints, arrIdsChangedValues, computedSolution, hints, true,
                true, false);
        // -> Sudoku is a valid Sudoku
        if (computedSolution.getSizeNotPopulatedArrIds() == 0) return 1;
        Set<Integer> mayBeDependValidValues = new HashSet<>(computedSolution.getPopulatedArrIds());
        @SuppressLint("UseSparseArrays")
        Map<Integer, Set<Integer>> dependentValidValues = new HashMap<>();
        // backtrack
        int count = 0;
        for (int arrId = 0; arrId < DIM * DIM; ) {
            if (computedSolution.isPopulated(arrId)) {
                arrId++;
                continue;
            }
            // "first" try
            computedSolution.set(arrId, initValue);
            boolean outOfBound = false;
            while (outOfBound || computedSolution.get(arrId) > 9 || computedSolution.get(arrId) < 1
                    || hints.isHint(arrId, computedSolution.get(arrId) - 1)) {
                try {
                    outOfBound = false;
                    // new try
                    computedSolution.set(arrId, computedSolution.get(arrId) + (countDown ? -1 : 1));
                } catch (IllegalArgumentException e) {
                    outOfBound = true;
                }
                // step back
                if (outOfBound || computedSolution.get(arrId) > 9 || computedSolution.get(arrId) < 1) {
                    if (arrId == 0) return -1; // -> Sudoku is not solvable
                    computedSolution.set(arrId, 0);
                    arrId--;
                    while (mayBeDependValidValues.contains(arrId)) {
                        if (arrId == 0) return -1; // -> Sudoku is not solvable
                        arrId--;
                    }
                    // rollback
                    // TODO: A better understanding
                    // log message reminds us: we don't know why there are sometimes still rollbacks
                    Log.w("solveByBacktracking","rollback ("+arrId+") - " + count);
                    if ((++count) % 100 == 0)
                        Log.v("solveByBacktracking","rollback ("+arrId+") - " + count);
                    hints.decrementStarGroup(arrId, computedSolution.get(arrId) - 1);
                    for (int tempArrId : dependentValidValues.get(arrId)) {
                        mayBeDependValidValues.remove(tempArrId);
                        hints.decrementStarGroup(tempArrId, computedSolution.get(tempArrId) - 1);
                        computedSolution.set(tempArrId, 0);
                    }
                    hints.initAdv();
                    try {
                        outOfBound = false;
                        // new try
                        computedSolution.set(arrId, computedSolution.get(arrId) + (countDown ? -1 : 1));
                    } catch (IllegalArgumentException e) {
                        outOfBound = true;
                    }
                }
            }
            // step forward
            if ((++count) % 100 == 0)
                Log.v("solveByBacktracking","take ("+arrId+";"+computedSolution.get(arrId)+") - " + count);
            hints.incrementStarGroup(arrId, computedSolution.get(arrId) - 1);
            arrIdsChangedValues.clear();
            updateSudokuStart(arrIdsChangedHints, arrIdsChangedValues, computedSolution, hints, true,
                    true, false);
            // -> Sudoku may be a valid Sudoku
            if (computedSolution.getSizeNotPopulatedArrIds() == 0) return 0;
            dependentValidValues.put(arrId, new HashSet<>(arrIdsChangedValues));
            mayBeDependValidValues.addAll(arrIdsChangedValues);
            arrId++;
        }
        return 0; // Sudoku -> may be a valid Sudoku
    }

    /**
     * Be careful, this algorithm resides in EXPTIME
     * @return a random valid 9x9 sudoku solution grid
     */
    static Playground makeRandomTrueGridByBacktracking(boolean verbose) {
        if (verbose) Log.v("makeRandomTrueGridBB","start");
        Playground solution = new Playground();
        Hints hints = new Hints(true, true, true, true, false);
        List<Integer> numbersLeft;
        Set<Integer> arrIdsChangedHints = new HashSet<>();
        Set<Integer> arrIdsChangedValues = new HashSet<>();
        for (int arrId :ALL_ARR_IDS) {
            if (solution.isPopulated(arrId)) continue;
            numbersLeft = new ArrayList<>(Arrays.asList(1, 2, 3, 4, 5, 6, 7, 8, 9));
            Collections.shuffle(numbersLeft);
            do {
                if (numbersLeft.isEmpty())
                    throw new ArithmeticException("no numbers left:" + solution.toString());
                solution.set(arrId, numbersLeft.get(0));
                numbersLeft.remove(0);
            } while (hints.isHint(arrId, solution.get(arrId) - 1));
            Log.v("set", arrId + "(" + solution.get(arrId) + ")");
            arrIdsChangedHints.clear();
            arrIdsChangedValues.clear();
            hints.incrementStarGroup(arrId, solution.get(arrId) - 1);
            // the advanced hints are getting updated in updateSudokuStart
            // we need to use the solver without relaxation, or we may run into half way solutions
            // which can not be completed
            updateSudokuStart(arrIdsChangedHints, arrIdsChangedValues, solution, hints, true, true,
                    false);
        }
        if (!isTrueGrid(solution)) throw new ArithmeticException("solution is no true grid");
        if (verbose) Log.d("makeRandomTrueGridBB","solution: " + solution.toString());
        return solution;
    }

    /**
     * Checks whether a (partly) populated playground represents a sudoku
     * Be careful, this algorithm resides in EXPTIME
     * @param sudoku a sudoku playground populated with some values
     * @param solution input must be null or a clone of sudoku, holds the calculated grid
     * @return -1 if sudoku is not solvable, 0 if sudoku has more than one possible solution, 1 if
     *         sudoku is a valid sudoku
     */
    private static int isSudoku(final Playground sudoku, Playground solution) {
        if (solution == null) solution = new Playground(sudoku);
        int result = solveByBacktracking(sudoku, false, solution);
        if (result != 0) return result; // -> -1: Sudoku is not solvable, 1: Sudoku is a valid Sudoku
        Playground possibleSolution = new Playground(solution);
        solution.init(sudoku);
        solveByBacktracking(sudoku, true, solution);
        if (possibleSolution.equals(solution)) return 1; // -> Sudoku is a valid Sudoku
        return 0; // -> Sudoku has more than one possible solution
    }

    /**
     * Transforms a solution or valid sudoku into a minimal sudoku
     * Be careful, this algorithm resides in EXPTIME
     * @param trueGrid a solution or valid sudoku
     * @param verbose if true the method writes its progress to the log
     * @return a minimal sudoku
     */
    static Playground makeMinimalSudokuByBacktrackingOutOfTrueGrid(final int[] trueGrid, boolean verbose) {
        if (verbose) Log.v("makeMinimalSudoku","start");
        Playground sudoku = new Playground(trueGrid);
        // erases random values iff sudoku stays a valid sudoku
        for (int arrId : getRandomizedArrIds())
            if (sudoku.isPopulated(arrId)) {
                if (verbose) Log.d("inspect", ""+arrId);
                sudoku.set(arrId, 0);
                int tmp = isSudoku(sudoku, null);
                if (tmp == -1) throw new ArithmeticException("trueGrid was not a solution or valid sudoku");
                else if (tmp == 0) sudoku.set(arrId, trueGrid[arrId]);
                // it is an error for us, if the implemented hint methods don't suffice to solve a
                // particular sudoku
                else Log.e("removed", ""+arrId);
            }
        return sudoku;
    }
}