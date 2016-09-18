package net.koudela.sudoku;

import java.util.ArrayDeque;
import java.util.Collection;
import java.util.HashSet;

// We use this class to track not tested hints
// Why a structure that combines ArrayDeque AND a HashSet:
// We need a structure that support a pop like action, adding elements and providing uniqueness of the elements in constant time.
// Unluckily there is no HashSetDeque and HashSet can not give a unspecified Element back.
// Therefore ArrayDeque provides the pop action and HashSet the contains action.
// All methods to use and maintain the structure can now be done in constant time.
// This class only supplies the most basic methods needed to maintain the structure.
public class HashSetLIFOQueue {
    private ArrayDeque<Integer> deque = new ArrayDeque<>();
    private HashSet<Integer> set = new HashSet<>();

    HashSetLIFOQueue(Collection<Integer> collection) {
        set.addAll(collection);
        deque.addAll(set);
    }

    public boolean isEmpty() {
        return set.isEmpty();
    }

    public int pop() {
        int elm = deque.pop();
        set.remove(elm);
        return elm;
    }

    public void push(int elm) {
        if (set.contains(elm)) return;
        deque.push(elm);
        set.add(elm);
    }

    public void addAll(Collection<Integer> collection) {
        for (int elm : collection) push(elm);
    }
}
