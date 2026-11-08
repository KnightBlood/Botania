/*
 * This class is distributed as part of the Botania Mod.
 * Get the Source Code in github:
 * https://github.com/Vazkii/Botania
 *
 * Botania is Open Source and distributed under the
 * Botania License: http://botaniamod.net/license.php
 */
package vazkii.botania.client.model;

import com.mojang.math.Transformation;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.client.data.models.model.ModelLocationUtils;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.block.dispatch.BlockModelRotation;
import net.minecraft.client.renderer.block.dispatch.ModelState;
import net.minecraft.client.renderer.item.ItemModel;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.item.ModelRenderProperties;
import net.minecraft.client.resources.model.ModelBaker;
import net.minecraft.client.resources.model.ResolvableModel;
import net.minecraft.client.resources.model.ResolvedModel;
import net.minecraft.client.resources.model.geometry.QuadCollection;
import net.minecraft.client.resources.model.sprite.TextureSlots;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.ItemOwner;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4fc;
import org.joml.Vector3f;
import org.joml.Vector3fc;

import vazkii.botania.common.helper.VecHelper;
import vazkii.botania.common.item.ManaBlasterItem;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

/**
 * Item model for the Mana Blaster. In older versions the per-lens/clip model switch was expressed
 * through {@code ItemOverrides}; in 26.1 the switch happens while updating the item render state
 * instead, with all variants pre-baked as {@link QuadCollection}s.
 */
public class ManaBlasterBakedModel implements ItemModel {
	private final ModelRenderProperties properties;
	private final QuadCollection gunNoClip;
	private final QuadCollection gunClip;
	private final Map<Item, QuadCollection> lensQuads;
	private final Supplier<Vector3fc[]> extents;

	ManaBlasterBakedModel(ModelRenderProperties properties, QuadCollection gunNoClip, QuadCollection gunClip,
			Map<Item, QuadCollection> lensQuads) {
		this.properties = properties;
		this.gunNoClip = gunNoClip;
		this.gunClip = gunClip;
		this.lensQuads = lensQuads;
		this.extents = com.google.common.base.Suppliers.memoize(() -> {
			var combined = new java.util.ArrayList<>(gunNoClip.getAll());
			for (QuadCollection lens : lensQuads.values()) {
				combined.addAll(lens.getAll());
			}
			return net.minecraft.client.renderer.item.CuboidItemModelWrapper.computeExtents(combined);
		});
	}

	@Override
	public void update(ItemStackRenderState output, ItemStack stack, ItemModelResolver resolver,
			ItemDisplayContext displayContext, @Nullable ClientLevel level, @Nullable ItemOwner owner, int seed) {
		boolean clip = ManaBlasterItem.hasClip(stack);
		ItemStack lens = ManaBlasterItem.getLens(stack);
		Item lensItem = lens.isEmpty() ? null : lens.getItem();
		QuadCollection lensModel = lensItem == null ? null : this.lensQuads.get(lensItem);

		output.appendModelIdentityElement(this);
		output.appendModelIdentityElement(clip);
		if (lensItem != null) {
			output.appendModelIdentityElement(lensItem);
		}

		if (stack.hasFoil()) {
			output.newLayer().setFoilType(ItemStackRenderState.FoilType.STANDARD);
			output.setAnimated();
		}

		ItemStackRenderState.LayerRenderState renderLayer = output.newLayer();
		QuadCollection gun = clip ? this.gunClip : this.gunNoClip;
		renderLayer.prepareQuadList().addAll(gun.getAll());
		if (lensModel != null) {
			renderLayer.prepareQuadList().addAll(lensModel.getAll());
		}
		renderLayer.setExtents(this.extents);
		this.properties.applyToLayer(renderLayer, displayContext);
		if (gun.hasMaterialFlag(2) || lensModel != null && lensModel.hasMaterialFlag(2)) {
			output.setAnimated();
		}
	}

	public record Unbaked(Identifier gunNoClip, Identifier gunClip) implements ItemModel.Unbaked {
		public static final MapCodec<Unbaked> MAP_CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
				Identifier.CODEC.fieldOf("gun_noclip").forGetter(Unbaked::gunNoClip),
				Identifier.CODEC.fieldOf("gun_clip").forGetter(Unbaked::gunClip)
		).apply(i, Unbaked::new));

		private static final ModelState LENS_STATE = new ModelState() {
			@Override
			public Transformation transformation() {
				return new Transformation(new Vector3f(-0.4F, 0.2F, 0.0F), VecHelper.rotateY(90),
						new Vector3f(0.625F, 0.625F, 0.625F), null);
			}
		};

		@Override
		public void resolveDependencies(ResolvableModel.Resolver resolver) {
			resolver.markDependency(this.gunNoClip);
			resolver.markDependency(this.gunClip);
			for (var item : BuiltInRegistries.ITEM) {
				// 26.1: item components are unbound during model discovery, so a default ItemStack
				// cannot be created here (see ManaBlasterItem.isValidLensItem).
				if (ManaBlasterItem.isValidLensItem(item)) {
					resolver.markDependency(ModelLocationUtils.getModelLocation(item));
				}
			}
		}

		@Override
		public ItemModel bake(ItemModel.BakingContext context, Matrix4fc transformation) {
			ModelBaker baker = context.blockModelBaker();
			ResolvedModel gunModel = baker.getModel(this.gunNoClip);
			TextureSlots slots = gunModel.getTopTextureSlots();
			ModelRenderProperties properties = ModelRenderProperties.fromResolvedModel(baker, gunModel, slots);
			QuadCollection gunNoClip = bake(baker, this.gunNoClip, BlockModelRotation.IDENTITY);
			QuadCollection gunClip = bake(baker, this.gunClip, BlockModelRotation.IDENTITY);

			Map<Item, QuadCollection> lenses = new HashMap<>();
			for (var item : BuiltInRegistries.ITEM) {
				if (ManaBlasterItem.isValidLensItem(item)) {
					lenses.put(item, bake(baker, ModelLocationUtils.getModelLocation(item), LENS_STATE));
				}
			}
			return new ManaBlasterBakedModel(properties, gunNoClip, gunClip, lenses);
		}

		private static QuadCollection bake(ModelBaker baker, Identifier id, ModelState state) {
			ResolvedModel model = baker.getModel(id);
			TextureSlots slots = model.getTopTextureSlots();
			return model.bakeTopGeometry(slots, baker, state);
		}

		@Override
		public MapCodec<Unbaked> type() {
			return MAP_CODEC;
		}
	}
}
