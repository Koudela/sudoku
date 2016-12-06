package net.koudela.sudoku;

import android.annotation.SuppressLint;

import org.junit.Test;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import static junit.framework.Assert.assertEquals;
import static net.koudela.sudoku.SudokuGroups.ALL_ARR_IDS;
import static net.koudela.sudoku.SudokuGroups.DIM;
import static org.junit.Assert.*;

/**
 * Unit Test for class Hints
 */
public class HintsUnitTest {
    private Hints hints = new Hints(false, false, false, false, false);

    private Hint getPrivateHintField(Hints hints, String name) throws NoSuchFieldException, IllegalAccessException {
        Field f = hints.getClass().getDeclaredField(name);
        f.setAccessible(true);
        return (Hint) f.get(hints);
    }

    private boolean isEqualHint(Hint hint1, Hint hint2) {
        for (int arrId : ALL_ARR_IDS) {
            for (int num = 0; num < DIM; num++) {
                if (hint1.get(arrId, num) != hint2.get(arrId, num)) return false;
            }
        }
        return true;
    }

    @Test
    public void constructor_isCorrect() throws NoSuchFieldException, IllegalAccessException {
        Random rand = new Random();
        boolean usePlain = rand.nextBoolean(),
                useAdv1 = rand.nextBoolean(),
                useAdv2 = rand.nextBoolean(),
                useAdv3 = rand.nextBoolean(),
                useUserHint = rand.nextBoolean();
        hints = new Hints(usePlain, useAdv1, useAdv2, useAdv3, useUserHint);
        Hint hint1 = new Hint();
        Hint hint2;
        hint2 = getPrivateHintField(hints, "hint");
        if (usePlain) assertEquals(true, isEqualHint(hint1, hint2));
        else assertEquals(null, hint2);
        hint2 = getPrivateHintField(hints, "hintAdv1");
        if (useAdv1) assertEquals(true, isEqualHint(hint1, hint2));
        else assertEquals(null, hint2);
        hint2 = getPrivateHintField(hints, "hintAdv2");
        if (useAdv2) assertEquals(true, isEqualHint(hint1, hint2));
        else assertEquals(null, hint2);
        hint2 = getPrivateHintField(hints, "hintAdv3");
        if (useAdv3) assertEquals(true, isEqualHint(hint1, hint2));
        else assertEquals(null, hint2);
        hint2 = getPrivateHintField(hints, "userHint");
        if (useUserHint) assertEquals(true, isEqualHint(hint1, hint2));
        else assertEquals(null, hint2);
    }

    private void setUseHintX(String name, boolean use) throws NoSuchMethodException {
        switch (name) {
            case "hint": hints.setUsePlain(use); break;
            case "hintAdv1": hints.setUseAdv1(use); break;
            case "hintAdv2": hints.setUseAdv2(use); break;
            case "hintAdv3": hints.setUseAdv3(use); break;
            case "userHint": hints.setUseUserHint(use); break;
            default: throw new NoSuchMethodException(name);
        }
    }

    private void setUseHintX_isCorrect(String name) throws NoSuchFieldException, IllegalAccessException, NoSuchMethodException {
        Random rand = new Random();
        int arrId = rand.nextInt(DIM * DIM),
                num = rand.nextInt(DIM),
                pos;
        Hint hint = new Hint();
        hints = new Hints(false, false, false, false, false);
        switch (name) {
            case "hint": pos = 0; break;
            case "hintAdv1": pos = 1; break;
            case "hintAdv2": pos = 2; break;
            case "hintAdv3": pos = 3; break;
            case "userHint": pos = 4; break;
            default: pos = -1;
        }
        setUseHintX(name, false);
        assertEquals(false, hints.getUsage()[pos]);
        assertEquals(null, getPrivateHintField(hints, name));
        setUseHintX(name, true);
        Hint plainHint = getPrivateHintField(hints, name);
        assertEquals(true, hints.getUsage()[pos]);
        assertEquals(true, isEqualHint(hint, plainHint));
        hint.set(arrId, num, 7);
        plainHint.set(arrId, num, 7);
        setUseHintX(name, true);
        assertEquals(true, hints.getUsage()[pos]);
        assertEquals(true, isEqualHint(hint, getPrivateHintField(hints, name)));
        setUseHintX(name, false);
        assertEquals(false, hints.getUsage()[pos]);
        // hintX is now in a not defined state
        setUseHintX(name, true);
        assertEquals(true, isEqualHint(new Hint(), getPrivateHintField(hints, name)));
    }

