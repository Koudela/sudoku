package net.koudela.sudoku;

import org.junit.Test;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import static java.util.Arrays.asList;
import static net.koudela.sudoku.SudokuGroups.DIM;
import static org.junit.Assert.*;

import static net.koudela.sudoku.SudokuGroups.ALL_ARR_IDS;

/**
 * Unit Test for class {@link PField}.
 */
public class PFieldUnitTest {
    private PField pField = new PField();
    private final int[] testField =  {
            9, 8, 7, 6, 3, 5, 2, 4, 1,
            3, 1, 4, 9, 7, 2, 6, 8, 5,
            2, 6, 5, 4, 8, 1, 3, 7, 9,
            1, 4, 2, 7, 5, 6, 8, 9, 3,
            5, 7, 8, 3, 4, 9, 1, 2, 6,
            6, 3, 9, 2, 1, 8, 4, 5, 7,
            8, 2, 6, 5, 9, 3, 7, 1, 4,
            7, 5, 3, 1, 2, 4, 9, 6, 8,
            4, 9, 1, 8, 6, 7, 5, 3, 2};
    private final List<List<Integer>> shuffleRelation = new ArrayList<List<Integer>>(){{
        add(new ArrayList<>(Arrays.asList(3, 1, 2, 0)));
        add(new ArrayList<>(Arrays.asList(3, 5, 7, 2, 1, 4, 9, 8, 6)));
        add(new ArrayList<>(Arrays.asList(1, 2, 0)));
        add(new ArrayList<>(Arrays.asList(2, 1, 0)));
        add(new ArrayList<>(Arrays.asList(0, 1, 2)));
        add(new ArrayList<>(Arrays.asList(0, 2, 1)));
        add(new ArrayList<>(Arrays.asList(1, 0, 2)));
        add(new ArrayList<>(Arrays.asList(0, 1, 2)));
        add(new ArrayList<>(Arrays.asList(2, 0, 1)));
        add(new ArrayList<>(Arrays.asList(0, 1, 2)));
    }};

    @Test
    public void constructor_isCorrect() {
        pField = new PField();
        for (int arrId : ALL_ARR_IDS) {
            assertEquals(0, pField.get(arrId));
        }

        Random rand = new Random();
        for (int arrId : ALL_ARR_IDS) {
            pField.set(arrId, rand.nextInt(DIM + 1));
        }

        PField pFieldClone = new PField(pField.getPField());
        for (int arrId : ALL_ARR_IDS) {
            assertEquals(pField.get(arrId), pFieldClone.get(arrId));
        }
    }

    @Test
    public void get_and_set_areCorrect() {
        int val;
        Random rand = new Random();
        for (int arrId : ALL_ARR_IDS) {
            val = rand.nextInt(DIM + 1);
            pField.set(arrId, val);
            assertEquals(val, pField.get(arrId));
        }
    }

    @Test
    public void isPopulated_isCorrect() {
        int val;
        Random rand = new Random();
        for (int arrId : ALL_ARR_IDS) {
            val = rand.nextInt(DIM + 1);
            pField.set(arrId, val);
            assertEquals(val != 0, pField.isPopulated(arrId));
        }
    }

    @Test
    public void getPField_isCorrect() {
        int[] testField = new int[DIM * DIM];
        Random rand = new Random();
        for (int arrId : ALL_ARR_IDS) {
            testField[arrId] = rand.nextInt(DIM + 1);
        }
        pField = new PField(testField);
        int[] actualField = pField.getPField();
        for (int arrId : ALL_ARR_IDS) {
            assertEquals(testField[arrId], actualField[arrId]);
        }
    }

    @Test
    public void shuffle_isCorrect() {
        int[] shuffledField = {
                7, 4, 5, 3, 8, 2, 1, 6, 9,
                9, 8, 2, 5, 6, 1, 7, 4, 3,
                6, 1, 3, 4, 7, 9, 5, 8, 2,
                5, 7, 6, 1, 3, 4, 2, 9, 8,
                1, 2, 9, 8, 5, 6, 3, 7, 4,
                4, 3, 8, 9, 2, 7, 6, 1, 5,
                8, 9, 7, 2, 1, 3, 4, 5, 6,
                2, 6, 4, 7, 9, 5, 8, 3, 1,
                3, 5, 1, 6, 4, 8, 9, 2, 7};
        pField = new PField(testField);
        pField.shuffle(shuffleRelation);
        assertArrayEquals(shuffledField, pField.getPField());
    }

