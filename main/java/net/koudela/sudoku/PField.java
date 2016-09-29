package net.koudela.sudoku;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static net.koudela.sudoku.SudokuGroups.DIM;

/**
 * transforms and memorizes a sudoku field
 *
 * @author Thomas Koudela
 * @version 1.0 stable
 */
@SuppressWarnings({"WeakerAccess", "unused"})
class PField {
    private int[] pField = new int[DIM * DIM];

    PField() {
        for (int arrId : Sudoku.ALL_ARR_IDS) pField[arrId] = 0;
    }

    PField(final int[] cloneField) {
        // It seems java supplies no method to enforce using the classes own method, if it is called
        // from a subclass witch hides the method. (Unfortunately the set method of the subclass is
        // used during construction, although that makes no sense at all, even explicit casting does
        // not work.) Renaming and making final seems a stupid way of programming.
        for (int arrId : Sudoku.ALL_ARR_IDS) setFinal(arrId, cloneField[arrId]);
    }

    int get(final int arrId) {
        return pField[arrId];
    }

    final void setFinal(final int arrId, final int val) {
        if (val < 0 || val > DIM)
            throw new IllegalArgumentException("val must be within [0, " + DIM + "]; val was " + val);
        pField[arrId] = val;
    }

    int[] getPField() {
        return pField.clone();
    }

    boolean isPopulated(final int arrId) {
        return pField[arrId] != 0;
    }

    /**
     * Fast way to generate a solution out of another solution.
     * Works for sudokus alike.
     * Number of different ways: 4 * 9! * 3!^8
     * = 4 * 362,880 * 6^8
     * = 1,451,520 * 1,679,616
     * = 2,437,996,216,320 (2.44 * 10^12)
     * The rotation of 180 degree can be simulated be flipping groups and rows vertical and
     * horizontal in the right way. Turning left 90 degree equals turning right 90 degrees and
     * turning 180 degrees. So we must divide the above by 2 to get the distinct solutions.
     * It is known that 6,670,903,752,021,072,936,960 (6.67×10^21) distinct solutions exists.
     * @param relations a list of lists that relate in a deterministic way to the transformation
     */
    void shuffle(List<List<Integer>> relations) {
        if (relations == null) relations = getShuffleRelations();
        int relNr = 0;
        switch (relations.get(relNr++).get(0)) {
            case 1: pField = rotate90Degree(pField, true); break;
            case 2: pField = rotate180Degree(pField); break;
            case 3: pField = rotate90Degree(pField, false); break;
            default: break;
        }
        renameEntries(pField, relations.get(relNr++));
        pField = flipHorizontalGroupsVertically(pField, relations.get(relNr++));
        pField = flipVerticalGroupsHorizontally(pField, relations.get(relNr++));
        pField = flipInHorizontalGroupsVertically(pField, 0, relations.get(relNr++));
        pField = flipInHorizontalGroupsVertically(pField, 1, relations.get(relNr++));
        pField = flipInHorizontalGroupsVertically(pField, 2, relations.get(relNr++));
        pField = flipInVerticalGroupsHorizontally(pField, 0, relations.get(relNr++));
        pField = flipInVerticalGroupsHorizontally(pField, 1, relations.get(relNr++));
        pField = flipInVerticalGroupsHorizontally(pField, 2, relations.get(relNr));
    }

    /**
     * @see #shuffle
     * @return relations needed as input for the shuffle transformation
     */
    static List<List<Integer>> getShuffleRelations() {
        List<List<Integer>> relations = new ArrayList<>();
        relations.add(new ArrayList<>(Arrays.asList(0, 1, 2, 3)));
        Collections.shuffle(relations.get(0));
        relations.add(new ArrayList<>(Arrays.asList(1, 2, 3, 4, 5, 6, 7, 8, 9)));
        Collections.shuffle(relations.get(1));
        for (int cnt = 0; cnt < 8; cnt++) {
            relations.add(new ArrayList<>(Arrays.asList(0, 1, 2)));
            Collections.shuffle(relations.get(cnt + 2));
        }
        return relations;
    }

    public static String relationToString(final List<Integer> list) {
        String relationString = "[";
        for (int i = 0; i < list.size(); i++)
            relationString += i + " => " + list.get(i) + ", ";
        return relationString + "]";
    }

