package net.koudela.sudoku;

import android.annotation.SuppressLint;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static net.koudela.sudoku.SudokuGroups.DIM;

/**
 * generates and memorizes different types of sudoku hints
 *
 * @author Thomas Koudela (excluding {@link #powerSet)
 * @version 1.0 stable
 */
@SuppressWarnings({"WeakerAccess", "unused"})
class Hints {
    private Hint hint;
    private Hint hintAdv1;
    private Hint hintAdv2;
    private Hint hintAdv3;
    private Hint userHint;
    private boolean usePlain;
    private boolean useAdv1;
    private boolean useAdv2;
    private boolean useAdv3;
    private boolean useUserHint;

    Hints(final boolean usePlain, final boolean useAdv1, final boolean useAdv2, final boolean useAdv3,
          final boolean useUserHint) {
        setUsePlain(usePlain);
        setUseAdv1(useAdv1);
        setUseAdv2(useAdv2);
        setUseAdv3(useAdv3);
        setUseUserHint(useUserHint);
    }

    void setUsePlain(final boolean usePlain) {
        this.usePlain = usePlain;
        if (this.usePlain) hint = new Hint();
    }

    void setUseAdv1(final boolean useAdv1) {
        this.useAdv1 = useAdv1;
        if (this.useAdv1) hintAdv1 = new Hint();
    }

    void setUseAdv2(final boolean useAdv2) {
        this.useAdv2 = useAdv2;
        if (this.useAdv2) hintAdv2 = new Hint();
    }

    void setUseAdv3(final boolean useAdv3) {
        this.useAdv3 = useAdv3;
        if (this.useAdv3) hintAdv3 = new Hint();
    }

    void setUseUserHint(final boolean useUserHint) {
        this.useUserHint = useUserHint;
        if (this.useUserHint) userHint = new Hint();
    }

    boolean[] getUsage() {
        return new boolean[]{usePlain, useAdv1, useAdv2, useAdv3, useUserHint};
    }

    void init(Playground pField, boolean firstRun, boolean useRelaxation) {
        // on first Run the hints are already initialized
        if (!firstRun) {
            if (usePlain) hint.init();
            if (useAdv1) hintAdv1.init();
            if (useAdv2) hintAdv2.init();
            if (useAdv3) hintAdv3.init();
            if (useUserHint) userHint.init();
        }
        if (usePlain) populatePlainHints(pField);
        if (useAdv1) setAutoHintsAdv1(pField, false);
        // on first Run we wanna skip functions with high execution time costs
        if (!firstRun || useRelaxation) {
            if (useAdv2) setAutoHintsAdv2(pField, false, useRelaxation);
            if (useAdv3) setAutoHintsAdv3(pField, false, useRelaxation);
        }
    }

    void initAdv() {
        if (useAdv1) hintAdv1.init();
        if (useAdv2) hintAdv2.init();
        if (useAdv3) hintAdv3.init();
    }

    void initAdv1() {
        hintAdv1.init();
    }

    void initAdv2() {
        hintAdv2.init();
    }

    void initAdv3() {
        hintAdv3.init();
    }

    int getPlainHint(final int arrId, final int num) {
        return hint.get(arrId, num);
    }

    boolean isUserHint(final int arrId, final int num) {
        return userHint.isHint(arrId, num);
    }

    void setUserHint(final int arrId, final int num) {
        userHint.set(arrId, num, userHint.isHint(arrId, num)?0:1);
    }

    Set<Integer> updateAdv1(final Playground pField) {
        if (useAdv1) return setAutoHintsAdv1(pField, false);
        return new HashSet<>();
    }

    Set<Integer> updateAdv2(final Playground pField, boolean useRelaxation) {
        if (useAdv2) return setAutoHintsAdv2(pField, false, useRelaxation);
        return new HashSet<>();
    }

    Set<Integer> updateAdv3(final Playground pField, boolean useRelaxation) {
        if (useAdv3) return setAutoHintsAdv3(pField, false, useRelaxation);
        return new HashSet<>();
    }

    boolean isHint(final int arrId, final int num) {
        return (useUserHint && userHint.isHint(arrId, num)
                || usePlain && hint.isHint(arrId, num)
                || useAdv1 && hintAdv1.isHint(arrId, num)
                || useAdv2 && hintAdv2.isHint(arrId, num)
                || useAdv3 && hintAdv3.isHint(arrId, num)
        );
    }