    @Test
    public void getShuffleRelations_isCorrect() {
        List<List<Integer>> shuffleRelation = PField.getShuffleRelations();
        assertEquals(this.shuffleRelation.size(), shuffleRelation.size());
        for (int relNr = 0; relNr < 10; relNr++) {
            assertEquals(this.shuffleRelation.get(0).size(), shuffleRelation.get(0).size());
            assertEquals(true, this.shuffleRelation.get(0).containsAll(shuffleRelation.get(0)));
            assertEquals(true, shuffleRelation.get(0).containsAll(this.shuffleRelation.get(0)));
        }
    }

    @Test
    public void rotate90Degree_isCorrect() throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        int[] rotateLeft = {
                1, 5, 9, 3, 6, 7, 4, 8, 2,
                4, 8, 7, 9, 2, 5, 1, 6, 3,
                2, 6, 3, 8, 1, 4, 7, 9, 5,
                5, 2, 1, 6, 9, 8, 3, 4, 7,
                3, 7, 8, 5, 4, 1, 9, 2, 6,
                6, 9, 4, 7, 3, 2, 5, 1, 8,
                7, 4, 5, 2, 8, 9, 6, 3, 1,
                8, 1, 6, 4, 7, 3, 2, 5, 9,
                9, 3, 2, 1, 5, 6, 8, 7, 4};
        Method m = PField.class.getDeclaredMethod("rotate90Degree", int[].class, boolean.class);
        m.setAccessible(true);
        int[] rotatedField = (int[]) m.invoke(null, testField, true);
        for (int arrId : ALL_ARR_IDS) {
            assertEquals(rotateLeft[arrId], rotatedField[arrId]);
        }
        int[] rotateRight = (int[]) m.invoke(null, m.invoke(null, rotatedField, true), true);
        rotatedField = (int[]) m.invoke(null, testField, false);
        for (int arrId : ALL_ARR_IDS) {
            assertEquals(rotateRight[arrId], rotatedField[arrId]);
        }
    }

    @Test
    public void rotate180Degree_isCorrect() throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        Method m = PField.class.getDeclaredMethod("rotate90Degree", int[].class, boolean.class);
        m.setAccessible(true);
        int[] rotate180 = (int[]) m.invoke(null, m.invoke(null, testField, true), true);
        m = PField.class.getDeclaredMethod("rotate180Degree", int[].class);
        m.setAccessible(true);
        int[] rotatedField = (int[]) m.invoke(null, testField);
        for (int arrId : ALL_ARR_IDS) {
            assertEquals(rotate180[arrId], rotatedField[arrId]);
        }
    }

    @Test
    public void renameEntries_isCorrect() throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        Method m = PField.class.getDeclaredMethod("renameEntries", int[].class, List.class);
        m.setAccessible(true);
        List<Integer> renameRelation = new ArrayList<>();
        for (int i = 1; i <= DIM; i++) renameRelation.add(i);
        int[] originalField = new int[DIM * DIM];
        int[] testField;
        Random rand = new Random();
        for (int arrId : ALL_ARR_IDS) {
            originalField[arrId] = rand.nextInt(DIM + 1);
        }
        Collections.shuffle(renameRelation);
        testField = originalField.clone();
        m.invoke(null, testField, renameRelation);
        for (int arrId : ALL_ARR_IDS) {
            assertEquals(originalField[arrId] == 0 ? 0 : renameRelation.get(originalField[arrId] - 1), testField[arrId]);
        }
    }

    @Test
    public void flipVerticalGroupsHorizontally_isCorrect() throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        final int[] flippedField = {
                6, 3, 5, 2, 4, 1, 9, 8, 7,
                9, 7, 2, 6, 8, 5, 3, 1, 4,
                4, 8, 1, 3, 7, 9, 2, 6, 5,
                7, 5, 6, 8, 9, 3, 1, 4, 2,
                3, 4, 9, 1, 2, 6, 5, 7, 8,
                2, 1, 8, 4, 5, 7, 6, 3, 9,
                5, 9, 3, 7, 1, 4, 8, 2, 6,
                1, 2, 4, 9, 6, 8, 7, 5, 3,
                8, 6, 7, 5, 3, 2, 4, 9, 1};
        Method m = PField.class.getDeclaredMethod("flipVerticalGroupsHorizontally", int[].class, List.class);
        m.setAccessible(true);
        List<Integer> flipRelation = new ArrayList<>(asList(2, 0, 1));
        assertArrayEquals(flippedField, (int[]) m.invoke(null, testField, flipRelation));
    }

    @Test
    public void flipHorizontalGroupsVertically_isCorrect() throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        final int[] flippedField = {
                1, 4, 2, 7, 5, 6, 8, 9, 3,
                5, 7, 8, 3, 4, 9, 1, 2, 6,
                6, 3, 9, 2, 1, 8, 4, 5, 7,
                8, 2, 6, 5, 9, 3, 7, 1, 4,
                7, 5, 3, 1, 2, 4, 9, 6, 8,
                4, 9, 1, 8, 6, 7, 5, 3, 2,
                9, 8, 7, 6, 3, 5, 2, 4, 1,
                3, 1, 4, 9, 7, 2, 6, 8, 5,
                2, 6, 5, 4, 8, 1, 3, 7, 9};
        Method m = PField.class.getDeclaredMethod("flipHorizontalGroupsVertically", int[].class, List.class);
        m.setAccessible(true);
        List<Integer> flipRelation = new ArrayList<>(asList(2, 0, 1));
        assertArrayEquals(flippedField, (int[]) m.invoke(null, testField, flipRelation));
    }

    @Test
    public void flipInVerticalGroupsHorizontally_isCorrect() throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        final int[] flippedField = {
                9, 8, 7, 3, 5, 6, 2, 4, 1,
                3, 1, 4, 7, 2, 9, 6, 8, 5,
                2, 6, 5, 8, 1, 4, 3, 7, 9,
                1, 4, 2, 5, 6, 7, 8, 9, 3,
                5, 7, 8, 4, 9, 3, 1, 2, 6,
                6, 3, 9, 1, 8, 2, 4, 5, 7,
                8, 2, 6, 9, 3, 5, 7, 1, 4,
                7, 5, 3, 2, 4, 1, 9, 6, 8,
                4, 9, 1, 6, 7, 8, 5, 3, 2};
        Method m = PField.class.getDeclaredMethod("flipInVerticalGroupsHorizontally", int[].class, int.class, List.class);
        m.setAccessible(true);
        List<Integer> flipRelation = new ArrayList<>(asList(2, 0, 1));
        assertArrayEquals(flippedField, (int[]) m.invoke(null, testField, 1, flipRelation));
    }

    @Test
    public void flipInHorizontalGroupsVertically_isCorrect() throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        final int[] flippedField = {
                9, 8, 7, 6, 3, 5, 2, 4, 1,
                3, 1, 4, 9, 7, 2, 6, 8, 5,
                2, 6, 5, 4, 8, 1, 3, 7, 9,
                5, 7, 8, 3, 4, 9, 1, 2, 6,
                6, 3, 9, 2, 1, 8, 4, 5, 7,
                1, 4, 2, 7, 5, 6, 8, 9, 3,
                8, 2, 6, 5, 9, 3, 7, 1, 4,
                7, 5, 3, 1, 2, 4, 9, 6, 8,
                4, 9, 1, 8, 6, 7, 5, 3, 2};
        Method m = PField.class.getDeclaredMethod("flipInHorizontalGroupsVertically", int[].class, int.class, List.class);
        m.setAccessible(true);
        List<Integer> flipRelation = new ArrayList<>(asList(2, 0, 1));
        assertArrayEquals(flippedField, (int[]) m.invoke(null, testField, 1, flipRelation));
    }
}
