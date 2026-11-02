/*
 * This class is distributed as part of the Botania Mod.
 * Get the Source Code in github:
 * https://github.com/Vazkii/Botania
 *
 * Botania is Open Source and distributed under the
 * Botania License: http://botaniamod.net/license.php
 */
package vazkii.botania.common.item;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.SlotAccess;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ClickAction;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.server.level.ServerLevel;
import org.jetbrains.annotations.Nullable;

import org.jetbrains.annotations.NotNull;

import vazkii.botania.client.lib.ResourcesLib;
import vazkii.botania.common.handler.BotaniaSounds;
import vazkii.botania.common.helper.ItemNBTHelper;

import java.util.List;
import java.util.function.Consumer;
import net.minecraft.world.item.component.TooltipDisplay;

public class ManufactoryHaloItem extends AssemblyHaloItem {
	public static final String TAG_ACTIVE = "active";

	private static final Identifier glowTexture = Identifier.parse(ResourcesLib.MISC_GLOW_CYAN);

	public ManufactoryHaloItem(Item.Properties props) {
		super(props);
	}

	@Override
	public void inventoryTick(ItemStack stack, ServerLevel world, Entity entity, @Nullable EquipmentSlot slot) {
		super.inventoryTick(stack, world, entity, slot);

		if (!world.isClientSide() && entity instanceof Player player && slot == null && isActive(stack)) {

			for (int i = 1; i < SEGMENTS; i++) {
				tryCraft(player, stack, i, false);
			}
		}
	}

	@Override
	public Identifier getGlowResource(ItemStack stack) {
		return isActive(stack) ? glowTexture : super.getGlowResource(stack);
	}

	@Override
	public void appendHoverText(@NotNull ItemStack stack, Item.TooltipContext context, TooltipDisplay display, @NotNull Consumer<Component> builder, @NotNull TooltipFlag flags) {
		if (isActive(stack)) {
			builder.accept(Component.translatable("botaniamisc.active"));
		} else {
			builder.accept(Component.translatable("botaniamisc.inactive"));
		}
	}

	@NotNull
	@Override
	public InteractionResult use(Level world, Player player, @NotNull InteractionHand hand) {
		ItemStack stack = player.getItemInHand(hand);
		if (getSegmentLookedAt(stack, player) == 0 && player.isSecondaryUseActive()) {
			togglePassive(stack, player, world);
			return world.isClientSide() ? InteractionResult.SUCCESS.heldItemTransformedTo(stack) : InteractionResult.SUCCESS_SERVER.heldItemTransformedTo(stack);
		}

		return super.use(world, player, hand);
	}

	@Override
	public boolean overrideOtherStackedOnMe(@NotNull ItemStack stack, @NotNull ItemStack cursor, @NotNull Slot slot,
			@NotNull ClickAction click, Player player, @NotNull SlotAccess access) {
		Level world = player.level();
		if (click == ClickAction.SECONDARY && slot.allowModification(player) && cursor.isEmpty()) {
			togglePassive(stack, player, world);
			access.set(cursor);
			return true;
		}
		return false;
	}

	private void togglePassive(ItemStack stack, LivingEntity living, Level world) {
		ItemNBTHelper.setBoolean(stack, TAG_ACTIVE, !isActive(stack));
		if (living instanceof Player player && world != null) {
			world.playSound(player, player.getX(), player.getY(), player.getZ(), BotaniaSounds.manufactoryHaloConfigure, SoundSource.NEUTRAL, 1F, 1F);
		}
	}

	private static boolean isActive(ItemStack stack) {
		return ItemNBTHelper.getBoolean(stack, TAG_ACTIVE, true);
	}
}

