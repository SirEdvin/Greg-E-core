# World Eater demand-run verification

Verified November 4, 2026 on Minecraft 1.20.1 / Forge 47.4.10 / GTCEu 7.5.2 / LDLib 1.0.40.b, using the project's Java 17 toolchain. Gradle itself runs under the installed Java 21 JVM. Branch: `feat/add-world-eater-demand-runs`.

## Results

- Production build and GameTest compilation passed. The wrapper is not executable in this checkout, so commands use `bash gradlew --no-daemon` without changing its mode.
- Dedicated-server verification: **58 discovered, 58 passed, zero failures or errors**. Parsed the final XML and compared its case-name multiset with all source `@GameTest` methods: no missing, unexpected, or duplicate discoveries. This includes native server loading, legacy survey/casing/structure/intention tests, and actual chunk eviction.
- Existing Python casing-asset regressions: **8 passed**.
- Final native client verification exited zero and its fresh report has `passed=true`, `failures=[]`, `nativeUI=true`, `pagesChecked=103`, and all intention recipe reload/resource reload/reconnect and demand resource reload/reconnect flags true.
- All **15 demand screenshots** exist. Inspected actual captures, not just the report: icon/count/full long name in four orientations, post-reload and post-reconnect scenes, PREPARED and pending-demand/pending-reward UI, both exact JEI recipes, all three material forms, and cleared displays after cancellation/timeout. Existing intention checks also retain dominant-color/connected formed-part appearance across reload/reconnect.
- `git diff --check` and strict validation pass. Strict validation reports the expected archive-baseline information: synchronize/archive `add-world-eater-intention-hatches` before archiving this dependent change.
- Final JAR contains the new production pool/material registrations and the exact generated language resource; no GameTest fixture or client-check class is packaged. No dependency, build configuration, or high-tier configuration changes were made.

## Commands and isolation

The final combined production/server run was:

    export WORLD_EATER_TEST_DIR="$(mktemp -d -p "$TMPDIR" world-eater-demand-server-XXXXXXXX)"
    cp "$TMPDIR/world-eater-demand-server/server.properties" "$WORLD_EATER_TEST_DIR/server.properties"
    JAVA_TOOL_OPTIONS=-Xmx8G bash gradlew --no-daemon \
      -I "$TMPDIR/world-eater-demand-verification.gradle" \
      compileJava compileGameTestJava build runGameTestServer

The scratch init script sets the actual ForgeGradle run configuration after project evaluation:

    gradle.projectsEvaluated {
        gradle.rootProject.minecraft.runs.gameTestServer.workingDirectory(
            new File(System.getenv('WORLD_EATER_TEST_DIR')))
    }

The final server world was `world-eater-demand-server-zsrEnC5O` under profile scratch. It used fresh flat terrain, not a reused absolute-coordinate fixture world. Its server properties were:

    level-name=world
    level-type=minecraft:flat
    generator-settings={"layers":[{"block":"minecraft:bedrock","height":1},{"block":"minecraft:dirt","height":2},{"block":"minecraft:grass_block","height":1}],"biome":"minecraft:plains","structures":{}}
    generate-structures=false

The final client run used the existing project entrypoint:

    JAVA_TOOL_OPTIONS=-Xmx8G LIBGL_ALWAYS_SOFTWARE=1 \
      xvfb-run -a -s '-screen 0 1280x900x24' \
      bash gradlew runClient --no-daemon \
      -PworldEaterClientVerification -PworldEaterClientWorld="$WORLD"

`WORLD` was `albina-world-eater-verification-demand-e0018845-0956-46e5-a7a2-913790c622d4`. Only `level.dat` was copied from an existing disposable verification seed; no old region/entity/POI data was reused. Previous client reports and worlds were preserved, and no play world was used.

Additional checks:

    PYTHONDONTWRITEBYTECODE=1 python -m unittest discover -s src/gameTest -p 'test_*.py'
    openspec validate add-world-eater-demand-runs --strict
    git diff --check

## Acceptance evidence

