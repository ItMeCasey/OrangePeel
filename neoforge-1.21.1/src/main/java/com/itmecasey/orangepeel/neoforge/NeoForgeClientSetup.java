package com.itmecasey.orangepeel.neoforge;

import com.itmecasey.orangepeel.OrangePeel;
import com.itmecasey.orangepeel.client.ClientHooks;
import com.itmecasey.orangepeel.compat.BridgeImpl;
import com.itmecasey.orangepeel.compat.HudDrawImpl;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.client.event.RegisterGuiLayersEvent;
import net.neoforged.neoforge.client.gui.VanillaGuiLayers;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.player.ItemTooltipEvent;

/** Client-only wiring, in its own class so a dedicated server never loads the client types it names. */
final class NeoForgeClientSetup {
    private NeoForgeClientSetup() {
    }

    static void install(IEventBus modBus) {
        ClientHooks.init(new BridgeImpl());
        modBus.addListener((RegisterGuiLayersEvent e) -> e.registerAbove(VanillaGuiLayers.FOOD_LEVEL,
                ResourceLocation.fromNamespaceAndPath(OrangePeel.MODID, "food_overlay"),
                (graphics, delta) -> ClientHooks.onHud(new HudDrawImpl(graphics))));
        NeoForge.EVENT_BUS.addListener((ItemTooltipEvent e) -> ClientHooks.onTooltip(e.getItemStack(), e.getToolTip()));
    }
}