    void populatePlainHints(final Playground pField) {
        populatePlainHints(hint, pField);
    }

    static void populatePlainHints(Hint hint, final Playground pField) {
        for (int arrId : pField.getPopulatedArrIds())
            incrementStarGroup(arrId, pField.get(arrId) - 1, hint);
    }

    Set<Integer> incrementStarGroup(final int arrId, final int num, final Playground pField) {
        Set<Integer> changed = new HashSet<>();
        for (int tempArrId : Sudoku.getStarGroup(arrId)) {
            if (!pField.isPopulated(tempArrId) && !isHint(tempArrId, num)) changed.add(tempArrId);
            hint.increment(tempArrId, num);
        }
        return changed;
    }

    static void incrementStarGroup(final int arrId, final int num, Hint hint) {
        for (int tempArrId : Sudoku.getStarGroup(arrId))
            hint.increment(tempArrId, num);
    }

    void incrementStarGroup(final int arrId, final int num) {
        incrementStarGroup(arrId, num, hint);
    }

    Set<Integer> decrementStarGroup(final int arrId, final int num, final Playground pField) {
        Set<Integer> changed = new HashSet<>();
        for (int tempArrId : Sudoku.getStarGroup(arrId)) {
            hint.decrement(tempArrId, num);
            if (!pField.isPopulated(tempArrId) && !isHint(tempArrId, num)) changed.add(tempArrId);
        }
        return changed;
    }

    static void decrementStarGroup(final int arrId, final int num, Hint hint) {
        for (int tempArrId : Sudoku.getStarGroup(arrId))
            hint.decrement(tempArrId, num);
    }

    void decrementStarGroup(final int arrId, final int num) {
        decrementStarGroup(arrId, num, hint);
    }

    /**
     * if a number is group wise bounded to a specific row/column, in the same row/column other
     * groups get hints
     * @param pField sudoku state object
     * @param getOnly if true no hints get set, instead as soon as a possible new hint is found a
     *                unit set containing the corresponding field id is returned
     * @return a HashSet containing the ids of the changed fields
     */
    Set<Integer> setAutoHintsAdv1(final Playground pField, boolean getOnly) {
        if (hintAdv1 == null) hintAdv1 = new Hint();
        Set<Integer> changed = new HashSet<>();
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
                if (tempArrId != -1)
                    for (int arrId : Sudoku.getComplementVerticalGroup(tempArrId))
                        if (!pField.isPopulated(arrId) && !hintAdv1.isHint(arrId, num)) {
                            if (getOnly) {
                                if (isHint(arrId, num)) continue;
                                changed.add(arrId);
                                return changed;
                            }
                            hintAdv1.increment(arrId, num);
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
                // add hints horizontal
                if (tempArrId != -1)
                    for (int arrId : Sudoku.getComplementHorizontalGroup(tempArrId))
                        if (!pField.isPopulated(arrId) && !hintAdv1.isHint(arrId, num)) {
                            if (getOnly) {
                                if (isHint(arrId, num)) continue;
                                changed.add(arrId);
                                return changed;
                            }
                            changed.add(arrId);
                            hintAdv1.increment(arrId, num);
                        }
            }
        return changed;
    }

