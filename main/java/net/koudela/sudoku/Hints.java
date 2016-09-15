package net.koudela.sudoku;

import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

public class Hints {
    protected static final int DIM = Sudoku.DIM;
    protected Hint hint = new Hint();
    protected Hint hintAdv1 = new Hint();
    protected Hint hintAdv2 = new Hint();
    protected Hint hintAdv3 = new Hint();
    protected boolean usePlain = true;
    protected boolean useAdv1 = false;
    protected boolean useAdv2 = false;
    protected boolean useAdv3 = false;

    public Hints() {}

    public Hints(boolean usePlain, boolean useAdv1, boolean useAdv2, boolean useAdv3) {
        this.usePlain = usePlain;
        this.useAdv1 = useAdv1;
        this.useAdv2 = useAdv2;
        this.useAdv3 = useAdv3;
    }

    public boolean isHint(int arrId, int num) {
        return (usePlain && hint.isHint(arrId, num)
                || useAdv1 && hintAdv1.isHint(arrId, num)
                || useAdv2 && hintAdv2.isHint(arrId, num)
                || useAdv3 && hintAdv3.isHint(arrId, num)
        );
    }

    public void init() {
        hint.init();
        hintAdv1.init();
        hintAdv2.init();
        hintAdv3.init();
    }

    public static void incrementStarGroup(int arrId, int num, Hint hint) {
        for (int tempArrId : Sudoku.getStarGroup(arrId))
            hint.increment(tempArrId, num);
    }

    public void incrementStarGroup(int arrId, int num) {
        incrementStarGroup(arrId, num, hint);
    }

    public static void decrementStarGroup(int arrId, int num, Hint hint) {
        for (int tempArrId : Sudoku.getStarGroup(arrId))
            hint.decrement(tempArrId, num);
    }

    public static void populatePlainHints(Hint hint, Playground pField) {
        for (Map.Entry<Integer, Integer> arrId : pField.getPopulatedArrIds().entrySet())
            incrementStarGroup(arrId.getValue(), pField.get(arrId.getValue()) - 1, hint);
    }

    public void populatePlainHints(Playground pField) {
        populatePlainHints(hint, pField);
    }

    // if a number is group wise bounded to a specific row/column, in the same row/column other groups get hints
    public int setAutoHintsAdv1(Playground pField) {
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
    protected int setAutoHintsAdv2ByGroup(Integer[] group, Playground pField) {
        int changed = 0;
        for (int arrId : group) {
            if (pField.isPopulated(arrId)) continue;
            Set<Integer> missing = new LinkedHashSet<>();
            Set<Integer> notMissing = new TreeSet<>();
            Set<Integer> otherArrIds = new LinkedHashSet<>();
            for (int num = 0; num < DIM; num++)
                if (isHint(arrId, num)) notMissing.add(num);
                else missing.add(num);
            if (missing.size() == 0 || notMissing.size() == 0) continue;
            int populatedFieldsCount = 0;
            for (int tempArrIds : group) {
                if (tempArrIds == arrId) continue;
                if (pField.isPopulated(tempArrIds)) {
                    populatedFieldsCount++;
                    continue;
                }
                if (notMissing.size() < otherArrIds.size()) break;
                for (int num = 0; num < DIM; num++)
                    if (!isHint(tempArrIds, num) && notMissing.contains(num)) {
                        otherArrIds.add(tempArrIds);
                        break;
                    }
            }
            if (notMissing.size() == otherArrIds.size() + populatedFieldsCount) changed += setAutoHintsAdv2ByGroupSub(missing, otherArrIds);
        }
        return changed;
    }

    protected int setAutoHintsAdv2ByGroupSub(Set<Integer> missing, Set<Integer> otherArrIds) {
        int changed = 0;
        for (int arrId : otherArrIds)
            for (int num : missing)
                if (!hintAdv2.isHint(arrId, num)) {
                    hintAdv2.increment(arrId, num);
                    changed++;
                }
        return changed;
    }

    public int setAutoHintsAdv2(Playground pField) {
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
    protected int setAutoHintsAdv3ByGroup(Integer[] group, Playground pField) {
        int changed = 0;
        Map<Integer, Set<Integer>> missing = new LinkedHashMap<>();
        Set<Integer> fields = new HashSet<>();
        for (int num = 0; num < DIM; num++) {
            missing.put(num, new LinkedHashSet<Integer>());
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

    public int setAutoHintsAdv3(Playground pField) {
        int changed = 0;
        for (int i = 0; i < DIM; i++) {
            changed += setAutoHintsAdv3ByGroup(Sudoku.VERTICAL_GROUPS[i], pField);
            changed += setAutoHintsAdv3ByGroup(Sudoku.HORIZONTAL_GROUPS[i], pField);
            changed += setAutoHintsAdv3ByGroup(Sudoku.GROUPED_GROUPS[i], pField);
        }
        return changed;
    }
}