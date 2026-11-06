package com.itmecasey.orangepeel.neoforge;

import com.itmecasey.orangepeel.ExhaustionSync;
import com.itmecasey.orangepeel.OrangePeel;
import com.itmecasey.orangepeel.ServerExhaustion;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;

/**
 * Sends a player's exhaustion from the server to that player's client, for the hunger bar's exhaustion underlay. The
 * channel is optional on both sides: a client without OrangePeel joins a server that has it (and the other way round),
 * and the client then simply has no exhaustion to draw.
 */
final class NeoForgeExhaustionNet {
    private NeoForgeExhaustionNet() {
    }

    /** The message: one float. */
    record Payload(float exhaustion) implements CustomPacketPayload {
        static final Type<Payload> TYPE = new Type<>(Identifier.fromNamespaceAndPath(OrangePeel.MODID, "exhaustion"));
        static final StreamCodec<FriendlyByteBuf, Payload> CODEC = StreamCodec.of(
                (buf, payload) -> buf.writeFloat(payload.exhaustion()), buf -> new Payload(buf.readFloat()));

        @Override
        public Type<Payload> type() {
            return TYPE;
        }
    }

    static void install(IEventBus modBus) {
        modBus.addListener((RegisterPayloadHandlersEvent e) -> e.registrar("1").optional().playToClient(Payload.TYPE,
                Payload.CODEC, (payload, context) -> ExhaustionSync.set(payload.exhaustion())));
        NeoForge.EVENT_BUS.addListener((PlayerTickEvent.Post e) -> {
            if (e.getEntity() instanceof ServerPlayer player) {
                ServerExhaustion.onPlayerTick(player.getUUID(), player.getFoodData(),
                        value -> PacketDistributor.sendToPlayer(player, new Payload(value)));
            }
        });
        NeoForge.EVENT_BUS.addListener((PlayerEvent.PlayerLoggedOutEvent e) -> ServerExhaustion.onPlayerLeft(e.getEntity().getUUID()));
    }
}
