package com.itmecasey.orangepeel.forge;

import com.itmecasey.orangepeel.OrangePeel;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLEnvironment;
import net.minecraftforge.fml.loading.FMLPaths;

/** Forge entry point for Minecraft 26.x. Everything shared lives in common/; this class only wires Forge in. */
@Mod(OrangePeel.MODID)
public final class ForgeOrangePeel {
    public ForgeOrangePeel(FMLJavaModLoadingContext context) {
        OrangePeel.init(FMLPaths.CONFIGDIR::get);
        ForgeConfig.register(context, FMLPaths.CONFIGDIR.get());
        ForgeExhaustionNet.install();
        if (FMLEnvironment.dist.isClient()) {
            ForgeClientSetup.install();
        }
    }
}
