package foo.starred.detexturify.mixin.mixins;

import foo.starred.detexturify.config.categories.MainCategory;
import kotlin.Unit;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.resources.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(value = GuiGraphicsExtractor.class, priority = Integer.MIN_VALUE)
public class GuiGraphicsMixin {
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

    @ModifyVariable(method = "tooltip", at = @At("HEAD"), ordinal = 0, argsOnly = true)
    private Identifier detexturify$renderTooltip(Identifier style) {
        if (style == null) return null;
        if (!detexturify$bool) return style;
        if (!style.getNamespace().equals("hypixel_skyblock")) return style;
        return null;
    }
}
