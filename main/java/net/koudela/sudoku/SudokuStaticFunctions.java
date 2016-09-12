package net.koudela.sudoku;

import java.util.Arrays;
import java.util.Collections;
import java.util.Set;

public class SudokuStaticFunctions extends SudokuGroups {
    public static int[] getNewEmptyGrid() {
        int[] emptyGrid = new int[DIM * DIM];
        for (int arrId : ALL_ARR_IDS) emptyGrid[arrId] = 0;
        return emptyGrid;
    }

    public static int[][] getNewEmptyHints() {
        int[][] hints = new int[DIM * DIM][DIM];
        for (int arrId : ALL_ARR_IDS) for (int number = 0; number < DIM; number++) hints[arrId][number] = 0;
        return hints;
    }

    public static String sudokuToString(int[] sudoku) {
        String sudokuString = "";
        for (int i = 0; i < DIM; i++) {
            sudokuString += "\n";
            for (int j = 0; j < DIM; j++) sudokuString += sudoku[i * DIM + j] + " ";
        }
        return sudokuString;
    }

    public static Integer[] getRandomizedArrIds() {
        Integer[] arrIds = new Integer[DIM * DIM];
        for (int arrId = 0; arrId < DIM * DIM; arrId++) arrIds[arrId] = arrId;
        Collections.shuffle(Arrays.asList(arrIds));
        return arrIds;
    }

    protected static boolean isAutoInsert1Solvable(int arrId, int[][] hints) {
        for (int number = 0; number < DIM; number++) if (hints[arrId][number] == 0) return false;
        return true;
    }

    // a level one sudoku is solvable by solely using AutoInsert1;
    public static int[] makeLevelOneSudoku(int[] solution) {
        int[] sudoku = getNewEmptyGrid();
        int[][] hints = getNewEmptyHints();
        for (int arrId : ALL_ARR_IDS) sudoku[arrId] = solution[arrId];
        for (int arrId : ALL_ARR_IDS) for (int tempArrId : getStarGroup(arrId)) hints[tempArrId][sudoku[arrId] - 1]++;
        for (int arrId : getRandomizedArrIds()) if (isAutoInsert1Solvable(arrId, hints)) {
            for (int tempArrId : getStarGroup(arrId)) hints[tempArrId][sudoku[arrId] - 1]--;
            sudoku[arrId] = 0;
        }
        return sudoku;
    }

    protected static boolean isAutoInsert2Solvable(int arrId, int[][] hints, int[] sudoku) {
        return (isAutoInsert2SolvableSub(getVerticalGroup(arrId), arrId, hints, sudoku)
                || isAutoInsert2SolvableSub(getHorizontalGroup(arrId), arrId, hints, sudoku)
                || isAutoInsert2SolvableSub(getGroupedGroup(arrId), arrId, hints, sudoku));
    }

    protected static boolean isAutoInsert2SolvableSub(Set<Integer> group, int arrId, int[][] hints, int[] sudoku) {
        for (int tempArrId : group) if (sudoku[tempArrId] == 0 && hints[tempArrId][sudoku[arrId] - 1] == 1) return false;
        return true;
    }

    // a level two sudoku is solvable by using AutoInsert1 and AutoInsert2
    public static int[] makeLevelTwoSudoku(int[] levelOneSudoku) {
        int[] sudoku = getNewEmptyGrid();
        int[][] hints = getNewEmptyHints();
        for (int arrId : ALL_ARR_IDS) sudoku[arrId] = levelOneSudoku[arrId];
        for (int arrId : ALL_ARR_IDS) for (int tempArrId : getStarGroup(arrId)) if (sudoku[arrId] != 0) hints[tempArrId][sudoku[arrId] - 1]++;
        for (int arrId : getRandomizedArrIds()) {
            if (sudoku[arrId] == 0) continue;
            if (isAutoInsert2Solvable(arrId, hints, sudoku)) {
                for (int tempArrId : getStarGroup(arrId)) hints[tempArrId][sudoku[arrId] - 1]--;
                sudoku[arrId] = 0;
            }
        }
        return sudoku;
    }

    protected static boolean isLevel3Solvable(int arrId, int[][] hints, int[] sudoku) {
        return true;
    }

    // a level two sudoku is solvable by using AutoInsert1, AutoInsert2 and ... TODO: Choose and implement isLevel3Solvable
    public static int[] makeLevelThreeSudoku(int[] levelOneSudoku) {
        int[] sudoku = getNewEmptyGrid();
        int[][] hints = getNewEmptyHints();
        for (int arrId : ALL_ARR_IDS) sudoku[arrId] = levelOneSudoku[arrId];
        for (int arrId : ALL_ARR_IDS) for (int tempArrId : getStarGroup(arrId)) if (sudoku[arrId] != 0) hints[tempArrId][sudoku[arrId] - 1]++;
        for (int arrId : getRandomizedArrIds()) {
            if (sudoku[arrId] == 0) continue;
            if (isLevel3Solvable(arrId, hints, sudoku)) {
                for (int tempArrId : getStarGroup(arrId)) hints[tempArrId][sudoku[arrId] - 1]--;
                sudoku[arrId] = 0;
            }
        }
        return sudoku;
    }
}
