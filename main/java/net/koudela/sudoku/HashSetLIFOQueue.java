package net.koudela.sudoku;

import java.util.ArrayDeque;
import java.util.Collection;
import java.util.HashSet;

/**
 * LIFO Queue that preserves uniqueness of its elements
 * We use this class to track not tested hints ({@link SudokuStaticFunctions#updateSudoku}).
 * Why a structure that combines ArrayDeque AND a HashSet:
 * We need a structure that support a pop like action, adding elements and providing uniqueness of the elements in constant time.
 * Unluckily there is no HashSetDeque and HashSet can not give a unspecified Element back.
 * Therefore ArrayDeque provides the pop action and HashSet the contains action.
 * All methods to use and maintain the structure can now be done in (amortized) constant time.
 * This class only supplies the most basic methods needed to maintain the structure.
 *
 * @author Thomas Koudela
 * @version 1.0 stable
 */
@SuppressWarnings({"WeakerAccess", "unused"})
class HashSetLIFOQueue {
    private ArrayDeque<Integer> deque = new ArrayDeque<>();
    private HashSet<Integer> set = new HashSet<>();

    HashSetLIFOQueue() {
    }

    HashSetLIFOQueue(Collection<Integer> collection) {
        set.addAll(collection);
        deque.addAll(set);
    }

    boolean isEmpty() {
        return set.isEmpty();
    }

    int pop() {
        int elm = deque.pop();
        set.remove(elm);
        return elm;
    }

    void push(int elm) {
        if (set.contains(elm)) return;
        deque.push(elm);
        set.add(elm);
    }

    void addAll(Collection<Integer> collection) {
        for (int elm : collection) push(elm);
    }
}
