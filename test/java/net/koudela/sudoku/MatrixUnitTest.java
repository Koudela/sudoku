package net.koudela.sudoku;

import android.annotation.SuppressLint;

import org.junit.Test;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static org.junit.Assert.*;

/**
 * Unit Test for class Matrix
 */
public class MatrixUnitTest {
    @Test
    public void buildMatrixFromCoverMap_and_findQuadraticField_areCorrect() {
        @SuppressLint("UseSparseArrays")
        Map<Integer, Set<Integer>> testMap = new HashMap<>();
        testMap.put(0, new HashSet<>(Arrays.asList(0, 1               )));
        testMap.put(1, new HashSet<>(Arrays.asList(0, 1, 2            )));
        testMap.put(2, new HashSet<>(Arrays.asList(0,    2,       5   )));
        testMap.put(3, new HashSet<>(Arrays.asList(0,       3         )));
        testMap.put(4, new HashSet<>(Arrays.asList(0, 1, 2,    4      )));
        testMap.put(5, new HashSet<>(Arrays.asList(0, 1,    3         )));
        testMap.put(6, new HashSet<>(Arrays.asList(0, 1,       4,    6)));
        testMap.put(7, new HashSet<>(Arrays.asList(0, 1               )));
        Set<Integer> resultSet1 = new HashSet<>(Arrays.asList(0, 7));
        Set<Integer> resultSet2 = new HashSet<>(Arrays.asList(0, 1, 7));
        Set<Integer> resultSet3 = new HashSet<>(Arrays.asList(0, 3, 5, 7));
        boolean foundSet1 = false,
                foundSet2 = false,
                foundSet3 = false,
                foundAnotherSet = false;

        class CoverCallback implements Callback {
            @SuppressWarnings("unchecked")
            private Set<Set<Integer>> keys = new HashSet();

            @Override
            public Set<Integer> invokeFunction(Set<Integer> keys) {
                this.keys.add(keys);
                return null;
            }

            @Override
            public Set<Integer> getKeys() {
                return null;
            }
        }
        CoverCallback coverCallback = new CoverCallback();

        Matrix matrix = new Matrix();
        Matrix newMatrix;
        matrix.buildMatrixFromCoverMap(testMap);
        // find Cover 3x3
        newMatrix = matrix.addFullRows(1);
        newMatrix.findQuadraticFields(4, 0, 0, coverCallback);
        // find Cover 2x2
        newMatrix = matrix.addFullRows(3);
        newMatrix.findQuadraticFields(5, 0, 0, coverCallback);
        for (Set<Integer> set : coverCallback.keys) {
            if (set.equals(resultSet1)) foundSet1 = true;
            else if (set.equals(resultSet2)) foundSet2 = true;
            else if (set.equals(resultSet3)) foundSet3 = true;
            else if (set.size() != 3 || !resultSet3.containsAll(set)) foundAnotherSet = true;
        }
        assertEquals(true, foundSet1);
        assertEquals(true, foundSet2);
        assertEquals(true, foundSet3);
        assertEquals(false, foundAnotherSet);
    }
}