    /**
     * If in a group/row/column are n fields with the same n hints or less missing, hints get set on
     * the same numbers on the other fields.
     * @param pField sudoku state object
     * @param getOnly if true no hints get set, instead as soon as a possible new hint is found a
     *                unit set containing the corresponding field id is returned
     * @param useRelaxation a faster version that only finds hints if one field generates a superset
     *                      is used
     * @return a HashSet containing the ids of the changed fields
     */
    Set<Integer> setAutoHintsAdv2(final Playground pField, boolean getOnly, boolean useRelaxation) {
        if (hintAdv2 == null) hintAdv2 = new Hint();
        Set<Integer> changed = new HashSet<>();
        Set<Integer> arrId;
        if (getOnly) for (int i = 0; i < DIM; i++) {
            arrId = (useRelaxation
                    ? setAutoHintsAdv2ByGroupMinus(Sudoku.VERTICAL_GROUPS[i], pField, true)
                    : setAutoHintsAdv2ByGroupPlusNew(Sudoku.VERTICAL_GROUPS[i], pField, true));
            if (!arrId.isEmpty()) return arrId;
            arrId = (useRelaxation
                    ? setAutoHintsAdv2ByGroupMinus(Sudoku.HORIZONTAL_GROUPS[i], pField, true)
                    : setAutoHintsAdv2ByGroupPlusNew(Sudoku.HORIZONTAL_GROUPS[i], pField, true));
            if (!arrId.isEmpty()) return arrId;
            arrId = (useRelaxation
                    ? setAutoHintsAdv2ByGroupMinus(Sudoku.GROUPED_GROUPS[i], pField, true)
                    : setAutoHintsAdv2ByGroupPlusNew(Sudoku.GROUPED_GROUPS[i], pField, true));
            if (!arrId.isEmpty()) return arrId;
        } else for (int i = 0; i < DIM; i++) {
            changed.addAll(useRelaxation
                    ? setAutoHintsAdv2ByGroupMinus(Sudoku.VERTICAL_GROUPS[i], pField, false)
                    : setAutoHintsAdv2ByGroupPlusNew(Sudoku.VERTICAL_GROUPS[i], pField, false));
            changed.addAll(useRelaxation
                    ? setAutoHintsAdv2ByGroupMinus(Sudoku.HORIZONTAL_GROUPS[i], pField, false)
                    : setAutoHintsAdv2ByGroupPlusNew(Sudoku.HORIZONTAL_GROUPS[i], pField, false));
            changed.addAll(useRelaxation
                    ? setAutoHintsAdv2ByGroupMinus(Sudoku.GROUPED_GROUPS[i], pField, false)
                    : setAutoHintsAdv2ByGroupPlusNew(Sudoku.GROUPED_GROUPS[i], pField, false));
        }
        return changed;
    }

    /**
     * If in a group/row/column are n hints witch are missing on the same n fields or less, hints
     * get set on the other numbers on the same n fields.
     * @param pField sudoku state object
     * @param getOnly if true no hints get set, instead as soon as a possible new hint is found a
     *                unit set containing the corresponding field id is returned
     * @param useRelaxation a faster version that only finds hints if one number generates a
     *                      superset is used
     * @return a HashSet containing the ids of the changed fields
     */
    Set<Integer> setAutoHintsAdv3(final Playground pField, boolean getOnly, boolean useRelaxation) {
        if (hintAdv3 == null) hintAdv3 = new Hint();
        Set<Integer> changed = new HashSet<>();
        Set<Integer> arrId;
        if (getOnly) for (int i = 0; i < DIM; i++) {
            arrId = (useRelaxation
                    ? setAutoHintsAdv3ByGroupMinus(Sudoku.VERTICAL_GROUPS[i], pField, true)
                    : setAutoHintsAdv3ByGroupPlusNew(Sudoku.VERTICAL_GROUPS[i], pField, true));
            if (!arrId.isEmpty()) return arrId;
            arrId = (useRelaxation
                    ? setAutoHintsAdv3ByGroupMinus(Sudoku.HORIZONTAL_GROUPS[i], pField, true)
                    : setAutoHintsAdv3ByGroupPlusNew(Sudoku.HORIZONTAL_GROUPS[i], pField, true));
            if (!arrId.isEmpty()) return arrId;
            arrId = (useRelaxation
                    ? setAutoHintsAdv3ByGroupMinus(Sudoku.GROUPED_GROUPS[i], pField, true)
                    : setAutoHintsAdv3ByGroupPlusNew(Sudoku.GROUPED_GROUPS[i], pField, true));
            if (!arrId.isEmpty()) return arrId;
        } else for (int i = 0; i < DIM; i++) {
            changed.addAll(useRelaxation
                    ? setAutoHintsAdv3ByGroupMinus(Sudoku.VERTICAL_GROUPS[i], pField, false)
                    : setAutoHintsAdv3ByGroupPlusNew(Sudoku.VERTICAL_GROUPS[i], pField, false));
            changed.addAll(useRelaxation
                    ? setAutoHintsAdv3ByGroupMinus(Sudoku.HORIZONTAL_GROUPS[i], pField, false)
                    : setAutoHintsAdv3ByGroupPlusNew(Sudoku.HORIZONTAL_GROUPS[i], pField, false));
            changed.addAll(useRelaxation
                    ? setAutoHintsAdv3ByGroupMinus(Sudoku.GROUPED_GROUPS[i], pField, false)
                    : setAutoHintsAdv3ByGroupPlusNew(Sudoku.GROUPED_GROUPS[i], pField, false));
        }
        return changed;
    }

