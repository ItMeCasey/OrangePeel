package com.itmecasey.orangepeel;

import java.util.UUID;
import net.minecraft.world.food.FoodData;

/**
 * A small check that runs inside the real game when the environment variable ORANGEPEEL_SELFTEST is set (the server check
 * script sets it) and logs {@code SELFTEST DONE failures=N}. It covers what unit tests cannot: that the exhaustion level
 * can be read from the game's own FoodData on this Minecraft version, plus the throttle and food math the sync relies on.
 */
public final class SelfTest {
    private static int failures;

    private SelfTest() {
    }

    private static void check(boolean ok, String what) {
        if (!ok) {
            failures++;
            OrangePeel.LOGGER.error("SELFTEST FAILED {}", what);
        }
    }

    public static void run() {
        failures = 0;
        FoodData food = new FoodData();
        check(ServerExhaustion.read(food) == 0f, "exhaustion of a new FoodData reads 0");
        food.addExhaustion(1.5f);
        check(Math.abs(ServerExhaustion.read(food) - 1.5f) < 0.001f, "exhaustion added to FoodData reads back");

        ExhaustionThrottle throttle = new ExhaustionThrottle();
        UUID id = UUID.randomUUID();
        check(throttle.shouldSend(id, 0f), "throttle sends first value");
        check(!throttle.shouldSend(id, 0f), "throttle holds an unchanged value");

        check(FoodMath.foodAfter(18, 6) == 20, "food after eating is capped at 20");
        check(FoodMath.wastedHunger(18, 6) == 4, "wasted hunger counts the overflow");
        OrangePeel.LOGGER.info("SELFTEST DONE failures={}", failures);
    }
}
