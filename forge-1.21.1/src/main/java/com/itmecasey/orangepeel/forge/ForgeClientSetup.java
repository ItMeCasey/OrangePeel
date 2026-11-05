package com.itmecasey.orangepeel.forge;

import com.itmecasey.orangepeel.client.ClientHooks;
import com.itmecasey.orangepeel.client.FoodTooltipData;
import com.itmecasey.orangepeel.compat.FoodTooltipComponent;
import com.itmecasey.orangepeel.compat.BridgeImpl;
import com.itmecasey.orangepeel.compat.HudDrawImpl;
import com.itmecasey.orangepeel.compat.ShotTour;
import com.itmecasey.orangepeel.OrangePeel;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.client.event.AddGuiOverlayLayersEvent;
import net.minecraftforge.client.event.RegisterClientTooltipComponentFactoriesEvent;
import net.minecraftforge.client.event.RenderTooltipEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

/** Client-only wiring, in its own class so a dedicated server never loads the client types it names. */
final class ForgeClientSetup {
    private ForgeClientSetup() {
    }

    static void install(FMLJavaModLoadingContext context) {
        ClientHooks.init(new BridgeImpl());
        context.getModEventBus().addListener((AddGuiOverlayLayersEvent e) -> e.getLayeredDraw().add(
                ResourceLocation.fromNamespaceAndPath(OrangePeel.MODID, "food_overlay"),
                (graphics, delta) -> ClientHooks.onHud(new HudDrawImpl(graphics))));
        MinecraftForge.EVENT_BUS.addListener((ItemTooltipEvent e) ->
                ClientHooks.onTooltip(e.getItemStack(), e.getToolTip()));
        context.getModEventBus().addListener((RegisterClientTooltipComponentFactoriesEvent e) ->
                e.register(FoodTooltipData.class, FoodTooltipComponent::new));
        MinecraftForge.EVENT_BUS.addListener((RenderTooltipEvent.GatherComponents e) -> {
            FoodTooltipData data = ClientHooks.graphicData(e.getItemStack());
            if (data != null) {
                ClientHooks.addGraphic(data, e.getTooltipElements());
            }
        });
        // Development only: the screenshot tour runs when this variable is set.
        if (System.getenv("ORANGEPEEL_SHOTS") != null) {
            MinecraftForge.EVENT_BUS.addListener((TickEvent.ClientTickEvent e) -> {
                if (e.phase == TickEvent.Phase.END) {
                    ShotTour.tick();
                }
            });
        }
    }
}
