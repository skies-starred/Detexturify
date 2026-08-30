package foo.starred.detexturify.mixin.mixins;

import foo.starred.detexturify.pack.HypixelPackCache;
import net.minecraft.client.Minecraft;
import net.minecraft.server.packs.repository.Pack;
import net.minecraft.server.packs.repository.RepositorySource;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

import java.util.Arrays;

@Mixin(Minecraft.class)
public class MinecraftMixin {
    @ModifyArg(method = "<init>", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/packs/repository/PackRepository;<init>([Lnet/minecraft/server/packs/repository/RepositorySource;)V"), index = 0)
    private static RepositorySource[] detexturify$init(RepositorySource[] sources) {
        final RepositorySource[] result = Arrays.copyOf(sources, sources.length + 1);

        result[sources.length] = onLoad -> {
            final Pack pack = HypixelPackCache.active;
            if (pack != null) onLoad.accept(pack);
        };

        return result;
    }
}
