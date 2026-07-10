package foo.starred.detexturify.mixin.mixins;

import com.mojang.blaze3d.preprocessor.GlslPreprocessor;
import foo.starred.detexturify.config.categories.MainCategory;
import net.minecraft.client.renderer.ShaderManager;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.Resource;
import org.jspecify.annotations.NonNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Map;
import java.util.Set;

@Mixin(ShaderManager.class)
public class ShaderManagerMixin {
    @Unique
    private static final Set<String> detexturify$set = Set.of("modify_vanilla_color.glsl", "minecraft:modify_vanilla_color.glsl");

    @Unique
    private static final String detexturify$no_op = """
        vec4 getModifiedVanillaColor(vec4 color) { return color; }
        vec4 getModifiedNormalColor(int color, float alpha) { return vec4(0.0); }
        vec4 getModifiedShadowColor(int color, float alpha) { return vec4(0.0); }
        """;

    @Inject(method = "createPreprocessor", at = @At("RETURN"), cancellable = true)
    private static void detexturify$createPreprocessor(Map<Identifier, Resource> files, Identifier location, CallbackInfoReturnable<GlslPreprocessor> cir) {
        if (!MainCategory.INSTANCE.getEnabled().getValue()) return;
        if (!MainCategory.INSTANCE.getVanillaColor()) return;
        GlslPreprocessor a = cir.getReturnValue();

        cir.setReturnValue(new GlslPreprocessor() {
            @Override
            public String applyImport(boolean b, @NonNull String c) {
                if (!b && detexturify$set.contains(c)) return detexturify$no_op;
                return a.applyImport(b, c);
            }
        });
    }
}
