package net.koudela.sudoku;

import java.util.Set;

/**
 * Interface to make callbacks work in java.
 * Needed for:
 * - {@link Hints#setAutoHintsAdv2ByGroupPlusNew}
 * - {@link Hints#setAutoHintsAdv3ByGroupPlusNew}
 *
 * @author Thomas Koudela
 * @version 1.0 stable
 */
interface Callback {
    Set<Integer> invokeFunction(Set<Integer> keys);
}
