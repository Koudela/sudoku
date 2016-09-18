package net.koudela.sudoku;

import android.util.Log;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Playground {
    private static final int DIM = Sudoku.DIM;
    private int[] pField = new int[DIM * DIM];
    private Set<Integer> populatedArrIds = new HashSet<>(128);
    private Set<Integer> notPopulatedArrIds = new HashSet<>(128);

    public Playground() {
        init();
    }

    public Playground(final int[] clone) {
        init(clone);
    }

    public Playground(final Playground clone) {
        init(clone);
    }

    public void init() {
        populatedArrIds.clear();
        notPopulatedArrIds.clear();
        for (int arrId : Sudoku.ALL_ARR_IDS) {
            notPopulatedArrIds.add(arrId);
            pField[arrId] = 0;
        }
    }

    public void init(final int[] cloneField) {
        populatedArrIds.clear();
        notPopulatedArrIds.clear();
        for (int arrId : Sudoku.ALL_ARR_IDS) {
            if (cloneField[arrId] < 0 || cloneField[arrId] > DIM)
                throw new IllegalArgumentException("value must be within [0, " + DIM + "]; value was " + cloneField[arrId]);
            pField[arrId] = cloneField[arrId];
            if (cloneField[arrId] != 0) populatedArrIds.add(arrId);
            else notPopulatedArrIds.add(arrId);
        }
    }

    public void init(final Playground clone) {
        populatedArrIds.clear();
        notPopulatedArrIds.clear();
        for (int arrId : Sudoku.ALL_ARR_IDS) {
            pField[arrId] = clone.get(arrId);
            if (clone.isPopulated(arrId)) populatedArrIds.add(arrId);
            else notPopulatedArrIds.add(arrId);
        }
    }

    public int get(final int arrId) {
        return pField[arrId];
    }

    public void set(final int arrId, final int val) {
        if (val < 0 || val > DIM)
            throw new IllegalArgumentException("val must be within [0, " + DIM + "]; val was " + val);
        if (pField[arrId] == val) return;
        if (val == 0) {
            populatedArrIds.remove(arrId);
            notPopulatedArrIds.add(arrId);
        } else {
            notPopulatedArrIds.remove(arrId);
            populatedArrIds.add(arrId);
        }
        pField[arrId] = val;
    }

    public int[] getPField() {
        return pField.clone();
    }

    public boolean isPopulated(final int arrId) {
        return pField[arrId] != 0;
    }

    public int getSizePopulatedArrIds() {
        return populatedArrIds.size();
    }

    public int getSizeNotPopulatedArrIds() {
        return notPopulatedArrIds.size();
    }

    public Set<Integer> getPopulatedArrIds() {
        return Collections.unmodifiableSet(populatedArrIds);
    }

    public Set<Integer> getNotPopulatedArrIds() {
        return Collections.unmodifiableSet(notPopulatedArrIds);
    }

    // if exact 8 hints are displayed it returns the missing number, null otherwise
    public int getAutoInsert1ByField(final int arrId, final Hints hints) {
        if (isPopulated(arrId)) return 0;
        int number = 0;
        for (int num = 0; num < DIM; num++)
            if (!hints.isHint(arrId, num)) {
                if (number != 0) return 0;
                else number = num + 1;
            }
        return number;
    }

    public int autoInsert1ByField(final int arrId, Hints hints, final boolean verbose) {
        int number = getAutoInsert1ByField(arrId, hints);
        if (number == 0) return 0;
        set(arrId, number);
        hints.incrementStarGroup(arrId, number - 1);
        if (verbose) Log.v("insert by AI1", arrId + " (" + number + ")");
        return number;
    }

    public Integer[] getAutoInsert1byStarGroup(final int arrId, final Hints hints) {
        int number;
        for (int tempArrId: Sudoku.getStarGroup(arrId)) {
            number = getAutoInsert1ByField(tempArrId, hints);
            if (number != 0) return new Integer[]{tempArrId, number};
        }
        return null;
    }

    public Integer[] getAutoInsert1(final Hints hints) {
        int number;
        for (int arrId : notPopulatedArrIds) {
            number = getAutoInsert1ByField(arrId, hints);
            if (number != 0) return new Integer[]{arrId, number};
        }
        return null;
    }

    // if a number is missing only once in 9 hint fields vertical, horizontal or group wise it gets returned
    // a populated field counts as a field with all possible 9 hints set
    // second returned int is the corresponding arrId
    // if nothing is found null gets returned
    private boolean getAutoInsert2ByGroup(final Integer[] group, final Hints hints, Integer[] ai2arr) {
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

    public Integer[] getAutoInsert2byStarGroup(final int arrId, final Hints hints) {
        Integer[] ai2arr = {null, null};
        if (getAutoInsert2ByGroup(Sudoku.VERTICAL_GROUPS[Sudoku.ID_VERTICAL_GROUPS[arrId]], hints, ai2arr)
                || getAutoInsert2ByGroup(Sudoku.HORIZONTAL_GROUPS[Sudoku.ID_HORIZONTAL_GROUPS[arrId]], hints, ai2arr)
                || getAutoInsert2ByGroup(Sudoku.GROUPED_GROUPS[Sudoku.ID_GROUPED_GROUPS[arrId]], hints, ai2arr))
            return ai2arr;
        return null;
    }

    public Integer[] getAutoInsert2(final Hints hints) {
        Integer[] ai2arr = {null, null};
        for (int i = 0; i < DIM; i++) {
            if (getAutoInsert2ByGroup(Sudoku.VERTICAL_GROUPS[i], hints, ai2arr)) return ai2arr;
            if (getAutoInsert2ByGroup(Sudoku.HORIZONTAL_GROUPS[i], hints, ai2arr)) return ai2arr;
            if (getAutoInsert2ByGroup(Sudoku.GROUPED_GROUPS[i], hints, ai2arr)) return ai2arr;
        }
        return null;
    }

    public Integer[] autoInsert2(Hints hints, final boolean verbose) {
        Integer[] ai2arr = getAutoInsert2(hints);
        if (ai2arr == null) return null;
        set(ai2arr[0], ai2arr[1]);
        hints.incrementStarGroup(ai2arr[0], ai2arr[1] - 1);
        if (verbose) Log.v("insert by AI2", ai2arr[0] + " (" + ai2arr[1] + ")");
        return ai2arr;
    }

	private static int[] rotate90Degree(final int[] pField, final boolean left) {
        Log.v("rotate90Degree", (left?"left":"right"));
		int[] newField = new int[DIM * DIM];
		int oldArrId;
		int newArrId;
		for (int i = 0; i < DIM; i++) {
			for (int j = 0; j < DIM; j++) {
				if (!left) {
					oldArrId = (DIM - (j+1)) * DIM + i;
					newArrId = i * DIM + j;
				} else {
					oldArrId = i * DIM + j;
					newArrId = (DIM - (j+1)) * DIM + i;
				}
				newField[newArrId] = pField[oldArrId];
			}
		}
		return newField;
	}

	private static int[] rotate180Degree(final int[] pField) {
        Log.v("rotate180Degree", "...");
		int[] newField = new int[DIM * DIM];
		for (int newArrId : Sudoku.ALL_ARR_IDS)
			newField[newArrId] = pField[(DIM * DIM - 1) - newArrId];
		return newField;
	}

	private static void renameEntries(int[] pField, final List<Integer> renameRelation) {
        Log.v("renameEntries", relationToString(renameRelation));
		for (int arrId : Sudoku.ALL_ARR_IDS) {
			if (pField[arrId] != 0) pField[arrId] = renameRelation.get(pField[arrId] - 1);
			else pField[arrId] = 0;
		}
	}

	private static int[] flipVerticalGroupsHorizontally(final int[] pField, final List<Integer> flipRelation) {
        Log.v("flipVerticalGroupsH.", relationToString(flipRelation));
		int oldArrId;
		int newArrId;
		int[] newField = new int[DIM * DIM];
		for (int cnt = 0; cnt < 3; cnt++)
			for (int i = 0; i < DIM; i++)
				for (int j = 0; j < 3; j++) {
					oldArrId = i * DIM + (cnt * 3 + j);
					newArrId = i * DIM + (flipRelation.get(cnt) * 3 + j);
					newField[newArrId] = pField[oldArrId];
				}
		return newField;
	}

	private static int[] flipHorizontalGroupsVertically(final int[] pField, final List<Integer> flipRelation) {
        Log.v("flipHorizontalGroupsV.", relationToString(flipRelation));
		int oldArrId;
		int newArrId;
		int[] newField = new int[DIM * DIM];
		for (int cnt = 0; cnt < 3; cnt++)
            for (int i = 0; i < DIM; i++)
                for (int j = 0; j < 3; j++) {
                    oldArrId = (cnt * 3 + j) * DIM + i;
                    newArrId = (flipRelation.get(cnt) * 3 + j) * DIM + i;
                    newField[newArrId] = pField[oldArrId];
                }
		return newField;
	}

	private static int[] flipInVerticalGroupsHorizontally(final int[] pField, final int groupId, final List<Integer> flipRelation) {
        Log.v("flipInVerticalGroupsH.", groupId + ":" + relationToString(flipRelation));
        int oldArrId;
		int newArrId;
		int[] newField = new int[DIM * DIM];
		for (int cnt = 0; cnt < 3; cnt++)
			for (int i = 0; i < DIM; i++)
                for (int j = 0; j < 3; j++)
                    if (j == groupId) {
                        oldArrId = i * DIM + (cnt + groupId * 3);
                        newArrId = i * DIM + (flipRelation.get(cnt) + groupId * 3);
                        newField[newArrId] = pField[oldArrId];
                    } else {
                        newArrId = i * DIM + (cnt + j * 3);
                        newField[newArrId] = pField[newArrId];
                    }
		return newField;
	}

	private static int[] flipInHorizontalInGroupsVertically(final int[] pField, final int groupId, final List<Integer> flipRelation) {
        Log.v("flipInH.GroupsV.", groupId + ":" + relationToString(flipRelation));
        int oldArrId;
		int newArrId;
        int[] newField = new int[DIM * DIM];
        for (int cnt = 0; cnt < 3; cnt++)
            for (int i = 0; i < DIM; i++)
                for (int j = 0; j < 3; j++)
                    if (j == groupId) {
                        oldArrId = (cnt + groupId * 3) * DIM + i;
                        newArrId = (flipRelation.get(cnt) + groupId * 3) * DIM + i;
                        newField[newArrId] = pField[oldArrId];
                    } else {
                        newArrId = (cnt + j * 3) * DIM + i;
                        newField[newArrId] = pField[newArrId];
                    }
		return newField;
	}

    // Fast way to generate a solution out of another solution.
    // Works for sudokus alike.
    // Number of different ways: 4 * 9! * 3!^8
    // = 4 * 362,880 * 6^8
    // = 1,451,520 * 1,679,616
    // = 2,437,996,216,320 (2.44 * 10^12)
    // The rotation of 180 degree can be simulated be flipping groups
    // and rows vertical and horizontal in the right way. Turning left
    // 90 degree equals turning right 90 degrees and turning 180 degrees.
    // So we must divide the above by 2 to get the distinct solutions.
    // It is known that 6,670,903,752,021,072,936,960 (6.67×10^21)
    // distinct solutions exists. We are way behind! Should we care? ;)
    public void shuffle() {
        shuffle(getShuffleRelations());
    }

    public static List<List<Integer>> getShuffleRelations() {
        List<List<Integer>> relations = new ArrayList<>();
        relations.add(new ArrayList<>(Arrays.asList(0, 1, 2, 3)));
        Collections.shuffle(relations.get(0));
        relations.add(new ArrayList<>(Arrays.asList(1, 2, 3, 4, 5, 6, 7, 8, 9)));
        Collections.shuffle(relations.get(1));
        for (int cnt = 0; cnt < 8; cnt++) {
            relations.add(new ArrayList<>(Arrays.asList(0, 1, 2)));
            Collections.shuffle(relations.get(cnt + 2));
        }
        return relations;
    }

    public void shuffle(final List<List<Integer>> relations) {
        int relNr = 0;
        switch (relations.get(relNr++).get(0)) {
            case 1: pField = rotate90Degree(pField, true); break;
            case 2: pField = rotate180Degree(pField); break;
            case 3: pField = rotate90Degree(pField, false); break;
            default: break;
        }
        renameEntries(pField, relations.get(relNr++));
        pField = flipHorizontalGroupsVertically(pField, relations.get(relNr++));
        pField = flipVerticalGroupsHorizontally(pField, relations.get(relNr++));
        pField = flipInHorizontalInGroupsVertically(pField, 0, relations.get(relNr++));
        pField = flipInHorizontalInGroupsVertically(pField, 1, relations.get(relNr++));
        pField = flipInHorizontalInGroupsVertically(pField, 2, relations.get(relNr++));
        pField = flipInVerticalGroupsHorizontally(pField, 0, relations.get(relNr++));
        pField = flipInVerticalGroupsHorizontally(pField, 1, relations.get(relNr++));
        pField = flipInVerticalGroupsHorizontally(pField, 2, relations.get(relNr));
        init(pField);
    }

    private static String relationToString(final List<Integer> list) {
        String relationString = "[";
        for (int i = 0; i < list.size(); i++)
            relationString += i + " => " + list.get(i) + ", ";
        return relationString + "]";
    }

    @Override
    public String toString() {
        String pFieldString = "";
        for (int i = 0; i < DIM; i++) {
            pFieldString += "\n";
            for (int j = 0; j < DIM; j++) pFieldString += pField[i * DIM + j] + ", ";
        }
        return pFieldString;
    }
}