    private static int[] rotate90Degree(final int[] pField, final boolean left) {
        int[] newField = new int[DIM * DIM];
        int oldArrId;
        int newArrId;
        for (int i = 0; i < DIM; i++) {
            for (int j = 0; j < DIM; j++) {
                if (!left) {
                    oldArrId = (DIM - (j+1)) * DIM + i;
                    newArrId = i * DIM + j;
                } else {
                    oldArrId = i * DIM + j;
                    newArrId = (DIM - (j+1)) * DIM + i;
                }
                newField[newArrId] = pField[oldArrId];
            }
        }
        return newField;
    }

    private static int[] rotate180Degree(final int[] pField) {
        int[] newField = new int[DIM * DIM];
        for (int newArrId : Sudoku.ALL_ARR_IDS)
            newField[newArrId] = pField[(DIM * DIM - 1) - newArrId];
        return newField;
    }

    private static void renameEntries(int[] pField, final List<Integer> renameRelation) {
        for (int arrId : Sudoku.ALL_ARR_IDS) {
            if (pField[arrId] != 0) pField[arrId] = renameRelation.get(pField[arrId] - 1);
            else pField[arrId] = 0;
        }
    }

    private static int[] flipVerticalGroupsHorizontally(final int[] pField,
                                                        final List<Integer> flipRelation) {
        int oldArrId;
        int newArrId;
        int[] newField = new int[DIM * DIM];
        for (int cnt = 0; cnt < 3; cnt++)
            for (int i = 0; i < DIM; i++)
                for (int j = 0; j < 3; j++) {
                    oldArrId = i * DIM + (cnt * 3 + j);
                    newArrId = i * DIM + (flipRelation.get(cnt) * 3 + j);
                    newField[newArrId] = pField[oldArrId];
                }
        return newField;
    }

    private static int[] flipHorizontalGroupsVertically(final int[] pField,
                                                        final List<Integer> flipRelation) {
        int oldArrId;
        int newArrId;
        int[] newField = new int[DIM * DIM];
        for (int cnt = 0; cnt < 3; cnt++)
            for (int i = 0; i < DIM; i++)
                for (int j = 0; j < 3; j++) {
                    oldArrId = (cnt * 3 + j) * DIM + i;
                    newArrId = (flipRelation.get(cnt) * 3 + j) * DIM + i;
                    newField[newArrId] = pField[oldArrId];
                }
        return newField;
    }

    private static int[] flipInVerticalGroupsHorizontally(final int[] pField, final int groupId,
                                                          final List<Integer> flipRelation) {
        int oldArrId;
        int newArrId;
        int[] newField = new int[DIM * DIM];
        for (int cnt = 0; cnt < 3; cnt++)
            for (int i = 0; i < DIM; i++)
                for (int j = 0; j < 3; j++)
                    if (j == groupId) {
                        oldArrId = i * DIM + (cnt + groupId * 3);
                        newArrId = i * DIM + (flipRelation.get(cnt) + groupId * 3);
                        newField[newArrId] = pField[oldArrId];
                    } else {
                        newArrId = i * DIM + (cnt + j * 3);
                        newField[newArrId] = pField[newArrId];
                    }
        return newField;
    }

    private static int[] flipInHorizontalGroupsVertically(final int[] pField, final int groupId,
                                                          final List<Integer> flipRelation) {
        int oldArrId;
        int newArrId;
        int[] newField = new int[DIM * DIM];
        for (int cnt = 0; cnt < 3; cnt++)
            for (int i = 0; i < DIM; i++)
                for (int j = 0; j < 3; j++)
                    if (j == groupId) {
                        oldArrId = (cnt + groupId * 3) * DIM + i;
                        newArrId = (flipRelation.get(cnt) + groupId * 3) * DIM + i;
                        newField[newArrId] = pField[oldArrId];
                    } else {
                        newArrId = (cnt + j * 3) * DIM + i;
                        newField[newArrId] = pField[newArrId];
                    }
        return newField;
    }

    @Override
    public String toString() {
        String pFieldString = "";
        for (int i = 0; i < DIM; i++) {
            pFieldString += "\n";
            for (int j = 0; j < DIM; j++) pFieldString += pField[i * DIM + j] + ", ";
        }
        return pFieldString;
    }
}
