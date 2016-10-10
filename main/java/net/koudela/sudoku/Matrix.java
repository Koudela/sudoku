package net.koudela.sudoku;

import android.annotation.SuppressLint;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

/**
 * Matrix is a helper class for a new version of {@link Hints#searchSetCover(Map, int, int, Set, Callback)},
 * with larger initialization costs but a very shallow recursion depth on most problems. It searches
 * for quadratic populated areas, that can be produced by flipping (erasing) rows and columns. A
 * modified version can be used to solve the clique problem on huge sparse matrices in reasonable
 * time. Nevertheless the worst case behavior over all possible inputs is still exponential in time.
 */
class Matrix {
    private int[] countV, countH;
    private boolean[][] matrix;
    private Map<Integer, Integer> rowIndices;
    private Map<Integer, Integer> colIndices;

    Matrix() {}

    private Matrix(boolean[][] matrix, Map<Integer, Integer> rowIndices, Map<Integer, Integer> colIndices) {
        this.matrix = matrix;
        this.rowIndices = rowIndices;
        this.colIndices = colIndices;
        initCount();
    }

    private void initCount() {
        countV = new int[matrix[0].length];
        countH = new int[matrix.length];
        for (int i = 0; i < matrix.length; i++) {
            for (int j = 0; j < matrix[0].length; j++) {
                if (matrix[i][j]) {
                    countH[i]++;
                    countV[j]++;
                }
            }
        }
    }

    int rowCount() {
        return matrix.length;
    }

    int colCount() {
        return matrix[0].length;
    }

    private static Map<Integer, Integer> reverseIndices(Map<Integer, Integer> indices) {
        @SuppressLint("UseSparseArrays")
        Map<Integer, Integer> reversedIndices = new HashMap<>();
        for (int key : indices.keySet())
            reversedIndices.put(indices.get(key), key);
        return reversedIndices;
    }

    /**
     * Reduces the matrix to a new matrix made of the rows/columns where the index column/row is
     * true and matrix element is contained in hContains and vContains.
     * Example:
     * matrix is:
     * 0 4 5 0 4 3 2 3 0 3
     * | | | | | | | | | |
     * 0 0 0 0 1 1 0 0 0 0 - 2
     * 0 0 1 0 0 1 0 0 0 1 - 3
     * 0 0 0 0 0 0 0 0 0 0 - 0
     * 0 0 0 0 0 0 1 0 0 1 - 2
     * 0 0 1 0 0 0 0 0 0 0 - 0
     * 0 1 1 0 1 0 1 1 0 0 - 5
     * 0 1 1 0 1 0 0 1 0 0 - 4
     * 0 1 0 0 0 0 0 0 0 1 - 2
     * 1 1 1 1 1 1 0 1 0 0 - 7
     * 0 0 0 0 0 0 0 0 0 0 - 0
     * vContains = [1, 2, 4, 5, 6, 7, 9]
     * hContains = [0, 1, 3, 5, 6, 7, 8]
     * index = 0
     * count = 2
     * reduceVertical = true
     * returns:
     * 4 3
     * | |
     * 1 1 - 2
     * 0 1 - 0
     * 0 0 - 0
     * 1 0 - 0
     * 1 0 - 0
     * 0 0 - 0
     * 1 1 - 2
     *
     * @param vContains all rows left to compute
     * @param hContains all columns left to compute
     * @param index the row/column to use for reducing
     * @param count the number of true entries that are left to compute in the row/column used for reducing
     * @param reduceVertical true for the use of a row for reducing, false for the use of a column
     * @return reduced matrix
     */
    private Matrix buildReducedMatrix(Set<Integer> vContains, Set<Integer> hContains, int index, int count, boolean reduceVertical) {
        @SuppressLint("UseSparseArrays")
        Map<Integer, Integer> newRowIndices = new HashMap<>();
        @SuppressLint("UseSparseArrays")
        Map<Integer, Integer> newColIndices = new HashMap<>();
        int xLength = reduceVertical?hContains.size():count;
        int yLength = reduceVertical?count:vContains.size();
        boolean[][] newMatrix = new boolean[xLength][yLength];
        int i, j;
        i = 0;
        for (int ii : hContains) {
            if (!reduceVertical && !matrix[ii][index]) continue;
            newRowIndices.put(i, rowIndices.get(ii));
            j = 0;
            for (int jj : vContains) {
                if (reduceVertical && !matrix[index][jj]) continue;
                newMatrix[i][j] = matrix[ii][jj];
                newColIndices.put(j, colIndices.get(jj));
                j++;
            }
            i++;
        }
        return new Matrix(newMatrix, newRowIndices, newColIndices);
    }

