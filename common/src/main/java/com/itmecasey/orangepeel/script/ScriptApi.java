package com.itmecasey.orangepeel.script;

import com.itmecasey.orangepeel.Config;
import com.itmecasey.orangepeel.FoodInfo;
import com.itmecasey.orangepeel.FoodMath;
import com.itmecasey.orangepeel.MealComparator;
import com.itmecasey.orangepeel.PlayerState;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Function;
import java.util.logging.Logger;

/**
 * The scripting surface, exposed to ModJS client scripts as {@code OrangePeel}. Everything here is read-only except
 * {@link #onFoodTooltip} (adds lines) and {@link #setConfig} (changes a setting for this session, not saved). Values are
 * plain numbers, strings and the small records of this mod, so any script engine can use them. A script that throws is
 * logged once and skipped; it never reaches the game.
 *
 * <pre>{@code
 * OrangePeel.onFoodTooltip(food => food.nutrition() >= 8 ? "Hearty" : null);
 * }</pre>
 */
public final class ScriptApi {
    public static final ScriptApi INSTANCE = new ScriptApi();
    private static final Logger LOG = Logger.getLogger("orangepeel");

    private static volatile ScriptState state;
    private static final List<Function<FoodInfo, Object>> TOOLTIP = new CopyOnWriteArrayList<>();
    private static final Set<Object> REPORTED = ConcurrentHashMap.newKeySet();

    /** The client wires this up once the game's food reader exists. */
    public static void attach(ScriptState s) {
        state = s;
    }

    // --- events -------------------------------------------------------------------------------------------------

    /**
     * Adds lines to the tooltip of every food. {@code fn} gets the food and returns a string, an array of strings, or
     * nothing. It runs on the client thread each time a food tooltip is built, so keep it quick.
     */
    public void onFoodTooltip(Function<FoodInfo, Object> fn) {
        if (fn != null) {
            TOOLTIP.add(fn);
        }
    }

    /** Used by OrangePeel: the lines the scripts add for this food. */
    public static List<String> tooltipLines(FoodInfo food) {
        List<String> out = new ArrayList<>();
        for (Function<FoodInfo, Object> fn : TOOLTIP) {
            try {
                collect(fn.apply(food), out);
            } catch (Throwable t) {
                if (REPORTED.add(fn)) {
                    LOG.warning("OrangePeel: a script tooltip function failed and is skipped: " + t);
                }
            }
        }
        return out;
    }

    /** Removes every registered tooltip function (used by tests and when scripts reload). */
    public static void clearTooltipFunctions() {
        TOOLTIP.clear();
        REPORTED.clear();
    }

    private static void collect(Object result, List<String> out) {
        if (result == null || result.getClass().getName().endsWith("Undefined")) {
            return;
        }
        if (result instanceof Iterable<?> items) {
            for (Object item : items) {
                collect(item, out);
            }
        } else if (result instanceof Object[] items) {
            for (Object item : items) {
                collect(item, out);
            }
        } else {
            String text = result.toString();
            if (!text.isEmpty()) {
                out.add(text);
            }
        }
    }

    // --- state --------------------------------------------------------------------------------------------------

    /** Hunger, saturation, exhaustion, health and absorption of the local player, or null outside a world. */
    public PlayerState player() {
        ScriptState s = state;
        return s == null ? null : s.player();
    }

    /** The food in the player's hands, or null. */
    public FoodInfo heldFood() {
        ScriptState s = state;
        return s == null ? null : s.held();
    }

    /** The foods in the player's inventory, one per stack. */
    public List<FoodInfo> carriedFoods() {
        ScriptState s = state;
        return s == null ? List.of() : s.carried();
    }

    /** How the held food ranks against the carried foods, or null when no food is held. */
    public MealComparator.Result ranking() {
        FoodInfo held = heldFood();
        return held == null ? null : ranking(held);
    }

    /** How this food ranks against the carried foods. */
    public MealComparator.Result ranking(FoodInfo food) {
        return MealComparator.compare(food, carriedFoods());
    }

    /** Hunger points of this food that would be wasted if eaten now (0 outside a world). */
    public int wasted(FoodInfo food) {
        PlayerState p = player();
        return p == null ? 0 : FoodMath.wastedHunger(p.foodLevel(), food.nutrition());
    }

    // --- settings -----------------------------------------------------------------------------------------------

    private static final List<String> CONFIG = List.of("hudOpacity", "hudOffsetX", "hudOffsetY", "showHungerPreview",
            "showSaturationOverlay", "showExhaustionUnderlay", "showHealthPreview", "steadyPreview", "showTooltip",
            "graphicalTooltip", "showComparator", "showWaste", "showEffects", "showFullness");

    /** The names {@link #config} and {@link #setConfig} accept (the keys of orangepeel-client.properties). */
    public List<String> configNames() {
        return CONFIG;
    }

    /** A setting's current value: a number or a boolean. */
    public Object config(String name) {
        return switch (name) {
            case "hudOpacity" -> Config.hudOpacity;
            case "hudOffsetX" -> Config.hudOffsetX;
            case "hudOffsetY" -> Config.hudOffsetY;
            case "showHungerPreview" -> Config.showHungerPreview;
            case "showSaturationOverlay" -> Config.showSaturationOverlay;
            case "showExhaustionUnderlay" -> Config.showExhaustionUnderlay;
            case "showHealthPreview" -> Config.showHealthPreview;
            case "steadyPreview" -> Config.steadyPreview;
            case "showTooltip" -> Config.showTooltip;
            case "graphicalTooltip" -> Config.graphicalTooltip;
            case "showComparator" -> Config.showComparator;
            case "showWaste" -> Config.showWaste;
            case "showEffects" -> Config.showEffects;
            case "showFullness" -> Config.showFullness;
            default -> throw new IllegalArgumentException("OrangePeel has no setting named " + name);
        };
    }

    /** Changes a setting until the game closes; the config file is not touched. */
    public void setConfig(String name, Object value) {
        switch (name) {
            case "hudOpacity" -> Config.hudOpacity = Math.max(0, Math.min(100, number(name, value)));
            case "hudOffsetX" -> Config.hudOffsetX = number(name, value);
            case "hudOffsetY" -> Config.hudOffsetY = number(name, value);
            case "showHungerPreview" -> Config.showHungerPreview = flag(name, value);
            case "showSaturationOverlay" -> Config.showSaturationOverlay = flag(name, value);
            case "showExhaustionUnderlay" -> Config.showExhaustionUnderlay = flag(name, value);
            case "showHealthPreview" -> Config.showHealthPreview = flag(name, value);
            case "steadyPreview" -> Config.steadyPreview = flag(name, value);
            case "showTooltip" -> Config.showTooltip = flag(name, value);
            case "graphicalTooltip" -> Config.graphicalTooltip = flag(name, value);
            case "showComparator" -> Config.showComparator = flag(name, value);
            case "showWaste" -> Config.showWaste = flag(name, value);
            case "showEffects" -> Config.showEffects = flag(name, value);
            case "showFullness" -> Config.showFullness = flag(name, value);
            default -> throw new IllegalArgumentException("OrangePeel has no setting named " + name);
        }
    }

    private static int number(String name, Object value) {
        if (value instanceof Number n) {
            return (int) Math.round(n.doubleValue());
        }
        throw new IllegalArgumentException("OrangePeel setting " + name + " takes a number");
    }

    private static boolean flag(String name, Object value) {
        if (value instanceof Boolean b) {
            return b;
        }
        throw new IllegalArgumentException("OrangePeel setting " + name + " takes true or false");
    }
}
