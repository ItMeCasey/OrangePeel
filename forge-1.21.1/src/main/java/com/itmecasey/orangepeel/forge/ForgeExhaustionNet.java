package com.itmecasey.orangepeel.forge;

import com.itmecasey.orangepeel.ExhaustionSync;
import com.itmecasey.orangepeel.OrangePeel;
import com.itmecasey.orangepeel.ServerExhaustion;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.network.ChannelBuilder;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.SimpleChannel;

/**
 * Sends a player's exhaustion from the server to that player's client, for the hunger bar's exhaustion underlay. The
 * channel is optional on both sides: a client without OrangePeel joins a server that has it (and the other way round),
 * and the client then simply has no exhaustion to draw.
 */
final class ForgeExhaustionNet {
    private static SimpleChannel channel;

    private ForgeExhaustionNet() {
    }

    /** The message: one float. */
    private record Message(float exhaustion) {
    }

    /** Forge wants channels made while mods load, so the mod constructor calls this. */
    static void install() {
        channel = ChannelBuilder.named(ResourceLocation.fromNamespaceAndPath(OrangePeel.MODID, "exhaustion"))
                .networkProtocolVersion(1)
                .optional()
                .simpleChannel()
                .messageBuilder(Message.class, NetworkDirection.PLAY_TO_CLIENT)
                .encoder((message, buf) -> buf.writeFloat(message.exhaustion()))
                .decoder(buf -> new Message(buf.readFloat()))
                .consumerNetworkThread((message, context) -> {
                    ExhaustionSync.set(message.exhaustion());
                    context.setPacketHandled(true);
                })
                .add();
        MinecraftForge.EVENT_BUS.addListener((TickEvent.PlayerTickEvent e) -> {
            if (e.phase == TickEvent.Phase.END && e.player instanceof ServerPlayer player) {
                ServerExhaustion.onPlayerTick(player.getUUID(), player.getFoodData(),
                        value -> channel.send(new Message(value), PacketDistributor.PLAYER.with(player)));
            }
        });
        MinecraftForge.EVENT_BUS.addListener((PlayerEvent.PlayerLoggedOutEvent e) ->
                ServerExhaustion.onPlayerLeft(e.getEntity().getUUID()));
    }
}
