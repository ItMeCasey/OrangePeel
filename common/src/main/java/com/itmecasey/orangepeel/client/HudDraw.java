package com.itmecasey.orangepeel.client;

/** The few drawing calls the food overlay needs. Each Minecraft version implements them over its own graphics class. */
public interface HudDraw {
    int width();

    int height();

    void fill(int x1, int y1, int x2, int y2, int argb);

    /** Draws a vanilla GUI sprite such as "hud/food_full", tinted with the ARGB color (alpha controls opacity). */
    void sprite(String name, int x, int y, int w, int h, int argb);

    /** Draws the item's normal tooltip, including OrangePeel's lines, at a fixed spot. Used by the screenshot tour. */
    void tooltip(net.minecraft.world.item.ItemStack stack, int x, int y);

    /** Draws a line of text with a shadow. */
    void text(String text, int x, int y, int argb);

    int textWidth(String text);
}
