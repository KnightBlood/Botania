package vazkii.botania.mixin;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityReference;
import net.minecraft.world.entity.projectile.Projectile;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(Projectile.class)
public interface ProjectileAccessor {
	@Accessor("leftOwner")
	boolean botania_getLeftOwner();

	// 26.1: the ownerUUID field was replaced by an EntityReference-typed owner field
	@Accessor("owner")
	EntityReference<Entity> botania_getOwnerRef();

	@Accessor("owner")
	void botania_setOwnerRef(EntityReference<Entity> owner);
}
