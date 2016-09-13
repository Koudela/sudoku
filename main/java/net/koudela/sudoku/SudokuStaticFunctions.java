package net.koudela.sudoku;

import java.util.Arrays;
import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

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

    public static int[][] getNewPopulatedHints(int[] sudoku) {
        int[][] hints = getNewEmptyHints();
        for (int arrId : ALL_ARR_IDS) if (sudoku[arrId] != 0) for (int tempArrId : getStarGroup(arrId)) hints[tempArrId][sudoku[arrId] - 1]++;
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

    // a level one sudoku gets constructed by solely using AutoInsert1 logic
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

    // a level two sudoku gets constructed by using AutoInsert1 and AutoInsert2 logic
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

    // a level three sudoku is solvable by using AutoInsert1, AutoInsert2
    public static int[] makeLevelThreeSudoku(int[] levelTwoSudoku) {
        int[] sudoku = getNewEmptyGrid();
        int[][] hints = getNewEmptyHints();
        for (int arrId : ALL_ARR_IDS) sudoku[arrId] = levelTwoSudoku[arrId];
        for (int arrId : ALL_ARR_IDS) for (int tempArrId : getStarGroup(arrId)) if (sudoku[arrId] != 0) hints[tempArrId][sudoku[arrId] - 1]++;
        for (int arrId : getRandomizedArrIds()) {
            if (sudoku[arrId] == 0) continue;
            if (isSolvableSudoku(arrId, sudoku, true, true)) {
                for (int tempArrId : getStarGroup(arrId)) hints[tempArrId][sudoku[arrId] - 1]--;
                sudoku[arrId] = 0;
            }
        }
        return sudoku;
    }

    protected static boolean isLevel4Solvable(int arrId, int[][] hints, int[] sudoku) {
        return true;
    }

    // a level four sudoku is solvable by using AutoInsert1, AutoInsert2 TODO: choose construction parameters and implement
    public static int[] makeLevelFourSudoku(int[] levelThreeSudoku) {
        int[] sudoku = getNewEmptyGrid();
        int[][] hints = getNewEmptyHints();
        for (int arrId : ALL_ARR_IDS) sudoku[arrId] = levelThreeSudoku[arrId];
        for (int arrId : ALL_ARR_IDS) for (int tempArrId : getStarGroup(arrId)) if (sudoku[arrId] != 0) hints[tempArrId][sudoku[arrId] - 1]++;
        for (int arrId : getRandomizedArrIds()) {
            if (sudoku[arrId] == 0) continue;
            if (isLevel4Solvable(arrId, hints, sudoku)) {
                for (int tempArrId : getStarGroup(arrId)) hints[tempArrId][sudoku[arrId] - 1]--;
                sudoku[arrId] = 0;
            }
        }
        return sudoku;
    }

    // if exact 8 hints are displayed it returns the missing number, null otherwise
    public static int getAutoInsert1ByField(int arrId, int[][] hints, int[] sudoku) {
        if (sudoku[arrId] != 0) return 0;
        int number = 0;
        for (int num = 0; num < DIM; num++) if (hints[arrId][num] == 0) {
            if (number != 0) return 0;
            else number = num + 1;
        }
        return number;
    }

    // if a number is missing only once in 9 hint fields vertical, horizontal or group wise it gets returned
    // a populated field counts as a field with all possible 9 hints set
    // second returned int is the corresponding arrId
    // if nothing is found null gets returned
    public static Integer[] getAutoInsert2ByGroup(Integer[] group, int[][] hints, int[] sudoku) {
        for (int num = 1; num <= DIM; num++) {
            int count = 0;
            int tempArrId = -1;
            for (int arrId : group) {
                if (sudoku[arrId] != 0 || hints[arrId][num - 1] > 0) count++;
                else if (tempArrId == -1) tempArrId = arrId;
                else break;
            }
            if (count == 8) return new Integer[]{num, tempArrId};
        }
        return null;
    }

    public static Integer[] getAutoInsert2(int[][] hints, int[] sudoku) {
        for (int i = 0; i < DIM; i++) {
            Integer[] ai2arr = getAutoInsert2ByGroup(Sudoku.VERTICAL_GROUPS[i], hints, sudoku);
            if (ai2arr != null) return ai2arr;
            ai2arr = getAutoInsert2ByGroup(SudokuGroups.HORIZONTAL_GROUPS[i], hints, sudoku);
            if (ai2arr != null) return ai2arr;
            ai2arr = getAutoInsert2ByGroup(SudokuGroups.GROUPED_GROUPS[i], hints, sudoku);
            if (ai2arr != null) return ai2arr;
        }
        return null;
    }

    public static int[] solveSudoku(int deletionAtArrId, int[] sudoku, boolean byAutoInsert1, boolean byAutoInsert2) {
        int[] solution = getNewEmptyGrid();
        Set<Integer> emptyArrIds = Collections.newSetFromMap(new ConcurrentHashMap<Integer, Boolean>());
        for (int arrId : ALL_ARR_IDS)
            if (sudoku[arrId] != 0) solution[arrId] = sudoku[arrId];
            else emptyArrIds.add(arrId);
        if (deletionAtArrId > -1) {
            emptyArrIds.add(deletionAtArrId);
            solution[deletionAtArrId] = 0;
        }
        int hints[][] = getNewPopulatedHints(solution);
        boolean changedSolution;
        do {
            changedSolution = false;
            for (int arrId : emptyArrIds) {
                int number = getAutoInsert1ByField(arrId, hints, sudoku);
                if (number > 0) {
                    solution[arrId] = number;
                    for (int tempArrId : getStarGroup(arrId)) hints[tempArrId][number - 1]++;
                    emptyArrIds.remove(arrId);
                    changedSolution = true;
                }
            }
            if (!changedSolution) {
                Integer[] ai2arr = getAutoInsert2(hints, solution);
                if (ai2arr != null) {
                    solution[ai2arr[1]] = ai2arr[0];
                    for (int tempArrId : getStarGroup(ai2arr[1])) hints[tempArrId][ai2arr[0] - 1]++;
                    emptyArrIds.remove(ai2arr[1]);
                    changedSolution = true;
                }
            }
        } while(changedSolution);
        return solution;
    }

    public static boolean isSolvableSudoku(int arrId, int[] sudoku, boolean byAutoInsert1, boolean byAutoInsert2) {
        return isTrueGrid(solveSudoku(arrId, sudoku, byAutoInsert1, byAutoInsert2));
    }

    public static boolean isTrueGrid(int[] sudoku) {
        int[][] hints = getNewEmptyHints();
        for (int arrId : ALL_ARR_IDS) {
            if (sudoku[arrId] == 0) return false;
            for (int tempArrId : getStarGroup(arrId)) hints[tempArrId][sudoku[arrId] - 1]++;
        }
        for (int arrId : ALL_ARR_IDS) if (hints[arrId][sudoku[arrId] - 1] != 1) return false;
        return true;
    }
}
