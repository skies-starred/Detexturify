package foo.starred.detexturify.mixin.mixins;

import foo.starred.detexturify.config.categories.MainCategory;
import kotlin.Unit;
//~ if >= 26.1 'GuiGraphics' -> 'GuiGraphicsExtractor'
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

//~ if >= 26.1 'GuiGraphics' -> 'GuiGraphicsExtractor'
@Mixin(value = GuiGraphics.class, priority = Integer.MIN_VALUE)
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

    //~ if >= 26.1 'renderTooltip' -> 'tooltip'
    @ModifyVariable(method = "renderTooltip", at = @At("HEAD"), ordinal = 0, argsOnly = true)
    private Identifier detexturify$renderTooltip(Identifier background) {
        if (background == null) return null;
        if (!detexturify$bool) return background;
        if (!background.getNamespace().equals("hypixel_skyblock")) return background;
        return null;
    }
}