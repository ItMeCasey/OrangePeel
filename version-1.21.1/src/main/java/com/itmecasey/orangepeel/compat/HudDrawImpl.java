package com.itmecasey.orangepeel.compat;

import com.itmecasey.orangepeel.client.HudDraw;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;

/** Minecraft 1.21.1: draws through GuiGraphics. */
public final class HudDrawImpl implements HudDraw {
    private final GuiGraphics g;

    public HudDrawImpl(GuiGraphics graphics) {
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
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        g.setColor(((argb >> 16) & 255) / 255f, ((argb >> 8) & 255) / 255f, (argb & 255) / 255f,
                ((argb >>> 24) & 255) / 255f);
        g.blitSprite(ResourceLocation.withDefaultNamespace(name), x, y, w, h);
        g.setColor(1f, 1f, 1f, 1f);
        RenderSystem.disableBlend();
    }

    @Override
    public void tooltip(net.minecraft.world.item.ItemStack stack, int x, int y) {
        g.renderTooltip(net.minecraft.client.Minecraft.getInstance().font, stack, x, y);
    }
}
