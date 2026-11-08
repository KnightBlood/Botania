# Contribution Guide
## Reporting Issues
* Have a look through the [Botania FAQ](https://botaniamod.net/faq.html), to see if your
  issue has been solved already
* Issues regarding "Bukkit+Forge" servers that are not reproducible with Forge only are
  **not accepted**
* Issues regarding outdated versions of the mod, especially for older versions of
Minecraft, are **not accepted**
* Duplicate issues or issues that have been solved already (use the search feature!) will
  be closed without warning.
* Do not tag your issues' names. "Something Broke" is preferred to "[Bug] Something Broke"
  because there's a proper label system in place.
* Suggestions are **not** accepted. In general, new features are added to Botania on
  a must-have basis. The bar for new features is quite high.

## Submitting Changes
* Run scripts/gitconfig.sh from the repo root to set some useful git defaults locally
* Run the `checkSyntax` Gradle task to make sure your changes pass our style guidelines
* The `spotlessJavaApply` Gradle task can fix most violations for you.
* Target the default branch of the repository in all PR's (unless it's a hotfix that only applies to an older version).
  The maintainers will cherry-pick things back ourselves when creating hotfixes.
* **Keep PR's small**. The smaller it is, the faster we will review it. Only fix one thing
  per PR instead of piling everything into one massive PR.
