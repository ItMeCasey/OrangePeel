package com.itmecasey.orangepeel;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

/**
 * Client settings, kept in a plain properties file so every loader and Minecraft version reads the same one.
 * Edit {@code config/orangepeel-client.properties} and restart (or rejoin) to apply.
 */
public final class Config {
    /** HUD opacity, 0 to 100 percent. */
    public static volatile int hudOpacity = 100;
    /** Moves the overlays relative to the vanilla food bar, in GUI pixels. */
    public static volatile int hudOffsetX = 0;
    public static volatile int hudOffsetY = 0;

    public static volatile boolean showHungerPreview = true;
    public static volatile boolean showSaturationOverlay = true;
    public static volatile boolean showExhaustionUnderlay = true;
    /** Server side: send each player's exhaustion to that player's client (needed for the underlay on a server). */
    public static volatile boolean sendExhaustion = true;
    public static volatile boolean showHealthPreview = true;
    /** true keeps the hunger preview at a steady opacity instead of pulsing. */
    public static volatile boolean steadyPreview = false;

    public static volatile boolean showTooltip = true;
    /** true draws shanks, saturation, hearts and a comparison bar in the tooltip; false keeps text lines only. */
    public static volatile boolean graphicalTooltip = true;
    public static volatile boolean showComparator = true;
    public static volatile boolean showWaste = true;
    public static volatile boolean showEffects = true;
    public static volatile boolean showFullness = true;

    private Config() {
    }

    public static float opacity() {
        return Math.max(0, Math.min(100, hudOpacity)) / 100f;
    }

    /** Reads the file (creating it with the defaults when missing). Bad values keep their defaults. */
    public static void load(Path file) {
        Properties p = new Properties();
        if (Files.isRegularFile(file)) {
            try (Reader r = Files.newBufferedReader(file)) {
                p.load(r);
            } catch (IOException | IllegalArgumentException e) {
                return;
            }
        }
        apply(p);
        try {
            Files.createDirectories(file.getParent());
            try (Writer w = Files.newBufferedWriter(file)) {
                w.write("# OrangePeel client settings. true/false switches; hudOpacity is 0-100.\n");
                w.write("# hudOffsetX/hudOffsetY nudge the overlays from the vanilla food bar, in GUI pixels.\n");
                p.store(w, null);
            }
        } catch (IOException ignored) {
            // read-only config folder: run with the values we have
        }
    }

    /** Copies every known key from the properties into the fields; unknown or malformed keys are skipped. */
    public static void apply(Properties p) {
        hudOpacity = clamp(intOf(p, "hudOpacity", hudOpacity), 0, 100);
        hudOffsetX = clamp(intOf(p, "hudOffsetX", hudOffsetX), -400, 400);
        hudOffsetY = clamp(intOf(p, "hudOffsetY", hudOffsetY), -400, 400);
        showHungerPreview = boolOf(p, "showHungerPreview", showHungerPreview);
        showSaturationOverlay = boolOf(p, "showSaturationOverlay", showSaturationOverlay);
        showExhaustionUnderlay = boolOf(p, "showExhaustionUnderlay", showExhaustionUnderlay);
        sendExhaustion = boolOf(p, "sendExhaustion", sendExhaustion);
        showHealthPreview = boolOf(p, "showHealthPreview", showHealthPreview);
        steadyPreview = boolOf(p, "steadyPreview", steadyPreview);
        showTooltip = boolOf(p, "showTooltip", showTooltip);
        graphicalTooltip = boolOf(p, "graphicalTooltip", graphicalTooltip);
        showComparator = boolOf(p, "showComparator", showComparator);
        showWaste = boolOf(p, "showWaste", showWaste);
        showEffects = boolOf(p, "showEffects", showEffects);
        showFullness = boolOf(p, "showFullness", showFullness);
        p.setProperty("hudOpacity", Integer.toString(hudOpacity));
        p.setProperty("hudOffsetX", Integer.toString(hudOffsetX));
        p.setProperty("hudOffsetY", Integer.toString(hudOffsetY));
        p.setProperty("showHungerPreview", Boolean.toString(showHungerPreview));
        p.setProperty("showSaturationOverlay", Boolean.toString(showSaturationOverlay));
        p.setProperty("showExhaustionUnderlay", Boolean.toString(showExhaustionUnderlay));
        p.setProperty("sendExhaustion", Boolean.toString(sendExhaustion));
        p.setProperty("showHealthPreview", Boolean.toString(showHealthPreview));
        p.setProperty("steadyPreview", Boolean.toString(steadyPreview));
        p.setProperty("showTooltip", Boolean.toString(showTooltip));
        p.setProperty("graphicalTooltip", Boolean.toString(graphicalTooltip));
        p.setProperty("showComparator", Boolean.toString(showComparator));
        p.setProperty("showWaste", Boolean.toString(showWaste));
        p.setProperty("showEffects", Boolean.toString(showEffects));
        p.setProperty("showFullness", Boolean.toString(showFullness));
    }

    private static int intOf(Properties p, String key, int fallback) {
        try {
            return Integer.parseInt(p.getProperty(key, "").trim());
        } catch (NumberFormatException e) {
            return fallback;
        }
    }

    private static boolean boolOf(Properties p, String key, boolean fallback) {
        String v = p.getProperty(key);
        if (v == null) {
            return fallback;
        }
        v = v.trim();
        if (v.equalsIgnoreCase("true")) {
            return true;
        }
        if (v.equalsIgnoreCase("false")) {
            return false;
        }
        return fallback;
    }

    private static int clamp(int v, int lo, int hi) {
        return Math.max(lo, Math.min(hi, v));
    }
}
