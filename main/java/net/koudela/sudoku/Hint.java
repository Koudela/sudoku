package net.koudela.sudoku;

public class Hint {
    protected final static int DIM = Sudoku.DIM;
    protected int[][] hint = new int[DIM * DIM][DIM];

    public Hint() {
        init();
    }

    public void init() {
        for (int arrId : Sudoku.ALL_ARR_IDS) for (int num = 0; num < DIM; num++) hint[arrId][num] = 0;
    }

    public boolean isHint(int arrId, int num) {
        return hint[arrId][num] != 0;
    }

    public int get(int arrId, int num) {
        return hint[arrId][num];
    }

    public void set(int arrId, int num, int val) {
        hint[arrId][num] = val;
    }

    public void increment(int arrId, int num) {
        hint[arrId][num]++;
    }

    public void decrement(int arrId, int num) {
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