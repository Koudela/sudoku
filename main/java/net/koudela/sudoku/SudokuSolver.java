package net.koudela.sudoku;

import android.util.Log;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import static net.koudela.sudoku.SudokuGroups.GROUPED_GROUPS;
import static net.koudela.sudoku.SudokuGroups.HORIZONTAL_GROUPS;
import static net.koudela.sudoku.SudokuGroups.ID_GROUPED_GROUPS;
import static net.koudela.sudoku.SudokuGroups.ID_HORIZONTAL_GROUPS;
import static net.koudela.sudoku.SudokuGroups.ID_VERTICAL_GROUPS;
import static net.koudela.sudoku.SudokuGroups.VERTICAL_GROUPS;

class SudokuSolver {
    Playground sudoku;
    Hints hints;
    private Set<Integer> arrIdsChangedHints;
    private Set<Integer> arrIdsChangedValues;
    private HashSetLIFOQueue arrIdsNotTestedHints;
    private HashSetLIFOQueue idsNotTestedVerticalGroups;
    private HashSetLIFOQueue idsNotTestedHorizontalGroups;
    private HashSetLIFOQueue idsNotTestedGroupedGroups;
    private boolean byAutoInsert1;
    private boolean byAutoInsert2;
    private boolean useRelaxation;

    /**
     * @param sudoku the sudoku to solve/holds the as far as possible solved sudoku
     * @param hints the hints to process/holds the updated hints
     * @param arrIdsChangedHints ids of fields where hints get added in the process are added here
     * @param arrIdsChangedValues ids of fields where values get added in the process are added here
     * @param byAutoInsert1 if true Playground.getAutoInsert1 is used
     * @param byAutoInsert2 if true Playground.getAutoInsert2 is used
     * @param useRelaxation if true a relaxed version of Hints.setAutoHintsAdv2/3 is used (it is
     *                      much faster but there can be possible Hints that won't get found)
     */
    void init(Playground sudoku, Hints hints, Set<Integer> arrIdsChangedHints,
              Set<Integer> arrIdsChangedValues, final boolean byAutoInsert1,
              final boolean byAutoInsert2, final boolean useRelaxation) {
        this.sudoku = sudoku;
        this.hints = hints;
        this.arrIdsChangedHints = arrIdsChangedHints;
        this.arrIdsChangedValues =  arrIdsChangedValues;
        arrIdsNotTestedHints = new HashSetLIFOQueue();
        idsNotTestedVerticalGroups = new HashSetLIFOQueue();
        idsNotTestedHorizontalGroups = new HashSetLIFOQueue();
        idsNotTestedGroupedGroups = new HashSetLIFOQueue();
        this.byAutoInsert1 = byAutoInsert1;
        this.byAutoInsert2 = byAutoInsert2;
        this.useRelaxation = useRelaxation;
    }

    private void updateTracker(Set<Integer> changed) {
        for (int arrId : changed) {
            idsNotTestedVerticalGroups.push(ID_VERTICAL_GROUPS[arrId]);
            idsNotTestedHorizontalGroups.push(ID_HORIZONTAL_GROUPS[arrId]);
            idsNotTestedGroupedGroups.push(ID_GROUPED_GROUPS[arrId]);
            arrIdsNotTestedHints.push(arrId);
            arrIdsChangedHints.add(arrId);
        }
    }

    private void updateTracker(int arrId) {
        arrIdsChangedValues.add(arrId);
        idsNotTestedVerticalGroups.push(ID_VERTICAL_GROUPS[arrId]);
        idsNotTestedHorizontalGroups.push(ID_HORIZONTAL_GROUPS[arrId]);
        idsNotTestedGroupedGroups.push(ID_GROUPED_GROUPS[arrId]);
    }

    /**
     * Updates the sudoku/plain hints with Playground.getAutoInsert1 as far as possible.
     */
    private void useAutoInsert1() {
        int arrId, number;
        while (!arrIdsNotTestedHints.isEmpty()) {
            arrId = arrIdsNotTestedHints.pop();
            if (sudoku.isPopulated(arrId)) continue;
            number = sudoku.getAutoInsert1ByField(arrId, hints);
            if (number != 0) {
                sudoku.set(arrId, number);
                updateTracker(arrId);
                updateTracker(hints.incrementStarGroup(arrId, number - 1, sudoku));
            }
        }
    }

