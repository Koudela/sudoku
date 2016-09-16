package net.koudela.sudoku;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class Hints {
    private static final int DIM = Sudoku.DIM;
    private Hint hint = new Hint();
    private Hint hintAdv1 = new Hint();
    private Hint hintAdv2 = new Hint();
    private Hint hintAdv3 = new Hint();
    private boolean usePlain = true;
    private boolean useAdv1 = false;
    private boolean useAdv2 = false;
    private boolean useAdv3 = false;

    public Hints() {}

    public Hints(final boolean usePlain, final boolean useAdv1, final boolean useAdv2, final boolean useAdv3) {
        this.usePlain = usePlain;
        this.useAdv1 = useAdv1;
        this.useAdv2 = useAdv2;
        this.useAdv3 = useAdv3;
    }

    public void init() {
        hint.init();
        hintAdv1.init();
        hintAdv2.init();
        hintAdv3.init();
    }

    public boolean isHint(final int arrId, final int num) {
        return (usePlain && hint.isHint(arrId, num)
                || useAdv1 && hintAdv1.isHint(arrId, num)
                || useAdv2 && hintAdv2.isHint(arrId, num)
                || useAdv3 && hintAdv3.isHint(arrId, num)
        );
    }

    public static void incrementStarGroup(final int arrId, final int num, Hint hint) {
        for (int tempArrId : Sudoku.getStarGroup(arrId))
            hint.increment(tempArrId, num);
    }

    public void incrementStarGroup(final int arrId, final int num) {
        incrementStarGroup(arrId, num, hint);
    }

    public static void decrementStarGroup(final int arrId, final int num, Hint hint) {
        for (int tempArrId : Sudoku.getStarGroup(arrId))
            hint.decrement(tempArrId, num);
    }

    public void decrementStarGroup(final int arrId, final int num) {
        decrementStarGroup(arrId, num, hint);
    }

    public static void populatePlainHints(Hint hint, final Playground pField) {
        for (int arrId : pField.getPopulatedArrIds())
            incrementStarGroup(arrId, pField.get(arrId) - 1, hint);
    }

    public void populatePlainHints(final Playground pField) {
        populatePlainHints(hint, pField);
    }

    // if a number is group wise bounded to a specific row/column, in the same row/column other groups get hints
    public int setAutoHintsAdv1(final Playground pField) {
        int changed = 0;
        for (int num = 0; num < DIM; num++)
            for (int cnt = 0; cnt < DIM; cnt++) {
                // check vertical
                int tempArrId = -1;
                for (int arrId : Sudoku.GROUPED_GROUPS[cnt])
                    if (!pField.isPopulated(arrId) && !isHint(arrId, num))
                        if (tempArrId == -1) tempArrId = arrId;
                        else if (arrId != tempArrId + 1 && arrId != tempArrId + 2) {
                            tempArrId = -1;
                            break;
                        }
                // add hints vertical
                if (tempArrId != -1) {
                    for (int arrId : Sudoku.getComplementVerticalGroup(tempArrId)) if (!hintAdv1.isHint(arrId, num)) {
                        hintAdv1.increment(arrId, num);
                        changed++;
                    }
                }
                // check horizontal
                tempArrId = -1;
                for (int arrId : Sudoku.GROUPED_GROUPS[cnt])
                    if (!pField.isPopulated(arrId) && !isHint(arrId, num))
                        if (tempArrId == -1) tempArrId = arrId;
                        else if (arrId != tempArrId + 9 && arrId != tempArrId + 18) {
                            tempArrId = -1;
                            break;
                        }
                // add hints vertical
                if (tempArrId != -1) {
                    for (int arrId : Sudoku.getComplementHorizontalGroup(tempArrId)) if (!hintAdv1.isHint(arrId, num)) {
                        hintAdv1.increment(arrId, num);
                        changed++;
                    }
                }
            }
        return changed;
    }

    // if in a group/row/column are n fields with the same n hints or less missing, hints get set on the same numbers on the other fields
    private int setAutoHintsAdv2ByGroup(final Integer[] group, final Playground pField) {
        int changed = 0;
        int populatedFieldsCount;
        Set<Integer> missing = new HashSet<>(16);
        Set<Integer> notMissing = new HashSet<>(16);
        Set<Integer> otherArrIds = new HashSet<>(16);
        for (int arrId : group) {
            if (pField.isPopulated(arrId)) continue;
            missing.clear();
            notMissing.clear();
            otherArrIds.clear();
            // Looking at a specific unpopulated field
            for (int num = 0; num < DIM; num++)
                if (isHint(arrId, num)) notMissing.add(num);
                else missing.add(num);
            // If no hints are set, we move on
            if (notMissing.size() == 0) continue;
            populatedFieldsCount = 0;
            // Looking at the other fields
            for (int tempArrIds : group) {
                if (tempArrIds == arrId) continue;
                if (pField.isPopulated(tempArrIds)) {
                    populatedFieldsCount++;
                    continue;
                }
                if (notMissing.size() < otherArrIds.size() + populatedFieldsCount) break;
                // mark all Fields that don't fit in our scheme, meaning other hints are missing
                for (int num = 0; num < DIM; num++)
                    if (!isHint(tempArrIds, num) && notMissing.contains(num)) {
                        otherArrIds.add(tempArrIds);
                        break;
                    }
            }
            // there are fields to process && missing size equals the count of similar fields
            if (otherArrIds.size() != 0 && notMissing.size() == otherArrIds.size() + populatedFieldsCount) changed += setAutoHintsAdv2ByGroupSub(missing, otherArrIds);
        }
        return changed;
    }

    private int setAutoHintsAdv2ByGroupSub(final Set<Integer> missing, final Set<Integer> otherArrIds) {
        int changed = 0;
        for (int arrId : otherArrIds)
            for (int num : missing)
                if (!hintAdv2.isHint(arrId, num)) {
                    hintAdv2.increment(arrId, num);
                    changed++;
                }
        return changed;
    }

    public int setAutoHintsAdv2(final Playground pField) {
        int changed = 0;
        for (int i = 0; i < DIM; i++) {
            changed += setAutoHintsAdv2ByGroup(Sudoku.VERTICAL_GROUPS[i], pField);
            changed += setAutoHintsAdv2ByGroup(Sudoku.HORIZONTAL_GROUPS[i], pField);
            changed += setAutoHintsAdv2ByGroup(Sudoku.GROUPED_GROUPS[i], pField);
        }
        return changed;
    }

    // if in a group/row/column are n missing hints distributed over n fields only, then hints get set on the other numbers on the same n fields
    // be careful, this algorithm resides in EXPTIME
    private int setAutoHintsAdv3ByGroup(final Integer[] group, final Playground pField) {
        int changed = 0;
        Map<Integer, Set<Integer>> missing = new HashMap<>();
        Set<Integer> fields = new HashSet<>();
        for (int num = 0; num < DIM; num++) {
            missing.put(num, new HashSet<Integer>());
            for (int arrId : group)
                if (!pField.isPopulated(arrId) && !isHint(arrId, num)) {
                    missing.get(num).add(arrId);
                    fields.add(num);
                }
        }
        for (Set<Integer> subset : SudokuExptimeFunctions.powerSet(fields)) {
            if (subset.size() <= 1 || subset.size() == fields.size()) continue;
            Set<Integer> distributedOverFields = new HashSet<>();
            for (int num : subset)
                distributedOverFields.addAll(missing.get(num));
            if (distributedOverFields.size() > subset.size()) continue;
            for (int num = 0; num < DIM; num++)
                if (!subset.contains(num))
                    for (int arrId : distributedOverFields)
                        if (!hintAdv3.isHint(arrId, num)) {
                            hintAdv3.increment(arrId, num);
                            changed++;
                        }
        }
        return changed;
    }

    public int setAutoHintsAdv3(final Playground pField) {
        int changed = 0;
        for (int i = 0; i < DIM; i++) {
            changed += setAutoHintsAdv3ByGroup(Sudoku.VERTICAL_GROUPS[i], pField);
            changed += setAutoHintsAdv3ByGroup(Sudoku.HORIZONTAL_GROUPS[i], pField);
            changed += setAutoHintsAdv3ByGroup(Sudoku.GROUPED_GROUPS[i], pField);
        }
        return changed;
    }
}