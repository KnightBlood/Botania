package vazkii.botania.forge.internal_caps;

import net.minecraft.core.Direction;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.ICapabilityProvider;
import net.neoforged.neoforge.transfer.EmptyResourceHandler;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.item.ItemResource;

import org.jetbrains.annotations.Nullable;

import vazkii.botania.common.block.block_entity.red_string.RedStringContainerBlockEntity;

public class RedStringContainerCapProvider implements ICapabilityProvider<RedStringContainerBlockEntity, Direction, ResourceHandler<ItemResource>> {
	private static final ResourceHandler<ItemResource> EMPTY = EmptyResourceHandler.instance();

	private final RedStringContainerBlockEntity container;

	public RedStringContainerCapProvider(RedStringContainerBlockEntity container) {
		this.container = container;
	}

	@Nullable
	@Override
	public ResourceHandler<ItemResource> getCapability(RedStringContainerBlockEntity be, @Nullable Direction side) {
		BlockEntity binding = container.getTileAtBinding();
		if (binding != null && binding.getLevel() != null) {
			return Capabilities.Item.BLOCK.getCapability(binding.getLevel(), binding.getBlockPos(),
					binding.getBlockState(), binding, side);
		}
		return EMPTY;
	}
}
