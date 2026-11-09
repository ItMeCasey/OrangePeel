package com.itmecasey.orangepeel;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * The server half of the exhaustion sync with no client and no network: what a dedicated server does each tick for each
 * player. Reading the value from the game's FoodData is covered by the server self test, which runs in the real game; the
 * packet itself is the loader's, and a client receiving it was seen in singleplayer (same channel on the integrated server).
 */
class ExhaustionSenderTest {
    private final UUID player = UUID.randomUUID();
    private final List<Float> sent = new ArrayList<>();

    @BeforeEach
    void enable() {
        Config.sendExhaustion = true;
        ExhaustionSender.forget(player);
    }

    @AfterEach
    void cleanUp() {
        ExhaustionSender.forget(player);
        Config.sendExhaustion = true;
    }

    private float exhaustion;

    private void tick(int times) {
        for (int i = 0; i < times; i++) {
            ExhaustionSender.tick(player, exhaustion, sent::add);
        }
    }

    @Test
    void joiningPlayerGetsTheCurrentValueOnce() {
        exhaustion = 1f;
        tick(40);
        assertEquals(List.of(1f), sent);
    }

    @Test
    void aChangeIsSentWithinTheInterval() {
        tick(1);
        exhaustion = 1.5f;
        tick(ExhaustionThrottle.INTERVAL);
        assertEquals(2, sent.size());
        assertEquals(1.5f, sent.get(1), 0.0001f);
    }

    @Test
    void theSwitchTurnsSendingOff() {
        Config.sendExhaustion = false;
        exhaustion = 3f;
        tick(40);
        assertTrue(sent.isEmpty());
    }

    @Test
    void aPlayerWhoLeavesAndReturnsIsSentTheValueAgain() {
        tick(20);
        ExhaustionSender.forget(player);
        tick(1);
        assertEquals(2, sent.size());
    }

    @Test
    void theGameSubtractingFourOnceSaturationIsUsedIsSent() {
        exhaustion = 3.9f;
        tick(1);
        exhaustion = 0.1f;
        tick(ExhaustionThrottle.INTERVAL);
        assertEquals(List.of(3.9f, 0.1f), sent);
    }
}
