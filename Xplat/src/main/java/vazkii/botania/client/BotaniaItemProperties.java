package vazkii.botania.client;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.item.properties.numeric.RangeSelectItemModelProperty;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.ItemOwner;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;

import org.jetbrains.annotations.Nullable;

import vazkii.botania.client.core.proxy.ClientProxy;
import vazkii.botania.common.block.mana.ManaPoolBlock;
import vazkii.botania.common.helper.ItemNBTHelper;
import vazkii.botania.common.item.*;
import vazkii.botania.common.item.brew.BaseBrewItem;
import vazkii.botania.common.item.equipment.bauble.RingOfMagnetizationItem;
import vazkii.botania.common.item.equipment.tool.bow.LivingwoodBowItem;
import vazkii.botania.common.item.equipment.tool.terrasteel.TerraShattererItem;
import vazkii.botania.common.item.equipment.tool.terrasteel.TerraTruncatorItem;
import vazkii.botania.common.item.relic.FruitOfGrisaiaItem;
import vazkii.botania.common.item.rod.SkiesRodItem;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import static vazkii.botania.common.lib.ResourceLocationHelper.prefix;

/**
 * Registers Botania's custom range-select item model properties.
 * <p>
 * In MC 26.1 the old {@code ItemProperties.register(...)} API was replaced by the
 * {@link RangeSelectItemModelProperty} system. Each property is a codec registered under an
 * identifier, and is evaluated against an {@link ItemStack} to produce a float in {@code [0, 1]}
 * which is then used to pick a model variant.
 */
public final class BotaniaItemProperties {
	/**
	 * Registers all Botania item model properties. The consumer is given the property identifier
	 * and the codec that produces its value.
	 */
	public static void init(PropertyRegistrar registrar) {
		register(registrar, prefix("open"), new TagFlag(BaubleBoxItem.TAG_OPEN, false));
		register(registrar, prefix("active"), new TagFlag(BlackHoleTalismanItem.TAG_ACTIVE, false));
		register(registrar, prefix("swigs_taken"), new SwigsTaken());
		register(registrar, prefix("vuvuzela"), new Vuvuzela());
		register(registrar, prefix("elven"), new TagFlag(LexicaBotaniaItem.TAG_ELVEN_UNLOCK, false));
		register(registrar, prefix("totalbiscuit"), new ContainsName("totalbiscuit"));
		register(registrar, prefix("active_slime"), new TagFlag(SlimeInABottleItem.TAG_ACTIVE, false));
		register(registrar, prefix("full"), new Full());
		register(registrar, prefix("active_temperance"), new TagFlag(StoneOfTemperanceItem.TAG_ACTIVE, false));
		register(registrar, prefix("bindmode"), new BindMode());
		register(registrar, prefix("active_halo"), new TagFlag(ManufactoryHaloItem.TAG_ACTIVE, true));
		register(registrar, prefix("holiday"), new Holiday());
		register(registrar, prefix("active_ring"), new MagnetRing());
		register(registrar, prefix("reddit"), new EqualsName("dammit reddit"));
		register(registrar, prefix("elucidator"), new EqualsName("the elucidator"));
		register(registrar, prefix("tipped"), new TagFlag("Tipped", false));
		register(registrar, prefix("boot"), new Boot());
		register(registrar, prefix("active_tornado"), new TagFlag(SkiesRodItem.TAG_FLYING, false));
		register(registrar, prefix("pull"), new Pull());
		register(registrar, prefix("terra_axe_active"), new TerraAxeActive());
		register(registrar, prefix("terra_pick_active"), new TagFlag(TerraShattererItem.TAG_ENABLED, false));
	}

	/**
	 * Looks up a registered property. Datagen needs the real instance: the id map resolves the
	 * property id back through the codec instance identity, so a stand-in codec cannot be encoded.
	 */
	public static RangeSelectItemModelProperty get(Identifier id) {
		RangeSelectItemModelProperty property = BY_ID.get(id);
		if (property == null) {
			throw new IllegalArgumentException("Unregistered Botania range-select item model property " + id);
		}
		return property;
	}

	private static void register(PropertyRegistrar registrar, Identifier id, RangeSelectItemModelProperty property) {
		Bound bound = new Bound(property);
		BY_ID.put(id, bound);
		registrar.register(id, bound.type());
	}

	private static final Map<Identifier, RangeSelectItemModelProperty> BY_ID = new HashMap<>();

