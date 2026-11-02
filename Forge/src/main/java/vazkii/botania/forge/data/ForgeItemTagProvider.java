package vazkii.botania.forge.data;

import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.TagsProvider;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.common.Tags;
import net.neoforged.neoforge.common.data.BlockTagCopyingItemTagProvider;

import vazkii.botania.common.block.BotaniaBlocks;
import vazkii.botania.common.helper.ColorHelper;
import vazkii.botania.common.lib.BotaniaTags;
import vazkii.botania.common.lib.LibMisc;

import java.util.concurrent.CompletableFuture;

import static vazkii.botania.common.item.BotaniaItems.*;

public class ForgeItemTagProvider extends BlockTagCopyingItemTagProvider {
	public ForgeItemTagProvider(PackOutput packOutput, CompletableFuture<HolderLookup.Provider> lookupProvider,
			CompletableFuture<TagsProvider.TagLookup<Block>> blockTagProvider) {
		super(packOutput, lookupProvider, blockTagProvider, LibMisc.MOD_ID);
	}

	@Override
	public String getName() {
		return "Botania item tags (Forge-specific)";
	}

	@Override
	protected void addTags(HolderLookup.Provider provider) {
		this.tag(forge("dusts/mana")).addTag(BotaniaTags.Items.DUSTS_MANA);
		this.tag(forge("dusts")).addTag(forge("dusts/mana"));

		this.tag(forge("gems/dragonstone")).addTag(BotaniaTags.Items.GEMS_DRAGONSTONE);
		this.tag(forge("gems/mana_diamond")).addTag(BotaniaTags.Items.GEMS_MANA_DIAMOND);
		this.tag(forge("gems")).addTag(forge("gems/dragonstone")).addTag(forge("gems/mana_diamond"));

		this.tag(forge("ingots/elementium")).addTag(BotaniaTags.Items.INGOTS_ELEMENTIUM);
		this.tag(forge("ingots/manasteel")).addTag(BotaniaTags.Items.INGOTS_MANASTEEL);
		this.tag(forge("ingots/terrasteel")).addTag(BotaniaTags.Items.INGOTS_TERRASTEEL);
		this.tag(forge("ingots")).addTag(forge("ingots/elementium"))
				.addTag(forge("ingots/manasteel"))
				.addTag(forge("ingots/terrasteel"));

		this.tag(forge("nuggets/elementium")).addTag(BotaniaTags.Items.NUGGETS_ELEMENTIUM);
		this.tag(forge("nuggets/manasteel")).addTag(BotaniaTags.Items.NUGGETS_MANASTEEL);
		this.tag(forge("nuggets/terrasteel")).addTag(BotaniaTags.Items.NUGGETS_TERRASTEEL);
		this.tag(forge("nuggets")).addTag(forge("nuggets/elementium"))
				.addTag(forge("nuggets/manasteel"))
				.addTag(forge("nuggets/terrasteel"));

		this.tag(ItemTags.SKULLS).add(BotaniaBlocks.gaiaHead.asItem());

		this.copyToSameName(ForgeBlockTagProvider.ELEMENTIUM);
		this.copyToSameName(ForgeBlockTagProvider.MANASTEEL);
		this.copyToSameName(ForgeBlockTagProvider.TERRASTEEL);
		this.copyToSameName(ForgeBlockTagProvider.MANA_DIAMOND);
		this.copyToSameName(ForgeBlockTagProvider.DRAGONSTONE);
		this.copyToSameName(ForgeBlockTagProvider.BLAZE_MESH);
		ColorHelper.supportedColors().map(ForgeBlockTagProvider.PETAL_BLOCKS::get).forEach(this::copyToSameName);
		this.copy(ForgeBlockTagProvider.MUSHROOMS, Tags.Items.MUSHROOMS);
		this.copy(ForgeBlockTagProvider.STORAGE_QUARTZ, ItemTags.create(Identifier.fromNamespaceAndPath("c", "storage_blocks/quartz")));
		this.copy(Tags.Blocks.STORAGE_BLOCKS, Tags.Items.STORAGE_BLOCKS);
		this.copy(Tags.Blocks.GLASS_BLOCKS, Tags.Items.GLASS_BLOCKS);
		this.copy(Tags.Blocks.GLASS_PANES, Tags.Items.GLASS_PANES);
		this.copy(Tags.Blocks.FENCES_WOODEN, Tags.Items.FENCES_WOODEN);
		this.copy(Tags.Blocks.FENCE_GATES_WOODEN, Tags.Items.FENCE_GATES_WOODEN);

		this.tag(BotaniaTags.Items.LENS_GLUE).addTag(Tags.Items.SLIME_BALLS).add(Items.HONEY_BOTTLE);

		this.generateToolTags();
		this.generateAccessoryTags();
		this.generateQuarkTags();
	}

