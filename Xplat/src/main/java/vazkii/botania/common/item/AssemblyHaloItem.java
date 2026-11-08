/*
 * This class is distributed as part of the Botania Mod.
 * Get the Source Code in github:
 * https://github.com/Vazkii/Botania
 *
 * Botania is Open Source and distributed under the
 * Botania License: http://botaniamod.net/license.php
 */
package vazkii.botania.common.item;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;

import net.minecraft.ChatFormatting;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderBuffers;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.NonNullList;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.recipebook.ServerPlaceRecipe;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.*;
import net.minecraft.world.Container;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityEquipment;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.player.StackedItemContents;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.inventory.CraftingMenu;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.ShapedRecipe;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;

import vazkii.botania.client.core.handler.ClientTickHandler;
import vazkii.botania.client.core.helper.RenderHelper;
import vazkii.botania.client.gui.crafting.AssemblyHaloContainer;
import vazkii.botania.client.lib.ResourcesLib;
import vazkii.botania.common.annotations.SoftImplement;
import vazkii.botania.common.helper.ItemNBTHelper;
import vazkii.botania.common.helper.PlayerHelper;
import vazkii.botania.common.helper.RecipeHelper;
import vazkii.botania.common.helper.VecHelper;
import vazkii.botania.network.EffectType;
import vazkii.botania.network.clientbound.BotaniaEffectPacket;
import vazkii.botania.xplat.XplatAbstractions;

import java.util.List;

public class AssemblyHaloItem extends Item {

	private static final Identifier glowTexture = Identifier.parse(ResourcesLib.MISC_GLOW_GREEN);
	private static ItemStack craftingTable;

	// 26.1: creating ItemStacks requires the item registry's components to be bound, which
	// happens after registration - so build this lazily instead of at class-init time.
	private static ItemStack craftingTable() {
		if (craftingTable == null) {
			craftingTable = new ItemStack(Blocks.CRAFTING_TABLE);
		}
		return craftingTable();
	}

	public static final int SEGMENTS = 12;

	private static final String TAG_LAST_CRAFTING = "lastCrafting";
	private static final String TAG_STORED_RECIPE_PREFIX = "storedRecipe";
	private static final String TAG_EQUIPPED = "equipped";
	private static final String TAG_ROTATION_BASE = "rotationBase";

	public AssemblyHaloItem(Properties props) {
		super(props);
	}

	@NotNull
	@Override
	public InteractionResult use(Level world, Player player, @NotNull InteractionHand hand) {
		ItemStack stack = player.getItemInHand(hand);
		if (!world.isClientSide()) {
			int segment = getSegmentLookedAt(stack, player);

			if (segment == 0) {
				// Pos is never used by workbench, so use origin.
				// But this cannot be the static dummy one in the interface, we have to pass an actual one for the
				// crafting matrix to update properly.
				ContainerLevelAccess wp = ContainerLevelAccess.create(world, BlockPos.ZERO);
				player.openMenu(new SimpleMenuProvider(
						(windowId, playerInv, p) -> new AssemblyHaloContainer(windowId, playerInv, wp),
						stack.getHoverName()));
			} else {
				if (getSavedRecipe(world, stack, segment) == null) {
					RecipeHolder<CraftingRecipe> lastRecipe = getLastRecipe(world, stack);
					if (lastRecipe != null) {
						saveRecipe(stack, lastRecipe.id().identifier(), segment);
					}
				} else {
					tryCraft(player, stack, segment, true);
				}
			}
		}

		return world.isClientSide() ? InteractionResult.SUCCESS.heldItemTransformedTo(stack) : InteractionResult.SUCCESS_SERVER.heldItemTransformedTo(stack);
	}

	@Override
	public void inventoryTick(ItemStack stack, ServerLevel world, Entity entity, @Nullable EquipmentSlot slot) {
		if (!(entity instanceof LivingEntity living)) {
			return;
		}

		boolean eqLastTick = wasEquipped(stack);
		boolean equipped = slot != null;

		if (!equipped && living.getOffhandItem() == stack) {
			equipped = true;
		}

		if (eqLastTick != equipped) {
			setEquipped(stack, equipped);
		}

		if (!equipped) {
			int angles = 360;
			int segAngles = angles / SEGMENTS;
			float shift = segAngles / 2.0F;
			setRotationBase(stack, getCheckingAngle(living) - shift);
		}
	}

