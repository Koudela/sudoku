package net.koudela.sudoku;

public class Sudoku extends SudokuExptimeFunctions {
    private static final Sudoku Singleton = new Sudoku();
    public int[] solution = new int[DIM * DIM];
    public int[] level1Sudoku = new int[DIM * DIM];
    public int[] level2Sudoku = new int[DIM * DIM];
    public int[] level3Sudoku = new int[DIM * DIM];
    protected int level = 1;

    protected Sudoku() {
        init();
    }

    public static Sudoku getInstance() {
        return Singleton;
    }

    public Sudoku init() {
        // TODO: Construct (fast computed) arbitrary starting grid
        solution = TRUE_GRID;//makeTrueGridByBruteForceBacktracking();
        level1Sudoku = makeLevelOneSudoku(solution);
        level2Sudoku = makeLevelTwoSudoku(level1Sudoku);
        level3Sudoku = makeLevelThreeSudoku(level2Sudoku);
        return Singleton;
    }

    public String sudokuToString() {
        switch (level) {
            case 0: return sudokuToString(solution);
            case 1: return sudokuToString(level1Sudoku);
            case 2: return sudokuToString(level2Sudoku);
            case 3: return sudokuToString(level3Sudoku);
            default: throw new IndexOutOfBoundsException();
        }
    }

    public Sudoku setLevel(String level) {
        switch (level) {
            case "0": this.level = 0; break;
            case "easy":
            case "1": this.level = 1; break;
            case "medium":
            case "2": this.level = 2; break;
            case "hard":
            case "3": this.level = 3; break;
            default: throw new IndexOutOfBoundsException(level);
        }
        return Singleton;
    }

    public int getSudokuField(int arrId) {
        switch (level) {
            case 0: return solution[arrId];
            case 1: return level1Sudoku[arrId];
            case 2: return level2Sudoku[arrId];
            case 3: return level3Sudoku[arrId];
            default: throw new IndexOutOfBoundsException(String.valueOf(level));
        }
    }

    public int[] getSudoku() {
        int[] sudoku = new int[DIM * DIM];
        for (int arrId : ALL_ARR_IDS) sudoku[arrId] = getSudokuField(arrId);
        return sudoku;
    }
}
