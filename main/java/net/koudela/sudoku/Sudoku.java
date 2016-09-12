package net.koudela.sudoku;

public class Sudoku extends SudokuExptimeFunctions {
    public int[] solution = makeTrueGridByBruteForceBacktracking();
    public int[] level1Sudoku = new int[DIM * DIM];
    public int[] level2Sudoku = new int[DIM * DIM];
    public int[] level3Sudoku = new int[DIM * DIM];

    public Sudoku() {
        // TODO: Construct (fast computed) arbitrary starting grid
        //for (int arrId : ALL_ARR_IDS) solution[arrId] =
        level1Sudoku = makeLevelOneSudoku(solution);
        level2Sudoku = makeLevelTwoSudoku(level1Sudoku);
        level3Sudoku = makeLevelThreeSudoku(level2Sudoku);
    }

    public String sudokuToString(int level) {
        switch (level) {
            case 0: return sudokuToString(solution);
            case 1: return sudokuToString(level1Sudoku);
            case 2: return sudokuToString(level2Sudoku);
            case 3: return sudokuToString(level3Sudoku);
        }
        return null;
    }
}
