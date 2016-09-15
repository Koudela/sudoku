package net.koudela.sudoku;

import android.util.Log;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class Playground {
    private static final int DIM = Sudoku.DIM;
    private int[] pField = new int[DIM * DIM];
    private Map<Integer, Integer> populatedArrIds = new ConcurrentHashMap<>();
    private Map<Integer, Integer> notPopulatedArrIds = new ConcurrentHashMap<>();

    public Playground() {
        init();
    }

    public Playground(int[] clone) {
        init(clone);
    }

    public Playground(Playground clone) {
        init(clone);
    }

    public void init() {
        populatedArrIds.clear();
        notPopulatedArrIds.clear();
        for (int arrId : Sudoku.ALL_ARR_IDS) {
            notPopulatedArrIds.put(arrId, arrId);
            pField[arrId] = 0;
        }
    }

    public void init(int[] cloneField) {
        populatedArrIds.clear();
        notPopulatedArrIds.clear();
        for (int arrId : Sudoku.ALL_ARR_IDS) {
            if (cloneField[arrId] < 0 || cloneField[arrId] > DIM)
                throw new IllegalArgumentException("value must be within [0, " + DIM + "]; value was " + cloneField[arrId]);
            pField[arrId] = cloneField[arrId];
            if (cloneField[arrId] != 0) populatedArrIds.put(arrId, arrId);
            else notPopulatedArrIds.put(arrId, arrId);
        }
    }

    public void init(Playground clone) {
        populatedArrIds.clear();
        notPopulatedArrIds.clear();
        for (int arrId : Sudoku.ALL_ARR_IDS) {
            pField[arrId] = clone.get(arrId);
            if (clone.isPopulated(arrId)) populatedArrIds.put(arrId, arrId);
            else notPopulatedArrIds.put(arrId, arrId);
        }
    }

    public int get(int arrId) {
        return pField[arrId];
    }

    public void set(int arrId, int val) {
        if (val < 0 || val > DIM)
            throw new IllegalArgumentException("val must be within [0, " + DIM + "]; val was " + val);
        if (pField[arrId] == val) return;
        if (val == 0) {
            populatedArrIds.remove(arrId);
            notPopulatedArrIds.put(arrId, arrId);
        } else {
            notPopulatedArrIds.remove(arrId);
            populatedArrIds.put(arrId, arrId);
        }
        pField[arrId] = val;
    }

    public int[] getPField() {
        return pField;
    }

    public boolean isPopulated(int arrId) {
        return pField[arrId] != 0;
    }

    public Map<Integer, Integer> getPopulatedArrIds() {
        return populatedArrIds;
    }

    public Map<Integer, Integer> getNotPopulatedArrIds() {
        return notPopulatedArrIds;
    }

    // if exact 8 hints are displayed it returns the missing number, null otherwise
    public int getAutoInsert1ByField(int arrId, Hints hints) {
        if (isPopulated(arrId)) return 0;
        int number = 0;
        for (int num = 0; num < DIM; num++)
            if (!hints.isHint(arrId, num)) {
                if (number != 0) return 0;
                else number = num + 1;
            }
        return number;
    }

    public int getAutoInsert1(Hints hints) {
        int number;
        for (Map.Entry<Integer, Integer> arrId : populatedArrIds.entrySet()) {
            number = getAutoInsert1ByField(arrId.getValue(), hints);
            if (number != 0) return number;
        }
        return 0;
    }

    // if a number is missing only once in 9 hint fields vertical, horizontal or group wise it gets returned
    // a populated field counts as a field with all possible 9 hints set
    // second returned int is the corresponding arrId
    // if nothing is found null gets returned
    public Integer[] getAutoInsert2ByGroup(Integer[] group, Hints hints) {
        for (int num = 0; num < DIM; num++) {
            int count = 0;
            int tempArrId = -1;
            for (int arrId : group) {
                if (isPopulated(arrId) || hints.isHint(arrId, num)) count++;
                else if (tempArrId == -1) tempArrId = arrId;
                else break;
            }
            if (count == 8) return new Integer[]{num + 1, tempArrId};
        }
        return null;
    }

    public Integer[] getAutoInsert2(Hints hints) {
        Integer[] ai2arr;
        for (int i = 0; i < DIM; i++) {
            ai2arr = getAutoInsert2ByGroup(Sudoku.VERTICAL_GROUPS[i], hints);
            if (ai2arr != null) return ai2arr;
            ai2arr = getAutoInsert2ByGroup(Sudoku.HORIZONTAL_GROUPS[i], hints);
            if (ai2arr != null) return ai2arr;
            ai2arr = getAutoInsert2ByGroup(Sudoku.GROUPED_GROUPS[i], hints);
            if (ai2arr != null) return ai2arr;
        }
        return null;
    }

	private static int[] rotate90Degree(int[] pField, boolean left) {
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

	private static int[] rotate180Degree(int[] pField) {
        Log.v("rotate180Degree", "...");
		int[] newField = new int[DIM * DIM];
		for (int newArrId : Sudoku.ALL_ARR_IDS)
			newField[newArrId] = pField[(DIM * DIM - 1) - newArrId];
		return newField;
	}

	private static int[] renameEntries(int[] pField, List<Integer> renameRelation) {
        Log.v("renameEntries", renameRelation.toString() + "; " + relationToString(renameRelation));
		int[] newField = new int[DIM * DIM];
		for (int arrId : Sudoku.ALL_ARR_IDS) {
			if (pField[arrId] != 0) newField[arrId] = renameRelation.get(pField[arrId] - 1);
			else newField[arrId] = 0;
		}
		return newField;
	}

	private static int[] flipVerticalGroupsHorizontally(int[] pField, List<Integer> flipRelation) {
        Log.v("flipVerticalGroupsH.", flipRelation.toString() + "; " + relationToString(flipRelation));
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

	private static int[] flipHorizontalGroupsVertically(int[] pField, List<Integer> flipRelation) {
        Log.v("flipHorizontalGroupsV.", flipRelation.toString() + "; " + relationToString(flipRelation));
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

	private static int[] flipInVerticalGroupsHorizontally(int[] pField, int groupId, List<Integer> flipRelation) {
        Log.v("flipInVerticalGroupsH.", groupId + ":" + flipRelation.toString() + "; " + relationToString(flipRelation));
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

	private static int[] flipInHorizontalInGroupsVertically(int[] pField, int groupId, List<Integer> flipRelation) {
        Log.v("flipInH.GroupsV.", groupId + ":" + flipRelation.toString() + "; " + relationToString(flipRelation));
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
        List<List<Integer>> relations = new ArrayList<>();
        relations.add(new ArrayList<Integer>() {{
            add(0); add(1); add(2); add(3);
        }});
        Collections.shuffle(relations.get(0));
        relations.add(new ArrayList<Integer>());
        for (int i = 1; i <= DIM; i++) {
            relations.get(1).add(i);
        }
        Collections.shuffle(relations.get(1));
        for (int cnt = 0; cnt < 8; cnt++) {
            relations.add(new ArrayList<Integer>());
            for (int i = 0; i < 3; i++) {
                relations.get(cnt+2).add(i);
            }
            Collections.shuffle(relations.get(cnt+2));
        }

        int relNr = 0;
        Log.v("0"+(relNr), toString());
        switch (relations.get(relNr++).get(0)) {
            case 1: pField = rotate90Degree(pField, true); break;
            case 2: pField = rotate180Degree(pField); break;
            case 3: pField = rotate90Degree(pField, false); break;
            default: break;
        }
        Log.v("0"+(relNr), toString());
        pField = renameEntries(pField, relations.get(relNr++));
        Log.v("0"+(relNr), toString());
        pField = flipHorizontalGroupsVertically(pField, relations.get(relNr++));
        Log.v("0"+(relNr), toString());
        pField = flipVerticalGroupsHorizontally(pField, relations.get(relNr++));
        Log.v("0"+(relNr), toString());
        pField = flipInHorizontalInGroupsVertically(pField, 0, relations.get(relNr++));
        Log.v("0"+(relNr), toString());
        pField = flipInHorizontalInGroupsVertically(pField, 1, relations.get(relNr++));
        Log.v("0"+(relNr), toString());
        pField = flipInHorizontalInGroupsVertically(pField, 2, relations.get(relNr++));
        Log.v("0"+(relNr), toString());
        pField = flipInVerticalGroupsHorizontally(pField, 0, relations.get(relNr++));
        Log.v("0"+(relNr), toString());
        pField = flipInVerticalGroupsHorizontally(pField, 1, relations.get(relNr++));
        Log.v("0"+(relNr), toString());
        pField = flipInVerticalGroupsHorizontally(pField, 2, relations.get(relNr++));
        Log.v("0"+(relNr), toString());
        init(pField);
    }


    public static String relationToString(List<Integer> list) {
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