    /**
     * If in a group/row/column are n fields with the same n hints or less missing, hints get set on
     * the same numbers on the other fields.
     * Be careful, this algorithm is a relaxation and only finds the targeted result, if one field
     * generates a superset.
     * @param group array containing the ids of the fields specified by the sudoku grouping
     * @param pField sudoku state object
     * @param getOnly if true no hints get set, instead as soon as a possible new hint is found a
     *                unit set containing the corresponding field id is returned
     * @return a HashSet containing the ids of the changed fields
     */
    private Set<Integer> setAutoHintsAdv2ByGroupMinus(final Integer[] group, final Playground pField,
                                                      boolean getOnly) {
        int populatedFieldsCount;
        Set<Integer> changed = new HashSet<>(8);
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
            if (otherArrIds.size() != 0
                    && notMissing.size() == otherArrIds.size() + populatedFieldsCount)
                for (int tempArrId : otherArrIds)
                    for (int num : missing)
                        if (!hintAdv2.isHint(tempArrId, num)) {
                            if (getOnly) {
                                if (isHint(tempArrId, num)) continue;
                                changed.add(tempArrId);
                                return changed;
                            }
                            changed.add(tempArrId);
                            hintAdv2.increment(tempArrId, num);
                        }
        }
        return changed;
    }

    /**
     * If in a group/row/column are n hints witch are missing on the same n fields or less, hints
     * get set on the other numbers on the same n fields.
     * Be careful, this algorithm is a relaxation and only finds the targeted result, if one number
     * generates a superset.
     * @param group array containing the ids of the fields specified by the sudoku grouping
     * @param pField sudoku state object
     * @param getOnly if true no hints get set, instead as soon as a possible new hint is found a
     *                unit set containing the corresponding field id is returned
     * @return a HashSet containing the ids of the changed fields
     */
    private Set<Integer> setAutoHintsAdv3ByGroupMinus(final Integer[] group, final Playground pField,
                                                      boolean getOnly) {
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
                if (pField.isPopulated(arrId)) populatedFieldsCount++;
                else if (isHint(arrId, num)) notMissing.add(arrId);
                else missing.add(arrId);
            // if all fields miss the hint, we move on
            if (notMissing.size() == 0) continue;
            // Looking at the other numbers
            for (int otherNum = 0; otherNum < DIM; otherNum++) {
                if (otherNum == num) continue;
                // mark all numbers that don't fit in our scheme,
                // meaning they are missing on other fields
                for (int arrId : group)
                    if (!pField.isPopulated(arrId)
                            && !isHint(arrId, otherNum)
                            && notMissing.contains(arrId)) {
                        otherNumbers.add(otherNum);
                        break;
                    }
            }
            // there are numbers to process && missing size equals the count of similar numbers
            if (otherNumbers.size() != 0
                    && missing.size() == DIM - (otherNumbers.size() + populatedFieldsCount))
                // hints get set on the other numbers on the same n fields.
                for (int otherNum : otherNumbers)
                    for (int arrId : missing)
                        if (!hintAdv3.isHint(arrId, otherNum)) {
                            if (getOnly) {
                                if (isHint(arrId, num)) continue;
                                changed.add(arrId);
                                return changed;
                            }
                            changed.add(arrId);
                            hintAdv3.increment(arrId, otherNum);
                        }
        }
        return changed;
    }

    /**
     * Taken from http://stackoverflow.com/questions/1670862/obtaining-a-powerset-of-a-set-in-java
     * Be careful, this algorithm resides in EXPTIME
     * @param originalSet the set to make a powerset from
     * @return the powerset of the original set
     */
    static Set<Set<Integer>> powerSet(final Set<Integer> originalSet) {
        Set<Set<Integer>> sets = new HashSet<>();
        if (originalSet.isEmpty()) {
            sets.add(new HashSet<Integer>());
            return sets;
        }
        List<Integer> list = new ArrayList<>(originalSet);
        Integer head = list.get(0);
        Set<Integer> rest = new HashSet<>(list.subList(1, list.size()));
        for (Set<Integer> set : powerSet(rest)) {
            Set<Integer> newSet = new HashSet<>();
            newSet.add(head);
            newSet.addAll(set);
            sets.add(newSet);
            sets.add(set);
        }
        return sets;
    }

    /**
     * If in a group/row/column are n fields with the same n hints or less missing, hints get set on
     * the same numbers on the other fields.
     * Be careful, this algorithm resides in EXPTIME.
     * @param pField sudoku state object
     * @param getOnly if true no hints get set, instead as soon as a possible new hint is found a
     *                unit set containing the corresponding field id is returned
     * @return a HashSet containing the ids of the changed fields
     */
    private Set<Integer> setAutoHintsAdv2ByGroupPlusOld(final Integer[] group, final Playground pField,
                                                     boolean getOnly) {
        Set<Integer> changed = new HashSet<>();
        @SuppressLint("UseSparseArrays")
        Map<Integer, Set<Integer>> missing = new HashMap<>();
        Set<Integer> fields = new HashSet<>();
        for (int arrId : group) {
            missing.put(arrId, new HashSet<Integer>());
            for (int num = 0; num < DIM; num++)
                if (!pField.isPopulated(arrId) && !isHint(arrId, num)) {
                    missing.get(arrId).add(num);
                    fields.add(arrId);
                }
        }
        for (Set<Integer> subset : powerSet(fields)) {
            if (subset.size() <= 1 || subset.size() == fields.size()) continue;
            Set<Integer> distributedOverNumbers = new HashSet<>();
            for (int arrId : subset)
                distributedOverNumbers.addAll(missing.get(arrId));
            if (distributedOverNumbers.size() > subset.size()) continue;
            for (int arrId : group)
                if (!subset.contains(arrId))
                    for (int num : distributedOverNumbers)
                        if (!hintAdv2.isHint(arrId, num)) {
                            if (getOnly) {
                                if (isHint(arrId, num)) continue;
                                changed.add(arrId);
                                return changed;
                            }
                            changed.add(arrId);
                            hintAdv2.increment(arrId, num);
                        }
        }
        return changed;
    }

    /**
     * If in a group/row/column are n hints witch are missing on the same n fields or less, hints
     * get set on the other numbers on the same n fields.
     * Be careful, this algorithm resides in EXPTIME
     * @param group array containing the ids of the fields specified by the sudoku grouping
     * @param pField sudoku state object
     * @param getOnly if true no hints get set, instead as soon as a possible new hint is found a
     *                unit set containing the corresponding field id is returned
     * @return a HashSet containing the ids of the changed fields
     */
    private Set<Integer> setAutoHintsAdv3ByGroupPlusOld(final Integer[] group, final Playground pField,
                                                     boolean getOnly) {
        Set<Integer> changed = new HashSet<>();
        @SuppressLint("UseSparseArrays")
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
        for (Set<Integer> subset : powerSet(fields)) {
            if (subset.size() <= 1 || subset.size() == fields.size()) continue;
            Set<Integer> distributedOverFields = new HashSet<>();
            for (int num : subset)
                distributedOverFields.addAll(missing.get(num));
            if (distributedOverFields.size() > subset.size()) continue;
            for (int num = 0; num < DIM; num++)
                if (!subset.contains(num))
                    for (int arrId : distributedOverFields)
                        if (!hintAdv3.isHint(arrId, num)) {
                            if (getOnly) {
                                if (isHint(arrId, num)) continue;
                                changed.add(arrId);
                                return changed;
                            }
                            changed.add(arrId);
                            hintAdv3.increment(arrId, num);
                        }
        }
        return changed;
    }

    /**
     * If in a group/row/column are n fields with the same n hints or less missing, hints get set on
     * the same numbers on the other fields.
     * Be careful, this algorithm resides in EXPTIME
     * @param group array containing the ids of the fields specified by the sudoku grouping
     * @param pField sudoku state object
     * @param getOnly if true no hints get set, instead as soon as a possible new hint is found a
     *                unit set containing the corresponding field id is returned
     * @return a HashSet containing the ids of the changed fields
     */
    private Set<Integer> setAutoHintsAdv2ByGroupPlusNew(final Integer[] group, final Playground pField,
                                                        final boolean getOnly) {
        class CallbackAdv2 implements Callback {
            Map<Integer, Set<Integer>> elements;
            Integer[] group;
            Playground pField;
            boolean getOnly;

            CallbackAdv2(final Integer[] group, final Playground pField, final boolean getOnly) {
                this.group = group;
                this.pField = pField;
                this.getOnly = getOnly;
            }

            public void init(final Map<Integer, Set<Integer>> elements) {
                this.elements = elements;
            }

            @Override
            public Set<Integer> invokeFunction(Set<Integer> coverKeys) {
                Set<Integer> changed = new HashSet<>();
                Set<Integer> distributedOverFields = new HashSet<>();
                for (int key : coverKeys) distributedOverFields.addAll(elements.get(key));
                for (int arrId : group) {
                    if (!pField.isPopulated(arrId) && !coverKeys.contains(arrId))
                        for (int num : distributedOverFields)
                            if (!hintAdv2.isHint(arrId, num)) {
                                if (getOnly) {
                                    if (isHint(arrId, num)) continue;
                                    changed.add(arrId);
                                    return changed;
                                }
                                changed.add(arrId);
                                hintAdv2.increment(arrId, num);
                            }
                }
                return changed.isEmpty() ? null : changed;
            }
        }
        CallbackAdv2 callback = new CallbackAdv2(group, pField, getOnly);
        Set<Integer> changed;
        @SuppressLint("UseSparseArrays")
        Map<Integer, Set<Integer>> missing = new HashMap<>();
        int populated = 0;
        for (int arrId : group) {
            if (pField.isPopulated(arrId)) populated++;
            else {
                missing.put(arrId, new HashSet<Integer>());
                for (int num = 0; num < DIM; num++)
                    if (!isHint(arrId, num))
                        missing.get(arrId).add(num);
                if (missing.get(arrId).size() == DIM)
                    missing.remove(arrId);
            }
        }
        callback.init(missing);
        changed = searchSetCover(missing, (DIM - populated) - 1, 0, new HashSet<Integer>(), callback);
        if (changed != null) return changed;
        return new HashSet<>();
    }

    /**
     * If in a group/row/column are n hints witch are missing on the same n fields or less, hints
     * get set on the other numbers on the same n fields.
     * Be careful, this algorithm resides in EXPTIME
     * @param group array containing the ids of the fields specified by the sudoku grouping
     * @param pField sudoku state object
     * @param getOnly if true no hints get set, instead as soon as a possible new hint is found a
     *                unit set containing the corresponding field id is returned
     * @return a HashSet containing the ids of the changed fields
     */
    private Set<Integer> setAutoHintsAdv3ByGroupPlusNew(final Integer[] group, final Playground pField,
                                                        final boolean getOnly) {
        class CallbackAdv3 implements Callback {
            Map<Integer, Set<Integer>> elements;
            Integer[] group;
            Playground pField;
            boolean getOnly;

            CallbackAdv3(final Integer[] group, final Playground pField, final boolean getOnly) {
                this.group = group;
                this.pField = pField;
                this.getOnly = getOnly;
            }

            public void init(final Map<Integer, Set<Integer>> elements) {
                this.elements = elements;
            }

            @Override
            public Set<Integer> invokeFunction(Set<Integer> coverKeys) {
                Set<Integer> changed = new HashSet<>();
                Set<Integer> distributedOverFields = new HashSet<>();
                for (int key : coverKeys) distributedOverFields.addAll(elements.get(key));
                for (int arrId : group) {
                    if (!pField.isPopulated(arrId) && !distributedOverFields.contains(arrId))
                        for (int key : coverKeys)
                            if (!hintAdv3.isHint(arrId, key)) {
                                if (getOnly) {
                                    if (isHint(arrId, key)) continue;
                                    //Log.v("HINTSADV3",arrId+","+key);
                                    changed.add(arrId);
                                    return changed;
                                }
                                changed.add(arrId);
                                hintAdv3.increment(arrId, key);
                            }
                }
                return changed.isEmpty()?null:changed;
            }
        }
        CallbackAdv3 callback = new CallbackAdv3(group, pField, getOnly);
        //Log.v("HINTSADV3-group",Arrays.toString(group));
        Set<Integer> changed;
        @SuppressLint("UseSparseArrays")
        Map<Integer, Set<Integer>> missing = new HashMap<>();
        int populated = 0;
        for (int arrId : group) if (pField.isPopulated(arrId)) populated++;
        for (int num = 0; num < DIM; num++) {
            missing.put(num, new HashSet<Integer>());
            for (int arrId : group)
                if (!pField.isPopulated(arrId) && !isHint(arrId, num))
                    missing.get(num).add(arrId);
            if (missing.get(num).size() == 0 || missing.get(num).size() == DIM - populated) missing.remove(num);
        }
        callback.init(missing);
        changed = searchSetCover(missing, (DIM - populated) - 1, 0, new HashSet<Integer>(), callback);
        if (changed != null) return changed;
        return new HashSet<>();
    }

    /**
     * In contrast to the algorithm using a superset this algorithm discards impractical subsets
     * Be careful, this algorithm resides in EXPTIME
     * @param elements a collection of sets that may fit, the size of these sets need to be
     *                 <= maxCoverSize
     * @param maxCoverSize how many set elements the cover is allowed to contain
     * @param coverSizeCaller how many set elements that are contained in the cover need to be found
     * @param coverKeysCaller the coverKeys the caller works with, the initial caller need to
     *                        supply an empty set
     * @param callback the object that handles the callback function
     * @return the return set of the callback function, if there is a collection of sets that are
     *         contained in a cover of coverSize and the cardinality of the collection is
     *         >= minCountElements and the callback function does not evaluate to null,
     *         null otherwise
     */
    @SuppressLint("UseSparseArrays")
    private Set<Integer> searchSetCover(Map<Integer, Set<Integer>> elements, final int maxCoverSize,
                                        final int coverSizeCaller, Set<Integer> coverKeysCaller,
                                        final Callback callback) {
        int subclassMaxCoverSize, subclassCoverSize;
        Set<Integer> foundKeys, returnCallback;
        Map<Integer, Set<Integer>> subclassElements;
        for (int key : elements.keySet()) {
            // the cover will contain all elements of elm(key)
            subclassMaxCoverSize = maxCoverSize - elements.get(key).size();
            subclassCoverSize = coverSizeCaller + elements.get(key).size();
            // we don't need to check if subclassMaxCoverSize < 0,
            // since elements.get(key).size() <= maxCoverSize by contract;
            subclassElements = new HashMap<>();
            foundKeys = new HashSet<>(coverKeysCaller);
            foundKeys.add(key);
            for (int tempKey : elements.keySet()) {
                // we look only at the other elements
                if (tempKey == key) continue;
                // holds all elements in elm(tempKey) that are not in elm(key)
                Set<Integer> newSet = new HashSet<>();
                for (int value : elements.get(tempKey))
                    if (!elements.get(key).contains(value))
                        newSet.add(value);
                if (newSet.isEmpty()) {
                    foundKeys.add(tempKey);
                    if (foundKeys.size() >= subclassCoverSize) {
                        returnCallback = callback.invokeFunction(foundKeys);
                        // the solution is feasible and was processed, tell the method to die
                        if (returnCallback != null) return returnCallback;
                        // the solution is not feasible, we need to go on with the next key
                    }
                } else if (newSet.size() <= subclassMaxCoverSize) subclassElements.put(tempKey, newSet);
            }
            // we search the subclasses (equal would only be possible, if subclassElements would
            // contain empty sets, but they are represented in foundKeys already)
            if (subclassElements.size() > subclassCoverSize - foundKeys.size()) {
                foundKeys = searchSetCover(subclassElements, subclassMaxCoverSize, subclassCoverSize,
                        foundKeys, callback);
                // a feasible solution was found and processed, tell the method to die
                if (foundKeys != null) return foundKeys;
            }
        }
        return null;
    }

    @SuppressWarnings("CloneDoesntCallSuperClone")
    @Override
    public Hints clone() {
        Hints clone = new Hints(usePlain, useAdv1, useAdv2,useAdv3, useUserHint);
        if (hint != null) clone.hint = new Hint(hint);
        if (hintAdv1 != null) clone.hintAdv1 = new Hint(hintAdv1);
        if (hintAdv2 != null) clone.hintAdv2 = new Hint(hintAdv2);
        if (hintAdv3 != null) clone.hintAdv3 = new Hint(hintAdv3);
        if (userHint != null) clone.userHint = new Hint(userHint);
        return clone;
    }
}
