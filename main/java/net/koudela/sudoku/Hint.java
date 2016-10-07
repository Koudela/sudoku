package net.koudela.sudoku;

import static net.koudela.sudoku.SudokuGroups.DIM;

/**
 *  base class for sudoku hints
 *
 * @author Thomas Koudela
 * @version 1.0 tested, stable
 */
@SuppressWarnings({"WeakerAccess", "unused"})
class Hint {
    private int[][] hint = new int[DIM * DIM][DIM];

    Hint() {
        pInit();
    }

    void init() {
        pInit();
    }

    private void pInit() {
        for (int arrId : Sudoku.ALL_ARR_IDS)
            for (int num = 0; num < DIM; num++)
                hint[arrId][num] = 0;
    }

    Hint(Hint hint) {
        for (int arrId : Sudoku.ALL_ARR_IDS)
            for (int num = 0; num < DIM; num++)
                this.hint[arrId][num] = hint.get(arrId, num);
    }

    int get(final int arrId, final int num) {
        return hint[arrId][num];
    }

    void set(final int arrId, final int num, final int val) {
        hint[arrId][num] = val;
    }

    void increment(final int arrId, final int num) {
        hint[arrId][num]++;
    }

    void decrement(final int arrId, final int num) {
        hint[arrId][num]--;
    }

    boolean isHint(final int arrId, final int num) {
        return hint[arrId][num] != 0;
    }

    @Override
    public String toString() {
        String hintsArr = "[\n";
        for (int arrId : Sudoku.ALL_ARR_IDS) {
            hintsArr += "[";
            for  (int num = 0; num < DIM; num++) hintsArr += hint[arrId][num] + ", ";
            hintsArr += "],\n";
        }
        return hintsArr + "]";
    }
}