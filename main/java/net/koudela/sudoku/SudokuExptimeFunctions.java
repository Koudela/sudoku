package net.koudela.sudoku;

import android.util.Log;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class SudokuExptimeFunctions extends SudokuStaticFunctions {

    // be careful, this algorithm resides in EXPTIME
    public static Set<Set<Integer>> powerSet(Set<Integer> originalSet) {
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
    protected static Playground solveByBacktracking(Playground sudoku, boolean countDown) {
        Hint hint = new Hint();
        Playground computedSolution = new Playground(sudoku);
        int initValue = countDown?9:1;
        // init
        Hints.populatePlainHints(hint, computedSolution);
        // initial check
        for (int arrId : ALL_ARR_IDS)
            for (int num = 0; num < DIM; num++)
                if (hint.get(arrId, num) > 3) return null; // -> Sudoku is not solvable
        // backtrack
        for (int arrId = 0; arrId < DIM * DIM; ) {
            if (sudoku.isPopulated(arrId)) {
                arrId++;
                continue;
            }
            computedSolution.set(arrId, initValue);
            boolean outOfBound = false;
            while (outOfBound || computedSolution.get(arrId) > 9 || computedSolution.get(arrId) < 1 || hint.isHint(arrId, computedSolution.get(arrId) - 1)) {
                try {
                    outOfBound = false;
                    computedSolution.set(arrId, computedSolution.get(arrId) + (countDown ? -1 : 1));
                } catch (IllegalArgumentException e) {
                    outOfBound = true;
                }
                // step back
                if (outOfBound || computedSolution.get(arrId) > 9 || computedSolution.get(arrId) < 1) {
                    if (arrId == 0) return null; // -> Sudoku is not solvable
                    arrId--;
                    while (sudoku.isPopulated(arrId)) {
                        if (arrId == 0) return null; // -> Sudoku is not solvable
                        arrId--;
                    }
                    Hints.decrementStarGroup(arrId, computedSolution.get(arrId) - 1, hint);
                    try {
                        outOfBound = false;
                        computedSolution.set(arrId, computedSolution.get(arrId) + (countDown ? -1 : 1));
                    } catch (IllegalArgumentException e) {
                        outOfBound = true;
                    }
                }
            }
            Hints.incrementStarGroup(arrId, computedSolution.get(arrId) - 1, hint);
            arrId++;
        }
        return computedSolution;
    }

    // be careful, this algorithm resides in EXPTIME
    public static int isSudoku(Playground sudoku) {
        Playground possibleSolution = solveByBacktracking(sudoku, false);
        if (possibleSolution == null) return -1; // -> Sudoku is not solvable
        if (possibleSolution.equals(solveByBacktracking(sudoku, true))) return 1; // -> Sudoku is a valid Sudoku
        return 0; // -> Sudoku has more than one possible solution
    }

    // be careful, this algorithm resides in EXPTIME
    public static Playground makeTrueGridByBruteForceBacktracking() {
        Playground sudoku = new Playground();
        Hint hint = new Hint();
        List<List<Integer>> numbersLeft = new ArrayList<>();
        // init
        for (int arrId :ALL_ARR_IDS) {
            numbersLeft.add(new ArrayList<Integer>());
            for (int number = 1; number <= DIM; number++) numbersLeft.get(arrId).add(number);
        }
        // set value
        int isSudoku;
        for (int arrId : getRandomizedArrIds())
            do {
                do {
                    sudoku.set(arrId, numbersLeft.get(arrId).get(0));
                    numbersLeft.get(arrId).remove(0);
                } while (hint.get(arrId, sudoku.get(arrId) - 1) > 1);
                isSudoku = isSudoku(sudoku);
                if (isSudoku == 1) return solveByBacktracking(sudoku, false);
                if (isSudoku == 0) Hints.incrementStarGroup(arrId, sudoku.get(arrId) - 1, hint);

            } while (isSudoku == -1) ;
        return sudoku;
    }

    // be careful, this algorithm resides in EXPTIME
    public static Playground makeMinimalSudokuByBruteForceBacktrackingOutOfTrueGrid(int[] trueGrid) {
        Playground sudoku = new Playground(trueGrid);
        // erase values
        for (int arrId : getRandomizedArrIds())
            if (sudoku.isPopulated(arrId)) {
                Log.v("inspect", ""+arrId);
                sudoku.set(arrId, 0);
                if (isSudoku(sudoku) == 0) sudoku.set(arrId, trueGrid[arrId]);
                else Log.v("removed", ""+arrId);
            }
        return sudoku;
    }

    // be careful, this algorithm resides in EXPTIME
    public static Playground makeMinimalSudokuByBruteForceBacktracking() {
        Playground sudoku = new Playground();
        Hint hint = new Hint();
        List<List<Integer>> numbersLeft = new ArrayList<>();
        // init
        for (int arrId : Sudoku.ALL_ARR_IDS) {
            numbersLeft.add(new ArrayList<Integer>());
            for (int number = 1; number <= DIM; number++) numbersLeft.get(arrId).add(number);
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
                isSudoku = isSudoku(sudoku);
                if (isSudoku == 1) return sudoku;
                if (isSudoku == 0) Hints.incrementStarGroup(arrId, sudoku.get(arrId) - 1, hint);
            } while (isSudoku == -1) ;
        return sudoku;
    }
}