package net.koudela.sudoku;

import android.content.Context;
import android.util.Log;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.List;

public class SudokuBuilder extends Thread {
    private static final SudokuBuilder Singleton = new SudokuBuilder();
    private List<Playground[]> sudokuStack = new ArrayList<>();
    private int opened = 0;
    private int closed = 0;

    private SudokuBuilder() {
        readSudokuStackFromFile();
        // seeding, we always wanna have something useful in stack
        if (sudokuStack.size() == 0) {
            sudokuStack.add(new Playground[]{
                    new Playground(Sudoku.SOLUTION),
                    new Playground(Sudoku.LEVEL1),
                    new Playground(Sudoku.LEVEL2),
                    new Playground(Sudoku.LEVEL3),
                    new Playground(Sudoku.LEVEL4),
                    new Playground(Sudoku.LEVEL5),
            });
            init(sudokuStack.get(0));
        }
    }

    public static void init(Playground[] sudokuLevels) {
        List<List<Integer>> shuffleRelations = Playground.getShuffleRelations();
        for (Playground level : sudokuLevels)
            level.shuffle(shuffleRelations);
    }

    public static SudokuBuilder getInstance() {
        return Singleton;
    }

    public int getCntOpened() {
        return opened;
    }

    public int getCntClosed() {
        return closed;
    }

    public int getStackSize() {
        return sudokuStack.size();
    }

    public Playground[] getResult(final int pos) {
        return sudokuStack.get(pos);
    }

    private void writeSudokuStackToFile() {
        String filename = "SudokuBuilder.SudokuStack.sav";
        FileOutputStream fos;
        ArrayList<ArrayList<int[]>> serializableList = new ArrayList<>();
        int count = -1;
        for (Playground[] pFamily : sudokuStack) {
            serializableList.add(++count, new ArrayList<int[]>());
            for (Playground playground : pFamily)
                if (playground != null)
                    serializableList.get(count)
                          .add(playground
                                .getPField());
        }
        try {
            Context context = MainActivity.getContext();
            fos = context.openFileOutput(filename, Context.MODE_PRIVATE);
            ObjectOutputStream oos = new ObjectOutputStream(fos);
            oos.writeObject(serializableList);
            oos.close();
            fos.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @SuppressWarnings("unchecked")
    private void readSudokuStackFromFile() {
        String filename = "SudokuBuilder.SudokuStack.sav";
        FileInputStream fis;
        try {
            Context context = MainActivity.getContext();
            fis = context.openFileInput(filename);
            ObjectInputStream ois = new ObjectInputStream(fis);
            ArrayList<ArrayList<int[]>> serializableList = (ArrayList<ArrayList<int[]>>) ois.readObject();
            ois.close();
            fis.close();
            int count = -1;
            for (ArrayList<int[]> pFamily : serializableList) {
                sudokuStack.add(++count, new Playground[6]);
                int i = 0;
                for (int[] playground : pFamily) sudokuStack.get(count)[i++] = new Playground(playground);
                if (sudokuStack.get(count)[5] == null) sudokuStack.remove(count--);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        // we try to have true  opened and closed
        boolean verbose = (MainActivity.talkativenessToLog >= 4);
        while (sudokuStack.size() < 10000) {
            synchronized (this) {
                opened++;
            }
            Playground[] workbench = new Playground[6];
            //workbench[0] = new Playground(Sudoku.TRUE_GRID);
            //workbench[0].shuffle();
            workbench[0] = Sudoku.makeRandomTrueGridByBruteForceBacktracking();
            workbench[1] = Sudoku.makeLevelOneSudoku(workbench[0], verbose);
            workbench[2] = Sudoku.makeLevelTwoSudoku(workbench[1], verbose);
            workbench[3] = Sudoku.makeLevelThreeSudoku(workbench[2], verbose);
            workbench[4] = Sudoku.makeLevelFourSudoku(workbench[3], verbose);
            workbench[5] = Sudoku.makeMinimalSudokuByBruteForceBacktrackingOutOfTrueGrid(workbench[4].getPField(), verbose);
            if (verbose) Log.i("level5Sudoku", workbench[5].toString());
            synchronized (this) {
                sudokuStack.add(workbench);
                writeSudokuStackToFile();
                closed++;
            }
        }
    }
}