package com.itmecasey.orangepeel;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Decides when the server sends a player's exhaustion to that player's client. It is called once per player per tick
 * and answers true on the first call, then only when at least {@link #INTERVAL} ticks have passed since the last send and
 * the value moved by at least {@link #MIN_DELTA}, so a walking player costs about two small packets a second at most.
 * Not thread safe: use it from the server thread only.
 */
public final class ExhaustionThrottle {
    /** Ticks between sends at the soonest. */
    public static final int INTERVAL = 5;
    /** Smallest change worth sending (a walked block adds 0.01 to 0.1). */
    public static final float MIN_DELTA = 0.05f;

    private static final class State {
        float last;
        int sinceSend;
    }

    private final Map<UUID, State> states = new HashMap<>();

    public boolean shouldSend(UUID player, float exhaustion) {
        State s = states.get(player);
        if (s == null) {
            s = new State();
            s.last = exhaustion;
            states.put(player, s);
            return true;
        }
        s.sinceSend++;
        if (s.sinceSend >= INTERVAL && Math.abs(exhaustion - s.last) >= MIN_DELTA) {
            s.last = exhaustion;
            s.sinceSend = 0;
            return true;
        }
        return false;
    }

    /** Forget a player (leaving the game), so the next join sends the value again. */
    public void forget(UUID player) {
        states.remove(player);
    }

    public int tracked() {
        return states.size();
    }
}
