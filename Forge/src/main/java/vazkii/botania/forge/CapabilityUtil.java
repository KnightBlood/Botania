package vazkii.botania.forge;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.neoforge.fluids.FluidType;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.access.ItemAccess;
import net.neoforged.neoforge.transfer.fluid.FluidResource;
import net.neoforged.neoforge.transfer.fluid.ItemAccessFluidHandler;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;

public final class CapabilityUtil {
	/**
	 * The water bowl always reads as a full bucket of water; draining it swaps
	 * the item for an empty bowl. Filling it with other fluids is not allowed.
	 * (26.1: rewritten on top of the ResourceHandler transfer API, replacing the
	 * removed FluidHandlerItemStackSimple.SwapEmpty template.)
	 */
	public static class WaterBowlFluidHandler extends ItemAccessFluidHandler {
		public WaterBowlFluidHandler(ItemAccess access) {
			super(access, ForgeRegistryHandler.SIMPLE_FLUID_CONTENT.get(), FluidType.BUCKET_VOLUME);
		}

		@Override
		protected FluidResource getResourceFrom(ItemResource resource, int index) {
			return FluidResource.of(Fluids.WATER);
		}

		@Override
		public boolean isValid(int index, FluidResource resource) {
			return false;
		}

		@Override
		protected ItemResource update(ItemResource current, int amount, FluidResource newFluid, int newAmount) {
			if (newAmount == 0) {
				return ItemResource.of(Items.BOWL);
			}
			return current;
		}
	}

	/**
	 * Accepts (and voids) one bucket worth of any fluid at a time,
	 * replacing the old VoidFluidHandler-based extrapolated bucket.
	 */
	public static class ExtrapolatedBucketFluidHandler implements ResourceHandler<FluidResource> {
		private final ItemStack container;

		public ExtrapolatedBucketFluidHandler(ItemStack container) {
			this.container = container;
		}

		@Override
		public int size() {
			return 1;
		}

		@Override
		public FluidResource getResource(int index) {
			return FluidResource.EMPTY;
		}

		@Override
		public long getAmountAsLong(int index) {
			return 0;
		}

		@Override
		public long getCapacityAsLong(int index, FluidResource resource) {
			return FluidType.BUCKET_VOLUME;
		}

		@Override
		public boolean isValid(int index, FluidResource resource) {
			return true;
		}

		@Override
		public int insert(int index, FluidResource resource, int amount, TransactionContext ctx) {
			return Math.min(amount, FluidType.BUCKET_VOLUME);
		}

		@Override
		public int extract(int index, FluidResource resource, int amount, TransactionContext ctx) {
			return 0;
		}
	}

	private CapabilityUtil() {}
}
