/*
 * This class is distributed as part of the Botania Mod.
 * Get the Source Code in github:
 * https://github.com/Vazkii/Botania
 *
 * Botania is Open Source and distributed under the
 * Botania License: http://botaniamod.net/license.php
 */
package vazkii.botania.common.item.brew;

import com.google.common.collect.Lists;
import com.mojang.datafixers.util.Pair;

import net.minecraft.ChatFormatting;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffectUtil;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.level.Level;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.item.component.TooltipDisplay;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import vazkii.botania.api.BotaniaAPI;
import vazkii.botania.api.brew.Brew;
import vazkii.botania.api.brew.BrewItem;
import vazkii.botania.common.brew.BotaniaBrews;
import vazkii.botania.common.helper.ItemNBTHelper;
import vazkii.botania.common.item.CustomCreativeTabContents;

import java.text.DecimalFormat;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;
import java.util.function.Consumer;

import static vazkii.botania.common.lib.ResourceLocationHelper.prefix;

public class BaseBrewItem extends Item implements BrewItem, CustomCreativeTabContents {

	private static final String TAG_BREW_KEY = "brewKey";
	private static final String TAG_SWIGS_LEFT = "swigsLeft";
	private static final String TAG_KEEP = "keep";
	// [VanillaCopy] was ItemStack.ATTRIBUTE_MODIFIER_FORMAT, removed in 26.1
	private static final DecimalFormat ATTRIBUTE_MODIFIER_FORMAT = new DecimalFormat("####.###");

	private final int swigs;
	private final int drinkSpeed;
	private final Supplier<Item> baseItem;

	public BaseBrewItem(Properties builder, int swigs, int drinkSpeed, Supplier<Item> baseItem) {
		super(builder);
		this.swigs = swigs;
		this.drinkSpeed = drinkSpeed;
		this.baseItem = baseItem;
	}

	@Override
	public int getUseDuration(ItemStack stack, LivingEntity user) {
		return drinkSpeed;
	}

	@NotNull
	@Override
	public ItemUseAnimation getUseAnimation(ItemStack stack) {
		return ItemUseAnimation.DRINK;
	}

	@NotNull
	@Override
	public InteractionResult use(Level world, Player player, @NotNull InteractionHand hand) {
		return ItemUtils.startUsingInstantly(world, player, hand);
	}

	@NotNull
	@Override
	public ItemStack finishUsingItem(@NotNull ItemStack stack, Level world, LivingEntity living) {
		if (!world.isClientSide()) {
			for (MobEffectInstance effect : getBrew(stack).getPotionEffects(stack)) {
				MobEffectInstance newEffect = new MobEffectInstance(effect.getEffect(), effect.getDuration(), effect.getAmplifier(), true, true);
				if (effect.getEffect().value().isInstantenous()) {
					effect.getEffect().value().applyInstantenousEffect((ServerLevel) world, living, living, living, newEffect.getAmplifier(), 1.0);
				} else {
					living.addEffect(newEffect);
				}
			}

			if (world.getRandom().nextBoolean()) {
				world.playSound(null, living.getX(), living.getY(), living.getZ(), SoundEvents.PLAYER_BURP, SoundSource.PLAYERS, 1F, 1F);
			}

			int swigs = getSwigsLeft(stack);
			if (living instanceof Player player && !player.getAbilities().instabuild) {
				if (swigs == 1) {
					ItemStack result = getBaseStack();
					if (!player.getInventory().add(result)) {
						return result;
					} else {
						return ItemStack.EMPTY;
					}
				}

				setSwigsLeft(stack, swigs - 1);
			}
		}

		return stack;
	}

	@Override
	public void addToCreativeTab(Item me, CreativeModeTab.Output output) {
		for (Brew brew : BotaniaAPI.instance().getBrewRegistry()) {
			if (brew == BotaniaBrews.fallbackBrew) {
				continue;
			}
			ItemStack stack = new ItemStack(this);
			setBrew(stack, brew);
			output.accept(stack);

		}
	}

	@NotNull
	@Override
	public Component getName(@NotNull ItemStack stack) {
		return Component.translatable(getDescriptionId(), Component.translatable(getBrew(stack).getTranslationKey(stack)),
				Component.literal(Integer.toString(getSwigsLeft(stack))).withStyle(ChatFormatting.BOLD));
	}

