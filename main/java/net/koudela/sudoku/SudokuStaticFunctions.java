package net.koudela.sudoku;

import android.util.Log;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class SudokuStaticFunctions extends SudokuGroups {

    public static List<Integer> getRandomizedArrIds() {
        List<Integer> arrIds = new ArrayList<>();
        for (int arrId = 0; arrId < DIM * DIM; arrId++) arrIds.add(arrId);
        Collections.shuffle(arrIds);
        return arrIds;
    }

    public static Playground solveSudoku(int deletionAtArrId, Playground sudoku, boolean byAutoInsert1, boolean byAutoInsert2, boolean byAutoHintAdv1, boolean byAutoHintAdv2, boolean byAutoHintAdv3) {
        Playground solution = new Playground(sudoku);
        if (deletionAtArrId > -1) solution.set(deletionAtArrId, 0);
        Hints hints = new Hints(true, byAutoHintAdv1, byAutoHintAdv2, byAutoHintAdv3);
        hints.populatePlainHints(solution);
        int newHintsFound;
        boolean changedSolution;
        int changedHints = 0;
        do {
            changedSolution = false;
            if (byAutoInsert1)
                for (Map.Entry<Integer, Integer> arrId : solution.getNotPopulatedArrIds().entrySet()) {
                    int number = solution.getAutoInsert1ByField(arrId.getValue(), hints);
                    if (number > 0) {
                        solution.set(arrId.getValue(), number);
                        hints.incrementStarGroup(arrId.getValue(), number - 1);
                        Log.v("solved by AI1", arrId.getValue() + " (" + number + ")");
                        changedSolution = true;
                    }
                }
            if (!changedSolution && byAutoInsert2) {
                Integer[] ai2arr = solution.getAutoInsert2(hints);
                if (ai2arr != null) {
                    solution.set(ai2arr[1], ai2arr[0]);
                    hints.incrementStarGroup(ai2arr[1], ai2arr[0] - 1);
                    Log.v("solved by AI2", ai2arr[1] + " (" + ai2arr[0] + ")");
                    changedSolution = true;
                }
            }
            if (!changedSolution) {
                //Log.v("search new hints", "...");
                if (byAutoHintAdv1) {
                    newHintsFound = hints.setAutoHintsAdv1(solution);
                    if (newHintsFound != 0) Log.v("hintsAdv1", ""+newHintsFound);
                    changedHints = newHintsFound;
                }
                if (byAutoHintAdv2) {
                    newHintsFound = hints.setAutoHintsAdv2(solution);
                    changedHints += newHintsFound;
                    if (newHintsFound != 0) Log.v("hintsAdv2", ""+newHintsFound);
                }
                if (byAutoHintAdv3) {
                    newHintsFound = hints.setAutoHintsAdv3(solution);
                    changedHints += newHintsFound;
                    if (newHintsFound != 0) Log.v("hintsAdv3", ""+newHintsFound);
                }
            }
        } while(changedSolution || changedHints > 0);
        return solution;
    }

    public static boolean isSolvableSudoku(int arrId, Playground sudoku, boolean byAutoInsert1, boolean byAutoInsert2, boolean byAutoHintAdv1, boolean byAutoHintAdv2, boolean byAutoHintAdv3) {
        return isTrueGrid(solveSudoku(arrId, sudoku, byAutoInsert1, byAutoInsert2, byAutoHintAdv1, byAutoHintAdv2, byAutoHintAdv3));
    }

    public static boolean isTrueGrid(Playground pField) {
        if (pField.getNotPopulatedArrIds().size() != 0) return false;
        Hint hint = new Hint();
        Hints.populatePlainHints(hint, pField);
        for (int arrId : ALL_ARR_IDS) if (hint.get(arrId, pField.get(arrId) - 1) != 1) return false;
        return true;
    }
}
