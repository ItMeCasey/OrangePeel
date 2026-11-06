package com.itmecasey.orangepeel;

import java.lang.reflect.Field;
import java.util.UUID;
import java.util.function.Consumer;
import net.minecraft.world.food.FoodData;

/**
 * The server half of the exhaustion underlay. The game never sends a player's exhaustion to the client, so when this mod
 * runs on the server it reads the value and passes it, throttled, to the loader's network code, which sends it to that
 * player. A client that gets nothing simply keeps the underlay hidden.
 */
public final class ServerExhaustion {
    private static final ExhaustionThrottle THROTTLE = new ExhaustionThrottle();
    private static Field field;
    private static boolean looked;

    private ServerExhaustion() {
    }

    /** The player's exhaustion, or -1 when this game version does not let it be read. */
    static float read(FoodData food) {
        if (!looked) {
            looked = true;
            try {
                field = FoodData.class.getDeclaredField("exhaustionLevel");
                field.setAccessible(true);
            } catch (ReflectiveOperationException | RuntimeException e) {
                OrangePeel.LOGGER.warn("Cannot read the exhaustion level; the exhaustion underlay stays off", e);
            }
        }
        if (field == null) {
            return -1f;
        }
        try {
            return field.getFloat(food);
        } catch (ReflectiveOperationException | RuntimeException e) {
            field = null;
            return -1f;
        }
    }

    /** Called every tick for each server player; sends only when the throttle says so. */
    public static void onPlayerTick(UUID player, FoodData food, Consumer<Float> send) {
        if (!Config.sendExhaustion) {
            return;
        }
        float value = read(food);
        if (value >= 0f && THROTTLE.shouldSend(player, value)) {
            send.accept(value);
        }
    }

    /** A player left: forget them so their next join sends the value again. */
    public static void onPlayerLeft(UUID player) {
        THROTTLE.forget(player);
    }
}