    @Test
    public void setUsePlain_isCorrect() throws NoSuchFieldException, IllegalAccessException, NoSuchMethodException {
        setUseHintX_isCorrect("hint");
    }

    @Test
    public void setUseAdv1_isCorrect() throws NoSuchFieldException, IllegalAccessException, NoSuchMethodException {
        setUseHintX_isCorrect("hintAdv1");
    }

    @Test
    public void setUseAdv2_isCorrect() throws NoSuchFieldException, IllegalAccessException, NoSuchMethodException {
        setUseHintX_isCorrect("hintAdv2");
    }

    @Test
    public void setUseAdv3_isCorrect() throws NoSuchFieldException, IllegalAccessException, NoSuchMethodException {
        setUseHintX_isCorrect("hintAdv3");
    }

    @Test
    public void setUseUserHint_isCorrect() throws NoSuchFieldException, IllegalAccessException, NoSuchMethodException {
        setUseHintX_isCorrect("userHint");
    }

    @Test
    public void getUsage_isCorrect() {
        Random rand = new Random();
        boolean usePlain = rand.nextBoolean(),
                useAdv1 = rand.nextBoolean(),
                useAdv2 = rand.nextBoolean(),
                useAdv3 = rand.nextBoolean(),
                useUserHint = rand.nextBoolean();
        hints = new Hints(usePlain, useAdv1, useAdv2, useAdv3, useUserHint);
        boolean[] usage = hints.getUsage();
        assertEquals(usePlain, usage[0]);
        assertEquals(useAdv1, usage[1]);
        assertEquals(useAdv2, usage[2]);
        assertEquals(useAdv3, usage[3]);
        assertEquals(useUserHint, usage[4]);
    }

    @Test
    public void init_isCorrect() {
        assertEquals(true, false);
    }

    @Test
    public void initAdv_isCorrect() {
        assertEquals(true, false);
    }

    @Test
    public void initAdv1_isCorrect() {
        assertEquals(true, false);
    }

    @Test
    public void initAdv2_isCorrect() {
        assertEquals(true, false);
    }

    @Test
    public void initAdv3_isCorrect() {
        assertEquals(true, false);
    }

    @Test
    public void getPlainHint_isCorrect() {
        assertEquals(true, false);
    }

    @Test
    public void isUserHint_isCorrect() {
        assertEquals(true, false);
    }

    @Test
    public void populatePlainHints_isCorrect() {
        assertEquals(true, false);
    }

    @Test
    public void populatePlainHints_static_isCorrect() {
        assertEquals(true, false);
    }

    @Test
    public void incrementStarGroup_setVersion_isCorrect() {
        assertEquals(true, false);
    }

    @Test
    public void incrementStarGroup_isCorrect() {
        assertEquals(true, false);
    }

    @Test
    public void incrementStarGroup_static_isCorrect() {
        assertEquals(true, false);
    }

    @Test
    public void decrementStarGroup_setVersion_isCorrect() {
        assertEquals(true, false);
    }

    @Test
    public void decrementStarGroup_isCorrect() {
        assertEquals(true, false);
    }

    @Test
    public void decrementStarGroup_static_isCorrect() {
        assertEquals(true, false);
    }

    @Test
    public void updateAdv1_isCorrect() {
        assertEquals(true, false);
    }

    @Test
    public void updateAdv2_isCorrect() {
        assertEquals(true, false);
    }

    @Test
    public void updateAdv3_isCorrect() {
        assertEquals(true, false);
    }

