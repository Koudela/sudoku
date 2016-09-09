package net.koudela.sudoku;

public class SudokuGroups {
    public final static int[][] VERTICAL_GROUPS = {
            { 0, 1, 2, 3, 4, 5, 6, 7, 8},
            { 9,10,11,12,13,14,15,16,17},
            {18,19,20,21,22,23,24,25,26},
            {27,28,29,30,31,32,33,34,35},
            {36,37,38,39,40,41,42,43,44},
            {45,46,47,48,49,50,51,52,53},
            {54,55,56,57,58,59,60,61,62},
            {63,64,65,66,67,68,69,70,71},
            {72,73,74,75,76,77,78,79,80}};
    public final static int[] ID_VERTICAL_GROUPS = {
              0, 0, 0, 0, 0, 0, 0, 0, 0,
              1, 1, 1, 1, 1, 1, 1, 1, 1,
              2, 2, 2, 2, 2, 2, 2, 2, 2,
              3, 3, 3, 3, 3, 3, 3, 3, 3,
              4, 4, 4, 4, 4, 4, 4, 4, 4,
              5, 5, 5, 5, 5, 5, 5, 5, 5,
              6, 6, 6, 6, 6, 6, 6, 6, 6,
              7, 7, 7, 7, 7, 7, 7, 7, 7,
              8, 8, 8, 8, 8, 8, 8, 8, 8};
    public final static int[][] HORIZONTAL_GROUPS = {
            { 0, 9,18,27,36,45,54,63,72},
            { 1,10,19,28,37,46,55,64,73},
            { 2,11,20,29,38,47,56,65,74},
            { 3,12,21,30,39,48,57,66,75},
            { 4,13,22,31,40,49,58,67,76},
            { 5,14,23,32,41,50,59,68,77},
            { 6,15,24,33,42,51,60,69,78},
            { 7,16,25,34,43,52,61,70,79},
            { 8,17,26,35,44,53,62,71,80}};
    public final static int[] ID_HORIZONTAL_GROUPS = {
              0, 1, 2, 3, 4, 5, 6, 7, 8,
              0, 1, 2, 3, 4, 5, 6, 7, 8,
              0, 1, 2, 3, 4, 5, 6, 7, 8,
              0, 1, 2, 3, 4, 5, 6, 7, 8,
              0, 1, 2, 3, 4, 5, 6, 7, 8,
              0, 1, 2, 3, 4, 5, 6, 7, 8,
              0, 1, 2, 3, 4, 5, 6, 7, 8,
              0, 1, 2, 3, 4, 5, 6, 7, 8,
              0, 1, 2, 3, 4, 5, 6, 7, 8};
    public final static int[][] GROUPED_GROUPS = {
            { 0, 1, 2, 9,10,11,18,19,20},
            {27,28,29,36,37,38,45,46,47},
            {54,55,56,63,64,65,72,73,74},
            { 3, 4, 5,12,13,14,21,22,23},
            {30,31,32,39,40,41,48,49,50},
            {57,58,59,66,67,68,75,76,77},
            { 6, 7, 8,15,16,17,24,25,26},
            {33,34,35,42,43,44,51,52,53},
            {60,61,62,69,70,71,78,79,80}};
    public final static int[] ID_GROUPED_GROUPS = {
              0, 0, 0, 3, 3, 3, 6, 6, 6,
              0, 0, 0, 3, 3, 3, 6, 6, 6,
              0, 0, 0, 3, 3, 3, 6, 6, 6,
              1, 1, 1, 4, 4, 4, 7, 7, 7,
              1, 1, 1, 4, 4, 4, 7, 7, 7,
              1, 1, 1, 4, 4, 4, 7, 7, 7,
              2, 2, 2, 5, 5, 5, 8, 8, 8,
              2, 2, 2, 5, 5, 5, 8, 8, 8,
              2, 2, 2, 5, 5, 5, 8, 8, 8};
    public final static int[] TRUE_GRID = {
              1,4,7,2,5,8,3,6,9,
              2,5,8,3,6,9,4,7,1,
              3,6,9,4,7,1,5,8,2,
              4,7,1,5,8,2,6,9,3,
              5,8,2,6,9,3,7,1,4,
              6,9,3,7,1,4,8,2,5,
              7,1,4,8,2,5,9,3,6,
              8,2,5,9,3,6,1,4,7,
              9,3,6,1,4,7,2,5,8};

    public static int[] getVerticalGroup(int arrId) {
        return SudokuGroups.VERTICAL_GROUPS[SudokuGroups.ID_VERTICAL_GROUPS[arrId]];
    }

    public static int[] getHorizontalGroup(int arrId) {
        return SudokuGroups.HORIZONTAL_GROUPS[SudokuGroups.ID_HORIZONTAL_GROUPS[arrId]];
    }

    public static int[] getGroupedGroup(int arrId) {
        return SudokuGroups.GROUPED_GROUPS[SudokuGroups.ID_GROUPED_GROUPS[arrId]];
    }

    public static int[] getStarGroup(int arrId) {
        int[] starGroup = new int[3 * SudokuData.DIM];
        int count = 0;
        for (int tempArrId: getVerticalGroup(arrId)) starGroup[count++] = tempArrId;
        for (int tempArrId: getHorizontalGroup(arrId)) starGroup[count++] = tempArrId;
        for (int tempArrId: getGroupedGroup(arrId)) starGroup[count++] = tempArrId;
        return starGroup; // contains 3 times arrId!
    }

    public static int[] getComplementVerticalGroup(int arrId) {
        int[] complementGroup = new int[6];
        int count = 0;
        int colId = arrId / SudokuData.DIM;
        int rowId = arrId % SudokuData.DIM;
        switch (rowId / 3) {// identifier row group
            case 0:
                for (int i = 0; i < 3; i++) {
                    complementGroup[count++] = colId * SudokuData.DIM + 3 + i;
                    complementGroup[count++] = colId * SudokuData.DIM + 6 + i;
                }
                break;
            case 1:
                for (int i = 0; i < 3; i++) {
                    complementGroup[count++] = colId * SudokuData.DIM + i;
                    complementGroup[count++] = colId * SudokuData.DIM + 6 + i;
                }
                break;
            case 2:
                for (int i = 0; i < 3; i++) {
                    complementGroup[count++] = colId * SudokuData.DIM + i;
                    complementGroup[count++] = colId * SudokuData.DIM + 3 + i;
                }
                break;
        }
        return complementGroup;
    }

    public static int[] getComplementHorizontalGroup(int arrId) {
        int[] complementGroup = new int[6];
        int count = 0;
        int colId = arrId / SudokuData.DIM;
        int rowId = arrId % SudokuData.DIM;
        switch (colId / 3) {// identifier col group
            case 0: // == rowId + 9 * (i + 0)
                for (int i = 0; i < 3; i++) {
                    complementGroup[count++] = rowId + SudokuData.DIM * (3 + i);
                    complementGroup[count++] = rowId + SudokuData.DIM * (6 + i);
                }
                break;
            case 1:
                for (int i = 0; i < 3; i++) {
                    complementGroup[count++] = rowId + SudokuData.DIM * i;
                    complementGroup[count++] = rowId + SudokuData.DIM * (6 + i);
                }
                break;
            case 2:
                for (int i = 0; i < 3; i++) {
                    complementGroup[count++] = rowId + SudokuData.DIM * i;
                    complementGroup[count++] = rowId + SudokuData.DIM * (3 + i);
                }
                break;
        }
        return complementGroup;
    }
}