	// [VanillaCopy] PotionContents.addPotionTooltip, with custom effect list
	public static void addPotionTooltip(List<MobEffectInstance> list, List<Component> lores, float durationFactor) {
		List<Pair<Holder<Attribute>, AttributeModifier>> list1 = Lists.newArrayList();
		if (list.isEmpty()) {
			lores.add((Component.translatable("effect.none")).withStyle(ChatFormatting.GRAY));
		} else {
			for (MobEffectInstance effectinstance : list) {
				MutableComponent iformattabletextcomponent = Component.translatable(effectinstance.getDescriptionId());
				MobEffect effect = effectinstance.getEffect().value();
				effect.createModifiers(effectinstance.getAmplifier(), (attrHolder, mod) -> {
					list1.add(new Pair<>(attrHolder, mod));
				});

				if (effectinstance.getAmplifier() > 0) {
					iformattabletextcomponent = Component.translatable("potion.withAmplifier", iformattabletextcomponent, Component.translatable("potion.potency." + effectinstance.getAmplifier()));
				}

				if (effectinstance.getDuration() > 20) {
					iformattabletextcomponent = Component.translatable("potion.withDuration", iformattabletextcomponent, MobEffectUtil.formatDuration(effectinstance, durationFactor, 20.0F));
				}

				lores.add(iformattabletextcomponent.withStyle(effect.getCategory().getTooltipFormatting()));
			}
		}

		if (!list1.isEmpty()) {
			lores.add(Component.empty());
			lores.add((Component.translatable("potion.whenDrank")).withStyle(ChatFormatting.DARK_PURPLE));

			for (Pair<Holder<Attribute>, AttributeModifier> pair : list1) {
				AttributeModifier attributemodifier2 = pair.getSecond();
				double d0 = attributemodifier2.amount();
				double d1;
				if (attributemodifier2.operation() != AttributeModifier.Operation.ADD_MULTIPLIED_BASE && attributemodifier2.operation() != AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL) {
					d1 = attributemodifier2.amount();
				} else {
					d1 = attributemodifier2.amount() * 100.0D;
				}

				if (d0 > 0.0D) {
					lores.add((Component.translatable("attribute.modifier.plus." + attributemodifier2.operation().id(), ItemAttributeModifiers.ATTRIBUTE_MODIFIER_FORMAT.format(d1), Component.translatable(pair.getFirst().value().getDescriptionId()))).withStyle(ChatFormatting.BLUE));
				} else if (d0 < 0.0D) {
					d1 = d1 * -1.0D;
					lores.add((Component.translatable("attribute.modifier.take." + attributemodifier2.operation().id(), ItemAttributeModifiers.ATTRIBUTE_MODIFIER_FORMAT.format(d1), Component.translatable(pair.getFirst().value().getDescriptionId()))).withStyle(ChatFormatting.RED));
				}
			}
		}
	}

	@Override
	public void appendHoverText(ItemStack stack, Item.TooltipContext context, TooltipDisplay display, Consumer<Component> builder, TooltipFlag flags) {
		List<Component> components = new java.util.ArrayList<>();
		addPotionTooltip(getBrew(stack).getPotionEffects(stack), components, 1);
		components.forEach(builder);
	}

	@Override
	public Brew getBrew(ItemStack stack) {
		String key = ItemNBTHelper.getString(stack, TAG_BREW_KEY, "");
		return BotaniaAPI.instance().getBrewRegistry().get(Identifier.tryParse(key))
				.map(Holder.Reference::value)
				.orElse(BotaniaBrews.fallbackBrew);
	}

	public static void setBrew(ItemStack stack, @Nullable Brew brew) {
		Identifier id;
		if (brew != null) {
			id = BotaniaAPI.instance().getBrewRegistry().getKey(brew);
		} else {
			id = prefix("fallback");
		}
		setBrew(stack, id);
	}

	public static void setBrew(ItemStack stack, Identifier brew) {
		ItemNBTHelper.setString(stack, TAG_BREW_KEY, brew.toString());
	}

	@NotNull
	public static String getSubtype(ItemStack stack) {
		return ItemNBTHelper.verifyExistance(stack, TAG_KEEP) ? ItemNBTHelper.getString(stack, TAG_BREW_KEY, "none") : "none";
	}

	public int getSwigs() {
		return swigs;
	}

	public int getSwigsLeft(ItemStack stack) {
		return ItemNBTHelper.getInt(stack, TAG_SWIGS_LEFT, swigs);
	}

	public void setSwigsLeft(ItemStack stack, int swigs) {
		ItemNBTHelper.setInt(stack, TAG_SWIGS_LEFT, swigs);
	}

	public ItemStack getBaseStack() {
		return new ItemStack(baseItem.get());
	}
}

