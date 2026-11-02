package vazkii.botania.fabric.mixin;

import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import vazkii.botania.common.item.BotaniaItems;

@Mixin(Enchantment.class)
public class EnchantmentFabricMixin {
	// 26.1: the enchanting table no longer calls canEnchant, it checks isSupportedItem.
	@Inject(method = "isSupportedItem", cancellable = true, at = @At("HEAD"))
	public void onEnchant(ItemStack stack, CallbackInfoReturnable<Boolean> cir) {
		Enchantment self = (Enchantment) (Object) this;
		if (stack.is(BotaniaItems.elementiumAxe) && isMobLooting(self)) {
			cir.setReturnValue(true);
		}
	}

	// 26.1: enchantments are data-driven, so Enchantment does not carry its registry key.
	// Identify mob looting by its description translation key instead.
	private static boolean isMobLooting(Enchantment enchantment) {
		return enchantment.description().getContents() instanceof TranslatableContents tc
				&& tc.getKey().equals("enchantment.minecraft.looting");
	}
}