    Matrix addFullColumns(final int number) {
        boolean[][] newMatrix = new boolean[matrix.length][matrix[0].length + number];
        for (int i = 0; i < matrix.length; i++) {
            System.arraycopy(matrix[i], 0, newMatrix[i], 0, matrix[0].length);
            for (int j = 0; j < number; j++)
                newMatrix[i][j + matrix[0].length] = true;
        }
        for (int j = 0; j < number; j++) colIndices.put(j + matrix[0].length, -1);
        return new Matrix(newMatrix, rowIndices, colIndices);
    }

    Matrix addFullRows(final int number) {
        boolean[][] newMatrix = new boolean[matrix.length + number][matrix[0].length];
        for (int i = 0; i < matrix.length; i++)
            System.arraycopy(matrix[i], 0, newMatrix[i], 0, matrix[0].length);
        for (int i = 0; i < number; i++) {
            rowIndices.put(i + matrix.length, -1);
            for (int j = 0; j < matrix[0].length; j++)
                newMatrix[i + matrix.length][j] = true;
        }
        return new Matrix(newMatrix, rowIndices, colIndices);
    }

    final int findQuadraticFields(final int size, int found, final int depth, final Callback callback) {
        TreeMap<Integer, Deque<Integer>> mapV = new TreeMap<Integer, Deque<Integer>>() {};
        TreeMap<Integer, Deque<Integer>> mapH = new TreeMap<Integer, Deque<Integer>>() {};
        @SuppressWarnings("unchecked")
        Set<Integer> vContains = new HashSet() {};
        @SuppressWarnings("unchecked")
        Set<Integer> hContains = new HashSet() {};
        int countV[] = new int[matrix[0].length];
        int countH[] = new int[matrix.length];
        int vCount = 0;
        int hCount = 0;
        // initialize
        if (size > 1) found = size - 1;
        if (found < 1) found = 1;
        for (int i = 0; i < matrix.length; i++) {
            for (int j = 0; j < matrix[0].length; j++) {
                if (matrix[i][j]) {
                    countV[j]++;
                    countH[i]++;
                }
            }
        }
        for (int j = 0; j < matrix[0].length; j++) {
            if (countV[j] <= found) {
                countV[j] = 0;
            }
            else {
                if (countV[j] == matrix.length) vCount++;
                vContains.add(j);
                if (mapV.get(countV[j]) == null) //noinspection unchecked
                    mapV.put(countV[j], new ArrayDeque() {});
                mapV.get(countV[j]).add(j);
            }
        }
        for (int i = 0; i < matrix.length; i++) {
            if (countH[i] <= found) {
                countH[i] = 0;
            }
            else {
                if (countH[i] == matrix[0].length) hCount++;
                hContains.add(i);
                if (mapH.get(countH[i]) == null) //noinspection unchecked
                    mapH.put(countH[i], new ArrayDeque() {});
                mapH.get(countH[i]).add(i);
            }
        }
        // quadratic field is found
        if (vCount >=  matrix.length) {
            if (matrix.length < size) {
                return matrix.length;
            } else {
                Set<Integer> foundKeys = new HashSet<>();
                for (int i : hContains)
                    if (rowIndices.get(i) > -1) foundKeys.add(rowIndices.get(i));
                return (callback.invokeFunction(foundKeys) == null)?matrix.length - 1:-1;
            }
        }
        // quadratic field is found
        if (hCount >= matrix[0].length) {
            if (matrix[0].length < size) {
                return matrix[0].length;
            } else {
                Set<Integer> foundKeys = new HashSet<>();
                for (int i : hContains)
                    if (rowIndices.get(i) > -1) foundKeys.add(rowIndices.get(i));
                return (callback.invokeFunction(foundKeys) == null)?matrix[0].length - 1:-1;
            }
        }
        while (vContains.size() > found && hContains.size() > found) {
            if (mapV.firstKey() < mapH.firstKey() || (mapV.firstKey().equals(mapH.firstKey()) && vContains.size() < hContains.size())) {
                int cnt = mapV.firstKey();
                if (mapV.get(cnt).isEmpty()) {
                    mapV.remove(cnt);
                    continue;
                }
                int j = mapV.get(cnt).pop();
                if (countV[j] > found) {
                    Matrix newMatrix = buildReducedMatrix(vContains, hContains, j, countV[j], false);
                    int newFound = newMatrix.findQuadraticFields(size, found, depth+1, callback);
                    if (newFound == -1) return -1;
                    if (newFound > found) found = newFound;
                }
                for (int i: hContains) {
                    if (matrix[i][j]) {
                        mapH.get(countH[i]).remove(i);
                        countH[i]--;
                        if (mapH.get(countH[i]) == null) //noinspection unchecked
                            mapH.put(countH[i], new ArrayDeque() {});
                        mapH.get(countH[i]).add(i);
                    }
                }
                countV[j] = 0;
                vContains.remove(j);
            } else {
                int cnt = mapH.firstKey();
                if (mapH.get(cnt).isEmpty()) {
                    mapH.remove(cnt);
                    continue;
                }
                int i = mapH.get(cnt).pop();
                if (countH[i] > found) {
                    Matrix newMatrix = buildReducedMatrix(vContains, hContains, i, countH[i], true);
                    int newFound = newMatrix.findQuadraticFields(size, found, depth+1, callback);
                    if (newFound == -1) return -1;
                    if (newFound > found) found = newFound;
                }
                for (int j: vContains) {
                    if (matrix[i][j]) {
                        mapV.get(countV[j]).remove(j);
                        countV[j]--;
                        if (mapV.get(countV[j]) == null) //noinspection unchecked
                            mapV.put(countV[j], new ArrayDeque() {});
                        mapV.get(countV[j]).add(j);
                    }
                }
                countH[i] = 0;
                hContains.remove(i);
            }
        }
        return found;
    }