	private static boolean hasRoomFor(Inventory inv, ItemStack stack) {
		// 26.1: `items` is private and the copy constructor now takes an EntityEquipment,
		// so build the dummy inventory from the public accessors instead.
		Inventory dummy = new Inventory(inv.player, new EntityEquipment());
		for (int i = 0; i < inv.getNonEquipmentItems().size(); i++) {
			dummy.getNonEquipmentItems().set(i, inv.getNonEquipmentItems().get(i).copy());
		}
		// warning: must be careful to not cause side effects / dupes with dummy here
		return dummy.add(stack.copy());
	}

	private static boolean canCraftHeuristic(Player player, Recipe<?> recipe) {
		StackedItemContents accounter = new StackedItemContents();
		player.getInventory().fillStackedContents(accounter);
		return accounter.canCraft(recipe, null);
	}

	void tryCraft(Player player, ItemStack halo, int slot, boolean particles) {
		RecipeHolder<CraftingRecipe> holder = getSavedRecipe(player.level(), halo, slot);
		if (holder == null || !(player instanceof ServerPlayer serverPlayer)) {
			return;
		}
		CraftingRecipe recipe = holder.value();

		CraftingMenu dummy = new CraftingMenu(-999, player.getInventory());
		CraftingContainer craftInv = (CraftingContainer) dummy.getInputGridSlots().getFirst().container;

		StackedItemContents stackedContents = new StackedItemContents();
		player.getInventory().fillStackedContents(stackedContents);
		dummy.fillCraftSlotsStackedContents(stackedContents);

		if (!stackedContents.canCraft(recipe, null)) {
			return;
		}

		// Try placing the recipe into the dummy workbench, extracting items from player's inventory to do so
		// [VanillaCopy] 26.1: ServerPlaceRecipe is final-ish (private members), so drive its static entry
		// point through a CraftingMenuAccess around the dummy menu.
		var menuAccess = new ServerPlaceRecipe.CraftingMenuAccess<CraftingRecipe>() {
			@Override
			public void fillCraftSlotsStackedContents(StackedItemContents contents) {
				dummy.fillCraftSlotsStackedContents(contents);
			}

			@Override
			public void clearCraftingContent() {
				craftInv.clearContent();
			}

			@Override
			public boolean recipeMatches(RecipeHolder<CraftingRecipe> holder) {
				return holder.value().matches(craftInv.asCraftInput(), player.level());
			}
		};
		ServerPlaceRecipe.placeRecipe(menuAccess, dummy.getGridWidth(), dummy.getGridHeight(),
				dummy.getInputGridSlots(), dummy.getInputGridSlots(), player.getInventory(),
				holder, false, true);

		// Double check that the recipe matches
		if (!recipe.matches(craftInv.asCraftInput(), player.level())) {
			// If the placer worked but the recipe still didn't, this might be a dynamic recipe with special conditions.
			// Return items to the inventory and bail.
			returnGrid(craftInv, player);
			return;
		}

		ItemStack result = recipe.assemble(craftInv.asCraftInput());

		// Check if we have room for the result
		if (!hasRoomFor(player.getInventory(), result)) {
			returnGrid(craftInv, player);
			return;
		}

		// Now we are good to go. Give the result
		player.getInventory().add(result);

		// Give or toss all byproducts
		NonNullList<ItemStack> remainingItems = recipe.getRemainingItems(craftInv.asCraftInput());
		remainingItems.forEach(s -> player.getInventory().placeItemBackInInventory(s));

		// The items we consumed will stay in the dummy workbench and get deleted

		if (particles) {
			XplatAbstractions.INSTANCE.sendToTracking(serverPlayer, new BotaniaEffectPacket(EffectType.HALO_CRAFT,
					player.getX(), player.getY(), player.getZ(), player.getId()));
		}
	}

	private static void returnGrid(CraftingContainer craftInv, Player player) {
		for (int i = 0; i < craftInv.getContainerSize(); i++) {
			ItemStack stack = craftInv.removeItemNoUpdate(i);
			if (!stack.isEmpty()) {
				player.getInventory().placeItemBackInInventory(stack);
			}
		}
	}

	@SoftImplement("IForgeItem")
	public boolean onEntitySwing(ItemStack stack, LivingEntity living) {
		int segment = getSegmentLookedAt(stack, living);
		if (segment == 0) {
			return false;
		}

		RecipeHolder<?> recipe = getSavedRecipe(living.level(), stack, segment);
		if (recipe != null && living.isShiftKeyDown()) {
			saveRecipe(stack, null, segment);
			return true;
		}

		return false;
	}

