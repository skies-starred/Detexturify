package foo.starred.detexturify.mixin.mixins.feature.noinstall;

import foo.starred.detexturify.config.impl.DetexturifyConfig;
import foo.starred.detexturify.impl.cache.DetexturifyPackCacheInstance;
import net.minecraft.client.multiplayer.ClientCommonPacketListenerImpl;
import net.minecraft.network.protocol.common.ClientboundResourcePackPushPacket;
import net.minecraft.network.protocol.common.ServerboundResourcePackPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientCommonPacketListenerImpl.class)
public class ClientCommonPacketListenerImplMixin {
    @Inject(method = "handleResourcePackPush", at = @At("HEAD"), cancellable = true)
    private void detexturify$handleResourcePackPush(ClientboundResourcePackPushPacket packet, CallbackInfo ci) {
        if (!DetexturifyConfig.INSTANCE.getEnabled().getValue()) return;
        if (!DetexturifyConfig.INSTANCE.getHypixelCache().getValue()) return;

        final String url = packet.url();
        if (!url.contains("hypixel.net")) return;
        if (!url.contains("SkyBlock")) return;

        ci.cancel();
        DetexturifyPackCacheInstance.INSTANCE.update(url);

        final ClientCommonPacketListenerImpl self = detexturify$self();
        self.send(new ServerboundResourcePackPacket(packet.id(), ServerboundResourcePackPacket.Action.ACCEPTED));
        self.send(new ServerboundResourcePackPacket(packet.id(), ServerboundResourcePackPacket.Action.SUCCESSFULLY_LOADED));
    }

    @Unique
    private ClientCommonPacketListenerImpl detexturify$self() {
        return (ClientCommonPacketListenerImpl) (Object) this;
    }
}
