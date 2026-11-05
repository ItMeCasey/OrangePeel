package com.itmecasey.orangepeel;

import java.nio.file.Path;
import java.util.function.Supplier;

/** Mod-wide constants and the one piece of loader state the shared code needs: where the config folder is. */
public final class OrangePeel {
    public static final String MODID = "orangepeel";

    private OrangePeel() {
    }

    /** Called once by each loader entry point. */
    public static void init(Supplier<Path> configFolder) {
        Config.load(configFolder.get().resolve("orangepeel-client.properties"));
    }
}