- `WorldEaterDemandMaterialTests`: native generated Desarium/Implementium gems and essence ingot, names/creative exposure, required gear/long-rod/rotor forms, and native processing outputs. Source review confirms no new token acquisition or world-generation source.
- `WorldEaterDemandPoolTests`: fixed material mapping, supported family/tier bounds, optional component removal and individual resolution through a controlled lookup, no duplicate weighting, and tier deadlines. The runtime configuration was not changed to force optional registrations; absent/partial resolver cases complement the development registry rather than claiming a separate production install was launched.
- `WorldEaterDemandTests`: native recipe discovery and serialization, state/tier/intention and invalid metadata gates, no candidate-check payment/reroll, empty pool and blocked-start rejection, single token with parallel hardware, exact split payment across distinct buses, wrong form/tier and partial-payment rejection, final-tick success, one reward, and permanent PREPARED compatibility with casing/Bravery crafting.
- Persistence tests cover suspension, native NBT reload, corrupt count/tier/deadline/operation/item/reward fields, optional-item absence, and exactly-once blocked reward delivery after structural reset. `WorldEaterUnloadTests.demandAndEarnedRewardSurviveRealEviction` verifies an actual unload event plus an absent non-loading cache entry before restoring fresh controllers, both waiting for demand and holding earned output.
- Consequence tests cover harmless tests, real frame/hatch removal callbacks, cancellation/lost eligibility, no token refund or partial demand loss, stale callbacks, Safety-only destruction, random-origin nearest selection/stable ties, loaded square/build-height boundaries, other-controller eligibility, part/self exclusion, empty/unloaded regions without new tickets, lost-victim revalidation, and unaffected neighboring blocks/parts/entities.
- `WorldEaterDemandClientChecks` executes real server-side token payment, synchronizes a deterministic longest-name demand for visual coverage, then verifies native world rendering, pause/reconnect/reload, payment, blocked reward, cancellation and timeout cleanup. JEI is opened by the exact registered category and recipe; all tamer captions are measured against native panel width, reserving room for controls.

## Defects caught during verification

- Old server worlds contaminated absolute-coordinate fixtures. Overriding a task's working directory/`user.dir` was insufficient and could disagree with native relative paths. Configuring `minecraft.runs.gameTestServer.workingDirectory` and using fresh flat worlds resolved isolation; all old regressions passed afterward.
- Boundary victims needed setup force tickets to remain resident for loaded-chunk assertions. Production target search still uses only non-loading chunk reads; a separate real-eviction test verifies no ticket/load is introduced.
- The initial client report passed while screenshot inspection exposed invisible world-space text and overflowing JEI captions. Following vanilla camera-facing text's negative X/Y scale, increasing readable glyph size with bounded wrapping, and shortening/measuring captions fixed both. A new native caption-width assertion failed on an intermediate caption before passing on the final text.
- A completed resource-reload future did not mean its loading overlay had disappeared. The fixture now waits for the overlay to clear and rendered frames to settle, and rejects screenshots taken under an overlay.
- An added corrupt-save regression failed because native item decoding narrowed a fractional count. The loader now validates the saved item field types and requires the canonical empty item representation for an earned reward; malformed data cancels harmlessly without revoking PREPARED or fabricating output. The final full suite passes all corruption cases.

## Artifact and evidence

Production JAR:

    /home/siredvin/projects/Greg-E-core/build/libs/gregecore-1.12.7-1.20.1.jar

Size: **930756 bytes**.

SHA-256:

    8e29743de24e5f3f1b44d54595be49fe986de587b923cd1909ec89499d78b94c

Evidence root:

    /home/siredvin/.hermes/profiles/albina/cache/scratch/

- `world-eater-demand-suite-final.log`: final production build and dedicated-server run.
- `world-eater-demand-server-final.xml`: preserved final 58-test report, separate from reports an integrated-server startup may replace.
- `world-eater-demand-client-final.log` and `world-eater-client/result.json`: final native client execution and assertions.
- `world-eater-client/screenshots/demand-*.png`: final 15 captures, including `demand-resource-reload.png`, `demand-reconnect.png`, `demand-active-ui.png`, `demand-earned-ui.png`, and both `demand-*-recipe.png` views.
- Earlier failed runs and pre-correction captures are not acceptance evidence. Scratch files can expire; this handoff retains the acceptance mapping and artifact checksum.

## Limits and follow-up

The headless audio device is unavailable, so audible explosion playback was not verified; normal native sound dispatch is implemented. Optional shaders, third-party handlers that violate simulation guarantees, and a separate high-tier-disabled production installation were not exercised. No deployment, version bump, commit, push, or archival was performed.

Back up worlds before deployment. Token acquisition remains the modpack's responsibility. Downgrading after introducing PREPARED/new material IDs requires the matching older world backup. Archive the intention-hatch predecessor first when archival is requested.

## Follow-up: queued runs and static bloom — November 4, 2026

The original results above describe the published feature baseline. This follow-up
fixes automatic restart and replaces the animated casing artwork; the current
appearance is documented in `docs/world-eater-casings.md`.

- Reproduced two failing native-tick regressions before the logic fix: a second
  queued Implementium run never started, and preparation did not discover a queued
  real run. Successful custom completion used the abort-style native reset, which
  dropped polling. Marking the recipe dirty after reset restores native discovery
  without manual searches or another scheduler; the survey completion sibling
  receives the same fix. Suspend-after-finish is still restored afterward.
- Fresh dedicated-server run: **60 discovered, 60 passed, no failures or errors**.
  The source and XML test-name multisets match exactly. New coverage drives only
  inventory changes and normal subscribed ticks through three successive real runs,
  preparation-to-real transition, blocked earned output, exactly-once delivery,
  suspend-after-finish and explicit resume. Existing interruption, persistence,
  output retention and bounded-consequence tests all pass.
