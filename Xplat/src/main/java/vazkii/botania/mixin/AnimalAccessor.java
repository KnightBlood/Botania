package vazkii.botania.mixin;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EntityReference;
import net.minecraft.world.entity.animal.Animal;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(Animal.class)
public interface AnimalAccessor {
	// 26.1: loveCause stores an EntityReference instead of a UUID
	@Accessor("loveCause")
	void botania_setLoveCause(EntityReference<ServerPlayer> id);
}
