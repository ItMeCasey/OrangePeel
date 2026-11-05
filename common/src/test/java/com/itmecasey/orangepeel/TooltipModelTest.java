package com.itmecasey.orangepeel;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import org.junit.jupiter.api.Test;

class TooltipModelTest {
    private static FoodInfo food(String id, int nutrition, float saturation, EffectInfo... effects) {
        return new FoodInfo("minecraft:" + id, id, nutrition, saturation, 32, List.of(effects));
    }

    private static boolean has(List<TooltipModel.Line> lines, String fragment, TooltipModel.Tone tone) {
        return lines.stream().anyMatch(l -> l.text().contains(fragment) && l.tone() == tone);
    }

    @Test
    void firstLineShowsHungerAndSaturation() {
        List<TooltipModel.Line> lines = TooltipModel.build(food("bread", 5, 6f), null, List.of());
        assertEquals("Hunger +2.5  Saturation +6", lines.get(0).text());
    }

    @Test
    void comparatorLineAndDeltaBar() {
        FoodInfo apple = food("apple", 4, 2.4f);
        FoodInfo steak = food("steak", 8, 12.8f);
        List<TooltipModel.Line> lines = TooltipModel.build(apple, null, List.of(apple, steak, food("bread", 5, 6f)));
        assertTrue(has(lines, "Better than 0 of your other 2 foods", TooltipModel.Tone.INFO), lines.toString());
        assertTrue(lines.stream().anyMatch(l -> l.text().contains("steak") && l.text().contains("31%")),
                lines.toString());
    }

    @Test
    void bestFoodIsFlagged() {
        FoodInfo steak = food("steak", 8, 12.8f);
        List<TooltipModel.Line> lines = TooltipModel.build(steak, null, List.of(steak, food("apple", 4, 2.4f)));
        assertTrue(has(lines, "Your best food", TooltipModel.Tone.BEST), lines.toString());
    }

    @Test
    void wasteIsOrangeAndOnlyWhenWasted() {
        FoodInfo bread = food("bread", 5, 6f);
        PlayerState nearlyFull = new PlayerState(18, 5f, 0f, 20f, 20f, 0f, true);
        PlayerState hungry = new PlayerState(8, 0f, 0f, 20f, 20f, 0f, true);
        assertTrue(has(TooltipModel.build(bread, nearlyFull, List.of()), "Wastes 1.5 shanks of hunger",
                TooltipModel.Tone.WARN));
        assertFalse(TooltipModel.build(bread, hungry, List.of()).stream()
                .anyMatch(l -> l.tone() == TooltipModel.Tone.WARN));
    }

    @Test
    void effectsListNameLevelDurationAndChance() {
        EffectInfo hunger = new EffectInfo("Hunger", 0, 600, 0.3f, false);
        EffectInfo regen = new EffectInfo("Regeneration", 1, 100, 1f, true);
        List<TooltipModel.Line> lines = TooltipModel.build(food("meat", 3, 1f, hunger, regen), null, List.of());
        assertTrue(has(lines, "Hunger (0:30) 30%", TooltipModel.Tone.BAD), lines.toString());
        assertTrue(has(lines, "Regeneration II (0:05)", TooltipModel.Tone.GOOD), lines.toString());
    }

    @Test
    void barFillsProportionally() {
        assertEquals("███░░░░░░░", TooltipModel.bar(3));
        assertEquals(10, TooltipModel.bar(99).length());
    }

    @Test
    void fullnessLineAppearsWhenLong() {
        PlayerState p = new PlayerState(12, 0f, 0f, 20f, 20f, 0f, true);
        List<TooltipModel.Line> lines = TooltipModel.build(food("steak", 8, 12.8f), p, List.of());
        assertTrue(lines.stream().anyMatch(l -> l.text().startsWith("Keeps you full for about")), lines.toString());
    }

    @Test
    void numberFormatting() {
        assertEquals("3", TooltipModel.trim(3.0f));
        assertEquals("2.4", TooltipModel.trim(2.4f));
        assertEquals("1:05", TooltipModel.clock(1300));
        assertEquals("1 minute", TooltipModel.minutes(50f));
    }
}
