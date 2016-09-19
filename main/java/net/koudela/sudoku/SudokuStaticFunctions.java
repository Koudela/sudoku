package net.koudela.sudoku;

import android.util.Log;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class SudokuStaticFunctions extends SudokuGroups {

    public static List<Integer> getRandomizedArrIds() {
        List<Integer> arrIds = new ArrayList<>(Arrays.asList(ALL_ARR_IDS));
        Collections.shuffle(arrIds);
        return arrIds;
    }

    public static Playground solveSudoku(final int deletionAtArrId, final Playground sudoku, final boolean byAutoInsert1, final boolean byAutoInsert2, final boolean byAutoHintAdv1, final boolean byAutoHintAdv2, final boolean byAutoHintAdv3, final boolean verbose) {
        Playground solution = new Playground(sudoku);
        if (deletionAtArrId > -1) solution.set(deletionAtArrId, 0);
        Hints hints = new Hints(true, byAutoHintAdv1, byAutoHintAdv2, byAutoHintAdv3);
        hints.populatePlainHints(solution);
        int newHintsFound;
        boolean changedSolution;
        int changedHints = 0;
        do {
            changedSolution = false;
            if (byAutoInsert1) for (int arrId : new HashSet<>(solution.getNotPopulatedArrIds()))
                changedSolution = (solution.autoInsert1ByField(arrId, hints, verbose) != 0);
            if (!changedSolution && byAutoInsert2) changedSolution = (solution.autoInsert2(hints, verbose) != null);
            if (solution.getSizeNotPopulatedArrIds() == 0) break;
            if (!changedSolution) {
                if (byAutoHintAdv1) {
                    newHintsFound = hints.setAutoHintsAdv1(solution, false);
                    if (verbose && newHintsFound != 0) Log.v("hintsAdv1", ""+newHintsFound);
                    changedHints = newHintsFound;
                }
                if (byAutoHintAdv2) {
                    newHintsFound = hints.setAutoHintsAdv2(solution, false);
                    changedHints += newHintsFound;
                    if (verbose && newHintsFound != 0) Log.v("hintsAdv2", ""+newHintsFound);
                }
                if (byAutoHintAdv3) {
                    newHintsFound = hints.setAutoHintsAdv3(solution, false);
                    changedHints += newHintsFound;
                    if (verbose && newHintsFound != 0) Log.v("hintsAdv3", ""+newHintsFound);
                }
            }
        } while ((changedSolution || changedHints > 0));
        return solution;
    }

    public static void updateSudokuStart(Set<Integer> arrIdsChangedHints, Set<Integer> arrIdsChangedValues, Playground sudoku, Hints hints, final boolean byAutoInsert1, final boolean byAutoInsert2) {
        HashSetLIFOQueue arrIdsNotTestedHints = new HashSetLIFOQueue(sudoku.getNotPopulatedArrIds());
        int countChanged;
        do {
            countChanged = arrIdsChangedValues.size();
            Set<Integer> changed = hints.updateAdvanced(sudoku);
            arrIdsChangedHints.addAll(changed);
            arrIdsNotTestedHints.addAll(changed);
            updateSudoku(arrIdsNotTestedHints, arrIdsChangedHints, arrIdsChangedValues, sudoku, hints, byAutoInsert1, byAutoInsert2);
        } while (countChanged != arrIdsChangedValues.size());
    }

    private static void updateSudoku(HashSetLIFOQueue arrIdsNotTestedHints, Set<Integer> arrIdsChangedHints, Set<Integer> arrIdsChangedValues, Playground sudoku, Hints hints, final boolean byAutoInsert1, final boolean byAutoInsert2) {
        int arrId;
        Set<Integer> changed;
        while (!arrIdsNotTestedHints.isEmpty()) {
            arrId = arrIdsNotTestedHints.pop();
            if (arrIdsChangedValues.contains(arrId)) continue;

            if (byAutoInsert1) {
                int number = sudoku.getAutoInsert1ByField(arrId, hints);
                if (number != 0) {
                    sudoku.set(arrId, number);
                    arrIdsChangedValues.add(arrId);
                    changed = hints.incrementStarGroup(arrId, number - 1, sudoku);
                    arrIdsNotTestedHints.addAll(changed);
                    arrIdsChangedHints.addAll(changed);
                    continue;
                }
            }
            if (byAutoInsert2) {
                Integer[] ai2arr = sudoku.getAutoInsert2byStarGroup(arrId, hints);
                if (ai2arr != null) {
                    sudoku.set(ai2arr[0], ai2arr[1]);
                    arrIdsChangedValues.add(ai2arr[0]);
                    changed = hints.incrementStarGroup(ai2arr[0], ai2arr[1] - 1, sudoku);
                    arrIdsNotTestedHints.addAll(changed);
                    arrIdsChangedHints.addAll(changed);
                }
            }
        }
    }

    public static boolean isSolvableSudoku(final int arrId, final Playground sudoku, final boolean byAutoInsert1, final boolean byAutoInsert2, final boolean byAutoHintAdv1, final boolean byAutoHintAdv2, final boolean byAutoHintAdv3, final boolean verbose) {
        return isTrueGrid(solveSudoku(arrId, sudoku, byAutoInsert1, byAutoInsert2, byAutoHintAdv1, byAutoHintAdv2, byAutoHintAdv3, verbose));
    }

    public static boolean isTrueGrid(final Playground pField) {
        if (pField.getSizeNotPopulatedArrIds() != 0) return false;
        Hint hint = new Hint();
        Hints.populatePlainHints(hint, pField);
        for (int arrId : ALL_ARR_IDS) if (hint.get(arrId, pField.get(arrId) - 1) != 1) return false;
        return true;
    }
}
