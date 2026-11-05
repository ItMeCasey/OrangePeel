package com.itmecasey.orangepeel.compat;

import com.itmecasey.orangepeel.client.HudDraw;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.Identifier;

/** Minecraft 26.x: draws through GuiGraphicsExtractor. */
public final class HudDrawImpl implements HudDraw {
    private final GuiGraphicsExtractor g;

    public HudDrawImpl(GuiGraphicsExtractor graphics) {
        this.g = graphics;
    }

    @Override
    public int width() {
        return g.guiWidth();
    }

    @Override
    public int height() {
        return g.guiHeight();
    }

    @Override
    public void fill(int x1, int y1, int x2, int y2, int argb) {
        g.fill(x1, y1, x2, y2, argb);
    }

    @Override
    public void sprite(String name, int x, int y, int w, int h, int argb) {
        g.blitSprite(RenderPipelines.GUI_TEXTURED, Identifier.withDefaultNamespace(name), x, y, w, h, argb);
    }

    @Override
    public void tooltip(net.minecraft.world.item.ItemStack stack, int x, int y) {
        g.setTooltipForNextFrame(net.minecraft.client.Minecraft.getInstance().font, stack, x, y);
    }

    @Override
    public void text(String text, int x, int y, int argb) {
        g.text(net.minecraft.client.Minecraft.getInstance().font, text, x, y, argb, true);
    }

    @Override
    public int textWidth(String text) {
        return net.minecraft.client.Minecraft.getInstance().font.width(text);
    }
}
