package foo.starred.detexturify.mixin.mixins;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import foo.starred.detexturify.Detexturify;
import foo.starred.detexturify.config.categories.MainCategory;
import foo.starred.detexturify.data.SkyBlockItem;
import foo.starred.detexturify.ducks.ItemStackDuck;
import kotlin.Unit;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

import java.util.Set;

import static xyz.aerii.library.api.PlayerKt.getPlayer;

@Mixin(ItemModelResolver.class)
public class ItemModelResolverMixin {
    @Unique
    private final static Identifier detexturify$arrow = Identifier.parse("minecraft:arrow");

    @Unique
    private final static Identifier detexturify$sword$0 = Identifier.parse("minecraft:stone_sword");

    @Unique
    private final static Identifier detexturify$sword$1 = Identifier.parse("minecraft:golden_sword");

    @Unique
    private final static Identifier detexturify$sword$2 = Identifier.parse("minecraft:iron_sword");

    @Unique
    private final static Identifier detexturify$sword$3 = Identifier.parse("minecraft:diamond_sword");

    @Unique
    private final static Set<String> detexturify$katanas = Set.of("VOIDEDGE_KATANA", "VORPAL_KATANA", "ATOMSPLIT_KATANA");

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
        final int c0 = b.detexturify$special();

        if (c != null) {
            if (detexturify$component) stack.set(DataComponents.ITEM_MODEL, c);
            return c;
        }

        final CompoundTag d = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
        final int e = d.getInt("td_attune_mode").orElse(-1);

        if (e == -1 && d.contains("quiver_arrow")) {
            b.detexturify$id(detexturify$arrow);
            return detexturify$arrow;
        }

        final String f = d.getStringOr("id", null);
        if (f == null) return original;

        final String g = f.replace(':', '-');
        final boolean h = detexturify$filter(b, g);

        if (!h) {
            b.detexturify$id(a);
            return original;
        }

        if (c0 == 0 || detexturify$katanas.contains(g)) {
            b.detexturify$special(0);
            final LocalPlayer i = getPlayer();
            return (i != null && i.getCooldowns().isOnCooldown(stack)) ? detexturify$sword$1 : detexturify$sword$3;
        }

        if (e != -1 && c0 != 1) b.detexturify$special(1);
        if (e == 0) return detexturify$sword$0;
        if (e == 1) return detexturify$sword$1;
        if (e == 2) return detexturify$sword$2;
        if (e == 3) return detexturify$sword$3;

        final SkyBlockItem i = Detexturify.MAP.get(g);
        if (i == null) return original;

        final Identifier j = i.getId();
        b.detexturify$id(j);

        return j;
    }

    @Unique
    private static boolean detexturify$filter(ItemStackDuck b, String g) {
        final Boolean a = b.detexturify$filtered();
        if (a != null) return a;

        final boolean c = MainCategory.INSTANCE.getFilterType() == MainCategory.FilterType.WHITELIST ? !Detexturify.WHITELIST.getValue().contains(g) : Detexturify.BLACKLIST.getValue().contains(g);
        b.detexturify$filtered(c);
        return c;
    }
}
