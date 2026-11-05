package com.itmecasey.orangepeel.client;

import com.itmecasey.orangepeel.Config;
import com.itmecasey.orangepeel.FoodMath;
import com.itmecasey.orangepeel.PlayerState;
import com.itmecasey.orangepeel.TooltipModel;
import java.util.function.ToIntFunction;

/**
 * Lays out and draws the graphical tooltip block with vanilla food and heart sprites and plain filled rectangles. Each
 * row is the drawn amount followed by its number (the sprites and colors say what it is):
 * <ol>
 * <li>hunger: the shanks the food restores (ones that would overflow the bar fade to orange), "+N",</li>
 * <li>saturation: golden shanks, "+N",</li>
 * <li>regeneration: hearts of extra regeneration when hurt, "+N",</li>
 * <li>comparison: a bar against the best food with a marker at its end, and "N% of Food" (or "Best food").</li>
 * </ol>
 * The text lines that repeat this are removed when the block is drawn; they stay as the fallback for viewers that cannot
 * draw it.
 */
public final class FoodTooltipGraphic {
    static final int ICON = 9;
    static final int STEP = 8;
    static final int ROW = 10;
    static final int LEAD = 0;
    static final int BAR_W = 60;
    static final int BAR_H = 5;
    static final int GAP = 3;
    private static final int WHITE = 0xFFFFFFFF;
    private static final int GOLD = 0xFFFFD24A;
    private static final int TEXT = 0xFFE8A33D;
    private static final int SAT_TEXT = 0xFFFFD966;
    private static final int HEART_TEXT = 0xFFFF7A7A;
    private static final int BAR_TEXT = 0xFF8F8FA3;

    private FoodTooltipGraphic() {
    }

    static int shanks(int points) {
        return Math.min(10, (points + 1) / 2);
    }

    static int satIcons(float saturation) {
        return Math.min(10, (int) Math.ceil(saturation / 2f - 0.0001f));
    }

    static float regenGain(FoodTooltipData data) {
        PlayerState p = data.player();
        if (p == null || !Config.showHealthPreview || p.absorption() > 0f || p.health() >= p.maxHealth()) {
            return 0f;
        }
        return FoodMath.regenPreview(p, data.food());
    }

    static boolean hasBar(FoodTooltipData data) {
        return Config.showComparator && data.ranking() != null && data.ranking().others() > 0;
    }

    static String hungerText(FoodTooltipData d) {
        return "+" + TooltipModel.trim(d.food().nutrition() / 2f);
    }

    static String satText(FoodTooltipData d) {
        return "+" + TooltipModel.trim(d.food().saturation());
    }

    static String heartText(FoodTooltipData d) {
        return "+" + TooltipModel.trim(regenGain(d) / 2f);
    }

    static String barText(FoodTooltipData d) {
        var r = d.ranking();
        return r.best() ? "Best food" : Math.round(r.fractionOfBest() * 100) + "% of " + r.bestFood().name();
    }

    private static int iconRowWidth(int icons, String text, ToIntFunction<String> textWidth) {
        return LEAD + (icons - 1) * STEP + ICON + GAP + textWidth.applyAsInt(text);
    }

    public static int width(FoodTooltipData data, ToIntFunction<String> textWidth) {
        int w = iconRowWidth(Math.max(1, shanks(Math.min(20, data.food().nutrition()))), hungerText(data), textWidth);
        if (data.food().saturation() > 0f) {
            w = Math.max(w, iconRowWidth(satIcons(data.food().saturation()), satText(data), textWidth));
        }
        float gain = regenGain(data);
        if (gain > 0f) {
            w = Math.max(w, iconRowWidth(Math.min(10, (int) Math.ceil(gain / 2f - 0.0001f)), heartText(data), textWidth));
        }
        if (hasBar(data)) {
            w = Math.max(w, LEAD + BAR_W + 2 + GAP + textWidth.applyAsInt(barText(data)));
        }
        return w;
    }

    public static int height(FoodTooltipData data) {
        int rows = 1 + (data.food().saturation() > 0f ? 1 : 0) + (regenGain(data) > 0f ? 1 : 0) + (hasBar(data) ? 1 : 0);
        return rows * ROW + 2;
    }

    public static void draw(HudDraw d, FoodTooltipData data, int x, int y) {
        int row = y + 2;

        // Hunger: the food's shanks. Points past the free space on the bar are wasted.
        int free = data.player() == null ? 20 : FoodMath.MAX_FOOD - data.player().foodLevel();
        int points = Math.min(20, data.food().nutrition());
        int n = shanks(points);
        for (int i = 0; i < n; i++) {
            int sx = x + LEAD + i * STEP;
            d.sprite("hud/food_empty", sx, row, ICON, ICON, WHITE);
            boolean full = points >= 2 * i + 2;
            boolean wasted = 2 * i >= free;
            d.sprite(full ? "hud/food_full" : "hud/food_half", sx, row, ICON, ICON, wasted ? 0xB0FF8C1A : WHITE);
        }
        d.text(hungerText(data), x + LEAD + (n - 1) * STEP + ICON + GAP, row + 1, TEXT);
        row += ROW;

        if (data.food().saturation() > 0f) {
            float s = data.food().saturation();
            int m = satIcons(s);
            for (int i = 0; i < m; i++) {
                int sx = x + LEAD + i * STEP;
                d.sprite("hud/food_empty", sx, row, ICON, ICON, 0x90FFFFFF);
                d.sprite(s >= 2 * i + 2 ? "hud/food_full" : "hud/food_half", sx, row, ICON, ICON, GOLD);
            }
            d.text(satText(data), x + LEAD + (m - 1) * STEP + ICON + GAP, row + 1, SAT_TEXT);
            row += ROW;
        }

        float gain = regenGain(data);
        if (gain > 0f) {
            int hearts = Math.min(10, (int) Math.ceil(gain / 2f - 0.0001f));
            for (int i = 0; i < hearts; i++) {
                int sx = x + LEAD + i * STEP;
                d.sprite("hud/heart/container", sx, row, ICON, ICON, WHITE);
                d.sprite("hud/heart/full", sx, row, ICON, ICON, WHITE);
            }
            d.text(heartText(data), x + LEAD + (hearts - 1) * STEP + ICON + GAP, row + 1, HEART_TEXT);
            row += ROW;
        }

        if (hasBar(data)) {
            var r = data.ranking();
            int by = row + 2;
            int bx = x + LEAD;
            d.fill(bx - 1, by - 1, bx + BAR_W + 1, by + BAR_H + 1, 0xFF000000);
            d.fill(bx, by, bx + BAR_W, by + BAR_H, 0xFF2A2640);
            int filled = Math.round(r.fractionOfBest() * BAR_W);
            d.fill(bx, by, bx + filled, by + BAR_H, r.best() ? 0xFFFFC83D : 0xFFFF8C1A);
            // marker at the end of the track: the best food's level
            d.fill(bx + BAR_W - 1, by - 2, bx + BAR_W + 1, by + BAR_H + 2, 0xFFFFE28C);
            d.text(barText(data), bx + BAR_W + 2 + GAP, by - 2, BAR_TEXT);
        }
    }
}
