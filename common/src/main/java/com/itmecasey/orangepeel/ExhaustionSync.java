package com.itmecasey.orangepeel;

/**
 * The server's exhaustion value for the local player. The game never sends exhaustion to the client, so this stays 0
 * (and the exhaustion underlay stays hidden) unless the server also runs OrangePeel and sends it.
 */
public final class ExhaustionSync {
    private static volatile float value;

    private ExhaustionSync() {
    }

    public static float get() {
        return value;
    }

    public static void set(float exhaustion) {
        value = exhaustion;
    }
}
