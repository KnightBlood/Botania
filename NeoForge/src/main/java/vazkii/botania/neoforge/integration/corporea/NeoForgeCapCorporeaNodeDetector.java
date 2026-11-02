package vazkii.botania.neoforge.integration.corporea;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.item.ItemResource;

import org.jetbrains.annotations.Nullable;

import vazkii.botania.api.corporea.CorporeaNode;
import vazkii.botania.api.corporea.CorporeaNodeDetector;
import vazkii.botania.api.corporea.CorporeaSpark;

public class NeoForgeCapCorporeaNodeDetector implements CorporeaNodeDetector {
	@Nullable
	@Override
	public CorporeaNode getNode(Level world, CorporeaSpark spark) {
		ResourceHandler<ItemResource> inv = getInventory(world, spark.getAttachPos());
		if (inv != null) {
			return new NeoForgeCapCorporeaNode(world, spark.getAttachPos(), inv, spark);
		}
		return null;
	}

	@Nullable
	private static ResourceHandler<ItemResource> getInventory(Level level, BlockPos pos) {
		var be = level.getBlockEntity(pos);

		if (be == null) {
			return null;
		}

		var state = be.getBlockState();
		ResourceHandler<ItemResource> ret = Capabilities.Item.BLOCK.getCapability(level, pos, state, be, Direction.UP);
		if (ret == null) {
			ret = Capabilities.Item.BLOCK.getCapability(level, pos, state, be, null);
		}
		return ret;
	}
}
