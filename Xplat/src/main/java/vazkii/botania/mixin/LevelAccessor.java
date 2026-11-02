package vazkii.botania.mixin;

import net.minecraft.world.level.Level;
import net.minecraft.world.level.redstone.CollectingNeighborUpdater;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(Level.class)
public interface LevelAccessor {
	// 26.1: the field's type is now the concrete CollectingNeighborUpdater, so the
	// accessor signature must match exactly (callers still use it as a NeighborUpdater).
	@Accessor("neighborUpdater")
	CollectingNeighborUpdater getNeighborUpdater();
}
