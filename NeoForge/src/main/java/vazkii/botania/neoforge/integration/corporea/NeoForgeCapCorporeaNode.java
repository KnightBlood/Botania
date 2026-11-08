package vazkii.botania.neoforge.integration.corporea;

import com.google.common.collect.ImmutableList;

import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.transaction.Transaction;

import vazkii.botania.api.corporea.CorporeaRequest;
import vazkii.botania.api.corporea.CorporeaSpark;
import vazkii.botania.common.impl.corporea.AbstractCorporeaNode;

import java.util.List;

/**
 * 26.1: rewritten on top of the ResourceHandler transfer API,
 * replacing the removed IItemHandler-based block capability.
 */
public class NeoForgeCapCorporeaNode extends AbstractCorporeaNode {
	protected final ResourceHandler<ItemResource> inv;

	public NeoForgeCapCorporeaNode(Level world, BlockPos pos, ResourceHandler<ItemResource> inv, CorporeaSpark spark) {
		super(world, pos, spark);
		this.inv = inv;
	}

	@Override
	public List<ItemStack> countItems(CorporeaRequest request) {
		return iterateOverSlots(request, false);
	}

	@Override
	public List<ItemStack> extractItems(CorporeaRequest request) {
		return iterateOverSlots(request, true);
	}

	protected List<ItemStack> iterateOverSlots(CorporeaRequest request, boolean doit) {
		ImmutableList.Builder<ItemStack> builder = ImmutableList.builder();

		for (int i = inv.size() - 1; i >= 0; i--) {
			ItemResource resource = inv.getResource(i);
			if (resource.isEmpty()) {
				continue;
			}
			ItemStack stackAt = resource.toStack((int) Math.min(inv.getAmountAsLong(i), Integer.MAX_VALUE));
			if (request.getMatcher().test(stackAt)) {
				request.trackFound(stackAt.getCount());

				int rem = Math.min(stackAt.getCount(), request.getStillNeeded() == -1 ? stackAt.getCount() : request.getStillNeeded());
				if (rem > 0) {
					request.trackSatisfied(rem);

					if (doit) {
						int extracted;
						try (var tx = Transaction.openRoot()) {
							extracted = inv.extract(i, resource, rem, tx);
							if (extracted > 0 && !getSpark().isCreative()) {
								tx.commit();
							}
						}
						if (extracted > 0) {
							ItemStack copy = resource.toStack(Math.min(extracted, stackAt.getCount()));
							builder.addAll(breakDownBigStack(resource.toStack(extracted)));
							getSpark().onItemExtracted(copy);
							request.trackExtracted(extracted);
						}
					} else {
						try (var tx = Transaction.openRoot()) {
							int simulated = inv.extract(i, resource, rem, tx);
							if (simulated > 0) {
								builder.add(resource.toStack(simulated));
							}
						}
					}
				}
			}
		}

		return builder.build();
	}

}
