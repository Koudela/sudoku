package net.koudela.sudoku;

import java.util.Set;

/**
 * Interface to make callbacks for {@link Hints#searchSetCover} work in java.
 * Needed for:
 * - {@link Hints#setAutoHintsAdv2ByGroupPlusNew}
 * - {@link Hints#setAutoHintsAdv3ByGroupPlusNew}
 *
 * @author Thomas Koudela
 * @version 1.0 stable
 */
interface Callback {
    /**
     * The method evaluates a found cover. It checks whether a cover gives new hints.
     * @param keys the keys of the found cover
     * @return a arbitrary set if searchSetCover should die, null otherwise
     */
    Set<Integer> invokeFunction(Set<Integer> keys);

    /**
     * @return a set of arrIds where the new found hints are placed, an empty set if no such hints
     * where found
     */
    Set<Integer> getKeys();
}