	/**
	 * Wraps a property so that each registered id owns a <em>distinct</em> codec instance.
	 * <p>
	 * {@code RangeSelectItemModelProperties} stores id → codec in a {@link com.google.common.collect.BiMap}
	 * and encodes by looking the codec instance up again, so ids sharing one codec (all of Botania's
	 * {@link TagFlag} variants do) could never be written back out under the right id. The unit codec is
	 * per-id by construction: the id itself carries the parameters, and decoding returns this instance.
	 */
	private static final class Bound implements RangeSelectItemModelProperty {
		private final RangeSelectItemModelProperty delegate;
		private MapCodec<Bound> codec;

		private Bound(RangeSelectItemModelProperty delegate) {
			this.delegate = delegate;
		}

		@Override
		public float get(ItemStack itemStack, @Nullable ClientLevel level, @Nullable ItemOwner owner, int seed) {
			return delegate.get(itemStack, level, owner, seed);
		}

		@Override
		public MapCodec<Bound> type() {
			if (codec == null) {
				codec = MapCodec.unit(this);
			}
			return codec;
		}
	}

	/**
	 * Callback used to hand a property's codec to the platform's registration hook.
	 */
	@FunctionalInterface
	public interface PropertyRegistrar {
		void register(Identifier id, MapCodec<? extends RangeSelectItemModelProperty> codec);
	}

	private record TagFlag(String tag, boolean defaultValue) implements RangeSelectItemModelProperty {
		@Override
		public float get(ItemStack itemStack, @Nullable ClientLevel level, @Nullable ItemOwner owner, int seed) {
			return ItemNBTHelper.getBoolean(itemStack, tag, defaultValue) ? 1.0F : 0.0F;
		}

		@Override
		public MapCodec<TagFlag> type() {
			return MAP_CODEC;
		}

