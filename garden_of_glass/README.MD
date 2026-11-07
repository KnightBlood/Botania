Garden of Glass
---

This subdirectory contains Garden of Glass, Botania's skyblock mode.

This is nothing more than a dressed-up datapack. It contains recipe and other overrides in its /data, but otherwise has no code at all.

The code side of GoG lives in Botania itself. Botania detects this mod by the id `gardenofglass` (see `BotaniaAPI.GOG_MODID`), so the jar must be a real mod on both loaders, not just a plain datapack dropped into a world.

To build, run `make` in this directory to produce a jar file that can be dropped into `mods/`.

MC 26.1 layout
---

The pack targets Minecraft 26.1.2 and follows the 26.1 datapack conventions:

- Singular registry directories: `recipe/`, `advancement/`, `loot_table/`, `tags/block/`, `tags/item/` (`worldgen/world_preset/` is unchanged).
- `pack.mcmeta` declares `min_format: [101, 1]` / `max_format: 101` instead of the old `pack_format: 8`.
- Item stacks use `{"id": ...}`; ingredients, recipe keys and mana infusion inputs are plain item ids; Botania's mana infusion output is `{"id": ...}`.
- Item predicates use `items` (tag references are `"#namespace:tag"`); the 1.20.1 `tag` field no longer exists.
- The Fabric manifest is `fabric.mod.json`; the NeoForge manifest is `META-INF/neoforge.mods.toml` with `modLoader="javafml"`. NeoForge 26.1 removed `lowcodefml`; a `javafml` mod with no `@Mod` class is still loaded and supplies the mod id that Botania's GOG detection needs.

Contents
---

- 9 recipes (5 crafting, 4 `botania:mana_infusion`).
- 5 recipe advancements, 2 loot tables (`extra_seeds`, `pebbles`).
- `gardenofglass:pebble_sources` block tag and `gardenofglass:shears` item tag.
- The `gardenofglass:gardenofglass` world preset, registered in `minecraft:normal` so it shows up in world creation; its generator is `botania:skyblock`, registered by Botania on both loaders.

Verification
---

`make` produces `gardenofglass-1.13.jar`; drop it into `mods/` and start a dedicated server
(`:Fabric:runServer` / `:NeoForge:runServer`, both pass `nogui`). Verified on MC 26.1.2:
`gardenofglass` is discovered as a mod on both loaders, the pack is auto-enabled,
`Loaded 2826 recipes` (2817 Botania + 9 GoG) with no parse errors, and a world created
from the `gardenofglass:gardenofglass` preset starts without errors.

26.1-specific fixes worth keeping in mind: `minecraft:grass` was renamed to
`minecraft:short_grass` (the `extra_seeds` loot table condition), item predicates use
`items` instead of `tag`, and neither loader ships `c:shears` / `forge:shears` any more,
so `gardenofglass:shears` lists `minecraft:shears` explicitly.