    /**
     * Transforms the elementsMap into a matrix, such that all map keys correspond to a row and all
     * elements of the map values corresponds to a false value in that row. The rest of the matrix
     * is set to true.
     * @param elementsMap as the elements parameter used in {@link Hints#searchSetCover(Map, int, int, Set, Callback)}
     */
    @SuppressLint("UseSparseArrays")
    void buildMatrixFromCoverMap(Map<Integer, Set<Integer>> elementsMap) {
        rowIndices = new HashMap<>();
        colIndices = new HashMap<>();
        int count = 0;
        for (Set<Integer> set : elementsMap.values())
            for (int elm : set)
                if (colIndices.get(elm) == null)
                    colIndices.put(elm, count++);
        matrix = new boolean[elementsMap.size()][count];
        for (int i = 0; i < matrix.length; i++)
            for (int j = 0; j < matrix[0].length; j++)
                matrix[i][j] = true;
        count = 0;
        for (int key : elementsMap.keySet()) {
            rowIndices.put(key, count);
            for (int elm : elementsMap.get(key))
                matrix[count][colIndices.get(elm)] = false;
            count++;
        }
        initCount();
        rowIndices = reverseIndices(rowIndices);
        colIndices = reverseIndices(colIndices);
   }

    @Override
    public String toString() {
        String string = "";
        String whiteSpace = "";
        int whiteSpaceWidth = String.valueOf(matrix[0].length).length();
        for (int c = 0; c < whiteSpaceWidth; c++)
            whiteSpace += " ";
        for (int j = 0; j < matrix[0].length; j++)
            string += String.format(Locale.getDefault(), "%"+(whiteSpaceWidth+1)+"d", countV[j]);
        string += "\n";
        for (int j = 0; j < matrix[0].length; j++)
            string += whiteSpace + "|";
        string += "\n";
        for (int i = 0; i < matrix.length; i++) {
            for (int j = 0; j < matrix[0].length; j++)
                string += whiteSpace + (matrix[i][j]?"1":"0");
            string += " - " + countH[i] + "\n";
        }
        return string;
    }
}
