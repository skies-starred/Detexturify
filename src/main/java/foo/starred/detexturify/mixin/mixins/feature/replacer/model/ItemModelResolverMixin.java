package foo.starred.detexturify.mixin.mixins.feature.replacer.model;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import foo.starred.detexturify.config.impl.DetexturifyConfig;
import foo.starred.detexturify.impl.data.data.SkyBlockItemData;
import foo.starred.detexturify.duck.ItemStackDuck;
import foo.starred.detexturify.impl.data.loader.DetexturifyItemDataInstance;
import foo.starred.kontext.api.item.extension.ItemStackGenericExtensionsKt;
import kotlin.Unit;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

import java.util.Objects;
import java.util.Set;

import static foo.starred.kontext.abstraction.client.extension.MinecraftClientExtensionsKt.client;

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
    private static boolean detexturify$bool = DetexturifyConfig.INSTANCE.getEnabled().getValue() && DetexturifyConfig.INSTANCE.getRetexture().getValue();

    @Unique
    private static boolean detexturify$component = DetexturifyConfig.INSTANCE.getRetextureType().getValue() == DetexturifyConfig.RetextureType.COMPONENT;

    static {
        DetexturifyConfig.INSTANCE.getEnabled().observe(bool -> {
            detexturify$bool = bool && DetexturifyConfig.INSTANCE.getRetexture().getValue();
            return Unit.INSTANCE;
        });

        DetexturifyConfig.INSTANCE.getRetexture().observe(bool -> {
            detexturify$bool = bool && DetexturifyConfig.INSTANCE.getEnabled().getValue();
            return Unit.INSTANCE;
        });

        DetexturifyConfig.INSTANCE.getRetextureType().observe(type -> {
            detexturify$component = type == DetexturifyConfig.RetextureType.COMPONENT;
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

        final Identifier model = (Identifier) original;
        if (model == null) return null;
        if (!model.getNamespace().equals("hypixel_skyblock")) return original;

        final ItemStackDuck duck = (ItemStackDuck) (Object) stack;
        final Identifier cached = duck.detexturify$id();
        final int special = duck.detexturify$special();

        if (cached != null) {
            if (detexturify$component) stack.set(DataComponents.ITEM_MODEL, cached);
            return cached;
        }

        final CompoundTag tag = ItemStackGenericExtensionsKt.customData(stack);
        if (tag == null) return null;

        final int mode = tag.getInt("td_attune_mode").orElse(-1);
        if (mode == -1 && tag.contains("quiver_arrow")) {
            duck.detexturify$id(detexturify$arrow);
            return detexturify$arrow;
        }

        final String id = tag.getStringOr("id", null);
        if (id == null) return original;

        final String key = id.replace(':', '-');
        if (!detexturify$filter(duck, key)) {
            duck.detexturify$id(model);
            return original;
        }

        final String path = DetexturifyItemDataInstance.REPLACEMENTS.getValue().get(key);
        if (path != null) {
            final Identifier replacement = Identifier.withDefaultNamespace(path);
            duck.detexturify$id(replacement);
            return replacement;
        }

        if (special == 0 || detexturify$katanas.contains(key)) {
            duck.detexturify$special(0);

            final LocalPlayer player = client.player;
            return player != null && player.getCooldowns().isOnCooldown(stack) ? detexturify$sword$1 : detexturify$sword$3;
        }

        if (mode != -1 && special != 1) duck.detexturify$special(1);
        if (mode == 0) return detexturify$sword$0;
        if (mode == 1) return detexturify$sword$1;
        if (mode == 2) return detexturify$sword$2;
        if (mode == 3) return detexturify$sword$3;

        final SkyBlockItemData entry = DetexturifyItemDataInstance.MAP.get(key);
        if (entry == null) return original;

        final Identifier target = entry.getId();
        if (Objects.equals(target.getPath(), "player_head") && entry.getModel() == null) return original;

        duck.detexturify$id(target);
        return target;
    }

    @Unique
    private static boolean detexturify$filter(ItemStackDuck duck, String key) {
        final Boolean cached = duck.detexturify$filtered();
        if (cached != null) return cached;

        final boolean filtered = DetexturifyConfig.INSTANCE.getFilterType() == DetexturifyConfig.FilterType.WHITELIST
                ? !DetexturifyItemDataInstance.WHITELIST.getValue().contains(key)
                : DetexturifyItemDataInstance.BLACKLIST.getValue().contains(key);

        duck.detexturify$filtered(filtered);
        return filtered;
    }
}
