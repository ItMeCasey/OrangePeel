package com.itmecasey.orangepeel.api;

import com.itmecasey.orangepeel.FoodInfo;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

/**
 * Public API for other mods. Call {@link #registerTooltipProvider} during client setup to add lines to the food
 * tooltip; OrangePeel calls the provider only for items that are foods, on the client, once per tooltip draw.
 *
 * <pre>{@code
 * OrangePeelApi.registerTooltipProvider((stack, food, lines) ->
 *         lines.add(Component.literal("Tastes like " + food.name())));
 * }</pre>
 */
public final class OrangePeelApi {
    private static final List<TooltipProvider> PROVIDERS = new CopyOnWriteArrayList<>();

    private OrangePeelApi() {
    }

    /** Adds extra tooltip lines for a food. Append to {@code lines}; do not remove or reorder existing lines. */
    @FunctionalInterface
    public interface TooltipProvider {
        void addLines(ItemStack stack, FoodInfo food, List<Component> lines);
    }

    public static void registerTooltipProvider(TooltipProvider provider) {
        PROVIDERS.add(provider);
    }

    /** Used by OrangePeel itself. */
    public static List<TooltipProvider> providers() {
        return PROVIDERS;
    }
}
