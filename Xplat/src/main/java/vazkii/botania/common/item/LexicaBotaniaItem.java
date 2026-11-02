/*
 * This class is distributed as part of the Botania Mod.
 * Get the Source Code in github:
 * https://github.com/Vazkii/Botania
 *
 * Botania is Open Source and distributed under the
 * Botania License: http://botaniamod.net/license.php
 */
package vazkii.botania.common.item;

import net.minecraft.ChatFormatting;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import com.google.gson.JsonParser;
import com.mojang.serialization.JsonOps;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.network.chat.Style.Serializer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.TagKey;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;

import java.util.function.Consumer;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;
import net.minecraft.world.level.block.entity.BannerPattern;
import net.minecraft.world.phys.BlockHitResult;

import org.jetbrains.annotations.NotNull;

import vazkii.botania.common.advancements.UseItemSuccessTrigger;
import vazkii.botania.common.handler.BotaniaSounds;
import vazkii.botania.common.helper.ItemNBTHelper;
import vazkii.botania.common.lib.BotaniaTags;
import vazkii.patchouli.api.PatchouliAPI;

import java.util.List;

public class LexicaBotaniaItem extends Item implements ItemWithBannerPattern, CustomCreativeTabContents {

	public static final String TAG_ELVEN_UNLOCK = "botania:elven_unlock";

	public LexicaBotaniaItem(Properties settings) {
		super(settings);
	}

	public static boolean isOpen() {
		return BuiltInRegistries.ITEM.getKey(BotaniaItems.lexicon).equals(PatchouliAPI.get().getOpenBookGui());
	}

	@Override
	public void addToCreativeTab(Item me, CreativeModeTab.Output output) {
		output.accept(this);
		ItemStack creative = new ItemStack(this);
		ItemNBTHelper.setBoolean(creative, TAG_ELVEN_UNLOCK, true);
		output.accept(creative);
	}

	@Override
	public void appendHoverText(ItemStack stack, Item.TooltipContext context, TooltipDisplay display, Consumer<Component> builder, TooltipFlag flags) {
		builder.accept(getEdition().copy().withStyle(ChatFormatting.GRAY));
	}

	@NotNull
	@Override
	public InteractionResult use(Level worldIn, Player playerIn, InteractionHand handIn) {
		ItemStack stack = playerIn.getItemInHand(handIn);

		if (playerIn instanceof ServerPlayer player) {
			UseItemSuccessTrigger.INSTANCE.trigger(player, stack, player.level(), player.getX(), player.getY(), player.getZ());
			PatchouliAPI.get().openBookGUI(player, BuiltInRegistries.ITEM.getKey(this));
			playerIn.playSound(BotaniaSounds.lexiconOpen, 1F, (float) (0.7 + Math.random() * 0.4));
		}

		return worldIn.isClientSide() ? InteractionResult.SUCCESS.heldItemTransformedTo(stack) : InteractionResult.SUCCESS_SERVER.heldItemTransformedTo(stack);
	}

	public static Component getEdition() {
		try {
			return PatchouliAPI.get().getSubtitle(BuiltInRegistries.ITEM.getKey(BotaniaItems.lexicon));
		} catch (IllegalArgumentException e) {
			return Component.literal(""); // TODO Adjust Patchouli because first search tree creation is too early to get the edition
		}
	}

	public static Component getTitle(ItemStack stack) {
		Component title = stack.getHoverName();

		// Akashic tome tag contains a `text` field, which is a stringified text component
		String akashicTomeNBT = "akashictome:displayName";
		if (ItemNBTHelper.verifyExistance(stack, akashicTomeNBT)) {
			CompoundTag nameTextComponent = ItemNBTHelper.getCompound(stack, akashicTomeNBT, false);
			title = ComponentSerialization.CODEC.parse(JsonOps.INSTANCE, JsonParser.parseString(nameTextComponent.getStringOr("text", ""))).result().orElse(title);
		}

		return title;
	}

	public static boolean isElven(ItemStack stack) {
		return ItemNBTHelper.getBoolean(stack, TAG_ELVEN_UNLOCK, false);
	}

	// Random item to expose this as public
	public static BlockHitResult doRayTrace(Level world, Player player, ClipContext.Fluid fluidMode) {
		return Item.getPlayerPOVHitResult(world, player, fluidMode);
	}

	@Override
	public TagKey<BannerPattern> getBannerPattern() {
		return BotaniaTags.BannerPatterns.PATTERN_ITEM_LEXICON;
	}
}

