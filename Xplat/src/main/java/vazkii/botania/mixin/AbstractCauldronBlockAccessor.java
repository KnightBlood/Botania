package vazkii.botania.mixin;

import net.minecraft.core.cauldron.CauldronInteraction;
import net.minecraft.world.level.block.AbstractCauldronBlock;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(AbstractCauldronBlock.class)
public interface AbstractCauldronBlockAccessor {
	// 26.1: interactions was a Map<Item, CauldronInteraction>, now a Dispatcher with a get(ItemStack) lookup
	@Accessor("interactions")
	CauldronInteraction.Dispatcher botania_getInteractions();
}
