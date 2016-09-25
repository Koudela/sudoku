package net.koudela.sudoku;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Some static functions for solving and building sudokus
 *
 * @author Thomas Koudela
 * @version 1.0 stable
 */
@SuppressWarnings({"WeakerAccess", "unused"})
class SudokuStaticFunctions extends SudokuGroups {

    static List<Integer> getRandomizedArrIds() {
        List<Integer> arrIds = new ArrayList<>(Arrays.asList(ALL_ARR_IDS));
        Collections.shuffle(arrIds);
        return arrIds;
    }

    /**
     * Solves a sudoku as far as possible with the specified functions. (Plain hints are used always
     * and UserHints never. This function uses this.updateSudokuStart/this.updateSudoku to process.)
     * @param deletionAtArrId if >= 0 sets the specified field to 0 prior to processing
     * @param sudoku the sudoku to solve
     * @param byAutoInsert1 if true Playground.getAutoInsert1 is used
     * @param byAutoInsert2 if true Playground.getAutoInsert2 is used
     * @param byAutoHintAdv1 if true Hints.setAutoHintsAdv1 is used
     * @param byAutoHintAdv2 if true Hints.setAutoHintsAdv2 is used
     * @param byAutoHintAdv3 if true Hints.setAutoHintsAdv3 is used
     * @param useRelaxation if true a relaxed version of Hints.setAutoHintsAdv2/3 is used (it is
     *                      much faster but there can be possible Hints that won't get found)
     * @return the as far as possible solved sudoku
     */
    private static Playground solveSudoku(final int deletionAtArrId, final Playground sudoku,
                                          final boolean byAutoInsert1, final boolean byAutoInsert2,
                                          final boolean byAutoHintAdv1, final boolean byAutoHintAdv2,
                                          final boolean byAutoHintAdv3, final boolean useRelaxation) {
        Playground solution = new Playground(sudoku);
        if (deletionAtArrId > -1) solution.set(deletionAtArrId, 0);
        Hints hints = new Hints(true, byAutoHintAdv1, byAutoHintAdv2, byAutoHintAdv3, false);
        hints.populatePlainHints(solution);
        Set<Integer> arrIdsChangedHints = new HashSet<>();
        Set<Integer> arrIdsChangedValues = new HashSet<>();
        updateSudokuStart(arrIdsChangedHints, arrIdsChangedValues, solution, hints, byAutoInsert1,
                byAutoInsert2, useRelaxation);
        return solution;
    }

    /**
     * Solves a sudoku as far as possible with the specified functions. (The used hints are
     * specified implicit by the hints parameter. This function uses this.updateSudoku to process.)
     * @param arrIdsChangedHints ids of fields where hints get added in the process are added here
     * @param arrIdsChangedValues ids of fields where values get added in the process are added here
     * @param sudoku the sudoku to solve/holds the as far as possible solved sudoku
     * @param hints the hints to process/holds the updated hints
     * @param byAutoInsert1 if true Playground.getAutoInsert1 is used
     * @param byAutoInsert2 if true Playground.getAutoInsert2 is used
     * @param useRelaxation if true a relaxed version of Hints.setAutoHintsAdv2/3 is used (it is
     *                      much faster but there can be possible Hints that won't get found)
     */
    static void updateSudokuStart(Set<Integer> arrIdsChangedHints, Set<Integer> arrIdsChangedValues,
                                  Playground sudoku, Hints hints, final boolean byAutoInsert1,
                                  final boolean byAutoInsert2, final boolean useRelaxation) {
        // principle: we use the cheaper solving methods first as long as they return results
        HashSetLIFOQueue arrIdsNotTestedHints = new HashSetLIFOQueue(sudoku.getNotPopulatedArrIds());
        int countChanged;
        Set<Integer> changed;
        while (true) {
            updateSudoku(arrIdsNotTestedHints, arrIdsChangedHints, arrIdsChangedValues, sudoku, hints,
                    byAutoInsert1, byAutoInsert2);
            // no auto insert possible - update the advanced (cheap/relaxed versions)
            changed = hints.updateAdvanced(sudoku, true);
            if (changed.size() == 0 && !useRelaxation)
                // relaxed methods hold no result - use the costly versions if applicable
                changed = hints.updateAdvanced2plus3(sudoku, false);
            if (changed.size() != 0) {
                arrIdsChangedHints.addAll(changed);
                arrIdsNotTestedHints.addAll(changed);
            } else break;
        }
    }

    /**
     * Updates the sudoku/plain hints with Playground.getAutoInsert1 and/or Playground.getAutoInsert2
     * as far as possible.
     * @param arrIdsNotTestedHints tracks the ids of the fields where hints where updated but not
     *                             checked with respect to hint/value insertion
     * @param arrIdsChangedHints ids of fields where hints get added in the process are added here
     * @param arrIdsChangedValues ids of fields where values get added in the process are added here
     * @param sudoku the sudoku to solve/holds the as far as possible solved sudoku
     * @param hints the hints to process/holds the updated hints
     * @param byAutoInsert1 if true Playground.getAutoInsert1 is used
     * @param byAutoInsert2 if true Playground.getAutoInsert2 is used
     */
    private static void updateSudoku(HashSetLIFOQueue arrIdsNotTestedHints, Set<Integer> arrIdsChangedHints,
                                     Set<Integer> arrIdsChangedValues, Playground sudoku, Hints hints,
                                     final boolean byAutoInsert1, final boolean byAutoInsert2) {
        int arrId;
        Set<Integer> changed;
        int number;
        Integer[] ai2arr;
        while (!arrIdsNotTestedHints.isEmpty()) {
            arrId = arrIdsNotTestedHints.pop();
            if (arrIdsChangedValues.contains(arrId)) continue;
            if (byAutoInsert1) {
                number = sudoku.getAutoInsert1ByField(arrId, hints);
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
                ai2arr = sudoku.getAutoInsert2byStarGroup(arrId, hints);
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
    static boolean isSolvableSudoku(final int deletionAtArrId, final Playground sudoku,
                                    final boolean byAutoInsert1, final boolean byAutoInsert2,
                                    final boolean byAutoHintAdv1, final boolean byAutoHintAdv2,
                                    final boolean byAutoHintAdv3, final boolean useRelaxation) {
        return isTrueGrid(solveSudoku(deletionAtArrId, sudoku, byAutoInsert1, byAutoInsert2,
                byAutoHintAdv1, byAutoHintAdv2, byAutoHintAdv3, useRelaxation));
    }

    /**
     * Checks if pField represents a valid solution.
     * @param pField the sudoku playground to check
     * @return true if pField represents a valid solution, false otherwise
     */
    static boolean isTrueGrid(final Playground pField) {
        if (pField.getSizeNotPopulatedArrIds() != 0) return false;
        Hint hint = new Hint();
        Hints.populatePlainHints(hint, pField);
        for (int arrId : ALL_ARR_IDS) if (hint.get(arrId, pField.get(arrId) - 1) != 1) return false;
        return true;
    }
}
