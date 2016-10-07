package net.koudela.sudoku;

import org.junit.Test;

import java.util.Random;

import static net.koudela.sudoku.SudokuGroups.ALL_ARR_IDS;
import static net.koudela.sudoku.SudokuGroups.DIM;
import static org.junit.Assert.*;

/**
 * Unit Test for class Hint
 */
public class HintUnitTest {
    private Hint hint = new Hint();

    @Test
    public void constructor_isCorrect() throws Exception {
        hint = new Hint();
        for (int arrId : ALL_ARR_IDS) {
            for (int num = 0; num < DIM; num++) {
                assertEquals(0, hint.get(arrId, num));
            }
        }

        Random rand = new Random();
        for (int arrId : ALL_ARR_IDS) {
            for (int num = 0; num < DIM; num++) {
                hint.set(arrId, num, rand.nextInt(DIM));
            }
        }

        Hint hintCopy = new Hint(hint);
        for (int arrId : ALL_ARR_IDS) {
            for (int num = 0; num < DIM; num++) {
                assertEquals(hintCopy.get(arrId, num), hint.get(arrId, num));
            }
        }
    }

    @Test
    public void init_isCorrect() throws Exception {
        hint.init();
        for (int arrId : ALL_ARR_IDS) {
            for (int num = 0; num < DIM; num++) {
                assertEquals(0, hint.get(arrId, num));
            }
        }
    }

    @Test
    public void set_isCorrect() throws Exception {
        Random rand = new Random();
        int val;
        for (int arrId : ALL_ARR_IDS) {
            for (int num = 0; num < DIM; num++) {
                val = rand.nextInt(DIM);
                hint.set(arrId, num, val);
                assertEquals(val, hint.get(arrId, num));
            }
        }
    }

    @Test
    public void increment_isCorrect() throws Exception {
        int val;
        for (int arrId : ALL_ARR_IDS) {
            for (int num = 0; num < DIM; num++) {
                val = hint.get(arrId, num);
                hint.increment(arrId, num);
                assertEquals(val+1, hint.get(arrId, num));
            }
        }
    }

    @Test
    public void decrement_isCorrect() throws Exception {
        int val;
        for (int arrId : ALL_ARR_IDS) {
            for (int num = 0; num < DIM; num++) {
                val = hint.get(arrId, num);
                hint.decrement(arrId, num);
                assertEquals(val-1, hint.get(arrId, num));
            }
        }
    }

    @Test
    public void isHint_isCorrect() throws Exception {
        Random rand = new Random();
        int val;
        for (int arrId : ALL_ARR_IDS) {
            for (int num = 0; num < DIM; num++) {
                val = rand.nextInt(DIM);
                hint.set(arrId, num, val);
                assertEquals(val != 0, hint.isHint(arrId, num));
            }
        }
    }
}