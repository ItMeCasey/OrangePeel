package com.itmecasey.orangepeel;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import org.junit.jupiter.api.Test;

class MealComparatorTest {
    private static FoodInfo food(String id, int nutrition, float saturation) {
        return new FoodInfo("minecraft:" + id, id, nutrition, saturation, 32, List.of());
    }

    private final FoodInfo bread = food("bread", 5, 6.0f);
    private final FoodInfo apple = food("apple", 4, 2.4f);
    private final FoodInfo steak = food("steak", 8, 12.8f);
    private final FoodInfo carrot = food("carrot", 3, 3.6f);
    private final FoodInfo berries = food("berries", 2, 0.4f);
    private final FoodInfo cookie = food("cookie", 2, 0.4f);

    @Test
    void ranksAgainstOtherDistinctFoods() {
        MealComparator.Result r = MealComparator.compare(bread, List.of(apple, steak, carrot, berries, bread));
        assertEquals(4, r.others());
        assertEquals(3, r.betterThan());
        assertFalse(r.best());
        assertEquals("minecraft:steak", r.bestFood().id());
    }

    @Test
    void duplicateStacksCountOnce() {
        MealComparator.Result r = MealComparator.compare(apple, List.of(apple, apple, bread, bread, bread));
        assertEquals(1, r.others());
        assertEquals(0, r.betterThan());
    }

    @Test
    void flagsTheBestFood() {
        MealComparator.Result r = MealComparator.compare(steak, List.of(apple, bread, steak));
        assertTrue(r.best());
        assertEquals(2, r.betterThan());
        assertEquals(1f, r.fractionOfBest());
        assertEquals(0f, r.deltaVsBest());
    }

    @Test
    void tiesAreNotBetterButStillBest() {
        MealComparator.Result r = MealComparator.compare(berries, List.of(cookie));
        assertEquals(0, r.betterThan());
        assertTrue(r.best());
    }

    @Test
    void deltaAgainstBestIsNegativeAndBounded() {
        MealComparator.Result r = MealComparator.compare(apple, List.of(steak));
        assertEquals(apple.score() - steak.score(), r.deltaVsBest(), 1e-4);
        assertEquals(apple.score() / steak.score(), r.fractionOfBest(), 1e-4);
    }

    @Test
    void aloneHasNoOthers() {
        MealComparator.Result r = MealComparator.compare(bread, List.of(bread));
        assertEquals(0, r.others());
        assertTrue(r.best());
    }

    private static PlayerState hunger(int food, float sat) {
        return new PlayerState(food, sat, 0f, 20f, 20f, 0f, true);
    }

    @Test
    void bestForHungerPrefersWhatFitsNotWhatIsBiggest() {
        // food 19: only one hunger point fits, but the steak still adds the most saturation
        FoodInfo best = MealComparator.bestForHunger(hunger(19, 0f), List.of(steak, berries, apple));
        assertEquals("minecraft:steak", best.id());
        // food 10: the steak fills the most
        assertEquals("minecraft:steak", MealComparator.bestForHunger(hunger(10, 0f), List.of(apple, steak)).id());
    }

    @Test
    void bestForHungerTiesGoToTheLessWastefulFood() {
        FoodInfo small = food("small", 2, 0f);
        FoodInfo big = food("big", 8, 0f);
        // food 18: both give 2 effective points; the small one wastes nothing
        assertEquals("minecraft:small", MealComparator.bestForHunger(hunger(18, 0f), List.of(big, small)).id());
    }

    @Test
    void fullBarHasNoBestFood() {
        assertEquals(null, MealComparator.bestForHunger(hunger(20, 5f), List.of(apple, steak)));
    }
}
