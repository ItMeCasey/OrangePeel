package com.itmecasey.orangepeel.script;

import com.itmecasey.orangepeel.Config;
import com.itmecasey.orangepeel.FoodInfo;
import com.itmecasey.orangepeel.FoodMath;
import com.itmecasey.orangepeel.MealComparator;
import com.itmecasey.orangepeel.PlayerState;
import com.itmecasey.orangepeel.Settings;
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

    /** The names {@link #config} and {@link #setConfig} accept: the client settings, the keys of orangepeel-client.toml. */
    public List<String> configNames() {
        List<String> names = new ArrayList<>();
        for (Settings.Entry e : Settings.ENTRIES) {
            if (e.side() == Settings.Side.CLIENT) {
                names.add(e.key());
            }
        }
        return names;
    }

    /** A setting's current value: a number or a boolean. */
    public Object config(String name) {
        requireClientSetting(name);
        return Config.get(name);
    }

    /** Changes a setting until the game closes; the config file is not touched. */
    public void setConfig(String name, Object value) {
        requireClientSetting(name);
        Config.set(name, value);
    }

    private static void requireClientSetting(String name) {
        Settings.Entry e = Settings.entry(name);
        if (e == null || e.side() != Settings.Side.CLIENT) {
            throw new IllegalArgumentException("OrangePeel has no setting named " + name);
        }
    }
}
