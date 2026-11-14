package com.itmecasey.orangepeel.neoforge;

import com.itmecasey.orangepeel.ConfigMigration;
import com.itmecasey.orangepeel.OrangePeel;
import com.itmecasey.orangepeel.Settings;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.config.IConfigSpec;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.config.ModConfigEvent;
import net.neoforged.neoforge.common.ModConfigSpec;

/**
 * The mod's settings as NeoForge config files: {@code orangepeel-client.toml} (everything a player sets) and
 * {@code orangepeel-common.toml} (what a server decides). Both are built from {@link Settings}, and their values are pushed into
 * the shared {@code Config} whenever they load or reload.
 *
 * <p>When a .toml does not exist yet and the pre-2026.11.5 {@code orangepeel-client.properties} does, the old values are
 * imported once, and the old file is then moved to a .back copy.
 */
final class NeoForgeConfig {
    static final ModConfigSpec CLIENT_SPEC;
    static final ModConfigSpec COMMON_SPEC;
    private static Path configDir;
    private static final java.util.Set<Settings.Side> PENDING = java.util.EnumSet.noneOf(Settings.Side.class);
    private static final Map<Settings.Side, Path> IMPORTED = new java.util.EnumMap<>(Settings.Side.class);
    private static final Map<String, ModConfigSpec.BooleanValue> FLAGS = new HashMap<>();
    private static final Map<String, ModConfigSpec.IntValue> NUMBERS = new HashMap<>();

    static {
        ModConfigSpec.Builder client = new ModConfigSpec.Builder();
        ModConfigSpec.Builder common = new ModConfigSpec.Builder();
        for (Settings.Entry e : Settings.ENTRIES) {
            ModConfigSpec.Builder b = e.side() == Settings.Side.CLIENT ? client : common;
            b.comment(e.comment());
            if (e.isNumber()) {
                NUMBERS.put(e.key(), b.defineInRange(e.key(), (Integer) e.def(), e.min(), e.max()));
            } else {
                FLAGS.put(e.key(), b.define(e.key(), ((Boolean) e.def()).booleanValue()));
            }
        }
        CLIENT_SPEC = client.build();
        COMMON_SPEC = common.build();
    }

    private NeoForgeConfig() {
    }

    static void register(IEventBus modBus, ModContainer container, Path configDir) {
        boolean freshClient = !Files.exists(configDir.resolve("orangepeel-client.toml"));
        boolean freshCommon = !Files.exists(configDir.resolve("orangepeel-common.toml"));
        ConfigMigration.Result old = (freshClient || freshCommon)
                ? ConfigMigration.read(configDir.resolve(ConfigMigration.OLD_FILE)) : null;
        NeoForgeConfig.configDir = configDir;
        if (old != null && old.found()) {
            if (freshClient) {
                PENDING.add(Settings.Side.CLIENT);
            }
            if (freshCommon) {
                PENDING.add(Settings.Side.COMMON);
            }
        }
        container.registerConfig(ModConfig.Type.CLIENT, CLIENT_SPEC);
        container.registerConfig(ModConfig.Type.COMMON, COMMON_SPEC);
        modBus.addListener((ModConfigEvent.Loading e) -> onConfig(e.getConfig().getSpec(), freshClient, freshCommon, old));
        modBus.addListener((ModConfigEvent.Reloading e) -> onConfig(e.getConfig().getSpec(), false, false, null));
    }

    private static synchronized void onConfig(IConfigSpec spec, boolean freshClient, boolean freshCommon, ConfigMigration.Result old) {
        Settings.Side side;
        boolean fresh;
        if (spec == CLIENT_SPEC) {
            side = Settings.Side.CLIENT;
            fresh = freshClient;
        } else if (spec == COMMON_SPEC) {
            side = Settings.Side.COMMON;
            fresh = freshCommon;
        } else {
            return;
        }
        if (fresh && old != null && old.found()) {
            int imported = Settings.apply(old, side, NeoForgeConfig::set);
            ((ModConfigSpec) spec).save();
            OrangePeel.LOGGER.info("OrangePeel: imported {} {} setting(s) from {}", imported,
                    side.name().toLowerCase(), ConfigMigration.OLD_FILE);
            for (String line : old.messages()) {
                OrangePeel.LOGGER.info("OrangePeel: config import: {}", line);
            }
            IMPORTED.put(side, configDir.resolve("orangepeel-" + side.name().toLowerCase() + ".toml"));
            PENDING.remove(side);
            if (PENDING.isEmpty()) {
                ConfigMigration.retire(configDir.resolve(ConfigMigration.OLD_FILE), old, IMPORTED, ConfigMigration.modVersion(),
                        java.time.LocalDate.now().toString(), line -> OrangePeel.LOGGER.info("OrangePeel: config import: {}", line));
            }
        }
        Settings.push(side, NeoForgeConfig::value);
    }

    private static void set(String key, Object value) {
        if (NUMBERS.containsKey(key)) {
            NUMBERS.get(key).set((Integer) value);
        } else {
            FLAGS.get(key).set((Boolean) value);
        }
    }

    private static Object value(String key) {
        return NUMBERS.containsKey(key) ? NUMBERS.get(key).get() : FLAGS.get(key).get();
    }
}