    @Test
    public void setAutoHintsAdv1_isCorrect() {
        assertEquals(true, false);
    }

    @Test
    public void setAutoHintsAdv2_isCorrect() {
        assertEquals(true, false);
    }

    @Test
    public void setAutoHintsAdv3_isCorrect() {
        assertEquals(true, false);
    }

    @Test
    public void setAutoHintsAdv2ByGroupMinus_isCorrect() throws NoSuchMethodException,
            NoSuchFieldException, InvocationTargetException, IllegalAccessException {
        Hints hints = new Hints(true, false, true, false, false);
        Playground playground = new Playground(new int[] {
                1, 2, 3, 4, 5, 6, 0, 0, 0,
                0, 0, 0, 0, 0, 0, 0, 0, 0,
                0, 0, 0, 0, 0, 0, 0, 0, 0,
                0, 0, 0, 0, 0, 0, 0, 0, 0,
                0, 0, 0, 0, 0, 0, 0, 0, 0,
                0, 0, 0, 0, 0, 0, 0, 0, 0,
                0, 0, 0, 0, 0, 0, 0, 0, 0,
                0, 0, 0, 0, 0, 0, 0, 0, 0,
                0, 0, 0, 0, 0, 0, 0, 0, 0});
        Integer[] group = new Integer[]{6, 7, 8, 15, 16, 17, 24, 25, 26};
        Hint hint = new Hint();
        for (int num = 6; num < 9; num++)
            for (int arrId : new int[]{15, 16, 17, 24, 25, 26})
                hint.increment(arrId, num);
        hints.populatePlainHints(playground);
        Method m = hints.getClass().getDeclaredMethod("setAutoHintsAdv2ByGroupMinus", Integer[].class,
                Playground.class, boolean.class);
        m.setAccessible(true);
        Field f = hints.getClass().getDeclaredField("hintAdv2");
        f.setAccessible(true);
        m.invoke(hints, group, playground, false);
        assertEquals(true, isEqualHint((Hint) f.get(hints), hint));
    }

    @Test
    public void setAutoHintsAdv3ByGroupMinus_isCorrect() throws NoSuchMethodException,
            NoSuchFieldException, InvocationTargetException, IllegalAccessException {
        Hints hints = new Hints(true, false, false, true, false);
        Playground playground = new Playground(new int[] {
                1, 2, 3, 4, 5, 6, 0, 0, 0,
                0, 0, 0, 0, 0, 0, 0, 0, 0,
                0, 0, 0, 0, 0, 0, 0, 0, 0,
                0, 0, 0, 0, 0, 0, 0, 0, 0,
                0, 0, 0, 0, 0, 0, 0, 0, 0,
                0, 0, 0, 0, 0, 0, 0, 0, 0,
                0, 0, 0, 0, 0, 0, 0, 0, 0,
                0, 0, 0, 0, 0, 0, 0, 0, 0,
                0, 0, 0, 0, 0, 0, 0, 0, 0});
        Integer[] group = new Integer[]{6, 7, 8, 15, 16, 17, 24, 25, 26};
        Hint hint = new Hint();
        for (int num = 6; num < 9; num++)
            for (int arrId : new int[]{15, 16, 17, 24, 25, 26})
                hint.increment(arrId, num);
        hints.populatePlainHints(playground);
        Method m = hints.getClass().getDeclaredMethod("setAutoHintsAdv3ByGroupMinus", Integer[].class,
                Playground.class, boolean.class);
        m.setAccessible(true);
        Field f = hints.getClass().getDeclaredField("hintAdv3");
        f.setAccessible(true);
        m.invoke(hints, group, playground, false);
        assertEquals(true, isEqualHint((Hint) f.get(hints), hint));
    }

    @Test
    public void powerSet_isCorrect() {

    }

