package com.itmecasey.orangepeel.neoforge;

import com.itmecasey.orangepeel.OrangePeel;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.fml.loading.FMLPaths;

/** NeoForge entry point for Minecraft 26.x. Everything shared lives in common/; this class only wires NeoForge in. */
@Mod(OrangePeel.MODID)
public final class NeoForgeOrangePeel {
    public NeoForgeOrangePeel(IEventBus modBus) {
        OrangePeel.init(FMLPaths.CONFIGDIR::get);
        NeoForgeExhaustionNet.install(modBus);
        if (FMLEnvironment.getDist() == Dist.CLIENT) {
            NeoForgeClientSetup.install(modBus);
        }
    }
}
