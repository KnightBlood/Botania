package vazkii.botania.neoforge.mixin;

import java.util.Map;

import net.minecraft.world.item.ShovelItem;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(ShovelItem.class)
public interface ShovelItemNeoForgeAccessor {
	@Accessor("FLATTENABLES")
	static Map<Block, BlockState> botania$getFlattenables() {
		throw new UnsupportedOperationException();
	}
}
