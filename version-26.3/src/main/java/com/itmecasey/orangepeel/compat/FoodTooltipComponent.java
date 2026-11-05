package com.itmecasey.orangepeel.compat;

import com.itmecasey.orangepeel.client.FoodTooltipData;
import com.itmecasey.orangepeel.client.FoodTooltipGraphic;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;

/** Minecraft 26.x: the drawn part of the food tooltip. */
public final class FoodTooltipComponent implements ClientTooltipComponent {
    private final FoodTooltipData data;

    public FoodTooltipComponent(FoodTooltipData data) {
        this.data = data;
    }

    @Override
    public int getHeight(Font font) {
        return FoodTooltipGraphic.height(data);
    }

    @Override
    public int getWidth(Font font) {
        return FoodTooltipGraphic.width(data, font::width);
    }

    @Override
    public void extractImage(Font font, int x, int y, int width, int height, GuiGraphicsExtractor graphics) {
        FoodTooltipGraphic.draw(new HudDrawImpl(graphics), data, x, y);
    }
}