	private void generateToolTags() {
		this.tag(Tags.Items.TOOLS_SHEAR).add(manasteelShears, elementiumShears);
		this.tag(Tags.Items.TOOLS_BOW).add(livingwoodBow, crystalBow);

		this.tag(ItemTags.HEAD_ARMOR).add(manasteelHelm, manaweaveHelm,
				elementiumHelm, terrasteelHelm);
		this.tag(ItemTags.CHEST_ARMOR).add(manasteelChest, manaweaveChest,
				elementiumChest, terrasteelChest);
		this.tag(ItemTags.LEG_ARMOR).add(manasteelLegs, manaweaveLegs,
				elementiumLegs, terrasteelLegs);
		this.tag(ItemTags.FOOT_ARMOR).add(manasteelBoots, manaweaveBoots,
				elementiumBoots, terrasteelBoots);
	}

	private void generateAccessoryTags() {
		tag(accessory("belt")).add(
				knockbackBelt, speedUpBelt, superTravelBelt, travelBelt
		);
		tag(accessory("body")).add(
				balanceCloak, holyCloak, invisibilityCloak, thirdEye, unholyCloak
		);
		tag(accessory("charm")).add(
				divaCharm, goddessCharm, monocle, tinyPlanet
		);
		tag(accessory("head")).add(flightTiara, itemFinder);
		tag(accessory("necklace")).add(
				bloodPendant, cloudPendant, icePendant, lavaPendant,
				superCloudPendant, superLavaPendant
		);
		tag(accessory("ring")).add(
				auraRing, auraRingGreater, dodgeRing, lokiRing, magnetRing, magnetRingGreater,
				manaRing, manaRingGreater, miningRing, odinRing, pixieRing, reachRing,
				swapRing, thorRing, waterRing
		);
		tag(accessory("curio")).add(
				blackBowtie, blackTie,
				redGlasses, puffyScarf,
				engineerGoggles, eyepatch,
				wickedEyepatch, redRibbons,
				pinkFlowerBud, polkaDottedBows,
				blueButterfly, catEars,
				witchPin, devilTail,
				kamuiEye, googlyEyes,
				fourLeafClover, clockEye,
				unicornHorn, devilHorns,
				hyperPlus, botanistEmblem,
				ancientMask, eerieMask,
				alienAntenna, anaglyphGlasses,
				orangeShades, grouchoGlasses,
				thickEyebrows, lusitanicShield,
				tinyPotatoMask, questgiverMark,
				thinkingHand
		);
	}

	private void generateQuarkTags() {
		tag(quark("big_harvest_hoes")).add(elementiumHoe);
		tag(quark("reacharound_able")).add(dirtRod, cobbleRod, blackHoleTalisman);
	}

	private static TagKey<Item> accessory(String name) {
		return ItemTags.create(Identifier.fromNamespaceAndPath("curios", name));
	}

	private static TagKey<Item> forge(String name) {
		return ItemTags.create(Identifier.fromNamespaceAndPath("forge", name));
	}

	private static TagKey<Item> quark(String name) {
		return ItemTags.create(Identifier.fromNamespaceAndPath("quark", name));
	}

	private void copyToSameName(TagKey<Block> source) {
		this.copy(source, ItemTags.create(source.location()));
	}
}
