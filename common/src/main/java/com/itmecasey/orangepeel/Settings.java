package com.itmecasey.orangepeel;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Function;

/**
 * Every setting of the mod in one list: key, side, default, range and the comment written into the .toml file. The loader
 * layers build their NeoForge {@code ModConfigSpec} and Forge {@code ForgeConfigSpec} from this list, and push the values
 * back into {@link Config} when the file loads or reloads, so the shared code and its tests never touch a loader class.
 */
public final class Settings {
    /** CLIENT settings live in {@code orangepeel-client.toml}, COMMON ones in {@code orangepeel-common.toml}. */
    public enum Side { CLIENT, COMMON }

    /**
     * One setting. A number setting has a range; a switch has {@code min == max == 0} and a Boolean default.
     *
     * @param def Integer for a number, Boolean for a switch
     */
    public record Entry(String key, Side side, Object def, int min, int max, String comment) {
        public boolean isNumber() {
            return def instanceof Integer;
        }
    }

    private static Entry number(String key, int def, int min, int max, String comment) {
        return new Entry(key, Side.CLIENT, def, min, max, comment);
    }

    private static Entry flag(String key, Side side, boolean def, String comment) {
        return new Entry(key, side, def, 0, 0, comment);
    }

    public static final List<Entry> ENTRIES = List.of(
            number("hudOpacity", 100, 0, 100, "Opacity of the hunger bar overlays, 0 to 100 percent."),
            number("hudOffsetX", 0, -400, 400, "Moves the overlays right (positive) or left, in GUI pixels."),
            number("hudOffsetY", 0, -400, 400, "Moves the overlays down (positive) or up, in GUI pixels."),
            flag("showHungerPreview", Side.CLIENT, true, "The pulsing preview of the hunger the held food restores."),
            flag("showSaturationOverlay", Side.CLIENT, true, "The golden rails around the shanks that saturation covers."),
            flag("showExhaustionUnderlay", Side.CLIENT, true,
                    "The veil over the shanks for exhaustion. The server has to send exhaustion, so it needs OrangePeel there."),
            flag("showHealthPreview", Side.CLIENT, true, "The preview of the health regeneration the held food adds."),
            flag("steadyPreview", Side.CLIENT, false, "Keeps the hunger preview at a steady opacity instead of pulsing."),
            flag("showTooltip", Side.CLIENT, true, "The food tooltip lines."),
            flag("graphicalTooltip", Side.CLIENT, true,
                    "Draws shanks, saturation, hearts and a comparison bar in the tooltip; false keeps text lines only."),
            flag("showComparator", Side.CLIENT, true, "How a food ranks against the other food you carry."),
            flag("showWaste", Side.CLIENT, true, "The warning when a food would waste hunger."),
            flag("showEffects", Side.CLIENT, true, "The effects a food applies, with their levels and durations."),
            flag("showFullness", Side.CLIENT, true, "How long a food keeps you full."),
            flag("sendExhaustion", Side.COMMON, true,
                    "Server side: send each player's exhaustion to that player's client (needed for the exhaustion veil)."));

    private Settings() {
    }

    public static Entry entry(String key) {
        for (Entry e : ENTRIES) {
            if (e.key().equals(key)) {
                return e;
            }
        }
        return null;
    }

    /** Every default, by key. */
    public static Map<String, Object> defaults() {
        Map<String, Object> out = new LinkedHashMap<>();
        for (Entry e : ENTRIES) {
            out.put(e.key(), e.def());
        }
        return out;
    }

    /** Copies the values of one side into {@link Config}. {@code values} answers the current value of a key. */
    public static void push(Side side, Function<String, Object> values) {
        for (Entry e : ENTRIES) {
            if (e.side() == side) {
                Object v = values.apply(e.key());
                if (v != null) {
                    Config.set(e.key(), v);
                }
            }
        }
    }

    /**
     * Hands the migrated old values of one side to {@code setter} (the loader's config values). Returns how many were set.
     * Only keys of that side are set; everything else keeps the default the new file was created with.
     */
    public static int apply(ConfigMigration.Result old, Side side, BiConsumer<String, Object> setter) {
        int count = 0;
        for (Map.Entry<String, Object> v : old.values().entrySet()) {
            Entry e = entry(v.getKey());
            if (e != null && e.side() == side) {
                setter.accept(v.getKey(), v.getValue());
                count++;
            }
        }
        return count;
    }
}
