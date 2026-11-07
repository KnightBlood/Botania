package vazkii.botania.mixin;

import net.minecraft.world.entity.animal.cow.MushroomCow;
import net.minecraft.world.item.component.SuspiciousStewEffects;

import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(MushroomCow.class)
public interface MushroomCowAccessor {
	@Accessor
	@Nullable
	SuspiciousStewEffects getStewEffects();

	@Accessor
	void setStewEffects(@Nullable SuspiciousStewEffects effects);

	// 26.1: the old public MushroomCow#setVariant(MushroomType) is now private and the
	// enum is MushroomCow.Variant; expose it for the drum -> brown mooshroom gametest.
	@Invoker("setVariant")
	void botania_setVariant(MushroomCow.Variant variant);
}
