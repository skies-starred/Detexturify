package foo.starred.detexturify.mixin.mixins;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import foo.starred.detexturify.Detexturify;
import foo.starred.detexturify.config.categories.MainCategory;
import foo.starred.detexturify.data.SkyBlockItem;
import foo.starred.detexturify.ducks.ItemStackDuck;
import kotlin.Unit;
import net.minecraft.client.renderer.special.PlayerHeadSpecialRenderer;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.component.ResolvableProfile;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(PlayerHeadSpecialRenderer.class)
public class PlayerHeadSpecialRendererMixin {
    @Unique
    private static boolean detexturify$bool = MainCategory.INSTANCE.getEnabled().getValue() && MainCategory.INSTANCE.getRetexture().getValue();

    static {
        MainCategory.INSTANCE.getEnabled().onChange(bool -> {
            detexturify$bool = bool && MainCategory.INSTANCE.getRetexture().getValue();
            return Unit.INSTANCE;
        });

        MainCategory.INSTANCE.getRetexture().onChange(bool -> {
            detexturify$bool = bool && MainCategory.INSTANCE.getEnabled().getValue();
            return Unit.INSTANCE;
        });
    }

    @ModifyExpressionValue(method = "extractArgument(Lnet/minecraft/world/item/ItemStack;)Lnet/minecraft/client/renderer/PlayerSkinRenderCache$RenderInfo;", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ItemStack;get(Lnet/minecraft/core/component/DataComponentType;)Ljava/lang/Object;"))
    private Object detexturify$extractArgument(Object original, @Local(argsOnly = true) ItemStack stack) {
        if (!detexturify$bool) return original;
        if (stack.isEmpty()) return original;
        if (original != null) return original;

        final ItemStackDuck b = (ItemStackDuck) (Object) stack;
        final ResolvableProfile c = b.detexturify$profile();
        if (c != null) return c;

        final CompoundTag d = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
        final String e = d.getStringOr("id", null);
        if (e == null) return original;

        final String f = e.replace(':', '-');
        final SkyBlockItem g = Detexturify.MAP.get(f);
        if (g == null) return original;

        final ResolvableProfile h = g.getProfile();
        b.detexturify$profile(h);

        return h;
    }
}