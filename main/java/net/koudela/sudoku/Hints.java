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
    private Hint userHint = new Hint();
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

    void setUsePlain(final boolean usePlain) {
        this.usePlain = usePlain;
    }

    void setUseAdv1(final boolean useAdv1) {
        this.useAdv1 = useAdv1;
    }

    void setUseAdv2(final boolean useAdv2) {
        this.useAdv2 = useAdv2;
    }

    void setUseAdv3(final boolean useAdv3) {
        this.useAdv3 = useAdv3;
    }

    public void init() {
        hint.init();
        hintAdv1.init();
        hintAdv2.init();
        hintAdv3.init();
        userHint.init();
    }

    public void init(Playground pField, boolean firstRun) {
        // on first Run the hints are already initialized
        if (!firstRun) init();
        for (int arrId : pField.getPopulatedArrIds()) incrementStarGroup(arrId, pField.get(arrId) - 1);
        // on first Run we wanna skip functions with high execution time costs
        if (!firstRun) {
            if (useAdv1) setAutoHintsAdv1(pField, false);
            if (useAdv2) setAutoHintsAdv2(pField, false);
            if (useAdv3) setAutoHintsAdv3(pField, false);
        }
    }

    public void initAdv() {
        if (useAdv1) hintAdv1.init();
        if (useAdv2) hintAdv2.init();
        if (useAdv3) hintAdv3.init();
    }

    public void initAdv1() {
        hintAdv1.init();
    }

    public void initAdv2() {
        hintAdv2.init();
    }

    public void initAdv3() {
        hintAdv3.init();
    }

    public int getPlainHint(final int arrId, final int num) {
        return hint.get(arrId, num);
    }

    public boolean isUserHint(final int arrId, final int num) {
        return userHint.isHint(arrId, num);
    }

    public void setUserHint(final int arrId, final int num) {
        userHint.set(arrId, num, userHint.isHint(arrId, num)?0:1);
    }

    public Set<Integer> updateAdvanced(final Playground pField) {
        Set<Integer> changed = new HashSet<>();
        if (useAdv1) setAutoHintsAdv1Plus(pField, changed);
        if (useAdv2) setAutoHintsAdv2Plus(pField, changed);
        if (useAdv3) setAutoHintsAdv3Plus(pField, changed);
        return changed;
    }

    public boolean isHint(final int arrId, final int num) {
        return (userHint.isHint(arrId, num)
                || usePlain && hint.isHint(arrId, num)
                || useAdv1 && hintAdv1.isHint(arrId, num)
                || useAdv2 && hintAdv2.isHint(arrId, num)
                || useAdv3 && hintAdv3.isHint(arrId, num)
        );
    }

    public Set<Integer> incrementStarGroup(final int arrId, final int num, final Playground pField) {
        Set<Integer> changed = new HashSet<>();
        for (int tempArrId : Sudoku.getStarGroup(arrId)) {
            hint.increment(tempArrId, num);
            if (!pField.isPopulated(tempArrId) && hint.get(tempArrId, num) == 1) changed.add(tempArrId);
        }
        return changed;
    }

    public static void incrementStarGroup(final int arrId, final int num, Hint hint) {
        for (int tempArrId : Sudoku.getStarGroup(arrId))
            hint.increment(tempArrId, num);
    }

    public void incrementStarGroup(final int arrId, final int num) {
        incrementStarGroup(arrId, num, hint);
    }

    public Set<Integer> decrementStarGroup(final int arrId, final int num, final Playground pField) {
        Set<Integer> changed = new HashSet<>();
        for (int tempArrId : Sudoku.getStarGroup(arrId)) {
            hint.decrement(tempArrId, num);
            if (!pField.isPopulated(tempArrId) && hint.get(tempArrId, num) == 0) changed.add(tempArrId);
        }
        return changed;
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
    public int setAutoHintsAdv1(final Playground pField, final boolean getOnly) {
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
                    for (int arrId : Sudoku.getComplementVerticalGroup(tempArrId)) if (!pField.isPopulated(arrId) && !hintAdv1.isHint(arrId, num)) {
                        if (getOnly) {
                            if (!isHint(arrId, num)) return arrId;
                            continue;
                        }
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
                    for (int arrId : Sudoku.getComplementHorizontalGroup(tempArrId)) if (!pField.isPopulated(arrId) && !hintAdv1.isHint(arrId, num)) {
                        if (getOnly) {
                            if (!isHint(arrId, num)) return arrId;
                            continue;
                        }
                        hintAdv1.increment(arrId, num);
                        changed++;
                    }
                }
            }
        if (getOnly) return -1;
        return changed;
    }

    // if in a group/row/column are n fields with the same n hints or less missing, hints get set on the same numbers on the other fields
    private int setAutoHintsAdv2ByGroup(final Integer[] group, final Playground pField, boolean getOnly) {
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
            if (otherArrIds.size() != 0 && notMissing.size() == otherArrIds.size() + populatedFieldsCount) {
                if (getOnly) {
                    for (int tempArrId : otherArrIds)
                        for (int num : missing)
                            if (!isHint(tempArrId, num)) return tempArrId;
                } else changed += setAutoHintsAdv2ByGroupSub(missing, otherArrIds);
            }
        }
        if (getOnly) return -1;
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

    public int setAutoHintsAdv2(final Playground pField, boolean getOnly) {
        int changed = 0;
        int arrId;
        if (getOnly) for (int i = 0; i < DIM; i++) {
            arrId = setAutoHintsAdv2ByGroup(Sudoku.VERTICAL_GROUPS[i], pField, true);
            if (arrId != -1) return arrId;
            arrId = setAutoHintsAdv2ByGroup(Sudoku.HORIZONTAL_GROUPS[i], pField, true);
            if (arrId != -1) return arrId;
            arrId = setAutoHintsAdv2ByGroup(Sudoku.GROUPED_GROUPS[i], pField, true);
            if (arrId != -1) return arrId;
        } else for (int i = 0; i < DIM; i++) {
            changed += setAutoHintsAdv2ByGroup(Sudoku.VERTICAL_GROUPS[i], pField, false);
            changed += setAutoHintsAdv2ByGroup(Sudoku.HORIZONTAL_GROUPS[i], pField, false);
            changed += setAutoHintsAdv2ByGroup(Sudoku.GROUPED_GROUPS[i], pField, false);
        }
        if (getOnly) return -1;
        return changed;
    }

    // if in a group/row/column are n missing hints distributed over n fields only, then hints get set on the other numbers on the same n fields
    // be careful, this algorithm resides in EXPTIME
    private int setAutoHintsAdv3ByGroup(final Integer[] group, final Playground pField, boolean getOnly) {
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
                    for (int arrId : distributedOverFields) {
                        if (getOnly) {
                            if (!isHint(arrId,num)) return arrId;
                        } else if (!hintAdv3.isHint(arrId, num)) {
                            hintAdv3.increment(arrId, num);
                            changed++;
                        }
                    }
        }
        if (getOnly) return -1;
        return changed;
    }

    public int setAutoHintsAdv3(final Playground pField, boolean getOnly) {
        int changed = 0;
        Set<Integer> arrId;
        if (getOnly) for (int i = 0; i < DIM; i++) {
            arrId = setAutoHintsAdv4ByGroupPlus(Sudoku.VERTICAL_GROUPS[i], pField, true);
            if (arrId.size() == 1) return ((Integer[]) arrId.toArray())[0];
            arrId = setAutoHintsAdv4ByGroupPlus(Sudoku.HORIZONTAL_GROUPS[i], pField, true);
            if (arrId.size() == 1) return ((Integer[]) arrId.toArray())[0];
            arrId = setAutoHintsAdv4ByGroupPlus(Sudoku.GROUPED_GROUPS[i], pField, true);
            if (arrId.size() == 1) return ((Integer[]) arrId.toArray())[0];
        } else for (int i = 0; i < DIM; i++) {
            changed += setAutoHintsAdv4ByGroupPlus(Sudoku.VERTICAL_GROUPS[i], pField, false).size();
            changed += setAutoHintsAdv4ByGroupPlus(Sudoku.HORIZONTAL_GROUPS[i], pField, false).size();
            changed += setAutoHintsAdv4ByGroupPlus(Sudoku.GROUPED_GROUPS[i], pField, false).size();
        }
        if (getOnly) return -1;
        return changed;
    }

    // if a number is group wise bounded to a specific row/column, in the same row/column other groups get hints
    public void setAutoHintsAdv1Plus(final Playground pField, Set<Integer> changed) {
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
                    for (int arrId : Sudoku.getComplementVerticalGroup(tempArrId)) if (!pField.isPopulated(arrId) && !hintAdv1.isHint(arrId, num)) {
                        hintAdv1.increment(arrId, num);
                        changed.add(arrId);
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
                    for (int arrId : Sudoku.getComplementHorizontalGroup(tempArrId)) if (!pField.isPopulated(arrId) && !hintAdv1.isHint(arrId, num)) {
                        hintAdv1.increment(arrId, num);
                        changed.add(arrId);
                    }
                }
            }
    }

    // if in a group/row/column are n fields with the same n hints or less missing, hints get set on the same numbers on the other fields
    private void setAutoHintsAdv2ByGroupPlus(final Integer[] group, final Playground pField, Set<Integer> changed) {
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
                // mark all fields that don't fit in our scheme, meaning other hints are missing
                for (int num = 0; num < DIM; num++)
                    if (!isHint(tempArrIds, num) && notMissing.contains(num)) {
                        otherArrIds.add(tempArrIds);
                        break;
                    }
            }
            // there are fields to process && missing size equals the count of similar fields
            if (otherArrIds.size() != 0 && notMissing.size() == otherArrIds.size() + populatedFieldsCount) setAutoHintsAdv2ByGroupSubPlus(missing, otherArrIds, changed);
        }
    }

    private void setAutoHintsAdv2ByGroupSubPlus(final Set<Integer> missing, final Set<Integer> otherArrIds, Set<Integer> changed) {
        for (int arrId : otherArrIds)
            for (int num : missing)
                if (!hintAdv2.isHint(arrId, num)) {
                    hintAdv2.increment(arrId, num);
                    changed.add(arrId);
                }
    }

    public void setAutoHintsAdv2Plus(final Playground pField, Set<Integer> changed) {
        for (int i = 0; i < DIM; i++) {
            setAutoHintsAdv2ByGroupPlus(Sudoku.VERTICAL_GROUPS[i], pField, changed);
            setAutoHintsAdv2ByGroupPlus(Sudoku.HORIZONTAL_GROUPS[i], pField, changed);
            setAutoHintsAdv2ByGroupPlus(Sudoku.GROUPED_GROUPS[i], pField, changed);
        }
    }

    // if in a group/row/column are n missing hints distributed over n fields only, then hints get set on the other numbers on the same n fields
    // be careful, this algorithm resides in EXPTIME
    private void setAutoHintsAdv3ByGroupPlus(final Integer[] group, final Playground pField, Set<Integer> changed) {
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
                            changed.add(arrId);
                        }
        }
    }

    public void setAutoHintsAdv3Plus(final Playground pField, Set<Integer> changed) {
        for (int i = 0; i < DIM; i++) {
            changed.addAll(setAutoHintsAdv4ByGroupPlus(Sudoku.VERTICAL_GROUPS[i], pField, false));
            changed.addAll(setAutoHintsAdv4ByGroupPlus(Sudoku.HORIZONTAL_GROUPS[i], pField, false));
            changed.addAll(setAutoHintsAdv4ByGroupPlus(Sudoku.GROUPED_GROUPS[i], pField, false));
        }
    }

    /**
      *    We calculate the following: If in a group/row/column are n missing hints distributed over n fields only, then hints get set on the other numbers on the same n fields.
      *    We use a powerSet to search for combinations that fit the if-clause, therefore we get a time complexity of O(2^n).
      *    But if we think outside the box, instead of searching the fields, we can search for n hints, missing the same or less n boxes. Hints.setAutoHintsAdv2 does something similar:
      *    If in a group/row/column are n fields with the same n hints or less missing, ... This search is done in O(n³).
      *    Therefore rewriting H.sAHA2 in the right way, can do the job.
      **/
    // polynomial time version of setAutoHintsAdv3ByGroupsPlus (to include getOnly the input/return type is altered!)
    // if in a group/row/column are n hints witch are missing on the same n fields or less, hints get set on the other numbers on the same n fields
    private Set<Integer> setAutoHintsAdv4ByGroupPlus(final Integer[] group, final Playground pField, boolean getOnly) {
        Set<Integer> changed = new HashSet<>(8);
        Set<Integer> missing = new HashSet<>(16);
        Set<Integer> notMissing = new HashSet<>(16);
        Set<Integer> otherNumbers = new HashSet<>(16);
        int populatedFieldsCount;
        for (int num = 0; num < DIM; num++) {
            missing.clear();
            notMissing.clear();
            otherNumbers.clear();
            populatedFieldsCount = 0;
            // Looking at a specific number
            for (int arrId : group)
                if (!pField.isPopulated(arrId)) populatedFieldsCount++;
                else if (isHint(arrId, num)) notMissing.add(arrId);
                else missing.add(arrId);
            // if all fields miss the hint, we move on
            if (notMissing.size() == 0) continue;
            // Looking at the other numbers
            for (int otherNum = 0; otherNum < DIM; otherNum++) {
                if (otherNum == num) continue;
                // mark all numbers that don't fit in our scheme, meaning they are missing on other fields
                for (int arrId : group)
                    if (!pField.isPopulated(arrId)
                            && !isHint(arrId, otherNum)
                            && notMissing.contains(arrId)) {
                        otherNumbers.add(otherNum);
                        break;
                    }
            }
            // there are numbers to process && missing size equals the count of similar numbers
            if (otherNumbers.size() != 0 && missing.size() == DIM - (otherNumbers.size() + populatedFieldsCount))
                // hints get set on the other numbers on the same n fields.
                for (int otherNum : otherNumbers)
                    for (int arrId : missing)
                        if (!hintAdv3.isHint(arrId, otherNum)) {
                            changed.add(arrId);
                            if (getOnly) return changed;
                            hintAdv3.increment(arrId, otherNum);
                        }
        }
        return changed;
    }
}