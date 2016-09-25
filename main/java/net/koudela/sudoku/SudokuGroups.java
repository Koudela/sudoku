package net.koudela.sudoku;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * Arrays and static function providing default values and grouping of identifiers for 9x9 sudokus
 *
 * @author Thomas Koudela
 * @version 1.0 stable
 */
@SuppressWarnings("unused")
class SudokuGroups {
    final static int DIM = 9;
    final static Integer[] ALL_ARR_IDS = {
             0 , 1, 2, 3, 4, 5, 6, 7, 8,
             9 ,10,11,12,13,14,15,16,17,
             18,19,20,21,22,23,24,25,26,
             27,28,29,30,31,32,33,34,35,
             36,37,38,39,40,41,42,43,44,
             45,46,47,48,49,50,51,52,53,
             54,55,56,57,58,59,60,61,62,
             63,64,65,66,67,68,69,70,71,
             72,73,74,75,76,77,78,79,80};
    final static Integer[][] VERTICAL_GROUPS = {
            { 0, 1, 2, 3, 4, 5, 6, 7, 8},
            { 9,10,11,12,13,14,15,16,17},
            {18,19,20,21,22,23,24,25,26},
            {27,28,29,30,31,32,33,34,35},
            {36,37,38,39,40,41,42,43,44},
            {45,46,47,48,49,50,51,52,53},
            {54,55,56,57,58,59,60,61,62},
            {63,64,65,66,67,68,69,70,71},
            {72,73,74,75,76,77,78,79,80}};
    final static int[] ID_VERTICAL_GROUPS = {
              0, 0, 0, 0, 0, 0, 0, 0, 0,
              1, 1, 1, 1, 1, 1, 1, 1, 1,
              2, 2, 2, 2, 2, 2, 2, 2, 2,
              3, 3, 3, 3, 3, 3, 3, 3, 3,
              4, 4, 4, 4, 4, 4, 4, 4, 4,
              5, 5, 5, 5, 5, 5, 5, 5, 5,
              6, 6, 6, 6, 6, 6, 6, 6, 6,
              7, 7, 7, 7, 7, 7, 7, 7, 7,
              8, 8, 8, 8, 8, 8, 8, 8, 8};
    final static Integer[][] HORIZONTAL_GROUPS = {
            { 0, 9,18,27,36,45,54,63,72},
            { 1,10,19,28,37,46,55,64,73},
            { 2,11,20,29,38,47,56,65,74},
            { 3,12,21,30,39,48,57,66,75},
            { 4,13,22,31,40,49,58,67,76},
            { 5,14,23,32,41,50,59,68,77},
            { 6,15,24,33,42,51,60,69,78},
            { 7,16,25,34,43,52,61,70,79},
            { 8,17,26,35,44,53,62,71,80}};
    final static int[] ID_HORIZONTAL_GROUPS = {
              0, 1, 2, 3, 4, 5, 6, 7, 8,
              0, 1, 2, 3, 4, 5, 6, 7, 8,
              0, 1, 2, 3, 4, 5, 6, 7, 8,
              0, 1, 2, 3, 4, 5, 6, 7, 8,
              0, 1, 2, 3, 4, 5, 6, 7, 8,
              0, 1, 2, 3, 4, 5, 6, 7, 8,
              0, 1, 2, 3, 4, 5, 6, 7, 8,
              0, 1, 2, 3, 4, 5, 6, 7, 8,
              0, 1, 2, 3, 4, 5, 6, 7, 8};
    final static Integer[][] GROUPED_GROUPS = {
            { 0, 1, 2, 9,10,11,18,19,20},
            {27,28,29,36,37,38,45,46,47},
            {54,55,56,63,64,65,72,73,74},
            { 3, 4, 5,12,13,14,21,22,23},
            {30,31,32,39,40,41,48,49,50},
            {57,58,59,66,67,68,75,76,77},
            { 6, 7, 8,15,16,17,24,25,26},
            {33,34,35,42,43,44,51,52,53},
            {60,61,62,69,70,71,78,79,80}};
    final static int[] ID_GROUPED_GROUPS = {
              0, 0, 0, 3, 3, 3, 6, 6, 6,
              0, 0, 0, 3, 3, 3, 6, 6, 6,
              0, 0, 0, 3, 3, 3, 6, 6, 6,
              1, 1, 1, 4, 4, 4, 7, 7, 7,
              1, 1, 1, 4, 4, 4, 7, 7, 7,
              1, 1, 1, 4, 4, 4, 7, 7, 7,
              2, 2, 2, 5, 5, 5, 8, 8, 8,
              2, 2, 2, 5, 5, 5, 8, 8, 8,
              2, 2, 2, 5, 5, 5, 8, 8, 8};
    final static int[] TRUE_GRID = {
            1, 4, 7, 2, 5, 8, 3, 6, 9,
            2, 5, 8, 3, 6, 9, 4, 7, 1,
            3, 6, 9, 4, 7, 1, 5, 8, 2,
            4, 7, 1, 5, 8, 2, 6, 9, 3,
            5, 8, 2, 6, 9, 3, 7, 1, 4,
            6, 9, 3, 7, 1, 4, 8, 2, 5,
            7, 1, 4, 8, 2, 5, 9, 3, 6,
            8, 2, 5, 9, 3, 6, 1, 4, 7,
            9, 3, 6, 1, 4, 7, 2, 5, 8};
    final static int[] SOLUTION = {
            9, 8, 7, 6, 3, 5, 2, 4, 1,
            3, 1, 4, 9, 7, 2, 6, 8, 5,
            2, 6, 5, 4, 8, 1, 3, 7, 9,
            1, 4, 2, 7, 5, 6, 8, 9, 3,
            5, 7, 8, 3, 4, 9, 1, 2, 6,
            6, 3, 9, 2, 1, 8, 4, 5, 7,
            8, 2, 6, 5, 9, 3, 7, 1, 4,
            7, 5, 3, 1, 2, 4, 9, 6, 8,
            4, 9, 1, 8, 6, 7, 5, 3, 2};
    final static int[] LEVEL1 = {
            9, 0, 7, 6, 0, 5, 2, 0, 0,
            3, 0, 0, 0, 0, 2, 6, 8, 0,
            2, 0, 5, 4, 0, 0, 3, 7, 0,
            1, 4, 2, 7, 0, 0, 8, 0, 0,
            5, 0, 8, 0, 4, 9, 0, 0, 0,
            0, 3, 9, 0, 1, 0, 0, 5, 7,
            0, 2, 0, 0, 9, 3, 0, 0, 4,
            0, 5, 0, 0, 0, 4, 0, 6, 8,
            0, 0, 1, 0, 0, 7, 5, 0, 2};
    final static int[] LEVEL2 = {
            9, 0, 7, 6, 0, 5, 2, 0, 0,
            3, 0, 0, 0, 0, 2, 6, 8, 0,
            0, 0, 5, 0, 0, 0, 3, 0, 0,
            0, 4, 2, 7, 0, 0, 8, 0, 0,
            5, 0, 8, 0, 4, 9, 0, 0, 0,
            0, 3, 0, 0, 1, 0, 0, 5, 7,
            0, 2, 0, 0, 9, 3, 0, 0, 4,
            0, 0, 0, 0, 0, 4, 0, 6, 0,
            0, 0, 1, 0, 0, 7, 5, 0, 2};
    final static int[] LEVEL3 = {
            9, 0, 0, 6, 0, 5, 2, 0, 0,
            3, 0, 0, 0, 0, 0, 6, 8, 0,
            0, 0, 5, 0, 0, 0, 0, 0, 0,
            0, 0, 2, 0, 0, 0, 8, 0, 0,
            5, 0, 8, 0, 4, 9, 0, 0, 0,
            0, 3, 0, 0, 1, 0, 0, 0, 0,
            0, 2, 0, 0, 9, 0, 0, 0, 4,
            0, 0, 0, 0, 0, 4, 0, 0, 0,
            0, 0, 1, 0, 0, 7, 5, 0, 2};
    final static int[] LEVEL4 = {
            0, 0, 0, 6, 0, 5, 2, 0, 0,
            3, 0, 0, 0, 0, 0, 6, 8, 0,
            0, 0, 5, 0, 0, 0, 0, 0, 0,
            0, 0, 2, 0, 0, 0, 8, 0, 0,
            5, 0, 8, 0, 4, 9, 0, 0, 0,
            0, 3, 0, 0, 1, 0, 0, 0, 0,
            0, 2, 0, 0, 9, 0, 0, 0, 4,
            0, 0, 0, 0, 0, 4, 0, 0, 0,
            0, 0, 1, 0, 0, 7, 5, 0, 2};
    final static int[] LEVEL5 = {
            0, 0, 0, 6, 0, 5, 2, 0, 0,
            3, 0, 0, 0, 0, 0, 6, 8, 0,
            0, 0, 5, 0, 0, 0, 0, 0, 0,
            0, 0, 2, 0, 0, 0, 8, 0, 0,
            5, 0, 8, 0, 4, 9, 0, 0, 0,
            0, 3, 0, 0, 1, 0, 0, 0, 0,
            0, 2, 0, 0, 9, 0, 0, 0, 4,
            0, 0, 0, 0, 0, 4, 0, 0, 0,
            0, 0, 1, 0, 0, 7, 5, 0, 2};
    final static int[] LEVEL6 = {
            0, 0, 0, 6, 0, 5, 2, 0, 0,
            3, 0, 0, 0, 0, 0, 6, 8, 0,
            0, 0, 5, 0, 0, 0, 0, 0, 0,
            0, 0, 2, 0, 0, 0, 8, 0, 0,
            5, 0, 8, 0, 4, 9, 0, 0, 0,
            0, 3, 0, 0, 1, 0, 0, 0, 0,
            0, 2, 0, 0, 9, 0, 0, 0, 4,
            0, 0, 0, 0, 0, 4, 0, 0, 0,
            0, 0, 1, 0, 0, 7, 5, 0, 2};

