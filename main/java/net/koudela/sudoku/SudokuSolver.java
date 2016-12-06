package net.koudela.sudoku;

import android.util.Log;

import java.util.HashSet;
import java.util.Set;

import static android.util.Log.v;
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
        for (int tempArrId : SudokuGroups.getStarGroup(arrId))
            if (!sudoku.isPopulated(tempArrId)) {
                idsNotTestedVerticalGroups.push(ID_VERTICAL_GROUPS[tempArrId]);
                idsNotTestedHorizontalGroups.push(ID_HORIZONTAL_GROUPS[tempArrId]);
                idsNotTestedGroupedGroups.push(ID_GROUPED_GROUPS[tempArrId]);
                arrIdsNotTestedHints.push(tempArrId);
            }
    }

    /**
     * Updates the sudoku/plain hints with Playground.getAutoInsert1 as far as possible.
     * @return number of values set
     */
    private int useAutoInsert1() {
        int arrId, number, count = 0;
        while (!arrIdsNotTestedHints.isEmpty()) {
            arrId = arrIdsNotTestedHints.pop();
            if (sudoku.isPopulated(arrId)) continue;
            number = sudoku.getAutoInsert1ByField(arrId, hints);
            if (number != 0) {
                count++;
                sudoku.set(arrId, number);
                updateTracker(arrId);
                updateTracker(hints.incrementStarGroup(arrId, number - 1, sudoku));
            }
        }
        return count;
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
    void updateSudoku(final boolean verbose) {
        // principle: we use the cheaper solving methods first as long as they return results
        updateTracker(sudoku.getNotPopulatedArrIds());
        Set<Integer> changed;
        String log = "";
        while (true) {
            if (byAutoInsert1) {
                int uAI = useAutoInsert1();
                if (verbose) log += "AI1:" + uAI;
            } else if (verbose) log += "AI1";
            if (byAutoInsert2) if (useAutoInsert2()) {
                if (verbose) log += "AI2:+";
                continue;
            } else if (verbose) log += "AI2";
            // no auto insert possible - update the advanced (cheap versions)
            changed = hints.updateAdv1(sudoku);
            if (verbose) {
                if (changed.size() != 0) log += "Adv1:" + changed.toString();
                else log += "Adv1";
            }
            // in the mean using the relaxed methods first is more expensive (there are lots of times
            // they yield no result); the not relaxed versions adv2 and adv3 hold the same results
            // on not degenerate sudokus - use adv2 which is faster in the mean
            if (changed.size() == 0) {
                if (useRelaxation) {
                    changed = hints.updateAdv2(sudoku, true);
                    if (verbose) {
                        if (changed.size() != 0) log += "Adv2r:" + changed.toString();
                        else log += "Adv2r";
                    }
                    if (changed.size() == 0) {
                        changed = hints.updateAdv3(sudoku, true);
                        if (verbose) {
                            if (changed.size() != 0) log += "Adv3r:" + changed.toString();
                            else log += "Adv3r";
                        }
                    }
                } else {
                    changed = hints.updateAdv2(sudoku, false);
                    if (verbose) {
                        if (changed.size() != 0) log += "Adv2:" + changed.toString();
                        else log += "Adv2";
                    }
                }
            }
            // no further computations possible: exit
            if (changed.size() == 0) {
                if (verbose) Log.v("updateSudoku", "exit(" + log + ")");
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
