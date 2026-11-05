package com.itmecasey.orangepeel.client;

import com.itmecasey.orangepeel.Config;
import com.itmecasey.orangepeel.FoodInfo;
import com.itmecasey.orangepeel.FoodMath;
import com.itmecasey.orangepeel.PlayerState;

/**
 * Draws the hunger-bar overlays on top of the vanilla bar: a pulsing preview of what the held food restores, a
 * saturation strip under each shank, an exhaustion underlay, and a preview of the health regeneration the food adds.
 */
public final class FoodHud {
    private FoodHud() {
    }

    /** Pulse between 0.35 and 0.75 of the configured opacity. */
    static float pulse(long nanos) {
        double t = nanos / 1.0e9;
        return (float) (0.55 + 0.2 * Math.sin(t * 4.0));
    }

    public static void render(HudDraw d, PlayerState p, FoodInfo held) {
        if (p == null || !p.hudVisible()) {
            return;
        }
        float opacity = Config.opacity();
        if (opacity <= 0f) {
            return;
        }
        int left = d.width() / 2 + 91 + Config.hudOffsetX;
        int top = d.height() - 39 + Config.hudOffsetY;

        if (Config.showExhaustionUnderlay && p.exhaustion() > 0f) {
            float frac = Math.min(1f, p.exhaustion() / FoodMath.EXHAUSTION_STEP);
            int w = Math.round(frac * 81);
            // fills from the right edge of the bar toward the left, like the exhaustion filling up
            d.fill(left - w, top + 10, left, top + 12, argb(0.8f * opacity, 0x2E, 0x2A, 0x46));
        }

        if (Config.showSaturationOverlay && p.saturation() > 0f) {
            for (int i = 0; i < 10; i++) {
                float points = Math.max(0f, Math.min(2f, p.saturation() - i * 2f));
                if (points <= 0f) {
                    break;
                }
                int x = left - i * 8 - 9;
                int w = Math.round(points / 2f * 9);
                d.fill(x + 9 - w, top + 9, x + 9, top + 10, argb(opacity, 0xFF, 0x9A, 0x2E));
            }
        }

        if (held != null && Config.showHungerPreview) {
            int after = FoodMath.foodAfter(p.foodLevel(), held.nutrition());
            int tint = argb(pulse(System.nanoTime()) * opacity, 0xFF, 0xFF, 0xFF);
            for (int i = 0; i < 10; i++) {
                int x = left - i * 8 - 9;
                int iconStart = i * 2;
                if (after >= iconStart + 2 && p.foodLevel() < iconStart + 2) {
                    d.sprite("hud/food_full", x, top, 9, 9, tint);
                } else if (after == iconStart + 1 && p.foodLevel() <= iconStart) {
                    d.sprite("hud/food_half", x, top, 9, 9, tint);
                }
            }
        }

        if (held != null && Config.showHealthPreview && p.absorption() <= 0f && p.maxHealth() <= 20f
                && p.health() < p.maxHealth()) {
            float gain = FoodMath.regenPreview(p, held);
            if (gain > 0f) {
                int tint = argb(pulse(System.nanoTime()) * opacity, 0xFF, 0xFF, 0xFF);
                int from = (int) Math.ceil(p.health() / 2f - 0.0001f);
                int to = (int) Math.ceil(Math.min(p.maxHealth(), p.health() + gain) / 2f - 0.0001f);
                int hx = d.width() / 2 - 91 + Config.hudOffsetX;
                for (int i = from; i < to && i < 10; i++) {
                    d.sprite("hud/heart/full", hx + i * 8, top - 10, 9, 9, tint);
                }
            }
        }
    }

    static int argb(float alpha, int r, int g, int b) {
        int a = Math.max(0, Math.min(255, Math.round(alpha * 255f)));
        return (a << 24) | (r << 16) | (g << 8) | b;
    }
}
