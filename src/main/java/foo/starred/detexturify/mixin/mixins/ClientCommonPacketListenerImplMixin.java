package foo.starred.detexturify.mixin.mixins;

import foo.starred.detexturify.Detexturify;
import foo.starred.detexturify.config.categories.MainCategory;
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
        if (!MainCategory.INSTANCE.getEnabled().getValue()) return;

        final String url = packet.url();
        if (!url.contains("hypixel.net") || !url.contains("SkyBlock")) {
            Detexturify.texture = false;
            return;
        }

        if (MainCategory.INSTANCE.getPreventDownload().getValue()) {
            ci.cancel();

            final ClientCommonPacketListenerImpl self = detexturify$self();
            self.send(new ServerboundResourcePackPacket(packet.id(), ServerboundResourcePackPacket.Action.ACCEPTED));
            self.send(new ServerboundResourcePackPacket(packet.id(), ServerboundResourcePackPacket.Action.SUCCESSFULLY_LOADED));
        }

        Detexturify.texture = true;
    }

    @Unique
    private ClientCommonPacketListenerImpl detexturify$self() {
        return (ClientCommonPacketListenerImpl) (Object) this;
    }
}
