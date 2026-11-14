package com.itmecasey.orangepeel;

/**
 * The current setting values the shared code reads. The loader layers fill them from the loader's own config files
 * ({@code orangepeel-client.toml} and {@code orangepeel-common.toml}) whenever those load or reload; the keys, defaults and
 * ranges are listed in {@link Settings}. Tests set the fields directly.
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

    /** A setting's current value (Integer or Boolean), by its key in {@link Settings}. */
    public static Object get(String key) {
        return switch (key) {
            case "hudOpacity" -> hudOpacity;
            case "hudOffsetX" -> hudOffsetX;
            case "hudOffsetY" -> hudOffsetY;
            case "showHungerPreview" -> showHungerPreview;
            case "showSaturationOverlay" -> showSaturationOverlay;
            case "showExhaustionUnderlay" -> showExhaustionUnderlay;
            case "sendExhaustion" -> sendExhaustion;
            case "showHealthPreview" -> showHealthPreview;
            case "steadyPreview" -> steadyPreview;
            case "showTooltip" -> showTooltip;
            case "graphicalTooltip" -> graphicalTooltip;
            case "showComparator" -> showComparator;
            case "showWaste" -> showWaste;
            case "showEffects" -> showEffects;
            case "showFullness" -> showFullness;
            default -> throw new IllegalArgumentException("OrangePeel has no setting named " + key);
        };
    }

    /** Sets a setting (an Integer within its range, or a Boolean). Used by the loader layers and by scripts. */
    public static void set(String key, Object value) {
        Settings.Entry entry = Settings.entry(key);
        if (entry == null) {
            throw new IllegalArgumentException("OrangePeel has no setting named " + key);
        }
        if (entry.isNumber()) {
            if (!(value instanceof Number n)) {
                throw new IllegalArgumentException("OrangePeel setting " + key + " takes a number");
            }
            int v = Math.max(entry.min(), Math.min(entry.max(), (int) Math.round(n.doubleValue())));
            switch (key) {
                case "hudOpacity" -> hudOpacity = v;
                case "hudOffsetX" -> hudOffsetX = v;
                default -> hudOffsetY = v;
            }
            return;
        }
        if (!(value instanceof Boolean b)) {
            throw new IllegalArgumentException("OrangePeel setting " + key + " takes true or false");
        }
        switch (key) {
            case "showHungerPreview" -> showHungerPreview = b;
            case "showSaturationOverlay" -> showSaturationOverlay = b;
            case "showExhaustionUnderlay" -> showExhaustionUnderlay = b;
            case "sendExhaustion" -> sendExhaustion = b;
            case "showHealthPreview" -> showHealthPreview = b;
            case "steadyPreview" -> steadyPreview = b;
            case "showTooltip" -> showTooltip = b;
            case "graphicalTooltip" -> graphicalTooltip = b;
            case "showComparator" -> showComparator = b;
            case "showWaste" -> showWaste = b;
            case "showEffects" -> showEffects = b;
            default -> showFullness = b;
        }
    }
}