- Static/bloom asset regressions failed against the old resources, then **all 8
  tests passed** after regeneration. All source/CTM layers are single-frame, have no
  animation metadata and retain native connected textures. Only emissive outlines
  opt into Shimmer bloom; the plate base stays shaded.
- Two fresh, fully isolated native client runs passed with empty failure lists,
  successful gameplay and resource reload: without Shimmer (**127 actual captures**)
  and with Shimmer **1.20.1-0.3.0**, Modrinth version **BeAfFYQQ** (**75 actual
  captures**). SHA-512 was checked against the version metadata before execution.
  The Shimmer run verified **404 native baked-quad bloom flags** across standalone
  and connected faces before/after reload. Lit, dark and close perimeter screenshots
  were inspected; optional post-processing visibly adds a halo without internal
  panel seams. This is steady bloom, not a timed pulse or world-light emission.
- Final `bash gradlew --no-daemon compileJava compileGameTestJava build` passed.
  All **32** packaged casing PNG/metadata files byte-match the working resources.
  No fixture classes, Python scripts or Shimmer classes are embedded. Shimmer was
  added only by a scratch init script to the verification runtime; production
  dependencies and `build.gradle` are unchanged. `git diff --check` passed.

Evidence is under
`/home/siredvin/.hermes/profiles/albina/cache/scratch/world-eater-followup-3G3zwUTR/`:
`server-red.log`, `server-green.log`, preserved `server-green.xml`, `assets-red.log`,
`build-final.log`, `client-verification.gradle`, both `client-*-shimmer.log` files,
the corresponding client directories and `native-casing-comparison.png`.
The client init script overrides the native client run's working directory and
adds runtime-only Shimmer when `-PworldEaterBloomVerification` is passed; both runs
use `-PworldEaterClientVerification -PworldEaterCasingVerification` with a guarded
disposable world name. No play worlds or normal run configuration were changed.

Current artifact: `build/libs/gregecore-1.12.7-1.20.1.jar`, **924266 bytes**, SHA-256
`7bf1ab10edede6f4c322604534338989387a57830c9d33d7c5ad8930f458d4ec`.
This follow-up did not rerun the separate 103-page/demand-UI client scenario,
exercise third-party shader packs, or verify audio. It is not committed, pushed,
deployed or archived.

## Follow-up: tier-two controller appearance

The controller now has a native GTCEu `casing_color` model-state property with
black, purple, green and orange values. Formation publishes the same dominant
frame color already used by parts; invalidation restores black. Generated
variants combine color, formation and recipe status, preserving front/status
overlays, the static emissive layer and the existing dynamic demand renderer.
The controller's connected-texture appearance follows the same color and still
honors cover appearance overrides. No block ID, progression or recipe changes
were introduced by this follow-up.

Verification:

- Before the implementation, the resource test failed on the absent color state
  and three native server tests failed on missing registration/synchronization.
- Final build plus dedicated-server run: **60/60 passed**, with exact source/XML
  discovery equality. Extended tests cover all four rotations and casing colors,
  native majority/tie transitions, downgrade, invalidation, repair and reload.
  Legacy compatibility is checked by encoding an orange state, removing the new
  property and requiring the native codec to restore the black default.
- **9 asset tests passed**, including all **32** color/formation/status model
  variants and their exact base, glow and workable-overlay textures.
- Full native clients passed both without Shimmer and with pinned Shimmer
  `1.20.1-0.3.0` (`BeAfFYQQ`): empty failure lists, **103 pages**, native UI, and
  all intention/demand reload and reconnect flags true. Each run produced all
  **6 controller captures**, covering every color, a mixed green/orange tie and
  reconnect. Native assertions check color state, base/glow/overlay quads,
  matching/unlike CTM neighbors and optional Shimmer bloom flags. Inspected the
  color comparison and post-reconnect captures; no stale black controller base.
- Native datagen completed. Its unrelated language/cache changes were restored
  from the pre-run snapshot, preserving hand-authored names and translations.
  The only generated-resource change is the controller model. The packaged
  model, language and all casing resources byte-match the verified working files;
  no fixture or optional Shimmer classes are packaged. `git diff --check` passed.

Evidence:
`/home/siredvin/.hermes/profiles/albina/cache/scratch/world-eater-controller-ntvT6BLO/`
contains `server-red-runtime.log`, `assets-red.log`, `datagen.log`,
`server-final.log`, `server-final.xml`, `client-without-shimmer-final.log`,
`client-with-shimmer.log`, corresponding fresh client reports/screenshots,
`controller-colors.png` and `artifact.json`. The earlier client failure was an
overstrict fixture assumption that a dominant-colored controller must connect
even when its immediate neighbors have another color; corrected checks compare
the actual native connection topology instead.

This artifact supersedes the checksum above: `build/libs/gregecore-1.12.7-1.20.1.jar`,
**925297 bytes**, SHA-256
`a0706ca5d13ccf8017c1f5c204a83522861b445b1d03f99d2b4ddd41fd65c7e6`.
Third-party shader packs and audio remain untested. Changes remain local:
no commit, push, deployment or archival.
