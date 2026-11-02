package vazkii.botania.forge.mixin;

import net.minecraft.core.NonNullList;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.AbstractFurnaceBlockEntity;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(AbstractFurnaceBlockEntity.class)
public interface AbstractFurnaceBlockEntityForgeAccessor {
	// 26.1: canBurn is now static and only checks output-slot capacity (items, maxStackSize, result);
	// the recipe assembly is done by the caller.
	@Invoker("canBurn")
	static boolean callCanBurn(NonNullList<ItemStack> items, int maxStackSize, ItemStack result) {
		throw new IllegalStateException();
	}
}
