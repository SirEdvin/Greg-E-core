# Tasks

## 1. Pixel-edited casing family and resources

- [x] 1.1 Pixel-edit black, dark purple, dark green, and very dark orange plate textures using the pinned Industrial Steam Casing reference; deliver native-resolution PNGs with fixed borders/bolts and document original/derived pixel provenance and any required attribution. Verify a nearest-neighbor contact sheet shows all four distinct dark palettes and the intended framed industrial design.
- [x] 1.2 Add compact animation strips and metadata with a 200-tick black shimmer cycle, 40-tick colored shimmer cycles, and subtly shifting colored shadow pixels. Verify frame bounds, square frame dimensions, positive durations, exact loop totals, stable borders/bolts, and changed interior shadow pixels through automated asset checks.
- [x] 1.3 Add normally shaded base plus transparent emissive detail composition using the pinned LDLib route, including casing block/item models and controller-compatible textures; verify in a shader-free native client that highlights glow in darkness, base plates remain shaded, no world light changes, and both animation cycles loop correctly. Retain visual evidence outside the repository and document the resource layout.
- [x] 1.4 Register four casing BlockItems through the existing GTRegistrate initialization with localized names, creative inventory entries, mining tags and self-drop loot. Add native tests verifying all four registrations, creative visibility, placement and correct variant drops; verify no loaded recipe produces black casing.

## 2. Black-only frame and optional parallel hatch

- [x] 2.1 Replace the tamer's UHV frame predicate, appearance block, and controller casing model with black casing, preserving the skeleton, orientation, controller overlay, independent five-per-category item/fluid limits, and energy-hatch rejection. Update the shared `WorldEaterStructureTests` fixture and all sibling casing placements; verify formation in supported orientations and rejection of UHV, each colored variant, and missing edges/corners.
- [x] 2.2 Add a maximum-one optional standard `PartAbility.PARALLEL_HATCH` substitution without changing other part limits. Add native tests proving zero/one hatch forms in either progression state, two fail, and no energy part is required; verify controller and formed-part black appearances in the client.
- [x] 2.3 Document the intentional old-frame incompatibility and creative-only black acquisition, retaining controller ID and valid saved progression. Verify a saved controller can reform after replacing its UHV frame with black and a pending completed report is delivered once after rebuild without reset or duplication.

## 3. State-dispatched native processing and exact recipes

- [x] 3.1 Separate common required-state checks from strict survey acceptance in `WorldEaterTamerRecipeLogic`, and delegate ACTIVE conversion setup/ticking/completion to native recipe processing without initializing a scan. Add regressions for missing/unknown/mismatched state metadata and verify no inputs are consumed on rejection; retain the existing strict survey operation/duration/no-tick/multiplier guards.
- [x] 3.2 Audit and adapt suspension/resumption, reset/interruption, save/load and UI progress for native ACTIVE jobs while preserving valid version-1 survey/progression data and corrupt-data rejection. Verify mid-craft save/load and disable/enable resume correctly, preserve ACTIVE, charge inputs once, deliver once, and show no scan chunk, survey progress, report, or discovery particles.
- [x] 3.3 Increase `WORLD_EATER_TAMER` item input capacity to two and register purple, green, and orange conversion recipes in `GregEGTAddon.addRecipes`, each explicitly ACTIVE with a distinct non-survey operation, one black casing plus 16 matching vanilla terracotta, one output, 100 ticks and no EU/fluid capability. Verify loaded definitions and real formed-machine execution for every color, wrong-color rejection for each selected recipe, missing casing, insufficient terracotta, DORMANT rejection, and output blockage before/during completion.
- [x] 3.4 Document exact recipes, state gating, zero-power behavior, and normal ACTIVE progress display alongside their implementation. Verify the documented recipe table matches loaded recipe definitions and client recipe-viewer information exposes required state and correct input/output counts.

## 4. State-sensitive hatch parallel processing

- [x] 4.1 Add a narrow controller recipe modifier delegating only valid ACTIVE casing conversions to the pinned native parallel-hatch modifier, returning identity for the survey and adding no overclock/batch modifiers. Verify no-hatch single processing and hatch-enabled multi-processing both take 100 ticks with exact scaled payment/output and no power requirement.
- [x] 4.2 Add native parallel-limit tests for hatch current setting, input-limited parallels, output-limited parallels and output blockage; verify only complete payable input sets are consumed, no overflow or loss occurs, and ACTIVE persists across parallel craft completion and save/load.
- [x] 4.3 Add a high-capacity-hatch DORMANT survey regression using surplus IV sensors; verify one consumed sensor, unchanged 2400 scanning ticks when chunks remain available, one report and the existing activation threshold. Separately verify pre-multiplied parallel/batch/subtick surveys reject without input consumption, rather than allowing the hatch to disable legitimate single surveys.
- [x] 4.4 Document that one standard hatch parallelizes ACTIVE casing conversions only, while activation remains single-run; verify the machine tooltip/help and implementation agree and the existing survey retry, output-delivery and non-loading tests remain passing with updated black fixtures.

## 5. End-to-end acceptance

- [x] 5.1 Discover available tasks with `./gradlew tasks --all --no-daemon`, then run production build, GameTest source compilation and the full relevant native World Eater suite using the declared Forge runs. Verify fresh reports contain the expected tests with zero failures, check packaged resources and absence of test fixtures in the production JAR, and run `git diff --check`; do not infer gameplay success solely from Gradle exit zero.
- [x] 5.2 Run opt-in client acceptance in an isolated disposable world: inspect all four item/block variants in lit/dark scenes through complete cycles, controller and formed-part appearance, resource reload, one ACTIVE conversion of each color, parallel conversion, and a hatch-installed activation survey. Verify fresh machine-readable results and retain screenshots/animation evidence outside the repository; document actual passing checks and any unverified environment limitations.

## 6. Connected-texture correction

- [x] 6.1 Add synchronized native LDLib CTM sheets and connection metadata for all four base/emissive pairs. Verify horizontal/vertical strips, filled and L-shaped panels, unlike-color boundaries, black controller/formed-part adjacency and resource reload with native baked-quad assertions and screenshots. Document the corrected behavior; retain the existing animation timing and gameplay contracts.

- [x] 6.2 Correct sampled CTM pixels: extrude half-connected edge profiles without standalone corner end-caps, use a seamless 16px-periodic shadow field, retain version-correct missing-diagonal corners, and test the user's 4×2 wall plus strips through every frame. Verify actual native baked UV perimeter pixels for all colors and all six faces before/after reload and inspect a close-up native green-wall capture. Update the prior acceptance record to disclose the false-positive sprite-selection checks.

## Workflow follow-up

- Before archiving, reconcile the completed `add-incomplete-world-eater-tamer` change with the empty main spec inventory, preserving its full survey contract and converting overlapping delta operations as needed once the baseline exists. Do not silently archive or overwrite it during implementation.
- Archive this change only after explicit workflow authorization and the project's review requirements are met, then verify the resulting main specs describe black-only frames and protected activation.
