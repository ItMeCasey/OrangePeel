package com.itmecasey.orangepeel;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.itmecasey.modjs.script.ExtraBindings;
import com.itmecasey.modjs.script.ScriptType;
import com.itmecasey.orangepeel.script.ModJsBridge;
import com.itmecasey.orangepeel.script.ScriptApi;
import com.itmecasey.orangepeel.script.ScriptState;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.ScriptableObject;

/** Runs real JavaScript (Rhino, the engine ModJS uses) against the OrangePeel global through the reflective bridge. */
class ScriptApiTest {
    private static final FoodInfo STEAK = new FoodInfo("minecraft:cooked_beef", "Steak", 8, 12.8f, 32, List.of());
    private static final FoodInfo APPLE = new FoodInfo("minecraft:apple", "Apple", 4, 2.4f, 32, List.of());
    private static final FoodInfo BREAD = new FoodInfo("minecraft:bread", "Bread", 5, 6f, 32, List.of());
    private static final PlayerState HUNGRY = new PlayerState(10, 2f, 1.5f, 20f, 20f, 0f, true);

    @BeforeEach
    void setUp() {
        ExtraBindings.clear();
        ScriptApi.clearTooltipFunctions();
        Config.showHungerPreview = true;
        Config.hudOpacity = 100;
        ScriptApi.attach(new ScriptState() {
            public PlayerState player() {
                return HUNGRY;
            }

            public FoodInfo held() {
                return APPLE;
            }

            public List<FoodInfo> carried() {
                return List.of(STEAK, APPLE, BREAD);
            }
        });
        assertTrue(ModJsBridge.install(ScriptApi.INSTANCE));
    }

    @AfterEach
    void tearDown() {
        ExtraBindings.clear();
        ScriptApi.clearTooltipFunctions();
        Config.showHungerPreview = true;
        Config.hudOpacity = 100;
    }

    private static Object run(ScriptType type, String script) {
        Context cx = Context.enter();
        try {
            cx.setLanguageVersion(Context.VERSION_ES6);
            cx.getWrapFactory().setJavaPrimitiveWrap(false);
            ScriptableObject scope = cx.initStandardObjects();
            ExtraBindings.install(type, cx, scope);
            return cx.evaluateString(scope, script, "test", 1, null);
        } finally {
            Context.exit();
        }
    }

    @Test
    void readsHeldFoodPlayerAndRanking() {
        assertEquals("Apple", run(ScriptType.CLIENT, "OrangePeel.heldFood().name()"));
        assertEquals(10, ((Number) run(ScriptType.CLIENT, "OrangePeel.player().foodLevel()")).intValue());
        assertEquals(3, ((Number) run(ScriptType.CLIENT, "OrangePeel.carriedFoods().size()")).intValue());
        assertEquals(2, ((Number) run(ScriptType.CLIENT, "OrangePeel.ranking().others()")).intValue());
        assertEquals("Steak", run(ScriptType.CLIENT, "OrangePeel.ranking().bestFood().name()"));
        assertEquals(0, ((Number) run(ScriptType.CLIENT, "OrangePeel.wasted(OrangePeel.heldFood())")).intValue());
    }

    @Test
    void tooltipFunctionsAddLinesAndAFailingOneIsSkipped() {
        run(ScriptType.CLIENT, "OrangePeel.onFoodTooltip(f => { throw new Error('boom'); });"
                + "OrangePeel.onFoodTooltip(f => f.nutrition() >= 8 ? ['Hearty', 'Filling'] : null);"
                + "OrangePeel.onFoodTooltip(f => 'Tastes like ' + f.name());");
        assertEquals(List.of("Hearty", "Filling", "Tastes like Steak"), ScriptApi.tooltipLines(STEAK));
        assertEquals(List.of("Tastes like Apple"), ScriptApi.tooltipLines(APPLE));
    }

    @Test
    void settingsReadAndChangeForTheSessionOnly() {
        assertEquals(Boolean.TRUE, run(ScriptType.CLIENT, "OrangePeel.config('showHungerPreview')"));
        run(ScriptType.CLIENT, "OrangePeel.setConfig('showHungerPreview', false); OrangePeel.setConfig('hudOpacity', 250);");
        assertFalse(Config.showHungerPreview);
        assertEquals(100, Config.hudOpacity);
        assertEquals(14, ((Number) run(ScriptType.CLIENT, "OrangePeel.configNames().size()")).intValue());
    }

    @Test
    void unknownSettingIsAScriptErrorNotAGameError() {
        Object result = run(ScriptType.CLIENT, "try { OrangePeel.config('nope'); 'no error' } catch (e) { 'caught' }");
        assertEquals("caught", String.valueOf(result));
    }

    @Test
    void theGlobalExistsInClientScriptsOnly() {
        assertEquals("object", String.valueOf(run(ScriptType.CLIENT, "typeof OrangePeel")));
        assertEquals("undefined", String.valueOf(run(ScriptType.SERVER, "typeof OrangePeel")));
        assertEquals("undefined", String.valueOf(run(ScriptType.STARTUP, "typeof OrangePeel")));
    }

    @Test
    void withoutModJsNothingIsInstalledAndNothingFails() {
        assertFalse(ModJsBridge.install(ScriptApi.INSTANCE, "com.itmecasey.modjs.script.NoSuchProvider",
                "com.itmecasey.modjs.script.NoSuchExtraBindings"));
        assertTrue(ScriptApi.tooltipLines(STEAK).isEmpty());
    }
}
