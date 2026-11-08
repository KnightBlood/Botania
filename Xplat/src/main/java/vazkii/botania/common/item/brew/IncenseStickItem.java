/*
 * This class is distributed as part of the Botania Mod.
 * Get the Source Code in github:
 * https://github.com/Vazkii/Botania
 *
 * Botania is Open Source and distributed under the
 * Botania License: http://botaniamod.net/license.php
 */
package vazkii.botania.common.item.brew;

import net.minecraft.ChatFormatting;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;

import vazkii.botania.api.BotaniaAPI;
import vazkii.botania.api.brew.Brew;
import vazkii.botania.api.brew.BrewContainer;
import vazkii.botania.api.brew.BrewItem;
import vazkii.botania.common.brew.BotaniaBrews;
import vazkii.botania.common.helper.ItemNBTHelper;
import vazkii.botania.common.item.CustomCreativeTabContents;

import java.util.List;
import java.util.function.Consumer;

public class IncenseStickItem extends Item implements BrewItem, BrewContainer, CustomCreativeTabContents {

	private static final String TAG_BREW_KEY = "brewKey";
	public static final int TIME_MULTIPLIER = 60;

	public IncenseStickItem(Properties builder) {
		super(builder);
	}

	@Override
	public void addToCreativeTab(Item me, CreativeModeTab.Output output) {
		output.accept(this);
		for (Brew brew : BotaniaAPI.instance().getBrewRegistry()) {
			ItemStack brewStack = getItemForBrew(brew, new ItemStack(this));
			if (!brewStack.isEmpty()) {
				output.accept(brewStack);
			}
		}
	}

	@Override
	public void appendHoverText(ItemStack stack, Item.TooltipContext context, TooltipDisplay display, Consumer<Component> builder, TooltipFlag flags) {
		Brew brew = getBrew(stack);
		if (brew == BotaniaBrews.fallbackBrew) {
			builder.accept(Component.translatable("botaniamisc.notInfused").withStyle(ChatFormatting.LIGHT_PURPLE));
			return;
		}

		builder.accept(Component.translatable("botaniamisc.brewOf", Component.translatable(brew.getTranslationKey(stack))).withStyle(ChatFormatting.LIGHT_PURPLE));
		List<Component> components = new java.util.ArrayList<>();
		BaseBrewItem.addPotionTooltip(brew.getPotionEffects(stack), components, TIME_MULTIPLIER);
		components.forEach(builder);
	}

	@Override
	public Brew getBrew(ItemStack stack) {
		String key = ItemNBTHelper.getString(stack, TAG_BREW_KEY, "");
		return BotaniaAPI.instance().getBrewRegistry().get(Identifier.tryParse(key))
				.map(Holder.Reference::value)
				.orElse(BotaniaBrews.fallbackBrew);
	}

	public static void setBrew(ItemStack stack, Brew brew) {
		setBrew(stack, BotaniaAPI.instance().getBrewRegistry().getKey(brew));
	}

	public static void setBrew(ItemStack stack, Identifier brew) {
		ItemNBTHelper.setString(stack, TAG_BREW_KEY, brew.toString());
	}

	@Override
	public ItemStack getItemForBrew(Brew brew, ItemStack stack) {
		if (!brew.canInfuseIncense() || brew.getPotionEffects(stack).size() != 1 || brew.getPotionEffects(stack).get(0).getEffect().value().isInstantenous()) {
			return ItemStack.EMPTY;
		}

		ItemStack brewStack = new ItemStack(this);
		setBrew(brewStack, brew);
		return brewStack;
	}

	@Override
	public int getManaCost(Brew brew, ItemStack stack) {
		return brew.getManaCost() * 10;
	}
}