		private static final MapCodec<TagFlag> MAP_CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
				Codec.STRING.fieldOf("tag").forGetter(TagFlag::tag),
				Codec.BOOL.optionalFieldOf("default", false).forGetter(TagFlag::defaultValue)
		).apply(i, TagFlag::new));
	}

	private record SwigsTaken() implements RangeSelectItemModelProperty {
		@Override
		public float get(ItemStack itemStack, @Nullable ClientLevel level, @Nullable ItemOwner owner, int seed) {
			if (itemStack.getItem() instanceof BaseBrewItem brewItem) {
				int swigsLeft = brewItem.getSwigsLeft(itemStack) - 1;
				int totalSwigs = brewItem.getSwigs() - 1;
				return swigsLeft == totalSwigs ? 0.0F : Math.nextUp((totalSwigs - swigsLeft) / (float) totalSwigs);
			}
			int swigsLeft = BottledManaItem.getSwigsLeft(itemStack) - 1;
			int totalSwigs = BottledManaItem.SWIGS - 1;
			return swigsLeft == totalSwigs ? 0.0F : Math.nextUp((totalSwigs - swigsLeft) / (float) totalSwigs);
		}

		@Override
		public MapCodec<SwigsTaken> type() {
			return MAP_CODEC;
		}

		private static final MapCodec<SwigsTaken> MAP_CODEC = MapCodec.unit(new SwigsTaken());
	}

	private record Vuvuzela() implements RangeSelectItemModelProperty {
		@Override
		public float get(ItemStack itemStack, @Nullable ClientLevel level, @Nullable ItemOwner owner, int seed) {
			return itemStack.getHoverName().getString().toLowerCase(Locale.ROOT).contains("vuvuzela") ? 1.0F : 0.0F;
		}

		@Override
		public MapCodec<Vuvuzela> type() {
			return MAP_CODEC;
		}

		private static final MapCodec<Vuvuzela> MAP_CODEC = MapCodec.unit(new Vuvuzela());
	}

	private record ContainsName(String needle) implements RangeSelectItemModelProperty {
		@Override
		public float get(ItemStack itemStack, @Nullable ClientLevel level, @Nullable ItemOwner owner, int seed) {
			return itemStack.getHoverName().getString().toLowerCase(Locale.ROOT).contains(needle) ? 1.0F : 0.0F;
		}

		@Override
		public MapCodec<ContainsName> type() {
			return MAP_CODEC;
		}

		private static final MapCodec<ContainsName> MAP_CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
				Codec.STRING.fieldOf("name").forGetter(ContainsName::needle)
		).apply(i, ContainsName::new));
	}

	private record EqualsName(String name) implements RangeSelectItemModelProperty {
		@Override
		public float get(ItemStack itemStack, @Nullable ClientLevel level, @Nullable ItemOwner owner, int seed) {
			return name.equals(itemStack.getHoverName().getString().toLowerCase(Locale.ROOT).trim()) ? 1.0F : 0.0F;
		}

		@Override
		public MapCodec<EqualsName> type() {
			return MAP_CODEC;
		}

		private static final MapCodec<EqualsName> MAP_CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
				Codec.STRING.fieldOf("name").forGetter(EqualsName::name)
		).apply(i, EqualsName::new));
	}

	private record Full() implements RangeSelectItemModelProperty {
		@Override
		public float get(ItemStack itemStack, @Nullable ClientLevel level, @Nullable ItemOwner owner, int seed) {
			if (itemStack.getItem() instanceof BlockItem blockItem) {
				Block block = blockItem.getBlock();
				if (block instanceof ManaPoolBlock pool) {
					boolean renderFull = pool.variant == ManaPoolBlock.Variant.CREATIVE || ItemNBTHelper.getBoolean(itemStack, "RenderFull", false);
					return renderFull ? 1.0F : 0.0F;
				}
			}
			return LifeAggregatorItem.hasData(itemStack) ? 1.0F : 0.0F;
		}

		@Override
		public MapCodec<Full> type() {
			return MAP_CODEC;
		}

		private static final MapCodec<Full> MAP_CODEC = MapCodec.unit(new Full());
	}

	private record BindMode() implements RangeSelectItemModelProperty {
		@Override
		public float get(ItemStack itemStack, @Nullable ClientLevel level, @Nullable ItemOwner owner, int seed) {
			return WandOfTheForestItem.getBindMode(itemStack) ? 1.0F : 0.0F;
		}

		@Override
		public MapCodec<BindMode> type() {
			return MAP_CODEC;
		}

		private static final MapCodec<BindMode> MAP_CODEC = MapCodec.unit(new BindMode());
	}

	private record Holiday() implements RangeSelectItemModelProperty {
		@Override
		public float get(ItemStack itemStack, @Nullable ClientLevel level, @Nullable ItemOwner owner, int seed) {
			return ClientProxy.jingleTheBells ? 1.0F : 0.0F;
		}

		@Override
		public MapCodec<Holiday> type() {
			return MAP_CODEC;
		}

		private static final MapCodec<Holiday> MAP_CODEC = MapCodec.unit(new Holiday());
	}

	private record MagnetRing() implements RangeSelectItemModelProperty {
		@Override
		public float get(ItemStack itemStack, @Nullable ClientLevel level, @Nullable ItemOwner owner, int seed) {
			return RingOfMagnetizationItem.getCooldown(itemStack) <= 0 ? 1.0F : 0.0F;
		}

		@Override
		public MapCodec<MagnetRing> type() {
			return MAP_CODEC;
		}

		private static final MapCodec<MagnetRing> MAP_CODEC = MapCodec.unit(new MagnetRing());
	}

	private record Boot() implements RangeSelectItemModelProperty {
		@Override
		public float get(ItemStack itemStack, @Nullable ClientLevel level, @Nullable ItemOwner owner, int seed) {
			return FruitOfGrisaiaItem.isBoot(itemStack) ? 1.0F : 0.0F;
		}

		@Override
		public MapCodec<Boot> type() {
			return MAP_CODEC;
		}

		private static final MapCodec<Boot> MAP_CODEC = MapCodec.unit(new Boot());
	}

	/**
	 * Terra pickaxe: {@code active} means enabled, but the terra axe's {@code active} means
	 * "will break blocks", which depends on the player holding it.
	 */
	private record TerraAxeActive() implements RangeSelectItemModelProperty {
		@Override
		public float get(ItemStack itemStack, @Nullable ClientLevel level, @Nullable ItemOwner owner, int seed) {
			LivingEntity entity = owner == null ? null : owner.asLivingEntity();
			if (entity instanceof Player player && !TerraTruncatorItem.shouldBreak(player)) {
				return 0.0F;
			}
			return 1.0F;
		}

		@Override
		public MapCodec<TerraAxeActive> type() {
			return MAP_CODEC;
		}

		private static final MapCodec<TerraAxeActive> MAP_CODEC = MapCodec.unit(new TerraAxeActive());
	}

	/**
	 * [VanillaCopy] ItemProperties' {@code pull} property, scaled by the bow's charge velocity multiplier.
	 */
	private record Pull() implements RangeSelectItemModelProperty {
		@Override
		public float get(ItemStack itemStack, @Nullable ClientLevel level, @Nullable ItemOwner owner, int seed) {
			LivingEntity entity = owner == null ? null : owner.asLivingEntity();
			if (entity == null || !(itemStack.getItem() instanceof LivingwoodBowItem item)
					|| entity.getUseItem() != itemStack) {
				return 0.0F;
			}
			return (itemStack.getUseDuration(entity) - entity.getUseItemRemainingTicks())
					* item.chargeVelocityMultiplier() / 20.0F;
		}

		@Override
		public MapCodec<Pull> type() {
			return MAP_CODEC;
		}

		private static final MapCodec<Pull> MAP_CODEC = MapCodec.unit(new Pull());
	}

	private BotaniaItemProperties() {}
}
