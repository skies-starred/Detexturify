package foo.starred.detexturify.mixin.mixins;

import foo.starred.detexturify.pack.HypixelPackCache;
import net.minecraft.server.packs.repository.Pack;
import net.minecraft.server.packs.repository.PackRepository;
import net.minecraft.server.packs.repository.RepositorySource;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

import java.util.Arrays;

@Mixin(PackRepository.class)
public class PackRepositoryMixin {
    @ModifyVariable(method = "<init>", at = @At("HEAD"), argsOnly = true)
    private static RepositorySource[] detexturify$init(RepositorySource[] sources) {
        final RepositorySource[] result = Arrays.copyOf(sources, sources.length + 1);

        result[sources.length] = onLoad -> {
            final Pack pack = HypixelPackCache.active;
            if (pack != null) onLoad.accept(pack);
        };

        return result;
    }
}