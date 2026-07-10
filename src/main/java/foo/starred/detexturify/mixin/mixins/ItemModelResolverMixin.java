package foo.starred.detexturify.mixin.mixins;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import foo.starred.detexturify.Detexturify;
import foo.starred.detexturify.config.categories.MainCategory;
import foo.starred.detexturify.data.SkyBlockItem;
import foo.starred.detexturify.ducks.ItemStackDuck;
import kotlin.Unit;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.CustomData;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

import java.util.Optional;

@Mixin(ItemModelResolver.class)
public class ItemModelResolverMixin {
    @Unique
    private static boolean detexturify$bool = MainCategory.INSTANCE.getEnabled().getValue() && MainCategory.INSTANCE.getRetexture().getValue();

    @Unique
    private static boolean detexturify$component = MainCategory.INSTANCE.getRetextureType().getValue() == MainCategory.RetextureType.COMPONENT;

    static {
        MainCategory.INSTANCE.getEnabled().onChange(bool -> {
            detexturify$bool = bool && MainCategory.INSTANCE.getRetexture().getValue();
            return Unit.INSTANCE;
        });

        MainCategory.INSTANCE.getRetexture().onChange(bool -> {
            detexturify$bool = bool && MainCategory.INSTANCE.getEnabled().getValue();
            return Unit.INSTANCE;
        });

        MainCategory.INSTANCE.getRetextureType().onChange(type -> {
            detexturify$component = type == MainCategory.RetextureType.COMPONENT;
            return Unit.INSTANCE;
        });
    }

    // TODO: test with other mods to check if the "shouldPlaySwapAnimation" and "swapAnimationScale" modifications are required.
    // - shouldPlaySwapAnimation: https://mcsrc.dev/1/26.1.2/net/minecraft/client/renderer/ItemInHandRenderer#L625
    // - swapAnimationScale: https://mcsrc.dev/1/26.1.2/net/minecraft/client/renderer/ItemInHandRenderer#L360
    @ModifyExpressionValue(method = { "appendItemLayers", "shouldPlaySwapAnimation", "swapAnimationScale" }, at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ItemStack;get(Lnet/minecraft/core/component/DataComponentType;)Ljava/lang/Object;"))
    private Object detexturify$appendItemLayers(Object original, @Local(argsOnly = true) ItemStack stack) {
        if (!detexturify$bool) return original;
        if (stack.isEmpty()) return original;

        final Identifier a = (Identifier) original;
        if (a == null) return null;
        if (!a.getNamespace().equals("hypixel_skyblock")) return original;

        final ItemStackDuck b = (ItemStackDuck) (Object) stack;
        final Identifier c = b.detexturify$id();
        if (c != null) {
            if (detexturify$component) stack.set(DataComponents.ITEM_MODEL, c);
            return c;
        }

        final CompoundTag d = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
        if (d.contains("quiver_arrow")) {
            final Identifier e = Items.ARROW.components().get(DataComponents.ITEM_MODEL);
            b.detexturify$id(e);
            return e;
        }

        final Optional<String> e = d.getString("id");
        if (e.isEmpty()) return original;

        final String f = e.get().replace(":", "-");
        final boolean g = MainCategory.INSTANCE.getFilterType() == MainCategory.FilterType.WHITELIST ? !Detexturify.WHITELIST.getValue().contains(f) : Detexturify.BLACKLIST.getValue().contains(f);

        if (!g) {
            b.detexturify$id(a);
            return original;
        }

        final SkyBlockItem h = Detexturify.MAP.get(f);
        if (h == null) return original;

        final Identifier i = h.getId();
        b.detexturify$id(i);

        return i;
    }
}
