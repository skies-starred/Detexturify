package foo.starred.detexturify.mixin.mixins;

import foo.starred.detexturify.config.categories.MainCategory;
import net.minecraft.client.multiplayer.ClientCommonPacketListenerImpl;
import net.minecraft.network.protocol.common.ClientboundResourcePackPushPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientCommonPacketListenerImpl.class)
public class ClientCommonPacketListenerImplMixin {
    @Inject(method = "handleResourcePackPush", at = @At("HEAD"), cancellable = true)
    private void detexturify$handleResourcePackPush(ClientboundResourcePackPushPacket packet, CallbackInfo ci) {
        if (!MainCategory.INSTANCE.getEnabled().getValue()) return;
        if (!packet.url().startsWith("https://resourcepacks2.hypixel.net/SkyBlockResourcePack")) return;
        ci.cancel();
    }
}
