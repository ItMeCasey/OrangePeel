package com.itmecasey.orangepeel.client;

import com.itmecasey.orangepeel.Config;
import com.itmecasey.orangepeel.FoodInfo;
import com.itmecasey.orangepeel.MealComparator;
import com.itmecasey.orangepeel.PlayerState;
import com.itmecasey.orangepeel.TooltipModel;
import com.itmecasey.orangepeel.api.OrangePeelApi;
import java.util.ArrayList;
import java.util.List;
import com.mojang.datafixers.util.Either;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.network.chat.Style;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.ItemStack;

/** What the loader layers call from their events. Client side only. */
public final class ClientHooks {
    /** Marks text lines that the drawn tooltip block repeats; an invisible style insertion. */
    private static final String DUPLICATE = "orangepeel:graphic";
    private static Bridge bridge;
    /** Set only by the dev screenshot tour: a stack whose tooltip is drawn at a fixed spot every frame. */
    public static volatile ItemStack tourTooltip = ItemStack.EMPTY;

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
            Style style = Style.EMPTY.withColor(color(line.tone()));
            if (line.graphic()) {
                style = style.withInsertion(DUPLICATE);
            }
            tooltip.add(Component.literal(line.text()).withStyle(style));
        }
        for (OrangePeelApi.TooltipProvider provider : OrangePeelApi.providers()) {
            provider.addLines(stack, food, tooltip);
        }
    }

    /**
     * The data for the drawn part of the tooltip, or null when the stack is not a food or graphics are off. Loader layers
     * add it to the tooltip's components; the text lines from {@link #onTooltip} stay as the fallback.
     */
    public static FoodTooltipData graphicData(ItemStack stack) {
        if (bridge == null || !Config.showTooltip || !Config.graphicalTooltip || stack.isEmpty()) {
            return null;
        }
        FoodInfo food = bridge.food(stack);
        if (food == null) {
            return null;
        }
        List<FoodInfo> carried = new ArrayList<>();
        for (ItemStack s : bridge.inventory()) {
            if (!s.isEmpty()) {
                FoodInfo f = bridge.food(s);
                if (f != null) {
                    carried.add(f);
                }
            }
        }
        MealComparator.Result ranking = MealComparator.compare(food, carried);
        return new FoodTooltipData(food, bridge.player(), ranking.others() > 0 ? ranking : null);
    }

    /**
     * Called with the components of a tooltip that is about to be drawn with the graphical block: removes the text lines the
     * block repeats, then adds the block. Viewers that never reach this keep the text lines.
     */
    public static void addGraphic(FoodTooltipData data, List<Either<FormattedText, TooltipComponent>> elements) {
        elements.removeIf(e -> e.left().filter(t -> t instanceof Component c
                && DUPLICATE.equals(c.getStyle().getInsertion())).isPresent());
        elements.add(Either.right(data));
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
        if (!tourTooltip.isEmpty()) {
            draw.tooltip(tourTooltip, draw.width() / 2 - 60, draw.height() / 2 - 70);
        }
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