	protected static int getSegmentLookedAt(ItemStack stack, LivingEntity living) {
		float yaw = getCheckingAngle(living, getRotationBase(stack));

		int angles = 360;
		int segAngles = angles / SEGMENTS;
		for (int seg = 0; seg < SEGMENTS; seg++) {
			float calcAngle = (float) seg * segAngles;
			if (yaw >= calcAngle && yaw < calcAngle + segAngles) {
				return seg;
			}
		}
		return -1;
	}

	private static float getCheckingAngle(LivingEntity living) {
		return getCheckingAngle(living, 0F);
	}

	// Screw the way minecraft handles rotation
	// Really...
	private static float getCheckingAngle(LivingEntity living, float base) {
		float yaw = Mth.wrapDegrees(living.getYRot()) + 90F;
		int angles = 360;
		int segAngles = angles / SEGMENTS;
		float shift = segAngles / 2;

		if (yaw < 0) {
			yaw = 180F + (180F + yaw);
		}
		yaw -= 360F - base;
		float angle = 360F - yaw + shift;

		if (angle < 0) {
			angle = 360F + angle;
		}

		return angle;
	}

	@Nullable
	private static RecipeHolder<CraftingRecipe> getSavedRecipe(Level world, ItemStack halo, int position) {
		String savedId = ItemNBTHelper.getString(halo, TAG_STORED_RECIPE_PREFIX + position, "");
		if (position <= 0 || position >= SEGMENTS || savedId.isEmpty()) {
			return null;
		}
		return getRecipeById(world, Identifier.tryParse(savedId));
	}

	@SuppressWarnings("unchecked")
	@Nullable
	private static RecipeHolder<CraftingRecipe> getRecipeById(Level world, @Nullable Identifier id) {
		if (id == null) {
			return null;
		}
		// 26.1: recipes live in a data-driven registry keyed by ResourceKey, so resolve the
		// saved id through the RecipeManager instead of Recipe#getId().
		RecipeManager manager = RecipeHelper.getRecipeManager(world);
		if (manager == null) {
			return null;
		}
		ResourceKey<Recipe<?>> key = ResourceKey.create(Registries.RECIPE, id);
		return manager.byKey(key)
				.filter(h -> h.value() instanceof CraftingRecipe)
				.map(h -> (RecipeHolder<CraftingRecipe>) h)
				.orElse(null);
	}

	private static void saveRecipe(ItemStack halo, @Nullable Identifier id, int position) {
		if (id == null) {
			ItemNBTHelper.removeEntry(halo, TAG_STORED_RECIPE_PREFIX + position);
		} else {
			ItemNBTHelper.setString(halo, TAG_STORED_RECIPE_PREFIX + position, id.toString());
		}
	}

	private static ItemStack getDisplayItem(Level world, ItemStack stack, int position) {
		if (position == 0) {
			return craftingTable();
		} else if (position >= SEGMENTS) {
			return ItemStack.EMPTY;
		} else {
			RecipeHolder<CraftingRecipe> recipe = getSavedRecipe(world, stack, position);
			if (recipe != null) {
				return RecipeHelper.getResultItem(recipe.value(), world);
			} else {
				return ItemStack.EMPTY;
			}
		}
	}

