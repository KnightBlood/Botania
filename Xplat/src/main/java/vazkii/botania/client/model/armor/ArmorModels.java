package vazkii.botania.client.model.armor;

import net.minecraft.client.Minecraft;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import org.jetbrains.annotations.Nullable;

import vazkii.botania.client.model.BotaniaModelLayers;
import vazkii.botania.common.item.equipment.armor.elementium.ElementiumArmorItem;
import vazkii.botania.common.item.equipment.armor.manasteel.ManasteelArmorItem;
import vazkii.botania.common.item.equipment.armor.manaweave.ManaweaveArmorItem;
import vazkii.botania.common.item.equipment.armor.terrasteel.TerrasteelArmorItem;

import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;

/**
 * Botania's 1.20.1 custom 3D armour geometry, ported to 26.1's model layer API.
 * <p>
 * The models are baked from the {@code 64x128} layer definitions in {@link BotaniaModelLayers}
 * and are submitted by {@code HumanoidArmorLayerMixin} together with the original
 * {@code textures/model/armor_*.png} atlases. They are deliberately <em>not</em> the vanilla
 * humanoid silhouette used by {@code Equippable.assetId()}: the raised pauldrons, helm crests
 * and crystal pieces only exist in this geometry.
 */
public class ArmorModels {
	private static Map<EquipmentSlot, ArmorModel> manasteel = Collections.emptyMap();
	private static Map<EquipmentSlot, ArmorModel> manaweave = Collections.emptyMap();
	private static Map<EquipmentSlot, ArmorModel> elementium = Collections.emptyMap();
	private static Map<EquipmentSlot, ArmorModel> terrasteel = Collections.emptyMap();
	private static boolean initialized = false;

	private static Map<EquipmentSlot, ArmorModel> make(EntityModelSet models, ModelLayerLocation inner, ModelLayerLocation outer) {
		Map<EquipmentSlot, ArmorModel> ret = new EnumMap<>(EquipmentSlot.class);
		for (var slot : EquipmentSlot.values()) {
			var mesh = models.bakeLayer(slot == EquipmentSlot.LEGS ? inner : outer);
			ret.put(slot, new ArmorModel(mesh, slot));
		}
		return ret;
	}

	public static void init(EntityModelSet models) {
		manasteel = make(models, BotaniaModelLayers.MANASTEEL_INNER_ARMOR, BotaniaModelLayers.MANASTEEL_OUTER_ARMOR);
		manaweave = make(models, BotaniaModelLayers.MANAWEAVE_INNER_ARMOR, BotaniaModelLayers.MANAWEAVE_OUTER_ARMOR);
		elementium = make(models, BotaniaModelLayers.ELEMENTIUM_INNER_ARMOR, BotaniaModelLayers.ELEMENTIUM_OUTER_ARMOR);
		terrasteel = make(models, BotaniaModelLayers.TERRASTEEL_INNER_ARMOR, BotaniaModelLayers.TERRASTEEL_OUTER_ARMOR);
		initialized = true;
	}

	/** Kept because {@code GaiaGuardianRenderer} is the one early client hook with a Context. */
	public static void init(EntityRendererProvider.Context ctx) {
		init(ctx.getModelSet());
	}

	/**
	 * Bakes the models on first use. Relying solely on {@code GaiaGuardianRenderer}'s constructor
	 * would be a silent degradation if that renderer were ever constructed after the first player
	 * render, so the render path also ensures initialization here. Idempotent.
	 */
	public static void ensureInitialized() {
		if (!initialized) {
			init(Minecraft.getInstance().getEntityModels());
		}
	}

	@Nullable
	public static ArmorModel get(ItemStack stack) {
		Item item = stack.getItem();
		if (item instanceof ManaweaveArmorItem) {
			return manaweave.get(getSlot(stack));
		} else if (item instanceof ElementiumArmorItem) {
			return elementium.get(getSlot(stack));
		} else if (item instanceof TerrasteelArmorItem) {
			return terrasteel.get(getSlot(stack));
		} else if (item instanceof ManasteelArmorItem) { // manasteel must be last because the other types extend from it
			return manasteel.get(getSlot(stack));
		}

		return null;
	}

	private static EquipmentSlot getSlot(ItemStack stack) {
		var equippable = stack.get(DataComponents.EQUIPPABLE);
		return equippable == null ? EquipmentSlot.MAINHAND : equippable.slot();
	}
}
