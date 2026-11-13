package com.itmecasey.orangepeel.client;

import com.itmecasey.orangepeel.Config;
import com.itmecasey.orangepeel.FoodInfo;
import com.itmecasey.orangepeel.MealComparator;
import com.itmecasey.orangepeel.OrangePeel;
import com.itmecasey.orangepeel.PlayerState;
import com.itmecasey.orangepeel.TooltipModel;
import com.itmecasey.orangepeel.api.OrangePeelApi;
import com.itmecasey.orangepeel.script.ModJsBridge;
import com.itmecasey.orangepeel.script.ScriptApi;
import com.itmecasey.orangepeel.script.ScriptState;
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
    private static java.lang.reflect.Method devTour;
    private static boolean devTourLooked;

    /**
     * Development only: runs the screenshot tour when it is on the classpath. The tour classes are left out of the release
     * jar, so they are looked up by name and nothing happens in a release.
     */
    public static void devTour() {
        if (!devTourLooked) {
            devTourLooked = true;
            if (System.getenv("ORANGEPEEL_SHOTS") != null) {
                try {
                    devTour = Class.forName("com.itmecasey.orangepeel.compat.ShotTour").getMethod("tick");
                } catch (ReflectiveOperationException e) {
                    OrangePeel.LOGGER.warn("ORANGEPEEL_SHOTS is set but the screenshot tour is not in this build");
                }
            }
        }
        if (devTour != null) {
            try {
                devTour.invoke(null);
            } catch (ReflectiveOperationException e) {
                devTour = null;
                OrangePeel.LOGGER.warn("The screenshot tour failed", e);
            }
        }
    }

    public static volatile ItemStack tourTooltip = ItemStack.EMPTY;

    private ClientHooks() {
    }

    public static void init(Bridge b) {
        bridge = b;
        ScriptApi.attach(new ScriptState() {
            @Override
            public PlayerState player() {
                return bridge == null ? null : bridge.player();
            }

            @Override
            public FoodInfo held() {
                ItemStack held = bridge == null ? ItemStack.EMPTY : bridge.heldFood();
                return held.isEmpty() ? null : bridge.food(held);
            }

            @Override
            public List<FoodInfo> carried() {
                return bridge == null ? List.of() : carriedFoods();
            }
        });
        ModJsBridge.install(ScriptApi.INSTANCE);
    }

    /** One entry per food stack in the inventory. */
    private static List<FoodInfo> carriedFoods() {
        List<FoodInfo> carried = new ArrayList<>();
        for (ItemStack s : bridge.inventory()) {
            if (!s.isEmpty()) {
                FoodInfo f = bridge.food(s);
                if (f != null) {
                    carried.add(f);
                }
            }
        }
        return carried;
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
        List<FoodInfo> carried = carriedFoods();
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
        for (String line : ScriptApi.tooltipLines(food)) {
            tooltip.add(Component.literal(line));
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
        List<FoodInfo> carried = carriedFoods();
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
