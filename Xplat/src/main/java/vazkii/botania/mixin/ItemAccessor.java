package vazkii.botania.mixin;

import net.minecraft.world.item.Item;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(Item.class)
public interface ItemAccessor {
	// 26.1: cached at construction from Properties.id; backfilled during registration
	@Mutable
	@Accessor("descriptionId")
	void botania_setDescriptionId(String descriptionId);
}
