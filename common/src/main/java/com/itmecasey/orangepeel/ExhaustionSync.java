package com.itmecasey.orangepeel;

/**
 * The server's exhaustion value for the local player. The game never sends exhaustion to the client, so this stays 0
 * (and the exhaustion underlay stays hidden) unless the server also runs OrangePeel and sends it (see {@link ServerExhaustion}).
 */
public final class ExhaustionSync {
    private static volatile float value;
    private static volatile boolean received;

    private ExhaustionSync() {
    }

    public static float get() {
        return value;
    }

    public static void set(float exhaustion) {
        value = exhaustion;
        if (!received) {
            received = true;
            OrangePeel.LOGGER.info("OrangePeel: received the first exhaustion update from the server ({})", exhaustion);
        }
    }

    /** Leaving a world: the next server may not send exhaustion, so show none. */
    public static void reset() {
        value = 0f;
        received = false;
    }
}
