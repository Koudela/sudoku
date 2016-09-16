package net.koudela.sudoku;

public class Hint {
    private final static int DIM = Sudoku.DIM;
    private int[][] hint = new int[DIM * DIM][DIM];

    public Hint() {
        init();
    }

    public void init() {
        for (int arrId : Sudoku.ALL_ARR_IDS)
            for (int num = 0; num < DIM; num++)
                hint[arrId][num] = 0;
    }

    public boolean isHint(final int arrId, final int num) {
        return hint[arrId][num] != 0;
    }

    public int get(final int arrId, final int num) {
        return hint[arrId][num];
    }

    public void set(final int arrId, final int num, final int val) {
        hint[arrId][num] = val;
    }

    public void increment(final int arrId, final int num) {
        hint[arrId][num]++;
    }

    public void decrement(final int arrId, final int num) {
        hint[arrId][num]--;
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