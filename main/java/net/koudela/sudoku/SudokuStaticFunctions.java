package net.koudela.sudoku;

import android.util.Log;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class SudokuStaticFunctions extends SudokuGroups {
    public static int[] getNewEmptyGrid() {
        int[] emptyGrid = new int[DIM * DIM];
        for (int arrId : ALL_ARR_IDS) emptyGrid[arrId] = 0;
        return emptyGrid;
    }

    public static int[][] getNewEmptyHints() {
        int[][] hints = new int[DIM * DIM][DIM];
        return initHints(hints);
    }

    public static int[][] getNewPopulatedHints(int[] sudoku) {
        int[][] hints = getNewEmptyHints();
        for (int arrId : ALL_ARR_IDS) if (sudoku[arrId] != 0) for (int tempArrId : getStarGroup(arrId)) hints[tempArrId][sudoku[arrId] - 1]++;
        return hints;
    }

    public static int[][] initHints(int[][] hints) {
        for (int arrId : ALL_ARR_IDS) for (int number = 0; number < DIM; number++) hints[arrId][number] = 0;
        return hints;
    }

    public static boolean isHint(int num, int arrId, int[][] hints, int[][] hintsAdv1, int[][] hintsAdv2,  int[][] hintsAdv3) {
        return (hints[arrId][num] > 0
                || hintsAdv1[arrId][num] > 0
                || hintsAdv2[arrId][num] > 0
                || hintsAdv3[arrId][num] > 0
        );
    }

    public static String hintsToString(int[][] hints) {
        String hintsArr = "[\n";
        for (int number = 0; number < DIM; number++) {
            hintsArr += "[";
            for  (int arrId : ALL_ARR_IDS) hintsArr += hints[arrId][number] + ", ";
            hintsArr += "]\n";
        }
        return hintsArr + "];";
    }

    public static String sudokuToString(int[] sudoku) {
        String sudokuString = "";
        for (int i = 0; i < DIM; i++) {
            sudokuString += "\n";
            for (int j = 0; j < DIM; j++) sudokuString += sudoku[i * DIM + j] + " ";
        }
        return sudokuString;
    }

    public static Integer[] getRandomizedArrIds() {
        Integer[] arrIds = new Integer[DIM * DIM];
        for (int arrId = 0; arrId < DIM * DIM; arrId++) arrIds[arrId] = arrId;
        Collections.shuffle(Arrays.asList(arrIds));
        return arrIds;
    }

    protected static boolean isAutoInsert1Solvable(int arrId, int[][] hints) {
        for (int number = 0; number < DIM; number++) if (hints[arrId][number] == 0) return false;
        return true;
    }

    // a level one sudoku gets constructed by solely using AutoInsert1 logic
    public static int[] makeLevelOneSudoku(int[] solution) {
        int[] sudoku = getNewEmptyGrid();
        int[][] hints = getNewEmptyHints();
        for (int arrId : ALL_ARR_IDS) sudoku[arrId] = solution[arrId];
        for (int arrId : ALL_ARR_IDS) for (int tempArrId : getStarGroup(arrId)) hints[tempArrId][sudoku[arrId] - 1]++;
        for (int arrId : getRandomizedArrIds()) if (isAutoInsert1Solvable(arrId, hints)) {
            Log.v("removed", arrId + " (" + sudoku[arrId] + ")");
            for (int tempArrId : getStarGroup(arrId)) hints[tempArrId][sudoku[arrId] - 1]--;
            sudoku[arrId] = 0;
        }
        return sudoku;
    }

    protected static boolean isAutoInsert2Solvable(int arrId, int[][] hints, int[] sudoku) {
        return (isAutoInsert2SolvableSub(getVerticalGroup(arrId), arrId, hints, sudoku)
                || isAutoInsert2SolvableSub(getHorizontalGroup(arrId), arrId, hints, sudoku)
                || isAutoInsert2SolvableSub(getGroupedGroup(arrId), arrId, hints, sudoku));
    }

    protected static boolean isAutoInsert2SolvableSub(Set<Integer> group, int arrId, int[][] hints, int[] sudoku) {
        for (int tempArrId : group) if (sudoku[tempArrId] == 0 && hints[tempArrId][sudoku[arrId] - 1] == 1) return false;
        return true;
    }

    // a level two sudoku gets constructed by using AutoInsert1 and AutoInsert2 logic
    public static int[] makeLevelTwoSudoku(int[] levelOneSudoku) {
        int[] sudoku = getNewEmptyGrid();
        int[][] hints = getNewEmptyHints();
        for (int arrId : ALL_ARR_IDS) sudoku[arrId] = levelOneSudoku[arrId];
        for (int arrId : ALL_ARR_IDS) for (int tempArrId : getStarGroup(arrId)) if (sudoku[arrId] != 0) hints[tempArrId][sudoku[arrId] - 1]++;
        for (int arrId : getRandomizedArrIds()) {
            if (sudoku[arrId] == 0) continue;
            if (isAutoInsert2Solvable(arrId, hints, sudoku)) {
                Log.v("removed", arrId + " (" + sudoku[arrId] + ")");
                for (int tempArrId : getStarGroup(arrId)) hints[tempArrId][sudoku[arrId] - 1]--;
                sudoku[arrId] = 0;
            }
        }
        return sudoku;
    }

    // a level three sudoku is solvable by using AutoInsert1, AutoInsert2
    public static int[] makeLevelThreeSudoku(int[] levelTwoSudoku) {
        int[] sudoku = getNewEmptyGrid();
        int[][] hints = getNewEmptyHints();
        for (int arrId : ALL_ARR_IDS) sudoku[arrId] = levelTwoSudoku[arrId];
        for (int arrId : ALL_ARR_IDS) for (int tempArrId : getStarGroup(arrId)) if (sudoku[arrId] != 0) hints[tempArrId][sudoku[arrId] - 1]++;
        for (int arrId : getRandomizedArrIds()) {
            if (sudoku[arrId] == 0) continue;
            if (isSolvableSudoku(arrId, sudoku, true, true, false, false, false)) {
                Log.v("removed", arrId + " (" + sudoku[arrId] + ")");
                for (int tempArrId : getStarGroup(arrId)) hints[tempArrId][sudoku[arrId] - 1]--;
                sudoku[arrId] = 0;
            }
        }
        return sudoku;
    }

    // a level four sudoku is solvable by using AutoInsert1, AutoInsert2, AutoHintAdv1, AutoHintAdv3
    public static int[] makeLevelFourSudoku(int[] levelThreeSudoku) {
        Log.v("makeLevelFourSudoku","start");
        int[] sudoku = getNewEmptyGrid();
        int[][] hints = getNewEmptyHints();
        for (int arrId : ALL_ARR_IDS) sudoku[arrId] = levelThreeSudoku[arrId];
        for (int arrId : ALL_ARR_IDS) for (int tempArrId : getStarGroup(arrId)) if (sudoku[arrId] != 0) hints[tempArrId][sudoku[arrId] - 1]++;
        for (int arrId : getRandomizedArrIds()) {
            if (sudoku[arrId] == 0) continue;
            if (isSolvableSudoku(arrId, sudoku, true, true, true, true, true)) {
                Log.v("removed", arrId + " (" + sudoku[arrId] + ")");
                for (int tempArrId : getStarGroup(arrId)) hints[tempArrId][sudoku[arrId] - 1]--;
                sudoku[arrId] = 0;
            }
        }
        return sudoku;
    }

    // if exact 8 hints are displayed it returns the missing number, null otherwise
    public static int getAutoInsert1ByField(int arrId, int[][] hints, int[][] hintsAdv1,  int[][] hintsAdv2,  int[][] hintsAdv3, int[] sudoku) {
        if (sudoku[arrId] != 0) return 0;
        int number = 0;
        for (int num = 0; num < DIM; num++) if (hints[arrId][num] == 0 && hintsAdv1[arrId][num] == 0 && hintsAdv2[arrId][num] == 0 && hintsAdv3[arrId][num] == 0) {
            if (number != 0) return 0;
            else number = num + 1;
        }
        return number;
    }

    // if a number is missing only once in 9 hint fields vertical, horizontal or group wise it gets returned
    // a populated field counts as a field with all possible 9 hints set
    // second returned int is the corresponding arrId
    // if nothing is found null gets returned
    public static Integer[] getAutoInsert2ByGroup(Integer[] group, int[][] hints, int[][] hintsAdv1, int[][] hintsAdv2,  int[][] hintsAdv3,  int[] sudoku) {
        for (int num = 1; num <= DIM; num++) {
            int count = 0;
            int tempArrId = -1;
            for (int arrId : group) {
                if (sudoku[arrId] != 0 || hints[arrId][num - 1] > 0 || hintsAdv1[arrId][num - 1] > 0 || hintsAdv2[arrId][num - 1] > 0 || hintsAdv3[arrId][num - 1] > 0) count++;
                else if (tempArrId == -1) tempArrId = arrId;
                else break;
            }
            if (count == 8) return new Integer[]{num, tempArrId};
        }
        return null;
    }

    public static Integer[] getAutoInsert2(int[][] hints, int[][] hintsAdv1,  int[][] hintsAdv2,  int[][] hintsAdv3, int[] sudoku) {
        for (int i = 0; i < DIM; i++) {
            Integer[] ai2arr = getAutoInsert2ByGroup(VERTICAL_GROUPS[i], hints, hintsAdv1, hintsAdv2, hintsAdv3, sudoku);
            if (ai2arr != null) return ai2arr;
            ai2arr = getAutoInsert2ByGroup(HORIZONTAL_GROUPS[i], hints, hintsAdv1, hintsAdv2, hintsAdv3, sudoku);
            if (ai2arr != null) return ai2arr;
            ai2arr = getAutoInsert2ByGroup(GROUPED_GROUPS[i], hints, hintsAdv1, hintsAdv2, hintsAdv3, sudoku);
            if (ai2arr != null) return ai2arr;
        }
        return null;
    }

    // if a number is group wise bounded to a specific row/column, in the same row/column other groups get hints
    public static int getAutoHintsAdv1(int[][] hints, int[][] hintsAdv1,  int[][] hintsAdv2,  int[][] hintsAdv3, int[] sudoku) {
        int changed = 0;
        for (int number = 1; number <= DIM; number++)
            for (int cnt = 0; cnt < DIM; cnt++) {
                // check vertical
                int tempArrId = -1;
                for (int arrId : GROUPED_GROUPS[cnt])
                    if (sudoku[arrId] == 0 && !isHint(number - 1, arrId, hints, hintsAdv1, hintsAdv2, hintsAdv3))
                        if (tempArrId == -1) tempArrId = arrId;
                        else if (arrId != tempArrId + 1 && arrId != tempArrId + 2) {
                            tempArrId = -1;
                            break;
                        }
                // add hints vertical
                if (tempArrId != -1) {
                    for (int arrId : getComplementVerticalGroup(tempArrId)) if (hintsAdv1[arrId][number - 1] == 0) {
                        hintsAdv1[arrId][number - 1]++;
                        changed++;
                    }
                }
                // check horizontal
                tempArrId = -1;
                for (int arrId : GROUPED_GROUPS[cnt])
                    if (sudoku[arrId] == 0 && !isHint(number - 1, arrId, hints, hintsAdv1, hintsAdv2, hintsAdv3))
                        if (tempArrId == -1) tempArrId = arrId;
                        else if (arrId != tempArrId + 9 && arrId != tempArrId + 18) {
                            tempArrId = -1;
                            break;
                        }
                // add hints vertical
                if (tempArrId != -1) {
                    for (int arrId : getComplementHorizontalGroup(tempArrId)) if (hintsAdv1[arrId][number -1] == 0) {
                        hintsAdv1[arrId][number - 1]++;
                        changed++;
                    }
                }
            }
        return changed;
    }

    // if in a group/row/column are n fields with the same n hints or less missing, hints get set on the same numbers on the other fields
    public static int getAutoHintsAdv2ByGroup(Integer[] group, int[][] hints, int[][] hintsAdv1, int[][] hintsAdv2,  int[][] hintsAdv3,  int[] sudoku) {
        int changed = 0;
        for (int arrId : group) {
            if (sudoku[arrId] != 0) continue;
            Set<Integer> missing = new LinkedHashSet<>();
            Set<Integer> notMissing = new LinkedHashSet<>();
            Set<Integer> otherArrIds = new LinkedHashSet<>();
            for (int num = 0; num < DIM; num++)
                if (isHint(num, arrId, hints, hintsAdv1, hintsAdv2, hintsAdv3)) notMissing.add(num);
                else missing.add(num);
            if (missing.size() == 0 || notMissing.size() == 0) continue;
            int populatedFieldsCount = 0;
            for (int tempArrIds : group) {
                if (tempArrIds == arrId) continue;
                if (sudoku[arrId] != 0) {
                    populatedFieldsCount++;
                    continue;
                }
                if (notMissing.size() < otherArrIds.size()) break;
                for (int num = 0; num < DIM; num++)
                    if (!isHint(num, tempArrIds, hints, hintsAdv1, hintsAdv2, hintsAdv3) && notMissing.contains(num)) {
                        otherArrIds.add(tempArrIds);
                        break;
                    }
            }
            if (notMissing.size() == otherArrIds.size() + populatedFieldsCount) changed += getAutoHintsAdv2ByGroupSub(missing, otherArrIds, hintsAdv2);
        }
        return changed;
    }

    protected static int getAutoHintsAdv2ByGroupSub(Set<Integer> missing, Set<Integer> otherArrIds, int[][] hintsAdv2) {
        int changed = 0;
        for (int arrId : otherArrIds) for (int num : missing) if (hintsAdv2[arrId][num] == 0) {
            hintsAdv2[arrId][num]++;
            changed++;
        }
        return changed;
    }

    public static int getAutoHintsAdv2(int[][] hints, int[][] hintsAdv1, int[][] hintsAdv2,  int[][] hintsAdv3,  int[] sudoku) {
        int changed = 0;
        for (int i = 0; i < DIM; i++) {
            changed += getAutoHintsAdv2ByGroup(VERTICAL_GROUPS[i], hints, hintsAdv1, hintsAdv2, hintsAdv3, sudoku);
            changed += getAutoHintsAdv2ByGroup(HORIZONTAL_GROUPS[i], hints, hintsAdv1, hintsAdv2, hintsAdv3, sudoku);
            changed += getAutoHintsAdv2ByGroup(GROUPED_GROUPS[i], hints, hintsAdv1, hintsAdv2, hintsAdv3, sudoku);
        }
        return changed;
    }

    // be careful, this algorithm resides in EXPTIME
    public static Set<Set<Integer>> powerSet(Set<Integer> originalSet) {
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

    // if in a group/row/column are n missing hints distributed over n fields only, then hints get set on the other numbers on the same n fields
    // be careful, this algorithm resides in EXPTIME
    public static int getAutoHintsAdv3ByGroup(Integer[] group, int[][] hints, int[][] hintsAdv1, int[][] hintsAdv2, int[][] hintsAdv3, int[] sudoku) {
        int changed = 0;
        Map<Integer, Set<Integer>> missing = new LinkedHashMap<>();
        Set<Integer> fields = new HashSet<>();
        for (int num = 0; num < DIM; num++) {
            missing.put(num, new LinkedHashSet<Integer>());
            for (int arrId : group)
                if (sudoku[arrId] == 0 && !isHint(num, arrId, hints, hintsAdv1, hintsAdv2, hintsAdv3)) {
                    missing.get(num).add(arrId);
                    fields.add(num);
                }
        }
        for (Set<Integer> subset : Sudoku.powerSet(fields)) {
            if (subset.size() <= 1 || subset.size() == fields.size()) continue;
            Set<Integer> distributedOverFields = new HashSet<>();
            for (int num : subset) {
                distributedOverFields.addAll(missing.get(num));
            }
            if (distributedOverFields.size() > subset.size()) continue;
            for (int num = 0; num < DIM; num++) {
                if (!subset.contains(num))
                    for (int arrId : distributedOverFields)
                        if (hintsAdv3[arrId][num] == 0) {
                            hintsAdv3[arrId][num]++;
                            changed++;
                        }
            }
        }
        return changed;
    }

    public static int getAutoHintsAdv3(int[][] hints, int[][] hintsAdv1, int[][] hintsAdv2, int[][] hintsAdv3,  int[] sudoku) {
        int changed = 0;
        for (int i = 0; i < DIM; i++) {
            changed += getAutoHintsAdv3ByGroup(VERTICAL_GROUPS[i], hints, hintsAdv1, hintsAdv2, hintsAdv3, sudoku);
            changed += getAutoHintsAdv3ByGroup(HORIZONTAL_GROUPS[i], hints, hintsAdv1, hintsAdv2, hintsAdv3,  sudoku);
            changed += getAutoHintsAdv3ByGroup(GROUPED_GROUPS[i], hints, hintsAdv1, hintsAdv2, hintsAdv3, sudoku);
        }
        return changed;
    }

    public static int[] solveSudoku(int deletionAtArrId, int[] sudoku, boolean byAutoInsert1, boolean byAutoInsert2, boolean byAutoHintAdv1, boolean byAutoHintAdv2, boolean byAutoHintAdv3) {
        int[] solution = getNewEmptyGrid();
        Set<Integer> emptyArrIds = Collections.newSetFromMap(new ConcurrentHashMap<Integer, Boolean>());
        for (int arrId : ALL_ARR_IDS)
            if (sudoku[arrId] != 0) solution[arrId] = sudoku[arrId];
            else emptyArrIds.add(arrId);
        if (deletionAtArrId > -1) {
            emptyArrIds.add(deletionAtArrId);
            solution[deletionAtArrId] = 0;
        }
        int hints[][] = getNewPopulatedHints(solution);
        int hintsAdv1[][] = getNewEmptyHints();
        int hintsAdv2[][] = getNewEmptyHints();
        int hintsAdv3[][] = getNewEmptyHints();
        // hints don't need to be deleted in this method, => we don't care about
        // initHints(hintsAdv1);
        // initHints(hintsAdv2);
        // initHints(hintsAdv3);

        int newHintsFound;
        boolean changedSolution;
        int changedHints = 0;
        do {
            changedSolution = false;
            if (byAutoInsert1) for (int arrId : emptyArrIds) {
                int number = getAutoInsert1ByField(arrId, hints, hintsAdv1, hintsAdv2, hintsAdv3, sudoku);
                if (number > 0) {
                    solution[arrId] = number;
                    for (int tempArrId : getStarGroup(arrId)) hints[tempArrId][number - 1]++;
                    emptyArrIds.remove(arrId);
                    Log.v("solved by AI1", ""+arrId);
                    changedSolution = true;
                }
            }
            if (!changedSolution && byAutoInsert2) {
                Integer[] ai2arr = getAutoInsert2(hints, hintsAdv1, hintsAdv2, hintsAdv3, solution);
                if (ai2arr != null) {
                    solution[ai2arr[1]] = ai2arr[0];
                    for (int tempArrId : getStarGroup(ai2arr[1])) hints[tempArrId][ai2arr[0] - 1]++;
                    emptyArrIds.remove(ai2arr[1]);
                    Log.v("solved by AI2", ""+ai2arr[1]);
                    changedSolution = true;
                }
            }
            if (!changedSolution) {
                //Log.v("search new hints", "...");
                if (byAutoHintAdv1) {
                    newHintsFound = getAutoHintsAdv1(hints, hintsAdv1, hintsAdv2, hintsAdv3, solution);
                    if (newHintsFound != 0) Log.v("hintsAdv1", ""+newHintsFound);
                    changedHints = newHintsFound;
                }
                if (byAutoHintAdv2) {
                    newHintsFound = getAutoHintsAdv2(hints, hintsAdv1, hintsAdv2, hintsAdv3, solution);
                    changedHints += newHintsFound;
                    if (newHintsFound != 0) Log.v("hintsAdv2", ""+newHintsFound);
                }
                if (byAutoHintAdv3) {
                    newHintsFound = getAutoHintsAdv3(hints, hintsAdv1, hintsAdv2, hintsAdv3, solution);
                    changedHints += newHintsFound;
                    if (newHintsFound != 0) Log.v("hintsAdv3", ""+newHintsFound);
                }
            }
        } while(changedSolution || changedHints > 0);
        return solution;
    }

    public static boolean isSolvableSudoku(int arrId, int[] sudoku, boolean byAutoInsert1, boolean byAutoInsert2, boolean byAutoHintAdv1, boolean byAutoHintAdv2, boolean byAutoHintAdv3) {
        return isTrueGrid(solveSudoku(arrId, sudoku, byAutoInsert1, byAutoInsert2, byAutoHintAdv1, byAutoHintAdv2, byAutoHintAdv3));
    }

    public static boolean isTrueGrid(int[] sudoku) {
        int[][] hints = getNewEmptyHints();
        for (int arrId : ALL_ARR_IDS) {
            if (sudoku[arrId] == 0) return false;
            for (int tempArrId : getStarGroup(arrId)) hints[tempArrId][sudoku[arrId] - 1]++;
        }
        for (int arrId : ALL_ARR_IDS) if (hints[arrId][sudoku[arrId] - 1] != 1) return false;
        return true;
    }
}