    static Integer[] getVerticalGroup(final int arrId) {
        return VERTICAL_GROUPS[ID_VERTICAL_GROUPS[arrId]];
    }

    static Integer[] getHorizontalGroup(final int arrId) {
        return HORIZONTAL_GROUPS[ID_HORIZONTAL_GROUPS[arrId]];
    }

    static Integer[] getGroupedGroup(final int arrId) {
        return GROUPED_GROUPS[ID_GROUPED_GROUPS[arrId]];
    }

    static Set<Integer> getStarGroup(final int arrId) {
        Set<Integer> starGroup = new HashSet<>(Arrays.asList(VERTICAL_GROUPS[ID_VERTICAL_GROUPS[arrId]]));
        starGroup.addAll(Arrays.asList(HORIZONTAL_GROUPS[ID_HORIZONTAL_GROUPS[arrId]]));
        starGroup.addAll(Arrays.asList(GROUPED_GROUPS[ID_GROUPED_GROUPS[arrId]]));
        return starGroup;
    }

    static Integer[] getComplementVerticalGroup(final int arrId) {
        Integer[] complementGroup = new Integer[6];
        int colId = arrId / DIM;
        int rowId = arrId % DIM;
        switch (rowId / 3) {// identifier row group
            case 0:
                for (int i = 0; i < 3; i++) {
                    complementGroup[i] = (colId * DIM + 3 + i);
                    complementGroup[i + 3] = (colId * DIM + 6 + i);
                }
                break;
            case 1:
                for (int i = 0; i < 3; i++) {
                    complementGroup[i] = (colId * DIM + i);
                    complementGroup[i + 3] = (colId * DIM + 6 + i);
                }
                break;
            case 2:
                for (int i = 0; i < 3; i++) {
                    complementGroup[i] = (colId * DIM + i);
                    complementGroup[i + 3] = (colId * DIM + 3 + i);
                }
                break;
        }
        return complementGroup;
    }

    static Integer[] getComplementHorizontalGroup(final int arrId) {
        Integer[] complementGroup = new Integer[6];
        int colId = arrId / DIM;
        int rowId = arrId % DIM;
        switch (colId / 3) {// identifier col group
            case 0:
                for (int i = 0; i < 3; i++) {
                    complementGroup[i] = (rowId + DIM * (3 + i));
                    complementGroup[i + 3] = (rowId + DIM * (6 + i));
                }
                break;
            case 1:
                for (int i = 0; i < 3; i++) {
                    complementGroup[i] = (rowId + DIM * i);
                    complementGroup[i + 3] = (rowId + DIM * (6 + i));
                }
                break;
            case 2:
                for (int i = 0; i < 3; i++) {
                    complementGroup[i] = (rowId + DIM * i);
                    complementGroup[i + 3] = (rowId + DIM * (3 + i));
                }
                break;
        }
        return complementGroup;
    }
}