* Consider the patch workflow to help keep your changes small.  If you hate GitHub's PR
  model like [some of the maintainers
  do](https://www.vincent-lee.net/blog/2022-02-28-github/), feel free to email patches to
  `~williewillus/violet-moon@lists.sr.ht`. See https://git-send-email.io to set up your
  Git environment for mailing patches and https://git-rebase.io for help with the
  rebase-oriented workflow.  Advanced users might like the
  [branchless](https://github.com/arxanas/git-branchless) workflow which pairs
  particularly well with the patch workflow.
* Please write descriptive commit messages with proper separation between the title and
  summary of the message. Do not write commit messages consisting only of "Fix #123", that
  means nothing to e.g. someone reading the logs offline without access to GitHub.
* If you fix a gameplay bug or add a new gameplay feature, consider adding a GameTest for
  it (see below)
* If applicable, add an entry to the changelog in `web/changelog.md` under the "Upcoming"
  section.

## Branching Discipline (for maintainers)
Each major Minecraft version has a dedicated Git branch, the primary support version is marked
as the default on web UI's. All new changes should be targeted to the primary support branch,
and backported as necessary to older branches via cherry-picking.

As much as possible, we *do not* use merge commits. They clutter the log and make
bisecting very annoying. When obtaining remote changes, use `git pull origin <branch>
--rebase`.  When merging PR's, use the "Rebase and Merge " or "Squash and Merge"
options.  For the latter, GitHub will usually generate a commit message concatenating all
the individual commit messages together, you will likely want to consider writing a better
message.

## Making a Release (for maintainers)
1. Pull from remote, test all changes, and commit everything.
2. Make sure the changelog in `web/changelog.md` is up to date, then change the "Upcoming"
   version of the section to the version to be released, and start a new "Upcoming"
   section. Commit this.
3. Run `git tag -a release-<VERSION>`. All Botania versions *must* follow the version
   format `<MC-VER>-INT`, so it'll probably look like `git tag -a release-1.16.3-407`.  If
   you don't remember which version is next, look at the `build_number` in
   `gradle.properties`.
4. Increment the `build_number` in `gradle.properties` to the next version
   (one greater than the version you just tagged). Commit this.
5. Push the branch and the tag you just made: `git push origin <branch> <release tag>`
6. CI should automatically build and upload the release to GitHub releases, CurseForge, and Modrinth. Double check
   those places to make sure it made it there properly. The release script is at `scripts/upload_releases.sh` if you need
   to debug. Note that if the required Java version ever changes, the script will need adjusting.
7. Announce the release on the Violet Moon forums under the `Botania` major tag and `sns-mirror` minor tag, which will mirror
   the announcement to Discord's #mod_updates channel and the Violet Moon Twitter account.

## Working with GameTest

> **26.1 status:** all 23 test classes / 95 cases compile and are discovered by
> `fabric-gametest-api-v1`. `./gradlew :Fabric:runGameTest` reports
> **all 95 required tests passed**. Getting there surfaced two real 26.1 product bugs,
> both fixed in the port:
> 1. **Mana burst movement truncation.** 26.1 `ThrowableProjectile.tick` truncates a
>    projectile's movement at the first hit point and only dispatches the hit afterwards,
>    so a burst spawned inside its source spreader never left the block. `ManaBurstEntity`
>    reimplements the projectile tick movement (VanillaCopy) to restore the 1.20.1
>    semantics: dispatch the hit, but always advance the full delta movement.
> 2. **Container drops on break.** 1.20.1 `Block#onRemove` container drops were (wrongly)
>    ported into `affectNeighborsAfterRemoval`, which runs *after* the block entity is
>    already removed, so breaking any of ~13 container blocks silently deleted their
>    contents. The drops live in `BlockEntity#preRemoveSideEffects` now (26.1's drop hook,
>    called before removal); the apothecary's scoop-water drop is replicated in
>    `setFluid`, because 26.1 fires no removal hook for same-block state changes.
>
> Also fixed along the way: `Entity#getLootTable()` returns a non-null `Optional` in 26.1
> and shadowed the container-vehicle check (use `getContainerLootTable()`), and
> `MobEffects.*` fields are `Holder<MobEffect>` (a `MobEffect == Holder` comparison
> compiles but is always false). No test was disabled, weakened or relabelled to make the
> suite look green. See `.workbuddy/memory/` for the full diagnosis log.
>
> Two 1.20.1 -> 26.1 convention changes are already handled:
> 1. **Structure origin.** 1.20.1 placed the template at `structureBlockPos.above()`, so a
>    structure coordinate `(x, y, z)` appeared at test-relative `(x, y + 1, z)`. In 26.1
>    `GameTestInfo.getTestOrigin()` *is* `TestInstanceBlockEntity.getStartCorner()`, i.e. the
>    exact place position, so the two coordinate systems are identical. All 32 `.snbt`
>    structures were shifted up by one on Y (blocks, entities and `size`) to restore the
>    original test coordinates. Re-exporting a structure from 26.1 produces the new layout,
>    so do **not** apply this shift again to freshly exported files.
> 2. **Default timeout.** 1.20.1 `@GameTest.timeoutTicks` defaulted to 100; Fabric 26.1's
>    `@GameTest.maxTicks` defaults to 20. All 71 annotations that relied on the old default now
>    state `maxTicks = 100` explicitly; the 22 annotations with an explicit timeout keep it.
>
> Keep `DataVersion: 3465` in the shipped `.snbt` files: 26.1's
> `DataFixTypes.STRUCTURE.updateToCurrentVersion` is required to upgrade the 1.20.1 block
> entity data. Setting it to the current world version (4786) to skip DFU was measured to make
> things worse (32 failures) and produces `Block-attached entity at invalid position` errors.
>
> Also note: 26.1 removed the old `batch` grouping and `@BeforeBatch`/`@AfterBatch` hooks.
> `BoreLensTest`'s per-batch config mocks are installed/restored inside the test sequence;
> the nine concurrently running cases serialize their config-mock windows through a static
> flag (`anotherCaseActive`), so the too-low-harvest-level cases keep real coverage.

1. Create a structure if wanted:
   1. Run `/test create <size>` to generate a test platform of the desired size. IMPORTANT:
      All tests should size themselves appropriately to ensure they don't interfere with
      other tests. E.g. a test testing a block with max radius 8 should leave at least 8
      blocks of room on all sides.
   2. Build the test setup then save the structure, *without a namespace*. Then run `/test
      export <thename>`.
   3. The command will print the location of the saved `.snbt` file in
      `run/gameteststructures`.
   4. Copy the file to `src/main/resources/data/botania/gametest/structure` (26.1: singular).
2. Create a class in `Fabric/src/gametest/java/vazkii/botania/test`. Fill it with methods
   annotated with Fabric's `@GameTest`, see the other tests for examples.
3. List the class in the `fabric-gametest` entrypoint in Botania's `fabric.mod.json`.

Tips:
* 26.1's `@GameTest` has no `batch` argument. Batch isolation was replaced by the
  `TEST_ENVIRONMENT` registry; all ported cases currently use `minecraft:default`.
* Please keep 5 blocks of padding in the N/S/E/W directions of all tests that have mana
  pools, spreaders, or flowers in them. This prevents them interfering with the tests
  about seeing whether flowers bind to the closest spreader, if Gametest happens to put
  them next to each other.
* If your test has a *lot* of air blocks in it, use structure voids to keep the filesize
  down. If you forget, run a regex find-and-replace on the `snbt` file, replacing
  `.*minecraft:air.*\n` with the empty string.

To run tests in-game: open the game normally, make sure you're not standing near anything
you care about, and use `/test runall`. (Gametest uses a default superflat with Generate
Structures, doMobSpawning, and doWeatherCycle all disabled as its testing environment.)

To run tests headlessly (all of these do the same thing):
* Use the `Minecraft Game Test` run configuration in your IDE.
* Launch the server with the argument `-Dfabric-api.gametest=1`.
* Use `./gradlew :Fabric:runGameTest`. (GitHub Actions does this)

After running tests headlessly, the testing world will be saved to `run/world`. Copy this
directory into `run/saves` and it will appear as a save file in singleplayer.
