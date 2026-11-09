package com.itmecasey.orangepeel;

import java.util.UUID;
import java.util.function.Consumer;

/**
 * What a server does each tick for each player with the player's exhaustion value in hand: obey the config switch and the
 * throttle, and hand the value to the loader's network code to send. It has no Minecraft types, so a dedicated server's
 * behavior can be unit tested without a game.
 */
public final class ExhaustionSender {
    private static final ExhaustionThrottle THROTTLE = new ExhaustionThrottle();

    private ExhaustionSender() {
    }

    public static void tick(UUID player, float exhaustion, Consumer<Float> send) {
        if (Config.sendExhaustion && THROTTLE.shouldSend(player, exhaustion)) {
            send.accept(exhaustion);
        }
    }

    public static void forget(UUID player) {
        THROTTLE.forget(player);
    }
}
