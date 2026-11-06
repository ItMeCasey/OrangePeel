package com.itmecasey.orangepeel;

import com.mojang.logging.LogUtils;
import java.nio.file.Path;
import java.util.function.Supplier;
import org.slf4j.Logger;

/** Mod-wide constants and the one piece of loader state the shared code needs: where the config folder is. */
public final class OrangePeel {
    public static final String MODID = "orangepeel";
    public static final Logger LOGGER = LogUtils.getLogger();

    private OrangePeel() {
    }

    /** Called once by each loader entry point. */
    public static void init(Supplier<Path> configFolder) {
        Config.load(configFolder.get().resolve("orangepeel-client.properties"));
        LOGGER.info("OrangePeel loaded");
        if (System.getenv("ORANGEPEEL_SELFTEST") != null) {
            SelfTest.run();
        }
    }
}
