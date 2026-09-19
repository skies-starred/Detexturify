package foo.starred.detexturify.mixin.mixins;

import foo.starred.detexturify.config.DetexturifyConfig;
import net.minecraft.client.renderer.ShaderManager;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.Resource;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Map;

//? if >= 26.3 {
/*import com.google.common.collect.ImmutableMap;
import com.mojang.renderpearl.api.pipeline.ShaderSource;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
*///?} else {
import com.mojang.blaze3d.preprocessor.GlslPreprocessor;
import org.jspecify.annotations.NonNull;
//?}

@Mixin(ShaderManager.class)
public class ShaderManagerMixin {
    @Unique
    private static final String detexturify$no_op = """
        vec4 getModifiedVanillaColor(vec4 color) { return color; }
        vec4 getModifiedNormalColor(int color, float alpha) { return vec4(0.0); }
        vec4 getModifiedShadowColor(int color, float alpha) { return vec4(0.0); }
        """;

    //? if >= 26.3 {
    /*@Inject(method = "loadInclude", at = @At("HEAD"), cancellable = true)
    private static void detexturify$loadInclude(Identifier location, Resource resource, ImmutableMap.Builder<Identifier, ShaderSource.CachedIncludeSource> builder, CallbackInfo ci) {
        if (!DetexturifyConfig.INSTANCE.getEnabled().getValue()) return;
        if (!DetexturifyConfig.INSTANCE.getVanillaColor()) return;
        if (!location.getPath().endsWith("modify_vanilla_color.glsl")) return;

        Identifier id = ShaderManager.SHADER_INCLUDE_CONVERTER.fileToId(location).withSuffix(".glsl");
        builder.put(id, ShaderSource.CachedIncludeSource.create(id, detexturify$no_op));
        ci.cancel();
    }
    *///?} else {
    @Inject(method = "createPreprocessor", at = @At("RETURN"), cancellable = true)
    private static void detexturify$createPreprocessor(Map<Identifier, Resource> files, Identifier location, CallbackInfoReturnable<GlslPreprocessor> cir) {
        if (!DetexturifyConfig.INSTANCE.getEnabled().getValue()) return;
        if (!DetexturifyConfig.INSTANCE.getVanillaColor()) return;
        GlslPreprocessor a = cir.getReturnValue();

        cir.setReturnValue(new GlslPreprocessor() {
            @Override
            public String applyImport(boolean b, @NonNull String c) {
                if (!b && c.endsWith("modify_vanilla_color.glsl")) return detexturify$no_op;
                return a.applyImport(b, c);
            }
        });
    }
    //?}
}
