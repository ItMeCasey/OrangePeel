package com.itmecasey.orangepeel.client;

import com.itmecasey.orangepeel.FoodInfo;
import com.itmecasey.orangepeel.MealComparator;
import com.itmecasey.orangepeel.PlayerState;
import net.minecraft.world.inventory.tooltip.TooltipComponent;

/**
 * Data for the drawn part of the food tooltip (shanks, saturation, regeneration hearts, comparison bar).
 *
 * @param player  the local player, or null when unknown (then nothing is marked as wasted and no hearts are shown)
 * @param ranking how the food ranks against the carried foods, or null when it is the only food
 */
public record FoodTooltipData(FoodInfo food, PlayerState player, MealComparator.Result ranking)
        implements TooltipComponent {
}
