package com.itmecasey.orangepeel.client;

import com.itmecasey.orangepeel.Config;
import com.itmecasey.orangepeel.FoodInfo;
import com.itmecasey.orangepeel.PlayerState;
import com.itmecasey.orangepeel.TooltipModel;
import com.itmecasey.orangepeel.api.OrangePeelApi;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.world.item.ItemStack;

/** What the loader layers call from their events. Client side only. */
public final class ClientHooks {
    private static Bridge bridge;

    private ClientHooks() {
    }

    public static void init(Bridge b) {
        bridge = b;
    }

    /** Adds the food lines to an item tooltip. Safe to call for every item. */
    public static void onTooltip(ItemStack stack, List<Component> tooltip) {
        if (bridge == null || !Config.showTooltip || stack.isEmpty()) {
            return;
        }
        FoodInfo food = bridge.food(stack);
        if (food == null) {
            return;
        }
        PlayerState player = bridge.player();
        List<FoodInfo> carried = new ArrayList<>();
        for (ItemStack s : bridge.inventory()) {
            if (!s.isEmpty()) {
                FoodInfo f = bridge.food(s);
                if (f != null) {
                    carried.add(f);
                }
            }
        }
        for (TooltipModel.Line line : TooltipModel.build(food, player, carried)) {
            tooltip.add(Component.literal(line.text()).withStyle(Style.EMPTY.withColor(color(line.tone()))));
        }
        for (OrangePeelApi.TooltipProvider provider : OrangePeelApi.providers()) {
            provider.addLines(stack, food, tooltip);
        }
    }

    /** Draws the hunger-bar overlays for the current frame. */
    public static void onHud(HudDraw draw) {
        if (bridge == null) {
            return;
        }
        PlayerState player = bridge.player();
        if (player == null) {
            return;
        }
        ItemStack held = bridge.heldFood();
        FoodInfo food = held.isEmpty() ? null : bridge.food(held);
        FoodHud.render(draw, player, food);
    }

    static int color(TooltipModel.Tone tone) {
        return switch (tone) {
            case INFO -> 0xAAAAAA;
            case HUNGER -> 0xE8A33D;
            case SATURATION -> 0xFFD966;
            case GOOD -> 0x55FF55;
            case BEST -> 0xFFAA00;
            case WARN -> 0xFF8C1A;
            case BAD -> 0xFF5555;
            case DIM -> 0x8F8FA3;
        };
    }
}
