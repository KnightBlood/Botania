/*
 * This class is distributed as part of the Botania Mod.
 * Get the Source Code in github:
 * https://github.com/Vazkii/Botania
 *
 * Botania is Open Source and distributed under the
 * Botania License: http://botaniamod.net/license.php
 */
package vazkii.botania.mixin;

import net.minecraft.core.component.DataComponentInitializers;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStackTemplate;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import vazkii.botania.common.item.material.SelfReturningItem;
import vazkii.botania.common.lib.RegistrationBackfill;

@Mixin(Item.class)
public class ItemMixin {
	// Marker passed to DataComponentInitializers.add when construction happens without an id
	// (queued for later backfill instead); replaced by the real initializer during registration.
	private static final DataComponentInitializers.Initializer<Item> NO_OP_INIT = (builder, provider, key) -> {
	};

	@Inject(at = @At("HEAD"), method = "getCraftingRemainder", cancellable = true)
	private void returnSelf(CallbackInfoReturnable<ItemStackTemplate> cir) {
		Item self = (Item) (Object) this;
		if (self instanceof SelfReturningItem) {
			// 26.1: renamed from getCraftingRemainingItem and now returns an ItemStackTemplate
			cir.setReturnValue(new ItemStackTemplate(self));
		}
	}

	// 26.1: Item construction requires Properties.id to be set (for descriptionId, model and
	// DataComponentInitializers). Botania constructs its items as static fields before the ids
	// are known, so during construction we substitute placeholders and queue the item for
	// backfill in RegistrationBackfill once the registration id is available.
	@Redirect(method = "<init>", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/Item$Properties;effectiveDescriptionId()Ljava/lang/String;"))
	private String botania$deferDescriptionId(Item.Properties props) {
		if (RegistrationBackfill.isDeferredItem(props)) {
			return "item.pending";
		}
		return ((ItemPropertiesAccessor) (Object) props).botania$effectiveDescriptionId();
	}

	@Redirect(method = "<init>", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/Item$Properties;effectiveModel()Lnet/minecraft/resources/Identifier;"))
	private Identifier botania$deferModel(Item.Properties props) {
		if (RegistrationBackfill.isDeferredItem(props)) {
			return null;
		}
		return ((ItemPropertiesAccessor) (Object) props).botania$effectiveModel();
	}

	@Redirect(method = "<init>", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/Item$Properties;finalizeInitializer(Lnet/minecraft/network/chat/Component;Lnet/minecraft/resources/Identifier;)Lnet/minecraft/core/component/DataComponentInitializers$Initializer;"))
	private DataComponentInitializers.Initializer<Item> botania$deferComponentInit(Item.Properties props, Component name, Identifier model) {
		if (RegistrationBackfill.isDeferredItem(props)) {
			RegistrationBackfill.queuePendingItem((Item) (Object) this, props, name);
			return NO_OP_INIT;
		}
		return ((ItemPropertiesAccessor) (Object) props).botania$finalizeInitializer(name, model);
	}

	@Redirect(method = "<init>", at = @At(value = "INVOKE", target = "Lnet/minecraft/core/component/DataComponentInitializers;add(Lnet/minecraft/resources/ResourceKey;Lnet/minecraft/core/component/DataComponentInitializers$Initializer;)V"))
	private void botania$skipPendingAdd(DataComponentInitializers registry, ResourceKey<Item> key, DataComponentInitializers.Initializer<Item> init) {
		if (init != NO_OP_INIT) {
			registry.add(key, init);
		}
	}

	@Redirect(method = "<init>", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/Item$Properties;itemIdOrThrow()Lnet/minecraft/resources/ResourceKey;"))
	private ResourceKey<Item> botania$deferKey(Item.Properties props) {
		if (RegistrationBackfill.isDeferredItem(props)) {
			// placeholder key; the add call is skipped for pending items anyway
			return RegistrationBackfill.PENDING_ITEM_KEY;
		}
		return ((ItemPropertiesAccessor) (Object) props).botania$getId();
	}
}
