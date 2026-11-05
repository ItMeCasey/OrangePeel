package com.itmecasey.orangepeel.client;

import com.itmecasey.orangepeel.FoodInfo;
import com.itmecasey.orangepeel.PlayerState;
import java.util.List;
import net.minecraft.world.item.ItemStack;

/** The Minecraft-version-specific reads, implemented once per Minecraft version in version-&lt;mc&gt;/. */
public interface Bridge {
    /** The food data of a stack, or null when the item is not a food. */
    FoodInfo food(ItemStack stack);

    /** The local player, or null when there is none. */
    PlayerState player();

    /** Every stack in the local player's inventory (main, hotbar and offhand). */
    List<ItemStack> inventory();

    /** The food in the main hand, else the food in the offhand, else {@link ItemStack#EMPTY}. */
    ItemStack heldFood();
}
