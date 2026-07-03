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
@Mixin(value = GuiGraphics.class, priority = Integer.MAX_VALUE)
public class GuiGraphicsMixin {
    @Unique
    private static int detexturify$enabled = MainCategory.INSTANCE.getEnabled().getValue() ? 1 : 0;

    static {
        MainCategory.INSTANCE.getEnabled().onChange(bool -> {
            detexturify$enabled = bool ? 1 : 0;
            return Unit.INSTANCE;
        });
    }

    //~ if >= 26.1 'renderTooltip' -> 'tooltip'
    @ModifyVariable(method = "renderTooltip", at = @At("HEAD"), ordinal = 0, argsOnly = true)
    private Identifier detexturify$renderTooltip(Identifier background) {
        if (background == null) return null;
        if (detexturify$enabled == 0) return background;
        if (background.getNamespace().equals("hypixel_skyblock")) return null;
        return background;
    }
}