    @Test
    public void setAutoHintsAdv2ByGroupPlusOld_isCorrect() throws NoSuchFieldException,
            InvocationTargetException, IllegalAccessException, NoSuchMethodException {
        Hints hints = new Hints(true, true, true, false, false);
        Playground playground = new Playground(new int[] {
                2, 7, 1, 3, 4, 9, 6, 5, 8,
                6, 3, 5, 2, 7, 8, 4, 9, 1,
                8, 9, 4, 6, 1, 5, 3, 2, 7,
                5, 4, 3, 1, 6, 2, 8, 7, 9,
                7, 6, 2, 0, 0, 0, 0, 0, 0,
                0, 0, 0, 0, 0, 0, 0, 0, 0,
                0, 0, 0, 0, 0, 0, 0, 0, 0,
                0, 0, 0, 0, 0, 0, 0, 0, 0,
                0, 0, 0, 0, 0, 0, 0, 0, 0});
        Integer[] group = new Integer[]{39, 40, 41, 42, 43, 44};
        hints.populatePlainHints(playground);
        hints.setAutoHintsAdv1(playground, false);
        hints.setAutoHintsAdv2(playground, false, true);
        Method m = hints.getClass().getDeclaredMethod("setAutoHintsAdv2ByGroupPlusOld", Integer[].class,
                Playground.class, boolean.class);
        m.setAccessible(true);
        Field f = hints.getClass().getDeclaredField("hintAdv2");
        f.setAccessible(true);
        Hint hint = new Hint((Hint) f.get(hints));
        hint.increment(39, 3);
        hint.increment(39, 4);
        hint.increment(40, 2);
        hint.increment(40, 4);
        while (true) {
            //noinspection unchecked
            if (((Set<Integer>) m.invoke(hints, group, playground, false)).size() == 0) break;
        }
        assertEquals(true, isEqualHint(hint, (Hint) f.get(hints)));

    }

    @Test
    public void setAutoHintsAdv3ByGroupPlusOld_isCorrect() throws NoSuchMethodException,
            NoSuchFieldException, InvocationTargetException, IllegalAccessException {
        Hints hints = new Hints(true, true, false, true, false);
        Playground playground = new Playground(new int[] {
                2, 7, 1, 3, 4, 9, 6, 5, 8,
                6, 3, 5, 2, 7, 8, 4, 9, 1,
                8, 9, 4, 6, 1, 5, 3, 2, 7,
                5, 4, 3, 1, 6, 2, 8, 7, 9,
                7, 6, 2, 0, 0, 0, 0, 0, 0,
                0, 0, 0, 0, 0, 0, 0, 0, 0,
                0, 0, 0, 0, 0, 0, 0, 0, 0,
                0, 0, 0, 0, 0, 0, 0, 0, 0,
                0, 0, 0, 0, 0, 0, 0, 0, 0});
        Integer[] group = new Integer[]{39, 40, 41, 42, 43, 44};
        hints.populatePlainHints(playground);
        hints.setAutoHintsAdv1(playground, false);
        hints.setAutoHintsAdv2(playground, false, true);
        Method m = hints.getClass().getDeclaredMethod("setAutoHintsAdv3ByGroupPlusOld", Integer[].class,
                Playground.class, boolean.class);
        m.setAccessible(true);
        Field f = hints.getClass().getDeclaredField("hintAdv3");
        f.setAccessible(true);
        Hint hint = new Hint((Hint) f.get(hints));
        hint.increment(39, 3);
        hint.increment(39, 4);
        hint.increment(40, 2);
        hint.increment(40, 4);
        while (true) {
            //noinspection unchecked
            if (((Set<Integer>) m.invoke(hints, group, playground, false)).size() == 0) break;
        }
        assertEquals(true, isEqualHint(hint, (Hint) f.get(hints)));
    }

