package com.itmecasey.orangepeel;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.UUID;
import org.junit.jupiter.api.Test;

class ExhaustionThrottleTest {
    private final UUID player = UUID.randomUUID();

    private static void ticks(ExhaustionThrottle t, UUID id, float value, int n) {
        for (int i = 0; i < n; i++) {
            t.shouldSend(id, value);
        }
    }

    @Test
    void firstCallSends() {
        assertTrue(new ExhaustionThrottle().shouldSend(player, 0f));
    }

    @Test
    void unchangedValueIsNeverResent() {
        ExhaustionThrottle t = new ExhaustionThrottle();
        t.shouldSend(player, 1.0f);
        for (int i = 0; i < 200; i++) {
            assertFalse(t.shouldSend(player, 1.0f));
        }
    }

    @Test
    void changeIsHeldUntilTheIntervalPasses() {
        ExhaustionThrottle t = new ExhaustionThrottle();
        t.shouldSend(player, 0f);
        for (int i = 1; i < ExhaustionThrottle.INTERVAL; i++) {
            assertFalse(t.shouldSend(player, 2f), "tick " + i);
        }
        assertTrue(t.shouldSend(player, 2f));
    }

    @Test
    void tinyChangeIsIgnored() {
        ExhaustionThrottle t = new ExhaustionThrottle();
        t.shouldSend(player, 1f);
        ticks(t, player, 1.01f, 50);
        assertFalse(t.shouldSend(player, 1.02f));
    }

    @Test
    void dropAfterTheGameSubtractsFourIsSent() {
        ExhaustionThrottle t = new ExhaustionThrottle();
        t.shouldSend(player, 3.9f);
        ticks(t, player, 0.1f, ExhaustionThrottle.INTERVAL - 1);
        assertTrue(t.shouldSend(player, 0.1f));
    }

    @Test
    void walkingSendsAtMostOncePerInterval() {
        ExhaustionThrottle t = new ExhaustionThrottle();
        int sent = 0;
        float value = 0f;
        for (int tick = 0; tick < 1200; tick++) {
            value += 0.01f;
            if (t.shouldSend(player, value)) {
                sent++;
            }
        }
        assertTrue(sent <= 1200 / ExhaustionThrottle.INTERVAL + 1, "sent " + sent);
        assertTrue(sent > 10);
    }

    @Test
    void playersAreIndependentAndForgettingResends() {
        ExhaustionThrottle t = new ExhaustionThrottle();
        UUID other = UUID.randomUUID();
        assertTrue(t.shouldSend(player, 0f));
        assertTrue(t.shouldSend(other, 0f));
        assertEquals(2, t.tracked());
        t.forget(player);
        assertEquals(1, t.tracked());
        assertTrue(t.shouldSend(player, 0f));
    }
}
