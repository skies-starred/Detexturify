package foo.starred.detexturify.mixin.mixins;

import foo.starred.detexturify.ducks.ItemStackDuck;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ResolvableProfile;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(ItemStack.class)
public class ItemStackMixin implements ItemStackDuck {
    @Unique
    Identifier detexturify$id;

    @Unique
    ResolvableProfile detexturify$profile;

    @Unique
    int detexturify$int = -1;

    @Unique
    @Nullable Boolean detexturify$filtered;

    @Override
    public @Nullable Identifier detexturify$id() {
        return detexturify$id;
    }

    @Override
    public void detexturify$id(@Nullable Identifier identifier) {
        detexturify$id = identifier;
    }

    @Override
    public @Nullable ResolvableProfile detexturify$profile() {
        return detexturify$profile;
    }

    @Override
    public void detexturify$profile(@Nullable ResolvableProfile profile) {
        detexturify$profile = profile;
    }

    @Override
    public int detexturify$special() {
        return detexturify$int;
    }

    @Override
    public void detexturify$special(int i) {
        detexturify$int = i;
    }

    @Override
    public @Nullable Boolean detexturify$filtered() {
        return detexturify$filtered;
    }

    @Override
    public void detexturify$filtered(@Nullable Boolean filtered) {
        detexturify$filtered = filtered;
    }
}