    @Test
    public void setAutoHintsAdv2ByGroupPlusNew_isCorrect() throws NoSuchMethodException,
            NoSuchFieldException, InvocationTargetException, IllegalAccessException {
        Hints hints = new Hints(true, true, true, false, false);
        Playground playground = new Playground(new int[] {
                2, 7, 1, 3, 4, 9, 6, 5, 8,
                6, 3, 5, 2, 7, 8, 4, 9, 1,
                8, 9, 4, 6, 1, 5, 3, 2, 7,
                5, 4, 3, 1, 6, 2, 8, 7, 9,
                7, 6, 2, 0, 0, 0, 0, 0, 0,
                0, 0, 0, 0, 0, 0, 0, 0, 0,
                0, 0, 0, 0, 0, 0, 0, 0, 0,
                0, 0, 0, 0, 0, 0, 0, 0, 0,
                0, 0, 0, 0, 0, 0, 0, 0, 0});
        Integer[] group = new Integer[]{39, 40, 41, 42, 43, 44};
        hints.populatePlainHints(playground);
        hints.setAutoHintsAdv1(playground, false);
        hints.setAutoHintsAdv2(playground, false, true);
        Method m = hints.getClass().getDeclaredMethod("setAutoHintsAdv2ByGroupPlusNew", Integer[].class,
                Playground.class, boolean.class);
        m.setAccessible(true);
        Field f = hints.getClass().getDeclaredField("hintAdv2");
        f.setAccessible(true);
        Hint hint = new Hint((Hint) f.get(hints));
        hint.increment(39, 3);
        hint.increment(39, 4);
        hint.increment(40, 2);
        hint.increment(40, 4);
        while (true) {
            //noinspection unchecked
            if (((Set<Integer>) m.invoke(hints, group, playground, false)).size() == 0) break;
        }
        assertEquals(true, isEqualHint(hint, (Hint) f.get(hints)));
    }

    @Test
    public void setAutoHintsAdv3ByGroupPlusNew_isCorrect() throws NoSuchMethodException,
            NoSuchFieldException, InvocationTargetException, IllegalAccessException {
        Hints hints = new Hints(true, true, false, true, false);
        Playground playground = new Playground(new int[] {
                2, 7, 1, 3, 4, 9, 6, 5, 8,
                6, 3, 5, 2, 7, 8, 4, 9, 1,
                8, 9, 4, 6, 1, 5, 3, 2, 7,
                5, 4, 3, 1, 6, 2, 8, 7, 9,
                7, 6, 2, 0, 0, 0, 0, 0, 0,
                0, 0, 0, 0, 0, 0, 0, 0, 0,
                0, 0, 0, 0, 0, 0, 0, 0, 0,
                0, 0, 0, 0, 0, 0, 0, 0, 0,
                0, 0, 0, 0, 0, 0, 0, 0, 0});
        Integer[] group = new Integer[]{39, 40, 41, 42, 43, 44};
        hints.populatePlainHints(playground);
        hints.setAutoHintsAdv1(playground, false);
        hints.setAutoHintsAdv2(playground, false, true);
        Method m = hints.getClass().getDeclaredMethod("setAutoHintsAdv3ByGroupPlusNew", Integer[].class,
                Playground.class, boolean.class);
        m.setAccessible(true);
        Field f = hints.getClass().getDeclaredField("hintAdv3");
        f.setAccessible(true);
        Hint hint = new Hint((Hint) f.get(hints));
        hint.increment(39, 3);
        hint.increment(39, 4);
        hint.increment(40, 2);
        hint.increment(40, 4);
        while (true) {
            //noinspection unchecked
            if (((Set<Integer>) m.invoke(hints, group, playground, false)).size() == 0) break;
        }
        assertEquals(true, isEqualHint(hint, (Hint) f.get(hints)));
    }

    @Test
    public void searchSetCover_isCorrect() throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
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

        Method m = Hints.class.getDeclaredMethod("searchSetCover", Map.class, int.class, int.class, Set.class, Callback.class);
        m.setAccessible(true);
        m.invoke(null, testMap, 3, 0, new HashSet<Integer>(), coverCallback);
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

        foundSet1 = false;
        foundSet2 = false;
        foundSet3 = false;
        foundAnotherSet = false;
        m = Hints.class.getDeclaredMethod("searchSetCover", Map.class, int.class, int.class, Callback.class);
        m.setAccessible(true);
        m.invoke(null, testMap, 2, 3, coverCallback);
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
