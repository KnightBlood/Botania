package vazkii.botania.client.render;

import net.minecraft.client.renderer.chunk.ChunkSectionLayer;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.FlowerBlock;
import net.minecraft.world.level.block.FlowerPotBlock;
import net.minecraft.world.level.block.TallFlowerBlock;

import vazkii.botania.common.block.BotaniaBlocks;
import vazkii.botania.common.block.decor.BotaniaMushroomBlock;
import vazkii.botania.common.block.decor.FloatingFlowerBlock;
import vazkii.botania.common.lib.LibMisc;

import java.util.function.BiConsumer;

public final class BlockRenderLayers {
	public static boolean skipPlatformBlocks;

	public static void init(BiConsumer<Block, ChunkSectionLayer> consumer) {
		consumer.accept(BotaniaBlocks.defaultAltar, ChunkSectionLayer.CUTOUT);
		consumer.accept(BotaniaBlocks.forestAltar, ChunkSectionLayer.CUTOUT);
		consumer.accept(BotaniaBlocks.plainsAltar, ChunkSectionLayer.CUTOUT);
		consumer.accept(BotaniaBlocks.mountainAltar, ChunkSectionLayer.CUTOUT);
		consumer.accept(BotaniaBlocks.fungalAltar, ChunkSectionLayer.CUTOUT);
		consumer.accept(BotaniaBlocks.swampAltar, ChunkSectionLayer.CUTOUT);
		consumer.accept(BotaniaBlocks.desertAltar, ChunkSectionLayer.CUTOUT);
		consumer.accept(BotaniaBlocks.taigaAltar, ChunkSectionLayer.CUTOUT);
		consumer.accept(BotaniaBlocks.mesaAltar, ChunkSectionLayer.CUTOUT);
		consumer.accept(BotaniaBlocks.mossyAltar, ChunkSectionLayer.CUTOUT);
		consumer.accept(BotaniaBlocks.ghostRail, ChunkSectionLayer.CUTOUT);
		consumer.accept(BotaniaBlocks.solidVines, ChunkSectionLayer.CUTOUT);

		consumer.accept(BotaniaBlocks.corporeaCrystalCube, ChunkSectionLayer.TRANSLUCENT);
		consumer.accept(BotaniaBlocks.manaGlass, ChunkSectionLayer.TRANSLUCENT);
		consumer.accept(BotaniaBlocks.managlassPane, ChunkSectionLayer.TRANSLUCENT);
		consumer.accept(BotaniaBlocks.elfGlass, ChunkSectionLayer.TRANSLUCENT);
		consumer.accept(BotaniaBlocks.alfglassPane, ChunkSectionLayer.TRANSLUCENT);
		consumer.accept(BotaniaBlocks.bifrost, ChunkSectionLayer.TRANSLUCENT);
		consumer.accept(BotaniaBlocks.bifrostPane, ChunkSectionLayer.TRANSLUCENT);
		consumer.accept(BotaniaBlocks.bifrostPerm, ChunkSectionLayer.TRANSLUCENT);
		consumer.accept(BotaniaBlocks.prism, ChunkSectionLayer.TRANSLUCENT);

		consumer.accept(BotaniaBlocks.starfield, ChunkSectionLayer.CUTOUT);
		if (!skipPlatformBlocks) {
			// Render type is set dynamically on Forge and undisguised platforms should render as "solid",
			// but "translucent" is the best compromise on Fabric.
			// Translucent comes with a couple of downsides, like hidden block breaking animation and bad
			// Z-ordering for non-cubic block models that should be rendered with the "cutout" render type.
			consumer.accept(BotaniaBlocks.abstrusePlatform, ChunkSectionLayer.TRANSLUCENT);
			consumer.accept(BotaniaBlocks.infrangiblePlatform, ChunkSectionLayer.TRANSLUCENT);
			consumer.accept(BotaniaBlocks.spectralPlatform, ChunkSectionLayer.TRANSLUCENT);
		}
		BuiltInRegistries.BLOCK.stream().filter(b -> BuiltInRegistries.BLOCK.getKey(b).getNamespace().equals(LibMisc.MOD_ID))
				.forEach(b -> {
					if (b instanceof FloatingFlowerBlock || b instanceof FlowerBlock
							|| b instanceof TallFlowerBlock || b instanceof BotaniaMushroomBlock
							|| b instanceof FlowerPotBlock) {
						consumer.accept(b, ChunkSectionLayer.CUTOUT);
					}
				});
	}

	private BlockRenderLayers() {}
}
