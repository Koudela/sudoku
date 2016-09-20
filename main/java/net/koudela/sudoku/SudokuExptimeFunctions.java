package net.koudela.sudoku;

import android.util.Log;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class SudokuExptimeFunctions extends SudokuStaticFunctions {

    // be careful, this algorithm resides in EXPTIME
    public static Set<Set<Integer>> powerSet(final Set<Integer> originalSet) {
        Set<Set<Integer>> sets = new HashSet<>();
        if (originalSet.isEmpty()) {
            sets.add(new HashSet<Integer>());
            return sets;
        }
        List<Integer> list = new ArrayList<>(originalSet);
        Integer head = list.get(0);
        Set<Integer> rest = new HashSet<>(list.subList(1, list.size()));
        for (Set<Integer> set : powerSet(rest)) {
            Set<Integer> newSet = new HashSet<>();
            newSet.add(head);
            newSet.addAll(set);
            sets.add(newSet);
            sets.add(set);
        }
        return sets;
    }

    // be careful, this algorithm resides in EXPTIME
    // @computedSolution: input must be null or a clone of sudoku, holds the calculated grid
    protected static int solveByBacktracking(final Playground sudoku, final boolean countDown, Playground computedSolution) {
        Hints hints = new Hints();
        Set<Integer> arrIdsChangedHints = new HashSet<>();
        Set<Integer> arrIdsChangedValues = new HashSet<>();
        int initValue = countDown?9:1;
        // initial check
        // we don't wanna init the advanced hints yet (if we use them) - updateSudokuStart takes care of that
        hints.init(sudoku, true);
        for (int arrId : sudoku.getPopulatedArrIds())
             if (hints.getPlainHint(arrId, sudoku.get(arrId) - 1) > 1) return -1; // -> Sudoku is not solvable
        // start the main computing
        if (computedSolution == null) computedSolution = new Playground(sudoku);
        //hints.setUseAdv1(true); we are minimal faster without
        // -> 20 times makeMinimalSudokuByBruteForceBacktrackingOutOfTrueGrid(level4sudoku) runs in 1:19.700 instead of 1:21.390 (min:sec.milliseconds)
        //hints.setUseAdv2(true); we are about 2 times faster without
        //hints.setUseAdv3(true); we are about 20 times (!) faster without
        updateSudokuStart(arrIdsChangedHints, arrIdsChangedValues, computedSolution, hints, true, true);
        if (computedSolution.getSizeNotPopulatedArrIds() == 0) return 1; // -> Sudoku is a valid Sudoku
        Set<Integer> mayBeDependValidValues = new HashSet<>(computedSolution.getPopulatedArrIds());
        Map<Integer, Set<Integer>> dependentValidValues = new HashMap<>();
        // backtrack
        for (int arrId = 0; arrId < DIM * DIM; ) {
            if (computedSolution.isPopulated(arrId)) {
                arrId++;
                continue;
            }
            // "first" try
            computedSolution.set(arrId, initValue);
            boolean outOfBound = false;
            while (outOfBound || computedSolution.get(arrId) > 9 || computedSolution.get(arrId) < 1 || hints.isHint(arrId, computedSolution.get(arrId) - 1)) {
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
            hints.incrementStarGroup(arrId, computedSolution.get(arrId) - 1);
            arrIdsChangedValues.clear();
            updateSudokuStart(arrIdsChangedHints, arrIdsChangedValues, computedSolution, hints, true, true);
            if (computedSolution.getSizeNotPopulatedArrIds() == 0) return 0; // -> Sudoku may be a valid Sudoku
            dependentValidValues.put(arrId, new HashSet<>(arrIdsChangedValues));
            mayBeDependValidValues.addAll(arrIdsChangedValues);
            arrId++;
        }
        return 0; // Sudoku -> may be a valid Sudoku
    }

    // be careful, this algorithm resides in EXPTIME
    // @solution: input must be null or a clone of sudoku, holds the calculated grid
    public static int isSudoku(final Playground sudoku, Playground solution) {
        if (solution == null) solution = new Playground(sudoku);
        int result = solveByBacktracking(sudoku, false, solution);
        if (result != 0) return result; // -> -1: Sudoku is not solvable, 1: Sudoku is a valid Sudoku
        Playground possibleSolution = new Playground(solution);
        solution.init(sudoku);
        solveByBacktracking(sudoku, true, solution);
        if (possibleSolution.equals(solution)) return 1; // -> Sudoku is a valid Sudoku
        return 0; // -> Sudoku has more than one possible solution
    }

    // be careful, this algorithm resides in EXPTIME
    public static Playground makeRandomTrueGridByBruteForceBacktracking() {
        Playground sudoku = new Playground();
        Playground solution = new Playground();
        Hint hint = new Hint();
        List<List<Integer>> numbersLeft = new ArrayList<>();
        // init
        for (int arrId :ALL_ARR_IDS) {
            numbersLeft.add(new ArrayList<>(Arrays.asList(1, 2, 3, 4, 5, 6, 7, 8, 9)));
            Collections.shuffle(numbersLeft.get(arrId));
        }
        // set value
        int isSudoku;
        for (int arrId : getRandomizedArrIds())
            do {
                do {
                    sudoku.set(arrId, numbersLeft.get(arrId).get(0));
                    numbersLeft.get(arrId).remove(0);
                } while (hint.get(arrId, sudoku.get(arrId) - 1) > 1);
                solution.init(sudoku);
                isSudoku = isSudoku(sudoku, solution);
                if (isSudoku == 1) return solution;
                if (isSudoku == 0) Hints.incrementStarGroup(arrId, sudoku.get(arrId) - 1, hint);
            } while (isSudoku == -1);
        return sudoku;
    }

    // be careful, this algorithm resides in EXPTIME
    public static Playground makeMinimalSudokuByBruteForceBacktrackingOutOfTrueGrid(final int[] trueGrid, boolean verbose) {
        Playground sudoku = new Playground(trueGrid);
        // erase values
        for (int arrId : getRandomizedArrIds())
            if (sudoku.isPopulated(arrId)) {
                if (verbose) Log.d("inspect", ""+arrId);
                sudoku.set(arrId, 0);
                int tmp = isSudoku(sudoku, null);
                if (tmp == -1) throw new ArithmeticException();
                else if (tmp == 0) sudoku.set(arrId, trueGrid[arrId]);
                else if (verbose) Log.d("removed", ""+arrId);
            }
        return sudoku;
    }
}