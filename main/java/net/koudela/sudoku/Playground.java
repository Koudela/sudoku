package net.koudela.sudoku;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static net.koudela.sudoku.SudokuGroups.DIM;

/**
 * transforms and memorizes a sudoku field
 * can find/set possible new values if {@link Hints} are supplied
 *
 * @author Thomas Koudela
 * @version 1.0 stable
 */
@SuppressWarnings({"WeakerAccess", "unused"})
class Playground extends PField{
    private Set<Integer> populatedArrIds = new HashSet<>(128);
    private Set<Integer> notPopulatedArrIds = new HashSet<>(128);

    Playground() {
        super();
        init();
    }

    Playground(final int[] clone) {
        super(clone);
        init();
    }

    Playground(final Playground clone) {
        super(clone.getPField());
        init();
    }

    void init() {
        populatedArrIds.clear();
        notPopulatedArrIds.clear();
        for (int arrId : Sudoku.ALL_ARR_IDS)
            if (isPopulated(arrId)) populatedArrIds.add(arrId);
            else notPopulatedArrIds.add(arrId);
    }

    void set(final int arrId, final int val) {
        super.setFinal(arrId, val);
        if (val == 0) {
            populatedArrIds.remove(arrId);
            notPopulatedArrIds.add(arrId);
        } else {
            notPopulatedArrIds.remove(arrId);
            populatedArrIds.add(arrId);
        }
    }

    int getSizePopulatedArrIds() {
        return populatedArrIds.size();
    }

    int getSizeNotPopulatedArrIds() {
        return notPopulatedArrIds.size();
    }

    Set<Integer> getPopulatedArrIds() {
        return Collections.unmodifiableSet(populatedArrIds);
    }

    Set<Integer> getNotPopulatedArrIds() {
        return Collections.unmodifiableSet(notPopulatedArrIds);
    }

    /**
     * @param arrId the id of the field to inspect
     * @param hints  {@link Hints}
     * @return if exact 8 hints are displayed the missing number, 0 otherwise
     */
    int getAutoInsert1ByField(final int arrId, final Hints hints) {
        if (isPopulated(arrId)) return 0;
        int number = 0;
        for (int num = 0; num < DIM; num++)
            if (!hints.isHint(arrId, num)) {
                if (number != 0) return 0;
                else number = num + 1;
            }
        return number;
    }

    /**
     * Inspects the whole playground for an insert value.
     * @see #getAutoInsert1ByField
     */
    Integer[] getAutoInsert1(final Hints hints) {
        int number;
        for (int arrId : notPopulatedArrIds) {
            number = getAutoInsert1ByField(arrId, hints);
            if (number != 0) return new Integer[]{arrId, number};
        }
        return null;
    }

    /**
     * @param group an array of the field ids to inspect
     * @param hints the {@link Hints} to process
     * @param ai2arr if the method returns true this array contains field id and number for a new
     *               playground value
     * @return true if a hint is missing only once in the fields of the group, false otherwise
     */
    boolean getAutoInsert2ByGroup(final Integer[] group, final Hints hints, Integer[] ai2arr) {
        for (int num = 0; num < DIM; num++) {
            int count = 0;
            ai2arr[0] = -1;
            for (int arrId : group) {
                if (isPopulated(arrId) || hints.isHint(arrId, num)) count++;
                else if (ai2arr[0] == -1) ai2arr[0] = arrId;
                else break;
            }
            if (count == 8) {
                ai2arr[1] = num + 1;
                return true;
            }
        }
        return false;
    }

    /**
     * Inspects a star group centered around arrId for an insert value.
     * @see #getAutoInsert2ByGroup
     * @param arrId the field id the three groups horizontal/vertical/group wise are centered
     */
    Integer[] getAutoInsert2byStarGroup(final int arrId, final Hints hints) {
        Integer[] ai2arr = {null, null};
        if (getAutoInsert2ByGroup(Sudoku.VERTICAL_GROUPS[Sudoku.ID_VERTICAL_GROUPS[arrId]], hints, ai2arr)
                || getAutoInsert2ByGroup(Sudoku.HORIZONTAL_GROUPS[Sudoku.ID_HORIZONTAL_GROUPS[arrId]], hints, ai2arr)
                || getAutoInsert2ByGroup(Sudoku.GROUPED_GROUPS[Sudoku.ID_GROUPED_GROUPS[arrId]], hints, ai2arr))
            return ai2arr;
        return null;
    }

    /**
     * Inspects the whole playground for an insert value.
     * @see #getAutoInsert2ByGroup
     */
    Integer[] getAutoInsert2(final Hints hints) {
        Integer[] ai2arr = {null, null};
        for (int i = 0; i < DIM; i++) {
            if (getAutoInsert2ByGroup(Sudoku.VERTICAL_GROUPS[i], hints, ai2arr)) return ai2arr;
            if (getAutoInsert2ByGroup(Sudoku.HORIZONTAL_GROUPS[i], hints, ai2arr)) return ai2arr;
            if (getAutoInsert2ByGroup(Sudoku.GROUPED_GROUPS[i], hints, ai2arr)) return ai2arr;
        }
        return null;
    }

    @Override
    void shuffle(List<List<Integer>> relations) {
        super.shuffle(relations);
        init();
    }
}