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

    /** Preview opacity: pulses between 0.4 and 0.85 (never below 0.35), or stays at 0.6 when steady. */
    static float pulse(long nanos) {
        if (Config.steadyPreview) {
            return 0.6f;
        }
        double t = nanos / 1.0e9;
        return (float) (0.625 + 0.225 * Math.sin(t * 4.0));
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
            // A translucent veil over the shanks, from the right edge toward the left (the way the bar empties): its
            // width is how much of the four exhaustion points that cost one saturation or hunger point have built up.
            float frac = Math.min(1f, p.exhaustion() / FoodMath.EXHAUSTION_STEP);
            int w = Math.max(1, Math.round(frac * 81));
            d.fill(left - w, top, left, top + 9, argb(0.38f * opacity, 0xE6, 0xE6, 0xFF));
            d.fill(left - w, top, left - w + 1, top + 9, argb(0.9f * opacity, 0xFF, 0xFF, 0xFF));
        }

        if (Config.showSaturationOverlay && p.saturation() > 0f) {
            // Golden rails above and below every shank the saturation covers (half a rail for one point).
            int gold = argb(opacity, 0xFF, 0xC8, 0x2E);
            for (int i = 0; i < 10; i++) {
                float points = Math.max(0f, Math.min(2f, p.saturation() - i * 2f));
                if (points <= 0f) {
                    break;
                }
                int x = left - i * 8 - 9;
                int from = points >= 2f ? x : x + 5;
                d.fill(from, top - 2, x + 9, top - 1, gold);
                d.fill(from, top + 10, x + 9, top + 11, gold);
            }
        }

        if (held != null && Config.showHungerPreview) {
            int after = FoodMath.foodAfter(p.foodLevel(), held.nutrition());
            int tint = argb(pulse(System.nanoTime()) * opacity, 0xFF, 0xF0, 0xB8);
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
                    d.sprite("hud/heart/full", hx + i * 8, top, 9, 9, tint);
                }
            }
        }
    }

    static int argb(float alpha, int r, int g, int b) {
        int a = Math.max(0, Math.min(255, Math.round(alpha * 255f)));
        return (a << 24) | (r << 16) | (g << 8) | b;
    }
}
