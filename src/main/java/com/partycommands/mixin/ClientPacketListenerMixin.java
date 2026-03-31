package com.partycommands.mixin;

import com.partycommands.utils.ServerUtils;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.protocol.ping.ClientboundPongResponsePacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientPacketListener.class)
public class ClientPacketListenerMixin {

    @Inject(method = "handlePongResponse", at = @At("HEAD"))
    private void onPongResponse(ClientboundPongResponsePacket packet, CallbackInfo ci) {
        ServerUtils.onPongResponse(packet.time());
    }
}