	public static void onItemCrafted(Player player, Container inv) {
		AbstractContainerMenu container = player.containerMenu;

		if (!(container instanceof AssemblyHaloContainer) ||
				!(inv instanceof CraftingContainer cc)) {
			return;
		}

		RecipeHelper.getRecipeManager(player.level()).getRecipeFor(RecipeType.CRAFTING, cc.asCraftInput(), player.level())
				.ifPresent(holder -> {
					for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
						ItemStack stack = player.getInventory().getItem(i);
						if (!stack.isEmpty() && stack.getItem() instanceof AssemblyHaloItem) {
							rememberLastRecipe(holder.id().identifier(), stack);
						}
					}
				});
	}

	private static void rememberLastRecipe(Identifier recipeId, ItemStack halo) {
		ItemNBTHelper.setString(halo, TAG_LAST_CRAFTING, recipeId.toString());
	}

	@Nullable
	private static RecipeHolder<CraftingRecipe> getLastRecipe(Level world, ItemStack halo) {
		String savedId = ItemNBTHelper.getString(halo, TAG_LAST_CRAFTING, "");
		return savedId.isEmpty() ? null : getRecipeById(world, Identifier.tryParse(savedId));
	}

	private static boolean wasEquipped(ItemStack stack) {
		return ItemNBTHelper.getBoolean(stack, TAG_EQUIPPED, false);
	}

	private static void setEquipped(ItemStack stack, boolean equipped) {
		ItemNBTHelper.setBoolean(stack, TAG_EQUIPPED, equipped);
	}

	private static float getRotationBase(ItemStack stack) {
		return ItemNBTHelper.getFloat(stack, TAG_ROTATION_BASE, 0F);
	}

	private static void setRotationBase(ItemStack stack, float rotation) {
		ItemNBTHelper.setFloat(stack, TAG_ROTATION_BASE, rotation);
	}

	public Identifier getGlowResource(ItemStack stack) {
		return glowTexture;
	}

	public static class Rendering {
		public static void onRenderWorldLast(Camera camera, float partialTicks, PoseStack ms, RenderBuffers buffers) {
			Player player = Minecraft.getInstance().player;
			ItemStack stack = PlayerHelper.getFirstHeldItemClass(player, AssemblyHaloItem.class);
			if (stack.isEmpty()) {
				return;
			}

			MultiBufferSource.BufferSource bufferSource = buffers.bufferSource();

			double renderPosX = camera.position().x();
			double renderPosY = camera.position().y();
			double renderPosZ = camera.position().z();

			ms.pushPose();
			float alpha = ((float) Math.sin((ClientTickHandler.ticksInGame + partialTicks) * 0.2F) * 0.5F + 0.5F) * 0.4F + 0.3F;

			double posX = player.xo + (player.getX() - player.xo) * partialTicks;
			double posY = player.yo + (player.getY() - player.yo) * partialTicks + player.getEyeHeight();
			double posZ = player.zo + (player.getZ() - player.zo) * partialTicks;

			ms.translate(posX - renderPosX, posY - renderPosY, posZ - renderPosZ);

			float base = getRotationBase(stack);
			int angles = 360;
			int segAngles = angles / SEGMENTS;
			float shift = base - segAngles / 2.0F;

			float u = 1F;
			float v = 0.25F;

			float s = 3F;
			float m = 0.8F;
			float y = v * s * 2;
			float y0 = 0;

			int segmentLookedAt = getSegmentLookedAt(stack, player);
			AssemblyHaloItem item = (AssemblyHaloItem) stack.getItem();
			RenderType layer = RenderHelper.getHaloLayer(item.getGlowResource(stack));

			for (int seg = 0; seg < SEGMENTS; seg++) {
				boolean inside = false;
				float rotationAngle = (seg + 0.5F) * segAngles + shift;
				ms.pushPose();
				ms.mulPose(VecHelper.rotateY(rotationAngle));
				ms.translate(s * m, -0.75F, 0F);

				if (segmentLookedAt == seg) {
					inside = true;
				}

				ItemStack slotStack = getDisplayItem(player.level(), stack, seg);
				if (!slotStack.isEmpty()) {
					float scale = seg == 0 ? 0.9F : 0.8F;
					ms.scale(scale, scale, scale);
					ms.mulPose(VecHelper.rotateY(180F));
					ms.translate(seg == 0 ? 0.5F : 0F, seg == 0 ? -0.1F : 0.6F, 0F);

					ms.mulPose(VecHelper.rotateY(90.0F));
					RenderHelper.renderItemStatic(slotStack, ItemDisplayContext.GUI, false,
							ms, bufferSource, 0xF000F0, OverlayTexture.NO_OVERLAY, player.level());
				}
				ms.popPose();

				ms.pushPose();
				ms.mulPose(VecHelper.rotateX(180));
				float r = 1, g = 1, b = 1, a = alpha;
				if (inside) {
					a += 0.3F;
					y0 = -y;
				}

				if (seg % 2 == 0) {
					r = g = b = 0.6F;
				}

				VertexConsumer buffer = bufferSource.getBuffer(layer);
				for (int i = 0; i < segAngles; i++) {
					Matrix4f mat = ms.last().pose();
					float ang = i + seg * segAngles + shift;
					float xp = (float) Math.cos(ang * Math.PI / 180F) * s;
					float zp = (float) Math.sin(ang * Math.PI / 180F) * s;

					buffer.addVertex(mat, xp * m, y, zp * m).setColor(r, g, b, a).setUv(u, v);
					buffer.addVertex(mat, xp, y0, zp).setColor(r, g, b, a).setUv(u, 0);

					xp = (float) Math.cos((ang + 1) * Math.PI / 180F) * s;
					zp = (float) Math.sin((ang + 1) * Math.PI / 180F) * s;

					buffer.addVertex(mat, xp, y0, zp).setColor(r, g, b, a).setUv(0, 0);
					buffer.addVertex(mat, xp * m, y, zp * m).setColor(r, g, b, a).setUv(0, v);
				}
				y0 = 0;
				ms.popPose();
			}
			ms.popPose();
			bufferSource.endBatch();
		}

		public static void renderHUD(GuiGraphicsExtractor gui, Player player, ItemStack stack) {
			Minecraft mc = Minecraft.getInstance();
			int slot = getSegmentLookedAt(stack, player);

			if (slot == 0) {
				String name = craftingTable().getHoverName().getString();
				int l = mc.font.width(name);
				int x = mc.getWindow().getGuiScaledWidth() / 2 - l / 2;
				int y = mc.getWindow().getGuiScaledHeight() / 2 - 65;

				gui.fill(x - 6, y - 6, x + l + 6, y + 37, 0x22000000);
				gui.fill(x - 4, y - 4, x + l + 4, y + 35, 0x22000000);
				gui.item(craftingTable(), mc.getWindow().getGuiScaledWidth() / 2 - 8, mc.getWindow().getGuiScaledHeight() / 2 - 52);

				gui.text(mc.font, name, x, y, 0xFFFFFF);
			} else {
				RecipeHolder<CraftingRecipe> recipe = getSavedRecipe(player.level(), stack, slot);
				Component label;
				boolean setRecipe = false;

				if (recipe == null) {
					label = Component.translatable("botaniamisc.unsetRecipe");
					recipe = getLastRecipe(player.level(), stack);
				} else {
					label = RecipeHelper.getResultItem(recipe.value(), player.level()).getHoverName();
					setRecipe = true;
				}

				renderRecipe(gui, label, recipe == null ? null : recipe.value(), player, setRecipe);
			}
		}

		private static void renderRecipe(GuiGraphicsExtractor gui, Component label, @Nullable CraftingRecipe recipe, Player player, boolean isSavedRecipe) {
			Minecraft mc = Minecraft.getInstance();

			ItemStack recipeResult;
			if (recipe != null && !(recipeResult = RecipeHelper.getResultItem(recipe, player.level())).isEmpty()) {
				int x = mc.getWindow().getGuiScaledWidth() / 2 - 45;
				int y = mc.getWindow().getGuiScaledHeight() / 2 - 90;

				gui.fill(x - 6, y - 6, x + 90 + 6, y + 60, 0x22000000);
				gui.fill(x - 4, y - 4, x + 90 + 4, y + 58, 0x22000000);

				gui.fill(x + 66, y + 14, x + 92, y + 40, 0x22000000);
				gui.fill(x - 2, y - 2, x + 56, y + 56, 0x22000000);

				int wrap = recipe instanceof ShapedRecipe shaped ? shaped.getWidth() : 3;
				var ingredients = RecipeHelper.getCraftingIngredients(recipe);
				for (int i = 0; i < ingredients.size(); i++) {
					Ingredient ingr = ingredients.get(i);
					if (!ingr.isEmpty()) {
						List<Holder<Item>> items = ingr.items().toList();
						ItemStack stack = new ItemStack(items.get(ClientTickHandler.ticksInGame / 20 % items.size()));
						int xpos = x + i % wrap * 18;
						int ypos = y + i / wrap * 18;
						gui.fill(xpos, ypos, xpos + 16, ypos + 16, 0x22000000);

						gui.item(stack, xpos, ypos);
					}
				}

				gui.item(recipeResult, x + 72, y + 18);
				gui.itemDecorations(mc.font, recipeResult, x + 72, y + 18);

			}

			int yoff = 110;
			if (isSavedRecipe && recipe != null && !canCraftHeuristic(player, recipe)) {
				String warning = ChatFormatting.RED + I18n.get("botaniamisc.cantCraft");
				gui.centeredText(mc.font, warning, mc.getWindow().getGuiScaledWidth() / 2, mc.getWindow().getGuiScaledHeight() / 2 - yoff, 0xFFFFFF);
				yoff += 12;
			}

			gui.centeredText(mc.font, label, mc.getWindow().getGuiScaledWidth() / 2, mc.getWindow().getGuiScaledHeight() / 2 - yoff, 0xFFFFFF);
		}
	}
}
