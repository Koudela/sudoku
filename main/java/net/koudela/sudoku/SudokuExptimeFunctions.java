package net.koudela.sudoku;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class SudokuExptimeFunctions extends SudokuStaticFunctions {

    // be careful, this algorithm resides in EXPTIME
    protected static int[] solveByBacktracking(int[] sudoku, boolean countDown) {
        int[][] hints = new int[DIM * DIM][DIM];
        int[] computedSolution = new int[DIM * DIM];
        int initValue = countDown?10:0;
        // init
        for (int arrId = 0; arrId < DIM * DIM; arrId++) for (int number = 0; number < DIM; number++) hints[arrId][number] = 0;
        for (int arrId = 0; arrId < DIM * DIM; arrId++) {
            if (sudoku[arrId] != 0) {
                computedSolution[arrId] = sudoku[arrId];
                for (int tempArrId : SudokuGroups.getStarGroup(arrId)) hints[tempArrId][computedSolution[arrId] - 1]++;
            } else computedSolution[arrId] = initValue;
        }
        // initial check
        for (int arrId = 0; arrId < DIM * DIM; arrId++) for (int number = 0; number < DIM; number++) if (hints[arrId][number] > 3) return null; // -> Sudoku is not solvable
        // backtrack
        for (int arrId = 0; arrId < DIM * DIM; ) {
            if (sudoku[arrId] != 0) {
                arrId++;
                continue;
            }
            do {
                computedSolution[arrId] += countDown?-1:1;
                // step back
                if (computedSolution[arrId] > 9 || computedSolution[arrId] < 1) {
                    if (arrId == 0) return null; // -> Sudoku is not solvable
                    computedSolution[arrId] = initValue;
                    arrId--;
                    while (sudoku[arrId] != 0) {
                        if (arrId == 0) return null; // -> Sudoku is not solvable
                        arrId--;
                    }
                    for (int tempArrId : SudokuGroups.getStarGroup(arrId)) hints[tempArrId][computedSolution[arrId] - 1]--;
                    computedSolution[arrId] += countDown?-1:1;
                }
            } while(computedSolution[arrId] > 9 || computedSolution[arrId] < 1 || hints[arrId][computedSolution[arrId] - 1] > 0);
            for (int tempArrId : SudokuGroups.getStarGroup(arrId)) hints[tempArrId][computedSolution[arrId] - 1]++;
            arrId++;
        }
        return computedSolution;
    }

    // be careful, this algorithm resides in EXPTIME
    public static int isSudoku(int[] sudoku) {
        int[] possibleSolution = solveByBacktracking(sudoku, false);
        if (possibleSolution == null) return -1; // -> Sudoku is not solvable
        if (Arrays.equals(possibleSolution, solveByBacktracking(sudoku, true))) return 1; // -> Sudoku is a valid Sudoku
        return 0; // -> Sudoku has more than one possible solution
    }

    // be careful, this algorithm resides in EXPTIME
    public static int[] makeTrueGridByBruteForceBacktracking() {
        int[] sudoku = getNewEmptyGrid();
        Integer[] arrIds = getRandomizedArrIds();
        int[][] hints = new int[DIM * DIM][DIM];
        List<List> numbersLeft = new ArrayList<>();
        // init
        for (int arrId :ALL_ARR_IDS) {
            numbersLeft.add(new ArrayList<Integer>());
            for (int number = 0; number < DIM; number++) {
                hints[arrId][number] = 0;
                numbersLeft.get(arrId).add(number + 1);
            }
            Collections.shuffle(numbersLeft.get(arrId));
        }
        // set value
        for (int i = 0; i < DIM * DIM;) {
            int arrId = arrIds[i];
            do {
                sudoku[arrId] = (Integer) numbersLeft.get(arrId).get(0);
                numbersLeft.get(arrId).remove(0);
            } while (hints[arrId][sudoku[arrId] - 1] > 1);
            int isSudoku = isSudoku(sudoku);
            if (isSudoku == 1) return solveByBacktracking(sudoku, false);
            else if (isSudoku == 0) {
                for (int tempArrId : SudokuGroups.getStarGroup(arrId)) hints[tempArrId][sudoku[arrId] - 1]++;
                i++;
            }
        }
        return sudoku;
    }

    // be careful, this algorithm resides in EXPTIME
    public static int[] makeMinimalSudokuByBruteForceBacktrackingOutOfTrueGrid(int[] trueGrid) {
        int[] sudoku = new int[DIM * DIM];
        Integer[] arrIds = new Integer[DIM * DIM];
        // init
        for (int arrId = 0; arrId < DIM * DIM; arrId++) {
            arrIds[arrId] = arrId;
            sudoku[arrId] = trueGrid[arrId];
        }
        Collections.shuffle(Arrays.asList(arrIds));
        // erase values
        for (int arrId : arrIds) {
            sudoku[arrId] = 0;
            if (isSudoku(sudoku) == 0) sudoku[arrId] = trueGrid[arrId];
        }
        return sudoku;
    }

    // be careful, this algorithm resides in EXPTIME
    public static int[] makeMinimalSudokuByBruteForceBacktracking() {
        int[] sudoku = getNewEmptyGrid();
        Integer[] arrIds = getRandomizedArrIds();
        int[][] hints = new int[DIM * DIM][DIM];
        List<List> numbersLeft = new ArrayList<>();
        // init
        for (int arrId : Sudoku.ALL_ARR_IDS) {
            arrIds[arrId] = arrId;
            numbersLeft.add(new ArrayList<Integer>());
            for (int number = 0; number < DIM; number++) {
                hints[arrId][number] = 0;
                numbersLeft.get(arrId).add(number + 1);
            }
            Collections.shuffle(numbersLeft.get(arrId));
        }
        // set value
        for (int i = 0; i < DIM * DIM;) {
            int arrId = arrIds[i];
            do {
                sudoku[arrId] = (Integer) numbersLeft.get(arrId).get(0);
                numbersLeft.get(arrId).remove(0);
            } while (hints[arrId][sudoku[arrId] - 1] > 1);
            int isSudoku = isSudoku(sudoku);
            if (isSudoku == 1) return sudoku;
            else if (isSudoku == 0) {
                for (int tempArrId : SudokuGroups.getStarGroup(arrId)) hints[tempArrId][sudoku[arrId] - 1]++;
                i++;
            }
        }
        return sudoku;
    }
}