package com.itmecasey.orangepeel;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

class SettingsTest {
    @AfterEach
    void restore() {
        for (Settings.Entry e : Settings.ENTRIES) {
            Config.set(e.key(), e.def());
        }
    }

    @Test
    void keysAreUniqueAndDefaultsMatchTheFields() {
        Set<String> seen = new HashSet<>();
        for (Settings.Entry e : Settings.ENTRIES) {
            assertTrue(seen.add(e.key()), "duplicate " + e.key());
            assertEquals(e.def(), Config.get(e.key()), e.key() + " default and field start value");
            assertTrue(e.comment() != null && !e.comment().isBlank(), e.key() + " has a comment");
            if (e.isNumber()) {
                int d = (Integer) e.def();
                assertTrue(d >= e.min() && d <= e.max(), e.key() + " default inside its range");
            }
        }
    }

    @Test
    void onlySendExhaustionIsACommonSetting() {
        for (Settings.Entry e : Settings.ENTRIES) {
            assertEquals(e.key().equals("sendExhaustion") ? Settings.Side.COMMON : Settings.Side.CLIENT, e.side(), e.key());
        }
    }

    @Test
    void pushCopiesOneSideIntoConfig() {
        Settings.push(Settings.Side.CLIENT, key -> switch (key) {
            case "hudOpacity" -> 42;
            case "showTooltip" -> false;
            case "sendExhaustion" -> false;
            default -> null;
        });
        assertEquals(42, Config.hudOpacity);
        assertEquals(false, Config.showTooltip);
        assertEquals(true, Config.sendExhaustion, "the common key is not part of the client push");
        Settings.push(Settings.Side.COMMON, key -> key.equals("sendExhaustion") ? (Object) false : null);
        assertEquals(false, Config.sendExhaustion);
    }

    @Test
    void setClampsNumbersAndRejectsTheWrongKind() {
        Config.set("hudOpacity", 250);
        assertEquals(100, Config.hudOpacity);
        Config.set("hudOffsetX", -9000.4);
        assertEquals(-400, Config.hudOffsetX);
        assertThrows(IllegalArgumentException.class, () -> Config.set("hudOpacity", true));
        assertThrows(IllegalArgumentException.class, () -> Config.set("showTooltip", 1));
        assertThrows(IllegalArgumentException.class, () -> Config.set("nope", 1));
        assertThrows(IllegalArgumentException.class, () -> Config.get("nope"));
    }

    @Test
    void defaultsMapHasEveryKey() {
        Map<String, Object> d = Settings.defaults();
        assertEquals(Settings.ENTRIES.size(), d.size());
        assertEquals(15, d.size());
    }
}
