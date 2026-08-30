package foo.starred.detexturify.mixin.mixins;

import com.google.common.collect.ImmutableList;
import net.minecraft.server.packs.repository.Pack;
import net.minecraft.server.packs.repository.PackRepository;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.IntStream;

@Mixin(PackRepository.class)
public class PackRepositoryMixin {
    @Inject(method = "rebuildSelected", at = @At("RETURN"), cancellable = true)
    private void detexturify$rebuildSelected(Collection<String> selectedNames, CallbackInfoReturnable<List<Pack>> cir) {
        final List<Pack> packs = cir.getReturnValue();
        if (packs == null) return;
        if (packs.isEmpty()) return;
        if (selectedNames.contains("detexturify/fallback/hypixel")) return;

        final int i0 = IntStream.range(0, packs.size()).filter(i -> packs.get(i).getId().equals("detexturify/fallback/hypixel")).findFirst().orElse(-1);
        final int i1 = IntStream.range(0, packs.size()).filter(i -> packs.get(i).getId().equals("vanilla")).findFirst().orElse(-1);

        if (i0 == -1) return;
        if (i1 == -1) return;
        if (i0 == i1 + 1) return;

        final List<Pack> mutable = new ArrayList<>(packs);
        final Pack pack = mutable.remove(i0);
        mutable.add(i0 < i1 ? i1 : i1 + 1, pack);

        cir.setReturnValue(ImmutableList.copyOf(mutable));
    }
}
