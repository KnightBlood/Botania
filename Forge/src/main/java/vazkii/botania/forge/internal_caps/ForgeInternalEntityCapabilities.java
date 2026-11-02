package vazkii.botania.forge.internal_caps;

import java.util.function.Supplier;

import net.minecraft.world.entity.item.PrimedTnt;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.EntityEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

import vazkii.botania.common.helper.EthicalTntHelper;
import vazkii.botania.common.internal_caps.EthicalComponent;
import vazkii.botania.common.internal_caps.ItemFlagsComponent;
import vazkii.botania.common.internal_caps.KeptItemsComponent;
import vazkii.botania.common.internal_caps.LooniumComponent;
import vazkii.botania.common.internal_caps.NarslimmusComponent;
import vazkii.botania.common.internal_caps.SpectralRailComponent;
import vazkii.botania.common.internal_caps.TigerseyeComponent;
import vazkii.botania.common.lib.LibMisc;

public final class ForgeInternalEntityCapabilities {
	private static final DeferredRegister<AttachmentType<?>> ATTACHMENTS = DeferredRegister
			.create(NeoForgeRegistries.Keys.ATTACHMENT_TYPES, LibMisc.MOD_ID);

	public static final DeferredHolder<AttachmentType<?>, AttachmentType<EthicalComponent>> TNT_ETHICAL = ATTACHMENTS
			.register("tnt_ethical", () -> AttachmentType.builder(() -> new EthicalComponent())
					.serialize(EthicalComponent.CODEC).build());
	public static final DeferredHolder<AttachmentType<?>, AttachmentType<SpectralRailComponent>> GHOST_RAIL = ATTACHMENTS
			.register("ghost_rail", () -> AttachmentType.builder(SpectralRailComponent::new).serialize(SpectralRailComponent.CODEC).build());
	public static final DeferredHolder<AttachmentType<?>, AttachmentType<ItemFlagsComponent>> INTERNAL_ITEM = ATTACHMENTS
			.register("iitem", () -> AttachmentType.builder(ItemFlagsComponent::new).serialize(ItemFlagsComponent.CODEC).build());
	public static final DeferredHolder<AttachmentType<?>, AttachmentType<KeptItemsComponent>> KEPT_ITEMS = ATTACHMENTS
			.register("kept_items", () -> AttachmentType.builder(KeptItemsComponent::new).serialize(KeptItemsComponent.CODEC).copyOnDeath().build());
	public static final DeferredHolder<AttachmentType<?>, AttachmentType<LooniumComponent>> LOONIUM_DROP = ATTACHMENTS
			.register("loonium_drop", () -> AttachmentType.builder(LooniumComponent::new).serialize(LooniumComponent.CODEC).build());
	public static final DeferredHolder<AttachmentType<?>, AttachmentType<NarslimmusComponent>> NARSLIMMUS = ATTACHMENTS
			.register("narslimmus", () -> AttachmentType.builder(NarslimmusComponent::new).serialize(NarslimmusComponent.CODEC).build());
	public static final DeferredHolder<AttachmentType<?>, AttachmentType<TigerseyeComponent>> TIGERSEYE = ATTACHMENTS
			.register("tigerseye_pacified", () -> AttachmentType.builder(TigerseyeComponent::new).serialize(TigerseyeComponent.CODEC).build());

	/**
	 * Keeps track of newly-spawned TNT to detect unethical spawning.
	 * All other components are created lazily through the attachment system.
	 */
	public static void register(IEventBus modBus) {
		ATTACHMENTS.register(modBus);
		// EntityConstructing is a game-bus event (fired for every entity construction),
		// not a mod-bus event - registering it on the mod bus crashes FML loading.
		NeoForge.EVENT_BUS.addListener(ForgeInternalEntityCapabilities::onEntityConstructing);
	}

	private static void onEntityConstructing(EntityEvent.EntityConstructing e) {
		if (e.getEntity() instanceof PrimedTnt tnt && !tnt.level().isClientSide()) {
			EthicalTntHelper.addTrackedTntEntity(tnt);
		}
	}

	public static Supplier<AttachmentType<EthicalComponent>> tntEthical() {
		return TNT_ETHICAL;
	}

	private ForgeInternalEntityCapabilities() {}
}
