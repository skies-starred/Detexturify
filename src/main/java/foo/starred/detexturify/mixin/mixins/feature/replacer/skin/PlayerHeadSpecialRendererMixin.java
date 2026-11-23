package foo.starred.detexturify.mixin.mixins.feature.replacer.skin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import foo.starred.detexturify.config.impl.DetexturifyConfig;
import foo.starred.detexturify.impl.data.data.SkyBlockItemData;
import foo.starred.detexturify.duck.ItemStackDuck;
import foo.starred.detexturify.impl.data.loader.DetexturifyItemDataInstance;
import foo.starred.kontext.api.item.extension.ItemStackGenericExtensionsKt;
import kotlin.Unit;
import net.minecraft.client.renderer.special.PlayerHeadSpecialRenderer;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ResolvableProfile;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(PlayerHeadSpecialRenderer.class)
public class PlayerHeadSpecialRendererMixin {
    @Unique
    private static boolean detexturify$bool = DetexturifyConfig.INSTANCE.getEnabled().getValue() && DetexturifyConfig.INSTANCE.getRetexture().getValue();

    static {
        DetexturifyConfig.INSTANCE.getEnabled().observe(bool -> {
            detexturify$bool = bool && DetexturifyConfig.INSTANCE.getRetexture().getValue();
            return Unit.INSTANCE;
        });

        DetexturifyConfig.INSTANCE.getRetexture().observe(bool -> {
            detexturify$bool = bool && DetexturifyConfig.INSTANCE.getEnabled().getValue();
            return Unit.INSTANCE;
        });
    }

    @ModifyExpressionValue(method = "extractArgument(Lnet/minecraft/world/item/ItemStack;)Lnet/minecraft/client/renderer/PlayerSkinRenderCache$RenderInfo;", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ItemStack;get(Lnet/minecraft/core/component/DataComponentType;)Ljava/lang/Object;"))
    private Object detexturify$extractArgument(Object original, @Local(argsOnly = true) ItemStack stack) {
        if (!detexturify$bool) return original;
        if (stack.isEmpty()) return original;
        if (original != null) return original;

        final ItemStackDuck duck = (ItemStackDuck) (Object) stack;
        final ResolvableProfile cached = duck.detexturify$profile();
        if (cached != null) return cached;

        final CompoundTag tag = ItemStackGenericExtensionsKt.customData(stack);
        if (tag == null) return null;

        final String id = tag.getStringOr("id", null);
        if (id == null) return null;

        final String key = id.replace(':', '-');
        if (DetexturifyItemDataInstance.REPLACEMENTS.getValue().containsKey(key)) {
            duck.detexturify$profile(null);
            return null;
        }

        final SkyBlockItemData entry = DetexturifyItemDataInstance.MAP.get(key);
        if (entry == null) return null;

        final ResolvableProfile profile = entry.getProfile();
        duck.detexturify$profile(profile);

        return profile;
    }
}
