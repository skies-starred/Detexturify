package foo.starred.detexturify.mixin.mixins;

import foo.starred.detexturify.Detexturify;
import foo.starred.detexturify.config.categories.MainCategory;
import kotlin.Unit;
import net.minecraft.client.gui.screens.inventory.tooltip.TooltipRenderUtil;
import net.minecraft.resources.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(TooltipRenderUtil.class)
public class TooltipRenderUtilMixin {
    @Unique
    private static final Identifier detexturify$background = Identifier.fromNamespaceAndPath(Detexturify.modId, "tooltip/background");

    @Unique
    private static final Identifier detexturify$frame = Identifier.fromNamespaceAndPath(Detexturify.modId, "tooltip/frame");

    @Unique
    private static boolean detexturify$bool = MainCategory.INSTANCE.getEnabled().getValue() && MainCategory.INSTANCE.getVanillaTooltip().getValue();

    static {
        MainCategory.INSTANCE.getEnabled().onChange(bool -> {
            detexturify$bool = bool && MainCategory.INSTANCE.getVanillaTooltip().getValue();
            return Unit.INSTANCE;
        });

        MainCategory.INSTANCE.getVanillaTooltip().onChange(bool -> {
            detexturify$bool = bool && MainCategory.INSTANCE.getEnabled().getValue();
            return Unit.INSTANCE;
        });
    }

    @Inject(method = "getBackgroundSprite", at = @At("HEAD"), cancellable = true)
    private static void detexturify$getBackgroundSprite(Identifier name, CallbackInfoReturnable<Identifier> cir) {
        if (name != null) return;
        if (!detexturify$bool) return;
        if (!Detexturify.texture) return;
        cir.setReturnValue(detexturify$background);
    }

    @Inject(method = "getFrameSprite", at = @At("HEAD"), cancellable = true)
    private static void detexturify$getFrameSprite(Identifier name, CallbackInfoReturnable<Identifier> cir) {
        if (name != null) return;
        if (!detexturify$bool) return;
        if (!Detexturify.texture) return;
        cir.setReturnValue(detexturify$frame);
    }
}
