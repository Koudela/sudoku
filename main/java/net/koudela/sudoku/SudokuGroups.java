package net.koudela.sudoku;

import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Set;

public class SudokuGroups {
    public final static int DIM = 9;
    public final static Set<Integer> ALL_ARR_IDS = new LinkedHashSet<>(Arrays.asList(
            0 , 1, 2, 3, 4, 5, 6, 7, 8,
            9 ,10,11,12,13,14,15,16,17,
            18,19,20,21,22,23,24,25,26,
            27,28,29,30,31,32,33,34,35,
            36,37,38,39,40,41,42,43,44,
            45,46,47,48,49,50,51,52,53,
            54,55,56,57,58,59,60,61,62,
            63,64,65,66,67,68,69,70,71,
            72,73,74,75,76,77,78,79,80));

    public final static Integer[][] VERTICAL_GROUPS = {
            { 0, 1, 2, 3, 4, 5, 6, 7, 8},
            { 9,10,11,12,13,14,15,16,17},
            {18,19,20,21,22,23,24,25,26},
            {27,28,29,30,31,32,33,34,35},
            {36,37,38,39,40,41,42,43,44},
            {45,46,47,48,49,50,51,52,53},
            {54,55,56,57,58,59,60,61,62},
            {63,64,65,66,67,68,69,70,71},
            {72,73,74,75,76,77,78,79,80}};
    public final static Integer[] ID_VERTICAL_GROUPS = {
              0, 0, 0, 0, 0, 0, 0, 0, 0,
              1, 1, 1, 1, 1, 1, 1, 1, 1,
              2, 2, 2, 2, 2, 2, 2, 2, 2,
              3, 3, 3, 3, 3, 3, 3, 3, 3,
              4, 4, 4, 4, 4, 4, 4, 4, 4,
              5, 5, 5, 5, 5, 5, 5, 5, 5,
              6, 6, 6, 6, 6, 6, 6, 6, 6,
              7, 7, 7, 7, 7, 7, 7, 7, 7,
              8, 8, 8, 8, 8, 8, 8, 8, 8};
    public final static Integer[][] HORIZONTAL_GROUPS = {
            { 0, 9,18,27,36,45,54,63,72},
            { 1,10,19,28,37,46,55,64,73},
            { 2,11,20,29,38,47,56,65,74},
            { 3,12,21,30,39,48,57,66,75},
            { 4,13,22,31,40,49,58,67,76},
            { 5,14,23,32,41,50,59,68,77},
            { 6,15,24,33,42,51,60,69,78},
            { 7,16,25,34,43,52,61,70,79},
            { 8,17,26,35,44,53,62,71,80}};
    public final static Integer[] ID_HORIZONTAL_GROUPS = {
              0, 1, 2, 3, 4, 5, 6, 7, 8,
              0, 1, 2, 3, 4, 5, 6, 7, 8,
              0, 1, 2, 3, 4, 5, 6, 7, 8,
              0, 1, 2, 3, 4, 5, 6, 7, 8,
              0, 1, 2, 3, 4, 5, 6, 7, 8,
              0, 1, 2, 3, 4, 5, 6, 7, 8,
              0, 1, 2, 3, 4, 5, 6, 7, 8,
              0, 1, 2, 3, 4, 5, 6, 7, 8,
              0, 1, 2, 3, 4, 5, 6, 7, 8};
    public final static Integer[][] GROUPED_GROUPS = {
            { 0, 1, 2, 9,10,11,18,19,20},
            {27,28,29,36,37,38,45,46,47},
            {54,55,56,63,64,65,72,73,74},
            { 3, 4, 5,12,13,14,21,22,23},
            {30,31,32,39,40,41,48,49,50},
            {57,58,59,66,67,68,75,76,77},
            { 6, 7, 8,15,16,17,24,25,26},
            {33,34,35,42,43,44,51,52,53},
            {60,61,62,69,70,71,78,79,80}};
    public final static Integer[] ID_GROUPED_GROUPS = {
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

    public static Set<Integer> getVerticalGroup(int arrId) {
        Set<Integer> verticalGroup = new LinkedHashSet<>();
        verticalGroup.addAll(Arrays.asList(VERTICAL_GROUPS[ID_VERTICAL_GROUPS[arrId]]));
        return verticalGroup;
    }

    public static Set<Integer> getHorizontalGroup(int arrId) {
        Set<Integer> horizontalGroup = new LinkedHashSet<>();
        horizontalGroup.addAll(Arrays.asList(HORIZONTAL_GROUPS[ID_HORIZONTAL_GROUPS[arrId]]));
        return horizontalGroup;
    }

    public static Set<Integer> getGroupedGroup(int arrId) {
        Set<Integer> groupedGroup = new LinkedHashSet<>();
        groupedGroup.addAll(Arrays.asList(GROUPED_GROUPS[ID_GROUPED_GROUPS[arrId]]));
        return groupedGroup;
    }

    // in the old version it contained 3 times arrId and 4 other ids were included twice!
    // now its a set, all ids are contained only once!
    public static Set<Integer> getStarGroup(int arrId) {
        Set<Integer> starGroup = new LinkedHashSet<>();
        for (int tempArrId: getVerticalGroup(arrId)) starGroup.add(tempArrId);
        for (int tempArrId: getHorizontalGroup(arrId)) starGroup.add(tempArrId);
        for (int tempArrId: getGroupedGroup(arrId)) starGroup.add(tempArrId);
        return starGroup;
    }

    public static Set<Integer> getComplementVerticalGroup(int arrId) {
        Set<Integer> complementGroup = new LinkedHashSet<>();
        int colId = arrId / DIM;
        int rowId = arrId % DIM;
        switch (rowId / 3) {// identifier row group
            case 0:
                for (int i = 0; i < 3; i++) {
                    complementGroup.add(colId * DIM + 3 + i);
                    complementGroup.add(colId * DIM + 6 + i);
                }
                break;
            case 1:
                for (int i = 0; i < 3; i++) {
                    complementGroup.add(colId * DIM + i);
                    complementGroup.add(colId * DIM + 6 + i);
                }
                break;
            case 2:
                for (int i = 0; i < 3; i++) {
                    complementGroup.add(colId * DIM + i);
                    complementGroup.add(colId * DIM + 3 + i);
                }
                break;
        }
        return complementGroup;
    }

    public static Set<Integer> getComplementHorizontalGroup(int arrId) {
        Set<Integer> complementGroup = new LinkedHashSet<>();
        int colId = arrId / DIM;
        int rowId = arrId % DIM;
        switch (colId / 3) {// identifier col group
            case 0:
                for (int i = 0; i < 3; i++) {
                    complementGroup.add(rowId + DIM * (3 + i));
                    complementGroup.add(rowId + DIM * (6 + i));
                }
                break;
            case 1:
                for (int i = 0; i < 3; i++) {
                    complementGroup.add(rowId + DIM * i);
                    complementGroup.add(rowId + DIM * (6 + i));
                }
                break;
            case 2:
                for (int i = 0; i < 3; i++) {
                    complementGroup.add(rowId + DIM * i);
                    complementGroup.add(rowId + DIM * (3 + i));
                }
                break;
        }
        return complementGroup;
    }
}
