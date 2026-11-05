package com.itmecasey.orangepeel.forge;

import com.itmecasey.orangepeel.OrangePeel;
import com.itmecasey.orangepeel.client.ClientHooks;
import com.itmecasey.orangepeel.compat.BridgeImpl;
import com.itmecasey.orangepeel.compat.HudDrawImpl;
import net.minecraft.resources.Identifier;
import net.minecraftforge.client.event.AddGuiOverlayLayersEvent;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;

/** Client-only wiring, in its own class so a dedicated server never loads the client types it names. */
final class ForgeClientSetup {
    private ForgeClientSetup() {
    }

    static void install() {
        ClientHooks.init(new BridgeImpl());
        AddGuiOverlayLayersEvent.BUS.addListener((AddGuiOverlayLayersEvent e) -> e.getLayeredDraw().add(
                Identifier.fromNamespaceAndPath(OrangePeel.MODID, "food_overlay"),
                (graphics, delta) -> ClientHooks.onHud(new HudDrawImpl(graphics))));
        ItemTooltipEvent.BUS.addListener((ItemTooltipEvent e) ->
                ClientHooks.onTooltip(e.getItemStack(), e.getToolTip()));
    }
}
