package com.itmecasey.orangepeel;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import org.junit.jupiter.api.Test;

class FoodMathTest {
    private static FoodInfo food(int nutrition, float saturation) {
        return new FoodInfo("minecraft:test", "test", nutrition, saturation, 32, List.of());
    }

    private static PlayerState player(int food, float sat, float exh, float hp) {
        return new PlayerState(food, sat, exh, hp, 20f, 0f, true);
    }

    @Test
    void wasteIsWhatOverflowsTheBar() {
        assertEquals(0, FoodMath.wastedHunger(10, 6));
        assertEquals(0, FoodMath.wastedHunger(14, 6));
        assertEquals(3, FoodMath.wastedHunger(17, 6));
        assertEquals(6, FoodMath.wastedHunger(20, 6));
    }

    @Test
    void saturationNeverExceedsFoodLevel() {
        assertEquals(10f, FoodMath.saturationAfter(8, 1f, 2, 12.8f), 1e-4);
        assertEquals(3.4f, FoodMath.saturationAfter(5, 1f, 4, 2.4f), 1e-4);
        assertEquals(20, FoodMath.foodAfter(19, 8));
    }

    @Test
    void noRegenBelowEighteen() {
        assertEquals(0f, FoodMath.regenHealth(17, 0f, 0f, 10f, 20f), 1e-4);
    }

    @Test
    void slowRegenAtEighteenHealsOnePerEightySeconds() {
        // 80 ticks per half-heart... one health point (a half heart) every 80 ticks, costing 6 exhaustion
        float gained = FoodMath.regenHealth(18, 0f, 0f, 10f, 20f);
        assertTrue(gained >= 1f, "gained " + gained);
        assertTrue(gained <= 10f);
    }

    @Test
    void fastRegenWithSaturationAtTwenty() {
        float fast = FoodMath.regenHealth(20, 10f, 0f, 10f, 20f);
        assertTrue(fast > 3f, "gained " + fast);
    }

    @Test
    void regenStopsAtMaxHealth() {
        assertEquals(2f, FoodMath.regenHealth(20, 20f, 0f, 18f, 20f), 1e-4);
        assertEquals(0f, FoodMath.regenHealth(20, 20f, 0f, 20f, 20f), 1e-4);
    }

    @Test
    void previewIsPositiveWhenFoodUnlocksRegen() {
        // food 12 cannot regenerate; a steak lifts it to 20 with saturation
        float gain = FoodMath.regenPreview(player(12, 0f, 0f, 10f), food(8, 12.8f));
        assertTrue(gain > 0f, "gain " + gain);
    }

    @Test
    void previewIsZeroAtFullHealth() {
        assertEquals(0f, FoodMath.regenPreview(player(12, 0f, 0f, 20f), food(8, 12.8f)), 1e-4);
    }

    @Test
    void fullnessGrowsWithTheFood() {
        PlayerState p = player(14, 0f, 0f, 20f);
        float apple = FoodMath.fullnessSeconds(p, food(4, 2.4f), 0.1f);
        float steak = FoodMath.fullnessSeconds(p, food(8, 12.8f), 0.1f);
        assertTrue(steak > apple, apple + " vs " + steak);
        assertEquals(0f, FoodMath.fullnessSeconds(player(2, 0f, 0f, 20f), food(4, 2.4f), 0.1f), 1e-4);
    }
}