    /**
     * Updates the sudoku/plain hints with one Playground.getAutoInsert2 if possible.
     * @return true on success, false otherwise
     */
    private boolean useAutoInsert2() {
        Integer[] ai2arr = {null, null};
        while (!idsNotTestedGroupedGroups.isEmpty() || !idsNotTestedHorizontalGroups.isEmpty() ||
                !idsNotTestedVerticalGroups.isEmpty()) {
            if ((idsNotTestedVerticalGroups.isEmpty() || !sudoku.getAutoInsert2ByGroup(
                    VERTICAL_GROUPS[idsNotTestedVerticalGroups.pop()], hints, ai2arr))
                    && (idsNotTestedHorizontalGroups.isEmpty() || !sudoku.getAutoInsert2ByGroup(
                    HORIZONTAL_GROUPS[idsNotTestedHorizontalGroups.pop()], hints, ai2arr))
                    && (idsNotTestedGroupedGroups.isEmpty() || !sudoku.getAutoInsert2ByGroup(
                    GROUPED_GROUPS[idsNotTestedGroupedGroups.pop()], hints, ai2arr))) continue;
            sudoku.set(ai2arr[0], ai2arr[1]);
            updateTracker(ai2arr[0]);
            updateTracker(hints.incrementStarGroup(ai2arr[0], ai2arr[1] - 1, sudoku));
            return true;
        }
        return false;
    }

    /**
     * Solves a sudoku as far as possible with the specified functions. (The used hints are
     * specified implicit by the hints parameter.)
     */
    void updateSudoku(boolean verbose) {
        // principle: we use the cheaper solving methods first as long as they return results
        updateTracker(sudoku.getNotPopulatedArrIds());
        Set<Integer> changed;
        int count[] = {0, 0, 0, 0, 0, 0, 0};
        while (true) {
            if (verbose) count[0]++;
            if (byAutoInsert1) useAutoInsert1();
            if (verbose) count[1]++;
            if (byAutoInsert2) if (useAutoInsert2()) continue;
            // no auto insert possible - update the advanced (cheap/relaxed versions)
            if (verbose) count[2]++;
            changed = hints.updateAdv1(sudoku);
            if (changed.size() == 0) {
                if (verbose) count[3]++;
                changed = hints.updateAdv2(sudoku, true);
                if (changed.size() == 0) {
                    if (verbose) count[4]++;
                    changed = hints.updateAdv3(sudoku, true);
                    // relaxed methods hold no result - use the costly versions if applicable
                    if (changed.size() == 0 && !useRelaxation) {
                        if (verbose) count[5]++;
                        changed = hints.updateAdv2(sudoku, false);
                        if (changed.size() == 0) {
                            if (verbose) count[6]++;
                            changed = hints.updateAdv3(sudoku, false);
                        }
                    }
                }
            }
            // no further computations possible: exit
            if (changed.size() == 0) {
                if (verbose) Log.v("updateSudoku", "exit(" + Arrays.toString(count) + ")");
                return;
            }
            updateTracker(changed);
        }
    }

    /**
     * Checks if a sudoku is solvable by the supported methods.
     * @param deletionAtArrId if >= 0 sets the specified field to 0 prior to processing
     * @param sudoku the sudoku to solve
     * @param byAutoInsert1 if true Playground.getAutoInsert1 is used
     * @param byAutoInsert2 if true Playground.getAutoInsert2 is used
     * @param byAutoHintAdv1 if true Hints.setAutoHintsAdv1 is used
     * @param byAutoHintAdv2 if true Hints.setAutoHintsAdv2 is used
     * @param byAutoHintAdv3 if true Hints.setAutoHintsAdv3 is used
     * @param useRelaxation if true a relaxed version of Hints.setAutoHintsAdv2/3 is used (it is
     *                      much faster but there can be possible Hints that won't get found)
     * @return true if the sudoku is solvable by the supported methods, false otherwise
     */
    boolean isSolvableSudoku(final int deletionAtArrId, final Playground sudoku,
                             final boolean byAutoInsert1, final boolean byAutoInsert2,
                             final boolean byAutoHintAdv1, final boolean byAutoHintAdv2,
                             final boolean byAutoHintAdv3, final boolean useRelaxation) {
        Playground solution = new Playground(sudoku);
        if (deletionAtArrId > -1) solution.set(deletionAtArrId, 0);
        Hints hints = new Hints(true, byAutoHintAdv1, byAutoHintAdv2, byAutoHintAdv3, false);
        hints.populatePlainHints(solution);
        Set<Integer> arrIdsChangedHints = new HashSet<>();
        Set<Integer> arrIdsChangedValues = new HashSet<>();
        init(solution, hints, arrIdsChangedHints, arrIdsChangedValues, byAutoInsert1, byAutoInsert2,
                useRelaxation);
        updateSudoku(false);
        return Sudoku.isTrueGrid(solution);
    }
}
