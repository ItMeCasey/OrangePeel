package com.itmecasey.orangepeel;

/** Food and regeneration arithmetic. Pure, so it is unit tested without a running game. */
public final class FoodMath {
    public static final int MAX_FOOD = 20;
    /** Exhaustion that costs one saturation point (or one hunger point once saturation is gone). */
    public static final float EXHAUSTION_STEP = 4.0f;
    /** Rough average exhaustion per second over a normal mix of walking, mining, fighting and sprinting. */
    public static final float AVERAGE_EXHAUSTION_PER_SECOND = 0.1f;

    private FoodMath() {
    }

    /** Hunger points that would be lost because the bar is already (nearly) full. */
    public static int wastedHunger(int foodLevel, int nutrition) {
        return Math.max(0, foodLevel + nutrition - MAX_FOOD);
    }

    public static int foodAfter(int foodLevel, int nutrition) {
        return Math.min(MAX_FOOD, foodLevel + nutrition);
    }

    /** Saturation after eating: it can never exceed the new food level. */
    public static float saturationAfter(int foodLevel, float saturation, int nutrition, float foodSaturation) {
        return Math.min(saturation + foodSaturation, foodAfter(foodLevel, nutrition));
    }

    /**
     * Health that natural regeneration would restore if the player did nothing else, simulated tick by tick the way
     * the game's food tick works (fast regen at food 20 with saturation, slow regen at food 18 and up). Stops when
     * health is full or regeneration is no longer possible.
     */
    public static float regenHealth(int foodLevel, float saturation, float exhaustion, float health, float maxHealth) {
        float hp = health;
        float sat = saturation;
        float exh = exhaustion;
        int food = foodLevel;
        int timer = 0;
        for (int tick = 0; tick < 40000 && hp < maxHealth; tick++) {
            if (exh > EXHAUSTION_STEP) {
                exh -= EXHAUSTION_STEP;
                if (sat > 0) {
                    sat = Math.max(sat - 1, 0);
                } else {
                    food = Math.max(food - 1, 0);
                }
            }
            if (sat > 0 && food >= MAX_FOOD) {
                if (++timer >= 10) {
                    float spent = Math.min(sat, 6f);
                    hp = Math.min(maxHealth, hp + spent / 6f);
                    exh += spent;
                    timer = 0;
                }
            } else if (food >= 18) {
                if (++timer >= 80) {
                    hp = Math.min(maxHealth, hp + 1f);
                    exh += 6f;
                    timer = 0;
                }
            } else {
                break;
            }
        }
        return hp - health;
    }

    /** Extra health eating the food would give through regeneration, compared with not eating it. */
    public static float regenPreview(PlayerState p, FoodInfo food) {
        int after = foodAfter(p.foodLevel(), food.nutrition());
        float satAfter = saturationAfter(p.foodLevel(), p.saturation(), food.nutrition(), food.saturation());
        float now = regenHealth(p.foodLevel(), p.saturation(), p.exhaustion(), p.health(), p.maxHealth());
        float later = regenHealth(after, satAfter, p.exhaustion(), p.health(), p.maxHealth());
        return Math.max(0f, later - now);
    }

    /**
     * Seconds until the food level would drop below 18 (the point where regeneration stops) after eating the food,
     * at the given average exhaustion per second. Returns 0 when the player would not reach 18 at all.
     */
    public static float fullnessSeconds(PlayerState p, FoodInfo food, float exhaustionPerSecond) {
        int after = foodAfter(p.foodLevel(), food.nutrition());
        if (after < 18 || exhaustionPerSecond <= 0f) {
            return 0f;
        }
        float satAfter = saturationAfter(p.foodLevel(), p.saturation(), food.nutrition(), food.saturation());
        float budget = EXHAUSTION_STEP * (satAfter + (after - 17)) - p.exhaustion();
        return Math.max(0f, budget / exhaustionPerSecond);
    }
}
